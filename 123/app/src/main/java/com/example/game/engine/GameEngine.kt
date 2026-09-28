package com.example.game.engine

import com.example.audio.GameAudioManager
import com.example.game.model.ArenaDef
import com.example.game.model.ArenaId
import com.example.game.model.ArenaRegistry
import com.example.game.model.DamageNumber
import com.example.game.model.ElementType
import com.example.game.model.EnemyEntity
import com.example.game.model.EnemyType
import com.example.game.model.FloatingCoin
import com.example.game.model.GameMode
import com.example.game.model.GroundZone
import com.example.game.model.GroundZoneType
import com.example.game.model.HeroDef
import com.example.game.model.HeroProgress
import com.example.game.model.Particle
import com.example.game.model.PlayerState
import com.example.game.model.Projectile
import com.example.game.model.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameEngine(
    val heroDef: HeroDef,
    val heroProgress: HeroProgress,
    val arenaDef: ArenaDef,
    val gameMode: GameMode,
    private val audioManager: GameAudioManager
) {
    val arenaWidth = 600f
    val arenaHeight = 900f
    private val minX = 40f
    private val maxX = 560f
    private val minY = 60f
    private val maxY = 840f

    val player = PlayerState().apply {
        resetForBattle(heroDef, heroProgress, Vector2(300f, 650f))
    }

    val enemies = mutableListOf<EnemyEntity>()
    val projectiles = mutableListOf<Projectile>()
    val particles = mutableListOf<Particle>()
    val damageNumbers = mutableListOf<DamageNumber>()
    val floatingCoins = mutableListOf<FloatingCoin>()
    val groundZones = mutableListOf<GroundZone>()

    var currentWave = 1
    val maxStoryWaves = 5
    var waveState = WaveState.IN_PROGRESS
    var waveBannerTimer = 0f
    var waveBannerText = "WAVE 1"

    var isGameOver = false
    var isVictory = false
    var isPaused = false

    var runCoinsEarned = 0
    var runXpEarned = 0
    var runEnemiesDefeated = 0
    var runAbilitiesUsed = 0
    var runTimeSeconds = 0f
    var isBossActive = false
    var bossReference: EnemyEntity? = null

    var screenShakeIntensity = 0f
    var screenShakeOffsetX = 0f
    var screenShakeOffsetY = 0f

    private var nextEntityId = 1000L
    private var spawnTimer = 0f

    enum class WaveState {
        WAVE_INTRO,
        IN_PROGRESS,
        WAVE_CLEARED
    }

    init {
        startWave(1)
    }

    fun startWave(wave: Int) {
        currentWave = wave
        waveState = WaveState.WAVE_INTRO
        waveBannerTimer = 2.0f
        waveBannerText = when {
            gameMode == GameMode.TRAINING -> "TRAINING GROUNDS"
            gameMode == GameMode.ENDLESS -> "WAVE $wave"
            wave == maxStoryWaves -> "FINAL WAVE - BOSS"
            else -> "WAVE $wave / $maxStoryWaves"
        }

        if (wave == maxStoryWaves && gameMode == GameMode.STORY) {
            audioManager.playSound(GameAudioManager.SoundType.WARNING)
        }

        spawnWaveEnemies(wave)
    }

    private fun spawnWaveEnemies(wave: Int) {
        if (gameMode == GameMode.TRAINING) {
            // Spawn 3 training golems
            val positions = listOf(Vector2(200f, 350f), Vector2(300f, 300f), Vector2(400f, 350f))
            positions.forEach { p ->
                enemies.add(
                    EnemyEntity(
                        id = nextEntityId++,
                        type = EnemyType.TANK,
                        pos = p,
                        health = 9999f,
                        maxHealth = 9999f,
                        damage = 0f,
                        baseSpeed = 0f,
                        radius = 28f
                    )
                )
            }
            return
        }

        if (gameMode == GameMode.STORY && wave == maxStoryWaves) {
            // Boss spawn!
            val bossType = when (arenaDef.id) {
                ArenaId.DARK_FOREST -> EnemyType.BOSS_BEHEMOTH
                ArenaId.ANCIENT_TEMPLE -> EnemyType.BOSS_PHARAOH
                ArenaId.CYBER_ARENA -> EnemyType.BOSS_CYBER
            }
            val boss = EnemyEntity(
                id = nextEntityId++,
                type = bossType,
                pos = Vector2(300f, 250f),
                health = 2200f + (heroProgress.level - 1) * 300f,
                maxHealth = 2200f + (heroProgress.level - 1) * 300f,
                damage = 45f,
                baseSpeed = 110f,
                radius = 48f
            )
            enemies.add(boss)
            bossReference = boss
            isBossActive = true
            return
        }

        // Standard or Endless wave composition
        val baseCount = if (gameMode == GameMode.ENDLESS) 4 + wave * 2 else 3 + wave * 2
        val types = when (wave) {
            1 -> listOf(EnemyType.BASIC)
            2 -> listOf(EnemyType.BASIC, EnemyType.FAST)
            3 -> listOf(EnemyType.BASIC, EnemyType.FAST, EnemyType.RANGED, EnemyType.TANK)
            4 -> listOf(EnemyType.FAST, EnemyType.RANGED, EnemyType.ELITE, EnemyType.TANK)
            else -> listOf(EnemyType.BASIC, EnemyType.FAST, EnemyType.TANK, EnemyType.RANGED, EnemyType.ELITE)
        }

        for (i in 0 until baseCount) {
            val type = types.random()
            val spawnPos = getRandomSpawnPosition()
            val hpMultiplier = 1f + (wave - 1) * 0.15f
            val baseHp = when (type) {
                EnemyType.BASIC -> 140f
                EnemyType.FAST -> 90f
                EnemyType.TANK -> 320f
                EnemyType.RANGED -> 110f
                EnemyType.ELITE -> 420f
                else -> 150f
            } * hpMultiplier

            val dmg = when (type) {
                EnemyType.BASIC -> 18f
                EnemyType.FAST -> 14f
                EnemyType.TANK -> 32f
                EnemyType.RANGED -> 16f
                EnemyType.ELITE -> 35f
                else -> 20f
            } * (1f + (wave - 1) * 0.10f)

            val spd = when (type) {
                EnemyType.BASIC -> 120f
                EnemyType.FAST -> 190f
                EnemyType.TANK -> 80f
                EnemyType.RANGED -> 100f
                EnemyType.ELITE -> 135f
                else -> 110f
            }

            val rad = when (type) {
                EnemyType.BASIC -> 20f
                EnemyType.FAST -> 16f
                EnemyType.TANK -> 30f
                EnemyType.RANGED -> 18f
                EnemyType.ELITE -> 28f
                else -> 20f
            }

            enemies.add(
                EnemyEntity(
                    id = nextEntityId++,
                    type = type,
                    pos = spawnPos,
                    health = baseHp,
                    maxHealth = baseHp,
                    damage = dmg,
                    baseSpeed = spd,
                    radius = rad
                )
            )
        }
    }

    private fun getRandomSpawnPosition(): Vector2 {
        val side = Random.nextInt(4)
        return when (side) {
            0 -> Vector2(Random.nextFloat() * (maxX - minX) + minX, minY + 30f)
            1 -> Vector2(Random.nextFloat() * (maxX - minX) + minX, minY + 120f)
            2 -> Vector2(minX + 30f, Random.nextFloat() * 400f + 100f)
            else -> Vector2(maxX - 30f, Random.nextFloat() * 400f + 100f)
        }
    }

    fun update(dt: Float, joystick: Vector2, isAttackHeld: Boolean) {
        if (isGameOver || isVictory || isPaused) return

        runTimeSeconds += dt

        // Screen shake decay
        if (screenShakeIntensity > 0f) {
            screenShakeOffsetX = (Random.nextFloat() * 2f - 1f) * screenShakeIntensity
            screenShakeOffsetY = (Random.nextFloat() * 2f - 1f) * screenShakeIntensity
            screenShakeIntensity = (screenShakeIntensity - dt * 25f).coerceAtLeast(0f)
        } else {
            screenShakeOffsetX = 0f
            screenShakeOffsetY = 0f
        }

        // Wave Banner timer
        if (waveBannerTimer > 0f) {
            waveBannerTimer -= dt
            if (waveBannerTimer <= 0f && waveState == WaveState.WAVE_INTRO) {
                waveState = WaveState.IN_PROGRESS
            }
        }

        // Update Player
        updatePlayer(dt, joystick, isAttackHeld)

        // Update Projectiles
        updateProjectiles(dt)

        // Update Ground Zones
        updateGroundZones(dt)

        // Update Enemies
        updateEnemies(dt)

        // Update Floating Coins
        updateCoins(dt)

        // Update Particles
        updateParticles(dt)

        // Update Damage Numbers
        updateDamageNumbers(dt)

        // Check Wave completion
        checkWaveProgression(dt)
    }

    private fun updatePlayer(dt: Float, joystick: Vector2, isAttackHeld: Boolean) {
        // Cooldowns
        if (player.dashCooldownTimer > 0f) player.dashCooldownTimer -= dt
        if (player.abilityCooldownTimer > 0f) player.abilityCooldownTimer -= dt
        if (player.attackCooldownTimer > 0f) player.attackCooldownTimer -= dt
        if (player.invulnerableTimer > 0f) player.invulnerableTimer -= dt
        if (player.damageFlashTimer > 0f) player.damageFlashTimer -= dt
        if (player.attackSwingTimer > 0f) player.attackSwingTimer -= dt

        // Combo timeout
        if (player.comboTimer > 0f) {
            player.comboTimer -= dt
            if (player.comboTimer <= 0f) {
                player.comboCount = 0
            }
        }

        // Energy recharge
        player.energy = (player.energy + dt * 15f).coerceAtMost(player.maxEnergy)

        // Movement & Dash
        if (player.isDashing) {
            player.dashDurationTimer -= dt
            // Emit trail particles
            particles.add(
                Particle(
                    x = player.pos.x + (Random.nextFloat() * 20f - 10f),
                    y = player.pos.y + (Random.nextFloat() * 20f - 10f),
                    vx = -player.vel.x * 0.15f,
                    vy = -player.vel.y * 0.15f,
                    radius = 8f,
                    color = heroDef.glowColor,
                    maxLife = 0.25f
                )
            )
            if (player.dashDurationTimer <= 0f) {
                player.isDashing = false
            }
        } else {
            val speed = heroProgress.getSpeed(heroDef)
            if (joystick.length() > 0.1f) {
                val norm = joystick.normalize()
                player.vel = norm * speed
                player.facingAngle = atan2(norm.y, norm.x)
            } else {
                player.vel = Vector2(0f, 0f)
            }
        }

        // Integrate velocity
        player.pos = player.pos + player.vel * dt
        clampPosition(player.pos, player.radius)

        // Check Obstacle collision for player
        resolveObstacleCollision(player.pos, player.radius)

        // Auto Attack if attack button is held and cooldown is ready
        if (isAttackHeld && player.attackCooldownTimer <= 0f) {
            triggerBasicAttack()
        }
    }

    fun requestDash() {
        if (player.dashCooldownTimer > 0f || player.isDashing) return

        player.isDashing = true
        player.dashDurationTimer = 0.22f
        player.dashCooldownTimer = player.dashCooldownMax
        player.invulnerableTimer = 0.35f

        val dashDir = if (player.vel.length() > 10f) {
            player.vel.normalize()
        } else {
            Vector2(cos(player.facingAngle), sin(player.facingAngle))
        }

        player.vel = dashDir * (heroProgress.getSpeed(heroDef) * 3.4f)
        audioManager.playSound(GameAudioManager.SoundType.DASH)
        audioManager.vibrate(30, 150)

        // Dash burst particles
        for (i in 0 until 12) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            particles.add(
                Particle(
                    x = player.pos.x,
                    y = player.pos.y,
                    vx = cos(angle) * 120f,
                    vy = sin(angle) * 120f,
                    radius = 5f,
                    color = heroDef.secondaryColor,
                    maxLife = 0.35f
                )
            )
        }
    }

    fun triggerBasicAttack() {
        player.attackCooldownTimer = heroDef.attackCooldownSec
        player.attackSwingTimer = 0.18f
        audioManager.playSound(GameAudioManager.SoundType.ATTACK)

        // Check if there is an enemy nearby to auto-target facing angle
        val nearest = findNearestEnemy(player.pos, 350f)
        if (nearest != null) {
            player.facingAngle = player.pos.angleTo(nearest.pos)
        }

        val damage = heroProgress.getDamage(heroDef)

        if (heroDef.isRanged) {
            // Spawn Ranged Projectile
            val dir = Vector2(cos(player.facingAngle), sin(player.facingAngle))
            val projSpeed = 480f
            projectiles.add(
                Projectile(
                    id = nextEntityId++,
                    pos = Vector2(player.pos.x + dir.x * 25f, player.pos.y + dir.y * 25f),
                    vel = dir * projSpeed,
                    radius = 10f,
                    damage = damage,
                    isPlayer = true,
                    lifeTimer = 1.2f,
                    maxLife = 1.2f,
                    color = heroDef.glowColor,
                    pierces = false,
                    effectType = heroDef.element
                )
            )
        } else {
            // Melee Slash Hitbox in front arc
            val forward = Vector2(cos(player.facingAngle), sin(player.facingAngle))
            val attackRange = heroDef.attackRange + 15f
            var hitAny = false

            enemies.forEach { enemy ->
                if (enemy.isAlive()) {
                    val dist = player.pos.distanceTo(enemy.pos)
                    if (dist <= attackRange + enemy.radius) {
                        val toEnemy = (enemy.pos - player.pos).normalize()
                        val dot = forward.x * toEnemy.x + forward.y * toEnemy.y
                        if (dot > 0.35f) { // roughly 120 degree cone in front
                            applyDamageToEnemy(enemy, damage, isCritical = (Random.nextFloat() < 0.25f))
                            // Knockback
                            enemy.pos = enemy.pos + forward * 28f
                            hitAny = true
                        }
                    }
                }
            }

            if (hitAny) {
                triggerScreenShake(3f)
                audioManager.playSound(GameAudioManager.SoundType.HIT)
                audioManager.vibrate(35, 180)
            }

            // Spawn Slash arc particles
            for (i in -3..3) {
                val arcAngle = player.facingAngle + (i * 0.22f)
                val dist = attackRange * 0.75f
                particles.add(
                    Particle(
                        x = player.pos.x + cos(arcAngle) * dist,
                        y = player.pos.y + sin(arcAngle) * dist,
                        vx = cos(arcAngle) * 80f,
                        vy = sin(arcAngle) * 80f,
                        radius = 6f,
                        color = heroDef.primaryColor,
                        maxLife = 0.18f,
                        isSpark = true
                    )
                )
            }
        }
    }

    fun requestSpecialAbility() {
        if (player.abilityCooldownTimer > 0f) return

        player.abilityCooldownTimer = player.abilityCooldownMax
        runAbilitiesUsed++
        triggerScreenShake(8f)
        audioManager.vibrate(60, 240)

        val power = heroProgress.getAbilityPower(heroDef)
        val baseDmg = heroProgress.getDamage(heroDef) * power

        when (heroDef.id) {
            com.example.game.model.HeroId.BLAZE -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                // Fire explosion 360 ring
                val blastRadius = 240f
                enemies.forEach { enemy ->
                    if (enemy.isAlive() && player.pos.distanceTo(enemy.pos) <= blastRadius) {
                        applyDamageToEnemy(enemy, baseDmg * 2.8f, isCritical = true)
                        val knockDir = (enemy.pos - player.pos).normalize()
                        enemy.pos = enemy.pos + knockDir * 60f
                    }
                }
                groundZones.add(
                    GroundZone(
                        id = nextEntityId++,
                        x = player.pos.x,
                        y = player.pos.y,
                        radius = blastRadius,
                        duration = 1.0f,
                        maxDuration = 1.0f,
                        type = GroundZoneType.GROUND_SMASH_CRATER,
                        color = 0xFFFF5722,
                        ownerIsPlayer = true
                    )
                )
                // Burst particles
                for (i in 0 until 40) {
                    val angle = Random.nextFloat() * PI.toFloat() * 2f
                    val speed = Random.nextFloat() * 240f + 60f
                    particles.add(
                        Particle(
                            x = player.pos.x,
                            y = player.pos.y,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            radius = Random.nextFloat() * 8f + 4f,
                            color = if (i % 2 == 0) 0xFFFF3D00 else 0xFFFFEA00,
                            maxLife = 0.55f,
                            isSpark = true
                        )
                    )
                }
            }

            com.example.game.model.HeroId.FROST -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_ICE)
                // Absolute Zero: freezes all enemies in the arena
                enemies.forEach { enemy ->
                    if (enemy.isAlive()) {
                        enemy.frozenTimer = 3.5f
                        applyDamageToEnemy(enemy, baseDmg * 1.8f, isCritical = false)
                    }
                }
                groundZones.add(
                    GroundZone(
                        id = nextEntityId++,
                        x = player.pos.x,
                        y = player.pos.y,
                        radius = 320f,
                        duration = 3.5f,
                        maxDuration = 3.5f,
                        type = GroundZoneType.BLIZZARD_FROST,
                        color = 0xFF00E5FF,
                        ownerIsPlayer = true
                    )
                )
                for (i in 0 until 35) {
                    val angle = Random.nextFloat() * PI.toFloat() * 2f
                    particles.add(
                        Particle(
                            x = player.pos.x + cos(angle) * 140f,
                            y = player.pos.y + sin(angle) * 140f,
                            vx = cos(angle) * 110f,
                            vy = sin(angle) * 110f,
                            radius = 7f,
                            color = 0xFF80D8FF,
                            maxLife = 0.8f,
                            isSpark = true
                        )
                    )
                }
            }

            com.example.game.model.HeroId.SHADOW -> {
                audioManager.playSound(GameAudioManager.SoundType.HIT)
                // Shadow Dash: invulnerable flurry strike
                player.invulnerableTimer = 1.0f
                val nearby = enemies.filter { it.isAlive() && player.pos.distanceTo(it.pos) <= 280f }
                nearby.forEach { enemy ->
                    for (slash in 0 until 3) {
                        applyDamageToEnemy(enemy, baseDmg * 1.3f, isCritical = true)
                    }
                    enemy.pos = enemy.pos + Vector2(Random.nextFloat() * 30f - 15f, Random.nextFloat() * 30f - 15f)
                }
                // Teleport slightly forward
                val fwd = Vector2(cos(player.facingAngle), sin(player.facingAngle))
                player.pos = player.pos + fwd * 120f
                clampPosition(player.pos, player.radius)
                for (i in 0 until 30) {
                    particles.add(
                        Particle(
                            x = player.pos.x + Random.nextFloat() * 40f - 20f,
                            y = player.pos.y + Random.nextFloat() * 40f - 20f,
                            vx = Random.nextFloat() * 200f - 100f,
                            vy = Random.nextFloat() * 200f - 100f,
                            radius = 6f,
                            color = 0xFFE040FB,
                            maxLife = 0.45f
                        )
                    )
                }
            }

            com.example.game.model.HeroId.TITAN -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                // Ground Smash: crater + stun
                val smashRadius = 260f
                enemies.forEach { enemy ->
                    if (enemy.isAlive() && player.pos.distanceTo(enemy.pos) <= smashRadius) {
                        enemy.frozenTimer = 2.5f // stun
                        applyDamageToEnemy(enemy, baseDmg * 3.2f, isCritical = true)
                        val knockDir = (enemy.pos - player.pos).normalize()
                        enemy.pos = enemy.pos + knockDir * 70f
                    }
                }
                groundZones.add(
                    GroundZone(
                        id = nextEntityId++,
                        x = player.pos.x,
                        y = player.pos.y,
                        radius = smashRadius,
                        duration = 1.5f,
                        maxDuration = 1.5f,
                        type = GroundZoneType.GROUND_SMASH_CRATER,
                        color = 0xFFFFB300,
                        ownerIsPlayer = true
                    )
                )
            }

            com.example.game.model.HeroId.VOLT -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_LIGHTNING)
                // Chain lightning to up to 6 enemies
                val aliveEnemies = enemies.filter { it.isAlive() }.toMutableList()
                var currentTarget: EnemyEntity? = findNearestEnemy(player.pos, 350f)
                var chainCount = 0

                while (currentTarget != null && chainCount < 6) {
                    applyDamageToEnemy(currentTarget, baseDmg * 1.9f, isCritical = (Random.nextFloat() < 0.4f))
                    // Emit lightning spark particles between chain points
                    val startP = if (chainCount == 0) player.pos else currentTarget.pos
                    for (i in 0 until 8) {
                        particles.add(
                            Particle(
                                x = startP.x + (currentTarget.pos.x - startP.x) * (i / 8f),
                                y = startP.y + (currentTarget.pos.y - startP.y) * (i / 8f) + (Random.nextFloat() * 20f - 10f),
                                vx = Random.nextFloat() * 50f - 25f,
                                vy = Random.nextFloat() * 50f - 25f,
                                radius = 5f,
                                color = 0xFFFFEA00,
                                maxLife = 0.25f,
                                isSpark = true
                            )
                        )
                    }
                    aliveEnemies.remove(currentTarget)
                    chainCount++
                    currentTarget = aliveEnemies.minByOrNull { currentTarget!!.pos.distanceTo(it.pos) }
                }
            }

            com.example.game.model.HeroId.NATURE -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_HEAL)
                // Heal 50% max HP + Healing ground zone
                val healAmount = player.maxHealth * 0.50f
                player.health = (player.health + healAmount).coerceAtMost(player.maxHealth)
                damageNumbers.add(
                    DamageNumber(
                        id = nextEntityId++,
                        text = "+${healAmount.toInt()} HP",
                        x = player.pos.x,
                        y = player.pos.y - 40f,
                        color = 0xFF00E676,
                        isCritical = true
                    )
                )
                groundZones.add(
                    GroundZone(
                        id = nextEntityId++,
                        x = player.pos.x,
                        y = player.pos.y,
                        radius = 200f,
                        duration = 4.0f,
                        maxDuration = 4.0f,
                        type = GroundZoneType.HEALING_AURA,
                        color = 0xFF00E676,
                        ownerIsPlayer = true
                    )
                )
            }

            com.example.game.model.HeroId.PHANTOM -> {
                audioManager.playSound(GameAudioManager.SoundType.HIT)
                // Teleport behind highest health enemy and execute backstab
                val target = enemies.filter { it.isAlive() }.maxByOrNull { it.health }
                if (target != null) {
                    val angle = target.facingAngle + PI.toFloat()
                    player.pos = Vector2(target.pos.x + cos(angle) * 45f, target.pos.y + sin(angle) * 45f)
                    player.facingAngle = player.pos.angleTo(target.pos)
                    clampPosition(player.pos, player.radius)
                    applyDamageToEnemy(target, baseDmg * 4.2f, isCritical = true)
                    for (i in 0 until 20) {
                        particles.add(
                            Particle(
                                x = target.pos.x,
                                y = target.pos.y,
                                vx = Random.nextFloat() * 180f - 90f,
                                vy = Random.nextFloat() * 180f - 90f,
                                radius = 7f,
                                color = 0xFF7C4DFF,
                                maxLife = 0.4f
                            )
                        )
                    }
                }
            }

            com.example.game.model.HeroId.VENOM -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                // Poison Cloud zone
                groundZones.add(
                    GroundZone(
                        id = nextEntityId++,
                        x = player.pos.x,
                        y = player.pos.y,
                        radius = 180f,
                        duration = 5.5f,
                        maxDuration = 5.5f,
                        type = GroundZoneType.POISON_CLOUD,
                        color = 0xFF76FF03,
                        ownerIsPlayer = true
                    )
                )
            }

            com.example.game.model.HeroId.SOLAR -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_LIGHTNING)
                // Solar Beam forward
                val fwd = Vector2(cos(player.facingAngle), sin(player.facingAngle))
                for (step in 1..8) {
                    val beamPos = player.pos + fwd * (step * 45f)
                    enemies.forEach { enemy ->
                        if (enemy.isAlive() && enemy.pos.distanceTo(beamPos) <= 45f) {
                            applyDamageToEnemy(enemy, baseDmg * 2.2f, isCritical = true)
                        }
                    }
                    particles.add(
                        Particle(
                            x = beamPos.x,
                            y = beamPos.y,
                            vx = Random.nextFloat() * 40f - 20f,
                            vy = Random.nextFloat() * 40f - 20f,
                            radius = 16f,
                            color = 0xFFFFD700,
                            maxLife = 0.45f
                        )
                    )
                }
            }

            com.example.game.model.HeroId.DRAGON -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                // Dragon Flame cone barrage
                val coneAngles = listOf(-0.35f, -0.18f, 0f, 0.18f, 0.35f)
                coneAngles.forEach { angleOffset ->
                    val angle = player.facingAngle + angleOffset
                    val dir = Vector2(cos(angle), sin(angle))
                    projectiles.add(
                        Projectile(
                            id = nextEntityId++,
                            pos = Vector2(player.pos.x, player.pos.y),
                            vel = dir * 420f,
                            radius = 18f,
                            damage = baseDmg * 2.5f,
                            isPlayer = true,
                            lifeTimer = 0.9f,
                            maxLife = 0.9f,
                            color = 0xFFFF1744,
                            pierces = true,
                            effectType = ElementType.DRAGON_FIRE
                        )
                    )
                }
            }
        }
    }

    private fun updateProjectiles(dt: Float) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val proj = iterator.next()
            proj.lifeTimer -= dt
            proj.pos = proj.pos + proj.vel * dt

            // Trail particles
            if (Random.nextFloat() < 0.4f) {
                particles.add(
                    Particle(
                        x = proj.pos.x,
                        y = proj.pos.y,
                        vx = -proj.vel.x * 0.1f,
                        vy = -proj.vel.y * 0.1f,
                        radius = proj.radius * 0.6f,
                        color = proj.color,
                        maxLife = 0.2f
                    )
                )
            }

            // Boundary collision
            if (proj.pos.x < minX || proj.pos.x > maxX || proj.pos.y < minY || proj.pos.y > maxY || proj.lifeTimer <= 0f) {
                iterator.remove()
                continue
            }

            // Hit detection
            if (proj.isPlayer) {
                var collided = false
                for (enemy in enemies) {
                    if (enemy.isAlive() && proj.pos.distanceTo(enemy.pos) <= proj.radius + enemy.radius) {
                        applyDamageToEnemy(enemy, proj.damage, isCritical = (Random.nextFloat() < 0.2f))
                        if (!proj.pierces) {
                            collided = true
                            break
                        }
                    }
                }
                if (collided) {
                    iterator.remove()
                }
            } else {
                // Enemy projectile hitting player
                if (player.invulnerableTimer <= 0f && proj.pos.distanceTo(player.pos) <= proj.radius + player.radius) {
                    applyDamageToPlayer(proj.damage)
                    iterator.remove()
                }
            }
        }
    }

    private fun updateGroundZones(dt: Float) {
        val iterator = groundZones.iterator()
        while (iterator.hasNext()) {
            val zone = iterator.next()
            zone.duration -= dt

            if (zone.type == GroundZoneType.HEALING_AURA && zone.ownerIsPlayer) {
                // Heals player if standing in it
                if (player.pos.distanceTo(Vector2(zone.x, zone.y)) <= zone.radius) {
                    player.health = (player.health + dt * 25f).coerceAtMost(player.maxHealth)
                }
                // Thorns damage to enemies
                enemies.forEach { enemy ->
                    if (enemy.isAlive() && enemy.pos.distanceTo(Vector2(zone.x, zone.y)) <= zone.radius) {
                        applyDamageToEnemy(enemy, dt * 35f, isCritical = false)
                    }
                }
            } else if (zone.type == GroundZoneType.POISON_CLOUD && zone.ownerIsPlayer) {
                enemies.forEach { enemy ->
                    if (enemy.isAlive() && enemy.pos.distanceTo(Vector2(zone.x, zone.y)) <= zone.radius) {
                        applyDamageToEnemy(enemy, dt * 50f, isCritical = false)
                    }
                }
            } else if (zone.type == GroundZoneType.BOSS_WARNING_CIRCLE) {
                // Warning indicator counts down to blast
                if (zone.duration <= 0.05f) {
                    // Explode!
                    if (player.pos.distanceTo(Vector2(zone.x, zone.y)) <= zone.radius && player.invulnerableTimer <= 0f) {
                        applyDamageToPlayer(55f)
                    }
                    triggerScreenShake(7f)
                    audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                }
            }

            if (zone.duration <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun updateEnemies(dt: Float) {
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            if (!enemy.isAlive()) {
                onEnemyKilled(enemy)
                iterator.remove()
                continue
            }

            // Timers
            if (enemy.hitFlashTimer > 0f) enemy.hitFlashTimer -= dt
            if (enemy.attackCooldownTimer > 0f) enemy.attackCooldownTimer -= dt

            // Frozen effect
            if (enemy.frozenTimer > 0f) {
                enemy.frozenTimer -= dt
                continue
            }

            // Distance & Angle to player
            val distToPlayer = enemy.pos.distanceTo(player.pos)
            enemy.facingAngle = enemy.pos.angleTo(player.pos)

            if (enemy.isBoss) {
                updateBossAI(enemy, dt, distToPlayer)
            } else {
                updateStandardEnemyAI(enemy, dt, distToPlayer)
            }

            // Clamp position
            clampPosition(enemy.pos, enemy.radius)
            resolveObstacleCollision(enemy.pos, enemy.radius)
        }
    }

    private fun updateStandardEnemyAI(enemy: EnemyEntity, dt: Float, distToPlayer: Float) {
        val dirToPlayer = (player.pos - enemy.pos).normalize()

        when (enemy.type) {
            EnemyType.RANGED -> {
                // Keep distance: retreat if too close, advance if too far
                val desiredDist = 260f
                if (distToPlayer < desiredDist - 40f) {
                    enemy.vel = dirToPlayer * (-enemy.baseSpeed * 0.8f)
                } else if (distToPlayer > desiredDist + 40f) {
                    enemy.vel = dirToPlayer * enemy.baseSpeed
                } else {
                    enemy.vel = Vector2(0f, 0f)
                }

                // Fire dark projectile
                if (enemy.attackCooldownTimer <= 0f && distToPlayer <= 380f) {
                    enemy.attackCooldownTimer = 2.0f
                    val projVel = dirToPlayer * 280f
                    projectiles.add(
                        Projectile(
                            id = nextEntityId++,
                            pos = Vector2(enemy.pos.x, enemy.pos.y),
                            vel = projVel,
                            radius = 9f,
                            damage = enemy.damage,
                            isPlayer = false,
                            lifeTimer = 1.8f,
                            maxLife = 1.8f,
                            color = 0xFF7C4DFF
                        )
                    )
                }
            }

            EnemyType.FAST -> {
                // Circling and fast darting
                val tangent = Vector2(-dirToPlayer.y, dirToPlayer.x)
                enemy.vel = (dirToPlayer * 0.7f + tangent * 0.5f).normalize() * enemy.baseSpeed
                if (distToPlayer <= enemy.radius + player.radius + 10f && enemy.attackCooldownTimer <= 0f) {
                    enemy.attackCooldownTimer = 1.1f
                    applyDamageToPlayer(enemy.damage)
                }
            }

            else -> {
                // Swarm player
                enemy.vel = dirToPlayer * enemy.baseSpeed
                if (distToPlayer <= enemy.radius + player.radius + 12f && enemy.attackCooldownTimer <= 0f) {
                    enemy.attackCooldownTimer = 1.3f
                    applyDamageToPlayer(enemy.damage)
                }
            }
        }

        // Apply separation from other enemies
        enemies.forEach { other ->
            if (other != enemy && other.isAlive()) {
                val d = enemy.pos.distanceTo(other.pos)
                if (d < enemy.radius + other.radius) {
                    val push = (enemy.pos - other.pos).normalize() * (30f)
                    enemy.pos = enemy.pos + push * dt
                }
            }
        }

        enemy.pos = enemy.pos + enemy.vel * dt
    }

    private fun updateBossAI(boss: EnemyEntity, dt: Float, distToPlayer: Float) {
        boss.specialAttackTimer -= dt
        val dirToPlayer = (player.pos - boss.pos).normalize()

        // Move towards player slowly
        boss.vel = dirToPlayer * boss.baseSpeed
        boss.pos = boss.pos + boss.vel * dt

        // Melee hit if player gets too close
        if (distToPlayer <= boss.radius + player.radius + 10f && boss.attackCooldownTimer <= 0f) {
            boss.attackCooldownTimer = 1.8f
            applyDamageToPlayer(boss.damage * 0.9f)
            triggerScreenShake(6f)
        }

        // Boss Special Abilities
        if (boss.specialAttackTimer <= 0f) {
            boss.specialAttackTimer = Random.nextFloat() * 2f + 3.5f

            when (boss.type) {
                EnemyType.BOSS_BEHEMOTH -> {
                    // Stomp shockwave warning circle
                    groundZones.add(
                        GroundZone(
                            id = nextEntityId++,
                            x = boss.pos.x,
                            y = boss.pos.y,
                            radius = 210f,
                            duration = 1.2f,
                            maxDuration = 1.2f,
                            type = GroundZoneType.BOSS_WARNING_CIRCLE,
                            color = 0xFFFF1744,
                            ownerIsPlayer = false
                        )
                    )
                    audioManager.playSound(GameAudioManager.SoundType.WARNING)
                }

                EnemyType.BOSS_PHARAOH -> {
                    // Triple sand orbs spread
                    val baseAngle = boss.pos.angleTo(player.pos)
                    listOf(-0.35f, 0f, 0.35f).forEach { offset ->
                        val a = baseAngle + offset
                        val pDir = Vector2(cos(a), sin(a))
                        projectiles.add(
                            Projectile(
                                id = nextEntityId++,
                                pos = Vector2(boss.pos.x, boss.pos.y),
                                vel = pDir * 290f,
                                radius = 12f,
                                damage = 35f,
                                isPlayer = false,
                                lifeTimer = 2.0f,
                                maxLife = 2.0f,
                                color = 0xFFFFB300
                            )
                        )
                    }
                    audioManager.playSound(GameAudioManager.SoundType.ABILITY_LIGHTNING)
                }

                EnemyType.BOSS_CYBER -> {
                    // Laser sweep or drone barrage
                    groundZones.add(
                        GroundZone(
                            id = nextEntityId++,
                            x = player.pos.x,
                            y = player.pos.y,
                            radius = 160f,
                            duration = 1.4f,
                            maxDuration = 1.4f,
                            type = GroundZoneType.BOSS_WARNING_CIRCLE,
                            color = 0xFF00E5FF,
                            ownerIsPlayer = false
                        )
                    )
                    audioManager.playSound(GameAudioManager.SoundType.WARNING)
                }

                else -> {}
            }
        }
    }

    private fun applyDamageToPlayer(damage: Float) {
        if (player.invulnerableTimer > 0f || isGameOver || isVictory) return

        player.health -= damage
        player.damageFlashTimer = 0.25f
        player.invulnerableTimer = 0.55f
        triggerScreenShake(7f)
        audioManager.playSound(GameAudioManager.SoundType.HIT)
        audioManager.vibrate(50, 220)

        damageNumbers.add(
            DamageNumber(
                id = nextEntityId++,
                text = "-${damage.toInt()}",
                x = player.pos.x + Random.nextFloat() * 20f - 10f,
                y = player.pos.y - 30f,
                color = 0xFFFF1744,
                isCritical = true
            )
        )

        if (player.health <= 0f) {
            player.health = 0f
            isGameOver = true
            audioManager.playSound(GameAudioManager.SoundType.DEFEAT)
            audioManager.vibrate(100, 255)
        }
    }

    private fun applyDamageToEnemy(enemy: EnemyEntity, damage: Float, isCritical: Boolean) {
        enemy.health -= damage
        enemy.hitFlashTimer = 0.15f

        val finalDmg = damage * (if (isCritical) 1.5f else 1.0f)
        damageNumbers.add(
            DamageNumber(
                id = nextEntityId++,
                text = "${finalDmg.toInt()}",
                x = enemy.pos.x + Random.nextFloat() * 24f - 12f,
                y = enemy.pos.y - 25f,
                color = if (isCritical) 0xFFFFEA00 else 0xFFFFFFFF,
                isCritical = isCritical
            )
        )

        // Spawn hit blood/sparks
        for (i in 0 until 5) {
            particles.add(
                Particle(
                    x = enemy.pos.x,
                    y = enemy.pos.y,
                    vx = Random.nextFloat() * 120f - 60f,
                    vy = Random.nextFloat() * 120f - 60f,
                    radius = 4f,
                    color = if (isCritical) 0xFFFF9800 else 0xFFE0E0E0,
                    maxLife = 0.25f,
                    isSpark = true
                )
            )
        }
    }

    private fun onEnemyKilled(enemy: EnemyEntity) {
        runEnemiesDefeated++
        val coinValue = if (enemy.isBoss) 150 else if (enemy.type == EnemyType.ELITE) 40 else (8..18).random()
        runCoinsEarned += coinValue
        runXpEarned += if (enemy.isBoss) 200 else (15..35).random()

        audioManager.playSound(GameAudioManager.SoundType.COIN)

        // Spawn coins
        floatingCoins.add(
            FloatingCoin(
                id = nextEntityId++,
                pos = Vector2(enemy.pos.x, enemy.pos.y),
                vel = Vector2(Random.nextFloat() * 80f - 40f, Random.nextFloat() * 80f - 40f),
                value = coinValue,
                isGem = (enemy.isBoss || Random.nextFloat() < 0.08f)
            )
        )

        // Death explosion particles
        for (i in 0 until (if (enemy.isBoss) 45 else 14)) {
            val angle = Random.nextFloat() * PI.toFloat() * 2f
            val speed = Random.nextFloat() * 160f + 40f
            particles.add(
                Particle(
                    x = enemy.pos.x,
                    y = enemy.pos.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    radius = Random.nextFloat() * 6f + 3f,
                    color = if (enemy.isBoss) 0xFFFFD700 else 0xFF9C27B0,
                    maxLife = 0.45f
                )
            )
        }

        if (enemy.isBoss) {
            isBossActive = false
            bossReference = null
            triggerVictory()
        }
    }

    private fun updateCoins(dt: Float) {
        val iterator = floatingCoins.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            coin.life += dt
            coin.pos = coin.pos + coin.vel * dt
            coin.vel = coin.vel * 0.92f // friction

            // Magnet towards player
            val dist = coin.pos.distanceTo(player.pos)
            if (dist < 150f) {
                val dir = (player.pos - coin.pos).normalize()
                coin.pos = coin.pos + dir * (320f * dt)
                if (dist < player.radius + 12f) {
                    iterator.remove()
                    continue
                }
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life += dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha = (1f - (p.life / p.maxLife)).coerceIn(0f, 1f)
            if (p.life >= p.maxLife) {
                iterator.remove()
            }
        }
    }

    private fun updateDamageNumbers(dt: Float) {
        val iterator = damageNumbers.iterator()
        while (iterator.hasNext()) {
            val d = iterator.next()
            d.life += dt
            d.y -= 45f * dt
            d.alpha = (1f - (d.life / d.maxLife)).coerceIn(0f, 1f)
            if (d.life >= d.maxLife) {
                iterator.remove()
            }
        }
    }

    private fun checkWaveProgression(dt: Float) {
        if (isGameOver || isVictory) return

        if (gameMode == GameMode.TRAINING) {
            // Respawn training dummy if none left
            if (enemies.isEmpty()) {
                spawnWaveEnemies(1)
            }
            return
        }

        if (enemies.isEmpty() && waveState == WaveState.IN_PROGRESS) {
            waveState = WaveState.WAVE_CLEARED
            waveBannerTimer = 1.8f
            waveBannerText = "WAVE $currentWave CLEARED!"
            audioManager.playSound(GameAudioManager.SoundType.LEVEL_UP)

            if (gameMode == GameMode.STORY && currentWave >= maxStoryWaves) {
                triggerVictory()
            } else {
                // Next wave
                startWave(currentWave + 1)
            }
        }
    }

    private fun triggerVictory() {
        isVictory = true
        audioManager.playSound(GameAudioManager.SoundType.VICTORY)
        audioManager.vibrate(80, 240)
    }

    private fun triggerScreenShake(intensity: Float) {
        screenShakeIntensity = intensity.coerceAtLeast(screenShakeIntensity)
    }

    private fun findNearestEnemy(from: Vector2, maxRadius: Float): EnemyEntity? {
        return enemies.filter { it.isAlive() && from.distanceTo(it.pos) <= maxRadius }
            .minByOrNull { from.distanceTo(it.pos) }
    }

    private fun clampPosition(pos: Vector2, radius: Float) {
        pos.x = pos.x.coerceIn(minX + radius, maxX - radius)
        pos.y = pos.y.coerceIn(minY + radius, maxY - radius)
    }

    private fun resolveObstacleCollision(pos: Vector2, radius: Float) {
        arenaDef.obstacles.forEach { obs ->
            val dist = pos.distanceTo(Vector2(obs.x, obs.y))
            val minDist = radius + obs.radius
            if (dist < minDist && dist > 0.001f) {
                val pushDir = (pos - Vector2(obs.x, obs.y)).normalize()
                val overlap = minDist - dist
                pos.x += pushDir.x * overlap
                pos.y += pushDir.y * overlap
            }
        }
    }
}

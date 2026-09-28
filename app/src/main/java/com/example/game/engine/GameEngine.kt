package com.example.game.engine

import com.example.audio.GameAudioManager
import com.example.game.model.AIBehaviorState
import com.example.game.model.AnimState
import com.example.game.model.CharacterDef
import com.example.game.model.CharacterProgress
import com.example.game.model.EnemyType
import com.example.game.model.EnemyWarrior
import com.example.game.model.PlayerWarrior
import com.example.game.model.PooledCoin
import com.example.game.model.PooledDamageNumber
import com.example.game.model.PooledParticle
import com.example.game.model.PooledProjectile
import com.example.game.model.SkinDef
import com.example.game.model.Vector2
import com.example.game.model.WeaponDef
import com.example.game.model.WeaponType
import com.example.game.model.WorldDef
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class StagePlatform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float = 16f
)

class GameEngine(
    val characterDef: CharacterDef,
    val characterProgress: CharacterProgress,
    val weaponDef: WeaponDef,
    val worldDef: WorldDef,
    val stageNumber: Int, // 1..4 (4 is Boss Stage)
    private val audioManager: GameAudioManager
) {
    // Stage Dimensions
    val stageLength: Float = if (stageNumber == 4) 2200f else 2800f
    val stageHeight: Float = 900f
    val groundY: Float = 530f
    val viewportWidth: Float = 600f
    val viewportHeight: Float = 900f

    // Camera
    var cameraX: Float = 0f
    var screenShakeIntensity: Float = 0f
    var screenShakeOffsetX: Float = 0f
    var screenShakeOffsetY: Float = 0f

    // Game State
    var isPaused: Boolean = false
    var isVictory: Boolean = false
    var isGameOver: Boolean = false
    var runTimeSeconds: Float = 0f
    var coinsCollected: Int = 0
    var xpEarned: Int = 0
    var enemiesKilled: Int = 0
    var bossDefeated: Boolean = false

    // Platforms along the stage
    val platforms = listOf(
        StagePlatform(360f, 430f, 160f),
        StagePlatform(720f, 380f, 180f),
        StagePlatform(1150f, 420f, 190f),
        StagePlatform(1520f, 360f, 210f),
        StagePlatform(1980f, 410f, 180f)
    )

    // Player
    val playerSkin: SkinDef = characterDef.skins.getOrElse(characterProgress.selectedSkinIndex) { characterDef.skins.first() }
    val player = PlayerWarrior(
        characterDef = characterDef,
        skinDef = playerSkin,
        weaponDef = weaponDef,
        maxHp = characterProgress.getHp(characterDef),
        attackStat = characterProgress.getAttack(characterDef) + weaponDef.baseDamage * 0.4f,
        defenseStat = characterProgress.getDefense(characterDef),
        speedStat = characterProgress.getSpeed(characterDef)
    )

    // Fixed Object Pools (Zero GC in game loop)
    val enemies = Array(8) { EnemyWarrior() }
    val projectiles = Array(24) { PooledProjectile() }
    val particles = Array(32) { PooledParticle() }
    val damageNumbers = Array(10) { PooledDamageNumber() }
    val coins = Array(12) { PooledCoin() }

    // Boss Tracking
    var activeBoss: EnemyWarrior? = null
    var isBossFightTriggered: Boolean = false

    // Spawner checkpoints along x
    private val spawnTriggerXs = floatArrayOf(280f, 650f, 1050f, 1480f, 1900f)
    private val spawnTriggered = BooleanArray(spawnTriggerXs.size) { false }

    init {
        resetGame()
    }

    fun resetGame() {
        player.reset(spawnX = 80f, spawnY = groundY)
        cameraX = 0f
        isPaused = false
        isVictory = false
        isGameOver = false
        runTimeSeconds = 0f
        coinsCollected = 0
        xpEarned = 0
        enemiesKilled = 0
        bossDefeated = false
        isBossFightTriggered = false
        activeBoss = null

        for (i in spawnTriggered.indices) spawnTriggered[i] = false
        for (e in enemies) e.active = false
        for (p in projectiles) p.active = false
        for (pt in particles) pt.active = false
        for (d in damageNumbers) d.active = false
        for (c in coins) c.active = false

        // Initial initial enemies
        spawnEnemyAt(380f, EnemyType.BASIC_FIGHTER)
        if (stageNumber > 1) {
            spawnEnemyAt(540f, EnemyType.FAST_FIGHTER)
        }
    }

    fun update(dt: Float, moveX: Float, jumpPressed: Boolean, attackHeld: Boolean) {
        if (isPaused || isGameOver || isVictory) return
        runTimeSeconds += dt

        // 1. Screen Shake Decay
        if (screenShakeIntensity > 0f) {
            screenShakeOffsetX = (Random.nextFloat() * 2f - 1f) * screenShakeIntensity
            screenShakeOffsetY = (Random.nextFloat() * 2f - 1f) * screenShakeIntensity
            screenShakeIntensity = (screenShakeIntensity - dt * 25f).coerceAtLeast(0f)
        } else {
            screenShakeOffsetX = 0f
            screenShakeOffsetY = 0f
        }

        // 2. Update Player
        updatePlayer(dt, moveX, jumpPressed, attackHeld)

        // 3. Update Camera
        val targetCamX = (player.pos.x - 200f).coerceIn(0f, stageLength - viewportWidth)
        cameraX += (targetCamX - cameraX) * (dt * 6.5f).coerceAtMost(1f)

        // 4. Check Spawner Checkpoints
        checkSpawners()

        // 5. Update Enemies
        updateEnemies(dt)

        // 6. Update Projectiles
        updateProjectiles(dt)

        // 7. Update Particles & Numbers & Coins
        for (i in particles.indices) particles[i].update(dt)
        for (i in damageNumbers.indices) damageNumbers[i].update(dt)
        for (i in coins.indices) {
            val c = coins[i]
            if (c.active) {
                c.update(dt, groundY)
                // Collect coin on player overlap
                val dist = player.pos.distanceTo(Vector2(c.x, c.y))
                if (dist < player.radius + 20f) {
                    c.active = false
                    coinsCollected += c.value
                    xpEarned += (c.value * 0.8f).toInt()
                    audioManager.playSound(GameAudioManager.SoundType.COIN)
                    spawnParticle(c.x, c.y, 0f, -40f, 3.5f, 0xFFFFD700, 0.25f)
                }
            }
        }

        // 8. Check Victory Condition
        if (stageNumber == 4) {
            if (bossDefeated && !isVictory) {
                isVictory = true
                player.animState = AnimState.VICTORY
                audioManager.playSound(GameAudioManager.SoundType.VICTORY)
                audioManager.vibrate(80, 240)
            }
        } else {
            if (player.pos.x >= stageLength - 90f && !isVictory) {
                isVictory = true
                player.animState = AnimState.VICTORY
                audioManager.playSound(GameAudioManager.SoundType.VICTORY)
                audioManager.vibrate(80, 240)
            }
        }
    }

    private fun updatePlayer(dt: Float, moveX: Float, jumpPressed: Boolean, attackHeld: Boolean) {
        // Cooldown timers
        if (player.dashCooldownTimer > 0f) player.dashCooldownTimer -= dt
        if (player.abilityCooldownTimer > 0f) player.abilityCooldownTimer -= dt
        if (player.attackCooldownTimer > 0f) player.attackCooldownTimer -= dt
        if (player.invulnerableTimer > 0f) player.invulnerableTimer -= dt
        if (player.hitFlashTimer > 0f) player.hitFlashTimer -= dt
        if (player.knockbackTimer > 0f) player.knockbackTimer -= dt

        // Combo timeout reset
        if (player.comboTimer > 0f) {
            player.comboTimer -= dt
            if (player.comboTimer <= 0f) {
                player.comboStep = 0
            }
        }
        if (player.comboDisplayTimer > 0f) {
            player.comboDisplayTimer -= dt
            if (player.comboDisplayTimer <= 0f) {
                player.comboDisplayCount = 0
            }
        }

        // Energy recharge
        player.energy = (player.energy + dt * 15f).coerceAtMost(player.maxEnergy)

        // Dashing behavior
        if (player.isDashing) {
            player.dashTimer -= dt
            player.pos.x += player.vel.x * dt
            spawnParticle(player.pos.x, player.pos.y, -player.vel.x * 0.1f, 0f, 5f, playerSkin.glowColor, 0.18f, true)
            if (player.dashTimer <= 0f) {
                player.isDashing = false
            }
        } else if (player.knockbackTimer > 0f) {
            // Under knockback
            player.pos.x += player.vel.x * dt
        } else {
            // Normal Horizontal Motion
            if (abs(moveX) > 0.12f) {
                player.facingRight = moveX > 0f
                val spd = player.speedStat
                player.vel.x = if (moveX > 0f) spd else -spd
                player.pos.x += player.vel.x * dt
            } else {
                player.vel.x = 0f
            }

            // Jump
            if (jumpPressed && player.isGrounded) {
                player.vel.y = -520f
                player.isGrounded = false
                audioManager.playSound(GameAudioManager.SoundType.JUMP)
                spawnParticle(player.pos.x, groundY, 0f, -30f, 4f, 0x88FFFFFF, 0.2f)
            }
        }

        // Vertical Gravity and Platforms
        if (!player.isDashing) {
            player.vel.y += 1200f * dt
            player.pos.y += player.vel.y * dt

            // Check platforms from above
            var landed = false
            if (player.vel.y >= 0f) {
                for (plat in platforms) {
                    if (player.pos.x >= plat.x - 10f && player.pos.x <= plat.x + plat.width + 10f) {
                        if (player.pos.y >= plat.y && player.pos.y - player.vel.y * dt <= plat.y + 12f) {
                            player.pos.y = plat.y
                            player.vel.y = 0f
                            player.isGrounded = true
                            landed = true
                            break
                        }
                    }
                }
            }

            // Ground floor check
            if (!landed) {
                if (player.pos.y >= groundY) {
                    player.pos.y = groundY
                    player.vel.y = 0f
                    player.isGrounded = true
                } else {
                    player.isGrounded = false
                }
            }
        }

        // Clamp to stage limits
        player.pos.x = player.pos.x.coerceIn(30f, stageLength - 30f)

        // Auto-attack when button held
        if (attackHeld && player.attackCooldownTimer <= 0f) {
            triggerPlayerAttack()
        }

        // Animation State Selection
        player.animTimer += dt
        if (!player.isAlive()) {
            player.animState = AnimState.DEATH
        } else if (player.isDashing) {
            player.animState = AnimState.DASH
        } else if (player.abilityTimer > 0f) {
            player.animState = AnimState.ABILITY
            player.abilityTimer -= dt
        } else if (player.knockbackTimer > 0f) {
            player.animState = AnimState.KNOCKBACK
        } else if (player.attackCooldownTimer > 0f) {
            player.animState = if (player.comboStep >= 3) AnimState.ATTACK_HEAVY else AnimState.ATTACK_LIGHT
        } else if (!player.isGrounded) {
            player.animState = if (player.vel.y < 0f) AnimState.JUMP else AnimState.FALL
        } else if (abs(player.vel.x) > 10f) {
            player.animState = AnimState.RUN
        } else {
            player.animState = AnimState.IDLE
        }

        player.pose.compute(player.animState, player.animTimer, player.facingRight)
    }

    fun requestJump() {
        if (player.isGrounded && player.isAlive() && !player.isDashing) {
            player.vel.y = -520f
            player.isGrounded = false
            audioManager.playSound(GameAudioManager.SoundType.JUMP)
            spawnParticle(player.pos.x, player.pos.y, 0f, -40f, 4f, 0x99FFFFFF, 0.2f)
        }
    }

    fun requestPlayerDash() {
        if (player.dashCooldownTimer > 0f || player.isDashing || !player.isAlive()) return

        player.isDashing = true
        player.dashTimer = 0.22f
        player.dashCooldownTimer = player.dashCooldownMax
        player.invulnerableTimer = 0.28f

        val dir = if (player.facingRight) 1f else -1f
        player.vel.x = dir * (player.speedStat * 3.4f).coerceAtLeast(680f)
        player.vel.y = 0f

        audioManager.playSound(GameAudioManager.SoundType.DASH)
        audioManager.vibrate(30, 160)
    }

    fun triggerPlayerAttack() {
        if (!player.isAlive() || player.attackCooldownTimer > 0f) return

        val speedMod = weaponDef.attackSpeed
        player.attackCooldownTimer = 1.0f / speedMod

        player.comboStep = (player.comboStep + 1) % 4
        player.comboTimer = 0.9f
        player.comboDisplayCount++
        player.comboDisplayTimer = 1.8f

        val isFinisher = player.comboStep == 3
        if (isFinisher) {
            audioManager.playSound(GameAudioManager.SoundType.HEAVY_ATTACK)
            triggerScreenShake(5f)
        } else {
            audioManager.playSound(GameAudioManager.SoundType.ATTACK)
        }

        val dir = if (player.facingRight) 1f else -1f

        if (weaponDef.isRanged) {
            // Spawn Ranged Projectile
            val projSpeed = if (weaponDef.projectileSpeed > 100f) weaponDef.projectileSpeed else 620f
            val px = player.pos.x + dir * 30f
            val py = player.pos.y - 25f
            val dmg = (player.attackStat * (if (isFinisher) 1.6f else 1.0f))
            spawnProjectile(px, py, dir * projSpeed, 0f, 5f, weaponDef.color, dmg, isPlayer = true, pierce = isFinisher)
        } else {
            // Melee Hit Detection Arc
            val hitRange = weaponDef.range + (if (isFinisher) 25f else 0f)
            var hitAny = false

            for (e in enemies) {
                if (!e.isAlive()) continue
                val dx = e.pos.x - player.pos.x
                val dy = e.pos.y - player.pos.y

                // Must be in front of player
                if ((dir > 0 && dx in 0f..hitRange) || (dir < 0 && dx in -hitRange..0f)) {
                    if (abs(dy) <= 65f) {
                        hitAny = true
                        val isCrit = isFinisher || Random.nextFloat() < 0.22f
                        val rawDmg = player.attackStat * (if (isFinisher) 1.55f else 1.0f)
                        val finalDmg = calculateDamage(rawDmg, e.defenseStat, isCrit)
                        applyDamageToEnemy(e, finalDmg, isCrit)

                        // Knockback
                        e.vel.x = dir * (if (isFinisher) 280f else 140f)
                        e.hitFlashTimer = 0.14f
                    }
                }
            }

            if (hitAny) {
                audioManager.playSound(GameAudioManager.SoundType.HIT)
                audioManager.vibrate(35, 180)
                triggerScreenShake(if (isFinisher) 6f else 3f)
                spawnSlashEffect(player.pos.x + dir * 35f, player.pos.y - 20f, weaponDef.color)
            }
        }
    }

    fun requestPlayerAbility() {
        if (player.abilityCooldownTimer > 0f || !player.isAlive()) return

        player.abilityCooldownTimer = characterDef.abilityCooldownSec
        player.abilityTimer = 0.35f
        triggerScreenShake(8f)
        audioManager.vibrate(60, 250)

        val dir = if (player.facingRight) 1f else -1f

        when (characterDef.id) {
            com.example.game.model.CharacterId.BLADE -> {
                audioManager.playSound(GameAudioManager.SoundType.ATTACK)
                // Whirlwind dash slashes
                player.invulnerableTimer = 0.4f
                player.pos.x += dir * 180f
                for (e in enemies) {
                    if (e.isAlive() && abs(e.pos.x - player.pos.x) < 140f && abs(e.pos.y - player.pos.y) < 60f) {
                        applyDamageToEnemy(e, player.attackStat * 2.5f, isCritical = true)
                    }
                }
                spawnBurstParticles(player.pos.x, player.pos.y - 20f, playerSkin.glowColor, 12)
            }
            com.example.game.model.CharacterId.GUNNER -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_LIGHTNING)
                // 360 Bullet storm
                for (angleDeg in 0 until 360 step 45) {
                    val rad = angleDeg * PI.toFloat() / 180f
                    val spd = 580f
                    spawnProjectile(player.pos.x, player.pos.y - 20f, cos(rad) * spd, sin(rad) * spd, 4.5f, 0xFFFFD600, player.attackStat * 1.2f, isPlayer = true, pierce = true)
                }
            }
            com.example.game.model.CharacterId.HAMMER -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                triggerScreenShake(12f)
                // Earth smash quakes
                for (e in enemies) {
                    if (e.isAlive() && abs(e.pos.x - player.pos.x) < 220f) {
                        applyDamageToEnemy(e, player.attackStat * 2.8f, isCritical = true)
                        e.vel.y = -280f
                    }
                }
                spawnBurstParticles(player.pos.x + dir * 40f, groundY, 0xFFFF9100, 14)
            }
            com.example.game.model.CharacterId.NINJA -> {
                audioManager.playSound(GameAudioManager.SoundType.HIT)
                // Teleport to nearest enemy
                var targetE: EnemyWarrior? = null
                var closestDist = 9999f
                for (e in enemies) {
                    if (e.isAlive()) {
                        val d = abs(e.pos.x - player.pos.x)
                        if (d < closestDist && d < 400f) {
                            closestDist = d
                            targetE = e
                        }
                    }
                }
                if (targetE != null) {
                    player.pos.x = targetE.pos.x - dir * 40f
                    applyDamageToEnemy(targetE, player.attackStat * 3.0f, isCritical = true)
                } else {
                    player.pos.x += dir * 220f
                }
                spawnBurstParticles(player.pos.x, player.pos.y - 20f, 0xFFE040FB, 10)
            }
            com.example.game.model.CharacterId.ARCHER -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_ICE)
                // Rain of arrows from sky
                for (offset in -80..80 step 40) {
                    val px = player.pos.x + dir * 140f + offset
                    spawnProjectile(px, player.pos.y - 320f, 0f, 650f, 4f, 0xFF76FF03, player.attackStat * 1.5f, isPlayer = true)
                }
            }
            com.example.game.model.CharacterId.SPEARMAN -> {
                audioManager.playSound(GameAudioManager.SoundType.DASH)
                player.invulnerableTimer = 0.5f
                player.vel.x = dir * 650f
                player.pos.x += dir * 200f
                for (e in enemies) {
                    if (e.isAlive() && abs(e.pos.x - player.pos.x) < 180f) {
                        applyDamageToEnemy(e, player.attackStat * 2.2f, isCritical = true)
                        e.vel.x = dir * 300f
                    }
                }
                spawnBurstParticles(player.pos.x, player.pos.y - 20f, 0xFF2979FF, 10)
            }
            com.example.game.model.CharacterId.FLAME -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                for (e in enemies) {
                    if (e.isAlive() && abs(e.pos.x - player.pos.x) < 170f) {
                        applyDamageToEnemy(e, player.attackStat * 2.4f, isCritical = true)
                    }
                }
                spawnBurstParticles(player.pos.x, player.pos.y - 20f, 0xFFFF3D00, 16)
            }
            com.example.game.model.CharacterId.CYBER -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_LIGHTNING)
                // Overcharged cannon laser
                spawnProjectile(player.pos.x + dir * 40f, player.pos.y - 22f, dir * 750f, 0f, 12f, 0xFF00E5FF, player.attackStat * 3.2f, isPlayer = true, pierce = true)
            }
            com.example.game.model.CharacterId.PHANTOM -> {
                audioManager.playSound(GameAudioManager.SoundType.HIT)
                player.invulnerableTimer = 1.8f // Ethereal phase
                spawnBurstParticles(player.pos.x, player.pos.y - 20f, 0xFF7C4DFF, 12)
            }
            com.example.game.model.CharacterId.DRAGON -> {
                audioManager.playSound(GameAudioManager.SoundType.ABILITY_FIRE)
                // Dragon wave projectile
                spawnProjectile(player.pos.x + dir * 45f, player.pos.y - 25f, dir * 550f, 0f, 16f, 0xFFFF1744, player.attackStat * 3.8f, isPlayer = true, pierce = true)
                spawnBurstParticles(player.pos.x, player.pos.y - 20f, 0xFFFF1744, 16)
            }
        }
    }

    private fun checkSpawners() {
        for (i in spawnTriggerXs.indices) {
            val trigX = spawnTriggerXs[i]
            if (!spawnTriggered[i] && player.pos.x >= trigX) {
                spawnTriggered[i] = true

                if (stageNumber == 4 && i == spawnTriggerXs.size - 1) {
                    // Spawn World Boss!
                    spawnBoss()
                } else {
                    // Spawn standard/elite wave
                    val eType = when (i % 3) {
                        0 -> EnemyType.BASIC_FIGHTER
                        1 -> EnemyType.FAST_FIGHTER
                        else -> EnemyType.HEAVY_FIGHTER
                    }
                    spawnEnemyAt(trigX + 300f, eType)
                    if (stageNumber >= 2) {
                        spawnEnemyAt(trigX + 380f, EnemyType.RANGED_FIGHTER)
                    }
                    if (stageNumber >= 3 && i == 3) {
                        spawnEnemyAt(trigX + 440f, EnemyType.ELITE_FIGHTER)
                    }
                }
            }
        }
    }

    private fun spawnBoss() {
        isBossFightTriggered = true
        audioManager.playSound(GameAudioManager.SoundType.WARNING)
        audioManager.vibrate(60, 240)
        triggerScreenShake(8f)

        for (e in enemies) {
            if (!e.active) {
                e.spawn(
                    eType = EnemyType.BOSS,
                    spawnX = stageLength - 280f,
                    spawnY = groundY,
                    customBossDef = worldDef.bossDef
                )
                activeBoss = e
                break
            }
        }
    }

    private fun spawnEnemyAt(x: Float, type: EnemyType) {
        val clampedX = x.coerceIn(100f, stageLength - 60f)
        for (e in enemies) {
            if (!e.active) {
                e.spawn(type, clampedX, groundY, hpScale = 1f + (stageNumber - 1) * 0.15f)
                break
            }
        }
    }

    private fun updateEnemies(dt: Float) {
        for (e in enemies) {
            if (!e.active) continue

            // Death fade check
            if (!e.isAlive()) {
                e.deathFadeTimer += dt
                e.animState = AnimState.DEATH
                e.pose.compute(e.animState, e.animTimer, e.facingRight)
                if (e.deathFadeTimer >= 0.7f) {
                    e.active = false
                }
                continue
            }

            // Timers
            if (e.attackCooldownTimer > 0f) e.attackCooldownTimer -= dt
            if (e.hitFlashTimer > 0f) e.hitFlashTimer -= dt
            e.animTimer += dt

            val distToPlayer = player.pos.distanceTo(e.pos)
            val dx = player.pos.x - e.pos.x
            e.facingRight = dx > 0f

            // Boss Pattern Handling
            if (e.isBoss) {
                e.bossPatternTimer -= dt
                if (e.bossPatternTimer <= 0f) {
                    e.bossPattern = (e.bossPattern + 1) % 3
                    e.bossPatternTimer = 2.8f
                    audioManager.playSound(GameAudioManager.SoundType.BOSS_ATTACK)

                    when (e.bossPattern) {
                        0 -> {
                            // Charge rush
                            e.vel.x = (if (e.facingRight) 1f else -1f) * 320f
                        }
                        1 -> {
                            // Heavy slam shockwave
                            triggerScreenShake(7f)
                            spawnBurstParticles(e.pos.x, groundY, worldDef.bossDef.color, 12)
                            if (distToPlayer < 240f) {
                                applyDamageToPlayer(e.attackStat * 1.3f)
                            }
                        }
                        2 -> {
                            // Fire energy orbs
                            val dir = if (e.facingRight) 1f else -1f
                            spawnProjectile(e.pos.x, e.pos.y - 25f, dir * 420f, 0f, 7f, worldDef.bossDef.color, e.attackStat * 0.9f, isPlayer = false)
                        }
                    }
                }
            }

            // Simple AI Logic
            e.aiDecisionTimer -= dt
            if (e.aiDecisionTimer <= 0f) {
                e.aiDecisionTimer = 0.2f
                val hpRatio = e.hp / e.maxHp
                e.aiState = when {
                    hpRatio < 0.25f && distToPlayer < 80f && !e.isBoss -> AIBehaviorState.RETREAT
                    distToPlayer <= e.attackRange + 10f -> AIBehaviorState.ATTACK
                    distToPlayer <= 500f -> AIBehaviorState.FOLLOW
                    else -> AIBehaviorState.IDLE
                }
            }

            // Execute Movement
            when (e.aiState) {
                AIBehaviorState.FOLLOW -> {
                    val dir = if (dx > 0) 1f else -1f
                    e.vel.x = dir * e.speedStat
                    e.pos.x += e.vel.x * dt
                    e.animState = AnimState.RUN
                }
                AIBehaviorState.RETREAT -> {
                    val dir = if (dx > 0) -1f else 1f
                    e.vel.x = dir * (e.speedStat * 0.8f)
                    e.pos.x += e.vel.x * dt
                    e.animState = AnimState.RUN
                }
                AIBehaviorState.ATTACK -> {
                    e.vel.x = 0f
                    e.animState = AnimState.ATTACK_LIGHT
                    if (e.attackCooldownTimer <= 0f) {
                        e.attackCooldownTimer = if (e.type == EnemyType.RANGED_FIGHTER) 1.8f else 1.1f
                        if (e.type == EnemyType.RANGED_FIGHTER) {
                            val dir = if (e.facingRight) 1f else -1f
                            spawnProjectile(e.pos.x + dir * 25f, e.pos.y - 20f, dir * 380f, 0f, 4f, e.type.color, e.attackStat, isPlayer = false)
                        } else {
                            if (distToPlayer <= e.attackRange + player.radius) {
                                applyDamageToPlayer(e.attackStat)
                            }
                        }
                    }
                }
                AIBehaviorState.IDLE -> {
                    e.vel.x = 0f
                    e.animState = AnimState.IDLE
                }
                else -> {}
            }

            // Ground Clamp
            e.pos.y = groundY
            e.pos.x = e.pos.x.coerceIn(40f, stageLength - 40f)
            e.pose.compute(e.animState, e.animTimer, e.facingRight)
        }
    }

    private fun updateProjectiles(dt: Float) {
        for (p in projectiles) {
            if (!p.active) continue
            p.update(dt)

            // Screen bounds cull
            if (p.x < cameraX - 50f || p.x > cameraX + viewportWidth + 50f || p.y < 0f || p.y > groundY + 20f) {
                p.active = false
                continue
            }

            if (p.isPlayerSource) {
                // Check enemy hits
                for (e in enemies) {
                    if (!e.isAlive()) continue
                    val dist = Vector2(p.x, p.y).distanceTo(e.pos)
                    if (dist <= p.radius + e.radius) {
                        applyDamageToEnemy(e, p.damage, isCritical = false)
                        spawnParticle(p.x, p.y, -p.vx * 0.2f, -p.vy * 0.2f, 3.5f, p.color, 0.2f)
                        if (!p.piercing) {
                            p.active = false
                            break
                        }
                    }
                }
            } else {
                // Check player hit
                val dist = Vector2(p.x, p.y).distanceTo(player.pos)
                if (dist <= p.radius + player.radius) {
                    applyDamageToPlayer(p.damage)
                    spawnParticle(p.x, p.y, -p.vx * 0.2f, 0f, 3.5f, p.color, 0.2f)
                    p.active = false
                }
            }
        }
    }

    private fun applyDamageToPlayer(rawDmg: Float) {
        if (player.invulnerableTimer > 0f || !player.isAlive()) return

        val mitigation = 100f / (100f + player.defenseStat)
        val finalDmg = (rawDmg * mitigation).coerceAtLeast(8f)

        player.hp = (player.hp - finalDmg).coerceAtLeast(0f)
        player.hitFlashTimer = 0.14f
        player.invulnerableTimer = 0.35f
        player.knockbackTimer = 0.16f
        player.vel.x = (if (player.facingRight) -1f else 1f) * 160f

        spawnDamageNumber("${finalDmg.toInt()}", player.pos.x, player.pos.y - 35f, 0xFFFF5252)
        audioManager.playSound(GameAudioManager.SoundType.HIT)
        audioManager.vibrate(45, 200)
        triggerScreenShake(4.5f)

        if (player.hp <= 0f) {
            isGameOver = true
            audioManager.playSound(GameAudioManager.SoundType.DEFEAT)
            audioManager.vibrate(80, 250)
        }
    }

    private fun applyDamageToEnemy(e: EnemyWarrior, dmg: Float, isCritical: Boolean) {
        e.hp = (e.hp - dmg).coerceAtLeast(0f)
        e.hitFlashTimer = 0.12f

        spawnDamageNumber("${dmg.toInt()}", e.pos.x, e.pos.y - 30f, if (isCritical) 0xFFFFD700 else 0xFFFFFFFF, isCritical)

        // Hit sparks
        for (i in 0 until 3) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val spd = Random.nextFloat() * 100f + 40f
            spawnParticle(e.pos.x, e.pos.y - 20f, cos(ang) * spd, sin(ang) * spd, 3.5f, weaponDef.color, 0.22f)
        }

        if (e.hp <= 0f) {
            enemiesKilled++
            xpEarned += (e.maxHp * 0.2f).toInt()
            audioManager.playSound(GameAudioManager.SoundType.ENEMY_DEATH)

            // Spawn coins
            val coinCount = if (e.isBoss) 6 else if (e.type == EnemyType.ELITE_FIGHTER) 3 else 1
            for (i in 0 until coinCount) {
                spawnCoin(e.pos.x + (i * 12f - 6f), e.pos.y - 10f, if (e.isBoss) 25 else 10)
            }

            if (e.isBoss) {
                bossDefeated = true
                coinsCollected += 200
                xpEarned += 250
            }
        }
    }

    private fun calculateDamage(attack: Float, defense: Float, isCritical: Boolean): Float {
        val mitigation = 100f / (100f + defense.coerceAtLeast(0f))
        val raw = attack * mitigation
        return (if (isCritical) raw * 1.5f else raw).coerceAtLeast(10f)
    }

    private fun triggerScreenShake(intensity: Float) {
        screenShakeIntensity = intensity.coerceAtLeast(screenShakeIntensity)
    }

    private fun spawnProjectile(x: Float, y: Float, vx: Float, vy: Float, rad: Float, clr: Long, dmg: Float, isPlayer: Boolean, pierce: Boolean = false) {
        for (p in projectiles) {
            if (!p.active) {
                p.spawn(x, y, vx, vy, rad, clr, dmg, isPlayer, pierce = pierce)
                return
            }
        }
    }

    private fun spawnParticle(x: Float, y: Float, vx: Float, vy: Float, rad: Float, clr: Long, dur: Float, slash: Boolean = false) {
        for (pt in particles) {
            if (!pt.active) {
                pt.init(x, y, vx, vy, rad, clr, dur, slash)
                return
            }
        }
    }

    private fun spawnSlashEffect(x: Float, y: Float, clr: Long) {
        for (i in 0 until 4) {
            val ang = (i * 0.4f - 0.6f)
            val spd = 120f
            spawnParticle(x, y, cos(ang) * spd, sin(ang) * spd, 4f, clr, 0.16f, slash = true)
        }
    }

    private fun spawnBurstParticles(x: Float, y: Float, clr: Long, count: Int) {
        val c = count.coerceAtMost(16)
        for (i in 0 until c) {
            val ang = (i * 2f * PI.toFloat()) / c
            val spd = Random.nextFloat() * 120f + 60f
            spawnParticle(x, y, cos(ang) * spd, sin(ang) * spd, 4f, clr, 0.28f)
        }
    }

    private fun spawnDamageNumber(t: String, x: Float, y: Float, clr: Long, isCrit: Boolean = false) {
        for (d in damageNumbers) {
            if (!d.active) {
                d.init(t, x, y, clr, isCrit)
                return
            }
        }
    }

    private fun spawnCoin(x: Float, y: Float, value: Int) {
        for (c in coins) {
            if (!c.active) {
                c.spawn(x, y, value)
                return
            }
        }
    }
}

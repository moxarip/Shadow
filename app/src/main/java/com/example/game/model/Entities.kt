package com.example.game.model

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2(var x: Float = 0f, var y: Float = 0f) {
    fun length(): Float = sqrt(x * x + y * y)

    fun normalize(): Vector2 {
        val len = length()
        return if (len > 0.0001f) Vector2(x / len, y / len) else Vector2(0f, 0f)
    }

    fun distanceTo(other: Vector2): Float {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }

    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)
}

/**
 * 2D Procedural Stick-Figure Pose Structure.
 * Angles in radians, offsets in local units.
 * Calculated smoothly with zero memory allocations per frame.
 */
class StickFigurePose {
    var headOffsetY: Float = -38f
    var torsoAngle: Float = 0f
    var leftUpperArmAngle: Float = 0.5f
    var leftForearmAngle: Float = 0.8f
    var rightUpperArmAngle: Float = -0.5f
    var rightForearmAngle: Float = -0.2f
    var leftThighAngle: Float = 0.4f
    var leftShinAngle: Float = 0.2f
    var rightThighAngle: Float = -0.4f
    var rightShinAngle: Float = 0.2f
    var weaponAngle: Float = -0.3f
    var weaponExtend: Float = 26f
    var crouchY: Float = 0f

    fun compute(state: AnimState, t: Float, facingRight: Boolean, speedRatio: Float = 1f) {
        val dir = if (facingRight) 1f else -1f

        when (state) {
            AnimState.IDLE -> {
                val breath = sin(t * 3f)
                headOffsetY = -38f + breath * 1.5f
                torsoAngle = breath * 0.04f * dir
                crouchY = breath * 1.2f
                leftUpperArmAngle = 0.6f + breath * 0.08f
                leftForearmAngle = 0.9f
                rightUpperArmAngle = -0.4f - breath * 0.08f
                rightForearmAngle = -0.2f
                leftThighAngle = 0.2f
                leftShinAngle = 0.1f
                rightThighAngle = -0.2f
                rightShinAngle = 0.1f
                weaponAngle = -0.4f + breath * 0.05f
            }
            AnimState.WALK, AnimState.RUN -> {
                val cycleSpeed = if (state == AnimState.RUN) 14f else 9f
                val stride = sin(t * cycleSpeed)
                val bounce = kotlin.math.abs(cos(t * cycleSpeed))
                headOffsetY = -38f - bounce * 3f
                crouchY = bounce * 2.5f
                torsoAngle = 0.18f * dir

                // Scissor legs
                leftThighAngle = stride * 0.75f
                leftShinAngle = (stride * 0.5f).coerceAtLeast(0f)
                rightThighAngle = -stride * 0.75f
                rightShinAngle = (-stride * 0.5f).coerceAtLeast(0f)

                // Arm swing
                leftUpperArmAngle = -stride * 0.6f
                leftForearmAngle = 0.7f
                rightUpperArmAngle = stride * 0.6f
                rightForearmAngle = -0.3f
                weaponAngle = 0.2f + stride * 0.3f
            }
            AnimState.JUMP -> {
                headOffsetY = -41f
                crouchY = -4f
                torsoAngle = -0.1f * dir
                leftThighAngle = -0.5f
                leftShinAngle = 0.8f
                rightThighAngle = -0.2f
                rightShinAngle = 0.6f
                leftUpperArmAngle = -1.2f
                rightUpperArmAngle = -1.0f
                weaponAngle = -1.4f
            }
            AnimState.FALL -> {
                headOffsetY = -37f
                crouchY = 0f
                torsoAngle = 0.05f * dir
                leftThighAngle = 0.2f
                leftShinAngle = 0.3f
                rightThighAngle = -0.1f
                rightShinAngle = 0.3f
                leftUpperArmAngle = -0.6f
                rightUpperArmAngle = -0.4f
                weaponAngle = -0.5f
            }
            AnimState.ATTACK_LIGHT -> {
                val progress = (t * 8f).coerceIn(0f, 1f)
                torsoAngle = 0.25f * dir
                headOffsetY = -37f
                rightUpperArmAngle = -1.5f + progress * 2.4f
                rightForearmAngle = -0.2f + progress * 0.6f
                leftUpperArmAngle = 0.8f
                weaponAngle = -1.6f + progress * 2.8f
                leftThighAngle = 0.4f
                rightThighAngle = -0.5f
            }
            AnimState.ATTACK_HEAVY -> {
                val progress = (t * 6f).coerceIn(0f, 1f)
                torsoAngle = 0.35f * dir
                headOffsetY = -36f
                crouchY = 3f
                rightUpperArmAngle = -2.2f + progress * 3.4f
                rightForearmAngle = -0.4f + progress * 0.8f
                weaponAngle = -2.4f + progress * 4.0f
                leftThighAngle = 0.6f
                rightThighAngle = -0.6f
            }
            AnimState.COMBO -> {
                val progress = (t * 10f) % (2f * PI.toFloat())
                torsoAngle = sin(progress) * 0.4f * dir
                headOffsetY = -38f
                rightUpperArmAngle = sin(progress) * 1.8f
                leftUpperArmAngle = -sin(progress) * 1.8f
                weaponAngle = progress * 2f * dir
                leftThighAngle = 0.3f
                rightThighAngle = -0.3f
            }
            AnimState.DASH -> {
                headOffsetY = -34f
                crouchY = 6f
                torsoAngle = 0.5f * dir
                leftThighAngle = 0.8f
                leftShinAngle = 0.2f
                rightThighAngle = -0.9f
                rightShinAngle = 0.4f
                leftUpperArmAngle = 1.2f
                rightUpperArmAngle = 1.4f
                weaponAngle = 0.6f
            }
            AnimState.ABILITY -> {
                headOffsetY = -39f
                crouchY = 2f
                torsoAngle = 0.2f * dir
                rightUpperArmAngle = -1.8f
                leftUpperArmAngle = -1.8f
                weaponAngle = -1.8f
                leftThighAngle = 0.3f
                rightThighAngle = -0.3f
            }
            AnimState.HIT, AnimState.KNOCKBACK -> {
                headOffsetY = -36f
                crouchY = 2f
                torsoAngle = -0.35f * dir
                leftUpperArmAngle = 1.0f
                rightUpperArmAngle = 1.1f
                leftThighAngle = -0.4f
                rightThighAngle = 0.4f
                weaponAngle = 0.8f
            }
            AnimState.DEATH -> {
                crouchY = 16f
                headOffsetY = -20f
                torsoAngle = 1.3f * dir
                leftUpperArmAngle = 1.8f
                rightUpperArmAngle = 1.9f
                weaponAngle = 1.4f
            }
            AnimState.VICTORY -> {
                val cheer = sin(t * 4f)
                headOffsetY = -40f + cheer * 1.5f
                torsoAngle = 0f
                rightUpperArmAngle = -2.4f + cheer * 0.2f
                rightForearmAngle = -0.1f
                leftUpperArmAngle = -2.2f
                weaponAngle = -2.6f
                leftThighAngle = 0.2f
                rightThighAngle = -0.2f
            }
        }
    }
}

/**
 * Player Shadow Warrior.
 */
class PlayerWarrior(
    var characterDef: CharacterDef,
    var skinDef: SkinDef,
    var weaponDef: WeaponDef,
    var maxHp: Float,
    var attackStat: Float,
    var defenseStat: Float,
    var speedStat: Float
) {
    val pos = Vector2(100f, 480f)
    val vel = Vector2(0f, 0f)
    var facingRight: Boolean = true
    var isGrounded: Boolean = true

    var hp: Float = maxHp
    var energy: Float = 100f
    val maxEnergy: Float = 100f

    var animState: AnimState = AnimState.IDLE
    var animTimer: Float = 0f
    val pose = StickFigurePose()

    // Combat & Combos
    var comboStep: Int = 0 // 0..3
    var comboTimer: Float = 0f
    var comboDisplayCount: Int = 0
    var comboDisplayTimer: Float = 0f

    // Action Cooldowns
    var attackCooldownTimer: Float = 0f
    var dashCooldownTimer: Float = 0f
    val dashCooldownMax: Float = 1.4f
    var abilityCooldownTimer: Float = 0f
    var abilityTimer: Float = 0f

    // States & Immunities
    var isDashing: Boolean = false
    var dashTimer: Float = 0f
    var invulnerableTimer: Float = 0f
    var hitFlashTimer: Float = 0f
    var knockbackTimer: Float = 0f

    val radius: Float = 22f

    fun isAlive(): Boolean = hp > 0f

    fun reset(spawnX: Float, spawnY: Float) {
        pos.x = spawnX
        pos.y = spawnY
        vel.x = 0f
        vel.y = 0f
        facingRight = true
        isGrounded = true
        hp = maxHp
        energy = 100f
        animState = AnimState.IDLE
        animTimer = 0f
        comboStep = 0
        comboTimer = 0f
        comboDisplayCount = 0
        attackCooldownTimer = 0f
        dashCooldownTimer = 0f
        abilityCooldownTimer = 0f
        isDashing = false
        invulnerableTimer = 0f
        hitFlashTimer = 0f
        knockbackTimer = 0f
    }
}

/**
 * Lightweight Enemy Warrior with simple state AI.
 */
class EnemyWarrior {
    var active: Boolean = false
    var type: EnemyType = EnemyType.BASIC_FIGHTER
    val pos = Vector2()
    val vel = Vector2()
    var facingRight: Boolean = false
    var isGrounded: Boolean = true

    var hp: Float = 100f
    var maxHp: Float = 100f
    var attackStat: Float = 25f
    var defenseStat: Float = 5f
    var speedStat: Float = 120f
    var attackRange: Float = 55f

    var isBoss: Boolean = false
    var bossDef: BossDef? = null
    var bossPattern: Int = 0
    var bossPatternTimer: Float = 0f
    var bossAttackWarningTimer: Float = 0f

    var animState: AnimState = AnimState.IDLE
    var animTimer: Float = 0f
    val pose = StickFigurePose()

    var aiState: AIBehaviorState = AIBehaviorState.IDLE
    var aiDecisionTimer: Float = 0.2f
    var attackCooldownTimer: Float = 0f
    var hitFlashTimer: Float = 0f
    var deathFadeTimer: Float = 0f

    val radius: Float = 22f

    fun isAlive(): Boolean = active && hp > 0f

    fun spawn(
        eType: EnemyType,
        spawnX: Float,
        spawnY: Float,
        customBossDef: BossDef? = null,
        hpScale: Float = 1f
    ) {
        type = eType
        pos.x = spawnX
        pos.y = spawnY
        vel.x = 0f
        vel.y = 0f
        facingRight = false
        isGrounded = true
        isBoss = customBossDef != null

        bossDef = customBossDef
        bossPattern = 0
        bossPatternTimer = 2.5f
        bossAttackWarningTimer = 0f

        maxHp = (customBossDef?.baseHp ?: eType.baseHp) * hpScale
        hp = maxHp
        attackStat = (customBossDef?.baseAttack ?: (eType.baseHp * 0.22f)) * (1f + (hpScale - 1f) * 0.25f)
        defenseStat = customBossDef?.baseDefense ?: 8f
        speedStat = eType.speed
        attackRange = if (eType == EnemyType.RANGED_FIGHTER) 320f else if (isBoss) 95f else 55f

        animState = AnimState.IDLE
        animTimer = 0f
        aiState = AIBehaviorState.DETECT
        aiDecisionTimer = 0.1f
        attackCooldownTimer = 0.5f
        hitFlashTimer = 0f
        deathFadeTimer = 0f
        active = true
    }
}

/**
 * Preallocated Pooled Projectile.
 */
class PooledProjectile {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var radius: Float = 4f
    var color: Long = 0xFFFFFFFF
    var damage: Float = 20f
    var isPlayerSource: Boolean = true
    var piercing: Boolean = false
    var life: Float = 0f
    var maxLife: Float = 1.5f

    fun spawn(
        px: Float, py: Float,
        pvx: Float, pvy: Float,
        rad: Float, clr: Long,
        dmg: Float, isPlayer: Boolean,
        dur: Float = 1.2f, pierce: Boolean = false
    ) {
        x = px
        y = py
        vx = pvx
        vy = pvy
        radius = rad
        color = clr
        damage = dmg
        isPlayerSource = isPlayer
        piercing = pierce
        life = 0f
        maxLife = dur
        active = true
    }

    fun update(dt: Float) {
        if (!active) return
        life += dt
        x += vx * dt
        y += vy * dt
        if (life >= maxLife) {
            active = false
        }
    }
}

/**
 * Preallocated Combat Particle.
 */
class PooledParticle {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var radius: Float = 3f
    var color: Long = 0xFFFFFFFF
    var life: Float = 0f
    var maxLife: Float = 0.25f
    var isSlashTrail: Boolean = false

    fun init(px: Float, py: Float, pvx: Float, pvy: Float, rad: Float, clr: Long, dur: Float, slash: Boolean = false) {
        x = px
        y = py
        vx = pvx
        vy = pvy
        radius = rad
        color = clr
        life = 0f
        maxLife = dur
        isSlashTrail = slash
        active = true
    }

    fun update(dt: Float) {
        if (!active) return
        life += dt
        x += vx * dt
        y += vy * dt
        if (life >= maxLife) {
            active = false
        }
    }
}

/**
 * Preallocated Floating Damage Number.
 */
class PooledDamageNumber {
    var active: Boolean = false
    var text: String = ""
    var x: Float = 0f
    var y: Float = 0f
    var color: Long = 0xFFFFFFFF
    var life: Float = 0f
    var maxLife: Float = 0.55f
    var isCrit: Boolean = false

    fun init(t: String, px: Float, py: Float, clr: Long, crit: Boolean = false) {
        text = t
        x = px
        y = py
        color = clr
        isCrit = crit
        life = 0f
        maxLife = 0.55f
        active = true
    }

    fun update(dt: Float) {
        if (!active) return
        life += dt
        y -= 42f * dt
        if (life >= maxLife) {
            active = false
        }
    }
}

/**
 * Preallocated Collectible Coin.
 */
class PooledCoin {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vy: Float = -120f
    var value: Int = 10
    var life: Float = 0f

    fun spawn(px: Float, py: Float, valAmt: Int = 10) {
        x = px
        y = py
        vy = -160f
        value = valAmt
        life = 0f
        active = true
    }

    fun update(dt: Float, groundY: Float) {
        if (!active) return
        life += dt
        if (y < groundY) {
            vy += 500f * dt
            y += vy * dt
            if (y >= groundY) {
                y = groundY
                vy = 0f
            }
        }
        if (life >= 12f) {
            active = false
        }
    }
}

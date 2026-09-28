package com.example.game.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

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

    fun angleTo(other: Vector2): Float = atan2(other.y - y, other.x - x)

    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)
}

class PlayerState(
    var pos: Vector2 = Vector2(300f, 450f),
    var vel: Vector2 = Vector2(0f, 0f),
    var facingAngle: Float = 0f,
    var health: Float = 500f,
    var maxHealth: Float = 500f,
    var energy: Float = 100f,
    val maxEnergy: Float = 100f,
    var dashCooldownTimer: Float = 0f,
    val dashCooldownMax: Float = 1.8f,
    var abilityCooldownTimer: Float = 0f,
    var abilityCooldownMax: Float = 8f,
    var attackCooldownTimer: Float = 0f,
    var isDashing: Boolean = false,
    var dashDurationTimer: Float = 0f,
    var invulnerableTimer: Float = 0f,
    var damageFlashTimer: Float = 0f,
    var attackSwingTimer: Float = 0f,
    var comboCount: Int = 0,
    var comboTimer: Float = 0f,
    val radius: Float = 26f
) {
    fun resetForBattle(heroDef: HeroDef, progress: HeroProgress, arenaCenter: Vector2 = Vector2(300f, 650f)) {
        pos = Vector2(arenaCenter.x, arenaCenter.y)
        vel = Vector2(0f, 0f)
        facingAngle = -1.57f // pointing up
        maxHealth = progress.getHp(heroDef)
        health = maxHealth
        energy = 100f
        dashCooldownTimer = 0f
        abilityCooldownMax = heroDef.abilityCooldownSec
        abilityCooldownTimer = 0f
        attackCooldownTimer = 0f
        isDashing = false
        dashDurationTimer = 0f
        invulnerableTimer = 0f
        damageFlashTimer = 0f
        attackSwingTimer = 0f
        comboCount = 0
        comboTimer = 0f
    }
}

class EnemyEntity(
    val id: Long,
    val type: EnemyType,
    var pos: Vector2,
    var vel: Vector2 = Vector2(0f, 0f),
    var facingAngle: Float = 0f,
    var health: Float,
    var maxHealth: Float,
    val damage: Float,
    val baseSpeed: Float,
    var attackCooldownTimer: Float = 0f,
    var attackWindupTimer: Float = 0f,
    var isWindingUp: Boolean = false,
    var hitFlashTimer: Float = 0f,
    var frozenTimer: Float = 0f,
    var poisonTimer: Float = 0f,
    var poisonTickAcc: Float = 0f,
    var specialAttackTimer: Float = Random.nextFloat() * 3f + 3f,
    val radius: Float,
    val isBoss: Boolean = type.isBoss
) {
    fun isAlive(): Boolean = health > 0f
}

data class Projectile(
    val id: Long,
    var pos: Vector2,
    var vel: Vector2,
    val radius: Float,
    val damage: Float,
    val isPlayer: Boolean,
    var lifeTimer: Float,
    val maxLife: Float,
    val color: Long,
    val pierces: Boolean = false,
    val effectType: ElementType = ElementType.FIRE
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val color: Long,
    var alpha: Float = 1f,
    var life: Float = 0f,
    val maxLife: Float = 0.5f,
    val isSpark: Boolean = false
)

data class DamageNumber(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Long,
    val isCritical: Boolean = false,
    var alpha: Float = 1f,
    var life: Float = 0f,
    val maxLife: Float = 0.85f
)

data class FloatingCoin(
    val id: Long,
    var pos: Vector2,
    var vel: Vector2,
    val value: Int,
    val isGem: Boolean = false,
    var life: Float = 0f
)

enum class GroundZoneType {
    HEALING_AURA,
    POISON_CLOUD,
    BLIZZARD_FROST,
    GROUND_SMASH_CRATER,
    BOSS_WARNING_CIRCLE,
    BOSS_WARNING_LINE,
    BOSS_SHOCKWAVE
}

data class GroundZone(
    val id: Long,
    var x: Float,
    var y: Float,
    var radius: Float,
    var duration: Float,
    val maxDuration: Float,
    val type: GroundZoneType,
    val color: Long,
    val ownerIsPlayer: Boolean,
    var targetX: Float = 0f,
    var targetY: Float = 0f
)

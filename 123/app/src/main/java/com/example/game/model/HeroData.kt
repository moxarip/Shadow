package com.example.game.model

import androidx.compose.ui.graphics.Color

data class HeroDef(
    val id: HeroId,
    val name: String,
    val title: String,
    val element: ElementType,
    val baseHp: Float,
    val baseDamage: Float,
    val baseSpeed: Float,
    val attackRange: Float, // melee or ranged
    val isRanged: Boolean,
    val attackCooldownSec: Float,
    val abilityName: String,
    val abilityDesc: String,
    val abilityCooldownSec: Float,
    val unlockGemsCost: Int,
    val primaryColor: Long,
    val secondaryColor: Long,
    val glowColor: Long
)

data class HeroProgress(
    val heroId: HeroId,
    val level: Int = 1,
    val isUnlocked: Boolean = false
) {
    fun getHp(def: HeroDef): Float = def.baseHp * (1f + (level - 1) * 0.15f)
    fun getDamage(def: HeroDef): Float = def.baseDamage * (1f + (level - 1) * 0.15f)
    fun getAbilityPower(def: HeroDef): Float = 1.0f + (level - 1) * 0.20f
    fun getSpeed(def: HeroDef): Float = def.baseSpeed + (level - 1) * 3f

    fun getUpgradeCost(): Int {
        if (level >= 10) return 0
        return when (level) {
            1 -> 150
            2 -> 350
            3 -> 700
            4 -> 1200
            5 -> 1800
            6 -> 2600
            7 -> 3600
            8 -> 5000
            9 -> 7000
            else -> 10000
        }
    }
}

object HeroRegistry {
    val allHeroes: List<HeroDef> = listOf(
        HeroDef(
            id = HeroId.BLAZE,
            name = "BLAZE",
            title = "Flame Vanguard",
            element = ElementType.FIRE,
            baseHp = 480f,
            baseDamage = 58f,
            baseSpeed = 220f,
            attackRange = 65f,
            isRanged = false,
            attackCooldownSec = 0.35f,
            abilityName = "Fire Explosion",
            abilityDesc = "Erupts in a violent 360° ring of fire, blasting enemies back and inflicting burn damage.",
            abilityCooldownSec = 8.0f,
            unlockGemsCost = 0, // Unlocked initially
            primaryColor = 0xFFFF5722,
            secondaryColor = 0xFFFF9800,
            glowColor = 0xFFFF3D00
        ),
        HeroDef(
            id = HeroId.FROST,
            name = "FROST",
            title = "Ice Archmage",
            element = ElementType.ICE,
            baseHp = 360f,
            baseDamage = 52f,
            baseSpeed = 200f,
            attackRange = 290f,
            isRanged = true,
            attackCooldownSec = 0.45f,
            abilityName = "Absolute Zero",
            abilityDesc = "Summons a glacial frost nova that flash-freezes all enemies for 3.5s and shatters them.",
            abilityCooldownSec = 10.0f,
            unlockGemsCost = 500,
            primaryColor = 0xFF00E5FF,
            secondaryColor = 0xFF80D8FF,
            glowColor = 0xFF00B0FF
        ),
        HeroDef(
            id = HeroId.SHADOW,
            name = "SHADOW",
            title = "Nightblade Assassin",
            element = ElementType.SHADOW,
            baseHp = 340f,
            baseDamage = 72f,
            baseSpeed = 280f,
            attackRange = 55f,
            isRanged = false,
            attackCooldownSec = 0.28f,
            abilityName = "Shadow Dash",
            abilityDesc = "Phases through enemies at lightning velocity, unleashing 5 critical shadow slashes.",
            abilityCooldownSec = 6.5f,
            unlockGemsCost = 800,
            primaryColor = 0xFF9C27B0,
            secondaryColor = 0xFFE040FB,
            glowColor = 0xFF7C4DFF
        ),
        HeroDef(
            id = HeroId.TITAN,
            name = "TITAN",
            title = "Iron Colossus",
            element = ElementType.EARTH,
            baseHp = 780f,
            baseDamage = 62f,
            baseSpeed = 160f,
            attackRange = 80f,
            isRanged = false,
            attackCooldownSec = 0.55f,
            abilityName = "Ground Smash",
            abilityDesc = "Slams the arena floor with titanic force, creating a crater shockwave that stuns foes.",
            abilityCooldownSec = 9.0f,
            unlockGemsCost = 1000,
            primaryColor = 0xFFFFB300,
            secondaryColor = 0xFF8D6E63,
            glowColor = 0xFFFFC107
        ),
        HeroDef(
            id = HeroId.VOLT,
            name = "VOLT",
            title = "Thunder Striker",
            element = ElementType.LIGHTNING,
            baseHp = 420f,
            baseDamage = 56f,
            baseSpeed = 240f,
            attackRange = 70f,
            isRanged = false,
            attackCooldownSec = 0.25f,
            abilityName = "Chain Lightning",
            abilityDesc = "Discharges high-voltage bolts that bounce rapidly between up to 6 targets.",
            abilityCooldownSec = 7.5f,
            unlockGemsCost = 1200,
            primaryColor = 0xFFFFEA00,
            secondaryColor = 0xFF00E5FF,
            glowColor = 0xFFFFD600
        ),
        HeroDef(
            id = HeroId.NATURE,
            name = "NATURE",
            title = "Grove Warden",
            element = ElementType.NATURE,
            baseHp = 500f,
            baseDamage = 50f,
            baseSpeed = 210f,
            attackRange = 180f,
            isRanged = true,
            attackCooldownSec = 0.38f,
            abilityName = "Healing Blossom",
            abilityDesc = "Conjures a sacred sanctuary that restores 50% HP and pierces nearby enemies with thorns.",
            abilityCooldownSec = 11.0f,
            unlockGemsCost = 1500,
            primaryColor = 0xFF00E676,
            secondaryColor = 0xFFB9F6CA,
            glowColor = 0xFF69F0AE
        ),
        HeroDef(
            id = HeroId.PHANTOM,
            name = "PHANTOM",
            title = "Soul Reaper",
            element = ElementType.VOID,
            baseHp = 390f,
            baseDamage = 68f,
            baseSpeed = 230f,
            attackRange = 65f,
            isRanged = false,
            attackCooldownSec = 0.32f,
            abilityName = "Teleport Strike",
            abilityDesc = "Instantly teleports behind the deadliest enemy in the arena and executes a lethal backstab.",
            abilityCooldownSec = 7.0f,
            unlockGemsCost = 1800,
            primaryColor = 0xFF7C4DFF,
            secondaryColor = 0xFFB388FF,
            glowColor = 0xFF651FFF
        ),
        HeroDef(
            id = HeroId.VENOM,
            name = "VENOM",
            title = "Toxic Stalker",
            element = ElementType.POISON,
            baseHp = 440f,
            baseDamage = 46f,
            baseSpeed = 225f,
            attackRange = 70f,
            isRanged = false,
            attackCooldownSec = 0.30f,
            abilityName = "Poison Cloud",
            abilityDesc = "Releases an expanding toxic shroud that melts enemy armor and deals heavy damage over time.",
            abilityCooldownSec = 8.5f,
            unlockGemsCost = 2200,
            primaryColor = 0xFF76FF03,
            secondaryColor = 0xFF00E676,
            glowColor = 0xFF64DD17
        ),
        HeroDef(
            id = HeroId.SOLAR,
            name = "SOLAR",
            title = "Dawn Paladin",
            element = ElementType.LIGHT,
            baseHp = 520f,
            baseDamage = 60f,
            baseSpeed = 215f,
            attackRange = 160f,
            isRanged = false,
            attackCooldownSec = 0.35f,
            abilityName = "Solar Beam",
            abilityDesc = "Channels a blazing ray of concentrated sunfire that pierces all enemies in a line.",
            abilityCooldownSec = 9.5f,
            unlockGemsCost = 3000,
            primaryColor = 0xFFFFD700,
            secondaryColor = 0xFFFFAB00,
            glowColor = 0xFFFFC400
        ),
        HeroDef(
            id = HeroId.DRAGON,
            name = "DRAGON",
            title = "Dragon Lord",
            element = ElementType.DRAGON_FIRE,
            baseHp = 680f,
            baseDamage = 88f,
            baseSpeed = 235f,
            attackRange = 90f,
            isRanged = false,
            attackCooldownSec = 0.32f,
            abilityName = "Dragon Flame",
            abilityDesc = "Summons ancient draconian breath, scorching an entire cone with annihilating wildfire.",
            abilityCooldownSec = 10.0f,
            unlockGemsCost = 5000,
            primaryColor = 0xFFFF1744,
            secondaryColor = 0xFFFF6D00,
            glowColor = 0xFFD50000
        )
    )

    fun getDef(id: HeroId): HeroDef = allHeroes.firstOrNull { it.id == id } ?: allHeroes.first()
}

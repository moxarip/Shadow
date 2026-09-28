package com.example.game.model

enum class WeaponType(val displayName: String) {
    ENERGY_KATANA("Energy Katana"),
    DUAL_PISTOLS("Dual Pistols"),
    MASSIVE_HAMMER("Massive Hammer"),
    TWIN_BLADES("Twin Blades"),
    ENERGY_BOW("Energy Bow"),
    ENERGY_SPEAR("Energy Spear"),
    FLAME_GAUNTLETS("Flame Gauntlets"),
    ENERGY_CANNON("Energy Cannon"),
    SPECTRAL_BLADES("Spectral Blades"),
    DRAGON_BLADE("Dragon Blade")
}

data class WeaponDef(
    val type: WeaponType,
    val name: String,
    val description: String,
    val isRanged: Boolean,
    val baseDamage: Float,
    val attackSpeed: Float, // swings per second
    val range: Float,
    val specialEffect: String,
    val projectileSpeed: Float = 0f,
    val color: Long,
    val unlockGemsCost: Int
)

data class WeaponProgress(
    val type: WeaponType,
    val level: Int = 1,
    val isUnlocked: Boolean = false
) {
    fun getDamage(def: WeaponDef): Float = def.baseDamage * (1f + (level - 1) * 0.12f)
    fun getUpgradeCost(): Int = if (level >= 10) 0 else 100 * level
}

object WeaponRegistry {
    val allWeapons: List<WeaponDef> = listOf(
        WeaponDef(
            type = WeaponType.ENERGY_KATANA,
            name = "Energy Katana",
            description = "High-frequency plasma blade delivering lightning-swift slashes.",
            isRanged = false,
            baseDamage = 45f,
            attackSpeed = 2.8f,
            range = 75f,
            specialEffect = "Rapid Slash Combo",
            color = 0xFF00E5FF,
            unlockGemsCost = 0
        ),
        WeaponDef(
            type = WeaponType.DUAL_PISTOLS,
            name = "Dual Energy Pistols",
            description = "Twin rapid-fire pulse blasters designed for mobile fire.",
            isRanged = true,
            baseDamage = 28f,
            attackSpeed = 3.4f,
            range = 280f,
            specialEffect = "Shoot on the move",
            projectileSpeed = 650f,
            color = 0xFFFFD600,
            unlockGemsCost = 500
        ),
        WeaponDef(
            type = WeaponType.MASSIVE_HAMMER,
            name = "Massive Hammer",
            description = "Colossal seismic hammer that shatters armor and terrain.",
            isRanged = false,
            baseDamage = 95f,
            attackSpeed = 1.2f,
            range = 85f,
            specialEffect = "Ground Stun Shockwave",
            color = 0xFFFFAB00,
            unlockGemsCost = 800
        ),
        WeaponDef(
            type = WeaponType.TWIN_BLADES,
            name = "Twin Blades",
            description = "Dual shadow daggers built for high-speed critical assassinations.",
            isRanged = false,
            baseDamage = 38f,
            attackSpeed = 3.6f,
            range = 65f,
            specialEffect = "Shadow Backstab",
            color = 0xFFE040FB,
            unlockGemsCost = 1000
        ),
        WeaponDef(
            type = WeaponType.ENERGY_BOW,
            name = "Energy Bow",
            description = "Precision long-range bow that discharges piercing light arrows.",
            isRanged = true,
            baseDamage = 48f,
            attackSpeed = 1.8f,
            range = 380f,
            specialEffect = "Piercing Arrow Volley",
            projectileSpeed = 700f,
            color = 0xFF76FF03,
            unlockGemsCost = 1200
        ),
        WeaponDef(
            type = WeaponType.ENERGY_SPEAR,
            name = "Energy Spear",
            description = "Balanced thrusting polearm with superior reach and defensive jab.",
            isRanged = false,
            baseDamage = 52f,
            attackSpeed = 2.2f,
            range = 110f,
            specialEffect = "Spear Penetration Charge",
            color = 0xFF2979FF,
            unlockGemsCost = 1500
        ),
        WeaponDef(
            type = WeaponType.FLAME_GAUNTLETS,
            name = "Flame Gauntlets",
            description = "Volcanic fist guards blasting eruptive fire punches at close range.",
            isRanged = false,
            baseDamage = 62f,
            attackSpeed = 2.4f,
            range = 70f,
            specialEffect = "Burn Damage Over Time",
            color = 0xFFFF3D00,
            unlockGemsCost = 2000
        ),
        WeaponDef(
            type = WeaponType.ENERGY_CANNON,
            name = "Energy Cannon",
            description = "Heavy particle accelerator firing charged explosive plasma orbs.",
            isRanged = true,
            baseDamage = 75f,
            attackSpeed = 1.4f,
            range = 320f,
            specialEffect = "Area Blast Knockback",
            projectileSpeed = 520f,
            color = 0xFF00B0FF,
            unlockGemsCost = 2500
        ),
        WeaponDef(
            type = WeaponType.SPECTRAL_BLADES,
            name = "Spectral Blades",
            description = "Ethereal blades that phase through enemy armor shields.",
            isRanged = false,
            baseDamage = 58f,
            attackSpeed = 2.6f,
            range = 75f,
            specialEffect = "Phase Armor Penetration",
            color = 0xFF7C4DFF,
            unlockGemsCost = 3000
        ),
        WeaponDef(
            type = WeaponType.DRAGON_BLADE,
            name = "Dragon Blade",
            description = "Legendary wyrm-forged greatsword unleashing apocalyptic dragonfire waves.",
            isRanged = false,
            baseDamage = 110f,
            attackSpeed = 2.0f,
            range = 100f,
            specialEffect = "Dragon Wave Blast",
            color = 0xFFFF1744,
            unlockGemsCost = 5000
        )
    )

    fun getDef(type: WeaponType): WeaponDef = allWeapons.firstOrNull { it.type == type } ?: allWeapons.first()
}

package com.example.game.model

enum class CharacterId(val displayName: String) {
    BLADE("Blade"),
    GUNNER("Gunner"),
    HAMMER("Hammer"),
    NINJA("Ninja"),
    ARCHER("Archer"),
    SPEARMAN("Spearman"),
    FLAME("Flame"),
    CYBER("Cyber"),
    PHANTOM("Phantom"),
    DRAGON("Dragon")
}

data class SkinDef(
    val skinId: String,
    val name: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val glowColor: Long,
    val unlockGemsCost: Int = 0
)

data class CharacterDef(
    val id: CharacterId,
    val name: String,
    val role: String,
    val weaponType: WeaponType,
    val speedRating: Int, // 1..10
    val attackRating: Int, // 1..10
    val defenseRating: Int, // 1..10
    val baseHp: Float,
    val baseAttack: Float,
    val baseDefense: Float,
    val baseSpeed: Float,
    val abilityName: String,
    val abilityDesc: String,
    val abilityCooldownSec: Float,
    val unlockGemsCost: Int,
    val silhouetteColor: Long,
    val energyColor: Long,
    val accentColor: Long,
    val skins: List<SkinDef>
) {
    val glowColor: Long get() = energyColor
    val primaryColor: Long get() = silhouetteColor
    val secondaryColor: Long get() = accentColor
}

data class CharacterProgress(
    val characterId: CharacterId,
    val level: Int = 1,
    val isUnlocked: Boolean = false,
    val selectedSkinIndex: Int = 0,
    val unlockedSkins: Set<Int> = setOf(0)
) {
    fun getHp(def: CharacterDef): Float = def.baseHp * (1f + (level - 1) * 0.10f)
    fun getAttack(def: CharacterDef): Float = def.baseAttack * (1f + (level - 1) * 0.10f)
    fun getDefense(def: CharacterDef): Float = def.baseDefense * (1f + (level - 1) * 0.08f)
    fun getSpeed(def: CharacterDef): Float = def.baseSpeed + (level - 1) * 1.5f
    fun getAbilityPower(def: CharacterDef): Float = 1.0f + (level - 1) * 0.12f

    fun getUpgradeCost(): Int {
        if (level >= 20) return 0
        return 120 + level * 85
    }
}

object CharacterRegistry {
    val allCharacters: List<CharacterDef> = listOf(
        CharacterDef(
            id = CharacterId.BLADE,
            name = "BLADE",
            role = "Fast Melee Striker",
            weaponType = WeaponType.ENERGY_KATANA,
            speedRating = 9,
            attackRating = 7,
            defenseRating = 5,
            baseHp = 480f,
            baseAttack = 52f,
            baseDefense = 12f,
            baseSpeed = 260f,
            abilityName = "Rapid Slash",
            abilityDesc = "Dashes forward executing a whirlwind of 4 energy katana slashes.",
            abilityCooldownSec = 5.0f,
            unlockGemsCost = 0,
            silhouetteColor = 0xFF10121A,
            energyColor = 0xFF00E5FF,
            accentColor = 0xFF00B0FF,
            skins = listOf(
                SkinDef("blade_default", "Cyan Edge", 0xFF10121A, 0xFF00E5FF, 0xFF00E5FF, 0),
                SkinDef("blade_crimson", "Blood Katana", 0xFF14080B, 0xFFFF1744, 0xFFFF5252, 200),
                SkinDef("blade_golden", "Solar Ronin", 0xFF141108, 0xFFFFD700, 0xFFFFEA00, 400)
            )
        ),
        CharacterDef(
            id = CharacterId.GUNNER,
            name = "GUNNER",
            role = "Mobile Ranged Gunner",
            weaponType = WeaponType.DUAL_PISTOLS,
            speedRating = 7,
            attackRating = 6,
            defenseRating = 4,
            baseHp = 420f,
            baseAttack = 40f,
            baseDefense = 10f,
            baseSpeed = 230f,
            abilityName = "Bullet Storm",
            abilityDesc = "Fires a rapid 360-degree storm of piercing plasma bullets.",
            abilityCooldownSec = 5.5f,
            unlockGemsCost = 500,
            silhouetteColor = 0xFF16151B,
            energyColor = 0xFFFFD600,
            accentColor = 0xFFFFAB00,
            skins = listOf(
                SkinDef("gunner_default", "Pulse Gold", 0xFF16151B, 0xFFFFD600, 0xFFFFD600, 0),
                SkinDef("gunner_toxic", "Acid Blaster", 0xFF0D140C, 0xFF76FF03, 0xFF64DD17, 250),
                SkinDef("gunner_cyber", "Neon Gunslinger", 0xFF0B141C, 0xFF00E5FF, 0xFF18FFFF, 450)
            )
        ),
        CharacterDef(
            id = CharacterId.HAMMER,
            name = "HAMMER",
            role = "Heavy Juggernaut",
            weaponType = WeaponType.MASSIVE_HAMMER,
            speedRating = 3,
            attackRating = 10,
            defenseRating = 9,
            baseHp = 750f,
            baseAttack = 80f,
            baseDefense = 26f,
            baseSpeed = 160f,
            abilityName = "Earth Smash",
            abilityDesc = "Slams the massive hammer into the ground creating seismic quakes.",
            abilityCooldownSec = 7.0f,
            unlockGemsCost = 800,
            silhouetteColor = 0xFF181512,
            energyColor = 0xFFFF9100,
            accentColor = 0xFFFF6D00,
            skins = listOf(
                SkinDef("hammer_default", "Molten Anvil", 0xFF181512, 0xFFFF9100, 0xFFFF9100, 0),
                SkinDef("hammer_iron", "Titan Steel", 0xFF15191C, 0xFF78909C, 0xFFB0BEC5, 250),
                SkinDef("hammer_magma", "Volcanic Core", 0xFF1B0B0B, 0xFFFF3D00, 0xFFFF1744, 450)
            )
        ),
        CharacterDef(
            id = CharacterId.NINJA,
            name = "NINJA",
            role = "Speed Assassin",
            weaponType = WeaponType.TWIN_BLADES,
            speedRating = 10,
            attackRating = 8,
            defenseRating = 3,
            baseHp = 390f,
            baseAttack = 64f,
            baseDefense = 8f,
            baseSpeed = 300f,
            abilityName = "Shadow Teleport",
            abilityDesc = "Instantly teleports forward behind target with a deadly shadow ambush.",
            abilityCooldownSec = 4.5f,
            unlockGemsCost = 1000,
            silhouetteColor = 0xFF130E1A,
            energyColor = 0xFFE040FB,
            accentColor = 0xFF9C27B0,
            skins = listOf(
                SkinDef("ninja_default", "Night Lotus", 0xFF130E1A, 0xFFE040FB, 0xFFE040FB, 0),
                SkinDef("ninja_ghost", "White Phantom", 0xFF1C1E24, 0xFFE0E0E0, 0xFFFFFFFF, 250),
                SkinDef("ninja_blood", "Crimson Shinobi", 0xFF180A0E, 0xFFFF1744, 0xFFFF5252, 500)
            )
        ),
        CharacterDef(
            id = CharacterId.ARCHER,
            name = "ARCHER",
            role = "Sniper Ranger",
            weaponType = WeaponType.ENERGY_BOW,
            speedRating = 6,
            attackRating = 7,
            defenseRating = 3,
            baseHp = 410f,
            baseAttack = 54f,
            baseDefense = 9f,
            baseSpeed = 220f,
            abilityName = "Rain of Arrows",
            abilityDesc = "Channels an airborne volley of piercing light arrows onto targets.",
            abilityCooldownSec = 6.0f,
            unlockGemsCost = 1200,
            silhouetteColor = 0xFF0E1610,
            energyColor = 0xFF76FF03,
            accentColor = 0xFF00E676,
            skins = listOf(
                SkinDef("archer_default", "Verdant Hunter", 0xFF0E1610, 0xFF76FF03, 0xFF76FF03, 0),
                SkinDef("archer_frost", "Glacial Arrow", 0xFF0A161C, 0xFF00E5FF, 0xFF80D8FF, 300),
                SkinDef("archer_gold", "Sunpiercer", 0xFF18150B, 0xFFFFD700, 0xFFFFEA00, 500)
            )
        ),
        CharacterDef(
            id = CharacterId.SPEARMAN,
            name = "SPEARMAN",
            role = "Balanced Vanguard",
            weaponType = WeaponType.ENERGY_SPEAR,
            speedRating = 7,
            attackRating = 7,
            defenseRating = 7,
            baseHp = 530f,
            baseAttack = 50f,
            baseDefense = 18f,
            baseSpeed = 230f,
            abilityName = "Spear Charge",
            abilityDesc = "Charges forward thrusting spear through multiple enemies with shield bash.",
            abilityCooldownSec = 5.5f,
            unlockGemsCost = 1500,
            silhouetteColor = 0xFF0D131C,
            energyColor = 0xFF2979FF,
            accentColor = 0xFF00B0FF,
            skins = listOf(
                SkinDef("spear_default", "Cerulean Guard", 0xFF0D131C, 0xFF2979FF, 0xFF2979FF, 0),
                SkinDef("spear_bronze", "Spartan Bronze", 0xFF1A140F, 0xFFFF9100, 0xFFFFAB00, 300),
                SkinDef("spear_void", "Abyssal Lance", 0xFF140D1E, 0xFF7C4DFF, 0xFFB388FF, 500)
            )
        ),
        CharacterDef(
            id = CharacterId.FLAME,
            name = "FLAME",
            role = "Fire Brawler",
            weaponType = WeaponType.FLAME_GAUNTLETS,
            speedRating = 6,
            attackRating = 8,
            defenseRating = 5,
            baseHp = 510f,
            baseAttack = 58f,
            baseDefense = 14f,
            baseSpeed = 225f,
            abilityName = "Flame Burst",
            abilityDesc = "Ignites fists creating an eruptive inferno explosion clearing nearby enemies.",
            abilityCooldownSec = 5.8f,
            unlockGemsCost = 2000,
            silhouetteColor = 0xFF1C0D0A,
            energyColor = 0xFFFF3D00,
            accentColor = 0xFFFF6D00,
            skins = listOf(
                SkinDef("flame_default", "Blazing Cinder", 0xFF1C0D0A, 0xFFFF3D00, 0xFFFF3D00, 0),
                SkinDef("flame_blue", "Blue Plasma", 0xFF091420, 0xFF00B0FF, 0xFF00E5FF, 350),
                SkinDef("flame_solar", "Solar Titan", 0xFF1A1408, 0xFFFFD700, 0xFFFFAB00, 550)
            )
        ),
        CharacterDef(
            id = CharacterId.CYBER,
            name = "CYBER",
            role = "Tech Heavy Cannon",
            weaponType = WeaponType.ENERGY_CANNON,
            speedRating = 5,
            attackRating = 9,
            defenseRating = 5,
            baseHp = 500f,
            baseAttack = 66f,
            baseDefense = 15f,
            baseSpeed = 210f,
            abilityName = "Energy Beam",
            abilityDesc = "Charges an overcharged horizontal laser beam obliterating the stage path.",
            abilityCooldownSec = 6.5f,
            unlockGemsCost = 2500,
            silhouetteColor = 0xFF0C161C,
            energyColor = 0xFF00E5FF,
            accentColor = 0xFF1DE9B6,
            skins = listOf(
                SkinDef("cyber_default", "Neon Matrix", 0xFF0C161C, 0xFF00E5FF, 0xFF00E5FF, 0),
                SkinDef("cyber_stealth", "Stealth Protocol", 0xFF121418, 0xFFFF1744, 0xFFFF5252, 350),
                SkinDef("cyber_gold", "Overclock Gold", 0xFF1C190D, 0xFFFFD700, 0xFFFFEA00, 600)
            )
        ),
        CharacterDef(
            id = CharacterId.PHANTOM,
            name = "PHANTOM",
            role = "Spectral Infiltrator",
            weaponType = WeaponType.SPECTRAL_BLADES,
            speedRating = 9,
            attackRating = 6,
            defenseRating = 4,
            baseHp = 440f,
            baseAttack = 56f,
            baseDefense = 11f,
            baseSpeed = 270f,
            abilityName = "Phase Strike",
            abilityDesc = "Phases into ethereal void (invulnerable for 1.8s) slicing while invisible.",
            abilityCooldownSec = 6.2f,
            unlockGemsCost = 3000,
            silhouetteColor = 0xFF140D20,
            energyColor = 0xFF7C4DFF,
            accentColor = 0xFFB388FF,
            skins = listOf(
                SkinDef("phantom_default", "Spectral Violet", 0xFF140D20, 0xFF7C4DFF, 0xFF7C4DFF, 0),
                SkinDef("phantom_cyan", "Astral Wraith", 0xFF0A181C, 0xFF18FFFF, 0xFF84FFFF, 400),
                SkinDef("phantom_radiant", "Radiant Archon", 0xFF181822, 0xFFFFD700, 0xFFFFFFFF, 650)
            )
        ),
        CharacterDef(
            id = CharacterId.DRAGON,
            name = "DRAGON",
            role = "Legendary Sovereign",
            weaponType = WeaponType.DRAGON_BLADE,
            speedRating = 7,
            attackRating = 10,
            defenseRating = 8,
            baseHp = 720f,
            baseAttack = 90f,
            baseDefense = 22f,
            baseSpeed = 240f,
            abilityName = "Dragon Wave",
            abilityDesc = "Summons an ancient astral dragon that roars across the stage destroying foes.",
            abilityCooldownSec = 8.0f,
            unlockGemsCost = 5000,
            silhouetteColor = 0xFF1A0A0E,
            energyColor = 0xFFFF1744,
            accentColor = 0xFFFF6D00,
            skins = listOf(
                SkinDef("dragon_default", "Ancient Crimson", 0xFF1A0A0E, 0xFFFF1744, 0xFFFF1744, 0),
                SkinDef("dragon_shadow", "Void Dragon", 0xFF120A1C, 0xFF7C4DFF, 0xFFE040FB, 500),
                SkinDef("dragon_celestial", "Celestial Sovereign", 0xFF1C1A14, 0xFFFFD700, 0xFFFFFFFF, 750)
            )
        )
    )

    fun getDef(id: CharacterId): CharacterDef = allCharacters.firstOrNull { it.id == id } ?: allCharacters.first()
}

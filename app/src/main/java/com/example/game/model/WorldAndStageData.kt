package com.example.game.model

enum class WorldId(val displayName: String, val subtitle: String) {
    NEON_DISTRICT("Neon District", "Cybernetic streets under vibrant neon holograms"),
    ANCIENT_RUINS("Ancient Ruins", "Weathered stone arches and sacred desert columns"),
    FROZEN_VALLEY("Frozen Valley", "Blizzard-swept mountain peaks and crystal glaciers"),
    VOLCANIC_FORTRESS("Volcanic Fortress", "Molten magma rivers and scorched basalt towers"),
    SHADOW_CITADEL("Shadow Citadel", "Dark obsidian spires in ethereal twilight mist")
}

data class BossDef(
    val id: String,
    val name: String,
    val title: String,
    val baseHp: Float,
    val baseAttack: Float,
    val baseDefense: Float,
    val color: Long,
    val accentColor: Long,
    val specialAttackName: String
)

data class WorldDef(
    val id: WorldId,
    val name: String,
    val subtitle: String,
    val skyColorTop: Long,
    val skyColorBottom: Long,
    val farSilhouetteColor: Long,
    val groundColor: Long,
    val platformColor: Long,
    val neonAccentColor: Long,
    val unlockLevelRequired: Int,
    val bossDef: BossDef
)

object WorldRegistry {
    val allWorlds: List<WorldDef> = listOf(
        WorldDef(
            id = WorldId.NEON_DISTRICT,
            name = "Neon District",
            subtitle = "Cybernetic streets under vibrant neon holograms",
            skyColorTop = 0xFF080614,
            skyColorBottom = 0xFF140D2E,
            farSilhouetteColor = 0xFF1C133C,
            groundColor = 0xFF0F0E1E,
            platformColor = 0xFF2A1C48,
            neonAccentColor = 0xFF00E5FF,
            unlockLevelRequired = 1,
            bossDef = BossDef(
                id = "boss_neon",
                name = "NEON OVERLORD",
                title = "Cybernetic Sovereign",
                baseHp = 2200f,
                baseAttack = 65f,
                baseDefense = 18f,
                color = 0xFF00E5FF,
                accentColor = 0xFFE040FB,
                specialAttackName = "Hyper Overcharge Laser"
            )
        ),
        WorldDef(
            id = WorldId.ANCIENT_RUINS,
            name = "Ancient Ruins",
            subtitle = "Weathered stone arches and sacred desert columns",
            skyColorTop = 0xFF120E08,
            skyColorBottom = 0xFF22170E,
            farSilhouetteColor = 0xFF352415,
            groundColor = 0xFF18120A,
            platformColor = 0xFF4A341E,
            neonAccentColor = 0xFFFFD700,
            unlockLevelRequired = 2,
            bossDef = BossDef(
                id = "boss_golem",
                name = "ANCIENT GOLEM",
                title = "Stone Titan of Antiquity",
                baseHp = 2800f,
                baseAttack = 75f,
                baseDefense = 28f,
                color = 0xFFFFB300,
                accentColor = 0xFFFF6F00,
                specialAttackName = "Seismic Cataclysm Smash"
            )
        ),
        WorldDef(
            id = WorldId.FROZEN_VALLEY,
            name = "Frozen Valley",
            subtitle = "Blizzard-swept mountain peaks and crystal glaciers",
            skyColorTop = 0xFF060D18,
            skyColorBottom = 0xFF0D1C2E,
            farSilhouetteColor = 0xFF152C45,
            groundColor = 0xFF0A1420,
            platformColor = 0xFF1E3A54,
            neonAccentColor = 0xFF00B0FF,
            unlockLevelRequired = 4,
            bossDef = BossDef(
                id = "boss_valkyrie",
                name = "FROST VALKYRIE",
                title = "Queen of Glacial Tempests",
                baseHp = 2400f,
                baseAttack = 70f,
                baseDefense = 20f,
                color = 0xFF80D8FF,
                accentColor = 0xFF00E5FF,
                specialAttackName = "Sub-Zero Blizzard Cyclone"
            )
        ),
        WorldDef(
            id = WorldId.VOLCANIC_FORTRESS,
            name = "Volcanic Fortress",
            subtitle = "Molten magma rivers and scorched basalt towers",
            skyColorTop = 0xFF180806,
            skyColorBottom = 0xFF2E0F0A,
            farSilhouetteColor = 0xFF48150C,
            groundColor = 0xFF1C0A07,
            platformColor = 0xFF58170D,
            neonAccentColor = 0xFFFF3D00,
            unlockLevelRequired = 6,
            bossDef = BossDef(
                id = "boss_magma",
                name = "MAGMA BEHEMOTH",
                title = "Infernal Core Destroyer",
                baseHp = 3200f,
                baseAttack = 85f,
                baseDefense = 24f,
                color = 0xFFFF3D00,
                accentColor = 0xFFFF9100,
                specialAttackName = "Eruptive Molten Deluge"
            )
        ),
        WorldDef(
            id = WorldId.SHADOW_CITADEL,
            name = "Shadow Citadel",
            subtitle = "Dark obsidian spires in ethereal twilight mist",
            skyColorTop = 0xFF0B0616,
            skyColorBottom = 0xFF16092B,
            farSilhouetteColor = 0xFF250D45,
            groundColor = 0xFF0F061C,
            platformColor = 0xFF371262,
            neonAccentColor = 0xFF7C4DFF,
            unlockLevelRequired = 8,
            bossDef = BossDef(
                id = "boss_sovereign",
                name = "SHADOW SOVEREIGN",
                title = "Emperor of the Void",
                baseHp = 3800f,
                baseAttack = 95f,
                baseDefense = 25f,
                color = 0xFF9C27B0,
                accentColor = 0xFFFF1744,
                specialAttackName = "Void Oblivion Rift"
            )
        )
    )

    fun getDef(id: WorldId): WorldDef = allWorlds.firstOrNull { it.id == id } ?: allWorlds.first()
}

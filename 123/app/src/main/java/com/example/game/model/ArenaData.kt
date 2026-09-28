package com.example.game.model

data class ArenaObstacle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val type: String // "tree", "pillar", "holocube", "rock"
)

data class ArenaDef(
    val id: ArenaId,
    val name: String,
    val subtitle: String,
    val floorColor: Long,
    val gridColor: Long,
    val wallColor: Long,
    val glowColor: Long,
    val ambientParticleColor: Long,
    val unlockLevelRequired: Int,
    val obstacles: List<ArenaObstacle>
)

object ArenaRegistry {
    val allArenas: List<ArenaDef> = listOf(
        ArenaDef(
            id = ArenaId.DARK_FOREST,
            name = "Dark Forest",
            subtitle = "Misty realm of shadow sprites and ancient roots",
            floorColor = 0xFF0E1118,
            gridColor = 0xFF1B2433,
            wallColor = 0xFF2D1E3E,
            glowColor = 0xFF7C4DFF,
            ambientParticleColor = 0xFF9C27B0,
            unlockLevelRequired = 1,
            obstacles = listOf(
                ArenaObstacle(140f, 260f, 28f, "tree"),
                ArenaObstacle(460f, 260f, 28f, "tree"),
                ArenaObstacle(300f, 480f, 32f, "rock"),
                ArenaObstacle(160f, 680f, 26f, "tree"),
                ArenaObstacle(440f, 680f, 26f, "tree")
            )
        ),
        ArenaDef(
            id = ArenaId.ANCIENT_TEMPLE,
            name = "Ancient Temple",
            subtitle = "Sacred desert sanctuary of forgotten pharaohs",
            floorColor = 0xFF19140E,
            gridColor = 0xFF2E2214,
            wallColor = 0xFF5D4037,
            glowColor = 0xFFFFB300,
            ambientParticleColor = 0xFFFFD54F,
            unlockLevelRequired = 3,
            obstacles = listOf(
                ArenaObstacle(150f, 220f, 30f, "pillar"),
                ArenaObstacle(450f, 220f, 30f, "pillar"),
                ArenaObstacle(200f, 490f, 24f, "pillar"),
                ArenaObstacle(400f, 490f, 24f, "pillar"),
                ArenaObstacle(300f, 720f, 34f, "rock")
            )
        ),
        ArenaDef(
            id = ArenaId.CYBER_ARENA,
            name = "Cyber Arena",
            subtitle = "Hyper-charged neon battleground of artificial sentience",
            floorColor = 0xFF080D1A,
            gridColor = 0xFF003B5C,
            wallColor = 0xFF00E5FF,
            glowColor = 0xFF00E5FF,
            ambientParticleColor = 0xFF00E5FF,
            unlockLevelRequired = 5,
            obstacles = listOf(
                ArenaObstacle(160f, 250f, 28f, "holocube"),
                ArenaObstacle(440f, 250f, 28f, "holocube"),
                ArenaObstacle(300f, 470f, 30f, "holocube"),
                ArenaObstacle(150f, 690f, 28f, "holocube"),
                ArenaObstacle(450f, 690f, 28f, "holocube")
            )
        )
    )

    fun getDef(id: ArenaId): ArenaDef = allArenas.firstOrNull { it.id == id } ?: allArenas.first()
}

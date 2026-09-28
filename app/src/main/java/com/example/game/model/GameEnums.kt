package com.example.game.model

enum class AppScreen {
    MAIN_MENU,
    BATTLE,
    CHARACTERS,
    WEAPONS,
    SKINS,
    SHOP,
    MISSIONS,
    REWARDS,
    ACHIEVEMENTS;

    companion object {
        val HEROES = CHARACTERS
    }
}

enum class GraphicsQuality(val label: String) {
    LOW("Low (Best Performance)"),
    MEDIUM("Medium (Balanced)"),
    HIGH("High (Full Effects)")
}

enum class AnimState {
    IDLE,
    WALK,
    RUN,
    JUMP,
    FALL,
    ATTACK_LIGHT,
    ATTACK_HEAVY,
    COMBO,
    DASH,
    ABILITY,
    HIT,
    KNOCKBACK,
    DEATH,
    VICTORY
}

enum class EnemyType(val displayName: String, val baseHp: Float, val speed: Float, val color: Long) {
    BASIC_FIGHTER("Shadow Grunt", 90f, 130f, 0xFF78909C),
    FAST_FIGHTER("Shadow Stalker", 70f, 210f, 0xFFE040FB),
    HEAVY_FIGHTER("Shadow Brute", 210f, 95f, 0xFFFF6D00),
    RANGED_FIGHTER("Shadow Sniper", 80f, 110f, 0xFF76FF03),
    SHIELD_FIGHTER("Shadow Sentinel", 160f, 105f, 0xFF00E5FF),
    FLYING_DRONE("Cyber Drone", 60f, 160f, 0xFFFFD600),
    ELITE_FIGHTER("Shadow Captain", 320f, 150f, 0xFFFF1744),
    BOSS("World Sovereign", 2400f, 110f, 0xFFFF1744)
}

enum class AIBehaviorState {
    IDLE,
    DETECT,
    FOLLOW,
    ATTACK,
    RETREAT,
    DEATH
}

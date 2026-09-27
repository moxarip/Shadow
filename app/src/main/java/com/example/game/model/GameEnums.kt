package com.example.game.model

enum class HeroId(val displayName: String) {
    BLAZE("Blaze"),
    FROST("Frost"),
    SHADOW("Shadow"),
    TITAN("Titan"),
    VOLT("Volt"),
    NATURE("Nature"),
    PHANTOM("Phantom"),
    VENOM("Venom"),
    SOLAR("Solar"),
    DRAGON("Dragon")
}

enum class ElementType(val label: String, val hexColor: Long) {
    FIRE("Fire", 0xFFFF5722),
    ICE("Ice", 0xFF00E5FF),
    SHADOW("Shadow", 0xFF9C27B0),
    EARTH("Earth", 0xFFFFB300),
    LIGHTNING("Lightning", 0xFFFFEA00),
    NATURE("Nature", 0xFF00E676),
    VOID("Ghost", 0xFF7C4DFF),
    POISON("Poison", 0xFF76FF03),
    LIGHT("Light", 0xFFFFD700),
    DRAGON_FIRE("Legendary", 0xFFFF1744)
}

enum class EnemyType(val displayName: String, val isBoss: Boolean = false) {
    BASIC("Shadow Crawler"),
    FAST("Void Stalker"),
    TANK("Iron Golem"),
    RANGED("Dark Cultist"),
    ELITE("Shadow Knight"),
    BOSS_BEHEMOTH("Forest Behemoth", isBoss = true),
    BOSS_PHARAOH("Sunken Pharaoh", isBoss = true),
    BOSS_CYBER("Cyber Overlord", isBoss = true)
}

enum class ArenaId(val displayName: String, val subtitle: String) {
    DARK_FOREST("Dark Forest", "Shadow Realm - Wave 1-5"),
    ANCIENT_TEMPLE("Ancient Temple", "Forgotten Sands - Wave 1-5"),
    CYBER_ARENA("Cyber Arena", "Neon Overdrive - Wave 1-5")
}

enum class GameMode(val title: String, val desc: String) {
    STORY("Story Battle", "Fight 5 escalating waves ending in an epic arena Boss!"),
    ENDLESS("Endless Survival", "Survive as long as possible against relentless waves!"),
    TRAINING("Training Grounds", "Test heroes, hone combos & gauge ability damage freely.")
}

enum class AppScreen {
    MAIN_MENU,
    BATTLE,
    HEROES,
    SHOP,
    MISSIONS,
    REWARDS,
    ACHIEVEMENTS
}

package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.game.model.AchievementItem
import com.example.game.model.CharacterId
import com.example.game.model.CharacterProgress
import com.example.game.model.CharacterRegistry
import com.example.game.model.GraphicsQuality
import com.example.game.model.MissionItem
import com.example.game.model.WeaponProgress
import com.example.game.model.WeaponRegistry
import com.example.game.model.WeaponType
import com.example.game.model.WorldId
import com.example.game.model.WorldRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class PlayerProgression(
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 200,
    val coins: Int = 800,
    val gems: Int = 400,
    val selectedCharacterId: CharacterId = CharacterId.BLADE,
    val selectedWeaponType: WeaponType = WeaponType.ENERGY_KATANA,
    val selectedWorldId: WorldId = WorldId.NEON_DISTRICT,
    val selectedStageNumber: Int = 1,
    val characters: Map<CharacterId, CharacterProgress> = emptyMap(),
    val weapons: Map<WeaponType, WeaponProgress> = emptyMap(),
    val unlockedWorlds: Set<WorldId> = setOf(WorldId.NEON_DISTRICT),
    val highestStageCleared: Int = 0,
    val totalEnemiesDefeated: Int = 0,
    val totalStagesCleared: Int = 0,
    val totalBossesDefeated: Int = 0,
    val totalAbilitiesUsed: Int = 0,
    val dailyRewardDayClaimed: Int = 0,
    val lastDailyClaimTime: Long = 0L,
    val lastFreeChestTime: Long = 0L,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.MEDIUM
) {
    // Aliases for UI backward compatibility
    val selectedHeroId: CharacterId get() = selectedCharacterId
    val heroes: Map<CharacterId, CharacterProgress> get() = characters
}

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shadow_warriors_save", Context.MODE_PRIVATE)

    private val _progression = MutableStateFlow(loadProgression())
    val progression: StateFlow<PlayerProgression> = _progression.asStateFlow()

    private val _missions = MutableStateFlow(loadMissions())
    val missions: StateFlow<List<MissionItem>> = _missions.asStateFlow()

    private val _achievements = MutableStateFlow(loadAchievements())
    val achievements: StateFlow<List<AchievementItem>> = _achievements.asStateFlow()

    private fun loadProgression(): PlayerProgression {
        val level = prefs.getInt("player_level", 1)
        val xp = prefs.getInt("player_xp", 0)
        val coins = prefs.getInt("player_coins", 800)
        val gems = prefs.getInt("player_gems", 400)

        val charStr = prefs.getString("selected_char", CharacterId.BLADE.name) ?: CharacterId.BLADE.name
        val selectedChar = try { CharacterId.valueOf(charStr) } catch (_: Exception) { CharacterId.BLADE }

        val weaponStr = prefs.getString("selected_weapon", WeaponType.ENERGY_KATANA.name) ?: WeaponType.ENERGY_KATANA.name
        val selectedWeapon = try { WeaponType.valueOf(weaponStr) } catch (_: Exception) { WeaponType.ENERGY_KATANA }

        val worldStr = prefs.getString("selected_world", WorldId.NEON_DISTRICT.name) ?: WorldId.NEON_DISTRICT.name
        val selectedWorld = try { WorldId.valueOf(worldStr) } catch (_: Exception) { WorldId.NEON_DISTRICT }
        val selectedStage = prefs.getInt("selected_stage", 1)

        // Characters map
        val charMap = mutableMapOf<CharacterId, CharacterProgress>()
        CharacterRegistry.allCharacters.forEach { def ->
            val unlocked = if (def.id == CharacterId.BLADE) true else prefs.getBoolean("char_unlocked_${def.id.name}", false)
            val charLvl = prefs.getInt("char_level_${def.id.name}", 1)
            val skinIdx = prefs.getInt("char_skin_${def.id.name}", 0)
            val skinSetStr = prefs.getString("char_skins_unlocked_${def.id.name}", "0") ?: "0"
            val unlockedSkins = skinSetStr.split(",").mapNotNull { it.toIntOrNull() }.toSet().ifEmpty { setOf(0) }
            charMap[def.id] = CharacterProgress(def.id, charLvl, unlocked, skinIdx, unlockedSkins)
        }

        // Weapons map
        val weaponMap = mutableMapOf<WeaponType, WeaponProgress>()
        WeaponRegistry.allWeapons.forEach { def ->
            val unlocked = if (def.type == WeaponType.ENERGY_KATANA) true else prefs.getBoolean("weapon_unlocked_${def.type.name}", false)
            val wLvl = prefs.getInt("weapon_lvl_${def.type.name}", 1)
            weaponMap[def.type] = WeaponProgress(def.type, wLvl, unlocked)
        }

        // Worlds unlocked
        val unlockedWorlds = mutableSetOf(WorldId.NEON_DISTRICT)
        if (level >= 2 || prefs.getBoolean("world_${WorldId.ANCIENT_RUINS.name}", false)) unlockedWorlds.add(WorldId.ANCIENT_RUINS)
        if (level >= 4 || prefs.getBoolean("world_${WorldId.FROZEN_VALLEY.name}", false)) unlockedWorlds.add(WorldId.FROZEN_VALLEY)
        if (level >= 6 || prefs.getBoolean("world_${WorldId.VOLCANIC_FORTRESS.name}", false)) unlockedWorlds.add(WorldId.VOLCANIC_FORTRESS)
        if (level >= 8 || prefs.getBoolean("world_${WorldId.SHADOW_CITADEL.name}", false)) unlockedWorlds.add(WorldId.SHADOW_CITADEL)

        val gfxStr = prefs.getString("gfx_quality", GraphicsQuality.MEDIUM.name) ?: GraphicsQuality.MEDIUM.name
        val gfxQuality = try { GraphicsQuality.valueOf(gfxStr) } catch (_: Exception) { GraphicsQuality.MEDIUM }

        return PlayerProgression(
            level = level,
            xp = xp,
            xpToNextLevel = level * 200,
            coins = coins,
            gems = gems,
            selectedCharacterId = selectedChar,
            selectedWeaponType = selectedWeapon,
            selectedWorldId = selectedWorld,
            selectedStageNumber = selectedStage,
            characters = charMap,
            weapons = weaponMap,
            unlockedWorlds = unlockedWorlds,
            highestStageCleared = prefs.getInt("high_stage", 0),
            totalEnemiesDefeated = prefs.getInt("stat_enemies", 0),
            totalStagesCleared = prefs.getInt("stat_stages", 0),
            totalBossesDefeated = prefs.getInt("stat_bosses", 0),
            totalAbilitiesUsed = prefs.getInt("stat_abilities", 0),
            dailyRewardDayClaimed = prefs.getInt("daily_day_claimed", 0),
            lastDailyClaimTime = prefs.getLong("last_daily_time", 0L),
            lastFreeChestTime = prefs.getLong("last_free_chest_time", 0L),
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            musicEnabled = prefs.getBoolean("music_enabled", true),
            vibrationEnabled = prefs.getBoolean("vibe_enabled", true),
            graphicsQuality = gfxQuality
        )
    }

    private fun loadMissions(): List<MissionItem> {
        val enemies = _progression.value.totalEnemiesDefeated
        val stages = _progression.value.totalStagesCleared
        val bosses = _progression.value.totalBossesDefeated
        val abilities = _progression.value.totalAbilitiesUsed
        val coinsTot = _progression.value.coins

        val defaultList = listOf(
            MissionItem("m_enemies_25", "First Blood", "Defeat 25 enemies in stages.", 25, enemies.coerceAtMost(25), 150, 10, false),
            MissionItem("m_enemies_100", "Shadow Purge", "Defeat 100 enemies.", 100, enemies.coerceAtMost(100), 500, 30, false),
            MissionItem("m_stages_3", "Vanguard Advance", "Complete 3 side-scrolling stages.", 3, stages.coerceAtMost(3), 200, 20, false),
            MissionItem("m_stages_10", "World Conqueror", "Complete 10 stages.", 10, stages.coerceAtMost(10), 800, 80, false),
            MissionItem("m_boss_1", "Sovereign's Demise", "Defeat any World Boss.", 1, bosses.coerceAtMost(1), 1000, 100, false),
            MissionItem("m_ability_20", "Energy Mastery", "Unleash character abilities 20 times.", 20, abilities.coerceAtMost(20), 300, 25, false),
            MissionItem("m_flawless", "Flawless Victor", "Complete a stage without falling in battle.", 1, stages.coerceAtMost(1), 250, 30, false),
            MissionItem("m_coins_1000", "Bounty Hunter", "Accumulate 1,000 Coins.", 1000, coinsTot.coerceAtMost(1000), 400, 50, false)
        )

        val jsonStr = prefs.getString("missions_claimed_json", null) ?: return defaultList
        return try {
            val arr = JSONArray(jsonStr)
            val claimed = mutableSetOf<String>()
            for (i in 0 until arr.length()) claimed.add(arr.getString(i))
            defaultList.map { it.copy(isClaimed = claimed.contains(it.id)) }
        } catch (_: Exception) {
            defaultList
        }
    }

    private fun loadAchievements(): List<AchievementItem> {
        val enemies = _progression.value.totalEnemiesDefeated
        val stages = _progression.value.totalStagesCleared
        val bosses = _progression.value.totalBossesDefeated
        val level = _progression.value.level
        val charsUnlocked = _progression.value.characters.values.count { it.isUnlocked }

        val defaultList = listOf(
            AchievementItem("ach_warrior_initiate", "Warrior Initiate", "Reach Player Level 5.", 5, level.coerceAtMost(5), 50, false),
            AchievementItem("ach_grandmaster", "Shadow Grandmaster", "Reach Player Level 10.", 10, level.coerceAtMost(10), 150, false),
            AchievementItem("ach_slayer_100", "Century Slayer", "Defeat 100 shadow enemies.", 100, enemies.coerceAtMost(100), 75, false),
            AchievementItem("ach_slayer_500", "Legendary Executioner", "Defeat 500 shadow enemies.", 500, enemies.coerceAtMost(500), 250, false),
            AchievementItem("ach_stages_25", "World Traveler", "Complete 25 total stages.", 25, stages.coerceAtMost(25), 100, false),
            AchievementItem("ach_bosses_5", "Sovereign Nemesis", "Defeat 5 World Bosses.", 5, bosses.coerceAtMost(5), 200, false),
            AchievementItem("ach_collector_5", "Arsenal Master", "Unlock 5 different Shadow Warriors.", 5, charsUnlocked.coerceAtMost(5), 120, false),
            AchievementItem("ach_collector_10", "Supreme Roster", "Unlock all 10 Shadow Warriors.", 10, charsUnlocked.coerceAtMost(10), 500, false)
        )

        val jsonStr = prefs.getString("achievements_claimed_json", null) ?: return defaultList
        return try {
            val arr = JSONArray(jsonStr)
            val claimed = mutableSetOf<String>()
            for (i in 0 until arr.length()) claimed.add(arr.getString(i))
            defaultList.map { it.copy(isClaimed = claimed.contains(it.id)) }
        } catch (_: Exception) {
            defaultList
        }
    }

    fun selectCharacter(charId: CharacterId) {
        val cur = _progression.value
        val c = cur.characters[charId]
        if (c?.isUnlocked == true) {
            prefs.edit().putString("selected_char", charId.name).apply()
            // Also equip the character's signature weapon by default
            val def = CharacterRegistry.getDef(charId)
            _progression.value = cur.copy(
                selectedCharacterId = charId,
                selectedWeaponType = def.weaponType
            )
        }
    }

    fun selectWeapon(type: WeaponType) {
        val cur = _progression.value
        val w = cur.weapons[type]
        if (w?.isUnlocked == true) {
            prefs.edit().putString("selected_weapon", type.name).apply()
            _progression.value = cur.copy(selectedWeaponType = type)
        }
    }

    fun selectWorldAndStage(worldId: WorldId, stage: Int) {
        prefs.edit()
            .putString("selected_world", worldId.name)
            .putInt("selected_stage", stage)
            .apply()
        _progression.value = _progression.value.copy(
            selectedWorldId = worldId,
            selectedStageNumber = stage
        )
    }

    fun unlockCharacter(charId: CharacterId): Boolean {
        val cur = _progression.value
        val prog = cur.characters[charId] ?: return false
        if (prog.isUnlocked) return true
        val def = CharacterRegistry.getDef(charId)
        if (cur.gems < def.unlockGemsCost) return false

        val newGems = cur.gems - def.unlockGemsCost
        val updatedChars = cur.characters.toMutableMap()
        updatedChars[charId] = prog.copy(isUnlocked = true)

        // Also unlock signature weapon
        val updatedWeapons = cur.weapons.toMutableMap()
        val curW = updatedWeapons[def.weaponType] ?: WeaponProgress(def.weaponType, 1, false)
        updatedWeapons[def.weaponType] = curW.copy(isUnlocked = true)

        prefs.edit()
            .putInt("player_gems", newGems)
            .putBoolean("char_unlocked_${charId.name}", true)
            .putBoolean("weapon_unlocked_${def.weaponType.name}", true)
            .apply()

        _progression.value = cur.copy(
            gems = newGems,
            characters = updatedChars,
            weapons = updatedWeapons,
            selectedCharacterId = charId,
            selectedWeaponType = def.weaponType
        )
        refreshMissions()
        return true
    }

    fun upgradeCharacter(charId: CharacterId): Boolean {
        val cur = _progression.value
        val prog = cur.characters[charId] ?: return false
        if (!prog.isUnlocked || prog.level >= 20) return false
        val cost = prog.getUpgradeCost()
        if (cur.coins < cost) return false

        val newCoins = cur.coins - cost
        val newLevel = prog.level + 1
        val updated = cur.characters.toMutableMap()
        updated[charId] = prog.copy(level = newLevel)

        prefs.edit()
            .putInt("player_coins", newCoins)
            .putInt("char_level_${charId.name}", newLevel)
            .apply()

        _progression.value = cur.copy(coins = newCoins, characters = updated)
        return true
    }

    fun unlockSkin(charId: CharacterId, skinIndex: Int): Boolean {
        val cur = _progression.value
        val prog = cur.characters[charId] ?: return false
        val def = CharacterRegistry.getDef(charId)
        val skinDef = def.skins.getOrNull(skinIndex) ?: return false
        if (prog.unlockedSkins.contains(skinIndex)) {
            // Already unlocked, just select
            return selectSkin(charId, skinIndex)
        }
        if (cur.gems < skinDef.unlockGemsCost) return false

        val newGems = cur.gems - skinDef.unlockGemsCost
        val newUnlockedSkins = prog.unlockedSkins + skinIndex
        val updated = cur.characters.toMutableMap()
        updated[charId] = prog.copy(selectedSkinIndex = skinIndex, unlockedSkins = newUnlockedSkins)

        prefs.edit()
            .putInt("player_gems", newGems)
            .putInt("char_skin_${charId.name}", skinIndex)
            .putString("char_skins_unlocked_${charId.name}", newUnlockedSkins.joinToString(","))
            .apply()

        _progression.value = cur.copy(gems = newGems, characters = updated)
        return true
    }

    fun selectSkin(charId: CharacterId, skinIndex: Int): Boolean {
        val cur = _progression.value
        val prog = cur.characters[charId] ?: return false
        if (!prog.unlockedSkins.contains(skinIndex)) return false

        val updated = cur.characters.toMutableMap()
        updated[charId] = prog.copy(selectedSkinIndex = skinIndex)

        prefs.edit().putInt("char_skin_${charId.name}", skinIndex).apply()
        _progression.value = cur.copy(characters = updated)
        return true
    }

    fun upgradeWeapon(type: WeaponType): Boolean {
        val cur = _progression.value
        val prog = cur.weapons[type] ?: return false
        if (!prog.isUnlocked || prog.level >= 10) return false
        val cost = prog.getUpgradeCost()
        if (cur.coins < cost) return false

        val newCoins = cur.coins - cost
        val newLevel = prog.level + 1
        val updated = cur.weapons.toMutableMap()
        updated[type] = prog.copy(level = newLevel)

        prefs.edit()
            .putInt("player_coins", newCoins)
            .putInt("weapon_lvl_${type.name}", newLevel)
            .apply()

        _progression.value = cur.copy(coins = newCoins, weapons = updated)
        return true
    }

    fun addRewards(
        earnedCoins: Int,
        earnedXp: Int,
        enemiesDefeated: Int,
        isStageVictory: Boolean,
        isBossDefeated: Boolean,
        abilitiesUsed: Int = 1
    ) {
        val cur = _progression.value
        var newXp = cur.xp + earnedXp
        var newLevel = cur.level
        var xpToNext = cur.xpToNextLevel
        var bonusGems = 0

        while (newXp >= xpToNext) {
            newXp -= xpToNext
            newLevel++
            xpToNext = newLevel * 200
            bonusGems += 25
        }

        val newCoins = cur.coins + earnedCoins
        val newGems = cur.gems + bonusGems
        val totalEnemies = cur.totalEnemiesDefeated + enemiesDefeated
        val totalStages = cur.totalStagesCleared + (if (isStageVictory) 1 else 0)
        val totalBosses = cur.totalBossesDefeated + (if (isBossDefeated) 1 else 0)
        val totalAbilities = cur.totalAbilitiesUsed + abilitiesUsed

        val unlockedWorlds = cur.unlockedWorlds.toMutableSet()
        if (newLevel >= 2) unlockedWorlds.add(WorldId.ANCIENT_RUINS)
        if (newLevel >= 4) unlockedWorlds.add(WorldId.FROZEN_VALLEY)
        if (newLevel >= 6) unlockedWorlds.add(WorldId.VOLCANIC_FORTRESS)
        if (newLevel >= 8) unlockedWorlds.add(WorldId.SHADOW_CITADEL)

        prefs.edit()
            .putInt("player_level", newLevel)
            .putInt("player_xp", newXp)
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .putInt("stat_enemies", totalEnemies)
            .putInt("stat_stages", totalStages)
            .putInt("stat_bosses", totalBosses)
            .putInt("stat_abilities", totalAbilities)
            .apply()

        _progression.value = cur.copy(
            level = newLevel,
            xp = newXp,
            xpToNextLevel = xpToNext,
            coins = newCoins,
            gems = newGems,
            totalEnemiesDefeated = totalEnemies,
            totalStagesCleared = totalStages,
            totalBossesDefeated = totalBosses,
            totalAbilitiesUsed = totalAbilities,
            unlockedWorlds = unlockedWorlds
        )

        refreshMissions()
        refreshAchievements()
    }

    fun addPurchasedGems(amount: Int) {
        val cur = _progression.value
        val newGems = cur.gems + amount
        prefs.edit().putInt("player_gems", newGems).apply()
        _progression.value = cur.copy(gems = newGems)
    }

    fun exchangeGemsForCoins(gemCost: Int, coinGain: Int): Boolean {
        val cur = _progression.value
        if (cur.gems < gemCost) return false
        val newGems = cur.gems - gemCost
        val newCoins = cur.coins + coinGain
        prefs.edit().putInt("player_gems", newGems).putInt("player_coins", newCoins).apply()
        _progression.value = cur.copy(gems = newGems, coins = newCoins)
        return true
    }

    fun claimDailyReward(dayNumber: Int, coinReward: Int, gemReward: Int): Boolean {
        val cur = _progression.value
        if (cur.dailyRewardDayClaimed >= dayNumber) return false

        val newCoins = cur.coins + coinReward
        val newGems = cur.gems + gemReward
        val now = System.currentTimeMillis()

        prefs.edit()
            .putInt("daily_day_claimed", dayNumber)
            .putLong("last_daily_time", now)
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .apply()

        _progression.value = cur.copy(
            dailyRewardDayClaimed = dayNumber,
            lastDailyClaimTime = now,
            coins = newCoins,
            gems = newGems
        )
        return true
    }

    fun claimMission(missionId: String): Boolean {
        val mission = _missions.value.firstOrNull { it.id == missionId } ?: return false
        if (mission.isClaimed || mission.current < mission.target) return false

        val cur = _progression.value
        val newCoins = cur.coins + mission.coinReward
        val newGems = cur.gems + mission.gemReward

        val updatedMissions = _missions.value.map {
            if (it.id == missionId) it.copy(isClaimed = true) else it
        }
        _missions.value = updatedMissions

        val claimedIds = updatedMissions.filter { it.isClaimed }.map { it.id }
        val arr = JSONArray(claimedIds)
        prefs.edit()
            .putString("missions_claimed_json", arr.toString())
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .apply()

        _progression.value = cur.copy(coins = newCoins, gems = newGems)
        return true
    }

    fun claimAchievement(achievementId: String): Boolean {
        val achievement = _achievements.value.firstOrNull { it.id == achievementId } ?: return false
        if (achievement.isClaimed || achievement.current < achievement.target) return false

        val cur = _progression.value
        val newGems = cur.gems + achievement.gemReward

        val updatedAchievements = _achievements.value.map {
            if (it.id == achievementId) it.copy(isClaimed = true) else it
        }
        _achievements.value = updatedAchievements

        val claimedIds = updatedAchievements.filter { it.isClaimed }.map { it.id }
        val arr = JSONArray(claimedIds)
        prefs.edit()
            .putString("achievements_claimed_json", arr.toString())
            .putInt("player_gems", newGems)
            .apply()

        _progression.value = cur.copy(gems = newGems)
        return true
    }

    fun claimFreeChest(): Pair<Int, Int> {
        val cur = _progression.value
        val coinReward = 150 + (cur.level * 40) + ((System.currentTimeMillis() % 100).toInt())
        val gemReward = 15 + ((System.currentTimeMillis() % 15).toInt())
        val now = System.currentTimeMillis()

        val newCoins = cur.coins + coinReward
        val newGems = cur.gems + gemReward

        prefs.edit()
            .putLong("last_free_chest_time", now)
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .apply()

        _progression.value = cur.copy(
            lastFreeChestTime = now,
            coins = newCoins,
            gems = newGems
        )
        return Pair(coinReward, gemReward)
    }

    fun setGraphicsQuality(quality: GraphicsQuality) {
        prefs.edit().putString("gfx_quality", quality.name).apply()
        _progression.value = _progression.value.copy(graphicsQuality = quality)
    }

    fun setAudioAndHaptics(sound: Boolean, music: Boolean, vibe: Boolean) {
        prefs.edit()
            .putBoolean("sound_enabled", sound)
            .putBoolean("music_enabled", music)
            .putBoolean("vibe_enabled", vibe)
            .apply()
        _progression.value = _progression.value.copy(
            soundEnabled = sound,
            musicEnabled = music,
            vibrationEnabled = vibe
        )
    }

    private fun refreshMissions() {
        _missions.value = loadMissions()
    }

    private fun refreshAchievements() {
        _achievements.value = loadAchievements()
    }
}

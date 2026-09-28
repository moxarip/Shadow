package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.game.model.AchievementItem
import com.example.game.model.ArenaId
import com.example.game.model.GameMode
import com.example.game.model.HeroId
import com.example.game.model.HeroProgress
import com.example.game.model.HeroRegistry
import com.example.game.model.MissionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class PlayerProgression(
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 200,
    val coins: Int = 600,
    val gems: Int = 300,
    val selectedHeroId: HeroId = HeroId.BLAZE,
    val selectedArenaId: ArenaId = ArenaId.DARK_FOREST,
    val selectedGameMode: GameMode = GameMode.STORY,
    val heroes: Map<HeroId, HeroProgress> = emptyMap(),
    val unlockedArenas: Set<ArenaId> = setOf(ArenaId.DARK_FOREST),
    val totalEnemiesDefeated: Int = 0,
    val totalBattlesWon: Int = 0,
    val totalAbilitiesUsed: Int = 0,
    val totalBossesDefeated: Int = 0,
    val highScoreEndlessWave: Int = 0,
    val dailyRewardDayClaimed: Int = 0,
    val lastDailyClaimTime: Long = 0L,
    val lastFreeChestTime: Long = 0L,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shadow_clash_save", Context.MODE_PRIVATE)

    private val _progression = MutableStateFlow(loadProgression())
    val progression: StateFlow<PlayerProgression> = _progression.asStateFlow()

    private val _missions = MutableStateFlow(loadMissions())
    val missions: StateFlow<List<MissionItem>> = _missions.asStateFlow()

    private val _achievements = MutableStateFlow(loadAchievements())
    val achievements: StateFlow<List<AchievementItem>> = _achievements.asStateFlow()

    private fun loadProgression(): PlayerProgression {
        val level = prefs.getInt("player_level", 1)
        val xp = prefs.getInt("player_xp", 0)
        val coins = prefs.getInt("player_coins", 600)
        val gems = prefs.getInt("player_gems", 300)
        val heroStr = prefs.getString("selected_hero", HeroId.BLAZE.name) ?: HeroId.BLAZE.name
        val selectedHero = try { HeroId.valueOf(heroStr) } catch (_: Exception) { HeroId.BLAZE }
        val arenaStr = prefs.getString("selected_arena", ArenaId.DARK_FOREST.name) ?: ArenaId.DARK_FOREST.name
        val selectedArena = try { ArenaId.valueOf(arenaStr) } catch (_: Exception) { ArenaId.DARK_FOREST }
        val modeStr = prefs.getString("selected_mode", GameMode.STORY.name) ?: GameMode.STORY.name
        val selectedMode = try { GameMode.valueOf(modeStr) } catch (_: Exception) { GameMode.STORY }

        val heroesMap = mutableMapOf<HeroId, HeroProgress>()
        HeroRegistry.allHeroes.forEach { def ->
            val unlocked = if (def.id == HeroId.BLAZE) true else prefs.getBoolean("hero_unlocked_${def.id.name}", false)
            val heroLevel = prefs.getInt("hero_level_${def.id.name}", 1)
            heroesMap[def.id] = HeroProgress(def.id, heroLevel, unlocked)
        }

        val unlockedArenas = mutableSetOf(ArenaId.DARK_FOREST)
        if (prefs.getBoolean("arena_unlocked_${ArenaId.ANCIENT_TEMPLE.name}", false) || level >= 3) {
            unlockedArenas.add(ArenaId.ANCIENT_TEMPLE)
        }
        if (prefs.getBoolean("arena_unlocked_${ArenaId.CYBER_ARENA.name}", false) || level >= 5) {
            unlockedArenas.add(ArenaId.CYBER_ARENA)
        }

        return PlayerProgression(
            level = level,
            xp = xp,
            xpToNextLevel = level * 200,
            coins = coins,
            gems = gems,
            selectedHeroId = selectedHero,
            selectedArenaId = selectedArena,
            selectedGameMode = selectedMode,
            heroes = heroesMap,
            unlockedArenas = unlockedArenas,
            totalEnemiesDefeated = prefs.getInt("stat_enemies", 0),
            totalBattlesWon = prefs.getInt("stat_battles", 0),
            totalAbilitiesUsed = prefs.getInt("stat_abilities", 0),
            totalBossesDefeated = prefs.getInt("stat_bosses", 0),
            highScoreEndlessWave = prefs.getInt("high_score_endless", 0),
            dailyRewardDayClaimed = prefs.getInt("daily_day_claimed", 0),
            lastDailyClaimTime = prefs.getLong("last_daily_time", 0L),
            lastFreeChestTime = prefs.getLong("last_free_chest_time", 0L),
            soundEnabled = prefs.getBoolean("sound_enabled", true),
            vibrationEnabled = prefs.getBoolean("vibe_enabled", true)
        )
    }

    private fun loadMissions(): List<MissionItem> {
        val jsonStr = prefs.getString("missions_data", null)
        val currentEnemies = prefs.getInt("stat_enemies", 0)
        val currentBattles = prefs.getInt("stat_battles", 0)
        val currentAbilities = prefs.getInt("stat_abilities", 0)

        val defaultList = listOf(
            MissionItem("m_enemies_20", "Slayer Initiate", "Defeat 20 enemies in arena combat.", 20, currentEnemies.coerceAtMost(20), 150, 0, false),
            MissionItem("m_enemies_100", "Master Executioner", "Defeat 100 enemies in battle.", 100, currentEnemies.coerceAtMost(100), 500, 20, false),
            MissionItem("m_battles_3", "Arena Contender", "Win 3 Story or Endless battles.", 3, currentBattles.coerceAtMost(3), 200, 25, false),
            MissionItem("m_battles_10", "Champion of the Arena", "Achieve 10 battle victories.", 10, currentBattles.coerceAtMost(10), 1000, 60, false),
            MissionItem("m_abilities_20", "Energy Conduit", "Unleash special hero abilities 20 times.", 20, currentAbilities.coerceAtMost(20), 250, 20, false),
            MissionItem("m_boss_1", "Colossus Breaker", "Defeat any arena Boss.", 1, prefs.getInt("stat_bosses", 0).coerceAtMost(1), 350, 30, false)
        )

        if (jsonStr.isNullOrEmpty()) return defaultList

        return try {
            val jsonArr = JSONArray(jsonStr)
            val claimedMap = mutableMapOf<String, Boolean>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                claimedMap[obj.getString("id")] = obj.getBoolean("claimed")
            }
            defaultList.map { m ->
                val isClaimed = claimedMap[m.id] ?: false
                m.copy(isClaimed = isClaimed)
            }
        } catch (_: Exception) {
            defaultList
        }
    }

    private fun loadAchievements(): List<AchievementItem> {
        val jsonStr = prefs.getString("achievements_data", null)
        val enemies = prefs.getInt("stat_enemies", 0)
        val battles = prefs.getInt("stat_battles", 0)
        val bosses = prefs.getInt("stat_bosses", 0)
        val maxHeroLevel = HeroRegistry.allHeroes.maxOf { def -> prefs.getInt("hero_level_${def.id.name}", 1) }
        val unlockedCount = HeroRegistry.allHeroes.count { def ->
            if (def.id == HeroId.BLAZE) true else prefs.getBoolean("hero_unlocked_${def.id.name}", false)
        }

        val defaultList = listOf(
            AchievementItem("ach_win_1", "First Blood", "Claim your first arena victory.", 1, battles.coerceAtMost(1), 50, false),
            AchievementItem("ach_kills_100", "Centurion", "Slay 100 enemies.", 100, enemies.coerceAtMost(100), 100, false),
            AchievementItem("ach_kills_500", "Shadow Destroyer", "Defeat 500 enemies across all arenas.", 500, enemies.coerceAtMost(500), 300, false),
            AchievementItem("ach_unlock_1", "Expanding Roster", "Unlock your first new hero.", 2, unlockedCount.coerceAtMost(2), 150, false),
            AchievementItem("ach_boss_1", "Titan's Fall", "Defeat your first epic Boss.", 1, bosses.coerceAtMost(1), 200, false),
            AchievementItem("ach_hero_lvl_10", "Awakened Power", "Upgrade any hero to max Level 10.", 10, maxHeroLevel.coerceAtMost(10), 500, false),
            AchievementItem("ach_wins_10", "Arena Warlord", "Win 10 battles.", 10, battles.coerceAtMost(10), 250, false)
        )

        if (jsonStr.isNullOrEmpty()) return defaultList

        return try {
            val jsonArr = JSONArray(jsonStr)
            val claimedMap = mutableMapOf<String, Boolean>()
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                claimedMap[obj.getString("id")] = obj.getBoolean("claimed")
            }
            defaultList.map { a ->
                val isClaimed = claimedMap[a.id] ?: false
                a.copy(isClaimed = isClaimed)
            }
        } catch (_: Exception) {
            defaultList
        }
    }

    fun selectHero(heroId: HeroId) {
        val current = _progression.value
        val hero = current.heroes[heroId]
        if (hero?.isUnlocked == true) {
            prefs.edit().putString("selected_hero", heroId.name).apply()
            _progression.value = current.copy(selectedHeroId = heroId)
        }
    }

    fun selectArena(arenaId: ArenaId) {
        val current = _progression.value
        if (current.unlockedArenas.contains(arenaId)) {
            prefs.edit().putString("selected_arena", arenaId.name).apply()
            _progression.value = current.copy(selectedArenaId = arenaId)
        }
    }

    fun selectGameMode(mode: GameMode) {
        prefs.edit().putString("selected_mode", mode.name).apply()
        _progression.value = _progression.value.copy(selectedGameMode = mode)
    }

    fun unlockHero(heroId: HeroId): Boolean {
        val current = _progression.value
        val hero = current.heroes[heroId] ?: return false
        if (hero.isUnlocked) return true
        val def = HeroRegistry.getDef(heroId)
        if (current.gems < def.unlockGemsCost) return false

        val newGems = current.gems - def.unlockGemsCost
        val updatedHeroes = current.heroes.toMutableMap()
        updatedHeroes[heroId] = hero.copy(isUnlocked = true)

        prefs.edit()
            .putInt("player_gems", newGems)
            .putBoolean("hero_unlocked_${heroId.name}", true)
            .apply()

        _progression.value = current.copy(
            gems = newGems,
            heroes = updatedHeroes,
            selectedHeroId = heroId
        )
        refreshAchievements()
        return true
    }

    fun upgradeHero(heroId: HeroId): Boolean {
        val current = _progression.value
        val hero = current.heroes[heroId] ?: return false
        if (!hero.isUnlocked || hero.level >= 10) return false
        val cost = hero.getUpgradeCost()
        if (current.coins < cost) return false

        val newCoins = current.coins - cost
        val newLevel = hero.level + 1
        val updatedHeroes = current.heroes.toMutableMap()
        updatedHeroes[heroId] = hero.copy(level = newLevel)

        prefs.edit()
            .putInt("player_coins", newCoins)
            .putInt("hero_level_${heroId.name}", newLevel)
            .apply()

        _progression.value = current.copy(
            coins = newCoins,
            heroes = updatedHeroes
        )
        refreshAchievements()
        return true
    }

    fun addRewards(earnedCoins: Int, earnedXp: Int, enemiesDefeated: Int, isVictory: Boolean, isBossDefeated: Boolean, abilitiesUsed: Int, endlessWave: Int = 0) {
        val current = _progression.value
        var newXp = current.xp + earnedXp
        var newLevel = current.level
        var xpToNext = current.xpToNextLevel
        var bonusGems = 0

        while (newXp >= xpToNext) {
            newXp -= xpToNext
            newLevel++
            xpToNext = newLevel * 200
            bonusGems += 20 // Level up reward
        }

        val newCoins = current.coins + earnedCoins
        val newGems = current.gems + bonusGems
        val totalEnemies = current.totalEnemiesDefeated + enemiesDefeated
        val totalWins = current.totalBattlesWon + (if (isVictory) 1 else 0)
        val totalAbilities = current.totalAbilitiesUsed + abilitiesUsed
        val totalBosses = current.totalBossesDefeated + (if (isBossDefeated) 1 else 0)
        val highScoreEndless = maxOf(current.highScoreEndlessWave, endlessWave)

        val unlockedArenas = current.unlockedArenas.toMutableSet()
        if (newLevel >= 3) unlockedArenas.add(ArenaId.ANCIENT_TEMPLE)
        if (newLevel >= 5) unlockedArenas.add(ArenaId.CYBER_ARENA)

        prefs.edit()
            .putInt("player_level", newLevel)
            .putInt("player_xp", newXp)
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .putInt("stat_enemies", totalEnemies)
            .putInt("stat_battles", totalWins)
            .putInt("stat_abilities", totalAbilities)
            .putInt("stat_bosses", totalBosses)
            .putInt("high_score_endless", highScoreEndless)
            .apply()

        _progression.value = current.copy(
            level = newLevel,
            xp = newXp,
            xpToNextLevel = xpToNext,
            coins = newCoins,
            gems = newGems,
            totalEnemiesDefeated = totalEnemies,
            totalBattlesWon = totalWins,
            totalAbilitiesUsed = totalAbilities,
            totalBossesDefeated = totalBosses,
            highScoreEndlessWave = highScoreEndless,
            unlockedArenas = unlockedArenas
        )

        refreshMissions()
        refreshAchievements()
    }

    fun addPurchasedGems(gemsToAdd: Int) {
        val current = _progression.value
        val newGems = current.gems + gemsToAdd
        prefs.edit().putInt("player_gems", newGems).apply()
        _progression.value = current.copy(gems = newGems)
    }

    fun exchangeGemsForCoins(gemsCost: Int, coinsToAdd: Int): Boolean {
        val current = _progression.value
        if (current.gems < gemsCost) return false
        val newGems = current.gems - gemsCost
        val newCoins = current.coins + coinsToAdd
        prefs.edit()
            .putInt("player_gems", newGems)
            .putInt("player_coins", newCoins)
            .apply()
        _progression.value = current.copy(gems = newGems, coins = newCoins)
        return true
    }

    fun claimDailyReward(): Boolean {
        val current = _progression.value
        val nextDay = (current.dailyRewardDayClaimed % 7) + 1
        val rewardDef = com.example.game.model.ShopCatalog.dailyRewards.firstOrNull { it.dayNumber == nextDay } ?: return false

        val newCoins = current.coins + rewardDef.coinReward
        val newGems = current.gems + rewardDef.gemReward
        val now = System.currentTimeMillis()

        prefs.edit()
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .putInt("daily_day_claimed", nextDay)
            .putLong("last_daily_time", now)
            .apply()

        _progression.value = current.copy(
            coins = newCoins,
            gems = newGems,
            dailyRewardDayClaimed = nextDay,
            lastDailyClaimTime = now
        )
        return true
    }

    fun claimFreeChest(): Pair<Int, Int> {
        val current = _progression.value
        val coins = (150..350).random()
        val gems = (5..15).random()
        val now = System.currentTimeMillis()

        val newCoins = current.coins + coins
        val newGems = current.gems + gems

        prefs.edit()
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .putLong("last_free_chest_time", now)
            .apply()

        _progression.value = current.copy(
            coins = newCoins,
            gems = newGems,
            lastFreeChestTime = now
        )
        return Pair(coins, gems)
    }

    fun claimMission(missionId: String): Boolean {
        val list = _missions.value.toMutableList()
        val idx = list.indexOfFirst { it.id == missionId }
        if (idx == -1) return false
        val item = list[idx]
        if (item.isClaimed || item.current < item.target) return false

        val current = _progression.value
        val newCoins = current.coins + item.coinReward
        val newGems = current.gems + item.gemReward

        list[idx] = item.copy(isClaimed = true)
        _missions.value = list

        val jsonArr = JSONArray()
        list.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("claimed", m.isClaimed)
            jsonArr.put(obj)
        }
        prefs.edit()
            .putInt("player_coins", newCoins)
            .putInt("player_gems", newGems)
            .putString("missions_data", jsonArr.toString())
            .apply()

        _progression.value = current.copy(coins = newCoins, gems = newGems)
        return true
    }

    fun claimAchievement(achId: String): Boolean {
        val list = _achievements.value.toMutableList()
        val idx = list.indexOfFirst { it.id == achId }
        if (idx == -1) return false
        val item = list[idx]
        if (item.isClaimed || item.current < item.target) return false

        val current = _progression.value
        val newGems = current.gems + item.gemReward

        list[idx] = item.copy(isClaimed = true)
        _achievements.value = list

        val jsonArr = JSONArray()
        list.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("claimed", a.isClaimed)
            jsonArr.put(obj)
        }
        prefs.edit()
            .putInt("player_gems", newGems)
            .putString("achievements_data", jsonArr.toString())
            .apply()

        _progression.value = current.copy(gems = newGems)
        return true
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _progression.value = _progression.value.copy(soundEnabled = enabled)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibe_enabled", enabled).apply()
        _progression.value = _progression.value.copy(vibrationEnabled = enabled)
    }

    private fun refreshMissions() {
        _missions.value = loadMissions()
    }

    private fun refreshAchievements() {
        _achievements.value = loadAchievements()
    }

    fun resetAllData() {
        prefs.edit().clear().apply()
        _progression.value = loadProgression()
        _missions.value = loadMissions()
        _achievements.value = loadAchievements()
    }
}

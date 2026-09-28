package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity storing overall player profile and progression permanently on the device.
 */
@Entity(tableName = "player_progression")
data class PlayerProgressionEntity(
    @PrimaryKey val id: Int = 1,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 200,
    val coins: Int = 800,
    val gems: Int = 400,
    val selectedCharacterId: String = "BLADE",
    val selectedWeaponType: String = "ENERGY_KATANA",
    val selectedWorldId: String = "NEON_DISTRICT",
    val selectedStageNumber: Int = 1,
    val highestStageCleared: Int = 0,
    val unlockedWorldsCsv: String = "NEON_DISTRICT",
    val totalEnemiesDefeated: Int = 0,
    val totalStagesCleared: Int = 0,
    val totalBossesDefeated: Int = 0,
    val totalAbilitiesUsed: Int = 0,
    val totalBattlesPlayed: Int = 0,
    val totalBattlesWon: Int = 0,
    val dailyRewardDayClaimed: Int = 0,
    val lastDailyClaimTime: Long = 0L,
    val lastFreeChestTime: Long = 0L,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val graphicsQuality: String = "MEDIUM",
    val updatedAt: Long = System.currentTimeMillis()
)

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity storing individual hero statistics, upgrades, skins, and lifetime combat records.
 */
@Entity(tableName = "hero_stats")
data class HeroStatsEntity(
    @PrimaryKey val heroId: String, // Maps to CharacterId.name (e.g. "BLADE", "GUNNER")
    val level: Int = 1,
    val isUnlocked: Boolean = false,
    val selectedSkinIndex: Int = 0,
    val unlockedSkinsCsv: String = "0", // Comma-separated skin indices, e.g. "0,1"

    // Combat & Lifetime Performance Statistics
    val battlesPlayed: Int = 0,
    val battlesWon: Int = 0,
    val enemiesDefeated: Int = 0,
    val bossesDefeated: Int = 0,
    val damageDealt: Long = 0L,
    val damageTaken: Long = 0L,
    val abilitiesUsed: Int = 0,
    val highestCombo: Int = 0,
    val highestScore: Int = 0,
    val totalPlayTimeSeconds: Long = 0L,
    val lastPlayedTimestamp: Long = 0L
)

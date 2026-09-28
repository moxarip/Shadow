package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity storing individual battle outcome logs and performance metrics.
 */
@Entity(tableName = "battle_records")
data class BattleRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val heroId: String,
    val weaponType: String,
    val worldId: String,
    val stageNumber: Int,
    val isVictory: Boolean,
    val isBossDefeated: Boolean,
    val enemiesKilled: Int,
    val coinsEarned: Int,
    val xpEarned: Int,
    val maxCombo: Int = 0,
    val damageDealt: Long = 0L,
    val durationSeconds: Float = 0f
)

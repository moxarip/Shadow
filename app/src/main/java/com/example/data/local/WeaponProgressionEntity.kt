package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity storing weapon unlock, upgrade levels, and battle statistics.
 */
@Entity(tableName = "weapon_progression")
data class WeaponProgressionEntity(
    @PrimaryKey val weaponType: String, // Maps to WeaponType.name (e.g. "ENERGY_KATANA")
    val level: Int = 1,
    val isUnlocked: Boolean = false,
    val battlesUsed: Int = 0,
    val enemiesDefeated: Int = 0
)

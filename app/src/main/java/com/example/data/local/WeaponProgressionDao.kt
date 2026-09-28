package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeaponProgressionDao {

    @Query("SELECT * FROM weapon_progression")
    fun getAllWeaponsFlow(): Flow<List<WeaponProgressionEntity>>

    @Query("SELECT * FROM weapon_progression WHERE weaponType = :type")
    suspend fun getWeapon(type: String): WeaponProgressionEntity?

    @Query("SELECT * FROM weapon_progression")
    suspend fun getAllWeapons(): List<WeaponProgressionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(weapon: WeaponProgressionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weapons: List<WeaponProgressionEntity>)

    @Query("UPDATE weapon_progression SET level = :level WHERE weaponType = :type")
    suspend fun updateLevel(type: String, level: Int)

    @Query("UPDATE weapon_progression SET isUnlocked = :unlocked WHERE weaponType = :type")
    suspend fun updateUnlocked(type: String, unlocked: Boolean)

    @Query("""
        UPDATE weapon_progression
        SET battlesUsed = battlesUsed + 1,
            enemiesDefeated = enemiesDefeated + :enemies
        WHERE weaponType = :type
    """)
    suspend fun recordWeaponBattle(type: String, enemies: Int)

    @Query("DELETE FROM weapon_progression")
    suspend fun deleteAll()
}

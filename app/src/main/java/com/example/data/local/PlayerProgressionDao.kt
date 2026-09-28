package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProgressionDao {

    @Query("SELECT * FROM player_progression WHERE id = 1")
    fun getProgressionFlow(): Flow<PlayerProgressionEntity?>

    @Query("SELECT * FROM player_progression WHERE id = 1")
    suspend fun getProgression(): PlayerProgressionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progression: PlayerProgressionEntity)

    @Query("UPDATE player_progression SET coins = :coins, gems = :gems, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateCurrencies(coins: Int, gems: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_progression SET selectedCharacterId = :heroId, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateSelectedHero(heroId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_progression SET selectedWeaponType = :weaponType, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateSelectedWeapon(weaponType: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_progression SET selectedWorldId = :worldId, selectedStageNumber = :stageNumber, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateSelectedStage(worldId: String, stageNumber: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE player_progression SET soundEnabled = :sound, musicEnabled = :music, vibrationEnabled = :vibration WHERE id = 1")
    suspend fun updateAudioSettings(sound: Boolean, music: Boolean, vibration: Boolean)

    @Query("UPDATE player_progression SET graphicsQuality = :quality WHERE id = 1")
    suspend fun updateGraphicsQuality(quality: String)

    @Query("DELETE FROM player_progression")
    suspend fun deleteAll()
}

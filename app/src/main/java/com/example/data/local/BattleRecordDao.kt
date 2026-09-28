package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BattleRecordDao {

    @Query("SELECT * FROM battle_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBattlesFlow(limit: Int = 20): Flow<List<BattleRecordEntity>>

    @Query("SELECT * FROM battle_records WHERE heroId = :heroId ORDER BY timestamp DESC LIMIT :limit")
    fun getBattlesForHeroFlow(heroId: String, limit: Int = 20): Flow<List<BattleRecordEntity>>

    @Query("SELECT COUNT(*) FROM battle_records WHERE heroId = :heroId AND isVictory = 1")
    suspend fun getVictoriesForHero(heroId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BattleRecordEntity): Long

    @Query("DELETE FROM battle_records")
    suspend fun deleteAll()
}

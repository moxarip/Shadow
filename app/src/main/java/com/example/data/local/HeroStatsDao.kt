package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroStatsDao {

    @Query("SELECT * FROM hero_stats ORDER BY heroId ASC")
    fun getAllHeroStatsFlow(): Flow<List<HeroStatsEntity>>

    @Query("SELECT * FROM hero_stats WHERE heroId = :heroId")
    fun getHeroStatsFlow(heroId: String): Flow<HeroStatsEntity?>

    @Query("SELECT * FROM hero_stats WHERE heroId = :heroId")
    suspend fun getHeroStats(heroId: String): HeroStatsEntity?

    @Query("SELECT * FROM hero_stats")
    suspend fun getAllHeroStats(): List<HeroStatsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(hero: HeroStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(heroes: List<HeroStatsEntity>)

    @Query("UPDATE hero_stats SET level = :level WHERE heroId = :heroId")
    suspend fun updateLevel(heroId: String, level: Int)

    @Query("UPDATE hero_stats SET isUnlocked = :unlocked WHERE heroId = :heroId")
    suspend fun updateUnlocked(heroId: String, unlocked: Boolean)

    @Query("UPDATE hero_stats SET selectedSkinIndex = :skinIndex, unlockedSkinsCsv = :skinsCsv WHERE heroId = :heroId")
    suspend fun updateSkins(heroId: String, skinIndex: Int, skinsCsv: String)

    @Query("""
        UPDATE hero_stats
        SET battlesPlayed = battlesPlayed + 1,
            battlesWon = battlesWon + CASE WHEN :isVictory = 1 THEN 1 ELSE 0 END,
            enemiesDefeated = enemiesDefeated + :enemies,
            bossesDefeated = bossesDefeated + :bosses,
            damageDealt = damageDealt + :damage,
            highestCombo = CASE WHEN :combo > highestCombo THEN :combo ELSE highestCombo END,
            totalPlayTimeSeconds = totalPlayTimeSeconds + :durationSeconds,
            lastPlayedTimestamp = :timestamp
        WHERE heroId = :heroId
    """)
    suspend fun recordBattle(
        heroId: String,
        isVictory: Boolean,
        enemies: Int,
        bosses: Int,
        damage: Long,
        combo: Int,
        durationSeconds: Long,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM hero_stats")
    suspend fun deleteAll()
}

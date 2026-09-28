package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room Database for Shadow Warriors.
 * Permanently stores player progression, hero statistics, weapon progression, and battle history.
 */
@Database(
    entities = [
        PlayerProgressionEntity::class,
        HeroStatsEntity::class,
        WeaponProgressionEntity::class,
        BattleRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerProgressionDao(): PlayerProgressionDao
    abstract fun heroStatsDao(): HeroStatsDao
    abstract fun weaponProgressionDao(): WeaponProgressionDao
    abstract fun battleRecordDao(): BattleRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shadow_warriors_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

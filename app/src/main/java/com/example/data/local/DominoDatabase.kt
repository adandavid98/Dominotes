package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MatchEntity::class, RoundEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DominoDatabase : RoomDatabase() {
    abstract fun dominoDao(): DominoDao

    companion object {
        @Volatile
        private var INSTANCE: DominoDatabase? = null

        fun getDatabase(context: Context): DominoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DominoDatabase::class.java,
                    "domino_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

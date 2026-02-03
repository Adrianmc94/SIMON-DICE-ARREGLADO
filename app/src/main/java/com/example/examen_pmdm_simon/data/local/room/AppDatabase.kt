package com.example.examen_pmdm_simon.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.examen_pmdm_simon.data.local.room.PartidaEntity

@Database(entities = [PartidaEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun partidaDao(): PartidaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Patrón Singleton para Room
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simon_room_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
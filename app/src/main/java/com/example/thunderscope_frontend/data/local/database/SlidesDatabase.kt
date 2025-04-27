package com.example.thunderscope_frontend.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thunderscope_frontend.data.models.SlidesEntity

@Database(entities = [SlidesEntity::class], version = 1, exportSchema = false)
abstract class SlidesDatabase : RoomDatabase() {
    abstract fun slidesDao(): SlidesDao

    companion object {
        @Volatile
        private var INSTANCE: SlidesDatabase? = null

        fun getDatabase(context: Context): SlidesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SlidesDatabase::class.java,
                    "Thunderscope.DB"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

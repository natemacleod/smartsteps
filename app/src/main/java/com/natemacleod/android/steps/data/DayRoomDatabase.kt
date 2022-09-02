package com.natemacleod.android.steps.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Day::class], version = 1, exportSchema = false)
abstract class DayRoomDatabase : RoomDatabase() {
    abstract fun dayDao(): DayDao

    companion object {
        @Volatile
        private var INSTANCE: DayRoomDatabase? = null

        fun getDatabase(context: Context): DayRoomDatabase {
            if (INSTANCE == null) synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DayRoomDatabase::class.java,
                    "day_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                return instance
            } else {
                return INSTANCE!!
            }
        }
    }
}

package com.example.healthocr.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [
        Metric::class,
        DBDevice::class,
        SessionInfo::class
    ]
)
abstract class AppRoomDatabase: RoomDatabase() {
    abstract fun getAppDAO(): AppDAO

    companion object {
        private var INSTANCE: AppRoomDatabase? = null
        fun getInstance(context: Context): AppRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppRoomDatabase::class.java,
                        "usersdb"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

package com.personal.voicememo.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.personal.voicememo.data.database.dao.VoiceMemoDao
import com.personal.voicememo.data.database.converter.DateConverter
import com.personal.voicememo.data.models.VoiceMemo

@Database(
    entities = [VoiceMemo::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voiceMemoDao(): VoiceMemoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voice_memo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 
package com.personal.voicememo.di

import android.content.Context
import androidx.room.Room
import com.personal.voicememo.data.database.VoiceMemoDatabase
import com.personal.voicememo.data.database.dao.VoiceMemoDao

object DatabaseModule {
    private var database: VoiceMemoDatabase? = null

    fun provideDatabase(context: Context): VoiceMemoDatabase {
        return database ?: synchronized(this) {
            database ?: buildDatabase(context).also { database = it }
        }
    }

    fun provideVoiceMemoDao(database: VoiceMemoDatabase): VoiceMemoDao {
        return database.voiceMemoDao()
    }

    private fun buildDatabase(context: Context): VoiceMemoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            VoiceMemoDatabase::class.java,
            "voice_memo.db"
        ).build()
    }
} 
package com.personal.voicememo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.data.database.dao.VoiceMemoDao
import com.personal.voicememo.data.database.converters.DateConverter

@Database(entities = [VoiceMemo::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class VoiceMemoDatabase : RoomDatabase() {
    abstract fun voiceMemoDao(): VoiceMemoDao
} 
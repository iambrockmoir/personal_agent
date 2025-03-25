package com.personal.voicememo.data.database.dao

import androidx.room.*
import com.personal.voicememo.data.models.VoiceMemo

@Dao
interface VoiceMemoDao {
    @Insert
    suspend fun insertMemo(memo: VoiceMemo): Long

    @Update
    suspend fun updateMemo(memo: VoiceMemo)

    @Delete
    suspend fun deleteMemo(memo: VoiceMemo)

    @Query("SELECT * FROM voice_memos WHERE id = :id")
    suspend fun getMemoById(id: Long): VoiceMemo?

    @Query("SELECT * FROM voice_memos ORDER BY createdAt DESC")
    suspend fun getAllMemos(): List<VoiceMemo>
} 
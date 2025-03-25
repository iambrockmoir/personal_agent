package com.personal.voicememo.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "voice_memos")
data class VoiceMemo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audioFilePath: String,
    val transcription: String? = null,
    val pineconeId: String? = null,
    val createdAt: Date
)

data class TranscriptionResult(
    val text: String,
    val error: String? = null
) 
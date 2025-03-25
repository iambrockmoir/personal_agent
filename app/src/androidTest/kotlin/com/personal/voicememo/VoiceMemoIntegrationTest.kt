package com.personal.voicememo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.personal.voicememo.data.Result
import com.personal.voicememo.data.database.VoiceMemoDatabase
import com.personal.voicememo.data.database.dao.VoiceMemoDao
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.data.service.WhisperTranscriptionService
import com.personal.voicememo.data.service.OpenAIEmbeddingService
import com.personal.voicememo.data.service.PineconeStorageService
import com.personal.voicememo.data.repository.VoiceMemoRepository
import com.personal.voicememo.service.AudioRecordingService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class VoiceMemoIntegrationTest {
    private lateinit var db: VoiceMemoDatabase
    private lateinit var dao: VoiceMemoDao
    private lateinit var repository: VoiceMemoRepository
    private lateinit var context: Context
    private lateinit var audioService: AudioRecordingService
    private lateinit var whisperService: WhisperTranscriptionService
    private lateinit var openAIService: OpenAIEmbeddingService
    private lateinit var pineconeService: PineconeStorageService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Set up in-memory database
        db = Room.inMemoryDatabaseBuilder(context, VoiceMemoDatabase::class.java).build()
        dao = db.voiceMemoDao()

        // Mock services
        whisperService = mockk()
        openAIService = mockk()
        pineconeService = mockk()
        audioService = AudioRecordingService()

        // Set up repository
        repository = VoiceMemoRepository(
            voiceMemoDao = dao,
            whisperService = whisperService,
            openAIService = openAIService,
            pineconeService = pineconeService
        )
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun testFullVoiceMemoFlow() = runBlocking {
        // Given
        val expectedTranscription = "This is a test transcription"
        val embedding = listOf(0.1f, 0.2f, 0.3f)
        
        coEvery { 
            whisperService.transcribeAudio(any())
        } returns expectedTranscription

        coEvery { 
            openAIService.generateEmbedding(expectedTranscription)
        } returns embedding

        coEvery { 
            pineconeService.upsertVector(any())
        } returns "test_vector_id"

        // When - Record audio
        val outputFile = File(context.cacheDir, "test_recording.m4a")
        audioService.startRecording(outputFile)
        Thread.sleep(1000) // Record for 1 second
        audioService.stopRecording()

        // Then - Verify recording
        assertTrue(outputFile.exists())

        // When - Save and process memo
        val result = repository.saveMemo(outputFile)
        assertTrue(result is Result.Success)
        val memo = (result as Result.Success).data
        assertNotNull(memo.id)

        // When - Transcribe
        val transcriptionResult = repository.transcribeMemo(memo)
        assertTrue(transcriptionResult is Result.Success)
        val transcribedMemo = (transcriptionResult as Result.Success).data
        assertEquals(expectedTranscription, transcribedMemo.transcription)

        // When - Save to vector DB
        val vectorResult = repository.saveToVectorDB(transcribedMemo)
        assertTrue(vectorResult is Result.Success)
        val finalMemo = (vectorResult as Result.Success).data
        assertNotNull(finalMemo.pineconeId)

        // Cleanup
        outputFile.delete()
        audioService.deleteRecording()
    }
} 
package com.personal.voicememo.data.repository

import android.util.Log
import com.personal.voicememo.data.Result
import com.personal.voicememo.data.database.dao.VoiceMemoDao
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.service.OpenAIService
import com.personal.voicememo.service.PineconeService
import com.personal.voicememo.service.WhisperService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class VoiceMemoRepositoryTest {
    private lateinit var repository: VoiceMemoRepository
    private lateinit var voiceMemoDao: VoiceMemoDao
    private lateinit var whisperService: WhisperService
    private lateinit var openAIService: OpenAIService
    private lateinit var pineconeService: PineconeService

    @Before
    fun setup() {
        voiceMemoDao = mockk(relaxed = true)
        whisperService = mockk(relaxed = true)
        openAIService = mockk(relaxed = true)
        pineconeService = mockk(relaxed = true)
        repository = VoiceMemoRepository(voiceMemoDao, whisperService, openAIService, pineconeService)
    }

    @Test
    fun `saveMemo saves audio file reference to database`() = runTest {
        // Create a temporary file for testing
        val file = File.createTempFile("test", ".m4a").apply { writeText("dummy content") }
        coEvery { voiceMemoDao.insertMemo(any()) } returns 1L

        // When
        val result = repository.saveMemo(file)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).data.id)
        coVerify { voiceMemoDao.insertMemo(any()) }

        file.delete()
    }

    @Test
    fun `transcribeMemo transcribes audio and updates database`() = runTest {
        // Create a temporary audio file that exists
        val tempFile = File.createTempFile("test", ".m4a").apply { writeText("dummy content") }
        val memo = VoiceMemo(
            id = 1L,
            audioFilePath = tempFile.absolutePath,
            createdAt = Date()
        )
        val transcription = "Test transcription"
        coEvery { whisperService.transcribeAudio(any()) } returns transcription
        coEvery { voiceMemoDao.updateMemo(any()) } just Runs

        // When
        val result = repository.transcribeMemo(memo)

        // Then
        assertTrue(result is Result.Success)
        assertEquals(transcription, (result as Result.Success).data.transcription)
        coVerify { whisperService.transcribeAudio(any()) }
        coVerify { voiceMemoDao.updateMemo(any()) }

        tempFile.delete()
    }

    @Test
    fun `saveToVectorDB generates embeddings and stores in Pinecone`() = runTest {
        // Create a temporary audio file that exists
        val tempFile = File.createTempFile("test", ".m4a").apply { writeText("dummy content") }
        val memo = VoiceMemo(
            id = 1L,
            audioFilePath = tempFile.absolutePath,
            transcription = "Test transcription",
            createdAt = Date()
        )
        val embedding = listOf(0.1f, 0.2f, 0.3f)
        coEvery { openAIService.generateEmbedding(memo.transcription!!) } returns embedding
        coEvery { pineconeService.upsertVector(any()) } returns "test_vector_id"
        coEvery { voiceMemoDao.updateMemo(any()) } just Runs

        // When
        val result = repository.saveToVectorDB(memo)

        // Then
        assertTrue(result is Result.Success)
        assertEquals("test_vector_id", (result as Result.Success).data.pineconeId)
        coVerify { openAIService.generateEmbedding(memo.transcription!!) }
        coVerify { pineconeService.upsertVector(any()) }
        coVerify { voiceMemoDao.updateMemo(any()) }

        tempFile.delete()
    }

    @Test
    fun `deleteMemo removes memo from database and vector store`() = runTest {
        // Given
        val memoId = 1L
        val memo = VoiceMemo(
            id = memoId,
            audioFilePath = "test.m4a",
            pineconeId = "test_vector_id",
            createdAt = Date()
        )
        coEvery { voiceMemoDao.getMemoById(memoId) } returns memo
        coEvery { voiceMemoDao.deleteMemo(memo) } just Runs
        coEvery { pineconeService.deleteVector(memo.pineconeId!!) } just Runs

        // When
        val result = repository.deleteMemo(memoId)

        // Then
        assertTrue(result is Result.Success)
        coVerify { voiceMemoDao.deleteMemo(memo) }
        coVerify { pineconeService.deleteVector(memo.pineconeId!!) }
    }

    @Test
    fun `getAllMemos returns memos from database`() = runTest {
        // Given
        val memos = listOf(
            VoiceMemo(id = 1, audioFilePath = "test1.m4a", createdAt = Date()),
            VoiceMemo(id = 2, audioFilePath = "test2.m4a", createdAt = Date())
        )
        coEvery { voiceMemoDao.getAllMemos() } returns memos

        // When
        val result = repository.getAllMemos()

        // Then
        assertTrue(result is Result.Success)
        assertEquals(memos, (result as Result.Success).data)
        coVerify { voiceMemoDao.getAllMemos() }
    }

    @Test
    fun `when database operation fails, returns Failure result`() = runTest {
        // Given
        val error = RuntimeException("Database error")
        coEvery { voiceMemoDao.getAllMemos() } throws error

        // When
        val result = repository.getAllMemos()

        // Then
        assertTrue(result is Result.Failure)
        assertEquals(error, (result as Result.Failure).exception)
    }
} 
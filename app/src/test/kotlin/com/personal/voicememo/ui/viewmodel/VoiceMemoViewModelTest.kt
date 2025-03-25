package com.personal.voicememo.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.personal.voicememo.data.Result
import com.personal.voicememo.data.models.VoiceMemo
import com.personal.voicememo.data.repository.VoiceMemoRepository
import com.personal.voicememo.service.AudioRecordingService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class VoiceMemoViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: VoiceMemoViewModel
    private lateinit var repository: VoiceMemoRepository
    private lateinit var audioService: AudioRecordingService
    private lateinit var context: Context
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        context = mockk(relaxed = true)
        repository = mockk(relaxed = true)
        audioService = mockk(relaxed = true)
        
        coEvery { repository.getAllMemos() } returns Result.Success(emptyList())
        
        viewModel = VoiceMemoViewModel(
            context = context,
            voiceMemoRepository = repository,
            audioRecordingService = audioService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startRecording updates isRecording state`() = runTest {
        // Given
        val testDir = File("/test")
        every { context.getExternalFilesDir(null) } returns testDir
        every { audioService.startRecording(any()) } just Runs

        // When
        viewModel.startRecording()

        // Then
        assertNotNull(viewModel.isRecording.value)
        assertEquals(true, viewModel.isRecording.value)
        verify { audioService.startRecording(any()) }
    }

    @Test
    fun `stopRecording updates isRecording state and saves memo`() = runTest {
        // Given
        val testDir = File("/test")
        val recordingFile = File(testDir, "recording.m4a")
        every { context.getExternalFilesDir(null) } returns testDir
        every { audioService.stopRecording() } just Runs
        every { audioService.getCurrentFilePath() } returns recordingFile.absolutePath

        val memo = VoiceMemo(
            id = 1L,
            audioFilePath = recordingFile.absolutePath,
            createdAt = Date()
        )
        val transcribedMemo = memo.copy(transcription = "Test transcription")
        val vectorizedMemo = transcribedMemo.copy(pineconeId = "test_vector_id")
        
        coEvery { repository.saveMemo(any()) } returns Result.Success(memo)
        coEvery { repository.transcribeMemo(memo) } returns Result.Success(transcribedMemo)
        coEvery { repository.saveToVectorDB(transcribedMemo) } returns Result.Success(vectorizedMemo)
        coEvery { repository.getAllMemos() } returns Result.Success(listOf(vectorizedMemo))

        // When
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(viewModel.isRecording.value)
        assertEquals(false, viewModel.isRecording.value)
        coVerifySequence {
            repository.getAllMemos() // Initial load
            repository.saveMemo(any())
            repository.transcribeMemo(memo)
            repository.saveToVectorDB(transcribedMemo)
            repository.getAllMemos() // Refresh after save
        }
    }

    @Test
    fun `deleteMemo deletes memo and refreshes list`() = runTest {
        // Given
        val memo = VoiceMemo(
            id = 1L,
            audioFilePath = "/test/audio.m4a",
            createdAt = Date()
        )
        coEvery { repository.deleteMemo(memo.id) } returns Result.Success(Unit)
        coEvery { repository.getAllMemos() } returns Result.Success(emptyList())

        // When
        viewModel.deleteMemo(memo)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerifySequence {
            repository.getAllMemos() // Initial load
            repository.deleteMemo(memo.id)
            repository.getAllMemos() // Refresh after delete
        }
    }
} 
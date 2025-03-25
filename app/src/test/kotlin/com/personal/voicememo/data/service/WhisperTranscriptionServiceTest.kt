package com.personal.voicememo.data.service

import android.util.Log
import com.personal.voicememo.data.network.WhisperService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WhisperTranscriptionServiceTest {
    private lateinit var service: WhisperTranscriptionService
    private lateinit var whisperService: WhisperService

    @Before
    fun setup() {
        whisperService = mockk(relaxed = true)
        service = WhisperTranscriptionService(whisperService)
    }

    @Test
    fun `transcribeAudio returns transcription on success`() = runTest {
        // Create a temporary file with m4a extension containing dummy content
        val tempFile = File.createTempFile("test", ".m4a").apply { writeText("dummy content") }

        val expectedText = "Test transcription"
        coEvery { 
            whisperService.transcribeAudio(any(), any()) 
        } returns WhisperService.TranscriptionResponse(expectedText)

        // When
        val result = service.transcribeAudio(tempFile)

        // Then
        assertEquals(expectedText, result)

        // Clean up
        tempFile.delete()
    }

    @Test
    fun `transcribeAudio throws exception when file does not exist`() = runTest {
        // Create a File instance pointing to a nonexistent file
        val nonExistentFile = File("nonexistent_file.m4a")

        // Then
        assertFailsWith<IllegalArgumentException> {
            service.transcribeAudio(nonExistentFile)
        }
    }

    @Test
    fun `transcribeAudio throws exception for invalid file format`() = runTest {
        // Create a temporary file with an invalid extension (.txt)
        val tempFile = File.createTempFile("test", ".txt").apply { writeText("dummy content") }

        // Then
        assertFailsWith<IllegalArgumentException> {
            service.transcribeAudio(tempFile)
        }
        tempFile.delete()
    }

    @Test
    fun `transcribeAudio throws exception when API call fails`() = runTest {
        // Create a temporary file with m4a extension containing dummy content
        val tempFile = File.createTempFile("test", ".m4a").apply { writeText("dummy content") }
        
        coEvery { 
            whisperService.transcribeAudio(any(), any()) 
        } throws RuntimeException("API Error")

        // Then
        assertFailsWith<RuntimeException> {
            service.transcribeAudio(tempFile)
        }
        tempFile.delete()
    }
} 
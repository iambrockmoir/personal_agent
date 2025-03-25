package com.personal.voicememo.service

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
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
class AudioRecordingServiceTest {
    private lateinit var service: AudioRecordingService
    private lateinit var outputFile: File
    private lateinit var context: Context

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        outputFile = mockk(relaxed = true) {
            every { exists() } returns true
            every { absolutePath } returns "/test/audio.m4a"
            every { delete() } returns true
            every { length() } returns 1024L
        }

        mockkConstructor(MediaRecorder::class)
        every { anyConstructed<MediaRecorder>().setAudioSource(any()) } just Runs
        every { anyConstructed<MediaRecorder>().setOutputFormat(any()) } just Runs
        every { anyConstructed<MediaRecorder>().setAudioEncoder(any()) } just Runs
        every { anyConstructed<MediaRecorder>().setOutputFile(any<String>()) } just Runs
        every { anyConstructed<MediaRecorder>().prepare() } just Runs
        every { anyConstructed<MediaRecorder>().start() } just Runs
        every { anyConstructed<MediaRecorder>().stop() } just Runs
        every { anyConstructed<MediaRecorder>().release() } just Runs

        service = AudioRecordingService(context)
    }

    @Test
    fun `startRecording initializes recording`() = runTest {
        // When
        service.startRecording(outputFile)

        // Then
        assertEquals("/test/audio.m4a", service.getCurrentFilePath())
        verifyOrder {
            anyConstructed<MediaRecorder>().setAudioSource(MediaRecorder.AudioSource.MIC)
            anyConstructed<MediaRecorder>().setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            anyConstructed<MediaRecorder>().setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            anyConstructed<MediaRecorder>().setOutputFile("/test/audio.m4a")
            anyConstructed<MediaRecorder>().prepare()
            anyConstructed<MediaRecorder>().start()
        }
    }

    @Test
    fun `stopRecording stops recording`() = runTest {
        // Given
        service.startRecording(outputFile)

        // When
        service.stopRecording()

        // Then
        verifyOrder {
            anyConstructed<MediaRecorder>().stop()
            anyConstructed<MediaRecorder>().release()
        }
        assertFailsWith<IllegalStateException> {
            service.stopRecording()
        }
    }

    @Test
    fun `startRecording throws exception if already recording`() = runTest {
        // Given
        service.startRecording(outputFile)

        // Then
        assertFailsWith<IllegalStateException> {
            service.startRecording(outputFile)
        }
    }

    @Test
    fun `stopRecording throws exception if not recording`() = runTest {
        assertFailsWith<IllegalStateException> {
            service.stopRecording()
        }
    }

    @Test
    fun `deleteRecording deletes current recording`() = runTest {
        // Given
        service.startRecording(outputFile)

        // When
        service.deleteRecording()

        // Then
        assertEquals(null, service.getCurrentFilePath())
        verify { outputFile.delete() }
    }
} 
package com.personal.voicememo.service

import android.content.Context
import android.media.MediaRecorder
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AudioRecordingServiceTest {
    private lateinit var context: Context
    private lateinit var mediaRecorder: FakeMediaRecorder
    private lateinit var service: TestAudioRecordingService
    private val filePath = "/test/path/recording.m4a"
    private lateinit var file: File

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mediaRecorder = FakeMediaRecorder()
        file = mockk {
            every { delete() } returns true
        }
        service = TestAudioRecordingService(mediaRecorder, file)
    }

    @Test
    fun `startRecording initializes recording`() {
        service.startRecording(filePath)
        assertEquals(filePath, service.getCurrentFilePath())
        assertEquals(MediaRecorder.AudioSource.MIC, mediaRecorder.getAudioSource())
        assertEquals(MediaRecorder.OutputFormat.MPEG_4, mediaRecorder.getOutputFormat())
        assertEquals(MediaRecorder.AudioEncoder.AAC, mediaRecorder.getAudioEncoder())
        assertEquals(filePath, mediaRecorder.getOutputFile())
        assertEquals(true, mediaRecorder.isPrepared())
        assertEquals(true, mediaRecorder.isStarted())
    }

    @Test
    fun `startRecording throws exception if already recording`() {
        service.startRecording(filePath)
        assertFailsWith<IllegalStateException> {
            service.startRecording(filePath)
        }
    }

    @Test
    fun `stopRecording stops recording`() {
        service.startRecording(filePath)
        service.stopRecording()
        assertNull(service.getCurrentFilePath())
        assertEquals(true, mediaRecorder.isStopped())
        assertEquals(true, mediaRecorder.isReleased())
    }

    @Test
    fun `stopRecording throws exception if not recording`() {
        assertFailsWith<IllegalStateException> {
            service.stopRecording()
        }
    }

    @Test
    fun `deleteRecording deletes current recording`() {
        service.startRecording(filePath)
        service.deleteRecording()
        assertNull(service.getCurrentFilePath())
        verify { file.delete() }
    }
}

private class TestAudioRecordingService(
    private val mediaRecorder: MediaRecorder,
    private val file: File
) : AudioRecordingService {
    private var currentFilePath: String? = null
    private var isRecording = false

    override fun startRecording(filePath: String) {
        if (isRecording) {
            throw IllegalStateException("Recording is already in progress")
        }

        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(filePath)
            prepare()
            start()
        }
        currentFilePath = filePath
        isRecording = true
    }

    override fun stopRecording() {
        if (!isRecording) {
            throw IllegalStateException("No recording in progress")
        }

        mediaRecorder.apply {
            stop()
            release()
        }
        currentFilePath = null
        isRecording = false
    }

    override fun deleteRecording() {
        currentFilePath?.let {
            file.delete()
        }
        currentFilePath = null
    }

    override fun getCurrentFilePath(): String? = currentFilePath
}

private class FakeMediaRecorder : MediaRecorder() {
    private var _audioSource: Int = -1
    private var _outputFormat: Int = -1
    private var _audioEncoder: Int = -1
    private var _outputFile: String? = null
    private var _isPrepared: Boolean = false
    private var _isStarted: Boolean = false
    private var _isStopped: Boolean = false
    private var _isReleased: Boolean = false

    override fun setAudioSource(audioSource: Int) {
        _audioSource = audioSource
    }

    override fun setOutputFormat(outputFormat: Int) {
        _outputFormat = outputFormat
    }

    override fun setAudioEncoder(audioEncoder: Int) {
        _audioEncoder = audioEncoder
    }

    override fun setOutputFile(path: String) {
        _outputFile = path
    }

    override fun prepare() {
        _isPrepared = true
    }

    override fun start() {
        _isStarted = true
    }

    override fun stop() {
        _isStopped = true
    }

    override fun release() {
        _isReleased = true
    }

    fun getAudioSource(): Int = _audioSource
    fun getOutputFormat(): Int = _outputFormat
    fun getAudioEncoder(): Int = _audioEncoder
    fun getOutputFile(): String? = _outputFile
    fun isPrepared(): Boolean = _isPrepared
    fun isStarted(): Boolean = _isStarted
    fun isStopped(): Boolean = _isStopped
    fun isReleased(): Boolean = _isReleased
} 
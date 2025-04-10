package com.personal.voicememo

import android.app.Application
import android.content.Context
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class VoiceMemoApplication : Application() {
    lateinit var baseDir: File
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Debug logging for Pinecone configuration
        Log.d("VoiceMemoApplication", "Pinecone BuildConfig values at startup:")
        Log.d("VoiceMemoApplication", "  PINECONE_INDEX = ${BuildConfig.PINECONE_INDEX}")
        Log.d("VoiceMemoApplication", "  PINECONE_ENVIRONMENT = ${BuildConfig.PINECONE_ENVIRONMENT}")
        Log.d("VoiceMemoApplication", "  PINECONE_HOST_URL = ${BuildConfig.PINECONE_HOST_URL}")
        
        instance = this
        setupStorageDirectory()
    }

    private fun setupStorageDirectory() {
        baseDir = File(getExternalFilesDir(null), "voice_memos").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        // Clean up temporary files
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("recording_") && file.extension == "m4a") {
                file.delete()
            }
        }
    }

    companion object {
        private lateinit var instance: VoiceMemoApplication

        fun getInstance(context: Context): VoiceMemoApplication {
            if (!::instance.isInitialized) {
                instance = context.applicationContext as VoiceMemoApplication
            }
            return instance
        }
    }
} 
package com.personal.voicememo.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.personal.voicememo.BuildConfig
import com.personal.voicememo.config.ApiKeys
import com.personal.voicememo.VoiceMemoApplication
import com.personal.voicememo.data.database.AppDatabase
import com.personal.voicememo.data.database.dao.VoiceMemoDao
import com.personal.voicememo.data.network.OpenAIService
import com.personal.voicememo.data.network.PineconeNetworkService
import com.personal.voicememo.data.network.WhisperService
import com.personal.voicememo.data.repository.VoiceMemoRepository
import com.personal.voicememo.data.service.OpenAIEmbeddingService
import com.personal.voicememo.data.service.PineconeStorageService
import com.personal.voicememo.data.service.WhisperTranscriptionService
import com.personal.voicememo.network.GoogleSheetsService
import com.personal.voicememo.network.GoogleSheetsServiceImpl
import com.personal.voicememo.network.OpenAiServiceImpl
import com.personal.voicememo.service.AudioRecordingService
import com.personal.voicememo.service.AudioRecordingServiceImpl
import com.personal.voicememo.service.OpenAIService as IOpenAIService
import com.personal.voicememo.service.PineconeService
import com.personal.voicememo.service.TodoService
import com.personal.voicememo.service.TodoServiceImpl
import com.personal.voicememo.service.WhisperService as IWhisperService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voice_memo_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVoiceMemoDao(database: AppDatabase): VoiceMemoDao {
        return database.voiceMemoDao()
    }

    @Provides
    @Singleton
    fun provideWhisperTranscriptionService(
        whisperService: WhisperService
    ): IWhisperService {
        return WhisperTranscriptionService(whisperService)
    }

    @Provides
    @Singleton
    fun provideOpenAIEmbeddingService(
        openAiService: OpenAIService
    ): IOpenAIService {
        return OpenAIEmbeddingService(openAiService)
    }

    @Provides
    @Singleton
    fun providePineconeStorageService(
        pineconeNetworkService: PineconeNetworkService
    ): PineconeService {
        return PineconeStorageService(pineconeNetworkService)
    }

    @Provides
    @Singleton
    fun provideVoiceMemoRepository(
        voiceMemoDao: VoiceMemoDao,
        whisperService: IWhisperService,
        openAIService: IOpenAIService,
        pineconeService: PineconeService
    ): VoiceMemoRepository {
        return VoiceMemoRepository(
            voiceMemoDao,
            whisperService,
            openAIService,
            pineconeService
        )
    }

    @Provides
    @Singleton
    fun provideAudioRecordingService(context: Context): AudioRecordingService {
        return AudioRecordingServiceImpl(context)
    }

    @Provides
    @Singleton
    fun provideGoogleSheetsServiceImpl(
        googleSheetsService: GoogleSheetsService
    ): GoogleSheetsServiceImpl {
        return GoogleSheetsServiceImpl(googleSheetsService)
    }

    @Provides
    @Singleton
    fun provideTodoService(
        openAiServiceImpl: OpenAiServiceImpl,
        googleSheetsServiceImpl: GoogleSheetsServiceImpl
    ): TodoService {
        return TodoServiceImpl(openAiServiceImpl, googleSheetsServiceImpl)
    }
} 
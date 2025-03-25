package com.personal.voicememo.config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class AppConfig(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var openAiApiKey: String?
        get() = sharedPreferences.getString(KEY_OPENAI_API_KEY, null)
        set(value) = sharedPreferences.edit().putString(KEY_OPENAI_API_KEY, value).apply()

    var pineconeApiKey: String?
        get() = sharedPreferences.getString(KEY_PINECONE_API_KEY, null)
        set(value) = sharedPreferences.edit().putString(KEY_PINECONE_API_KEY, value).apply()

    var pineconeEnvironment: String?
        get() = sharedPreferences.getString(KEY_PINECONE_ENVIRONMENT, null)
        set(value) = sharedPreferences.edit().putString(KEY_PINECONE_ENVIRONMENT, value).apply()

    var pineconeIndexName: String?
        get() = sharedPreferences.getString(KEY_PINECONE_INDEX_NAME, null)
        set(value) = sharedPreferences.edit().putString(KEY_PINECONE_INDEX_NAME, value).apply()

    companion object {
        private const val KEY_OPENAI_API_KEY = "openai_api_key"
        private const val KEY_PINECONE_API_KEY = "pinecone_api_key"
        private const val KEY_PINECONE_ENVIRONMENT = "pinecone_environment"
        private const val KEY_PINECONE_INDEX_NAME = "pinecone_index_name"
    }
} 
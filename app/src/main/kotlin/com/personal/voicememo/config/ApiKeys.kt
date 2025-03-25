package com.personal.voicememo.config

import com.personal.voicememo.BuildConfig

object ApiKeys {
    // Access API keys from BuildConfig fields set via local.properties and gradle.
    const val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY
    const val OPENAI_PROJECT_ID = BuildConfig.OPENAI_PROJECT_ID
    
    const val PINECONE_API_KEY = BuildConfig.PINECONE_API_KEY
    const val PINECONE_INDEX = BuildConfig.PINECONE_INDEX
    const val PINECONE_ENVIRONMENT = BuildConfig.PINECONE_ENVIRONMENT
    const val PINECONE_HOST_URL = BuildConfig.PINECONE_HOST_URL
} 
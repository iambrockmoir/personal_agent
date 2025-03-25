package com.personal.voicememo.di

import com.personal.voicememo.config.ApiKeys
import com.personal.voicememo.data.network.OpenAIService
import com.personal.voicememo.data.network.PineconeNetworkService
import com.personal.voicememo.data.network.WhisperService
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.personal.voicememo.BuildConfig
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import okhttp3.HttpUrl

@Module
object NetworkModule {
    private const val OPENAI_BASE_URL = "https://api.openai.com/v1/"
    
    init {
        Log.e("NetworkModule", "=== Module Initialization ===")
        try {
            val url = createPineconeBaseUrl()
            Log.e("NetworkModule", "Successfully created Pinecone URL during init: $url")
        } catch (e: Exception) {
            Log.e("NetworkModule", "Error creating Pinecone URL during init: ${e.message}")
            Log.e("NetworkModule", "Stack trace: ${e.stackTrace.joinToString("\n")}")
            throw e
        }
    }

    private fun createPineconeBaseUrl(): String {
        Log.e("NetworkModule", "=== Starting Pinecone URL Construction ===")
        
        // Log raw BuildConfig values first
        Log.e("NetworkModule", "Raw BuildConfig values:")
        Log.e("NetworkModule", "  BuildConfig.PINECONE_INDEX = '${BuildConfig.PINECONE_INDEX}'")
        Log.e("NetworkModule", "  BuildConfig.PINECONE_ENVIRONMENT = '${BuildConfig.PINECONE_ENVIRONMENT}'")
        Log.e("NetworkModule", "  BuildConfig.PINECONE_HOST_URL = '${BuildConfig.PINECONE_HOST_URL}'")
        
        // Get values from ApiKeys
        val pineconeHostUrl = ApiKeys.PINECONE_HOST_URL
        
        Log.e("NetworkModule", "Values from ApiKeys:")
        Log.e("NetworkModule", "  PINECONE_HOST_URL = '$pineconeHostUrl'")
        
        // Validate values
        if (pineconeHostUrl.isBlank()) {
            val error = "PINECONE_HOST_URL cannot be blank. Check your local.properties and Gradle configuration."
            Log.e("NetworkModule", error)
            throw IllegalStateException(error)
        }

        // Use the host URL directly
        val baseUrl = pineconeHostUrl
        Log.e("NetworkModule", "Using Pinecone host URL: $baseUrl")
        
        return baseUrl
    }

    private fun createAuthInterceptor(apiKey: String) = Interceptor { chain ->
        Log.d("NetworkModule", "Using OpenAI API key: " + apiKey.take(4) + "****")
        Log.d("NetworkModule", "API key length: ${apiKey.length}")
        Log.d("NetworkModule", "Is project key: ${apiKey.startsWith("sk-proj-")}")
        
        val requestBuilder = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $apiKey")
        
        // Add project ID header for project-scoped API keys
        if (apiKey.startsWith("sk-proj-")) {
            val openAiProjectId = ApiKeys.OPENAI_PROJECT_ID
            Log.d("NetworkModule", "Adding OpenAI project ID header: $openAiProjectId")
            requestBuilder.addHeader("OpenAI-Project-Id", openAiProjectId)
        }
        
        val request = requestBuilder.build()
        Log.d("NetworkModule", "Final request headers: ${request.headers}")
        
        chain.proceed(request)
    }

    private fun createPineconeAuthInterceptor(apiKey: String) = Interceptor { chain ->
        Log.d("NetworkModule", "Using Pinecone API key: " + apiKey.take(4) + "****")
        val request = chain.request().newBuilder()
            .addHeader("Api-Key", apiKey)
            .addHeader("Content-Type", "application/json")
            .addHeader("accept", "application/json")
            .build()
            
        // Log the final request details
        Log.e("NetworkModule", "=== Final Request Details ===")
        Log.e("NetworkModule", "Final URL: ${request.url}")
        Log.e("NetworkModule", "Headers: ${request.headers}")
        Log.e("NetworkModule", "Method: ${request.method}")
        
        try {
            val response = chain.proceed(request)
            if (!response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.e("NetworkModule", "=== Pinecone Error Response ===")
                Log.e("NetworkModule", "Response Code: ${response.code}")
                Log.e("NetworkModule", "Response Message: ${response.message}")
                Log.e("NetworkModule", "Response Body: $responseBody")
                Log.e("NetworkModule", "Response Headers: ${response.headers}")
                response.close()
                throw Exception("Pinecone request failed with code ${response.code}: $responseBody")
            }
            response
        } catch (e: Exception) {
            Log.e("NetworkModule", "Error during Pinecone request: ${e.message}")
            Log.e("NetworkModule", "Stack trace: ${e.stackTrace.joinToString("\n")}")
            
            // Add DNS resolution test
            try {
                val host = request.url.host
                Log.e("NetworkModule", "Attempting DNS resolution for host: $host")
                val addresses = java.net.InetAddress.getAllByName(host)
                Log.e("NetworkModule", "DNS resolution successful. Addresses: ${addresses.joinToString()}")
            } catch (dnsError: Exception) {
                Log.e("NetworkModule", "DNS resolution failed: ${dnsError.message}")
                Log.e("NetworkModule", "DNS error stack trace: ${dnsError.stackTrace.joinToString("\n")}")
            }
            
            throw e
        }
    }

    private fun createSSLContext(): Pair<SSLContext, X509TrustManager> {
        // Get the default trust manager
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(null as KeyStore?)
        val defaultTrustManager = tmf.trustManagers[0] as X509TrustManager

        // Create a trust manager that logs certificate info
        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                defaultTrustManager.checkClientTrusted(chain, authType)
            }
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                try {
                    defaultTrustManager.checkServerTrusted(chain, authType)
                    Log.d("NetworkModule", "Certificate validation successful")
                    Log.d("NetworkModule", "Server certificate info:")
                    chain.forEachIndexed { index, cert ->
                        Log.d("NetworkModule", "Certificate [$index]:")
                        Log.d("NetworkModule", "  Subject: ${cert.subjectDN}")
                        Log.d("NetworkModule", "  Issuer: ${cert.issuerDN}")
                        Log.d("NetworkModule", "  Valid from: ${cert.notBefore}")
                        Log.d("NetworkModule", "  Valid until: ${cert.notAfter}")
                        Log.d("NetworkModule", "  Signature algorithm: ${cert.sigAlgName}")
                    }
                } catch (e: Exception) {
                    Log.e("NetworkModule", "Certificate validation failed: ${e.message}")
                    throw e
                }
            }
            override fun getAcceptedIssuers(): Array<X509Certificate> = defaultTrustManager.acceptedIssuers
        }

        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, arrayOf(trustManager), null)
        return Pair(sslContext, trustManager)
    }

    @Provides
    @Singleton
    @Named("openai")
    fun provideOpenAIRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor(ApiKeys.OPENAI_API_KEY))
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(OPENAI_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("pinecone")
    fun providePineconeRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val (sslContext, trustManager) = createSSLContext()

        // Get the base URL
        val baseUrl = createPineconeBaseUrl()
        Log.e("NetworkModule", "Using Pinecone base URL for Retrofit: $baseUrl")

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(createPineconeAuthInterceptor(ApiKeys.PINECONE_API_KEY))
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIService(@Named("openai") retrofit: Retrofit): OpenAIService =
        retrofit.create(OpenAIService::class.java)

    @Provides
    @Singleton
    fun provideWhisperService(@Named("openai") retrofit: Retrofit): WhisperService =
        retrofit.create(WhisperService::class.java)

    @Provides
    @Singleton
    fun providePineconeNetworkService(
        @Named("pinecone") retrofit: Retrofit
    ): PineconeNetworkService = retrofit.create(PineconeNetworkService::class.java)
} 
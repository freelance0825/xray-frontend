package com.example.thunderscope_frontend.data.remote

import android.app.Application
import android.content.Context
import com.example.thunderscope_frontend.BuildConfig
import com.example.thunderscope_frontend.data.local.datastore.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    fun getApiService(authDataStore: AuthDataStore): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val authToken = runBlocking { authDataStore.getToken().first().toString() }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)

        if (authToken != AuthDataStore.preferencesDefaultValue) {
            clientBuilder.addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $authToken")
                    .build()
                chain.proceed(request)
            }
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.APP_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .client(clientBuilder.build())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
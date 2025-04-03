package com.example.thunderscope_frontend.data.remote

import android.app.Application
import android.content.Context
import com.example.thunderscope_frontend.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    fun getApiService(context: Context): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val authToken = sharedPreferences.getString("token", null)

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)

        if (!authToken.isNullOrEmpty()) {
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
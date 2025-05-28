package com.example.xray_frontend.ui.utils.helpers

import android.content.Context
import android.content.res.Configuration
import com.example.xray_frontend.data.local.datastore.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

object LocaleHelper {
    fun getLocale(context: Context): Locale {
        val languageCode = runBlocking { AuthDataStore.getInstance(context).getLanguage().first() }
        return Locale(languageCode)
    }

    fun applyLocale(context: Context,): Context {
        val locale = getLocale(context)

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}

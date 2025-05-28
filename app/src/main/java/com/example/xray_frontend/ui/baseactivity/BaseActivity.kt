package com.example.xray_frontend.ui.baseactivity

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.xray_frontend.ui.utils.helpers.LocaleHelper


open class BaseActivity : AppCompatActivity() {

    private var currentLanguageCode: String = ""

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.applyLocale(newBase)
        currentLanguageCode = LocaleHelper.getLocale(context).language
        super.attachBaseContext(context)
    }

    override fun onResume() {
        super.onResume()
        val latestLanguageCode = LocaleHelper.getLocale(this).language
        if (latestLanguageCode != currentLanguageCode) {
            Log.d("LocaleDebug", "Locale changed from $currentLanguageCode to $latestLanguageCode, recreating...")
            currentLanguageCode = latestLanguageCode
            recreate()
        }
    }
}

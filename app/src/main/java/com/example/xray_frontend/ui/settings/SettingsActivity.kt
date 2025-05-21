package com.example.xray_frontend.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import com.example.xray_frontend.R
import com.example.xray_frontend.data.repo.ThunderscopeRepository
import com.example.xray_frontend.databinding.ActivitySettingsBinding
import com.example.xray_frontend.ui.baseactivity.BaseActivity
import com.example.xray_frontend.ui.login.LoginActivity
import com.example.xray_frontend.ui.utils.enums.LanguageOptions

class SettingsActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val settingsViewModel by viewModels<SettingsViewModel> {
        SettingsViewModel.Factory(ThunderscopeRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViews()
        setListeners()
    }

    private fun setViews() {
        binding.apply {
            settingsViewModel.langCode.observe(this@SettingsActivity) { lang ->
                when (lang) {
                    LanguageOptions.English.langValue -> rbEnglish.isChecked = true
                    LanguageOptions.Indonesia.langValue -> rbIndonesia.isChecked = true
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            btnBack.setOnClickListener { finish() }

            rgLangOptions.setOnCheckedChangeListener { _, checkedId ->
                val selectedLang = when (checkedId) {
                    R.id.rb_english -> LanguageOptions.English.langValue
                    R.id.rb_indonesia -> LanguageOptions.Indonesia.langValue
                    else -> return@setOnCheckedChangeListener
                }

                val currentLang = settingsViewModel.langCode.value
                if (selectedLang != currentLang) {
                    settingsViewModel.saveLanguage(selectedLang, this@SettingsActivity)
                    recreate()
                }
            }

            btnLogout.setOnClickListener {
                settingsViewModel.logout()
                finishAffinity()
                startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
            }
        }
    }
}

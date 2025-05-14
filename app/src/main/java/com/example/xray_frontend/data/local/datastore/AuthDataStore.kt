package com.example.xray_frontend.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

class AuthDataStore private constructor(private val dataStore: DataStore<Preferences>) {
    fun getToken() = dataStore.data.map { it[TOKEN_PREFERENCES] ?: preferencesDefaultValue }
    fun getDoctorId() = dataStore.data.map { it[DOCTOR_ID_PREFERENCES] ?: 0 }

    suspend fun saveToken(
        token: String
    ) {
        dataStore.edit { prefs ->
            prefs[TOKEN_PREFERENCES] = token
        }
    }

    suspend fun saveDoctorId(
        doctorId: Int
    ) {
        dataStore.edit { prefs ->
            prefs[DOCTOR_ID_PREFERENCES] = doctorId
        }
    }

    suspend fun clearPreferences() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "account_preferences")
        private val TOKEN_PREFERENCES = stringPreferencesKey("token_preferences")
        private val DOCTOR_ID_PREFERENCES = intPreferencesKey("doctor_id_preferences")

        const val preferencesDefaultValue: String = "preferences_default_value"

        @Volatile
        private var INSTANCE: AuthDataStore? = null

        fun getInstance(context: Context) = INSTANCE ?: synchronized(this) {
            val instance = AuthDataStore(context.dataStore)
            INSTANCE = instance
            instance
        }
    }
}
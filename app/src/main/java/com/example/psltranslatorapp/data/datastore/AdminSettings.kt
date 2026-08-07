package com.example.psltranslatorapp.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "admin_settings")
class AdminSettings(private val context: Context) {

    private object Keys {
        val THRESHOLD = floatPreferencesKey("threshold")
    }

    val confidenceThreshold = context.dataStore.data.map { prefs ->
        prefs[Keys.THRESHOLD] ?: 0.85f
    }

    suspend fun updateThreshold(value: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THRESHOLD] = value
        }
    }
}
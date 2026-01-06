package com.example.mazika.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class AccentColor { PURPLE, GREEN, BLUE, ORANGE }

object AppSettings {

    private val KEY_THEME = stringPreferencesKey("theme_mode")
    private val KEY_ACCENT = stringPreferencesKey("accent_color")

    fun themeFlow(context: Context): Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_THEME]
                ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM
        }

    fun accentFlow(context: Context): Flow<AccentColor> =
        context.dataStore.data.map { prefs ->
            prefs[KEY_ACCENT]
                ?.let { runCatching { AccentColor.valueOf(it) }.getOrNull() }
                ?: AccentColor.PURPLE
        }

    suspend fun setTheme(context: Context, mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setAccent(context: Context, accent: AccentColor) {
        context.dataStore.edit { it[KEY_ACCENT] = accent.name }
    }

    suspend fun getTheme(context: Context): ThemeMode = themeFlow(context).first()
    suspend fun getAccent(context: Context): AccentColor = accentFlow(context).first()
}

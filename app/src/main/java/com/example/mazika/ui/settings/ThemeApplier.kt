package com.example.mazika.settings

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.mazika.R
import kotlinx.coroutines.runBlocking

object ThemeApplier {

    fun apply(activity: Activity) {
        val ctx = activity.applicationContext
        val themeMode = runBlocking { AppSettings.getTheme(ctx) }
        val accent = runBlocking { AppSettings.getAccent(ctx) }

        applyNightMode(themeMode)

        // ✅ Base theme then FORCE overlay accent
        activity.setTheme(R.style.Theme_Mazika)
        activity.theme.applyStyle(accentThemeRes(accent), true)
    }

    fun applyNightModeFromPrefs(context: Context) {
        val mode = runBlocking { AppSettings.getTheme(context.applicationContext) }
        applyNightMode(mode)
    }

    fun applyNightMode(mode: ThemeMode) {
        val desired = when (mode) {
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }

        // ✅ prevents “toggling a lot”
        if (AppCompatDelegate.getDefaultNightMode() != desired) {
            AppCompatDelegate.setDefaultNightMode(desired)
        }
    }

    private fun accentThemeRes(accent: AccentColor): Int = when (accent) {
        AccentColor.PURPLE -> R.style.Theme_Mazika_Accent_Purple
        AccentColor.GREEN -> R.style.Theme_Mazika_Accent_Green
        AccentColor.BLUE -> R.style.Theme_Mazika_Accent_Blue
        AccentColor.ORANGE -> R.style.Theme_Mazika_Accent_Orange
    }
}

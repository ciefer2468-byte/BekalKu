package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

/** Pilihan tema: 0 = ikuti sistem, 1 = terang, 2 = gelap. Tersimpan walau pengguna logout. */
object ThemeManager {
    const val SYSTEM = 0
    const val LIGHT = 1
    const val DARK = 2

    private const val PREFS = "bekalku_theme"
    private const val KEY_MODE = "mode"

    fun get(context: Context): Int =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_MODE, SYSTEM)

    fun set(context: Context, mode: Int) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_MODE, mode).apply()
        apply(mode)
    }

    fun applySaved(context: Context) = apply(get(context))

    private fun apply(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}

fun Context.isNightMode(): Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

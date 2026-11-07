package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import android.content.Context
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.model.User

/** Menyimpan sesi login (pengguna tidak perlu login ulang) dan pengaturan pengingat. */
class SessionManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val isLoggedIn: Boolean get() = userId > 0

    val userId: Long get() = prefs.getLong(KEY_USER_ID, -1L)
    val userName: String get() = prefs.getString(KEY_USER_NAME, "") ?: ""
    val userEmail: String get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    var reminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    var reminderHour: Int
        get() = prefs.getInt(KEY_REMINDER_HOUR, 6)
        set(value) = prefs.edit().putInt(KEY_REMINDER_HOUR, value).apply()

    var reminderMinute: Int
        get() = prefs.getInt(KEY_REMINDER_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_REMINDER_MINUTE, value).apply()

    fun save(user: User) {
        prefs.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .apply()
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val PREFS_NAME = "bekalku_session"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_REMINDER_ENABLED = "reminder_enabled"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"
    }
}

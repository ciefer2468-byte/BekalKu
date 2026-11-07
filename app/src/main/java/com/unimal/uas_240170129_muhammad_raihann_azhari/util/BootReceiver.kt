package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Menjadwalkan ulang pengingat setelah perangkat dinyalakan kembali. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val session = SessionManager(context)
        if (session.isLoggedIn && session.reminderEnabled) {
            ReminderScheduler.schedule(context, session.reminderHour, session.reminderMinute)
        }
    }
}

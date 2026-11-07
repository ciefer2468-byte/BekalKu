package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.unimal.uas_240170129_muhammad_raihann_azhari.R
import com.unimal.uas_240170129_muhammad_raihann_azhari.data.repository.MenuRepository
import com.unimal.uas_240170129_muhammad_raihann_azhari.ui.main.MainActivity

/** Menampilkan notifikasi pengingat lalu menjadwalkan ulang untuk esok hari. */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val session = SessionManager(context)
        if (!session.isLoggedIn || !session.reminderEnabled) return

        val pending = goAsync()
        AppExecutors.io.execute {
            try {
                showNotification(context, session)
                ReminderScheduler.schedule(context, session.reminderHour, session.reminderMinute)
            } finally {
                pending.finish()
            }
        }
    }

    private fun showNotification(context: Context, session: SessionManager) {
        // Aplikasi tetap berjalan normal bila izin notifikasi ditolak.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        ReminderScheduler.createChannel(context)

        val todayMenus = try {
            MenuRepository(context).getByDay(session.userId, todayIndex()).filter { !it.packed }
        } catch (e: Exception) {
            emptyList()
        }
        val text = if (todayMenus.isEmpty()) {
            context.getString(R.string.notif_text_empty)
        } else {
            context.getString(R.string.notif_text_menu, todayMenus.joinToString(", ") { it.name })
        }

        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_title))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val NOTIFICATION_ID = 2001
    }
}

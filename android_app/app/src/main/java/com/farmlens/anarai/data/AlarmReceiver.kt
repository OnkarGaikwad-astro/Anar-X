package com.farmlens.anarai.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.farmlens.anarai.MainActivity
import com.farmlens.anarai.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val chemical = intent.getStringExtra("chemicalName") ?: "Treatment"
        val notes = intent.getStringExtra("notes") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val timeText = intent.getStringExtra("timeText") ?: "Now"

        val fullScreenIntent = Intent(context, com.farmlens.anarai.AlarmScreenActivity::class.java).apply {
            putExtra("chemicalName", chemical)
            putExtra("notes", notes)
            putExtra("timeText", timeText)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "treatment_reminders")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Anar X Reminder: $chemical")
            .setContentText("It is time for your scheduled treatment.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

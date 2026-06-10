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

        // Intent to open the app when the notification is tapped
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "treatment_reminders")
            .setSmallIcon(R.mipmap.ic_launcher_round) // Using the app icon
            .setContentTitle("Anar X Reminder: $chemical")
            .setContentText(if (notes.isNotEmpty()) "Notes: $notes" else "It is time for your scheduled treatment.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Use a unique ID for each notification based on current time
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

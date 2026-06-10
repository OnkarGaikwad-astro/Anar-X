package com.farmlens.anarai.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.farmlens.anarai.data.AlarmReceiver

fun scheduleAlarm(context: Context, id: Int, chemical: String, notes: String, timeInMillis: Long) {
    if (timeInMillis <= System.currentTimeMillis()) return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("chemicalName", chemical)
        putExtra("notes", notes)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    } catch (e: SecurityException) {
        // Fallback if permission not granted
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

fun cancelAlarm(context: Context, id: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}

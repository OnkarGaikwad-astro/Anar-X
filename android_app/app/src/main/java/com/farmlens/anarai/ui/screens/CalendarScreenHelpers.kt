package com.farmlens.anarai.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.farmlens.anarai.data.AlarmReceiver

fun scheduleAlarm(context: Context, id: Int, chemical: String, notes: String, timeInMillis: Long, reminderOffsetMillis: Long) {
    val timeText = when (reminderOffsetMillis) {
        30 * 60 * 1000L -> "in 30 minutes"
        60 * 60 * 1000L -> "in 1 hour"
        else -> "Now"
    }
    
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val triggerTime = timeInMillis - reminderOffsetMillis
    
    if (triggerTime <= System.currentTimeMillis()) return
    
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("chemicalName", chemical)
        putExtra("notes", notes)
        putExtra("timeText", timeText)
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
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    } catch (e: SecurityException) {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
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

package com.eduplanpro.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.eduplanpro.models.Event

class ReminderScheduler(private val context: Context) {

    fun scheduleReminder(event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Calculate reminder time based on user selection
        val reminderTime = when (event.reminderType) {
            "2_days" -> event.date - (2 * 24 * 60 * 60 * 1000)  // 2 days before
            "1_day" -> event.date - (24 * 60 * 60 * 1000)       // 1 day before
            "1_hour" -> event.date - (60 * 60 * 1000)           // 1 hour before
            else -> event.date
        }

        // Create intent for BroadcastReceiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("event_title", event.title)
            putExtra("event_description", event.description)
        }

        // Create PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        if (reminderTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // For Android 12+
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6-11
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            } else {
                // For Android 5 and below
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        }
    }

    fun cancelReminder(eventId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
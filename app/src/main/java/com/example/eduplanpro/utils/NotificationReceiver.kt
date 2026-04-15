package com.eduplanpro.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Get event details from intent
        val title = intent.getStringExtra("event_title") ?: "Event Reminder"
        val message = intent.getStringExtra("event_description") ?: "You have an upcoming event"

        // Create notification helper and show notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.createNotificationChannel()
        notificationHelper.showReminder(title, message)
    }
}
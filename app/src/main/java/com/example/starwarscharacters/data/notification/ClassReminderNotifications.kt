package com.example.starwarscharacters.data.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.starwarscharacters.MainActivity
import com.example.starwarscharacters.feature.profile.R

object ClassReminderNotifications {

    private const val CHANNEL_ID = "favorite_class_reminders"
    private const val NOTIFICATION_ID = 7001
    private const val OPEN_APP_REQUEST_CODE = 7002

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(
            NotificationManager::class.java
        )

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания о любимой паре",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Уведомления о начале пары в выбранное время"
        }

        manager.createNotificationChannel(channel)
    }

    fun canShowNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                return false
            }
        }

        if (
            !NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
        ) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(
                NotificationManager::class.java
            )

            val channel = manager.getNotificationChannel(CHANNEL_ID)

            if (channel?.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }

        return true
    }

    @SuppressLint("MissingPermission")
    fun show(context: Context, profileName: String) {
        createChannel(context)

        // Разрешение проверяется перед отправкой уведомления.
        if (!canShowNotifications(context)) {
            return
        }

        val openAppIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            OPEN_APP_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val message = "Пора на пару, $profileName!"

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_clock)
            .setContentTitle("Сработало напоминание")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Разрешение могло быть отозвано после проверки.
        }
    }
}

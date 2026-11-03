package com.example.starwarscharacters.data.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.starwarscharacters.domain.reminder.ClassReminderScheduler

class AlarmManagerClassReminderScheduler(
    context: Context
) : ClassReminderScheduler {

    private val applicationContext = context.applicationContext

    private val alarmManager = applicationContext.getSystemService(
        AlarmManager::class.java
    )

    override fun canScheduleExactReminders(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun canShowNotifications(): Boolean {
        ClassReminderNotifications.createChannel(applicationContext)

        return ClassReminderNotifications.canShowNotifications(
            applicationContext
        )
    }

    override fun schedule(
        profileName: String,
        triggerAtMillis: Long
    ) {
        val normalizedName = profileName.trim()

        require(normalizedName.isNotBlank()) {
            "Введите ФИО для напоминания"
        }

        require(triggerAtMillis > System.currentTimeMillis()) {
            "Выбранное время уже прошло. Укажите более позднее время сегодня."
        }

        check(canShowNotifications()) {
            "Разрешите уведомления для приложения"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            check(alarmManager.canScheduleExactAlarms()) {
                "Разрешите приложению устанавливать точные напоминания"
            }
        }

        val intent = Intent(
            applicationContext,
            ClassReminderReceiver::class.java
        ).apply {
            action = ClassReminderReceiver.ACTION_SHOW_REMINDER

            putExtra(
                ClassReminderReceiver.EXTRA_PROFILE_NAME,
                normalizedName
            )
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            REMINDER_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (error: SecurityException) {
            throw IllegalStateException(
                "Не удалось установить напоминание. " +
                        "Проверьте разрешение на точные напоминания.",
                error
            )
        }
    }

    private companion object {
        const val REMINDER_REQUEST_CODE = 7003
    }
}
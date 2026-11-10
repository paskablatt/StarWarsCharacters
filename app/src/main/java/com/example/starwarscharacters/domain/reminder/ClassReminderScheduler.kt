package com.example.starwarscharacters.domain.reminder

interface ClassReminderScheduler {

    fun canScheduleExactReminders(): Boolean

    fun canShowNotifications(): Boolean

    fun schedule(
        profileName: String,
        triggerAtMillis: Long
    )
}
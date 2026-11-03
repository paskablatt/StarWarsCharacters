package com.example.starwarscharacters.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ClassReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        if (intent.action != ACTION_SHOW_REMINDER) {
            return
        }

        val profileName = intent
            .getStringExtra(EXTRA_PROFILE_NAME)
            ?.takeIf { it.isNotBlank() }
            ?: return

        ClassReminderNotifications.show(
            context = context,
            profileName = profileName
        )
    }

    companion object {
        const val ACTION_SHOW_REMINDER =
            "com.example.starwarscharacters.action.SHOW_CLASS_REMINDER"

        const val EXTRA_PROFILE_NAME = "profile_name"
    }
}
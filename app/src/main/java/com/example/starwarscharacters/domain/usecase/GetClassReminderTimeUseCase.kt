package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.validation.FavoriteClassTimeValidator
import java.util.Calendar

class GetClassReminderTimeUseCase {

    operator fun invoke(time: String): Long {
        require(FavoriteClassTimeValidator.isValid(time)) {
            "Введите время в формате HH:mm: от 00:00 до 23:59"
        }

        val hour = time.substring(0, 2).toInt()
        val minute = time.substring(3, 5).toInt()

        val now = System.currentTimeMillis()

        val reminderCalendar = Calendar.getInstance().apply {
            timeInMillis = now
            isLenient = false

            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val triggerAtMillis = try {
            reminderCalendar.timeInMillis
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Это время недоступно сегодня в часовом поясе устройства"
            )
        }

        require(triggerAtMillis > now) {
            "Выбранное время уже прошло. " +
                    "Укажите более позднее время сегодня."
        }

        return triggerAtMillis
    }
}
package com.example.starwarscharacters.presentation.profile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.starwarscharacters.domain.reminder.ClassReminderScheduler

@Composable
fun ReminderSaveButton(
    scheduler: ClassReminderScheduler,
    enabled: Boolean,
    onSave: () -> Unit
) {
    val context = LocalContext.current

    val currentOnSave by rememberUpdatedState(onSave)
    val currentEnabled by rememberUpdatedState(enabled)

    var notificationPermissionRequested by rememberSaveable {
        mutableStateOf(false)
    }

    var showNotificationSettingsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showExactAlarmDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        when {
            !scheduler.canScheduleExactReminders() -> {
                showReminderMessage(
                    context,
                    "Доступ к точным напоминаниям не предоставлен"
                )
            }

            !scheduler.canShowNotifications() -> {
                showNotificationSettingsDialog = true
            }

            currentEnabled -> {
                currentOnSave()
            }
        }
    }

    val continueSaving: () -> Unit = {
        when {
            !currentEnabled -> Unit

            !scheduler.canShowNotifications() -> {
                showNotificationSettingsDialog = true
            }

            !scheduler.canScheduleExactReminders() -> {
                showExactAlarmDialog = true
            }

            else -> {
                currentOnSave()
            }
        }
    }

    val notificationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (scheduler.canShowNotifications()) {
            continueSaving()
        } else {
            showReminderMessage(
                context,
                "Уведомления остаются отключены"
            )
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            continueSaving()
        } else {
            showReminderMessage(
                context,
                "Для напоминания нужно разрешить уведомления. " +
                        "Нажмите «Готово» повторно, чтобы открыть настройки."
            )
        }
    }

    TextButton(
        enabled = enabled,
        onClick = {
            val needsNotificationPermission =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED

            if (
                needsNotificationPermission &&
                !notificationPermissionRequested
            ) {
                notificationPermissionRequested = true

                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                continueSaving()
            }
        }
    ) {
        Text("Готово")
    }

    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = {
                showNotificationSettingsDialog = false
            },
            title = {
                Text("Разрешите уведомления")
            },
            text = {
                Text(
                    "Чтобы показать напоминание о паре, включите " +
                            "уведомления приложения и канал " +
                            "«Напоминания о любимой паре» в настройках."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNotificationSettingsDialog = false

                        val intent = if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ) {
                            Intent(
                                Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            ).apply {
                                putExtra(
                                    Settings.EXTRA_APP_PACKAGE,
                                    context.packageName
                                )
                            }
                        } else {
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        }

                        try {
                            notificationSettingsLauncher.launch(intent)
                        } catch (_: ActivityNotFoundException) {
                            showReminderMessage(
                                context,
                                "Откройте настройки уведомлений " +
                                        "приложения вручную"
                            )
                        } catch (_: SecurityException) {
                            showReminderMessage(
                                context,
                                "Не удалось открыть настройки уведомлений"
                            )
                        }
                    }
                ) {
                    Text("Настройки")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNotificationSettingsDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = {
                showExactAlarmDialog = false
            },
            title = {
                Text("Разрешите точные напоминания")
            },
            text = {
                Text(
                    "Чтобы напоминание сработало в выбранное время, " +
                            "разрешите приложению устанавливать " +
                            "будильники и напоминания."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExactAlarmDialog = false

                        if (
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ) {
                            val intent = Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:${context.packageName}")
                            )

                            try {
                                exactAlarmSettingsLauncher.launch(intent)
                            } catch (_: ActivityNotFoundException) {
                                showReminderMessage(
                                    context,
                                    "Откройте доступ к будильникам " +
                                            "и напоминаниям вручную " +
                                            "в настройках приложения"
                                )
                            } catch (_: SecurityException) {
                                showReminderMessage(
                                    context,
                                    "Не удалось открыть настройки " +
                                            "точных напоминаний"
                                )
                            }
                        }
                    }
                ) {
                    Text("Настройки")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExactAlarmDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

private fun showReminderMessage(
    context: Context,
    message: String
) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    ).show()
}

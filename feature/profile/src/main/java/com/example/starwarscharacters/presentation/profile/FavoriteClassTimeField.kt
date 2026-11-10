package com.example.starwarscharacters.presentation.profile

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.starwarscharacters.domain.validation.FavoriteClassTimeValidator
import com.example.starwarscharacters.feature.profile.R
import java.util.Calendar
import java.util.Locale

@Composable
fun FavoriteClassTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentOnValueChange by rememberUpdatedState(onValueChange)

    var showTimePicker by rememberSaveable {
        mutableStateOf(false)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text("Время любимой пары")
        },
        placeholder = {
            Text("HH:mm")
        },
        supportingText = {
            Text(
                text = error
                    ?: "Введите время или выберите его на часах"
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    showTimePicker = true
                },
                enabled = enabled
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clock),
                    contentDescription = "Выбрать время"
                )
            }
        },
        isError = error != null,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
        modifier = modifier.fillMaxWidth()
    )

    if (showTimePicker && enabled) {
        DisposableEffect(context) {
            val now = Calendar.getInstance()

            val hasValidTime =
                FavoriteClassTimeValidator.isValid(value)

            val initialHour = if (hasValidTime) {
                value.substring(0, 2).toInt()
            } else {
                now.get(Calendar.HOUR_OF_DAY)
            }

            val initialMinute = if (hasValidTime) {
                value.substring(3, 5).toInt()
            } else {
                now.get(Calendar.MINUTE)
            }

            val dialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedTime = String.format(
                        Locale.ROOT,
                        "%02d:%02d",
                        hourOfDay,
                        minute
                    )

                    currentOnValueChange(selectedTime)
                    showTimePicker = false
                },
                initialHour,
                initialMinute,
                true
            )

            dialog.setOnDismissListener {
                showTimePicker = false
            }

            dialog.show()

            onDispose {
                dialog.setOnDismissListener(null)
                dialog.dismiss()
            }
        }
    }
}

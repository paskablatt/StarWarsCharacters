package com.example.starwarscharacters.presentation.profile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.di.AppContainer
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit
) {
    val editViewModel: EditProfileViewModel = viewModel(
        factory = EditProfileViewModel.factory(
            observeProfile = AppContainer.observeProfileUseCase,
            saveProfile = AppContainer.saveProfileUseCase,
            photoRepository = AppContainer.profilePhotoRepository
        )
    )

    val state by editViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnBackClick by rememberUpdatedState(onBackClick)

    var hasMediaAccess by remember {
        mutableStateOf(hasPhotoReadPermission(context))
    }

    var mediaPermissionRequested by rememberSaveable {
        mutableStateOf(false)
    }

    var pendingCameraUri by rememberSaveable {
        mutableStateOf("")
    }

    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasMediaAccess = hasPhotoReadPermission(context)

        if (!hasMediaAccess) {
            Toast.makeText(
                context,
                "Доступ к фотографиям не предоставлен",
                Toast.LENGTH_SHORT
            ).show()

            currentOnBackClick()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            editViewModel.onPhotoSelected(uri.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        pendingCameraUri = ""

        editViewModel.onCameraResult(uri, success)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            editViewModel.onCameraClick()
        } else {
            editViewModel.onPhotoError(
                "Для съёмки фотографии разрешите доступ к камере"
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!hasMediaAccess && !mediaPermissionRequested) {
            mediaPermissionRequested = true
            mediaPermissionLauncher.launch(photoReadPermissions())
        }
    }

    LaunchedEffect(editViewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(
            Lifecycle.State.STARTED
        ) {
            launch {
                editViewModel.savedEvents.collect {
                    currentOnBackClick()
                }
            }

            launch {
                editViewModel.cameraEvents.collect { uri ->
                    pendingCameraUri = uri

                    try {
                        cameraLauncher.launch(Uri.parse(uri))
                    } catch (_: ActivityNotFoundException) {
                        pendingCameraUri = ""
                        editViewModel.onCameraLaunchFailed(uri)
                    } catch (_: SecurityException) {
                        pendingCameraUri = ""
                        editViewModel.onCameraLaunchFailed(uri)
                    }
                }
            }
        }
    }

    if (!hasMediaAccess) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    EditProfileContent(
        state = state,
        onBackClick = onBackClick,
        onFullNameChange = editViewModel::onFullNameChange,
        onPositionChange = editViewModel::onPositionChange,
        onResumeUrlChange = editViewModel::onResumeUrlChange,
        onRemovePhoto = editViewModel::onRemovePhoto,
        onSaveClick = editViewModel::save,
        onRetryClick = editViewModel::loadProfile,
        onGalleryClick = {
            try {
                galleryLauncher.launch("image/*")
            } catch (_: ActivityNotFoundException) {
                editViewModel.onPhotoError(
                    "На устройстве нет приложения для выбора фотографий"
                )
            } catch (_: SecurityException) {
                editViewModel.onPhotoError(
                    "Не удалось получить доступ к фотографиям"
                )
            }
        },
        onCameraClick = {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (granted) {
                editViewModel.onCameraClick()
            } else {
                cameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(
    state: EditProfileUiState,
    onBackClick: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onPositionChange: (String) -> Unit,
    onResumeUrlChange: (String) -> Unit,
    onRemovePhoto: () -> Unit,
    onSaveClick: () -> Unit,
    onRetryClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    var showPhotoChoice by rememberSaveable {
        mutableStateOf(false)
    }

    val busy = state.isSaving || state.isProcessingPhoto
    val canEdit = state.isLoaded && !state.isLoading && !busy

    BackHandler(enabled = busy) {}

    if (showPhotoChoice) {
        AlertDialog(
            onDismissRequest = {
                showPhotoChoice = false
            },
            title = {
                Text("Фотография профиля")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Выберите действие")

                    TextButton(
                        onClick = {
                            showPhotoChoice = false
                            onGalleryClick()
                        },
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Галерея")
                    }

                    TextButton(
                        onClick = {
                            showPhotoChoice = false
                            onCameraClick()
                        },
                        enabled = canEdit,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Камера")
                    }

                    if (state.photoUri.isNotBlank()) {
                        TextButton(
                            onClick = {
                                showPhotoChoice = false
                                onRemovePhoto()
                            },
                            enabled = canEdit,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Удалить фото",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPhotoChoice = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Редактирование")
                },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick,
                        enabled = !busy
                    ) {
                        Text("Назад")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSaveClick,
                        enabled = canEdit
                    ) {
                        Text("Готово")
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !state.isLoaded -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        16.dp,
                        Alignment.CenterVertically
                    )
                ) {
                    Text(
                        text = state.error
                            ?: "Не удалось загрузить профиль",
                        textAlign = TextAlign.Center
                    )

                    Button(onClick = onRetryClick) {
                        Text("Повторить")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileAvatar(
                        photoUri = state.photoUri,
                        modifier = Modifier.clickable(
                            enabled = canEdit,
                            onClickLabel = "Изменить фотографию профиля"
                        ) {
                            showPhotoChoice = true
                        }
                    )

                    Text(
                        text = "Нажмите на фото, чтобы изменить " +
                                "или удалить фотографию",
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (state.isProcessingPhoto) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Подготовка фотографии…")
                    }

                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = onFullNameChange,
                        label = { Text("ФИО") },
                        placeholder = { Text("Введите ваше ФИО") },
                        singleLine = true,
                        enabled = canEdit,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.position,
                        onValueChange = onPositionChange,
                        label = { Text("Должность") },
                        placeholder = {
                            Text("Например: Android-разработчик")
                        },
                        singleLine = true,
                        enabled = canEdit,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.resumeUrl,
                        onValueChange = onResumeUrlChange,
                        label = { Text("Ссылка на резюме") },
                        placeholder = {
                            Text("https://example.com/resume.pdf")
                        },
                        supportingText = {
                            Text(
                                "Прямая ссылка на файл. " +
                                        "Для проверки используйте HTTPS."
                            )
                        },
                        singleLine = true,
                        enabled = canEdit,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.error?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (state.isSaving) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Сохранение профиля…")
                    }

                    Text(
                        text = "Изменения сохраняются после " +
                                "нажатия «Готово».",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun photoReadPermissions(): Array<String> {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        }

        else -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

private fun hasPhotoReadPermission(context: Context): Boolean {
    fun granted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
            granted(Manifest.permission.READ_MEDIA_IMAGES) ||
                    granted(
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            granted(Manifest.permission.READ_MEDIA_IMAGES)
        }

        else -> {
            granted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
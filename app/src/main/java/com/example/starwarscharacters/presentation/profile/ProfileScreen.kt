package com.example.starwarscharacters.presentation.profile

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.starwarscharacters.di.AppContainer

@Composable
fun ProfileScreen(
    onEditClick: () -> Unit
) {
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            observeProfile = AppContainer.observeProfileUseCase,
            downloadResume = AppContainer.downloadResumeUseCase
        )
    )

    val state by
    profileViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(profileViewModel, lifecycleOwner, context) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(
            Lifecycle.State.STARTED
        ) {
            profileViewModel.events.collect { event ->
                when (event) {
                    is ProfileEvent.OpenResume -> {
                        val uri = Uri.parse(event.document.uri)

                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(
                                uri,
                                event.document.mimeType
                            )

                            addFlags(
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )

                            clipData = ClipData.newRawUri(
                                "Резюме",
                                uri
                            )
                        }

                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                            profileViewModel.onDocumentOpenError()
                        } catch (_: SecurityException) {
                            profileViewModel.onDocumentOpenError()
                        }
                    }
                }
            }
        }
    }

    ProfileContent(
        state = state,
        onEditClick = onEditClick,
        onResumeClick = profileViewModel::onResumeClick,
        onRetryClick = profileViewModel::loadProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onEditClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Профиль")
                },
                actions = {
                    TextButton(
                        onClick = onEditClick,
                        enabled = !state.isLoading &&
                                state.loadError == null &&
                                !state.isDownloadingResume
                    ) {
                        Text("Редактировать")
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

            state.loadError != null -> {
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
                        text = state.loadError,
                        textAlign = TextAlign.Center
                    )

                    Button(onClick = onRetryClick) {
                        Text("Повторить")
                    }
                }
            }

            else -> {
                val profile = state.profile

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileAvatar(
                        photoUri = profile.photoUri
                    )

                    Text(
                        text = profile.fullName.ifBlank {
                            "Профиль пока не заполнен"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )

                    if (profile.position.isNotBlank()) {
                        Text(
                            text = profile.position,
                            style = MaterialTheme.typography.titleMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (profile.fullName.isBlank()) {
                        Text(
                            text = "Нажмите «Редактировать», " +
                                    "чтобы добавить фотографию, ФИО " +
                                    "и ссылку на резюме.",
                            textAlign = TextAlign.Center,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onResumeClick,
                        enabled = profile.resumeUrl.isNotBlank() &&
                                !state.isDownloadingResume,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isDownloadingResume) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )

                            Spacer(
                                modifier = Modifier.width(10.dp)
                            )

                            Text("Скачивание…")
                        } else {
                            Text("Резюме")
                        }
                    }

                    if (
                        profile.resumeUrl.isBlank() &&
                        profile.fullName.isNotBlank()
                    ) {
                        Text(
                            text = "Добавьте ссылку на резюме " +
                                    "в редактировании профиля.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    state.actionError?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
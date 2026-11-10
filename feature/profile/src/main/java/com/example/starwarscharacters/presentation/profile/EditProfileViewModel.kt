package com.example.starwarscharacters.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.UserProfile
import com.example.starwarscharacters.domain.reminder.ClassReminderScheduler
import com.example.starwarscharacters.domain.repository.ProfilePhotoRepository
import com.example.starwarscharacters.domain.usecase.GetClassReminderTimeUseCase
import com.example.starwarscharacters.domain.usecase.ObserveProfileUseCase
import com.example.starwarscharacters.domain.usecase.SaveProfileUseCase
import com.example.starwarscharacters.domain.validation.FavoriteClassTimeValidator
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val fullName: String = "",
    val photoUri: String = "",
    val resumeUrl: String = "",
    val position: String = "",
    val favoriteClassTime: String = "",
    val isLoading: Boolean = true,
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val isProcessingPhoto: Boolean = false,
    val error: String? = null
) {
    val favoriteClassTimeError: String?
        get() = when {
            !isLoaded -> null

            favoriteClassTime.isEmpty() ->
                "Укажите время любимой пары"

            !FavoriteClassTimeValidator.isValid(favoriteClassTime) ->
                "Некорректное время. Используйте HH:mm, от 00:00 до 23:59"

            else -> null
        }

    val canEdit: Boolean
        get() = isLoaded &&
                !isLoading &&
                !isSaving &&
                !isProcessingPhoto

    val canSave: Boolean
        get() = canEdit &&
                FavoriteClassTimeValidator.isValid(favoriteClassTime)
}

class EditProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
    private val photoRepository: ProfilePhotoRepository,
    private val getClassReminderTime: GetClassReminderTimeUseCase,
    private val reminderScheduler: ClassReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val savedChannel = Channel<Unit>(Channel.BUFFERED)
    val savedEvents = savedChannel.receiveAsFlow()

    private val cameraChannel = Channel<String>(Channel.BUFFERED)
    val cameraEvents = cameraChannel.receiveAsFlow()

    private var loadRequest: Job? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (loadRequest?.isActive == true) return

        val state = _uiState.value
        if (state.isSaving || state.isProcessingPhoto) return

        _uiState.update {
            it.copy(
                isLoading = true,
                isLoaded = false,
                error = null
            )
        }

        loadRequest = viewModelScope.launch {
            try {
                val profile = observeProfile().first()

                _uiState.update {
                    it.copy(
                        fullName = profile.fullName,
                        photoUri = profile.photoUri,
                        resumeUrl = profile.resumeUrl,
                        position = profile.position,
                        favoriteClassTime = profile.favoriteClassTime,
                        isLoading = false,
                        isLoaded = true,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoaded = false,
                        error = "Не удалось загрузить профиль. " +
                                "Попробуйте ещё раз."
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        if (!_uiState.value.canEdit) return

        _uiState.update {
            it.copy(fullName = value, error = null)
        }
    }

    fun onPositionChange(value: String) {
        if (!_uiState.value.canEdit) return

        _uiState.update {
            it.copy(position = value, error = null)
        }
    }

    fun onResumeUrlChange(value: String) {
        if (!_uiState.value.canEdit) return

        _uiState.update {
            it.copy(resumeUrl = value, error = null)
        }
    }

    fun onFavoriteClassTimeChange(value: String) {
        if (!_uiState.value.canEdit) return

        _uiState.update {
            it.copy(
                favoriteClassTime = value,
                error = null
            )
        }
    }

    fun onRemovePhoto() {
        if (!_uiState.value.canEdit) return

        _uiState.update {
            it.copy(
                photoUri = "",
                error = null
            )
        }
    }

    fun onPhotoSelected(uri: String) {
        if (uri.isBlank()) return

        importPhoto(
            sourceUri = uri,
            discardSource = false
        )
    }

    fun onCameraClick() {
        if (!_uiState.value.canEdit) return

        _uiState.update {
            it.copy(
                isProcessingPhoto = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val uri = photoRepository.createCameraPhoto()
                cameraChannel.send(uri)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Не удалось подготовить файл для камеры"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isProcessingPhoto = false)
                }
            }
        }
    }

    fun onCameraResult(uri: String, success: Boolean) {
        if (uri.isBlank()) return

        if (success) {
            importPhoto(
                sourceUri = uri,
                discardSource = true
            )
        } else {
            discardCameraPhoto(uri)
        }
    }

    fun onCameraLaunchFailed(uri: String) {
        discardCameraPhoto(uri)

        onPhotoError(
            "Не удалось открыть камеру. " +
                    "Проверьте, доступно ли приложение камеры."
        )
    }

    fun onPhotoError(message: String) {
        if (_uiState.value.isSaving) return

        _uiState.update {
            it.copy(error = message)
        }
    }

    private fun importPhoto(
        sourceUri: String,
        discardSource: Boolean
    ) {
        val state = _uiState.value

        if (state.isSaving || state.isProcessingPhoto) {
            if (discardSource) {
                discardCameraPhoto(sourceUri)
            }
            return
        }

        _uiState.update {
            it.copy(
                isProcessingPhoto = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                loadRequest?.join()

                if (!_uiState.value.isLoaded) {
                    throw IOException("Профиль ещё не загружен")
                }

                val savedUri = photoRepository.importPhoto(sourceUri)

                _uiState.update {
                    it.copy(
                        photoUri = savedUri,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        error = e.message
                            ?: "Не удалось обработать фотографию"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Не удалось сохранить фотографию. " +
                                "Попробуйте выбрать другую."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isProcessingPhoto = false)
                }

                if (discardSource) {
                    discardCameraPhoto(sourceUri)
                }
            }
        }
    }

    private fun discardCameraPhoto(uri: String) {
        viewModelScope.launch {
            try {
                photoRepository.discardPhoto(uri)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Ошибка удаления временного файла
                // не должна мешать редактированию.
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        val profile = UserProfile(
            fullName = state.fullName,
            photoUri = state.photoUri,
            resumeUrl = state.resumeUrl,
            position = state.position,
            favoriteClassTime = state.favoriteClassTime
        )

        _uiState.update {
            it.copy(
                isSaving = true,
                error = null
            )
        }

        viewModelScope.launch {
            var profileWasSaved = false

            try {
                // Проверяем время до записи профиля.
                val triggerAtMillis = getClassReminderTime(
                    profile.favoriteClassTime
                )

                check(reminderScheduler.canShowNotifications()) {
                    "Разрешите уведомления для приложения"
                }

                check(reminderScheduler.canScheduleExactReminders()) {
                    "Разрешите приложению устанавливать точные напоминания"
                }

                saveProfile(profile)
                profileWasSaved = true

                reminderScheduler.schedule(
                    profileName = profile.fullName,
                    triggerAtMillis = triggerAtMillis
                )
            } catch (e: CancellationException) {
                _uiState.update {
                    it.copy(isSaving = false)
                }
                throw e
            } catch (e: Exception) {
                reportSaveError(
                    error = e,
                    profileWasSaved = profileWasSaved
                )
                return@launch
            }

            // Закрываем редактирование только после успешного
            // сохранения профиля и установки напоминания.
            savedChannel.send(Unit)
        }
    }

    private fun reportSaveError(
        error: Exception,
        profileWasSaved: Boolean
    ) {
        val explanation = when (error) {
            is IllegalArgumentException,
            is IllegalStateException -> {
                error.message ?: "Попробуйте ещё раз."
            }

            else -> {
                "Попробуйте ещё раз."
            }
        }

        val message = when {
            profileWasSaved -> {
                "Профиль сохранён, но напоминание не установлено. " +
                        explanation
            }

            error is IllegalArgumentException ||
                    error is IllegalStateException -> {
                explanation
            }

            else -> {
                "Не удалось сохранить профиль. $explanation"
            }
        }

        _uiState.update {
            it.copy(
                isSaving = false,
                error = message
            )
        }
    }

    companion object {

        fun factory(
            observeProfile: ObserveProfileUseCase,
            saveProfile: SaveProfileUseCase,
            photoRepository: ProfilePhotoRepository,
            getClassReminderTime: GetClassReminderTimeUseCase,
            reminderScheduler: ClassReminderScheduler
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            EditProfileViewModel::class.java
                        )
                    )

                    return EditProfileViewModel(
                        observeProfile = observeProfile,
                        saveProfile = saveProfile,
                        photoRepository = photoRepository,
                        getClassReminderTime = getClassReminderTime,
                        reminderScheduler = reminderScheduler
                    ) as T
                }
            }
        }
    }
}
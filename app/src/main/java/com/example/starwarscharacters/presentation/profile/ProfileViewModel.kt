package com.example.starwarscharacters.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.starwarscharacters.domain.DownloadedDocument
import com.example.starwarscharacters.domain.UserProfile
import com.example.starwarscharacters.domain.usecase.DownloadResumeUseCase
import com.example.starwarscharacters.domain.usecase.ObserveProfileUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isDownloadingResume: Boolean = false,
    val loadError: String? = null,
    val actionError: String? = null
)

sealed interface ProfileEvent {

    data class OpenResume(
        val document: DownloadedDocument
    ) : ProfileEvent
}

class ProfileViewModel(
    private val observeProfile: ObserveProfileUseCase,
    private val downloadResume: DownloadResumeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val eventChannel = Channel<ProfileEvent>(
        Channel.BUFFERED
    )
    val events = eventChannel.receiveAsFlow()

    private var profileRequest: Job? = null

    init {
        loadProfile()
    }

    fun loadProfile() {
        profileRequest?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                loadError = null
            )
        }

        profileRequest = viewModelScope.launch {
            try {
                observeProfile().collect { profile ->
                    _uiState.update {
                        it.copy(
                            profile = profile,
                            isLoading = false,
                            loadError = null
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = "Не удалось загрузить профиль. " +
                                "Попробуйте ещё раз."
                    )
                }
            }
        }
    }

    fun onResumeClick() {
        val state = _uiState.value

        if (
            state.isLoading ||
            state.loadError != null ||
            state.isDownloadingResume
        ) {
            return
        }

        val url = state.profile.resumeUrl

        if (url.isBlank()) {
            _uiState.update {
                it.copy(
                    actionError =
                        "Добавьте ссылку на резюме в редактировании профиля"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isDownloadingResume = true,
                actionError = null
            )
        }

        viewModelScope.launch {
            try {
                val document = downloadResume(url)

                eventChannel.send(
                    ProfileEvent.OpenResume(document)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        actionError = e.message
                            ?: "Проверьте ссылку на резюме"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        actionError =
                            "Не удалось скачать резюме. " +
                                    "Проверьте подключение к интернету " +
                                    "и прямую ссылку на файл."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isDownloadingResume = false)
                }
            }
        }
    }

    fun onDocumentOpenError() {
        _uiState.update {
            it.copy(
                actionError =
                    "Не удалось открыть скачанный файл. " +
                            "Проверьте, установлен ли просмотрщик " +
                            "для этого типа документа."
            )
        }
    }

    companion object {

        fun factory(
            observeProfile: ObserveProfileUseCase,
            downloadResume: DownloadResumeUseCase
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>
                ): T {
                    require(
                        modelClass.isAssignableFrom(
                            ProfileViewModel::class.java
                        )
                    )

                    return ProfileViewModel(
                        observeProfile = observeProfile,
                        downloadResume = downloadResume
                    ) as T
                }
            }
        }
    }
}
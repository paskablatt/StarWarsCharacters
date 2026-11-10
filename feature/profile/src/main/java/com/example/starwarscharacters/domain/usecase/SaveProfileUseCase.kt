package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.UserProfile
import com.example.starwarscharacters.domain.repository.ProfileRepository
import com.example.starwarscharacters.domain.validation.FavoriteClassTimeValidator
import java.net.URI

class SaveProfileUseCase(
    private val repository: ProfileRepository
) {

    suspend operator fun invoke(profile: UserProfile) {
        val normalizedProfile = profile.copy(
            fullName = profile.fullName.trim(),
            resumeUrl = profile.resumeUrl.trim(),
            position = profile.position.trim()
        )

        require(normalizedProfile.fullName.isNotBlank()) {
            "Введите ФИО"
        }

        require(
            FavoriteClassTimeValidator.isValid(
                normalizedProfile.favoriteClassTime
            )
        ) {
            "Введите время в формате HH:mm: от 00:00 до 23:59"
        }

        val resumeUrl = normalizedProfile.resumeUrl

        if (resumeUrl.isNotBlank()) {
            val uri = try {
                URI(resumeUrl)
            } catch (_: Exception) {
                throw IllegalArgumentException(
                    "Введите корректную ссылку на резюме"
                )
            }

            require(
                (
                        uri.scheme.equals("https", ignoreCase = true) ||
                                uri.scheme.equals("http", ignoreCase = true)
                        ) &&
                        !uri.host.isNullOrBlank() &&
                        uri.rawUserInfo == null
            ) {
                "Ссылка должна начинаться с https:// или http:// " +
                        "и содержать адрес сайта"
            }
        }

        repository.saveProfile(normalizedProfile)
    }
}
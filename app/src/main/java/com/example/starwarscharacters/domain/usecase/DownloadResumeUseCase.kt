package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.DownloadedDocument
import com.example.starwarscharacters.domain.repository.ResumeRepository

class DownloadResumeUseCase(
    private val repository: ResumeRepository
) {

    suspend operator fun invoke(url: String): DownloadedDocument {
        val normalizedUrl = url.trim()

        require(normalizedUrl.isNotBlank()) {
            "Сначала добавьте ссылку на резюме в профиле"
        }

        return repository.downloadResume(normalizedUrl)
    }
}
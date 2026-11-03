package com.example.starwarscharacters.domain.repository

import com.example.starwarscharacters.domain.DownloadedDocument

interface ResumeRepository {

    suspend fun downloadResume(url: String): DownloadedDocument
}
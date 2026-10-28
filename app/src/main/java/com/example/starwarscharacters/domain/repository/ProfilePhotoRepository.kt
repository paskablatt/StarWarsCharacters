package com.example.starwarscharacters.domain.repository

interface ProfilePhotoRepository {

    suspend fun createCameraPhoto(): String

    suspend fun importPhoto(sourceUri: String): String

    suspend fun discardPhoto(uri: String)
}
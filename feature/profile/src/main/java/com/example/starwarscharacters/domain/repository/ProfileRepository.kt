package com.example.starwarscharacters.domain.repository

import com.example.starwarscharacters.domain.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    val profile: Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile)
}
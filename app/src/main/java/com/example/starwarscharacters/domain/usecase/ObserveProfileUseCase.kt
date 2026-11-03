package com.example.starwarscharacters.domain.usecase

import com.example.starwarscharacters.domain.UserProfile
import com.example.starwarscharacters.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveProfileUseCase(
    private val repository: ProfileRepository
) {

    operator fun invoke(): Flow<UserProfile> {
        return repository.profile
    }
}
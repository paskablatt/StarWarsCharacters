package com.example.starwarscharacters.domain.repository

import com.example.starwarscharacters.domain.CharacterFilters
import kotlinx.coroutines.flow.Flow

interface SearchSettingsRepository {

    val filters: Flow<CharacterFilters>

    suspend fun saveFilters(filters: CharacterFilters)

    // Сохраняем совместимость с текущим экраном поиска.
    val searchQuery: Flow<String>

    suspend fun saveSearchQuery(query: String)
}
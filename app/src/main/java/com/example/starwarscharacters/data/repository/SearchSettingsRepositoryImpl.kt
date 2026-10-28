package com.example.starwarscharacters.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.starwarscharacters.domain.CharacterFilters
import com.example.starwarscharacters.domain.HeightFilter
import com.example.starwarscharacters.domain.repository.SearchSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.searchSettingsDataStore by preferencesDataStore(
    name = "character_search_settings"
)

class SearchSettingsRepositoryImpl(
    context: Context
) : SearchSettingsRepository {

    private val dataStore =
        context.applicationContext.searchSettingsDataStore

    override val filters: Flow<CharacterFilters> =
        dataStore.data
            .map { preferences ->
                val savedHeight = preferences[HEIGHT_FILTER]

                val heightFilter = HeightFilter.entries
                    .firstOrNull { it.name == savedHeight }
                    ?: HeightFilter.ANY

                CharacterFilters(
                    query = preferences[SEARCH_QUERY].orEmpty(),
                    episodeId = preferences[EPISODE_ID],
                    heightFilter = heightFilter
                )
            }
            .distinctUntilChanged()

    override val searchQuery: Flow<String> =
        filters
            .map { it.query }
            .distinctUntilChanged()

    override suspend fun saveFilters(filters: CharacterFilters) {
        dataStore.edit { preferences ->
            preferences[SEARCH_QUERY] = filters.query.trim()
            preferences[HEIGHT_FILTER] = filters.heightFilter.name

            val episodeId = filters.episodeId

            if (episodeId == null) {
                preferences.remove(EPISODE_ID)
            } else {
                preferences[EPISODE_ID] = episodeId
            }
        }
    }

    override suspend fun saveSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_QUERY] = query.trim()
        }
    }

    private companion object {
        val SEARCH_QUERY = stringPreferencesKey("search_query")
        val EPISODE_ID = intPreferencesKey("episode_id")
        val HEIGHT_FILTER = stringPreferencesKey("height_filter")
    }
}
package com.example.starwarscharacters.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.starwarscharacters.domain.UserProfile
import com.example.starwarscharacters.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(
    name = "user_profile"
)

class ProfileRepositoryImpl(
    context: Context
) : ProfileRepository {

    private val dataStore =
        context.applicationContext.profileDataStore

    override val profile: Flow<UserProfile> =
        dataStore.data
            .map { preferences ->
                UserProfile(
                    fullName = preferences[FULL_NAME].orEmpty(),
                    photoUri = preferences[PHOTO_URI].orEmpty(),
                    resumeUrl = preferences[RESUME_URL].orEmpty(),
                    position = preferences[POSITION].orEmpty()
                )
            }
            .distinctUntilChanged()

    override suspend fun saveProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[FULL_NAME] = profile.fullName.trim()
            preferences[PHOTO_URI] = profile.photoUri
            preferences[RESUME_URL] = profile.resumeUrl.trim()
            preferences[POSITION] = profile.position.trim()
        }
    }

    private companion object {
        val FULL_NAME = stringPreferencesKey("full_name")
        val PHOTO_URI = stringPreferencesKey("photo_uri")
        val RESUME_URL = stringPreferencesKey("resume_url")
        val POSITION = stringPreferencesKey("position")
    }
}
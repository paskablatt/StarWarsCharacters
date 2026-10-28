package com.example.starwarscharacters.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorite_characters ORDER BY name ASC")
    fun observeFavorites(): Flow<List<FavoriteCharacterEntity>>

    @Query("SELECT * FROM favorite_characters WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): FavoriteCharacterEntity?

    @Query(
        "SELECT EXISTS(" +
                "SELECT 1 FROM favorite_characters WHERE name = :name" +
                ")"
    )
    fun observeIsFavorite(name: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(character: FavoriteCharacterEntity)

    @Query("DELETE FROM favorite_characters WHERE name = :name")
    suspend fun remove(name: String)
}
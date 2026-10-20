package com.example.starwarscharacters.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteCharacterEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StarWarsDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao
}
package com.example.starwarscharacters.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.starwarscharacters.domain.StarWarsCharacter

@Entity(tableName = "favorite_characters")
data class FavoriteCharacterEntity(
    @PrimaryKey
    val name: String,
    val imageRes: Int,
    val weaponImageRes: Int?,
    val weaponName: String,
    val height: String,
    val mass: String,
    val hairColor: String,
    val skinColor: String,
    val eyeColor: String,
    val birthYear: String,
    val gender: String,
    val wikiUrl: String
)

fun StarWarsCharacter.toFavoriteEntity(): FavoriteCharacterEntity {
    return FavoriteCharacterEntity(
        name = name,
        imageRes = imageRes,
        weaponImageRes = weaponImageRes,
        weaponName = weaponName,
        height = height,
        mass = mass,
        hairColor = hairColor,
        skinColor = skinColor,
        eyeColor = eyeColor,
        birthYear = birthYear,
        gender = gender,
        wikiUrl = wikiUrl
    )
}

fun FavoriteCharacterEntity.toDomain(): StarWarsCharacter {
    return StarWarsCharacter(
        name = name,
        imageRes = imageRes,
        weaponImageRes = weaponImageRes,
        weaponName = weaponName,
        height = height,
        mass = mass,
        hairColor = hairColor,
        skinColor = skinColor,
        eyeColor = eyeColor,
        birthYear = birthYear,
        gender = gender,
        wikiUrl = wikiUrl
    )
}
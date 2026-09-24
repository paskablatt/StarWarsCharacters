package com.example.starwarscharacters.domain

data class StarWarsCharacter(
    val name: String,
    val imageRes: Int,
    val weaponImageRes: Int,
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
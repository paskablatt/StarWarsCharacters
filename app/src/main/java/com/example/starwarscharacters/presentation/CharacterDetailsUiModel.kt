package com.example.starwarscharacters.presentation

data class CharacterDetailsUiModel(
    val name: String,
    val imageRes: Int,
    val weaponImageRes: Int?,
    val weaponName: String,
    val basicInfo: List<String>,
    val appearance: List<String>,
    val wikiUrl: String,
    val shareText: String,
    val isFavorite: Boolean? = null,
    val isSavingFavorite: Boolean = false,
    val favoriteError: String? = null
)
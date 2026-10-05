package com.example.starwarscharacters.data.mapper

import com.example.starwarscharacters.data.local.characterLocalData
import com.example.starwarscharacters.data.remote.dto.CharacterDto
import com.example.starwarscharacters.domain.StarWarsCharacter

fun CharacterDto.toDomainOrNull(): StarWarsCharacter? {
    val localData = characterLocalData[name] ?: return null

    return StarWarsCharacter(
        name = name,
        imageRes = localData.imageRes,
        weaponImageRes = localData.weaponImageRes,
        weaponName = localData.weaponName,
        height = height,
        mass = mass,
        hairColor = translateHairColor(hairColor),
        skinColor = translateSkinColor(skinColor),
        eyeColor = translateEyeColor(eyeColor),
        birthYear = birthYear,
        gender = translateGender(gender),
        wikiUrl = localData.wikiUrl
    )
}

private fun translateGender(value: String): String = when (value.lowercase()) {
    "male" -> "Мужской"
    "female" -> "Женский"
    "hermaphrodite" -> "Гермафродит"
    "n/a", "none", "unknown" -> "Нет данных"
    else -> value
}

private fun translateHairColor(value: String): String = when (value.lowercase()) {
    "blond" -> "Светлые"
    "brown" -> "Каштановые"
    "black" -> "Чёрные"
    "white" -> "Белые"
    "grey" -> "Седые"
    "auburn" -> "Рыжие"
    "auburn, white" -> "Рыжие, седые"
    "brown, grey" -> "Каштановые, седые"
    "none", "n/a" -> "Нет"
    "unknown" -> "Нет данных"
    else -> value
}

private fun translateSkinColor(value: String): String = when (value.lowercase()) {
    "fair" -> "Светлая"
    "light" -> "Светлая"
    "white" -> "Белая"
    "green" -> "Зелёная"
    "pale" -> "Бледная"
    "dark" -> "Тёмная"
    "brown" -> "Коричневая"
    "unknown" -> "Нет данных"
    else -> value
}

private fun translateEyeColor(value: String): String = when (value.lowercase()) {
    "blue" -> "Голубые"
    "blue-gray" -> "Серо-голубые"
    "brown" -> "Карие"
    "yellow" -> "Жёлтые"
    "green" -> "Зелёные"
    "red" -> "Красные"
    "black" -> "Чёрные"
    "unknown" -> "Нет данных"
    else -> value
}
package com.example.starwarscharacters.data

import com.example.starwarscharacters.R
import com.example.starwarscharacters.domain.StarWarsCharacter

val mockCharacters = listOf(

    StarWarsCharacter(
        name = "Luke Skywalker",
        imageRes = R.drawable.luke_skywalker,
        weaponImageRes = R.drawable.luke_weapon,
        weaponName = "Световой меч",
        height = "172",
        mass = "77",
        hairColor = "Светлые",
        skinColor = "Светлая",
        eyeColor = "Голубые",
        birthYear = "19BBY",
        gender = "Мужской",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Люк_Скайуокер"
    ),

    StarWarsCharacter(
        name = "Darth Vader",
        imageRes = R.drawable.darth_vader,
        weaponImageRes = R.drawable.vader_weapon,
        weaponName = "Световой меч",
        height = "202",
        mass = "136",
        hairColor = "Нет",
        skinColor = "Белая",
        eyeColor = "Жёлтые",
        birthYear = "41.9BBY",
        gender = "Мужской",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Дарт_Вейдер:_Лорд_ситхов"
    ),

    StarWarsCharacter(
        name = "Leia Organa",
        imageRes = R.drawable.leia_organa,
        weaponImageRes = R.drawable.leia_weapon,
        weaponName = "Бластер",
        height = "150",
        mass = "49",
        hairColor = "Каштановые",
        skinColor = "Светлая",
        eyeColor = "Карие",
        birthYear = "19BBY",
        gender = "Женский",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Лея_Органа-Соло"
    ),

    StarWarsCharacter(
        name = "Obi-Wan Kenobi",
        imageRes = R.drawable.obi_wan_kenobi,
        weaponImageRes = R.drawable.obi_wan_weapon,
        weaponName = "Световой меч",
        height = "182",
        mass = "77",
        hairColor = "Рыжие, седые",
        skinColor = "Светлая",
        eyeColor = "Серо-голубые",
        birthYear = "57BBY",
        gender = "Мужской",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Оби-Ван_Кеноби"
    ),

    StarWarsCharacter(
        name = "Yoda",
        imageRes = R.drawable.yoda,
        weaponImageRes = R.drawable.yoda_weapon,
        weaponName = "Световой меч",
        height = "66",
        mass = "17",
        hairColor = "Белые",
        skinColor = "Зелёная",
        eyeColor = "Карие",
        birthYear = "896BBY",
        gender = "Мужской",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Йода"
    )
)
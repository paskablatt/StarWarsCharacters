package com.example.starwarscharacters.data.local

import androidx.annotation.DrawableRes
import com.example.starwarscharacters.R

data class CharacterLocalData(
    @DrawableRes val imageRes: Int,
    @DrawableRes val weaponImageRes: Int,
    val weaponName: String,
    val wikiUrl: String
)

val characterLocalData = mapOf(

    "Luke Skywalker" to CharacterLocalData(
        imageRes = R.drawable.luke_skywalker,
        weaponImageRes = R.drawable.luke_weapon,
        weaponName = "Световой меч",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Люк_Скайуокер"
    ),

    "Darth Vader" to CharacterLocalData(
        imageRes = R.drawable.darth_vader,
        weaponImageRes = R.drawable.vader_weapon,
        weaponName = "Световой меч",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Дарт_Вейдер"
    ),

    "Leia Organa" to CharacterLocalData(
        imageRes = R.drawable.leia_organa,
        weaponImageRes = R.drawable.leia_weapon,
        weaponName = "Бластер",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Лея_Органа"
    ),

    "Obi-Wan Kenobi" to CharacterLocalData(
        imageRes = R.drawable.obi_wan_kenobi,
        weaponImageRes = R.drawable.obi_wan_weapon,
        weaponName = "Световой меч",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Оби-Ван_Кеноби"
    ),

    "Yoda" to CharacterLocalData(
        imageRes = R.drawable.yoda,
        weaponImageRes = R.drawable.yoda_weapon,
        weaponName = "Световой меч",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Йода"
    ),

    "Han Solo" to CharacterLocalData(
        imageRes = R.drawable.han_solo,
        weaponImageRes = R.drawable.han_solo_weapon,
        weaponName = "Бластер DL-44",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Хан_Соло"
    ),

    "Chewbacca" to CharacterLocalData(
        imageRes = R.drawable.chewbacca,
        weaponImageRes = R.drawable.chewbacca_weapon,
        weaponName = "Арбалет",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Чубакка"
    ),

    "Palpatine" to CharacterLocalData(
        imageRes = R.drawable.palpatine,
        weaponImageRes = R.drawable.palpatine_weapon,
        weaponName = "Световой меч",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Палпатин"
    ),

    "Boba Fett" to CharacterLocalData(
        imageRes = R.drawable.boba_fett,
        weaponImageRes = R.drawable.boba_fett_weapon,
        weaponName = "Бластерная винтовка",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Боба_Фетт"
    ),

    "Lando Calrissian" to CharacterLocalData(
        imageRes = R.drawable.lando_calrissian,
        weaponImageRes = R.drawable.lando_weapon,
        weaponName = "Бластер",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Лэндо_Калриссиан"
    ),

    "Anakin Skywalker" to CharacterLocalData(
        imageRes = R.drawable.anakin_skywalker,
        weaponImageRes = R.drawable.anakin_weapon,
        weaponName = "Световой меч",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Энакин_Скайуокер"
    ),

    "Mace Windu" to CharacterLocalData(
        imageRes = R.drawable.mace_windu,
        weaponImageRes = R.drawable.mace_windu_weapon,
        weaponName = "Световой меч",
        wikiUrl = "https://starwars.fandom.com/ru/wiki/Мейс_Винду"
    )
)
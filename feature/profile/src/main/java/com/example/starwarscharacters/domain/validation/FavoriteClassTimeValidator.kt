package com.example.starwarscharacters.domain.validation

object FavoriteClassTimeValidator {

    private val timePattern =
        Regex("([01][0-9]|2[0-3]):[0-5][0-9]")

    fun isValid(value: String): Boolean {
        return timePattern.matches(value)
    }
}
package com.example.starwarscharacters.domain

enum class HeightFilter(
    val title: String
) {
    ANY("Любой рост"),
    BELOW_150("Ниже 150 см"),
    FROM_150_TO_180("От 150 до 180 см"),
    ABOVE_180("Выше 180 см");

    fun matches(value: String): Boolean {
        if (this == ANY) return true

        val height = value.trim().toDoubleOrNull()
            ?: return false

        return when (this) {
            ANY -> true
            BELOW_150 -> height < 150
            FROM_150_TO_180 -> height >= 150 && height <= 180
            ABOVE_180 -> height > 180
        }
    }
}

data class CharacterFilters(
    val query: String = "",
    val episodeId: Int? = null,
    val heightFilter: HeightFilter = HeightFilter.ANY
) {
    val isActive: Boolean
        get() = query.isNotBlank() ||
                episodeId != null ||
                heightFilter != HeightFilter.ANY
}

data class FilmFilterOption(
    val episodeId: Int,
    val title: String
)
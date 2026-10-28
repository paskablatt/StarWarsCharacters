package com.example.starwarscharacters.presentation.settings

import com.example.starwarscharacters.domain.CharacterFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterBadgeCache {

    private val _showBadge = MutableStateFlow(false)
    val showBadge = _showBadge.asStateFlow()

    fun update(filters: CharacterFilters) {
        _showBadge.value = filters.isActive
    }
}
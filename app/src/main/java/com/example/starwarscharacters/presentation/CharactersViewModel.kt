package com.example.starwarscharacters.presentation

import androidx.lifecycle.ViewModel
import com.example.starwarscharacters.data.mockCharacters
import com.example.starwarscharacters.domain.StarWarsCharacter

class CharactersViewModel : ViewModel() {

    val characters: List<StarWarsCharacter> = mockCharacters
}
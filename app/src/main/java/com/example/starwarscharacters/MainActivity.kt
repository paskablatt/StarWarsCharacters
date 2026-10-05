package com.example.starwarscharacters

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.starwarscharacters.navigation.AppNavigation
import com.example.starwarscharacters.ui.theme.StarWarsCharactersTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            StarWarsCharactersTheme {
                AppNavigation()
            }
        }
    }
}
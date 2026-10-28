package com.example.starwarscharacters.domain

data class UserProfile(
    val fullName: String = "",
    val photoUri: String = "",
    val resumeUrl: String = "",
    val position: String = ""
)
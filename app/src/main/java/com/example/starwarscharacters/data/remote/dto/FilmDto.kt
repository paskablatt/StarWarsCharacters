package com.example.starwarscharacters.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FilmDto(
    val title: String,

    @SerializedName("episode_id")
    val episodeId: Int,

    @SerializedName("opening_crawl")
    val openingCrawl: String,

    val director: String,
    val producer: String,

    @SerializedName("release_date")
    val releaseDate: String
)
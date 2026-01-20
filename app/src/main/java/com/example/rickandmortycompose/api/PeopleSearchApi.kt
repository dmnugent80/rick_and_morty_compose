package com.example.rickandmortycompose.api

import retrofit2.http.GET
import retrofit2.http.Query

interface PeopleSearchApi {
    @GET("character/")
    suspend fun searchCharacters(
        @Query("name") query: String,
        @Query("page") page: Int = 1
    ): CharacterResponse
}

data class PageInfo(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

data class CharacterResponse(
    val info: PageInfo,
    val results: List<CharacterDto>
)

data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: LocationDto,
    val location: LocationDto,
    val image: String,
    val episode: List<String>,
    val url: String,
    val created: String
)

data class LocationDto(
    val name: String,
    val url: String
)

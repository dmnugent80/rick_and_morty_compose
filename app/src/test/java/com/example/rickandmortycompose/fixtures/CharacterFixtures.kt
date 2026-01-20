package com.example.rickandmortycompose.fixtures

import com.example.rickandmortycompose.api.CharacterDto
import com.example.rickandmortycompose.api.CharacterResponse
import com.example.rickandmortycompose.api.LocationDto
import com.example.rickandmortycompose.api.PageInfo
import com.example.rickandmortycompose.model.Character
import com.example.rickandmortycompose.repository.SearchResult

object CharacterFixtures {

    fun createCharacter(
        id: Int = 1,
        name: String = "Rick Sanchez",
        status: String = "Alive",
        species: String = "Human",
        type: String = "",
        gender: String = "Male",
        origin: String = "Earth (C-137)",
        location: String = "Citadel of Ricks",
        imageUrl: String = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
    ) = Character(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        origin = origin,
        location = location,
        imageUrl = imageUrl
    )

    fun createCharacterDto(
        id: Int = 1,
        name: String = "Rick Sanchez",
        status: String = "Alive",
        species: String = "Human",
        type: String = "",
        gender: String = "Male",
        originName: String = "Earth (C-137)",
        originUrl: String = "https://rickandmortyapi.com/api/location/1",
        locationName: String = "Citadel of Ricks",
        locationUrl: String = "https://rickandmortyapi.com/api/location/3",
        image: String = "https://rickandmortyapi.com/api/character/avatar/1.jpeg",
        episode: List<String> = listOf("https://rickandmortyapi.com/api/episode/1"),
        url: String = "https://rickandmortyapi.com/api/character/1",
        created: String = "2017-11-04T18:48:46.250Z"
    ) = CharacterDto(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        origin = LocationDto(name = originName, url = originUrl),
        location = LocationDto(name = locationName, url = locationUrl),
        image = image,
        episode = episode,
        url = url,
        created = created
    )

    fun createPageInfo(
        count: Int = 826,
        pages: Int = 42,
        next: String? = "https://rickandmortyapi.com/api/character?page=2&name=rick",
        prev: String? = null
    ) = PageInfo(
        count = count,
        pages = pages,
        next = next,
        prev = prev
    )

    fun createCharacterResponse(
        characters: List<CharacterDto> = listOf(createCharacterDto()),
        info: PageInfo = createPageInfo()
    ) = CharacterResponse(
        info = info,
        results = characters
    )

    fun createSearchResult(
        characters: List<Character> = listOf(createCharacter()),
        hasNextPage: Boolean = true,
        nextPage: Int? = 2
    ) = SearchResult(
        characters = characters,
        hasNextPage = hasNextPage,
        nextPage = nextPage
    )

    val rickSanchez = createCharacter(
        id = 1,
        name = "Rick Sanchez",
        status = "Alive",
        species = "Human",
        location = "Citadel of Ricks"
    )

    val mortySmith = createCharacter(
        id = 2,
        name = "Morty Smith",
        status = "Alive",
        species = "Human",
        location = "Citadel of Ricks"
    )

    val summerSmith = createCharacter(
        id = 3,
        name = "Summer Smith",
        status = "Alive",
        species = "Human",
        location = "Earth (Replacement Dimension)"
    )

    val rickSanchezDto = createCharacterDto(id = 1, name = "Rick Sanchez")
    val mortySmithDto = createCharacterDto(id = 2, name = "Morty Smith")
    val summerSmithDto = createCharacterDto(id = 3, name = "Summer Smith")
}

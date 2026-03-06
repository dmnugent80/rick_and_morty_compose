package com.example.rickandmorty.core.domain.fixtures

import com.example.rickandmorty.core.domain.model.Character

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
}

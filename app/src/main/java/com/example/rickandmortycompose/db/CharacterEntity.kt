package com.example.rickandmortycompose.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.rickandmortycompose.model.Character

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: String,
    val location: String,
    val imageUrl: String
) {
    fun toCharacter(): Character = Character(
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

    companion object {
        fun fromCharacter(character: Character): CharacterEntity = CharacterEntity(
            id = character.id,
            name = character.name,
            status = character.status,
            species = character.species,
            type = character.type,
            gender = character.gender,
            origin = character.origin,
            location = character.location,
            imageUrl = character.imageUrl
        )
    }
}

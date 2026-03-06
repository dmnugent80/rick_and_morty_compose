package com.example.rickandmorty.navigation

import kotlinx.serialization.Serializable

@Serializable
object Search

@Serializable
data class CharacterDetail(val characterId: Int)

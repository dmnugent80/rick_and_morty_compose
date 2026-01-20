package com.example.rickandmortycompose.navigation

object NavRoutes {
    const val SEARCH = "search"
    const val CHARACTER_DETAIL = "character/{characterId}"

    fun characterDetail(characterId: Int): String = "character/$characterId"
}

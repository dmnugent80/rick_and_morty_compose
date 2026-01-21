package com.example.rickandmortycompose.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.rickandmortycompose.api.CharacterSearchApi
import com.example.rickandmortycompose.model.Character

class CharacterPagingSource(
    private val api: CharacterSearchApi,
    private val query: String?
) : PagingSource<Int, Character>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Character> {
        val page = params.key ?: 1
        return try {
            val response = api.searchCharacters(query = query, page = page)
            val characters = response.results.map { dto ->
                Character(
                    id = dto.id,
                    name = dto.name,
                    status = dto.status,
                    species = dto.species,
                    type = dto.type,
                    gender = dto.gender,
                    origin = dto.origin.name,
                    location = dto.location.name,
                    imageUrl = dto.image
                )
            }
            LoadResult.Page(
                data = characters,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.info.next != null) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Character>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

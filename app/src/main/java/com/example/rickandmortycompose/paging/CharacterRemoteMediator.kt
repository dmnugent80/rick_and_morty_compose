package com.example.rickandmortycompose.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.rickandmortycompose.api.CharacterSearchApi
import com.example.rickandmortycompose.db.AppDatabase
import com.example.rickandmortycompose.db.CharacterEntity
import com.example.rickandmortycompose.db.RemoteKeyEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class CharacterRemoteMediator(
    private val api: CharacterSearchApi,
    private val database: AppDatabase
) : RemoteMediator<Int, CharacterEntity>() {

    private val characterDao = database.characterDao()
    private val remoteKeyDao = database.remoteKeyDao()

    override suspend fun initialize(): InitializeAction {
        val count = characterDao.count()
        return if (count > 0) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CharacterEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                if (lastItem == null) {
                    1
                } else {
                    val remoteKey = remoteKeyDao.remoteKeyByCharacterId(lastItem.id)
                    remoteKey?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
        }

        return try {
            val response = api.searchCharacters(query = null, page = page)
            val characters = response.results.map { dto ->
                CharacterEntity(
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

            val endOfPaginationReached = response.info.next == null

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    characterDao.clearAll()
                    remoteKeyDao.clearAll()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val remoteKeys = characters.map { character ->
                    RemoteKeyEntity(
                        characterId = character.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }

                characterDao.insertAll(characters)
                remoteKeyDao.insertAll(remoteKeys)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            // On network error, check if we have cached data
            // If we do, return success and let Room serve the cached data
            val cachedCount = characterDao.count()
            if (cachedCount > 0 && loadType == LoadType.REFRESH) {
                // We have cached data, return success to display it
                MediatorResult.Success(endOfPaginationReached = false)
            } else {
                // No cached data, surface the error to the user
                MediatorResult.Error(e)
            }
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}

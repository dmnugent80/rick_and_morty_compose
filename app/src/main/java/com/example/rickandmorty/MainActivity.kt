package com.example.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rickandmorty.core.ui.theme.RickAndMortyComposeTheme
import com.example.rickandmorty.feature.detail.DetailScreen
import com.example.rickandmorty.feature.detail.DetailViewModel
import com.example.rickandmorty.feature.search.SearchScreen
import com.example.rickandmorty.feature.search.SearchViewModel
import com.example.rickandmorty.navigation.CharacterDetail
import com.example.rickandmorty.navigation.Search
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalSharedTransitionApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RickAndMortyComposeTheme {
                val navController = rememberNavController()

                SharedTransitionLayout {
                    NavHost(
                        navController = navController,
                        startDestination = Search
                    ) {
                        composable<Search> {
                            val viewModel: SearchViewModel = hiltViewModel()
                            val state by viewModel.state.collectAsStateWithLifecycle()
                            val pagingItems = viewModel.pagingData.collectAsLazyPagingItems()

                            SearchScreen(
                                state = state,
                                pagingItems = pagingItems,
                                onIntent = viewModel::handleIntent,
                                onCharacterClick = { characterId ->
                                    navController.navigate(CharacterDetail(characterId))
                                },
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable
                            )
                        }

                        composable<CharacterDetail> {
                            val viewModel: DetailViewModel = hiltViewModel()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            DetailScreen(
                                state = state,
                                onBackClick = { navController.popBackStack() },
                                onRetry = viewModel::retry,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable
                            )
                        }
                    }
                }
            }
        }
    }
}

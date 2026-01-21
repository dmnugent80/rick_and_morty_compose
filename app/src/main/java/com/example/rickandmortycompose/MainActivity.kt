package com.example.rickandmortycompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rickandmortycompose.feature.detail.composables.DetailScreen
import com.example.rickandmortycompose.feature.detail.viewModel.DetailViewModel
import com.example.rickandmortycompose.feature.search.composables.SearchScreen
import com.example.rickandmortycompose.feature.search.viewModel.SearchViewModel
import com.example.rickandmortycompose.navigation.CharacterDetail
import com.example.rickandmortycompose.navigation.Search
import com.example.rickandmortycompose.ui.theme.RickAndMortyComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RickAndMortyComposeTheme {
                val navController = rememberNavController()

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
                            }
                        )
                    }

                    composable<CharacterDetail> {
                        val viewModel: DetailViewModel = hiltViewModel()
                        val state by viewModel.state.collectAsStateWithLifecycle()

                        DetailScreen(
                            state = state,
                            onBackClick = { navController.popBackStack() },
                            onRetry = viewModel::retry
                        )
                    }
                }
            }
        }
    }
}

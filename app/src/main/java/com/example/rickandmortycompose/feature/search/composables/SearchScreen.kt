package com.example.rickandmortycompose.feature.search.composables

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.rickandmortycompose.R
import com.example.rickandmortycompose.feature.search.viewModel.SearchViewState
import com.example.rickandmortycompose.ui.theme.RickAndMortyBlue
import com.example.rickandmortycompose.ui.theme.RickAndMortyComposeTheme
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreen(
    state: SearchViewState,
    pagingItems: LazyPagingItems<SearchResultItem>,
    onIntent: (SearchIntent) -> Unit,
    onCharacterClick: (Int) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {

        TopAppBar(
            title = {
                Text(
                    "Rick And Morty Search",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.background
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { newQuery ->
                    onIntent(SearchIntent.QueryChanged(newQuery))
                },
                modifier = Modifier.weight(1f),
                label = { Text("Search Characters") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onIntent(SearchIntent.SubmitSearch)
                        focusManager.clearFocus()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    onIntent(SearchIntent.SubmitSearch)
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        TextButton(
            onClick = {
                onIntent(SearchIntent.SeeAll)
                focusManager.clearFocus()
            }
        ) {
            Text(
                text = stringResource(R.string.see_all),
                color = RickAndMortyBlue,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isSearchActive && pagingItems.loadState.refresh is LoadState.Loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (pagingItems.loadState.refresh is LoadState.Error) {
            val error = (pagingItems.loadState.refresh as LoadState.Error).error
            Text(
                text = error.localizedMessage ?: stringResource(R.string.an_error_occurred),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                count = pagingItems.itemCount,
                key = { index -> pagingItems[index]?.id ?: index }
            ) { index ->
                val item = pagingItems[index]
                if (item != null) {
                    SearchResultRow(
                        item = item,
                        onClick = { onCharacterClick(item.id) },
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    HorizontalDivider(
                        modifier = Modifier,
                        thickness = DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }

            if (pagingItems.loadState.append is LoadState.Loading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (pagingItems.loadState.append is LoadState.Error) {
                item {
                    val error = (pagingItems.loadState.append as LoadState.Error).error
                    Text(
                        text = error.localizedMessage ?: stringResource(R.string.failed_to_load_more),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchResultRow(
    item: SearchResultItem,
    onClick: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                Modifier
                    .sharedElement(
                        rememberSharedContentState(key = "avatar-${item.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .size(60.dp)
                    .clip(CircleShape)
            }
        } else {
            Modifier
                .size(60.dp)
                .clip(CircleShape)
        }
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            modifier = imageModifier,
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    RickAndMortyComposeTheme {
        val previewItems = flowOf(
            PagingData.from(
                listOf(
                    SearchResultItem(
                        1,
                        "Rick Sanchez",
                        "Alive - Human",
                        "Location: Citadel of Ricks",
                        "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
                    ),
                    SearchResultItem(
                        2,
                        "Morty Smith",
                        "Alive - Human",
                        "Location: Citadel of Ricks",
                        "https://rickandmortyapi.com/api/character/avatar/2.jpeg"
                    ),
                    SearchResultItem(
                        3,
                        "Summer Smith",
                        "Alive - Human",
                        "Location: Earth",
                        "https://rickandmortyapi.com/api/character/avatar/3.jpeg"
                    ),
                    SearchResultItem(
                        4,
                        "Beth Smith",
                        "Alive - Human",
                        "Location: Earth",
                        "https://rickandmortyapi.com/api/character/avatar/4.jpeg"
                    ),
                )
            )
        ).collectAsLazyPagingItems()

        SearchScreen(
            state = SearchViewState(query = "Rick"),
            pagingItems = previewItems,
            onIntent = {},
        )
    }
}


// --- State definitions ---

data class SearchResultItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val imageUrl: String
)

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object SubmitSearch : SearchIntent
    data object SeeAll : SearchIntent
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun ExploreScreen(
    language: Language,
    currentUser: UserAccount?,
    movies: List<MovieItem>,
    availableDjs: List<String>,
    selectedCategory: MovieCategory,
    selectedDj: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (MovieCategory) -> Unit,
    onSelectDj: (String?) -> Unit,
    onMovieClick: (MovieItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        // Search Input
        item {
            Text(
                text = Strings.navExplore(language),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MovieTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        if (language == Language.SW) "Tafuta filamu, DJ, au mfululizo..." else "Rechercher films, DJs, séries...",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MovieTextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MovieTextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("explore_search_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MovieRedPrimary,
                    unfocusedBorderColor = MovieBorder,
                    focusedTextColor = MovieTextPrimary,
                    unfocusedTextColor = MovieTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Category Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(MovieCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category) },
                        label = {
                            Text(
                                text = if (language == Language.SW) category.nameSw else category.nameFr,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MovieRedPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MovieSurfaceVariant,
                            labelColor = MovieTextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // DJ Filter Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    DjPill(
                        name = if (language == Language.SW) "Wote (All)" else "Tous",
                        isSelected = selectedDj == null,
                        onClick = { onSelectDj(null) }
                    )
                }
                items(availableDjs) { dj ->
                    DjPill(
                        name = dj,
                        isSelected = selectedDj == dj,
                        onClick = { onSelectDj(if (selectedDj == dj) null else dj) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Results Count
        item {
            Text(
                text = if (language == Language.SW)
                    "Matokeo: Filamu ${movies.size} zimepatikana"
                else
                    "Résultats : ${movies.size} films trouvés",
                fontSize = 12.sp,
                color = MovieTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Grid of 2 items
        val chunked = movies.chunked(2)
        items(chunked) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (movie in pair) {
                    Box(modifier = Modifier.weight(1f)) {
                        MovieCard(
                            movie = movie,
                            currentUser = currentUser,
                            language = language,
                            onClick = { onMovieClick(movie) }
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

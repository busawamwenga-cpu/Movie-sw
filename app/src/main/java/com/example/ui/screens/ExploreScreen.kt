package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CloudMovie
import com.example.ui.theme.*

@Composable
fun ExploreScreen(
    movies: List<CloudMovie>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onMovieClick: (CloudMovie) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Search",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MovieTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search movies by title...", fontSize = 13.sp) },
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

            Text(
                text = "Results: ${movies.size} movie(s)",
                fontSize = 12.sp,
                color = MovieTextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

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
                        CloudMovieCard(
                            movie = movie,
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

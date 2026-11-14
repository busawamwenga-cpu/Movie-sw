package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CloudMovie
import com.example.ui.components.CloudMovieThumbnail
import com.example.ui.theme.*

@Composable
fun AlbumScreen(
    allMovies: List<CloudMovie>,
    favoriteIds: Set<String>,
    onMovieClick: (CloudMovie) -> Unit,
    onRemoveFavorite: (CloudMovie) -> Unit
) {
    val savedMovies = remember(allMovies, favoriteIds) {
        allMovies.filter { favoriteIds.contains(it.id) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Text(
                text = "Saved Movies (${savedMovies.size})",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MovieTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (savedMovies.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = MovieTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No saved movies yet.",
                            fontSize = 14.sp,
                            color = MovieTextSecondary
                        )
                    }
                }
            }
        } else {
            items(savedMovies, key = { it.id }) { movie ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onMovieClick(movie) }
                        .testTag("album_item_${movie.id}"),
                    colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 72.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            CloudMovieThumbnail(
                                movie = movie,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MovieTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = movie.formattedDate(),
                                fontSize = 11.sp,
                                color = MovieTextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onMovieClick(movie) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play",
                                tint = MovieRedPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(
                            onClick = { onRemoveFavorite(movie) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove from saved",
                                tint = MovieTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun HomeScreen(
    movies: List<CloudMovie>,
    favoriteIds: Set<String>,
    errorMessage: String?,
    onMovieClick: (CloudMovie) -> Unit,
    onToggleFavorite: (CloudMovie) -> Unit
) {
    val featuredMovie = remember(movies) { movies.firstOrNull() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        if (!errorMessage.isNullOrBlank()) {
            item {
                Surface(
                    color = MovieRedPrimary.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieRedPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = errorMessage,
                        color = MovieRedPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (featuredMovie != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clickable { onMovieClick(featuredMovie) }
                ) {
                    CloudMovieThumbnail(
                        movie = featuredMovie,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MovieDarkBg.copy(alpha = 0.65f),
                                        MovieDarkBg
                                    ),
                                    startY = 80f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            color = MovieRedPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "LATEST MOVIE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = featuredMovie.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MovieTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (featuredMovie.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = featuredMovie.description,
                                fontSize = 12.sp,
                                color = MovieTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onMovieClick(featuredMovie) },
                                colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("hero_watch_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Watch Now", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            val isFav = favoriteIds.contains(featuredMovie.id)
                            OutlinedButton(
                                onClick = { onToggleFavorite(featuredMovie) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MovieSurfaceVariant.copy(alpha = 0.75f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("hero_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    tint = if (isFav) MovieGold else MovieTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFav) "Saved" else "Save",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFav) MovieGold else MovieTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Movies",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = MovieTextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (movies.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = MovieTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No movies yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MovieTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Movies added by an admin will appear here.",
                            fontSize = 13.sp,
                            color = MovieTextSecondary
                        )
                    }
                }
            }
        } else {
            val chunked = movies.chunked(2)
            items(chunked) { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
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
}

@Composable
fun CloudMovieCard(
    movie: CloudMovie,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("movie_card_${movie.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MovieCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
            ) {
                CloudMovieThumbnail(
                    movie = movie,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = movie.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MovieTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movie.formattedDate(),
                        fontSize = 11.sp,
                        color = MovieTextSecondary,
                        maxLines = 1
                    )
                    if (movie.durationSeconds > 0) {
                        Text(
                            text = movie.formattedDuration(),
                            fontSize = 11.sp,
                            color = MovieGoldLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

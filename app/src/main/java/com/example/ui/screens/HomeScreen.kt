package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.components.MoviePosterImage
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun HomeScreen(
    language: Language,
    currentUser: UserAccount?,
    movies: List<MovieItem>,
    availableDjs: List<String>,
    selectedCategory: MovieCategory,
    selectedDj: String?,
    isLoading: Boolean = false,
    onRefresh: () -> Unit = {},
    onSelectCategory: (MovieCategory) -> Unit,
    onSelectDj: (String?) -> Unit,
    onMovieClick: (MovieItem) -> Unit,
    onToggleFavorite: (MovieItem) -> Unit,
    onVipUpgradeClick: () -> Unit
) {
    val featuredMovie = remember(movies) { movies.firstOrNull() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MovieDarkBg),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            if (isLoading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(3.dp),
                        color = MovieGold,
                        trackColor = MovieSurfaceVariant
                    )
                }
            }
            // 1. Featured Hero Banner
            if (featuredMovie != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clickable { onMovieClick(featuredMovie) }
                    ) {
                        // Banner Image
                        MoviePosterImage(
                            movie = featuredMovie,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                    // Cinematic Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MovieDarkBg.copy(alpha = 0.6f),
                                        MovieDarkBg
                                    ),
                                    startY = 100f
                                )
                            )
                    )

                    // Info Content
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
                                text = Strings.featuredBadge(language),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = featuredMovie.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MovieTextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (featuredMovie.djName != null) {
                                Surface(
                                    color = MovieSurfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "DJ: ${featuredMovie.djName}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MovieGoldLight,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Surface(
                                color = MovieSurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = featuredMovie.ageRating,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (featuredMovie.ageRating == "18+") MovieRedPrimary else MovieBlueVip,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = MovieGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${featuredMovie.rating}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MovieTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Watch Now & Add to Album
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onMovieClick(featuredMovie) },
                                colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("hero_watch_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.watchNow(language),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            val isFav = currentUser?.favoritesMovieIds?.contains(featuredMovie.id) == true
                            OutlinedButton(
                                onClick = { onToggleFavorite(featuredMovie) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MovieSurfaceVariant.copy(alpha = 0.7f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("hero_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                                    contentDescription = null,
                                    tint = if (isFav) MovieGold else MovieTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFav) Strings.inAlbum(language) else Strings.addToAlbum(language),
                                    fontSize = 12.sp,
                                    color = if (isFav) MovieGold else MovieTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. VIP Alert Banner for Bure / Expired Users
        if (currentUser?.isVipActive() != true) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(MovieRedDark, MovieSurfaceVariant)
                            )
                        )
                        .clickable { onVipUpgradeClick() }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MovieGoldLight,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = if (language == Language.SW) "Faili za Admin: Zimefungwa (VIP Tu)" else "Fichiers Admin : Verrouillés (Réservé VIP)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (language == Language.SW) "Boresha kuwa VIP Pro kupakua na kufungua zote." else "Passez à VIP Pro pour débloquer.",
                                    fontSize = 11.sp,
                                    color = MovieTextSecondary
                                )
                            }
                        }
                        Button(
                            onClick = onVipUpgradeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "VIP $3.99",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Filter Chips
        item {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MovieRedPrimary else MovieBorder
                            )
                        )
                    }
                }
            }
        }

        // 4. Ma-DJ Filter Row (Dubbed Translators)
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = Strings.djSectionTitle(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MovieTextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
            }
        }

        // 5. Movies Grid / List Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == Language.SW) "Filamu Zinazopatikana" else "Films Disponibles",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MovieTextPrimary
                )
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(34.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MovieGold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MovieGoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Movie cards in rows of 2
        val chunkedMovies = movies.chunked(2)
        items(chunkedMovies) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
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
}

@Composable
fun DjPill(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MovieGold else MovieSurfaceVariant,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MovieGold else MovieBorder),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("dj_pill_$name")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.Black else MovieDarkBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else MovieGoldLight,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else MovieTextPrimary
            )
        }
    }
}

@Composable
fun MovieCard(
    movie: MovieItem,
    currentUser: UserAccount?,
    language: Language,
    onClick: () -> Unit
) {
    val isLocked = movie.isVipOnly && (currentUser?.isVipActive() != true)

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
                    .height(180.dp)
            ) {
                MoviePosterImage(
                    movie = movie,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Age rating badge
                Surface(
                    color = if (movie.ageRating == "18+") MovieRedPrimary else MovieBlueVip,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = movie.ageRating,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                // Lock overlay if VIP Only and user is Free
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "VIP Locked",
                                tint = MovieGold,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = MovieGold,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "VIP ONLY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Rating Pill at Bottom End
                Surface(
                    color = MovieDarkBg.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MovieGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${movie.rating}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieTextPrimary
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = movie.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
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
                    if (movie.djName != null) {
                        Text(
                            text = "DJ: ${movie.djName}",
                            fontSize = 11.sp,
                            color = MovieGoldLight,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = movie.category.nameSw,
                            fontSize = 11.sp,
                            color = MovieTextSecondary
                        )
                    }

                    Text(
                        text = if (movie.isSeries) "Series" else "${movie.durationMinutes}m",
                        fontSize = 10.sp,
                        color = MovieTextSecondary
                    )
                }
            }
        }
    }
}

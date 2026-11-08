package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun AlbumScreen(
    language: Language,
    currentUser: UserAccount?,
    allMovies: List<MovieItem>,
    activeDownloads: Map<String, DownloadTask> = emptyMap(),
    onMovieClick: (MovieItem) -> Unit,
    onRemoveFavorite: (MovieItem) -> Unit,
    onVipUpgradeClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Favorites, 1: Downloads

    val favoriteMovies = remember(currentUser, allMovies) {
        val ids = currentUser?.favoritesMovieIds ?: emptyList()
        allMovies.filter { ids.contains(it.id) }
    }

    val downloadedMovies = remember(currentUser, allMovies) {
        val ids = currentUser?.downloadedMovieIds ?: emptyList()
        allMovies.filter { ids.contains(it.id) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        // Header
        item {
            Text(
                text = Strings.navAlbum(language),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MovieTextPrimary
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Tab Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MovieSurfaceVariant)
                    .padding(4.dp)
            ) {
                // Favorites Tab
                Surface(
                    color = if (selectedTab == 0) MovieRedPrimary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                        .testTag("tab_favorites")
                ) {
                    Text(
                        text = "${Strings.tabFavorites(language)} (${favoriteMovies.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.White else MovieTextSecondary,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // Downloads Tab
                Surface(
                    color = if (selectedTab == 1) MovieRedPrimary else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                        .testTag("tab_downloads")
                ) {
                    Text(
                        text = "${Strings.tabDownloads(language)} (${downloadedMovies.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.White else MovieTextSecondary,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quota Bar (for downloads)
        item {
            val userVip = currentUser?.vipTier ?: VipTier.BURE
            Surface(
                color = MovieCardBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = MovieGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (language == Language.SW) "Hali ya Upakuaji Kwenye Simu" else "Statut des Téléchargements",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MovieTextPrimary
                            )
                        }

                        Surface(
                            color = when (userVip) {
                                VipTier.VIP_PRO_MAX -> MovieGold
                                VipTier.VIP_PRO -> MovieBlueVip
                                VipTier.BURE -> MovieSurfaceVariant
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = userVip.titleSw,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (userVip == VipTier.VIP_PRO_MAX) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    when (userVip) {
                        VipTier.VIP_PRO -> {
                            val actionLeft = (10 - (currentUser?.actionDownloadsThisMonth ?: 0)).coerceAtLeast(0)
                            val seriesLeft = (5 - (currentUser?.seriesDownloadsThisMonth ?: 0)).coerceAtLeast(0)
                            Text(
                                text = Strings.downloadQuotaPro(language, actionLeft, seriesLeft),
                                fontSize = 12.sp,
                                color = MovieGoldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        VipTier.VIP_PRO_MAX -> {
                            Text(
                                text = Strings.downloadQuotaUnlimited(language),
                                fontSize = 12.sp,
                                color = MovieGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        VipTier.BURE -> {
                            Text(
                                text = Strings.downloadQuotaFree(language),
                                fontSize = 12.sp,
                                color = MovieTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = onVipUpgradeClick,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = Strings.upgradeToVip(language),
                                    color = MovieGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Vidmate Downloads Section
        if (selectedTab == 1 && activeDownloads.isNotEmpty()) {
            item {
                Text(
                    text = if (language == Language.SW) "Upakuaji Unaondelea Kwenye Faili la Simu (Vidmate %)" else "Téléchargements en cours (Style Vidmate)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MovieGreenSuccess
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            val inProgressTasks = activeDownloads.values.filter { it.status != DownloadStatus.COMPLETED }
            items(inProgressTasks.toList()) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieGreenSuccess)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(
                                    progress = { task.progress },
                                    modifier = Modifier.size(26.dp),
                                    color = MovieGreenSuccess,
                                    trackColor = MovieSurfaceVariant
                                )
                                Column {
                                    Text(
                                        text = task.movieTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MovieTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${task.downloadedMb.toInt()} MB / ${task.totalMb.toInt()} MB (${task.speed})",
                                        fontSize = 11.sp,
                                        color = MovieGoldLight
                                    )
                                }
                            }

                            Surface(
                                color = MovieGreenSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${task.percentage}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MovieGreenSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { task.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MovieGreenSuccess,
                            trackColor = MovieSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (task.percentage >= 100)
                                (if (language == Language.SW) "Imekamilika kwenye faili la simu!" else "Téléchargement terminé !")
                            else
                                (if (language == Language.SW) "Inapakuliwa kwenye hifadhi ya ndani..." else "Enregistrement en cours..."),
                            fontSize = 10.sp,
                            color = MovieTextSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (language == Language.SW) "Filamu Zilizokamilika Kwenye Kifaa" else "Films Terminés sur l'Appareil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MovieTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // List Content for Active Tab
        val activeList = if (selectedTab == 0) favoriteMovies else downloadedMovies

        if (activeList.isEmpty() && (selectedTab == 0 || activeDownloads.isEmpty())) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.BookmarkBorder else Icons.Default.FileDownloadOff,
                            contentDescription = null,
                            tint = MovieTextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) {
                                if (language == Language.SW) "Bado hujaiweka filamu yoyote kwenye Albamu." else "Aucun film enregistré dans votre album."
                            } else {
                                if (language == Language.SW) "Hakuna filamu zilizopakuliwa kwenye simu bado." else "Aucun film téléchargé sur l'appareil."
                            },
                            fontSize = 13.sp,
                            color = MovieTextSecondary
                        )
                    }
                }
            }
        } else {
            items(activeList) { movie ->
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
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(width = 80.dp, height = 75.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = painterResource(id = movie.posterRes ?: R.drawable.img_onboarding_1),
                                contentDescription = movie.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MovieTextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            if (movie.djName != null) {
                                Text(
                                    text = "DJ: ${movie.djName}",
                                    fontSize = 12.sp,
                                    color = MovieGoldLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "${movie.category.nameSw} • ${movie.durationMinutes} min",
                                fontSize = 11.sp,
                                color = MovieTextSecondary
                            )
                        }

                        // Actions
                        IconButton(
                            onClick = { onMovieClick(movie) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Play",
                                tint = MovieRedPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        IconButton(
                            onClick = { onRemoveFavorite(movie) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove",
                                tint = MovieTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

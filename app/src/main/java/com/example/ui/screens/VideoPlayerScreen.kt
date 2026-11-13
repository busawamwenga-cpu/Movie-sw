package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.*
import com.example.ui.components.MoviePosterImage
import com.example.ui.theme.*
import com.example.util.Strings
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun VideoPlayerScreen(
    movie: MovieItem,
    language: Language,
    currentUser: UserAccount?,
    comments: List<CommentItem>,
    activeDownloads: Map<String, DownloadTask> = emptyMap(),
    onBack: () -> Unit,
    onToggleFavorite: (MovieItem) -> Unit,
    onDownload: (MovieItem) -> Unit,
    onPostComment: (String, Int) -> Unit,
    onVipUpgradeClick: () -> Unit
) {
    val context = LocalContext.current
    val isLocked = movie.isVipOnly && (currentUser?.isVipActive() != true)

    var isPlaying by remember { mutableStateOf(!isLocked) }
    var currentProgressSeconds by remember { mutableIntStateOf(0) }
    var totalSeconds by remember(movie.id) { mutableIntStateOf((movie.durationMinutes * 60).coerceAtLeast(60)) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var newCommentText by remember { mutableStateOf("") }
    var newRating by remember { mutableIntStateOf(5) }
    var selectedEpisode by remember { mutableIntStateOf(1) }

    val activeVideoUrl = remember(movie, selectedEpisode) {
        if (movie.isSeries && movie.episodes.isNotEmpty()) {
            movie.episodes.find { it.episodeNumber == selectedEpisode }?.driveLink ?: movie.videoUrl
        } else {
            movie.videoUrl
        }
    }

    val isRealLocalVideo = remember(activeVideoUrl) {
        if (activeVideoUrl.startsWith("file://")) {
            val path = Uri.parse(activeVideoUrl).path
            !path.isNullOrBlank() && File(path).exists()
        } else {
            false
        }
    }

    var isBuffering by remember(movie.id, selectedEpisode) { mutableStateOf(!isLocked) }
    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    LaunchedEffect(movie.id, selectedEpisode, isLocked, isRealLocalVideo) {
        if (!isLocked && !isRealLocalVideo) {
            isBuffering = true
            delay(500)
            isBuffering = false
        }
    }

    // Simulating player time counter when playing fallback/Drive preview or syncing local VideoView
    LaunchedEffect(isPlaying, isRealLocalVideo, isBuffering) {
        while (isPlaying) {
            delay(1000)
            if (isRealLocalVideo) {
                val vv = videoViewRef
                if (vv != null && vv.isPlaying) {
                    currentProgressSeconds = (vv.currentPosition / 1000).coerceAtLeast(0)
                    val durSec = vv.duration / 1000
                    if (durSec > 0) totalSeconds = durSec
                }
            } else if (!isBuffering) {
                if (currentProgressSeconds < totalSeconds) {
                    currentProgressSeconds++
                } else {
                    isPlaying = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // 1. Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("player_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MovieTextPrimary
                    )
                }

                Text(
                    text = movie.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MovieTextPrimary,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )

                // Current Quality Badge
                Surface(
                    color = MovieSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
                ) {
                    Text(
                        text = (currentUser?.preferredQuality ?: VideoQuality.P360).label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovieGoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 2. Video Player Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color.Black)
            ) {
                if (isLocked) {
                    // VIP Lock Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MovieGold,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = Strings.lockVipOnly(language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MovieTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Strings.lockedNotice(language),
                                fontSize = 12.sp,
                                color = MovieTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onVipUpgradeClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("player_unlock_vip_button")
                            ) {
                                Text(
                                    text = Strings.upgradeToVip(language),
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // Active Player (Real local Android file VideoView or Poster Preview)
                    if (isRealLocalVideo) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    videoViewRef = this
                                    setVideoURI(Uri.parse(activeVideoUrl))
                                    setOnPreparedListener { mp ->
                                        isBuffering = false
                                        val durSec = (mp.duration / 1000).coerceAtLeast(1)
                                        totalSeconds = durSec
                                        if (isPlaying) start()
                                    }
                                    setOnCompletionListener {
                                        isPlaying = false
                                    }
                                    setOnErrorListener { _, _, _ ->
                                        isBuffering = false
                                        true
                                    }
                                }
                            },
                            update = { view ->
                                if (isPlaying && !view.isPlaying && !isBuffering) {
                                    view.start()
                                } else if (!isPlaying && view.isPlaying) {
                                    view.pause()
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        MoviePosterImage(
                            movie = movie,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Dark overlay for controls
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    )

                    if (isBuffering) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(horizontal = 18.dp, vertical = 12.dp)
                                .testTag("player_loading_indicator"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(34.dp),
                                color = MovieGold,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (language == Language.SW) "Inapakia video..." else "Chargement de la vidéo...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Center Play / Pause / Replay Controls
                        Row(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // -10s
                            IconButton(
                                onClick = {
                                    val target = (currentProgressSeconds - 10).coerceAtLeast(0)
                                    currentProgressSeconds = target
                                    if (isRealLocalVideo) videoViewRef?.seekTo(target * 1000)
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Replay10, contentDescription = "-10s", tint = Color.White)
                            }

                            // Play/Pause
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MovieRedPrimary)
                                    .testTag("player_play_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // +10s
                            IconButton(
                                onClick = {
                                    val target = (currentProgressSeconds + 10).coerceAtMost(totalSeconds)
                                    currentProgressSeconds = target
                                    if (isRealLocalVideo) videoViewRef?.seekTo(target * 1000)
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Forward10, contentDescription = "+10s", tint = Color.White)
                            }
                        }
                    }

                    // Bottom Player Controls (Timeline, Timers, Speed)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Slider seekbar
                        Slider(
                            value = currentProgressSeconds.toFloat().coerceIn(0f, totalSeconds.toFloat().coerceAtLeast(1f)),
                            onValueChange = {
                                val target = it.toInt()
                                currentProgressSeconds = target
                                if (isRealLocalVideo) videoViewRef?.seekTo(target * 1000)
                            },
                            valueRange = 0f..totalSeconds.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MovieRedPrimary,
                                activeTrackColor = MovieRedPrimary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentMinutes = currentProgressSeconds / 60
                            val currentSecs = currentProgressSeconds % 60
                            val totalMinutes = totalSeconds / 60
                            val totalSecs = totalSeconds % 60

                            Text(
                                text = "%02d:%02d / %02d:%02d".format(currentMinutes, currentSecs, totalMinutes, totalSecs),
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Speed toggle
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.clickable {
                                        playbackSpeed = when (playbackSpeed) {
                                            1.0f -> 1.25f
                                            1.25f -> 1.5f
                                            1.5f -> 0.75f
                                            else -> 1.0f
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "${playbackSpeed}x",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Movie Details & Fallback Actions
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    // Title & DJ info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = movie.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (movie.djName != null) {
                                    Text(
                                        text = "DJ: ${movie.djName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MovieGoldLight
                                    )
                                }
                                Text(
                                    text = "• ${movie.releaseYear} • ${movie.category.nameSw} • ${movie.ageRating}",
                                    fontSize = 12.sp,
                                    color = MovieTextSecondary
                                )
                            }
                        }

                        // Rating
                        Surface(
                            color = MovieSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MovieGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${movie.rating}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MovieTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Google Drive fallback, Download, Favorite
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Google Drive fallback or APK Local File button
                        val isWebLink = activeVideoUrl.startsWith("http://") || activeVideoUrl.startsWith("https://")
                        Button(
                            onClick = {
                                if (isWebLink) {
                                    runCatching {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(activeVideoUrl))
                                        context.startActivity(intent)
                                    }
                                } else if (isRealLocalVideo) {
                                    isPlaying = true
                                    Toast.makeText(
                                        context,
                                        if (language == Language.SW) "Video hii inacheza moja kwa moja ndani ya APK!" else "Cette vidéo est lue directement dans l'APK !",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (language == Language.SW) "Faili hili liko ndani ya APK." else "Ce fichier est stocké dans l'APK.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MovieSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("btn_open_google_drive")
                        ) {
                            Icon(
                                imageVector = if (isWebLink) Icons.Default.CloudQueue else Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = if (isWebLink) MovieBlueVip else MovieGreenSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isWebLink) {
                                    Strings.openDrive(language)
                                } else if (language == Language.SW) {
                                    "Video Ndani ya APK"
                                } else {
                                    "Vidéo dans l'APK"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieTextPrimary
                            )
                        }

                        // Download Button
                        val isDownloaded = currentUser?.downloadedMovieIds?.contains(movie.id) == true
                        Button(
                            onClick = { onDownload(movie) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDownloaded) MovieGreenSuccess.copy(alpha = 0.2f) else MovieSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isDownloaded) MovieGreenSuccess else MovieBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_download_movie")
                        ) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                contentDescription = null,
                                tint = if (isDownloaded) MovieGreenSuccess else MovieTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDownloaded) Strings.downloaded(language) else Strings.download(language),
                                fontSize = 11.sp,
                                color = if (isDownloaded) MovieGreenSuccess else MovieTextPrimary
                            )
                        }

                        // Favorite / Album toggle
                        val isFav = currentUser?.favoritesMovieIds?.contains(movie.id) == true
                        IconButton(
                            onClick = { onToggleFavorite(movie) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MovieSurfaceVariant)
                                .border(1.dp, MovieBorder, RoundedCornerShape(10.dp))
                                .testTag("btn_toggle_fav_player")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                                contentDescription = "Favorite",
                                tint = if (isFav) MovieGold else MovieTextPrimary
                            )
                        }
                    }

                    // Vidmate Live Download Progress Card
                    val downloadTask = activeDownloads["dl_" + movie.id]
                    if (downloadTask != null && downloadTask.status != DownloadStatus.COMPLETED) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MovieGreenSuccess),
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            progress = { downloadTask.progress },
                                            modifier = Modifier.size(28.dp),
                                            color = MovieGreenSuccess,
                                            trackColor = MovieSurfaceVariant
                                        )
                                        Column {
                                            Text(
                                                text = if (language == Language.SW) "Vidmate Download: ${downloadTask.percentage}%" else "Téléchargement Vidmate : ${downloadTask.percentage}%",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MovieTextPrimary
                                            )
                                            Text(
                                                text = "${downloadTask.downloadedMb.toInt()} MB / ${downloadTask.totalMb.toInt()} MB (${downloadTask.speed})",
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
                                            text = "${downloadTask.percentage}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MovieGreenSuccess,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { downloadTask.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MovieGreenSuccess,
                                    trackColor = MovieSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (language == Language.SW) "Inahifadhiwa kwenye faili la simu yako..." else "Enregistrement dans le stockage de l'appareil...",
                                    fontSize = 10.sp,
                                    color = MovieTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description text
                    Text(
                        text = if (language == Language.SW) movie.descriptionSw else movie.descriptionFr,
                        fontSize = 13.sp,
                        color = MovieTextSecondary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // If it is a series: Episodes List
                    if (movie.isSeries && movie.episodes.isNotEmpty()) {
                        Text(
                            text = Strings.episodes(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MovieTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            movie.episodes.forEach { ep ->
                                val isSelected = ep.episodeNumber == selectedEpisode
                                Surface(
                                    color = if (isSelected) MovieRedPrimary.copy(alpha = 0.2f) else MovieSurfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MovieRedPrimary else MovieBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedEpisode = ep.episodeNumber }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.PlayCircle else Icons.Default.PlayCircleOutline,
                                                contentDescription = null,
                                                tint = if (isSelected) MovieRedPrimary else MovieTextSecondary
                                            )
                                            Column {
                                                Text(
                                                    text = "Ep ${ep.episodeNumber}: ${if (language == Language.SW) ep.titleSw else ep.titleFr}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MovieTextPrimary
                                                )
                                                Text(
                                                    text = "${ep.durationMinutes} min",
                                                    fontSize = 11.sp,
                                                    color = MovieTextSecondary
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                if (ep.driveLink.startsWith("http://") || ep.driveLink.startsWith("https://")) {
                                                    runCatching {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ep.driveLink))
                                                        context.startActivity(intent)
                                                    }
                                                } else {
                                                    selectedEpisode = ep.episodeNumber
                                                    isPlaying = true
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (ep.driveLink.startsWith("http")) Icons.Default.CloudQueue else Icons.Default.PlayArrow,
                                                contentDescription = "Open episode",
                                                tint = if (ep.driveLink.startsWith("http")) MovieBlueVip else MovieGreenSuccess,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Comments Section Header
                    HorizontalDivider(color = MovieDivider)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = Strings.commentsTitle(language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MovieTextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input for new comment
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text(Strings.addCommentHint(language), fontSize = 12.sp) },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newCommentText.isNotBlank()) {
                                        onPostComment(newCommentText, newRating)
                                        newCommentText = ""
                                    }
                                },
                                modifier = Modifier.testTag("btn_post_comment")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Post comment",
                                    tint = MovieRedPrimary
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_comment"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MovieRedPrimary,
                            unfocusedBorderColor = MovieBorder,
                            focusedTextColor = MovieTextPrimary,
                            unfocusedTextColor = MovieTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Filter comments for this movie
                val movieComments = comments.filter { it.movieId == movie.id }

                if (movieComments.isEmpty()) {
                    item {
                        Text(
                            text = if (language == Language.SW) "Kuwa wa kwanza kutoa maoni kwenye filamu hii!" else "Soyez le premier à commenter ce film !",
                            fontSize = 12.sp,
                            color = MovieTextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(movieComments) { comment ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MovieCardBg)
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = comment.userName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MovieTextPrimary
                                        )
                                        if (comment.vipTier != VipTier.BURE) {
                                            Surface(
                                                color = MovieGold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "VIP",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MovieGold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = comment.timestamp,
                                        fontSize = 10.sp,
                                        color = MovieTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.commentText,
                                    fontSize = 12.sp,
                                    color = MovieTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.model.CloudMovie
import com.example.model.CommentItem
import com.example.ui.theme.*

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    movie: CloudMovie,
    isFavorite: Boolean,
    comments: List<CommentItem>,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onPostComment: (String) -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember(movie.id) { mutableStateOf(true) }
    var playerError by remember(movie.id) { mutableStateOf<String?>(null) }
    var showDetailsSheet by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }

    val exoPlayer = remember(movie.videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            if (movie.videoUrl.isNotBlank()) {
                setMediaItem(MediaItem.fromUri(Uri.parse(movie.videoUrl)))
                prepare()
                playWhenReady = true
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    playerError = null
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playerError = "Unable to play this video. Please check your connection."
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("full_screen_player")
    ) {
        if (movie.videoUrl.isBlank()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MovieRedPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Video URL is missing for this movie.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        } else {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    if (view.player !== exoPlayer) {
                        view.player = exoPlayer
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isBuffering && playerError == null && movie.videoUrl.isNotBlank()) {
            CircularProgressIndicator(
                color = MovieGold,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
                    .testTag("player_loading_indicator")
            )
        }

        if (playerError != null) {
            Surface(
                color = MovieSurface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MovieRedPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playerError ?: "",
                        color = MovieTextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            playerError = null
                            isBuffering = true
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary)
                    ) {
                        Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Top Overlay Bar (Back, Title, Save, Info)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .testTag("player_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (movie.description.isNotBlank()) {
                    Text(
                        text = movie.description,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("btn_toggle_fav_player")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                        contentDescription = if (isFavorite) "Remove from Saved" else "Save movie",
                        tint = if (isFavorite) MovieGold else Color.White
                    )
                }

                IconButton(
                    onClick = { showDetailsSheet = !showDetailsSheet },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .testTag("btn_player_info")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Movie details and comments",
                        tint = Color.White
                    )
                }
            }
        }

        if (showDetailsSheet) {
            Surface(
                color = MovieSurface.copy(alpha = 0.96f),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.BottomCenter)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = movie.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { showDetailsSheet = false }) {
                                Text("Close", color = MovieGoldLight)
                            }
                        }
                        if (movie.description.isNotBlank()) {
                            Text(
                                text = movie.description,
                                fontSize = 13.sp,
                                color = MovieTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Uploaded: ${movie.formattedDate()} • Duration: ${movie.formattedDuration()}",
                            fontSize = 11.sp,
                            color = MovieGoldLight
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MovieDivider)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Comments (${comments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MovieTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Write a comment...", fontSize = 12.sp) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            onPostComment(newCommentText.trim())
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (comments.isEmpty()) {
                        item {
                            Text(
                                text = "Be the first to comment on this movie.",
                                fontSize = 12.sp,
                                color = MovieTextSecondary
                            )
                        }
                    } else {
                        items(comments) { c ->
                            Surface(
                                color = MovieCardBg,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = c.userName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MovieGoldLight
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = c.commentText,
                                        fontSize = 13.sp,
                                        color = MovieTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

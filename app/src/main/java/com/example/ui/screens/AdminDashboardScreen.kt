package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.model.CloudMovie
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.example.util.LocalVideoFileHelper
import com.example.util.PickedMediaInfo
import com.example.viewmodel.MovieUploadUiState
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    currentAdminUid: String,
    movies: List<CloudMovie>,
    users: List<UserProfile>,
    uploadState: MovieUploadUiState,
    onUploadMovie: (
        videoUri: Uri,
        fileName: String,
        fileSizeBytes: Long,
        durationSeconds: Int,
        title: String,
        description: String
    ) -> Unit,
    onClearUploadFeedback: () -> Unit,
    onDeleteMovie: (CloudMovie) -> Unit,
    onToggleUserRole: (UserProfile) -> Unit,
    onToggleUserLock: (UserProfile) -> Unit,
    onLockOutdatedUsers: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Upload, 1: Movies, 2: Users
    var pickedMedia by remember { mutableStateOf<PickedMediaInfo?>(null) }
    var movieTitle by remember { mutableStateOf("") }
    var movieDescription by remember { mutableStateOf("") }
    var showPermissionExplanation by remember { mutableStateOf(false) }
    var movieToDelete by remember { mutableStateOf<CloudMovie?>(null) }

    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val openVideoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onClearUploadFeedback()
            pickedMedia = LocalVideoFileHelper.queryPickedFile(context, uri)
            if (movieTitle.isBlank()) {
                val baseName = pickedMedia?.displayName
                    ?.substringBeforeLast('.')
                    ?.replace('_', ' ')
                    ?.replace('-', ' ')
                    ?: ""
                movieTitle = baseName
            }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openVideoPicker.launch(arrayOf("video/*"))
        } else {
            // Document picker (ACTION_OPEN_DOCUMENT) still works via Android Storage Access Framework
            openVideoPicker.launch(arrayOf("video/*"))
        }
    }

    LaunchedEffect(uploadState.successMessage) {
        if (uploadState.successMessage != null) {
            pickedMedia = null
            movieTitle = ""
            movieDescription = ""
        }
    }

    if (showPermissionExplanation) {
        AlertDialog(
            onDismissRequest = { showPermissionExplanation = false },
            containerColor = MovieSurface,
            title = {
                Text("Video Access Permission", color = MovieTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Movie SW needs permission to access videos on your device so you can select a movie file to upload.",
                    color = MovieTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionExplanation = false
                        requestPermissionLauncher.launch(mediaPermission)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionExplanation = false }) {
                    Text("Cancel", color = MovieTextSecondary)
                }
            }
        )
    }

    movieToDelete?.let { pendingMovie ->
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            containerColor = MovieSurface,
            title = {
                Text("Delete movie", color = MovieTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${pendingMovie.title}\"? This removes the movie from Firestore and Storage.",
                    color = MovieTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMovie(pendingMovie)
                        movieToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { movieToDelete = null }) {
                    Text("Cancel", color = MovieTextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg)
    ) {
        // Section Selector Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MovieSurface,
            contentColor = MovieGold
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Text(
                        "Upload movie",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSection == 0) MovieGold else MovieTextSecondary
                    )
                }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    Text(
                        "Manage movies (${movies.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSection == 1) MovieGold else MovieTextSecondary
                    )
                }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = {
                    Text(
                        "Manage users (${users.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSection == 2) MovieGold else MovieTextSecondary
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MovieDarkBg),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedSection) {
                0 -> {
                    item {
                        Surface(
                            color = MovieCardBg,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Upload movie",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MovieTextPrimary
                                )

                                OutlinedButton(
                                    onClick = {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context,
                                            mediaPermission
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            openVideoPicker.launch(arrayOf("video/*"))
                                        } else {
                                            showPermissionExplanation = true
                                        }
                                    },
                                    enabled = !uploadState.isUploading,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieGold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_choose_video")
                                ) {
                                    Icon(Icons.Default.VideoFile, contentDescription = null, tint = MovieGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Choose video file", color = MovieGold, fontWeight = FontWeight.Bold)
                                }

                                pickedMedia?.let { media ->
                                    Surface(
                                        color = MovieSurfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "File: ${media.displayName}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MovieTextPrimary
                                            )
                                            val sizeLabel = if (media.sizeBytes > 0L) {
                                                val mb = media.sizeBytes / (1024f * 1024f)
                                                "%.1f MB".format(Locale.US, mb)
                                            } else {
                                                "%.1f MB".format(Locale.US, media.sizeMb)
                                            }
                                            val durLabel = formatDurationSeconds(media.durationSeconds)
                                            Text(
                                                text = "Size: $sizeLabel • Duration: $durLabel",
                                                fontSize = 12.sp,
                                                color = MovieGoldLight
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = movieTitle,
                                    onValueChange = {
                                        movieTitle = it
                                        onClearUploadFeedback()
                                    },
                                    label = { Text("Movie title") },
                                    singleLine = true,
                                    enabled = !uploadState.isUploading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_movie_title"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = movieDescription,
                                    onValueChange = { movieDescription = it },
                                    label = { Text("Description (optional)") },
                                    minLines = 3,
                                    maxLines = 5,
                                    enabled = !uploadState.isUploading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_movie_description"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                if (uploadState.isUploading) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = uploadState.statusText,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MovieGoldLight
                                        )
                                        LinearProgressIndicator(
                                            progress = { uploadState.progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MovieGold,
                                            trackColor = MovieSurfaceVariant
                                        )
                                    }
                                }

                                uploadState.errorMessage?.let { err ->
                                    Surface(
                                        color = MovieRedPrimary.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MovieRedPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = err,
                                            color = MovieRedPrimary,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }

                                uploadState.successMessage?.let { ok ->
                                    Surface(
                                        color = MovieGreenSuccess.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MovieGreenSuccess),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = ok,
                                            color = MovieGreenSuccess,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        val media = pickedMedia ?: return@Button
                                        onUploadMovie(
                                            media.uri,
                                            media.displayName,
                                            media.sizeBytes.takeIf { it > 0L }
                                                ?: (media.sizeMb * 1024L * 1024L).toLong(),
                                            media.durationSeconds,
                                            movieTitle,
                                            movieDescription
                                        )
                                    },
                                    enabled = !uploadState.isUploading && pickedMedia != null && movieTitle.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_save_movie")
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (uploadState.isUploading) "Uploading..." else "Save movie",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        Text(
                            text = "Manage movies",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieTextPrimary
                        )
                    }
                    if (movies.isEmpty()) {
                        item {
                            Text(
                                text = "No movies uploaded yet.",
                                fontSize = 13.sp,
                                color = MovieTextSecondary
                            )
                        }
                    } else {
                        items(movies, key = { it.id }) { movie ->
                            Surface(
                                color = MovieCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = MovieGoldLight
                                        )
                                        Column {
                                            Text(
                                                text = movie.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MovieTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = movie.formattedDate(),
                                                fontSize = 12.sp,
                                                color = MovieTextSecondary
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = { movieToDelete = movie },
                                        modifier = Modifier.testTag("btn_delete_movie_${movie.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MovieRedPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", color = MovieRedPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Manage users",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieTextPrimary
                            )
                            OutlinedButton(
                                onClick = { onLockOutdatedUsers(BuildConfig.VERSION_CODE) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_lock_outdated_users")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MovieGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock old versions", fontSize = 11.sp, color = MovieGold)
                            }
                        }
                    }

                    if (users.isEmpty()) {
                        item {
                            Text(
                                text = "No users found yet.",
                                fontSize = 13.sp,
                                color = MovieTextSecondary
                            )
                        }
                    } else {
                        items(users, key = { it.uid }) { user ->
                            val isSelf = user.uid == currentAdminUid
                            Surface(
                                color = MovieCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (user.isLocked) MovieRedPrimary.copy(alpha = 0.7f) else MovieBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.People,
                                                contentDescription = null,
                                                tint = if (user.isAdmin) MovieGold else MovieTextSecondary
                                            )
                                            Column {
                                                Text(
                                                    text = user.email.ifBlank { user.displayName },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MovieTextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Role: ${user.role} • App vCode ${user.appVersionCode}" +
                                                        if (user.isLocked) " • Locked" else "",
                                                    fontSize = 11.sp,
                                                    color = if (user.isLocked) MovieRedPrimary else MovieTextSecondary
                                                )
                                            }
                                        }
                                    }

                                    if (!isSelf) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { onToggleUserRole(user) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("btn_switch_role_${user.uid}")
                                            ) {
                                                Text(
                                                    text = if (user.isAdmin) "Set user" else "Set admin",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MovieGoldLight
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = { onToggleUserLock(user) },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(44.dp)
                                                    .testTag("btn_lock_user_${user.uid}")
                                            ) {
                                                Icon(
                                                    imageVector = if (user.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = if (user.isLocked) MovieGreenSuccess else MovieRedPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (user.isLocked) "Unlock" else "Lock",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (user.isLocked) MovieGreenSuccess else MovieRedPrimary
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
        }
    }
}

private fun formatDurationSeconds(durationSeconds: Int): String {
    if (durationSeconds <= 0) return "--:--"
    val mins = durationSeconds / 60
    val secs = durationSeconds % 60
    return if (mins >= 60) {
        val hrs = mins / 60
        val rem = mins % 60
        "%d:%02d:%02d".format(hrs, rem, secs)
    } else {
        "%02d:%02d".format(mins, secs)
    }
}

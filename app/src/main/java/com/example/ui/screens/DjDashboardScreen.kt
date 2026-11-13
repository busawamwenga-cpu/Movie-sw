package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*
import com.example.model.AdminUploadState
import com.example.util.LocalVideoFileHelper
import com.example.util.PickedMediaInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DjDashboardScreen(
    language: Language,
    currentDj: UserAccount?,
    allMovies: List<MovieItem>,
    uploadState: AdminUploadState,
    onLanguageToggle: () -> Unit,
    onAddMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Boolean, Int) -> Unit,
    onStartUploadMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Int) -> Unit,
    onImportPickedVideo: (Uri, String, Float, Uri?, String, MovieCategory, String, String, String, String?, Boolean, Boolean, Int, Boolean) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onDeleteMovie: (String) -> Unit,
    onLogout: () -> Unit
) {
    BackHandler {
        onLogout()
    }

    var djTab by remember { mutableStateOf(0) } // 0: Pakia Filamu, 1: Filamu Zangu
    val djName = currentDj?.djStageName ?: currentDj?.name ?: "DJ"
    val myMovies = remember(allMovies, djName) {
        allMovies.filter { it.djName.equals(djName, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MovieDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .border(1.dp, MovieRedPrimary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(30.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DASHBOARD YA DJ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MovieTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MovieGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieGold)
                                ) {
                                    Text(
                                        text = djName.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MovieGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (language == Language.SW) "Lango Rasmi la Kuweka Filamu" else "Portail de Publication DJ",
                                fontSize = 10.sp,
                                color = MovieTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher
                    Surface(
                        color = MovieSurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clickable { onLanguageToggle() }
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            text = if (language == Language.SW) "SW" else "FR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieGoldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Logout Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("dj_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Ondoka",
                            tint = MovieRedPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MovieSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // DJ Access Banner
            Surface(
                color = MovieSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MovieGreenSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (language == Language.SW)
                                "Access Imeruhusiwa na Admin (Yesu Kishilo)"
                            else
                                "Accès Autorisé par l'Administrateur",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MovieGreenSuccess
                        )
                    }

                    Surface(
                        color = MovieGreenSuccess.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ACCESS ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieGreenSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = djTab,
                containerColor = MovieSurface,
                contentColor = MovieGold
            ) {
                Tab(
                    selected = djTab == 0,
                    onClick = { djTab = 0 },
                    text = {
                        Text(
                            text = if (language == Language.SW) "Pakia Filamu Yako" else "Publier Film",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (djTab == 0) MovieGold else MovieTextSecondary
                        )
                    }
                )
                Tab(
                    selected = djTab == 1,
                    onClick = { djTab = 1 },
                    text = {
                        Text(
                            text = if (language == Language.SW) "Filamu Zangu (${myMovies.size})" else "Mes Films (${myMovies.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (djTab == 1) MovieGold else MovieTextSecondary
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (djTab) {
                    0 -> DjPublishTab(
                        language = language,
                        djName = djName,
                        uploadState = uploadState,
                        onAddMovie = onAddMovie,
                        onStartUploadMovie = onStartUploadMovie,
                        onImportPickedVideo = onImportPickedVideo
                    )
                    1 -> DjMyMoviesTab(
                        language = language,
                        movies = myMovies,
                        onDeleteMovie = onDeleteMovie
                    )
                }
            }
        }
    }
}

@Composable
private fun DjPublishTab(
    language: Language,
    djName: String,
    uploadState: AdminUploadState,
    onAddMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Boolean, Int) -> Unit,
    onStartUploadMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Int) -> Unit,
    onImportPickedVideo: (Uri, String, Float, Uri?, String, MovieCategory, String, String, String, String?, Boolean, Boolean, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(MovieCategory.ACTION) }
    var ageRating by remember { mutableStateOf("10+") }
    var descSw by remember { mutableStateOf("") }
    var descFr by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var isDriveLink by remember { mutableStateOf(false) } // Default: File Upload
    var isSeries by remember { mutableStateOf(false) }
    var episodesCount by remember { mutableStateOf(1) }
    var selectedLocalFileName by remember { mutableStateOf("") }
    var pickedMedia by remember { mutableStateOf<PickedMediaInfo?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val info = LocalVideoFileHelper.queryPickedFile(context, uri)
            pickedMedia = info
            selectedLocalFileName = info.displayName
            if (title.isBlank()) {
                title = info.displayName.substringBeforeLast(".").replace("_", " ")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            color = MovieCardBg,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == Language.SW) "Kuweka Filamu Mpya (Tafsiri ya $djName)" else "Publier un Film Traduit par $djName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MovieTextPrimary
                )
                Text(
                    text = if (language == Language.SW)
                        "Jina lako la DJ litaonekana moja kwa moja kwenye orodha ya filamu na kategoria ya Ma-DJ."
                    else
                        "Votre nom de DJ sera automatiquement attribué au film publié.",
                    fontSize = 12.sp,
                    color = MovieTextSecondary
                )
            }
        }

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(if (language == Language.SW) "Jina la Filamu" else "Titre du Film") },
            placeholder = { Text(if (language == Language.SW) "mfano: Mapigano ya Mwisho" else "ex. Le Dernier Combat") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dj_input_title"),
            shape = RoundedCornerShape(10.dp)
        )

        // DJ Name badge (Locked to this DJ)
        Surface(
            color = MovieSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MovieGold),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (language == Language.SW) "DJ Mtafsiri (Akaunti Yako):" else "DJ Traducteur :",
                        fontSize = 11.sp,
                        color = MovieTextSecondary
                    )
                    Text(
                        text = djName,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MovieGoldLight
                    )
                }
                Surface(
                    color = MovieGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "LOCKED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovieGold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Category Selection
        Text(
            text = if (language == Language.SW) "Aina ya Filamu (Category)" else "Catégorie",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MovieTextPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCat == MovieCategory.ACTION,
                onClick = { selectedCat = MovieCategory.ACTION },
                label = { Text("Action", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedCat == MovieCategory.DRAMA,
                onClick = { selectedCat = MovieCategory.DRAMA },
                label = { Text("Drama", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedCat == MovieCategory.LOVE_STORY,
                onClick = { selectedCat = MovieCategory.LOVE_STORY },
                label = { Text("Love Story", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCat == MovieCategory.CHINESE_DRAMA,
                onClick = { selectedCat = MovieCategory.CHINESE_DRAMA },
                label = { Text("China Drama", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedCat == MovieCategory.KOREAN_DRAMA,
                onClick = { selectedCat = MovieCategory.KOREAN_DRAMA },
                label = { Text("Korea K-Drama", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }

        // Age rating
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == Language.SW) "Umri:" else "Âge :",
                fontSize = 13.sp,
                color = MovieTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            FilterChip(
                selected = ageRating == "10+",
                onClick = { ageRating = "10+" },
                label = { Text("10+ Familia") }
            )
            FilterChip(
                selected = ageRating == "18+",
                onClick = { ageRating = "18+" },
                label = { Text("18+ Watu Wazima") }
            )
        }

        // Description
        OutlinedTextField(
            value = descSw,
            onValueChange = { descSw = it },
            label = { Text(if (language == Language.SW) "Maelezo ya Filamu (Kiswahili)" else "Description en Swahili") },
            modifier = Modifier.fillMaxWidth().testTag("dj_input_desc_sw"),
            shape = RoundedCornerShape(10.dp),
            minLines = 2
        )

        // Upload Mode: File from phone vs Google Drive
        Text(
            text = if (language == Language.SW) "Njia ya Kupakia Video:" else "Mode de Téléversement :",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MovieTextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isDriveLink,
                onClick = { isDriveLink = false },
                label = { Text(if (language == Language.SW) "Faili la Simu (Progress %)" else "Fichier Local (%)") },
                leadingIcon = {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isDriveLink,
                onClick = { isDriveLink = true },
                label = { Text("Google Drive") },
                leadingIcon = {
                    Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }

        if (isDriveLink) {
            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text("Google Drive Link") },
                placeholder = { Text("https://drive.google.com/file/d/...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        } else {
            // Local file selection and % progress bar
            Surface(
                color = MovieSurfaceVariant,
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == Language.SW) "Faili Lililochaguliwa:" else "Fichier Sélectionné :",
                                fontSize = 11.sp,
                                color = MovieTextSecondary
                            )
                            Text(
                                text = pickedMedia?.let { "${it.displayName} (${"%.1f".format(it.sizeMb)} MB)" }
                                    ?: selectedLocalFileName.ifBlank { "video_${title.ifBlank { "movie" }.lowercase().replace(" ", "_")}.mp4" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (pickedMedia != null) MovieGreenSuccess else MovieTextPrimary
                            )
                        }

                        Button(
                            onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                            colors = ButtonDefaults.buttonColors(containerColor = MovieGoldLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Chagua Faili", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uploadState.isUploading) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == Language.SW) "Inapakia kwenye APK..." else "Téléversement en cours...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieGold
                            )
                            Text(
                                text = "${uploadState.percentage}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MovieGoldLight
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uploadState.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MovieGold,
                            trackColor = MovieDarkBg,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uploadState.mbUploaded.toInt()} MB / ${uploadState.totalMb.toInt()} MB (${uploadState.speed})",
                            fontSize = 11.sp,
                            color = MovieTextSecondary
                        )
                    }
                }
            }
        }

        // Series toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (language == Language.SW) "Je, ni Mfululizo (Series)?" else "Est-ce une Série ?",
                fontSize = 14.sp,
                color = MovieTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = isSeries,
                onCheckedChange = { isSeries = it }
            )
        }

        if (isSeries) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (language == Language.SW) "Idadi ya Vipindi:" else "Nombre d'Épisodes :",
                    fontSize = 13.sp,
                    color = MovieTextSecondary
                )
                IconButton(onClick = { if (episodesCount > 1) episodesCount-- }) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MovieGold)
                }
                Text(
                    text = "$episodesCount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovieTextPrimary
                )
                IconButton(onClick = { if (episodesCount < 50) episodesCount++ }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = MovieGold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Publish Button
        Button(
            onClick = {
                if (title.isBlank()) return@Button
                if (isDriveLink) {
                    onAddMovie(
                        title,
                        selectedCat,
                        ageRating,
                        descSw,
                        descFr.ifBlank { descSw },
                        djName,
                        videoUrl.ifBlank { "https://drive.google.com/sample" },
                        true,
                        false,
                        isSeries,
                        episodesCount
                    )
                    title = ""
                    descSw = ""
                    videoUrl = ""
                } else {
                    val picked = pickedMedia
                    if (picked != null) {
                        onImportPickedVideo(
                            picked.uri,
                            picked.displayName,
                            picked.sizeMb,
                            null,
                            title,
                            selectedCat,
                            ageRating,
                            descSw,
                            descFr.ifBlank { descSw },
                            djName,
                            false,
                            isSeries,
                            episodesCount,
                            true
                        )
                        title = ""
                        descSw = ""
                        pickedMedia = null
                        selectedLocalFileName = ""
                    } else {
                        onStartUploadMovie(
                            title,
                            selectedCat,
                            ageRating,
                            descSw,
                            descFr.ifBlank { descSw },
                            djName,
                            selectedLocalFileName.ifBlank { "${title.lowercase().replace(" ", "_")}.mp4" },
                            false,
                            isSeries,
                            episodesCount
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("dj_btn_publish_movie"),
            colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
            shape = RoundedCornerShape(12.dp),
            enabled = !uploadState.isUploading && title.isNotBlank()
        ) {
            if (uploadState.isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.Black
                )
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (uploadState.isUploading)
                    (if (language == Language.SW) "Inapakia... (${uploadState.percentage}%)" else "Téléversement... (${uploadState.percentage}%)")
                else if (language == Language.SW) "Chapisha Filamu ya $djName" else "Publier le Film de $djName",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DjMyMoviesTab(
    language: Language,
    movies: List<MovieItem>,
    onDeleteMovie: (String) -> Unit
) {
    if (movies.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null,
                    tint = MovieTextSecondary,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (language == Language.SW) "Bado hujapakia filamu yoyote." else "Aucun film publié pour le moment.",
                    color = MovieTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(movies, key = { it.id }) { movie ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
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
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MovieSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = MovieGoldLight)
                            }
                            Column {
                                Text(
                                    text = movie.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MovieTextPrimary
                                )
                                Text(
                                    text = "${movie.category.nameSw} • ${movie.ageRating} • ${movie.viewsCount} views",
                                    fontSize = 11.sp,
                                    color = MovieTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteMovie(movie.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Futa",
                                tint = MovieRedPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MovieRepository
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.AppNotificationHelper
import com.example.util.LocalVideoFileHelper
import com.example.util.PickedMediaInfo
import com.example.util.Strings

@Composable
fun AdminDashboardScreen(
    language: Language,
    users: List<UserAccount>,
    movies: List<MovieItem>,
    socialLinks: List<SocialLink>,
    adminUploadState: AdminUploadState,
    isActionLoading: Boolean = false,
    onAddMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Boolean, Int, Boolean) -> Unit,
    onStartUploadMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Int, Boolean) -> Unit,
    onImportPickedVideo: (Uri, String, Float, Uri?, String, MovieCategory, String, String, String, String?, Boolean, Boolean, Int, Boolean) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onDeleteMovie: (String) -> Unit,
    onChangeUserVip: (String, VipTier) -> Unit,
    onDeleteUser: (String) -> Unit,
    onBroadcastNotif: (String, String, String, String) -> Unit,
    onCreateDjAccount: (String, String, String, String, String, Boolean) -> Unit,
    onToggleDjAccess: (String, Boolean) -> Unit,
    onCancelUserVip: (String) -> Unit,
    onRenewUserVip: (String, VipTier) -> Unit,
    onLanguageToggle: () -> Unit,
    onExitAdmin: () -> Unit
) {
    var adminTab by remember { mutableIntStateOf(0) } // 0: Publish, 1: Arifa & Mitandao, 2: Manage Movies, 3: Customers, 4: Ma-DJ & Access, 5: Analytics

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
            // Top Bar
            Surface(
                color = MovieSurface,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onExitAdmin,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Admin",
                                tint = MovieGold
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ADMIN",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MovieGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DASHBOARD",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MovieTextPrimary
                                )
                            }
                            Text(
                                text = "Movie SW Central Console",
                                fontSize = 10.sp,
                                color = MovieTextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Language Toggle
                        Surface(
                            color = MovieSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .clickable { onLanguageToggle() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (language == Language.SW) "SW" else "FR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieGoldLight
                            )
                        }

                        // Exit button
                        Button(
                            onClick = onExitAdmin,
                            colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = Strings.btnExitAdmin(language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tab Bar
            ScrollableTabRow(
                selectedTabIndex = adminTab,
                containerColor = MovieSurfaceVariant,
                contentColor = MovieGold,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = adminTab == 0,
                    onClick = { adminTab = 0 },
                    text = {
                        Text(
                            text = Strings.tabPublish(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminTab == 0) MovieGold else MovieTextSecondary
                        )
                    }
                )
                Tab(
                    selected = adminTab == 1,
                    onClick = { adminTab = 1 },
                    text = {
                        Text(
                            text = if (language == Language.SW) "Tuma Arifa" else "Notifications",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminTab == 1) MovieGold else MovieTextSecondary
                        )
                    }
                )
                Tab(
                    selected = adminTab == 2,
                    onClick = { adminTab = 2 },
                    text = {
                        Text(
                            text = "${Strings.tabManageMovies(language)} (${movies.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminTab == 2) MovieGold else MovieTextSecondary
                        )
                    }
                )
                Tab(
                    selected = adminTab == 3,
                    onClick = { adminTab = 3 },
                    text = {
                        Text(
                            text = "${Strings.tabCustomers(language)} (${users.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminTab == 3) MovieGold else MovieTextSecondary
                        )
                    }
                )
                val djCount = remember(users) { users.count { it.role == UserRole.DJ } }
                Tab(
                    selected = adminTab == 4,
                    onClick = { adminTab = 4 },
                    text = {
                        Text(
                            text = if (language == Language.SW) "Ma-DJ & Access ($djCount)" else "DJs & Accès ($djCount)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminTab == 4) MovieGold else MovieTextSecondary
                        )
                    }
                )
                Tab(
                    selected = adminTab == 5,
                    onClick = { adminTab = 5 },
                    text = {
                        Text(
                            text = Strings.tabStats(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (adminTab == 5) MovieGold else MovieTextSecondary
                        )
                    }
                )
            }

            // Tab Contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (adminTab) {
                    0 -> AdminPublishTab(language, adminUploadState, onAddMovie, onStartUploadMovie, onImportPickedVideo)
                    1 -> AdminSocialAndNotifsTab(language, socialLinks, isActionLoading, onBroadcastNotif)
                    2 -> AdminManageMoviesTab(language, movies, onDeleteMovie)
                    3 -> AdminCustomersTab(language, users, onChangeUserVip, onCancelUserVip, onRenewUserVip, onDeleteUser)
                    4 -> AdminDjAccountsTab(language, users, onCreateDjAccount, onToggleDjAccess, onDeleteUser)
                    5 -> AdminAnalyticsTab(language, movies)
                }
            }
        }
    }
}

// 1. Kuchapisha Filamu na Mfululizo (Kwa Faili Halisi la Simu Android -> APK & Arifa)
@Composable
private fun AdminPublishTab(
    language: Language,
    uploadState: AdminUploadState,
    onAddMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Boolean, Int, Boolean) -> Unit,
    onStartUploadMovie: (String, MovieCategory, String, String, String, String?, String, Boolean, Boolean, Int, Boolean) -> Unit,
    onImportPickedVideo: (Uri, String, Float, Uri?, String, MovieCategory, String, String, String, String?, Boolean, Boolean, Int, Boolean) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(MovieCategory.ACTION) }
    var ageRating by remember { mutableStateOf("10+") }
    var djName by remember { mutableStateOf("DJ Afro") }
    var descSw by remember { mutableStateOf("") }
    var descFr by remember { mutableStateOf("") }
    var uploadMode by remember { mutableIntStateOf(0) } // 0: Faili la Simu (Phone File to APK), 1: Google Drive link
    var pickedMedia by remember { mutableStateOf<PickedMediaInfo?>(null) }
    var pickedPosterUri by remember { mutableStateOf<Uri?>(null) }
    var localFileName by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }
    var isVipOnly by remember { mutableStateOf(true) }
    var isSeries by remember { mutableStateOf(false) }
    var episodesCount by remember { mutableIntStateOf(3) }
    var sendNotification by remember { mutableStateOf(true) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val info = LocalVideoFileHelper.queryPickedFile(context, uri)
            pickedMedia = info
            localFileName = info.displayName
            if (title.isBlank()) {
                title = info.displayName.substringBeforeLast(".").replace("_", " ")
            }
        }
    }

    val posterPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedPosterUri = uri
        }
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = if (language == Language.SW) "Chapisha Filamu (Kutoka Faili la Simu au Drive)" else "Publier un Film (Fichier Local ou Drive)",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MovieTextPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Upload Mode Switcher (Faili la Simu vs Google Drive)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MovieSurfaceVariant)
                .padding(4.dp)
        ) {
            Surface(
                color = if (uploadMode == 0) MovieGold else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { uploadMode = 0 }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (uploadMode == 0) Color.Black else MovieTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == Language.SW) "Faili la Simu -> APK" else "Fichier Local -> APK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uploadMode == 0) Color.Black else MovieTextSecondary
                    )
                }
            }

            Surface(
                color = if (uploadMode == 1) MovieGold else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { uploadMode = 1 }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (uploadMode == 1) Color.Black else MovieTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == Language.SW) "Kiungo cha Drive" else "Lien Google Drive",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uploadMode == 1) Color.Black else MovieTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Upload Progress Card (when uploading into APK)
        if (uploadState.isUploading) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MovieGold),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(
                                progress = { uploadState.progress },
                                modifier = Modifier.size(32.dp),
                                color = MovieGold,
                                trackColor = MovieSurfaceVariant
                            )
                            Column {
                                Text(
                                    text = if (language == Language.SW) "Inapakia Ndani ya APK..." else "Téléversement dans l'APK...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MovieTextPrimary
                                )
                                Text(
                                    text = uploadState.fileName,
                                    fontSize = 11.sp,
                                    color = MovieGoldLight
                                )
                            }
                        }

                        Text(
                            text = "${uploadState.percentage}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MovieGold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { uploadState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MovieGold,
                        trackColor = MovieSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${uploadState.mbUploaded.toInt()} MB / ${uploadState.totalMb.toInt()} MB",
                            fontSize = 11.sp,
                            color = MovieTextSecondary
                        )
                        Text(
                            text = "Kasi: ${uploadState.speed}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieGreenSuccess
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = uploadState.stageText,
                        fontSize = 11.sp,
                        color = MovieTextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(if (language == Language.SW) "Jina la Filamu" else "Titre du Film") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("admin_input_title"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // File selection from Android phone storage (if uploadMode == 0)
        if (uploadMode == 0) {
            Surface(
                color = MovieCardBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == Language.SW) "Chagua Video Kutoka Kwenye Simu Yako (Android):" else "Sélectionner une Vidéo Android :",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovieTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = MovieBlueVip),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("admin_btn_pick_video")
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == Language.SW) "Fungua Faili za Simu (Chagua Video)" else "Choisir une Vidéo sur le Téléphone",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (pickedMedia != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MovieGreenSuccess.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MovieGreenSuccess),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MovieGreenSuccess, modifier = Modifier.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = pickedMedia!!.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MovieTextPrimary
                                    )
                                    Text(
                                        text = "${"%.1f".format(pickedMedia!!.sizeMb)} MB • Iko tayari kupakiwa kwenye APK",
                                        fontSize = 10.sp,
                                        color = MovieGreenSuccess
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = localFileName,
                            onValueChange = { localFileName = it },
                            label = { Text(if (language == Language.SW) "Au andika jina la faili (.mp4, .mkv)" else "Ou nom du fichier (.mp4, .mkv)") },
                            leadingIcon = {
                                Icon(Icons.Default.VideoFile, contentDescription = null, tint = MovieGoldLight)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                posterPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = MovieGoldLight, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (pickedPosterUri != null)
                                    (if (language == Language.SW) "Jalada Limechaguliwa" else "Affiche Choisie")
                                else
                                    (if (language == Language.SW) "Picha ya Jalada (Hiari)" else "Affiche (Optionnel)"),
                                fontSize = 11.sp,
                                color = MovieGoldLight
                            )
                        }
                        if (pickedPosterUri != null) {
                            AsyncImage(
                                model = pickedPosterUri,
                                contentDescription = "Selected Poster",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        } else {
            // Google Drive URL
            OutlinedTextField(
                value = videoUrl,
                onValueChange = { videoUrl = it },
                label = { Text(if (language == Language.SW) "Kiungo cha Video / Google Drive" else "Lien de la Vidéo / Google Drive") },
                placeholder = { Text("https://drive.google.com/file/d/...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_input_url"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DJ Name
        OutlinedTextField(
            value = djName,
            onValueChange = { djName = it },
            label = { Text(if (language == Language.SW) "Jina la DJ aliyetafsiri (mfano DJ Afro)" else "Nom du DJ traducteur") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("admin_input_dj"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category selection chips
        Text(
            text = if (language == Language.SW) "Aina ya Filamu:" else "Catégorie :",
            fontSize = 12.sp,
            color = MovieTextSecondary,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(MovieCategory.ACTION, MovieCategory.DRAMA, MovieCategory.LOVE_STORY, MovieCategory.KOREAN_DRAMA, MovieCategory.CHINESE_DRAMA).forEach { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat },
                    label = { Text(if (language == Language.SW) cat.nameSw else cat.nameFr, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MovieRedPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Age Rating (10+ vs 18+)
        Text(
            text = if (language == Language.SW) "Kikomo cha Umri:" else "Classification par Âge :",
            fontSize = 12.sp,
            color = MovieTextSecondary,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = ageRating == "10+",
                onClick = { ageRating = "10+" },
                label = { Text("10+ (Familia)") }
            )
            FilterChip(
                selected = ageRating == "18+",
                onClick = { ageRating = "18+" },
                label = { Text("18+ (Watu Wazima)") }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Swahili description
        OutlinedTextField(
            value = descSw,
            onValueChange = { descSw = it },
            label = { Text("Maelezo kwa Kiswahili") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // French description
        OutlinedTextField(
            value = descFr,
            onValueChange = { descFr = it },
            label = { Text("Description en Français") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Switch: VIP Only (Admin File)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MovieCardBg)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (language == Language.SW) "Faili la Admin (Inahitaji VIP tu)" else "Fichier Admin (Réservé VIP)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MovieTextPrimary
                )
                Text(
                    text = if (language == Language.SW) "Itaonekana na kufuli kwa wateja wa bure" else "Verrouillé pour les utilisateurs gratuits",
                    fontSize = 11.sp,
                    color = MovieTextSecondary
                )
            }
            Switch(
                checked = isVipOnly,
                onCheckedChange = { isVipOnly = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MovieGold)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Switch: Is Series
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MovieCardBg)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (language == Language.SW) "Ni Mfululizo (Series yenye vipindi)" else "Est une Série (avec épisodes)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MovieTextPrimary
                )
                if (isSeries) {
                    Text(
                        text = "Vipindi: $episodesCount",
                        fontSize = 11.sp,
                        color = MovieGoldLight
                    )
                }
            }
            Switch(
                checked = isSeries,
                onCheckedChange = { isSeries = it }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Switch: Auto-send Notification to all viewers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MovieCardBg)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == Language.SW) "Tuma Arifa (Notification) kwa Wote" else "Notifier Tous les Utilisateurs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MovieGoldLight
                )
                Text(
                    text = if (language == Language.SW) "Wateja watapata arifa mara filamu ikipakiwa" else "Envoie une alerte dès la publication",
                    fontSize = 11.sp,
                    color = MovieTextSecondary
                )
            }
            Switch(
                checked = sendNotification,
                onCheckedChange = { sendNotification = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MovieGreenSuccess)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button (Upload real Android file with % progress or Drive publish)
        Button(
            onClick = {
                if (sendNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !AppNotificationHelper.hasNotificationPermission(context)
                ) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (title.isNotBlank()) {
                    val swDesc = descSw.ifBlank { "Filamu iliyopakiwa kutoka kwenye simu na kutafsiriwa na $djName" }
                    val frDesc = descFr.ifBlank { "Film téléversé depuis le fichier local et traduit par $djName" }
                    if (uploadMode == 0) {
                        val picked = pickedMedia
                        if (picked != null) {
                            onImportPickedVideo(
                                picked.uri,
                                picked.displayName,
                                picked.sizeMb,
                                pickedPosterUri,
                                title,
                                selectedCat,
                                ageRating,
                                swDesc,
                                frDesc,
                                djName,
                                isVipOnly,
                                isSeries,
                                episodesCount,
                                sendNotification
                            )
                        } else {
                            onStartUploadMovie(
                                title,
                                selectedCat,
                                ageRating,
                                swDesc,
                                frDesc,
                                djName,
                                localFileName.ifBlank { "${title.replace(" ", "_")}.mp4" },
                                isVipOnly,
                                isSeries,
                                episodesCount,
                                sendNotification
                            )
                        }
                    } else {
                        onAddMovie(
                            title,
                            selectedCat,
                            ageRating,
                            descSw.ifBlank { "Filamu ya kiungo cha mtandao iliyotafsiriwa na $djName" },
                            descFr.ifBlank { "Film en ligne avec doublage par $djName" },
                            djName,
                            videoUrl.ifBlank { "https://drive.google.com/file/d/sample/view" },
                            true,
                            isVipOnly,
                            isSeries,
                            episodesCount,
                            sendNotification
                        )
                    }
                    title = ""
                    pickedMedia = null
                    pickedPosterUri = null
                    localFileName = ""
                }
            },
            enabled = !uploadState.isUploading && title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("admin_btn_publish"),
            colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
            shape = RoundedCornerShape(12.dp)
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
                else if (uploadMode == 0)
                    (if (language == Language.SW) "Pakia Faili Kwenye APK (Onyesha %)" else "Téléverser dans l'APK (avec %)")
                else
                    (if (language == Language.SW) "Chapisha kwa Kiungo cha Drive" else "Publier le Lien Drive"),
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp
            )
        }
    }
}

// 2. Kusimamia na Kufuta Filamu Kwenye App (Admin Exclusive)
@Composable
private fun AdminManageMoviesTab(
    language: Language,
    movies: List<MovieItem>,
    onDeleteMovie: (String) -> Unit
) {
    var movieToDelete by remember { mutableStateOf<MovieItem?>(null) }

    if (movieToDelete != null) {
        val movie = movieToDelete!!
        AlertDialog(
            onDismissRequest = { movieToDelete = null },
            title = {
                Text(
                    text = Strings.btnDeleteMovie(language),
                    fontWeight = FontWeight.Bold,
                    color = MovieRedPrimary
                )
            },
            text = {
                Text(
                    text = Strings.confirmDeleteMovie(language, movie.title),
                    color = MovieTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMovie(movie.id)
                        movieToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary)
                ) {
                    Text(if (language == Language.SW) "Ndio, Futa Filamu" else "Oui, Supprimer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { movieToDelete = null }) {
                    Text(if (language == Language.SW) "Ghairi" else "Annuler")
                }
            },
            containerColor = MovieSurfaceVariant
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = if (language == Language.SW) "Filamu Zilizopo Kwenye App (${movies.size})" else "Films sur l'Application (${movies.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MovieTextPrimary
            )
            Text(
                text = if (language == Language.SW) "Admin pekee ndiye anayeweza kuweka au kufuta filamu zilizopo kwenye app." else "Seul l'administrateur a le droit d'ajouter ou de supprimer des films.",
                fontSize = 12.sp,
                color = MovieTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(movies) { movie ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = if (movie.isVipOnly) MovieGold.copy(alpha = 0.2f) else MovieBlueVip.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (movie.isVipOnly) "VIP" else "FREE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (movie.isVipOnly) MovieGold else MovieBlueVip,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        Column {
                            Text(
                                text = movie.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MovieTextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "DJ: ${movie.djName ?: "Original"} • ${movie.category.nameSw} • ${movie.ageRating}",
                                fontSize = 11.sp,
                                color = MovieTextSecondary
                            )
                            Text(
                                text = if (movie.isDriveLink) "Google Drive" else "Faili la Simu (APK)",
                                fontSize = 10.sp,
                                color = MovieGoldLight
                            )
                        }
                    }

                    Button(
                        onClick = { movieToDelete = movie },
                        colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Strings.btnDeleteMovie(language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// 3. Orodha ya Wateja (Admin Exclusive)
@Composable
private fun AdminCustomersTab(
    language: Language,
    users: List<UserAccount>,
    onChangeUserVip: (String, VipTier) -> Unit,
    onCancelUserVip: (String) -> Unit,
    onRenewUserVip: (String, VipTier) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var userToDelete by remember { mutableStateOf<UserAccount?>(null) }

    if (userToDelete != null) {
        val u = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Text(
                    text = Strings.btnDeleteUser(language),
                    fontWeight = FontWeight.Bold,
                    color = MovieRedPrimary
                )
            },
            text = {
                Text(
                    text = Strings.confirmDeleteUser(language, u.name),
                    color = MovieTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(u.id)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary)
                ) {
                    Text(if (language == Language.SW) "Ndio, Futa Mteja" else "Oui, Supprimer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text(if (language == Language.SW) "Ghairi" else "Annuler")
                }
            },
            containerColor = MovieSurfaceVariant
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = if (language == Language.SW) "Wateja Waliojisajili (${users.size})" else "Clients Enregistrés (${users.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MovieTextPrimary
            )
            Text(
                text = if (language == Language.SW) "Unaweza kubadilisha kiwango cha mteja (Bure au VIP Pro) au kumfuta." else "Gérez les forfaits VIP Pro ou supprimez des utilisateurs.",
                fontSize = 12.sp,
                color = MovieTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(users) { user ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MovieSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = MovieTextPrimary
                                )
                            }
                            Column {
                                Text(
                                    text = user.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MovieTextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "${user.emailOrPhone} • ${user.country}",
                                        fontSize = 11.sp,
                                        color = MovieTextSecondary
                                    )
                                    if (user.isEmailVerified) {
                                        Surface(
                                            color = MovieGreenSuccess.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MovieGreenSuccess)
                                        ) {
                                            Text(
                                                text = "VÉRIFIÉ",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MovieGreenSuccess,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            color = MovieGold.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MovieGold)
                                        ) {
                                            Text(
                                                text = "NON VÉRIFIÉ",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MovieGold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Delete button
                        if (user.role != UserRole.ADMIN) {
                            Button(
                                onClick = { userToDelete = user },
                                colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary.copy(alpha = 0.85f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Strings.btnDeleteUser(language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Role + VIP expiry status row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (user.role) {
                                UserRole.ADMIN -> MovieGold.copy(alpha = 0.2f)
                                UserRole.DJ -> MovieBlueVip.copy(alpha = 0.2f)
                                UserRole.VIEWER -> MovieSurfaceVariant
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when (user.role) {
                                    UserRole.ADMIN -> "ADMIN"
                                    UserRole.DJ -> "DJ: ${user.djStageName ?: user.name}"
                                    UserRole.VIEWER -> "VIEWER"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (user.role) {
                                    UserRole.ADMIN -> MovieGold
                                    UserRole.DJ -> MovieBlueVip
                                    UserRole.VIEWER -> MovieTextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (user.role != UserRole.ADMIN) {
                            val activeVip = user.isVipActive()
                            Surface(
                                color = if (activeVip) MovieGreenSuccess.copy(alpha = 0.16f) else MovieSurfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (activeVip) {
                                        if (language == Language.SW)
                                            "VIP Mwezi 1: Siku ${user.daysRemaining()} zimebaki"
                                        else
                                            "VIP 1 Mois: ${user.daysRemaining()}j restants"
                                    } else {
                                        if (language == Language.SW) "Bure (Haina VIP)" else "Gratuit (Expiré)"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (activeVip) MovieGreenSuccess else MovieTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MovieSurfaceVariant,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Umri: ${user.ageTier.label} | Downloads: ${user.downloadedMovieIds.size}",
                                fontSize = 10.sp,
                                color = MovieTextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // VIP Tier selector (1-month activation or revoke back to Bure)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            VipTier.values().forEach { tier ->
                                val isSelected = if (tier == VipTier.BURE) !user.isVipActive() else (user.isVipActive() && user.vipTier == tier)
                                Surface(
                                    color = if (isSelected) {
                                        when (tier) {
                                            VipTier.VIP_PRO -> MovieGold
                                            VipTier.BURE -> MovieSurfaceVariant
                                        }
                                    } else MovieDarkBg,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                                    modifier = Modifier.clickable {
                                        when (tier) {
                                            VipTier.BURE -> onCancelUserVip(user.id)
                                            else -> onRenewUserVip(user.id, tier)
                                        }
                                    }
                                ) {
                                    Text(
                                        text = when (tier) {
                                            VipTier.VIP_PRO -> "VIP Pro 30d"
                                            VipTier.BURE -> "Bure"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected && tier == VipTier.VIP_PRO) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

// 3b. Akaunti za Ma-DJ na Udhibiti wa Access ya Dashboard (Admin Exclusive)
@Composable
private fun AdminDjAccountsTab(
    language: Language,
    users: List<UserAccount>,
    onCreateDjAccount: (String, String, String, String, String, Boolean) -> Unit,
    onToggleDjAccess: (String, Boolean) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    val djUsers = remember(users) { users.filter { it.role == UserRole.DJ } }
    var fullName by remember { mutableStateOf("") }
    var stageName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("RD Congo (RDC)") }
    var password by remember { mutableStateOf("") }
    var grantAccess by remember { mutableStateOf(true) }
    var djToDelete by remember { mutableStateOf<UserAccount?>(null) }

    if (djToDelete != null) {
        val target = djToDelete!!
        AlertDialog(
            onDismissRequest = { djToDelete = null },
            title = {
                Text(
                    text = if (language == Language.SW) "Futa Akaunti ya DJ" else "Supprimer le compte DJ",
                    fontWeight = FontWeight.Bold,
                    color = MovieRedPrimary
                )
            },
            text = {
                Text(
                    text = if (language == Language.SW)
                        "Una uhakika unataka kufuta akaunti ya ${target.djStageName ?: target.name}?"
                    else
                        "Voulez-vous vraiment supprimer le compte DJ ${target.djStageName ?: target.name} ?",
                    color = MovieTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(target.id)
                        djToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary)
                ) {
                    Text(if (language == Language.SW) "Futa" else "Supprimer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { djToDelete = null }) {
                    Text(if (language == Language.SW) "Ghairi" else "Annuler")
                }
            },
            containerColor = MovieSurfaceVariant
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Create DJ Account Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieGold)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = MovieGold)
                        Text(
                            text = if (language == Language.SW)
                                "Unda Akaunti Mpya ya DJ (Kuweka Filamu Zao)"
                            else
                                "Créer un Compte DJ (Publication de Films)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MovieTextPrimary
                        )
                    }

                    Text(
                        text = if (language == Language.SW)
                            "Admin anatengeneza akaunti ya DJ na anaamua kumpa au kumnyima access ya kuingia kwenye Dashboard."
                        else
                            "L'administrateur crée le compte DJ et contrôle son accès au tableau de bord.",
                        fontSize = 11.sp,
                        color = MovieTextSecondary
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text(if (language == Language.SW) "Jina Kamili la DJ" else "Nom complet du DJ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_dj_input_name"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = stageName,
                        onValueChange = { stageName = it },
                        label = { Text(if (language == Language.SW) "Jina la Kisanii (mfano: DJ Mwanga)" else "Nom de scène (ex: DJ Mwanga)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_dj_input_stage"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Gmail / Barua pepe ya DJ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_dj_input_email"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text(if (language == Language.SW) "Nchi" else "Pays") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (language == Language.SW) "Nywila (6+)" else "Mot de passe") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("admin_dj_input_password"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MovieSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == Language.SW) "Mpe Access ya Dashboard" else "Autoriser l'accès Dashboard",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieTextPrimary
                            )
                            Text(
                                text = if (grantAccess)
                                    (if (language == Language.SW) "Ataweza kuingia na kupakia filamu zake" else "Pourra se connecter et publier")
                                else
                                    (if (language == Language.SW) "Akaunti itaundwa bila access hadi umruhusu" else "Compte bloqué jusqu'à activation"),
                                fontSize = 10.sp,
                                color = MovieTextSecondary
                            )
                        }
                        Switch(
                            checked = grantAccess,
                            onCheckedChange = { grantAccess = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MovieGreenSuccess)
                        )
                    }

                    Button(
                        onClick = {
                            onCreateDjAccount(fullName, email, password, stageName, country, grantAccess)
                            if (fullName.isNotBlank() && email.isNotBlank() && password.length >= 6 && stageName.isNotBlank()) {
                                fullName = ""
                                stageName = ""
                                email = ""
                                password = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_btn_create_dj"),
                        colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == Language.SW) "Unda Akaunti ya DJ" else "Créer le Compte DJ",
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = if (language == Language.SW)
                    "Ma-DJ Waliosajiliwa & Udhibiti wa Access (${djUsers.size})"
                else
                    "DJs Enregistrés & Contrôle d'Accès (${djUsers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MovieTextPrimary
            )
        }

        items(djUsers, key = { it.id }) { dj ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (dj.hasDashboardAccess) MovieGreenSuccess.copy(alpha = 0.6f) else MovieRedPrimary.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dj.djStageName ?: dj.name,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MovieGoldLight
                                )
                                Surface(
                                    color = if (dj.hasDashboardAccess)
                                        MovieGreenSuccess.copy(alpha = 0.2f)
                                    else
                                        MovieRedPrimary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (dj.hasDashboardAccess) "ACCESS ACTIVE" else "ACCESS BLOQUÉE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dj.hasDashboardAccess) MovieGreenSuccess else MovieRedPrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${dj.name} • ${dj.emailOrPhone} • ${dj.country}",
                                fontSize = 11.sp,
                                color = MovieTextSecondary
                            )
                        }

                        IconButton(
                            onClick = { djToDelete = dj },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete DJ",
                                tint = MovieRedPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MovieSurfaceVariant)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (dj.hasDashboardAccess)
                                (if (language == Language.SW) "Ruhusa ya Kuweka Filamu: IMEWASHWA" else "Accès Publication : ACTIVÉ")
                            else
                                (if (language == Language.SW) "Ruhusa ya Kuweka Filamu: IMEZIMWA" else "Accès Publication : BLOQUÉ"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dj.hasDashboardAccess) MovieGreenSuccess else MovieRedPrimary
                        )

                        Switch(
                            checked = dj.hasDashboardAccess,
                            onCheckedChange = { onToggleDjAccess(dj.id, it) },
                            modifier = Modifier.testTag("admin_dj_access_switch_${dj.id}"),
                            colors = SwitchDefaults.colors(checkedThumbColor = MovieGreenSuccess)
                        )
                    }
                }
            }
        }
    }
}

// 3. Takwimu (Analytics)
@Composable
private fun AdminAnalyticsTab(
    language: Language,
    movies: List<MovieItem>
) {
    val stats = MovieRepository.countryStats

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (language == Language.SW) "Takwimu za Movie SW" else "Statistiques de Movie SW",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MovieTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Active Users Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == Language.SW) "Watazamaji Leo" else "Spectateurs Aujourd'hui",
                            fontSize = 11.sp,
                            color = MovieTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1,420",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MovieGoldLight
                        )
                        Text(
                            text = "+18% ikilinganishwa na jana",
                            fontSize = 9.sp,
                            color = MovieGreenSuccess
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == Language.SW) "Watazamaji Wiki Hii" else "Spectateurs cette Semaine",
                            fontSize = 11.sp,
                            color = MovieTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "8,950",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MovieRedPrimary
                        )
                        Text(
                            text = "+24% mwezi huu",
                            fontSize = 9.sp,
                            color = MovieGreenSuccess
                        )
                    }
                }
            }
        }

        // Country Distribution Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == Language.SW) "Usambazaji wa Wateja kwa Nchi" else "Répartition des Clients par Pays",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MovieTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    stats.forEach { stat ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stat.country, fontSize = 12.sp, color = MovieTextPrimary)
                                Text(text = "${stat.percentage}% (${stat.userCount})", fontSize = 11.sp, color = MovieGoldLight)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { stat.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MovieGold,
                                trackColor = MovieSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Most Watched Movies Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == Language.SW) "Filamu Zinazotazamwa Zaidi" else "Films les Plus Regardés",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MovieTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    movies.sortedByDescending { it.viewsCount }.take(5).forEachIndexed { idx, m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = if (idx == 0) MovieGold else MovieTextSecondary
                                )
                                Column {
                                    Text(
                                        text = m.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MovieTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "DJ: ${m.djName ?: "Original"} • ${m.category.nameSw}",
                                        fontSize = 11.sp,
                                        color = MovieTextSecondary
                                    )
                                }
                            }

                            Surface(
                                color = MovieSurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = MovieTextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${m.viewsCount}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MovieTextPrimary
                                    )
                                }
                            }
                        }
                        if (idx < 4) HorizontalDivider(color = MovieDivider)
                    }
                }
            }
        }
    }
}

// 4. Arifa kwa Watazamaji & Mitandao ya Kijamii
@Composable
private fun AdminSocialAndNotifsTab(
    language: Language,
    socialLinks: List<SocialLink>,
    isActionLoading: Boolean = false,
    onBroadcastNotif: (String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var notifTitleSw by remember { mutableStateOf("") }
    var notifTitleFr by remember { mutableStateOf("") }
    var notifMsgSw by remember { mutableStateOf("") }
    var notifMsgFr by remember { mutableStateOf("") }

    val notifPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = if (language == Language.SW) "Tuma Arifa Mpya kwa Watazamaji Wote" else "Diffuser une Nouvelle Notification",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MovieGold
        )
        Text(
            text = if (language == Language.SW)
                "Arifa itaonekana ndani ya app na pia kama notification ya simu."
            else
                "La notification apparaîtra dans l'application et sur le téléphone.",
            fontSize = 11.sp,
            color = MovieTextSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = notifTitleSw,
            onValueChange = { notifTitleSw = it },
            label = { Text("Kichwa cha Arifa (Kiswahili)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("admin_notif_title_sw"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notifMsgSw,
            onValueChange = { notifMsgSw = it },
            label = { Text("Ujumbe wa Arifa (Kiswahili)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("admin_notif_msg_sw"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notifTitleFr,
            onValueChange = { notifTitleFr = it },
            label = { Text("Titre de la Notification (Français - Hiari)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notifMsgFr,
            onValueChange = { notifMsgFr = it },
            label = { Text("Message de la Notification (Français - Hiari)") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    !AppNotificationHelper.hasNotificationPermission(context)
                ) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                if (notifTitleSw.isNotBlank() && notifMsgSw.isNotBlank()) {
                    onBroadcastNotif(
                        notifTitleSw,
                        notifTitleFr.ifBlank { notifTitleSw },
                        notifMsgSw,
                        notifMsgFr.ifBlank { notifMsgSw }
                    )
                    notifTitleSw = ""
                    notifTitleFr = ""
                    notifMsgSw = ""
                    notifMsgFr = ""
                }
            },
            enabled = !isActionLoading && notifTitleSw.isNotBlank() && notifMsgSw.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("admin_btn_send_notif")
        ) {
            if (isActionLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.5.dp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == Language.SW) "Inatuma Arifa..." else "Envoi en cours...",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == Language.SW) "Tuma Arifa kwa Wote" else "Envoyer la Notification",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        HorizontalDivider(color = MovieDivider)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (language == Language.SW) "Mitandao ya Kijamii ya App" else "Réseaux Sociaux de l'App",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MovieTextPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        socialLinks.forEach { soc ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MovieCardBg),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = soc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MovieTextPrimary)
                        Text(text = soc.handle, fontSize = 11.sp, color = MovieGoldLight)
                        Text(text = soc.url, fontSize = 10.sp, color = MovieTextSecondary)
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(soc.url))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open", tint = MovieBlueVip)
                    }
                }
            }
        }
    }
}

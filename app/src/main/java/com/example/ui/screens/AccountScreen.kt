package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.UserProfile
import com.example.ui.theme.*
import com.example.util.AppUpdateUiState
import com.example.util.RemoteUpdateInfo
import java.io.File

@Composable
fun AccountScreen(
    userProfile: UserProfile?,
    appUpdateState: AppUpdateUiState,
    onCheckForUpdates: () -> Unit,
    onDownloadAndInstallUpdate: (RemoteUpdateInfo) -> Unit,
    onLaunchApkInstall: (File) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Account",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MovieTextPrimary
            )
        }

        // Profile Summary Card
        item {
            Surface(
                color = MovieCardBg,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val initial = (userProfile?.displayName?.takeIf { it.isNotBlank() }?.take(1)
                        ?: userProfile?.email?.take(1)
                        ?: "U").uppercase()
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MovieRedPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile?.displayName?.ifBlank { "Movie SW User" } ?: "Movie SW User",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MovieTextPrimary
                        )
                        Text(
                            text = userProfile?.email ?: "",
                            fontSize = 12.sp,
                            color = MovieTextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val isAdmin = userProfile?.isAdmin == true
                        Surface(
                            color = if (isAdmin) MovieGold.copy(alpha = 0.2f) else MovieSurfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isAdmin) "Role: Admin" else "Role: User",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdmin) MovieGold else MovieTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // App Version & Update Card
        item {
            AppUpdateSectionCard(
                appUpdateState = appUpdateState,
                onCheckForUpdates = onCheckForUpdates,
                onDownloadAndInstallUpdate = onDownloadAndInstallUpdate,
                onLaunchApkInstall = onLaunchApkInstall
            )
        }

        // Log out Card
        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MovieSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieRedPrimary.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("account_logout_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = MovieRedPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovieRedPrimary
                )
            }
        }
    }
}

@Composable
private fun AppUpdateSectionCard(
    appUpdateState: AppUpdateUiState,
    onCheckForUpdates: () -> Unit,
    onDownloadAndInstallUpdate: (RemoteUpdateInfo) -> Unit,
    onLaunchApkInstall: (File) -> Unit
) {
    val isChecking = appUpdateState is AppUpdateUiState.Checking
    val isDownloading = appUpdateState is AppUpdateUiState.Downloading

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MovieCardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MovieBorder, RoundedCornerShape(16.dp))
            .testTag("account_update_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = MovieGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "App Updates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MovieTextPrimary
                    )
                }

                Surface(
                    color = MovieSurfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovieGoldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Version: ${BuildConfig.VERSION_NAME} (Code ${BuildConfig.VERSION_CODE})",
                fontSize = 13.sp,
                color = MovieTextSecondary,
                modifier = Modifier.testTag("text_installed_version")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCheckForUpdates,
                enabled = !isChecking && !isDownloading,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_check_updates")
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Checking...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check for updates",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            when (appUpdateState) {
                is AppUpdateUiState.Idle,
                is AppUpdateUiState.Checking -> Unit

                is AppUpdateUiState.UpToDate -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("text_app_up_to_date")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MovieGreenSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Your app is up to date",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MovieGreenSuccess
                        )
                    }
                }

                is AppUpdateUiState.UpdateAvailable -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MovieSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "New version available (${appUpdateState.info.versionName})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieGoldLight,
                                modifier = Modifier.testTag("text_new_version_available")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FilledTonalButton(
                                onClick = { onDownloadAndInstallUpdate(appUpdateState.info) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MovieGold,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_update_now")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Update",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                is AppUpdateUiState.Downloading -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val label = if (appUpdateState.progress != null) {
                            "Downloading: ${appUpdateState.percentage}%"
                        } else {
                            "Downloading update..."
                        }
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MovieGoldLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (appUpdateState.progress != null) {
                            LinearProgressIndicator(
                                progress = { appUpdateState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MovieGold,
                                trackColor = MovieSurfaceVariant
                            )
                        } else {
                            LinearProgressIndicator(
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

                is AppUpdateUiState.ReadyToInstall -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    FilledTonalButton(
                        onClick = { onLaunchApkInstall(appUpdateState.apkFile) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MovieGreenSuccess,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Install update",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                is AppUpdateUiState.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("text_update_error")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MovieRedPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = appUpdateState.message,
                            fontSize = 13.sp,
                            color = MovieRedPrimary
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.AppUpdateUiState
import com.example.util.RemoteUpdateInfo
import com.example.util.Strings
import java.io.File

@Composable
fun AccountScreen(
    language: Language,
    currentUser: UserAccount?,
    socialLinks: List<SocialLink>,
    sentVerifyEmailOtp: String? = null,
    isLoading: Boolean = false,
    appUpdateState: AppUpdateUiState = AppUpdateUiState.Idle,
    onLanguageToggle: () -> Unit,
    onSelectQuality: (VideoQuality) -> Unit,
    onVipUpgradeClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit,
    onSendEmailVerification: (String) -> Unit = {},
    onVerifyEmail: (String) -> Boolean = { false },
    onCheckForUpdates: () -> Unit = {},
    onDownloadAndInstallUpdate: (RemoteUpdateInfo) -> Unit = {},
    onLaunchApkInstall: (File) -> Unit = {}
) {
    val context = LocalContext.current
    val currentQuality = currentUser?.preferredQuality ?: VideoQuality.P360
    val isVipActive = currentUser?.isVipActive() == true
    val userVip = if (isVipActive) currentUser!!.vipTier else VipTier.BURE
    var showVerifyInput by remember { mutableStateOf(false) }
    var verifyCodeInput by remember { mutableStateOf("") }
    var isVerifyingLocal by remember { mutableStateOf(false) }

    LaunchedEffect(isVerifyingLocal) {
        if (isVerifyingLocal) {
            kotlinx.coroutines.delay(300)
            val ok = onVerifyEmail(verifyCodeInput)
            isVerifyingLocal = false
            if (ok) {
                showVerifyInput = false
                verifyCodeInput = ""
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        if (isLoading) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = MovieGold,
                    trackColor = MovieSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        // Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.navAccount(language),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = MovieTextPrimary
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MovieGold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Profile Card
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
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MovieRedPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.name?.take(1) ?: "U").uppercase(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Mtumiaji wa Movie SW",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MovieTextPrimary
                        )
                        Text(
                            text = "${currentUser?.emailOrPhone ?: ""} • ${currentUser?.country ?: ""}",
                            fontSize = 12.sp,
                            color = MovieTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = MovieSurfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Umri: ${currentUser?.ageTier?.label ?: "18+"}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MovieGoldLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                color = when (userVip) {
                                    VipTier.VIP_PRO -> MovieGold
                                    VipTier.BURE -> MovieSurfaceVariant
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = userVip.titleSw,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (userVip == VipTier.VIP_PRO) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section: Mise à jour de l'application (Compte & Version)
        item {
            AppUpdateSectionCard(
                appUpdateState = appUpdateState,
                onCheckForUpdates = onCheckForUpdates,
                onDownloadAndInstallUpdate = onDownloadAndInstallUpdate,
                onLaunchApkInstall = onLaunchApkInstall
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section: Ulinzi wa Akaunti & Uhakiki wa Mail (Anti-Hijack Security)
        item {
            val isVerified = currentUser?.isEmailVerified == true
            Surface(
                color = MovieSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isVerified) MovieGreenSuccess else MovieGold
                ),
                modifier = Modifier.fillMaxWidth().testTag("account_security_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isVerified) Icons.Default.VerifiedUser else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isVerified) MovieGreenSuccess else MovieGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = if (language == Language.SW) "Ulinzi wa Akaunti (Anti-Hijack)" else "Sécurité Anti-Piratage",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MovieTextPrimary
                            )
                        }

                        Surface(
                            color = if (isVerified) MovieGreenSuccess.copy(alpha = 0.2f) else MovieGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isVerified) MovieGreenSuccess else MovieGold
                            )
                        ) {
                            Text(
                                text = if (isVerified) "VÉRIFIÉ" else "NON VÉRIFIÉ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isVerified) MovieGreenSuccess else MovieGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isVerified) {
                        Text(
                            text = if (language == Language.SW)
                                "Barua pepe ya akaunti hii (${currentUser?.emailOrPhone ?: ""}) imethibitishwa. Akaunti yako inalindwa dhidi ya udukuzi na ku-jack. Hakuna mtu anayeweza kuiba akaunti yako."
                            else
                                "Votre adresse (${currentUser?.emailOrPhone ?: ""}) est certifiée. Votre compte est sécurisé contre le piratage et l'usurpation.",
                            fontSize = 12.sp,
                            color = MovieTextSecondary
                        )
                    } else {
                        Text(
                            text = if (language == Language.SW)
                                "Tahadhari ya Ulinzi: Akaunti yako bado haijathibitishwa na Gmail. Watu wengine wanaweza kujaribu ku-jack akaunti yako. Thibitisha sasa kwa kupokea code ya tarakimu 6."
                            else
                                "Alerte de Sécurité : Votre compte n'est pas encore vérifié. Vérifiez votre adresse email avec un code à 6 chiffres pour verrouiller l'accès.",
                            fontSize = 12.sp,
                            color = MovieTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (showVerifyInput) {
                            if (!sentVerifyEmailOtp.isNullOrBlank()) {
                                Surface(
                                    color = MovieGold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (language == Language.SW) "CODE YA UHAKIKI IMETUMWA GMAIL:" else "CODE DE VÉRIFICATION ENVOYÉ :",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MovieGold
                                        )
                                        Text(
                                            text = sentVerifyEmailOtp,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 3.sp,
                                            color = MovieGoldLight
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = verifyCodeInput,
                                    onValueChange = { if (it.length <= 6) verifyCodeInput = it },
                                    label = { Text(if (language == Language.SW) "Code (Tarakimu 6)" else "Code à 6 chiffres", fontSize = 11.sp) },
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("input_account_verify_code"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Button(
                                    onClick = {
                                        if (!isVerifyingLocal) isVerifyingLocal = true
                                    },
                                    enabled = !isVerifyingLocal,
                                    colors = ButtonDefaults.buttonColors(containerColor = MovieGreenSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_confirm_account_verify")
                                ) {
                                    if (isVerifyingLocal) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.Black
                                        )
                                    } else {
                                        Text(if (language == Language.SW) "Thibitisha" else "Valider", fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val email = currentUser?.emailOrPhone?.takeIf { it.contains("@") } ?: "mtumiaji@gmail.com"
                                    onSendEmailVerification(email)
                                    showVerifyInput = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_start_email_verification")
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == Language.SW) "Thibitisha Gmail Yako Sasa (Zuia Ku-jack)" else "Vérifier mon Email (Anti-Piratage)",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Passer à VIP Promotion Card (Removed while 1-month VIP is active; returns on expiry)
        if (!isVipActive) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVipUpgradeClick() }
                        .testTag("account_passer_vip_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MovieSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieGold)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MovieGold, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Strings.upgradeToVip(language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MovieGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (language == Language.SW)
                                    "Fungua filamu zote, 1080p/8K na upakuaji wa kifaa bila kikomo (Mwezi 1)."
                                else
                                    "Accédez à tous les films, 1080p/8K et téléchargements illimités (1 mois).",
                                fontSize = 12.sp,
                                color = MovieTextSecondary
                            )
                        }

                        Button(
                            onClick = onVipUpgradeClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MovieGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("VIP", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        } else {
            item {
                Surface(
                    color = MovieSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MovieGreenSuccess),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_active_vip_status")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MovieGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == Language.SW)
                                    "VIP Imewashwa (${userVip.titleSw})"
                                else
                                    "VIP Actif (${userVip.titleFr})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MovieGoldLight
                            )
                            val daysLeft = currentUser?.daysRemaining() ?: 30
                            Text(
                                text = if (currentUser?.role == UserRole.ADMIN) {
                                    if (language == Language.SW) "Akaunti ya Admin: Haina kikomo." else "Compte Admin : Illimité."
                                } else {
                                    if (language == Language.SW)
                                        "Abonnement ya mwezi 1: Siku $daysLeft zimebaki. Kitufe cha VIP kimerudishwa hadi mwezi uishe."
                                    else
                                        "Abonnement 1 mois : $daysLeft jours restants. Le bouton VIP réapparaîtra à l'expiration."
                                },
                                fontSize = 12.sp,
                                color = MovieTextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Section: "Ubora wa Video" (144p, 360p, 750p, 1080p, 8K)
        item {
            Text(
                text = Strings.videoQuality(language),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MovieTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (language == Language.SW)
                    "Ubora wa 750p, 1080p na 8K unahitaji akaunti ya VIP Pro."
                else
                    "La qualité 750p, 1080p et 8K nécessite un compte VIP Pro.",
                fontSize = 12.sp,
                color = MovieTextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MovieCardBg,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    VideoQuality.values().forEach { quality ->
                        val isSelected = currentQuality == quality
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectQuality(quality) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectQuality(quality) },
                                    colors = RadioButtonDefaults.colors(selectedColor = MovieRedPrimary)
                                )
                                Text(
                                    text = quality.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MovieTextPrimary else MovieTextSecondary,
                                    fontSize = 14.sp
                                )
                            }

                            if (quality.requiresVip) {
                                Surface(
                                    color = MovieGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "VIP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MovieGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (quality != VideoQuality.P8K) {
                            HorizontalDivider(color = MovieDivider)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Social Media Channels
        item {
            Text(
                text = Strings.socialFollow(language),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MovieTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = MovieCardBg,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    socialLinks.forEach { social ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(social.url))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when (social.iconName) {
                                        "whatsapp" -> Icons.Default.Chat
                                        "youtube" -> Icons.Default.VideoLibrary
                                        "instagram" -> Icons.Default.CameraAlt
                                        else -> Icons.Default.Share
                                    },
                                    contentDescription = social.name,
                                    tint = when (social.iconName) {
                                        "whatsapp" -> MovieGreenSuccess
                                        "youtube" -> MovieRedPrimary
                                        "instagram" -> MovieGoldLight
                                        else -> MovieBlueVip
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = social.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = MovieTextPrimary
                                    )
                                    Text(
                                        text = social.handle,
                                        fontSize = 11.sp,
                                        color = MovieTextSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MovieTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (social != socialLinks.last()) {
                            HorizontalDivider(color = MovieDivider)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Language Switcher & Admin Portal & Logout
        item {
            Surface(
                color = MovieCardBg,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Language Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageToggle() }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MovieGoldLight, modifier = Modifier.size(20.dp))
                            Text(
                                text = Strings.switchLanguage(language),
                                fontSize = 14.sp,
                                color = MovieTextPrimary
                            )
                        }
                        Text(
                            text = if (language == Language.SW) "Kiswahili" else "Français",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieGoldLight
                        )
                    }

                    // Admin Portal Button (ONLY accessible for Admin - normal users even with VIP Pro Max cannot access)
                    if (currentUser?.role == UserRole.ADMIN) {
                        HorizontalDivider(color = MovieDivider)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAdminClick() }
                                .padding(12.dp)
                                .testTag("account_admin_portal_button"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MovieGold, modifier = Modifier.size(20.dp))
                                Text(
                                    text = Strings.adminDashboardTitle(language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MovieGold
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MovieGold, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(color = MovieDivider)

                    // Logout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLogout() }
                            .padding(12.dp)
                            .testTag("account_logout_button"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = MovieRedPrimary, modifier = Modifier.size(20.dp))
                            Text(
                                text = Strings.logout(language),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MovieRedPrimary
                            )
                        }
                    }
                }
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
                        text = "Mise à jour de l'application",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
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
                text = "Version installée : ${BuildConfig.VERSION_NAME} (Code ${BuildConfig.VERSION_CODE})",
                fontSize = 12.sp,
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
                        text = "Vérification...",
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
                        text = "Vérifier les mises à jour",
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
                            text = "Votre application est à jour",
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
                                text = "Nouvelle version disponible (${appUpdateState.info.versionName})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MovieGoldLight,
                                modifier = Modifier.testTag("text_new_version_available")
                            )
                            if (appUpdateState.info.releaseNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = appUpdateState.info.releaseNotes,
                                    fontSize = 11.sp,
                                    color = MovieTextSecondary
                                )
                            }
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
                                    text = "Mettre à jour",
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
                            "Téléchargement : ${appUpdateState.percentage}%"
                        } else {
                            "Téléchargement en cours..."
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
                                    .clip(RoundedCornerShape(3.dp))
                                    .testTag("progress_update_download"),
                                color = MovieGold,
                                trackColor = MovieSurfaceVariant
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .testTag("progress_update_download"),
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
                            .testTag("btn_install_apk")
                    ) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Installer la mise à jour",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                is AppUpdateUiState.Error -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = appUpdateState.message,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MovieRedPrimary,
                        modifier = Modifier.testTag("text_update_error")
                    )
                    if (appUpdateState.retryInfo != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onDownloadAndInstallUpdate(appUpdateState.retryInfo) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Mettre à jour",
                                fontWeight = FontWeight.Bold,
                                color = MovieGoldLight
                            )
                        }
                    }
                }
            }
        }
    }
}

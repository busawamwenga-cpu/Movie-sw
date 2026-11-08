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
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun AccountScreen(
    language: Language,
    currentUser: UserAccount?,
    socialLinks: List<SocialLink>,
    sentVerifyEmailOtp: String? = null,
    onLanguageToggle: () -> Unit,
    onSelectQuality: (VideoQuality) -> Unit,
    onVipUpgradeClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit,
    onSendEmailVerification: (String) -> Unit = {},
    onVerifyEmail: (String) -> Boolean = { false }
) {
    val context = LocalContext.current
    val currentQuality = currentUser?.preferredQuality ?: VideoQuality.P360
    val userVip = currentUser?.vipTier ?: VipTier.BURE
    var showVerifyInput by remember { mutableStateOf(false) }
    var verifyCodeInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        // Title
        item {
            Text(
                text = Strings.navAccount(language),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = MovieTextPrimary
            )
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
                                    VipTier.VIP_PRO_MAX -> MovieGold
                                    VipTier.VIP_PRO -> MovieBlueVip
                                    VipTier.BURE -> MovieSurfaceVariant
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = userVip.titleSw,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (userVip == VipTier.VIP_PRO_MAX) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
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
                                        val ok = onVerifyEmail(verifyCodeInput)
                                        if (ok) {
                                            showVerifyInput = false
                                            verifyCodeInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MovieGreenSuccess),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_confirm_account_verify")
                                ) {
                                    Text(if (language == Language.SW) "Thibitisha" else "Valider", fontWeight = FontWeight.Bold, color = Color.Black)
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

        // Passer à VIP Promotion Card
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
                                "Fungua filamu zote, 1080p/8K na upakuaji wa kifaa bila kikomo."
                            else
                                "Accédez à tous les films, 1080p/8K et téléchargements illimités.",
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
                    "Ubora wa 750p, 1080p na 8K unahitaji akaunti ya VIP Pro au VIP Pro Max."
                else
                    "La qualité 750p, 1080p et 8K nécessite un compte VIP Pro ou VIP Pro Max.",
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

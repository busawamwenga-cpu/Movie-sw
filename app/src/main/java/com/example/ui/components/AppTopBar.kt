package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.model.Language
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.VipTier
import com.example.ui.theme.*

@Composable
fun AppTopBar(
    language: Language,
    currentUser: UserAccount?,
    unreadNotificationsCount: Int,
    onLanguageToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    onVipClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MovieDarkBg,
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.Black)
                        .border(1.dp, MovieRedPrimary, RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "Movie SW Logo",
                        modifier = Modifier.size(34.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MOVIE",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MovieTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SW",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MovieRedPrimary
                        )
                    }
                    Text(
                        text = if (language == Language.SW) "Kiswahili & Français" else "Français & Kiswahili",
                        fontSize = 10.sp,
                        color = MovieTextSecondary
                    )
                }
            }

            // Actions: Language Toggle, VIP Badge, Notifications, Admin
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Switcher Pill
                Surface(
                    color = MovieSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .testTag("language_toggle_button")
                        .clickable { onLanguageToggle() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (language == Language.SW) "SW" else "FR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieGoldLight
                        )
                    }
                }

                // VIP Badge Button
                val userVip = currentUser?.vipTier ?: VipTier.BURE
                Surface(
                    color = when (userVip) {
                        VipTier.VIP_PRO_MAX -> MovieGold
                        VipTier.VIP_PRO -> MovieBlueVip
                        VipTier.BURE -> MovieSurfaceVariant
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .testTag("vip_badge_button")
                        .clickable { onVipClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (userVip != VipTier.BURE) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "VIP",
                                tint = if (userVip == VipTier.VIP_PRO_MAX) Color.Black else Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = when (userVip) {
                                VipTier.VIP_PRO_MAX -> "MAX"
                                VipTier.VIP_PRO -> "PRO"
                                VipTier.BURE -> if (language == Language.SW) "VIP" else "VIP"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (userVip == VipTier.VIP_PRO_MAX) Color.Black else Color.White
                        )
                    }
                }

                // Notifications Bell
                Box(
                    modifier = Modifier
                        .testTag("notifications_button")
                        .clip(CircleShape)
                        .background(MovieSurfaceVariant)
                        .clickable { onNotificationsClick() }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MovieTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadNotificationsCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MovieRedPrimary)
                                .align(Alignment.TopEnd)
                        )
                    }
                }

                // Admin Button (If user is Admin or for Admin entry)
                if (currentUser?.role == UserRole.ADMIN) {
                    IconButton(
                        onClick = onAdminClick,
                        modifier = Modifier
                            .testTag("admin_dashboard_button")
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Dashboard",
                            tint = MovieGoldLight
                        )
                    }
                }
            }
        }
    }
}

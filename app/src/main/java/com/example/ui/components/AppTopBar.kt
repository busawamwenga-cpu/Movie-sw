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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.ui.theme.MovieDarkBg
import com.example.ui.theme.MovieGoldLight
import com.example.ui.theme.MovieRedPrimary
import com.example.ui.theme.MovieSurfaceVariant
import com.example.ui.theme.MovieTextPrimary
import com.example.ui.theme.MovieTextSecondary

@Composable
fun AppTopBar(
    isAdmin: Boolean,
    unreadNotificationsCount: Int,
    onNotificationsClick: () -> Unit,
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
                        text = if (isAdmin) "Admin Mode" else "Watch Movies",
                        fontSize = 10.sp,
                        color = if (isAdmin) MovieGoldLight else MovieTextSecondary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .testTag("notifications_button")
                        .clip(CircleShape)
                        .background(MovieSurfaceVariant)
                        .clickable { onNotificationsClick() }
                        .padding(8.dp)
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

                if (isAdmin) {
                    IconButton(
                        onClick = onAdminClick,
                        modifier = Modifier
                            .testTag("admin_dashboard_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Open Admin Panel",
                            tint = MovieGoldLight
                        )
                    }
                }
            }
        }
    }
}

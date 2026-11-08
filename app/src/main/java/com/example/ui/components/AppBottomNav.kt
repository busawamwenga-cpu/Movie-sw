package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun AppBottomNav(
    selectedTab: String,
    language: Language,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MovieSurface,
        contentColor = MovieTextPrimary,
        tonalElevation = 8.dp
    ) {
        // 1. Home
        val isHome = selectedTab == "home"
        NavigationBarItem(
            selected = isHome,
            onClick = { onTabSelected("home") },
            icon = {
                Icon(
                    imageVector = if (isHome) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = Strings.navHome(language),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = Strings.navHome(language),
                    fontSize = 11.sp,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRedPrimary,
                selectedTextColor = MovieRedPrimary,
                unselectedIconColor = MovieTextSecondary,
                unselectedTextColor = MovieTextSecondary,
                indicatorColor = MovieSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        // 2. Explore
        val isExplore = selectedTab == "explore"
        NavigationBarItem(
            selected = isExplore,
            onClick = { onTabSelected("explore") },
            icon = {
                Icon(
                    imageVector = if (isExplore) Icons.Default.Search else Icons.Outlined.Search,
                    contentDescription = Strings.navExplore(language),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = Strings.navExplore(language),
                    fontSize = 11.sp,
                    fontWeight = if (isExplore) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRedPrimary,
                selectedTextColor = MovieRedPrimary,
                unselectedIconColor = MovieTextSecondary,
                unselectedTextColor = MovieTextSecondary,
                indicatorColor = MovieSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_explore")
        )

        // 3. Album
        val isAlbum = selectedTab == "album"
        NavigationBarItem(
            selected = isAlbum,
            onClick = { onTabSelected("album") },
            icon = {
                Icon(
                    imageVector = if (isAlbum) Icons.Default.VideoLibrary else Icons.Outlined.VideoLibrary,
                    contentDescription = Strings.navAlbum(language),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = Strings.navAlbum(language),
                    fontSize = 11.sp,
                    fontWeight = if (isAlbum) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRedPrimary,
                selectedTextColor = MovieRedPrimary,
                unselectedIconColor = MovieTextSecondary,
                unselectedTextColor = MovieTextSecondary,
                indicatorColor = MovieSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_album")
        )

        // 4. Account
        val isAccount = selectedTab == "account"
        NavigationBarItem(
            selected = isAccount,
            onClick = { onTabSelected("account") },
            icon = {
                Icon(
                    imageVector = if (isAccount) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = Strings.navAccount(language),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = Strings.navAccount(language),
                    fontSize = 11.sp,
                    fontWeight = if (isAccount) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MovieRedPrimary,
                selectedTextColor = MovieRedPrimary,
                unselectedIconColor = MovieTextSecondary,
                unselectedTextColor = MovieTextSecondary,
                indicatorColor = MovieSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_account")
        )
    }
}

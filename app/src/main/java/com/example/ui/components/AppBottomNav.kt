package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MovieGold
import com.example.ui.theme.MovieRedPrimary
import com.example.ui.theme.MovieSurface
import com.example.ui.theme.MovieSurfaceVariant
import com.example.ui.theme.MovieTextPrimary
import com.example.ui.theme.MovieTextSecondary

@Composable
fun AppBottomNav(
    selectedTab: String,
    isAdmin: Boolean,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MovieSurface,
        contentColor = MovieTextPrimary,
        tonalElevation = 8.dp
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MovieRedPrimary,
            selectedTextColor = MovieRedPrimary,
            unselectedIconColor = MovieTextSecondary,
            unselectedTextColor = MovieTextSecondary,
            indicatorColor = MovieSurfaceVariant
        )

        // 1. Movies
        val isHome = selectedTab == "home"
        NavigationBarItem(
            selected = isHome,
            onClick = { onTabSelected("home") },
            icon = {
                Icon(
                    imageVector = if (isHome) Icons.Default.Movie else Icons.Outlined.Movie,
                    contentDescription = "Movies",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Movies",
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navColors,
            modifier = Modifier.testTag("nav_item_home")
        )

        // 2. Search
        val isExplore = selectedTab == "explore"
        NavigationBarItem(
            selected = isExplore,
            onClick = { onTabSelected("explore") },
            icon = {
                Icon(
                    imageVector = if (isExplore) Icons.Default.Search else Icons.Outlined.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Search",
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = if (isExplore) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navColors,
            modifier = Modifier.testTag("nav_item_explore")
        )

        // 3. Saved
        val isAlbum = selectedTab == "album"
        NavigationBarItem(
            selected = isAlbum,
            onClick = { onTabSelected("album") },
            icon = {
                Icon(
                    imageVector = if (isAlbum) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Saved",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Saved",
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = if (isAlbum) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navColors,
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
                    contentDescription = "Account",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Account",
                    fontSize = 11.sp,
                    maxLines = 1,
                    fontWeight = if (isAccount) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = navColors,
            modifier = Modifier.testTag("nav_item_account")
        )

        // 5. Admin (Visible ONLY when Firestore role == "admin"; completely hidden for regular users)
        if (isAdmin) {
            val isAdminSelected = selectedTab == "admin"
            NavigationBarItem(
                selected = isAdminSelected,
                onClick = { onTabSelected("admin") },
                icon = {
                    Icon(
                        imageVector = if (isAdminSelected) Icons.Default.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                        contentDescription = "Admin",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "Admin",
                        fontSize = 11.sp,
                        maxLines = 1,
                        fontWeight = if (isAdminSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MovieGold,
                    selectedTextColor = MovieGold,
                    unselectedIconColor = MovieTextSecondary,
                    unselectedTextColor = MovieTextSecondary,
                    indicatorColor = MovieSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_item_admin")
            )
        }
    }
}

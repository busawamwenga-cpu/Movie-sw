package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CloudMovie
import com.example.ui.theme.*

@Composable
fun NotificationsDialog(
    recentMovies: List<CloudMovie>,
    onDismiss: () -> Unit,
    onMovieClick: (CloudMovie) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MovieSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
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
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MovieRedPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Latest Movies",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MovieTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MovieTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (recentMovies.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No new notifications right now.",
                            color = MovieTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recentMovies.take(8)) { movie ->
                            Surface(
                                onClick = {
                                    onDismiss()
                                    onMovieClick(movie)
                                },
                                color = MovieCardBg,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "New movie: ${movie.title}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MovieTextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = movie.formattedDate(),
                                            fontSize = 10.sp,
                                            color = MovieGoldLight
                                        )
                                    }
                                    if (movie.description.isNotBlank()) {
                                        Text(
                                            text = movie.description,
                                            fontSize = 12.sp,
                                            color = MovieTextSecondary,
                                            maxLines = 2
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

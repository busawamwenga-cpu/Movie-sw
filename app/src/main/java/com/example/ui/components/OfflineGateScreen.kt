package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OfflineGateScreen(
    language: Language,
    onRetry: () -> Unit
) {
    var isChecking by remember { mutableStateOf(false) }

    LaunchedEffect(isChecking) {
        if (isChecking) {
            delay(500)
            isChecking = false
            onRetry()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg)
            .padding(24.dp)
            .testTag("offline_gate_screen"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MovieSurface,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MovieRedPrimary.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MovieRedPrimary.copy(alpha = 0.16f))
                        .border(1.5.dp, MovieRedPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet",
                        tint = MovieRedPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (language == Language.SW)
                        "Hakuna Muunganisho wa Intaneti"
                    else
                        "Pas de connexion Internet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MovieTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (language == Language.SW)
                        "Movie SW ni app ya mtandaoni. Tafadhali washa Wi-Fi au Data yako ya simu ili kuendelea kutumia APK."
                    else
                        "Movie SW nécessite une connexion Internet active (Wi-Fi ou données mobiles) pour fonctionner.",
                    fontSize = 13.sp,
                    color = MovieTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = { if (!isChecking) isChecking = true },
                    enabled = !isChecking,
                    colors = ButtonDefaults.buttonColors(containerColor = MovieRedPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_retry_internet")
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == Language.SW) "Inakagua mtandao..." else "Vérification...",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == Language.SW) "Jaribu Tena" else "Réessayer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

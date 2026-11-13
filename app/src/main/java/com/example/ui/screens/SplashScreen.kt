package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.ui.components.PopcornPouringLogo
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    language: Language,
    onFinish: () -> Unit
) {
    val scale = remember { Animatable(0.75f) }
    val alpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    var pourFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fade in logo
        alpha.animateTo(1f, animationSpec = tween(400))
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        subtitleAlpha.animateTo(1f, animationSpec = tween(600))
    }

    LaunchedEffect(pourFinished) {
        if (pourFinished) {
            delay(400)
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MovieDarkBg, MovieSurface, MovieDarkBg)
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Allow instant tap-to-continue
                onFinish()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .testTag("splash_screen")
                .padding(24.dp)
        ) {
            // Branded Animated Logo where the popcorn bowl tilts and pours onto S
            PopcornPouringLogo(
                size = 190.dp,
                autoPlay = true,
                interactive = true,
                onPourComplete = {
                    pourFinished = true
                }
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Brand Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "MOVIE ",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MovieTextPrimary
                )
                Text(
                    text = "SW",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MovieRedPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Tagline
            Text(
                text = if (language == Language.SW)
                    "KISWAHILI & FRANÇAIS STREAMING"
                else
                    "STREAMING EN SWAHILI & FRANÇAIS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
                color = MovieGoldLight,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(18.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = MovieRedPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hint
            Text(
                text = if (language == Language.SW) "Inapakia... Gusa popote kuendelea" else "Chargement... Touchez pour continuer",
                fontSize = 11.sp,
                color = MovieTextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}

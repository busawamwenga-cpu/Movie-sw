package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Language
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun OnboardingScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) } // 0, 1, 2

    val images = listOf(
        R.drawable.img_onboarding_1,
        R.drawable.img_onboarding_2,
        R.drawable.img_onboarding_3
    )

    val titles = listOf(
        Strings.onb1Title(language),
        Strings.onb2Title(language),
        Strings.onb3Title(language)
    )

    val descriptions = listOf(
        Strings.onb1Desc(language),
        Strings.onb2Desc(language),
        Strings.onb3Desc(language)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar with Skip and Language Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Switcher
                Surface(
                    color = MovieSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .testTag("onboarding_lang_toggle")
                        .clickable { onLanguageToggle() }
                ) {
                    Text(
                        text = if (language == Language.SW) "Kiswahili" else "Français",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MovieGoldLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                // Skip button
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = Strings.btnSkip(language),
                        color = MovieTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Image Banner with Animated Transition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "onboarding_image"
                ) { currentStep ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = images[currentStep]),
                            contentDescription = titles[currentStep],
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Vignette
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            MovieDarkBg.copy(alpha = 0.5f),
                                            MovieDarkBg
                                        ),
                                        startY = 200f
                                    )
                                )
                        )
                    }
                }
            }

            // Texts and Bottom Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (step == index) 28.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (step == index) MovieRedPrimary else MovieBorder
                                )
                        )
                    }
                }

                // Title
                Text(
                    text = titles[step],
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MovieTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                Text(
                    text = descriptions[step],
                    fontSize = 14.sp,
                    color = MovieTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.height(65.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Continue / Start Button
                Button(
                    onClick = {
                        if (step < 2) {
                            step++
                        } else {
                            onFinish()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_continue_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MovieRedPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (step == 2) Strings.btnStart(language) else Strings.btnContinue(language),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun BienvenueScreen(
    language: Language,
    onLanguageToggle: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MovieDarkBg)
    ) {
        // Poster Grid Background (like Netflix)
        Image(
            painter = painterResource(id = R.drawable.img_welcome_posters),
            contentDescription = "Movie SW Posters Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark gradient overlays for high legibility and cinema contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MovieDarkBg.copy(alpha = 0.75f),
                            MovieDarkBg.copy(alpha = 0.85f),
                            MovieDarkBg.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        // Top Bar: Logo & Language Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "MOVIE SW",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MovieTextPrimary
                )
            }

            Surface(
                color = MovieSurfaceVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier
                    .testTag("bienvenue_lang_toggle")
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
        }

        // Center Content & Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(2.dp, MovieRedPrimary, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Movie SW Logo",
                    modifier = Modifier.size(74.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = Strings.welcomeHeading(language),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MovieTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = Strings.welcomeSubheading(language),
                fontSize = 15.sp,
                color = MovieTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Button: S'inscrire (Kujisajili)
            Button(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_signup_welcome"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MovieRedPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = Strings.btnSignUp(language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Button: Se connecter (Kuingia)
            OutlinedButton(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_signin_welcome"),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MovieSurfaceVariant.copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MovieBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = Strings.btnSignIn(language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MovieTextPrimary
                )
            }
        }
    }
}

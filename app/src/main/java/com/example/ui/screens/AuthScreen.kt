package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgeTier
import com.example.model.Language
import com.example.ui.components.CountryPickerDialog
import com.example.ui.theme.*
import com.example.util.CountryData
import com.example.util.CountryInfo
import com.example.util.Strings
import com.example.viewmodel.AuthMode

@Composable
fun AuthScreen(
    language: Language,
    authMode: AuthMode,
    errorMessage: String?,
    sentOtpCode: String?,
    otpTargetEmail: String?,
    sentVerifyEmailOtp: String? = null,
    isLoading: Boolean = false,
    onSwitchMode: (AuthMode) -> Unit,
    onBack: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, AgeTier, Boolean) -> Unit,
    onSendOtp: (String) -> Unit,
    onResetPassword: (String, String, String) -> Unit,
    onSendVerifyEmailOtp: (String) -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(CountryData.defaultCountry) }
    var showCountryPicker by remember { mutableStateOf(false) }
    var registerWithPhone by remember { mutableStateOf(true) }
    var phoneNumber by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var isEmailCodeVerified by remember { mutableStateOf(false) }
    var emailVerifyCodeInput by remember { mutableStateOf("") }
    var identifier by remember(authMode) { mutableStateOf("") }
    var password by remember(authMode) { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedAgeTier by remember { mutableStateOf(AgeTier.AGE_18_PLUS) }

    // State for Forgot Password / OTP Reset
    var resetEmail by remember { mutableStateOf("") }
    var resetOtpInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }

    val isRegister = authMode == AuthMode.SIGN_UP
    val isAdmin = authMode == AuthMode.ADMIN_LOGIN
    val isForgotPassword = authMode == AuthMode.FORGOT_PASSWORD

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Back Arrow & Title Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (isForgotPassword) onSwitchMode(AuthMode.SIGN_IN) else onBack()
                    },
                    modifier = Modifier.testTag("auth_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MovieTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isForgotPassword) (if (language == Language.SW) "Rudi Kuingia" else "Retour Connexion") else Strings.backToWelcome(language),
                    color = MovieTextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Netflix-styled dark form card
            Surface(
                color = MovieSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header
                    Text(
                        text = when {
                            isForgotPassword -> if (language == Language.SW) "Badilisha Nywila (Gmail OTP)" else "Réinitialiser par Gmail"
                            isAdmin -> Strings.authAdminTitle(language)
                            isRegister -> Strings.authTitleRegister(language)
                            else -> Strings.authTitleLogin(language)
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MovieTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            isForgotPassword -> if (language == Language.SW)
                                "Weka Gmail yako kupokea code ya tarakimu 6 ya uhakiki na kuweka nywila mpya."
                            else
                                "Entrez votre Gmail pour recevoir le code de vérification à 6 chiffres."
                            isAdmin -> if (language == Language.SW)
                                "Ingiza Gmail yako ya siri ya Admin na Code yako ya usalama kuingia."
                            else
                                "Entrez votre Gmail administrateur et votre code d'accès personnel."
                            isRegister -> if (language == Language.SW)
                                "Unda akaunti uanze kutazama filamu mara moja."
                            else
                                "Créez votre compte pour commencer le streaming."
                            else -> if (language == Language.SW)
                                "Ingia kutazama filamu ulizohifadhi na kufungua VIP."
                            else
                                "Connectez-vous pour retrouver vos favoris et VIP."
                        },
                        fontSize = 13.sp,
                        color = MovieTextSecondary
                    )

                    // Error Banner
                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MovieRedPrimary.copy(alpha = 0.2f))
                                .border(1.dp, MovieRedPrimary, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = MovieRedPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isForgotPassword) {
                        // FORGOT PASSWORD / GMAIL CODE FLOW
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Gmail Yako / Votre Gmail") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MovieGoldLight)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("input_reset_email"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Button: Send Code to Gmail
                        Button(
                            onClick = { onSendOtp(resetEmail) },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_send_otp_gmail"),
                            colors = ButtonDefaults.buttonColors(containerColor = MovieBlueVip),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == Language.SW) "Inatuma..." else "Envoi...",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == Language.SW) "Tuma Code kwa Gmail" else "Envoyer le Code par Gmail",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Display sent OTP hint banner if generated
                        if (!sentOtpCode.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MovieGold.copy(alpha = 0.15f))
                                    .border(1.dp, MovieGold, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (language == Language.SW) "CODE YA UHAKIKI IMETUMWA GMAIL:" else "CODE DE VÉRIFICATION ENVOYÉ :",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MovieGold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$sentOtpCode",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 4.sp,
                                        color = MovieGoldLight
                                    )
                                    Text(
                                        text = if (language == Language.SW) "Weka tarakimu hizi hapa chini kuweka nywila mpya." else "Entrez ce code ci-dessous pour changer de mot de passe.",
                                        fontSize = 11.sp,
                                        color = MovieTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Input OTP Code
                            OutlinedTextField(
                                value = resetOtpInput,
                                onValueChange = { if (it.length <= 6) resetOtpInput = it },
                                label = { Text(if (language == Language.SW) "Weka Code (Tarakimu 6)" else "Code à 6 chiffres") },
                                leadingIcon = {
                                    Icon(Icons.Default.Pin, contentDescription = null, tint = MovieGoldLight)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("input_otp_code"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // New Password
                            OutlinedTextField(
                                value = newPasswordInput,
                                onValueChange = { newPasswordInput = it },
                                label = { Text(if (language == Language.SW) "Nywila Mpya (Herufi 8+)" else "Nouveau Mot de passe (8+ car)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = MovieTextSecondary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(
                                            imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth().testTag("input_new_password"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onResetPassword(resetEmail, resetOtpInput, newPasswordInput)
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth().height(50.dp).testTag("btn_confirm_reset_password"),
                                colors = ButtonDefaults.buttonColors(containerColor = MovieGreenSuccess),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == Language.SW) "Inapakia..." else "Chargement...",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                } else {
                                    Text(
                                        text = if (language == Language.SW) "Thibitisha na Weka Nywila Mpya" else "Confirmer & Réinitialiser",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (language == Language.SW) "Unakumbuka nywila? Ingia hapa" else "Vous vous souvenez du mot de passe ? Connexion",
                            color = MovieGoldLight,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSwitchMode(AuthMode.SIGN_IN) }
                                .padding(vertical = 4.dp)
                        )

                    } else {
                        // STANDARD SIGN IN / SIGN UP / ADMIN FLOW
                        if (isRegister) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(Strings.labelName(language)) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MovieTextSecondary)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_name"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Country Selector (Displays all world countries, default RDC +243)
                            Text(
                                text = if (language == Language.SW) "Nchi Yako (Bofya kuchagua)" else "Votre Pays (Touchez pour choisir)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MovieTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MovieBorder, RoundedCornerShape(12.dp))
                                    .clickable { showCountryPicker = true }
                                    .testTag("select_country_button"),
                                color = MovieDarkBg,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = MovieGoldLight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Surface(
                                            color = MovieSurfaceVariant,
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder)
                                        ) {
                                            Text(
                                                text = selectedCountry.code,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MovieGoldLight,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = selectedCountry.displayName(language == Language.SW),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MovieTextPrimary
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Surface(
                                            color = MovieGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = selectedCountry.dialCode,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MovieGoldLight,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MovieTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Choice between registering with Phone number or Gmail
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = registerWithPhone,
                                    onClick = { registerWithPhone = true },
                                    label = {
                                        Text(
                                            if (language == Language.SW) "Namba ya Simu (${selectedCountry.dialCode})" else "Téléphone (${selectedCountry.dialCode})",
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    modifier = Modifier.weight(1f).testTag("chip_register_phone")
                                )
                                FilterChip(
                                    selected = !registerWithPhone,
                                    onClick = { registerWithPhone = false },
                                    label = {
                                        Text(
                                            if (language == Language.SW) "Gmail" else "Gmail",
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                    },
                                    modifier = Modifier.weight(1f).testTag("chip_register_gmail")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (registerWithPhone) {
                                // Phone input with country dial code badge
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text(if (language == Language.SW) "Namba ya Simu" else "Numéro de Téléphone") },
                                    placeholder = { Text(if (language == Language.SW) "mfano: 812 345 678" else "ex. 812 345 678") },
                                    leadingIcon = {
                                        Surface(
                                            color = MovieGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .padding(start = 10.dp, end = 6.dp)
                                                .clickable { showCountryPicker = true }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = selectedCountry.dialCode,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MovieGoldLight
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = MovieGoldLight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth().testTag("input_phone_number"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (phoneNumber.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = (if (language == Language.SW) "Namba itakayosajiliwa: " else "Numéro complet : ") +
                                                "${selectedCountry.dialCode} ${phoneNumber.trim().removePrefix("0")}",
                                        fontSize = 11.sp,
                                        color = MovieGoldLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = {
                                        emailInput = it
                                        isEmailCodeVerified = false
                                    },
                                    label = { Text("Gmail / Barua Pepe") },
                                    placeholder = { Text("jina@gmail.com") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = MovieTextSecondary)
                                    },
                                    trailingIcon = {
                                        if (isEmailCodeVerified) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Vérifié", tint = MovieGreenSuccess)
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier.fillMaxWidth().testTag("input_email_register"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Anti-Hijack Email Verification Box
                                if (isEmailCodeVerified) {
                                    Surface(
                                        color = MovieGreenSuccess.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MovieGreenSuccess),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MovieGreenSuccess, modifier = Modifier.size(18.dp))
                                            Column {
                                                Text(
                                                    text = if (language == Language.SW) "GMAIL IMETHIBITISHWA [VÉRIFIÉ]" else "EMAIL VÉRIFIÉ [SÉCURISÉ]",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MovieGreenSuccess
                                                )
                                                Text(
                                                    text = if (language == Language.SW) "Ulinzi umewashwa: Hakuna mtu anayeweza ku-jack akaunti hii." else "Anti-piratage activé : Personne ne peut usurper ce compte.",
                                                    fontSize = 10.sp,
                                                    color = MovieTextSecondary
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Button to send verification code
                                    OutlinedButton(
                                        onClick = {
                                            if (emailInput.contains("@")) {
                                                onSendVerifyEmailOtp(emailInput)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().testTag("btn_send_register_email_otp"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = MovieGoldLight, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == Language.SW) "Tuma Code ya Uhakiki (Zuia Ku-jack)" else "Envoyer Code Anti-Piratage",
                                            fontSize = 12.sp,
                                            color = MovieGoldLight,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (!sentVerifyEmailOtp.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MovieGold.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MovieGold),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (language == Language.SW) "CODE YA UHAKIKI GMAIL (ULINZI):" else "CODE DE VÉRIFICATION EMAIL :",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MovieGold
                                                )
                                                Text(
                                                    text = sentVerifyEmailOtp,
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 4.sp,
                                                    color = MovieGoldLight
                                                )
                                                Text(
                                                    text = if (language == Language.SW) "Weka tarakimu hizi 6 hapa chini kuthibitisha umiliki wa Gmail yako." else "Entrez ce code ci-dessous pour certifier votre adresse.",
                                                    fontSize = 10.sp,
                                                    color = MovieTextSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = emailVerifyCodeInput,
                                                onValueChange = { if (it.length <= 6) emailVerifyCodeInput = it },
                                                label = { Text(if (language == Language.SW) "Code (Tarakimu 6)" else "Code à 6 chiffres", fontSize = 11.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f).testTag("input_register_email_code"),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            Button(
                                                onClick = {
                                                    if (emailVerifyCodeInput.trim() == sentVerifyEmailOtp) {
                                                        isEmailCodeVerified = true
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MovieGreenSuccess),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("btn_confirm_register_email_code")
                                            ) {
                                                Text(if (language == Language.SW) "Thibitisha" else "Valider", fontWeight = FontWeight.Bold, color = Color.Black)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        } else {
                            // Standard Login / Admin Login
                            OutlinedTextField(
                                value = identifier,
                                onValueChange = { identifier = it },
                                label = { Text(if (isAdmin) (if (language == Language.SW) "Gmail ya Admin" else "Gmail Admin") else Strings.labelEmailOrPhone(language)) },
                                placeholder = {
                                    Text(
                                        text = if (isAdmin) "admin@gmail.com" else if (language == Language.SW) "mfano: +243... au jina@gmail.com" else "ex. +243... ou nom@gmail.com",
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MovieTextSecondary
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("input_identifier"),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Password / Admin Code
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(if (isAdmin) "Admin Code (Nywila)" else Strings.labelPassword(language)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MovieTextSecondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = MovieTextSecondary
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("input_password"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Forgot Password Link (for standard login)
                        if (!isRegister && !isAdmin) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text(
                                    text = if (language == Language.SW) "Umesahau code / nywila? Tuma kwa Gmail" else "Mot de passe oublié ? Réinitialiser par Gmail",
                                    color = MovieGoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable {
                                            resetEmail = identifier.takeIf { it.contains("@") } ?: ""
                                            onSwitchMode(AuthMode.FORGOT_PASSWORD)
                                        }
                                        .padding(vertical = 4.dp)
                                        .testTag("link_forgot_password")
                                )
                            }
                        }

                        // Age rating selection if Registering
                        if (isRegister) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = Strings.labelAgeRating(language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MovieTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilterChip(
                                    selected = selectedAgeTier == AgeTier.AGE_10_PLUS,
                                    onClick = { selectedAgeTier = AgeTier.AGE_10_PLUS },
                                    label = { Text(Strings.ageOption10(language), fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).testTag("chip_age_10")
                                )
                                FilterChip(
                                    selected = selectedAgeTier == AgeTier.AGE_18_PLUS,
                                    onClick = { selectedAgeTier = AgeTier.AGE_18_PLUS },
                                    label = { Text(Strings.ageOption18(language), fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f).testTag("chip_age_18")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button with Loading State
                        Button(
                            onClick = {
                                if (isRegister) {
                                    val finalIdentifier = if (registerWithPhone) {
                                        val clean = phoneNumber.trim().removePrefix("0")
                                        if (clean.isBlank()) "" else "${selectedCountry.dialCode} $clean"
                                    } else {
                                        emailInput.trim()
                                    }
                                    onRegister(
                                        name,
                                        selectedCountry.displayName(language == Language.SW),
                                        finalIdentifier,
                                        password,
                                        selectedAgeTier,
                                        isEmailCodeVerified
                                    )
                                } else {
                                    onLogin(identifier, password)
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("auth_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAdmin) MovieGold else MovieRedPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = if (isAdmin) Color.Black else Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (language == Language.SW) "Inapakia..." else "Chargement...",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdmin) Color.Black else Color.White
                                )
                            } else {
                                Text(
                                    text = when {
                                        isAdmin -> if (language == Language.SW) "Ingia kama Admin (Gérer APK)" else "Entrer Admin (Gérer APK)"
                                        isRegister -> Strings.btnSignUp(language)
                                        else -> Strings.btnSignIn(language)
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAdmin) Color.Black else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Switch link
                        if (!isAdmin) {
                            Text(
                                text = if (isRegister) Strings.switchHaveAccount(language) else Strings.switchNeedAccount(language),
                                color = MovieGoldLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSwitchMode(if (isRegister) AuthMode.SIGN_IN else AuthMode.SIGN_UP)
                                    }
                                    .padding(vertical = 6.dp)
                                    .testTag("auth_switch_mode_link")
                            )
                        } else {
                            Text(
                                text = if (language == Language.SW) "Rudi kwenye Kuingia Kawaida" else "Retour à la connexion standard",
                                color = MovieTextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSwitchMode(AuthMode.SIGN_IN) }
                                    .padding(vertical = 6.dp)
                            )
                        }

                        if (!isAdmin && !isRegister) {
                            Spacer(modifier = Modifier.height(20.dp))

                            HorizontalDivider(color = MovieDivider)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (language == Language.SW) "Ufikiaji wa Haraka (Mtazamaji / DJ):" else "Accès Rapide (Spectateur / DJ) :",
                                fontSize = 11.sp,
                                color = MovieTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onSwitchMode(AuthMode.SIGN_IN)
                                        identifier = "viewer@moviesw.com"
                                        password = "viewer1234"
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("Viewer Demo", fontSize = 11.sp, color = MovieTextPrimary)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onSwitchMode(AuthMode.SIGN_IN)
                                        identifier = "djafro@moviesw.com"
                                        password = "viewer1234"
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("DJ Demo", fontSize = 11.sp, color = MovieBlueVip)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showCountryPicker) {
            CountryPickerDialog(
                language = language,
                selectedCountry = selectedCountry,
                onSelectCountry = { country ->
                    selectedCountry = country
                    showCountryPicker = false
                },
                onDismiss = { showCountryPicker = false }
            )
        }
    }
}

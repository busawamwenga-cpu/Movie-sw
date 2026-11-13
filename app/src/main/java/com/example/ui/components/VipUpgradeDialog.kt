package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.window.Dialog
import com.example.model.Language
import com.example.model.UserAccount
import com.example.model.VipTier
import com.example.ui.theme.*
import com.example.util.Strings

@Composable
fun VipUpgradeDialog(
    language: Language,
    currentUser: UserAccount?,
    onDismiss: () -> Unit,
    onSelectPlan: (VipTier) -> Unit
) {
    var selectedTier by remember {
        mutableStateOf(
            if (currentUser?.isVipActive() == true) currentUser.vipTier else VipTier.VIP_PRO
        )
    }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            kotlinx.coroutines.delay(350)
            isProcessing = false
            onSelectPlan(selectedTier)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MovieSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MovieBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with close button
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
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP",
                            tint = MovieGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = Strings.upgradeToVip(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MovieTextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MovieTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (language == Language.SW)
                        "Fungua filamu zote za Admin, pakua kwenye kifaa chako na furahia ubora wa hadi 8K!"
                    else
                        "Débloquez tous les films exclusifs, téléchargez sur votre appareil et profitez de la 8K !",
                    fontSize = 13.sp,
                    color = MovieTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tier 1: Bure (Free)
                PlanCard(
                    title = if (language == Language.SW) "Bure (Free)" else "Gratuit",
                    price = if (language == Language.SW) "0 TSh / bure" else "Gratuit",
                    badge = null,
                    features = if (language == Language.SW) listOf(
                        "Filamu za Google Drive tu",
                        "Ubora wa 144p & 360p",
                        "Faili za admin zimefungwa (VIP Tu)",
                        "Hakuna upakuaji kwenye simu"
                    ) else listOf(
                        "Liens Google Drive uniquement",
                        "Qualité standard 144p & 360p",
                        "Fichiers admin verrouillés (Réservé VIP)",
                        "Pas de téléchargement"
                    ),
                    isSelected = selectedTier == VipTier.BURE,
                    isCurrent = currentUser?.vipTier == VipTier.BURE,
                    onSelect = { selectedTier = VipTier.BURE },
                    accentColor = MovieTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tier 2: VIP Pro ($3.99 / mwezi)
                PlanCard(
                    title = "VIP Pro",
                    price = if (language == Language.SW) "3.99 $ / mwezi" else "3.99 $ / mois",
                    badge = if (language == Language.SW) "MAARUFU ZAIDI" else "POPULAIRE",
                    features = if (language == Language.SW) listOf(
                        "Tazama filamu & mfululizo zote (Hata za Admin)",
                        "Ubora wa juu wa 1080p Full HD & 8K Ultra HD",
                        "Upakuaji BILA KIKOMO kwenye kifaa chako",
                        "Usaidizi wa kipaumbele wa VIP kwa mwezi 1"
                    ) else listOf(
                        "Accès complet aux films et séries (incluant admin)",
                        "Haute définition 1080p Full HD & 8K Ultra HD",
                        "Téléchargements ILLIMITÉS sur l'appareil",
                        "Support VIP prioritaire pendant 1 mois"
                    ),
                    isSelected = selectedTier == VipTier.VIP_PRO,
                    isCurrent = currentUser?.vipTier == VipTier.VIP_PRO,
                    onSelect = { selectedTier = VipTier.VIP_PRO },
                    accentColor = MovieGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notice about payment pending API
                Card(
                    colors = CardDefaults.cardColors(containerColor = MovieSurfaceVariant),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = Strings.vipPaymentNotice(language),
                        fontSize = 11.sp,
                        color = MovieTextSecondary,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { if (!isProcessing) isProcessing = true },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_vip_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTier == VipTier.VIP_PRO) MovieGold else MovieRedPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = if (selectedTier == VipTier.VIP_PRO) Color.Black else Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == Language.SW) "Inapakia..." else "Activation...",
                            color = if (selectedTier == VipTier.VIP_PRO) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = if (currentUser?.vipTier == selectedTier)
                                (if (language == Language.SW) "Mpango Wako wa Sasa" else "Votre Forfait Actuel")
                            else
                                (if (language == Language.SW) "Washa ${selectedTier.titleSw} ($3.99)" else "Activer ${selectedTier.titleFr} (3.99 $)"),
                            color = if (selectedTier == VipTier.VIP_PRO) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    badge: String?,
    features: List<String>,
    isSelected: Boolean,
    isCurrent: Boolean,
    onSelect: () -> Unit,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) MovieSurfaceVariant else MovieCardBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else MovieBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onSelect,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = accentColor,
                            unselectedColor = MovieTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MovieTextPrimary
                        )
                        Text(
                            text = price,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                    }
                }

                if (badge != null) {
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else if (isCurrent) {
                    Surface(
                        color = MovieGreenSuccess.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CURRENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MovieGreenSuccess,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = MovieTextSecondary
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated Movie SW Logo featuring the popcorn bucket ("ki saani") tilting
 * and pouring golden popcorn kernels onto the letter "S" ("inamwaga kwa S").
 */
@Composable
fun PopcornPouringLogo(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    autoPlay: Boolean = true,
    interactive: Boolean = true,
    onPourComplete: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var animationTrigger by remember { mutableIntStateOf(0) }

    // Bucket tilt animation: tilts towards S (negative rotation), pours, and springs back
    val bucketTilt = remember { Animatable(0f) }
    // Bucket vertical bounce
    val bucketOffsetY = remember { Animatable(0f) }
    // Kernels pour progression (0f -> 1f)
    val pourProgress = remember { Animatable(0f) }
    // Letter S reaction shake & bounce
    val sShake = remember { Animatable(0f) }
    val sScale = remember { Animatable(1f) }
    val sGlow = remember { Animatable(0f) }

    // Popcorn particles definition for pouring arc
    val kernels = remember {
        listOf(
            KernelData(id = 0, delayFraction = 0.05f, arcHeight = 35f, targetOffsetX = -55f, targetOffsetY = 20f, size = 11f, rotation = 45f),
            KernelData(id = 1, delayFraction = 0.12f, arcHeight = 45f, targetOffsetX = -70f, targetOffsetY = 38f, size = 13f, rotation = 90f),
            KernelData(id = 2, delayFraction = 0.20f, arcHeight = 55f, targetOffsetX = -60f, targetOffsetY = -10f, size = 12f, rotation = 130f),
            KernelData(id = 3, delayFraction = 0.28f, arcHeight = 40f, targetOffsetX = -80f, targetOffsetY = 15f, size = 14f, rotation = 200f),
            KernelData(id = 4, delayFraction = 0.35f, arcHeight = 50f, targetOffsetX = -68f, targetOffsetY = 55f, size = 10f, rotation = 260f),
            KernelData(id = 5, delayFraction = 0.42f, arcHeight = 30f, targetOffsetX = -50f, targetOffsetY = -25f, size = 12f, rotation = 310f),
            KernelData(id = 6, delayFraction = 0.50f, arcHeight = 60f, targetOffsetX = -85f, targetOffsetY = 42f, size = 13f, rotation = 350f),
            KernelData(id = 7, delayFraction = 0.58f, arcHeight = 42f, targetOffsetX = -75f, targetOffsetY = 0f, size = 11f, rotation = 75f)
        )
    }

    suspend fun runPourAnimation() {
        // Reset
        bucketTilt.snapTo(0f)
        bucketOffsetY.snapTo(0f)
        pourProgress.snapTo(0f)
        sShake.snapTo(0f)
        sScale.snapTo(1f)
        sGlow.snapTo(0f)

        delay(150)

        // 1. Initial anticipation: slight dip
        bucketOffsetY.animateTo(4f, animationSpec = tween(120, easing = FastOutSlowInEasing))

        // 2. Tilt bucket sharply towards S ("inamwaga kwa S")
        coroutineScope.launch {
            bucketOffsetY.animateTo(-8f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        }
        bucketTilt.animateTo(
            targetValue = -34f, // 34 degrees tilt towards S
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
        )

        // 3. Pour popcorn kernels in flight towards S
        coroutineScope.launch {
            pourProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = LinearOutSlowInEasing)
            )
        }

        // When kernels start landing on S (around 250ms into pour), S shakes and glows!
        delay(260)
        coroutineScope.launch {
            sGlow.animateTo(1f, animationSpec = tween(200))
            sGlow.animateTo(0f, animationSpec = tween(500))
        }
        coroutineScope.launch {
            sScale.animateTo(1.12f, animationSpec = tween(140))
            sScale.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }

        // Vibration effect on S
        for (i in 0..4) {
            val offset = if (i % 2 == 0) -4f else 4f
            sShake.animateTo(offset, animationSpec = tween(40))
        }
        sShake.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))

        delay(300)

        // 4. Return bucket upright with smooth spring
        coroutineScope.launch {
            bucketOffsetY.animateTo(0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        bucketTilt.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )

        delay(200)
        onPourComplete?.invoke()
    }

    LaunchedEffect(animationTrigger, autoPlay) {
        if (autoPlay || animationTrigger > 0) {
            runPourAnimation()
        }
    }

    // Outer branded frame styled after the user's logo badge (Squircle, red border, dark gradient)
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.22f))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF220A0E),
                        Color(0xFF0F0B10),
                        Color(0xFF050508)
                    )
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFE50914),
                        Color(0xFFFF3344),
                        Color(0xFFB00610),
                        Color(0xFFFF5252),
                        Color(0xFFE50914)
                    )
                ),
                shape = RoundedCornerShape(size * 0.22f)
            )
            .then(
                if (interactive) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        animationTrigger++
                    }
                } else Modifier
            )
            .testTag("popcorn_pouring_logo"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle cinema light sheen on top and bottom of badge
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(this.size.width * 0.5f, this.size.height * 0.18f),
                    radius = this.size.width * 0.35f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(this.size.width * 0.5f, this.size.height * 0.82f),
                    radius = this.size.width * 0.3f
                )
            )
        }

        // Center Logo Row: S  [Bucket]  W
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            // Letter "S" (with shake, scale, and golden burst reaction)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(sScale.value)
                    .rotate(sShake.value)
            ) {
                // Glow aura when popcorn hits S
                if (sGlow.value > 0f) {
                    Canvas(modifier = Modifier.size(70.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD54F).copy(alpha = 0.6f * sGlow.value),
                                    Color(0xFFE50914).copy(alpha = 0.3f * sGlow.value),
                                    Color.Transparent
                                )
                            ),
                            radius = size.toPx() * 0.2f
                        )
                    }
                }

                Text(
                    text = "S",
                    fontSize = (size.value * 0.38f).sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(size * 0.02f))

            // Popcorn Bucket ("ki saani") Container with Tilt & Pouring Particles
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = bucketOffsetY.value.dp)
                    .rotate(bucketTilt.value)
            ) {
                // The Popcorn Bucket Drawing
                PopcornBucketCanvas(size = size * 0.36f)

                // Pouring Kernels Canvas ("inamwaga kwa S")
                if (pourProgress.value > 0f && pourProgress.value < 1f) {
                    Canvas(
                        modifier = Modifier
                            .size(size * 0.8f)
                            .offset(x = (-size * 0.18f))
                    ) {
                        val progress = pourProgress.value
                        val originX = this.size.width * 0.62f
                        val originY = this.size.height * 0.35f

                        kernels.forEach { k ->
                            if (progress >= k.delayFraction) {
                                val localT = ((progress - k.delayFraction) / (1f - k.delayFraction)).coerceIn(0f, 1f)

                                // Parabolic trajectory towards S
                                val currentX = originX + (k.targetOffsetX * localT * (size.toPx() / 200f))
                                val currentY = originY + (k.targetOffsetY * localT * (size.toPx() / 200f)) -
                                        (sin(localT * Math.PI).toFloat() * k.arcHeight * (size.toPx() / 200f))

                                val alpha = if (localT > 0.85f) ((1f - localT) / 0.15f) else 1f

                                // Draw fluffy popcorn kernel
                                drawPopcornKernel(
                                    center = Offset(currentX, currentY),
                                    kernelSize = k.size * (size.toPx() / 200f),
                                    rotation = k.rotation + (localT * 360f),
                                    alpha = alpha
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(size * 0.02f))

            // Letter "W"
            Text(
                text = "W",
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

/**
 * Draws the red-and-white striped cinema popcorn bucket overflowing with fluffy popcorn.
 */
@Composable
private fun PopcornBucketCanvas(size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val bucketTop = h * 0.34f
        val bucketBottom = h * 0.94f
        val topWidth = w * 0.72f
        val bottomWidth = w * 0.54f

        val topStartX = (w - topWidth) / 2f
        val topEndX = topStartX + topWidth
        val bottomStartX = (w - bottomWidth) / 2f
        val bottomEndX = bottomStartX + bottomWidth

        // 1. Red Bucket Body (Trapezoid)
        val bucketPath = Path().apply {
            moveTo(topStartX, bucketTop)
            lineTo(topEndX, bucketTop)
            lineTo(bottomEndX, bucketBottom)
            lineTo(bottomStartX, bucketBottom)
            close()
        }
        drawPath(
            path = bucketPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE50914), Color(0xFFB00610), Color(0xFF8B0000))
            )
        )

        // 2. White Stripes on the bucket
        val stripeCount = 5
        for (i in 0 until stripeCount) {
            if (i % 2 == 1) {
                val f1 = i.toFloat() / stripeCount
                val f2 = (i + 1).toFloat() / stripeCount

                val sTopStart = topStartX + (topWidth * f1)
                val sTopEnd = topStartX + (topWidth * f2)
                val sBottomStart = bottomStartX + (bottomWidth * f1)
                val sBottomEnd = bottomStartX + (bottomWidth * f2)

                val stripePath = Path().apply {
                    moveTo(sTopStart, bucketTop)
                    lineTo(sTopEnd, bucketTop)
                    lineTo(sBottomEnd, bucketBottom)
                    lineTo(sBottomStart, bucketBottom)
                    close()
                }
                drawPath(
                    path = stripePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFEDEDED), Color(0xFFD4D4D4))
                    )
                )
            }
        }

        // 3. Top Rim of the bucket
        drawRoundRect(
            color = Color(0xFFFF2233),
            topLeft = Offset(topStartX - (w * 0.02f), bucketTop - (h * 0.04f)),
            size = androidx.compose.ui.geometry.Size(topWidth + (w * 0.04f), h * 0.07f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )

        // 4. Overflowing Popcorn Heaps on top
        val popcornColors = listOf(
            Color(0xFFFFF9C4), // Light cream
            Color(0xFFFFEE58), // Golden corn
            Color(0xFFFFD54F), // Butter gold
            Color(0xFFFFB300)  // Toasted kernel
        )

        // Fluffy clouds of popcorn
        val kernelsPositions = listOf(
            Offset(w * 0.28f, h * 0.30f) to (w * 0.12f),
            Offset(w * 0.40f, h * 0.22f) to (w * 0.13f),
            Offset(w * 0.52f, h * 0.18f) to (w * 0.14f),
            Offset(w * 0.64f, h * 0.24f) to (w * 0.13f),
            Offset(w * 0.72f, h * 0.32f) to (w * 0.11f),
            Offset(w * 0.36f, h * 0.33f) to (w * 0.12f),
            Offset(w * 0.50f, h * 0.28f) to (w * 0.13f),
            Offset(w * 0.62f, h * 0.34f) to (w * 0.11f)
        )

        kernelsPositions.forEachIndexed { idx, (pos, radius) ->
            // Base cloud
            drawCircle(
                color = popcornColors[idx % popcornColors.size],
                radius = radius,
                center = pos
            )
            // Kernel heart/texture
            drawCircle(
                color = Color(0xFFFFB300).copy(alpha = 0.6f),
                radius = radius * 0.35f,
                center = Offset(pos.x + (radius * 0.1f), pos.y + (radius * 0.1f))
            )
        }
    }
}

/**
 * Draws an individual flying popcorn piece during the pouring animation.
 */
private fun DrawScope.drawPopcornKernel(
    center: Offset,
    kernelSize: Float,
    rotation: Float,
    alpha: Float
) {
    if (alpha <= 0.01f) return

    val baseColor = Color(0xFFFFEE58).copy(alpha = alpha)
    val butterColor = Color(0xFFFFB300).copy(alpha = alpha)
    val highlightColor = Color(0xFFFFF9C4).copy(alpha = alpha)

    val rad = Math.toRadians(rotation.toDouble())
    val cosR = cos(rad).toFloat()
    val sinR = sin(rad).toFloat()

    fun rotatePoint(dx: Float, dy: Float): Offset {
        return Offset(
            center.x + (dx * cosR - dy * sinR),
            center.y + (dx * sinR + dy * cosR)
        )
    }

    // 3 interlocking puffs for a realistic popcorn kernel
    drawCircle(color = baseColor, radius = kernelSize * 0.55f, center = rotatePoint(-kernelSize * 0.3f, 0f))
    drawCircle(color = highlightColor, radius = kernelSize * 0.6f, center = rotatePoint(kernelSize * 0.2f, -kernelSize * 0.2f))
    drawCircle(color = baseColor, radius = kernelSize * 0.55f, center = rotatePoint(kernelSize * 0.1f, kernelSize * 0.3f))
    // Butter center
    drawCircle(color = butterColor, radius = kernelSize * 0.28f, center = center)
}

private data class KernelData(
    val id: Int,
    val delayFraction: Float,
    val arcHeight: Float,
    val targetOffsetX: Float,
    val targetOffsetY: Float,
    val size: Float,
    val rotation: Float
)

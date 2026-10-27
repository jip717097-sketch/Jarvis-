package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisStatus
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisCyanDim
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun JarvisArcReactor(
    status: JarvisStatus,
    audioWave: Float,
    isListening: Boolean,
    isSpeaking: Boolean,
    onReactorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactorAnimations")

    // Slow clockwise rotation for outer tech ring
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRingRotation"
    )

    // Faster counter-clockwise rotation for middle segmented ring
    val midRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "midRingRotation"
    )

    // Core pulsing animation
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    // Dynamic scale driven by audioWave and status
    val dynamicAudioScale = 1f + (audioWave * 0.28f)
    val effectivePulse = if (isListening || isSpeaking) dynamicAudioScale else ambientPulse

    val coreGlowColor = when {
        status == JarvisStatus.ALERT -> JarvisRed
        isListening -> JarvisCyanBright
        isSpeaking -> JarvisGold
        else -> JarvisCyan
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Futuristic Multi-Layer Canvas HUD
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.minDimension / 2f - 12.dp.toPx()

                // 1. Outermost Ambient Glow Halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            coreGlowColor.copy(alpha = 0.25f * effectivePulse.coerceIn(0.7f, 1.4f)),
                            coreGlowColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.08f
                    ),
                    radius = baseRadius * 1.08f,
                    center = center
                )

                // 2. Outer Rotating Tech Ticks
                rotate(outerRotation, center) {
                    val tickRadius = baseRadius
                    val tickCount = 36
                    for (i in 0 until tickCount) {
                        val angleDeg = (i * (360f / tickCount))
                        val angleRad = Math.toRadians(angleDeg.toDouble())
                        val isMajor = i % 3 == 0
                        val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                        val strokeW = if (isMajor) 2.dp.toPx() else 1.dp.toPx()

                        val startX = center.x + (tickRadius - tickLen) * cos(angleRad).toFloat()
                        val startY = center.y + (tickRadius - tickLen) * sin(angleRad).toFloat()
                        val endX = center.x + tickRadius * cos(angleRad).toFloat()
                        val endY = center.y + tickRadius * sin(angleRad).toFloat()

                        drawLine(
                            color = if (isMajor) coreGlowColor.copy(alpha = 0.85f) else JarvisCyanDim.copy(alpha = 0.4f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )
                    }

                    // Outer thin ring
                    drawCircle(
                        color = coreGlowColor.copy(alpha = 0.45f),
                        radius = baseRadius,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // 3. Middle Segmented Arcs (Counter-Rotating)
                rotate(midRotation, center) {
                    val midRadius = baseRadius * 0.82f
                    val arcSegments = 4
                    val sweepAngle = 65f
                    val gapAngle = (360f - (arcSegments * sweepAngle)) / arcSegments

                    for (i in 0 until arcSegments) {
                        val startAngle = i * (sweepAngle + gapAngle)
                        drawArc(
                            color = if (i % 2 == 0) coreGlowColor else JarvisCyanDim,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(center.x - midRadius, center.y - midRadius),
                            size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2)
                        )
                    }

                    // Subtle dot markers on segment borders
                    for (i in 0 until arcSegments) {
                        val dotAngle = Math.toRadians((i * (sweepAngle + gapAngle) + sweepAngle / 2).toDouble())
                        val dotX = center.x + midRadius * cos(dotAngle).toFloat()
                        val dotY = center.y + midRadius * sin(dotAngle).toFloat()
                        drawCircle(
                            color = JarvisGold,
                            radius = 2.5.dp.toPx(),
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                // 4. Inner Ring with Arc Reactor Grid Spokes
                val innerRadius = baseRadius * 0.62f
                drawCircle(
                    color = coreGlowColor.copy(alpha = 0.7f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 8 Diagonal energy spokes
                val spokeCount = 8
                for (s in 0 until spokeCount) {
                    val spokeAngle = Math.toRadians((s * 45.0) + (outerRotation * 0.35))
                    val innerStart = innerRadius * 0.45f
                    val p1 = Offset(
                        center.x + innerStart * cos(spokeAngle).toFloat(),
                        center.y + innerStart * sin(spokeAngle).toFloat()
                    )
                    val p2 = Offset(
                        center.x + innerRadius * cos(spokeAngle).toFloat(),
                        center.y + innerRadius * sin(spokeAngle).toFloat()
                    )
                    drawLine(
                        color = coreGlowColor.copy(alpha = 0.6f),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                // 5. Central Reactor Core Orb (Pulsing with audio)
                val coreRadius = (innerRadius * 0.40f) * effectivePulse
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            coreGlowColor,
                            coreGlowColor.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = coreRadius * 1.2f
                    ),
                    radius = coreRadius,
                    center = center
                )
            }

            // Interactive Center Tap Target
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onReactorClick
                    )
                    .testTag("voice_assistant_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop voice listening" else "Start voice listening",
                    tint = if (isListening) JarvisRed else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reactor State Label & Interaction Hint
        Text(
            text = when {
                status == JarvisStatus.ALERT -> "[ INCOMING CALL PROTOCOL ]"
                isListening -> "● LISTENING TO SPEECH..."
                isSpeaking -> "▲ JARVIS SPEAKING..."
                status == JarvisStatus.PROCESSING -> "■ ANALYZING COMMAND..."
                else -> "ARC REACTOR ONLINE // TAP TO SPEAK"
            },
            fontFamily = OrbitronFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.4.sp,
            color = coreGlowColor
        )

        Text(
            text = if (isListening) "Say Hindi or English command" else "Tap reactor or say: \"Jarvis, YouTube kholo\"",
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 11.sp,
            color = JarvisTextSecondary
        )
    }
}

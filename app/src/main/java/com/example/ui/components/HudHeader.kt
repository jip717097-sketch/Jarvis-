package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JarvisStatus
import com.example.model.SystemTelemetry
import com.example.model.VoiceLanguage
import com.example.ui.theme.JarvisBackgroundSecondary
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily

@Composable
fun HudHeader(
    status: JarvisStatus,
    telemetry: SystemTelemetry,
    selectedLanguage: VoiceLanguage,
    isMuted: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (status) {
            JarvisStatus.LISTENING -> JarvisCyanBright
            JarvisStatus.SPEAKING -> JarvisGold
            JarvisStatus.ALERT -> JarvisRed
            JarvisStatus.ONLINE -> JarvisGreen
            JarvisStatus.PROCESSING -> JarvisOrange
            JarvisStatus.INITIALIZING -> JarvisCyan
        },
        label = "statusColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(JarvisSurfaceBorder, JarvisCyanGlow, JarvisSurfaceBorder)
                ),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            ),
        color = JarvisSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Row: Brand & Status & Audio Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Stark Brand + Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pulsing LED indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = pulseAlpha))
                            .border(1.dp, statusColor, CircleShape)
                    )

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "STARK HUD",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp,
                                color = JarvisCyan
                            )
                            Text(
                                text = " // JARVIS",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 11.sp,
                                color = JarvisTextSecondary
                            )
                        }
                        Text(
                            text = status.name,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Right: Controls (Language Toggle & Mute Toggle)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Switch Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisBackgroundSecondary)
                            .border(1.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onToggleLanguage() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (selectedLanguage == VoiceLanguage.ENGLISH) "EN" else "HI",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = JarvisCyan
                        )
                    }

                    // Mute / Unmute Button
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisBackgroundSecondary)
                            .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Jarvis" else "Mute Jarvis",
                            tint = if (isMuted) JarvisRed else JarvisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Sub-Row: Digital Clock, Date, Battery, Network
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clock & Date Readout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = telemetry.currentTime,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = JarvisTextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = " | ${telemetry.currentDate}",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = JarvisTextMuted
                    )
                }

                // Battery & Network Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Battery
                    val batteryColor = when {
                        telemetry.batteryPercent <= 20 -> JarvisRed
                        telemetry.batteryPercent <= 40 -> JarvisOrange
                        else -> JarvisGreen
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (telemetry.isCharging) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Charging",
                                tint = JarvisGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "${telemetry.batteryPercent}%",
                            fontFamily = ShareTechMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = batteryColor
                        )
                    }

                    // Network
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(JarvisBackgroundSecondary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = telemetry.networkType,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 9.sp,
                            color = if (telemetry.isNetworkAvailable) JarvisCyan else JarvisRed
                        )
                    }
                }
            }
        }
    }
}

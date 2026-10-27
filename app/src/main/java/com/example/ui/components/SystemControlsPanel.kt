package com.example.ui.components

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SystemTelemetry
import com.example.ui.theme.JarvisBackgroundSecondary
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily

@Composable
fun SystemControlsPanel(
    telemetry: SystemTelemetry,
    onToggleFlashlight: () -> Unit,
    onOpenSetting: (String) -> Unit,
    onOpenPermissionDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(12.dp)),
        color = JarvisSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Panel Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "System Diagnostics",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SYSTEM HARDWARE & SETTINGS",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = JarvisCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisBackgroundSecondary)
                        .border(1.dp, JarvisGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable { onOpenPermissionDialog() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("permissions_hub_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Permissions",
                            tint = JarvisGold,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "SECURITY HUD",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = JarvisGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Flashlight Hardware Controller
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisBackgroundSecondary)
                    .border(
                        1.dp,
                        if (telemetry.isFlashlightOn) JarvisGold else JarvisSurfaceBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (telemetry.isFlashlightOn) JarvisGold.copy(alpha = 0.2f) else JarvisSurface)
                                .border(1.dp, if (telemetry.isFlashlightOn) JarvisGold else JarvisSurfaceBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (telemetry.isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                contentDescription = "Flashlight",
                                tint = if (telemetry.isFlashlightOn) JarvisGold else JarvisTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "FLASHLIGHT ILLUMINATOR",
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = JarvisTextPrimary
                            )
                            Text(
                                text = if (telemetry.isFlashlightOn) "BEAM ACTIVE // Torch Engaged" else "BEAM STANDBY // Say \"Flashlight on\"",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 10.sp,
                                color = if (telemetry.isFlashlightOn) JarvisGold else JarvisTextMuted
                            )
                        }
                    }

                    Switch(
                        checked = telemetry.isFlashlightOn,
                        onCheckedChange = { onToggleFlashlight() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JarvisGold,
                            checkedTrackColor = JarvisGold.copy(alpha = 0.35f),
                            uncheckedThumbColor = JarvisTextMuted,
                            uncheckedTrackColor = JarvisSurface
                        ),
                        modifier = Modifier.testTag("flashlight_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // RAM Diagnostic Meter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisBackgroundSecondary)
                    .padding(10.dp)
            ) {
                val usedRamMb = (telemetry.totalRamMb - telemetry.availableRamMb).coerceAtLeast(0)
                val ramFraction = if (telemetry.totalRamMb > 0) {
                    (usedRamMb.toFloat() / telemetry.totalRamMb.toFloat()).coerceIn(0f, 1f)
                } else 0.5f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYNAPTIC RAM BUFFER",
                        fontFamily = OrbitronFontFamily,
                        fontSize = 10.sp,
                        color = JarvisCyan
                    )
                    Text(
                        text = "${usedRamMb}MB / ${telemetry.totalRamMb}MB (${(ramFraction * 100).toInt()}%)",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = JarvisTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { ramFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (ramFraction > 0.85f) JarvisGold else JarvisCyan,
                    trackColor = JarvisSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Settings Shortcuts Grid (Wi-Fi, Bluetooth, Audio, System)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingShortcutButton(
                    label = "WI-FI",
                    icon = Icons.Default.Wifi,
                    onClick = { onOpenSetting(Settings.ACTION_WIFI_SETTINGS) },
                    modifier = Modifier.weight(1f),
                    testTag = "setting_wifi_button"
                )

                SettingShortcutButton(
                    label = "BLUETOOTH",
                    icon = Icons.Default.Bluetooth,
                    onClick = { onOpenSetting(Settings.ACTION_BLUETOOTH_SETTINGS) },
                    modifier = Modifier.weight(1f),
                    testTag = "setting_bluetooth_button"
                )

                SettingShortcutButton(
                    label = "AUDIO",
                    icon = Icons.Default.VolumeUp,
                    onClick = { onOpenSetting(Settings.ACTION_SOUND_SETTINGS) },
                    modifier = Modifier.weight(1f),
                    testTag = "setting_sound_button"
                )

                SettingShortcutButton(
                    label = "SETTINGS",
                    icon = Icons.Default.Settings,
                    onClick = { onOpenSetting(Settings.ACTION_SETTINGS) },
                    modifier = Modifier.weight(1f),
                    testTag = "setting_main_button"
                )
            }
        }
    }
}

@Composable
private fun SettingShortcutButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisBackgroundSecondary)
            .border(1.dp, JarvisSurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = JarvisCyan,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 9.sp,
                color = JarvisTextPrimary
            )
        }
    }
}

package com.example.ui.components

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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import com.example.model.AppLauncherItem
import com.example.ui.theme.JarvisBackgroundSecondary
import com.example.ui.theme.JarvisCyan
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
fun AppLauncherMatrix(
    apps: List<AppLauncherItem>,
    onLaunchApp: (packageName: String, appName: String, fallbackUrl: String?) -> Unit,
    onLaunchCamera: () -> Unit,
    onLaunchDialer: () -> Unit,
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
            // Header
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
                        imageVector = Icons.Default.Apps,
                        contentDescription = "Applications",
                        tint = JarvisCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "APPLICATION CONTROL MATRIX",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = JarvisCyan
                    )
                }

                Text(
                    text = "${apps.count { it.isInstalled }} / ${apps.size} INSTALLED",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = JarvisTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2-Column Grid of Target Applications
            val rows = apps.chunked(2)
            rows.forEach { rowApps ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowApps.forEach { app ->
                        AppMatrixItem(
                            app = app,
                            onClick = { onLaunchApp(app.packageName, app.name, app.fallbackWebUrl) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowApps.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Quick Hardware Shortcuts: Camera & Phone Dialer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Camera
                QuickHardwareButton(
                    label = "CAMERA SENSOR",
                    subtext = "\"Camera kholo\"",
                    icon = Icons.Default.CameraAlt,
                    accentColor = JarvisCyan,
                    onClick = onLaunchCamera,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_camera_button"
                )

                // Dialer
                QuickHardwareButton(
                    label = "PHONE DIALER",
                    subtext = "\"Call dialer kholo\"",
                    icon = Icons.Default.Phone,
                    accentColor = JarvisGold,
                    onClick = onLaunchDialer,
                    modifier = Modifier.weight(1f),
                    testTag = "quick_dialer_button"
                )
            }
        }
    }
}

@Composable
private fun AppMatrixItem(
    app: AppLauncherItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (app.id) {
        "youtube" -> Icons.Default.PlayArrow
        "instagram" -> Icons.Default.CameraAlt
        "whatsapp" -> Icons.Default.Message
        "facebook" -> Icons.Default.Share
        "freefire" -> Icons.Default.Games
        else -> Icons.Default.Language
    }

    val iconColor = when (app.id) {
        "youtube" -> JarvisRed
        "freefire" -> JarvisGold
        "whatsapp" -> JarvisGreen
        "instagram" -> JarvisOrange
        else -> JarvisCyan
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisBackgroundSecondary)
            .border(
                1.dp,
                if (app.isInstalled) JarvisCyan.copy(alpha = 0.35f) else JarvisSurfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(8.dp)
            .testTag("app_item_${app.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f))
                    .border(1.dp, iconColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = app.name,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.name,
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = JarvisTextPrimary
                    )

                    // Status Indicator
                    if (app.isInstalled) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Installed",
                            tint = JarvisGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Not Installed",
                            tint = JarvisOrange,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Text(
                    text = app.hindiCommand,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = JarvisTextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun QuickHardwareButton(
    label: String,
    subtext: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisBackgroundSecondary)
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
            .testTag(testTag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = JarvisTextPrimary
                )
                Text(
                    text = subtext,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = JarvisTextMuted
                )
            }
        }
    }
}

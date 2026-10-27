package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBackgroundSecondary
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.JarvisTextMuted
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily

@Composable
fun PermissionDialog(
    onRequestAllPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, JarvisCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
            color = JarvisSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = JarvisCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "STARK SECURITY CLEARANCE",
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = JarvisCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Jarvis requires authorization to access device hardware sensors and communication protocols:",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = JarvisTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                PermissionRowItem(
                    icon = Icons.Default.Mic,
                    title = "MICROPHONE (Audio Input)",
                    desc = "Real-time SpeechRecognizer in Hindi & English"
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionRowItem(
                    icon = Icons.Default.Phone,
                    title = "TELEPHONY & CALL STATE",
                    desc = "Announce incoming calls (\"Sir, aapka phone aaya hai\")"
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionRowItem(
                    icon = Icons.Default.CameraAlt,
                    title = "OPTICAL & TORCH (Camera)",
                    desc = "Flashlight control & camera sensor launcher"
                )

                Spacer(modifier = Modifier.height(8.dp))

                PermissionRowItem(
                    icon = Icons.Default.Notifications,
                    title = "NOTIFICATIONS (Alerts)",
                    desc = "Post background call and diagnostic alerts"
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = JarvisTextMuted
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisSurfaceBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "CLOSE",
                            fontFamily = OrbitronFontFamily,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            onRequestAllPermissions()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("grant_permissions_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JarvisCyan,
                            contentColor = JarvisBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AUTHORIZE",
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRowItem(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(JarvisBackgroundSecondary)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = JarvisGold,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = OrbitronFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = JarvisTextPrimary
                )
                Text(
                    text = desc,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = JarvisTextMuted
                )
            }
        }
    }
}

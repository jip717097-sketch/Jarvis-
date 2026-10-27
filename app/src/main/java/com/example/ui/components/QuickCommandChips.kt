package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JarvisBackgroundSecondary
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanFaint
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisSurfaceBorder
import com.example.ui.theme.ShareTechMonoFontFamily

data class QuickCommand(
    val label: String,
    val commandText: String,
    val isAlertOrSpecial: Boolean = false,
    val isGold: Boolean = false
)

@Composable
fun QuickCommandChips(
    onCommandClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val commands = listOf(
        QuickCommand("▶ YouTube Kholo", "Jarvis, YouTube kholo"),
        QuickCommand("📸 Instagram Kholo", "Jarvis, Instagram kholo"),
        QuickCommand("💬 WhatsApp Kholo", "Jarvis, WhatsApp kholo"),
        QuickCommand("👥 Facebook Kholo", "Jarvis, Facebook kholo"),
        QuickCommand("🔥 Free Fire MAX", "Jarvis, Free Fire MAX kholo", isGold = true),
        QuickCommand("🌐 Browser Kholo", "Jarvis, browser kholo"),
        QuickCommand("⚡ Battery Kitni Hai?", "Jarvis, battery kitni hai?"),
        QuickCommand("🕒 Time Batao", "Jarvis, time batao"),
        QuickCommand("💡 Torch Toggle", "Jarvis, flashlight on karo"),
        QuickCommand("⚙️ Settings Kholo", "Jarvis, settings kholo"),
        QuickCommand("📞 Phone Call Test", "SIMULATE_CALL", isAlertOrSpecial = true)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        commands.forEach { cmd ->
            val borderColor = when {
                cmd.isAlertOrSpecial -> JarvisRed.copy(alpha = 0.6f)
                cmd.isGold -> JarvisGold.copy(alpha = 0.7f)
                else -> JarvisCyan.copy(alpha = 0.4f)
            }

            val textColor = when {
                cmd.isAlertOrSpecial -> JarvisRed
                cmd.isGold -> JarvisGold
                else -> JarvisCyan
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisBackgroundSecondary)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable { onCommandClick(cmd.commandText) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("quick_chip_${cmd.label.filter { it.isLetterOrDigit() }}")
            ) {
                Text(
                    text = cmd.label,
                    fontFamily = ShareTechMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = textColor
                )
            }
        }
    }
}

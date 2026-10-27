package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanBright
import com.example.ui.theme.JarvisGold
import kotlin.math.sin

@Composable
fun SoundWaveVisualizer(
    amplitude: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 21

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            // Wave curvature formula from center
            val distanceFromCenter = kotlin.math.abs(i - (barCount / 2f)) / (barCount / 2f)
            val bellCurve = 1.0f - (distanceFromCenter * 0.65f)

            // Dynamic height based on amplitude
            val targetFraction = if (isActive) {
                val waveOffset = sin((i * 0.7f) + (amplitude * 10f)).toFloat() * 0.25f
                ((amplitude * 0.85f * bellCurve) + 0.15f + waveOffset).coerceIn(0.12f, 1.0f)
            } else {
                0.10f
            }

            val animatedHeightFraction by animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = spring(stiffness = 500f),
                label = "barHeight_$i"
            )

            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(animatedHeightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                JarvisCyanBright,
                                if (isActive && amplitude > 0.6f) JarvisGold else JarvisCyan,
                                JarvisCyan.copy(alpha = 0.4f)
                            )
                        )
                    )
            )

            if (i < barCount - 1) {
                Box(modifier = Modifier.width(3.dp))
            }
        }
    }
}

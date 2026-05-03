package com.insieme.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun FlowerIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    centerColor: Color = MaterialTheme.colorScheme.tertiary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flower_anim")
    
    // Wobble effect: slight rotation back and forth
    val wobble by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val petalRadius = (size.width / 3) * scale
        val centerRadius = (size.width / 6) * scale

        // Combine continuous rotation with the wobble
        rotate(rotation + wobble, center) {
            for (i in 0 until 6) {
                val angle = i * (2 * Math.PI / 6)
                val petalCenter = Offset(
                    center.x + (petalRadius * cos(angle)).toFloat(),
                    center.y + (petalRadius * sin(angle)).toFloat()
                )
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color, color.copy(alpha = 0.3f)),
                        center = petalCenter,
                        radius = petalRadius
                    ),
                    radius = petalRadius,
                    center = petalCenter
                )
            }
        }

        // Draw flower center with a gentle glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(centerColor, centerColor.copy(alpha = 0.4f)),
                center = center,
                radius = centerRadius * 2f
            ),
            radius = centerRadius,
            center = center
        )
        
        // Highlight for "premium" gloss
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = centerRadius * 0.4f,
            center = Offset(center.x - centerRadius * 0.3f, center.y - centerRadius * 0.3f)
        )
    }
}


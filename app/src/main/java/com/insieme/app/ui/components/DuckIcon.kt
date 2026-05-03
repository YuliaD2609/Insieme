package com.insieme.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun DuckIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFEB3B) // Yellow
) {
    val infiniteTransition = rememberInfiniteTransition(label = "duck_anim")
    
    // Floating bobbing effect
    val bobbing by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = SineWaveEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )

    // Quacking beak effect
    val quack by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "quack"
    )

    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val verticalOffset = bobbing.dp.toPx()

        // Body
        drawCircle(
            color = color,
            radius = w * 0.35f,
            center = Offset(w * 0.45f, h * 0.6f + verticalOffset)
        )

        // Head
        drawCircle(
            color = color,
            radius = w * 0.22f,
            center = Offset(w * 0.7f, h * 0.35f + verticalOffset)
        )

        // Beak (Orange) - Quacking
        val beakPath = Path().apply {
            moveTo(w * 0.85f, h * 0.35f + verticalOffset)
            lineTo(w * 0.98f, h * (0.38f) + verticalOffset + quack)
            lineTo(w * 0.85f, h * 0.45f + verticalOffset)
            close()
        }
        drawPath(beakPath, color = Color(0xFFFF9800))

        // Eye (Black)
        drawCircle(
            color = Color.Black,
            radius = w * 0.03f,
            center = Offset(w * 0.75f, h * 0.3f + verticalOffset)
        )
    }
}

val SineWaveEasing = Easing { f ->
    kotlin.math.sin(f * Math.PI.toFloat() * 1f) / 2f + 0.5f
}

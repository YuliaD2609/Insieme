package com.insieme.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FlowerIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    centerColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val petalRadius = size.width / 3
        val centerRadius = size.width / 6

        // Draw 5 petals
        for (i in 0 until 5) {
            val angle = i * (2 * Math.PI / 5)
            val petalCenter = Offset(
                center.x + (petalRadius * cos(angle)).toFloat(),
                center.y + (petalRadius * sin(angle)).toFloat()
            )
            drawCircle(
                color = color.copy(alpha = 0.8f),
                radius = petalRadius,
                center = petalCenter
            )
        }

        // Draw flower center
        drawCircle(
            color = centerColor,
            radius = centerRadius,
            center = center
        )
    }
}

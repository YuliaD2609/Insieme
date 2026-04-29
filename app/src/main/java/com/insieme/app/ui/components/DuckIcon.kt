package com.insieme.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
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
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height

        // Body
        drawCircle(
            color = color,
            radius = w * 0.35f,
            center = Offset(w * 0.45f, h * 0.6f)
        )

        // Head
        drawCircle(
            color = color,
            radius = w * 0.22f,
            center = Offset(w * 0.7f, h * 0.35f)
        )

        // Beak (Orange)
        val beakPath = Path().apply {
            moveTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.98f, h * 0.38f)
            lineTo(w * 0.85f, h * 0.45f)
            close()
        }
        drawPath(beakPath, color = Color(0xFFFF9800))

        // Eye (Black)
        drawCircle(
            color = Color.Black,
            radius = w * 0.03f,
            center = Offset(w * 0.75f, h * 0.3f)
        )
    }
}

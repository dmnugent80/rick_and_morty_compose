package com.example.rickandmortycompose.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.rickandmortycompose.ui.theme.RickAndMortySurface

private val ShimmerBaseColor = RickAndMortySurface
private val ShimmerHighlightColor = RickAndMortySurface.copy(alpha = 1f).let {
    androidx.compose.ui.graphics.Color(0xFF2A2A2A)
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    this.drawWithCache {
        // Diagonal shimmer moving across the element
        val width = size.width
        val height = size.height

        val startX = (-width) + (2f * width * progress)
        val startY = (-height) + (2f * height * progress)

        val brush = Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(startX, startY),
            end = Offset(startX + width, startY + height)
        )

        onDrawBehind {
            drawRect(brush)
        }
    }
}

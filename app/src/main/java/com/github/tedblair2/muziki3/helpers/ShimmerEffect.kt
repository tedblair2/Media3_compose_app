package com.github.tedblair2.muziki3.helpers

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun ShimmerEffect(
    content: @Composable (brush: Brush) -> Unit
) {
    val shimmerColors=remember {
        listOf(
            Color.LightGray.copy(alpha = 0.6f),
            Color.LightGray.copy(alpha = 0.2f),
            Color.LightGray.copy(alpha = 0.6f)
        )
    }
    val transition= rememberInfiniteTransition(label = "shimmer")
    val transitionAnim=transition.animateFloat(
        initialValue = 0f ,
        targetValue = 1000f ,
        animationSpec = infiniteRepeatable(
            repeatMode = RepeatMode.Reverse,
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        ),
        label = "shimmerInfinite"
    )

    val brush=Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = transitionAnim.value, y = transitionAnim.value)
    )
    content(brush)
}
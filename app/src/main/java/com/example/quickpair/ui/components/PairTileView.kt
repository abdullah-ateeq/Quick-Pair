package com.example.quickpair.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.quickpair.model.PairTile
import kotlin.math.roundToInt

@Composable
fun PairTileView(
    tile: PairTile,
    isShaking: Boolean,
    isMatching: Boolean,
    reducedMotion: Boolean,
    iconSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isShaking) {
        if (isShaking) {
            if (reducedMotion) {
                shakeOffset.snapTo(0f)
            } else {
                shakeOffset.animateTo(12f, tween(40, easing = LinearEasing))
                shakeOffset.animateTo(-12f, tween(40, easing = LinearEasing))
                shakeOffset.animateTo(8f, tween(40, easing = LinearEasing))
                shakeOffset.animateTo(-8f, tween(40, easing = LinearEasing))
                shakeOffset.animateTo(0f, tween(40, easing = LinearEasing))
            }
        }
    }

    // Matched animation: scale down and fade out
    val scale by animateFloatAsState(
        targetValue = when {
            tile.matched -> 0.0f
            isMatching -> if (reducedMotion) 1.0f else 1.15f
            tile.selected -> if (reducedMotion) 1.0f else 1.04f
            else -> 1.0f
        },
        animationSpec = tween(if (reducedMotion) 100 else 220),
        label = "tileScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (tile.matched) 0.0f else 1.0f,
        animationSpec = tween(if (reducedMotion) 100 else 220),
        label = "tileAlpha"
    )

    if (tile.matched && alpha <= 0.01f) {
        // Empty slot once matched and animation completes
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .padding(3.dp)
        )
        return
    }

    val symbolColor = getSymbolColor(tile.symbol)

    val borderColor = when {
        isShaking -> MaterialTheme.colorScheme.error
        tile.selected -> symbolColor
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    val borderWidth = when {
        isShaking -> 2.5.dp
        tile.selected -> 2.5.dp
        else -> 1.dp
    }

    val backgroundColor = when {
        isShaking -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        tile.selected -> symbolColor.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !tile.matched,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Symbol Icon
        PairSymbolIcon(
            symbol = tile.symbol,
            size = iconSize,
            tint = symbolColor
        )

        // Selected checkmark indicator on top corner
        if (tile.selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(16.dp)
                    .background(symbolColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(9.dp)) {
                    val w = this.size.width
                    val h = this.size.height
                    val checkmarkPath = Path().apply {
                        moveTo(w * 0.15f, h * 0.55f)
                        lineTo(w * 0.42f, h * 0.85f)
                        lineTo(w * 0.88f, h * 0.2f)
                    }
                    drawPath(
                        path = checkmarkPath,
                        color = Color.White,
                        style = Stroke(
                            width = 2f,
                            cap = StrokeCap.Round
                        )
                    )
                }
            }
        }
    }
}

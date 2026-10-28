package com.example.quickpair.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StreakBanner(
    title: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = title != null,
        enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        if (title != null) {
            val gradient = when (title) {
                "GOOD!" -> Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF06B6D4)))
                "GREAT!" -> Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6)))
                "ON FIRE!" -> Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFEF4444)))
                else -> Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD946EF)))
            }

            Box(
                modifier = Modifier
                    .background(gradient, RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

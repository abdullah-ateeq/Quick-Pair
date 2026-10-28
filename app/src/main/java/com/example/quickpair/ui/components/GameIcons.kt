package com.example.quickpair.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IconBack(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.1f
        val path = Path().apply {
            moveTo(w * 0.6f, h * 0.25f)
            lineTo(w * 0.32f, h * 0.5f)
            lineTo(w * 0.6f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconSettings(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.38f
        val strokeW = w * 0.1f

        drawCircle(color = tint, radius = r * 0.45f, center = Offset(cx, cy), style = Stroke(strokeW))

        // Teeth of the gear
        for (i in 0 until 6) {
            val angle = i * (PI / 3.0)
            val x1 = (cx + r * 0.55f * cos(angle)).toFloat()
            val y1 = (cy + r * 0.55f * sin(angle)).toFloat()
            val x2 = (cx + r * 0.95f * cos(angle)).toFloat()
            val y2 = (cy + r * 0.95f * sin(angle)).toFloat()
            drawLine(
                color = tint,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = strokeW * 1.3f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun IconInfo(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.42f
        val strokeW = w * 0.09f

        // Outer circle
        drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = Stroke(strokeW))

        // Dot of 'i'
        drawCircle(color = tint, radius = strokeW * 0.7f, center = Offset(cx, cy - r * 0.42f))

        // Stem of 'i'
        drawLine(
            color = tint,
            start = Offset(cx, cy - r * 0.12f),
            end = Offset(cx, cy + r * 0.45f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun IconPlay(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.3f, h * 0.2f)
            lineTo(w * 0.8f, h * 0.5f)
            lineTo(w * 0.3f, h * 0.8f)
            close()
        }
        drawPath(path = path, color = tint, style = Fill)
    }
}

@Composable
fun IconHome(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.09f
        val path = Path().apply {
            moveTo(w * 0.18f, h * 0.45f)
            lineTo(w * 0.5f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.45f)
            lineTo(w * 0.82f, h * 0.82f)
            lineTo(w * 0.18f, h * 0.82f)
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconReplay(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.35f
        val strokeW = w * 0.09f

        drawArc(
            color = tint,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2f, r * 2f),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )

        // Arrow head at start of arc
        val arrowP = Path().apply {
            moveTo(cx + r * 0.5f, cy - r * 0.9f)
            lineTo(cx + r * 0.85f, cy - r * 0.45f)
            lineTo(cx + r * 0.25f, cy - r * 0.45f)
            close()
        }
        drawPath(arrowP, color = tint, style = Fill)
    }
}

@Composable
fun IconDelete(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val strokeW = w * 0.08f

        // Lid
        drawLine(
            color = tint,
            start = Offset(w * 0.2f, h * 0.3f),
            end = Offset(w * 0.8f, h * 0.3f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )

        // Handle
        val lidPath = Path().apply {
            moveTo(w * 0.38f, h * 0.3f)
            lineTo(w * 0.38f, h * 0.2f)
            lineTo(w * 0.62f, h * 0.2f)
            lineTo(w * 0.62f, h * 0.3f)
        }
        drawPath(lidPath, color = tint, style = Stroke(strokeW, cap = StrokeCap.Round))

        // Body
        val body = Path().apply {
            moveTo(w * 0.26f, h * 0.32f)
            lineTo(w * 0.3f, h * 0.82f)
            lineTo(w * 0.7f, h * 0.82f)
            lineTo(w * 0.74f, h * 0.32f)
            close()
        }
        drawPath(body, color = tint, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Inner vertical lines
        drawLine(color = tint, start = Offset(w * 0.42f, h * 0.42f), end = Offset(w * 0.42f, h * 0.72f), strokeWidth = strokeW, cap = StrokeCap.Round)
        drawLine(color = tint, start = Offset(w * 0.58f, h * 0.42f), end = Offset(w * 0.58f, h * 0.72f), strokeWidth = strokeW, cap = StrokeCap.Round)
    }
}

@Composable
fun IconCheckCircle(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = LocalContentColor.current
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.42f
        val strokeW = w * 0.09f

        drawCircle(color = tint, radius = r, center = Offset(cx, cy), style = Stroke(strokeW))

        val checkPath = Path().apply {
            moveTo(w * 0.3f, h * 0.52f)
            lineTo(w * 0.45f, h * 0.67f)
            lineTo(w * 0.72f, h * 0.36f)
        }
        drawPath(checkPath, color = tint, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

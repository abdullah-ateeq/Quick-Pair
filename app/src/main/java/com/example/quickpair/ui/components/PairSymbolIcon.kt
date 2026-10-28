package com.example.quickpair.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.quickpair.model.PairSymbol
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun getSymbolColor(symbol: PairSymbol): Color {
    return when (symbol) {
        PairSymbol.STAR -> Color(0xFFF59E0B)     // Amber Gold
        PairSymbol.CIRCLE -> Color(0xFF10B981)   // Emerald Green
        PairSymbol.TRIANGLE -> Color(0xFF8B5CF6) // Royal Violet
        PairSymbol.SQUARE -> Color(0xFF3B82F6)   // Sapphire Blue
        PairSymbol.DIAMOND -> Color(0xFFF43F5E)  // Ruby Rose
        PairSymbol.HEART -> Color(0xFFEF4444)    // Coral Red
        PairSymbol.PLUS -> Color(0xFF06B6D4)     // Bright Cyan
        PairSymbol.SUN -> Color(0xFFF97316)      // Vivid Orange
        PairSymbol.HEXAGON -> Color(0xFF6366F1)  // Indigo
        PairSymbol.SPARK -> Color(0xFFD946EF)    // Radiant Magenta
        PairSymbol.CLOVER -> Color(0xFF22C55E)   // Fresh Green
        PairSymbol.RING -> Color(0xFF0EA5E9)     // Sky Blue
    }
}

@Composable
fun PairSymbolIcon(
    symbol: PairSymbol,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    tint: Color = getSymbolColor(symbol)
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) / 2f * 0.82f

        when (symbol) {
            PairSymbol.STAR -> drawFivePointStar(cx, cy, r, tint)
            PairSymbol.CIRCLE -> drawCircle(color = tint, radius = r * 0.88f, center = Offset(cx, cy))
            PairSymbol.TRIANGLE -> drawEquilateralTriangle(cx, cy, r, tint)
            PairSymbol.SQUARE -> drawRoundedSquare(cx, cy, r, tint)
            PairSymbol.DIAMOND -> drawDiamond(cx, cy, r, tint)
            PairSymbol.HEART -> drawHeart(cx, cy, r, tint)
            PairSymbol.PLUS -> drawCross(cx, cy, r, tint)
            PairSymbol.SUN -> drawSun(cx, cy, r, tint)
            PairSymbol.HEXAGON -> drawHexagon(cx, cy, r, tint)
            PairSymbol.SPARK -> drawFourPointSpark(cx, cy, r, tint)
            PairSymbol.CLOVER -> drawClover(cx, cy, r, tint)
            PairSymbol.RING -> drawRingShape(cx, cy, r, tint)
        }
    }
}

private fun DrawScope.drawFivePointStar(cx: Float, cy: Float, r: Float, color: Color) {
    val innerR = r * 0.45f
    val path = Path()
    val angleStep = PI / 5.0
    val startAngle = -PI / 2.0

    for (i in 0 until 10) {
        val currentRadius = if (i % 2 == 0) r else innerR
        val angle = startAngle + i * angleStep
        val x = (cx + currentRadius * cos(angle)).toFloat()
        val y = (cy + currentRadius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawEquilateralTriangle(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    val hOffset = r * 0.15f
    val p1 = Offset(cx, cy - r + hOffset)
    val p2 = Offset(cx + r * 0.95f, cy + r * 0.75f)
    val p3 = Offset(cx - r * 0.95f, cy + r * 0.75f)
    path.moveTo(p1.x, p1.y)
    path.lineTo(p2.x, p2.y)
    path.lineTo(p3.x, p3.y)
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawRoundedSquare(cx: Float, cy: Float, r: Float, color: Color) {
    val side = r * 1.55f
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - side / 2f, cy - side / 2f),
        size = Size(side, side),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(side * 0.22f)
    )
}

private fun DrawScope.drawDiamond(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    path.moveTo(cx, cy - r)
    path.lineTo(cx + r * 0.85f, cy)
    path.lineTo(cx, cy + r)
    path.lineTo(cx - r * 0.85f, cy)
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    val scale = r / 16f
    // Draw heart using bezier curves
    val topY = cy - 6f * scale
    val bottomY = cy + 12f * scale
    path.moveTo(cx, cy - 2f * scale)
    path.cubicTo(
        cx - 8f * scale, topY - 8f * scale,
        cx - 15f * scale, cy + 2f * scale,
        cx, bottomY
    )
    path.cubicTo(
        cx + 15f * scale, cy + 2f * scale,
        cx + 8f * scale, topY - 8f * scale,
        cx, cy - 2f * scale
    )
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawCross(cx: Float, cy: Float, r: Float, color: Color) {
    val thickness = r * 0.44f
    val halfT = thickness / 2f
    val armLength = r * 0.95f
    val path = Path()
    path.moveTo(cx - halfT, cy - armLength)
    path.lineTo(cx + halfT, cy - armLength)
    path.lineTo(cx + halfT, cy - halfT)
    path.lineTo(cx + armLength, cy - halfT)
    path.lineTo(cx + armLength, cy + halfT)
    path.lineTo(cx + halfT, cy + halfT)
    path.lineTo(cx + halfT, cy + armLength)
    path.lineTo(cx - halfT, cy + armLength)
    path.lineTo(cx - halfT, cy + halfT)
    path.lineTo(cx - armLength, cy + halfT)
    path.lineTo(cx - armLength, cy - halfT)
    path.lineTo(cx - halfT, cy - halfT)
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawSun(cx: Float, cy: Float, r: Float, color: Color) {
    val centerR = r * 0.48f
    drawCircle(color = color, radius = centerR, center = Offset(cx, cy))
    val rayInner = r * 0.62f
    val rayOuter = r * 0.95f
    val strokeWidth = r * 0.18f

    for (i in 0 until 8) {
        val angle = i * (PI / 4.0)
        val x1 = (cx + rayInner * cos(angle)).toFloat()
        val y1 = (cy + rayInner * sin(angle)).toFloat()
        val x2 = (cx + rayOuter * cos(angle)).toFloat()
        val y2 = (cy + rayOuter * sin(angle)).toFloat()
        drawLine(
            color = color,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawHexagon(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    for (i in 0 until 6) {
        val angle = i * (PI / 3.0) - (PI / 6.0)
        val x = (cx + r * cos(angle)).toFloat()
        val y = (cy + r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawFourPointSpark(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    val inner = r * 0.22f
    for (i in 0 until 8) {
        val currentRadius = if (i % 2 == 0) r else inner
        val angle = i * (PI / 4.0)
        val x = (cx + currentRadius * cos(angle)).toFloat()
        val y = (cy + currentRadius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawClover(cx: Float, cy: Float, r: Float, color: Color) {
    val petalR = r * 0.42f
    val dist = r * 0.42f
    drawCircle(color = color, radius = petalR, center = Offset(cx, cy - dist))
    drawCircle(color = color, radius = petalR, center = Offset(cx + dist, cy))
    drawCircle(color = color, radius = petalR, center = Offset(cx, cy + dist))
    drawCircle(color = color, radius = petalR, center = Offset(cx - dist, cy))
    drawCircle(color = color, radius = petalR * 0.5f, center = Offset(cx, cy))
}

private fun DrawScope.drawRingShape(cx: Float, cy: Float, r: Float, color: Color) {
    val strokeW = r * 0.32f
    drawCircle(
        color = color,
        radius = r - strokeW / 2f,
        center = Offset(cx, cy),
        style = Stroke(width = strokeW)
    )
}

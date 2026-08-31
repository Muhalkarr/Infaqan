package com.example.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.core.accounting.JournalEntry
import com.example.core.calendar.HijriDate
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun HijriRadialHeatmap(
    entries: List<JournalEntry>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(280.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (min(size.width, size.height) / 2f) - 28.dp.toPx()
            val innerRadius = 45.dp.toPx()

            // Aggregate infaq by Hijri month (1..12)
            val monthlyInfaq = (1..12).associateWith { 0.0 }.toMutableMap()
            for (entry in entries) {
                val infaqSum = entry.lines
                    .filter { it.accountId == "acc_vault" }
                    .sumOf { it.credit }
                monthlyInfaq[entry.hijriMonth] = (monthlyInfaq[entry.hijriMonth] ?: 0.0) + infaqSum
            }

            val maxVal = monthlyInfaq.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
            val anglePerMonth = (2 * Math.PI) / 12.0

            val textPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 24f
                color = android.graphics.Color.WHITE
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val ramadanPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 26f
                color = android.graphics.Color.rgb(255, 213, 79)
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            for (m in 1..12) {
                val startAngle = (m - 1) * anglePerMonth - (Math.PI / 2.0)
                val sweepAngle = anglePerMonth - 0.07
                val valInfaq = monthlyInfaq[m] ?: 0.0
                val intensity = (valInfaq / maxVal).toFloat().coerceIn(0.12f, 1.0f)

                val color = if (valInfaq <= 0.0) {
                    Color(0xFF132225)
                } else {
                    lerp(EmeraldPrimary, GoldAccent, intensity)
                }

                val path = Path().apply {
                    val outerRect = Rect(center.x - maxRadius, center.y - maxRadius, center.x + maxRadius, center.y + maxRadius)
                    val innerRect = Rect(center.x - innerRadius, center.y - innerRadius, center.x + innerRadius, center.y + innerRadius)

                    arcTo(outerRect, Math.toDegrees(startAngle).toFloat(), Math.toDegrees(sweepAngle).toFloat(), false)
                    arcTo(innerRect, Math.toDegrees(startAngle + sweepAngle).toFloat(), Math.toDegrees(-sweepAngle).toFloat(), false)
                    close()
                }

                drawPath(path = path, color = color)
                drawPath(path = path, color = DarkBorder, style = Stroke(width = 1.5f))

                // Label month code
                val labelAngle = startAngle + (sweepAngle / 2.0)
                val labelRadius = maxRadius + 18.dp.toPx()
                val lx = center.x + (labelRadius * cos(labelAngle)).toFloat()
                val ly = center.y + (labelRadius * sin(labelAngle)).toFloat() + 8f

                val shortName = HijriDate.monthNames[m - 1].take(3)
                val paintToUse = if (m == 9) ramadanPaint else textPaint

                drawContext.canvas.nativeCanvas.drawText(
                    if (m == 9) "★ $shortName" else shortName,
                    lx,
                    ly,
                    paintToUse
                )
            }

            // Center Ring Circle
            drawCircle(
                color = DarkBorder,
                radius = innerRadius - 2.dp.toPx(),
                center = center,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = Color(0xFF0E1A1C),
                radius = innerRadius - 4.dp.toPx(),
                center = center
            )

            // Center Text
            val centerTitlePaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 22f
                color = android.graphics.Color.WHITE
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val centerSubPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 18f
                color = android.graphics.Color.rgb(77, 182, 172)
                textAlign = android.graphics.Paint.Align.CENTER
            }

            drawContext.canvas.nativeCanvas.drawText("12 Bulan", center.x, center.y - 8f, centerTitlePaint)
            drawContext.canvas.nativeCanvas.drawText("Hijriah", center.x, center.y + 16f, centerSubPaint)
        }
    }
}

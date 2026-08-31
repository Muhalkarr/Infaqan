package com.example.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

data class FlowNode(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun SankeyFlowDiagram(
    sources: List<FlowNode>,
    targets: List<FlowNode>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(240.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalSource = sources.sumOf { it.value }.coerceAtLeast(1.0)
            val totalTarget = targets.sumOf { it.value }.coerceAtLeast(1.0)

            val width = size.width
            val height = size.height

            val nodeWidth = 18.dp.toPx()
            val paddingY = 14.dp.toPx()
            val availableHeight = height - (max(sources.size, targets.size) * paddingY)

            // Text painter paint
            val androidPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 28f
                color = android.graphics.Color.WHITE
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val subTextPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 22f
                color = android.graphics.Color.argb(180, 200, 220, 220)
            }

            // Calculate Source Nodes (Left)
            var srcY = 12.dp.toPx()
            val srcRects = mutableListOf<Rect>()
            for (src in sources) {
                val nodeH = ((src.value / totalSource) * availableHeight).toFloat().coerceIn(24.dp.toPx(), height - 20f)
                val rect = Rect(Offset(12.dp.toPx(), srcY), Size(nodeWidth, nodeH))
                srcRects.add(rect)

                // Draw source node bar
                drawRoundRect(
                    color = src.color,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Draw source text label
                drawContext.canvas.nativeCanvas.drawText(
                    src.label,
                    rect.right + 8.dp.toPx(),
                    rect.center.y - 4f,
                    androidPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "Rp ${formatCompact(src.value)}",
                    rect.right + 8.dp.toPx(),
                    rect.center.y + 24f,
                    subTextPaint
                )

                srcY += nodeH + paddingY
            }

            // Calculate Target Nodes (Right)
            var tgtY = 12.dp.toPx()
            val tgtRects = mutableListOf<Rect>()
            for (tgt in targets) {
                val nodeH = ((tgt.value / totalTarget) * availableHeight).toFloat().coerceIn(24.dp.toPx(), height - 20f)
                val rect = Rect(Offset(width - 12.dp.toPx() - nodeWidth, tgtY), Size(nodeWidth, nodeH))
                tgtRects.add(rect)

                // Draw target node bar
                drawRoundRect(
                    color = tgt.color,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Draw target text label (right-aligned)
                val labelWidth = androidPaint.measureText(tgt.label)
                val subText = "Rp ${formatCompact(tgt.value)}"
                val subTextWidth = subTextPaint.measureText(subText)

                drawContext.canvas.nativeCanvas.drawText(
                    tgt.label,
                    rect.left - 8.dp.toPx() - labelWidth,
                    rect.center.y - 4f,
                    androidPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    subText,
                    rect.left - 8.dp.toPx() - subTextWidth,
                    rect.center.y + 24f,
                    subTextPaint
                )

                tgtY += nodeH + paddingY
            }

            // Draw Curved Bezier Ribbons (Sankey Flows)
            for (i in sources.indices) {
                for (j in targets.indices) {
                    val srcR = srcRects[i]
                    val tgtR = tgtRects[j]

                    val ribbonWeight = ((sources[i].value / totalSource) * (targets[j].value / totalTarget) * 28.0)
                        .toFloat().coerceIn(3f, 20.dp.toPx())

                    val path = Path().apply {
                        moveTo(srcR.right, srcR.center.y)
                        cubicTo(
                            width * 0.45f, srcR.center.y,
                            width * 0.55f, tgtR.center.y,
                            tgtR.left, tgtR.center.y
                        )
                    }

                    drawPath(
                        path = path,
                        color = targets[j].color.copy(alpha = 0.35f),
                        style = Stroke(width = ribbonWeight, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

private fun formatCompact(value: Double): String {
    if (value >= 1_000_000_000) return String.format(Locale.GERMANY, "%.1fM", value / 1_000_000_000)
    if (value >= 1_000_000) return String.format(Locale.GERMANY, "%.1fjt", value / 1_000_000)
    if (value >= 1_000) return String.format(Locale.GERMANY, "%.0frb", value / 1_000)
    return String.format(Locale.GERMANY, "%.0f", value)
}

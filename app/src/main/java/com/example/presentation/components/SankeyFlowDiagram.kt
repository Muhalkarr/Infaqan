package com.example.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import java.util.Locale

data class FlowNode(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun SankeyFlowDiagram(
    sources: List<FlowNode>,
    targets: List<FlowNode>,
    isMasked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    // Tinggi ditingkatkan menjadi 300.dp agar seluruh pos target (termasuk Aset Bersih) tampil leluasa tanpa terpotong
    Box(modifier = modifier.fillMaxWidth().height(300.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalSource = sources.sumOf { it.value }.coerceAtLeast(1.0)
            val totalTarget = targets.sumOf { it.value }.coerceAtLeast(1.0)

            val width = size.width
            val height = size.height

            val nodeWidth = 14.dp.toPx()
            val topPadding = 16.dp.toPx()
            val bottomMargin = 26.dp.toPx()
            val gap = 16.dp.toPx()

            // Hitung ruang vertikal efektif yang terjamin tidak melebihi kanvas
            val srcAvailableHeight = (height - topPadding - bottomMargin - ((sources.size - 1).coerceAtLeast(0) * gap)).coerceAtLeast(60f)
            val tgtAvailableHeight = (height - topPadding - bottomMargin - ((targets.size - 1).coerceAtLeast(0) * gap)).coerceAtLeast(60f)

            val minNodeHeight = 22.dp.toPx()

            // Distribusi tinggi source proporsional terjamin pas batas kanvas
            val srcMinTotal = sources.size * minNodeHeight
            val srcRemaining = (srcAvailableHeight - srcMinTotal).coerceAtLeast(0f)
            val srcHeights = sources.map {
                minNodeHeight + ((it.value / totalSource).toFloat() * srcRemaining)
            }

            // Distribusi tinggi target proporsional terjamin pas batas kanvas
            val tgtMinTotal = targets.size * minNodeHeight
            val tgtRemaining = (tgtAvailableHeight - tgtMinTotal).coerceAtLeast(0f)
            val tgtHeights = targets.map {
                minNodeHeight + ((it.value / totalTarget).toFloat() * tgtRemaining)
            }

            // Text painter
            val androidPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 25f
                color = onSurfaceColor.toArgb()
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val subTextPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = 20f
                color = onSurfaceVariantColor.toArgb()
            }

            // 1. Hitung Posisi Geometri Seluruh Node (Tanpa Menggambar Dulu)
            var srcY = topPadding
            val srcRects = mutableListOf<Rect>()
            for (i in sources.indices) {
                val nodeH = srcHeights[i]
                val rect = Rect(Offset(12.dp.toPx(), srcY), Size(nodeWidth, nodeH))
                srcRects.add(rect)
                srcY += nodeH + gap
            }

            var tgtY = topPadding
            val tgtRects = mutableListOf<Rect>()
            for (j in targets.indices) {
                val nodeH = tgtHeights[j]
                val rect = Rect(Offset(width - 12.dp.toPx() - nodeWidth, tgtY), Size(nodeWidth, nodeH))
                tgtRects.add(rect)
                tgtY += nodeH + gap
            }

            // 2. LAYER 1: Gambar Pita Aliran Bezier (Sankey Flow Ribbons) LEBIH DULU DI LATAR BELAKANG
            for (i in sources.indices) {
                for (j in targets.indices) {
                    val srcR = srcRects[i]
                    val tgtR = tgtRects[j]

                    val ribbonWeight = ((sources[i].value / totalSource) * (targets[j].value / totalTarget) * 26.0)
                        .toFloat().coerceIn(3.5f, 18.dp.toPx())

                    val path = Path().apply {
                        moveTo(srcR.right, srcR.center.y)
                        cubicTo(
                            width * 0.44f, srcR.center.y,
                            width * 0.56f, tgtR.center.y,
                            tgtR.left, tgtR.center.y
                        )
                    }

                    drawPath(
                        path = path,
                        color = targets[j].color.copy(alpha = 0.28f),
                        style = Stroke(width = ribbonWeight, cap = StrokeCap.Round)
                    )
                }
            }

            // 3. LAYER 2: Gambar Scrim / Pill Latar Belakang Teks agar Garis Aliran Tidak Menimpa Tulisan
            for (i in sources.indices) {
                val rect = srcRects[i]
                val src = sources[i]
                val labelWidth = androidPaint.measureText(src.label)
                val pct = ((src.value / totalSource) * 100.0).toInt()
                val subText = if (isMasked) "Porsi: $pct% (Rp •••)" else "Rp ${formatCompact(src.value)}"
                val subTextWidth = subTextPaint.measureText(subText)
                val maxW = maxOf(labelWidth, subTextWidth)

                val pillLeft = rect.right + 6.dp.toPx()
                val pillTop = rect.center.y - 17.dp.toPx()
                val pillRight = pillLeft + maxW + 10.dp.toPx()
                val pillBottom = rect.center.y + 17.dp.toPx()

                // Backdrop anti-tabrakan
                drawRoundRect(
                    color = surfaceColor.copy(alpha = 0.88f),
                    topLeft = Offset(pillLeft, pillTop),
                    size = Size(pillRight - pillLeft, pillBottom - pillTop),
                    cornerRadius = CornerRadius(5.dp.toPx())
                )
                drawRoundRect(
                    color = outlineColor.copy(alpha = 0.35f),
                    topLeft = Offset(pillLeft, pillTop),
                    size = Size(pillRight - pillLeft, pillBottom - pillTop),
                    cornerRadius = CornerRadius(5.dp.toPx()),
                    style = Stroke(width = 1f)
                )
            }

            for (j in targets.indices) {
                val rect = tgtRects[j]
                val tgt = targets[j]
                val labelWidth = androidPaint.measureText(tgt.label)
                val pct = ((tgt.value / totalTarget) * 100.0).toInt()
                val subText = if (isMasked) "Porsi: $pct% (Rp •••)" else "Rp ${formatCompact(tgt.value)}"
                val subTextWidth = subTextPaint.measureText(subText)
                val maxW = maxOf(labelWidth, subTextWidth)

                val pillRight = rect.left - 6.dp.toPx()
                val pillLeft = pillRight - maxW - 10.dp.toPx()
                val pillTop = rect.center.y - 17.dp.toPx()
                val pillBottom = rect.center.y + 17.dp.toPx()

                // Backdrop anti-tabrakan
                drawRoundRect(
                    color = surfaceColor.copy(alpha = 0.88f),
                    topLeft = Offset(pillLeft, pillTop),
                    size = Size(pillRight - pillLeft, pillBottom - pillTop),
                    cornerRadius = CornerRadius(5.dp.toPx())
                )
                drawRoundRect(
                    color = outlineColor.copy(alpha = 0.35f),
                    topLeft = Offset(pillLeft, pillTop),
                    size = Size(pillRight - pillLeft, pillBottom - pillTop),
                    cornerRadius = CornerRadius(5.dp.toPx()),
                    style = Stroke(width = 1f)
                )
            }

            // 4. LAYER 3: Gambar Batang Node Vertikal
            for (i in sources.indices) {
                val rect = srcRects[i]
                drawRoundRect(
                    color = sources[i].color,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            for (j in targets.indices) {
                val rect = tgtRects[j]
                drawRoundRect(
                    color = targets[j].color,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }

            // 5. LAYER 4: Gambar Teks Label dan Nilai Nominal DI LAPISAN PALING ATAS
            for (i in sources.indices) {
                val rect = srcRects[i]
                val src = sources[i]
                val pct = ((src.value / totalSource) * 100.0).toInt()
                val subText = if (isMasked) "Porsi: $pct% (Rp •••)" else "Rp ${formatCompact(src.value)}"
                val textX = rect.right + 11.dp.toPx()
                drawContext.canvas.nativeCanvas.drawText(
                    src.label,
                    textX,
                    rect.center.y - 2f,
                    androidPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    subText,
                    textX,
                    rect.center.y + 20f,
                    subTextPaint
                )
            }

            for (j in targets.indices) {
                val rect = tgtRects[j]
                val tgt = targets[j]
                val labelWidth = androidPaint.measureText(tgt.label)
                val pct = ((tgt.value / totalTarget) * 100.0).toInt()
                val subText = if (isMasked) "Porsi: $pct% (Rp •••)" else "Rp ${formatCompact(tgt.value)}"
                val subTextWidth = subTextPaint.measureText(subText)
                val textRight = rect.left - 11.dp.toPx()

                drawContext.canvas.nativeCanvas.drawText(
                    tgt.label,
                    textRight - labelWidth,
                    rect.center.y - 2f,
                    androidPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    subText,
                    textRight - subTextWidth,
                    rect.center.y + 20f,
                    subTextPaint
                )
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

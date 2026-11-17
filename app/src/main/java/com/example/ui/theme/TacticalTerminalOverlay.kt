package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Overlay Taktis Terminal Arknights / Stronghold Protocol
 * Menghadirkan:
 * 1. Garis CRT scanline retro-futuristik beranimasi halus.
 * 2. Garis siku neon taktis di 4 sudut layar (Tactical HUD Corner Brackets).
 * 3. Indikator telemetry baris status protokol syariah.
 */
@Composable
fun TacticalTerminalOverlay(
    modifier: Modifier = Modifier,
    isLight: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "crt_scanline_anim")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hud_pulse_glow"
    )

    val density = LocalDensity.current
    val statusBarTopPx = WindowInsets.statusBars.getTop(density).toFloat()
    val navBarBottomPx = WindowInsets.navigationBars.getBottom(density).toFloat()

    Box(modifier = modifier.fillMaxSize()) {
        // Konten utama aplikasi
        content()

        // Tactical HUD & Background Grid Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val safeTopInset = statusBarTopPx + 8.dp.toPx()
            val safeBottomInset = navBarBottomPx + 8.dp.toPx()

            if (isLight) {
                // ==========================================
                // RHINE LAB ARKNIGHTS // CLEAN LAB BACKGROUND
                // ==========================================
                // 1. Subtle Engineering Grid Lines (56.dp grid)
                val gridStep = 56.dp.toPx()
                val gridLineColor = Color(0xFF0A1412).copy(alpha = 0.032f)
                val crosshairColor = Color(0xFF006D44).copy(alpha = 0.12f)

                var x = gridStep
                while (x < canvasWidth) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f
                    )
                    x += gridStep
                }

                var y = gridStep
                while (y < canvasHeight) {
                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )
                    y += gridStep
                }

                // 2. Faint Hexagonal / Molecular Lab Watermark (Right Side Margin - Rhine Lab signature)
                val hexRadius = 18.dp.toPx()
                val hexStrokeColor = Color(0xFF334944).copy(alpha = 0.045f)
                val hexCenters = listOf(
                    Offset(canvasWidth - 36.dp.toPx(), 130.dp.toPx()),
                    Offset(canvasWidth - 64.dp.toPx(), 146.dp.toPx()),
                    Offset(canvasWidth - 36.dp.toPx(), 162.dp.toPx()),
                    Offset(canvasWidth - 28.dp.toPx(), canvasHeight - 170.dp.toPx()),
                    Offset(canvasWidth - 56.dp.toPx(), canvasHeight - 154.dp.toPx())
                )
                hexCenters.forEach { c ->
                    val hexPath = androidx.compose.ui.graphics.Path()
                    for (vertex in 0..5) {
                        val angle = Math.toRadians((60 * vertex - 30).toDouble())
                        val vx = c.x + hexRadius * kotlin.math.cos(angle).toFloat()
                        val vy = c.y + hexRadius * kotlin.math.sin(angle).toFloat()
                        if (vertex == 0) hexPath.moveTo(vx, vy) else hexPath.lineTo(vx, vy)
                    }
                    hexPath.close()
                    drawPath(
                        path = hexPath,
                        color = hexStrokeColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
                    )
                }

                // 3. Rhine Lab Precision Crosshairs (+) at key grid intersections
                val crossSize = 3.5.dp.toPx()
                var crossX = gridStep * 2
                while (crossX < canvasWidth) {
                    var crossY = gridStep * 3
                    while (crossY < canvasHeight) {
                        drawLine(
                            color = crosshairColor,
                            start = Offset(crossX - crossSize, crossY),
                            end = Offset(crossX + crossSize, crossY),
                            strokeWidth = 1.3f
                        )
                        drawLine(
                            color = crosshairColor,
                            start = Offset(crossX, crossY - crossSize),
                            end = Offset(crossX, crossY + crossSize),
                            strokeWidth = 1.3f
                        )
                        crossY += gridStep * 4
                    }
                    crossX += gridStep * 3
                }

                // 4. Rhine Lab Left Structural Axis Line with Obsidian Calibration Ticks & Lime Pip
                val leftAxisX = 5.dp.toPx()
                val topSafe = safeTopInset + 6.dp.toPx()
                val bottomSafe = (canvasHeight - safeBottomInset - 6.dp.toPx()).coerceAtLeast(topSafe + 40.dp.toPx())
                drawLine(
                    color = Color(0xFF0A1412).copy(alpha = 0.24f),
                    start = Offset(leftAxisX, topSafe),
                    end = Offset(leftAxisX, bottomSafe),
                    strokeWidth = 1.5f
                )
                // Calibration tick marks along left structural rail
                var tickY = topSafe + 40.dp.toPx()
                while (tickY < bottomSafe - 24.dp.toPx()) {
                    drawLine(
                        color = Color(0xFF0A1412).copy(alpha = 0.25f),
                        start = Offset(leftAxisX, tickY),
                        end = Offset(leftAxisX + 3.dp.toPx(), tickY),
                        strokeWidth = 1f
                    )
                    tickY += 72.dp.toPx()
                }
                // Acid Lime Indicator pip on structural axis (Rhine Lab signature)
                drawRect(
                    color = Color(0xFF84CC16),
                    topLeft = Offset(leftAxisX - 2.dp.toPx(), topSafe + 24.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(5.dp.toPx(), 10.dp.toPx())
                )
                // Small Obsidian Anchor Block at bottom-left structural end
                drawRect(
                    color = Color(0xFF0A1412).copy(alpha = 0.7f),
                    topLeft = Offset(leftAxisX - 1.5.dp.toPx(), bottomSafe - 10.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(4.dp.toPx(), 10.dp.toPx())
                )

                // 5. Tactical Corner HUD Brackets ┌ ┐ └ ┘ (inset cleanly below status bar & above nav bar)
                val bracketLen = 20.dp.toPx()
                val bracketColor = Color(0xFF006D44).copy(alpha = (0.6f + pulseGlow * 0.35f).coerceAtMost(1f))
                val obsidianCapColor = Color(0xFF0A1412).copy(alpha = 0.75f)
                val strokeW = 2.dp.toPx()
                val insetX = 8.dp.toPx()
                val topInsetY = safeTopInset
                val bottomInsetY = safeBottomInset

                // Top-Left ┌
                drawLine(bracketColor, Offset(insetX, topInsetY), Offset(insetX + bracketLen, topInsetY), strokeW, StrokeCap.Square)
                drawLine(obsidianCapColor, Offset(insetX, topInsetY), Offset(insetX, topInsetY + bracketLen), strokeW, StrokeCap.Square)

                // Top-Right ┐
                drawLine(bracketColor, Offset(canvasWidth - insetX - bracketLen, topInsetY), Offset(canvasWidth - insetX, topInsetY), strokeW, StrokeCap.Square)
                drawLine(obsidianCapColor, Offset(canvasWidth - insetX, topInsetY), Offset(canvasWidth - insetX, topInsetY + bracketLen), strokeW, StrokeCap.Square)

                // Bottom-Left └
                drawLine(bracketColor, Offset(insetX, canvasHeight - bottomInsetY), Offset(insetX + bracketLen, canvasHeight - bottomInsetY), strokeW, StrokeCap.Square)
                drawLine(obsidianCapColor, Offset(insetX, canvasHeight - bottomInsetY - bracketLen), Offset(insetX, canvasHeight - bottomInsetY), strokeW, StrokeCap.Square)

                // Bottom-Right ┘
                drawLine(bracketColor, Offset(canvasWidth - insetX - bracketLen, canvasHeight - bottomInsetY), Offset(canvasWidth - insetX, canvasHeight - bottomInsetY), strokeW, StrokeCap.Square)
                drawLine(obsidianCapColor, Offset(canvasWidth - insetX, canvasHeight - bottomInsetY - bracketLen), Offset(canvasWidth - insetX, canvasHeight - bottomInsetY), strokeW, StrokeCap.Square)

            } else {
                // ==========================================
                // DARK CYBER TERMINAL SCANLINES & BRACKETS
                // ==========================================
                val step = 4.dp.toPx()
                var currentY = 0f
                val scanlineColor = Color(0xFF00FF9D).copy(alpha = 0.025f)

                while (currentY < canvasHeight) {
                    drawLine(
                        color = scanlineColor,
                        start = Offset(0f, currentY),
                        end = Offset(canvasWidth, currentY),
                        strokeWidth = 1f
                    )
                    currentY += step
                }

                // Tactical Corner HUD Brackets ┌ ┐ └ ┘ (respecting status bar & navigation bar insets)
                val bracketLen = 22.dp.toPx()
                val bracketColor = Color(0xFF00FF9D).copy(alpha = pulseGlow * 0.75f)
                val strokeW = 2.dp.toPx()
                val insetX = 8.dp.toPx()
                val topY = safeTopInset
                val bottomY = (canvasHeight - safeBottomInset).coerceAtLeast(topY + bracketLen + 4.dp.toPx())

                // Top-Left ┌
                drawLine(bracketColor, Offset(insetX, topY), Offset(insetX + bracketLen, topY), strokeW, StrokeCap.Square)
                drawLine(bracketColor, Offset(insetX, topY), Offset(insetX, topY + bracketLen), strokeW, StrokeCap.Square)

                // Top-Right ┐
                drawLine(bracketColor, Offset(canvasWidth - insetX - bracketLen, topY), Offset(canvasWidth - insetX, topY), strokeW, StrokeCap.Square)
                drawLine(bracketColor, Offset(canvasWidth - insetX, topY), Offset(canvasWidth - insetX, topY + bracketLen), strokeW, StrokeCap.Square)

                // Bottom-Left └
                drawLine(bracketColor, Offset(insetX, bottomY), Offset(insetX + bracketLen, bottomY), strokeW, StrokeCap.Square)
                drawLine(bracketColor, Offset(insetX, bottomY - bracketLen), Offset(insetX, bottomY), strokeW, StrokeCap.Square)

                // Bottom-Right ┘
                drawLine(bracketColor, Offset(canvasWidth - insetX - bracketLen, bottomY), Offset(canvasWidth - insetX, bottomY), strokeW, StrokeCap.Square)
                drawLine(bracketColor, Offset(canvasWidth - insetX, bottomY - bracketLen), Offset(canvasWidth - insetX, bottomY), strokeW, StrokeCap.Square)
            }
        }
    }
}

/**
 * Banner Telemetri Taktis ala Cyber Terminal Console
 */
@Composable
fun TacticalTelemetryRibbon(
    modifier: Modifier = Modifier,
    isLight: Boolean = false
) {
    val bgColors = if (isLight) {
        listOf(
            Color(0xFFE4ECE9),
            Color(0xFFD0F0E2),
            Color(0xFFE4ECE9)
        )
    } else {
        listOf(
            Color(0xFF071210),
            Color(0xFF0A1E18),
            Color(0xFF071210)
        )
    }
    val borderColor = if (isLight) Color(0xFF76938C) else Color(0xFF00FF9D).copy(alpha = 0.4f)
    val accentColor = if (isLight) Color(0xFF006D44) else Color(0xFF00FF9D)
    val statusColor = if (isLight) Color(0xFFB86600) else Color(0xFFFFD54F)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(colors = bgColors)
            )
            .border(
                width = 0.8.dp,
                color = borderColor,
                shape = CutCornerShape(topStart = 0.dp, topEnd = 6.dp, bottomStart = 6.dp, bottomEnd = 0.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accentColor, CutCornerShape(2.dp))
                )
                Text(
                    text = "SYS//ONLINE • STRONGHOLD PROTOCOL",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = "STATUS: [AMANAH_VERIFIED]",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 8.5.sp,
                color = statusColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

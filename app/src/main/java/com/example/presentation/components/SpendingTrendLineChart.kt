package com.example.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.JournalEntry
import com.example.core.budget.BudgetAllocation
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class MonthlyDataPoint(
    val monthIndex: Int, // 0..11
    val monthLabel: String,
    val incomeAmount: Double,
    val expenseAmount: Double,
    val budgetLimit: Double
)

@Composable
fun SpendingTrendLineChart(
    journalEntries: List<JournalEntry>,
    budgets: List<BudgetAllocation>,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember {
        NumberFormat.getNumberInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    // Build past 6 months of data
    val monthlyData = remember(journalEntries, budgets) {
        val list = mutableListOf<MonthlyDataPoint>()
        val cal = Calendar.getInstance()
        val totalBudget = budgets.sumOf { it.monthlyLimit }

        for (i in 5 downTo 0) {
            val mCal = Calendar.getInstance().apply {
                time = cal.time
                add(Calendar.MONTH, -i)
            }
            val mIdx = mCal.get(Calendar.MONTH)
            val yIdx = mCal.get(Calendar.YEAR)
            val label = SimpleDateFormat("MMM", Locale("id", "ID")).format(mCal.time)

            var income = 0.0
            var expense = 0.0

            for (entry in journalEntries) {
                val eCal = Calendar.getInstance().apply { time = entry.gregorianDate }
                if (eCal.get(Calendar.MONTH) == mIdx && eCal.get(Calendar.YEAR) == yIdx) {
                    if (entry.transactionType == "INFLOW") {
                        income += entry.lines.filter { it.accountId.startsWith("rev_") || it.accountId.startsWith("inc_") || it.accountId.startsWith("acc_sal") || it.accountId.startsWith("acc_trade") || it.accountId.startsWith("acc_gift") }.sumOf { it.credit }
                            .let { if (it > 0) it else entry.totalDebit }
                    } else if (entry.transactionType == "EXPENSE") {
                        // Strict check: Only count expense accounts, never include TRANSFER or balance movements
                        val expDebit = entry.lines.filter { 
                            it.accountId.startsWith("exp_") || it.accountId.startsWith("cons_") || it.accountId.startsWith("acc_living") ||
                            it.accountId.startsWith("acc_trans") || it.accountId.startsWith("acc_util") || it.accountId.startsWith("acc_edu") ||
                            it.accountId.startsWith("acc_health") || it.accountId.startsWith("acc_other_exp")
                        }.sumOf { it.debit }
                        expense += if (expDebit > 0) expDebit else entry.totalDebit
                    }
                }
            }

            list.add(
                MonthlyDataPoint(
                    monthIndex = mIdx,
                    monthLabel = label,
                    incomeAmount = income,
                    expenseAmount = expense,
                    budgetLimit = totalBudget
                )
            )
        }
        list
    }

    val maxAmount = remember(monthlyData) {
        val peak = monthlyData.maxOfOrNull { max(it.incomeAmount, max(it.expenseAmount, it.budgetLimit)) } ?: 1000000.0
        if (peak <= 0.0) 1000000.0 else peak * 1.25
    }

    val animationProgress = remember { Animatable(0f) }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    LaunchedEffect(journalEntries.size) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900)
        )
    }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val chartPointInnerColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_trend_line_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tren Pengeluaran & Pendapatan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visualisasi 6 Bulan Terakhir & Batas Anggaran",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldDark.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Realtime D3/Graph",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primaryColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pendapatan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(errorColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pengeluaran", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Batas Anggaran", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Selected data tooltip if tapped
            if (selectedPointIndex != null && selectedPointIndex in monthlyData.indices) {
                val point = monthlyData[selectedPointIndex!!]
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bulan ${point.monthLabel}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Masuk: Rp ${numberFormat.format(point.incomeAmount)}",
                                fontSize = 11.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Keluar: Rp ${numberFormat.format(point.expenseAmount)}",
                                fontSize = 11.sp,
                                color = errorColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            val axisTextColorArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
            val canvasGridColorArgb = gridColor.toArgb()

            // Interactive Line Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(monthlyData) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (monthlyData.size - 1).coerceAtLeast(1)
                                val tappedIndex = ((offset.x + stepX / 2f) / stepX).toInt().coerceIn(0, monthlyData.size - 1)
                                selectedPointIndex = tappedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 26.dp.toPx()
                    val paddingTop = 12.dp.toPx()
                    val chartHeight = height - paddingBottom - paddingTop
                    val count = monthlyData.size
                    val stepX = if (count > 1) width / (count - 1) else width

                    // 1. Draw Grid Lines & Y-Axis values
                    val gridPaint = android.graphics.Paint().apply {
                        color = canvasGridColorArgb
                        strokeWidth = 1f
                    }
                    val labelPaint = android.graphics.Paint().apply {
                        color = axisTextColorArgb
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }

                    val yLines = 4
                    for (i in 0..yLines) {
                        val y = paddingTop + (chartHeight / yLines) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // 2. Budget Limit Line (Gold Dotted)
                    if (monthlyData.isNotEmpty() && monthlyData[0].budgetLimit > 0.0) {
                        val budgetY = paddingTop + chartHeight * (1f - (monthlyData[0].budgetLimit / maxAmount).toFloat().coerceIn(0f, 1f))
                        drawLine(
                            color = secondaryColor.copy(alpha = 0.7f),
                            start = Offset(0f, budgetY),
                            end = Offset(width, budgetY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    }

                    // 3. Construct Smooth Paths for Income & Expense
                    val incomePoints = mutableListOf<Offset>()
                    val expensePoints = mutableListOf<Offset>()

                    for (i in 0 until count) {
                        val x = i * stepX
                        val item = monthlyData[i]

                        val normIncome = (item.incomeAmount / maxAmount).toFloat().coerceIn(0f, 1f) * animationProgress.value
                        val normExpense = (item.expenseAmount / maxAmount).toFloat().coerceIn(0f, 1f) * animationProgress.value

                        val yIncome = paddingTop + chartHeight * (1f - normIncome)
                        val yExpense = paddingTop + chartHeight * (1f - normExpense)

                        incomePoints.add(Offset(x, yIncome))
                        expensePoints.add(Offset(x, yExpense))

                        // Draw Month Label on X-axis
                        drawContext.canvas.nativeCanvas.drawText(
                            item.monthLabel,
                            x,
                            height - 4.dp.toPx(),
                            labelPaint
                        )
                    }

                    // Helper to draw smooth bezier path
                    fun drawSmoothCurve(points: List<Offset>, strokeColor: Color, fillColor: Color) {
                        if (points.isEmpty()) return

                        val strokePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(points.last().x, paddingTop + chartHeight)
                            lineTo(points.first().x, paddingTop + chartHeight)
                            close()
                        }

                        // Gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(fillColor.copy(alpha = 0.35f), fillColor.copy(alpha = 0.02f)),
                                startY = paddingTop,
                                endY = paddingTop + chartHeight
                            )
                        )

                        // Stroke line
                        drawPath(
                            path = strokePath,
                            color = strokeColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Data points
                        for ((idx, p) in points.withIndex()) {
                            val isSelected = selectedPointIndex == idx
                            drawCircle(color = onSurfaceColor, radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = p
                            )
                            drawCircle(
                                color = strokeColor,
                                radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                                center = p
                            )
                            if (isSelected) {
                                drawCircle(color = onSurfaceColor,
                                    
                                    radius = 2.dp.toPx(),
                                    center = p
                                )
                            }
                        }
                    }

                    // Draw Income Curve (Emerald)
                    drawSmoothCurve(incomePoints, primaryColor, primaryColor)

                    // Draw Expense Curve (Coral Red)
                    drawSmoothCurve(expensePoints, errorColor, errorColor)
                }
            }
        }
    }
}

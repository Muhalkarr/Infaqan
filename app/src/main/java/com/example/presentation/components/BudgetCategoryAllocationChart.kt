package com.example.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.JournalEntry
import com.example.core.budget.BudgetAllocation
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import java.text.NumberFormat
import java.util.Locale

data class ShariaBudgetPartition(
    val id: String,
    val title: String,
    val shortTitle: String,
    val shariaClass: String, // Dharuriyyat, Hajiyyat, Tahsiniyyat, Infaq/Tabungan
    val targetPercent: Double,
    val color: Color,
    val description: String
)

@Composable
fun BudgetCategoryAllocationChart(
    budgetCategories: List<BudgetAllocation>,
    journalEntries: List<JournalEntry>,
    currencySymbol: String = "Rp",
    onNavigateToBudget: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    var selectedPartitionIndex by remember { mutableStateOf<Int?>(null) }

    // Real Sharia Maqashid Partition Calculation from actual journal entries & budget allocations
    val partitions = remember(budgetCategories, journalEntries) {
        val expenseEntries = journalEntries.filter { it.transactionType == "EXPENSE" }

        // 1. Dharuriyyat (Pokok: Makanan, Kesehatan, Rumah, Pendidikan)
        val dharuriyyatAccountIds = setOf("acc_living", "acc_food", "acc_health", "acc_education", "acc_home")
        val dharuriyyatSpent = expenseEntries.sumOf { entry ->
            entry.lines.filter { it.accountId in dharuriyyatAccountIds && it.debit > 0 }.sumOf { it.debit }
        }

        val dharuriyyatBudget = budgetCategories.filter {
            it.accountId in dharuriyyatAccountIds ||
            it.categoryName.contains("Pokok", ignoreCase = true) ||
            it.categoryName.contains("Pangan", ignoreCase = true) ||
            it.categoryName.contains("Hidup", ignoreCase = true) ||
            it.categoryName.contains("Makan", ignoreCase = true) ||
            it.categoryName.contains("Kesehatan", ignoreCase = true) ||
            it.categoryName.contains("Pendidikan", ignoreCase = true) ||
            it.categoryName.contains("Rumah", ignoreCase = true)
        }.sumOf { it.monthlyLimit }

        // 2. Hajiyyat (Penunjang: Transportasi, Utilitas, Komunikasi, Tagihan)
        val hajiyyatAccountIds = setOf("acc_transport", "acc_utility", "acc_utilities", "acc_bills", "acc_operational")
        val hajiyyatSpent = expenseEntries.sumOf { entry ->
            entry.lines.filter { it.accountId in hajiyyatAccountIds && it.debit > 0 }.sumOf { it.debit }
        }

        val hajiyyatBudget = budgetCategories.filter {
            it.accountId in hajiyyatAccountIds ||
            it.categoryName.contains("Transport", ignoreCase = true) ||
            it.categoryName.contains("Listrik", ignoreCase = true) ||
            it.categoryName.contains("Tagihan", ignoreCase = true) ||
            it.categoryName.contains("Utilitas", ignoreCase = true) ||
            it.categoryName.contains("Bensin", ignoreCase = true) ||
            it.categoryName.contains("Pulsa", ignoreCase = true) ||
            it.categoryName.contains("Operasional", ignoreCase = true)
        }.sumOf { it.monthlyLimit }

        // 3. Infaq Kasab & Tabungan Ibadah (Investasi Akhirat: Vault, Sedekah, Zakat)
        val infaqAccountIds = setOf("acc_vault", "acc_zakat_payable", "acc_disbursed", "acc_infaq_exp", "acc_charity")
        val infaqSpent = journalEntries.sumOf { entry ->
            entry.lines.filter { line ->
                (line.accountId in setOf("acc_disbursed", "acc_infaq_exp", "acc_zakat_payable", "acc_charity") && line.debit > 0) ||
                (entry.transactionType in listOf("DISBURSE", "SEDEKAH", "INFAQ_PAYOUT") && line.accountId == "acc_vault" && line.debit > 0)
            }.sumOf { it.debit }
        }

        val infaqBudget = budgetCategories.filter {
            it.accountId in infaqAccountIds ||
            it.categoryName.contains("Infaq", ignoreCase = true) ||
            it.categoryName.contains("Sedekah", ignoreCase = true) ||
            it.categoryName.contains("Tabungan", ignoreCase = true) ||
            it.categoryName.contains("Zakat", ignoreCase = true)
        }.sumOf { it.monthlyLimit }

        // 4. Tahsiniyyat (Pelengkap/Gaya Hidup Halal: Rekreasi, Hiburan, Belanja)
        val tahsiniyyatAccountIds = setOf("acc_lifestyle", "acc_entertainment", "acc_hobby", "acc_shopping", "acc_other_exp")
        val tahsiniyyatSpent = expenseEntries.sumOf { entry ->
            entry.lines.filter { it.accountId in tahsiniyyatAccountIds && it.debit > 0 }.sumOf { it.debit }
        }

        val tahsiniyyatBudget = budgetCategories.filter {
            it.accountId in tahsiniyyatAccountIds ||
            it.categoryName.contains("Gaya Hidup", ignoreCase = true) ||
            it.categoryName.contains("Hiburan", ignoreCase = true) ||
            it.categoryName.contains("Belanja", ignoreCase = true) ||
            it.categoryName.contains("Hobi", ignoreCase = true) ||
            it.categoryName.contains("Pelengkap", ignoreCase = true) ||
            it.categoryName.contains("Lainnya", ignoreCase = true) ||
            it.categoryName.contains("Rekreasi", ignoreCase = true)
        }.sumOf { it.monthlyLimit }

        listOf(
            PartitionSummary(
                partition = ShariaBudgetPartition(
                    id = "dharuriyyat",
                    title = "Dharuriyyat (Pokok)",
                    shortTitle = "Dharuriyyat",
                    shariaClass = "Maqashid Pokok",
                    targetPercent = 50.0,
                    color = EmeraldPrimary,
                    description = "Kebutuhan esensial pangan, papan, kesehatan & pendidikan."
                ),
                actualSpent = dharuriyyatSpent,
                budgetCeiling = dharuriyyatBudget
            ),
            PartitionSummary(
                partition = ShariaBudgetPartition(
                    id = "hajiyyat",
                    title = "Hajiyyat (Kenyamanan)",
                    shortTitle = "Hajiyyat",
                    shariaClass = "Penunjang",
                    targetPercent = 30.0,
                    color = Color(0xFF00B4D8),
                    description = "Transportasi, komunikasi, utilitas & operasional harian."
                ),
                actualSpent = hajiyyatSpent,
                budgetCeiling = hajiyyatBudget
            ),
            PartitionSummary(
                partition = ShariaBudgetPartition(
                    id = "infaq_tabungan",
                    title = "Infaq & Tabungan Syariah",
                    shortTitle = "Infaq & Tabungan",
                    shariaClass = "Investasi Akhirat",
                    targetPercent = 10.0,
                    color = GoldAccent,
                    description = "Infaq Kasab, Sedekah Subuh, Zakat & tabungan ibadah."
                ),
                actualSpent = infaqSpent,
                budgetCeiling = infaqBudget
            ),
            PartitionSummary(
                partition = ShariaBudgetPartition(
                    id = "tahsiniyyat",
                    title = "Tahsiniyyat (Pelengkap)",
                    shortTitle = "Tahsiniyyat",
                    shariaClass = "Pelengkap",
                    targetPercent = 10.0,
                    color = Color(0xFFAB47BC),
                    description = "Rekreasi & gaya hidup halal tanpa melampaui batas israf."
                ),
                actualSpent = tahsiniyyatSpent,
                budgetCeiling = tahsiniyyatBudget
            )
        )
    }

    val realTotalSpent = partitions.sumOf { it.actualSpent }
    val totalBudget = partitions.sumOf { it.budgetCeiling }
    val hasSpentData = realTotalSpent > 0.0

    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(realTotalSpent) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 700))
    }

    // Determine Sharia Compliance Assessment
    val complianceStatus = remember(partitions, realTotalSpent) {
        if (realTotalSpent == 0.0) {
            ShariaComplianceStatus(
                label = "Belum Ada Pengeluaran",
                detail = "Mulai mencatat transaksi pengeluaran kas atau pos anggaran.",
                isPositive = true,
                color = EmeraldLight
            )
        } else {
            val dharuriyyatPct = (partitions[0].actualSpent / realTotalSpent) * 100
            val hajiyyatPct = (partitions[1].actualSpent / realTotalSpent) * 100
            val infaqPct = (partitions[2].actualSpent / realTotalSpent) * 100
            val tahsiniyyatPct = (partitions[3].actualSpent / realTotalSpent) * 100

            when {
                tahsiniyyatPct > 25.0 -> ShariaComplianceStatus(
                    label = "Porsi Pelengkap Tinggi (Israf)",
                    detail = "Pengeluaran gaya hidup mencapai ${tahsiniyyatPct.toInt()}%. Disarankan menahan konsumsi komplementer.",
                    isPositive = false,
                    color = ExpenseCoral
                )
                infaqPct >= 15.0 -> ShariaComplianceStatus(
                    label = "Sangat Berkah (Infaq Unggul)",
                    detail = "Alokasi sedekah & infaq mencapai ${infaqPct.toInt()}%, melampaui target minimal 10%.",
                    isPositive = true,
                    color = GoldAccent
                )
                dharuriyyatPct > 70.0 -> ShariaComplianceStatus(
                    label = "Dominan Kebutuhan Pokok",
                    detail = "Sebagian besar kas terserap untuk kebutuhan primer (${dharuriyyatPct.toInt()}%).",
                    isPositive = true,
                    color = EmeraldLight
                )
                else -> ShariaComplianceStatus(
                    label = "Kepatuhan: Seimbang (Proporsional)",
                    detail = "Alokasi pengeluaran selaras dengan kaidah Maqashid Syariah 50/30/10/10.",
                    isPositive = true,
                    color = EmeraldLight
                )
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_allocation_category_chart_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Grafik Alokasi Anggaran",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Proporsi Anggaran Syariah",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kaidah Maqashid Syariah 50 / 30 / 10 / 10",
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }
                }

                Surface(
                    onClick = onNavigateToBudget,
                    color = EmeraldPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("btn_manage_budget_chart")
                ) {
                    Text(
                        text = "Kelola",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldLight,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Donut Chart + Center Stats
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                val strokeDp = 16.dp
                Canvas(
                    modifier = Modifier
                        .size(190.dp)
                        .testTag("budget_donut_canvas")
                ) {
                    val strokeWidth = strokeDp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    var startAngle = -90f

                    // Draw base background ring
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.15f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    if (hasSpentData) {
                        partitions.forEachIndexed { index, part ->
                            val sweep = ((part.actualSpent / realTotalSpent) * 360f * animationProgress.value).toFloat()
                            val isSelected = selectedPartitionIndex == index
                            val sliceStrokeWidth = if (isSelected) strokeWidth + 4.dp.toPx() else strokeWidth

                            if (sweep > 0.5f) {
                                drawArc(
                                    color = part.partition.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep.coerceAtLeast(2f),
                                    useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(width = sliceStrokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            startAngle += sweep
                        }
                    } else {
                        // Draw balanced default target guidance ring segments
                        val idealSplits = listOf(
                            EmeraldPrimary to 180f, // 50%
                            Color(0xFF00B4D8) to 108f, // 30%
                            GoldAccent to 36f, // 10%
                            Color(0xFFAB47BC) to 36f // 10%
                        )
                        var guideAngle = -90f
                        idealSplits.forEach { (color, sweep) ->
                            drawArc(
                                color = color.copy(alpha = 0.35f * animationProgress.value),
                                startAngle = guideAngle,
                                sweepAngle = sweep - 4f,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeWidth * 0.7f, cap = StrokeCap.Round)
                            )
                            guideAngle += sweep
                        }
                    }
                }

                // Center Information inside Donut (Strictly constrained with ample breathing space)
                Column(
                    modifier = Modifier
                        .widthIn(max = 110.dp)
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val selected = selectedPartitionIndex?.let { partitions.getOrNull(it) }

                    if (selected != null) {
                        Text(
                            text = selected.partition.shortTitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = selected.partition.color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (hasSpentData) "${((selected.actualSpent / realTotalSpent) * 100).toInt()}%" else "0%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currencySymbol ${nf.format(selected.actualSpent.toLong())}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = if (hasSpentData) "Realisasi Kas" else "Kas Keluar",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currencySymbol ${nf.format(realTotalSpent.toLong())}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (hasSpentData && totalBudget > 0) {
                                "${((realTotalSpent / totalBudget) * 100).toInt()}% Pagu"
                            } else if (hasSpentData) {
                                "4 Kategori"
                            } else {
                                "50/30/10/10"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoldLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dedicated Sharia Compliance Banner (Located outside the donut ring to give full width)
            Surface(
                color = complianceStatus.color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, complianceStatus.color.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compliance_status_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (complianceStatus.isPositive) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = "Status Kepatuhan",
                        tint = complianceStatus.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = complianceStatus.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = complianceStatus.color
                        )
                        Text(
                            text = complianceStatus.detail,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Partitions list / interactive breakdown items
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                partitions.forEachIndexed { index, summary ->
                    val isSelected = selectedPartitionIndex == index
                    val percent = if (hasSpentData) ((summary.actualSpent / realTotalSpent) * 100).toInt() else 0
                    val isOverTarget = percent > (summary.partition.targetPercent + 10)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) summary.partition.color.copy(alpha = 0.14f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) summary.partition.color.copy(alpha = 0.5f) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedPartitionIndex = if (isSelected) null else index
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(summary.partition.color)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = summary.partition.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (isOverTarget) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Mendekati Israf",
                                            tint = ExpenseCoral,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "$currencySymbol ${nf.format(summary.actualSpent.toLong())}  •  Target: ${summary.partition.targetPercent.toInt()}%",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Percentage Pill
                        Surface(
                            color = summary.partition.color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (hasSpentData) "$percent%" else "${summary.partition.targetPercent.toInt()}% Target",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = summary.partition.color,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class PartitionSummary(
    val partition: ShariaBudgetPartition,
    val actualSpent: Double,
    val budgetCeiling: Double
)

private data class ShariaComplianceStatus(
    val label: String,
    val detail: String,
    val isPositive: Boolean,
    val color: Color
)

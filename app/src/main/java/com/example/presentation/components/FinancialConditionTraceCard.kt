package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.analytics.FinancialTraceabilityEngine
import com.example.core.analytics.PeriodDeltaMetric
import com.example.core.analytics.TraceBucket
import com.example.core.analytics.TracePeriodFilter
import com.example.core.analytics.TraceableJournalItem
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.isTactical
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FinancialConditionTraceCard(
    state: AmanahLedgerUiState,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTactical = state.themeMode.isTactical
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp)
    val subShape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp)
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
    val titleFont = if (isTactical) ChakraPetchFontFamily else null
    val monoFont = if (isTactical) ShareTechMonoFontFamily else null

    val nf = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")) }

    var selectedPeriod by remember { mutableStateOf(TracePeriodFilter.ALL_TIME) }
    var selectedBucket by remember { mutableStateOf(TraceBucket.SEMUA) }
    var searchQuery by remember { mutableStateOf("") }
    var visibleLimit by remember { mutableIntStateOf(6) }

    val report = remember(state.journalEntries, state.accounts, state.wallets, state.qardhRecords, selectedPeriod, selectedBucket, searchQuery) {
        FinancialTraceabilityEngine.analyze(
            state = state,
            periodFilter = selectedPeriod,
            selectedBucket = selectedBucket,
            searchQuery = searchQuery
        )
    }

    fun maskableRp(amount: Double, showSign: Boolean = false): String {
        if (isMasked) return "Rp ••••••"
        val prefix = when {
            showSign && amount > 0.0 -> "+Rp "
            showSign && amount < 0.0 -> "-Rp "
            amount < 0.0 -> "-Rp "
            else -> "Rp "
        }
        return "$prefix${nf.format(abs(amount).toLong())}"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KARTU 1: RAPOR KESEHATAN KEUANGAN (BERBASIS RASIO & INDIKATOR)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("financial_health_ratio_card"),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(
                1.dp,
                if (isTactical) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(pillShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isTactical) "// INDIKATOR KESEHATAN & RASIO KAS" else "Rapor Kesehatan & Rasio Keuangan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = titleFont,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Evaluasi persentase arus kas, ketahanan dana & tren bulanan",
                                fontSize = 11.sp,
                                fontFamily = monoFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleMask,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("trace_privacy_mask_btn")
                    ) {
                        Icon(
                            imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isMasked) "Tampilkan Nominal" else "Sensor Nominal",
                            tint = if (isMasked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Filter Periode
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TracePeriodFilter.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = {
                                selectedPeriod = period
                                visibleLimit = 6
                            },
                            label = { Text(period.label, fontSize = 11.sp, fontFamily = monoFont) },
                            leadingIcon = if (selectedPeriod == period) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            shape = pillShape,
                            modifier = Modifier.testTag("trace_period_chip_${period.name}")
                        )
                    }
                }

                // Banner Predikat Kesehatan Keuangan
                val statusIsGood = report.savingsRatePercent >= 0.0 && report.consumptionRatioPercent <= 80.0
                val statusColor = if (statusIsGood) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                Surface(
                    shape = subShape,
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Status (${report.periodFilter.label}): ${report.healthStatusLabel}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = titleFont,
                            color = statusColor
                        )
                        Text(
                            text = report.healthGuidance,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // 6 Indikator Rasio Keuangan (Tanpa Memaparkan Nominal Pribadi Secara Langsung)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HealthRatioTile(
                            title = "Rasio Tabungan",
                            value = "${formatRatio(report.savingsRatePercent)}%",
                            subtitle = "Surplus dari total masuk",
                            accent = if (report.savingsRatePercent >= 10.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            shape = subShape,
                            titleFont = titleFont,
                            modifier = Modifier.weight(1f)
                        )
                        HealthRatioTile(
                            title = "Rasio Konsumsi",
                            value = "${formatRatio(report.consumptionRatioPercent)}%",
                            subtitle = "Beban belanja vs masuk",
                            accent = if (report.consumptionRatioPercent <= 80.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            shape = subShape,
                            titleFont = titleFont,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HealthRatioTile(
                            title = "Ketahanan Dana",
                            value = "${formatRatio(report.emergencyRunwayMonths)} Bln",
                            subtitle = "Likuid vs belanja/bln",
                            accent = if (report.emergencyRunwayMonths >= 3.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            shape = subShape,
                            titleFont = titleFont,
                            modifier = Modifier.weight(1f)
                        )
                        HealthRatioTile(
                            title = "Rasio Hutang Qardh",
                            value = "${formatRatio(report.debtToAssetRatioPercent)}%",
                            subtitle = "Kewajiban vs saldo likuid",
                            accent = if (report.debtToAssetRatioPercent <= 30.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            shape = subShape,
                            titleFont = titleFont,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HealthRatioTile(
                            title = "Struktur Sumber Dana",
                            value = "${formatRatio(report.kasabSharePercent, 0)}% / ${formatRatio(report.nonKasabSharePercent, 0)}%",
                            subtitle = "Kasab Aktif / Non-Kasab",
                            accent = MaterialTheme.colorScheme.secondary,
                            shape = subShape,
                            titleFont = titleFont,
                            modifier = Modifier.weight(1f)
                        )
                        HealthRatioTile(
                            title = "Pos Belanja Dominan",
                            value = "${formatRatio(report.topExpenseSharePercent)}%",
                            subtitle = report.topExpenseCategoryName,
                            accent = MaterialTheme.colorScheme.onSurface,
                            shape = subShape,
                            titleFont = titleFont,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Perbandingan Bulan Ini vs Bulan Lalu (MoM)
                Text(
                    text = "Tren Bulan Ini vs Bulan Sebelumnya",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = titleFont,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = subShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoMRow(
                            label = "Pemasukan Bulanan",
                            metric = report.incomeMoM,
                            maskedAmount = maskableRp(report.incomeMoM.current),
                            positiveIsGood = true,
                            monoFont = monoFont
                        )
                        MoMRow(
                            label = "Belanja Konsumsi",
                            metric = report.expenseMoM,
                            maskedAmount = maskableRp(report.expenseMoM.current),
                            positiveIsGood = false,
                            monoFont = monoFont
                        )
                        MoMRow(
                            label = "Alokasi & Penyaluran Infaq",
                            metric = report.infaqMoM,
                            maskedAmount = maskableRp(report.infaqMoM.current),
                            positiveIsGood = true,
                            monoFont = monoFont
                        )
                    }
                }
            }
        }

        // KARTU 2: PENELUSUR JEJAK TRANSAKSI PEMBENTUK KONDISI KEUANGAN
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("financial_traceability_explorer_card"),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(pillShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ManageSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTactical) "// PENELUSURAN JEJAK TRANSAKSI" else "Telusuri Jejak Pembentuk Kondisi Keuangan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = titleFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lacak transaksi & alur akun Debit/Kredit di balik setiap angka analisa",
                            fontSize = 11.sp,
                            fontFamily = monoFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Ringkasan 4 Angka Periode Terpilih (Hormati Sensor Nominal)
                Surface(
                    shape = subShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pemasukan Periode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(maskableRp(report.periodIncomeTotal), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = monoFont, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Belanja Konsumsi Periode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(maskableRp(report.periodConsumptionTotal), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = monoFont, color = MaterialTheme.colorScheme.error)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Alokasi Vault Infaq Periode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(maskableRp(report.periodInfaqAllocated), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = monoFont, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Surplus / Defisit Bersih", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                maskableRp(report.periodNetSurplus, showSign = true),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = monoFont,
                                color = if (report.periodNetSurplus >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Filter Kelompok Transaksi (Buckets)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TraceBucket.entries.forEach { bucket ->
                        val count = report.bucketCounts[bucket] ?: 0
                        FilterChip(
                            selected = selectedBucket == bucket,
                            onClick = {
                                selectedBucket = bucket
                                visibleLimit = 6
                            },
                            label = { Text("${bucket.label} ($count)", fontSize = 11.sp, fontFamily = monoFont) },
                            shape = pillShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("trace_bucket_chip_${bucket.name}")
                        )
                    }
                }

                // Pencarian Deskripsi / Akun
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari deskripsi, nama akun, atau tanggal Hijriah", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Bersihkan pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = pillShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trace_search_input")
                )

                // Daftar Transaksi Pembentuk
                if (report.traceItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada transaksi yang cocok dengan filter ini.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val displayed = report.traceItems.take(visibleLimit)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        displayed.forEach { item ->
                            TraceableItemRow(
                                item = item,
                                formattedDate = dateFormat.format(item.date),
                                amountText = maskableRp(item.amount),
                                infaqText = if (item.infaqAllocated > 0.0) maskableRp(item.infaqAllocated) else null,
                                shape = subShape,
                                monoFont = monoFont
                            )
                        }
                    }

                    if (report.traceItems.size > visibleLimit) {
                        OutlinedButton(
                            onClick = { visibleLimit += 8 },
                            shape = pillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("trace_load_more_btn")
                        ) {
                            Text(
                                "Muat Lebih Banyak (${report.traceItems.size - visibleLimit} transaksi lagi)",
                                fontSize = 12.sp
                            )
                        }
                    } else if (visibleLimit > 6 && report.traceItems.size > 6) {
                        TextButton(
                            onClick = { visibleLimit = 6 },
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("trace_collapse_btn")
                        ) {
                            Text("Ringkas Daftar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthRatioTile(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    shape: androidx.compose.ui.graphics.Shape,
    titleFont: androidx.compose.ui.text.font.FontFamily?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = titleFont,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MoMRow(
    label: String,
    metric: PeriodDeltaMetric,
    maskedAmount: String,
    positiveIsGood: Boolean,
    monoFont: androidx.compose.ui.text.font.FontFamily?
) {
    val deltaPct = metric.deltaPercent
    val deltaStr = when {
        deltaPct == null -> "Belum ada pembanding"
        deltaPct >= 0.0 -> "+${String.format(Locale.forLanguageTag("id-ID"), "%.1f", deltaPct)}% vs bln lalu"
        else -> "${String.format(Locale.forLanguageTag("id-ID"), "%.1f", deltaPct)}% vs bln lalu"
    }
    val deltaColor = when {
        deltaPct == null -> MaterialTheme.colorScheme.onSurfaceVariant
        (deltaPct >= 0.0 && positiveIsGood) || (deltaPct < 0.0 && !positiveIsGood) -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(deltaStr, fontSize = 10.sp, color = deltaColor, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = maskedAmount,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = monoFont,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TraceableItemRow(
    item: TraceableJournalItem,
    formattedDate: String,
    amountText: String,
    infaqText: String?,
    shape: androidx.compose.ui.graphics.Shape,
    monoFont: androidx.compose.ui.text.font.FontFamily?
) {
    val accentColor = when (item.bucket) {
        TraceBucket.KASAB, TraceBucket.NON_KASAB -> MaterialTheme.colorScheme.primary
        TraceBucket.KONSUMSI -> MaterialTheme.colorScheme.error
        TraceBucket.INFAQ_ZAKAT -> MaterialTheme.colorScheme.secondary
        TraceBucket.TRANSFER_QARDH, TraceBucket.SEMUA -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.bucket.label} • $formattedDate (${item.hijriLabel})",
                    fontSize = 10.sp,
                    fontFamily = monoFont,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = amountText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = monoFont,
                    color = accentColor
                )
            }
            Text(
                text = item.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "D: ${item.debitAccounts}  →  K: ${item.creditAccounts}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (infaqText != null) {
                Text(
                    text = "↳ Alokasi Infaq Vault: $infaqText",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

private fun formatRatio(value: Double, decimals: Int = 1): String {
    return String.format(Locale.forLanguageTag("id-ID"), "%.${decimals}f", value)
}

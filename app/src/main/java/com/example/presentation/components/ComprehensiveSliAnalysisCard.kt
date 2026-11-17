package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.infaq.InfaqSliComprehensiveAnalysis
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.isTactical
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

@Composable
fun ComprehensiveSliAnalysisCard(
    state: AmanahLedgerUiState,
    onNavigateToVaultHistory: () -> Unit = {},
    onNavigateToInfaqRules: () -> Unit = {},
    isMasked: Boolean = state.securityConfig.isMaskBalance,
    onToggleMask: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val analysis: InfaqSliComprehensiveAnalysis = state.infaqSliAnalysis
    val isTactical = state.themeMode.isTactical
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp)
    val subShape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp)
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
    val titleFont = if (isTactical) ChakraPetchFontFamily else null
    val monoFont = if (isTactical) ShareTechMonoFontFamily else null
    val nf = remember { NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")) }

    fun maskRp(amount: Double, sign: Boolean = false): String {
        if (isMasked) return "Rp ••••••"
        val prefix = when {
            sign && amount > 0.0 -> "+Rp "
            sign && amount < 0.0 -> "-Rp "
            amount < 0.0 -> "-Rp "
            else -> "Rp "
        }
        return "$prefix${nf.format(abs(amount).toLong())}"
    }

    var showSourceBreakdown by remember { mutableStateOf(true) }
    var showAuditSimulator by remember { mutableStateOf(false) }

    // Input Simulator Audit Manual dimulai kosong agar tidak langsung menuliskan nominal pribadi pengguna
    var simIncomeText by remember { mutableStateOf("") }
    var simConsumptionText by remember { mutableStateOf("") }
    var simInfaqText by remember { mutableStateOf("") }
    var simTargetRateText by remember(analysis.expectedBlendedIncomeRatePercent) {
        val defaultPct = if (analysis.expectedBlendedIncomeRatePercent > 0.0) {
            analysis.expectedBlendedIncomeRatePercent
        } else 5.0
        mutableStateOf(String.format(Locale.US, "%.1f", defaultPct))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("comprehensive_sli_analysis_card"),
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(pillShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isTactical) "// AUDIT KOMPREHENSIF SLI & RASIO INFAQ" else "Audit Komprehensif SLI & Rasio Infaq",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = titleFont,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Transparansi rumus SLI (berbasis konsumsi) vs rasio dari total pendapatan & target wajib aturan",
                        fontSize = 11.sp,
                        fontFamily = monoFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onToggleMask,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .testTag("sli_privacy_mask_btn")
                ) {
                    Icon(
                        imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isMasked) "Tampilkan Nominal Rupiah" else "Sembunyikan Nominal Rupiah",
                        tint = if (isMasked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 2. PERBANDINGAN DUA BASIS RASIO (MENJAWAB PERBEDAAN 3,9% vs 3,4%)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Kolom A: SLI (Infaq / Belanja Konsumsi)
                Surface(
                    shape = subShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SLI (vs Konsumsi)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = monoFont,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${formatPct(analysis.sliConsumptionBasedPercent)}%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = titleFont,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isMasked) "Basis: Infaq Vault ÷ Total Konsumsi" else "${maskRp(analysis.totalAllocatedToVault)} ÷ Konsumsi ${maskRp(analysis.totalConsumptionExpense)}",
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Kolom B: Rasio dari Total Pendapatan
                Surface(
                    shape = subShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Rasio dr Pendapatan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = monoFont,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${formatPct(analysis.infaqToTotalIncomePercent)}%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = titleFont,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = if (isMasked) "Basis: Infaq Vault ÷ Total Pendapatan" else "${maskRp(analysis.totalAllocatedToVault)} ÷ Pendapatan ${maskRp(analysis.totalAllIncome)}",
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Penjelasan Formula Mengapa Kedua Angka Berbeda (tanpa mencantumkan nominal pribadi dalam paragraf)
            Surface(
                shape = pillShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildString {
                            append("Mengapa persentase SLI (${formatPct(analysis.sliConsumptionBasedPercent)}%) berbeda dengan rasio terhadap pendapatan (${formatPct(analysis.infaqToTotalIncomePercent)}%)? ")
                            append("SLI mengukur proporsi alokasi infaq terhadap total belanja konsumsi (gaya hidup), ")
                            append("sedangkan rasio pendapatan mengukur porsi alokasi infaq terhadap total seluruh pemasukan (Kasab + Non-Kasab).")
                        },
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Rasio Tambahan (Kasab saja & Total Semua Kanal Sedekah)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MetricKeyValueRow(
                    label = "Rasio Infaq Vault vs Pendapatan Kasab Saja",
                    value = "${formatPct(analysis.infaqToKasabIncomePercent)}%",
                    subDetail = "Basis Kasab: ${maskRp(analysis.totalIncomeKasab)}",
                    monoFont = monoFont
                )
                MetricKeyValueRow(
                    label = "Total Kedermawanan Semua Kanal vs Pendapatan",
                    value = "${formatPct(analysis.allChannelsToTotalIncomePercent)}%",
                    subDetail = "Vault + Sedekah Subuh/Ekstra (${maskRp(analysis.totalInfaqAllChannels)})",
                    monoFont = monoFont
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 3. AUDIT KEWAJIBAN & NOMINAL INFAQ YANG HARUS DIBAYARKAN
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Audit Kewajiban & Nominal Infaq yang Harus Dibayarkan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = titleFont,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mengacu aturan aktif: Kasab ${formatPct(analysis.kasabRuleRatePercent)}% • Non-Kasab ${formatPct(analysis.nonKasabRuleRatePercent)}% (+ Rikaz 20% / Syubhat 100% jika ada)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = subShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricKeyValueRow(
                            label = "Target Infaq Ideal (Tarif Efektif ${formatPct(analysis.expectedBlendedIncomeRatePercent)}%)",
                            value = maskRp(analysis.expectedTotalInfaqFromIncomeRules),
                            valueColor = MaterialTheme.colorScheme.onSurface,
                            monoFont = monoFont
                        )
                        MetricKeyValueRow(
                            label = "Sudah Dialokasikan ke Virtual Vault",
                            value = maskRp(analysis.totalAllocatedToVault),
                            valueColor = MaterialTheme.colorScheme.primary,
                            monoFont = monoFont
                        )
                        MetricKeyValueRow(
                            label = if (analysis.allocationGapAgainstRules >= 0.0) "Selisih Alokasi (Surplus di Atas Aturan)" else "Selisih Kurang Alokasi terhadap Aturan",
                            value = maskRp(analysis.allocationGapAgainstRules, sign = true),
                            valueColor = if (analysis.isMeetingIncomeRuleTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            monoFont = monoFont
                        )

                        // Progress bar kepatuhan target aturan
                        val complianceRatio = if (analysis.expectedTotalInfaqFromIncomeRules > 0.0) {
                            (analysis.totalAllocatedToVault / analysis.expectedTotalInfaqFromIncomeRules).toFloat().coerceIn(0f, 1f)
                        } else if (analysis.totalAllocatedToVault > 0.0) 1f else 0f

                        LinearProgressIndicator(
                            progress = { complianceRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(pillShape),
                            color = if (analysis.isMeetingIncomeRuleTarget) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                        MetricKeyValueRow(
                            label = "Sudah Disalurkan Nyata ke Mustahiq (dari Vault)",
                            value = maskRp(analysis.vaultDisbursedAmount),
                            valueColor = MaterialTheme.colorScheme.primary,
                            monoFont = monoFont
                        )
                        MetricKeyValueRow(
                            label = "Sisa Saldo Vault Wajib Segera Disalurkan",
                            value = maskRp(analysis.vaultRemainingPayable),
                            valueColor = if (analysis.vaultRemainingPayable > 0.0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            monoFont = monoFont
                        )

                        // Banner Rekomendasi Tindakan (tanpa memaparkan nominal mentah di teks)
                        Surface(
                            shape = pillShape,
                            color = if (analysis.combinedMustPayAmount > 0.0) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = if (analysis.combinedMustPayAmount > 0.0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (analysis.combinedMustPayAmount > 0.0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = buildString {
                                        if (analysis.combinedMustPayAmount <= 0.0) {
                                            append("Target aturan infaq terpenuhi dan saldo Virtual Vault telah disalurkan seluruhnya ke mustahiq.")
                                        } else {
                                            if (analysis.vaultRemainingPayable > 0.0) {
                                                append("Terdapat titipan saldo Vault yang perlu disalurkan ke mustahiq. ")
                                            }
                                            if (analysis.underAllocationShortfall > 0.5) {
                                                append("Alokasi infaq saat ini berada di bawah target tarif efektif (${formatPct(analysis.expectedBlendedIncomeRatePercent)}%) sesuai aturan aktif.")
                                            }
                                        }
                                    },
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Tombol Aksi Cepat
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToInfaqRules,
                        shape = pillShape,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("sli_audit_open_rules_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aturan Infaq", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onNavigateToVaultHistory,
                        shape = pillShape,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("sli_audit_disburse_vault_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salurkan Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 4. RINCIAN ASAL NOMINAL INFAQ & ZAKAT (EXPANDABLE)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(pillShape)
                        .clickable { showSourceBreakdown = !showSourceBreakdown }
                        .padding(vertical = 4.dp)
                        .testTag("sli_source_breakdown_toggle"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rincian Pembentuk Nominal Infaq",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = titleFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Telusuri komponen sumber alokasi Vault & kanal sedekah lain",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = if (showSourceBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Buka rincian sumber infaq",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = showSourceBreakdown,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = subShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MetricKeyValueRow(
                                label = "1. Alokasi Otomatis Pemasukan (INFLOW → Vault)",
                                value = maskRp(analysis.infaqFromIncomeAllocation),
                                monoFont = monoFont
                            )
                            MetricKeyValueRow(
                                label = "2. Round-Up Belanja Konsumsi (EXPENSE → Vault)",
                                value = maskRp(analysis.infaqFromExpenseRoundUp),
                                monoFont = monoFont
                            )
                            MetricKeyValueRow(
                                label = "3. Infaq Rezeki Non-Tunai (Masuk Vault)",
                                value = maskRp(analysis.infaqFromNonCashRezekiVault),
                                monoFont = monoFont
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            MetricKeyValueRow(
                                label = "Total Alokasi Infaq Vault (Basis Hitung SLI)",
                                value = maskRp(analysis.totalAllocatedToVault),
                                valueColor = MaterialTheme.colorScheme.primary,
                                monoFont = monoFont
                            )
                            MetricKeyValueRow(
                                label = "4. Sedekah Subuh Langsung (di luar Vault)",
                                value = maskRp(analysis.directSedekahSubuhAmount),
                                monoFont = monoFont
                            )
                            MetricKeyValueRow(
                                label = "5. Sedekah Ekstra & Rezeki Langsung",
                                value = maskRp(analysis.directExtraAndRezekiInfaqAmount),
                                monoFont = monoFont
                            )
                            if (analysis.zakatDisbursedAmount > 0.0) {
                                MetricKeyValueRow(
                                    label = "Penyaluran Zakat Tercatat (Dipisahkan dr Infaq)",
                                    value = maskRp(analysis.zakatDisbursedAmount),
                                    valueColor = MaterialTheme.colorScheme.secondary,
                                    monoFont = monoFont
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // 5. KALKULATOR UJI HITUNG & AUDIT MANUAL (SIMULASI INTERAKTIF)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(pillShape)
                        .clickable { showAuditSimulator = !showAuditSimulator }
                        .padding(vertical = 4.dp)
                        .testTag("sli_audit_simulator_toggle"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Simulasi & Uji Hitung Audit Manual",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = titleFont,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Verifikasi angka pendapatan, konsumsi, & persentase target infaq",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (showAuditSimulator) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Buka kalkulator audit SLI",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = showAuditSimulator,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val simIncome = simIncomeText.toDoubleOrNull() ?: 0.0
                    val simConsumption = simConsumptionText.toDoubleOrNull() ?: 0.0
                    val simInfaq = simInfaqText.toDoubleOrNull() ?: 0.0
                    val simTargetRate = (simTargetRateText.replace(',', '.').toDoubleOrNull() ?: 5.0).coerceIn(0.0, 100.0)

                    val simIncomePct = if (simIncome > 0.0) (simInfaq / simIncome) * 100.0 else 0.0
                    val simSliPct = if (simConsumption > 0.0) (simInfaq / simConsumption) * 100.0 else 0.0
                    val simTargetNominal = round(simIncome * (simTargetRate / 100.0))
                    val simDiff = simInfaq - simTargetNominal

                    Surface(
                        shape = subShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Input Simulasi Mandiri",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(
                                        onClick = {
                                            simIncomeText = ""
                                            simConsumptionText = ""
                                            simInfaqText = ""
                                        },
                                        modifier = Modifier.testTag("sli_simulator_clear_btn")
                                    ) {
                                        Text("Kosongkan", fontSize = 11.sp)
                                    }
                                    if (!isMasked) {
                                        TextButton(
                                            onClick = {
                                                simIncomeText = analysis.totalAllIncome.toLong().toString()
                                                simConsumptionText = analysis.totalConsumptionExpense.toLong().toString()
                                                simInfaqText = analysis.totalAllocatedToVault.toLong().toString()
                                                simTargetRateText = String.format(
                                                    Locale.US,
                                                    "%.1f",
                                                    if (analysis.expectedBlendedIncomeRatePercent > 0.0) analysis.expectedBlendedIncomeRatePercent else 5.0
                                                )
                                            },
                                            modifier = Modifier.testTag("sli_simulator_reset_btn")
                                        ) {
                                            Text("Ambil Data Buku Kas", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = simIncomeText,
                                    onValueChange = { simIncomeText = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Pendapatan (Rp)", fontSize = 11.sp) },
                                    placeholder = { Text("Masukkan nominal", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sli_sim_income_input")
                                )
                                OutlinedTextField(
                                    value = simConsumptionText,
                                    onValueChange = { simConsumptionText = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Konsumsi (Rp)", fontSize = 11.sp) },
                                    placeholder = { Text("Masukkan nominal", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sli_sim_consumption_input")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = simInfaqText,
                                    onValueChange = { simInfaqText = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Nominal Infaq (Rp)", fontSize = 11.sp) },
                                    placeholder = { Text("Masukkan nominal", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sli_sim_infaq_input")
                                )
                                OutlinedTextField(
                                    value = simTargetRateText,
                                    onValueChange = { simTargetRateText = it.filter { ch -> ch.isDigit() || ch == '.' || ch == ',' } },
                                    label = { Text("Target Infaq (%)", fontSize = 11.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sli_sim_target_rate_input")
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            MetricKeyValueRow(
                                label = "Persentase dari Total Pendapatan",
                                value = "${formatPct(simIncomePct, 2)}% (≈ ${formatPct(simIncomePct, 1)}%)",
                                valueColor = MaterialTheme.colorScheme.secondary,
                                monoFont = monoFont
                            )
                            MetricKeyValueRow(
                                label = "SLI (Persentase vs Konsumsi)",
                                value = "${formatPct(simSliPct, 2)}% (≈ ${formatPct(simSliPct, 1)}%)",
                                valueColor = MaterialTheme.colorScheme.primary,
                                monoFont = monoFont
                            )
                            MetricKeyValueRow(
                                label = "Target Nominal Wajib (${formatPct(simTargetRate, 1)}% × Pendapatan)",
                                value = "Rp ${nf.format(simTargetNominal.toLong())}",
                                monoFont = monoFont
                            )
                            MetricKeyValueRow(
                                label = if (simDiff >= 0.0) "Kelebihan dari Target" else "Kekurangan yang Harus Ditambah",
                                value = if (simDiff >= 0.0) "+Rp ${nf.format(simDiff.toLong())}" else "Rp ${nf.format(abs(simDiff).toLong())}",
                                valueColor = if (simDiff >= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                monoFont = monoFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricKeyValueRow(
    label: String,
    value: String,
    subDetail: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    monoFont: androidx.compose.ui.text.font.FontFamily? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!subDetail.isNullOrBlank()) {
                Text(
                    text = subDetail,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = monoFont,
            color = valueColor
        )
    }
}

private fun formatPct(value: Double, decimals: Int = 1): String {
    return String.format(Locale.forLanguageTag("id-ID"), "%.${decimals}f", value)
}

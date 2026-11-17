package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.Account
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.AssetBlue
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberTacticalGold
import com.example.ui.theme.isTactical
import java.text.NumberFormat
import java.util.Locale

private enum class BreakdownTab(val label: String, val icon: ImageVector) {
    KASAB("Kasab (Aktif)", Icons.Default.TrendingUp),
    NON_KASAB("Non-Kasab", Icons.Default.MonetizationOn),
    EXPENSE("Pengeluaran", Icons.Default.ShoppingBag),
    SAVINGS("Aset & Infaq", Icons.Default.Savings)
}

@Composable
fun CashflowBreakdownDetailCard(
    state: AmanahLedgerUiState,
    isMasked: Boolean = state.securityConfig.isMaskBalance,
    modifier: Modifier = Modifier
) {
    val isTactical = state.themeMode.isTactical
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp)
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)

    val nf = remember { NumberFormat.getNumberInstance(Locale.GERMANY) }

    var selectedTab by remember { mutableStateOf(BreakdownTab.KASAB) }
    var isAsetBersihExpanded by remember { mutableStateOf(true) }
    var isIsrafInfoExpanded by remember { mutableStateOf(false) }

    val kasabList = remember(state.journalEntries, state.accounts) { state.getKasabBreakdown() }
    val nonKasabList = remember(state.journalEntries, state.accounts) { state.getNonKasabBreakdown() }
    val expenseList = remember(state.journalEntries, state.accounts) { state.getExpenseBreakdown() }

    val totalIncome = state.totalIncomeKasab + state.totalIncomeNonKasab
    val totalExpense = state.totalConsumptionExpense
    val totalInfaq = state.totalPurifiedInfaq
    val netSavings = state.netSavingsAmount

    fun maskRp(amount: Double, sign: Boolean = false): String {
        if (isMasked) return "Rp ••••••"
        val absVal = kotlin.math.abs(amount).toLong()
        val p = when {
            sign && amount > 0 -> "+Rp "
            amount < 0 -> "-Rp "
            else -> "Rp "
        }
        return "$p${nf.format(absVal)}"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. KOTAK PENJELASAN KHUSUS: APA ITU ASET BERSIH / TABUNGAN?
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("aset_bersih_explainer_card"),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isAsetBersihExpanded = !isAsetBersihExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(pillShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTactical) "// PENJELASAN: ASET BERSIH / TABUNGAN" else "Apa itu Aset Bersih / Tabungan?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTactical) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (netSavings >= 0) "Surplus Arus Kas: ${maskRp(netSavings, sign = true)}" else "Defisit Arus Kas: ${maskRp(netSavings)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (netSavings >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    IconButton(
                        onClick = { isAsetBersihExpanded = !isAsetBersihExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isAsetBersihExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Penjelasan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isAsetBersihExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Aset Bersih / Tabungan dalam diagram arus kas ini adalah sisa uang surplus riil yang berhasil Anda simpan setelah seluruh rezeki masuk dipotong belanja konsumsi dan titipan infaq.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Formula Box Transparan
                        Surface(
                            shape = pillShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Pemasukan (Kasab + Non-Kasab)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(maskRp(totalIncome), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Belanja Konsumsi (Pengeluaran)", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    Text(if (isMasked) "Rp ••••••" else "- Rp ${nf.format(totalExpense.toLong())}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Titipan Infaq Disucikan (Vault)", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(if (isMasked) "Rp ••••••" else "- Rp ${nf.format(totalInfaq.toLong())}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Sisa Surplus = Aset Bersih / Tabungan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        maskRp(netSavings),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (netSavings >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        // Penjelasan Makna Kondisi Keuangan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (netSavings >= 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (netSavings >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (netSavings >= 0) {
                                    "Alhamdulillah, arus kas Anda surplus (+). Dana ini otomatis berakumulasi menjadi saldo kas fisik, rekening bank, atau tabungan emas Anda."
                                } else {
                                    "Perhatian: Arus kas Anda defisit (-). Pengeluaran belanja lebih besar daripada pemasukan, sehingga Anda sedang memakai tabungan masa lalu Anda."
                                },
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 2. KOTAK RINCIAN DETAIL: KASAB, NON-KASAB, PENGELUARAN
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cashflow_itemized_breakdown_card"),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                if (isTactical) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Rincian
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(pillShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTactical) "// RINCIAN SUMBER & ALOKASI DANA" else "Rincian Sumber & Alokasi Dana",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTactical) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Detail per akun nominal kasab, non-kasab, dan jenis belanja",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs / Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BreakdownTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            shape = pillShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (tab) {
                                    BreakdownTab.KASAB -> MaterialTheme.colorScheme.primaryContainer
                                    BreakdownTab.NON_KASAB -> MaterialTheme.colorScheme.secondaryContainer
                                    BreakdownTab.EXPENSE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                    BreakdownTab.SAVINGS -> AssetBlue.copy(alpha = 0.25f)
                                },
                                selectedLabelColor = when (tab) {
                                    BreakdownTab.KASAB -> MaterialTheme.colorScheme.primary
                                    BreakdownTab.NON_KASAB -> MaterialTheme.colorScheme.secondary
                                    BreakdownTab.EXPENSE -> MaterialTheme.colorScheme.error
                                    BreakdownTab.SAVINGS -> AssetBlue
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("breakdown_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content Rincian berdasarkan Tab
                when (selectedTab) {
                    BreakdownTab.KASAB -> {
                        BreakdownSectionContent(
                            categoryTitle = "Pemasukan Kasab (Usaha & Kerja Aktif)",
                            categoryDescription = "Pendapatan halal yang diperoleh melalui ikhtiar kerja, gaji pokok/ujrah, atau margin perniagaan dagang halal.",
                            totalAmount = state.totalIncomeKasab,
                            items = kasabList,
                            nf = nf,
                            isTactical = isTactical,
                            isMasked = isMasked,
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                    BreakdownTab.NON_KASAB -> {
                        BreakdownSectionContent(
                            categoryTitle = "Pemasukan Non-Kasab (Pasif & Hadiah)",
                            categoryDescription = "Rezeki yang datang tanpa akad kerja langsung: hibah, hadiah keluarga/rekan, rikaz (harta karun kuno), dan karantina syubhat.",
                            totalAmount = state.totalIncomeNonKasab,
                            items = nonKasabList,
                            nf = nf,
                            isTactical = isTactical,
                            isMasked = isMasked,
                            accentColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                    BreakdownTab.EXPENSE -> {
                        BreakdownSectionContent(
                            categoryTitle = "Pengeluaran (Belanja & Operasional)",
                            categoryDescription = "Seluruh pos konsumsi riil rumah tangga: pangan, transportasi, tagihan, pendidikan, kesehatan, serta kategori kustom.",
                            totalAmount = state.totalConsumptionExpense,
                            items = expenseList,
                            nf = nf,
                            isTactical = isTactical,
                            isMasked = isMasked,
                            accentColor = MaterialTheme.colorScheme.error
                        )
                    }
                    BreakdownTab.SAVINGS -> {
                        SavingsAndInfaqBreakdownContent(
                            state = state,
                            nf = nf,
                            isTactical = isTactical,
                            isMasked = isMasked
                        )
                    }
                }
            }
        }

        // 3. KOTAK EDUKASI: APA ITU ISRAF DAN CARA MENCEGAHNYA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("israf_education_card"),
            shape = cardShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                if (isTactical) MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isIsrafInfoExpanded = !isIsrafInfoExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(pillShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTactical) "// PRINSIP SYARIAH: PENGAWASAN ISRAF" else "Prinsip Syariah: Pengawasan Israf",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTactical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Memahami batasan boros menurut kaidah fiqh finansial",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { isIsrafInfoExpanded = !isIsrafInfoExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isIsrafInfoExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Israf Info",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isIsrafInfoExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "\"Dan janganlah kamu berlebih-lebihan. Sesungguhnya Allah tidak menyukai orang-orang yang berlebih-lebihan.\" (QS. Al-An'am: 141)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isTactical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Dalam fiqh Islam, Israf adalah membelanjakan harta melebihi batas kewajaran finansial atau berlebihan dalam perkara yang sebenarnya mubah (halal).",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Fitur 'Analisis & Israf' memantau perbandingan antara belanja konsumsi vs pendapatan kasab dan tabungan Anda. Bila belanja konsumsi mendekati pagu atau arus kas berbalik defisit, sistem memberikan sinyal agar pola belanja tetap seimbang dan berkah.",
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownSectionContent(
    categoryTitle: String,
    categoryDescription: String,
    totalAmount: Double,
    items: List<Pair<Account, Double>>,
    nf: NumberFormat,
    isTactical: Boolean,
    isMasked: Boolean,
    accentColor: Color
) {
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Subheader Total
        Surface(
            shape = pillShape,
            color = accentColor.copy(alpha = 0.12f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(categoryTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(categoryDescription, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Kelompok:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                if (isMasked) "Rp ••••••" else "Rp ${nf.format(totalAmount.toLong())}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada transaksi tercatat pada kelompok ini.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { (acc, amount) ->
                    val percentage = if (totalAmount > 0) ((amount / totalAmount) * 100).toFloat() else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = acc.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (acc.description.isNotBlank()) {
                                    Text(
                                        text = acc.description,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isMasked) "Rp ••••••" else "Rp ${nf.format(amount.toLong())}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${String.format(Locale.GERMANY, "%.1f", percentage)}%",
                                    fontSize = 10.sp,
                                    color = accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { (percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = accentColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsAndInfaqBreakdownContent(
    state: AmanahLedgerUiState,
    nf: NumberFormat,
    isTactical: Boolean,
    isMasked: Boolean
) {
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Item 1: Virtual Infaq Vault
        Surface(
            shape = pillShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(pillShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Virtual Infaq Vault (Hak Mustahiq)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Dana penyucian dari rezeki masuk yang siap disalurkan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(
                    if (isMasked) "Rp ••••••" else "Rp ${nf.format(state.totalPurifiedInfaq.toLong())}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Item 2: Aset Bersih / Tabungan
        Surface(
            shape = pillShape,
            color = AssetBlue.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, AssetBlue.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(pillShape)
                        .background(AssetBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = AssetBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Akumulasi Aset Bersih / Tabungan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Surplus yang tersimpan di kas, bank & emas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(
                    if (isMasked) "Rp ••••••" else "Rp ${nf.format(state.netSavingsAmount.toLong())}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.netSavingsAmount >= 0) AssetBlue else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

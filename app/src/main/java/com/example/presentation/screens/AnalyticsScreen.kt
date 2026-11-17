package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.report.FinancialPdfGenerator
import com.example.core.report.PdfDateRange
import com.example.core.report.PdfPeriodFilterHelper
import com.example.core.state.AmanahLedgerViewModel
import com.example.presentation.components.BudgetAllocationHealthChart
import com.example.presentation.components.CashflowBreakdownDetailCard
import com.example.presentation.components.ComprehensiveSliAnalysisCard
import com.example.presentation.components.FinancialConditionTraceCard
import com.example.presentation.components.FlowNode
import com.example.presentation.components.HijriRadialHeatmap
import com.example.presentation.components.PdfDateRangeCard
import com.example.presentation.components.SankeyFlowDiagram
import com.example.presentation.components.SpendingTrendLineChart
import com.example.ui.theme.AssetBlue
import com.example.ui.theme.TacticalSubScreenHeader
import com.example.ui.theme.isTactical
import com.example.ui.theme.White12
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onNavigateToVaultHistory: () -> Unit = {},
    onNavigateToInfaqRules: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showExportDialog by remember { mutableStateOf(false) }
    var analyticsDateRange by remember { mutableStateOf(PdfDateRange()) }

    val isTactical = state.themeMode.isTactical
    val isMasked = state.securityConfig.isMaskBalance
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(14.dp)

    val scopedState = remember(state, analyticsDateRange) {
        if (!analyticsDateRange.isCustomEnabled) state
        else {
            val scoped = PdfPeriodFilterHelper.compute(state, analyticsDateRange)
            state.copy(
                journalEntries = scoped.filteredEntries,
                infaqDistributions = scoped.filteredDistributions,
                walletMutations = scoped.filteredMutations,
                rezekiNonTunaiList = scoped.filteredRezeki,
                ibadahDeposits = scoped.filteredIbadahDeposits,
                sedekahSubuhState = scoped.filteredSubuhState
            )
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val errorColor = MaterialTheme.colorScheme.error

    val sources = remember(scopedState.totalIncomeKasab, scopedState.totalIncomeNonKasab, primaryColor, secondaryColor) {
        listOf(
            FlowNode(
                label = "Kasab (Aktif)",
                value = scopedState.totalIncomeKasab.coerceAtLeast(1000.0),
                color = primaryColor
            ),
            FlowNode(
                label = "Non-Kasab (Windfall)",
                value = scopedState.totalIncomeNonKasab.coerceAtLeast(1000.0),
                color = secondaryColor
            )
        )
    }

    val netSavings = (scopedState.totalIncomeKasab + scopedState.totalIncomeNonKasab - scopedState.totalConsumptionExpense - scopedState.totalPurifiedInfaq)
        .coerceAtLeast(1000.0)

    val targets = remember(scopedState.totalConsumptionExpense, scopedState.totalPurifiedInfaq, netSavings, errorColor, secondaryColor) {
        listOf(
            FlowNode(
                label = "Belanja Konsumsi",
                value = scopedState.totalConsumptionExpense.coerceAtLeast(1000.0),
                color = errorColor
            ),
            FlowNode(
                label = "Vault Infaq Amanah",
                value = scopedState.totalPurifiedInfaq.coerceAtLeast(1000.0),
                color = secondaryColor
            ),
            FlowNode(
                label = "Aset Bersih / Tabungan",
                value = netSavings,
                color = AssetBlue
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isTactical) {
                TacticalSubScreenHeader(
                    title = "Analisis & Audit SLI",
                    subCode = "ISRAF//SLI-AUDIT",
                    onBack = onNavigateBack,
                    trailingActions = {
                        IconButton(
                            onClick = { viewModel.toggleBalancePrivacy() },
                            modifier = Modifier.testTag("analytics_privacy_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isMasked) "Tampilkan Nominal" else "Sensor Nominal",
                                tint = if (isMasked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                val cal = java.util.Calendar.getInstance()
                                val pdfFile = FinancialPdfGenerator.generateMonthlyReportPdf(
                                    context = context,
                                    state = state,
                                    targetMonth = cal.get(java.util.Calendar.MONTH),
                                    targetYear = cal.get(java.util.Calendar.YEAR)
                                )
                                FinancialPdfGenerator.viewPdfReport(context, pdfFile)
                                Toast.makeText(context, "Laporan PDF Berhasil Diunduh!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("download_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Unduh PDF Laporan Bulanan",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("export_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Ekspor Buku Besar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("analytics_menu_sidebar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Buka Menu Sidebar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("analytics_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "Analisis & Israf",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Visualisasi Arus Kas, Audit SLI & Monitoring Israf",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleBalancePrivacy() },
                            modifier = Modifier.testTag("analytics_privacy_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isMasked) "Tampilkan Nominal" else "Sensor Nominal",
                                tint = if (isMasked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val cal = java.util.Calendar.getInstance()
                                val pdfFile = FinancialPdfGenerator.generateMonthlyReportPdf(
                                    context = context,
                                    state = state,
                                    targetMonth = cal.get(java.util.Calendar.MONTH),
                                    targetYear = cal.get(java.util.Calendar.YEAR)
                                )
                                FinancialPdfGenerator.viewPdfReport(context, pdfFile)
                                Toast.makeText(context, "Laporan PDF Berhasil Diunduh!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("download_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Unduh PDF Laporan Bulanan",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        IconButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier.testTag("export_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Ekspor Buku Besar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("analytics_menu_sidebar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Buka Menu Sidebar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Kartu Pemilihan Rentang Waktu & Kalender (Masehi / Hijriyah)
            PdfDateRangeCard(
                uiState = state,
                dateRange = analyticsDateRange,
                onDateRangeChange = { analyticsDateRange = it },
                compact = false
            )

            // 2. Audit Spiritual Liquidity Index (SLI) & Rasio Infaq
            ComprehensiveSliAnalysisCard(
                state = scopedState,
                onNavigateToVaultHistory = onNavigateToVaultHistory,
                onNavigateToInfaqRules = onNavigateToInfaqRules,
                isMasked = isMasked,
                onToggleMask = { viewModel.toggleBalancePrivacy() }
            )

            // 3. Rapor Kesehatan Keuangan & Jejak Transaksi Pembentuk
            FinancialConditionTraceCard(
                state = scopedState,
                isMasked = isMasked,
                onToggleMask = { viewModel.toggleBalancePrivacy() }
            )

            // 4. Rincian Terperinci Aliran Arus Kas & Tabungan Bersih
            CashflowBreakdownDetailCard(state = scopedState, isMasked = isMasked)

            // 5. Sankey Flow Diagram (Visualisasi Sumber Rezeki ke Belanja & Vault)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = cardShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sankey Cashflow Arus Dana",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visualisasi aliran dari sumber rezeki ke pos belanja, vault infaq, dan akumulasi harta.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SankeyFlowDiagram(sources = sources, targets = targets, isMasked = isMasked)
                }
            }

            // 6. Grafik Tren Belanja Harian & Kepatuhan Batas
            SpendingTrendLineChart(
                journalEntries = scopedState.journalEntries,
                budgets = scopedState.budgets,
                isMasked = isMasked,
                isTactical = isTactical
            )

            // 7. Monitor Kesehatan Alokasi Anggaran Bulanan
            BudgetAllocationHealthChart(
                budgets = scopedState.budgets,
                getMonthlySpent = { accountId ->
                    scopedState.getMonthlySpentForAccount(accountId)
                },
                isMasked = isMasked,
                isTactical = isTactical
            )

            // 8. Hijri Radial Heatmap Kedermawanan
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = cardShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hijri Radial Heatmap Infaq",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Intensitas kedermawanan sepanjang 12 bulan kalender Hijriah (Emas = Ramadan & Puncak Berkah).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    HijriRadialHeatmap(entries = scopedState.journalEntries)
                }
            }
        }
    }

    if (showExportDialog) {
        val csvPreview = buildString {
            appendLine("ID;Tanggal_Masehi;Tahun_Hijriah;Bulan_Hijriah;Hari_Hijriah;Keterangan;Tipe;Total_Debit;Total_Kredit;Status")
            for (e in state.journalEntries) {
                appendLine("${e.id.take(8)};${e.gregorianDate};${e.hijriYear};${e.hijriMonth};${e.hijriDay};${e.description};${e.transactionType};${e.totalDebit};${e.totalCredit};BALANCED")
            }
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Ekspor Buku Besar Syariah",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Format CSV Double-Entry (${state.journalEntries.size} entri):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = csvPreview,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(csvPreview))
                        Toast.makeText(context, "Data CSV disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Salin CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.isTactical
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.core.report.FinancialPdfGenerator
import com.example.core.report.PdfCalendarMode
import com.example.core.report.PdfDateRange
import com.example.core.state.AmanahLedgerViewModel
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onNavigateToReceiptArchive: (() -> Unit)? = null,
    onEditTransaction: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val isTactical = state.themeMode.isTactical
    val context = LocalContext.current

    val currentCal = remember { Calendar.getInstance() }
    val currentHijri = remember { HijriCalendarEngine.fromGregorian(currentCal.time) }
    var isHijriCalendarBasis by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }
    var selectedHijriMonth by remember { mutableIntStateOf(currentHijri.month) } // 1..12
    var selectedHijriYear by remember { mutableIntStateOf(currentHijri.year) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showMonthlyEditorialPreview by remember { mutableStateOf(false) }
    var monthlyPdfDateRange by remember(
        isHijriCalendarBasis,
        selectedHijriMonth,
        selectedHijriYear,
        state.selectedHijriOffset
    ) {
        mutableStateOf(
            if (isHijriCalendarBasis) {
                PdfDateRange.forHijriMonth(selectedHijriYear, selectedHijriMonth, state.selectedHijriOffset)
            } else {
                PdfDateRange.allTime(PdfCalendarMode.MASEHI)
            }
        )
    }

    // Transaction Pagination & Filter State
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("SEMUA") }
    var onlyWithReceipts by remember { mutableStateOf(false) }
    var showAllPeriodsInJournal by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    var previewReceiptPath by remember { mutableStateOf<String?>(null) }
    var entryToDelete by remember { mutableStateOf<com.example.core.accounting.JournalEntry?>(null) }
    val pageSize = 8

    val reportCal = remember(selectedMonth, selectedYear) {
        Calendar.getInstance().apply {
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.YEAR, selectedYear)
        }
    }
    val gregorianMonthName = remember(reportCal) {
        SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(reportCal.time)
    }
    val gregorianHijriEquivalent = remember(reportCal) {
        HijriCalendarEngine.fromGregorian(reportCal.time)
    }

    val hijriMonthName = remember(selectedHijriMonth, selectedHijriYear) {
        val name = HijriDate.monthNames.getOrElse(selectedHijriMonth - 1) { "Bulan $selectedHijriMonth" }
        "$name $selectedHijriYear H"
    }

    val displayPrimaryMonth = if (isHijriCalendarBasis) hijriMonthName else gregorianMonthName
    val displaySecondaryMonth = if (isHijriCalendarBasis) {
        "Periode Kalender Hijriyah Islam (Bulan ke-$selectedHijriMonth)"
    } else {
        "${gregorianHijriEquivalent.monthName} ${gregorianHijriEquivalent.year} H"
    }

    // Filter transaksi untuk periode bulan terpilih (Masehi vs Hijriyah)
    val periodEntries = remember(state.journalEntries, isHijriCalendarBasis, selectedMonth, selectedYear, selectedHijriMonth, selectedHijriYear) {
        state.journalEntries.filter { entry ->
            if (isHijriCalendarBasis) {
                entry.hijriMonth == selectedHijriMonth && entry.hijriYear == selectedHijriYear
            } else {
                val c = Calendar.getInstance().apply { time = entry.gregorianDate }
                c.get(Calendar.MONTH) == selectedMonth && c.get(Calendar.YEAR) == selectedYear
            }
        }
    }

    val periodIncome = remember(periodEntries) {
        periodEntries.filter { it.transactionType == "INFLOW" || it.transactionType == "INCOME" }.sumOf { it.primaryAmount }
    }
    val periodExpense = remember(periodEntries) {
        periodEntries.filter { it.transactionType == "EXPENSE" }.sumOf { it.primaryAmount }
    }
    val periodInfaq = remember(periodEntries) {
        periodEntries.filter { it.transactionType.contains("INFAQ") || it.transactionType == "ZAKAT" }.sumOf { it.primaryAmount }
    }
    val periodInternalTransfer = remember(periodEntries) {
        periodEntries.filter { it.transactionType == "TRANSFER" }.sumOf { it.primaryAmount }
    }

    fun formatRupiah(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Laporan Keuangan Bulanan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Ringkasan Anggaran, Infaq & Neraca",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("report_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("report_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // 1. Month Selector Bar (Dual Calendar Basis: Masehi vs Hijriyah)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("monthly_report_basis_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Toggle Segmented Control: Masehi vs Hijriyah
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                                    .background(if (!isHijriCalendarBasis) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        isHijriCalendarBasis = false
                                        currentPage = 1
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("report_mode_masehi"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Kalender Masehi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isHijriCalendarBasis) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                                    .background(if (isHijriCalendarBasis) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        isHijriCalendarBasis = true
                                        currentPage = 1
                                    }
                                    .padding(vertical = 8.dp)
                                    .testTag("report_mode_hijriyah"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Kalender Hijriyah 🌙",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHijriCalendarBasis) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Baris Navigasi Bulan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    currentPage = 1
                                    if (isHijriCalendarBasis) {
                                        if (selectedHijriMonth == 1) {
                                            selectedHijriMonth = 12
                                            selectedHijriYear--
                                        } else {
                                            selectedHijriMonth--
                                        }
                                    } else {
                                        if (selectedMonth == 0) {
                                            selectedMonth = 11
                                            selectedYear--
                                        } else {
                                            selectedMonth--
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("prev_month_button")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "Bulan Lalu", modifier = Modifier.size(16.dp))
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = displayPrimaryMonth,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = displaySecondaryMonth,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            IconButton(
                                onClick = {
                                    currentPage = 1
                                    if (isHijriCalendarBasis) {
                                        if (selectedHijriMonth == 12) {
                                            selectedHijriMonth = 1
                                            selectedHijriYear++
                                        } else {
                                            selectedHijriMonth++
                                        }
                                    } else {
                                        if (selectedMonth == 11) {
                                            selectedMonth = 0
                                            selectedYear++
                                        } else {
                                            selectedMonth++
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("next_month_button")
                            ) {
                                Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = "Bulan Depan", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // 2. Action Card: Generate & Download PDF
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    shape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dokumen PDF Siap Diunduh",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Berisi neraca aset, status anggaran, dan distribusi infaq.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isGenerating = true
                                    try {
                                        val file = FinancialPdfGenerator.generateMonthlyReportPdf(
                                            context = context,
                                            state = state,
                                            targetMonth = selectedMonth,
                                            targetYear = selectedYear,
                                            dateRange = monthlyPdfDateRange
                                        )
                                        generatedPdfFile = file
                                        FinancialPdfGenerator.viewPdfReport(context, file)
                                        Toast.makeText(context, "PDF Berhasil Dibuat: ${file.name}", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isGenerating = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("download_pdf_report_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Unduh & Buka PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val file = generatedPdfFile ?: FinancialPdfGenerator.generateMonthlyReportPdf(
                                            context = context,
                                            state = state,
                                            targetMonth = selectedMonth,
                                            targetYear = selectedYear,
                                            dateRange = monthlyPdfDateRange
                                        )
                                        generatedPdfFile = file
                                        FinancialPdfGenerator.sharePdfReport(context, file)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(44.dp)
                                    .testTag("share_pdf_report_button")
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showMonthlyEditorialPreview = true },
                            shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("monthly_preview_editorial_pdf_button")
                        ) {
                            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pratinjau Halaman & Ubah Redaksi (${state.pdfEditorialConfig.stylePreset.badge})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 3. Executive Metrics Overview (Period-Specific)
            item {
                Text(
                    text = "Ringkasan Kinerja Keuangan Periode $displayPrimaryMonth",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Pemasukan Kasab Periode", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatRupiah(periodIncome)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("${periodEntries.count { it.transactionType == "INFLOW" || it.transactionType == "INCOME" }} transaksi masuk", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Beban Konsumsi Periode", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatRupiah(periodExpense)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("${periodEntries.count { it.transactionType == "EXPENSE" }} transaksi belanja", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Infaq & Zakat Periode", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatRupiah(periodInfaq)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("Penyucian harta & syukur", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Aset Halal (Neraca)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatRupiah(state.totalAssets)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Kas, tabungan & emas", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 4. Status Alokasi Anggaran
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Realisasi Anggaran", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = "Total Limit: Rp ${formatRupiah(state.totalMonthlyBudgetLimit)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (state.budgets.isEmpty()) {
                            Text("Belum ada data anggaran", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            state.budgets.forEach { budget ->
                                val spent = state.getMonthlySpentForAccount(budget.accountId, selectedMonth, selectedYear)
                                val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).coerceIn(0.0, 1.0) else 0.0
                                val percentage = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit) * 100.0 else 0.0
                                val isOver = spent > budget.monthlyLimit

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(budget.categoryName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            text = if (isOver) "OVER (${String.format("%.0f", percentage)}%)" else "${String.format("%.0f", percentage)}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio.toFloat() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(if (isTactical) CutCornerShape(2.dp) else RoundedCornerShape(3.dp)),
                                        color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Terpakai Rp ${formatRupiah(spent)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Limit Rp ${formatRupiah(budget.monthlyLimit)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Penyaluran Infaq & Mustahiq Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Distribusi Tabarru' & Infaq", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Infaq Tersucikan Masuk", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(state.totalPurifiedInfaq)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Tersalurkan ke Mustahiq", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(state.totalDisbursedInfaq)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        val sliAnalysis = state.infaqSliAnalysis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("SLI (Infaq / Konsumsi)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${String.format(Locale.forLanguageTag("id-ID"), "%.1f", sliAnalysis.sliConsumptionBasedPercent)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rasio Infaq / Pendapatan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${String.format(Locale.forLanguageTag("id-ID"), "%.1f", sliAnalysis.infaqToTotalIncomePercent)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Sisa Wajib Salur Vault", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "Rp ${formatRupiah(sliAnalysis.vaultRemainingPayable)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 6. Riwayat Transaksi Terpaginasi & Filtered
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Riwayat Transaksi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "${periodEntries.size} Transaksi (${if (isHijriCalendarBasis) "Hijriyah" else "Masehi"})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                currentPage = 1
                            },
                            placeholder = { Text("Cari deskripsi, catatan, atau toko...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = ""; currentPage = 1 }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val filters = listOf("SEMUA" to "Semua", "EXPENSE" to "Beban", "INCOME" to "Kasab", "TRANSFER" to "Transfer", "INFAQ" to "Infaq")
                            filters.forEach { (typeKey, label) ->
                                FilterChip(
                                    selected = selectedTypeFilter == typeKey,
                                    onClick = {
                                        selectedTypeFilter = typeKey
                                        currentPage = 1
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }

                            FilterChip(
                                selected = showAllPeriodsInJournal,
                                onClick = {
                                    showAllPeriodsInJournal = !showAllPeriodsInJournal
                                    currentPage = 1
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(13.dp))
                                },
                                label = { Text("Semua Periode (Arsip)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier.testTag("journal_all_periods_chip")
                            )

                            FilterChip(
                                selected = onlyWithReceipts,
                                onClick = {
                                    onlyWithReceipts = !onlyWithReceipts
                                    currentPage = 1
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(13.dp))
                                },
                                label = { Text("Ada Struk 📎", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )

                            if (onNavigateToReceiptArchive != null) {
                                AssistChip(
                                    onClick = onNavigateToReceiptArchive,
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            modifier = Modifier.size(13.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    label = { Text("Galeri Struk ↗", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filter items dari periode bulan terpilih atau seluruh riwayat (Arsip)
                        val sourceEntries = if (showAllPeriodsInJournal) {
                            state.journalEntries.sortedByDescending { it.gregorianDate.time }
                        } else {
                            periodEntries
                        }
                        val filteredList = sourceEntries.filter { entry ->
                            val matchesType = when (selectedTypeFilter) {
                                "SEMUA" -> true
                                "EXPENSE" -> entry.transactionType.equals("EXPENSE", ignoreCase = true)
                                "INCOME" -> entry.transactionType.equals("INFLOW", ignoreCase = true) || entry.transactionType.equals("INCOME", ignoreCase = true)
                                "TRANSFER" -> entry.transactionType.equals("TRANSFER", ignoreCase = true)
                                "INFAQ" -> entry.transactionType.contains("INFAQ", ignoreCase = true)
                                else -> entry.transactionType.contains(selectedTypeFilter, ignoreCase = true)
                            }
                            val matchesSearch = searchQuery.isBlank() ||
                                entry.description.contains(searchQuery, ignoreCase = true)
                            val matchesReceipt = !onlyWithReceipts || (entry.receiptAttachment != null)
                            matchesType && matchesSearch && matchesReceipt
                        }

                        val totalCount = filteredList.size
                        val totalPages = maxOf(1, ceil(totalCount.toDouble() / pageSize).toInt())
                        val safePage = currentPage.coerceIn(1, totalPages)
                        val pageItems = filteredList.drop((safePage - 1) * pageSize).take(pageSize)

                        if (pageItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (searchQuery.isNotBlank() || selectedTypeFilter != "SEMUA")
                                        "Tidak ada transaksi yang cocok dengan filter."
                                    else
                                        "Belum ada catatan transaksi.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pageItems.forEach { entry ->
                                    val isIncome = entry.transactionType.equals("INFLOW", ignoreCase = true) || entry.transactionType.equals("INCOME", ignoreCase = true)
                                    val isExpense = entry.transactionType.equals("EXPENSE", ignoreCase = true)
                                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(entry.gregorianDate)
                                    val walletName = state.wallets.firstOrNull { w ->
                                        entry.lines.any { it.accountId == w.linkedAccountId }
                                    }?.name ?: entry.lines.firstOrNull { line ->
                                        line.accountId != "acc_disbursed" && state.accounts.any { it.id == line.accountId }
                                    }?.let { line ->
                                        state.getAccount(line.accountId)?.name
                                    } ?: "Dompet Kas"
                                    val displayAmount = entry.primaryAmount

                                    Card(
                                        shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(end = 12.dp)
                                                ) {
                                                    Text(entry.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(" • ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(walletName, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }

                                                Text(
                                                    text = "${if (isIncome) "+" else if (isExpense) "-" else ""}Rp ${formatRupiah(displayAmount)}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    color = when {
                                                        isIncome -> MaterialTheme.colorScheme.primary
                                                        isExpense -> MaterialTheme.colorScheme.error
                                                        else -> MaterialTheme.colorScheme.secondary
                                                    },
                                                    maxLines = 1
                                                )
                                            }

                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val imagePath = entry.receiptAttachment?.imagePath
                                                if (imagePath != null) {
                                                    IconButton(
                                                        onClick = { previewReceiptPath = imagePath },
                                                        modifier = Modifier.size(48.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Receipt,
                                                            contentDescription = "Lihat Kuitansi",
                                                            tint = MaterialTheme.colorScheme.secondary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                IconButton(
                                                    onClick = { onEditTransaction(entry.id) },
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .testTag("journal_edit_btn_${entry.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Edit Catatan",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { entryToDelete = entry },
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .testTag("journal_delete_btn_${entry.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Hapus Catatan",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Pagination Controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { if (currentPage > 1) currentPage-- },
                                    enabled = currentPage > 1,
                                    shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
                                ) {
                                    Text("Sebelumnya", fontSize = 11.sp)
                                }

                                Text(
                                    "Hal $safePage dari $totalPages ($totalCount data)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedButton(
                                    onClick = { if (currentPage < totalPages) currentPage++ },
                                    enabled = currentPage < totalPages,
                                    shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
                                ) {
                                    Text("Berikutnya", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }

        // Receipt Preview Dialog
        previewReceiptPath?.let { path ->
            AlertDialog(
                onDismissRequest = { previewReceiptPath = null },
                title = { Text("Foto Kuitansi Transaksi", fontWeight = FontWeight.Bold) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Bukti Kuitansi",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(12.dp))
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { previewReceiptPath = null }) {
                        Text("Tutup")
                    }
                }
            )
        }

        if (showMonthlyEditorialPreview) {
            PdfEditorialPreviewDialog(
                target = PdfReportTarget.MONTHLY_REPORT,
                uiState = state,
                initialTab = 0,
                targetMonth = selectedMonth,
                targetYear = selectedYear,
                dateRange = monthlyPdfDateRange,
                onDateRangeChanged = {
                    monthlyPdfDateRange = it
                    generatedPdfFile = null
                },
                onDismiss = { showMonthlyEditorialPreview = false },
                onSaveAsDefault = { newConfig ->
                    viewModel.updatePdfEditorialConfig(newConfig)
                }
            )
        }

        entryToDelete?.let { target ->
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("Hapus Catatan Transaksi?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Catatan '${target.description}' akan dihapus dari buku besar dan saldo kas terkait akan otomatis dipulihkan.",
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteJournalEntry(target.id)
                            entryToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Ya, Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

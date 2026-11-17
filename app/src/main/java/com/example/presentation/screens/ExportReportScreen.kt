package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import com.example.core.debug.AppDebugLogger
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import com.example.core.qardh.QardhType
import com.example.core.qardh.QardhStatus
import com.example.core.report.DocumentExporter
import com.example.core.report.FinancialPdfGenerator
import com.example.core.report.AllInOneMasterPdfGenerator
import com.example.core.report.AnnualBaznasZakatPdfGenerator
import com.example.core.report.PdfDateRange
import com.example.core.report.PdfEditorialStyle
import com.example.core.report.PdfSectionKey
import com.example.presentation.components.PdfDateRangeCard
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.isTactical
import com.example.ui.theme.TacticalSubScreenHeader
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.HorizontalDivider
import java.io.File
import java.text.NumberFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ExportDataType(val title: String, val subtitle: String) {
    JOURNAL_ENTRIES("Riwayat Jurnal", "Baris per baris transaksi (Tanggal, Deskripsi, Tipe, Akun Debit/Kredit, Nominal)"),
    FINANCIAL_SUMMARY("Ringkasan Kategori", "Rekapitulasi total pendapatan kasab, rincian pengeluaran per kategori, arus kas bersih & status zakat")
}

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    CSV("CSV Spreadsheet (.csv)", "csv", "text/csv"),
    JSON("JSON Terstruktur (.json)", "json", "application/json")
}

enum class DateRangePreset(val label: String) {
    ALL_TIME("Semua Waktu"),
    THIS_MONTH("Bulan Ini"),
    LAST_30_DAYS("30 Hari"),
    THIS_YEAR("Tahun Berjalan")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExportReportScreen(
    viewModel: AmanahLedgerViewModel,
    onBackClick: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onNavigateToZakatReport: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }

    val isTactical = uiState.themeMode.isTactical
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp)
    val buttonShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp)
    val badgeShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
    val tacticalTitleFont = if (isTactical) ChakraPetchFontFamily else null
    val tacticalMonoFont = if (isTactical) ShareTechMonoFontFamily else null

    var isGeneratingAllInOnePdf by remember { mutableStateOf(false) }
    var isGeneratingLedgerPdf by remember { mutableStateOf(false) }
    var isGeneratingZakatPdf by remember { mutableStateOf(false) }
    var isGeneratingBaznasZakatPdf by remember { mutableStateOf(false) }
    var isGeneratingMonthlyPdf by remember { mutableStateOf(false) }
    var isGeneratingComprehensivePdf by remember { mutableStateOf(false) }
    var isGeneratingComprehensiveCsv by remember { mutableStateOf(false) }

    var previewFile by remember { mutableStateOf<File?>(null) }
    var previewTitle by remember { mutableStateOf("") }
    var previewMimeType by remember { mutableStateOf("application/pdf") }
    var showPreviewSheet by remember { mutableStateOf(false) }

    var editorialPreviewTarget by remember { mutableStateOf<PdfReportTarget?>(null) }
    var editorialPreviewInitialTab by remember { mutableIntStateOf(0) }
    var selectedPdfDateRange by remember { mutableStateOf(PdfDateRange.allTime()) }

    Scaffold(
        topBar = {
            if (isTactical) {
                TacticalSubScreenHeader(
                    title = "INTEL DOSSIER & REPORT HUB",
                    subCode = "SYS//REP-07",
                    onBack = onBackClick,
                    trailingActions = {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("export_menu_sidebar_button")
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
                    title = {
                        Column {
                            Text("Ekspor Laporan Keuangan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(
                                "Dokumen Formal PDF & Spreadsheet CSV Syariah",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("export_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("export_menu_sidebar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Buka Menu Sidebar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // 1. Header Info Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = cardShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                if (isTactical) "// SYARIAH AUDIT DOSSIER & ENCRYPTED EXPORT" else "Laporan Berkop Standar Syariah",
                                fontWeight = FontWeight.Bold,
                                fontFamily = tacticalTitleFont,
                                fontSize = 14.sp
                            )
                            Text(
                                if (isTactical) "Generate official A4 documents with double-entry audit trail, multi-wallet balance matrix, and ZISWAF conformity." else "Hasilkan dokumen PDF resmi dengan audit trail double-entry dan rekapitulasi zakat/infaq sesuai kaidah fiqh.",
                                fontSize = 11.sp,
                                fontFamily = tacticalMonoFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 1A. Kostumisasi Redaksi, Gaya Bahasa & Penamaan Konten PDF
            item {
                val cfg = uiState.pdfEditorialConfig
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.EditNote,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Kostumisasi Redaksi & Gaya PDF",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = tacticalTitleFont,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Aktif: ${cfg.stylePreset.title} • ${cfg.activeSectionCount()}/${PdfSectionKey.entries.size} bagian",
                                        fontSize = 11.sp,
                                        fontFamily = tacticalMonoFont,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Pilih gaya bahasa sederhana/formal untuk laporan kas standar tanpa terbawa gaya tema UI, ubah judul kop, nama berkas, istilah kasab, hingga nama tiap sub-bab beserta pratinjau halaman A4 sebelum mengekspor.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PdfEditorialStyle.entries.forEach { preset ->
                                FilterChip(
                                    selected = cfg.stylePreset == preset,
                                    onClick = {
                                        viewModel.applyPdfEditorialPreset(preset)
                                        Toast.makeText(context, "Gaya redaksi PDF: ${preset.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    label = { Text(preset.badge, fontSize = 11.sp, fontFamily = tacticalMonoFont) },
                                    leadingIcon = if (cfg.stylePreset == preset) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                    modifier = Modifier.testTag("export_editorial_preset_${preset.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        FilledTonalButton(
                            onClick = {
                                editorialPreviewInitialTab = 1
                                editorialPreviewTarget = PdfReportTarget.ALL_IN_ONE
                                AppDebugLogger.logUserAction("Ekspor PDF", "Membuka editor kostumisasi redaksi & konten PDF")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("open_pdf_editorial_customizer_btn"),
                            shape = buttonShape
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ubah Redaksi, Judul Konten & Pratinjau", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 1A-2. Pengaturan Rentang Waktu PDF (Masehi & Hijriyah: Dari Kapan Ke Kapan)
            item {
                PdfDateRangeCard(
                    uiState = uiState,
                    dateRange = selectedPdfDateRange,
                    onDateRangeChange = { newRange ->
                        selectedPdfDateRange = newRange
                        AppDebugLogger.logUserAction("Ekspor PDF", "Mengubah periode PDF: ${newRange.formatDualCalendarSummary()}")
                    }
                )
            }

            // 1B. MASTER ALL-IN-ONE PDF EXPORT CARD (Laporan Utama Lengkap)
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        if (isTactical) "MASTER DOSSIER // ALL-IN-ONE PDF" else "Ekspor PDF Laporan Utama Lengkap (All-in-One)",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = tacticalTitleFont,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        if (isTactical) "Full Balance, Kasab/Non-Kasab, SLI Audit, 4 Zakat & Journal Lines" else "Gabungan Seluruh Data Keuangan dalam 1 Dokumen PDF",
                                        fontSize = 11.sp,
                                        fontFamily = tacticalMonoFont,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = badgeShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    "ALL-IN-ONE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = tacticalMonoFont,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Mencakup seluruh modul dalam satu berkas A4: Neraca Dompet & Qardh, rincian Kasab/Non-Kasab & Belanja Kategori, Analisa SLI Komprehensif, 4 Jenis Zakat (Maal, Profesi, Perniagaan, Fitrah), Sedekah Subuh, Rezeki Non-Tunai, 8 Asnaf, Aturan Otomatis, Skor Audit Syariah, serta Buku Besar lengkap dengan pasangan akun Debit/Kredit.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editorialPreviewInitialTab = 0
                                    editorialPreviewTarget = PdfReportTarget.ALL_IN_ONE
                                    AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Laporan Utama All-in-One")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("preview_all_in_one_pdf_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau & Ubah", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isGeneratingAllInOnePdf = true
                                    try {
                                        val cal = Calendar.getInstance()
                                        val file = AllInOneMasterPdfGenerator.generateAllInOneMasterPdf(
                                            context = context,
                                            state = uiState,
                                            targetMonth = cal.get(Calendar.MONTH),
                                            targetYear = cal.get(Calendar.YEAR),
                                            dateRange = selectedPdfDateRange
                                        )
                                        FinancialPdfGenerator.sharePdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membagikan PDF Laporan Utama All-in-One")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal ekspor PDF All-in-One: ${e.message}", e)
                                        Toast.makeText(context, "Gagal ekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingAllInOnePdf = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("export_all_in_one_pdf_btn"),
                                shape = buttonShape,
                                enabled = !isGeneratingAllInOnePdf
                            ) {
                                if (isGeneratingAllInOnePdf) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isTactical) "EKSPOR SEMUA" else "Bagikan PDF", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Comprehensive Financial Condition Export Card (Saldo Dompet, Hutang Piutang & Zakat)
            item {
                val totalWalletBalance = uiState.totalWalletBalance
                val receivables = uiState.qardhRecords.filter { it.type == QardhType.PIUTANG_SAYA }
                val payables = uiState.qardhRecords.filter { it.type == QardhType.HUTANG_SAYA }
                val totalPiutang = receivables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }.sumOf { it.remainingAmount }
                val totalHutang = payables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }.sumOf { it.remainingAmount }
                val netWorth = totalWalletBalance + totalPiutang - totalHutang

                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        if (isTactical) "FINANCIAL DOSSIER & ASSET MATRIX" else "Laporan Finansial Komprehensif",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = tacticalTitleFont,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        if (isTactical) "Double-Entry Balance, Multi-Vault & Liabilities" else "Neraca Lengkap, Saldo Dompet & Hutang Piutang",
                                        fontSize = 11.sp,
                                        fontFamily = tacticalMonoFont,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            androidx.compose.material3.Surface(
                                shape = badgeShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    if (isTactical) "CORE//01" else "Utama",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = tacticalMonoFont,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Dokumen neraca lengkap yang menyajikan seluruh kondisi finansial pengguna: rincian saldo semua kantong kas/bank, daftar kewajiban hutang & hak piutang Qardhul Hasan, total kekayaan bersih (Net Worth), serta status kewajiban zakat.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview Ringkasan Angka
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val isMasked = uiState.securityConfig.isMaskBalance
                            Column {
                                Text("Saldo Kas", fontSize = 10.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (isMasked) "Rp ••••••" else "Rp ${rupiahFormat.format(totalWalletBalance).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Piutang Aktif", fontSize = 10.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (isMasked) "Rp ••••••" else "Rp ${rupiahFormat.format(totalPiutang).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.secondary)
                            }
                            Column {
                                Text("Hutang", fontSize = 10.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (isMasked) "Rp ••••••" else "Rp ${rupiahFormat.format(totalHutang).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.error)
                            }
                            Column {
                                Text("Kekayaan Bersih", fontSize = 10.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (isMasked) "Rp ••••••" else "Rp ${rupiahFormat.format(netWorth).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tombol Aksi PDF & CSV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editorialPreviewInitialTab = 0
                                    editorialPreviewTarget = PdfReportTarget.COMPREHENSIVE_FINANCIAL
                                    AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Laporan Finansial Komprehensif")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("preview_comprehensive_pdf_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau & Ubah", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isGeneratingComprehensivePdf = true
                                    try {
                                        val pdfFile = FinancialPdfGenerator.generateComprehensiveFinancialPdf(
                                            context = context,
                                            state = uiState,
                                            dateRange = selectedPdfDateRange
                                        )
                                        previewFile = pdfFile
                                        previewTitle = if (isTactical) "FINANCIAL DOSSIER // NET BALANCE" else "Laporan Finansial Komprehensif"
                                        previewMimeType = "application/pdf"
                                        showPreviewSheet = true
                                        Toast.makeText(context, "Laporan Finansial Komprehensif siap!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal ekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingComprehensivePdf = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("export_comprehensive_pdf_btn"),
                                shape = buttonShape,
                                enabled = !isGeneratingComprehensivePdf
                            ) {
                                if (isGeneratingComprehensivePdf) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isTactical) "DOSSIER PDF" else "Unduh PDF", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        androidx.compose.material3.FilledTonalButton(
                            onClick = {
                                isGeneratingComprehensiveCsv = true
                                try {
                                    val csvFile = DocumentExporter.exportComprehensiveFinancialCsvFile(context, uiState)
                                    previewFile = csvFile
                                    previewTitle = if (isTactical) "DATA MATRIX // COMPREHENSIVE CSV" else "Spreadsheet Finansial Komprehensif"
                                    previewMimeType = "text/csv"
                                    showPreviewSheet = true
                                    Toast.makeText(context, "Spreadsheet CSV Komprehensif siap!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal ekspor CSV: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isGeneratingComprehensiveCsv = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("export_comprehensive_csv_btn"),
                            shape = buttonShape,
                            enabled = !isGeneratingComprehensiveCsv
                        ) {
                            if (isGeneratingComprehensiveCsv) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isTactical) "EXCEL/CSV" else "Ekspor Spreadsheet CSV", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 3. Comprehensive Ledger PDF Export Card
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isTactical) "BUKU BESAR // AUDIT TRAIL" else "Buku Besar Jurnal Syariah (Ledger PDF)",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = tacticalTitleFont,
                                    fontSize = 15.sp
                                )
                                Text(
                                    if (isTactical) "Double-Entry Trail, Cash Balances & Zero-Balancing" else "Audit Trail Debit-Kredit, Rekening Kas & Saldo",
                                    fontSize = 11.sp,
                                    fontFamily = tacticalMonoFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Mencakup seluruh daftar mutasi buku besar, saldo multi-kantong kas, dan rekonsiliasi jurnal double-entry seimbang.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editorialPreviewInitialTab = 0
                                    editorialPreviewTarget = PdfReportTarget.COMPREHENSIVE_LEDGER
                                    AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Buku Kas Lengkap")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("preview_ledger_pdf_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau & Ubah", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isGeneratingLedgerPdf = true
                                    try {
                                        val file = FinancialPdfGenerator.generateComprehensiveLedgerPdf(
                                            context = context,
                                            state = uiState,
                                            dateRange = selectedPdfDateRange
                                        )
                                        FinancialPdfGenerator.sharePdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membagikan berkas PDF Buku Kas Lengkap")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal mengekspor PDF Buku Kas: ${e.message}", e)
                                        Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingLedgerPdf = false
                                    }
                                },
                                modifier = Modifier.weight(1.2f).testTag("export_ledger_pdf_btn"),
                                shape = buttonShape
                            ) {
                                if (isGeneratingLedgerPdf) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bagikan PDF")
                                }
                            }
                        }
                    }
                }
            }

            // 3. Zakat & Infaq Report PDF Export Card
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isTactical) "ZISWAF & HAUL RADAR DOSSIER" else "Rekapitulasi Zakat, Infaq & Haul/Nisab",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = tacticalTitleFont,
                                    fontSize = 15.sp
                                )
                                Text(
                                    if (isTactical) "Zakat Maal Threshold, Lunar Haul & Mustahiq Log" else "Kalkulasi ZISWAF, Haul Emas & Distribusi Mustahiq",
                                    fontSize = 11.sp,
                                    fontFamily = tacticalMonoFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Laporan komprehensif penghitungan Zakat Maal (Nisab 85g Emas), status kepemilikan 1 tahun Hijriah (Haul), dan penyaluran Infaq.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editorialPreviewInitialTab = 0
                                    editorialPreviewTarget = PdfReportTarget.ZAKAT_INFAQ
                                    AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Laporan Zakat & Infaq")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("preview_zakat_pdf_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau & Ubah", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isGeneratingZakatPdf = true
                                    try {
                                        val file = FinancialPdfGenerator.generateZakatAndInfaqReportPdf(
                                            context = context,
                                            state = uiState,
                                            dateRange = selectedPdfDateRange
                                        )
                                        FinancialPdfGenerator.sharePdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membagikan berkas PDF Laporan Zakat & Infaq")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal mengekspor PDF Zakat & Infaq: ${e.message}", e)
                                        Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingZakatPdf = false
                                    }
                                },
                                modifier = Modifier.weight(1.2f).testTag("export_zakat_pdf_btn"),
                                shape = buttonShape
                            ) {
                                if (isGeneratingZakatPdf) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bagikan PDF")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onNavigateToZakatReport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("export_open_zakat_report_btn"),
                            shape = buttonShape
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTactical) "OPEN ZAKAT REPORT MODULE" else "Buka Modul Laporan Zakat (Maal & Fitrah)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3b. Official BAZNAS Zakat Maal & Multi-Wallet PDF Export Card
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (isTactical) "BAZNAS ZAKAT PROTOCOL" else "Lembar Penetapan Zakat BAZNAS",
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = tacticalTitleFont,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        shape = badgeShape
                                    ) {
                                        Text(
                                            if (isTactical) "STANDARD" else "Format Resmi",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = tacticalMonoFont,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    if (isTactical) "Annual Hisab, Multi-Wallet Quotas & Amil Signature" else "Hisab Tahunan, Multi-Kantong & Lembar Amil",
                                    fontSize = 11.sp,
                                    fontFamily = tacticalMonoFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Dokumen resmi hisab tahunan standar BAZNAS lengkap dengan pemisahan beban zakat per kantong kas, status haul, dan kolom tanda tangan muzakki & amil.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editorialPreviewInitialTab = 0
                                    editorialPreviewTarget = PdfReportTarget.BAZNAS_ZAKAT
                                    AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Lembar Penetapan Zakat BAZNAS")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("preview_baznas_pdf_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau & Ubah", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isGeneratingBaznasZakatPdf = true
                                    try {
                                        val file = AnnualBaznasZakatPdfGenerator.generateAnnualBaznasZakatReportPdf(
                                            context = context,
                                            state = uiState,
                                            muzakkiName = uiState.userNameKasMukmin,
                                            dateRange = selectedPdfDateRange
                                        )
                                        FinancialPdfGenerator.sharePdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membagikan PDF Lembar Penetapan Zakat BAZNAS")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal mengekspor PDF BAZNAS: ${e.message}", e)
                                        Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingBaznasZakatPdf = false
                                    }
                                },
                                modifier = Modifier.weight(1.2f).testTag("export_baznas_pdf_btn"),
                                shape = buttonShape
                            ) {
                                if (isGeneratingBaznasZakatPdf) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bagikan PDF")
                                }
                            }
                        }
                    }
                }
            }

            // 4. Monthly Financial Report PDF Card
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isTactical) "MONTHLY CYCLE & SPIRITUAL LIQUIDITY" else "Laporan Keuangan Bulanan & Anggaran",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = tacticalTitleFont,
                                    fontSize = 15.sp
                                )
                                Text(
                                    if (isTactical) "Budget Limits, Cash Flows & Infaq Vault Reserves" else "Realisasi Pos Anggaran & Spiritual Liquidity Index",
                                    fontSize = 11.sp,
                                    fontFamily = tacticalMonoFont,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Format A4 lengkap berisi tabel evaluasi batas anggaran konsumsi, arus kas masuk/keluar, dan saldo titipan Infaq Vault.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    editorialPreviewInitialTab = 0
                                    editorialPreviewTarget = PdfReportTarget.MONTHLY_REPORT
                                    AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Laporan Bulanan")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("preview_monthly_pdf_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pratinjau & Ubah", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    isGeneratingMonthlyPdf = true
                                    try {
                                        val cal = Calendar.getInstance()
                                        val file = FinancialPdfGenerator.generateMonthlyReportPdf(
                                            context = context,
                                            state = uiState,
                                            targetMonth = cal.get(Calendar.MONTH),
                                            targetYear = cal.get(Calendar.YEAR),
                                            dateRange = selectedPdfDateRange
                                        )
                                        FinancialPdfGenerator.sharePdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membagikan berkas PDF Laporan Bulanan")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal mengekspor PDF Laporan Bulanan: ${e.message}", e)
                                        Toast.makeText(context, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingMonthlyPdf = false
                                    }
                                },
                                modifier = Modifier.weight(1.2f).testTag("export_monthly_pdf_btn"),
                                shape = buttonShape
                            ) {
                                if (isGeneratingMonthlyPdf) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bagikan PDF")
                                }
                            }
                        }
                    }
                }
            }

            // 5. Ekspor Fleksibel Riwayat & Ringkasan Data (CSV & JSON dengan Rentang Tanggal)
            item {
                var selectedExportType by remember { mutableStateOf(ExportDataType.JOURNAL_ENTRIES) }
                var selectedExportFormat by remember { mutableStateOf(ExportFormat.CSV) }
                var selectedDatePreset by remember { mutableStateOf(DateRangePreset.ALL_TIME) }

                val (startMillis, endMillis) = remember(selectedDatePreset) {
                    val cal = Calendar.getInstance()
                    when (selectedDatePreset) {
                        DateRangePreset.ALL_TIME -> Pair(null, null)
                        DateRangePreset.THIS_MONTH -> {
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            Pair(cal.timeInMillis, System.currentTimeMillis())
                        }
                        DateRangePreset.LAST_30_DAYS -> {
                            val start = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                            Pair(start, System.currentTimeMillis())
                        }
                        DateRangePreset.THIS_YEAR -> {
                            cal.set(Calendar.DAY_OF_YEAR, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            Pair(cal.timeInMillis, System.currentTimeMillis())
                        }
                    }
                }

                val filteredEntries = remember(uiState.journalEntries, startMillis, endMillis) {
                    DocumentExporter.filterJournalEntries(uiState.journalEntries, startMillis, endMillis)
                }

                fun generateSelectedContent(): String {
                    val startDate = startMillis?.let { Date(it) }
                    val endDate = endMillis?.let { Date(it) }
                    return when (selectedExportType) {
                        ExportDataType.JOURNAL_ENTRIES -> {
                            if (selectedExportFormat == ExportFormat.CSV) {
                                DocumentExporter.generateTransactionHistoryCsv(filteredEntries, uiState.accounts, startDate, endDate)
                            } else {
                                DocumentExporter.generateTransactionHistoryJson(filteredEntries, uiState.accounts, startDate, endDate)
                            }
                        }
                        ExportDataType.FINANCIAL_SUMMARY -> {
                            if (selectedExportFormat == ExportFormat.CSV) {
                                DocumentExporter.generateFinancialSummaryCsv(uiState, startDate, endDate)
                            } else {
                                DocumentExporter.generateFinancialSummaryJson(uiState, startDate, endDate)
                            }
                        }
                    }
                }

                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isTactical) "CUSTOM SPREADSHEET & JSON EXTRACTOR" else "Ekspor Fleksibel Riwayat & Ringkasan",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = tacticalTitleFont,
                                    fontSize = 15.sp
                                )
                                Text(
                                    if (isTactical) "Custom Range, UTF-8 BOM CSV & Structured JSON" else "Pilih Jenis Data, Format CSV/JSON & Rentang Tanggal",
                                    fontSize = 11.sp,
                                    fontFamily = tacticalMonoFont,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // A. Pilihan Jenis Data (Riwayat Jurnal vs Ringkasan Kategori)
                        Text(
                            "1. Pilih Jenis Data:",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = tacticalMonoFont,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedExportType == ExportDataType.JOURNAL_ENTRIES,
                                onClick = { selectedExportType = ExportDataType.JOURNAL_ENTRIES },
                                label = { Text("Riwayat Jurnal", fontSize = 12.sp, fontFamily = tacticalMonoFont) },
                                leadingIcon = if (selectedExportType == ExportDataType.JOURNAL_ENTRIES) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1f).testTag("export_type_journal_chip")
                            )
                            FilterChip(
                                selected = selectedExportType == ExportDataType.FINANCIAL_SUMMARY,
                                onClick = { selectedExportType = ExportDataType.FINANCIAL_SUMMARY },
                                label = { Text("Ringkasan Kategori", fontSize = 12.sp, fontFamily = tacticalMonoFont) },
                                leadingIcon = if (selectedExportType == ExportDataType.FINANCIAL_SUMMARY) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1f).testTag("export_type_summary_chip")
                            )
                        }
                        Text(
                            text = selectedExportType.subtitle,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                        )

                        // B. Pilihan Format Data (CSV vs JSON)
                        Text(
                            "2. Pilih Format Berkas:",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = tacticalMonoFont,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedExportFormat == ExportFormat.CSV,
                                onClick = { selectedExportFormat = ExportFormat.CSV },
                                label = { Text("CSV Spreadsheet", fontSize = 12.sp, fontFamily = tacticalMonoFont) },
                                leadingIcon = if (selectedExportFormat == ExportFormat.CSV) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1f).testTag("export_format_csv_chip")
                            )
                            FilterChip(
                                selected = selectedExportFormat == ExportFormat.JSON,
                                onClick = { selectedExportFormat = ExportFormat.JSON },
                                label = { Text("JSON Data", fontSize = 12.sp, fontFamily = tacticalMonoFont) },
                                leadingIcon = if (selectedExportFormat == ExportFormat.JSON) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1f).testTag("export_format_json_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // C. Pilihan Rentang Tanggal (Date Range Selection)
                        Text(
                            "3. Rentang Tanggal:",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = tacticalMonoFont,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedDatePreset == DateRangePreset.ALL_TIME,
                                onClick = { selectedDatePreset = DateRangePreset.ALL_TIME },
                                label = { Text("Semua", fontSize = 11.sp, fontFamily = tacticalMonoFont) },
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1f).testTag("export_date_range_all_chip")
                            )
                            FilterChip(
                                selected = selectedDatePreset == DateRangePreset.THIS_MONTH,
                                onClick = { selectedDatePreset = DateRangePreset.THIS_MONTH },
                                label = { Text("Bulan Ini", fontSize = 11.sp, fontFamily = tacticalMonoFont) },
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1.1f).testTag("export_date_range_month_chip")
                            )
                            FilterChip(
                                selected = selectedDatePreset == DateRangePreset.LAST_30_DAYS,
                                onClick = { selectedDatePreset = DateRangePreset.LAST_30_DAYS },
                                label = { Text("30 Hari", fontSize = 11.sp, fontFamily = tacticalMonoFont) },
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1f).testTag("export_date_range_30d_chip")
                            )
                            FilterChip(
                                selected = selectedDatePreset == DateRangePreset.THIS_YEAR,
                                onClick = { selectedDatePreset = DateRangePreset.THIS_YEAR },
                                label = { Text("Tahun Ini", fontSize = 11.sp, fontFamily = tacticalMonoFont) },
                                shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                modifier = Modifier.weight(1.1f).testTag("export_date_range_year_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // D. Info Box Ringkasan Filter
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Menyaring ${filteredEntries.size} entri transaksi (${selectedDatePreset.label}) • Format: ${selectedExportFormat.extension.uppercase()}",
                                fontSize = 11.sp,
                                fontFamily = tacticalMonoFont,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // E. Tombol Aksi Salin Data & Bagikan Berkas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val content = generateSelectedContent()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Amanah Ledger Export", content)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Data ${selectedExportType.title} (${selectedExportFormat.label}) disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("copy_csv_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin Data")
                            }

                            Button(
                                onClick = {
                                    val content = generateSelectedContent()
                                    val prefix = if (selectedExportType == ExportDataType.JOURNAL_ENTRIES) "Jurnal_AmanahLedger" else "Ringkasan_AmanahLedger"
                                    val file = DocumentExporter.createExportFile(context, content, prefix, selectedExportFormat.extension)
                                    previewFile = file
                                    previewTitle = if (isTactical) "EKSPOR // ${selectedExportType.title.uppercase()}" else "Ekspor ${selectedExportType.title}"
                                    previewMimeType = selectedExportFormat.mimeType
                                    showPreviewSheet = true
                                },
                                modifier = Modifier.weight(1f).testTag("share_csv_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pratinjau / Bagikan")
                            }
                        }
                    }
                }
            }

            // 6. Summary Metrics Card
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (isTactical) "// AUDIT METRICS & REPOSITORY SUMMARY" else "Status Data yang Akan Diekspor:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = tacticalTitleFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Total Transaksi Jurnal: ${uiState.journalEntries.size} entri (dengan detail akun Debit/Kredit)", fontSize = 11.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Total Kantong/Rekening Kas: ${uiState.wallets.size} kantong (${uiState.walletMutations.size} mutasi)", fontSize = 11.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Catatan Hutang Piutang (Qardh): ${uiState.qardhRecords.size} akad", fontSize = 11.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Ibadah & Kedermawanan: ${uiState.ibadahGoals.size} target, Streak Subuh ${uiState.sedekahSubuhState.currentStreak} hari, ${uiState.rezekiNonTunaiList.size} rezeki non-tunai", fontSize = 11.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Otomatisasi: ${uiState.rules.size} aturan infaq & ${uiState.recurringTransactions.size} jadwal berulang", fontSize = 11.sp, fontFamily = tacticalMonoFont, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // In-App Quick Document Preview Bottom Sheet
    if (showPreviewSheet && previewFile != null) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showPreviewSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = if (isTactical) CutCornerShape(topStart = 16.dp, topEnd = 16.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            val file = previewFile!!
            val fileSizeKb = file.length() / 1024
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(if (isTactical) CutCornerShape(8.dp) else CircleShape)
                            .background(
                                if (previewMimeType == "text/csv") MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (previewMimeType == "text/csv") Icons.Default.TableChart else Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = if (previewMimeType == "text/csv") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = previewTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = tacticalTitleFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = file.name,
                            fontSize = 12.sp,
                            fontFamily = tacticalMonoFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Detail Dokumen Card
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Status Validasi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (isTactical) "VERIFIED // ZERO-DEFICIT OK" else "Sah & Terverifikasi Fiqh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = tacticalMonoFont,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ukuran Berkas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "$fileSizeKb KB (${file.length()} bytes)",
                                fontSize = 12.sp,
                                fontFamily = tacticalMonoFont,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Format Dokumen", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (previewMimeType == "text/csv") "CSV Spreadsheet (UTF-8 BOM)" else "Adobe PDF Portable Document (A4)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (previewMimeType == "application/pdf") {
                                FinancialPdfGenerator.viewPdfReport(context, file)
                            } else {
                                DocumentExporter.shareFile(context, file, previewMimeType, "Buka $previewTitle")
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("preview_open_external_btn"),
                        shape = buttonShape
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Dokumen", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (previewMimeType == "application/pdf") {
                                FinancialPdfGenerator.sharePdfReport(context, file)
                            } else {
                                DocumentExporter.shareFile(context, file, previewMimeType, "Bagikan $previewTitle")
                            }
                        },
                        modifier = Modifier.weight(1.2f).testTag("preview_share_direct_btn"),
                        shape = buttonShape
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan Berkas", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = { showPreviewSheet = false },
                    modifier = Modifier.fillMaxWidth().testTag("preview_close_sheet_btn"),
                    shape = buttonShape
                ) {
                    Text("Tutup Pratinjau")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    editorialPreviewTarget?.let { target ->
        PdfEditorialPreviewDialog(
            target = target,
            uiState = uiState,
            initialTab = editorialPreviewInitialTab,
            dateRange = selectedPdfDateRange,
            onDateRangeChanged = { selectedPdfDateRange = it },
            onDismiss = { editorialPreviewTarget = null },
            onSaveAsDefault = { newConfig ->
                viewModel.updatePdfEditorialConfig(newConfig)
            }
        )
    }
}


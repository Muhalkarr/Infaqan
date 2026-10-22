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
import com.example.core.report.AnnualBaznasZakatPdfGenerator
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
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.EmeraldDark
import java.io.File
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportScreen(
    viewModel: AmanahLedgerViewModel,
    onBackClick: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }

    var isGeneratingLedgerPdf by remember { mutableStateOf(false) }
    var isGeneratingZakatPdf by remember { mutableStateOf(false) }
    var isGeneratingBaznasZakatPdf by remember { mutableStateOf(false) }
    var isGeneratingMonthlyPdf by remember { mutableStateOf(false) }
    var isGeneratingComprehensivePdf by remember { mutableStateOf(false) }
    var isGeneratingComprehensiveCsv by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
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
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
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
                                "Laporan Berkop Standar Syariah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Hasilkan dokumen PDF resmi dengan audit trail double-entry dan rekapitulasi zakat/infaq sesuai kaidah fiqh.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                    shape = RoundedCornerShape(16.dp),
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
                                        .clip(CircleShape)
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
                                        "Laporan Finansial Komprehensif",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Neraca Lengkap, Saldo Dompet & Hutang Piutang",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    "Utama",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
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
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Saldo Kas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${rupiahFormat.format(totalWalletBalance).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Piutang Aktif", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${rupiahFormat.format(totalPiutang).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Column {
                                Text("Hutang", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${rupiahFormat.format(totalHutang).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Column {
                                Text("Kekayaan Bersih", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${rupiahFormat.format(netWorth).replace("Rp", "").trim()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tombol Aksi PDF & CSV
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isGeneratingComprehensivePdf = true
                                    try {
                                        val pdfFile = FinancialPdfGenerator.generateComprehensiveFinancialPdf(context, uiState)
                                        Toast.makeText(context, "Laporan Finansial Komprehensif siap!", Toast.LENGTH_SHORT).show()
                                        FinancialPdfGenerator.sharePdfReport(context, pdfFile)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal ekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingComprehensivePdf = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_comprehensive_pdf_btn"),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isGeneratingComprehensivePdf
                            ) {
                                if (isGeneratingComprehensivePdf) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Unduh PDF", fontSize = 12.sp)
                                }
                            }

                            androidx.compose.material3.FilledTonalButton(
                                onClick = {
                                    isGeneratingComprehensiveCsv = true
                                    try {
                                        val csvFile = DocumentExporter.exportComprehensiveFinancialCsvFile(context, uiState)
                                        Toast.makeText(context, "Spreadsheet CSV Komprehensif siap!", Toast.LENGTH_SHORT).show()
                                        DocumentExporter.shareFile(context, csvFile, "text/csv", "Laporan Finansial Komprehensif")
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal ekspor CSV: ${e.message}", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isGeneratingComprehensiveCsv = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_comprehensive_csv_btn"),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isGeneratingComprehensiveCsv
                            ) {
                                if (isGeneratingComprehensiveCsv) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ekspor CSV", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Comprehensive Ledger PDF Export Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
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
                                    "Buku Besar Jurnal Syariah (Ledger PDF)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Audit Trail Debit-Kredit, Rekening Kas & Saldo",
                                    fontSize = 11.sp,
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
                                    try {
                                        val file = FinancialPdfGenerator.generateComprehensiveLedgerPdf(context, uiState)
                                        FinancialPdfGenerator.viewPdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Buku Kas Lengkap")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal membuka PDF Buku Kas: ${e.message}", e)
                                        Toast.makeText(context, "Gagal membuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("preview_ledger_pdf_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka PDF")
                            }

                            Button(
                                onClick = {
                                    isGeneratingLedgerPdf = true
                                    try {
                                        val file = FinancialPdfGenerator.generateComprehensiveLedgerPdf(context, uiState)
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
                                shape = RoundedCornerShape(10.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
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
                                    "Rekapitulasi Zakat, Infaq & Haul/Nisab",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Kalkulasi ZISWAF, Haul Emas & Distribusi Mustahiq",
                                    fontSize = 11.sp,
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
                                    try {
                                        val file = FinancialPdfGenerator.generateZakatAndInfaqReportPdf(context, uiState)
                                        FinancialPdfGenerator.viewPdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Laporan Zakat & Infaq")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal membuka PDF Zakat & Infaq: ${e.message}", e)
                                        Toast.makeText(context, "Gagal membuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("preview_zakat_pdf_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka PDF")
                            }

                            Button(
                                onClick = {
                                    isGeneratingZakatPdf = true
                                    try {
                                        val file = FinancialPdfGenerator.generateZakatAndInfaqReportPdf(context, uiState)
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
                                shape = RoundedCornerShape(10.dp)
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
                    }
                }
            }

            // 3b. Official BAZNAS Zakat Maal & Multi-Wallet PDF Export Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
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
                                        "Lembar Penetapan Zakat BAZNAS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "Format Resmi",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    "Hisab Tahunan, Multi-Kantong & Lembar Amil",
                                    fontSize = 11.sp,
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
                                    try {
                                        val file = AnnualBaznasZakatPdfGenerator.generateAnnualBaznasZakatReportPdf(
                                            context = context,
                                            state = uiState,
                                            muzakkiName = uiState.userNameKasMukmin
                                        )
                                        FinancialPdfGenerator.viewPdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Lembar Penetapan Zakat BAZNAS")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal membuka PDF Zakat BAZNAS: ${e.message}", e)
                                        Toast.makeText(context, "Gagal membuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("preview_baznas_pdf_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka PDF")
                            }

                            Button(
                                onClick = {
                                    isGeneratingBaznasZakatPdf = true
                                    try {
                                        val file = AnnualBaznasZakatPdfGenerator.generateAnnualBaznasZakatReportPdf(
                                            context = context,
                                            state = uiState,
                                            muzakkiName = uiState.userNameKasMukmin
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
                                shape = RoundedCornerShape(10.dp)
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
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
                                    "Laporan Keuangan Bulanan & Anggaran",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Realisasi Pos Anggaran & Spiritual Liquidity Index",
                                    fontSize = 11.sp,
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
                                    try {
                                        val cal = Calendar.getInstance()
                                        val file = FinancialPdfGenerator.generateMonthlyReportPdf(
                                            context,
                                            uiState,
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.YEAR)
                                        )
                                        FinancialPdfGenerator.viewPdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membuka pratinjau Laporan Bulanan")
                                    } catch (e: Exception) {
                                        AppDebugLogger.logHandledError("Ekspor PDF", "Gagal membuka PDF Laporan Bulanan: ${e.message}", e)
                                        Toast.makeText(context, "Gagal membuka PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("preview_monthly_pdf_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka PDF")
                            }

                            Button(
                                onClick = {
                                    isGeneratingMonthlyPdf = true
                                    try {
                                        val cal = Calendar.getInstance()
                                        val file = FinancialPdfGenerator.generateMonthlyReportPdf(
                                            context,
                                            uiState,
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.YEAR)
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
                                shape = RoundedCornerShape(10.dp)
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

            // 5. CSV / Spreadsheet Export Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
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
                                    "Spreadsheet CSV (Excel & Google Sheets)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    "Seluruh Data Jurnal, Multi-Kantong & Qardh",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Ekspor tabel data lengkap yang dapat diolah langsung di Microsoft Excel, Google Sheets, atau software akuntansi.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val csvContent = viewModel.generateCsvReport()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("CSV Report", csvContent)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Teks CSV berhasil disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).testTag("copy_csv_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salin CSV")
                            }

                            Button(
                                onClick = {
                                    val csvContent = viewModel.generateCsvReport()
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, csvContent)
                                        type = "text/plain"
                                    }
                                    context.startActivity(android.content.Intent.createChooser(sendIntent, "Bagikan Data CSV"))
                                },
                                modifier = Modifier.weight(1f).testTag("share_csv_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bagikan CSV")
                            }
                        }
                    }
                }
            }

            // 6. Summary Metrics Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Status Data yang Akan Diekspor:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Total Transaksi Jurnal: ${uiState.journalEntries.size} entri", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Total Kantong/Rekening Kas: ${uiState.wallets.size} kantong", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Catatan Hutang Piutang (Qardh): ${uiState.qardhRecords.size} akad", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("• Program Target Ibadah: ${uiState.ibadahGoals.size} target", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

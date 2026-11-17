package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.report.FinancialPdfGenerator
import com.example.core.state.AmanahLedgerUiState
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.TacticalSubScreenHeader
import com.example.ui.theme.isTactical
import com.example.ui.theme.rememberAppCardShape
import com.example.ui.theme.rememberAppShape
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatReportScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    val isTactical = state.themeMode.isTactical
    val cardShape = rememberAppCardShape(14)

    var showDisburseModal by remember { mutableStateOf(false) }
    var disburseTitlePreset by remember { mutableStateOf("Zakat Maal (Simpanan)") }
    var disburseAmountPreset by remember { mutableStateOf(0.0) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) {
        viewModel.syncZakatMaalFromDatabase()
    }

    val zakatDisbursements = remember(state.infaqDistributions) {
        state.infaqDistributions.filter {
            it.programName.contains("zakat", ignoreCase = true) ||
                it.notes.contains("zakat", ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isTactical) {
                TacticalSubScreenHeader(
                    title = "Laporan Zakat Komprehensif",
                    subCode = "ZIS//REPORT-ZKT",
                    onBack = onNavigateBack,
                    trailingActions = {
                        IconButton(
                            onClick = { viewModel.syncZakatMaalFromDatabase() },
                            modifier = Modifier.testTag("zakat_report_header_sync_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sinkronisasi Database", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("zakat_report_menu_sidebar_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka Menu Sidebar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            } else {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("zakat_report_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "Laporan Zakat Maal & Fitrah",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Audit Nisab, Haul, Tanggungan & Bahan Pokok Lokal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.syncZakatMaalFromDatabase() },
                            modifier = Modifier.testTag("zakat_report_header_sync_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sinkronisasi Database", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("zakat_report_menu_sidebar_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka Menu Sidebar", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Ringkasan Eksekutif Kewajiban Zakat
            item {
                ZakatReportExecutiveSummaryCard(
                    state = state,
                    nf = nf,
                    onExportPdf = {
                        try {
                            val file = FinancialPdfGenerator.generateZakatAndInfaqReportPdf(context, state)
                            generatedPdfFile = file
                        } catch (e: Exception) {
                            Toast.makeText(context, "Gagal membuat PDF Zakat: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onCopySummary = {
                        clipboard.setText(AnnotatedString(buildZakatReportTextSummary(state, nf)))
                        Toast.makeText(context, "Ringkasan laporan zakat disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 2. Laporan Rinci Zakat Maal (Nisab & Haul)
            item {
                ZakatMaalReportSection(
                    calc = state.zakatMaal,
                    nf = nf,
                    onUpdateMaal = { viewModel.updateZakatMaal(it) },
                    onUpdateHaulStartDate = { viewModel.updateHaulStartDate(it) },
                    onSyncDatabase = {
                        viewModel.syncZakatMaalFromDatabase()
                        Toast.makeText(context, "Neraca Zakat Maal disinkronkan dengan database.", Toast.LENGTH_SHORT).show()
                    },
                    onDisburseMaal = { amt ->
                        disburseTitlePreset = "Zakat Maal (Nisab & Haul)"
                        disburseAmountPreset = amt
                        showDisburseModal = true
                    }
                )
            }

            // 3. Laporan Zakat Fitrah (Tanggungan & Harga Bahan Pokok Lokal)
            item {
                ZakatFitrahReportSection(
                    calc = state.zakatFitrah,
                    nf = nf,
                    onUpdateFitrah = { viewModel.updateZakatFitrah(it) },
                    onAddDependent = { name, rel -> viewModel.addZakatFitrahDependent(name, rel) },
                    onToggleDependent = { id -> viewModel.toggleZakatFitrahDependent(id) },
                    onRemoveDependent = { id -> viewModel.removeZakatFitrahDependent(id) },
                    onDisburseFitrah = { amt ->
                        disburseTitlePreset = "Zakat Fitrah Keluarga (${state.zakatFitrah.activeDependentsCount} Jiwa)"
                        disburseAmountPreset = amt
                        showDisburseModal = true
                    }
                )
            }

            // 4. Riwayat Penyaluran Zakat Tercatat
            item {
                Text(
                    text = "Catatan Penyaluran Zakat Tercatat (${zakatDisbursements.size})",
                    fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (zakatDisbursements.isEmpty()) {
                item {
                    Card(
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Belum ada penyaluran zakat yang tercatat di buku kas.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gunakan tombol Tunaikan Zakat Maal atau Tunaikan Zakat Fitrah di atas setelah menunaikannya.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(zakatDisbursements, key = { it.id }) { rec ->
                    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }
                    Surface(
                        shape = cardShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rec.programName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("${rec.recipientName} • ${rec.asnafCategory.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${sdf.format(rec.distributionDate)} (${rec.hijriDateString})", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rp ${nf.format(rec.amount.toLong())}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                if (rec.receiptNumber.isNotBlank()) {
                                    Text(rec.receiptNumber, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // 5. Landasan Dalil Zakat
            item {
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Landasan Dalil & Kaidah Hisab Zakat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = "• Zakat Maal: \"Ambillah zakat dari sebagian harta mereka, dengan zakat itu kamu membersihkan dan mensucikan mereka.\" (QS. At-Taubah: 103). Wajib saat harta bersih mencapai nisab 85 gram emas dan genap 1 tahun haul.\n" +
                                "• Zakat Fitrah: \"Rasulullah SAW mewajibkan zakat fitrah satu sha' kurma atau satu sha' gandum atas umat Islam.\" (HR. Bukhari & Muslim) setara 2.5 kg atau 3.5 liter makanan pokok daerah setempat per jiwa.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showDisburseModal) {
        ZakatReportDisburseDialog(
            presetTitle = disburseTitlePreset,
            presetAmount = disburseAmountPreset,
            wallets = state.wallets,
            onDismiss = { showDisburseModal = false },
            onConfirm = { title, amount, asnaf, accountId, recipient, notes, receipt ->
                viewModel.disburseZakat(title, amount, asnaf, accountId, recipient, notes, receipt)
                showDisburseModal = false
            }
        )
    }

    generatedPdfFile?.let { file ->
        AlertDialog(
            onDismissRequest = { generatedPdfFile = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Laporan PDF Zakat Siap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Berkas: ${file.name}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Dokumen PDF mencakup neraca Zakat Maal, status Nisab & Haul, daftar tanggungan Zakat Fitrah, dan riwayat penyaluran 8 Asnaf.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        FinancialPdfGenerator.sharePdfReport(context, file)
                        generatedPdfFile = null
                    },
                    shape = rememberAppShape(8)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bagikan PDF")
                }
            },
            dismissButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            FinancialPdfGenerator.viewPdfReport(context, file)
                            generatedPdfFile = null
                        },
                        shape = rememberAppShape(8)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buka")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(onClick = { generatedPdfFile = null }) { Text("Tutup") }
                }
            }
        )
    }
}

private fun buildZakatReportTextSummary(state: AmanahLedgerUiState, nf: NumberFormat): String {
    val zm = state.zakatMaal
    val zf = state.zakatFitrah
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val haulStartStr = if (zm.haulStartDateMillis > 0L) sdf.format(Date(zm.haulStartDateMillis)) else "Belum disetel"
    val deps = zf.dependents.filter { it.isIncluded }.joinToString(", ") { "${it.name} (${it.relation})" }
    return buildString {
        appendLine("=== LAPORAN ZAKAT AMANAH LEDGER ===")
        appendLine("Nama Kas: ${state.userNameKasMukmin}")
        appendLine()
        appendLine("1. ZAKAT MAAL (NISAB & HAUL)")
        appendLine("- Harta Bersih Kena Zakat: Rp ${nf.format(zm.netTaxableAssets.toLong())}")
        appendLine("- Ambang Nisab (85g Emas): Rp ${nf.format(zm.nisabGold85g.toLong())} (${if (zm.isNisabReached) "TERCAPAI" else "BELUM TERCAPAI"})")
        appendLine("- Mulai Haul: $haulStartStr (${zm.elapsedHaulDays()}/${zm.haulTargetDays} hari)")
        appendLine("- Kewajiban Zakat Maal: Rp ${nf.format(zm.zakatAmount.toLong())}")
        appendLine()
        appendLine("2. ZAKAT FITRAH KELUARGA")
        appendLine("- Bahan Pokok: ${zf.stapleFoodName} (${zf.regionName})")
        appendLine("- Takaran & Harga: ${zf.riceAmountPerPersonKg} ${zf.measurementUnit} x Rp ${nf.format(zf.ricePricePerKg.toLong())} = Rp ${nf.format(zf.ratePerSoulRupiah.toLong())}/jiwa")
        appendLine("- Tanggungan (${zf.activeDependentsCount} Jiwa): ${deps.ifBlank { "-" }}")
        appendLine("- Total Zakat Fitrah: Rp ${nf.format(zf.totalFitrahRupiah.toLong())} (${zf.totalRiceKg} ${zf.measurementUnit})")
        appendLine()
        appendLine("3. TOTAL GABUNGAN & REALISASI")
        appendLine("- Total Kewajiban Zakat: Rp ${nf.format(state.totalZakatReportPayable.toLong())}")
        appendLine("- Sudah Disalurkan: Rp ${nf.format(state.totalZakatDisbursedRecorded.toLong())}")
    }
}

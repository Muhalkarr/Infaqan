package com.example.presentation.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.debug.AppDebugLogger
import com.example.core.report.AllInOneMasterPdfGenerator
import com.example.core.report.AnnualBaznasZakatPdfGenerator
import com.example.core.report.FinancialPdfGenerator
import com.example.core.report.PdfDateRange
import com.example.core.report.PdfEditorialConfig
import com.example.core.report.PdfEditorialStyle
import com.example.core.report.PdfSectionCustomization
import com.example.core.report.PdfSectionKey
import com.example.core.state.AmanahLedgerUiState
import com.example.presentation.components.PdfDateRangeCard
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.isTactical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

enum class PdfReportTarget(
    val displayTitle: String,
    val defaultFilePrefix: String,
    val configurableSections: List<PdfSectionKey>
) {
    ALL_IN_ONE(
        displayTitle = "Laporan Utama Lengkap (All-in-One)",
        defaultFilePrefix = "Laporan_Utama_AllInOne",
        configurableSections = PdfSectionKey.entries.toList()
    ),
    COMPREHENSIVE_FINANCIAL(
        displayTitle = "Laporan Kondisi Finansial Komprehensif",
        defaultFilePrefix = "Laporan_Finansial_Komprehensif",
        configurableSections = listOf(
            PdfSectionKey.WALLETS,
            PdfSectionKey.CASHFLOW_KASAB,
            PdfSectionKey.ZAKAT_MAAL_FITRAH
        )
    ),
    COMPREHENSIVE_LEDGER(
        displayTitle = "Buku Besar Jurnal Transaksi",
        defaultFilePrefix = "Buku_Besar_Ledger",
        configurableSections = listOf(
            PdfSectionKey.WALLETS,
            PdfSectionKey.JOURNAL_LEDGER
        )
    ),
    ZAKAT_INFAQ(
        displayTitle = "Laporan Zakat, Infaq & Sedekah",
        defaultFilePrefix = "Laporan_Zakat_Infaq",
        configurableSections = listOf(
            PdfSectionKey.ZAKAT_PROFESI_PERNIAGAAN,
            PdfSectionKey.SEDEKAH_SUBUH,
            PdfSectionKey.ASNAF_DISTRIBUTION,
            PdfSectionKey.IBADAH_GOALS
        )
    ),
    BAZNAS_ZAKAT(
        displayTitle = "Lembar Penetapan Zakat BAZNAS",
        defaultFilePrefix = "Penetapan_Zakat_BAZNAS",
        configurableSections = emptyList()
    ),
    MONTHLY_REPORT(
        displayTitle = "Laporan Keuangan Bulanan",
        defaultFilePrefix = "Laporan_Bulanan",
        configurableSections = listOf(
            PdfSectionKey.BUDGETS,
            PdfSectionKey.CASHFLOW_KASAB,
            PdfSectionKey.SLI_ANALYSIS,
            PdfSectionKey.ASNAF_DISTRIBUTION,
            PdfSectionKey.JOURNAL_LEDGER
        )
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PdfEditorialPreviewDialog(
    target: PdfReportTarget,
    uiState: AmanahLedgerUiState,
    initialTab: Int = 0,
    targetMonth: Int? = null,
    targetYear: Int? = null,
    dateRange: PdfDateRange = PdfDateRange(),
    onDateRangeChanged: (PdfDateRange) -> Unit = {},
    onDismiss: () -> Unit,
    onSaveAsDefault: (PdfEditorialConfig) -> Unit
) {
    val context = LocalContext.current
    val isTactical = uiState.themeMode.isTactical
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(14.dp)
    val buttonShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp)
    val titleFont = if (isTactical) ChakraPetchFontFamily else null
    val monoFont = if (isTactical) ShareTechMonoFontFamily else null

    var activeTab by remember { mutableIntStateOf(initialTab) }
    var draftConfig by remember(uiState.pdfEditorialConfig) { mutableStateOf(uiState.pdfEditorialConfig) }
    var draftDateRange by remember(dateRange) { mutableStateOf(dateRange) }
    var showInlineDatePicker by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var previewFile by remember { mutableStateOf<File?>(null) }
    var renderedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var totalPageCount by remember { mutableIntStateOf(0) }
    var isRendering by remember { mutableStateOf(true) }
    var renderError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshTrigger, target, targetMonth, targetYear, draftDateRange) {
        isRendering = true
        renderError = null
        val result = withContext(Dispatchers.IO) {
            try {
                val file = generatePdfForTarget(
                    context = context,
                    target = target,
                    state = uiState,
                    config = draftConfig,
                    targetMonth = targetMonth,
                    targetYear = targetYear,
                    dateRange = draftDateRange
                )
                val (bitmaps, count) = renderPdfPages(file)
                Triple(file, bitmaps, count) to null
            } catch (e: Exception) {
                Triple(null, emptyList<Bitmap>(), 0) to (e.message ?: "Gagal merender pratinjau PDF")
            }
        }
        val (triple, err) = result
        previewFile = triple.first
        renderedPages = triple.second
        totalPageCount = triple.third
        renderError = err
        isRendering = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 18.dp),
            shape = cardShape,
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pratinjau & Redaksi PDF",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = titleFont,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = target.displayTitle,
                                    fontSize = 11.sp,
                                    fontFamily = monoFont,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                    .testTag("pdf_preview_close_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup Pratinjau")
                            }
                        }

                        TabRow(selectedTabIndex = activeTab) {
                            Tab(
                                selected = activeTab == 0,
                                onClick = {
                                    if (activeTab != 0) {
                                        activeTab = 0
                                        refreshTrigger++
                                    }
                                },
                                modifier = Modifier.testTag("pdf_preview_tab_view"),
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pratinjau Halaman", fontSize = 12.sp)
                                    }
                                }
                            )
                            Tab(
                                selected = activeTab == 1,
                                onClick = { activeTab = 1 },
                                modifier = Modifier.testTag("pdf_preview_tab_edit"),
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ubah Redaksi & Konten", fontSize = 12.sp)
                                    }
                                }
                            )
                        }
                    }
                }

                // Body Area
                Box(modifier = Modifier.weight(1f)) {
                    if (activeTab == 0) {
                        // TAB 1: LIVE PREVIEW (Gaya Cepat + Ringkasan Struktur + Halaman A4 Terender)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Strip ganti gaya cepat langsung di pratinjau
                            item {
                                Card(
                                    shape = cardShape,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Gaya Bahasa Laporan:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = titleFont
                                            )
                                            TextButton(
                                                onClick = { activeTab = 1 },
                                                modifier = Modifier.testTag("pdf_preview_jump_edit_btn")
                                            ) {
                                                Text("Ubah Judul & Isi ->", fontSize = 11.sp)
                                            }
                                        }
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            PdfEditorialStyle.entries.forEach { preset ->
                                                FilterChip(
                                                    selected = draftConfig.stylePreset == preset,
                                                    onClick = {
                                                        draftConfig = PdfEditorialConfig.forPreset(
                                                            style = preset,
                                                            keepOwnerAndClosingNoteFrom = draftConfig
                                                        )
                                                        refreshTrigger++
                                                    },
                                                    label = { Text(preset.badge, fontSize = 11.sp) },
                                                    leadingIcon = if (draftConfig.stylePreset == preset) {
                                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                                    } else null,
                                                    shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                                                    modifier = Modifier.testTag("pdf_preset_chip_${preset.name}")
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val effectiveTitle = draftConfig.resolveDocumentTitle(target.displayTitle)
                                        val effectiveBrand = draftConfig.headerBrandTitle.trim()
                                            .ifBlank { effectiveTitle.uppercase() }
                                        val effectiveOwner = draftConfig.resolveOwnerName(uiState.userNameKasMukmin)
                                        val fileKb = (previewFile?.length() ?: 0L) / 1024
                                        Text(
                                            text = "Kop: $effectiveBrand • ${draftConfig.resolveOwnerLabel(isTactical)} $effectiveOwner",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Periode: ${draftDateRange.formatDualCalendarSummary()}",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Berkas: ${previewFile?.name ?: "${draftConfig.resolveFilePrefix(target.defaultFilePrefix)}.pdf"} ($totalPageCount halaman • ${fileKb} KB)",
                                            fontSize = 10.sp,
                                            fontFamily = monoFont,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        TextButton(
                                            onClick = { showInlineDatePicker = !showInlineDatePicker },
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 40.dp)
                                                .testTag("pdf_preview_toggle_date_range_btn")
                                        ) {
                                            Text(
                                                text = if (showInlineDatePicker) "Sembunyikan Filter Tanggal ▲" else "Atur Rentang Tanggal (Masehi / Hijriyah) ▼",
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (showInlineDatePicker) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            PdfDateRangeCard(
                                                uiState = uiState,
                                                dateRange = draftDateRange,
                                                onDateRangeChange = {
                                                    draftDateRange = it
                                                    onDateRangeChanged(it)
                                                },
                                                compact = true
                                            )
                                        }
                                    }
                                }
                            }

                            if (isRendering) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator()
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                "Menyusun pratinjau halaman PDF...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else if (renderError != null) {
                                item {
                                    Card(
                                        shape = cardShape,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text("Pratinjau visual tidak tersedia: $renderError", fontSize = 12.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                "Anda tetap dapat mengekspor berkas PDF melalui tombol di bawah.",
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            } else {
                                itemsIndexed(renderedPages) { index, bitmap ->
                                    Card(
                                        shape = RoundedCornerShape(6.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "Halaman ${index + 1} dari $totalPageCount (A4)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = monoFont,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    draftConfig.stylePreset.badge,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Pratinjau halaman PDF ${index + 1}",
                                                contentScale = ContentScale.FillWidth,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(595f / 842f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // TAB 2: KOSTUMISASI REDAKSI & PENAMAAN KONTEN
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 0. Rentang Periode Masehi / Hijriyah
                            PdfDateRangeCard(
                                uiState = uiState,
                                dateRange = draftDateRange,
                                onDateRangeChange = {
                                    draftDateRange = it
                                    onDateRangeChanged(it)
                                },
                                compact = true
                            )
                            HorizontalDivider()

                            // A. Preset Gaya Bahasa
                            Text(
                                "1. Pilih Gaya Bahasa Laporan",
                                fontWeight = FontWeight.Bold,
                                fontFamily = titleFont,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            PdfEditorialStyle.entries.forEach { preset ->
                                val selected = draftConfig.stylePreset == preset
                                Card(
                                    onClick = {
                                        draftConfig = PdfEditorialConfig.forPreset(
                                            style = preset,
                                            keepOwnerAndClosingNoteFrom = draftConfig
                                        )
                                    },
                                    shape = cardShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(preset.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(preset.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (selected) {
                                            Icon(Icons.Default.Check, contentDescription = "Terpilih", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }

                            // Pratinjau Redaksi Default Berdasarkan Preset Aktif
                            Card(
                                shape = cardShape,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pratinjau Redaksi Default Saat Ini (${draftConfig.stylePreset.badge})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("• Judul: ${target.displayTitle}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("• Kop Header: ${draftConfig.resolveHeaderBrand(isTactical)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("• Sub-judul: ${draftConfig.resolveHeaderSubtitle(isTactical, "Ringkasan Saldo, Pemasukan, Pengeluaran & Transaksi")}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("• Label Pemilik: ${draftConfig.resolveOwnerLabel(isTactical)} ${uiState.userNameKasMukmin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("• Catatan Kaki: ${draftConfig.resolveFooterText(isTactical)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("• Istilah Akun: ${draftConfig.resolveKasabLabel()} / ${draftConfig.resolveNonKasabLabel()} / ${draftConfig.resolveExpenseLabel()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            HorizontalDivider()

                            // B. Kop, Identitas Pemilik, Nama File & Footer
                            Text(
                                "2. Judul, Kop, Pemilik & Catatan Kaki",
                                fontWeight = FontWeight.Bold,
                                fontFamily = titleFont,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Kosongkan kolom jika ingin memakai teks bawaan dari gaya bahasa di atas.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = draftConfig.documentTitleOverride,
                                onValueChange = { draftConfig = draftConfig.copy(documentTitleOverride = it) },
                                label = { Text("Judul Dokumen") },
                                placeholder = { Text(target.displayTitle, fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_edit_doc_title_input")
                            )

                            OutlinedTextField(
                                value = draftConfig.headerBrandTitle,
                                onValueChange = { draftConfig = draftConfig.copy(headerBrandTitle = it) },
                                label = { Text("Teks Kop / Header Utama") },
                                placeholder = { Text(draftConfig.resolveHeaderBrand(isTactical), fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_edit_brand_title_input")
                            )

                            OutlinedTextField(
                                value = draftConfig.headerSubtitle,
                                onValueChange = { draftConfig = draftConfig.copy(headerSubtitle = it) },
                                label = { Text("Sub-judul Laporan") },
                                placeholder = { Text("Ringkasan Saldo, Pemasukan, Pengeluaran & Transaksi", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_edit_subtitle_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = draftConfig.ownerLabel,
                                    onValueChange = { draftConfig = draftConfig.copy(ownerLabel = it) },
                                    label = { Text("Label Pemilik") },
                                    placeholder = { Text(draftConfig.resolveOwnerLabel(isTactical), fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .testTag("pdf_edit_owner_label_input")
                                )
                                OutlinedTextField(
                                    value = draftConfig.ownerNameOverride,
                                    onValueChange = { draftConfig = draftConfig.copy(ownerNameOverride = it) },
                                    label = { Text("Nama Pemilik / Usaha") },
                                    placeholder = { Text(uiState.userNameKasMukmin, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("pdf_edit_owner_name_input")
                                )
                            }

                            OutlinedTextField(
                                value = draftConfig.customFileNamePrefix,
                                onValueChange = { draftConfig = draftConfig.copy(customFileNamePrefix = it) },
                                label = { Text("Nama Berkas PDF") },
                                placeholder = { Text(target.defaultFilePrefix, fontSize = 12.sp) },
                                supportingText = {
                                    Text(
                                        "Hasil: ${draftConfig.resolveFilePrefix(target.defaultFilePrefix)}_....pdf",
                                        fontSize = 10.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_edit_filename_input")
                            )

                            OutlinedTextField(
                                value = draftConfig.footerText,
                                onValueChange = { draftConfig = draftConfig.copy(footerText = it) },
                                label = { Text("Teks Catatan Kaki (Footer)") },
                                placeholder = { Text(draftConfig.resolveFooterText(isTactical), fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_edit_footer_input")
                            )

                            OutlinedTextField(
                                value = draftConfig.closingNote,
                                onValueChange = { draftConfig = draftConfig.copy(closingNote = it) },
                                label = { Text("Catatan Penutup / Tanda Tangan (Opsional)") },
                                placeholder = { Text("Misal: Disetujui oleh Bendahara Keluarga / Kota & Tanggal", fontSize = 12.sp) },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pdf_edit_closing_note_input")
                            )

                            // Toggles opsi tampilan tambahan
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sertakan Penanggalan Hijriah", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = draftConfig.includeHijriDate,
                                    onCheckedChange = { draftConfig = draftConfig.copy(includeHijriDate = it) },
                                    modifier = Modifier.testTag("pdf_edit_hijri_switch")
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sertakan 4 Kartu Ringkasan KPI di Atas", fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Switch(
                                    checked = draftConfig.includeExecutiveKpis,
                                    onCheckedChange = { draftConfig = draftConfig.copy(includeExecutiveKpis = it) },
                                    modifier = Modifier.testTag("pdf_edit_kpi_switch")
                                )
                            }

                            HorizontalDivider()

                            // C. Kebebasan Penamaan Istilah Arus Kas
                            Text(
                                "3. Penamaan Istilah Pemasukan & Pengeluaran",
                                fontWeight = FontWeight.Bold,
                                fontFamily = titleFont,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = draftConfig.kasabLabel,
                                    onValueChange = { draftConfig = draftConfig.copy(kasabLabel = it) },
                                    label = { Text("Label Kasab") },
                                    placeholder = { Text(draftConfig.resolveKasabLabel(), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("pdf_edit_kasab_label_input")
                                )
                                OutlinedTextField(
                                    value = draftConfig.nonKasabLabel,
                                    onValueChange = { draftConfig = draftConfig.copy(nonKasabLabel = it) },
                                    label = { Text("Label Non-Kasab") },
                                    placeholder = { Text(draftConfig.resolveNonKasabLabel(), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("pdf_edit_non_kasab_label_input")
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = draftConfig.expenseLabel,
                                    onValueChange = { draftConfig = draftConfig.copy(expenseLabel = it) },
                                    label = { Text("Label Pengeluaran") },
                                    placeholder = { Text(draftConfig.resolveExpenseLabel(), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("pdf_edit_expense_label_input")
                                )
                                OutlinedTextField(
                                    value = draftConfig.netSavingsLabel,
                                    onValueChange = { draftConfig = draftConfig.copy(netSavingsLabel = it) },
                                    label = { Text("Label Tabungan Bersih") },
                                    placeholder = { Text(draftConfig.resolveNetSavingsLabel(), fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("pdf_edit_net_savings_label_input")
                                )
                            }

                             // D. Kebebasan Penamaan & Pemilihan Bagian Konten
                             if (target.configurableSections.isNotEmpty()) {
                                 HorizontalDivider()
                                Text(
                                    "4. Penamaan & Pilihan Isi Konten (${target.configurableSections.size} Bagian)",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = titleFont,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Ubah nama sub-bab sesuai selera Anda atau matikan bagian yang tidak ingin disertakan.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                target.configurableSections.forEach { secKey ->
                                    val secCustom = draftConfig.sectionCustomizations[secKey] ?: PdfSectionCustomization()
                                    Card(
                                        shape = cardShape,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = secKey.labelInEditor,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Switch(
                                                    checked = secCustom.included,
                                                    onCheckedChange = { checked ->
                                                        val updated = draftConfig.sectionCustomizations.toMutableMap()
                                                        updated[secKey] = secCustom.copy(included = checked)
                                                        draftConfig = draftConfig.copy(sectionCustomizations = updated)
                                                    },
                                                    modifier = Modifier.testTag("pdf_section_toggle_${secKey.name}")
                                                )
                                            }
                                            if (secCustom.included) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                OutlinedTextField(
                                                    value = secCustom.customTitle,
                                                    onValueChange = { newTitle ->
                                                        val updated = draftConfig.sectionCustomizations.toMutableMap()
                                                        updated[secKey] = secCustom.copy(customTitle = newTitle)
                                                        draftConfig = draftConfig.copy(sectionCustomizations = updated)
                                                    },
                                                    label = { Text("Judul Bagian di PDF", fontSize = 11.sp) },
                                                    placeholder = {
                                                        Text(
                                                            secKey.defaultTitleFor(draftConfig.stylePreset, isTactical),
                                                            fontSize = 11.sp
                                                        )
                                                    },
                                                    singleLine = true,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("pdf_section_title_${secKey.name}")
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        draftConfig = PdfEditorialConfig()
                                        refreshTrigger++
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("pdf_edit_reset_btn"),
                                    shape = buttonShape
                                ) {
                                    Text("Reset Default", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        activeTab = 0
                                        refreshTrigger++
                                    },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("pdf_edit_apply_preview_btn"),
                                    shape = buttonShape
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Terapkan & Pratinjau", fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Bottom Persistent Action Bar (Ekspor setelah yakin dengan pratinjau)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onSaveAsDefault(draftConfig)
                                    onDateRangeChanged(draftDateRange)
                                    refreshTrigger++
                                    Toast.makeText(context, "Redaksi & periode PDF disimpan.", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(0.95f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("pdf_preview_save_default_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Simpan", fontSize = 11.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        onDateRangeChanged(draftDateRange)
                                        val file = generatePdfForTarget(context, target, uiState, draftConfig, targetMonth, targetYear, draftDateRange)
                                        FinancialPdfGenerator.viewPdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Membuka PDF kustom ${target.displayTitle}")
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuka PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(0.95f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("pdf_preview_open_viewer_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Buka", fontSize = 11.sp, maxLines = 1)
                            }

                            Button(
                                onClick = {
                                    try {
                                        onSaveAsDefault(draftConfig)
                                        onDateRangeChanged(draftDateRange)
                                        val file = generatePdfForTarget(context, target, uiState, draftConfig, targetMonth, targetYear, draftDateRange)
                                        FinancialPdfGenerator.sharePdfReport(context, file)
                                        AppDebugLogger.logUserAction("Ekspor PDF", "Mengekspor PDF kustom ${target.displayTitle}")
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal ekspor PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("pdf_preview_final_export_btn"),
                                shape = buttonShape
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Ekspor PDF", fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generatePdfForTarget(
    context: Context,
    target: PdfReportTarget,
    state: AmanahLedgerUiState,
    config: PdfEditorialConfig,
    targetMonth: Int? = null,
    targetYear: Int? = null,
    dateRange: PdfDateRange = PdfDateRange()
): File {
    val cal = Calendar.getInstance()
    val m = targetMonth ?: cal.get(Calendar.MONTH)
    val y = targetYear ?: cal.get(Calendar.YEAR)
    return when (target) {
        PdfReportTarget.ALL_IN_ONE -> AllInOneMasterPdfGenerator.generateAllInOneMasterPdf(
            context = context,
            state = state,
            targetMonth = m,
            targetYear = y,
            editorialConfig = config,
            dateRange = dateRange
        )
        PdfReportTarget.COMPREHENSIVE_FINANCIAL -> FinancialPdfGenerator.generateComprehensiveFinancialPdf(
            context = context,
            state = state,
            editorialConfig = config,
            dateRange = dateRange
        )
        PdfReportTarget.COMPREHENSIVE_LEDGER -> FinancialPdfGenerator.generateComprehensiveLedgerPdf(
            context = context,
            state = state,
            targetMonth = m,
            targetYear = y,
            editorialConfig = config,
            dateRange = dateRange
        )
        PdfReportTarget.ZAKAT_INFAQ -> FinancialPdfGenerator.generateZakatAndInfaqReportPdf(
            context = context,
            state = state,
            editorialConfig = config,
            dateRange = dateRange
        )
        PdfReportTarget.BAZNAS_ZAKAT -> AnnualBaznasZakatPdfGenerator.generateAnnualBaznasZakatReportPdf(
            context = context,
            state = state,
            muzakkiName = state.userNameKasMukmin,
            editorialConfig = config,
            dateRange = dateRange
        )
        PdfReportTarget.MONTHLY_REPORT -> FinancialPdfGenerator.generateMonthlyReportPdf(
            context = context,
            state = state,
            targetMonth = m,
            targetYear = y,
            editorialConfig = config,
            dateRange = dateRange
        )
    }
}

private fun renderPdfPages(file: File): Pair<List<Bitmap>, Int> {
    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    val renderer = PdfRenderer(pfd)
    val bitmaps = mutableListOf<Bitmap>()
    return try {
        val count = renderer.pageCount
        val renderCount = count.coerceAtMost(20)
        for (i in 0 until renderCount) {
            val page = renderer.openPage(i)
            try {
                val targetW = 900
                val targetH = ((targetW.toFloat() / page.width.toFloat()) * page.height.toFloat()).toInt().coerceAtLeast(1)
                val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bmp)
                canvas.drawColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bmp)
            } finally {
                page.close()
            }
        }
        bitmaps to count
    } finally {
        renderer.close()
        pfd.close()
    }
}

package com.example.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.core.report.PdfCalendarMode
import com.example.core.report.PdfDateRange
import com.example.core.report.PdfPeriodFilterHelper
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.isTactical
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ActiveDatePickTarget { START, END }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PdfDateRangeCard(
    uiState: AmanahLedgerUiState,
    dateRange: PdfDateRange,
    onDateRangeChange: (PdfDateRange) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isTactical = uiState.themeMode.isTactical
    val hijriOffset = uiState.selectedHijriOffset
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(14.dp)
    val buttonShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp)
    val titleFont = if (isTactical) ChakraPetchFontFamily else null
    val monoFont = if (isTactical) ShareTechMonoFontFamily else null

    val todayHijri = remember(hijriOffset) {
        HijriCalendarEngine.fromGregorian(Date(), maghribHour = 24, manualOffsetDays = hijriOffset)
    }
    val sdfShort = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val scopedPreview = remember(uiState.journalEntries, uiState.infaqDistributions, dateRange) {
        PdfPeriodFilterHelper.compute(uiState, dateRange)
    }

    var pickTarget by remember { mutableStateOf<ActiveDatePickTarget?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (dateRange.isCustomEnabled) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (dateRange.isCustomEnabled) 1.5.dp else 1.dp,
            color = if (dateRange.isCustomEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(if (compact) 12.dp else 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isTactical) "PERIODE LAPORAN // MASEHI & HIJRIYAH" else "Rentang Waktu Laporan PDF",
                            fontWeight = FontWeight.Bold,
                            fontFamily = titleFont,
                            fontSize = if (compact) 13.sp else 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tentukan dari tanggal ke tanggal (kalender Masehi atau Hijriyah)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (dateRange.isCustomEnabled) {
                    TextButton(
                        onClick = { onDateRangeChange(PdfDateRange.allTime(dateRange.calendarMode)) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("pdf_range_reset_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1. Pilihan Sistem Kalender: Masehi vs Hijriyah
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = dateRange.calendarMode == PdfCalendarMode.MASEHI,
                    onClick = {
                        if (dateRange.calendarMode != PdfCalendarMode.MASEHI) {
                            val updated = if (dateRange.isCustomEnabled && dateRange.startMillis != null && dateRange.endMillis != null) {
                                PdfDateRange.fromGregorianRange(
                                    rawStartMillis = dateRange.startMillis,
                                    rawEndMillis = dateRange.endMillis,
                                    manualOffsetDays = hijriOffset,
                                    presetTag = PdfDateRange.PRESET_MASEHI_CUSTOM
                                )
                            } else {
                                PdfDateRange.allTime(PdfCalendarMode.MASEHI)
                            }
                            onDateRangeChange(updated)
                        }
                    },
                    label = { Text("Kalender Masehi", fontSize = 11.sp, fontFamily = monoFont) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (dateRange.calendarMode == PdfCalendarMode.MASEHI) Icons.Default.Check else Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("pdf_range_mode_masehi_chip")
                )

                FilterChip(
                    selected = dateRange.calendarMode == PdfCalendarMode.HIJRIYAH,
                    onClick = {
                        if (dateRange.calendarMode != PdfCalendarMode.HIJRIYAH) {
                            val updated = if (dateRange.isCustomEnabled && dateRange.startMillis != null && dateRange.endMillis != null) {
                                val sH = dateRange.startHijri ?: HijriCalendarEngine.fromGregorian(Date(dateRange.startMillis), 24, hijriOffset)
                                val eH = dateRange.endHijri ?: HijriCalendarEngine.fromGregorian(Date(dateRange.endMillis), 24, hijriOffset)
                                PdfDateRange.fromHijriRange(
                                    rawStart = sH,
                                    rawEnd = eH,
                                    manualOffsetDays = hijriOffset,
                                    presetTag = PdfDateRange.PRESET_HIJRI_CUSTOM
                                )
                            } else {
                                PdfDateRange.allTime(PdfCalendarMode.HIJRIYAH)
                            }
                            onDateRangeChange(updated)
                        }
                    },
                    label = { Text("Kalender Hijriyah", fontSize = 11.sp, fontFamily = monoFont) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (dateRange.calendarMode == PdfCalendarMode.HIJRIYAH) Icons.Default.Check else Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                    },
                    shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("pdf_range_mode_hijri_chip")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Preset Cepat sesuai kalender aktif
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterChip(
                    selected = !dateRange.isCustomEnabled,
                    onClick = { onDateRangeChange(PdfDateRange.allTime(dateRange.calendarMode)) },
                    label = { Text("Semua Waktu", fontSize = 11.sp, fontFamily = monoFont) },
                    shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                    modifier = Modifier.testTag("pdf_range_preset_all")
                )

                if (dateRange.calendarMode == PdfCalendarMode.MASEHI) {
                    FilterChip(
                        selected = dateRange.isCustomEnabled && dateRange.presetTag == PdfDateRange.PRESET_MASEHI_THIS_MONTH,
                        onClick = { onDateRangeChange(PdfDateRange.forCurrentGregorianMonth(hijriOffset)) },
                        label = { Text("Bulan Ini", fontSize = 11.sp, fontFamily = monoFont) },
                        shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                        modifier = Modifier.testTag("pdf_range_preset_month")
                    )
                    FilterChip(
                        selected = dateRange.isCustomEnabled && dateRange.presetTag == PdfDateRange.PRESET_MASEHI_LAST_30D,
                        onClick = { onDateRangeChange(PdfDateRange.forLast30Days(hijriOffset)) },
                        label = { Text("30 Hari Terakhir", fontSize = 11.sp, fontFamily = monoFont) },
                        shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                        modifier = Modifier.testTag("pdf_range_preset_30d")
                    )
                    FilterChip(
                        selected = dateRange.isCustomEnabled && dateRange.presetTag == PdfDateRange.PRESET_MASEHI_THIS_YEAR,
                        onClick = { onDateRangeChange(PdfDateRange.forCurrentGregorianYear(hijriOffset)) },
                        label = { Text("Tahun Ini", fontSize = 11.sp, fontFamily = monoFont) },
                        shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                        modifier = Modifier.testTag("pdf_range_preset_year")
                    )
                } else {
                    FilterChip(
                        selected = dateRange.isCustomEnabled && dateRange.presetTag == PdfDateRange.PRESET_HIJRI_THIS_MONTH,
                        onClick = { onDateRangeChange(PdfDateRange.forCurrentHijriMonth(hijriOffset)) },
                        label = { Text("Bulan ${todayHijri.monthName}", fontSize = 11.sp, fontFamily = monoFont) },
                        shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                        modifier = Modifier.testTag("pdf_range_preset_hijri_month")
                    )
                    FilterChip(
                        selected = dateRange.isCustomEnabled && dateRange.presetTag == PdfDateRange.PRESET_HIJRI_RAMADAN,
                        onClick = {
                            onDateRangeChange(
                                PdfDateRange.forHijriMonth(
                                    hijriYear = todayHijri.year,
                                    hijriMonth = 9,
                                    manualOffsetDays = hijriOffset,
                                    presetTag = PdfDateRange.PRESET_HIJRI_RAMADAN
                                )
                            )
                        },
                        label = { Text("Ramadan ${todayHijri.year} H", fontSize = 11.sp, fontFamily = monoFont) },
                        shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                        modifier = Modifier.testTag("pdf_range_preset_ramadan")
                    )
                    FilterChip(
                        selected = dateRange.isCustomEnabled && dateRange.presetTag == PdfDateRange.PRESET_HIJRI_THIS_YEAR,
                        onClick = { onDateRangeChange(PdfDateRange.forHijriYear(todayHijri.year, hijriOffset)) },
                        label = { Text("Tahun ${todayHijri.year} H", fontSize = 11.sp, fontFamily = monoFont) },
                        shape = if (isTactical) CutCornerShape(4.dp) else FilterChipDefaults.shape,
                        modifier = Modifier.testTag("pdf_range_preset_hijri_year")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Tombol Pilih Tanggal Dari -> Sampai
            val startPrimaryText = when {
                !dateRange.isCustomEnabled -> "Pilih Tanggal Awal"
                dateRange.calendarMode == PdfCalendarMode.HIJRIYAH && dateRange.startHijri != null ->
                    "${dateRange.startHijri.day} ${dateRange.startHijri.monthName} ${dateRange.startHijri.year} H"
                dateRange.startMillis != null -> sdfShort.format(Date(dateRange.startMillis))
                else -> "Pilih Tanggal Awal"
            }
            val startSecondaryText = when {
                !dateRange.isCustomEnabled -> "Ketuk untuk atur batas awal"
                dateRange.calendarMode == PdfCalendarMode.HIJRIYAH && dateRange.startMillis != null ->
                    "≈ ${sdfShort.format(Date(dateRange.startMillis))} M"
                dateRange.startHijri != null ->
                    "≈ ${dateRange.startHijri.day} ${dateRange.startHijri.monthName} ${dateRange.startHijri.year} H"
                else -> ""
            }

            val endPrimaryText = when {
                !dateRange.isCustomEnabled -> "Pilih Tanggal Akhir"
                dateRange.calendarMode == PdfCalendarMode.HIJRIYAH && dateRange.endHijri != null ->
                    "${dateRange.endHijri.day} ${dateRange.endHijri.monthName} ${dateRange.endHijri.year} H"
                dateRange.endMillis != null -> sdfShort.format(Date(dateRange.endMillis))
                else -> "Pilih Tanggal Akhir"
            }
            val endSecondaryText = when {
                !dateRange.isCustomEnabled -> "Ketuk untuk atur batas akhir"
                dateRange.calendarMode == PdfCalendarMode.HIJRIYAH && dateRange.endMillis != null ->
                    "≈ ${sdfShort.format(Date(dateRange.endMillis))} M"
                dateRange.endHijri != null ->
                    "≈ ${dateRange.endHijri.day} ${dateRange.endHijri.monthName} ${dateRange.endHijri.year} H"
                else -> ""
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { pickTarget = ActiveDatePickTarget.START },
                    shape = buttonShape,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 56.dp)
                        .testTag("pdf_range_start_btn")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "DARI TANGGAL",
                            fontSize = 9.sp,
                            fontFamily = monoFont,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = startPrimaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (startSecondaryText.isNotEmpty()) {
                            Text(
                                text = startSecondaryText,
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = { pickTarget = ActiveDatePickTarget.END },
                    shape = buttonShape,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 56.dp)
                        .testTag("pdf_range_end_btn")
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SAMPAI TANGGAL",
                            fontSize = 9.sp,
                            fontFamily = monoFont,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = endPrimaryText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (endSecondaryText.isNotEmpty()) {
                            Text(
                                text = endSecondaryText,
                                fontSize = 9.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Ringkasan Periode Aktif & Cakupan Data (Tanpa membocorkan nominal)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = "Periode Aktif: ${dateRange.formatDualCalendarSummary()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = monoFont,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cakupan Data: ${scopedPreview.filteredEntries.size} entri jurnal • ${scopedPreview.filteredDistributions.size} catatan penyaluran",
                        fontSize = 10.5.sp,
                        fontFamily = monoFont,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Picker Dialogs
    pickTarget?.let { activeTarget ->
        if (dateRange.calendarMode == PdfCalendarMode.MASEHI) {
            val initialLocalMillis = when (activeTarget) {
                ActiveDatePickTarget.START -> dateRange.startMillis ?: System.currentTimeMillis()
                ActiveDatePickTarget.END -> dateRange.endMillis ?: System.currentTimeMillis()
            }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = PdfDateRange.localMillisToUtcPickerMillis(initialLocalMillis)
            )
            DatePickerDialog(
                onDismissRequest = { pickTarget = null },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedUtc = datePickerState.selectedDateMillis
                            if (selectedUtc != null) {
                                val pickedLocalMid = PdfDateRange.utcPickerMillisToLocalMidday(selectedUtc)
                                val fallbackOther = System.currentTimeMillis()
                                val newStart = if (activeTarget == ActiveDatePickTarget.START) {
                                    pickedLocalMid
                                } else {
                                    dateRange.startMillis ?: pickedLocalMid
                                }
                                val newEnd = if (activeTarget == ActiveDatePickTarget.END) {
                                    pickedLocalMid
                                } else {
                                    dateRange.endMillis ?: maxOf(pickedLocalMid, fallbackOther)
                                }
                                onDateRangeChange(
                                    PdfDateRange.fromGregorianRange(
                                        rawStartMillis = newStart,
                                        rawEndMillis = newEnd,
                                        manualOffsetDays = hijriOffset,
                                        presetTag = PdfDateRange.PRESET_MASEHI_CUSTOM
                                    )
                                )
                            }
                            pickTarget = null
                        },
                        modifier = Modifier.testTag("masehi_picker_confirm_btn")
                    ) {
                        Text("Terapkan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pickTarget = null }) {
                        Text("Batal")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        } else {
            val initialHijri = when (activeTarget) {
                ActiveDatePickTarget.START -> dateRange.startHijri ?: todayHijri
                ActiveDatePickTarget.END -> dateRange.endHijri ?: todayHijri
            }
            HijriDatePickerDialog(
                title = if (activeTarget == ActiveDatePickTarget.START) "Pilih Tanggal Awal (Hijriyah)" else "Pilih Tanggal Akhir (Hijriyah)",
                initialDate = initialHijri,
                hijriOffsetDays = hijriOffset,
                isTactical = isTactical,
                onDismiss = { pickTarget = null },
                onConfirm = { pickedHijri ->
                    val newStart = if (activeTarget == ActiveDatePickTarget.START) {
                        pickedHijri
                    } else {
                        dateRange.startHijri ?: pickedHijri
                    }
                    val newEnd = if (activeTarget == ActiveDatePickTarget.END) {
                        pickedHijri
                    } else {
                        dateRange.endHijri ?: if (pickedHijri <= todayHijri) todayHijri else pickedHijri
                    }
                    onDateRangeChange(
                        PdfDateRange.fromHijriRange(
                            rawStart = newStart,
                            rawEnd = newEnd,
                            manualOffsetDays = hijriOffset,
                            presetTag = PdfDateRange.PRESET_HIJRI_CUSTOM
                        )
                    )
                    pickTarget = null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HijriDatePickerDialog(
    title: String,
    initialDate: HijriDate,
    hijriOffsetDays: Int = 0,
    isTactical: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (HijriDate) -> Unit
) {
    val cardShape = if (isTactical) CutCornerShape(10.dp) else RoundedCornerShape(18.dp)
    val buttonShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp)
    val titleFont = if (isTactical) ChakraPetchFontFamily else null
    val monoFont = if (isTactical) ShareTechMonoFontFamily else null

    var selectedYear by remember(initialDate) { mutableIntStateOf(initialDate.year) }
    var selectedMonth by remember(initialDate) { mutableIntStateOf(initialDate.month.coerceIn(1, 12)) }
    val maxDayInMonth = remember(selectedYear, selectedMonth, hijriOffsetDays) {
        HijriCalendarEngine.daysInHijriMonth(selectedYear, selectedMonth, hijriOffsetDays)
    }
    var selectedDay by remember(initialDate) { mutableIntStateOf(initialDate.day.coerceIn(1, 30)) }
    val clampedDay = selectedDay.coerceIn(1, maxDayInMonth)

    val previewGregorian = remember(selectedYear, selectedMonth, clampedDay, hijriOffsetDays) {
        val g = HijriCalendarEngine.toGregorian(selectedYear, selectedMonth, clampedDay, hijriOffsetDays)
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(g)
    }
    val previewHijri = HijriDate(selectedYear, selectedMonth, clampedDay)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontFamily = titleFont,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "${previewHijri.day} ${previewHijri.monthName} ${previewHijri.year} H",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = titleFont,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bertepatan: $previewGregorian M",
                            fontSize = 11.sp,
                            fontFamily = monoFont,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Kontrol Tahun Hijriyah
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (selectedYear > 1350) selectedYear-- },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("hijri_picker_year_prev_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Tahun Hijriyah Sebelumnya")
                    }
                    Text(
                        text = "$selectedYear H",
                        fontWeight = FontWeight.Bold,
                        fontFamily = monoFont,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { if (selectedYear < 1600) selectedYear++ },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("hijri_picker_year_next_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Tahun Hijriyah Berikutnya")
                    }
                }

                // Pilihan 12 Bulan Hijriyah (Grid 3x4)
                Text(
                    text = "Pilih Bulan Hijriyah",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (rowIdx in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (colIdx in 0 until 3) {
                                val mIdx = rowIdx * 3 + colIdx + 1
                                val mName = HijriDate.monthNames[mIdx - 1]
                                val isSel = mIdx == selectedMonth
                                Surface(
                                    onClick = { selectedMonth = mIdx },
                                    shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 40.dp)
                                        .testTag("hijri_picker_month_$mIdx")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$mIdx. $mName",
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Pilihan Tanggal (1..29/30) dalam grid 6 kolom
                Text(
                    text = "Pilih Tanggal ($maxDayInMonth Hari)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    var dayCursor = 1
                    while (dayCursor <= maxDayInMonth) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until 6) {
                                if (dayCursor <= maxDayInMonth) {
                                    val d = dayCursor
                                    val isSel = d == clampedDay
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .defaultMinSize(minHeight = 40.dp)
                                            .clip(if (isTactical) CutCornerShape(4.dp) else CircleShape)
                                            .background(
                                                if (isSel) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            )
                                            .border(
                                                width = if (isSel) 1.5.dp else 0.5.dp,
                                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = if (isTactical) CutCornerShape(4.dp) else CircleShape
                                            )
                                            .clickable { selectedDay = d }
                                            .testTag("hijri_picker_day_$d")
                                    ) {
                                        Text(
                                            text = "$d",
                                            fontSize = 12.sp,
                                            fontFamily = monoFont,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    dayCursor++
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = buttonShape,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("hijri_picker_cancel_btn")
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = { onConfirm(HijriDate(selectedYear, selectedMonth, clampedDay)) },
                        shape = buttonShape,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("hijri_picker_confirm_btn")
                    ) {
                        Text("Pilih")
                    }
                }
            }
        }
    }
}

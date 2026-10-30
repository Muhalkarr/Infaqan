package com.example.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.accounting.JournalEntry
import com.example.core.calendar.DualCalendarDay
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.core.state.AmanahLedgerViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private enum class CalendarDayFilter {
    ALL,
    FASTING_AND_SPECIAL,
    WITH_TRANSACTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DualCalendarScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransactionWithDate: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    val todayCal = remember { Calendar.getInstance() }

    // State navigasi bulan/tahun Masehi aktif
    var activeYear by remember { mutableIntStateOf(todayCal.get(Calendar.YEAR)) }
    var activeMonth by remember { mutableIntStateOf(todayCal.get(Calendar.MONTH)) } // 0..11

    // State tanggal terpilih untuk inspeksi detail
    var selectedCalendarDate by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time)
    }

    var activeFilter by remember { mutableStateOf(CalendarDayFilter.ALL) }
    var showOffsetDialog by remember { mutableStateOf(false) }

    // Hitung grid hari dalam bulan aktif dengan penyesuaian offset Hijriah
    val daysInMonth by remember(activeYear, activeMonth, uiState.selectedHijriOffset) {
        derivedStateOf {
            HijriCalendarEngine.buildMonthDays(
                year = activeYear,
                monthIndex = activeMonth,
                manualOffsetDays = uiState.selectedHijriOffset
            )
        }
    }

    // Nama bulan Masehi & estimasi rentang bulan Hijriah
    val monthTitleMasehi = remember(activeYear, activeMonth) {
        "${HijriCalendarEngine.gregorianMonthNames.getOrElse(activeMonth) { "Bulan" }} $activeYear"
    }

    val hijriMonthSpan = remember(daysInMonth) {
        val currentMonthDays = daysInMonth.filter { it.isCurrentMonth }
        if (currentMonthDays.isEmpty()) ""
        else {
            val firstH = currentMonthDays.first().hijriDate
            val lastH = currentMonthDays.last().hijriDate
            if (firstH.month == lastH.month) {
                "${firstH.monthName} ${firstH.year} H"
            } else {
                "${firstH.monthName} - ${lastH.monthName} ${lastH.year} H"
            }
        }
    }

    // Map transaksi per hari (key: "yyyy-MM-dd")
    val dayKeyFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val transactionsByDateKey = remember(uiState.journalEntries) {
        uiState.journalEntries.groupBy { dayKeyFormatter.format(it.gregorianDate) }
    }

    // Transaksi dan ringkasan untuk tanggal terpilih
    val selectedDateKey = dayKeyFormatter.format(selectedCalendarDate)
    val entriesOnSelectedDate = transactionsByDateKey[selectedDateKey] ?: emptyList()
    val totalIncomeSelected = entriesOnSelectedDate
        .filter { it.transactionType == "INFLOW" || it.transactionType == "INCOME" }
        .sumOf { it.totalDebit }
    val totalExpenseSelected = entriesOnSelectedDate
        .filter { it.transactionType == "EXPENSE" }
        .sumOf { it.totalDebit }
    val totalInfaqSelected = entriesOnSelectedDate
        .filter { it.transactionType == "INFAQ_PAYOUT" || it.transactionType == "INFAQ_SEDEKAH" || it.transactionType == "ZAKAT" }
        .sumOf { it.totalDebit }

    // Hijri date untuk tanggal terpilih
    val selectedHijriDate = remember(selectedCalendarDate, uiState.selectedHijriOffset) {
        HijriCalendarEngine.fromGregorian(selectedCalendarDate, manualOffsetDays = uiState.selectedHijriOffset)
    }

    val selectedDayOfWeek = remember(selectedCalendarDate) {
        val c = Calendar.getInstance().apply { time = selectedCalendarDate }
        when (c.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Ahad (الأحد)"
            Calendar.MONDAY -> "Senin (الإثنين)"
            Calendar.TUESDAY -> "Selasa (الثلاثاء)"
            Calendar.WEDNESDAY -> "Rabu (الأربعاء)"
            Calendar.THURSDAY -> "Kamis (الخميس)"
            Calendar.FRIDAY -> "Jumat (الجمعة)"
            Calendar.SATURDAY -> "Sabtu (السبت)"
            else -> ""
        }
    }

    val selectedDateMasehiFormatted = remember(selectedCalendarDate) {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).format(selectedCalendarDate)
    }

    val selectedFastingInfo = remember(selectedCalendarDate, selectedHijriDate) {
        HijriCalendarEngine.getFastingTag(selectedCalendarDate, selectedHijriDate)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("dual_calendar_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kalender Hijriyah & Masehi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Sinkronisasi Fiqih, Ibadah & Arus Kas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("dual_calendar_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda"
                        )
                    }
                },
                actions = {
                    // Tombol Hari Ini
                    IconButton(
                        onClick = {
                            val now = Calendar.getInstance()
                            activeYear = now.get(Calendar.YEAR)
                            activeMonth = now.get(Calendar.MONTH)
                            selectedCalendarDate = now.time
                        },
                        modifier = Modifier.testTag("calendar_today_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Hari Ini",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Tombol Atur Offset Hilal
                    IconButton(
                        onClick = { showOffsetDialog = true },
                        modifier = Modifier.testTag("calendar_offset_tune_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Penyesuaian Hilal Hijriah",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // 1. Month Navigation Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("month_navigation_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (activeMonth == 0) {
                                        activeMonth = 11
                                        activeYear -= 1
                                    } else {
                                        activeMonth -= 1
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("calendar_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Bulan Sebelumnya",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = monthTitleMasehi,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (hijriMonthSpan.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = hijriMonthSpan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    if (activeMonth == 11) {
                                        activeMonth = 0
                                        activeYear += 1
                                    } else {
                                        activeMonth += 1
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("calendar_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Bulan Berikutnya",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Penanda Offset Hijriah jika aktif
                        if (uiState.selectedHijriOffset != 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "Offset Hilal: ${if (uiState.selectedHijriOffset > 0) "+${uiState.selectedHijriOffset}" else "${uiState.selectedHijriOffset}"} hari dari standar JDN",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Filter Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == CalendarDayFilter.ALL,
                        onClick = { activeFilter = CalendarDayFilter.ALL },
                        label = { Text("Semua Hari", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = activeFilter == CalendarDayFilter.FASTING_AND_SPECIAL,
                        onClick = { activeFilter = CalendarDayFilter.FASTING_AND_SPECIAL },
                        label = { Text("Hari Puasa & Utama", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Nightlight, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                    FilterChip(
                        selected = activeFilter == CalendarDayFilter.WITH_TRANSACTIONS,
                        onClick = { activeFilter = CalendarDayFilter.WITH_TRANSACTIONS },
                        label = { Text("Ada Transaksi", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }
            }

            // 3. Grid Kalender Bulanan
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calendar_grid_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Nama Hari (Ahad s/d Sabtu)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HijriCalendarEngine.dayNamesIndonesian.forEachIndexed { idx, dayName ->
                                val isJumah = idx == 5 // Jumat
                                val isSeninKamis = idx == 1 || idx == 4
                                Text(
                                    text = dayName.take(3),
                                    fontWeight = if (isJumah) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when {
                                        isJumah -> MaterialTheme.colorScheme.primary
                                        isSeninKamis -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Grid 7 Kolom
                        val chunkedDays = daysInMonth.chunked(7)
                        chunkedDays.forEach { weekDays ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weekDays.forEach { dayInfo ->
                                    val dayKey = dayKeyFormatter.format(dayInfo.gregorianDate)
                                    val isSelected = (dayKey == selectedDateKey)
                                    val dayEntries = transactionsByDateKey[dayKey] ?: emptyList()
                                    val hasTransactions = dayEntries.isNotEmpty()
                                    val isFasting = dayInfo.fastingTag != null
                                    val isSpecial = dayInfo.eventTag != null

                                    // Filter check
                                    val isDimmedByFilter = when (activeFilter) {
                                        CalendarDayFilter.ALL -> false
                                        CalendarDayFilter.FASTING_AND_SPECIAL -> !isFasting && !isSpecial
                                        CalendarDayFilter.WITH_TRANSACTIONS -> !hasTransactions
                                    }

                                    CalendarDayCell(
                                        dayInfo = dayInfo,
                                        isSelected = isSelected,
                                        isDimmed = isDimmedByFilter,
                                        hasTransactions = hasTransactions,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("day_cell_${dayInfo.gregorianDay}"),
                                        onClick = {
                                            selectedCalendarDate = dayInfo.gregorianDate
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Panel Detail Tanggal Terpilih & Transaksi
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_day_detail_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = selectedDateMasehiFormatted,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$selectedHijriDate • $selectedDayOfWeek",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            Button(
                                onClick = {
                                    onNavigateToAddTransactionWithDate(selectedCalendarDate.time)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("calendar_add_tx_on_date")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Catat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Banner Informasi Fiqih / Puasa Sunnah jika ada
                        if (selectedFastingInfo != null || selectedHijriDate.eventName != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        if (selectedFastingInfo != null) {
                                            Text(
                                                text = selectedFastingInfo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (selectedHijriDate.eventName != null && selectedHijriDate.eventName != selectedFastingInfo) {
                                            Text(
                                                text = selectedHijriDate.eventName ?: "",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Ringkasan Arus Kas Hari Terpilih
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DailyStatChip(
                                label = "Pemasukan",
                                amount = totalIncomeSelected,
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            DailyStatChip(
                                label = "Pengeluaran",
                                amount = totalExpenseSelected,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            DailyStatChip(
                                label = "Infaq/Zakat",
                                amount = totalInfaqSelected,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 5. Daftar Transaksi pada Tanggal Terpilih
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaksi Tanggal Ini (${entriesOnSelectedDate.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (entriesOnSelectedDate.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada transaksi tercatat pada tanggal ini",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { onNavigateToAddTransactionWithDate(selectedCalendarDate.time) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Catat Transaksi Sekarang", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(entriesOnSelectedDate, key = { it.id }) { entry ->
                    TransactionDayItemCard(entry = entry, nf = nf)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Dialog Pengaturan Offset Hilal Kemenag / Hisab
    if (showOffsetDialog) {
        var tempOffset by remember { mutableIntStateOf(uiState.selectedHijriOffset) }
        AlertDialog(
            onDismissRequest = { showOffsetDialog = false },
            title = {
                Text("Penyesuaian Kalender Hijriah", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Kalender Hijriah dihitung dengan hisab astronomis standar JDN. Jika hasil hisab berbeda 1 hari dengan ketetapan Rukyatul Hilal Kemenag RI atau hisab ormas Anda, silakan sesuaikan pergeseran hari di bawah ini:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Pergeseran: ${if (tempOffset > 0) "+$tempOffset" else "$tempOffset"} Hari",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Slider(
                        value = tempOffset.toFloat(),
                        onValueChange = { tempOffset = it.roundToInt() },
                        valueRange = -2f..2f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("-2 Hari", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text("Standar (0)", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text("+2 Hari", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateHijriOffset(tempOffset)
                        showOffsetDialog = false
                    }
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOffsetDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun CalendarDayCell(
    dayInfo: DualCalendarDay,
    isSelected: Boolean,
    isDimmed: Boolean,
    hasTransactions: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isCurrentMonth = dayInfo.isCurrentMonth
    val textColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
        dayInfo.isFriday -> MaterialTheme.colorScheme.primary
        dayInfo.dayOfWeek == Calendar.SUNDAY -> Color(0xFFEF4444)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val hijriColor = when {
        !isCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        dayInfo.hijriDate.isRamadan -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    dayInfo.isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                    dayInfo.fastingTag != null && isCurrentMonth -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = when {
                    isSelected -> 1.8.dp
                    dayInfo.isToday -> 1.dp
                    else -> 0.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    dayInfo.isToday -> MaterialTheme.colorScheme.secondary
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 3.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Angka Masehi
            Text(
                text = "${dayInfo.gregorianDay}",
                fontSize = 13.sp,
                fontWeight = if (dayInfo.isToday || isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isDimmed) textColor.copy(alpha = 0.3f) else textColor
            )

            // Angka Hijriah
            Text(
                text = "${dayInfo.hijriDate.day}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDimmed) hijriColor.copy(alpha = 0.25f) else hijriColor
            )

            // Indikator Titik Status (Puasa / Transaksi)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(6.dp)
            ) {
                if (dayInfo.fastingTag != null && isCurrentMonth) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
                if (hasTransactions) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyStatChip(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (amount > 0) "Rp ${nf.format(amount)}" else "Rp 0",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransactionDayItemCard(
    entry: JournalEntry,
    nf: NumberFormat,
    modifier: Modifier = Modifier
) {
    val isIncome = entry.transactionType == "INFLOW" || entry.transactionType == "INCOME"
    val isExpense = entry.transactionType == "EXPENSE"
    val isInfaq = entry.transactionType == "INFAQ_PAYOUT" || entry.transactionType == "INFAQ_SEDEKAH" || entry.transactionType == "ZAKAT"

    val badgeColor = when {
        isIncome -> Color(0xFF10B981)
        isExpense -> Color(0xFFEF4444)
        isInfaq -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    val typeLabel = when (entry.transactionType) {
        "INFLOW", "INCOME" -> "Pemasukan"
        "EXPENSE" -> "Pengeluaran"
        "INFAQ_PAYOUT", "INFAQ_SEDEKAH" -> "Infaq/Sedekah"
        "ZAKAT" -> "Zakat"
        "TRANSFER" -> "Transfer"
        else -> entry.transactionType
    }

    val timeFormatted = remember(entry.gregorianDate) {
        SimpleDateFormat("HH:mm", Locale("id", "ID")).format(entry.gregorianDate)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Jam $timeFormatted WIB",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.description.ifBlank { "Transaksi Kas Syariah" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${if (isIncome) "+Rp " else if (isExpense) "-Rp " else "Rp "}${nf.format(entry.totalDebit)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = badgeColor
            )
        }
    }
}

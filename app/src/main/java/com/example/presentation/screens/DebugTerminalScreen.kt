package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.debug.AppDebugLogger
import com.example.core.debug.DebugLogEntry
import com.example.core.debug.DebugLogLevel
import com.example.core.debug.GlobalCrashHandler
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugTerminalScreen(
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allLogs by AppDebugLogger.logsState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevelFilter by remember { mutableStateOf<DebugLogLevel?>(null) }
    var autoScrollEnabled by remember { mutableStateOf(true) }
    var showCrashDialog by remember { mutableStateOf(false) }
    var showSimulateCrashConfirm by remember { mutableStateOf(false) }
    var latestCrashReport by remember { mutableStateOf(GlobalCrashHandler.getLatestCrashReport(context)) }

    val listState = rememberLazyListState()

    // Filter logs based on search query and log level
    val filteredLogs = remember(allLogs, searchQuery, selectedLevelFilter) {
        allLogs.filter { log ->
            val matchLevel = selectedLevelFilter == null || log.level == selectedLevelFilter
            val matchQuery = searchQuery.isBlank() ||
                    log.message.contains(searchQuery, ignoreCase = true) ||
                    log.tag.contains(searchQuery, ignoreCase = true) ||
                    log.level.label.contains(searchQuery, ignoreCase = true) ||
                    (log.stackTrace?.contains(searchQuery, ignoreCase = true) == true)
            matchLevel && matchQuery
        }
    }

    // Auto scroll to bottom when new logs arrive if auto-scroll enabled
    LaunchedEffect(filteredLogs.size, autoScrollEnabled) {
        if (autoScrollEnabled && filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.size - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
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
                        modifier = Modifier.testTag("debug_terminal_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Terminal Log & Diagnostik",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Live Debug Console (${filteredLogs.size} logs)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Copy all logs
                    IconButton(
                        onClick = {
                            val logsText = AppDebugLogger.getFormattedLogsText()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Amanah Terminal Logs", logsText))
                            Toast.makeText(context, "Log terminal berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("debug_terminal_copy_all")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin Semua Log",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Share logs
                    IconButton(
                        onClick = {
                            val logsText = AppDebugLogger.getFormattedLogsText()
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, logsText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Log Terminal"))
                        },
                        modifier = Modifier.testTag("debug_terminal_share")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan Log",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Clear logs
                    IconButton(
                        onClick = {
                            AppDebugLogger.clearLogs()
                            Toast.makeText(context, "Buffer log telah dibersihkan", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("debug_terminal_clear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Bersihkan Log",
                            tint = ExpenseCoral
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Alert Banner: Laporan Crash Terakhir (jika ada)
            if (!latestCrashReport.isNullOrBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .testTag("crash_detected_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Crash Alert",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Laporan Crash Terakhir Ditemukan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aplikasi mendeteksi bahwa sesi sebelumnya mengalami crash (galat fatal). Anda dapat membaca berkas laporan kerusakan untuk mengetahui lokasi persis penyebab error.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showCrashDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("view_crash_report_button")
                            ) {
                                Text("Baca Detail Crash", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Crash Report", latestCrashReport))
                                    Toast.makeText(context, "Laporan crash disalin!", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Salin Laporan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                            TextButton(
                                onClick = {
                                    GlobalCrashHandler.clearLatestCrashReport(context)
                                    latestCrashReport = null
                                    Toast.makeText(context, "Laporan crash dibersihkan.", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Hapus", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // 2. Search & Controls Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("terminal_search_input"),
                        placeholder = {
                            Text(
                                "Cari teks log, tag, atau error...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus Pencarian",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter chips & Auto scroll switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Level Filter Chips
                        FilterChip(
                            selected = selectedLevelFilter == null,
                            onClick = { selectedLevelFilter = null },
                            label = { Text("SEMUA (${allLogs.size})", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        DebugLogLevel.values().forEach { level ->
                            val count = allLogs.count { it.level == level }
                            val chipColor = when (level) {
                                DebugLogLevel.VERBOSE -> MaterialTheme.colorScheme.onSurfaceVariant
                                DebugLogLevel.DEBUG -> Color(0xFF0284C7)
                                DebugLogLevel.INFO -> MaterialTheme.colorScheme.primary
                                DebugLogLevel.WARN -> MaterialTheme.colorScheme.secondary
                                DebugLogLevel.ERROR -> ExpenseCoral
                                DebugLogLevel.CRASH -> Color(0xFFEF4444)
                            }
                            FilterChip(
                                selected = selectedLevelFilter == level,
                                onClick = {
                                    selectedLevelFilter = if (selectedLevelFilter == level) null else level
                                },
                                label = {
                                    Text(
                                        "${level.label} ($count)",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (selectedLevelFilter == level) Color.White else chipColor
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipColor,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = chipColor
                                )
                            )
                        }

                        // Auto-scroll toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Auto-Scroll",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = autoScrollEnabled,
                                onCheckedChange = { autoScrollEnabled = it },
                                modifier = Modifier.size(width = 36.dp, height = 24.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            // Diagnostic Trigger Row (for manual testing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS: ${if (filteredLogs.isEmpty()) "MENUNGGU LOG" else "STREAMING AKTIF"}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (filteredLogs.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Test log button
                    OutlinedButton(
                        onClick = {
                            AppDebugLogger.i("TestDiagnostic", "Pengujian log informasi sukses.")
                            AppDebugLogger.w("TestDiagnostic", "Peringatan uji coba: Parameter memori mendekati batas.")
                            AppDebugLogger.e("TestDiagnostic", "Simulasi uji coba galat (Error test)", RuntimeException("Simulasi error verifikasi"))
                            Toast.makeText(context, "3 Log pengujian dicatat!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Tes Catat Log", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    // Test crash simulation button
                    OutlinedButton(
                        onClick = { showSimulateCrashConfirm = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ExpenseCoral
                        )
                    ) {
                        Text("Tes Crash", fontSize = 10.sp, color = ExpenseCoral)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // 3. Main Terminal Log Output
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (filteredLogs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Tidak ada log yang cocok dengan kata kunci '$searchQuery'" else "Belum ada rekaman log terminal",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aktivitas transaksi, database, dan galat sistem akan otomatis ditampilkan di sini.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                            TerminalLogRow(log = log)
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Baca Laporan Crash Terakhir
    if (showCrashDialog && !latestCrashReport.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { showCrashDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Detail Laporan Crash",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Stack trace & aktivitas sistem sebelum terjadi crash:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = latestCrashReport ?: "",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.error,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Crash Report", latestCrashReport))
                        Toast.makeText(context, "Laporan crash disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Salin Laporan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCrashDialog = false }) {
                    Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Modal Konfirmasi: Tes Simulasi Crash
    if (showSimulateCrashConfirm) {
        AlertDialog(
            onDismissRequest = { showSimulateCrashConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Simulasi Crash Pengujian", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Tindakan ini akan sengaja memicu Unhandled Exception fatal untuk memverifikasi bahwa GlobalCrashHandler berhasil menangkap stack trace, mencatat ke memori internal ponsel, dan dapat dibaca kembali saat aplikasi dibuka lagi.\n\nAplikasi akan menutup dan saat dibuka kembali banner crash report akan muncul.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSimulateCrashConfirm = false
                        GlobalCrashHandler.simulateTestCrash()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Picu Crash Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulateCrashConfirm = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun TerminalLogRow(log: DebugLogEntry) {
    var isExpanded by remember { mutableStateOf(false) }

    val levelColor = when (log.level) {
        DebugLogLevel.VERBOSE -> MaterialTheme.colorScheme.onSurfaceVariant
        DebugLogLevel.DEBUG -> Color(0xFF0284C7)
        DebugLogLevel.INFO -> MaterialTheme.colorScheme.primary
        DebugLogLevel.WARN -> MaterialTheme.colorScheme.secondary
        DebugLogLevel.ERROR -> ExpenseCoral
        DebugLogLevel.CRASH -> Color(0xFFEF4444)
    }

    val errorRowBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (log.level == DebugLogLevel.ERROR || log.level == DebugLogLevel.CRASH) errorRowBg else Color.Transparent)
            .clickable(enabled = log.stackTrace != null) { isExpanded = !isExpanded }
            .padding(vertical = 3.dp, horizontal = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Time
            Text(
                text = log.timeFormatted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.width(76.dp)
            )

            // Level Tag
            Text(
                text = "[${log.level.label}]",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = levelColor,
                modifier = Modifier.width(44.dp)
            )

            // Tag
            Text(
                text = "${log.tag}: ",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF7C3AED),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(90.dp)
            )

            // Message
            Text(
                text = log.message,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = if (log.level == DebugLogLevel.ERROR || log.level == DebugLogLevel.CRASH) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                lineHeight = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // Expandable Stack Trace (jika ada galat)
        if (log.stackTrace != null) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 76.dp, top = 4.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        .padding(6.dp)
                ) {
                    Text(
                        text = log.stackTrace,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 12.sp
                    )
                }
            }

            if (!isExpanded) {
                Text(
                    text = "▶ Ketuk untuk melihat stack trace galat",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 76.dp, top = 2.dp)
                )
            }
        }
    }
}

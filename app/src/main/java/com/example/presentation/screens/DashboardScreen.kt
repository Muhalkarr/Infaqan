package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.example.Screen
import com.example.presentation.components.AppNavigationDrawerContent
import com.example.presentation.components.BudgetCategoryAllocationChart
import com.example.presentation.components.DashboardRezekiNonTunaiWidget
import com.example.presentation.components.TunaikanInfaqSyukurDialog
import com.example.presentation.components.EditRezekiDialog
import com.example.presentation.components.DualCalendarCard
import com.example.ui.theme.TacticalTelemetryRibbon
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.isTactical
import com.example.presentation.screens.tactical.TacticalCyberDashboardView
import com.example.core.rezeki.RezekiNonTunaiRecord
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.JournalEntry
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.core.state.AmanahLedgerUiState
import com.example.core.state.AmanahLedgerViewModel
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Locale
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.White12
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.util.Date
import java.io.File
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AmanahLedgerViewModel,
    onOpenDrawer: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    onNavigateToAddExpense: () -> Unit = onNavigateToAddTransaction,
    onNavigateToAddIncome: () -> Unit = onNavigateToAddTransaction,
    onNavigateToRezeki: () -> Unit = onNavigateToAddTransaction,
    onEditTransaction: (String) -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToHaulNisab: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToMonthlyReport: () -> Unit,
    onNavigateToVaultHistory: () -> Unit,
    onNavigateToSedekahSubuh: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToCentralSettings: () -> Unit,
    onNavigateToMultiWallet: () -> Unit = {},
    onNavigateToIbadahGoals: () -> Unit = {},
    onNavigateToZakatHub: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToQardh: () -> Unit = {},
    onNavigateToAmilDirectory: () -> Unit = {},
    onNavigateToFaraidh: () -> Unit = {},
    onNavigateToExportReport: () -> Unit = {},
    onNavigateToIslamicGrounding: () -> Unit = {},
    onNavigateToDualCalendar: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val isDataBackedUp = (syncState.lastSyncedTimeMillis > 0L) || (state.lastBackupTimeMillis > 0L)
    val latestAlert by viewModel.appStateNotifier.latestAlert.collectAsState()
    val hijriNow = remember { HijriCalendarEngine.fromGregorian() }
    val masehiNow = remember { SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(Date()) }
    val isFriday = remember { HijriCalendarEngine.isFriday() }
    var showDisburseDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedEntryForDetail by remember { mutableStateOf<JournalEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }
    var selectedRezekiForInfaq by remember { mutableStateOf<RezekiNonTunaiRecord?>(null) }
    var editingRezekiRecord by remember { mutableStateOf<RezekiNonTunaiRecord?>(null) }
    var rezekiToDelete by remember { mutableStateOf<RezekiNonTunaiRecord?>(null) }
    var dismissBackupNudge by remember { mutableStateOf(false) }
    var transactionDisplayLimit by remember { mutableStateOf(30) }
    var journalSearchQuery by remember { mutableStateOf("") }
    var dashboardTab by remember { mutableStateOf(0) }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.dashboardScrollToTopEvent.collect {
            listState.animateScrollToItem(0)
        }
    }

    if (state.themeMode.isTactical) {
        TacticalCyberDashboardView(
            state = state,
            viewModel = viewModel,
            listState = listState,
            onOpenDrawer = onOpenDrawer,
            onOpenThemeDialog = { showThemeDialog = true },
            onNavigateToAddTransaction = onNavigateToAddTransaction,
            onNavigateToRezeki = onNavigateToRezeki,
            onNavigateToBudget = onNavigateToBudget,
            onNavigateToRules = onNavigateToRules,
            onNavigateToHaulNisab = onNavigateToHaulNisab,
            onNavigateToVaultHistory = onNavigateToVaultHistory,
            onNavigateToFaraidh = onNavigateToFaraidh,
            onSelectEntryForDetail = { selectedEntryForDetail = it },
            onEditEntry = { onEditTransaction(it) },
            onDeleteEntry = { entryToDelete = it },
            onEditRezeki = { editingRezekiRecord = it },
            onDeleteRezeki = { rezekiToDelete = it }
        )
    } else {
        Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("dashboard_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Infaqan Syariah",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$hijriNow ${if (isFriday) "• Hari Jumat Berkah" else ""}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // Mode Switcher Action Chip (Ringkas vs Lengkap)
                    Surface(
                        onClick = { viewModel.toggleAppDisplayMode() },
                        shape = RoundedCornerShape(18.dp),
                        color = if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .testTag("dashboard_mode_switch_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE)
                                    Icons.Default.AutoAwesome
                                else
                                    Icons.Default.Nightlight,
                                contentDescription = null,
                                tint = if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) "Ringkas" else "Lengkap",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Quick Balance Privacy Mask Toggle (Mata Saldo)
                    IconButton(
                        onClick = { viewModel.toggleBalancePrivacy() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("dashboard_privacy_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (state.securityConfig.isMaskBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Sembunyikan Saldo",
                            tint = if (state.securityConfig.isMaskBalance) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Theme Selector Button
                    Surface(
                        onClick = { showThemeDialog = true },
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .padding(end = 8.dp, start = 2.dp)
                            .testTag("dashboard_theme_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Pilih Tema",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "THEME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (state.themeMode.isTactical) {
                item {
                    TacticalTelemetryRibbon(isLight = state.themeMode == AppThemeMode.TACTICAL_LIGHT)
                }
            }

            // 1. PRIORITAS TERTINGGI: App State Notifier Live Alert Banner (Peringatan & Status Sistem)
            if (latestAlert != null) {
                item {
                    val alert = latestAlert!!
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("app_state_notifier_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = when (alert.severity) {
                                com.example.core.state.NotificationSeverity.SUCCESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                com.example.core.state.NotificationSeverity.ALERT -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                com.example.core.state.NotificationSeverity.WARNING -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (alert.severity) {
                                com.example.core.state.NotificationSeverity.SUCCESS -> MaterialTheme.colorScheme.primary
                                com.example.core.state.NotificationSeverity.ALERT -> MaterialTheme.colorScheme.error
                                com.example.core.state.NotificationSeverity.WARNING -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = alert.message,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.appStateNotifier.dismissLatestAlert() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("✕", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) {
                // ==========================================================
                // TAMPILAN MODE SEDERHANA (RAMAH PEMULA & ARUS KAS HARIAN)
                // ==========================================================

                // A. KARTU SALDO KAS UTAMA BERSIH & RAMAH
                item {
                    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
                    val totalIncome = remember(state.journalEntries) {
                        state.journalEntries.filter { it.transactionType == "INFLOW" || it.transactionType == "INCOME" }.sumOf { it.primaryAmount }
                    }
                    val totalExpense = remember(state.journalEntries) {
                        state.journalEntries.filter { it.transactionType == "EXPENSE" }.sumOf { it.primaryAmount }
                    }
                    val currentBalance = state.totalWalletBalance

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simple_hero_balance_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Saldo Kas Anda",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (state.securityConfig.isMaskBalance) "Rp ••••••••" else "Rp ${nf.format(currentBalance)}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.clickable { onNavigateToVaultHistory() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Savings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (state.securityConfig.isMaskBalance) "Infaq: Rp •••" else "Infaq: Rp ${nf.format(state.virtualInfaqVaultBalance)}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Ringkasan Pemasukan & Pengeluaran Berdampingan
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Pemasukan",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Pemasukan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            if (state.securityConfig.isMaskBalance) "Rp •••" else "Rp ${nf.format(totalIncome)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFC62828).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Pengeluaran",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Pengeluaran", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            if (state.securityConfig.isMaskBalance) "Rp •••" else "Rp ${nf.format(totalExpense)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // B. 3 TOMBOL AKSI CEPAT RAMAH PEMULA
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Catat Pengeluaran
                        Button(
                            onClick = onNavigateToAddExpense,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("simple_action_expense")
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Keluar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Catat Pemasukan
                        Button(
                            onClick = onNavigateToAddIncome,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("simple_action_income")
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Masuk", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Sedekah Subuh / Infaq
                        Button(
                            onClick = onNavigateToSedekahSubuh,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("simple_action_sedekah")
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sedekah", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // C. WIDGET KESIAPAN IBADAH & REZEKI (RINGKAS & TIDAK MENGINTIMIDASI)
                if (state.featureVisibilityConfig.isSyariahModuleEnabled && state.featureVisibilityConfig.showSedekahSubuh) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("simple_ibadah_glance_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WbSunny,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Rutinitas Sedekah Fajar",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (state.sedekahSubuhState.currentStreak > 0)
                                                "🔥 Streak ${state.sedekahSubuhState.currentStreak} hari aktif"
                                            else
                                                "Mulai hari berkah dengan sedekah",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.recordSedekahSubuh(amount = 2000.0) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                                ) {
                                    Text("+ Rp 2.000", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                // 2. PRIORITAS UTAMA (HERO): Virtual Infaq Vault & Total Assets (Brankas Amanah & Saldo Kas)
                if (state.featureVisibilityConfig.isSyariahModuleEnabled && state.featureVisibilityConfig.showInfaqVault) {
                    item {
                        VirtualInfaqVaultCard(
                            vaultBalance = state.virtualInfaqVaultBalance,
                            distributionCount = state.infaqDistributions.size,
                            isMasked = state.securityConfig.isMaskBalance,
                            onToggleMask = { viewModel.toggleBalancePrivacy() },
                            onDisburseClick = { showDisburseDialog = true },
                            onHistoryClick = onNavigateToVaultHistory
                        )
                    }
                }
            }

            // 3. KONTROL TAMPILAN: Segmented View Mode (Mengatasi Kepadatan Informasi / Progressive Disclosure)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_view_mode_selector"),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dashboardTab = 0 }
                                .testTag("dashboard_tab_summary"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (dashboardTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (dashboardTab == 0) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = if (dashboardTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ringkasan Utama",
                                    fontSize = 12.sp,
                                    fontWeight = if (dashboardTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (dashboardTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dashboardTab = 1 }
                                .testTag("dashboard_tab_analytics"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (dashboardTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (dashboardTab == 1) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = if (dashboardTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Analisis & Anggaran",
                                    fontSize = 12.sp,
                                    fontWeight = if (dashboardTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (dashboardTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (state.overBudgetCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (dashboardTab == 0 || state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) {
                if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.COMPREHENSIVE) {
                    // TAB 0: RINGKASAN UTAMA & OPERASIONAL HARIAN
                    // 4. Dual Calendar Gregorian & Hijriah Header
                    item {
                        DualCalendarCard(
                            selectedHijriOffset = state.selectedHijriOffset,
                            onOpenFullCalendar = onNavigateToDualCalendar
                        )
                    }

                // 5. Sedekah Subuh Streak Visualizer Widget (Habit Tracker Fajar)
                item {
                    DashboardSedekahSubuhWidget(
                        subuhState = state.sedekahSubuhState,
                        onClick = onNavigateToSedekahSubuh,
                        onQuickGive = { amount ->
                            viewModel.recordSedekahSubuh(amount = amount)
                        }
                    )
                }

                // 5B. Rezeki Non-Tunai & Hadiah Widget
                item {
                    DashboardRezekiNonTunaiWidget(
                        rezekiList = state.rezekiNonTunaiList,
                        onAddRezekiClick = onNavigateToRezeki,
                        onTunaikanInfaqClick = { record ->
                            selectedRezekiForInfaq = record
                        },
                        onEditRezekiClick = { record ->
                            editingRezekiRecord = record
                        },
                        onDeleteRezekiClick = { record ->
                            rezekiToDelete = record
                        }
                    )
                }

                // 6. Pintasan Cepat Fitur Kunci (Grid Aksi Cepat Dinamis Berdasarkan Fitur Aktif)
                val showRecurringShortcut = state.featureVisibilityConfig.isOperationalModuleEnabled && state.featureVisibilityConfig.showRecurringTransactions
                val showWalletShortcut = state.featureVisibilityConfig.isOperationalModuleEnabled && state.featureVisibilityConfig.showMultiWallet
                val showReportShortcut = state.featureVisibilityConfig.isPlanningModuleEnabled && state.featureVisibilityConfig.showMonthlyReport
                val showZakatShortcut = state.featureVisibilityConfig.isSyariahModuleEnabled && state.featureVisibilityConfig.showZakatHub

                if (showRecurringShortcut || showWalletShortcut || showReportShortcut || showZakatShortcut) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (showRecurringShortcut || showWalletShortcut) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (showRecurringShortcut) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onNavigateToRecurring() }
                                                .testTag("shortcut_recurring_card"),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AutoMode,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Otomasi Rutin",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = if (state.dueRecurringCount > 0) "${state.dueRecurringCount} Jatuh Tempo" else "${state.recurringTransactions.size} Terjadwal",
                                                        fontSize = 10.sp,
                                                        color = if (state.dueRecurringCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (showWalletShortcut) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onNavigateToMultiWallet() }
                                                .testTag("shortcut_multi_wallet_card"),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountBalanceWallet,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Multi-Wallet",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "${state.wallets.size} Akun Kas",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (showReportShortcut || showZakatShortcut) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (showReportShortcut) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onNavigateToMonthlyReport() }
                                                .testTag("shortcut_report_card"),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PictureAsPdf,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Laporan PDF",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Unduh & Bagikan",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (showZakatShortcut) {
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onNavigateToZakatHub() }
                                                .testTag("shortcut_zakat_hub_card"),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.VolunteerActivism,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Zakat Hub",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "8 Asnaf Syariah",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }

            // 12. PRIORITAS OPERASIONAL: Double-Entry Journal History Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE)
                            "Riwayat Transaksi Terbaru"
                        else
                            "Buku Besar Terkini (Double-Entry Log)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Balanced",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE)
                                "Arus Kas Rapi"
                            else
                                "∑Debit = ∑Kredit",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 13. PRIORITAS OPERASIONAL: List of Journal Entries (Searchable + Windowed / Tampilkan Semua)
            if (state.journalEntries.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = journalSearchQuery,
                        onValueChange = { journalSearchQuery = it },
                        placeholder = {
                            Text(
                                text = "Cari catatan transaksi lama / nominal...",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (journalSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { journalSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_journal_search_input")
                    )
                }
            }

            val filteredDashboardEntries = if (journalSearchQuery.isBlank()) {
                state.journalEntries
            } else {
                state.journalEntries.filter {
                    it.description.contains(journalSearchQuery, ignoreCase = true) ||
                        it.transactionType.contains(journalSearchQuery, ignoreCase = true) ||
                        it.primaryAmount.toLong().toString().contains(journalSearchQuery.trim()) ||
                        it.totalDebit.toLong().toString().contains(journalSearchQuery.trim())
                }
            }

            if (filteredDashboardEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (journalSearchQuery.isBlank()) "Belum ada transaksi tercatat" else "Tidak ada transaksi yang cocok dengan '$journalSearchQuery'",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                val totalEntries = filteredDashboardEntries.size
                val entriesToShow = filteredDashboardEntries.take(transactionDisplayLimit)

                items(entriesToShow, key = { it.id }) { entry ->
                    JournalEntryItemCard(
                        entry = entry,
                        onClick = { selectedEntryForDetail = entry },
                        onEditClick = { onEditTransaction(entry.id) },
                        onDeleteClick = { entryToDelete = entry }
                    )
                }

                if (totalEntries > 30) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (transactionDisplayLimit < totalEntries) {
                                Button(
                                    onClick = { transactionDisplayLimit += 30 },
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("load_more_transactions_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Muat +30 (${entriesToShow.size}/$totalEntries)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Button(
                                    onClick = { transactionDisplayLimit = totalEntries },
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("show_all_transactions_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Tampilkan Semua ($totalEntries)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            if (transactionDisplayLimit > 30) {
                                OutlinedButton(
                                    onClick = { transactionDisplayLimit = 30 },
                                    modifier = Modifier
                                        .weight(if (transactionDisplayLimit < totalEntries) 0.7f else 1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("collapse_transactions_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "Ringkas (30)",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.appDisplayMode == com.example.core.datastore.AppDisplayMode.COMPREHENSIVE) {
                // 14. PRIORITAS PALING BAWAH (EDUKASI & PANDUAN): Interactive Guide Hero Banner Card
                item {
                    Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToGuide() }
                        .testTag("dashboard_interactive_guide_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Panduan Interaktif Aplikasi",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "PANDUAN LENGKAP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Panduan interaktif seluruh fitur, simulasi infaq kasab, fiqih muamalah, dan tips syariah.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 15. PRIORITAS PALING BAWAH (REFERENSI FATWA): Search Grounding & Islamic Knowledge Hub Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToIslamicGrounding() }
                        .testTag("dashboard_islamic_grounding_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Rujukan Fatwa DSN-MUI",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ensiklopedia Fatwa & Zakat",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "DSN-MUI",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Rujukan Fiqih & Bebas Riba",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Pencarian fatwa fiqih muamalah, hisab zakat maal/profesi, dan uji bebas riba.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            } else {
                // Banner Rekomendasi Fitur Lengkap Syariah (Hanya Mode Sederhana)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simple_mode_pro_features_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Fitur Akuntansi Syariah Lengkap",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Kalkulator Waris (Faraidh), Pinjaman Qardh, Hisab Zakat Maal & Multi-Kantong.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.setAppDisplayMode(com.example.core.datastore.AppDisplayMode.COMPREHENSIVE) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                            ) {
                                Text("Mode Lengkap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 16. PRIORITAS PALING BAWAH (PEMELIHARAAN SISTEM): Periodic Local Room Database Backup Nudge
            if (!isDataBackedUp && !state.isBackupNudgeDismissed && !dismissBackupNudge) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("database_backup_nudge_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Cadangan Database",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Amankan Data Finansial Syariah",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Data tersimpan di database Room lokal. Cadangkan berkas secara berkala untuk proteksi optimal.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = onNavigateToBackupRestore,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            contentColor = MaterialTheme.colorScheme.onSecondary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("backup_now_button")
                                    ) {
                                        Text(
                                            text = "Cadangkan Sekarang",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            dismissBackupNudge = true
                                            viewModel.dismissBackupNudgePermanently()
                                        },
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Text(
                                            text = "Nanti",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            } else {
                // TAB 1: ANALISIS & ANGGARAN (MENGURANGI KEPADATAN INFORMASI)
                // Over-Budget Alert Visual Indicator Banner (Peringatan Melebihi Batas Pagu)
                if (state.overBudgetCount > 0) {
                    item {
                        DashboardOverBudgetAlertBanner(
                            overBudgetCount = state.overBudgetCount,
                            onClick = onNavigateToBudget
                        )
                    }
                }

                // Metrik Finansial & Spiritual: SLI & Status Nisab 85g Emas (Row of 2 cards)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Spiritual Liquidity Index Card
                        val sliAnalysis = state.infaqSliAnalysis
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToAnalytics() }
                                .testTag("sli_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Spiritual Liquidity (SLI)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForwardIos,
                                        contentDescription = "Buka Audit SLI",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${String.format(Locale("id", "ID"), "%.1f", state.spiritualLiquidityIndex)}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${String.format(Locale("id", "ID"), "%.1f", sliAnalysis.infaqToTotalIncomePercent)}% dr Pendapatan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Basis konsumsi • Ketuk audit",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Nisab & Haul Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToHaulNisab() }
                                .testTag("nisab_card"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (state.isNisabReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Status Nisab (85g)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (state.isNisabReached) "Nisab Tercapai" else "Dibawah Nisab",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isNisabReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (state.securityConfig.isMaskBalance) "Aset: Rp ••••••••" else "Aset: Rp ${formatRupiah(state.totalAssets)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Pemisahan Anggaran (Budget Allocation Overview Card)
                item {
                    DashboardBudgetAllocationCard(
                        state = state,
                        onClick = onNavigateToBudget
                    )
                }

                // Sharia Budget Category Allocation Chart
                item {
                    BudgetCategoryAllocationChart(
                        budgetCategories = state.budgets,
                        journalEntries = state.journalEntries,
                        currencySymbol = state.primaryCurrencySymbol,
                        onNavigateToBudget = onNavigateToBudget
                    )
                }

                // Status Anggaran per Kategori (Visual Progress Bars)
                item {
                    DashboardCategoryBudgetsSection(
                        state = state,
                        onManageClick = onNavigateToBudget
                    )
                }

                // Ringkasan Kasab vs Non-Kasab
                item {
                    IncomeBreakdownCard(
                        incomeKasab = state.totalIncomeKasab,
                        incomeNonKasab = state.totalIncomeNonKasab
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    }
    }

    // Dialogs shared across both Tactical and Standard dashboard modes
    if (selectedEntryForDetail != null) {
        val entry = selectedEntryForDetail!!
        TransactionDetailDialog(
            entry = entry,
            onDismiss = { selectedEntryForDetail = null },
            onEdit = {
                selectedEntryForDetail = null
                onEditTransaction(entry.id)
            },
            onDelete = {
                val target = entry
                selectedEntryForDetail = null
                entryToDelete = target
            }
        )
    }

    // Delete Confirmation Dialog
    if (entryToDelete != null) {
        val target = entryToDelete!!
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Transaksi Jurnal?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus transaksi '${target.description}'? Buku besar akan memulihkan saldo akun kas/bank dan membatalkan alokasi infaq terkait.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "⚠️ Saldo dan laporan keuangan akan diperbarui secara otomatis.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = target.id
                        entryToDelete = null
                        viewModel.deleteJournalEntry(id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("dashboard_confirm_delete_button")
                ) {
                    Text("Ya, Hapus Transaksi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { entryToDelete = null },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("dashboard_cancel_delete_button")
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            modifier = Modifier.testTag("dashboard_delete_dialog")
        )
    }

    if (showDisburseDialog) {
        DisburseInfaqDialog(
            vaultBalance = state.virtualInfaqVaultBalance,
            wallets = state.wallets,
            getWalletBalance = { state.getWalletBalance(it) },
            onDismiss = { showDisburseDialog = false },
            onConfirmWithDetails = { amount, recipient, asnaf, sourceAcc, program, notes ->
                viewModel.disburseInfaqWithDetails(
                    amount = amount,
                    fromAccountId = sourceAcc,
                    recipientName = recipient,
                    asnafCategory = asnaf,
                    programName = program,
                    notes = notes
                )
                showDisburseDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeSwitcherDialog(
            themeMode = state.themeMode,
            isDarkMode = state.isDarkMode,
            isHighContrast = state.isHighContrast,
            onDismiss = { showThemeDialog = false },
            onSelectTheme = { mode ->
                viewModel.setThemeMode(mode)
            },
            onToggleHighContrast = {
                viewModel.toggleHighContrast()
            }
        )
    }

    selectedRezekiForInfaq?.let { record ->
        TunaikanInfaqSyukurDialog(
            record = record,
            wallets = state.wallets,
            getWalletBalance = { state.getWalletBalance(it) },
            onDismiss = { selectedRezekiForInfaq = null },
            onConfirm = { walletId, amount, note, isAllocateToVault ->
                viewModel.tunaikanInfaqSyukur(
                    rezekiId = record.id,
                    walletId = walletId,
                    infaqAmount = amount,
                    note = note,
                    isAllocateToVault = isAllocateToVault
                )
                selectedRezekiForInfaq = null
            }
        )
    }

    editingRezekiRecord?.let { record ->
        EditRezekiDialog(
            record = record,
            onDismiss = { editingRezekiRecord = null },
            onSave = { updatedTitle, updatedGiver, updatedCategory, updatedEstimatedValue, updatedNotes ->
                viewModel.updateRezekiNonTunai(
                    id = record.id,
                    title = updatedTitle,
                    giverName = updatedGiver,
                    category = updatedCategory,
                    estimatedValue = updatedEstimatedValue,
                    notes = updatedNotes
                )
                editingRezekiRecord = null
            },
            onDelete = {
                val target = record
                editingRezekiRecord = null
                rezekiToDelete = target
            }
        )
    }

    rezekiToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { rezekiToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Catatan Rezeki?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus catatan rezeki non-tunai '${record.title}' senilai Rp ${formatRupiah(record.estimatedValue)}?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    if (record.isPaidInfaq && record.paidJournalEntryId != null) {
                        Text(
                            text = if (record.isVaultAllocated) {
                                "⚠️ Rezeki ini memiliki alokasi Infaq Vault. Entri jurnal komitmen vault terkait juga akan dihapus dan dinormalkan kembali."
                            } else {
                                "⚠️ Rezeki ini memiliki pencatatan pengeluaran infaq tunai terkait yang akan dihapus dari buku besar."
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = record.id
                        rezekiToDelete = null
                        viewModel.deleteRezekiNonTunai(id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("dashboard_confirm_delete_rezeki_button")
                ) {
                    Text("Ya, Hapus Rezeki", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { rezekiToDelete = null },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("dashboard_cancel_delete_rezeki_button")
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            modifier = Modifier.testTag("dashboard_delete_rezeki_dialog")
        )
    }
}

@Composable
fun ThemeSwitcherDialog(
    themeMode: com.example.ui.theme.AppThemeMode,
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    onDismiss: () -> Unit,
    onSelectTheme: (com.example.ui.theme.AppThemeMode) -> Unit,
    onToggleHighContrast: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tema & Aksesibilitas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Pilih preferensi visual tampilan yang nyaman untuk mata Anda:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 0: Ikuti Sistem (Otomatis)
                ThemeOptionCard(
                    title = "📱 Ikuti Sistem Ponsel (Otomatis)",
                    description = "Menyesuaikan otomatis dengan mode cerah atau gelap pada pengaturan ponsel Anda",
                    isSelected = themeMode == com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM) }
                )

                // Option 1A: Protokol Taktis (Gelap // Cyber Terminal)
                ThemeOptionCard(
                    title = "⚡ Protokol Taktis (Gelap // Cyber Terminal)",
                    description = "Retro-futuristik sci-fi HUD dengan aksen phosphor cyber green & dark obsidian",
                    isSelected = themeMode == com.example.ui.theme.AppThemeMode.TACTICAL_CYBER,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.TACTICAL_CYBER) }
                )

                // Option 1B: Protokol Taktis (Terang // Clean Lab)
                ThemeOptionCard(
                    title = "🔬 Protokol Taktis (Terang // Clean Lab)",
                    description = "Cleanroom Lab cerah beraksen tactical emerald, wireframe borders & amber HUD",
                    isSelected = themeMode == com.example.ui.theme.AppThemeMode.TACTICAL_LIGHT,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.TACTICAL_LIGHT) }
                )

                // Option 2: Elegant Dark
                ThemeOptionCard(
                    title = "🌙 Elegant Dark",
                    description = "Nuansa obsidian & emerald glow beraksen emas",
                    isSelected = themeMode == com.example.ui.theme.AppThemeMode.ELEGANT_DARK,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.ELEGANT_DARK) }
                )

                // Option 2: Light Mode
                ThemeOptionCard(
                    title = "☀️ Light Mode",
                    description = "Latar terang bersih, ramah saat siang hari",
                    isSelected = themeMode == com.example.ui.theme.AppThemeMode.LIGHT_MODE,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.LIGHT_MODE) }
                )

                // Option 3: High Contrast Light Mode
                ThemeOptionCard(
                    title = "👁️ High-Contrast Light (Aksesibilitas)",
                    description = "Teks hitam pekat & garis tegas untuk visibilitas optimal",
                    isSelected = themeMode == com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT) }
                )

                // Option 4: High Contrast Dark Mode
                ThemeOptionCard(
                    title = "🖤 High-Contrast Dark",
                    description = "Hitam OLED murni dengan kontras teks maksimal",
                    isSelected = themeMode == com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = if (themeMode.isTactical) CutCornerShape(4.dp) else ButtonDefaults.shape,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = if (themeMode.isTactical) CutCornerShape(12.dp) else RoundedCornerShape(18.dp)
    )
}

@Composable
fun ThemeOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val currentTheme = LocalAppThemeMode.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = if (currentTheme.isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = if (currentTheme.isTactical) Icons.Default.Check else Icons.Default.CheckCircle,
                    contentDescription = "Terpilih",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DashboardSedekahSubuhWidget(
    subuhState: com.example.core.infaq.SedekahSubuhState,
    onClick: () -> Unit,
    onQuickGive: (Double) -> Unit
) {
    val isTodayDone = subuhState.isCompletedToday()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("dashboard_subuh_widget"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isTodayDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sedekah Subuh Streak",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${subuhState.currentStreak} Hari Berturut-turut • ${subuhState.badges.count { it.isUnlocked }} Lencana",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    color = if (isTodayDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isTodayDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (isTodayDone) "✓ Sudah Subuh" else "Belum Subuh",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTodayDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick give row in widget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTodayDone) "Istiqomah hari ini terjaga!" else "Sedekah subuh kilat:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5000.0 to "5rb", 10000.0 to "10rb").forEach { (amt, lbl) ->
                        androidx.compose.material3.OutlinedButton(
                            onClick = { onQuickGive(amt) },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 56.dp, minHeight = 48.dp)
                                .testTag("dashboard_quick_subuh_${lbl}")
                        ) {
                            Text("+$lbl", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualInfaqVaultCard(
    vaultBalance: Double,
    distributionCount: Int = 0,
    isMasked: Boolean = false,
    onToggleMask: () -> Unit = {},
    onDisburseClick: () -> Unit,
    onHistoryClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("virtual_infaq_vault_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIRTUAL INFAQ VAULT (AMANAH)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.8.sp
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Hutang Ibadah / Titipan",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isMasked) "Rp ••••••••" else "Rp ${formatRupiah(vaultBalance)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(
                    onClick = onToggleMask,
                    modifier = Modifier.size(48.dp).testTag("vault_card_mask_toggle")
                ) {
                    Icon(
                        imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Privasi Saldo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Dana siap disalurkan ke Mustahiq, Amil, atau Fasilitas Umum",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDisburseClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("disburse_infaq_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Salurkan Infaq",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onHistoryClick,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("vault_history_button")
                ) {
                    Text(
                        text = "Riwayat ($distributionCount)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeBreakdownCard(
    incomeKasab: Double,
    incomeNonKasab: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Struktur Arus Rezeki Masuk",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Kasab
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kasab (Active/Gaji)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp ${formatRupiah(incomeKasab)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Non-Kasab
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Non-Kasab (Windfall)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp ${formatRupiah(incomeNonKasab)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun JournalEntryItemCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isIncome = entry.transactionType == "INFLOW" || entry.transactionType == "INCOME"
    val isPayout = entry.transactionType == "INFAQ_PAYOUT"

    val iconColor = when {
        isIncome -> MaterialTheme.colorScheme.primary
        isPayout -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    val icon = when {
        isIncome -> Icons.Default.ArrowDownward
        isPayout -> Icons.Default.VolunteerActivism
        else -> Icons.Default.ShoppingBag
    }

    val primaryAmount = entry.primaryAmount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("journal_entry_card_${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                val masehiDateFormatted = remember(entry.gregorianDate) {
                    SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(entry.gregorianDate)
                }
                Text(
                    text = entry.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$masehiDateFormatted • ${entry.hijriDay}/${entry.hijriMonth}/${entry.hijriYear} H",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isIncome) "+" else "-"}Rp ${formatRupiah(primaryAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) MaterialTheme.colorScheme.primary else if (isPayout) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("edit_entry_button_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Transaksi",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_entry_button_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Transaksi",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionDetailDialog(
    entry: JournalEntry,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = entry.transactionType == "INFLOW" || entry.transactionType == "INCOME"
    val isPayout = entry.transactionType == "INFAQ_PAYOUT"

    val typeLabel = when {
        isIncome -> "Pemasukan (Inflow Arus Kas)"
        isPayout -> "Penyaluran Infaq (Vault Disbursement)"
        else -> "Pengeluaran (Expense / Belanja)"
    }

    val primaryAmount = entry.primaryAmount

    val isTactical = LocalAppThemeMode.current.isTactical

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = if (isTactical) CutCornerShape(12.dp) else RoundedCornerShape(18.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rincian Transaksi Jurnal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Description & Type
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = entry.description,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = typeLabel,
                            fontSize = 11.sp,
                            color = if (isIncome) MaterialTheme.colorScheme.primary else if (isPayout) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Penanggalan: ${entry.hijriDay}/${entry.hijriMonth}/${entry.hijriYear} H (${entry.gregorianDate})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Journal Lines Double Entry Breakdown
                Text(
                    text = "Struktur Jurnal Berpasangan (Double-Entry):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        entry.lines.forEach { line ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = getAccountDisplayName(line.accountId),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (line.debit > 0) {
                                    Text(
                                        text = "Dr: Rp ${formatRupiah(line.debit)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(
                                        text = "Cr: Rp ${formatRupiah(line.credit)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Receipt Attachment Information & Physical Photo Display
                entry.receiptAttachment?.let { receipt ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = if (isTactical) CutCornerShape(6.dp) else RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🧾 ${receipt.title}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = receipt.receiptType.displayName,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (receipt.merchantName.isNotBlank()) {
                                Text(
                                    text = "Toko/Merchant: ${receipt.merchantName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (receipt.referenceNumber.isNotBlank()) {
                                Text(
                                    text = "No. Ref: ${receipt.referenceNumber}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            // Physical receipt photo
                            if (!receipt.imagePath.isNullOrBlank()) {
                                val photoFile = File(receipt.imagePath)
                                if (photoFile.exists()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Foto Fisik Dokumen:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AsyncImage(
                                        model = photoFile,
                                        contentDescription = "Foto Fisik Struk Transaksi",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("detail_dialog_delete_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.7f)),
                        shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("detail_dialog_edit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubah Data", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("detail_dialog_close_button")
            ) {
                Text("Tutup", color = MaterialTheme.colorScheme.primary)
            }
        },
        modifier = Modifier.testTag("transaction_detail_dialog")
    )
}

@Composable
fun DashboardBudgetAllocationCard(
    state: AmanahLedgerUiState,
    onClick: () -> Unit
) {
    val totalLimit = state.totalMonthlyBudgetLimit
    val totalSpent = state.totalMonthlyBudgetSpent
    val usageRatio = if (totalLimit > 0.0) (totalSpent / totalLimit).coerceIn(0.0, 1.0) else 0.0
    val usagePercentage = if (totalLimit > 0.0) (totalSpent / totalLimit) * 100.0 else 0.0
    val remainingBudget = (totalLimit - totalSpent).coerceAtLeast(0.0)
    val overBudgetCount = state.overBudgetCount

    val progressColor = when {
        usagePercentage >= 100.0 -> MaterialTheme.colorScheme.error
        usagePercentage >= 80.0 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("dashboard_budget_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (overBudgetCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pemisahan Anggaran Bulanan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.budgets.size} Kategori Pengeluaran",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = progressColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", usagePercentage)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = progressColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Kelola Anggaran",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { usageRatio.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terpakai: Rp ${formatRupiah(totalSpent)} / Rp ${formatRupiah(totalLimit)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (overBudgetCount > 0) "$overBudgetCount Over-Budget!" else "Sisa: Rp ${formatRupiah(remainingBudget)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (overBudgetCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DashboardOverBudgetAlertBanner(
    overBudgetCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("dashboard_overbudget_warning_banner"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Peringatan: $overBudgetCount Kategori Melebihi Anggaran!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Pengeluaran melampaui batas bulanan. Klik untuk sesuaikan.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun DashboardCategoryBudgetsSection(
    state: AmanahLedgerUiState,
    onManageClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pengeluaran & Anggaran per Pos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Kelola Anggaran",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clickable { onManageClick() }
                    .testTag("manage_budgets_link")
            )
        }

        if (state.budgets.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada batas anggaran yang ditentukan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.budgets.forEach { budget ->
                    val spent = state.getMonthlySpentForAccount(budget.accountId)
                    val ratio = if (budget.monthlyLimit > 0.0) (spent / budget.monthlyLimit).coerceIn(0.0, 1.0) else 0.0
                    val percentage = if (budget.monthlyLimit > 0.0) (spent / budget.monthlyLimit) * 100.0 else 0.0
                    val isOver = spent > budget.monthlyLimit
                    val remaining = (budget.monthlyLimit - spent).coerceAtLeast(0.0)

                    // Color changing from green to yellow to red as budget is consumed
                    val barColor = when {
                        percentage >= 100.0 -> MaterialTheme.colorScheme.error
                        percentage >= 75.0 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onManageClick() }
                            .testTag("dashboard_category_budget_${budget.accountId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isOver) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(barColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getDashboardCategoryIcon(budget.accountId),
                                            contentDescription = null,
                                            tint = barColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = budget.categoryName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Batas: Rp ${formatRupiah(budget.monthlyLimit)}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format("%.1f", percentage)}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = barColor
                                    )
                                    Text(
                                        text = if (isOver) {
                                            "Over +Rp ${formatRupiah(spent - budget.monthlyLimit)}"
                                        } else {
                                            "Sisa Rp ${formatRupiah(remaining)}"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Visual progress bar comparing actual spending against defined limit
                            LinearProgressIndicator(
                                progress = { ratio.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Terpakai: Rp ${formatRupiah(spent)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isOver) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Melebihi Batas",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getAccountDisplayName(accountId: String): String {
    return when (accountId) {
        "acc_cash" -> "Kas Fisik / Tunai"
        "acc_bank" -> "Rekening Bank Syariah"
        "acc_ewallet" -> "Dompet Digital (E-Wallet)"
        "acc_gold" -> "Tabungan Emas"
        "acc_infaq_vault" -> "Vault Amanah Infaq"
        "acc_living" -> "Pengeluaran Kebutuhan Pokok"
        "acc_entertainment" -> "Pengeluaran Hiburan / Belanja"
        "acc_transport" -> "Pengeluaran Transportasi"
        "acc_education" -> "Pengeluaran Pendidikan"
        "acc_health" -> "Pengeluaran Kesehatan"
        "acc_bills" -> "Pengeluaran Tagihan / Listrik"
        "acc_income_kasab" -> "Pendapatan Kasab (Gaji/Bisnis)"
        "acc_income_nonkasab" -> "Pendapatan Non-Kasab (Hadiah/Bonus)"
        "acc_infaq_expense" -> "Penyaluran Infaq (Mustahik)"
        "acc_disbursed" -> "Penyaluran Infaq Terbayar"
        "acc_sedekah_extra" -> "Sedekah Ekstra & Sukarela"
        else -> accountId
    }
}

fun getDashboardCategoryIcon(accountId: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (accountId) {
        "acc_living" -> Icons.Default.Fastfood
        "acc_entertainment" -> Icons.Default.ShoppingBag
        "acc_transport" -> Icons.Default.Commute
        "acc_education" -> Icons.Default.School
        "acc_health" -> Icons.Default.LocalHospital
        "acc_bills" -> Icons.Default.Bolt
        else -> Icons.Default.PieChart
    }
}



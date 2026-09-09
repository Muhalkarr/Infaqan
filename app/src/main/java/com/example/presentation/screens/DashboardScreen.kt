package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import com.example.Screen
import com.example.presentation.components.AppNavigationDrawerContent
import com.example.presentation.components.BudgetCategoryAllocationChart
import com.example.presentation.components.DualCalendarCard
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.JournalEntry
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.core.state.AmanahLedgerUiState
import com.example.core.state.AmanahLedgerViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.White12
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AmanahLedgerViewModel,
    onOpenDrawer: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit,
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
    onNavigateToIslamicGrounding: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val latestAlert by viewModel.appStateNotifier.latestAlert.collectAsState()
    val hijriNow = remember { HijriCalendarEngine.fromGregorian() }
    val masehiNow = remember { SimpleDateFormat("d MMM yyyy", Locale("id", "ID")).format(Date()) }
    val isFriday = remember { HijriCalendarEngine.isFriday() }
    var showDisburseDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedEntryForDetail by remember { mutableStateOf<JournalEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }
    var dismissBackupNudge by remember { mutableStateOf(false) }

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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$hijriNow ${if (isFriday) "• Hari Jumat Berkah" else ""}",
                            fontSize = 11.sp,
                            color = EmeraldLight
                        )
                    }
                },
                actions = {
                    // Quick Balance Privacy Mask Toggle (Mata Saldo)
                    IconButton(
                        onClick = { viewModel.toggleBalancePrivacy() },
                        modifier = Modifier.testTag("dashboard_privacy_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (state.securityConfig.isMaskBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Privasi Saldo",
                            tint = if (state.securityConfig.isMaskBalance) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Catat Transaksi") },
                text = { Text("Catat Transaksi", fontWeight = FontWeight.Bold) },
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("add_transaction_fab")
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. PRIORITAS TERTINGGI: App State Notifier Live Alert Banner (Peringatan & Status Sistem)
            if (latestAlert != null) {
                item {
                    val alert = latestAlert!!
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("app_state_notifier_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = when (alert.severity) {
                                com.example.core.state.NotificationSeverity.SUCCESS -> EmeraldPrimary.copy(alpha = 0.18f)
                                com.example.core.state.NotificationSeverity.ALERT -> ExpenseCoral.copy(alpha = 0.2f)
                                com.example.core.state.NotificationSeverity.WARNING -> GoldAccent.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (alert.severity) {
                                com.example.core.state.NotificationSeverity.SUCCESS -> EmeraldLight
                                com.example.core.state.NotificationSeverity.ALERT -> ExpenseCoral
                                com.example.core.state.NotificationSeverity.WARNING -> GoldAccent
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

            // 2. PRIORITAS TERTINGGI: Over-Budget Alert Visual Indicator Banner (Peringatan Melebihi Batas Pagu)
            if (state.overBudgetCount > 0) {
                item {
                    DashboardOverBudgetAlertBanner(
                        overBudgetCount = state.overBudgetCount,
                        onClick = onNavigateToBudget
                    )
                }
            }

            // 3. PRIORITAS UTAMA (HERO): Virtual Infaq Vault & Total Assets (Brankas Amanah & Saldo Kas)
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

            // 4. PRIORITAS UTAMA: Dual Calendar Gregorian & Hijriah Header
            item {
                DualCalendarCard(
                    selectedHijriOffset = state.selectedHijriOffset
                )
            }

            // 5. PRIORITAS UTAMA: Sedekah Subuh Streak Visualizer Widget (Habit Tracker Fajar)
            item {
                DashboardSedekahSubuhWidget(
                    subuhState = state.sedekahSubuhState,
                    onClick = onNavigateToSedekahSubuh,
                    onQuickGive = { amount ->
                        viewModel.recordSedekahSubuh(amount = amount)
                    }
                )
            }

            // 6. PRIORITAS TINGGI: Pintasan Cepat Fitur Kunci (2x2 Grid Aksi Cepat)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoMode,
                                        contentDescription = null,
                                        tint = EmeraldLight,
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
                                        color = if (state.dueRecurringCount > 0) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

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
                                        .background(GoldAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = GoldAccent,
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                        .background(GoldAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = GoldAccent,
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
                                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolunteerActivism,
                                        contentDescription = null,
                                        tint = EmeraldLight,
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

            // 7. PRIORITAS MENENGAH: Metrik Finansial & Spiritual (SLI & Status Nisab 85g Emas)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Spiritual Liquidity Index Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sli_card"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Spiritual Liquidity (SLI)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${String.format("%.1f", state.spiritualLiquidityIndex)}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.spiritualLiquidityIndex >= 5.0) "Sangat Dermawan" else "Tingkatkan Infaq",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldAccent
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
                            if (state.isNisabReached) GoldAccent else MaterialTheme.colorScheme.outline
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
                                color = if (state.isNisabReached) GoldAccent else MaterialTheme.colorScheme.onSurface
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

            // 8. PRIORITAS MENENGAH: Pemisahan Anggaran (Budget Allocation Overview Card)
            item {
                DashboardBudgetAllocationCard(
                    state = state,
                    onClick = onNavigateToBudget
                )
            }

            // 9. PRIORITAS MENENGAH: Sharia Budget Category Allocation Chart
            item {
                BudgetCategoryAllocationChart(
                    budgetCategories = state.budgets,
                    journalEntries = state.journalEntries,
                    currencySymbol = state.primaryCurrencySymbol,
                    onNavigateToBudget = onNavigateToBudget
                )
            }

            // 10. PRIORITAS MENENGAH: Status Anggaran per Kategori (Visual Progress Bars)
            item {
                DashboardCategoryBudgetsSection(
                    state = state,
                    onManageClick = onNavigateToBudget
                )
            }

            // 11. PRIORITAS MENENGAH: Ringkasan Kasab vs Non-Kasab
            item {
                IncomeBreakdownCard(
                    incomeKasab = state.totalIncomeKasab,
                    incomeNonKasab = state.totalIncomeNonKasab
                )
            }

            // 12. PRIORITAS OPERASIONAL: Double-Entry Journal History Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buku Besar Terkini (Double-Entry Log)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Balanced",
                            tint = EmeraldLight,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "∑Debit = ∑Kredit",
                            fontSize = 10.sp,
                            color = EmeraldLight
                        )
                    }
                }
            }

            // 13. PRIORITAS OPERASIONAL: List of Journal Entries
            if (state.journalEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada transaksi tercatat",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(state.journalEntries, key = { it.id }) { entry ->
                    JournalEntryItemCard(
                        entry = entry,
                        onClick = { selectedEntryForDetail = entry },
                        onEditClick = { onEditTransaction(entry.id) },
                        onDeleteClick = { entryToDelete = entry }
                    )
                }
            }

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
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
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
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = GoldAccent,
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
                                    color = GoldAccent.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "PANDUAN LENGKAP",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent,
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
                            tint = EmeraldLight,
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
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
                                .background(EmeraldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Rujukan Fatwa DSN-MUI",
                                tint = EmeraldLight,
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
                                    color = EmeraldPrimary.copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, EmeraldLight.copy(alpha = 0.5f))
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

            // 16. PRIORITAS PALING BAWAH (PEMELIHARAAN SISTEM): Periodic Local Room Database Backup Nudge
            if (!dismissBackupNudge) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("database_backup_nudge_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
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
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Cadangan Database",
                                    tint = GoldAccent,
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
                                    Surface(
                                        onClick = onNavigateToBackupRestore,
                                        color = GoldAccent,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("backup_now_button")
                                    ) {
                                        Text(
                                            text = "Cadangkan Sekarang",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }

                                    Text(
                                        text = "Nanti",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .clickable { dismissBackupNudge = true }
                                            .padding(horizontal = 6.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Detail Modal for selected journal entry
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
            containerColor = Color(0xFF1E1414),
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ExpenseCoral,
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
                        color = GoldAccent
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
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseCoral),
                    modifier = Modifier.testTag("dashboard_confirm_delete_button")
                ) {
                    Text("Ya, Hapus Transaksi", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { entryToDelete = null },
                    modifier = Modifier.testTag("dashboard_cancel_delete_button")
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
}

@Composable
fun ThemeSwitcherDialog(
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
                    tint = GoldAccent,
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pilih preferensi visual tampilan yang nyaman untuk mata Anda:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Elegant Dark
                ThemeOptionCard(
                    title = "🌙 Elegant Dark (Default)",
                    description = "Nuansa obsidian & emerald glow beraksen emas",
                    isSelected = isDarkMode && !isHighContrast,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.ELEGANT_DARK) }
                )

                // Option 2: Light Mode
                ThemeOptionCard(
                    title = "☀️ Light Mode",
                    description = "Latar terang bersih, ramah saat siang hari",
                    isSelected = !isDarkMode && !isHighContrast,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.LIGHT_MODE) }
                )

                // Option 3: High Contrast Light Mode
                ThemeOptionCard(
                    title = "👁️ High-Contrast Light (Aksesibilitas)",
                    description = "Teks hitam pekat & garis tegas untuk visibilitas optimal",
                    isSelected = !isDarkMode && isHighContrast,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT) }
                )

                // Option 4: High Contrast Dark Mode
                ThemeOptionCard(
                    title = "🖤 High-Contrast Dark",
                    description = "Hitam OLED murni dengan kontras teks maksimal",
                    isSelected = isDarkMode && isHighContrast,
                    onClick = { onSelectTheme(com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun ThemeOptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EmeraldPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Terpilih",
                    tint = EmeraldLight,
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
            if (isTodayDone) EmeraldPrimary.copy(alpha = 0.5f) else GoldAccent.copy(alpha = 0.4f)
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
                            .background(GoldAccent.copy(alpha = 0.15f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape),
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
                            color = EmeraldLight
                        )
                    }
                }

                Surface(
                    color = if (isTodayDone) EmeraldPrimary.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isTodayDone) EmeraldLight.copy(alpha = 0.5f) else GoldAccent.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (isTodayDone) "✓ Sudah Subuh" else "Belum Subuh",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTodayDone) EmeraldLight else GoldAccent,
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
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = EmeraldPrimary.copy(alpha = 0.15f),
                                contentColor = GoldAccent
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 56.dp, minHeight = 34.dp)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF0E3B36), Color(0xFF0A2522))
                )
            )
            .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VIRTUAL INFAQ VAULT (AMANAH)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        letterSpacing = 0.8.sp
                    )
                }
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Hutang Ibadah / Titipan",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.85f),
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
                    color = Color.White
                )
                IconButton(
                    onClick = onToggleMask,
                    modifier = Modifier.size(32.dp).testTag("vault_card_mask_toggle")
                ) {
                    Icon(
                        imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Privasi Saldo",
                        tint = GoldAccent.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Dana siap disalurkan ke Mustahiq, Amil, atau Fasilitas Umum",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDisburseClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = Color(0xFF1E1A00)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
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
                            tint = EmeraldLight,
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
                            tint = GoldAccent,
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
    val isIncome = entry.transactionType == "INFLOW"
    val isPayout = entry.transactionType == "INFAQ_PAYOUT"

    val iconColor = when {
        isIncome -> EmeraldPrimary
        isPayout -> GoldAccent
        else -> ExpenseCoral
    }

    val icon = when {
        isIncome -> Icons.Default.ArrowDownward
        isPayout -> Icons.Default.VolunteerActivism
        else -> Icons.Default.ShoppingBag
    }

    val primaryAmount = entry.lines.firstOrNull()?.let {
        if (it.debit > 0) it.debit else it.credit
    } ?: 0.0

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
                .padding(12.dp),
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.hijriDay}/${entry.hijriMonth}/${entry.hijriYear} H • ∑Debit=∑Kredit Seimbang",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"}Rp ${formatRupiah(primaryAmount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) MaterialTheme.colorScheme.primary else if (isPayout) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("edit_entry_button_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Transaksi",
                            tint = EmeraldLight.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_entry_button_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Transaksi",
                            tint = ExpenseCoral.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
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
    val isIncome = entry.transactionType == "INFLOW"
    val isPayout = entry.transactionType == "INFAQ_PAYOUT"

    val typeLabel = when {
        isIncome -> "Pemasukan (Inflow Arus Kas)"
        isPayout -> "Penyaluran Infaq (Vault Disbursement)"
        else -> "Pengeluaran (Expense / Belanja)"
    }

    val primaryAmount = entry.lines.firstOrNull()?.let {
        if (it.debit > 0) it.debit else it.credit
    } ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = GoldAccent,
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
                    shape = RoundedCornerShape(10.dp)
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
                            color = if (isIncome) MaterialTheme.colorScheme.primary else if (isPayout) MaterialTheme.colorScheme.secondary else ExpenseCoral
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
                    shape = RoundedCornerShape(10.dp),
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

                // Action Buttons inside Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_dialog_delete_button"),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseCoral.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseCoral, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", color = ExpenseCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onEdit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_dialog_edit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubah Data", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("detail_dialog_close_button")
            ) {
                Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        usagePercentage >= 100.0 -> ExpenseCoral
        usagePercentage >= 80.0 -> GoldAccent
        else -> EmeraldLight
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
            if (overBudgetCount > 0) ExpenseCoral.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
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
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = GoldAccent,
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
                    color = if (overBudgetCount > 0) ExpenseCoral else EmeraldLight
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1414)),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseCoral.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ExpenseCoral.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ExpenseCoral,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Peringatan: $overBudgetCount Kategori Melebihi Anggaran!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseCoral
                )
                Text(
                    text = "Pengeluaran melampaui batas bulanan. Klik untuk sesuaikan.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = ExpenseCoral,
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
                color = GoldAccent,
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
                        percentage >= 100.0 -> ExpenseCoral
                        percentage >= 75.0 -> GoldAccent
                        else -> EmeraldLight
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
                            if (isOver) ExpenseCoral.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline
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
                                        color = if (isOver) ExpenseCoral else EmeraldLight
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
                                            tint = ExpenseCoral,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Melebihi Batas",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseCoral
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
        "acc_cash" -> "Kas Tunai"
        "acc_bank" -> "Rekening Bank Syariah"
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



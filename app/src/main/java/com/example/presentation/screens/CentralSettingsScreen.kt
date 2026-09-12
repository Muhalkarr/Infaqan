package com.example.presentation.screens


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AmanahLedgerViewModel
import com.example.presentation.screens.settings.BudgetSettingsSection
import com.example.presentation.screens.settings.EditGoldPriceDialog
import com.example.presentation.screens.settings.EditInitialBalanceDialog
import com.example.presentation.screens.settings.EditInitialDateDialog
import com.example.presentation.screens.settings.EditProfileDialog
import com.example.presentation.screens.settings.ExportSummaryDialog
import com.example.presentation.screens.settings.InfaqSettingsSection
import com.example.presentation.screens.settings.LedgerIdentitySettingsSection
import com.example.presentation.screens.settings.MaintenanceSettingsSection
import com.example.presentation.screens.settings.RecurringSettingsSection
import com.example.presentation.screens.settings.ResetConfirmationDialog
import com.example.presentation.screens.settings.SecuritySettingsSection
import com.example.presentation.screens.settings.TextScaleSettingsSection
import com.example.presentation.screens.settings.ThemeContrastSettingsSection
import com.example.presentation.screens.settings.ZakatNisabSettingsSection
import java.text.NumberFormat
import java.util.Locale

private enum class SettingsCategory(val label: String, val icon: ImageVector) {
    ALL("Semua", Icons.Default.Tune),
    LEDGER("Kas & Identitas", Icons.Default.AccountBalance),
    THEME("Tema & Kontras", Icons.Default.Palette),
    TEXT_SCALE("Skala Teks", Icons.Default.FormatSize),
    INFAQ("Infaq & Shadaqah", Icons.Default.Savings),
    ZAKAT("Zakat & Emas", Icons.Default.VolunteerActivism),
    BUDGET("Anggaran & Israf", Icons.Default.PieChart),
    RECURRING("Transaksi Rutin", Icons.Default.Schedule),
    SECURITY("Keamanan & PIN", Icons.Default.Shield),
    MAINTENANCE("Fiqh & Data", Icons.Default.MenuBook)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentralSettingsScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSecuritySettings: () -> Unit,
    onNavigateToInteractiveGuide: () -> Unit,
    onNavigateToMultiWallet: () -> Unit = {},
    onNavigateToIbadahGoals: () -> Unit = {},
    onNavigateToZakatHub: () -> Unit = {},
    onNavigateToBackupRestore: () -> Unit = {},
    onNavigateToQardh: () -> Unit = {},
    onNavigateToAmilDirectory: () -> Unit = {},
    onNavigateToFaraidh: () -> Unit = {},
    onNavigateToExportReport: () -> Unit = {},
    onNavigateToDebugTerminal: () -> Unit = {},
    onNavigateToShariahRules: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    var selectedCategory by remember { mutableStateOf(SettingsCategory.ALL) }

    // Dialog state
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditInitialDateDialog by remember { mutableStateOf(false) }
    var showEditInitialBalanceDialog by remember { mutableStateOf(false) }
    var showEditGoldPriceDialog by remember { mutableStateOf(false) }
    var showResetConfirmationDialog by remember { mutableStateOf(false) }
    var showExportSummaryDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Column {
                        Text(
                            text = "Pengaturan Terpusat",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Konfigurasi Seluruh Modul & Fitur Aplikasi",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("central_settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Layar Sebelumnya",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportSummaryDialog = true },
                        modifier = Modifier.testTag("central_settings_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Ekspor Ringkasan Buku Kas",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = { showResetConfirmationDialog = true },
                        modifier = Modifier.testTag("central_settings_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Pengaturan Awal",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("central_settings_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Bar (Horizontal Scroll)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category.label, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("settings_filter_${category.name.lowercase()}")
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Main Settings Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. KAS & IDENTITAS BUKU BESAR
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.LEDGER) {
                    LedgerIdentitySettingsSection(
                        state = state,
                        nf = nf,
                        onEditProfileClick = { showEditProfileDialog = true },
                        onEditInitialDateClick = { showEditInitialDateDialog = true },
                        onEditInitialBalanceClick = { showEditInitialBalanceDialog = true },
                        onUpdateFiscalCycleType = { viewModel.updateFiscalCycleType(it) },
                        onSetStartDayOfMonth = { viewModel.setStartDayOfMonth(it) },
                        onSetDeficitProtectionEnabled = { viewModel.setDeficitProtectionEnabled(it) }
                    )
                }

                // 2. TEMA & KONTRAS TAMPILAN
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.THEME) {
                    ThemeContrastSettingsSection(
                        state = state,
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetMaskBalanceByDefault = { viewModel.setMaskBalanceByDefault(it) },
                        onSetScreenshotProtection = { viewModel.setScreenshotProtection(it) }
                    )
                }

                // 2B. SKALA TAMPILAN & UKURAN TEKS
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.THEME || selectedCategory == SettingsCategory.TEXT_SCALE) {
                    TextScaleSettingsSection(
                        state = state,
                        onSetUiScaleMode = { viewModel.setUiScaleMode(it) },
                        onSetUiScaleFactor = { viewModel.setUiScaleFactor(it) }
                    )
                }

                // 3. INFAQ, SHADAQAH & SEDEKAH SUBUH
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.INFAQ) {
                    InfaqSettingsSection(
                        state = state,
                        nf = nf,
                        onSetDefaultInfaqRate = { viewModel.setDefaultInfaqRate(it) },
                        onSetAutoDeductInfaqEnabled = { viewModel.setAutoDeductInfaqEnabled(it) },
                        onSetVaultMonthlyTarget = { viewModel.setVaultMonthlyTarget(it) },
                        onSetSedekahSubuhTargetDays = { viewModel.setSedekahSubuhTargetDays(it) }
                    )
                }

                // 4. ZAKAT MAL, HAUL & NISAB EMAS
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.ZAKAT) {
                    ZakatNisabSettingsSection(
                        state = state,
                        nf = nf,
                        onEditGoldPriceClick = { showEditGoldPriceDialog = true },
                        onNavigateToShariahRules = onNavigateToShariahRules,
                        onSetHijriOffset = { viewModel.setHijriOffset(it) }
                    )
                }

                // 5. ANGGARAN & PENGAWASAN ISRAF
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.BUDGET) {
                    BudgetSettingsSection(
                        state = state,
                        onSetFinancialGoalMode = { viewModel.setFinancialGoalMode(it) },
                        onSetIsrafWarningThresholdPercent = { viewModel.setIsrafWarningThresholdPercent(it) },
                        onSetStrictBudgetEnforced = { viewModel.setStrictBudgetEnforced(it) }
                    )
                }

                // 6. TRANSAKSI RUTIN & OTOMASI
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.RECURRING) {
                    RecurringSettingsSection(
                        state = state,
                        onSetAutoExecuteRecurringEnabled = { viewModel.setAutoExecuteRecurringEnabled(it) },
                        onSetNotifyOnRecurringDue = { viewModel.setNotifyOnRecurringDue(it) }
                    )
                }

                // 7. KEAMANAN, PIN & BIOMETRIK
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.SECURITY) {
                    SecuritySettingsSection(
                        state = state,
                        onNavigateToSecuritySettings = onNavigateToSecuritySettings,
                        onSetBiometricEnabled = { viewModel.setBiometricEnabled(it) },
                        onSetAutoLockInterval = { viewModel.setAutoLockInterval(it) },
                        onLockAppNow = { viewModel.lockAppNow() }
                    )
                }

                // 8. FIQH EDUKASI & PEMELIHARAAN DATA
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.MAINTENANCE) {
                    MaintenanceSettingsSection(
                        state = state,
                        context = context,
                        clipboardManager = clipboardManager,
                        summaryText = viewModel.exportLedgerSummaryText(),
                        onSetShowDailyHadith = { viewModel.setShowDailyHadith(it) },
                        onNavigateToInteractiveGuide = onNavigateToInteractiveGuide,
                        onNavigateToBackupRestore = onNavigateToBackupRestore,
                        onNavigateToDebugTerminal = onNavigateToDebugTerminal,
                        onRequestResetConfirmation = { showResetConfirmationDialog = true },
                        onLoadDummyData = {
                            viewModel.loadDummyData()
                            Toast.makeText(context, "Data contoh/dummy berhasil dimuat kembali!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // DIALOGS
    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = state.userNameKasMukmin,
            onDismiss = { showEditProfileDialog = false },
            onSave = { viewModel.setUserNameKasMukmin(it) }
        )
    }

    if (showEditInitialDateDialog) {
        EditInitialDateDialog(
            initialDate = state.initialLedgerDate,
            onDismiss = { showEditInitialDateDialog = false },
            onSave = { viewModel.updateInitialLedgerDate(it) }
        )
    }

    if (showEditInitialBalanceDialog) {
        EditInitialBalanceDialog(
            initialBalance = state.initialLedgerBalance,
            onDismiss = { showEditInitialBalanceDialog = false },
            onSave = { viewModel.updateInitialLedgerBalance(it) }
        )
    }

    if (showEditGoldPriceDialog) {
        EditGoldPriceDialog(
            currentGoldPrice = state.goldPricePerGram,
            nf = nf,
            onDismiss = { showEditGoldPriceDialog = false },
            onSave = { viewModel.setGoldPrice(it) }
        )
    }

    if (showResetConfirmationDialog) {
        ResetConfirmationDialog(
            onDismiss = { showResetConfirmationDialog = false },
            onConfirmReset = {
                viewModel.clearAllDummyData()
                Toast.makeText(context, "Data dummy berhasil dihapus. Buku kas bersih.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showExportSummaryDialog) {
        ExportSummaryDialog(
            summaryText = viewModel.exportLedgerSummaryText(),
            clipboardManager = clipboardManager,
            context = context,
            onDismiss = { showExportSummaryDialog = false }
        )
    }
}

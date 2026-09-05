package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.budget.FinancialGoalMode
import com.example.core.security.AutoLockInterval
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
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
                            color = EmeraldLight
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
                            tint = GoldAccent
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
                            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = EmeraldLight,
                            selectedLeadingIconColor = EmeraldLight
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
                // ==========================================
                // 1. KAS & IDENTITAS BUKU BESAR
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.LEDGER) {
                    SettingsSectionCard(
                        title = "Identitas Kas & Buku Besar",
                        subtitle = "Konfigurasi nama akun dan siklus periode pencatatan",
                        icon = Icons.Default.AccountBalance
                    ) {
                        // Nama Profil Kas Mukmin
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEditProfileDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nama Akun Kas",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = state.userNameKasMukmin,
                                    fontSize = 12.sp,
                                    color = EmeraldLight
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Ubah Nama Kas",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Tanggal Awal Pembukuan / Buku Kas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEditInitialDateDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tanggal Awal Buku Kas",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Mulai pencatatan: ${state.initialLedgerDate}",
                                    fontSize = 12.sp,
                                    color = EmeraldLight
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Ubah Tanggal Awal",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Saldo Awal Ekuitas / Modal Awal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEditInitialBalanceDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Saldo Awal Pembukuan (Modal Awal)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Rp ${nf.format(state.initialLedgerBalance)}",
                                    fontSize = 12.sp,
                                    color = GoldAccent
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Ubah Saldo Awal",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Jenis Siklus Periode Fiskal
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                text = "Model Siklus Periode Pembukuan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            com.example.core.accounting.FiscalCycleType.entries.forEach { cycle ->
                                val isSelected = state.fiscalCycleType == cycle
                                Surface(
                                    color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable { viewModel.updateFiscalCycleType(cycle) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = when (cycle) {
                                                    com.example.core.accounting.FiscalCycleType.MONTHLY_SALARY_DATE -> "Siklus Bulanan Gajian (Tgl ${state.startDayOfMonth})"
                                                    com.example.core.accounting.FiscalCycleType.CALENDAR_MONTH -> "Bulan Kalender Masehi (1 - Akhir Bulan)"
                                                    com.example.core.accounting.FiscalCycleType.HIJRI_MONTH -> "Bulan Tarikh Hijriyah (1 - 29/30 H)"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Tanggal Awal Periode Bulanan (Gajian)
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tanggal Mulai Siklus Buku Kas",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tanggal ${state.startDayOfMonth} setiap bulan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                            Slider(
                                value = state.startDayOfMonth.toFloat(),
                                onValueChange = { viewModel.setStartDayOfMonth(it.toInt()) },
                                valueRange = 1f..28f,
                                steps = 26,
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldLight,
                                    activeTrackColor = EmeraldPrimary
                                ),
                                modifier = Modifier.testTag("settings_start_day_slider")
                            )
                            Text(
                                text = "💡 Anggaran dan pengeluaran bulanan akan dihitung mulai tanggal ${state.startDayOfMonth} (misal saat gajian).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Simbol Mata Uang
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Standar Mata Uang",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Format Rupiah Indonesia (IDR)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Rp (IDR)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 2. TEMA & KONTRAS TAMPILAN
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.THEME) {
                    SettingsSectionCard(
                        title = "Tema & Kontras Tampilan",
                        subtitle = "Preferensi mode visual terang/gelap dan privasi tampilan",
                        icon = Icons.Default.Palette
                    ) {
                        Text(
                            text = "Mode Tema Visual",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Theme Mode Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionBox(
                                label = "Gelap Elegan",
                                icon = Icons.Default.DarkMode,
                                isSelected = state.isDarkMode && !state.isHighContrast,
                                modifier = Modifier.weight(1f),
                                testTag = "settings_theme_dark"
                            ) {
                                viewModel.setThemeMode(AppThemeMode.ELEGANT_DARK)
                            }
                            ThemeOptionBox(
                                label = "Terang Bersih",
                                icon = Icons.Default.LightMode,
                                isSelected = !state.isDarkMode && !state.isHighContrast,
                                modifier = Modifier.weight(1f),
                                testTag = "settings_theme_light"
                            ) {
                                viewModel.setThemeMode(AppThemeMode.LIGHT_MODE)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOptionBox(
                                label = "Kontras Tinggi (Terang)",
                                icon = Icons.Default.Visibility,
                                isSelected = !state.isDarkMode && state.isHighContrast,
                                modifier = Modifier.weight(1f),
                                testTag = "settings_theme_hc_light"
                            ) {
                                viewModel.setThemeMode(AppThemeMode.HIGH_CONTRAST_LIGHT)
                            }
                            ThemeOptionBox(
                                label = "Kontras Tinggi (Gelap)",
                                icon = Icons.Default.Visibility,
                                isSelected = state.isDarkMode && state.isHighContrast,
                                modifier = Modifier.weight(1f),
                                testTag = "settings_theme_hc_dark"
                            ) {
                                viewModel.setThemeMode(AppThemeMode.HIGH_CONTRAST_DARK)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Switch: Samarkan Saldo Bawaan
                        SettingsSwitchRow(
                            title = "Samarkan Nilai Saldo secara Bawaan",
                            subtitle = "Tampilkan 'Rp ••••••••' saat aplikasi baru dibuka",
                            checked = state.securityConfig.maskBalanceByDefault,
                            testTag = "settings_mask_balance_default_switch",
                            onCheckedChange = { viewModel.setMaskBalanceByDefault(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Switch: Perlindungan Screenshot & Pengalih Aplikasi
                        SettingsSwitchRow(
                            title = "Perlindungan Layar & Anti-Screenshot",
                            subtitle = "Cegah tangkapan layar dan samarkan preview di recent apps",
                            checked = state.securityConfig.isScreenshotProtected,
                            testTag = "settings_screenshot_protection_switch",
                            onCheckedChange = { viewModel.setScreenshotProtection(it) }
                        )
                    }
                }

                // ==========================================
                // 2B. SKALA TAMPILAN & UKURAN TEKS
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.THEME || selectedCategory == SettingsCategory.TEXT_SCALE) {
                    SettingsSectionCard(
                        title = "Skala Tampilan & Ukuran Teks",
                        subtitle = "Pengaturan ukuran font, keterbacaan, dan kenyamanan visual",
                        icon = Icons.Default.FormatSize
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Ukuran Huruf & Skala Antarmuka",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Mode: ${state.uiScaleMode.displayName} (${(state.uiScaleFactor * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${(state.uiScaleFactor * 100).toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4 preset mode buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val modes = listOf(
                                com.example.ui.theme.UiScaleMode.COMPACT to "85%",
                                com.example.ui.theme.UiScaleMode.DEFAULT to "100%",
                                com.example.ui.theme.UiScaleMode.LARGE to "115%",
                                com.example.ui.theme.UiScaleMode.EXTRA_LARGE to "130%"
                            )

                            modes.forEach { (m, pctLabel) ->
                                val isSelected = state.uiScaleMode == m
                                Surface(
                                    onClick = { viewModel.setUiScaleMode(m) },
                                    color = if (isSelected) EmeraldPrimary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.weight(1f).testTag("btn_ui_scale_${m.name.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = pctLabel,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = when(m) {
                                                com.example.ui.theme.UiScaleMode.COMPACT -> "Ringkas"
                                                com.example.ui.theme.UiScaleMode.DEFAULT -> "Standar"
                                                com.example.ui.theme.UiScaleMode.LARGE -> "Besar"
                                                com.example.ui.theme.UiScaleMode.EXTRA_LARGE -> "Ekstra"
                                            },
                                            fontSize = 9.sp,
                                            color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Penyesuaian Halus Skala:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Fine slider control
                        Slider(
                            value = state.uiScaleFactor,
                            onValueChange = { viewModel.setUiScaleFactor(it) },
                            valueRange = 0.80f..1.35f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldLight,
                                activeTrackColor = EmeraldPrimary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("slider_ui_scale_factor")
                        )

                        // Interactive live text preview
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FormatSize, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Pratinjau Keterbacaan Teks Skala ${(state.uiScaleFactor * 100).toInt()}%:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                                Text(
                                    text = "Buku Besar Syariah Amanah: Catatan Transaksi Keuangan Halal & Berkah",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Kas Tunai: Rp 1.500.000 (Kasab) • Infaq Rutin: Rp 37.500",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 3. INFAQ, SHADAQAH & SEDEKAH SUBUH
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.INFAQ) {
                    SettingsSectionCard(
                        title = "Infaq, Shadaqah & Sedekah Subuh",
                        subtitle = "Pengaturan persentase infaq otomatis dan target filantropi",
                        icon = Icons.Default.Savings
                    ) {
                        // Slider: Default Infaq Rate
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Persentase Infaq Masuk Bawaan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(state.defaultInfaqRate * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            }
                            Slider(
                                value = (state.defaultInfaqRate * 100).toFloat(),
                                onValueChange = { viewModel.setDefaultInfaqRate(it.toDouble() / 100.0) },
                                valueRange = 1f..20f,
                                steps = 18,
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldLight,
                                    activeTrackColor = EmeraldPrimary
                                ),
                                modifier = Modifier.testTag("settings_infaq_rate_slider")
                            )
                            Text(
                                text = "💡 Saat mencatat rezeki/gaji, persentase ${(state.defaultInfaqRate * 100).toInt()}% langsung diusulkan untuk disucikan.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Switch: Potong Infaq Otomatis
                        SettingsSwitchRow(
                            title = "Alokasi Otomatis ke Virtual Infaq Vault",
                            subtitle = "Langsung debet ke akun titipan hak mustahiq saat catat pemasukan",
                            checked = state.isAutoDeductInfaqEnabled,
                            testTag = "settings_auto_infaq_switch",
                            onCheckedChange = { viewModel.setAutoDeductInfaqEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Target Bulanan Virtual Infaq Vault
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                text = "Target Saldo Infaq Bulanan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(2500000.0, 5000000.0, 10000000.0).forEach { target ->
                                    val isSelected = state.vaultMonthlyTarget == target
                                    Surface(
                                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setVaultMonthlyTarget(target) }
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Rp ${nf.format(target / 1000000)} Jt",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Target Hari Sedekah Subuh
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Target Tantangan Sedekah Subuh",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${state.sedekahSubuhTargetDays} Hari Istiqomah",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(7, 30, 40, 100).forEach { days ->
                                    val isSelected = state.sedekahSubuhTargetDays == days
                                    Surface(
                                        color = if (isSelected) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GoldAccent) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setSedekahSubuhTargetDays(days) }
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$days Hari",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurface,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. ZAKAT MAL, HAUL & NISAB EMAS
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.ZAKAT) {
                    SettingsSectionCard(
                        title = "Zakat Mal, Haul & Standar Nisab",
                        subtitle = "Parameter harga emas acuan dan sinkronisasi penanggalan Hijriyah",
                        icon = Icons.Default.VolunteerActivism
                    ) {
                        // Harga Acuan Emas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEditGoldPriceDialog = true }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Harga Acuan Emas Fisik",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Rp ${nf.format(state.goldPricePerGram)} / gram",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Ubah Harga Emas",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Nisab Info Card
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Nisab Zakat Mal Acuan (85 Gram Emas):",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Rp ${nf.format(state.nisabThreshold)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Koreksi Kalender Hijriyah
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Koreksi Kalender Hijriyah (Offset)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.selectedHijriOffset >= 0) "+${state.selectedHijriOffset} Hari" else "${state.selectedHijriOffset} Hari",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(-2, -1, 0, 1, 2).forEach { offset ->
                                    val isSelected = state.selectedHijriOffset == offset
                                    Surface(
                                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setHijriOffset(offset) }
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (offset > 0) "+$offset" else "$offset",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 5. ANGGARAN & PENGAWASAN ISRAF
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.BUDGET) {
                    SettingsSectionCard(
                        title = "Anggaran & Batas Pengeluaran Syariah",
                        subtitle = "Strategi alokasi kebutuhan pokok dan pengawasan israf",
                        icon = Icons.Default.PieChart
                    ) {
                        Text(
                            text = "Model Alokasi Anggaran Syariah",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FinancialGoalMode.entries.forEach { mode ->
                            val isSelected = state.selectedGoalMode == mode
                            Surface(
                                color = if (isSelected) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setFinancialGoalMode(mode) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.PieChart,
                                        contentDescription = null,
                                        tint = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mode.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = mode.description,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Slider Ambang Israf
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ambang Batas Peringatan Israf",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${state.israfWarningThresholdPercent}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF7043)
                                )
                            }
                            Slider(
                                value = state.israfWarningThresholdPercent.toFloat(),
                                onValueChange = { viewModel.setIsrafWarningThresholdPercent(it.toInt()) },
                                valueRange = 50f..100f,
                                steps = 9,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFFF7043),
                                    activeTrackColor = Color(0xFFFF7043)
                                ),
                                modifier = Modifier.testTag("settings_israf_slider")
                            )
                            Text(
                                text = "⚠️ Peringatan israf (pemborosan) akan muncul bila pengeluaran pos melampaui ${state.israfWarningThresholdPercent}% dari plafon bulanan.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Switch: Peringatan Ketat
                        SettingsSwitchRow(
                            title = "Peringatan Ketat Over-Budget",
                            subtitle = "Berikan notifikasi keras jika suatu pos melewati 100% batas bulanan",
                            checked = state.isStrictBudgetEnforced,
                            testTag = "settings_strict_budget_switch",
                            onCheckedChange = { viewModel.setStrictBudgetEnforced(it) }
                        )
                    }
                }

                // ==========================================
                // 6. TRANSAKSI RUTIN & OTOMASI
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.RECURRING) {
                    SettingsSectionCard(
                        title = "Transaksi Berulang & Otomasi",
                        subtitle = "Pengelolaan eksekusi transaksi terjadwal otomatis",
                        icon = Icons.Default.Schedule
                    ) {
                        SettingsSwitchRow(
                            title = "Eksekusi Otomatis saat Jatuh Tempo",
                            subtitle = "Catat otomatis transaksi rutin (gaji, tagihan, SPP) saat tanggal tiba",
                            checked = state.autoExecuteRecurringEnabled,
                            testTag = "settings_auto_recurring_switch",
                            onCheckedChange = { viewModel.setAutoExecuteRecurringEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        SettingsSwitchRow(
                            title = "Notifikasi Pengingat Jatuh Tempo",
                            subtitle = "Berikan banner peringatan untuk transaksi berulang yang perlu dieksekusi",
                            checked = state.notifyOnRecurringDue,
                            testTag = "settings_notify_recurring_switch",
                            onCheckedChange = { viewModel.setNotifyOnRecurringDue(it) }
                        )
                    }
                }

                // ==========================================
                // 7. KEAMANAN, PIN & BIOMETRIK
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.SECURITY) {
                    SettingsSectionCard(
                        title = "Keamanan & Kunci Aplikasi",
                        subtitle = "Proteksi PIN 6-digit, sidik jari, dan interval auto-lock",
                        icon = Icons.Default.Shield
                    ) {
                        // Status PIN
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Status Kunci PIN 6-Digit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.securityConfig.isPinEnabled) "Aktif • Enkripsi SHA-256" else "Belum Diaktifkan",
                                    fontSize = 11.sp,
                                    color = if (state.securityConfig.isPinEnabled) EmeraldLight else MaterialTheme.colorScheme.error
                                )
                            }
                            Button(
                                onClick = onNavigateToSecuritySettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.securityConfig.isPinEnabled) MaterialTheme.colorScheme.surfaceVariant else EmeraldPrimary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("settings_open_security_center_button")
                            ) {
                                Text(
                                    text = if (state.securityConfig.isPinEnabled) "Kelola PIN" else "Atur PIN",
                                    fontSize = 12.sp,
                                    color = if (state.securityConfig.isPinEnabled) MaterialTheme.colorScheme.onSurface else Color.White
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Switch: Biometrik
                        SettingsSwitchRow(
                            title = "Otentikasi Biometrik / Sidik Jari",
                            subtitle = "Gunakan pemindai sidik jari perangkat untuk membuka kunci",
                            checked = state.securityConfig.isBiometricEnabled,
                            testTag = "settings_biometric_switch",
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Interval Auto-Lock
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                text = "Interval Kunci Otomatis (Auto-Lock)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AutoLockInterval.entries.forEach { interval ->
                                    val isSelected = state.securityConfig.autoLockInterval == interval
                                    Surface(
                                        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { viewModel.setAutoLockInterval(interval) }
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = when (interval) {
                                                AutoLockInterval.IMMEDIATE -> "Segera"
                                                AutoLockInterval.SECONDS_15 -> "15s"
                                                AutoLockInterval.MINUTE_1 -> "1m"
                                                AutoLockInterval.MINUTES_5 -> "5m"
                                                AutoLockInterval.NEVER -> "Mati"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        if (state.securityConfig.isPinEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { viewModel.lockAppNow() },
                                modifier = Modifier.fillMaxWidth().testTag("settings_lock_now_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kunci Aplikasi Sekarang", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // ==========================================
                // 8. FIQH EDUKASI & PEMELIHARAAN DATA
                // ==========================================
                if (selectedCategory == SettingsCategory.ALL || selectedCategory == SettingsCategory.MAINTENANCE) {
                    SettingsSectionCard(
                        title = "Fiqh Edukasi & Pemeliharaan Data",
                        subtitle = "Panduan interaktif pengguna dan pencadangan data kas",
                        icon = Icons.Default.MenuBook
                    ) {
                        SettingsSwitchRow(
                            title = "Tampilkan Hadits & Hikmah Harian di Beranda",
                            subtitle = "Mutiara nasehat amanah harta dan motivasi kedermawanan",
                            checked = state.showDailyHadith,
                            testTag = "settings_show_hadith_switch",
                            onCheckedChange = { viewModel.setShowDailyHadith(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Tombol Akses Panduan Interaktif
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToInteractiveGuide() }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Buka Panduan Interaktif Pengguna Baru",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Pelajari konsep Double-Entry Syariah & Virtual Vault",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Buka Panduan",
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Backup & Restore
                        Button(
                            onClick = onNavigateToBackupRestore,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("settings_backup_restore_button")
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pencadangan & Pemulihan Terenkripsi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Salin Ringkasan Jurnal
                        OutlinedButton(
                            onClick = {
                                val summary = viewModel.exportLedgerSummaryText()
                                clipboardManager.setText(AnnotatedString(summary))
                                Toast.makeText(context, "Ringkasan Buku Kas disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("settings_copy_summary_button")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salin Ringkasan Buku Kas ke Clipboard", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Hapus Semua Data Dummy (Buku Kas Bersih)
                        OutlinedButton(
                            onClick = { showResetConfirmationDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("settings_reset_data_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hapus Seluruh Data Dummy (Mulai Buku Kas Bersih)", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Muat Ulang Data Sampel / Dummy
                        OutlinedButton(
                            onClick = {
                                viewModel.loadDummyData()
                                Toast.makeText(context, "Data contoh/dummy berhasil dimuat kembali!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("settings_load_dummy_button")
                        ) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Muat Ulang Data Contoh / Dummy", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================

    // Dialog Edit Profil Kas
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(state.userNameKasMukmin) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Ubah Nama Akun Kas") },
            text = {
                Column {
                    Text(
                        text = "Masukkan nama entitas, keluarga, atau yayasan pemilik buku kas:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        placeholder = { Text("Contoh: Kas Keluarga Mukmin") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_input_profile_name")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.setUserNameKasMukmin(tempName.trim())
                        }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Edit Tanggal Awal Buku Kas
    if (showEditInitialDateDialog) {
        var tempDate by remember { mutableStateOf(state.initialLedgerDate) }
        AlertDialog(
            onDismissRequest = { showEditInitialDateDialog = false },
            title = { Text("Ubah Tanggal Awal Buku Kas") },
            text = {
                Column {
                    Text(
                        text = "Tentukan tanggal pembukaan / cutoff awal pembukuan kas (format: DD/MM/YYYY):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempDate,
                        onValueChange = { tempDate = it },
                        singleLine = true,
                        placeholder = { Text("DD/MM/YYYY") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_input_initial_date")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val todayStr = sdf.format(java.util.Date())
                        val startOfMonthStr = "01/" + java.text.SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(java.util.Date())
                        val startOfYearStr = "01/01/" + java.text.SimpleDateFormat("yyyy", Locale.getDefault()).format(java.util.Date())

                        listOf("Hari Ini" to todayStr, "Awal Bulan" to startOfMonthStr, "Awal Tahun" to startOfYearStr).forEach { (label, dateVal) ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { tempDate = dateVal }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempDate.isNotBlank()) {
                            viewModel.updateInitialLedgerDate(tempDate.trim())
                        }
                        showEditInitialDateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditInitialDateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Edit Saldo Awal Pembukuan
    if (showEditInitialBalanceDialog) {
        var tempBalanceStr by remember { mutableStateOf(state.initialLedgerBalance.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showEditInitialBalanceDialog = false },
            title = { Text("Ubah Saldo Awal (Modal Awal)") },
            text = {
                Column {
                    Text(
                        text = "Masukkan total ekuitas / saldo awal kas saat pertama kali memulai pembukuan:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempBalanceStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tempBalanceStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("Rp ") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_input_initial_balance")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempBalanceStr.toDoubleOrNull()
                        if (parsed != null && parsed >= 0) {
                            viewModel.updateInitialLedgerBalance(parsed)
                        }
                        showEditInitialBalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditInitialBalanceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Edit Harga Emas
    if (showEditGoldPriceDialog) {
        var tempPriceStr by remember { mutableStateOf(state.goldPricePerGram.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showEditGoldPriceDialog = false },
            title = { Text("Ubah Harga Acuan Emas") },
            text = {
                Column {
                    Text(
                        text = "Harga emas murni per gram saat ini untuk menentukan nisab zakat:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempPriceStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) tempPriceStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        prefix = { Text("Rp ") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_input_gold_price")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(1300000L, 1350000L, 1400000L).forEach { p ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { tempPriceStr = p.toString() }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Rp ${nf.format(p / 1000)}k",
                                    fontSize = 10.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempPriceStr.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.setGoldPrice(parsed)
                        }
                        showEditGoldPriceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoldPriceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Konfirmasi Reset Data
    if (showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmationDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Semua Data Dummy?") },
            text = {
                Text(
                    "Tindakan ini akan menghapus seluruh data contoh/dummy (transaksi jurnal kas, kantong rekening, anggaran belanja, target ibadah, hutang/piutang qardh, dan riwayat sedekah subuh) sehingga buku kas Anda menjadi bersih 100% untuk mulai mencatat keuangan pribadi secara riil.\n\nCatatan: Anda dapat memuat ulang data contoh kapan saja melalui tombol 'Muat Ulang Data Contoh'.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDummyData()
                        showResetConfirmationDialog = false
                        Toast.makeText(context, "Data dummy berhasil dihapus. Buku kas bersih.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_reset_dummy_button")
                ) {
                    Text("Ya, Hapus Data Dummy")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmationDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Ekspor Ringkasan Buku Kas
    if (showExportSummaryDialog) {
        val summaryText = remember { viewModel.exportLedgerSummaryText() }
        AlertDialog(
            onDismissRequest = { showExportSummaryDialog = false },
            title = { Text("Ringkasan Buku Kas") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = summaryText,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(summaryText))
                        Toast.makeText(context, "Ringkasan Buku Kas berhasil disalin!", Toast.LENGTH_SHORT).show()
                        showExportSummaryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Salin Teks")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportSummaryDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldPrimary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun ThemeOptionBox(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldLight) else null,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) EmeraldLight else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

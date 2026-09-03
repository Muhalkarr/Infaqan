package com.example.presentation.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.AutoLockInterval
import com.example.core.state.AmanahLedgerViewModel
import com.example.core.sync.SyncStateStatus
import com.example.presentation.components.ShimmerListSkeleton
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditGoldPriceDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showClearDummyConfirmDialog by remember { mutableStateOf(false) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    var tempUserName by remember(state.userNameKasMukmin) { mutableStateOf(state.userNameKasMukmin) }
    var tempStartDay by remember(state.startDayOfMonth) { mutableIntStateOf(state.startDayOfMonth) }
    var tempCurrency by remember(state.primaryCurrencySymbol) { mutableStateOf(state.primaryCurrencySymbol) }
    var tempGoldPrice by remember(state.goldPricePerGram) { mutableStateOf(state.goldPricePerGram.toLong().toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pengaturan Aplikasi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tersimpan Permanen di Perangkat (DataStore)",
                            fontSize = 11.sp,
                            color = EmeraldLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("settings_menu_sidebar_button")
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
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Storage persistence banner & Room Database Sync / Backup Status Indicator
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("room_database_status_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Room Database Status",
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Database Room Lokal",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        color = EmeraldPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Tersimpan Permanen",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Seluruh jurnal & preferensi tersimpan di penyimpanan internal ponsel.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        // Status rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Integritas Data SQLite",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${state.journalEntries.size} Jurnal • ${state.wallets.size} Kantong",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldLight
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = when (syncState.status) {
                                        SyncStateStatus.SYNCING -> Icons.Default.CloudSync
                                        SyncStateStatus.SUCCESS -> Icons.Default.CloudDone
                                        SyncStateStatus.ERROR -> Icons.Default.CloudOff
                                        else -> Icons.Default.SdCard
                                    },
                                    contentDescription = null,
                                    tint = when (syncState.status) {
                                        SyncStateStatus.SYNCING -> GoldAccent
                                        SyncStateStatus.SUCCESS -> EmeraldLight
                                        SyncStateStatus.ERROR -> Color(0xFFEF4444)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Status Cadangan / Cloud",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = when (syncState.status) {
                                    SyncStateStatus.SYNCING -> "Sedang Menyinkronkan..."
                                    SyncStateStatus.SUCCESS -> "Tersinkron (${syncState.lastSyncedTimeString})"
                                    SyncStateStatus.ERROR -> "Offline / Lokal Aman"
                                    else -> "Tersimpan Lokal (Siap Cadang)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when (syncState.status) {
                                    SyncStateStatus.SYNCING -> GoldAccent
                                    SyncStateStatus.SUCCESS -> EmeraldLight
                                    SyncStateStatus.ERROR -> Color(0xFFEF4444)
                                    else -> GoldAccent
                                }
                            )
                        }
                    }
                }
            }

            // 1. Profil & Kas Mukmin
            item {
                SettingsSectionCard(
                    title = "Profil & Entitas Kas",
                    icon = Icons.Default.Person
                ) {
                    SettingsRowItem(
                        title = "Nama Profil Kas",
                        subtitle = state.userNameKasMukmin,
                        trailingContent = {
                            IconButton(onClick = {
                                tempUserName = state.userNameKasMukmin
                                tempStartDay = state.startDayOfMonth
                                tempCurrency = state.primaryCurrencySymbol
                                showEditProfileDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profil",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowItem(
                        title = "Mata Uang Utama",
                        subtitle = "Simbol: ${state.primaryCurrencySymbol} (Indonesian Rupiah)"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowItem(
                        title = "Tanggal Awal Buku Kas",
                        subtitle = "Tanggal ${state.startDayOfMonth} setiap bulan kalender"
                    )
                }
            }

            // 2. Standar Nisab & Harga Emas
            item {
                SettingsSectionCard(
                    title = "Standar Nisab & Haul Zakat",
                    icon = Icons.Default.MonetizationOn
                ) {
                    SettingsRowItem(
                        title = "Harga Acuan Emas Murni",
                        subtitle = "Rp ${formatNumberRp(state.goldPricePerGram)} / gram",
                        trailingContent = {
                            IconButton(onClick = {
                                tempGoldPrice = state.goldPricePerGram.toLong().toString()
                                showEditGoldPriceDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Ubah Harga Emas",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowItem(
                        title = "Batas Nisab Harta (85 Gram Emas)",
                        subtitle = "Rp ${formatNumberRp(state.nisabThreshold)} (Dihitung otomatis)"
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowItem(
                        title = "Koreksi Kalender Hijriah",
                        subtitle = if (state.selectedHijriOffset == 0) "Sesuai Standar Kemenag RI (0 hari)"
                        else "${if (state.selectedHijriOffset > 0) "+" else ""}${state.selectedHijriOffset} hari penyesuaian",
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { viewModel.updateHijriOffset(state.selectedHijriOffset - 1) },
                                    enabled = state.selectedHijriOffset > -2
                                ) {
                                    Text("-1", fontWeight = FontWeight.Bold, color = EmeraldLight)
                                }
                                TextButton(
                                    onClick = { viewModel.updateHijriOffset(state.selectedHijriOffset + 1) },
                                    enabled = state.selectedHijriOffset < 2
                                ) {
                                    Text("+1", fontWeight = FontWeight.Bold, color = EmeraldLight)
                                }
                            }
                        }
                    )
                }
            }

            // 3. Aturan Infaq & Virtual Vault
            item {
                SettingsSectionCard(
                    title = "Aturan Infaq & Virtual Vault",
                    icon = Icons.Default.VolunteerActivism
                ) {
                    SettingsRowToggle(
                        title = "Otomatis Potong Infaq Kasab",
                        subtitle = "Setiap pemasukan gaji/usaha otomatis disisihkan ke Virtual Vault",
                        isChecked = state.isAutoDeductInfaqEnabled,
                        onCheckedChange = { viewModel.updateAutoDeductInfaq(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tarif Default Infaq Kasab",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(state.defaultInfaqRate * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                        Slider(
                            value = (state.defaultInfaqRate * 100).toFloat(),
                            onValueChange = { viewModel.updateDefaultInfaqRate(it.toDouble() / 100.0) },
                            valueRange = 1f..25f,
                            steps = 23,
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = GoldAccent,
                                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowToggle(
                        title = "Peringatan Israf (Boros)",
                        subtitle = "Notifikasi jika belanja konsumsi mendekati ${state.israfWarningThresholdPercent}% batas anggaran",
                        isChecked = state.israfWarningThresholdPercent > 0,
                        onCheckedChange = { enabled ->
                            viewModel.updateIsrafWarningThreshold(if (enabled) 80 else 0)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowToggle(
                        title = "Penegakan Anggaran Ketat",
                        subtitle = "Tampilkan peringatan tegas saat mencatat transaksi di atas pagu kategori",
                        isChecked = state.isStrictBudgetEnforced,
                        onCheckedChange = { viewModel.updateStrictBudgetEnforced(it) }
                    )
                }
            }

            // 4. Ibadah & Sedekah Subuh
            item {
                SettingsSectionCard(
                    title = "Ibadah & Motivasi Harian",
                    icon = Icons.Default.VolunteerActivism
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Sedekah Subuh Streak",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${state.sedekahSubuhTargetDays} Hari",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                        }
                        Slider(
                            value = state.sedekahSubuhTargetDays.toFloat(),
                            onValueChange = { viewModel.updateSedekahSubuhTargetDays(it.toInt()) },
                            valueRange = 7f..100f,
                            steps = 92,
                            colors = SliderDefaults.colors(
                                thumbColor = EmeraldLight,
                                activeTrackColor = EmeraldLight,
                                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowToggle(
                        title = "Hadits Hikmah Harian",
                        subtitle = "Tampilkan petikan hadits & motivasi syariah di beranda",
                        isChecked = state.showDailyHadith,
                        onCheckedChange = { viewModel.updateShowDailyHadith(it) }
                    )
                }
            }

            // 5. Tampilan & Aksesibilitas
            item {
                SettingsSectionCard(
                    title = "Tampilan & Aksesibilitas",
                    icon = Icons.Default.Brightness4
                ) {
                    SettingsRowToggle(
                        title = "Mode Gelap (Dark Mode)",
                        subtitle = if (state.isDarkMode) "Tema gelap Islami aktif (Emerald-Dark)" else "Tema terang aktif",
                        isChecked = state.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowToggle(
                        title = "Kontras Tinggi (High Contrast)",
                        subtitle = "Meningkatkan ketajaman teks dan garis batas untuk kemudahan membaca",
                        isChecked = state.isHighContrast,
                        onCheckedChange = { viewModel.toggleHighContrast() }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    SettingsRowToggle(
                        title = "Privasi Saldo (Mask Balance)",
                        subtitle = "Samarkan nilai saldo dengan asteris (Rp ••••••)",
                        isChecked = state.securityConfig.isMaskBalance,
                        onCheckedChange = { viewModel.toggleBalancePrivacy() }
                    )
                }
            }

            // 6. Keamanan & Kunci Aplikasi
            item {
                SettingsSectionCard(
                    title = "Keamanan & Kunci Aplikasi",
                    icon = Icons.Default.Shield
                ) {
                    SettingsRowToggle(
                        title = "Kunci PIN Aplikasi",
                        subtitle = if (state.securityConfig.isPinEnabled) "PIN keamanan 6-digit aktif" else "Kunci PIN belum diaktifkan",
                        isChecked = state.securityConfig.isPinEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                newPinInput = ""
                                pinError = null
                                showPinSetupDialog = true
                            } else {
                                viewModel.disablePinLock()
                            }
                        }
                    )
                    if (state.securityConfig.isPinEnabled) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        SettingsRowToggle(
                            title = "Autentikasi Biometrik (Sidik Jari)",
                            subtitle = "Gunakan sidik jari atau Face Unlock untuk membuka aplikasi",
                            isChecked = state.securityConfig.isBiometricEnabled,
                            onCheckedChange = { viewModel.toggleBiometric(it) }
                        )
                    }
                }
            }

            // 7. Reset Pengaturan
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showClearDummyConfirmDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hapus Semua Data Dummy (Buku Kas Bersih)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Text(
                                text = "Hapus data contoh transaksi, anggaran, kantong, & hutang piutang",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            viewModel.loadDummyData()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Muat Ulang Data Contoh / Dummy",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tampilkan kembali simulasi lengkap seluruh fitur syariah",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showResetConfirmDialog = true },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kembalikan Preferensi Bawaan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Mengatur ulang semua preferensi lokal ke setelan standar",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialog Edit Profil
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("Ubah Profil Kas Mukmin", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = tempUserName,
                        onValueChange = { tempUserName = it },
                        label = { Text("Nama Kas / Keluarga") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldLight,
                            cursorColor = EmeraldLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempCurrency,
                        onValueChange = { tempCurrency = it },
                        label = { Text("Simbol Mata Uang") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldLight,
                            cursorColor = EmeraldLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempUserName.isNotBlank()) {
                            viewModel.updateUserName(tempUserName.trim())
                        }
                        if (tempCurrency.isNotBlank()) {
                            viewModel.updateCurrencySymbol(tempCurrency.trim())
                        }
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Edit Harga Emas
    if (showEditGoldPriceDialog) {
        AlertDialog(
            onDismissRequest = { showEditGoldPriceDialog = false },
            title = {
                Text("Ubah Acuan Harga Emas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Harga per gram emas murni (24 Karat) digunakan untuk menghitung nisab zakat maal & haul otomatis.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempGoldPrice,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                tempGoldPrice = input
                            }
                        },
                        label = { Text("Harga per gram (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            cursorColor = GoldAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempGoldPrice.toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            viewModel.updateGoldPrice(parsed)
                        }
                        showEditGoldPriceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditGoldPriceDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Setup PIN Baru
    if (showPinSetupDialog) {
        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = {
                Text("Setel PIN Keamanan Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Masukkan 6 digit angka rahasia untuk mengunci aplikasi.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) newPinInput = it },
                        label = { Text("PIN 6 Digit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldLight,
                            cursorColor = EmeraldLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Text(
                            text = pinError ?: "",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 6) {
                            viewModel.setupPinLock(newPinInput)
                            showPinSetupDialog = false
                        } else {
                            pinError = "PIN harus tepat 6 digit angka."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Aktifkan PIN", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetupDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Clear Dummy Confirm
    if (showClearDummyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearDummyConfirmDialog = false },
            title = {
                Text("Hapus Semua Data Dummy?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Semua data contoh (transaksi kas, anggaran belanja, kantong kas, target ibadah, hutang/piutang qardh, dan sedekah subuh) akan dihapus sehingga buku kas Anda bersih untuk mulai mencatat keuangan pribadi.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDummyData()
                        showClearDummyConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ya, Hapus Data Dummy", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDummyConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog Reset Confirm
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Kembalikan Setelan Pabrik?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Semua preferensi tampilan dan konfigurasi lokal akan dikembalikan ke nilai awal.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllSettingsToDefault()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Reset", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            content()
        }
    }
}

@Composable
private fun SettingsRowItem(
    title: String,
    subtitle: String,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
private fun SettingsRowToggle(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldLight,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

private fun formatNumberRp(value: Double): String {
    return try {
        java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID")).format(value)
    } catch (_: Exception) {
        value.toLong().toString()
    }
}

package com.example.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.datastore.AppDisplayMode
import com.example.core.feature.AppFeatureVisibilityConfig
import com.example.core.feature.FeaturePresetMode
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberTacticalGold
import com.example.ui.theme.isTactical

@Composable
fun FeatureCustomizationSettingsSection(
    state: AmanahLedgerUiState,
    onApplyPreset: (FeaturePresetMode) -> Unit,
    onUpdateConfig: (AppFeatureVisibilityConfig) -> Unit,
    onSetDisplayMode: (AppDisplayMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isTactical = state.themeMode.isTactical
    val config = state.featureVisibilityConfig

    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp)
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
    val smallBadgeShape = if (isTactical) CutCornerShape(3.dp) else RoundedCornerShape(6.dp)

    var isSyariahAccordionExpanded by remember { mutableStateOf(true) }
    var isPlanningAccordionExpanded by remember { mutableStateOf(false) }
    var isOperationalAccordionExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER KARTU UTAMA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("feature_customization_main_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = cardShape,
            border = BorderStroke(
                1.dp,
                if (isTactical) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Judul & Badge Ringkasan Fitur Aktif
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(pillShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                                .then(
                                    if (isTactical) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, pillShape) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Kustomisasi Modul & Fitur",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pilih fitur yang ingin ditampilkan di aplikasi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Badge Jumlah Fitur Aktif
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = smallBadgeShape,
                        border = if (isTactical) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Text(
                            text = "${config.totalActiveFeatures}/24 Aktif",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Nonaktifkan modul yang tidak Anda perlukan untuk menyederhanakan sidebar, pintasan beranda, dan widget dashboard.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // -------------------------------------------------------------
                // MODE TAMPILAN APLIKASI (Ringkas vs Lengkap Syariah)
                // -------------------------------------------------------------
                Text(
                    text = "MODE TAMPILAN APLIKASI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTactical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isSimpleActive = state.appDisplayMode == AppDisplayMode.SIMPLE
                    val isCompleteActive = state.appDisplayMode == AppDisplayMode.COMPREHENSIVE

                    DisplayModeOptionCard(
                        title = "Mode Ringkas",
                        subtitle = "Menu esensial & ramah pemula",
                        icon = Icons.Default.AutoAwesome,
                        isActive = isSimpleActive,
                        isTactical = isTactical,
                        shape = pillShape,
                        testTag = "settings_mode_simple",
                        modifier = Modifier.weight(1f),
                        onClick = { onSetDisplayMode(AppDisplayMode.SIMPLE) }
                    )

                    DisplayModeOptionCard(
                        title = "Mode Lengkap",
                        subtitle = "Seluruh instrumen & menu syariah",
                        icon = Icons.Default.Nightlight,
                        isActive = isCompleteActive,
                        isTactical = isTactical,
                        shape = pillShape,
                        testTag = "settings_mode_comprehensive",
                        modifier = Modifier.weight(1f),
                        onClick = { onSetDisplayMode(AppDisplayMode.COMPREHENSIVE) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // -------------------------------------------------------------
                // PRESET PROFIL CEPAT (Ringkas, Bisnis, Lengkap)
                // -------------------------------------------------------------
                Text(
                    text = "PROFIL PRESET CEPAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTactical) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeaturePresetCard(
                        preset = FeaturePresetMode.RINGKAS,
                        icon = Icons.Default.Mosque,
                        isActive = config == AppFeatureVisibilityConfig.createPreset(FeaturePresetMode.RINGKAS),
                        isTactical = isTactical,
                        shape = pillShape,
                        modifier = Modifier.weight(1f),
                        onClick = { onApplyPreset(FeaturePresetMode.RINGKAS) }
                    )

                    FeaturePresetCard(
                        preset = FeaturePresetMode.BISNIS,
                        icon = Icons.Default.BusinessCenter,
                        isActive = config == AppFeatureVisibilityConfig.createPreset(FeaturePresetMode.BISNIS),
                        isTactical = isTactical,
                        shape = pillShape,
                        modifier = Modifier.weight(1f),
                        onClick = { onApplyPreset(FeaturePresetMode.BISNIS) }
                    )

                    FeaturePresetCard(
                        preset = FeaturePresetMode.LENGKAP,
                        icon = Icons.Default.CheckCircle,
                        isActive = config == AppFeatureVisibilityConfig.createPreset(FeaturePresetMode.LENGKAP),
                        isTactical = isTactical,
                        shape = pillShape,
                        modifier = Modifier.weight(1f),
                        onClick = { onApplyPreset(FeaturePresetMode.LENGKAP) }
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // MODUL 1: KEUANGAN SYARIAH & ZISWAF (13 Sub-Fitur)
        // -------------------------------------------------------------
        ModuleAccordionCard(
            title = "1. Keuangan Syariah & ZISWAF",
            icon = Icons.Default.VolunteerActivism,
            activeCount = config.activeSyariahCount,
            totalCount = config.totalSyariahCount,
            isModuleEnabled = config.isSyariahModuleEnabled,
            isExpanded = isSyariahAccordionExpanded,
            isTactical = isTactical,
            cardShape = cardShape,
            pillShape = pillShape,
            testTag = "module_syariah_accordion",
            onToggleModule = { enabled ->
                onUpdateConfig(config.copy(isSyariahModuleEnabled = enabled))
            },
            onToggleExpand = { isSyariahAccordionExpanded = !isSyariahAccordionExpanded }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SubFeatureSwitchRow(
                    title = "Zakat Hub (Profesi/Maal/Fitrah)",
                    description = "Kalkulator hisab zakat komprehensif & 8 asnaf penerima",
                    icon = Icons.Default.VolunteerActivism,
                    isChecked = config.showZakatHub,
                    isTactical = isTactical,
                    testTag = "sub_switch_zakat_hub",
                    onCheckedChange = { onUpdateConfig(config.copy(showZakatHub = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Sedekah Subuh & Streak",
                    description = "Rutinitas sedekah harian dengan pencatat streak istiqomah",
                    icon = Icons.Default.WbSunny,
                    isChecked = config.showSedekahSubuh,
                    isTactical = isTactical,
                    testTag = "sub_switch_sedekah_subuh",
                    onCheckedChange = { onUpdateConfig(config.copy(showSedekahSubuh = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Brankas Infaq & Mustahiq",
                    description = "Virtual Vault akumulasi infaq dan riwayat penyaluran",
                    icon = Icons.Default.Savings,
                    isChecked = config.showInfaqVault,
                    isTactical = isTactical,
                    testTag = "sub_switch_infaq_vault",
                    onCheckedChange = { onUpdateConfig(config.copy(showInfaqVault = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Haul & Nisab Tahunan",
                    description = "Pelacak batas nisab 85g emas dan siklus haul 1 tahun",
                    icon = Icons.Default.MenuBook,
                    isChecked = config.showHaulNisab,
                    isTactical = isTactical,
                    testTag = "sub_switch_haul_nisab",
                    onCheckedChange = { onUpdateConfig(config.copy(showHaulNisab = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Pelacak Emas Acuan BAZNAS",
                    description = "Harga acuan hisab emas murni sesuai ketetapan BAZNAS",
                    icon = Icons.Default.MonetizationOn,
                    isChecked = config.showGoldNisabTracker,
                    isTactical = isTactical,
                    testTag = "sub_switch_gold_tracker",
                    onCheckedChange = { onUpdateConfig(config.copy(showGoldNisabTracker = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Zakat Perniagaan & Portofolio",
                    description = "Hisab zakat dagang, aset lancar bisnis & saham syariah",
                    icon = Icons.Default.BusinessCenter,
                    isChecked = config.showBusinessZakat,
                    isTactical = isTactical,
                    testTag = "sub_switch_business_zakat",
                    onCheckedChange = { onUpdateConfig(config.copy(showBusinessZakat = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Audit Kepatuhan Syariah",
                    description = "Health check transaksi bebas riba, gharar & maysir",
                    icon = Icons.Default.VerifiedUser,
                    isChecked = config.showShariahAudit,
                    isTactical = isTactical,
                    testTag = "sub_switch_shariah_audit",
                    onCheckedChange = { onUpdateConfig(config.copy(showShariahAudit = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Aturan Infaq Kasab Otomatis",
                    description = "Penyisihan sedekah persentase dari tiap pendapatan masuk",
                    icon = Icons.Default.AutoFixHigh,
                    isChecked = config.showAutoInfaqRules,
                    isTactical = isTactical,
                    testTag = "sub_switch_auto_infaq",
                    onCheckedChange = { onUpdateConfig(config.copy(showAutoInfaqRules = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Hutang Piutang (Qardhul Hasan)",
                    description = "Pencatatan pinjaman kebajikan tanpa bunga dan jatuh tempo",
                    icon = Icons.Default.Handshake,
                    isChecked = config.showQardhHasan,
                    isTactical = isTactical,
                    testTag = "sub_switch_qardh",
                    onCheckedChange = { onUpdateConfig(config.copy(showQardhHasan = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Direktori Lembaga Amil ZISWAF",
                    description = "Katalog lembaga amil resmi BAZNAS dan LAZ terverifikasi",
                    icon = Icons.Default.LocationCity,
                    isChecked = config.showAmilDirectory,
                    isTactical = isTactical,
                    testTag = "sub_switch_amil_dir",
                    onCheckedChange = { onUpdateConfig(config.copy(showAmilDirectory = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Kalkulator Waris (Faraidh)",
                    description = "Penghitungan pembagian tirkah dan ashabul furudh syar'i",
                    icon = Icons.Default.Calculate,
                    isChecked = config.showFaraidhCalculator,
                    isTactical = isTactical,
                    testTag = "sub_switch_faraidh",
                    onCheckedChange = { onUpdateConfig(config.copy(showFaraidhCalculator = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Fatwa & Fiqih Muamalah DSN-MUI",
                    description = "Basis dalil Al-Qur'an, hadits & fatwa Dewan Syariah Nasional",
                    icon = Icons.Default.MenuBook,
                    isChecked = config.showIslamicGrounding,
                    isTactical = isTactical,
                    testTag = "sub_switch_grounding",
                    onCheckedChange = { onUpdateConfig(config.copy(showIslamicGrounding = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Pusat Kustomisasi Rules Syariah",
                    description = "Penyesuaian parameter hisab, ambang batas & bobot",
                    icon = Icons.Default.Tune,
                    isChecked = config.showShariahRulesCustomization,
                    isTactical = isTactical,
                    testTag = "sub_switch_rules_custom",
                    onCheckedChange = { onUpdateConfig(config.copy(showShariahRulesCustomization = it)) }
                )
            }
        }

        // -------------------------------------------------------------
        // MODUL 2: PERENCANAAN & LAPORAN (7 Sub-Fitur)
        // -------------------------------------------------------------
        ModuleAccordionCard(
            title = "2. Perencanaan & Laporan",
            icon = Icons.Default.PieChart,
            activeCount = config.activePlanningCount,
            totalCount = config.totalPlanningCount,
            isModuleEnabled = config.isPlanningModuleEnabled,
            isExpanded = isPlanningAccordionExpanded,
            isTactical = isTactical,
            cardShape = cardShape,
            pillShape = pillShape,
            testTag = "module_planning_accordion",
            onToggleModule = { enabled ->
                onUpdateConfig(config.copy(isPlanningModuleEnabled = enabled))
            },
            onToggleExpand = { isPlanningAccordionExpanded = !isPlanningAccordionExpanded }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SubFeatureSwitchRow(
                    title = "Target Tabungan Ibadah",
                    description = "Niat dan progres tabungan Qurban, Aqiqah, Umroh & Haji",
                    icon = Icons.Default.Savings,
                    isChecked = config.showIbadahGoals,
                    isTactical = isTactical,
                    testTag = "sub_switch_ibadah_goals",
                    onCheckedChange = { onUpdateConfig(config.copy(showIbadahGoals = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Anggaran Belanja & Pagu Pos",
                    description = "Plafon anggaran per kategori dengan indikator israf",
                    icon = Icons.Default.PieChart,
                    isChecked = config.showBudgetAllocation,
                    isTactical = isTactical,
                    testTag = "sub_switch_budget",
                    onCheckedChange = { onUpdateConfig(config.copy(showBudgetAllocation = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Analisis & Israf",
                    description = "Grafik distribusi pengeluaran & Spiritual Liquidity Index",
                    icon = Icons.Default.Analytics,
                    isChecked = config.showAnalytics,
                    isTactical = isTactical,
                    testTag = "sub_switch_analytics",
                    onCheckedChange = { onUpdateConfig(config.copy(showAnalytics = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Laporan Bulanan Kas",
                    description = "Rekap mutasi, pemasukan, pengeluaran & penyaluran ZIS",
                    icon = Icons.Default.ReceiptLong,
                    isChecked = config.showMonthlyReport,
                    isTactical = isTactical,
                    testTag = "sub_switch_monthly_report",
                    onCheckedChange = { onUpdateConfig(config.copy(showMonthlyReport = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Arsip Bukti & Struk Transaksi",
                    description = "Galeri dokumen kuitansi & nota pembelanjaan",
                    icon = Icons.Default.PhotoLibrary,
                    isChecked = config.showReceiptArchive,
                    isTactical = isTactical,
                    testTag = "sub_switch_receipt_archive",
                    onCheckedChange = { onUpdateConfig(config.copy(showReceiptArchive = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Ekspor Laporan PDF & CSV",
                    description = "Unduh salinan pembukuan kas untuk arsip dan cetak",
                    icon = Icons.Default.FileDownload,
                    isChecked = config.showExportReport,
                    isTactical = isTactical,
                    testTag = "sub_switch_export_report",
                    onCheckedChange = { onUpdateConfig(config.copy(showExportReport = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Laporan Zakat Maal & Fitrah",
                    description = "Rekap nisab, haul, daftar tanggungan & harga bahan pokok lokal",
                    icon = Icons.Default.VolunteerActivism,
                    isChecked = config.showZakatReport,
                    isTactical = isTactical,
                    testTag = "sub_switch_zakat_report",
                    onCheckedChange = { onUpdateConfig(config.copy(showZakatReport = it)) }
                )
            }
        }

        // -------------------------------------------------------------
        // MODUL 3: OPERASIONAL & PEMBUKUAN KAS (4 Sub-Fitur)
        // -------------------------------------------------------------
        ModuleAccordionCard(
            title = "3. Operasional & Kas Mukmin",
            icon = Icons.Default.AccountBalance,
            activeCount = config.activeOperationalCount,
            totalCount = config.totalOperationalCount,
            isModuleEnabled = config.isOperationalModuleEnabled,
            isExpanded = isOperationalAccordionExpanded,
            isTactical = isTactical,
            cardShape = cardShape,
            pillShape = pillShape,
            testTag = "module_operational_accordion",
            onToggleModule = { enabled ->
                onUpdateConfig(config.copy(isOperationalModuleEnabled = enabled))
            },
            onToggleExpand = { isOperationalAccordionExpanded = !isOperationalAccordionExpanded }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SubFeatureSwitchRow(
                    title = "Multi-Wallet (Banyak Kantong)",
                    description = "Kelola dompet tunai, rekening bank syariah & e-wallet",
                    icon = Icons.Default.AccountBalance,
                    isChecked = config.showMultiWallet,
                    isTactical = isTactical,
                    testTag = "sub_switch_multi_wallet",
                    onCheckedChange = { onUpdateConfig(config.copy(showMultiWallet = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Transaksi Rutin & Terjadwal",
                    description = "Otomasi tagihan bulanan & infaq terprogram berkala",
                    icon = Icons.Default.Schedule,
                    isChecked = config.showRecurringTransactions,
                    isTactical = isTactical,
                    testTag = "sub_switch_recurring",
                    onCheckedChange = { onUpdateConfig(config.copy(showRecurringTransactions = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Kalender Hijriyah & Masehi",
                    description = "Sinkronisasi tarikh Islam dan pengingat puasa sunnah",
                    icon = Icons.Default.CalendarMonth,
                    isChecked = config.showDualCalendar,
                    isTactical = isTactical,
                    testTag = "sub_switch_calendar",
                    onCheckedChange = { onUpdateConfig(config.copy(showDualCalendar = it)) }
                )
                SubFeatureSwitchRow(
                    title = "Rezeki Non-Tunai & Hadiah",
                    description = "Pencatatan berkah natura, bingkisan & komitmen infaq syukur",
                    icon = Icons.Default.CardGiftcard,
                    isChecked = config.showRezekiNonTunai,
                    isTactical = isTactical,
                    testTag = "sub_switch_rezeki",
                    onCheckedChange = { onUpdateConfig(config.copy(showRezekiNonTunai = it)) }
                )
            }
        }
    }
}

@Composable
private fun DisplayModeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    isTactical: Boolean,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val activeBorderColor = MaterialTheme.colorScheme.primary
    val activeBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val inactiveBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Surface(
        color = if (isActive) activeBgColor else inactiveBgColor,
        shape = shape,
        border = BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) activeBorderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) activeBorderColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isActive) activeBorderColor else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = subtitle,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isActive) {
                Surface(
                    color = activeBorderColor,
                    shape = if (isTactical) CutCornerShape(2.dp) else CircleShape
                ) {
                    Text(
                        text = "AKTIF",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturePresetCard(
    preset: FeaturePresetMode,
    icon: ImageVector,
    isActive: Boolean,
    isTactical: Boolean,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val activeBorderColor = MaterialTheme.colorScheme.primary
    val activeBgColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    val inactiveBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Surface(
        color = if (isActive) activeBgColor else inactiveBgColor,
        shape = shape,
        border = BorderStroke(
            if (isActive) 1.5.dp else 1.dp,
            if (isActive) activeBorderColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag("preset_card_${preset.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) activeBorderColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = preset.iconDescription,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) activeBorderColor else MaterialTheme.colorScheme.onSurface
            )
            if (isActive) {
                Surface(
                    color = activeBorderColor,
                    shape = if (isTactical) CutCornerShape(2.dp) else CircleShape
                ) {
                    Text(
                        text = "AKTIF",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleAccordionCard(
    title: String,
    icon: ImageVector,
    activeCount: Int,
    totalCount: Int,
    isModuleEnabled: Boolean,
    isExpanded: Boolean,
    isTactical: Boolean,
    cardShape: androidx.compose.ui.graphics.Shape,
    pillShape: androidx.compose.ui.graphics.Shape,
    testTag: String,
    onToggleModule: (Boolean) -> Unit,
    onToggleExpand: () -> Unit,
    content: @Composable () -> Unit
) {
    val borderColor = if (isModuleEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isModuleEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = cardShape,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Master Header Row (Module Title, Count Badge, Switch, and Expand Icon)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(pillShape)
                            .background(
                                if (isModuleEnabled) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                }
                            )
                            .then(
                                if (isTactical && isModuleEnabled) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, pillShape) else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isModuleEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                                color = if (isModuleEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                color = if (isModuleEnabled) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                },
                                shape = if (isTactical) CutCornerShape(3.dp) else RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "$activeCount/$totalCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    color = if (isModuleEnabled) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isModuleEnabled) "Modul aktif • ${if (isExpanded) "Ketuk untuk ciutkan" else "Ketuk untuk rincian"}" else "Seluruh modul dinonaktifkan",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isModuleEnabled,
                        onCheckedChange = onToggleModule,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("${testTag}_master_switch")
                    )

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Ciutkan" else "Buka Rincian",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = isExpanded && isModuleEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    content()
                }
            }
        }
    }
}

@Composable
private fun SubFeatureSwitchRow(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    isTactical: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = description,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

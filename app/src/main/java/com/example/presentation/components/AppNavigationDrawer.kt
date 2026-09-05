package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Screen
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AppNavigationDrawerContent(
    currentScreen: Screen,
    uiState: AmanahLedgerUiState,
    onSelectScreen: (Screen) -> Unit,
    onCloseDrawer: () -> Unit,
    onToggleTheme: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onLockAppNow: () -> Unit
) {
    val hijriDate = HijriCalendarEngine.fromGregorian()
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .testTag("app_navigation_sidebar_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // ==========================================
            // SIDEBAR HEADER (ISLAMIC MOTIF & PROFILE)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                EmeraldDark,
                                EmeraldPrimary.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldPrimary)
                                    .border(1.5.dp, GoldAccent, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Nightlight,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "AMANAH LEDGER",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Sistem Akuntansi Syariah",
                                    fontSize = 11.sp,
                                    color = EmeraldLight
                                )
                            }
                        }

                        IconButton(
                            onClick = onCloseDrawer,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("sidebar_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup Sidebar",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Profil Kas Mukmin & Hijri Card
                    Surface(
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.userNameKasMukmin,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = hijriDate.toString(),
                                    fontSize = 10.sp,
                                    color = GoldLight
                                )
                            }

                            // Quick Mask Indicator
                            IconButton(
                                onClick = onTogglePrivacy,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("sidebar_quick_privacy_toggle")
                            ) {
                                Icon(
                                    imageVector = if (uiState.securityConfig.isMaskBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Privasi Saldo",
                                    tint = if (uiState.securityConfig.isMaskBalance) GoldAccent else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // KELOMPOK 1: BUKU KAS & TRANSAKSI
            // ==========================================
            SidebarCategoryHeader(title = "BUKU BESAR & KAS")

            SidebarNavigationItem(
                label = "Beranda & Ringkasan Kas",
                icon = Icons.Default.Home,
                selected = currentScreen == Screen.DASHBOARD,
                testTag = "sidebar_nav_dashboard",
                onClick = { onSelectScreen(Screen.DASHBOARD) }
            )

            SidebarNavigationItem(
                label = "Catat Transaksi Baru",
                icon = Icons.Default.AddCircle,
                selected = currentScreen == Screen.ADD_TRANSACTION,
                badgeText = "+ Entri",
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_add_transaction",
                onClick = { onSelectScreen(Screen.ADD_TRANSACTION) }
            )

            SidebarNavigationItem(
                label = "Kantong Rekening & Mutasi",
                icon = Icons.Default.AccountBalance,
                selected = currentScreen == Screen.MULTI_WALLET,
                badgeText = "${uiState.wallets.size} Kantong",
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_multi_wallet",
                onClick = { onSelectScreen(Screen.MULTI_WALLET) }
            )

            SidebarNavigationItem(
                label = "Transaksi Rutin & Berulang",
                icon = Icons.Default.Schedule,
                selected = currentScreen == Screen.RECURRING_TRANSACTIONS,
                badgeText = if (uiState.dueRecurringCount > 0) "${uiState.dueRecurringCount} Jatuh Tempo" else null,
                badgeColor = if (uiState.dueRecurringCount > 0) Color(0xFFFF7043) else null,
                testTag = "sidebar_nav_recurring",
                onClick = { onSelectScreen(Screen.RECURRING_TRANSACTIONS) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // ==========================================
            // KELOMPOK 2: FILANTROPI & SYARIAH
            // ==========================================
            SidebarCategoryHeader(title = "KEUANGAN SYARIAH")

            SidebarNavigationItem(
                label = "Zakat Hub (Profesi/Tijarah/Fitrah)",
                icon = Icons.Default.VolunteerActivism,
                selected = currentScreen == Screen.ZAKAT_HUB,
                badgeText = if (uiState.isNisabReached) "Wajib Zakat" else "Komprehensif",
                badgeColor = GoldAccent,
                testTag = "sidebar_nav_zakat_hub",
                onClick = { onSelectScreen(Screen.ZAKAT_HUB) }
            )

            SidebarNavigationItem(
                label = "Sedekah Subuh & Streak",
                icon = Icons.Default.WbSunny,
                selected = currentScreen == Screen.SEDEKAH_SUBUH,
                badgeText = if (uiState.sedekahSubuhState.currentStreak > 0) "🔥 ${uiState.sedekahSubuhState.currentStreak} Hari" else null,
                badgeColor = GoldAccent,
                testTag = "sidebar_nav_sedekah_subuh",
                onClick = { onSelectScreen(Screen.SEDEKAH_SUBUH) }
            )

            SidebarNavigationItem(
                label = "Brankas Infaq & Mustahiq",
                icon = Icons.Default.Savings,
                selected = currentScreen == Screen.VAULT_HISTORY,
                badgeText = if (uiState.securityConfig.isMaskBalance) "Rp •••" else "Rp ${nf.format(uiState.virtualInfaqVaultBalance)}",
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_vault_history",
                onClick = { onSelectScreen(Screen.VAULT_HISTORY) }
            )

            SidebarNavigationItem(
                label = "Haul & Nisab Tahunan",
                icon = Icons.Default.MenuBook,
                selected = currentScreen == Screen.HAUL_NISAB,
                badgeText = if (uiState.isNisabReached) "Tercapai" else "Dipantau",
                badgeColor = if (uiState.isNisabReached) GoldAccent else null,
                testTag = "sidebar_nav_haul_nisab",
                onClick = { onSelectScreen(Screen.HAUL_NISAB) }
            )

            SidebarNavigationItem(
                label = "Aturan Infaq Otomatis",
                icon = Icons.Default.AutoFixHigh,
                selected = currentScreen == Screen.INFAQ_RULES,
                badgeText = "${uiState.rules.size} Aturan",
                testTag = "sidebar_nav_infaq_rules",
                onClick = { onSelectScreen(Screen.INFAQ_RULES) }
            )

            SidebarNavigationItem(
                label = "Hutang Piutang (Qardhul Hasan)",
                icon = Icons.Default.Handshake,
                selected = currentScreen == Screen.QARDH,
                badgeText = if (uiState.qardhRecords.isNotEmpty()) "${uiState.qardhRecords.size} Akad" else null,
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_qardh",
                onClick = { onSelectScreen(Screen.QARDH) }
            )

            SidebarNavigationItem(
                label = "Direktori Lembaga Amil ZISWAF",
                icon = Icons.Default.LocationCity,
                selected = currentScreen == Screen.AMIL_DIRECTORY,
                badgeText = "BAZNAS & LAZ",
                badgeColor = GoldAccent,
                testTag = "sidebar_nav_amil_directory",
                onClick = { onSelectScreen(Screen.AMIL_DIRECTORY) }
            )

            SidebarNavigationItem(
                label = "Kalkulator Waris (Faraidh)",
                icon = Icons.Default.Calculate,
                selected = currentScreen == Screen.FARAIDH_CALCULATOR,
                badgeText = "Hukum Waris",
                testTag = "sidebar_nav_faraidh",
                onClick = { onSelectScreen(Screen.FARAIDH_CALCULATOR) }
            )

            SidebarNavigationItem(
                label = "Fatwa & Fiqih Muamalah DSN-MUI",
                icon = Icons.Default.MenuBook,
                selected = currentScreen == Screen.ISLAMIC_GROUNDING,
                badgeText = "Fatwa & Nisab",
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_islamic_grounding",
                onClick = { onSelectScreen(Screen.ISLAMIC_GROUNDING) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // ==========================================
            // KELOMPOK 3: PERENCANAAN & ANALISIS
            // ==========================================
            SidebarCategoryHeader(title = "PERENCANAAN & LAPORAN")

            SidebarNavigationItem(
                label = "Target Ibadah (Qurban / Haji)",
                icon = Icons.Default.Savings,
                selected = currentScreen == Screen.IBADAH_GOALS,
                badgeText = "${uiState.ibadahGoals.size} Program",
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_ibadah_goals",
                onClick = { onSelectScreen(Screen.IBADAH_GOALS) }
            )

            SidebarNavigationItem(
                label = "Anggaran & Batas Pos",
                icon = Icons.Default.PieChart,
                selected = currentScreen == Screen.BUDGET_ALLOCATION,
                badgeText = if (uiState.overBudgetCount > 0) "${uiState.overBudgetCount} Over" else null,
                badgeColor = if (uiState.overBudgetCount > 0) Color(0xFFFF7043) else null,
                testTag = "sidebar_nav_budget",
                onClick = { onSelectScreen(Screen.BUDGET_ALLOCATION) }
            )

            SidebarNavigationItem(
                label = "Analisis & Israf Guard",
                icon = Icons.Default.Analytics,
                selected = currentScreen == Screen.ANALYTICS,
                badgeText = "SLI ${String.format(Locale.US, "%.0f", uiState.spiritualLiquidityIndex)}%",
                testTag = "sidebar_nav_analytics",
                onClick = { onSelectScreen(Screen.ANALYTICS) }
            )

            SidebarNavigationItem(
                label = "Laporan Bulanan & Ekspor",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                selected = currentScreen == Screen.MONTHLY_REPORT,
                testTag = "sidebar_nav_monthly_report",
                onClick = { onSelectScreen(Screen.MONTHLY_REPORT) }
            )

            SidebarNavigationItem(
                label = "Ekspor PDF & CSV Mutasi",
                icon = Icons.Default.FileDownload,
                selected = currentScreen == Screen.EXPORT_REPORT,
                badgeText = "PDF / CSV",
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_export_report",
                onClick = { onSelectScreen(Screen.EXPORT_REPORT) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // ==========================================
            // KELOMPOK 4: PUSAT PENGATURAN TERPADU
            // ==========================================
            SidebarCategoryHeader(title = "PENGATURAN TERPUSAT & SISTEM")

            // ITEM UTAMA: PENGATURAN TERPUSAT
            Surface(
                color = if (currentScreen == Screen.CENTRAL_SETTINGS) EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectScreen(Screen.CENTRAL_SETTINGS) }
                    .testTag("sidebar_nav_central_settings")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (currentScreen == Screen.CENTRAL_SETTINGS) EmeraldPrimary else GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan Terpusat",
                            tint = if (currentScreen == Screen.CENTRAL_SETTINGS) Color.White else GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pengaturan Terpusat",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentScreen == Screen.CENTRAL_SETTINGS) EmeraldLight else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Konfigurasi seluruh modul & fitur",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = GoldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "HUB",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            SidebarNavigationItem(
                label = "Cadangan Data & Restore",
                icon = Icons.Default.Security,
                selected = currentScreen == Screen.BACKUP_RESTORE,
                badgeText = "Enkripsi",
                badgeColor = EmeraldLight,
                testTag = "sidebar_nav_backup_restore",
                onClick = { onSelectScreen(Screen.BACKUP_RESTORE) }
            )

            SidebarNavigationItem(
                label = "Keamanan & Kunci PIN",
                icon = Icons.Default.Shield,
                selected = currentScreen == Screen.SECURITY_SETTINGS,
                badgeText = if (uiState.securityConfig.isPinEnabled) "PIN Aktif" else "Nonaktif",
                badgeColor = if (uiState.securityConfig.isPinEnabled) EmeraldLight else null,
                testTag = "sidebar_nav_security",
                onClick = { onSelectScreen(Screen.SECURITY_SETTINGS) }
            )

            SidebarNavigationItem(
                label = "Panduan Interaktif & Fiqh",
                icon = Icons.Default.HelpOutline,
                selected = currentScreen == Screen.INTERACTIVE_GUIDE,
                testTag = "sidebar_nav_guide",
                onClick = { onSelectScreen(Screen.INTERACTIVE_GUIDE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // FOOTER & QUICK UTILITY ACTIONS
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Theme Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleTheme() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("sidebar_footer_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Ganti Tema",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isDarkMode) "Terang" else "Gelap",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Quick Lock
                        if (uiState.securityConfig.isPinEnabled) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onLockAppNow() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("sidebar_footer_quick_lock")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Kunci Sekarang",
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Kunci",
                                    fontSize = 12.sp,
                                    color = EmeraldLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Amanah Ledger v2.4 • 100% Syariah-Compliant",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarCategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = EmeraldLight,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SidebarNavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badgeText: String? = null,
    badgeColor: Color? = null,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (badgeText != null) {
                    Surface(
                        color = (badgeColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = EmeraldPrimary.copy(alpha = 0.12f),
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = EmeraldLight,
            unselectedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 1.dp)
            .testTag(testTag)
    )
}

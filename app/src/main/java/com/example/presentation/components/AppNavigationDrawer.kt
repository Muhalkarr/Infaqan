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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import com.example.ui.theme.isTactical
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.GoldLight
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberTacticalBorder
import com.example.ui.theme.CyberTacticalGold
import com.example.ui.theme.CyberTerminalBackground
import com.example.ui.theme.CyberTerminalSurface
import com.example.ui.theme.CyberTerminalTextPrimary
import com.example.ui.theme.CyberTerminalTextSecondary
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.filled.Bolt
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
    onLockAppNow: () -> Unit,
    onOpenFeedback: (() -> Unit)? = null,
    onToggleDisplayMode: (() -> Unit)? = null,
    onOpenSystemDiagnostics: (() -> Unit)? = null
) {
    val isSimpleMode = uiState.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE
    var isSyariahExpanded by remember(isSimpleMode) { mutableStateOf(!isSimpleMode) }
    var isPlanningExpanded by remember(isSimpleMode) { mutableStateOf(!isSimpleMode) }
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
            // SIDEBAR HEADER (ISLAMIC MOTIF & PROFILE / PROTOKOL TAKTIS DOSSIER)
            // ==========================================
            if (uiState.themeMode.isTactical) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent))
                            )
                        )
                        .padding(18.dp)
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
                                        .size(42.dp)
                                        .clip(CutCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CutCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "KONSOL BUKU BESAR // SYARIAH",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "PROTOKOL KEUANGAN AMANAH",
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CutCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), CutCornerShape(6.dp))
                                    .clickable { onCloseDrawer() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup Sidebar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Profil Operator Taktis
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "OPERATOR: ${uiState.userNameKasMukmin.uppercase()}",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(modifier = Modifier.size(5.dp).background(MaterialTheme.colorScheme.primary, CutCornerShape(1.dp)))
                                    }
                                    Text(
                                        text = "CLEARANCE: ALPHA // $hijriDate",
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = onTogglePrivacy,
                                    modifier = Modifier.size(32.dp).testTag("sidebar_quick_privacy_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (uiState.securityConfig.isMaskBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Privasi Saldo",
                                        tint = if (uiState.securityConfig.isMaskBalance) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mode Switcher Banner (Tactical Theme)
                        val isTacticalSimple = uiState.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(6.dp))
                                .background(
                                    if (isTacticalSimple)
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                )
                                .border(
                                    1.dp,
                                    if (isTacticalSimple)
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    CutCornerShape(6.dp)
                                )
                                .clickable { onToggleDisplayMode?.invoke() }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("sidebar_mode_toggle")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isTacticalSimple) Icons.Default.AutoAwesome else Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = if (isTacticalSimple) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isTacticalSimple) "MODE RINGKAS (PEMULA)" else "MODE LENGKAP SYARIAH",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isTacticalSimple) "Ketuk ganti ke Mode Lengkap" else "Ketuk ganti ke Mode Ringkas",
                                            fontFamily = ShareTechMonoFontFamily,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Ganti Mode Tampilan",
                                    tint = if (isTacticalSimple) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    EmeraldDark,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.95f)
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
                                        .background(MaterialTheme.colorScheme.primary)
                                        .border(1.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
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
                                        color = Color.White.copy(alpha = 0.8f)
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
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profil Kas Mukmin & Hijri Card
                        Surface(
                            color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f),
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
                                        color = Color.White.copy(alpha = 0.7f)
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
                                        tint = if (uiState.securityConfig.isMaskBalance) MaterialTheme.colorScheme.secondary else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                    // Mode Switcher Banner
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleDisplayMode?.invoke() }
                            .testTag("sidebar_mode_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) Icons.Default.AutoAwesome else Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (uiState.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) "Mode Ringkas (Pemula)" else "Mode Lengkap Syariah",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (uiState.appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) "Ketuk ganti ke Mode Lengkap" else "Ketuk ganti ke Mode Ringkas",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Ganti Mode",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isSimpleMode) {
                // ==========================================
                // MODE RINGKAS: 3 SEKSI ESENSIAL RAMAH AWAM
                // ==========================================
                SidebarCategoryHeader(title = "BUKU KAS & KEUANGAN")

                SidebarNavigationItem(
                    label = "Beranda & Ringkasan Kas",
                    subtitle = "Arus kas harian & saldo dompet",
                    icon = Icons.Default.Home,
                    selected = currentScreen == Screen.DASHBOARD,
                    testTag = "sidebar_nav_dashboard",
                    onClick = { onSelectScreen(Screen.DASHBOARD) }
                )

                SidebarNavigationItem(
                    label = "Catat Transaksi Baru",
                    subtitle = "Pemasukan, pengeluaran & sedekah",
                    icon = Icons.Default.AddCircle,
                    selected = currentScreen == Screen.ADD_TRANSACTION,
                    badgeText = "+ Entri",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    testTag = "sidebar_nav_add_transaction",
                    onClick = { onSelectScreen(Screen.ADD_TRANSACTION) }
                )

                if (uiState.featureVisibilityConfig.isOperationalModuleEnabled && uiState.featureVisibilityConfig.showMultiWallet) {
                    SidebarNavigationItem(
                        label = "Kantong Rekening & Kas",
                        subtitle = "Dompet tunai, rekening bank & e-wallet",
                        icon = Icons.Default.AccountBalance,
                        selected = currentScreen == Screen.MULTI_WALLET,
                        badgeText = "${uiState.wallets.size} Kantong",
                        badgeColor = MaterialTheme.colorScheme.primary,
                        testTag = "sidebar_nav_multi_wallet",
                        onClick = { onSelectScreen(Screen.MULTI_WALLET) }
                    )
                }

                if (uiState.featureVisibilityConfig.isOperationalModuleEnabled && uiState.featureVisibilityConfig.showDualCalendar) {
                    SidebarNavigationItem(
                        label = "Kalender Hijriyah & Masehi",
                        subtitle = "Tarikh Islam & jadwal puasa sunnah",
                        icon = Icons.Default.CalendarMonth,
                        selected = currentScreen == Screen.DUAL_CALENDAR,
                        badgeText = "Tarikh & Puasa",
                        badgeColor = MaterialTheme.colorScheme.secondary,
                        testTag = "sidebar_nav_dual_calendar",
                        onClick = { onSelectScreen(Screen.DUAL_CALENDAR) }
                    )
                }

                val hasSimpleIbadahFeatures = (uiState.featureVisibilityConfig.isSyariahModuleEnabled && (uiState.featureVisibilityConfig.showSedekahSubuh || uiState.featureVisibilityConfig.showZakatHub)) ||
                    (uiState.featureVisibilityConfig.isPlanningModuleEnabled && (uiState.featureVisibilityConfig.showIbadahGoals || uiState.featureVisibilityConfig.showZakatReport))

                if (hasSimpleIbadahFeatures) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    SidebarCategoryHeader(title = "IBADAH & KEUANGAN ISLAM")

                    if (uiState.featureVisibilityConfig.isSyariahModuleEnabled && uiState.featureVisibilityConfig.showSedekahSubuh) {
                        SidebarNavigationItem(
                            label = "Sedekah Subuh Harian",
                            subtitle = "Rutinitas sedekah fajar berkah",
                            icon = Icons.Default.WbSunny,
                            selected = currentScreen == Screen.SEDEKAH_SUBUH,
                            badgeText = if (uiState.sedekahSubuhState.currentStreak > 0) "🔥 ${uiState.sedekahSubuhState.currentStreak} Hari" else null,
                            badgeColor = MaterialTheme.colorScheme.secondary,
                            testTag = "sidebar_nav_sedekah_subuh",
                            onClick = { onSelectScreen(Screen.SEDEKAH_SUBUH) }
                        )
                    }

                    if (uiState.featureVisibilityConfig.isSyariahModuleEnabled && uiState.featureVisibilityConfig.showZakatHub) {
                        SidebarNavigationItem(
                            label = "Zakat Hub & Kalkulator",
                            subtitle = "Hisab zakat profesi & maal mudah",
                            icon = Icons.Default.VolunteerActivism,
                            selected = currentScreen == Screen.ZAKAT_HUB,
                            badgeText = if (uiState.isNisabReached) "Wajib Zakat" else "Hitung",
                            badgeColor = MaterialTheme.colorScheme.secondary,
                            testTag = "sidebar_nav_zakat_hub",
                            onClick = { onSelectScreen(Screen.ZAKAT_HUB) }
                        )
                    }

                    if (uiState.featureVisibilityConfig.isPlanningModuleEnabled && uiState.featureVisibilityConfig.showZakatReport) {
                        SidebarNavigationItem(
                            label = "Laporan Zakat (Maal & Fitrah)",
                            subtitle = "Rekap nisab, haul & tanggungan fitrah",
                            icon = Icons.Default.Assessment,
                            selected = currentScreen == Screen.ZAKAT_REPORT,
                            badgeText = if (uiState.totalZakatReportPayable > 0) "Terhitung" else "Laporan",
                            badgeColor = MaterialTheme.colorScheme.primary,
                            testTag = "sidebar_nav_zakat_report",
                            onClick = { onSelectScreen(Screen.ZAKAT_REPORT) }
                        )
                    }

                    if (uiState.featureVisibilityConfig.isPlanningModuleEnabled && uiState.featureVisibilityConfig.showIbadahGoals) {
                        SidebarNavigationItem(
                            label = "Target Tabungan Ibadah",
                            subtitle = "Niat Qurban, Aqiqah & Umroh",
                            icon = Icons.Default.Savings,
                            selected = currentScreen == Screen.IBADAH_GOALS,
                            badgeText = "${uiState.ibadahGoals.size} Target",
                            badgeColor = MaterialTheme.colorScheme.primary,
                            testTag = "sidebar_nav_ibadah_goals",
                            onClick = { onSelectScreen(Screen.IBADAH_GOALS) }
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                SidebarCategoryHeader(title = "PENGATURAN & BANTUAN")

                SidebarNavigationItem(
                    label = "Pengaturan Terpusat",
                    subtitle = "Tampilan, mata uang & preferensi",
                    icon = Icons.Default.Settings,
                    selected = currentScreen == Screen.CENTRAL_SETTINGS,
                    badgeText = "HUB",
                    badgeColor = MaterialTheme.colorScheme.secondary,
                    testTag = "sidebar_nav_central_settings",
                    onClick = { onSelectScreen(Screen.CENTRAL_SETTINGS) }
                )

                SidebarNavigationItem(
                    label = "Panduan Interaktif & Fiqh",
                    subtitle = "Tutorial fitur lengkap & dasar syariah",
                    icon = Icons.Default.HelpOutline,
                    selected = currentScreen == Screen.INTERACTIVE_GUIDE,
                    testTag = "sidebar_nav_guide",
                    onClick = { onSelectScreen(Screen.INTERACTIVE_GUIDE) }
                )

                SidebarNavigationItem(
                    label = "Cadangan Data & Restore",
                    subtitle = "Backup aman data pembukuan kas",
                    icon = Icons.Default.Security,
                    selected = currentScreen == Screen.BACKUP_RESTORE,
                    badgeText = "Enkripsi",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    testTag = "sidebar_nav_backup_restore",
                    onClick = { onSelectScreen(Screen.BACKUP_RESTORE) }
                )

                // Info card Mode Ringkas
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (onToggleDisplayMode != null) {
                                onToggleDisplayMode.invoke()
                            } else {
                                onSelectScreen(Screen.CENTRAL_SETTINGS)
                            }
                        }
                        .testTag("sidebar_simple_mode_upgrade_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mode Ringkas Aktif",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Memerlukan hisab emas, waris faraidh, audit syariah, atau ekspor laporan? Ketuk kartu ini untuk beralih ke Mode Lengkap.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                // ==========================================
                // MODE LENGKAP: FITUR KOMPREHENSIF DENGAN ACCORDION
                // ==========================================
                SidebarCategoryHeader(title = "BUKU BESAR & KAS")

                SidebarNavigationItem(
                    label = "Beranda & Ringkasan Kas",
                    subtitle = "Arus kas harian & saldo dompet",
                    icon = Icons.Default.Home,
                    selected = currentScreen == Screen.DASHBOARD,
                    testTag = "sidebar_nav_dashboard",
                    onClick = { onSelectScreen(Screen.DASHBOARD) }
                )

                SidebarNavigationItem(
                    label = "Catat Transaksi Baru",
                    subtitle = "Pemasukan, pengeluaran & sedekah",
                    icon = Icons.Default.AddCircle,
                    selected = currentScreen == Screen.ADD_TRANSACTION,
                    badgeText = "+ Entri",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    testTag = "sidebar_nav_add_transaction",
                    onClick = { onSelectScreen(Screen.ADD_TRANSACTION) }
                )

                if (uiState.featureVisibilityConfig.isOperationalModuleEnabled && uiState.featureVisibilityConfig.showDualCalendar) {
                    SidebarNavigationItem(
                        label = "Kalender Hijriyah & Masehi",
                        subtitle = "Tarikh Islam, puasa sunnah & masehi",
                        icon = Icons.Default.CalendarMonth,
                        selected = currentScreen == Screen.DUAL_CALENDAR,
                        badgeText = "Tarikh & Puasa",
                        badgeColor = MaterialTheme.colorScheme.secondary,
                        testTag = "sidebar_nav_dual_calendar",
                        onClick = { onSelectScreen(Screen.DUAL_CALENDAR) }
                    )
                }

                if (uiState.featureVisibilityConfig.isOperationalModuleEnabled && uiState.featureVisibilityConfig.showMultiWallet) {
                    SidebarNavigationItem(
                        label = "Kantong Rekening & Mutasi",
                        subtitle = "Dompet tunai, rekening bank & e-wallet",
                        icon = Icons.Default.AccountBalance,
                        selected = currentScreen == Screen.MULTI_WALLET,
                        badgeText = "${uiState.wallets.size} Kantong",
                        badgeColor = MaterialTheme.colorScheme.primary,
                        testTag = "sidebar_nav_multi_wallet",
                        onClick = { onSelectScreen(Screen.MULTI_WALLET) }
                    )
                }

                if (uiState.featureVisibilityConfig.isOperationalModuleEnabled && uiState.featureVisibilityConfig.showRecurringTransactions) {
                    SidebarNavigationItem(
                        label = "Transaksi Rutin & Berulang",
                        subtitle = "Jadwal tagihan & infaq otomatis",
                        icon = Icons.Default.Schedule,
                        selected = currentScreen == Screen.RECURRING_TRANSACTIONS,
                        badgeText = if (uiState.dueRecurringCount > 0) "${uiState.dueRecurringCount} Jatuh Tempo" else null,
                        badgeColor = if (uiState.dueRecurringCount > 0) Color(0xFFFF7043) else null,
                        testTag = "sidebar_nav_recurring",
                        onClick = { onSelectScreen(Screen.RECURRING_TRANSACTIONS) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // KELOMPOK 2: FILANTROPI & SYARIAH (Collapsible)
                if (uiState.featureVisibilityConfig.isSyariahModuleEnabled && uiState.featureVisibilityConfig.activeSyariahCount > 0) {
                    SidebarCategoryHeader(
                        title = "KEUANGAN SYARIAH",
                        isCollapsible = true,
                        isExpanded = isSyariahExpanded,
                        onToggleExpand = { isSyariahExpanded = !isSyariahExpanded }
                    )

                    AnimatedVisibility(visible = isSyariahExpanded) {
                        Column {
                            if (uiState.featureVisibilityConfig.showZakatHub) {
                                SidebarNavigationItem(
                                    label = "Zakat Hub (Profesi/Tijarah/Fitrah)",
                                    subtitle = "Kalkulator zakat maal, fitrah & profesi",
                                    icon = Icons.Default.VolunteerActivism,
                                    selected = currentScreen == Screen.ZAKAT_HUB,
                                    badgeText = if (uiState.isNisabReached) "Wajib Zakat" else "Komprehensif",
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    testTag = "sidebar_nav_zakat_hub",
                                    onClick = { onSelectScreen(Screen.ZAKAT_HUB) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showSedekahSubuh) {
                                SidebarNavigationItem(
                                    label = "Sedekah Subuh & Streak",
                                    subtitle = "Rutinitas sedekah berkah di fajar hari",
                                    icon = Icons.Default.WbSunny,
                                    selected = currentScreen == Screen.SEDEKAH_SUBUH,
                                    badgeText = if (uiState.sedekahSubuhState.currentStreak > 0) "🔥 ${uiState.sedekahSubuhState.currentStreak} Hari" else null,
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    testTag = "sidebar_nav_sedekah_subuh",
                                    onClick = { onSelectScreen(Screen.SEDEKAH_SUBUH) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showInfaqVault) {
                                SidebarNavigationItem(
                                    label = "Brankas Infaq & Mustahiq",
                                    subtitle = "Akumulasi infaq & riwayat penyaluran",
                                    icon = Icons.Default.Savings,
                                    selected = currentScreen == Screen.VAULT_HISTORY,
                                    badgeText = if (uiState.securityConfig.isMaskBalance) "Rp •••" else "Rp ${nf.format(uiState.virtualInfaqVaultBalance)}",
                                    badgeColor = MaterialTheme.colorScheme.primary,
                                    testTag = "sidebar_nav_vault_history",
                                    onClick = { onSelectScreen(Screen.VAULT_HISTORY) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showHaulNisab) {
                                SidebarNavigationItem(
                                    label = "Haul & Nisab Tahunan",
                                    subtitle = "Batas minimal & waktu zakat kekayaan",
                                    icon = Icons.Default.MenuBook,
                                    selected = currentScreen == Screen.HAUL_NISAB,
                                    badgeText = if (uiState.isNisabReached) "Tercapai" else "Dipantau",
                                    badgeColor = if (uiState.isNisabReached) MaterialTheme.colorScheme.secondary else null,
                                    testTag = "sidebar_nav_haul_nisab",
                                    onClick = { onSelectScreen(Screen.HAUL_NISAB) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showGoldNisabTracker) {
                                SidebarNavigationItem(
                                    label = "Pelacak Emas & Nisab BAZNAS",
                                    subtitle = "Pantau harga emas acuan hisab BAZNAS",
                                    icon = Icons.Default.MonetizationOn,
                                    selected = currentScreen == Screen.GOLD_NISAB_TRACKER,
                                    badgeText = "SK BAZNAS",
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    isExperimental = true,
                                    testTag = "sidebar_nav_gold_nisab",
                                    onClick = { onSelectScreen(Screen.GOLD_NISAB_TRACKER) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showBusinessZakat) {
                                SidebarNavigationItem(
                                    label = "Zakat Perniagaan & Saham",
                                    subtitle = "Zakat dagang, bisnis & portofolio syariah",
                                    icon = Icons.Default.BusinessCenter,
                                    selected = currentScreen == Screen.BUSINESS_ZAKAT,
                                    badgeText = "Bisnis & DES",
                                    badgeColor = MaterialTheme.colorScheme.primary,
                                    isExperimental = true,
                                    testTag = "sidebar_nav_business_zakat",
                                    onClick = { onSelectScreen(Screen.BUSINESS_ZAKAT) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showShariahAudit) {
                                SidebarNavigationItem(
                                    label = "Audit Kepatuhan Syariah",
                                    subtitle = "Pemeriksaan akad & kepatuhan syariat",
                                    icon = Icons.Default.VerifiedUser,
                                    selected = currentScreen == Screen.SHARIAH_AUDIT,
                                    badgeText = "Health Check",
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    isExperimental = true,
                                    testTag = "sidebar_nav_shariah_audit",
                                    onClick = { onSelectScreen(Screen.SHARIAH_AUDIT) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showAutoInfaqRules) {
                                SidebarNavigationItem(
                                    label = "Aturan Infaq Otomatis",
                                    subtitle = "Penyisihan sedekah dari tiap pemasukan",
                                    icon = Icons.Default.AutoFixHigh,
                                    selected = currentScreen == Screen.INFAQ_RULES,
                                    badgeText = "${uiState.rules.size} Aturan",
                                    testTag = "sidebar_nav_infaq_rules",
                                    onClick = { onSelectScreen(Screen.INFAQ_RULES) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showQardhHasan) {
                                SidebarNavigationItem(
                                    label = "Hutang Piutang (Qardhul Hasan)",
                                    subtitle = "Pinjaman kebajikan tanpa bunga/riba",
                                    icon = Icons.Default.Handshake,
                                    selected = currentScreen == Screen.QARDH,
                                    badgeText = if (uiState.qardhRecords.isNotEmpty()) "${uiState.qardhRecords.size} Akad" else null,
                                    badgeColor = MaterialTheme.colorScheme.primary,
                                    testTag = "sidebar_nav_qardh",
                                    onClick = { onSelectScreen(Screen.QARDH) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showAmilDirectory) {
                                SidebarNavigationItem(
                                    label = "Direktori Lembaga Amil ZISWAF",
                                    subtitle = "Penyalur resmi BAZNAS & LAZ terverifikasi",
                                    icon = Icons.Default.LocationCity,
                                    selected = currentScreen == Screen.AMIL_DIRECTORY,
                                    badgeText = "BAZNAS & LAZ",
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    testTag = "sidebar_nav_amil_directory",
                                    onClick = { onSelectScreen(Screen.AMIL_DIRECTORY) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showFaraidhCalculator) {
                                SidebarNavigationItem(
                                    label = "Kalkulator Waris (Faraidh)",
                                    subtitle = "Penghitungan pembagian waris syar'i",
                                    icon = Icons.Default.Calculate,
                                    selected = currentScreen == Screen.FARAIDH_CALCULATOR,
                                    badgeText = "Hukum Waris",
                                    isExperimental = true,
                                    testTag = "sidebar_nav_faraidh",
                                    onClick = { onSelectScreen(Screen.FARAIDH_CALCULATOR) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showIslamicGrounding) {
                                SidebarNavigationItem(
                                    label = "Fatwa & Fiqih Muamalah DSN-MUI",
                                    subtitle = "Rujukan fatwa DSN-MUI & landasan dalil",
                                    icon = Icons.Default.MenuBook,
                                    selected = currentScreen == Screen.ISLAMIC_GROUNDING,
                                    badgeText = "Fatwa & Nisab",
                                    badgeColor = MaterialTheme.colorScheme.primary,
                                    testTag = "sidebar_nav_islamic_grounding",
                                    onClick = { onSelectScreen(Screen.ISLAMIC_GROUNDING) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showShariahRulesCustomization) {
                                SidebarNavigationItem(
                                    label = "Pusat Kustomisasi Logika Syariah",
                                    subtitle = "Pengaturan bobot hisab & parameter",
                                    icon = Icons.Default.Tune,
                                    selected = currentScreen == Screen.SHARIAH_RULES_CUSTOMIZATION,
                                    badgeText = "Rules Studio",
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    isExperimental = true,
                                    testTag = "sidebar_nav_shariah_rules_customization",
                                    onClick = { onSelectScreen(Screen.SHARIAH_RULES_CUSTOMIZATION) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                // KELOMPOK 3: PERENCANAAN & ANALISIS (Collapsible)
                if (uiState.featureVisibilityConfig.isPlanningModuleEnabled && uiState.featureVisibilityConfig.activePlanningCount > 0) {
                    SidebarCategoryHeader(
                        title = "PERENCANAAN & LAPORAN",
                        isCollapsible = true,
                        isExpanded = isPlanningExpanded,
                        onToggleExpand = { isPlanningExpanded = !isPlanningExpanded }
                    )

                    AnimatedVisibility(visible = isPlanningExpanded) {
                        Column {
                            if (uiState.featureVisibilityConfig.showIbadahGoals) {
                                SidebarNavigationItem(
                                    label = "Target Finansial dan Ibadah",
                                    subtitle = "Tabungan Qurban, Aqiqah & Haji",
                                    icon = Icons.Default.Savings,
                                    selected = currentScreen == Screen.IBADAH_GOALS,
                                    badgeText = "${uiState.ibadahGoals.size} Target",
                                    badgeColor = MaterialTheme.colorScheme.primary,
                                    testTag = "sidebar_nav_ibadah_goals",
                                    onClick = { onSelectScreen(Screen.IBADAH_GOALS) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showBudgetAllocation) {
                                SidebarNavigationItem(
                                    label = "Anggaran & Batas Pos",
                                    subtitle = "Pagu belanja & peringatan boros",
                                    icon = Icons.Default.PieChart,
                                    selected = currentScreen == Screen.BUDGET_ALLOCATION,
                                    badgeText = if (uiState.overBudgetCount > 0) "${uiState.overBudgetCount} Over" else null,
                                    badgeColor = if (uiState.overBudgetCount > 0) Color(0xFFFF7043) else null,
                                    testTag = "sidebar_nav_budget",
                                    onClick = { onSelectScreen(Screen.BUDGET_ALLOCATION) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showAnalytics) {
                                SidebarNavigationItem(
                                    label = "Analisis & Israf",
                                    subtitle = "Kesehatan finansial, arus dana & peringatan israf",
                                    icon = Icons.Default.Analytics,
                                    selected = currentScreen == Screen.ANALYTICS,
                                    badgeText = "SLI ${String.format(Locale.US, "%.0f", uiState.spiritualLiquidityIndex)}%",
                                    testTag = "sidebar_nav_analytics",
                                    onClick = { onSelectScreen(Screen.ANALYTICS) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showMonthlyReport) {
                                SidebarNavigationItem(
                                    label = "Laporan Bulanan & Ekspor",
                                    subtitle = "Rekapitulasi mutasi bulanan & infaq",
                                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                                    selected = currentScreen == Screen.MONTHLY_REPORT,
                                    testTag = "sidebar_nav_monthly_report",
                                    onClick = { onSelectScreen(Screen.MONTHLY_REPORT) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showReceiptArchive) {
                                SidebarNavigationItem(
                                    label = "Arsip Bukti Struk Transaksi",
                                    subtitle = "Foto kuitansi & nota pembelanjaan",
                                    icon = Icons.Default.PhotoLibrary,
                                    selected = currentScreen == Screen.RECEIPT_ARCHIVE,
                                    badgeText = "${uiState.journalEntries.count { it.receiptAttachment != null }} Struk",
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    isExperimental = true,
                                    testTag = "sidebar_nav_receipt_archive",
                                    onClick = { onSelectScreen(Screen.RECEIPT_ARCHIVE) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showExportReport) {
                                SidebarNavigationItem(
                                    label = "Ekspor PDF & CSV Mutasi",
                                    subtitle = "Unduh salinan berkas pembukuan kas",
                                    icon = Icons.Default.FileDownload,
                                    selected = currentScreen == Screen.EXPORT_REPORT,
                                    badgeText = "PDF / CSV",
                                    badgeColor = MaterialTheme.colorScheme.primary,
                                    testTag = "sidebar_nav_export_report",
                                    onClick = { onSelectScreen(Screen.EXPORT_REPORT) }
                                )
                            }

                            if (uiState.featureVisibilityConfig.showZakatReport) {
                                SidebarNavigationItem(
                                    label = "Laporan Zakat (Maal & Fitrah)",
                                    subtitle = "Audit nisab, haul & tanggungan fitrah",
                                    icon = Icons.Default.Assessment,
                                    selected = currentScreen == Screen.ZAKAT_REPORT,
                                    badgeText = if (uiState.totalZakatReportPayable > 0) "Rp ${nf.format(uiState.totalZakatReportPayable)}" else "Hisab",
                                    badgeColor = MaterialTheme.colorScheme.secondary,
                                    testTag = "sidebar_nav_zakat_report",
                                    onClick = { onSelectScreen(Screen.ZAKAT_REPORT) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                // KELOMPOK 4: PUSAT PENGATURAN TERPADU & SISTEM
                SidebarCategoryHeader(title = "PENGATURAN & SISTEM")

                // ITEM UTAMA: PENGATURAN TERPUSAT
                Surface(
                    color = if (currentScreen == Screen.CENTRAL_SETTINGS) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
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
                                .background(if (currentScreen == Screen.CENTRAL_SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Pengaturan Terpusat",
                                tint = if (currentScreen == Screen.CENTRAL_SETTINGS) Color.White else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pengaturan Terpusat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentScreen == Screen.CENTRAL_SETTINGS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Konfigurasi seluruh modul & fitur",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "HUB",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                SidebarNavigationItem(
                    label = "Pusat Notifikasi & Pengingat",
                    subtitle = "Infaq harian, hutang Qardh & haul zakat",
                    icon = Icons.Default.NotificationsActive,
                    selected = currentScreen == Screen.NOTIFICATION_SETTINGS,
                    badgeText = if (uiState.notificationConfig.isMasterEnabled) "${uiState.notificationConfig.activeChannelCount()}/3" else "Off",
                    badgeColor = if (uiState.notificationConfig.isMasterEnabled) MaterialTheme.colorScheme.primary else null,
                    testTag = "sidebar_nav_notification_settings",
                    onClick = { onSelectScreen(Screen.NOTIFICATION_SETTINGS) }
                )

                SidebarNavigationItem(
                    label = "Cadangan Data & Restore",
                    subtitle = "Enkripsi & pemulihan database lokal",
                    icon = Icons.Default.Security,
                    selected = currentScreen == Screen.BACKUP_RESTORE,
                    badgeText = "Enkripsi",
                    badgeColor = MaterialTheme.colorScheme.primary,
                    testTag = "sidebar_nav_backup_restore",
                    onClick = { onSelectScreen(Screen.BACKUP_RESTORE) }
                )

                SidebarNavigationItem(
                    label = "Keamanan & Kunci PIN",
                    subtitle = "Otentikasi biometrik & kunci PIN",
                    icon = Icons.Default.Shield,
                    selected = currentScreen == Screen.SECURITY_SETTINGS,
                    badgeText = if (uiState.securityConfig.isPinEnabled) "PIN Aktif" else "Nonaktif",
                    badgeColor = if (uiState.securityConfig.isPinEnabled) MaterialTheme.colorScheme.primary else null,
                    testTag = "sidebar_nav_security",
                    onClick = { onSelectScreen(Screen.SECURITY_SETTINGS) }
                )

                SidebarNavigationItem(
                    label = "Panduan Interaktif & Fiqh",
                    subtitle = "24 topik edukasi & tutorial aplikasi",
                    icon = Icons.Default.HelpOutline,
                    selected = currentScreen == Screen.INTERACTIVE_GUIDE,
                    testTag = "sidebar_nav_guide",
                    onClick = { onSelectScreen(Screen.INTERACTIVE_GUIDE) }
                )

                SidebarNavigationItem(
                    label = "Terminal Log & Diagnostik",
                    subtitle = "Diagnostik sistem & runtime log",
                    icon = Icons.Default.Terminal,
                    selected = currentScreen == Screen.DEBUG_TERMINAL,
                    badgeText = "LIVE",
                    badgeColor = Color(0xFF38BDF8),
                    testTag = "sidebar_nav_debug_terminal",
                    onClick = { onSelectScreen(Screen.DEBUG_TERMINAL) }
                )
            }

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
                        // Quick Theme Toggle (Terang / Gelap)
                        val systemIsDark = androidx.compose.foundation.isSystemInDarkTheme()
                        val effectiveIsDark = when (uiState.themeMode) {
                            AppThemeMode.FOLLOW_SYSTEM -> systemIsDark
                            AppThemeMode.TACTICAL_CYBER, AppThemeMode.ELEGANT_DARK, AppThemeMode.HIGH_CONTRAST_DARK -> true
                            else -> false
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(if (uiState.themeMode.isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                                .clickable { onToggleTheme() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("sidebar_footer_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (effectiveIsDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Ganti Tema",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (effectiveIsDark) "Terang" else "Gelap",
                                fontSize = 12.sp,
                                fontFamily = if (uiState.themeMode.isTactical) ShareTechMonoFontFamily else null,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // SYS//TEMA Selector Button (Tombol SYS/TEMA di samping switch tema)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(if (uiState.themeMode.isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                                .background(if (uiState.themeMode.isTactical) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .border(
                                    1.dp,
                                    if (uiState.themeMode.isTactical) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    if (uiState.themeMode.isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    onCloseDrawer()
                                    onOpenSystemDiagnostics?.invoke()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("sidebar_footer_sys_theme_toggle")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.primary, if (uiState.themeMode.isTactical) CutCornerShape(1.dp) else CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "SYS//TEMA",
                                fontSize = 11.sp,
                                fontFamily = if (uiState.themeMode.isTactical) ChakraPetchFontFamily else null,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Quick Lock
                        if (uiState.securityConfig.isPinEnabled) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(if (uiState.themeMode.isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                                    .clickable { onLockAppNow() }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("sidebar_footer_quick_lock")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Kunci Sekarang",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Kunci",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(if (uiState.themeMode.isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                            .clickable {
                                onCloseDrawer()
                                onOpenFeedback?.invoke()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("drawer_feedback_footer_button")
                    ) {
                        Text(
                            text = "Amanah Ledger v2.6\n© 2026 Muhalkarr • Buka Masukan & Changelog",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarCategoryHeader(
    title: String,
    badgeText: String? = null,
    isCollapsible: Boolean = false,
    isExpanded: Boolean = true,
    onToggleExpand: (() -> Unit)? = null
) {
    val isTactical = LocalAppThemeMode.current.isTactical
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            .then(
                if (isCollapsible && onToggleExpand != null) {
                    Modifier
                        .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                        .clickable { onToggleExpand() }
                } else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )
            if (badgeText != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = if (isTactical) CutCornerShape(3.dp) else RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
        }
        if (isCollapsible && onToggleExpand != null) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Tutup Kategori" else "Buka Kategori",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SidebarNavigationItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color? = null,
    isExperimental: Boolean = false,
    testTag: String,
    onClick: () -> Unit
) {
    val themeMode = LocalAppThemeMode.current
    val isTacticalLight = themeMode == AppThemeMode.TACTICAL_LIGHT
    val isTacticalCyber = themeMode == AppThemeMode.TACTICAL_CYBER
    val isTactical = isTacticalLight || isTacticalCyber
    val itemShape = if (isTactical) CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp) else RoundedCornerShape(12.dp)

    NavigationDrawerItem(
        shape = itemShape,
        label = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isTactical) label.uppercase() else label,
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (isExperimental) Modifier.weight(1f, fill = false) else Modifier
                        )
                        if (isExperimental) {
                            ExperimentalBadge(text = if (isTactical) "EXP//LAB" else "🧪 Lab")
                        }
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            fontSize = 10.sp,
                            color = if (isTacticalCyber) CyberTerminalTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (badgeText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    val currentBadgeAccent = badgeColor ?: if (isTacticalCyber) CyberNeonGreen else MaterialTheme.colorScheme.primary
                    Surface(
                        color = currentBadgeAccent.copy(alpha = if (isTacticalLight) 0.12f else 0.15f),
                        shape = if (isTactical) CutCornerShape(3.dp) else RoundedCornerShape(6.dp),
                        border = if (isTactical) androidx.compose.foundation.BorderStroke(1.dp, currentBadgeAccent.copy(alpha = if (isTacticalLight) 0.7f else 0.5f)) else null
                    ) {
                        Text(
                            text = badgeText,
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentBadgeAccent,
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
                tint = if (selected) {
                    if (isTacticalCyber) CyberNeonGreen else MaterialTheme.colorScheme.primary
                } else {
                    if (isTacticalCyber) CyberTerminalTextSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
        },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = if (isTacticalCyber) CyberNeonGreen.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            unselectedContainerColor = Color.Transparent,
            selectedTextColor = if (isTacticalCyber) CyberNeonGreen else MaterialTheme.colorScheme.primary,
            unselectedTextColor = if (isTacticalCyber) CyberTerminalTextPrimary else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 1.dp)
            .then(
                if (isTactical && selected) {
                    Modifier.border(1.2.dp, if (isTacticalCyber) CyberNeonGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary, itemShape)
                } else Modifier
            )
            .testTag(testTag)
    )
}

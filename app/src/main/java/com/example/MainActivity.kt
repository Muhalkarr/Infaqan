package com.example

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.fragment.app.FragmentActivity
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.isTactical
import com.example.Screen
import com.example.core.debug.AppDebugLogger
import com.example.core.state.AmanahLedgerViewModel
import com.example.navigation.AmanahNavHost
import com.example.navigation.AmanahRoutes
import com.example.presentation.components.AppNavigationDrawerContent
import com.example.presentation.screens.AppLockScreen
import com.example.presentation.screens.OnboardingScreen
import com.example.ui.theme.AmanahLedgerTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val viewModel: AmanahLedgerViewModel by viewModels()
    private var shakeDetector: com.example.core.sensor.ShakeDetector? = null
    private var pendingNavigationDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.core.auth.FirebaseSafeInitializer.init(this)
        enableEdgeToEdge()
        viewModel.initContextDependencies(this)
        viewModel.trySilentGoogleSignIn(this)
        pendingNavigationDestination = intent?.getStringExtra("NAVIGATE_TO")

        shakeDetector = com.example.core.sensor.ShakeDetector {
            if (viewModel.uiState.value.isShakeFeedbackEnabled) {
                viewModel.openFeedbackSheet(0)
            }
        }

        setContent {
            val state by viewModel.uiState.collectAsState()
            val systemInDarkTheme = isSystemInDarkTheme()
            val effectiveDarkMode = when (state.themeMode) {
                AppThemeMode.FOLLOW_SYSTEM -> systemInDarkTheme
                AppThemeMode.TACTICAL_CYBER, AppThemeMode.ELEGANT_DARK, AppThemeMode.HIGH_CONTRAST_DARK -> true
                AppThemeMode.TACTICAL_LIGHT, AppThemeMode.LIGHT_MODE, AppThemeMode.HIGH_CONTRAST_LIGHT -> false
            }
            val effectiveHighContrast = when (state.themeMode) {
                AppThemeMode.HIGH_CONTRAST_LIGHT, AppThemeMode.HIGH_CONTRAST_DARK -> true
                else -> state.isHighContrast
            }

            androidx.compose.runtime.SideEffect {
                val controller = androidx.core.view.WindowCompat.getInsetsController(this@MainActivity.window, this@MainActivity.window.decorView)
                controller.isAppearanceLightStatusBars = !effectiveDarkMode
                controller.isAppearanceLightNavigationBars = !effectiveDarkMode
            }

            var showFeedbackSheet by remember { androidx.compose.runtime.mutableStateOf(false) }
            var feedbackSheetTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                viewModel.openFeedbackEvent.collect { tab ->
                    feedbackSheetTab = tab
                    showFeedbackSheet = true
                }
            }

            LaunchedEffect(state.securityConfig.isScreenshotProtected) {
                if (state.securityConfig.isScreenshotProtected) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            AmanahLedgerTheme(
                themeMode = state.themeMode,
                darkTheme = effectiveDarkMode,
                highContrast = effectiveHighContrast,
                uiScaleFactor = state.uiScaleFactor
            ) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                    AmanahMainApp(
                        viewModel = viewModel,
                        initialDestination = pendingNavigationDestination,
                        onNavigationHandled = { pendingNavigationDestination = null }
                    )

                    AnimatedVisibility(
                        visible = !state.hasCompletedOnboarding,
                        enter = fadeIn(tween(350)),
                        exit = fadeOut(tween(300))
                    ) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onComplete = { viewModel.completeOnboarding() }
                        )
                    }

                    if (state.securityConfig.isPinEnabled && state.securityConfig.isAppLocked) {
                        AppLockScreen(viewModel = viewModel)
                    }

                    if (showFeedbackSheet) {
                        com.example.presentation.components.FeedbackAndChangelogSheet(
                            onDismissRequest = { showFeedbackSheet = false },
                            initialTab = feedbackSheetTab
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        shakeDetector?.stop()
        AppDebugLogger.i("MainActivity", "Aplikasi beralih ke latar belakang (onStop/Background).")
        viewModel.onAppBackgrounded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val target = intent.getStringExtra("NAVIGATE_TO")
        if (!target.isNullOrBlank()) {
            pendingNavigationDestination = target
        }
    }

    override fun onStart() {
        super.onStart()
        shakeDetector?.start(this)
        AppDebugLogger.i("MainActivity", "Aplikasi aktif di latar depan (onStart/Foreground).")
        viewModel.onAppForegrounded()
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)

@Composable
fun AmanahMainApp(
    viewModel: AmanahLedgerViewModel,
    initialDestination: String? = null,
    onNavigationHandled: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = remember(currentRoute) { Screen.fromRoute(currentRoute) }
    val state by viewModel.uiState.collectAsState()

    // Navigasi instan dari Shortcut, Notifikasi, atau App Widget (baik cold start maupun onNewIntent)
    LaunchedEffect(initialDestination, state.securityConfig.isAppLocked) {
        if (!initialDestination.isNullOrBlank() && !state.securityConfig.isAppLocked) {
            val route = when (initialDestination) {
                "ADD_TRANSACTION_EXPENSE" -> "${AmanahRoutes.ADD_TRANSACTION}?type=EXPENSE"
                "ADD_TRANSACTION_INCOME" -> "${AmanahRoutes.ADD_TRANSACTION}?type=INFLOW"
                "ADD_TRANSACTION" -> AmanahRoutes.ADD_TRANSACTION
                "SEDEKAH_SUBUH" -> AmanahRoutes.SEDEKAH_SUBUH
                "IBADAH_GOALS" -> AmanahRoutes.IBADAH_GOALS
                "ZAKAT_HUB" -> AmanahRoutes.ZAKAT_HUB
                "ZAKAT_REPORT" -> AmanahRoutes.ZAKAT_REPORT
                "VAULT_HISTORY" -> AmanahRoutes.VAULT_HISTORY
                "FARAIDH" -> AmanahRoutes.FARAIDH
                "QARDH" -> AmanahRoutes.QARDH
                "RECURRING_TRANSACTIONS" -> AmanahRoutes.RECURRING_TRANSACTIONS
                "NOTIFICATION_SETTINGS" -> AmanahRoutes.NOTIFICATION_SETTINGS
                "BACKUP_RESTORE" -> AmanahRoutes.BACKUP_RESTORE
                else -> null
            }
            if (route != null) {
                AppDebugLogger.i("MainActivity", "Navigasi shortcut/widget ke: $route")
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
            onNavigationHandled()
        }
    }

    // Otomatis catat pergantian layar ke log diagnostik
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            AppDebugLogger.logNavigation(route)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val leftNavItems = listOf(
        BottomNavItem(
            route = AmanahRoutes.DASHBOARD,
            title = "Beranda",
            icon = Icons.Default.Dashboard,
            testTag = "nav_dashboard"
        ),
        BottomNavItem(
            route = AmanahRoutes.LEDGER,
            title = "Buku Kas",
            icon = Icons.Default.MenuBook,
            testTag = "nav_ledger"
        )
    )

    val rightNavItems = listOf(
        BottomNavItem(
            route = AmanahRoutes.MULTI_WALLET,
            title = "Kantong",
            icon = Icons.Default.AccountBalance,
            testTag = "nav_wallets"
        ),
        BottomNavItem(
            route = AmanahRoutes.CENTRAL_SETTINGS,
            title = "Pengaturan",
            icon = Icons.Default.Settings,
            testTag = "nav_settings"
        )
    )

    val topLevelRoutes = remember {
        setOf(
            AmanahRoutes.DASHBOARD,
            AmanahRoutes.LEDGER,
            AmanahRoutes.INFAQ_VAULT,
            AmanahRoutes.MULTI_WALLET,
            AmanahRoutes.CENTRAL_SETTINGS
        )
    }
    val showBottomBar = currentRoute in topLevelRoutes
    var showSystemDiagnosticsDialog by remember { androidx.compose.runtime.mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawerContent(
                currentScreen = currentScreen,
                uiState = state,
                onSelectScreen = { screen ->
                    val targetRoute = screen.toRoute()
                    AppDebugLogger.logUserAction("Navigasi Sidebar", "Membuka ${screen.name} ($targetRoute)")
                    coroutineScope.launch { drawerState.close() }
                    if (screen == Screen.DASHBOARD || targetRoute == AmanahRoutes.DASHBOARD) {
                        if (currentRoute == AmanahRoutes.DASHBOARD) {
                            viewModel.triggerDashboardScrollToTop()
                        } else {
                            navController.navigate(AmanahRoutes.DASHBOARD) {
                                popUpTo(AmanahRoutes.DASHBOARD) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                            viewModel.triggerDashboardScrollToTop()
                        }
                    } else if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onCloseDrawer = {
                    coroutineScope.launch { drawerState.close() }
                },
                onToggleTheme = {
                    AppDebugLogger.logUserAction("Pengaturan Tema", "Pengguna beralih tema gelap/terang")
                    viewModel.toggleDarkMode()
                },
                onTogglePrivacy = {
                    AppDebugLogger.logUserAction("Privasi Saldo", "Pengguna beralih sensor privasi saldo")
                    viewModel.toggleBalancePrivacy()
                },
                onLockAppNow = {
                    AppDebugLogger.logUserAction("Keamanan", "Pengguna mengunci aplikasi secara instan")
                    coroutineScope.launch { drawerState.close() }
                    viewModel.lockAppNow()
                },
                onOpenFeedback = {
                    viewModel.openFeedbackSheet(0)
                },
                onToggleDisplayMode = {
                    viewModel.toggleAppDisplayMode()
                },
                onOpenSystemDiagnostics = {
                    coroutineScope.launch { drawerState.close() }
                    showSystemDiagnosticsDialog = true
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    if (state.themeMode.isTactical) {
                        com.example.ui.theme.TacticalCommsBottomBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                coroutineScope.launch { drawerState.close() }
                                if (route == AmanahRoutes.DASHBOARD) {
                                    if (currentRoute == AmanahRoutes.DASHBOARD) {
                                        viewModel.triggerDashboardScrollToTop()
                                    } else {
                                        navController.navigate(AmanahRoutes.DASHBOARD) {
                                            popUpTo(AmanahRoutes.DASHBOARD) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                        viewModel.triggerDashboardScrollToTop()
                                    }
                                } else if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            onAddTransaction = {
                                coroutineScope.launch { drawerState.close() }
                                viewModel.clearTransactionDraft()
                                navController.navigate("${AmanahRoutes.ADD_TRANSACTION}?type=EXPENSE") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    } else {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            // 1 & 2: Sisi Kiri (Beranda, Buku Kas)
                            leftNavItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        AppDebugLogger.logUserAction("Bilah Bawah", "Memilih tab '${item.title}' (${item.route})")
                                        coroutineScope.launch { drawerState.close() }
                                        if (item.route == AmanahRoutes.DASHBOARD) {
                                            if (currentRoute == AmanahRoutes.DASHBOARD) {
                                                viewModel.triggerDashboardScrollToTop()
                                            } else {
                                                navController.navigate(AmanahRoutes.DASHBOARD) {
                                                    popUpTo(AmanahRoutes.DASHBOARD) {
                                                        inclusive = false
                                                    }
                                                    launchSingleTop = true
                                                }
                                                viewModel.triggerDashboardScrollToTop()
                                            }
                                        } else if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }

                            // 3: TOMBOL UTAMA TENGAH (CENTER DOCKED ACTION BUTTON) - CATAT TRANSAKSI
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    AppDebugLogger.logUserAction("Bilah Bawah", "Memilih tombol utama tengah (+) Catat Transaksi")
                                    coroutineScope.launch { drawerState.close() }
                                    viewModel.clearTransactionDraft()
                                    navController.navigate("${AmanahRoutes.ADD_TRANSACTION}?type=EXPENSE") {
                                        launchSingleTop = true
                                    }
                                },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Catat Transaksi",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = "Catat",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.testTag("nav_center_add_transaction")
                            )

                            // 4 & 5: Sisi Kanan (Kantong, Pengaturan)
                            rightNavItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        AppDebugLogger.logUserAction("Bilah Bawah", "Memilih tab '${item.title}' (${item.route})")
                                        coroutineScope.launch { drawerState.close() }
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AmanahNavHost(
                    navController = navController,
                    viewModel = viewModel,
                    onOpenDrawer = {
                        AppDebugLogger.logUserAction("Navigasi", "Membuka drawer navigasi samping")
                        coroutineScope.launch { drawerState.open() }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // DIALOG TELEMETRI DIAGNOSTIK SISTEM & GANTI TEMA CEPAT
        if (showSystemDiagnosticsDialog) {
            val nf = remember { java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")) }
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showSystemDiagnosticsDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CutCornerShape(2.dp))
                        )
                        Text(
                            text = "KONSOL DIAGNOSTIK SISTEM",
                            fontFamily = com.example.ui.theme.ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "STATUS SISTEM: v4.2 // PROTOKOL AMANAH",
                            fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "INTEGRITAS SYARIAH: 100% AMANAH (ZERO RIBA & GHARAR)",
                            fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "AMBANG NISAB EMAS: Rp ${nf.format(state.nisabThreshold.toLong())} (85g Emas)",
                            fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "STATUS NISAB: ${if (state.isNisabReached) "[MENCAPAI NISAB // WAJIB ZAKAT]" else "[DI BAWAH NISAB]"}",
                            fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = if (state.isNisabReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "DATABASE LOKAL: TERENKRIPSI & AMAN (ROOM SQLITE CIPHER)",
                            fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            thickness = 1.dp
                        )

                        Text(
                            text = "PILIHAN TEMA APLIKASI:",
                            fontFamily = com.example.ui.theme.ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Pilihan Tema Cepat (Seluruh 7 Mode Tema)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                AppThemeMode.TACTICAL_CYBER to "Protokol Taktis (Gelap // Cyber Terminal)",
                                AppThemeMode.TACTICAL_LIGHT to "Protokol Taktis (Terang // Clean Lab)",
                                AppThemeMode.ELEGANT_DARK to "Mode Gelap Elegan",
                                AppThemeMode.LIGHT_MODE to "Mode Terang Bersih",
                                AppThemeMode.HIGH_CONTRAST_LIGHT to "Kontras Tinggi (Terang)",
                                AppThemeMode.HIGH_CONTRAST_DARK to "Kontras Tinggi (Gelap)",
                                AppThemeMode.FOLLOW_SYSTEM to "Ikuti Sistem Ponsel (Otomatis)"
                            ).forEach { (mode, name) ->
                                val isSelected = state.themeMode == mode
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(androidx.compose.foundation.shape.CutCornerShape(4.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            androidx.compose.foundation.shape.CutCornerShape(4.dp)
                                        )
                                        .clickable {
                                            viewModel.setThemeMode(mode)
                                            showSystemDiagnosticsDialog = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = name,
                                        fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "[AKTIF]",
                                            fontFamily = com.example.ui.theme.ShareTechMonoFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showSystemDiagnosticsDialog = false }
                    ) {
                        Text(
                            text = "TUTUP [ESC]",
                            fontFamily = com.example.ui.theme.ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = androidx.compose.foundation.shape.CutCornerShape(12.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

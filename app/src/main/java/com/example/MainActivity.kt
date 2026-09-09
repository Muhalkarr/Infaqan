package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.Screen
import com.example.core.debug.AppDebugLogger
import com.example.core.state.AmanahLedgerViewModel
import com.example.navigation.AmanahNavHost
import com.example.navigation.AmanahRoutes
import com.example.presentation.components.AppNavigationDrawerContent
import com.example.presentation.screens.AppLockScreen
import com.example.ui.theme.AmanahLedgerTheme
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private val viewModel: AmanahLedgerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.core.auth.FirebaseSafeInitializer.init(this)
        enableEdgeToEdge()
        viewModel.initContextDependencies(this)

        setContent {
            val state by viewModel.uiState.collectAsState()
            AmanahLedgerTheme(
                darkTheme = state.isDarkMode,
                highContrast = state.isHighContrast,
                uiScaleFactor = state.uiScaleFactor
            ) {
                if (state.securityConfig.isPinEnabled && state.securityConfig.isAppLocked) {
                    AppLockScreen(viewModel = viewModel)
                } else {
                    AmanahMainApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        AppDebugLogger.i("MainActivity", "Aplikasi beralih ke latar belakang (onStop/Background).")
        viewModel.onAppBackgrounded()
    }

    override fun onStart() {
        super.onStart()
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
fun AmanahMainApp(viewModel: AmanahLedgerViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentScreen = remember(currentRoute) { Screen.fromRoute(currentRoute) }
    val state by viewModel.uiState.collectAsState()

    // Otomatis catat pergantian layar ke log diagnostik
    LaunchedEffect(currentRoute) {
        currentRoute?.let { route ->
            AppDebugLogger.logNavigation(route)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val bottomNavItems = listOf(
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
        ),
        BottomNavItem(
            route = AmanahRoutes.INFAQ_VAULT,
            title = "Brankas Infaq",
            icon = Icons.Default.VolunteerActivism,
            testTag = "nav_infaq_vault"
        ),
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

    val showBottomBar = true

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawerContent(
                currentScreen = currentScreen,
                uiState = state,
                onSelectScreen = { screen ->
                    val targetRoute = screen.toRoute()
                    AppDebugLogger.logUserAction("Navigasi Sidebar", "Membuka ${screen.name} ($targetRoute)")
                    if (currentRoute != targetRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    // Do NOT close drawerState so sidebar stays open for swift multi-feature exploration
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
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    AppDebugLogger.logUserAction("Bilah Bawah", "Memilih tab '${item.title}' (${item.route})")
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
        ) { paddingValues ->
            AmanahNavHost(
                navController = navController,
                viewModel = viewModel,
                onOpenDrawer = {
                    AppDebugLogger.logUserAction("Navigasi", "Membuka drawer navigasi samping")
                    coroutineScope.launch { drawerState.open() }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

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
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.state.AmanahLedgerViewModel
import com.example.navigation.AmanahNavHost
import com.example.navigation.AmanahRoutes
import com.example.presentation.screens.AppLockScreen
import com.example.ui.theme.AmanahLedgerTheme
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

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
                highContrast = state.isHighContrast
            ) {
                if (state.securityConfig.isPinEnabled && state.securityConfig.isAppLocked) {
                    AppLockScreen(viewModel = viewModel)
                } else {
                    AmanahMainApp(viewModel = viewModel)
                }
            }
        }
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

    val showBottomBar = currentRoute in listOf(
        AmanahRoutes.DASHBOARD,
        AmanahRoutes.LEDGER,
        AmanahRoutes.INFAQ_VAULT,
        AmanahRoutes.MULTI_WALLET,
        AmanahRoutes.CENTRAL_SETTINGS
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
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
                                selectedIconColor = EmeraldLight,
                                selectedTextColor = EmeraldLight,
                                indicatorColor = EmeraldPrimary.copy(alpha = 0.2f),
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f)
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
            modifier = Modifier.padding(paddingValues)
        )
    }
}

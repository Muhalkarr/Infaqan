package com.example.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.core.state.AmanahLedgerViewModel
import com.example.presentation.screens.AddTransactionScreen
import com.example.presentation.screens.AmilDirectoryScreen
import com.example.presentation.screens.AnalyticsScreen
import com.example.presentation.screens.AppLockScreen
import com.example.presentation.screens.BackupRestoreScreen
import com.example.presentation.screens.BudgetAllocationScreen
import com.example.presentation.screens.CentralSettingsScreen
import com.example.presentation.screens.DashboardScreen
import com.example.presentation.screens.ExportReportScreen
import com.example.presentation.screens.FaraidhScreen
import com.example.presentation.screens.HaulNisabScreen
import com.example.presentation.screens.IbadahGoalsScreen
import com.example.presentation.screens.InfaqRulesScreen
import com.example.presentation.screens.InteractiveGuideScreen
import com.example.presentation.screens.IslamicKnowledgeGroundingScreen
import com.example.presentation.screens.MonthlyReportScreen
import com.example.presentation.screens.MultiWalletScreen
import com.example.presentation.screens.QardhScreen
import com.example.presentation.screens.RecurringTransactionsScreen
import com.example.presentation.screens.SecuritySettingsScreen
import com.example.presentation.screens.SedekahSubuhScreen
import com.example.presentation.screens.SettingsScreen
import com.example.presentation.screens.VaultDistributionHistoryScreen
import com.example.presentation.screens.ZakatHubScreen

object AmanahRoutes {
    const val DASHBOARD = "dashboard"
    const val LEDGER = "ledger"
    const val INFAQ_VAULT = "infaq_vault"
    const val VAULT_HISTORY = "vault_history"
    const val ADD_TRANSACTION = "add_transaction"
    const val MULTI_WALLET = "multi_wallet"
    const val IBADAH_GOALS = "ibadah_goals"
    const val ZAKAT_HUB = "zakat_hub"
    const val BACKUP_RESTORE = "backup_restore"
    const val QARDH = "qardh"
    const val AMIL_DIRECTORY = "amil_directory"
    const val FARAIDH = "faraidh"
    const val EXPORT_REPORT = "export_report"
    const val MONTHLY_REPORT = "monthly_report"
    const val ANALYTICS = "analytics"
    const val CENTRAL_SETTINGS = "central_settings"
    const val HAUL_NISAB = "haul_nisab"
    const val INFAQ_RULES = "infaq_rules"
    const val SEDEKAH_SUBUH = "sedekah_subuh"
    const val SECURITY_SETTINGS = "security_settings"
    const val RECURRING_TRANSACTIONS = "recurring_transactions"
    const val BUDGET_ALLOCATION = "budget_allocation"
    const val APP_LOCK = "app_lock"
    const val INTERACTIVE_GUIDE = "interactive_guide"
    const val ISLAMIC_GROUNDING = "islamic_grounding"
}

@Composable
fun AmanahNavHost(
    navController: NavHostController,
    viewModel: AmanahLedgerViewModel,
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val handleSmartBack: () -> Unit = {
        if (viewModel.isGuideModeActive) {
            val popped = navController.popBackStack(AmanahRoutes.INTERACTIVE_GUIDE, inclusive = false)
            if (!popped) {
                navController.navigate(AmanahRoutes.INTERACTIVE_GUIDE) {
                    popUpTo(AmanahRoutes.DASHBOARD) { inclusive = false }
                    launchSingleTop = true
                }
            }
        } else {
            navController.navigate(AmanahRoutes.DASHBOARD) {
                popUpTo(AmanahRoutes.DASHBOARD) { inclusive = false }
                launchSingleTop = true
            }
            onOpenDrawer()
        }
    }

    NavHost(
        navController = navController,
        startDestination = AmanahRoutes.DASHBOARD,
        modifier = modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        // 1. Dashboard (Main Hub)
        composable(AmanahRoutes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onOpenDrawer = onOpenDrawer,
                onNavigateToAddTransaction = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.ADD_TRANSACTION)
                },
                onEditTransaction = { _ ->
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.ADD_TRANSACTION)
                },
                onNavigateToBudget = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.BUDGET_ALLOCATION)
                },
                onNavigateToRules = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.INFAQ_RULES)
                },
                onNavigateToAnalytics = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.ANALYTICS)
                },
                onNavigateToHaulNisab = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.HAUL_NISAB)
                },
                onNavigateToRecurring = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.RECURRING_TRANSACTIONS)
                },
                onNavigateToMonthlyReport = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.MONTHLY_REPORT)
                },
                onNavigateToVaultHistory = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.VAULT_HISTORY)
                },
                onNavigateToSedekahSubuh = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.SEDEKAH_SUBUH)
                },
                onNavigateToGuide = {
                    navController.navigate(AmanahRoutes.INTERACTIVE_GUIDE)
                },
                onNavigateToSecurity = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.SECURITY_SETTINGS)
                },
                onNavigateToCentralSettings = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.CENTRAL_SETTINGS)
                },
                onNavigateToMultiWallet = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.MULTI_WALLET)
                },
                onNavigateToIbadahGoals = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.IBADAH_GOALS)
                },
                onNavigateToZakatHub = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.ZAKAT_HUB)
                },
                onNavigateToBackupRestore = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.BACKUP_RESTORE)
                },
                onNavigateToQardh = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.QARDH)
                },
                onNavigateToAmilDirectory = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.AMIL_DIRECTORY)
                },
                onNavigateToFaraidh = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.FARAIDH)
                },
                onNavigateToExportReport = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.EXPORT_REPORT)
                },
                onNavigateToIslamicGrounding = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.ISLAMIC_GROUNDING)
                }
            )
        }

        // 2. Ledger / Jurnal Mutasi
        composable(AmanahRoutes.LEDGER) {
            MonthlyReportScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 3. Infaq Vault & Zakat Hub
        composable(AmanahRoutes.INFAQ_VAULT) {
            ZakatHubScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 4. Add Transaction
        composable(AmanahRoutes.ADD_TRANSACTION) {
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 5. Multi Wallet & Kantong Kas
        composable(AmanahRoutes.MULTI_WALLET) {
            MultiWalletScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 6. Ibadah Goals
        composable(AmanahRoutes.IBADAH_GOALS) {
            IbadahGoalsScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 7. Zakat Hub
        composable(AmanahRoutes.ZAKAT_HUB) {
            ZakatHubScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 8. Backup & Restore (Room + Cloud Sync)
        composable(AmanahRoutes.BACKUP_RESTORE) {
            BackupRestoreScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 9. Qardh (Hutang Piutang)
        composable(AmanahRoutes.QARDH) {
            QardhScreen(
                viewModel = viewModel,
                onBackClick = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 10. Amil Directory
        composable(AmanahRoutes.AMIL_DIRECTORY) {
            AmilDirectoryScreen(
                viewModel = viewModel,
                onBackClick = handleSmartBack,
                onNavigateToDisburse = { _ -> navController.navigate(AmanahRoutes.ZAKAT_HUB) },
                onOpenDrawer = onOpenDrawer
            )
        }

        // 11. Faraidh Calculator
        composable(AmanahRoutes.FARAIDH) {
            FaraidhScreen(
                viewModel = viewModel,
                onBackClick = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 12. Export Report (PDF/CSV)
        composable(AmanahRoutes.EXPORT_REPORT) {
            ExportReportScreen(
                viewModel = viewModel,
                onBackClick = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 13. Haul & Nisab
        composable(AmanahRoutes.HAUL_NISAB) {
            HaulNisabScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 14. Infaq Rules
        composable(AmanahRoutes.INFAQ_RULES) {
            InfaqRulesScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 15. Sedekah Subuh
        composable(AmanahRoutes.SEDEKAH_SUBUH) {
            SedekahSubuhScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 16. Recurring Transactions
        composable(AmanahRoutes.RECURRING_TRANSACTIONS) {
            RecurringTransactionsScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 17. Budget Allocation
        composable(AmanahRoutes.BUDGET_ALLOCATION) {
            BudgetAllocationScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 18. Security Settings
        composable(AmanahRoutes.SECURITY_SETTINGS) {
            SecuritySettingsScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 19. Monthly Report
        composable(AmanahRoutes.MONTHLY_REPORT) {
            MonthlyReportScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 19a. Analytics Screen
        composable(AmanahRoutes.ANALYTICS) {
            AnalyticsScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 19b. Vault Distribution History
        composable(AmanahRoutes.VAULT_HISTORY) {
            VaultDistributionHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 20. Interactive Guide
        composable(AmanahRoutes.INTERACTIVE_GUIDE) {
            InteractiveGuideScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    viewModel.deactivateGuideMode()
                    navController.navigate(AmanahRoutes.DASHBOARD) {
                        popUpTo(AmanahRoutes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                    onOpenDrawer()
                },
                onNavigateToAddTransaction = { navController.navigate(AmanahRoutes.ADD_TRANSACTION) },
                onNavigateToBudget = { navController.navigate(AmanahRoutes.BUDGET_ALLOCATION) },
                onNavigateToRules = { navController.navigate(AmanahRoutes.INFAQ_RULES) },
                onNavigateToHaulNisab = { navController.navigate(AmanahRoutes.HAUL_NISAB) },
                onNavigateToSedekahSubuh = { navController.navigate(AmanahRoutes.SEDEKAH_SUBUH) },
                onNavigateToVaultHistory = { navController.navigate(AmanahRoutes.VAULT_HISTORY) },
                onNavigateToAmilDirectory = { navController.navigate(AmanahRoutes.AMIL_DIRECTORY) },
                onNavigateToSettings = { navController.navigate(AmanahRoutes.CENTRAL_SETTINGS) },
                onNavigateToFaraidh = { navController.navigate(AmanahRoutes.FARAIDH) },
                onNavigateToQardh = { navController.navigate(AmanahRoutes.QARDH) },
                onNavigateToZakatHub = { navController.navigate(AmanahRoutes.ZAKAT_HUB) },
                onNavigateToMultiWallet = { navController.navigate(AmanahRoutes.MULTI_WALLET) },
                onNavigateToRecurring = { navController.navigate(AmanahRoutes.RECURRING_TRANSACTIONS) },
                onNavigateToIbadahGoals = { navController.navigate(AmanahRoutes.IBADAH_GOALS) },
                onNavigateToIslamicGrounding = { navController.navigate(AmanahRoutes.ISLAMIC_GROUNDING) },
                onNavigateToExportReport = { navController.navigate(AmanahRoutes.EXPORT_REPORT) },
                onOpenDrawer = onOpenDrawer
            )
        }

        // 21. Central Settings
        composable(AmanahRoutes.CENTRAL_SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 22. Islamic Knowledge Grounding
        composable(AmanahRoutes.ISLAMIC_GROUNDING) {
            IslamicKnowledgeGroundingScreen(
                viewModel = viewModel,
                onNavigateBack = handleSmartBack,
                onOpenDrawer = onOpenDrawer
            )
        }

        // 23. App Lock Screen
        composable(AmanahRoutes.APP_LOCK) {
            AppLockScreen(
                viewModel = viewModel
            )
        }
    }
}

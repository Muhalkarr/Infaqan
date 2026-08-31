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
import com.example.presentation.screens.MonthlyReportScreen
import com.example.presentation.screens.MultiWalletScreen
import com.example.presentation.screens.QardhScreen
import com.example.presentation.screens.RecurringTransactionsScreen
import com.example.presentation.screens.SecuritySettingsScreen
import com.example.presentation.screens.SedekahSubuhScreen
import com.example.presentation.screens.ZakatHubScreen

object AmanahRoutes {
    const val DASHBOARD = "dashboard"
    const val LEDGER = "ledger"
    const val INFAQ_VAULT = "infaq_vault"
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
    const val CENTRAL_SETTINGS = "central_settings"
    const val HAUL_NISAB = "haul_nisab"
    const val INFAQ_RULES = "infaq_rules"
    const val SEDEKAH_SUBUH = "sedekah_subuh"
    const val SECURITY_SETTINGS = "security_settings"
    const val RECURRING_TRANSACTIONS = "recurring_transactions"
    const val BUDGET_ALLOCATION = "budget_allocation"
    const val APP_LOCK = "app_lock"
    const val INTERACTIVE_GUIDE = "interactive_guide"
}

@Composable
fun AmanahNavHost(
    navController: NavHostController,
    viewModel: AmanahLedgerViewModel,
    modifier: Modifier = Modifier
) {
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
                onNavigateToAddTransaction = { navController.navigate(AmanahRoutes.ADD_TRANSACTION) },
                onEditTransaction = { _ -> navController.navigate(AmanahRoutes.ADD_TRANSACTION) },
                onNavigateToBudget = { navController.navigate(AmanahRoutes.BUDGET_ALLOCATION) },
                onNavigateToRules = { navController.navigate(AmanahRoutes.INFAQ_RULES) },
                onNavigateToAnalytics = { navController.navigate(AmanahRoutes.MONTHLY_REPORT) },
                onNavigateToHaulNisab = { navController.navigate(AmanahRoutes.HAUL_NISAB) },
                onNavigateToRecurring = { navController.navigate(AmanahRoutes.RECURRING_TRANSACTIONS) },
                onNavigateToMonthlyReport = { navController.navigate(AmanahRoutes.MONTHLY_REPORT) },
                onNavigateToVaultHistory = { navController.navigate(AmanahRoutes.INFAQ_VAULT) },
                onNavigateToSedekahSubuh = { navController.navigate(AmanahRoutes.SEDEKAH_SUBUH) },
                onNavigateToGuide = { navController.navigate(AmanahRoutes.INTERACTIVE_GUIDE) },
                onNavigateToSecurity = { navController.navigate(AmanahRoutes.SECURITY_SETTINGS) },
                onNavigateToCentralSettings = { navController.navigate(AmanahRoutes.CENTRAL_SETTINGS) },
                onNavigateToMultiWallet = { navController.navigate(AmanahRoutes.MULTI_WALLET) },
                onNavigateToIbadahGoals = { navController.navigate(AmanahRoutes.IBADAH_GOALS) },
                onNavigateToZakatHub = { navController.navigate(AmanahRoutes.ZAKAT_HUB) },
                onNavigateToBackupRestore = { navController.navigate(AmanahRoutes.BACKUP_RESTORE) },
                onNavigateToQardh = { navController.navigate(AmanahRoutes.QARDH) },
                onNavigateToAmilDirectory = { navController.navigate(AmanahRoutes.AMIL_DIRECTORY) },
                onNavigateToFaraidh = { navController.navigate(AmanahRoutes.FARAIDH) },
                onNavigateToExportReport = { navController.navigate(AmanahRoutes.EXPORT_REPORT) }
            )
        }

        // 2. Ledger / Jurnal Mutasi
        composable(AmanahRoutes.LEDGER) {
            MonthlyReportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 3. Infaq Vault & Zakat Hub
        composable(AmanahRoutes.INFAQ_VAULT) {
            ZakatHubScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Add Transaction
        composable(AmanahRoutes.ADD_TRANSACTION) {
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Multi Wallet & Kantong Kas
        composable(AmanahRoutes.MULTI_WALLET) {
            MultiWalletScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 6. Ibadah Goals
        composable(AmanahRoutes.IBADAH_GOALS) {
            IbadahGoalsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Zakat Hub
        composable(AmanahRoutes.ZAKAT_HUB) {
            ZakatHubScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 8. Backup & Restore (Room + Cloud Sync)
        composable(AmanahRoutes.BACKUP_RESTORE) {
            BackupRestoreScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 9. Qardh (Hutang Piutang)
        composable(AmanahRoutes.QARDH) {
            QardhScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 10. Amil Directory
        composable(AmanahRoutes.AMIL_DIRECTORY) {
            AmilDirectoryScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToDisburse = { _ -> navController.navigate(AmanahRoutes.ZAKAT_HUB) }
            )
        }

        // 11. Faraidh Calculator
        composable(AmanahRoutes.FARAIDH) {
            FaraidhScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 12. Export Report (PDF/CSV)
        composable(AmanahRoutes.EXPORT_REPORT) {
            ExportReportScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 13. Haul & Nisab
        composable(AmanahRoutes.HAUL_NISAB) {
            HaulNisabScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 14. Infaq Rules
        composable(AmanahRoutes.INFAQ_RULES) {
            InfaqRulesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 15. Sedekah Subuh
        composable(AmanahRoutes.SEDEKAH_SUBUH) {
            SedekahSubuhScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 16. Recurring Transactions
        composable(AmanahRoutes.RECURRING_TRANSACTIONS) {
            RecurringTransactionsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 17. Budget Allocation
        composable(AmanahRoutes.BUDGET_ALLOCATION) {
            BudgetAllocationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 18. Security Settings
        composable(AmanahRoutes.SECURITY_SETTINGS) {
            SecuritySettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 19. Monthly Report
        composable(AmanahRoutes.MONTHLY_REPORT) {
            MonthlyReportScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 20. Interactive Guide
        composable(AmanahRoutes.INTERACTIVE_GUIDE) {
            InteractiveGuideScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTransaction = { navController.navigate(AmanahRoutes.ADD_TRANSACTION) },
                onNavigateToBudget = { navController.navigate(AmanahRoutes.BUDGET_ALLOCATION) },
                onNavigateToRules = { navController.navigate(AmanahRoutes.INFAQ_RULES) },
                onNavigateToHaulNisab = { navController.navigate(AmanahRoutes.HAUL_NISAB) },
                onNavigateToSedekahSubuh = { navController.navigate(AmanahRoutes.SEDEKAH_SUBUH) },
                onNavigateToVaultHistory = { navController.navigate(AmanahRoutes.INFAQ_VAULT) }
            )
        }

        // 21. Central Settings
        composable(AmanahRoutes.CENTRAL_SETTINGS) {
            CentralSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSecuritySettings = { navController.navigate(AmanahRoutes.SECURITY_SETTINGS) },
                onNavigateToInteractiveGuide = { navController.navigate(AmanahRoutes.INTERACTIVE_GUIDE) },
                onNavigateToMultiWallet = { navController.navigate(AmanahRoutes.MULTI_WALLET) },
                onNavigateToIbadahGoals = { navController.navigate(AmanahRoutes.IBADAH_GOALS) },
                onNavigateToZakatHub = { navController.navigate(AmanahRoutes.ZAKAT_HUB) },
                onNavigateToBackupRestore = { navController.navigate(AmanahRoutes.BACKUP_RESTORE) },
                onNavigateToQardh = { navController.navigate(AmanahRoutes.QARDH) },
                onNavigateToAmilDirectory = { navController.navigate(AmanahRoutes.AMIL_DIRECTORY) },
                onNavigateToFaraidh = { navController.navigate(AmanahRoutes.FARAIDH) },
                onNavigateToExportReport = { navController.navigate(AmanahRoutes.EXPORT_REPORT) }
            )
        }

        // 22. App Lock Screen
        composable(AmanahRoutes.APP_LOCK) {
            AppLockScreen(
                viewModel = viewModel
            )
        }
    }
}

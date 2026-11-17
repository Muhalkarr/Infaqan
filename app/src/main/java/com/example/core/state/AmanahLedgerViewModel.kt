package com.example.core.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
import com.example.core.accounting.CurrencyPrecisionUtils
import com.example.core.receipt.ReceiptImageStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.example.core.backup.BackupEngine
import com.example.core.backup.RestoreResult
import com.example.core.budget.BudgetAllocation
import com.example.core.budget.BudgetCapSuggestion
import com.example.core.budget.BudgetOptimizerEngine
import com.example.core.budget.FinancialGoalMode
import com.example.core.budget.SpendingPatternAnalysis
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.debug.AppDebugLogger
import com.example.core.ibadah.IbadahDepositRecord
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.infaq.AsnafCategory
import com.example.core.infaq.InfaqCalculationType
import com.example.core.infaq.InfaqDistributionRecord
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.InfaqSliAnalysisCalculator
import com.example.core.infaq.InfaqSliComprehensiveAnalysis
import com.example.core.infaq.InfaqVaultReminderConfig
import com.example.core.infaq.SedekahSubuhState
import com.example.core.infaq.SedekahSubuhStreakEngine
import com.example.core.qardh.QardhInstallment
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.report.DocumentExporter
import com.example.core.report.PdfEditorialConfig
import com.example.core.report.PdfEditorialStyle
import com.example.core.scheduler.RecurringFrequency
import com.example.core.scheduler.RecurringTransaction
import com.example.core.scheduler.RecurringType
import com.example.core.security.AutoLockInterval
import com.example.core.security.SecurityConfig
import com.example.core.security.SecurityEventType
import com.example.core.security.SecurityLogEntry
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletMutationRecord
import com.example.core.wallet.WalletType
import com.example.core.zakat.ZakatCategoryType
import com.example.core.zakat.ZakatFitrahDefaults
import com.example.core.zakat.ZakatFitrahDependent
import com.example.core.zakat.ZakatFitrahFamilyCalculation
import com.example.core.zakat.ZakatMaalCalculation
import com.example.core.zakat.ZakatPerniagaanCalculation
import com.example.core.zakat.ZakatProfesiCalculation
import com.example.core.directory.AmilInstitution
import com.example.core.directory.AmilBankAccount
import com.example.core.directory.AmilCategory
import com.example.core.directory.AmilDirectoryRepository
import com.example.core.shariah.ShariahRulesConfig
import com.example.core.shariah.ShariahRuling
import com.example.core.shariah.ShariahDefaultRulings
import com.example.core.rezeki.RezekiCategory
import com.example.core.rezeki.RezekiNonTunaiRecord
import com.example.core.auth.AmanahAuthManager
import com.example.core.auth.AmanahAuthState
import com.example.core.auth.AmanahUserProfile
import com.example.core.auth.AuthStatus
import com.example.core.backup.BackupPreferencesSnapshot
import com.example.core.database.AmanahDatabase
import com.example.core.database.AmanahDummyDataGenerator
import com.example.core.database.EntityMappers
import com.example.core.database.SettingsEntity
import com.example.core.datastore.DataStoreManager
import com.example.core.notification.AmanahNotificationConfig
import com.example.core.notification.NotificationCategoryType
import com.example.core.repository.AmanahRepository
import com.example.core.repository.AmanahRepositoryImpl
import com.example.core.sync.CloudSnapshotSummary
import com.example.core.sync.FirestoreSyncEngine
import com.example.core.sync.SyncState
import com.example.core.sync.SyncStateStatus
import com.example.core.worker.AmanahReminderWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date
import java.util.UUID

/**
 * Menyimpan draf form transaksi yang sedang diisi pengguna
 * agar data (angka, teks, struk) tidak hilang saat berpindah aplikasi atau ter-lock.
 */
data class TransactionDraftState(
    val isDraftActive: Boolean = false,
    val isIncome: Boolean = false,
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val amountText: String = "",
    val descriptionText: String = "",
    val selectedCategoryAccountId: String = "acc_living",
    val selectedAssetAccountId: String = "acc_cash",
    val infaqRate: Double = 0.05,
    val enableRoundUp: Boolean = true,
    val roundUpStep: Double = 5000.0,
    val attachReceipt: Boolean = false,
    val receiptMerchant: String = "",
    val receiptRefNumber: String = "",
    val receiptType: ReceiptType = ReceiptType.STORE_RECEIPT,
    val receiptNotes: String = "",
    val receiptImagePath: String? = null,
    val editEntryId: String? = null,
    val selectedWalletId: String = ""
)

data class LedgerAggregates(
    val totalIncomeKasab: Double = 0.0,
    val totalIncomeNonKasab: Double = 0.0,
    val totalConsumptionExpense: Double = 0.0,
    val totalPurifiedInfaq: Double = 0.0,
    val totalDisbursedInfaq: Double = 0.0,
    val totalInternalTransfer: Double = 0.0
)

data class AmanahLedgerUiState(
    val accounts: List<Account> = emptyList(),
    val journalEntries: List<JournalEntry> = emptyList(),
    val rules: List<InfaqRule> = emptyList(),
    val budgets: List<BudgetAllocation> = emptyList(),
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val infaqDistributions: List<InfaqDistributionRecord> = emptyList(),
    val sedekahSubuhState: SedekahSubuhState = SedekahSubuhState(),
    val wallets: List<WalletAccount> = emptyList(),
    val walletMutations: List<WalletMutationRecord> = emptyList(),
    val ibadahGoals: List<IbadahGoal> = emptyList(),
    val ibadahDeposits: List<IbadahDepositRecord> = emptyList(),
    val qardhRecords: List<QardhRecord> = emptyList(),
    val rezekiNonTunaiList: List<RezekiNonTunaiRecord> = emptyList(),
    val zakatProfesi: ZakatProfesiCalculation = ZakatProfesiCalculation(),
    val zakatPerniagaan: ZakatPerniagaanCalculation = ZakatPerniagaanCalculation(),
    val zakatFitrah: ZakatFitrahFamilyCalculation = ZakatFitrahFamilyCalculation(),
    val zakatMaal: ZakatMaalCalculation = ZakatMaalCalculation(),
    val selectedGoalMode: FinancialGoalMode = FinancialGoalMode.BALANCED_50_30_20,
    val themeMode: com.example.ui.theme.AppThemeMode = com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM,
    val isDarkMode: Boolean = true,
    val isHighContrast: Boolean = false,
    val goldPricePerGram: Double = 1350000.0, // Rp 1.350.000 per gram
    val goldPriceLastUpdatedMillis: Long = System.currentTimeMillis(),
    val goldPriceSource: String = "Antam / Standar BAZNAS",
    val transactionDisplayLimit: Int = 50,
    val selectedHijriOffset: Int = 0,
    val securityConfig: SecurityConfig = SecurityConfig(),
    val userNameKasMukmin: String = "Kas Keluarga Mukmin",
    val startDayOfMonth: Int = 1,
    val initialLedgerDate: String = "01/01/2024",
    val fiscalCycleType: com.example.core.accounting.FiscalCycleType = com.example.core.accounting.FiscalCycleType.MONTHLY_SALARY_DATE,
    val initialLedgerBalance: Double = 0.0,
    val fiscalYearStartMonth: Int = 1,
    val primaryCurrencySymbol: String = "Rp",
    val primaryCurrencyCode: String = "IDR",
    val haulStartDate: Long? = null,
    val defaultInfaqRate: Double = 0.05,
    val isAutoDeductInfaqEnabled: Boolean = true,
    val vaultMonthlyTarget: Double = 5000000.0,
    val sedekahSubuhTargetDays: Int = 40,
    val israfWarningThresholdPercent: Int = 80,
    val isStrictBudgetEnforced: Boolean = false,
    val isDeficitProtectionEnabled: Boolean = true,
    val autoExecuteRecurringEnabled: Boolean = true,
    val notifyOnRecurringDue: Boolean = true,
    val showDailyHadith: Boolean = true,
    val showQuickTutorial: Boolean = true,
    val amilInstitutions: List<AmilInstitution> = emptyList(),
    val shariahConfig: ShariahRulesConfig = ShariahRulesConfig(),
    val shariahRulings: List<ShariahRuling> = emptyList(),
    val uiScaleMode: com.example.ui.theme.UiScaleMode = com.example.ui.theme.UiScaleMode.DEFAULT,
    val uiScaleFactor: Float = 1.0f,
    val completedGuides: Set<String> = emptySet(),
    val hasCompletedOnboarding: Boolean = false,
    val isShakeFeedbackEnabled: Boolean = true,
    val lastBackupTimeMillis: Long = 0L,
    val isBackupNudgeDismissed: Boolean = false,
    val appDisplayMode: com.example.core.datastore.AppDisplayMode = com.example.core.datastore.AppDisplayMode.SIMPLE,
    val featureVisibilityConfig: com.example.core.feature.AppFeatureVisibilityConfig = com.example.core.feature.AppFeatureVisibilityConfig(),
    val infaqVaultReminderConfig: InfaqVaultReminderConfig = InfaqVaultReminderConfig(),
    val notificationConfig: AmanahNotificationConfig = AmanahNotificationConfig(),
    val autoCloudBackupEnabled: Boolean = false,
    val cloudSnapshotSummary: CloudSnapshotSummary? = null,
    val isLoadingCloudSummary: Boolean = false,
    val pdfEditorialConfig: PdfEditorialConfig = PdfEditorialConfig(),
    val disabledExpenseAccountIds: Set<String> = emptySet(),
    val purifiedAuditEntryIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
) {
    val visibleExpenseAccounts: List<Account>
        get() {
            val expenseList = accounts.filter { it.category == com.example.core.accounting.AccountCategory.EXPENSE }
            val filtered = expenseList.filter { it.id !in disabledExpenseAccountIds }
            return if (filtered.isNotEmpty()) filtered else expenseList
        }

    val dueRecurringCount: Int
        get() = recurringTransactions.count { it.isDue() }
    val nisabThreshold: Double
        get() = shariahConfig.goldNisabGram * goldPricePerGram // Variabel bobot hisab dinamis dari Rules Engine (default 85g emas)

    val pagedJournalEntries: List<JournalEntry>
        get() = journalEntries.take(transactionDisplayLimit)

    val spendingPatternAnalysis: SpendingPatternAnalysis by lazy {
        // Optimasi skala dataset: batasi jendela analisis ke 12 bulan terakhir untuk komputasi instan
        val cutoff = System.currentTimeMillis() - (365L * 24 * 3600 * 1000)
        val recentEntries = journalEntries.filter { it.gregorianDate.time >= cutoff }
        BudgetOptimizerEngine.analyzeAndOptimize(
            accounts = accounts,
            budgets = budgets,
            journalEntries = if (recentEntries.isNotEmpty()) recentEntries else journalEntries,
            goalMode = selectedGoalMode
        )
    }

    fun getAccount(id: String): Account? = accounts.firstOrNull { it.id == id }

    fun getWallet(id: String): WalletAccount? = wallets.firstOrNull { it.id == id }

    // Fast O(1) single-pass pre-calculated balance lookup dengan presisi moneter bulat Rupiah
    val accountBalances: Map<String, Double> by lazy {
        val debitMap = mutableMapOf<String, Double>()
        val creditMap = mutableMapOf<String, Double>()
        for (entry in journalEntries) {
            for (line in entry.lines) {
                debitMap[line.accountId] = (debitMap[line.accountId] ?: 0.0) + line.debit
                creditMap[line.accountId] = (creditMap[line.accountId] ?: 0.0) + line.credit
            }
        }
        val result = mutableMapOf<String, Double>()
        for (acc in accounts) {
            val d = debitMap[acc.id] ?: 0.0
            val c = creditMap[acc.id] ?: 0.0
            val rawBal = if (acc.category == AccountCategory.ASSET || acc.category == AccountCategory.EXPENSE) {
                d - c
            } else {
                c - d
            }
            result[acc.id] = kotlin.math.round(rawBal)
        }
        result
    }

    fun getWalletBalance(walletId: String): Double {
        val wallet = getWallet(walletId) ?: return 0.0
        return getAccountBalance(wallet.linkedAccountId)
    }

    val totalWalletBalance: Double
        get() = wallets.map { it.linkedAccountId }.distinct().sumOf { getAccountBalance(it) }

    fun getAccountBalance(accountId: String): Double {
        return accountBalances[accountId] ?: 0.0
    }

    // Fast monthly spent map for current month/year to avoid repeated filtering
    val currentMonthExpenseMap: Map<String, Double> by lazy {
        val cal = Calendar.getInstance()
        val curMonth = cal.get(Calendar.MONTH)
        val curYear = cal.get(Calendar.YEAR)
        val map = mutableMapOf<String, Double>()
        for (entry in journalEntries) {
            if (entry.transactionType != "EXPENSE") continue
            cal.time = entry.gregorianDate
            if (cal.get(Calendar.MONTH) == curMonth && cal.get(Calendar.YEAR) == curYear) {
                for (line in entry.lines) {
                    val acc = getAccount(line.accountId)
                    if (line.accountId != "acc_disbursed" && line.accountId != "acc_vault" && (acc == null || acc.category == AccountCategory.EXPENSE) && line.debit > 0.0) {
                        map[line.accountId] = (map[line.accountId] ?: 0.0) + line.debit
                    }
                }
            }
        }
        map
    }

    /**
     * Hitung total pengeluaran untuk akun tertentu pada bulan dan tahun berjalan
     * Secara tegas mengecualikan transaksi bertipe TRANSFER dan hanya menyaring EXPENSE
     */
    fun getMonthlySpentForAccount(
        accountId: String,
        targetMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
        targetYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): Double {
        val nowCal = Calendar.getInstance()
        if (targetMonth == nowCal.get(Calendar.MONTH) && targetYear == nowCal.get(Calendar.YEAR)) {
            return currentMonthExpenseMap[accountId] ?: 0.0
        }
        val cal = Calendar.getInstance()
        var total = 0.0
        for (entry in journalEntries) {
            if (entry.transactionType != "EXPENSE") continue
            cal.time = entry.gregorianDate
            if (cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear) {
                for (line in entry.lines) {
                    if (line.accountId == accountId && line.debit > 0.0) {
                        total += line.debit
                    }
                }
            }
        }
        return total
    }

    val totalMonthlyBudgetLimit: Double
        get() = budgets.sumOf { it.monthlyLimit }

    val totalMonthlyBudgetSpent: Double
        get() = budgets.sumOf { getMonthlySpentForAccount(it.accountId) }

    val overBudgetCount: Int
        get() = budgets.count { it.isOverBudget(getMonthlySpentForAccount(it.accountId)) }

    val totalAssets: Double
        get() = accounts
            .filter { it.category == AccountCategory.ASSET && it.id != "acc_vault" }
            .sumOf { getAccountBalance(it.id) }
            .coerceAtLeast(0.0)

    val totalInternalTransfer: Double
        get() = journalEntries
            .filter { it.transactionType == "TRANSFER" }
            .sumOf { it.totalDebit }

    val virtualInfaqVaultBalance: Double
        get() = getAccountBalance("acc_vault")

    val quarantineSyubhatBalance: Double
        get() = journalEntries
            .flatMap { it.lines }
            .filter { it.accountId == "acc_syubhat" }
            .sumOf { it.credit - it.debit }
            .coerceAtLeast(0.0)

    val totalIncomeKasab: Double
        get() = journalEntries
            .filter { it.transactionType == "INFLOW" }
            .flatMap { it.lines }
            .filter { getAccount(it.accountId)?.category == AccountCategory.INCOME_KASAB }
            .sumOf { it.credit }

    val totalIncomeNonKasab: Double
        get() = journalEntries
            .filter { it.transactionType == "INFLOW" }
            .flatMap { it.lines }
            .filter { getAccount(it.accountId)?.category == AccountCategory.INCOME_NON_KASAB }
            .sumOf { it.credit }

    val totalConsumptionExpense: Double
        get() = journalEntries
            .filter { it.transactionType == "EXPENSE" }
            .flatMap { it.lines }
            .filter { line ->
                val acc = getAccount(line.accountId)
                acc != null && acc.category == AccountCategory.EXPENSE && acc.id != "acc_disbursed"
            }
            .sumOf { it.debit }

    val totalPurifiedInfaq: Double
        get() = journalEntries
            .flatMap { it.lines }
            .filter { it.accountId == "acc_vault" }
            .sumOf { it.credit }

    val totalDisbursedInfaq: Double
        get() = journalEntries
            .filter { it.transactionType == "INFAQ_PAYOUT" }
            .flatMap { it.lines }
            .filter { it.accountId == "acc_vault" || it.accountId == "acc_disbursed" || it.accountId == "acc_sedekah_extra" }
            .sumOf { it.debit }

    val netSavingsAmount: Double
        get() = (totalIncomeKasab + totalIncomeNonKasab - totalConsumptionExpense - totalPurifiedInfaq)

    fun getKasabBreakdown(): List<Pair<Account, Double>> {
        val kasabAccounts = accounts.filter { it.category == AccountCategory.INCOME_KASAB }
        return kasabAccounts.mapNotNull { acc ->
            val sum = journalEntries
                .filter { it.transactionType == "INFLOW" }
                .flatMap { it.lines }
                .filter { it.accountId == acc.id }
                .sumOf { it.credit }
            if (sum > 0.0) Pair(acc, sum) else null
        }.sortedByDescending { it.second }
    }

    fun getNonKasabBreakdown(): List<Pair<Account, Double>> {
        val nonKasabAccounts = accounts.filter { it.category == AccountCategory.INCOME_NON_KASAB }
        return nonKasabAccounts.mapNotNull { acc ->
            val sum = journalEntries
                .filter { it.transactionType == "INFLOW" }
                .flatMap { it.lines }
                .filter { it.accountId == acc.id }
                .sumOf { it.credit }
            if (sum > 0.0) Pair(acc, sum) else null
        }.sortedByDescending { it.second }
    }

    fun getExpenseBreakdown(): List<Pair<Account, Double>> {
        val expenseAccounts = accounts.filter { it.category == AccountCategory.EXPENSE && it.id != "acc_disbursed" }
        return expenseAccounts.mapNotNull { acc ->
            val sum = journalEntries
                .filter { it.transactionType == "EXPENSE" }
                .flatMap { it.lines }
                .filter { it.accountId == acc.id }
                .sumOf { it.debit }
            if (sum > 0.0) Pair(acc, sum) else null
        }.sortedByDescending { it.second }
    }

    /**
     * Spiritual Liquidity Index (SLI)
     * Rasio kedermawanan vs pengeluaran konsumsi gaya hidup
     */
    val spiritualLiquidityIndex: Double
        get() {
            val consumption = totalConsumptionExpense
            if (consumption <= 0.0) return if (totalPurifiedInfaq > 0) 100.0 else 0.0
            return (totalPurifiedInfaq / consumption) * 100.0
        }

    val infaqSliAnalysis: InfaqSliComprehensiveAnalysis by lazy {
        InfaqSliAnalysisCalculator.analyze(
            accounts = accounts,
            journalEntries = journalEntries,
            rules = rules,
            totalIncomeKasab = totalIncomeKasab,
            totalIncomeNonKasab = totalIncomeNonKasab,
            totalConsumptionExpense = totalConsumptionExpense,
            totalPurifiedInfaq = totalPurifiedInfaq,
            virtualInfaqVaultBalance = virtualInfaqVaultBalance
        )
    }

    val isNisabReached: Boolean
        get() = totalAssets >= nisabThreshold

    val zakatObligationLunar: Double
        get() = if (isNisabReached) totalAssets * 0.025 else 0.0 // 2.500% Lunar (354 Hari)

    val zakatObligationSolar: Double
        get() = if (isNisabReached) totalAssets * 0.02577 else 0.0 // 2.577% Solar (365 Hari)

    val totalZakatReportPayable: Double
        get() = zakatMaal.zakatAmount + zakatFitrah.totalFitrahRupiah

    val totalZakatDisbursedRecorded: Double
        get() = infaqDistributions
            .filter {
                it.programName.contains("zakat", ignoreCase = true) ||
                    it.notes.contains("zakat", ignoreCase = true)
            }
            .sumOf { it.amount }
}

class AmanahLedgerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AmanahLedgerUiState())
    val uiState: StateFlow<AmanahLedgerUiState> = _uiState.asStateFlow()
    val appStateNotifier: AppStateNotifier = AppStateNotifier()

    private val _openFeedbackEvent = kotlinx.coroutines.flow.MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val openFeedbackEvent: kotlinx.coroutines.flow.SharedFlow<Int> = _openFeedbackEvent.asSharedFlow()

    fun openFeedbackSheet(tab: Int = 0) {
        _openFeedbackEvent.tryEmit(tab)
    }

    private val _dashboardScrollToTopEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dashboardScrollToTopEvent: kotlinx.coroutines.flow.SharedFlow<Unit> = _dashboardScrollToTopEvent.asSharedFlow()

    fun triggerDashboardScrollToTop() {
        _dashboardScrollToTopEvent.tryEmit(Unit)
    }

    // Interactive Guide State Tracking
    var activeGuideTopicIndex by mutableIntStateOf(0)
        private set
    var isGuideModeActive by mutableStateOf(false)
        private set
    var activeGuideTopicTitle by mutableStateOf("")
        private set

    fun setGuideTopic(index: Int, title: String = "") {
        activeGuideTopicIndex = index
        if (title.isNotEmpty()) activeGuideTopicTitle = title
    }

    fun deactivateGuideMode() {
        isGuideModeActive = false
    }

    
    fun completeOnboarding() {
        viewModelScope.launch {
            repository?.setOnboardingCompleted()
        }
    }

    fun markGuideTopicCompleted(title: String) {
        viewModelScope.launch {
            repository?.markGuideCompleted(title)
        }
    }

    fun activateGuideMode(topicIndex: Int, topicTitle: String) {
        activeGuideTopicIndex = topicIndex
        activeGuideTopicTitle = topicTitle
        isGuideModeActive = true
    }


    init {
        initDefaultData()
    }

    private fun initDefaultData() {
        val defaultAccounts = listOf(
            Account("acc_cash", "101", "Kas Fisik / Dompet Tunai", AccountCategory.ASSET, "Uang kas fisik di dompet"),
            Account("acc_bank", "102", "Rekening Bank Syariah", AccountCategory.ASSET, "Tabungan mudharabah/wadiah"),
            Account("acc_bank_konven", "102.1", "Rekening Bank Konvensional", AccountCategory.ASSET, "Rekening perbankan umum / konvensional"),
            Account("acc_bank_special", "102.2", "Tabungan Khusus (Haji/Pendidikan)", AccountCategory.ASSET, "Tabungan khusus amanah & ibadah haji"),
            Account("acc_ewallet", "105", "Saldo Dompet Digital", AccountCategory.ASSET, "Saldo uang elektronik / dompet digital"),
            Account("acc_gold", "103", "Tabungan Emas Fisik", AccountCategory.ASSET, "Logam mulia batangan"),
            Account("acc_other_asset", "106", "Aset & Kantong Khusus Lainnya", AccountCategory.ASSET, "Saldo simpanan kantong khusus / lainnya"),
            Account("acc_receivable", "104", "Piutang Qardh Hasan Diberikan", AccountCategory.ASSET, "Dana pinjaman kebajikan tanpa riba yang belum kembali"),
            Account("acc_vault", "201", "Virtual Infaq Vault (Titipan Amanah)", AccountCategory.LIABILITY, "Hak Mustahiq yang wajib disalurkan"),
            Account("acc_debt", "202", "Hutang Qardh / Cicilan", AccountCategory.LIABILITY, "Kewajiban pihak ketiga"),
            Account("acc_equity", "301", "Modal Awal / Harta Bersih", AccountCategory.EQUITY, "Saldo awal harta"),
            Account("acc_salary", "401", "Kasab: Ujrah / Gaji Pokok", AccountCategory.INCOME_KASAB, "Penghasilan kerja aktif"),
            Account("acc_trade", "402", "Kasab: Margin Usaha Dagang", AccountCategory.INCOME_KASAB, "Laba tijarah halal"),
            Account("acc_gift", "451", "Non-Kasab: Hibah & Hadiah", AccountCategory.INCOME_NON_KASAB, "Pemberian sukarela keluarga/rekan"),
            Account("acc_rikaz", "452", "Non-Kasab: Rikaz / Temuan", AccountCategory.INCOME_NON_KASAB, "Harta karun purbakala kuno (wajib zakat 20% seketika - HR. Bukhari No. 1499 & PMA No. 52/2014)"),
            Account("acc_syubhat", "453", "Non-Kasab: Karantina Syubhat", AccountCategory.INCOME_NON_KASAB, "Dana bunga bank / tidak jelas halal"),
            Account("acc_living", "501", "Biaya Hidup & Pangan", AccountCategory.EXPENSE, "Belanja pangan, konsumsi & dapur"),
            Account("acc_transport", "502", "Transportasi & Bensin", AccountCategory.EXPENSE, "Bahan bakar, ojek, tol & servis"),
            Account("acc_utility", "503", "Tagihan Listrik, Air & Pulsa", AccountCategory.EXPENSE, "PLN, PDAM, pulsa & paket internet"),
            Account("acc_education", "504", "Pendidikan & Majelis Dakwah", AccountCategory.EXPENSE, "SPP sekolah, buku kajian & majelis ilmu"),
            Account("acc_health", "505", "Kesehatan & Pengobatan", AccountCategory.EXPENSE, "Obat, vitamin & medis keluarga"),
            Account("acc_entertainment", "507", "Hiburan & Rekreasi Halal", AccountCategory.EXPENSE, "Kebutuhan rekreasi dan hiburan keluarga"),
            Account("acc_other_exp", "506", "Pengeluaran Rutin Lainnya", AccountCategory.EXPENSE, "Kebutuhan operasional rumah tangga"),
            Account("acc_disbursed", "599", "Penyaluran Infaq Terbayar", AccountCategory.EXPENSE, "Realisasi distribusi ke mustahiq"),
            Account("acc_sedekah_extra", "598", "Sedekah Ekstra & Sukarela", AccountCategory.EXPENSE, "Sedekah sukarela melebihi kewajiban atau hasil round-up")
        )

        val defaultRules = DEFAULT_INFAQ_RULES

        _uiState.update {

            it.copy(
                accounts = defaultAccounts,
                rules = defaultRules,
                budgets = AmanahDummyDataGenerator.getSampleBudgets(),
                recurringTransactions = AmanahDummyDataGenerator.getSampleRecurringTransactions(),
                wallets = AmanahDummyDataGenerator.getSampleWallets(),
                ibadahGoals = AmanahDummyDataGenerator.getSampleIbadahGoals(),
                qardhRecords = AmanahDummyDataGenerator.getSampleQardhRecords(),
                journalEntries = AmanahDummyDataGenerator.getSampleJournalEntries(),
                sedekahSubuhState = AmanahDummyDataGenerator.getSampleSedekahSubuhState(),
                infaqDistributions = AmanahDummyDataGenerator.getSampleInfaqDistributions(),
                zakatProfesi = AmanahDummyDataGenerator.getSampleZakatProfesi(),
                zakatPerniagaan = AmanahDummyDataGenerator.getSampleZakatPerniagaan(),
                zakatFitrah = AmanahDummyDataGenerator.getSampleZakatFitrah(),
                zakatMaal = AmanahDummyDataGenerator.getSampleZakatMaal(),
                amilInstitutions = AmilDirectoryRepository.verifiedInstitutions
            )
        }
    }

    /**
     * Pencatatan Pemasukan (Inflow) dengan Double-Entry Otomatis & Lampiran Bukti
     */
    fun recordIncome(
        grossAmount: Double,
        incomeAccountId: String,
        depositAccountId: String,
        customInfaqRate: Double,
        description: String,
        date: Date,
        receiptAttachment: ReceiptAttachment? = null
    ) {
        AppDebugLogger.i("AmanahVM", "Mencatat Pemasukan: Rp ${grossAmount.toLong()} ke akun $depositAccountId ($description)")
        val hijri = HijriCalendarEngine.fromGregorian(date)
        val infaqAmount = grossAmount * customInfaqRate
        val journalId = UUID.randomUUID().toString()

        val lines = mutableListOf(
            // 1. Debit Akun Kas/Bank (Aset bertambah)
            JournalLine(accountId = depositAccountId, debit = grossAmount, credit = 0.0),
            // 2. Kredit Akun Pendapatan Kasab/Non-Kasab (Pendapatan bertambah)
            JournalLine(accountId = incomeAccountId, debit = 0.0, credit = grossAmount)
        )

        // 3. Jika ada alokasi infaq, langsung poskan ke Virtual Infaq Vault (Liability Titipan Amanah)
        if (infaqAmount > 0.0) {
            lines.add(JournalLine(accountId = "acc_disbursed", debit = infaqAmount, credit = 0.0))
            lines.add(JournalLine(accountId = "acc_vault", debit = 0.0, credit = infaqAmount))
        }

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = description,
            transactionType = "INFLOW",
            lines = lines,
            receiptAttachment = receiptAttachment
        )

        _uiState.update { state ->
            state.copy(journalEntries = listOf(entry) + state.journalEntries)
        }
        persistEntryAsync(entry)

        val infaqPct = customInfaqRate * 100.0
        val infaqPctFormatted = if (infaqPct % 1.0 == 0.0) {
            "${infaqPct.toInt()}%"
        } else {
            "${String.format(Locale.US, "%.1f", infaqPct).replace('.', ',')}%"
        }
        appStateNotifier.notify(
            title = "Pemasukan Berhasil Dicatat",
            message = "Rezeki Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(grossAmount)} tercatat dengan alokasi infaq $infaqPctFormatted.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Memperbarui transaksi pemasukan yang sudah ada dengan perhitungan ulang Double-Entry
     */
    fun updateIncome(
        entryId: String,
        grossAmount: Double,
        incomeAccountId: String,
        depositAccountId: String,
        customInfaqRate: Double,
        description: String,
        date: Date,
        receiptAttachment: ReceiptAttachment? = null
    ) {
        val hijri = HijriCalendarEngine.fromGregorian(date)
        val infaqAmount = grossAmount * customInfaqRate

        val lines = mutableListOf(
            JournalLine(accountId = depositAccountId, debit = grossAmount, credit = 0.0),
            JournalLine(accountId = incomeAccountId, debit = 0.0, credit = grossAmount)
        )

        if (infaqAmount > 0.0) {
            lines.add(JournalLine(accountId = "acc_disbursed", debit = infaqAmount, credit = 0.0))
            lines.add(JournalLine(accountId = "acc_vault", debit = 0.0, credit = infaqAmount))
        }

        val updatedEntry = JournalEntry(
            id = entryId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = description,
            transactionType = "INFLOW",
            lines = lines,
            receiptAttachment = receiptAttachment
        )

        _uiState.update { state ->
            val updatedList = state.journalEntries.map {
                if (it.id == entryId) updatedEntry else it
            }
            state.copy(journalEntries = updatedList)
        }
        persistEntryAsync(updatedEntry)
        AppDebugLogger.i("AmanahVM", "Memperbarui Pemasukan: Rp ${grossAmount.toLong()} (ID: $entryId, '$description')")

        appStateNotifier.notify(
            title = "Pemasukan Diperbarui",
            message = "Catatan pemasukan '$description' telah berhasil diubah.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Pencatatan Belanja Konsumsi dengan Fitur Round-Up Micro Infaq & Bukti Transaksi
     */
    fun recordExpense(
        amount: Double,
        expenseAccountId: String,
        fromAccountId: String,
        enableRoundUp: Boolean,
        roundUpStep: Double,
        description: String,
        date: Date,
        receiptAttachment: ReceiptAttachment? = null
    ) {
        AppDebugLogger.i("AmanahVM", "Mencatat Pengeluaran: Rp ${amount.toLong()} dari akun $fromAccountId ($description)")
        val hijri = HijriCalendarEngine.fromGregorian(date)
        val journalId = UUID.randomUUID().toString()

        var roundUpAmount = 0.0
        if (enableRoundUp && roundUpStep > 0.0) {
            val remainder = amount % roundUpStep
            if (remainder > 0.0) {
                roundUpAmount = roundUpStep - remainder
            }
        }

        val lines = mutableListOf(
            // Debit Pengeluaran Belanja
            JournalLine(accountId = expenseAccountId, debit = amount, credit = 0.0),
            // Kredit Kas/Bank (dana terbayar ke merchant)
            JournalLine(accountId = fromAccountId, debit = 0.0, credit = amount)
        )

        if (roundUpAmount > 0.0) {
            // Alokasi selisih pembulatan ke Beban Infaq dan Virtual Infaq Vault (Liability)
            lines.add(JournalLine(accountId = "acc_disbursed", debit = roundUpAmount, credit = 0.0))
            lines.add(JournalLine(accountId = "acc_vault", debit = 0.0, credit = roundUpAmount))
        }

        val fullDesc = if (roundUpAmount > 0.0) {
            "$description (Round-up: Rp ${roundUpAmount.toInt()})"
        } else {
            description
        }

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = fullDesc,
            transactionType = "EXPENSE",
            lines = lines,
            receiptAttachment = receiptAttachment
        )

        _uiState.update { state ->
            state.copy(journalEntries = listOf(entry) + state.journalEntries)
        }
        persistEntryAsync(entry)

        appStateNotifier.notify(
            title = "Pengeluaran Berhasil Dicatat",
            message = "Belanja Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)} tercatat.${if (roundUpAmount > 0) " Round-up Rp ${roundUpAmount.toInt()} masuk vault." else ""}",
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Memperbarui transaksi pengeluaran yang sudah ada dengan perhitungan ulang Double-Entry
     */
    fun updateExpense(
        entryId: String,
        amount: Double,
        expenseAccountId: String,
        fromAccountId: String,
        enableRoundUp: Boolean,
        roundUpStep: Double,
        description: String,
        date: Date,
        receiptAttachment: ReceiptAttachment? = null
    ) {
        val hijri = HijriCalendarEngine.fromGregorian(date)

        var roundUpAmount = 0.0
        if (enableRoundUp && roundUpStep > 0.0) {
            val remainder = amount % roundUpStep
            if (remainder > 0.0) {
                roundUpAmount = roundUpStep - remainder
            }
        }

        val lines = mutableListOf(
            JournalLine(accountId = expenseAccountId, debit = amount, credit = 0.0),
            JournalLine(accountId = fromAccountId, debit = 0.0, credit = amount)
        )

        if (roundUpAmount > 0.0) {
            lines.add(JournalLine(accountId = "acc_disbursed", debit = roundUpAmount, credit = 0.0))
            lines.add(JournalLine(accountId = "acc_vault", debit = 0.0, credit = roundUpAmount))
        }

        // Clean any existing "(Round-up: ...)" tag in desc
        val cleanDesc = description.replace(Regex("""\s*\(Round-up:.*?\)$"""), "")
        val fullDesc = if (roundUpAmount > 0.0) {
            "$cleanDesc (Round-up: Rp ${roundUpAmount.toInt()})"
        } else {
            cleanDesc
        }

        val updatedEntry = JournalEntry(
            id = entryId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = fullDesc,
            transactionType = "EXPENSE",
            lines = lines,
            receiptAttachment = receiptAttachment
        )

        _uiState.update { state ->
            val updatedList = state.journalEntries.map {
                if (it.id == entryId) updatedEntry else it
            }
            state.copy(journalEntries = updatedList)
        }
        persistEntryAsync(updatedEntry)
        AppDebugLogger.i("AmanahVM", "Memperbarui Pengeluaran: Rp ${amount.toLong()} (ID: $entryId, '$cleanDesc')")

        appStateNotifier.notify(
            title = "Pengeluaran Diperbarui",
            message = "Catatan pengeluaran '$cleanDesc' telah berhasil diubah.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Memperbarui transaksi khusus (TRANSFER, INFAQ_PAYOUT, ZAKAT, INFAQ_SEDEKAH, QARDH, dll.)
     * termasuk catatan historis lama tanpa merusak struktur pasangan akun double-entry.
     */
    fun updateSpecialJournalEntry(
        entryId: String,
        newAmount: Double,
        newDescription: String,
        newDate: Date,
        receiptAttachment: ReceiptAttachment? = null
    ) {
        val target = _uiState.value.journalEntries.firstOrNull { it.id == entryId } ?: return
        val hijri = HijriCalendarEngine.fromGregorian(newDate)
        val cleanDesc = newDescription.trim().ifBlank { target.description }
        val cleanNewAmount = kotlin.math.round(newAmount)
        val oldTotalDebit = target.lines.sumOf { it.debit }
        val updatedLines = if (cleanNewAmount > 0.0 && oldTotalDebit > 0.0) {
            val ratio = cleanNewAmount / oldTotalDebit
            val debitIndices = target.lines.indices.filter { target.lines[it].debit > 0.0 }
            val creditIndices = target.lines.indices.filter { target.lines[it].credit > 0.0 }

            val newDebits = DoubleArray(target.lines.size)
            var runningDebit = 0.0
            debitIndices.forEachIndexed { pos, lineIdx ->
                if (pos == debitIndices.lastIndex) {
                    newDebits[lineIdx] = (cleanNewAmount - runningDebit).coerceAtLeast(0.0)
                } else {
                    val remaining = (cleanNewAmount - runningDebit).coerceAtLeast(0.0)
                    val scaled = kotlin.math.round(target.lines[lineIdx].debit * ratio).coerceIn(0.0, remaining)
                    newDebits[lineIdx] = scaled
                    runningDebit += scaled
                }
            }

            val newCredits = DoubleArray(target.lines.size)
            var runningCredit = 0.0
            creditIndices.forEachIndexed { pos, lineIdx ->
                if (pos == creditIndices.lastIndex) {
                    newCredits[lineIdx] = (cleanNewAmount - runningCredit).coerceAtLeast(0.0)
                } else {
                    val remaining = (cleanNewAmount - runningCredit).coerceAtLeast(0.0)
                    val scaled = kotlin.math.round(target.lines[lineIdx].credit * ratio).coerceIn(0.0, remaining)
                    newCredits[lineIdx] = scaled
                    runningCredit += scaled
                }
            }

            target.lines.mapIndexed { idx, line ->
                line.copy(debit = newDebits[idx], credit = newCredits[idx])
            }
        } else {
            target.lines
        }

        val updatedEntry = target.copy(
            gregorianDate = newDate,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = cleanDesc,
            lines = updatedLines,
            receiptAttachment = receiptAttachment
        )

        var updatedDistRecord: InfaqDistributionRecord? = null
        _uiState.update { state ->
            val updatedEntries = state.journalEntries.map {
                if (it.id == entryId) updatedEntry else it
            }
            val updatedDistributions = if (target.transactionType == "INFAQ_PAYOUT") {
                state.infaqDistributions.map { dist ->
                    if ("j_${dist.id}" == entryId || dist.id == entryId) {
                        val synced = dist.copy(
                            amount = if (newAmount > 0.0) newAmount else dist.amount,
                            distributionDate = newDate,
                            hijriDateString = "${hijri.day} ${hijri.monthName} ${hijri.year} H",
                            notes = if (dist.notes.isBlank()) cleanDesc else dist.notes
                        )
                        updatedDistRecord = synced
                        synced
                    } else {
                        dist
                    }
                }
            } else {
                state.infaqDistributions
            }
            state.copy(
                journalEntries = updatedEntries,
                infaqDistributions = updatedDistributions
            )
        }
        persistEntryAsync(updatedEntry)
        updatedDistRecord?.let { rec ->
            viewModelScope.launch(Dispatchers.IO) {
                repository?.saveInfaqDistribution(rec)
            }
        }

        AppDebugLogger.i("AmanahVM", "Memperbarui Transaksi Khusus (${target.transactionType}): Rp ${newAmount.toLong()} (ID: $entryId, '$cleanDesc')")
        appStateNotifier.notify(
            title = "Catatan Transaksi Diperbarui",
            message = "Perubahan catatan '$cleanDesc' berhasil disimpan & disinkronkan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Menghapus transaksi jurnal dan memulihkan saldo/posisi neraca
     */
    fun deleteJournalEntry(entryId: String) {
        val target = _uiState.value.journalEntries.firstOrNull { it.id == entryId } ?: return
        AppDebugLogger.w("AmanahVM", "Menghapus Transaksi Jurnal ID: $entryId (${target.description})")

        _uiState.update { state ->
            // If it was an infaq distribution, also remove from infaqDistributions list
            val updatedDistributions = if (target.transactionType == "INFAQ_PAYOUT") {
                state.infaqDistributions.filterNot { "j_${it.id}" == entryId || it.id == entryId }
            } else {
                state.infaqDistributions
            }

            // Jika transaksi ini terhubung dengan cicilan Qardh, pulihkan saldo sisa dan status akad
            var qardhRestored: QardhRecord? = null
            val updatedQardhRecords = state.qardhRecords.map { q ->
                val linkedInstallment = q.installments.firstOrNull { it.linkedJournalEntryId == entryId }
                if (linkedInstallment != null) {
                    val restoredRemaining = (q.remainingAmount + linkedInstallment.amount).coerceAtMost(q.totalAmount)
                    val restoredStatus = if (restoredRemaining >= q.totalAmount) QardhStatus.AKTIF else QardhStatus.SEBAGIAN_LUNAS
                    val restoredQ = q.copy(
                        remainingAmount = restoredRemaining,
                        status = restoredStatus,
                        installments = q.installments.filterNot { it.id == linkedInstallment.id }
                    )
                    qardhRestored = restoredQ
                    restoredQ
                } else {
                    q
                }
            }
            if (qardhRestored != null) {
                persistQardhAsync(qardhRestored!!)
            }

            state.copy(
                journalEntries = state.journalEntries.filterNot { it.id == entryId },
                infaqDistributions = updatedDistributions,
                qardhRecords = updatedQardhRecords
            )
        }
        deleteEntryAsync(entryId)
        if (target.receiptAttachment?.imagePath != null) {
            ReceiptImageStorage.deleteImageFile(target.receiptAttachment.imagePath)
        }
        if (target.transactionType == "INFAQ_PAYOUT") {
            viewModelScope.launch(Dispatchers.IO) {
                val distId = if (entryId.startsWith("j_")) entryId.removePrefix("j_") else entryId
                repository?.deleteInfaqDistribution(distId)
                repository?.deleteInfaqDistribution(entryId)
            }
        }

        appStateNotifier.notify(
            title = "Transaksi Dihapus",
            message = "Transaksi '${target.description}' telah dihapus dari buku besar syariah.",
            severity = NotificationSeverity.INFO
        )
    }

    fun getJournalEntry(entryId: String): JournalEntry? {
        return _uiState.value.journalEntries.firstOrNull { it.id == entryId }
    }

    /**
     * Skalabilitas Memori: Aliran data transaksi terpaginasi (limit & offset) dari Room DB
     */
    fun getPagedJournalEntries(limit: Int, offset: Int): Flow<List<JournalEntry>> {
        val repo = repository
        return if (repo != null) {
            repo.getPagedTransactionsFlow(limit, offset)
        } else {
            kotlinx.coroutines.flow.flowOf(_uiState.value.journalEntries.drop(offset).take(limit))
        }
    }

    suspend fun fetchPagedEntries(limit: Int, offset: Int): List<JournalEntry> {
        val repo = repository
        return if (repo != null) {
            repo.getPagedTransactions(limit, offset)
        } else {
            _uiState.value.journalEntries.drop(offset).take(limit)
        }
    }

    suspend fun getTotalEntriesCount(): Int {
        val repo = repository
        return repo?.getTransactionsCount() ?: _uiState.value.journalEntries.size
    }

    /**
     * Penyaluran Dana Infaq dari Virtual Vault ke Mustahiq/Lembaga Amil
     * Virtual Vault (Debit - lunas kewajiban) = Kas/Bank (Kredit)
     */
    fun disburseInfaq(
        amount: Double,
        fromAccountId: String,
        mustahiqDescription: String,
        date: Date = Date(),
        asnafCategory: AsnafCategory = AsnafCategory.UMUM,
        programName: String = "Penyaluran Infaq",
        notes: String = ""
    ) {
        disburseInfaqWithDetails(
            amount = amount,
            fromAccountId = fromAccountId,
            recipientName = mustahiqDescription,
            asnafCategory = asnafCategory,
            programName = programName,
            notes = notes,
            date = date
        )
    }

    fun disburseInfaqWithDetails(
        amount: Double,
        fromAccountId: String,
        recipientName: String,
        asnafCategory: AsnafCategory = AsnafCategory.UMUM,
        programName: String = "Penyaluran Infaq Mandiri",
        notes: String = "",
        date: Date = Date()
    ) {
        val cleanAmount = kotlin.math.round(amount)
        if (cleanAmount <= 0.0) {
            appStateNotifier.notify(
                title = "Gagal Menyalurkan Infaq",
                message = "Nominal penyaluran harus lebih besar dari Rp 0.",
                severity = NotificationSeverity.WARNING
            )
            return
        }

        val currentVaultBalance = _uiState.value.virtualInfaqVaultBalance
        val vaultPortion = minOf(cleanAmount, maxOf(0.0, currentVaultBalance))
        val extraSedekah = maxOf(0.0, cleanAmount - vaultPortion)

        // Resolusi dompet riil: pastikan akun kas/bank/dompet digital riil yang dipotong
        val realWallet = _uiState.value.getWallet(fromAccountId)
        val resolvedAssetAccountId = realWallet?.linkedAccountId ?: fromAccountId

        val hijri = HijriCalendarEngine.fromGregorian(date)
        val journalId = UUID.randomUUID().toString()
        val distId = "dist_${UUID.randomUUID().toString().take(8)}"
        val receiptNo = "INV-VAULT-${SimpleDateFormat("yyyyMMdd", Locale.US).format(date)}-${(1000..9999).random()}"

        val lines = mutableListOf<JournalLine>()
        if (vaultPortion > 0.0) {
            // 1. Debit Virtual Vault (Kewajiban amanah terlaksana & lunas)
            lines.add(JournalLine(accountId = "acc_vault", debit = vaultPortion, credit = 0.0))
        }
        if (extraSedekah > 0.0) {
            // 2. Debit Penyaluran Sedekah Ekstra (Tabarru' sukarela melebihi kewajiban vault)
            lines.add(JournalLine(accountId = "acc_disbursed", debit = extraSedekah, credit = 0.0))
        }
        // 3. Kredit Kas/Bank/Dompet Digital riil (Uang fisik benar-benar keluar dari rekening dompet riil pengguna)
        lines.add(JournalLine(accountId = resolvedAssetAccountId, debit = 0.0, credit = cleanAmount))

        val descExtraTag = if (extraSedekah > 0.0) {
            " [Sedekah Ekstra: Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(extraSedekah.toLong())}]"
        } else ""

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = "Penyaluran Infaq: $recipientName (${asnafCategory.displayName})$descExtraTag",
            transactionType = "INFAQ_PAYOUT",
            lines = lines
        )

        val updatedNotes = if (extraSedekah > 0.0) {
            val tag = "[Sedekah Ekstra: Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(extraSedekah.toLong())}]"
            if (notes.isNotBlank()) "$notes $tag" else tag
        } else {
            notes
        }

        val distRecord = InfaqDistributionRecord(
            id = distId,
            amount = cleanAmount,
            recipientName = recipientName,
            asnafCategory = asnafCategory,
            distributionDate = date,
            hijriDateString = "${hijri.day} ${hijri.monthName} ${hijri.year} H",
            sourceAccountId = resolvedAssetAccountId,
            programName = programName.ifBlank { "Penyaluran Tabarru'" },
            receiptNumber = receiptNo,
            notes = updatedNotes,
            isVerified = true
        )

        _uiState.update { state ->
            state.copy(
                journalEntries = listOf(entry) + state.journalEntries,
                infaqDistributions = listOf(distRecord) + state.infaqDistributions
            )
        }
        persistEntryAsync(entry)
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveInfaqDistribution(distRecord)
        }
        AppDebugLogger.i("AmanahInfaq", "Penyaluran Infaq: Rp ${cleanAmount.toLong()} (Vault: ${vaultPortion.toLong()}, Sedekah Ekstra: ${extraSedekah.toLong()}) ke $recipientName via $resolvedAssetAccountId")

        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))
        val notifyMsg = if (extraSedekah > 0.0) {
            "Dana Rp ${nf.format(cleanAmount)} disalurkan ke $recipientName (Vault: Rp ${nf.format(vaultPortion.toLong())} + Sedekah Ekstra: Rp ${nf.format(extraSedekah.toLong())})"
        } else {
            "Dana Rp ${nf.format(cleanAmount)} disalurkan ke $recipientName (${asnafCategory.displayName})"
        }

        appStateNotifier.notify(
            title = "Penyaluran Infaq Berhasil",
            message = notifyMsg,
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Pembersihan Dana Non-Halal / Bunga Bank (Tathhir Fund Cleansing)
     * Berdasarkan Fatwa DSN-MUI No. 123/DSN-MUI/XI/2018 tentang Pemanfaatan Dana Sosial / Non-Halal.
     * Dana disalurkan 100% untuk kemaslahatan umum/fasilitas sosial tanpa niat sedekah pahala.
     */
    fun disburseTathhirFund(
        amount: Double,
        publicPurpose: String,
        fromWalletId: String,
        notes: String = ""
    ) {
        val cleanAmount = amount.coerceAtLeast(0.0)
        if (cleanAmount <= 0.0) {
            appStateNotifier.notify(
                title = "Nominal Tidak Valid",
                message = "Masukkan nominal dana pembersihan yang sah.",
                severity = NotificationSeverity.WARNING
            )
            return
        }

        val date = Date()
        val hijri = HijriCalendarEngine.fromGregorian(date)
        val journalId = UUID.randomUUID().toString()
        val distId = "tathhir_${UUID.randomUUID().toString().take(8)}"
        val receiptNo = "TATHHIR-${SimpleDateFormat("yyyyMMdd", Locale.US).format(date)}-${(1000..9999).random()}"

        val realWallet = _uiState.value.getWallet(fromWalletId)
        val resolvedAssetAccountId = realWallet?.linkedAccountId ?: fromWalletId
        val quarantineBal = _uiState.value.quarantineSyubhatBalance.coerceAtLeast(0.0)
        val syubhatUsed = minOf(cleanAmount, quarantineBal)
        val remainingDisbursed = (cleanAmount - syubhatUsed).coerceAtLeast(0.0)

        val lines = buildList {
            if (syubhatUsed > 0.0) {
                add(JournalLine(accountId = "acc_syubhat", debit = syubhatUsed, credit = 0.0))
            }
            if (remainingDisbursed > 0.0) {
                add(JournalLine(accountId = "acc_disbursed", debit = remainingDisbursed, credit = 0.0))
            }
            add(JournalLine(accountId = resolvedAssetAccountId, debit = 0.0, credit = cleanAmount))
        }

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = "Tathhir Dana Non-Halal: $publicPurpose",
            transactionType = "INFAQ_PAYOUT",
            lines = lines
        )

        val distRecord = InfaqDistributionRecord(
            id = distId,
            amount = cleanAmount,
            recipientName = publicPurpose,
            asnafCategory = AsnafCategory.FISABILILLAH,
            distributionDate = date,
            hijriDateString = "${hijri.day} ${hijri.monthName} ${hijri.year} H",
            sourceAccountId = resolvedAssetAccountId,
            programName = "Tathhir (Pembersihan Dana Non-Halal / Bunga Bank)",
            receiptNumber = receiptNo,
            notes = "${notes.trim()} [Penyaluran dana non-halal/syubhat untuk kemaslahatan umum sesuai Fatwa DSN-MUI No. 123/2018]".trim(),
            isVerified = true
        )

        _uiState.update { state ->
            state.copy(
                journalEntries = listOf(entry) + state.journalEntries,
                infaqDistributions = listOf(distRecord) + state.infaqDistributions
            )
        }
        persistEntryAsync(entry)
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveInfaqDistribution(distRecord)
        }

        appStateNotifier.notify(
            title = "Tathhir Berhasil Dilaksanakan",
            message = "Dana penyucian Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(cleanAmount)} berhasil disalurkan untuk fasilitas umum ($publicPurpose) tanpa mengganggu Virtual Infaq Vault.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Menyucikan transaksi yang terindikasi syubhat/riba (mis. mengandung kata denda/bunga) langsung
     * dari layar Audit Kepatuhan Syariah: menyalurkan dana Tathhir dan menandai mutasi asal sebagai SUDAH DISUCIKAN.
     */
    fun purifyFlaggedTransaction(
        entryId: String,
        amount: Double,
        publicPurpose: String,
        fromWalletId: String,
        notes: String = ""
    ) {
        val cleanAmount = amount.coerceAtLeast(0.0)
        if (cleanAmount <= 0.0) {
            appStateNotifier.notify(
                title = "Nominal Tidak Valid",
                message = "Masukkan nominal dana penyucian yang lebih dari 0.",
                severity = NotificationSeverity.WARNING
            )
            return
        }
        val targetDesc = getJournalEntry(entryId)?.description.orEmpty()
        val combinedNotes = buildString {
            if (notes.isNotBlank()) append(notes.trim()).append(" • ")
            if (targetDesc.isNotBlank()) append("Penyucian catatan: $targetDesc")
        }
        disburseTathhirFund(
            amount = cleanAmount,
            publicPurpose = publicPurpose,
            fromWalletId = fromWalletId,
            notes = combinedNotes
        )
        markAuditEntryAsCleared(entryId, isPurification = true)
    }

    /**
     * Menandai mutasi hasil audit sebagai sudah diklarifikasi/suci (misal bukan riba atau sudah ditathhir)
     * agar tidak lagi memunculkan peringatan berulang pada Audit Kepatuhan Syariah.
     */
    fun markAuditEntryAsCleared(
        entryId: String,
        isPurification: Boolean = false
    ) {
        _uiState.update { state ->
            state.copy(purifiedAuditEntryIds = state.purifiedAuditEntryIds + entryId)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.markAuditEntryPurified(entryId)
        }
        if (!isPurification) {
            appStateNotifier.notify(
                title = "Status Audit Diperbarui",
                message = "Catatan telah ditandai aman / diklarifikasi non-riba pada Audit Syariah.",
                severity = NotificationSeverity.INFO
            )
        }
    }

    /**
     * Pencatatan Sedekah Subuh Harian & Pembaruan Streak
     */
    fun recordSedekahSubuh(
        amount: Double,
        note: String = "Sedekah Subuh Berkah Fajar",
        fromAccountId: String = "acc_cash",
        date: Date = Date()
    ) {
        val hijri = HijriCalendarEngine.fromGregorian(date)
        val journalId = UUID.randomUUID().toString()

        // 1. Jurnal Entry: Infaq Beban (Debit) dan Kas/Bank (Kredit)
        val lines = listOf(
            JournalLine(accountId = "acc_disbursed", debit = amount, credit = 0.0),
            JournalLine(accountId = fromAccountId, debit = 0.0, credit = amount)
        )

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = "Sedekah Subuh: $note",
            transactionType = "EXPENSE",
            lines = lines
        )

        // 2. Update Streak Map
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        val currentHistory = _uiState.value.sedekahSubuhState.historyMap.toMutableMap()
        currentHistory[todayKey] = (currentHistory[todayKey] ?: 0.0) + amount

        val updatedStreakState = SedekahSubuhStreakEngine.calculateStreak(
            historyMap = currentHistory,
            baseBadges = _uiState.value.sedekahSubuhState.badges.ifEmpty { SedekahSubuhState.getDefaultBadges() }
        )

        _uiState.update { state ->
            state.copy(
                journalEntries = listOf(entry) + state.journalEntries,
                sedekahSubuhState = updatedStreakState
            )
        }
        persistEntryAsync(entry)
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveSedekahSubuh(updatedStreakState)
        }
        AppDebugLogger.i("AmanahSedekah", "Sedekah Subuh tercatat: Rp ${amount.toLong()} via $fromAccountId (Streak: hari ke-${updatedStreakState.currentStreak})")

        appStateNotifier.notify(
            title = "Sedekah Subuh Tercatat",
            message = "Alhamdulillah, sedekah Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)} tercatat. Streak hari ke-${updatedStreakState.currentStreak}!",
            severity = NotificationSeverity.SUCCESS
        )
    }

    /**
     * Pencatatan Rezeki Non-Tunai (Hadiah, Traktiran, Bingkisan, Manfaat)
     * Mendukung alokasi langsung ke Virtual Infaq Vault bulanan, penunaian tunai langsung,
     * atau pencatatan murni tanpa memotong saldo kas seketika.
     */
    fun recordRezekiNonTunai(
        title: String,
        giverName: String = "",
        category: RezekiCategory = RezekiCategory.FOOD_BEVERAGE,
        estimatedValue: Double,
        suggestedInfaqRate: Double = 0.05,
        notes: String = "",
        dateMillis: Long = System.currentTimeMillis(),
        infaqAction: com.example.core.rezeki.RezekiInfaqAction = com.example.core.rezeki.RezekiInfaqAction.ALLOCATE_TO_VAULT,
        targetWalletId: String? = null,
        customInfaqAmount: Double? = null,
        onSuccess: ((RezekiNonTunaiRecord) -> Unit)? = null
    ) {
        val actualInfaq = customInfaqAmount ?: (estimatedValue * suggestedInfaqRate)
        val hijriDate = HijriCalendarEngine.fromGregorian(Date(dateMillis))
        val hijriStr = hijriDate.toString()
        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))

        var finalIsPaidInfaq = false
        var finalPaidJournalEntryId: String? = null
        var finalNotes = notes
        var generatedJournalEntry: JournalEntry? = null

        when (infaqAction) {
            com.example.core.rezeki.RezekiInfaqAction.ALLOCATE_TO_VAULT -> {
                if (actualInfaq > 0) {
                    val entryId = "vault_entry_${UUID.randomUUID().toString().take(8)}"
                    val desc = "Infaq Syukur atas ${title.ifBlank { "Rezeki" }} [Infaq Vault]"
                    val lines = listOf(
                        JournalLine(
                            accountId = "acc_infaq_expense",
                            debit = actualInfaq,
                            credit = 0.0
                        ),
                        JournalLine(
                            accountId = "acc_vault",
                            debit = 0.0,
                            credit = actualInfaq
                        )
                    )
                    val entry = JournalEntry(
                        id = entryId,
                        gregorianDate = Date(dateMillis),
                        hijriYear = hijriDate.year,
                        hijriMonth = hijriDate.month,
                        hijriDay = hijriDate.day,
                        description = desc,
                        transactionType = "INFAQ_PAYOUT",
                        lines = lines
                    )
                    persistEntryAsync(entry)
                    generatedJournalEntry = entry
                    finalIsPaidInfaq = true
                    finalPaidJournalEntryId = entryId
                    finalNotes = if (notes.isBlank()) "[Infaq Vault]" else "$notes\n[Infaq Vault]"
                }
            }
            com.example.core.rezeki.RezekiInfaqAction.PAY_DIRECT_NOW -> {
                if (actualInfaq > 0) {
                    val targetWallet = _uiState.value.wallets.find { it.id == targetWalletId }
                        ?: _uiState.value.wallets.firstOrNull()
                    if (targetWallet != null) {
                        val entryId = UUID.randomUUID().toString()
                        val desc = "Infaq Syukur atas ${title.ifBlank { "Rezeki" }} (${targetWallet.name})"
                        val lines = listOf(
                            JournalLine(
                                accountId = "acc_infaq_expense",
                                debit = actualInfaq,
                                credit = 0.0
                            ),
                            JournalLine(
                                accountId = targetWallet.linkedAccountId,
                                debit = 0.0,
                                credit = actualInfaq
                            )
                        )
                        val entry = JournalEntry(
                            id = entryId,
                            gregorianDate = Date(dateMillis),
                            hijriYear = hijriDate.year,
                            hijriMonth = hijriDate.month,
                            hijriDay = hijriDate.day,
                            description = desc,
                            transactionType = "INFAQ_PAYOUT",
                            lines = lines
                        )
                        persistEntryAsync(entry)
                        generatedJournalEntry = entry
                        finalIsPaidInfaq = true
                        finalPaidJournalEntryId = entryId
                    }
                }
            }
            com.example.core.rezeki.RezekiInfaqAction.SKIP_FOR_NOW -> {
                // Tidak ada jurnal infaq seketika
            }
        }

        val record = RezekiNonTunaiRecord(
            title = title.ifBlank { "Rezeki Pemberian" },
            giverName = giverName,
            category = category,
            estimatedValue = estimatedValue,
            suggestedInfaqRate = suggestedInfaqRate,
            suggestedInfaqAmount = actualInfaq,
            isPaidInfaq = finalIsPaidInfaq,
            paidJournalEntryId = finalPaidJournalEntryId,
            dateMillis = dateMillis,
            hijriDateString = hijriStr,
            notes = finalNotes
        )

        val db = roomDatabase
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db?.rezekiNonTunaiDao()?.insertOrUpdate(EntityMappers.toEntity(record))
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Gagal menyimpan rezeki non-tunai", e)
            }
            _uiState.update { current ->
                val newEntries = if (generatedJournalEntry != null) {
                    listOf(generatedJournalEntry) + current.journalEntries
                } else {
                    current.journalEntries
                }
                current.copy(
                    journalEntries = newEntries,
                    rezekiNonTunaiList = listOf(record) + current.rezekiNonTunaiList.filter { it.id != record.id }
                )
            }
            withContext(Dispatchers.Main) {
                onSuccess?.invoke(record)
            }
        }

        val notifyMsg = when (infaqAction) {
            com.example.core.rezeki.RezekiInfaqAction.ALLOCATE_TO_VAULT ->
                "Alhamdulillah, rezeki '${record.title}' tercatat. Infaq syukur Rp ${nf.format(actualInfaq.toLong())} telah ditambahkan ke Infaq Vault bulanan Anda."
            com.example.core.rezeki.RezekiInfaqAction.PAY_DIRECT_NOW ->
                "Alhamdulillah, rezeki '${record.title}' tercatat dan infaq syukur Rp ${nf.format(actualInfaq.toLong())} berhasil ditunaikan secara tunai."
            com.example.core.rezeki.RezekiInfaqAction.SKIP_FOR_NOW ->
                "Alhamdulillah, rezeki '${record.title}' (taksiran Rp ${nf.format(estimatedValue.toLong())}) berhasil dicatat."
        }

        appStateNotifier.notify(
            title = if (infaqAction == com.example.core.rezeki.RezekiInfaqAction.ALLOCATE_TO_VAULT) "Rezeki & Infaq Vault Tersimpan" else "Rezeki Dicatat",
            message = notifyMsg,
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun deleteRezekiNonTunai(id: String) {
        val target = _uiState.value.rezekiNonTunaiList.find { it.id == id }
        val db = roomDatabase
        val journalId = target?.paidJournalEntryId

        viewModelScope.launch(Dispatchers.IO) {
            if (journalId != null) {
                try {
                    db?.journalDao()?.deleteById(journalId)
                } catch (e: Exception) {
                    android.util.Log.e("AmanahLedgerVM", "Gagal menghapus entri jurnal terkait rezeki", e)
                }
            }
            try {
                db?.rezekiNonTunaiDao()?.deleteById(id)
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Gagal menghapus rezeki non-tunai", e)
            }
            _uiState.update { current ->
                current.copy(
                    rezekiNonTunaiList = current.rezekiNonTunaiList.filter { it.id != id },
                    journalEntries = if (journalId != null) current.journalEntries.filter { it.id != journalId } else current.journalEntries
                )
            }
            withContext(Dispatchers.Main) {
                appStateNotifier.notify(
                    title = "Catatan Rezeki Dihapus",
                    message = "Catatan '${target?.title ?: "Rezeki"}' berhasil dihapus.${if (journalId != null) " Entri jurnal infaq terkait telah disesuaikan." else ""}",
                    severity = NotificationSeverity.INFO
                )
            }
        }
    }

    fun updateRezekiNonTunai(
        id: String,
        title: String,
        giverName: String,
        category: com.example.core.rezeki.RezekiCategory,
        estimatedValue: Double,
        suggestedInfaqRate: Double = 0.05,
        notes: String = "",
        dateMillis: Long = System.currentTimeMillis(),
        onSuccess: (() -> Unit)? = null
    ) {
        val currentRecord = _uiState.value.rezekiNonTunaiList.find { it.id == id } ?: return
        val newInfaqAmount = estimatedValue * suggestedInfaqRate

        val updatedRecord = currentRecord.copy(
            title = title.trim(),
            giverName = giverName.trim(),
            category = category,
            estimatedValue = estimatedValue,
            suggestedInfaqRate = suggestedInfaqRate,
            suggestedInfaqAmount = newInfaqAmount,
            notes = notes.trim(),
            dateMillis = dateMillis
        )

        val journalId = updatedRecord.paidJournalEntryId
        val isVaultAllocated = updatedRecord.isVaultAllocated
        val db = roomDatabase

        viewModelScope.launch(Dispatchers.IO) {
            var updatedEntries = _uiState.value.journalEntries
            if (journalId != null && isVaultAllocated) {
                val existingEntry = updatedEntries.find { it.id == journalId }
                if (existingEntry != null) {
                    val updatedLines = existingEntry.lines.map { line ->
                        if (line.accountId == "acc_infaq_expense") {
                            line.copy(debit = newInfaqAmount)
                        } else if (line.accountId == "acc_vault") {
                            line.copy(credit = newInfaqAmount)
                        } else {
                            line
                        }
                    }
                    val updatedEntry = existingEntry.copy(
                        description = "Infaq Syukur atas ${updatedRecord.title} [Infaq Vault]",
                        lines = updatedLines
                    )
                    try {
                        db?.journalDao()?.insertOrUpdate(EntityMappers.toEntity(updatedEntry))
                    } catch (e: Exception) {
                        android.util.Log.e("AmanahLedgerVM", "Gagal memperbarui entri jurnal vault rezeki", e)
                    }
                    updatedEntries = updatedEntries.map { if (it.id == journalId) updatedEntry else it }
                }
            }

            try {
                db?.rezekiNonTunaiDao()?.insertOrUpdate(EntityMappers.toEntity(updatedRecord))
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Gagal memperbarui rezeki non-tunai di DB", e)
            }

            _uiState.update { current ->
                current.copy(
                    rezekiNonTunaiList = current.rezekiNonTunaiList.map { if (it.id == id) updatedRecord else it },
                    journalEntries = updatedEntries
                )
            }

            withContext(Dispatchers.Main) {
                onSuccess?.invoke()
                val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
                appStateNotifier.notify(
                    title = "Rezeki Berhasil Diperbarui",
                    message = "Catatan rezeki '${updatedRecord.title}' (Taksiran Rp ${nf.format(estimatedValue.toLong())}) berhasil diperbarui.",
                    severity = NotificationSeverity.SUCCESS
                )
            }
        }
    }

    fun tunaikanInfaqSyukur(
        rezekiId: String,
        walletId: String,
        infaqAmount: Double,
        note: String = "",
        isAllocateToVault: Boolean = false
    ) {
        val rezeki = _uiState.value.rezekiNonTunaiList.find { it.id == rezekiId } ?: return
        val actualInfaq = if (infaqAmount > 0) infaqAmount else rezeki.suggestedInfaqAmount
        if (actualInfaq <= 0) return

        val now = Date()
        val hijriDate = HijriCalendarEngine.fromGregorian(now)

        val entry = if (isAllocateToVault) {
            val entryId = "vault_entry_${UUID.randomUUID().toString().take(8)}"
            val desc = if (note.isNotBlank()) note else "Infaq Syukur atas ${rezeki.title} [Infaq Vault]"
            val lines = listOf(
                JournalLine(
                    accountId = "acc_infaq_expense",
                    debit = actualInfaq,
                    credit = 0.0
                ),
                JournalLine(
                    accountId = "acc_vault",
                    debit = 0.0,
                    credit = actualInfaq
                )
            )
            JournalEntry(
                id = entryId,
                gregorianDate = now,
                hijriYear = hijriDate.year,
                hijriMonth = hijriDate.month,
                hijriDay = hijriDate.day,
                description = desc,
                transactionType = "INFAQ_PAYOUT",
                lines = lines
            )
        } else {
            val targetWallet = _uiState.value.wallets.find { it.id == walletId }
                ?: _uiState.value.wallets.firstOrNull()
                ?: return
            val entryId = UUID.randomUUID().toString()
            val desc = if (note.isNotBlank()) note else "Infaq Syukur atas ${rezeki.title}"
            val lines = listOf(
                JournalLine(
                    accountId = "acc_infaq_expense",
                    debit = actualInfaq,
                    credit = 0.0
                ),
                JournalLine(
                    accountId = targetWallet.linkedAccountId,
                    debit = 0.0,
                    credit = actualInfaq
                )
            )
            JournalEntry(
                id = entryId,
                gregorianDate = now,
                hijriYear = hijriDate.year,
                hijriMonth = hijriDate.month,
                hijriDay = hijriDate.day,
                description = desc,
                transactionType = "INFAQ_PAYOUT",
                lines = lines
            )
        }

        persistEntryAsync(entry)
        val db = roomDatabase
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db?.rezekiNonTunaiDao()?.markInfaqPaid(rezekiId, entry.id)
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Gagal menandai infaq rezeki", e)
            }
        }

        _uiState.update { current ->
            current.copy(
                journalEntries = listOf(entry) + current.journalEntries,
                rezekiNonTunaiList = current.rezekiNonTunaiList.map {
                    if (it.id == rezekiId) it.copy(isPaidInfaq = true, paidJournalEntryId = entry.id) else it
                }
            )
        }

        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))
        val successMsg = if (isAllocateToVault) {
            "Infaq syukur Rp ${nf.format(actualInfaq.toLong())} berhasil ditambahkan ke saldo Infaq Vault bulanan."
        } else {
            val wName = _uiState.value.wallets.find { it.id == walletId }?.name ?: "Kantong Kas"
            "Infaq syukur Rp ${nf.format(actualInfaq.toLong())} dari $wName berhasil disalurkan."
        }

        appStateNotifier.notify(
            title = if (isAllocateToVault) "Masuk Infaq Vault" else "Infaq Syukur Berhasil",
            message = successMsg,
            severity = NotificationSeverity.SUCCESS
        )
    }


    fun setFinancialGoalMode(mode: FinancialGoalMode) {
        _uiState.update { it.copy(selectedGoalMode = mode) }
    }

    fun applyBudgetCapSuggestions(suggestions: List<BudgetCapSuggestion>) {
        _uiState.update { state ->
            val updatedBudgets = state.budgets.map { b ->
                val suggestion = suggestions.firstOrNull { it.accountId == b.accountId }
                if (suggestion != null) {
                    b.copy(monthlyLimit = suggestion.suggestedCap)
                } else b
            }
            state.copy(budgets = updatedBudgets)
        }
        appStateNotifier.notify(
            title = "Pagu Anggaran Diperbarui",
            message = "Saran optimasi pagu syariah berhasil diterapkan untuk ${suggestions.size} kategori.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun applySingleBudgetCapSuggestion(suggestion: BudgetCapSuggestion) {
        _uiState.update { state ->
            val updatedBudgets = state.budgets.map { b ->
                if (b.accountId == suggestion.accountId) {
                    b.copy(monthlyLimit = suggestion.suggestedCap)
                } else b
            }
            state.copy(budgets = updatedBudgets)
        }
        appStateNotifier.notify(
            title = "Pagu ${suggestion.categoryName} Disesuaikan",
            message = "Pagu baru Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(suggestion.suggestedCap)} berhasil disetel.",
            severity = NotificationSeverity.INFO
        )
    }

    fun setBudget(
        accountId: String,
        categoryName: String,
        monthlyLimit: Double,
        alertThresholdPercent: Double = 0.8,
        iconKey: String = "shopping"
    ) {
        _uiState.update { state ->
            val existing = state.budgets.firstOrNull { it.accountId == accountId }
            val updatedBudgets = if (existing != null) {
                state.budgets.map {
                    if (it.accountId == accountId) {
                        it.copy(
                            categoryName = categoryName,
                            monthlyLimit = monthlyLimit,
                            alertThresholdPercent = alertThresholdPercent,
                            iconKey = iconKey
                        )
                    } else it
                }
            } else {
                val newBudget = BudgetAllocation(
                    id = "b_${UUID.randomUUID()}",
                    accountId = accountId,
                    categoryName = categoryName,
                    monthlyLimit = monthlyLimit,
                    iconKey = iconKey,
                    alertThresholdPercent = alertThresholdPercent
                )
                state.budgets + newBudget
            }
            state.copy(budgets = updatedBudgets)
        }
        val budgetToPersist = _uiState.value.budgets.firstOrNull { it.accountId == accountId }
        if (budgetToPersist != null) persistBudgetAsync(budgetToPersist)
        AppDebugLogger.i("AmanahBudget", "Pagu Anggaran '$categoryName' disetel: Rp ${monthlyLimit.toLong()} (Ambang: ${(alertThresholdPercent * 100).toInt()}%)")
    }

    fun deleteBudget(budgetId: String) {
        val target = _uiState.value.budgets.firstOrNull { it.id == budgetId }
        _uiState.update { state ->
            state.copy(budgets = state.budgets.filterNot { it.id == budgetId })
        }
        deleteBudgetAsync(budgetId)
        AppDebugLogger.w("AmanahBudget", "Menghapus alokasi anggaran: ${target?.categoryName ?: budgetId}")
    }

    fun addRule(rule: InfaqRule) {
        _uiState.update { state ->
            state.copy(rules = state.rules + rule)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveInfaqRule(rule)
        }
    }

    fun deleteRule(id: String) {
        _uiState.update { state ->
            state.copy(rules = state.rules.filterNot { it.id == id })
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.deleteInfaqRule(id)
        }
    }

    fun updateRule(rule: InfaqRule) {
        _uiState.update { state ->
            state.copy(rules = state.rules.map { if (it.id == rule.id) rule else it })
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveInfaqRule(rule)
        }
        appStateNotifier.notify(
            title = "Aturan Infaq Diperbarui",
            message = "Aturan '${rule.title}' berhasil disesuaikan.",
            severity = NotificationSeverity.SUCCESS
        )
    }


    private var preHighContrastThemeMode: com.example.ui.theme.AppThemeMode? = null

    fun toggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        val newThemeMode = if (newMode) com.example.ui.theme.AppThemeMode.ELEGANT_DARK else com.example.ui.theme.AppThemeMode.LIGHT_MODE
        preHighContrastThemeMode = newThemeMode
        _uiState.update { state ->
            state.copy(isDarkMode = newMode, themeMode = newThemeMode)
        }
        viewModelScope.launch { repository?.saveThemeMode(newThemeMode) }
    }

    fun setDarkMode(isDark: Boolean) {
        val newThemeMode = if (isDark) com.example.ui.theme.AppThemeMode.ELEGANT_DARK else com.example.ui.theme.AppThemeMode.LIGHT_MODE
        preHighContrastThemeMode = newThemeMode
        _uiState.update { state ->
            state.copy(isDarkMode = isDark, themeMode = newThemeMode)
        }
        viewModelScope.launch { repository?.saveThemeMode(newThemeMode) }
    }

    fun toggleHighContrast() {
        val newContrast = !_uiState.value.isHighContrast
        val currentMode = _uiState.value.themeMode
        if (newContrast && currentMode != com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK && currentMode != com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT) {
            preHighContrastThemeMode = currentMode
        }
        val newThemeMode = when {
            newContrast && _uiState.value.isDarkMode -> com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK
            newContrast && !_uiState.value.isDarkMode -> com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT
            !newContrast && preHighContrastThemeMode != null -> preHighContrastThemeMode!!
            !newContrast && _uiState.value.isDarkMode -> com.example.ui.theme.AppThemeMode.ELEGANT_DARK
            else -> com.example.ui.theme.AppThemeMode.LIGHT_MODE
        }
        val effectiveDark = when (newThemeMode) {
            com.example.ui.theme.AppThemeMode.TACTICAL_CYBER,
            com.example.ui.theme.AppThemeMode.ELEGANT_DARK,
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK -> true
            com.example.ui.theme.AppThemeMode.TACTICAL_LIGHT,
            com.example.ui.theme.AppThemeMode.LIGHT_MODE,
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT -> false
            com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM -> _uiState.value.isDarkMode
        }
        _uiState.update { state ->
            state.copy(isHighContrast = newContrast, isDarkMode = effectiveDark, themeMode = newThemeMode)
        }
        viewModelScope.launch { repository?.saveThemeMode(newThemeMode) }
    }

    fun setHighContrast(isHigh: Boolean) {
        val currentMode = _uiState.value.themeMode
        if (isHigh && currentMode != com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK && currentMode != com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT) {
            preHighContrastThemeMode = currentMode
        }
        val newThemeMode = when {
            isHigh && _uiState.value.isDarkMode -> com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK
            isHigh && !_uiState.value.isDarkMode -> com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT
            !isHigh && preHighContrastThemeMode != null -> preHighContrastThemeMode!!
            !isHigh && _uiState.value.isDarkMode -> com.example.ui.theme.AppThemeMode.ELEGANT_DARK
            else -> com.example.ui.theme.AppThemeMode.LIGHT_MODE
        }
        _uiState.update { state ->
            state.copy(isHighContrast = isHigh, themeMode = newThemeMode)
        }
        viewModelScope.launch { repository?.saveThemeMode(newThemeMode) }
    }

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        if (mode != com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT && mode != com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK) {
            preHighContrastThemeMode = mode
        }
        val (isDark, isHigh) = when (mode) {
            com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM -> _uiState.value.isDarkMode to false
            com.example.ui.theme.AppThemeMode.TACTICAL_CYBER -> true to false
            com.example.ui.theme.AppThemeMode.TACTICAL_LIGHT -> false to false
            com.example.ui.theme.AppThemeMode.ELEGANT_DARK -> true to false
            com.example.ui.theme.AppThemeMode.LIGHT_MODE -> false to false
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT -> false to true
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK -> true to true
        }
        _uiState.update { state ->
            state.copy(themeMode = mode, isDarkMode = isDark, isHighContrast = isHigh)
        }
        viewModelScope.launch {
            repository?.saveThemeMode(mode)
        }
    }

    fun setFollowSystemTheme(enabled: Boolean) {
        if (enabled) {
            setThemeMode(com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM)
        } else {
            setThemeMode(if (_uiState.value.isDarkMode) com.example.ui.theme.AppThemeMode.ELEGANT_DARK else com.example.ui.theme.AppThemeMode.LIGHT_MODE)
        }
    }

    fun setUiScaleMode(mode: com.example.ui.theme.UiScaleMode) {
        _uiState.update { state ->
            state.copy(
                uiScaleMode = mode,
                uiScaleFactor = mode.scaleFactor
            )
        }
        viewModelScope.launch { repository?.saveUiScale(mode, mode.scaleFactor) }
        appStateNotifier.notify(
            title = "Skala Tampilan Diubah",
            message = "Ukuran antarmuka disesuaikan menjadi ${mode.displayName}.",
            severity = NotificationSeverity.INFO
        )
    }

    fun setUiScaleFactor(factor: Float) {
        val clamped = factor.coerceIn(0.75f, 1.50f)
        val matchingMode = com.example.ui.theme.UiScaleMode.values().minByOrNull { kotlin.math.abs(it.scaleFactor - clamped) }
            ?: com.example.ui.theme.UiScaleMode.DEFAULT
        _uiState.update { state ->
            state.copy(
                uiScaleFactor = clamped,
                uiScaleMode = matchingMode
            )
        }
        viewModelScope.launch { repository?.saveUiScale(matchingMode, clamped) }
    }

    fun addRecurringTransaction(recurring: RecurringTransaction) {
        _uiState.update { state ->
            state.copy(recurringTransactions = state.recurringTransactions + recurring)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveRecurringTransaction(recurring)
        }
        appStateNotifier.notify(
            title = "Jadwal Baru Ditambahkan",
            message = "Transaksi rutin '${recurring.title}' dijadwalkan otomatis.",
            severity = NotificationSeverity.INFO
        )
    }

    fun updateRecurringTransaction(recurring: RecurringTransaction) {
        _uiState.update { state ->
            val updated = state.recurringTransactions.map {
                if (it.id == recurring.id) recurring else it
            }
            state.copy(recurringTransactions = updated)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveRecurringTransaction(recurring)
        }
        appStateNotifier.notify(
            title = "Jadwal Diperbarui",
            message = "Transaksi rutin '${recurring.title}' berhasil disesuaikan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun deleteRecurringTransaction(id: String) {
        val target = _uiState.value.recurringTransactions.firstOrNull { it.id == id }
        _uiState.update { state ->
            state.copy(recurringTransactions = state.recurringTransactions.filterNot { it.id == id })
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.deleteRecurringTransaction(id)
        }
        if (target != null) {
            appStateNotifier.notify(
                title = "Jadwal Dihapus",
                message = "Transaksi rutin '${target.title}' telah dihapus dari antrian.",
                severity = NotificationSeverity.INFO
            )
        }
    }

    fun toggleRecurringTransaction(id: String) {
        var updatedItem: RecurringTransaction? = null
        _uiState.update { state ->
            val updated = state.recurringTransactions.map {
                if (it.id == id) {
                    val item = it.copy(isActive = !it.isActive)
                    updatedItem = item
                    item
                } else it
            }
            state.copy(recurringTransactions = updated)
        }
        updatedItem?.let { item ->
            viewModelScope.launch(Dispatchers.IO) {
                repository?.saveRecurringTransaction(item)
            }
        }
    }

    fun executeRecurringTransaction(id: String, date: Date = Date()): Pair<Boolean, Double> {
        val target = _uiState.value.recurringTransactions.firstOrNull { it.id == id } ?: return Pair(false, 0.0)
        var infaqAmount = 0.0

        if (target.type == RecurringType.INCOME) {
            val netAmount = target.amount * (1.0 - target.customInfaqRate)
            infaqAmount = target.amount * target.customInfaqRate
            recordIncome(
                grossAmount = target.amount,
                incomeAccountId = target.categoryAccountId,
                depositAccountId = target.assetAccountId,
                customInfaqRate = target.customInfaqRate,
                description = "${target.title} (Otomatis)",
                date = date
            )
        } else {
            val currentBal = _uiState.value.getAccountBalance(target.assetAccountId)
            if (_uiState.value.isDeficitProtectionEnabled && target.amount > currentBal) {
                AppDebugLogger.w("AmanahVM", "Transaksi rutin '${target.title}' tertunda karena saldo akun '${target.assetAccountId}' tidak mencukupi (Saldo: $currentBal, Dibutuhkan: ${target.amount})")
                appStateNotifier.notify(
                    title = "Transaksi Rutin Tertunda",
                    message = "Saldo kas/bank tidak mencukupi untuk transaksi rutin '${target.title}' (Tersedia: Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(currentBal)}).",
                    severity = NotificationSeverity.WARNING
                )
                return Pair(false, 0.0)
            }
            val roundUp = if (target.enableRoundUp && target.roundUpStep > 0) {
                val rem = target.amount % target.roundUpStep
                if (rem > 0) target.roundUpStep - rem else 0.0
            } else 0.0
            infaqAmount = roundUp
            recordExpense(
                amount = target.amount,
                expenseAccountId = target.categoryAccountId,
                fromAccountId = target.assetAccountId,
                enableRoundUp = target.enableRoundUp,
                roundUpStep = target.roundUpStep,
                description = "${target.title} (Otomatis)",
                date = date
            )
        }

        // Update last executed date and next due date
        val nextDue = RecurringTransaction.calculateNextDueDate(
            lastDate = date,
            frequency = target.frequency,
            dayValue = target.dayOfMonthOrWeek
        )

        val updatedTarget = target.copy(
            lastExecutedDate = date,
            lastExecutedPeriodKey = target.generatePeriodKey(date),
            lastExecutionTimestamp = date.time,
            nextDueDate = nextDue
        )

        _uiState.update { state ->
            val updated = state.recurringTransactions.map {
                if (it.id == id) updatedTarget else it
            }
            state.copy(recurringTransactions = updated)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveRecurringTransaction(updatedTarget)
        }

        return Pair(true, infaqAmount)
    }


    fun processAllDueRecurringTransactions(): Int {
        val dueList = _uiState.value.recurringTransactions.filter { it.isDue() && it.isActive && it.autoExecute }
        if (dueList.isEmpty()) return 0

        return appStateNotifier.handleScheduledTasks(dueTasks = dueList) { task ->
            executeRecurringTransaction(task.id)
        }
    }

    fun updateGoldPrice(newPrice: Double) {
        updateGoldPriceWithMetadata(newPrice, "Input Manual Pengguna", System.currentTimeMillis())
    }

    fun updateGoldPriceWithMetadata(
        newPrice: Double,
        source: String = "Antam / Standar BAZNAS",
        timestampMillis: Long = System.currentTimeMillis()
    ) {
        val cleanPrice = kotlin.math.round(newPrice)
        if (cleanPrice > 0) {
            _uiState.update { state ->
                state.copy(
                    goldPricePerGram = cleanPrice,
                    goldPriceSource = source,
                    goldPriceLastUpdatedMillis = timestampMillis,
                    zakatPerniagaan = state.zakatPerniagaan.copy(goldPricePerGram = cleanPrice)
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                repository?.saveGoldPriceWithMetadata(cleanPrice, source, timestampMillis)
            }
            appStateNotifier.notify(
                title = "Harga Emas Diperbarui",
                message = "Acuan hisab zakat: Rp ${cleanPrice.toLong()}/gram ($source)",
                severity = NotificationSeverity.INFO
            )
        }
    }

    fun loadMoreTransactions(step: Int = 50) {
        _uiState.update { state ->
            state.copy(transactionDisplayLimit = state.transactionDisplayLimit + step)
        }
    }

    // ==========================================
    // SECURITY & APP LOCK MANAGEMENT
    // ==========================================

    private var lastBackgroundTimestamp: Long = 0L
    private var isSessionAuthenticated: Boolean = false
    private var hasAppliedInitialMaskDefault: Boolean = false
    private var hasProcessedStartupRecurring: Boolean = false
    private val APP_LOCK_GRACE_PERIOD_MILLIS = 8_000L // 8 detik toleransi jeda (grace period) saat berpindah aplikasi sejenak

    // ==========================================
    // TRANSACTION DRAFT PERSISTENCE (STATE PRESERVATION)
    // ==========================================
    private val _transactionDraft = MutableStateFlow(TransactionDraftState())
    val transactionDraft: StateFlow<TransactionDraftState> = _transactionDraft.asStateFlow()

    fun updateTransactionDraft(draft: TransactionDraftState) {
        _transactionDraft.value = draft
    }

    fun clearTransactionDraft() {
        _transactionDraft.value = TransactionDraftState()
    }

    fun onAppBackgrounded() {
        val current = _uiState.value.securityConfig
        if (!current.isPinEnabled) return
        lastBackgroundTimestamp = System.currentTimeMillis()
        if (current.autoLockInterval == AutoLockInterval.IMMEDIATE) {
            isSessionAuthenticated = false
            lockApp()
        }
        AppDebugLogger.i("AmanahSecurity", "Aplikasi beralih ke latar belakang pada $lastBackgroundTimestamp.")
    }

    fun onAppForegrounded() {
        val current = _uiState.value.securityConfig
        if (!current.isPinEnabled) return
        if (current.isAppLocked) return

        if (lastBackgroundTimestamp > 0L) {
            val elapsedMillis = System.currentTimeMillis() - lastBackgroundTimestamp
            val elapsedSeconds = elapsedMillis / 1000
            AppDebugLogger.i("AmanahSecurity", "Aplikasi kembali ke latar depan. Durasi di latar belakang: ${elapsedMillis}ms.")

            if (current.autoLockInterval == AutoLockInterval.IMMEDIATE) {
                // Terapkan grace period sebelum mengunci aplikasi secara instan
                if (elapsedMillis >= APP_LOCK_GRACE_PERIOD_MILLIS) {
                    isSessionAuthenticated = false
                    lockApp()
                    AppDebugLogger.w("AmanahSecurity", "Kunci aplikasi aktif setelah grace period terlampaui.")
                } else {
                    AppDebugLogger.i("AmanahSecurity", "Aplikasi dibuka kembali dalam grace period (${elapsedMillis}ms). Form transaksi tetap aman.")
                }
            } else if (current.autoLockInterval != AutoLockInterval.NEVER && elapsedSeconds >= current.autoLockInterval.seconds) {
                isSessionAuthenticated = false
                lockApp()
                AppDebugLogger.w("AmanahSecurity", "Kunci aplikasi aktif sesuai interval ${current.autoLockInterval.label}.")
            }
        }
    }

    fun enablePin(pin: String, question: String, answer: String): Boolean {
        if (pin.length < 4) return false
        val hashedPin = SecurityConfig.hashString(pin)
        val hashedAnswer = SecurityConfig.hashString(answer.trim().lowercase(Locale.ROOT))
        val newConfig = _uiState.value.securityConfig.copy(
            isPinEnabled = true,
            pinHash = hashedPin,
            securityQuestion = question,
            securityAnswerHash = hashedAnswer,
            failedAttempts = 0,
            lockoutUntilTimestamp = 0L,
            autoLockInterval = AutoLockInterval.IMMEDIATE,
            isAppLocked = false
        )
        isSessionAuthenticated = true
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.PIN_ENABLED,
                description = "Kunci Keamanan PIN berhasil diaktifkan"
            )
            state.copy(
                securityConfig = newConfig.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newConfig) }
        return true
    }

    fun disablePin(enteredPin: String): Boolean {
        val current = _uiState.value.securityConfig
        if (current.isPinEnabled && !current.verifyPin(enteredPin)) {
            recordSecurityEvent(
                SecurityEventType.PIN_FAILED,
                "Gagal menonaktifkan PIN: PIN otentikasi salah",
                isWarning = true
            )
            return false
        }

        val newConfig = _uiState.value.securityConfig.copy(
            isPinEnabled = false,
            pinHash = "",
            failedAttempts = 0,
            lockoutUntilTimestamp = 0L,
            isAppLocked = false
        )
        isSessionAuthenticated = false
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.PIN_DISABLED,
                description = "Kunci Keamanan PIN dinonaktifkan oleh pengguna"
            )
            state.copy(
                securityConfig = newConfig.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newConfig) }
        return true
    }

    fun changePin(oldPin: String, newPin: String): Boolean {
        val current = _uiState.value.securityConfig
        if (current.isPinEnabled && !current.verifyPin(oldPin)) {
            recordSecurityEvent(
                SecurityEventType.PIN_FAILED,
                "Gagal mengganti PIN: PIN lama tidak sesuai",
                isWarning = true
            )
            return false
        }
        if (newPin.length < 4) return false

        val hashedNewPin = SecurityConfig.hashString(newPin)
        val newConfig = _uiState.value.securityConfig.copy(
            pinHash = hashedNewPin,
            failedAttempts = 0,
            lockoutUntilTimestamp = 0L
        )
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.PIN_CHANGED,
                description = "PIN Kunci Aplikasi berhasil diperbarui"
            )
            state.copy(
                securityConfig = newConfig.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newConfig) }
        return true
    }

    fun unlockWithPin(enteredPin: String): Boolean {
        val current = _uiState.value.securityConfig
        if (current.isCurrentlyLockedOut()) {
            return false
        }

        val isValid = current.verifyPin(enteredPin)
        if (isValid) {
            isSessionAuthenticated = true
            _uiState.update { state ->
                val newLog = SecurityLogEntry(
                    eventType = SecurityEventType.PIN_SUCCESS,
                    description = "Otentikasi PIN berhasil"
                )
                state.copy(
                    securityConfig = state.securityConfig.copy(
                        isAppLocked = false,
                        failedAttempts = 0,
                        lockoutUntilTimestamp = 0L,
                        logs = listOf(newLog) + state.securityConfig.logs
                    )
                )
            }
            return true
        } else {
            val newFailCount = current.failedAttempts + 1
            val lockout = if (newFailCount >= 5) {
                System.currentTimeMillis() + 30_000 // 30 detik penalti lockout
            } else 0L

            _uiState.update { state ->
                val newLog = SecurityLogEntry(
                    eventType = SecurityEventType.PIN_FAILED,
                    description = "Percobaan PIN gagal ($newFailCount kali berturut-turut)",
                    isWarning = true
                )
                state.copy(
                    securityConfig = state.securityConfig.copy(
                        failedAttempts = newFailCount,
                        lockoutUntilTimestamp = lockout,
                        logs = listOf(newLog) + state.securityConfig.logs
                    )
                )
            }
            return false
        }
    }

    fun unlockWithBiometric(): Boolean {
        val current = _uiState.value.securityConfig
        if (!current.isBiometricEnabled || current.isCurrentlyLockedOut()) return false

        isSessionAuthenticated = true
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.BIOMETRIC_SUCCESS,
                description = "Otentikasi Sidik Jari / Biometrik berhasil"
            )
            state.copy(
                securityConfig = state.securityConfig.copy(
                    isAppLocked = false,
                    failedAttempts = 0,
                    lockoutUntilTimestamp = 0L,
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        return true
    }

    fun lockApp() {
        val current = _uiState.value.securityConfig
        if (!current.isPinEnabled) return
        isSessionAuthenticated = false
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.APP_LOCKED,
                description = "Aplikasi terkunci (Kunci Layar Aktif)"
            )
            state.copy(
                securityConfig = state.securityConfig.copy(
                    isAppLocked = true,
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
    }

    fun lockAppNow() = lockApp()

    fun resetPinWithRecovery(answer: String, newPin: String): Boolean {
        val current = _uiState.value.securityConfig
        if (!current.verifyRecoveryAnswer(answer)) {
            recordSecurityEvent(
                SecurityEventType.PIN_FAILED,
                "Gagal pemulihan PIN: Jawaban keamanan salah",
                isWarning = true
            )
            return false
        }
        if (newPin.length < 4) return false

        val hashedNewPin = SecurityConfig.hashString(newPin)
        val newCfg = current.copy(
            pinHash = hashedNewPin,
            failedAttempts = 0,
            lockoutUntilTimestamp = 0L,
            isAppLocked = false
        )
        isSessionAuthenticated = true
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.SECURITY_QUESTION_RESET,
                description = "PIN diatur ulang menggunakan Verifikasi Pertanyaan Pemulihan"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
        return true
    }

    fun updateSecurityRecovery(currentPin: String, newQuestion: String, newAnswer: String): Boolean {
        val current = _uiState.value.securityConfig
        if (current.isPinEnabled && !current.verifyPin(currentPin)) {
            recordSecurityEvent(
                SecurityEventType.PIN_FAILED,
                "Gagal mengubah pertanyaan pemulihan: PIN salah",
                isWarning = true
            )
            return false
        }
        if (newQuestion.isBlank() || newAnswer.isBlank()) return false

        val hashedAnswer = SecurityConfig.hashString(newAnswer.trim().lowercase(Locale.ROOT))
        val newCfg = current.copy(
            securityQuestion = newQuestion.trim(),
            securityAnswerHash = hashedAnswer
        )
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.SECURITY_QUESTION_RESET,
                description = "Pertanyaan dan jawaban pemulihan PIN berhasil diperbarui"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
        appStateNotifier.notify(
            title = "Pemulihan PIN Diperbarui",
            message = "Pertanyaan dan kunci jawaban pemulihan darurat berhasil disimpan.",
            severity = NotificationSeverity.SUCCESS
        )
        return true
    }

    fun setAutoLockInterval(interval: AutoLockInterval) {
        val newCfg = _uiState.value.securityConfig.copy(autoLockInterval = interval)
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.SECURITY_ALERT,
                description = "Interval kunci otomatis diubah ke: ${interval.label}"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val newCfg = _uiState.value.securityConfig.copy(isBiometricEnabled = enabled)
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.SECURITY_ALERT,
                description = if (enabled) "Otentikasi Biometrik diaktifkan" else "Otentikasi Biometrik dinonaktifkan"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
    }

    fun toggleBalancePrivacy() {
        val newMaskState = !_uiState.value.securityConfig.isMaskBalance
        val newCfg = _uiState.value.securityConfig.copy(isMaskBalance = newMaskState)
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.BALANCE_PRIVACY_TOGGLED,
                description = if (newMaskState) "Mode Privasi Saldo diaktifkan (Disamarkan)" else "Mode Privasi Saldo dinonaktifkan"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch {
            repository?.saveSecurityConfig(newCfg)
            repository?.saveMaskBalance(newMaskState)
        }
    }

    fun setShakeFeedbackEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isShakeFeedbackEnabled = enabled) }
        viewModelScope.launch {
            repository?.saveShakeFeedbackEnabled(enabled)
        }
    }

    fun setMaskBalanceByDefault(enabled: Boolean) {
        val newCfg = _uiState.value.securityConfig.copy(
            maskBalanceByDefault = enabled,
            isMaskBalance = if (enabled) true else _uiState.value.securityConfig.isMaskBalance
        )
        _uiState.update { state ->
            state.copy(
                securityConfig = newCfg.copy(
                    logs = state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
    }

    fun setScreenshotProtection(enabled: Boolean) {
        val newCfg = _uiState.value.securityConfig.copy(isScreenshotProtected = enabled)
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.SECURITY_ALERT,
                description = if (enabled) "Perlindungan Layar & Anti-Screenshot diaktifkan" else "Perlindungan Layar dinonaktifkan"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
    }

    fun clearSecurityLogs() {
        _uiState.update { state ->
            state.copy(
                securityConfig = state.securityConfig.copy(
                    logs = listOf(
                        SecurityLogEntry(
                            eventType = SecurityEventType.SECURITY_ALERT,
                            description = "Riwayat log keamanan dibersihkan oleh pengguna"
                        )
                    )
                )
            )
        }
    }

    private fun recordSecurityEvent(type: SecurityEventType, desc: String, isWarning: Boolean = false) {
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = type,
                description = desc,
                isWarning = isWarning
            )
            state.copy(
                securityConfig = state.securityConfig.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
    }

    // ==========================================
    // PENGATURAN TERPUSAT (CENTRALIZED SETTINGS)
    // ==========================================

    fun setUserNameKasMukmin(name: String) = updateUserName(name)

    fun setPrimaryCurrencySymbol(symbol: String) = updateCurrencySymbol(symbol)

    fun setDefaultInfaqRate(rate: Double) = updateDefaultInfaqRate(rate)

    fun setAutoDeductInfaqEnabled(enabled: Boolean) = updateAutoDeductInfaq(enabled)

    fun setVaultMonthlyTarget(target: Double) = updateVaultMonthlyTarget(target)

    fun setSedekahSubuhTargetDays(days: Int) = updateSedekahSubuhTargetDays(days)

    fun setGoldPrice(
        price: Double,
        source: String = "Antam / Logam Mulia",
        timestampMillis: Long = System.currentTimeMillis()
    ) {
        val sanitized = CurrencyPrecisionUtils.roundRupiah(price.coerceAtLeast(100000.0))
        _uiState.update {
            it.copy(
                goldPricePerGram = sanitized,
                goldPriceLastUpdatedMillis = timestampMillis,
                goldPriceSource = source
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveGoldPriceWithMetadata(sanitized, source, timestampMillis)
        }
    }

    fun setHijriOffset(offset: Int) = updateHijriOffset(offset)

    fun setIsrafWarningThresholdPercent(percent: Int) = updateIsrafWarningThreshold(percent)

    fun setStrictBudgetEnforced(enforced: Boolean) = updateStrictBudgetEnforced(enforced)

    fun setAutoExecuteRecurringEnabled(enabled: Boolean) {
        _uiState.update { it.copy(autoExecuteRecurringEnabled = enabled) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveAutoExecuteRecurring(enabled)
        }
    }

    fun setNotifyOnRecurringDue(notify: Boolean) {
        _uiState.update { it.copy(notifyOnRecurringDue = notify) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveNotifyOnRecurringDue(notify)
        }
    }

    fun setShowDailyHadith(show: Boolean) {
        _uiState.update { it.copy(showDailyHadith = show) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveShowDailyHadith(show)
        }
    }

    fun setShowQuickTutorial(show: Boolean) {
        _uiState.update { it.copy(showQuickTutorial = show) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveShowQuickTutorial(show)
        }
    }

    // ==========================================
    // MULTI-WALLET (PENGELOLAAN KANTONG REKENING)
    // ==========================================

    fun addWallet(wallet: WalletAccount) {
        _uiState.update { it.copy(wallets = it.wallets + wallet) }
        persistWalletAsync(wallet)
        AppDebugLogger.i("AmanahWallet", "Menambahkan kantong rekening baru: '${wallet.name}' (${wallet.type})")
        appStateNotifier.notify(
            title = "Kantong Rekening Dibuat",
            message = "Kantong '${wallet.name}' berhasil ditambahkan ke daftar rekening.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun updateWallet(wallet: WalletAccount) {
        _uiState.update { state ->
            state.copy(wallets = state.wallets.map { if (it.id == wallet.id) wallet else it })
        }
        persistWalletAsync(wallet)
        AppDebugLogger.i("AmanahWallet", "Memperbarui info kantong: '${wallet.name}' (${wallet.type})")
        appStateNotifier.notify(
            title = "Kantong Rekening Diperbarui",
            message = "Data rekening '${wallet.name}' telah disesuaikan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun saveWalletWithBalance(wallet: WalletAccount, targetBalance: Double, isEdit: Boolean) {
        if (!isEdit) {
            addWallet(wallet)
            if (targetBalance > 0.0) {
                val cal = Calendar.getInstance()
                val hijri = HijriCalendarEngine.fromGregorian(cal.time)
                val journalId = "j_init_w_${UUID.randomUUID().toString().take(8)}"
                val entry = JournalEntry(
                    id = journalId,
                    gregorianDate = cal.time,
                    hijriYear = hijri.year,
                    hijriMonth = hijri.month,
                    hijriDay = hijri.day,
                    description = "Saldo Awal Kantong: ${wallet.name}",
                    transactionType = "INFLOW",
                    lines = listOf(
                        JournalLine(accountId = wallet.linkedAccountId, debit = targetBalance, credit = 0.0),
                        JournalLine(accountId = "acc_equity", debit = 0.0, credit = targetBalance)
                    )
                )
                _uiState.update { state ->
                    state.copy(journalEntries = listOf(entry) + state.journalEntries)
                }
                persistEntryAsync(entry)
                AppDebugLogger.i("AmanahWallet", "Mencatat saldo awal untuk kantong '${wallet.name}': Rp $targetBalance")
            }
        } else {
            val currentBal = _uiState.value.getWalletBalance(wallet.id)
            updateWallet(wallet)
            val diff = targetBalance - currentBal
            if (kotlin.math.abs(diff) >= 1.0) {
                val cal = Calendar.getInstance()
                val hijri = HijriCalendarEngine.fromGregorian(cal.time)
                val journalId = "j_adj_w_${UUID.randomUUID().toString().take(8)}"
                val lines = if (diff > 0.0) {
                    listOf(
                        JournalLine(accountId = wallet.linkedAccountId, debit = diff, credit = 0.0),
                        JournalLine(accountId = "acc_equity", debit = 0.0, credit = diff)
                    )
                } else {
                    val absDiff = kotlin.math.abs(diff)
                    listOf(
                        JournalLine(accountId = "acc_equity", debit = absDiff, credit = 0.0),
                        JournalLine(accountId = wallet.linkedAccountId, debit = 0.0, credit = absDiff)
                    )
                }
                val entry = JournalEntry(
                    id = journalId,
                    gregorianDate = cal.time,
                    hijriYear = hijri.year,
                    hijriMonth = hijri.month,
                    hijriDay = hijri.day,
                    description = "Penyesuaian Saldo Kantong: ${wallet.name} (${if (diff > 0) "+" else "-"}Rp ${kotlin.math.abs(diff).toLong()})",
                    transactionType = if (diff > 0) "INFLOW" else "EXPENSE",
                    lines = lines
                )
                _uiState.update { state ->
                    state.copy(journalEntries = listOf(entry) + state.journalEntries)
                }
                persistEntryAsync(entry)
                AppDebugLogger.i("AmanahWallet", "Menyesuaikan saldo kantong '${wallet.name}': diff = $diff")
            }
        }
    }

    fun addNewAssetAccount(name: String, code: String = "", description: String = ""): String {
        val sanitizedId = "acc_custom_" + UUID.randomUUID().toString().take(8)
        val generatedCode = if (code.isNotBlank()) code.trim() else "109.${System.currentTimeMillis() % 1000}"
        val newAccount = Account(
            id = sanitizedId,
            code = generatedCode,
            name = name.trim(),
            category = AccountCategory.ASSET,
            description = if (description.isNotBlank()) description.trim() else "Akun buku besar aset mandiri"
        )
        _uiState.update { state ->
            if (state.accounts.any { it.id == sanitizedId }) state
            else state.copy(accounts = state.accounts + newAccount)
        }
        persistCustomAccountsAsync()
        return sanitizedId
    }

    private fun persistCustomAccountsAsync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = roomDatabase ?: return@launch
                val customAccounts = _uiState.value.accounts.filter { it.id.startsWith("acc_custom_") }
                val jsonArr = org.json.JSONArray()
                for (acc in customAccounts) {
                    val obj = org.json.JSONObject()
                    obj.put("id", acc.id)
                    obj.put("code", acc.code)
                    obj.put("name", acc.name)
                    obj.put("description", acc.description)
                    jsonArr.put(obj)
                }
                db.settingsDao().insertOrUpdate(SettingsEntity("custom_asset_accounts", jsonArr.toString()))
            } catch (e: Exception) {
                AppDebugLogger.w("AmanahWallet", "Gagal menyimpan akun kustom: ${e.message}")
            }
        }
    }

    fun toggleExpenseCategoryVisibility(accountId: String) {
        val currentDisabled = _uiState.value.disabledExpenseAccountIds
        val isCurrentlyDisabled = accountId in currentDisabled
        val allExpense = _uiState.value.accounts.filter { it.category == AccountCategory.EXPENSE }

        if (!isCurrentlyDisabled) {
            val visibleCount = allExpense.count { it.id !in currentDisabled }
            if (visibleCount <= 1) {
                appStateNotifier.notify(
                    title = "Batas Minimal Kategori",
                    message = "Minimal harus ada 1 kategori pengeluaran yang aktif.",
                    severity = NotificationSeverity.WARNING
                )
                return
            }
            val newDisabled = currentDisabled + accountId
            _uiState.update { it.copy(disabledExpenseAccountIds = newDisabled) }
        } else {
            val newDisabled = currentDisabled - accountId
            _uiState.update { it.copy(disabledExpenseAccountIds = newDisabled) }
        }
        persistDisabledExpenseAccountsAsync()
    }

    fun setExpenseCategoryVisibility(accountId: String, isVisible: Boolean) {
        val currentDisabled = _uiState.value.disabledExpenseAccountIds
        val allExpense = _uiState.value.accounts.filter { it.category == AccountCategory.EXPENSE }
        if (!isVisible) {
            val visibleCount = allExpense.count { it.id !in currentDisabled }
            if (visibleCount <= 1) {
                appStateNotifier.notify(
                    title = "Batas Minimal Kategori",
                    message = "Minimal harus ada 1 kategori pengeluaran yang aktif.",
                    severity = NotificationSeverity.WARNING
                )
                return
            }
            _uiState.update { it.copy(disabledExpenseAccountIds = currentDisabled + accountId) }
        } else {
            _uiState.update { it.copy(disabledExpenseAccountIds = currentDisabled - accountId) }
        }
        persistDisabledExpenseAccountsAsync()
    }

    fun applyExpenseCategoryPreset(preset: com.example.core.accounting.ExpenseCategoryPreset) {
        val allExpense = _uiState.value.accounts.filter { it.category == AccountCategory.EXPENSE }
        val newDisabled = when (preset) {
            com.example.core.accounting.ExpenseCategoryPreset.MINIMAL -> {
                val keepVisible = setOf("acc_living", "acc_transport", "acc_utility", "acc_other_exp")
                allExpense.map { it.id }.filterNot { it in keepVisible || it.startsWith("acc_custom_exp_") }.toSet()
            }
            com.example.core.accounting.ExpenseCategoryPreset.FAMILY -> {
                val keepVisible = setOf("acc_living", "acc_transport", "acc_utility", "acc_education", "acc_health", "acc_other_exp")
                allExpense.map { it.id }.filterNot { it in keepVisible || it.startsWith("acc_custom_exp_") }.toSet()
            }
            com.example.core.accounting.ExpenseCategoryPreset.FULL -> {
                emptySet()
            }
        }
        _uiState.update { it.copy(disabledExpenseAccountIds = newDisabled) }
        persistDisabledExpenseAccountsAsync()
        appStateNotifier.notify(
            title = "Preset Kategori Diterapkan",
            message = "Preset ${preset.title} berhasil diaktifkan.",
            severity = NotificationSeverity.INFO
        )
    }

    fun addNewExpenseAccount(name: String, description: String = ""): String {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return ""
        val sanitizedId = "acc_custom_exp_" + UUID.randomUUID().toString().take(8)
        val generatedCode = "509.${System.currentTimeMillis() % 1000}"
        val newAccount = Account(
            id = sanitizedId,
            code = generatedCode,
            name = trimmed,
            category = AccountCategory.EXPENSE,
            description = if (description.isNotBlank()) description.trim() else "Kategori pengeluaran kustom mandiri",
            isCustom = true
        )
        _uiState.update { state ->
            if (state.accounts.any { it.id == sanitizedId }) state
            else state.copy(
                accounts = state.accounts + newAccount,
                disabledExpenseAccountIds = state.disabledExpenseAccountIds - sanitizedId
            )
        }
        persistCustomExpenseAccountsAsync()
        appStateNotifier.notify(
            title = "Kategori Baru Ditambahkan",
            message = "Kategori pengeluaran '$trimmed' siap digunakan.",
            severity = NotificationSeverity.INFO
        )
        return sanitizedId
    }

    fun deleteCustomExpenseCategory(accountId: String) {
        val target = _uiState.value.accounts.firstOrNull { it.id == accountId }
        if (target == null || (!target.isCustom && !target.id.startsWith("acc_custom_exp_"))) {
            return
        }
        _uiState.update { state ->
            state.copy(
                accounts = state.accounts.filterNot { it.id == accountId },
                disabledExpenseAccountIds = state.disabledExpenseAccountIds - accountId
            )
        }
        persistCustomExpenseAccountsAsync()
        persistDisabledExpenseAccountsAsync()
        appStateNotifier.notify(
            title = "Kategori Dihapus",
            message = "Kategori '${target.name}' telah dihapus.",
            severity = NotificationSeverity.INFO
        )
    }

    fun resetExpenseCategoriesToDefault() {
        _uiState.update { it.copy(disabledExpenseAccountIds = emptySet()) }
        persistDisabledExpenseAccountsAsync()
        appStateNotifier.notify(
            title = "Kategori Direset",
            message = "Seluruh kategori pengeluaran bawaan kembali ditampilkan.",
            severity = NotificationSeverity.INFO
        )
    }

    private fun persistDisabledExpenseAccountsAsync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = roomDatabase ?: return@launch
                val jsonArr = org.json.JSONArray()
                for (id in _uiState.value.disabledExpenseAccountIds) {
                    jsonArr.put(id)
                }
                db.settingsDao().insertOrUpdate(SettingsEntity("disabled_expense_account_ids", jsonArr.toString()))
            } catch (e: Exception) {
                AppDebugLogger.w("AmanahExpense", "Gagal menyimpan konfigurasi kategori: ${e.message}")
            }
        }
    }

    private fun persistCustomExpenseAccountsAsync() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = roomDatabase ?: return@launch
                val customExpense = _uiState.value.accounts.filter { it.category == AccountCategory.EXPENSE && (it.isCustom || it.id.startsWith("acc_custom_exp_")) }
                val jsonArr = org.json.JSONArray()
                for (acc in customExpense) {
                    val obj = org.json.JSONObject()
                    obj.put("id", acc.id)
                    obj.put("code", acc.code)
                    obj.put("name", acc.name)
                    obj.put("description", acc.description)
                    jsonArr.put(obj)
                }
                db.settingsDao().insertOrUpdate(SettingsEntity("custom_expense_accounts", jsonArr.toString()))
            } catch (e: Exception) {
                AppDebugLogger.w("AmanahExpense", "Gagal menyimpan kategori kustom: ${e.message}")
            }
        }
    }

    fun deleteWallet(walletId: String) {
        val target = _uiState.value.wallets.firstOrNull { it.id == walletId }
        _uiState.update { state ->
            state.copy(wallets = state.wallets.filterNot { it.id == walletId })
        }
        deleteWalletAsync(walletId)
        AppDebugLogger.w("AmanahWallet", "Menghapus kantong rekening: '${target?.name ?: walletId}'")
        appStateNotifier.notify(
            title = "Kantong Rekening Dihapus",
            message = "Kantong '${target?.name ?: walletId}' telah dihapus.",
            severity = NotificationSeverity.INFO
        )
    }

    fun transferBetweenWallets(
        fromWalletId: String,
        toWalletId: String,
        amount: Double,
        adminFee: Double = 0.0,
        note: String = "",
        date: Date = Date(),
        receiptAttachment: ReceiptAttachment? = null
    ) {
        val fromWallet = _uiState.value.getWallet(fromWalletId) ?: return
        val toWallet = _uiState.value.getWallet(toWalletId) ?: return

        val hijri = HijriCalendarEngine.fromGregorian(date)
        val journalId = UUID.randomUUID().toString()

        val lines = mutableListOf(
            // Debit Rekening Tujuan (Aset Bertambah)
            JournalLine(accountId = toWallet.linkedAccountId, debit = amount, credit = 0.0),
            // Kredit Rekening Asal (Aset Berkurang)
            JournalLine(accountId = fromWallet.linkedAccountId, debit = 0.0, credit = amount + adminFee)
        )

        // Biaya Admin Transfer jika ada
        if (adminFee > 0.0) {
            lines.add(JournalLine(accountId = "acc_other_exp", debit = adminFee, credit = 0.0))
        }

        val fullDesc = "Mutasi Antar-Kantong: ${fromWallet.name} ➔ ${toWallet.name}${if (note.isNotBlank()) " ($note)" else ""}"

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = fullDesc,
            transactionType = "TRANSFER",
            lines = lines,
            receiptAttachment = receiptAttachment
        )

        val mutation = WalletMutationRecord(
            id = UUID.randomUUID().toString(),
            fromWalletId = fromWalletId,
            toWalletId = toWalletId,
            amount = amount,
            adminFee = adminFee,
            note = note,
            timestampMillis = date.time,
            linkedJournalEntryId = journalId,
            receipt = receiptAttachment
        )

        _uiState.update { state ->
            val updatedMutations = listOf(mutation) + state.walletMutations
            persistWalletMutationsAsync(updatedMutations)
            state.copy(
                journalEntries = listOf(entry) + state.journalEntries,
                walletMutations = updatedMutations
            )
        }
        persistEntryAsync(entry)
        AppDebugLogger.i("AmanahWallet", "Mutasi Antar-Kantong: Rp ${amount.toLong()} dari ${fromWallet.name} ke ${toWallet.name} (Admin: Rp ${adminFee.toLong()})")

        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))
        appStateNotifier.notify(
            title = "Mutasi Saldo Berhasil",
            message = "Pindah dana Rp ${nf.format(amount)} dari ${fromWallet.name} ke ${toWallet.name} sukses tercatat.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    // ==========================================
    // IBADAH GOAL PLANNER (QURBAN, HAJI, UMRAH)
    // ==========================================

    fun addIbadahGoal(goal: IbadahGoal) {
        _uiState.update { it.copy(ibadahGoals = it.ibadahGoals + goal) }
        persistGoalAsync(goal)
        appStateNotifier.notify(
            title = "Rencana Ibadah Dibuat",
            message = "Target ibadah '${goal.title}' dengan target Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(goal.targetAmount)} dimulai. Bismillah!",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun updateIbadahGoal(goal: IbadahGoal) {
        _uiState.update { state ->
            state.copy(ibadahGoals = state.ibadahGoals.map { if (it.id == goal.id) goal else it })
        }
        persistGoalAsync(goal)
        appStateNotifier.notify(
            title = "Rencana Ibadah Diperbarui",
            message = "Target ibadah '${goal.title}' telah diperbarui.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun deleteIbadahGoal(goalId: String) {
        val target = _uiState.value.ibadahGoals.firstOrNull { it.id == goalId }
        _uiState.update { state ->
            state.copy(ibadahGoals = state.ibadahGoals.filterNot { it.id == goalId })
        }
        deleteGoalAsync(goalId)
        appStateNotifier.notify(
            title = "Rencana Ibadah Dihapus",
            message = "Target '${target?.title ?: goalId}' telah dihapus.",
            severity = NotificationSeverity.INFO
        )
    }

    fun depositToIbadahGoal(
        goalId: String,
        amount: Double,
        sourceWalletId: String,
        notes: String = "",
        receipt: ReceiptAttachment? = null
    ) {
        val goal = _uiState.value.ibadahGoals.firstOrNull { it.id == goalId } ?: return
        val cleanAmount = kotlin.math.round(amount)
        if (cleanAmount <= 0.0) return

        val sourceWallet = _uiState.value.getWallet(sourceWalletId)
        val fromAccountId = sourceWallet?.linkedAccountId
            ?: if (sourceWalletId.startsWith("acc_")) sourceWalletId else "acc_cash"

        val goalLinkedAccount = _uiState.value.getWallet(goal.linkedWalletId)?.linkedAccountId
            ?: if (goal.linkedWalletId.startsWith("acc_")) goal.linkedWalletId else "acc_bank_special"
        val destAccountId = if (goalLinkedAccount == fromAccountId) {
            if (fromAccountId == "acc_bank_special") "acc_other_asset" else "acc_bank_special"
        } else {
            goalLinkedAccount
        }

        val now = Date()
        val hijri = HijriCalendarEngine.fromGregorian(now)
        val transferEntry = JournalEntry(
            id = "j_ibadah_${UUID.randomUUID().toString().take(8)}",
            gregorianDate = now,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = "Setoran Tabungan Ibadah (${goal.title})${if (notes.isNotBlank()) " - $notes" else ""}",
            transactionType = "TRANSFER",
            lines = listOf(
                JournalLine(accountId = destAccountId, debit = cleanAmount, credit = 0.0),
                JournalLine(accountId = fromAccountId, debit = 0.0, credit = cleanAmount)
            ),
            receiptAttachment = receipt
        )

        val depositRecord = IbadahDepositRecord(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            amount = cleanAmount,
            sourceWalletId = sourceWalletId,
            fromWalletId = sourceWalletId,
            depositDate = now,
            dateMillis = now.time,
            notes = notes,
            note = notes,
            receipt = receipt
        )

        val newAccumulated = goal.currentAccumulated + cleanAmount
        val isNowCompleted = newAccumulated >= goal.targetAmount
        val updatedGoal = goal.copy(
            currentAccumulated = newAccumulated,
            isCompleted = isNowCompleted
        )

        lateinit var updatedDepositsToPersist: List<IbadahDepositRecord>
        _uiState.update { state ->
            val updatedDeposits = listOf(depositRecord) + state.ibadahDeposits
            updatedDepositsToPersist = updatedDeposits
            state.copy(
                ibadahGoals = state.ibadahGoals.map { if (it.id == goalId) updatedGoal else it },
                ibadahDeposits = updatedDeposits,
                journalEntries = listOf(transferEntry) + state.journalEntries
            )
        }
        persistGoalAsync(updatedGoal)
        persistEntryAsync(transferEntry)
        persistIbadahDepositsAsync(updatedDepositsToPersist)

        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))
        appStateNotifier.notify(
            title = if (isNowCompleted) "Alhamdulillah! Target Tercapai" else "Setoran Ibadah Tercatat",
            message = "Setoran Rp ${nf.format(cleanAmount)} untuk '${goal.title}' berhasil disimpan.${if (isNowCompleted) " Target tabungan ibadah telah genap 100%!" else ""}",
            severity = NotificationSeverity.SUCCESS
        )
    }

    // ==========================================
    // ZAKAT HUB (PROFESI, PERNIAGAAN, FITRAH)
    // ==========================================

    fun updateZakatProfesi(calc: ZakatProfesiCalculation) {
        _uiState.update { it.copy(zakatProfesi = calc) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveCustomSetting("zakat_hub_profesi", serializeZakatProfesi(calc))
        }
    }

    fun updateZakatPerniagaan(calc: ZakatPerniagaanCalculation) {
        _uiState.update { it.copy(zakatPerniagaan = calc) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveCustomSetting("zakat_hub_perniagaan", serializeZakatPerniagaan(calc))
        }
    }

    fun updateZakatFitrah(calc: ZakatFitrahFamilyCalculation) {
        _uiState.update { it.copy(zakatFitrah = calc) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveCustomSetting("zakat_hub_fitrah", serializeZakatFitrah(calc))
        }
    }

    fun addZakatFitrahDependent(name: String, relation: String = "Anggota Keluarga") {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val newDep = ZakatFitrahDependent(
            id = "dep_${UUID.randomUUID().toString().take(6)}",
            name = trimmed,
            relation = relation.ifBlank { "Anggota Keluarga" },
            isIncluded = true
        )
        val updatedDeps = _uiState.value.zakatFitrah.dependents + newDep
        val updatedCalc = _uiState.value.zakatFitrah.copy(
            dependents = updatedDeps,
            familyMembersCount = updatedDeps.count { it.isIncluded }
        )
        updateZakatFitrah(updatedCalc)
    }

    fun toggleZakatFitrahDependent(dependentId: String) {
        val updatedDeps = _uiState.value.zakatFitrah.dependents.map {
            if (it.id == dependentId) it.copy(isIncluded = !it.isIncluded) else it
        }
        val updatedCalc = _uiState.value.zakatFitrah.copy(
            dependents = updatedDeps,
            familyMembersCount = updatedDeps.count { it.isIncluded }
        )
        updateZakatFitrah(updatedCalc)
    }

    fun removeZakatFitrahDependent(dependentId: String) {
        val updatedDeps = _uiState.value.zakatFitrah.dependents.filterNot { it.id == dependentId }
        val updatedCalc = _uiState.value.zakatFitrah.copy(
            dependents = updatedDeps,
            familyMembersCount = updatedDeps.count { it.isIncluded }
        )
        updateZakatFitrah(updatedCalc)
    }

    fun updateZakatMaal(calc: ZakatMaalCalculation) {
        _uiState.update {
            it.copy(
                zakatMaal = calc,
                haulStartDate = if (calc.haulStartDateMillis > 0L) calc.haulStartDateMillis else it.haulStartDate
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveCustomSetting("zakat_hub_maal", serializeZakatMaal(calc))
        }
    }

    fun updateHaulStartDate(timestampMillis: Long, syncFulfilledFromDuration: Boolean = true) {
        val currentMaal = _uiState.value.zakatMaal
        val updatedBase = currentMaal.copy(haulStartDateMillis = timestampMillis)
        val fulfilled = if (syncFulfilledFromDuration && timestampMillis > 0L) {
            updatedBase.remainingHaulDays() == 0
        } else {
            updatedBase.isHaulFulfilled
        }
        val finalMaal = updatedBase.copy(isHaulFulfilled = fulfilled)
        _uiState.update {
            it.copy(
                haulStartDate = if (timestampMillis > 0L) timestampMillis else null,
                zakatMaal = finalMaal
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveHaulStartDateMillis(timestampMillis)
            repository?.saveCustomSetting("zakat_hub_maal", serializeZakatMaal(finalMaal))
            appContext?.getSharedPreferences("amanah_zakat_reminders", android.content.Context.MODE_PRIVATE)
                ?.edit()
                ?.apply {
                    if (timestampMillis > 0L) putLong("haul_start_date_ts", timestampMillis)
                    else remove("haul_start_date_ts")
                }
                ?.apply()
        }
    }

    fun updateInfaqVaultReminderConfig(config: InfaqVaultReminderConfig) {
        _uiState.update { it.copy(infaqVaultReminderConfig = config) }
        appContext?.let { ctx ->
            com.example.core.worker.InfaqVaultReminderWorker.scheduleOrUpdate(ctx, config)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveInfaqVaultReminderConfig(config)
        }
    }

    fun sendTestInfaqVaultNotification(
        context: android.content.Context,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val sent = com.example.core.worker.InfaqVaultReminderWorker.sendImmediateTestNotification(context.applicationContext)
            onComplete(sent)
        }
    }

    fun updateNotificationConfig(config: AmanahNotificationConfig) {
        lastScheduledNotificationConfig = config
        _uiState.update { it.copy(notificationConfig = config) }
        appContext?.let { ctx ->
            AmanahReminderWorker.scheduleFromConfig(ctx, config)
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveNotificationConfig(config)
        }
    }

    fun sendTestNotification(
        context: android.content.Context,
        category: NotificationCategoryType,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val sent = if (category == NotificationCategoryType.INFAQ_VAULT) {
                com.example.core.worker.InfaqVaultReminderWorker.sendImmediateTestNotification(context.applicationContext)
            } else {
                AmanahReminderWorker.sendImmediateTestNotification(context.applicationContext, category)
            }
            onComplete(sent)
        }
    }

    fun setAutoCloudBackupEnabled(enabled: Boolean) {
        _uiState.update { it.copy(autoCloudBackupEnabled = enabled) }
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveAutoCloudBackupEnabled(enabled)
        }
    }

    fun refreshCloudSnapshotSummary() {
        val se = syncEngine ?: return
        val currentAuth = _authState.value
        val userId = currentAuth.user.uid
        if (currentAuth.status != AuthStatus.AUTHENTICATED || userId.isBlank() || userId.startsWith("guest_")) {
            _uiState.update { it.copy(cloudSnapshotSummary = null, isLoadingCloudSummary = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCloudSummary = true) }
            val res = se.fetchCloudSnapshotSummary(userId)
            _uiState.update { st ->
                st.copy(
                    cloudSnapshotSummary = res.getOrNull() ?: st.cloudSnapshotSummary,
                    isLoadingCloudSummary = false
                )
            }
        }
    }

    /**
     * Menghitung dan menyinkronkan Zakat Maal secara otomatis berdasarkan total aset kas & rekening
     * serta total tabungan target ibadah yang tersimpan secara riil di Room Database.
     */
    fun syncZakatMaalFromDatabase() {
        val currentState = _uiState.value
        // Total saldo kas & rekening dari DB
        val totalWalletSavings = currentState.totalWalletBalance.coerceAtLeast(0.0)
        // Total akumulasi tabungan target ibadah dari DB
        val totalIbadahSavings = currentState.ibadahGoals.sumOf { it.currentAccumulated }.coerceAtLeast(0.0)

        // Piutang lancar dari Qardh (uang kita yang dipinjamkan ke pihak lain dan belum lunas)
        val receivables = currentState.qardhRecords
            .filter { it.type == QardhType.PIUTANG_SAYA && it.status != QardhStatus.LUNAS && it.status != QardhStatus.DIIKHLASKAN_SEDEKAH }
            .sumOf { it.remainingAmount }

        // Hutang jangka pendek dari Qardh (kewajiban hutang kita yang belum lunas)
        val shortTermDebt = currentState.qardhRecords
            .filter { it.type == QardhType.HUTANG_SAYA && it.status != QardhStatus.LUNAS && it.status != QardhStatus.DIIKHLASKAN_SEDEKAH }
            .sumOf { it.remainingAmount }

        val effectiveHaulStart = currentState.zakatMaal.haulStartDateMillis.takeIf { it > 0L }
            ?: currentState.haulStartDate
            ?: 0L

        val previousMaal = currentState.zakatMaal
        val updatedMaal = previousMaal.copy(
            totalWalletSavings = totalWalletSavings,
            totalIbadahSavings = totalIbadahSavings,
            tradeableReceivables = receivables,
            shortTermLiabilities = shortTermDebt,
            goldPricePerGram = currentState.goldPricePerGram,
            isLunarCalendar = currentState.shariahConfig.haulCalculationMethod != com.example.core.shariah.HaulCalculationMethod.MASEHI,
            haulStartDateMillis = effectiveHaulStart,
            lastSyncedFromDatabaseMillis = System.currentTimeMillis()
        )
        _uiState.update { it.copy(zakatMaal = updatedMaal) }
    }

    fun disburseZakat(
        title: String,
        amount: Double,
        asnafCategory: AsnafCategory,
        sourceAccountId: String,
        recipientName: String,
        notes: String,
        receipt: ReceiptAttachment? = null
    ) {
        val now = Date()
        val hijri = HijriCalendarEngine.fromGregorian(now)
        val journalId = UUID.randomUUID().toString()

        val jEntry = JournalEntry(
            id = journalId,
            gregorianDate = now,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = "Penyaluran Zakat: $title - $recipientName (${asnafCategory.displayName})",
            transactionType = "INFAQ_PAYOUT",
            lines = listOf(
                JournalLine(accountId = "acc_disbursed", debit = amount, credit = 0.0),
                JournalLine(accountId = sourceAccountId, debit = 0.0, credit = amount)
            ),
            receiptAttachment = receipt
        )

        val dist = InfaqDistributionRecord(
            id = UUID.randomUUID().toString(),
            amount = amount,
            recipientName = recipientName,
            asnafCategory = asnafCategory,
            distributionDate = now,
            hijriDateString = "${hijri.day} ${hijri.monthName} ${hijri.year} H",
            sourceAccountId = sourceAccountId,
            programName = title,
            receiptNumber = receipt?.referenceNumber ?: "ZAKAT-${System.currentTimeMillis() % 100000}",
            notes = notes,
            isVerified = true
        )

        _uiState.update { state ->
            state.copy(
                journalEntries = listOf(jEntry) + state.journalEntries,
                infaqDistributions = listOf(dist) + state.infaqDistributions
            )
        }
        persistEntryAsync(jEntry)
        viewModelScope.launch(Dispatchers.IO) {
            repository?.saveInfaqDistribution(dist)
        }

        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))
        appStateNotifier.notify(
            title = "Tunaikan Zakat Sukses",
            message = "Penyaluran zakat Rp ${nf.format(amount)} kepada $recipientName (${asnafCategory.displayName}) telah dibukukan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    // ==========================================
    // BACKUP & RESTORE TERENKRIPSI (LOCAL EXPORT/IMPORT)
    // ==========================================

    fun recordBackupCompleted(timestamp: Long = System.currentTimeMillis()) {
        _uiState.update { it.copy(lastBackupTimeMillis = timestamp) }
        viewModelScope.launch {
            repository?.saveLastBackupTime(timestamp)
        }
    }

    fun dismissBackupNudgePermanently() {
        _uiState.update { it.copy(isBackupNudgeDismissed = true) }
        viewModelScope.launch {
            repository?.saveBackupNudgeDismissed(true)
        }
    }

    fun setAppDisplayMode(mode: com.example.core.datastore.AppDisplayMode) {
        _uiState.update { it.copy(appDisplayMode = mode) }
        viewModelScope.launch {
            repository?.saveAppDisplayMode(mode)
        }
        val modeLabel = if (mode == com.example.core.datastore.AppDisplayMode.SIMPLE) "Mode Sederhana (Ramah Pemula)" else "Mode Lengkap Syariah"
        appStateNotifier.notify(
            title = "Mode Tampilan Diperbarui",
            message = "Beralih ke $modeLabel",
            severity = NotificationSeverity.INFO
        )
    }

    fun updateFeatureVisibilityConfig(config: com.example.core.feature.AppFeatureVisibilityConfig) {
        _uiState.update { it.copy(featureVisibilityConfig = config) }
        viewModelScope.launch {
            repository?.saveFeatureVisibilityConfig(config)
        }
    }

    fun applyFeaturePreset(preset: com.example.core.feature.FeaturePresetMode) {
        val config = com.example.core.feature.AppFeatureVisibilityConfig.createPreset(preset)
        val targetDisplayMode = when (preset) {
            com.example.core.feature.FeaturePresetMode.RINGKAS -> com.example.core.datastore.AppDisplayMode.SIMPLE
            com.example.core.feature.FeaturePresetMode.BISNIS,
            com.example.core.feature.FeaturePresetMode.LENGKAP -> com.example.core.datastore.AppDisplayMode.COMPREHENSIVE
        }
        _uiState.update {
            it.copy(
                featureVisibilityConfig = config,
                appDisplayMode = targetDisplayMode
            )
        }
        viewModelScope.launch {
            repository?.saveFeatureVisibilityConfig(config)
            repository?.saveAppDisplayMode(targetDisplayMode)
        }
        val modeLabel = if (targetDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE) "Mode Ringkas" else "Mode Lengkap Syariah"
        appStateNotifier.notify(
            title = "Preset Fitur Diterapkan",
            message = "Profil ${preset.title} ($modeLabel, ${config.totalActiveFeatures} fitur aktif)",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun updatePdfEditorialConfig(config: PdfEditorialConfig, showNotification: Boolean = true) {
        _uiState.update { it.copy(pdfEditorialConfig = config) }
        viewModelScope.launch {
            repository?.savePdfEditorialConfig(config)
        }
        if (showNotification) {
            appStateNotifier.notify(
                title = "Redaksi PDF Disimpan",
                message = "Gaya ${config.stylePreset.title} (${config.activeSectionCount()} bagian aktif) diterapkan.",
                severity = NotificationSeverity.SUCCESS
            )
        }
    }

    fun applyPdfEditorialPreset(style: PdfEditorialStyle) {
        val current = _uiState.value.pdfEditorialConfig
        val next = PdfEditorialConfig.forPreset(style, keepOwnerAndClosingNoteFrom = current)
        updatePdfEditorialConfig(next)
    }

    fun toggleAppDisplayMode() {
        val current = _uiState.value.appDisplayMode
        val nextMode = if (current == com.example.core.datastore.AppDisplayMode.SIMPLE) {
            com.example.core.datastore.AppDisplayMode.COMPREHENSIVE
        } else {
            com.example.core.datastore.AppDisplayMode.SIMPLE
        }
        setAppDisplayMode(nextMode)
    }

    private fun buildPreferencesSnapshot(): BackupPreferencesSnapshot {
        val state = _uiState.value
        return BackupPreferencesSnapshot(
            userNameKasMukmin = state.userNameKasMukmin,
            primaryCurrencySymbol = state.primaryCurrencySymbol,
            goldPricePerGram = state.goldPricePerGram,
            selectedHijriOffset = state.selectedHijriOffset,
            startDayOfMonth = state.startDayOfMonth,
            initialLedgerDate = state.initialLedgerDate,
            fiscalCycleType = state.fiscalCycleType.name,
            initialLedgerBalance = state.initialLedgerBalance,
            fiscalYearStartMonth = state.fiscalYearStartMonth,
            defaultInfaqRate = state.defaultInfaqRate,
            isAutoDeductInfaqEnabled = state.isAutoDeductInfaqEnabled,
            vaultMonthlyTarget = state.vaultMonthlyTarget,
            sedekahSubuhTargetDays = state.sedekahSubuhTargetDays,
            israfWarningThresholdPercent = state.israfWarningThresholdPercent,
            isStrictBudgetEnforced = state.isStrictBudgetEnforced,
            isDeficitProtectionEnabled = state.isDeficitProtectionEnabled,
            autoExecuteRecurringEnabled = state.autoExecuteRecurringEnabled,
            notifyOnRecurringDue = state.notifyOnRecurringDue,
            showDailyHadith = state.showDailyHadith,
            showQuickTutorial = state.showQuickTutorial,
            selectedGoalMode = state.selectedGoalMode.name,
            themeMode = state.themeMode.name,
            uiScaleMode = state.uiScaleMode.name,
            uiScaleFactor = state.uiScaleFactor,
            appDisplayMode = state.appDisplayMode.name,
            haulStartDateMillis = state.haulStartDate ?: state.zakatMaal.haulStartDateMillis,
            isShakeFeedbackEnabled = state.isShakeFeedbackEnabled,
            autoCloudBackupEnabled = state.autoCloudBackupEnabled,
            notificationConfigJson = state.notificationConfig.toJson(),
            infaqVaultReminderConfigJson = BackupPreferencesSnapshot.serializeInfaqVaultReminder(state.infaqVaultReminderConfig),
            shariahConfigJson = BackupPreferencesSnapshot.serializeShariahConfig(state.shariahConfig),
            featureVisibilityConfigJson = state.featureVisibilityConfig.toJson(),
            pdfEditorialConfigJson = state.pdfEditorialConfig.toJson(),
            disabledExpenseAccountIds = state.disabledExpenseAccountIds,
            purifiedAuditEntryIds = state.purifiedAuditEntryIds
        )
    }

    private fun applyPreferencesSnapshot(snapshot: BackupPreferencesSnapshot) {
        val restoredThemeMode = try {
            com.example.ui.theme.AppThemeMode.valueOf(snapshot.themeMode)
        } catch (_: Exception) { _uiState.value.themeMode }
        val (isDark, isHighContrast) = when (restoredThemeMode) {
            com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM -> _uiState.value.isDarkMode to false
            com.example.ui.theme.AppThemeMode.TACTICAL_CYBER -> true to false
            com.example.ui.theme.AppThemeMode.TACTICAL_LIGHT -> false to false
            com.example.ui.theme.AppThemeMode.ELEGANT_DARK -> true to false
            com.example.ui.theme.AppThemeMode.LIGHT_MODE -> false to false
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT -> false to true
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK -> true to true
        }
        val restoredScaleMode = try {
            com.example.ui.theme.UiScaleMode.valueOf(snapshot.uiScaleMode)
        } catch (_: Exception) { _uiState.value.uiScaleMode }
        val restoredScaleFactor = snapshot.uiScaleFactor.coerceIn(0.75f, 1.50f)
        val restoredDisplayMode = try {
            com.example.core.datastore.AppDisplayMode.valueOf(snapshot.appDisplayMode)
        } catch (_: Exception) { _uiState.value.appDisplayMode }
        val restoredFiscalCycle = try {
            com.example.core.accounting.FiscalCycleType.valueOf(snapshot.fiscalCycleType)
        } catch (_: Exception) { _uiState.value.fiscalCycleType }
        val restoredGoalMode = try {
            FinancialGoalMode.valueOf(snapshot.selectedGoalMode)
        } catch (_: Exception) { _uiState.value.selectedGoalMode }
        val restoredNotificationConfig = AmanahNotificationConfig.fromJson(snapshot.notificationConfigJson)
        val restoredInfaqVaultConfig = BackupPreferencesSnapshot.deserializeInfaqVaultReminder(snapshot.infaqVaultReminderConfigJson)
        val restoredShariahConfig = BackupPreferencesSnapshot.deserializeShariahConfig(snapshot.shariahConfigJson)
        val restoredFeatureVisibility = com.example.core.feature.AppFeatureVisibilityConfig.fromJson(snapshot.featureVisibilityConfigJson)
        val restoredPdfConfig = PdfEditorialConfig.fromJson(snapshot.pdfEditorialConfigJson)

        lastScheduledNotificationConfig = restoredNotificationConfig
        lastScheduledInfaqConfig = restoredInfaqVaultConfig

        _uiState.update { st ->
            st.copy(
                userNameKasMukmin = snapshot.userNameKasMukmin,
                primaryCurrencySymbol = snapshot.primaryCurrencySymbol,
                goldPricePerGram = snapshot.goldPricePerGram,
                selectedHijriOffset = snapshot.selectedHijriOffset.coerceIn(-3, 3),
                startDayOfMonth = snapshot.startDayOfMonth.coerceIn(1, 28),
                initialLedgerDate = snapshot.initialLedgerDate,
                fiscalCycleType = restoredFiscalCycle,
                initialLedgerBalance = snapshot.initialLedgerBalance,
                fiscalYearStartMonth = snapshot.fiscalYearStartMonth.coerceIn(1, 12),
                defaultInfaqRate = snapshot.defaultInfaqRate.coerceIn(0.01, 0.50),
                isAutoDeductInfaqEnabled = snapshot.isAutoDeductInfaqEnabled,
                vaultMonthlyTarget = snapshot.vaultMonthlyTarget.coerceAtLeast(0.0),
                sedekahSubuhTargetDays = snapshot.sedekahSubuhTargetDays.coerceIn(7, 365),
                israfWarningThresholdPercent = snapshot.israfWarningThresholdPercent.coerceIn(0, 100),
                isStrictBudgetEnforced = snapshot.isStrictBudgetEnforced,
                isDeficitProtectionEnabled = snapshot.isDeficitProtectionEnabled,
                autoExecuteRecurringEnabled = snapshot.autoExecuteRecurringEnabled,
                notifyOnRecurringDue = snapshot.notifyOnRecurringDue,
                showDailyHadith = snapshot.showDailyHadith,
                showQuickTutorial = snapshot.showQuickTutorial,
                selectedGoalMode = restoredGoalMode,
                themeMode = restoredThemeMode,
                isDarkMode = isDark,
                isHighContrast = isHighContrast,
                uiScaleMode = restoredScaleMode,
                uiScaleFactor = restoredScaleFactor,
                appDisplayMode = restoredDisplayMode,
                haulStartDate = snapshot.haulStartDateMillis.takeIf { it > 0L },
                zakatMaal = if (snapshot.haulStartDateMillis > 0L) {
                    st.zakatMaal.copy(haulStartDateMillis = snapshot.haulStartDateMillis)
                } else st.zakatMaal,
                isShakeFeedbackEnabled = snapshot.isShakeFeedbackEnabled,
                autoCloudBackupEnabled = snapshot.autoCloudBackupEnabled,
                notificationConfig = restoredNotificationConfig,
                infaqVaultReminderConfig = restoredInfaqVaultConfig,
                shariahConfig = restoredShariahConfig,
                featureVisibilityConfig = restoredFeatureVisibility,
                pdfEditorialConfig = restoredPdfConfig,
                disabledExpenseAccountIds = snapshot.disabledExpenseAccountIds,
                purifiedAuditEntryIds = snapshot.purifiedAuditEntryIds
            )
        }

        appContext?.let { ctx ->
            AmanahReminderWorker.scheduleFromConfig(ctx, restoredNotificationConfig)
            com.example.core.worker.InfaqVaultReminderWorker.scheduleOrUpdate(ctx, restoredInfaqVaultConfig)
            if (snapshot.haulStartDateMillis > 0L) {
                ctx.getSharedPreferences("amanah_zakat_reminders", android.content.Context.MODE_PRIVATE)
                    .edit()
                    .putLong("haul_start_date_ts", snapshot.haulStartDateMillis)
                    .apply()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            val repo = repository ?: return@launch
            repo.saveUserName(snapshot.userNameKasMukmin)
            repo.saveCurrencySymbol(snapshot.primaryCurrencySymbol)
            repo.saveGoldPrice(snapshot.goldPricePerGram)
            repo.saveHijriOffset(snapshot.selectedHijriOffset.coerceIn(-3, 3))
            repo.saveStartDayOfMonth(snapshot.startDayOfMonth.coerceIn(1, 28))
            repo.saveInitialLedgerDate(snapshot.initialLedgerDate)
            repo.saveFiscalCycleType(restoredFiscalCycle)
            repo.saveInitialLedgerBalance(snapshot.initialLedgerBalance)
            repo.saveFiscalYearStartMonth(snapshot.fiscalYearStartMonth.coerceIn(1, 12))
            repo.saveDefaultInfaqRate(snapshot.defaultInfaqRate.coerceIn(0.01, 0.50))
            repo.saveAutoDeductInfaq(snapshot.isAutoDeductInfaqEnabled)
            repo.saveVaultMonthlyTarget(snapshot.vaultMonthlyTarget.coerceAtLeast(0.0))
            repo.saveSedekahSubuhTargetDays(snapshot.sedekahSubuhTargetDays.coerceIn(7, 365))
            repo.saveIsrafWarningThreshold(snapshot.israfWarningThresholdPercent.coerceIn(0, 100))
            repo.saveStrictBudgetEnforced(snapshot.isStrictBudgetEnforced)
            repo.saveDeficitProtectionEnabled(snapshot.isDeficitProtectionEnabled)
            repo.saveAutoExecuteRecurring(snapshot.autoExecuteRecurringEnabled)
            repo.saveNotifyOnRecurringDue(snapshot.notifyOnRecurringDue)
            repo.saveShowDailyHadith(snapshot.showDailyHadith)
            repo.saveShowQuickTutorial(snapshot.showQuickTutorial)
            repo.saveSelectedGoalMode(restoredGoalMode)
            repo.saveThemeMode(restoredThemeMode)
            repo.saveUiScale(restoredScaleMode, restoredScaleFactor)
            repo.saveAppDisplayMode(restoredDisplayMode)
            if (snapshot.haulStartDateMillis > 0L) {
                repo.saveHaulStartDateMillis(snapshot.haulStartDateMillis)
            }
            repo.saveShakeFeedbackEnabled(snapshot.isShakeFeedbackEnabled)
            repo.saveAutoCloudBackupEnabled(snapshot.autoCloudBackupEnabled)
            repo.saveNotificationConfig(restoredNotificationConfig)
            repo.saveInfaqVaultReminderConfig(restoredInfaqVaultConfig)
            repo.saveShariahConfig(restoredShariahConfig)
            repo.saveFeatureVisibilityConfig(restoredFeatureVisibility)
            repo.savePdfEditorialConfig(restoredPdfConfig)
            repo.saveCustomSetting(
                "disabled_expense_account_ids",
                org.json.JSONArray(snapshot.disabledExpenseAccountIds.toList()).toString()
            )
            snapshot.purifiedAuditEntryIds.forEach { entryId ->
                repo.markAuditEntryPurified(entryId)
            }
        }
    }

    fun generateBackupPackage(password: String = ""): String {
        recordBackupCompleted()
        val state = _uiState.value
        AppDebugLogger.i("AmanahBackup", "Membuat paket pencadangan terenkripsi (${state.journalEntries.size} transaksi jurnal, ${state.wallets.size} kantong)")
        return BackupEngine.createBackupPackage(
            journalEntries = state.journalEntries,
            wallets = state.wallets,
            goals = state.ibadahGoals,
            budgets = state.budgets,
            rules = state.rules,
            recurring = state.recurringTransactions,
            sedekahSubuhState = state.sedekahSubuhState,
            distributions = state.infaqDistributions,
            qardhRecords = state.qardhRecords,
            ibadahDeposits = state.ibadahDeposits,
            rezekiNonTunai = state.rezekiNonTunaiList,
            walletMutations = state.walletMutations,
            preferences = buildPreferencesSnapshot(),
            password = password
        )
    }

    fun restoreBackupPackage(backupPackage: String, password: String = ""): RestoreResult {
        val result = BackupEngine.parseAndRestore(backupPackage, password)
        if (result is RestoreResult.Success) {
            recordBackupCompleted()
            _uiState.update { state ->
                state.copy(
                    journalEntries = result.restoredEntries,
                    wallets = if (result.restoredWallets.isNotEmpty()) result.restoredWallets else state.wallets,
                    ibadahGoals = if (result.restoredGoals.isNotEmpty()) result.restoredGoals else state.ibadahGoals,
                    budgets = if (result.restoredBudgets.isNotEmpty()) result.restoredBudgets else state.budgets,
                    rules = if (result.restoredRules.isNotEmpty()) result.restoredRules else state.rules,
                    recurringTransactions = if (result.restoredRecurring.isNotEmpty()) result.restoredRecurring else state.recurringTransactions,
                    infaqDistributions = if (result.restoredDistributions.isNotEmpty()) result.restoredDistributions else state.infaqDistributions,
                    qardhRecords = if (result.restoredQardh.isNotEmpty()) result.restoredQardh else state.qardhRecords,
                    ibadahDeposits = if (result.restoredIbadahDeposits.isNotEmpty()) result.restoredIbadahDeposits else state.ibadahDeposits,
                    rezekiNonTunaiList = if (result.restoredRezekiNonTunai.isNotEmpty()) result.restoredRezekiNonTunai else state.rezekiNonTunaiList,
                    walletMutations = if (result.restoredWalletMutations.isNotEmpty()) result.restoredWalletMutations else state.walletMutations,
                    sedekahSubuhState = result.restoredSedekahState
                )
            }
            result.restoredPreferences?.let { applyPreferencesSnapshot(it) }
            syncZakatMaalFromDatabase()
            viewModelScope.launch(Dispatchers.IO) {
                if (result.restoredEntries.isNotEmpty()) repository?.saveAllTransactions(result.restoredEntries)
                if (result.restoredWallets.isNotEmpty()) repository?.saveAllWallets(result.restoredWallets)
                if (result.restoredGoals.isNotEmpty()) repository?.saveAllGoals(result.restoredGoals)
                if (result.restoredBudgets.isNotEmpty()) repository?.saveAllBudgets(result.restoredBudgets)
                if (result.restoredRules.isNotEmpty()) repository?.saveAllInfaqRules(result.restoredRules)
                if (result.restoredRecurring.isNotEmpty()) repository?.saveAllRecurringTransactions(result.restoredRecurring)
                if (result.restoredDistributions.isNotEmpty()) repository?.saveAllInfaqDistributions(result.restoredDistributions)
                if (result.restoredQardh.isNotEmpty()) repository?.saveAllQardh(result.restoredQardh)
                if (result.restoredIbadahDeposits.isNotEmpty()) persistIbadahDepositsAsync(result.restoredIbadahDeposits)
                if (result.restoredWalletMutations.isNotEmpty()) persistWalletMutationsAsync(result.restoredWalletMutations)
                if (result.restoredRezekiNonTunai.isNotEmpty()) {
                    roomDatabase?.rezekiNonTunaiDao()?.insertAll(
                        result.restoredRezekiNonTunai.map { EntityMappers.toEntity(it) }
                    )
                }
                repository?.saveSedekahSubuh(result.restoredSedekahState)
                repository?.saveCustomSetting("dummy_data_version", "v4_multi_wallet_isolated")
            }
            AppDebugLogger.i("AmanahBackup", "Pemulihan data sukses: ${result.restoredEntries.size} jurnal, ${result.restoredWallets.size} kantong")
            appStateNotifier.notify(
                title = "Pemulihan Data Sukses",
                message = "Berhasil memulihkan ${result.restoredEntries.size} transaksi buku kas & ${result.restoredWallets.size} kantong rekening.",
                severity = NotificationSeverity.SUCCESS
            )
        } else if (result is RestoreResult.Failure) {
            AppDebugLogger.logHandledError("AmanahBackup", "Gagal memulihkan cadangan: ${result.errorMessage}")
            appStateNotifier.notify(
                title = "Gagal Memulihkan Data",
                message = result.errorMessage,
                severity = NotificationSeverity.WARNING
            )
        }
        return result
    }

    /**
     * Menghapus seluruh data dummy / contoh dari memori dan Room Database
     * sehingga pengguna mendapatkan buku kas bersih untuk pencatatan pribadi.
     */
    fun clearAllDummyData() {
        viewModelScope.launch {
            try {
                val db = roomDatabase
                if (db != null) {
                    db.settingsDao().insertOrUpdate(SettingsEntity("user_has_cleared_dummy_data", "true"))
                    db.journalDao().clearAll()
                    db.budgetDao().clearAll()
                    db.walletDao().clearAll()
                    db.ibadahGoalDao().clearAll()
                    db.qardhDao().clearAll()
                    db.sedekahSubuhDao().clearAll()
                    db.recurringTransactionDao().clearAll()
                    db.infaqDistributionDao().clearAll()
                    db.settingsDao().deleteByKey("wallet_mutations_json")
                    db.settingsDao().deleteByKey("ibadah_deposits_json")
                    db.settingsDao().deleteByKey("sedekah_subuh_history_json")
                }
                repository?.clearAllTransactions()
                repository?.clearAllBudgets()
                repository?.clearAllWallets()
                repository?.clearAllGoals()
                repository?.clearAllQardh()
                repository?.clearSedekahSubuh()
                repository?.clearAllRecurringTransactions()
                repository?.clearAllInfaqDistributions()
                repository?.saveCustomSetting("user_has_cleared_dummy_data", "true")
                AppDebugLogger.w("AmanahDatabase", "Pengguna mengosongkan seluruh data dummy dari Room DB & memori.")
            } catch (e: Exception) {
                android.util.Log.e("AmanahVM", "Error clearing dummy data: ${e.message}")
                AppDebugLogger.logHandledError("AmanahDatabase", "Gagal menghapus data dummy dari Room DB: ${e.message}", e)
            }

            _uiState.update { current ->
                current.copy(
                    journalEntries = emptyList(),
                    wallets = emptyList(),
                    walletMutations = emptyList(),
                    budgets = emptyList(),
                    ibadahGoals = emptyList(),
                    ibadahDeposits = emptyList(),
                    qardhRecords = emptyList(),
                    recurringTransactions = emptyList(),
                    infaqDistributions = emptyList(),
                    sedekahSubuhState = SedekahSubuhStreakEngine.calculateStreak(emptyMap())
                )
            }

            appStateNotifier.notify(
                title = "Data Dummy Dihapus",
                message = "Seluruh data contoh telah dihapus. Buku kas Anda kini bersih dan siap untuk pencatatan riil.",
                severity = NotificationSeverity.SUCCESS
            )
        }
    }

    /**
     * Memuat ulang data dummy ke dalam memori dan Room Database
     * untuk memberikan gambaran lengkap seluruh fitur syariah bagi pengguna baru.
     */
    fun loadDummyData() {
        val dummyEntries = AmanahDummyDataGenerator.getSampleJournalEntries()
        val dummyWallets = AmanahDummyDataGenerator.getSampleWallets()
        val dummyBudgets = AmanahDummyDataGenerator.getSampleBudgets()
        val dummyGoals = AmanahDummyDataGenerator.getSampleIbadahGoals()
        val dummyQardh = AmanahDummyDataGenerator.getSampleQardhRecords()
        val dummyRecurring = AmanahDummyDataGenerator.getSampleRecurringTransactions()
        val dummySedekah = AmanahDummyDataGenerator.getSampleSedekahSubuhState()
        val dummyDistributions = AmanahDummyDataGenerator.getSampleInfaqDistributions()
        val dummyZakatProfesi = AmanahDummyDataGenerator.getSampleZakatProfesi()
        val dummyZakatPerniagaan = AmanahDummyDataGenerator.getSampleZakatPerniagaan()
        val dummyZakatFitrah = AmanahDummyDataGenerator.getSampleZakatFitrah()
        val dummyZakatMaal = AmanahDummyDataGenerator.getSampleZakatMaal()

        _uiState.update { current ->
            current.copy(
                journalEntries = dummyEntries,
                wallets = dummyWallets,
                budgets = dummyBudgets,
                ibadahGoals = dummyGoals,
                qardhRecords = dummyQardh,
                recurringTransactions = dummyRecurring,
                sedekahSubuhState = dummySedekah,
                infaqDistributions = dummyDistributions,
                zakatProfesi = dummyZakatProfesi,
                zakatPerniagaan = dummyZakatPerniagaan,
                zakatFitrah = dummyZakatFitrah,
                zakatMaal = dummyZakatMaal
            )
        }

        viewModelScope.launch {
            try {
                val db = roomDatabase
                if (db != null) {
                    db.settingsDao().insertOrUpdate(SettingsEntity("user_has_cleared_dummy_data", "false"))
                    seedDummyDataToRoom(db)
                }
                repository?.saveCustomSetting("user_has_cleared_dummy_data", "false")
                AppDebugLogger.i("AmanahDatabase", "Pengguna memuat ulang dataset simulasi / contoh ke Room DB.")
            } catch (e: Exception) {
                android.util.Log.e("AmanahVM", "Error saving dummy data to Room: ${e.message}")
                AppDebugLogger.logHandledError("AmanahDatabase", "Gagal memuat dataset simulasi ke Room DB: ${e.message}", e)
            }

            appStateNotifier.notify(
                title = "Data Contoh Dimuat",
                message = "Data simulasi transaksi, anggaran, target ibadah, dan qardh berhasil dimuat.",
                severity = NotificationSeverity.SUCCESS
            )
        }
    }

    fun resetAllDataToDefault() {
        clearAllDummyData()
    }

    fun addQardhRecord(record: QardhRecord) {
        _uiState.update { current ->
            current.copy(qardhRecords = listOf(record) + current.qardhRecords)
        }
        persistQardhAsync(record)
        appStateNotifier.notify(
            title = "Akad Qardh Tercatat",
            message = "Pencatatan ${record.type.badge} sebesar Rp ${record.totalAmount.toLong()} bersama ${record.counterpartyName} berhasil disimpan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun updateQardhRecord(record: QardhRecord) {
        _uiState.update { current ->
            current.copy(qardhRecords = current.qardhRecords.map { if (it.id == record.id) record else it })
        }
        persistQardhAsync(record)
        appStateNotifier.notify(
            title = "Catatan Akad Diperbarui",
            message = "Perubahan akad bersama ${record.counterpartyName} berhasil disimpan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun recordQardhInstallment(
        qardhId: String,
        amount: Double,
        walletId: String,
        note: String,
        receipt: ReceiptAttachment?
    ) {
        val targetQardh = _uiState.value.qardhRecords.firstOrNull { it.id == qardhId }
        if (targetQardh == null) {
            appStateNotifier.notify(
                title = "Akad Tidak Ditemukan",
                message = "Data akad Qardh tidak ditemukan.",
                severity = NotificationSeverity.ALERT
            )
            return
        }

        val cleanAmount = kotlin.math.round(amount)
        if (cleanAmount <= 0.0) {
            appStateNotifier.notify(
                title = "Nominal Tidak Valid",
                message = "Nominal cicilan harus lebih besar dari Rp 0.",
                severity = NotificationSeverity.WARNING
            )
            return
        }

        if (cleanAmount > targetQardh.remainingAmount) {
            appStateNotifier.notify(
                title = "Nominal Melebihi Sisa Kewajiban",
                message = "Nominal pelunasan (Rp ${cleanAmount.toLong()}) melebihi sisa kewajiban akad (Rp ${targetQardh.remainingAmount.toLong()}).",
                severity = NotificationSeverity.WARNING
            )
            return
        }

        val realWallet = _uiState.value.getWallet(walletId)
        val resolvedAssetAccountId = realWallet?.linkedAccountId ?: if (walletId.startsWith("acc_")) walletId else "acc_cash"

        val now = Date()
        val hijri = HijriCalendarEngine.fromGregorian(now)
        val journalId = UUID.randomUUID().toString()

        val isPiutang = targetQardh.type == QardhType.PIUTANG_SAYA
        // If PIUTANG_DIBERIKAN: Debitur bayar ke pengguna -> Uang masuk ke kas/bank (Debit Kas/Bank), Piutang berkurang (Kredit Piutang)
        // If HUTANG_DITERIMA: Pengguna bayar utang ke kreditur -> Utang berkurang (Debit Hutang), Uang keluar dari kas/bank (Kredit Kas/Bank)
        val lines = if (isPiutang) {
            listOf(
                JournalLine(accountId = resolvedAssetAccountId, debit = cleanAmount, credit = 0.0),
                JournalLine(accountId = "acc_receivable", debit = 0.0, credit = cleanAmount)
            )
        } else {
            listOf(
                JournalLine(accountId = "acc_debt", debit = cleanAmount, credit = 0.0),
                JournalLine(accountId = resolvedAssetAccountId, debit = 0.0, credit = cleanAmount)
            )
        }

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = now,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = "${if (isPiutang) "Pelunasan Piutang" else "Cicilan Utang"}: ${targetQardh.counterpartyName} - $note",
            transactionType = if (isPiutang) "INCOME" else "EXPENSE",
            lines = lines
        )

        _uiState.update { current ->
            val updatedList = current.qardhRecords.map { q ->
                if (q.id == qardhId) {
                    val installment = QardhInstallment(
                        qardhId = qardhId,
                        amount = cleanAmount,
                        fromWalletId = walletId,
                        note = note,
                        receipt = receipt,
                        linkedJournalEntryId = journalId
                    )
                    val newRemaining = (q.remainingAmount - cleanAmount).coerceAtLeast(0.0)
                    val newStatus = if (newRemaining <= 0.0) QardhStatus.LUNAS else QardhStatus.SEBAGIAN_LUNAS
                    q.copy(
                        remainingAmount = newRemaining,
                        status = newStatus,
                        installments = q.installments + installment
                    )
                } else {
                    q
                }
            }
            current.copy(
                qardhRecords = updatedList,
                journalEntries = listOf(entry) + current.journalEntries
            )
        }
        val updatedQ = _uiState.value.qardhRecords.firstOrNull { it.id == qardhId }
        if (updatedQ != null) persistQardhAsync(updatedQ)
        persistEntryAsync(entry)

        appStateNotifier.notify(
            title = "Pembayaran Cicilan Qardh",
            message = "Pembayaran cicilan Rp ${cleanAmount.toLong()} (${targetQardh.counterpartyName}) berhasil dibukukan ke jurnal & dompet.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun forgiveQardhAsSedekah(qardhId: String) {
        _uiState.update { current ->
            val target = current.qardhRecords.firstOrNull { it.id == qardhId }
            if (target != null && target.remainingAmount > 0) {
                val updatedList = current.qardhRecords.map {
                    if (it.id == qardhId) it.copy(remainingAmount = 0.0, status = QardhStatus.DIIKHLASKAN_SEDEKAH)
                    else it
                }
                current.copy(qardhRecords = updatedList)
            } else current
        }
        val updatedQ = _uiState.value.qardhRecords.firstOrNull { it.id == qardhId }
        if (updatedQ != null) persistQardhAsync(updatedQ)

        appStateNotifier.notify(
            title = "Alhamdulillah (Amal Sedekah)",
            message = "Piutang telah diikhlaskan menjadi sedekah jariyah. Semoga Allah melipatgandakan pahala kebaikan Anda.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun deleteQardhRecord(qardhId: String) {
        _uiState.update { current ->
            current.copy(qardhRecords = current.qardhRecords.filterNot { it.id == qardhId })
        }
        deleteQardhAsync(qardhId)
    }

    fun generateCsvReport(): String {
        val state = _uiState.value
        return DocumentExporter.generateCsvReport(
            entries = state.journalEntries,
            wallets = state.wallets,
            accounts = state.accounts,
            qardhRecords = state.qardhRecords,
            ibadahGoals = state.ibadahGoals,
            getWalletBalance = { state.getWalletBalance(it) }
        )
    }

    fun generatePdfReport(context: android.content.Context): java.io.File {
        val state = _uiState.value
        return DocumentExporter.generatePdfReportFile(
            context = context,
            entries = state.journalEntries,
            wallets = state.wallets,
            getWalletBalance = { state.getWalletBalance(it) },
            totalInfaqDisbursed = state.totalDisbursedInfaq,
            totalIncome = state.totalIncomeKasab,
            totalExpense = state.totalConsumptionExpense
        )
    }

    fun exportLedgerSummaryText(): String {
        val state = _uiState.value
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val nowStr = sdf.format(Date())
        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))
        return buildString {
            appendLine("══════════════════════════════════════════════")
            appendLine("       RINGKASAN BUKU BESAR AMANAH LEDGER      ")
            appendLine("══════════════════════════════════════════════")
            appendLine("Nama Akun Kas : ${state.userNameKasMukmin}")
            appendLine("Waktu Ekspor  : $nowStr")
            appendLine("Standar Mata Uang: ${state.primaryCurrencySymbol} (IDR)")
            appendLine("──────────────────────────────────────────────")
            appendLine("Total Saldo Multi-Kantong : Rp ${nf.format(state.totalWalletBalance)}")
            appendLine("Total Aset Kas & Emas     : Rp ${nf.format(state.totalAssets)}")
            appendLine("Saldo Brankas Infaq       : Rp ${nf.format(state.virtualInfaqVaultBalance)}")
            appendLine("Pemasukan Kasab Halal     : Rp ${nf.format(state.totalIncomeKasab)}")
            appendLine("Pengeluaran Konsumsi      : Rp ${nf.format(state.totalConsumptionExpense)}")
            appendLine("Infaq & Zakat Disalurkan  : Rp ${nf.format(state.totalDisbursedInfaq)}")
            appendLine("Spiritual Liquidity       : ${String.format(Locale.US, "%.1f", state.spiritualLiquidityIndex)}%")
            appendLine("Nisab Emas Acuan          : Rp ${nf.format(state.nisabThreshold)} (85g Emas)")
            appendLine("Status Wajib Zakat        : ${if (state.isNisabReached) "Mencapai Nisab" else "Belum Mencapai Nisab"}")
            appendLine("Target Tabungan Ibadah    : ${state.ibadahGoals.size} program aktif")
            appendLine("Total Transaksi Jurnal    : ${state.journalEntries.size} catatan")
            appendLine("══════════════════════════════════════════════")
            appendLine("Amanah Ledger • Sistem Akuntansi Syariah Modern")
        }
    }

    // ==========================================
    // ROOM DATABASE & CLOUD FIRESTORE SYNC & AUTH
    // ==========================================

    private var appContext: android.content.Context? = null
    private var roomDatabase: AmanahDatabase? = null
    private var syncEngine: FirestoreSyncEngine? = null
    private var authManager: AmanahAuthManager? = null
    private var repository: AmanahRepository? = null
    private var lastScheduledInfaqConfig: InfaqVaultReminderConfig? = null
    private var lastScheduledNotificationConfig: AmanahNotificationConfig? = null

    private fun persistWalletMutationsAsync(mutations: List<WalletMutationRecord>) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val arr = org.json.JSONArray()
                for (m in mutations.take(200)) {
                    val obj = org.json.JSONObject()
                    obj.put("id", m.id)
                    obj.put("fromWalletId", m.fromWalletId)
                    obj.put("toWalletId", m.toWalletId)
                    obj.put("amount", m.amount)
                    obj.put("adminFee", m.adminFee)
                    obj.put("note", m.note)
                    obj.put("timestampMillis", m.timestampMillis)
                    obj.put("transactionId", m.transactionId)
                    obj.put("linkedJournalEntryId", m.linkedJournalEntryId)
                    arr.put(obj)
                }
                db.settingsDao().insertOrUpdate(SettingsEntity("wallet_mutations_json", arr.toString()))
            } catch (e: Exception) {
                AppDebugLogger.w("AmanahWallet", "Gagal menyimpan riwayat mutasi kantong: ${e.message}")
            }
        }
    }

    private fun persistIbadahDepositsAsync(deposits: List<IbadahDepositRecord>) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val arr = org.json.JSONArray()
                for (dep in deposits.take(500)) {
                    val obj = org.json.JSONObject()
                    obj.put("id", dep.id)
                    obj.put("goalId", dep.goalId)
                    obj.put("amount", dep.amount)
                    obj.put("sourceWalletId", dep.sourceWalletId)
                    obj.put("fromWalletId", dep.fromWalletId)
                    obj.put("depositDateMillis", dep.depositDate.time)
                    obj.put("notes", dep.notes)
                    arr.put(obj)
                }
                db.settingsDao().insertOrUpdate(SettingsEntity("ibadah_deposits_json", arr.toString()))
            } catch (e: Exception) {
                AppDebugLogger.w("AmanahIbadah", "Gagal menyimpan riwayat setoran ibadah: ${e.message}")
            }
        }
    }

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _authState = MutableStateFlow(AmanahAuthState())
    val authState: StateFlow<AmanahAuthState> = _authState.asStateFlow()

    private fun persistEntryAsync(entry: JournalEntry) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.journalDao().insertOrUpdate(EntityMappers.toEntity(entry))
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error persisting journal entry", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menyimpan transaksi jurnal ID: ${entry.id}", e)
            }
        }
    }

    private fun deleteEntryAsync(entryId: String) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.journalDao().deleteById(entryId)
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error deleting journal entry", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menghapus transaksi jurnal ID: $entryId", e)
            }
        }
    }

    private fun persistWalletAsync(wallet: WalletAccount) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.walletDao().insertOrUpdate(EntityMappers.toEntity(wallet))
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error persisting wallet", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menyimpan kantong: ${wallet.name}", e)
            }
        }
    }

    private fun deleteWalletAsync(walletId: String) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.walletDao().deleteById(walletId)
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error deleting wallet", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menghapus kantong ID: $walletId", e)
            }
        }
    }

    private fun persistBudgetAsync(budget: BudgetAllocation) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.budgetDao().insertOrUpdate(EntityMappers.toEntity(budget))
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error persisting budget", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menyimpan anggaran pos: ${budget.categoryName}", e)
            }
        }
    }

    private fun deleteBudgetAsync(budgetId: String) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.budgetDao().deleteById(budgetId)
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error deleting budget", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menghapus anggaran ID: $budgetId", e)
            }
        }
    }

    private fun persistGoalAsync(goal: IbadahGoal) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.ibadahGoalDao().insertOrUpdate(EntityMappers.toEntity(goal))
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error persisting ibadah goal", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menyimpan target ibadah: ${goal.title}", e)
            }
        }
    }

    private fun deleteGoalAsync(goalId: String) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.ibadahGoalDao().deleteById(goalId)
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error deleting goal", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menghapus target ibadah ID: $goalId", e)
            }
        }
    }

    private fun persistQardhAsync(qardh: QardhRecord) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.qardhDao().insertOrUpdate(EntityMappers.toEntity(qardh))
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error persisting qardh", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menyimpan qardh: ${qardh.counterpartyName}", e)
            }
        }
    }

    private fun deleteQardhAsync(qardhId: String) {
        val db = roomDatabase ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                db.qardhDao().deleteById(qardhId)
            } catch (e: Exception) {
                android.util.Log.e("AmanahLedgerVM", "Error deleting qardh", e)
                AppDebugLogger.logHandledError("RoomDatabase", "Gagal menghapus qardh ID: $qardhId", e)
            }
        }
    }

    fun initContextDependencies(context: android.content.Context) {
        appContext = context.applicationContext
        if (roomDatabase == null) {
            val db = AmanahDatabase.getDatabase(context)
            roomDatabase = db
            val se = FirestoreSyncEngine(context, db)
            syncEngine = se
            val am = AmanahAuthManager(context)
            authManager = am
            val repo = AmanahRepositoryImpl(db, DataStoreManager(context))
            repository = repo

            val legacyHaulTs = context.getSharedPreferences("amanah_zakat_reminders", android.content.Context.MODE_PRIVATE)
                .getLong("haul_start_date_ts", 0L)

            viewModelScope.launch {
                se.syncState.collect { s ->
                    _syncState.value = s
                    if (s.status == SyncStateStatus.SUCCESS && s.lastSyncedTimeMillis > 0L) {
                        recordBackupCompleted(s.lastSyncedTimeMillis)
                    }
                }
            }
            viewModelScope.launch {
                am.authState.collect { a ->
                    _authState.value = a
                    if (a.status == AuthStatus.AUTHENTICATED && a.user.uid.isNotBlank() && !a.user.uid.startsWith("guest_")) {
                        refreshCloudSnapshotSummary()
                    } else {
                        _uiState.update { it.copy(cloudSnapshotSummary = null, isLoadingCloudSummary = false) }
                    }
                }
            }
            viewModelScope.launch {
                repo.userPreferencesFlow.collect { prefs ->
                    val effectiveHaulTs = prefs.haulStartDateMillis.takeIf { it > 0L }
                        ?: legacyHaulTs.takeIf { it > 0L }
                        ?: 0L
                    val applyDefaultMask = !hasAppliedInitialMaskDefault && prefs.securityConfig.maskBalanceByDefault
                    hasAppliedInitialMaskDefault = true
                    _uiState.update { current ->
                        val shouldLock = prefs.securityConfig.isPinEnabled && !isSessionAuthenticated
                        val resolvedMask = if (applyDefaultMask) true else prefs.securityConfig.isMaskBalance
                        current.copy(
                            themeMode = prefs.themeMode,
                            isDarkMode = prefs.isDarkMode,
                            completedGuides = prefs.completedGuides,
                            hasCompletedOnboarding = prefs.hasCompletedOnboarding,
                            isHighContrast = prefs.isHighContrast,
                            goldPricePerGram = prefs.goldPricePerGram,
                            goldPriceLastUpdatedMillis = prefs.goldPriceLastUpdated,
                            selectedHijriOffset = prefs.selectedHijriOffset,
                            userNameKasMukmin = prefs.userNameKasMukmin,
                            startDayOfMonth = prefs.startDayOfMonth,
                            initialLedgerDate = prefs.initialLedgerDate,
                            fiscalCycleType = prefs.fiscalCycleType,
                            initialLedgerBalance = prefs.initialLedgerBalance,
                            fiscalYearStartMonth = prefs.fiscalYearStartMonth,
                            primaryCurrencySymbol = prefs.primaryCurrencySymbol,
                            defaultInfaqRate = prefs.defaultInfaqRate,
                            isAutoDeductInfaqEnabled = prefs.isAutoDeductInfaqEnabled,
                            vaultMonthlyTarget = prefs.vaultMonthlyTarget,
                            sedekahSubuhTargetDays = prefs.sedekahSubuhTargetDays,
                            israfWarningThresholdPercent = prefs.israfWarningThresholdPercent,
                            isStrictBudgetEnforced = prefs.isStrictBudgetEnforced,
                            isDeficitProtectionEnabled = prefs.isDeficitProtectionEnabled,
                            autoExecuteRecurringEnabled = prefs.autoExecuteRecurringEnabled,
                            notifyOnRecurringDue = prefs.notifyOnRecurringDue,
                            showDailyHadith = prefs.showDailyHadith,
                            showQuickTutorial = prefs.showQuickTutorial,
                            selectedGoalMode = prefs.selectedGoalMode,
                            uiScaleMode = prefs.uiScaleMode,
                            uiScaleFactor = prefs.uiScaleFactor,
                            lastBackupTimeMillis = prefs.lastBackupTimeMillis,
                            isBackupNudgeDismissed = prefs.isBackupNudgeDismissed,
                            appDisplayMode = prefs.appDisplayMode,
                            isShakeFeedbackEnabled = prefs.isShakeFeedbackEnabled,
                            featureVisibilityConfig = prefs.featureVisibilityConfig,
                            infaqVaultReminderConfig = prefs.infaqVaultReminderConfig,
                            notificationConfig = prefs.notificationConfig,
                            autoCloudBackupEnabled = prefs.autoCloudBackupEnabled,
                            pdfEditorialConfig = prefs.pdfEditorialConfig,
                            purifiedAuditEntryIds = prefs.purifiedAuditEntryIds,
                            haulStartDate = effectiveHaulTs.takeIf { it > 0L } ?: current.haulStartDate,
                            zakatMaal = if (effectiveHaulTs > 0L && current.zakatMaal.haulStartDateMillis <= 0L) {
                                current.zakatMaal.copy(haulStartDateMillis = effectiveHaulTs)
                            } else current.zakatMaal,
                            securityConfig = current.securityConfig.copy(
                                isPinEnabled = prefs.securityConfig.isPinEnabled,
                                pinHash = prefs.securityConfig.pinHash,
                                isBiometricEnabled = prefs.securityConfig.isBiometricEnabled,
                                isMaskBalance = resolvedMask,
                                maskBalanceByDefault = prefs.securityConfig.maskBalanceByDefault,
                                isScreenshotProtected = prefs.securityConfig.isScreenshotProtected,
                                securityQuestion = prefs.securityConfig.securityQuestion,
                                securityAnswerHash = prefs.securityConfig.securityAnswerHash,
                                autoLockInterval = prefs.securityConfig.autoLockInterval,
                                isAppLocked = shouldLock
                            )
                        )
                    }
                    if (prefs.infaqVaultReminderConfig != lastScheduledInfaqConfig) {
                        lastScheduledInfaqConfig = prefs.infaqVaultReminderConfig
                        com.example.core.worker.InfaqVaultReminderWorker.scheduleOrUpdate(
                            context.applicationContext,
                            prefs.infaqVaultReminderConfig
                        )
                    }
                    if (prefs.notificationConfig != lastScheduledNotificationConfig) {
                        lastScheduledNotificationConfig = prefs.notificationConfig
                        AmanahReminderWorker.scheduleFromConfig(
                            context.applicationContext,
                            prefs.notificationConfig
                        )
                    }
                }
            }

            viewModelScope.launch {
                db.rezekiNonTunaiDao().getAllRezekiFlow().collect { entities ->
                    val domainList = entities.map { EntityMappers.toDomain(it) }
                    _uiState.update { current ->
                        current.copy(rezekiNonTunaiList = domainList)
                    }
                }
            }

            // Load initial data from Room if available
            viewModelScope.launch {
                try {
                    val isCleared = db.settingsDao().getSettingByKey("user_has_cleared_dummy_data")?.value == "true"
                    val dummyVersion = db.settingsDao().getSettingByKey("dummy_data_version")?.value
                    val existingEntries = db.journalDao().getAllEntries()
                    val hasUserEntries = existingEntries.any { !it.id.startsWith("j_sample_") }

                    if (!isCleared && dummyVersion != "v4_multi_wallet_isolated" && !hasUserEntries && existingEntries.isNotEmpty()) {
                        // Refresh dummy data to ensure isolated wallets (acc_cash vs acc_ewallet vs acc_other_asset)
                        db.budgetDao().clearAll()
                        db.journalDao().clearAll()
                        db.walletDao().clearAll()
                        val freshEntries = AmanahDummyDataGenerator.getSampleJournalEntries()
                        val freshBudgets = AmanahDummyDataGenerator.getSampleBudgets()
                        val freshWallets = AmanahDummyDataGenerator.getSampleWallets()
                        db.journalDao().insertAll(freshEntries.map { EntityMappers.toEntity(it) })
                        db.budgetDao().insertAll(freshBudgets.map { EntityMappers.toEntity(it) })
                        db.walletDao().insertAll(freshWallets.map { EntityMappers.toEntity(it) })
                        db.settingsDao().insertOrUpdate(SettingsEntity("dummy_data_version", "v4_multi_wallet_isolated"))

                        val localWallets = freshWallets
                        val localGoals = db.ibadahGoalDao().getAllGoals().map { EntityMappers.toDomain(it) }
                        val localQardh = db.qardhDao().getAllRecords().map { EntityMappers.toDomain(it) }
                        val localSedekah = repository?.getSedekahSubuh()
                        val localRecurring = db.recurringTransactionDao().getAll().map { EntityMappers.toDomain(it) }
                        val localDistributions = db.infaqDistributionDao().getAll().map { EntityMappers.toDomain(it) }
                        var localRules = db.infaqRuleDao().getAll().map { EntityMappers.toDomain(it) }
                        if (localRules.isEmpty()) {
                            db.infaqRuleDao().insertAll(DEFAULT_INFAQ_RULES.map { EntityMappers.toEntity(it) })
                            localRules = DEFAULT_INFAQ_RULES
                        }

                        _uiState.update { current ->
                            current.copy(
                                journalEntries = freshEntries,
                                budgets = freshBudgets,
                                wallets = if (localWallets.isNotEmpty()) localWallets else current.wallets,
                                ibadahGoals = if (localGoals.isNotEmpty()) localGoals else current.ibadahGoals,
                                qardhRecords = if (localQardh.isNotEmpty()) localQardh else current.qardhRecords,
                                recurringTransactions = if (localRecurring.isNotEmpty()) localRecurring else current.recurringTransactions,
                                infaqDistributions = if (localDistributions.isNotEmpty()) localDistributions else current.infaqDistributions,
                                rules = if (localRules.isNotEmpty()) localRules else current.rules,
                                sedekahSubuhState = localSedekah ?: current.sedekahSubuhState
                            )
                        }
                    } else {
                        if (hasUserEntries && dummyVersion != "v4_multi_wallet_isolated") {
                            db.settingsDao().insertOrUpdate(SettingsEntity("dummy_data_version", "v4_multi_wallet_isolated"))
                        }
                        val localEntries = existingEntries.map { EntityMappers.toDomain(it) }
                        val rawLocalWallets = db.walletDao().getAllWallets().map { EntityMappers.toDomain(it) }
                        val localWallets = rawLocalWallets.map { w ->
                            if (w.type == com.example.core.wallet.WalletType.E_WALLET && (w.linkedAccountId == "acc_cash" || w.linkedAccountId == "acc_bank")) {
                                val fixed = w.copy(linkedAccountId = "acc_ewallet")
                                persistWalletAsync(fixed)
                                fixed
                            } else if (w.type == com.example.core.wallet.WalletType.OTHER && w.linkedAccountId == "acc_cash") {
                                val fixed = w.copy(linkedAccountId = "acc_other_asset")
                                persistWalletAsync(fixed)
                                fixed
                            } else w
                        }
                        val localGoals = db.ibadahGoalDao().getAllGoals().map { EntityMappers.toDomain(it) }
                        val localQardh = db.qardhDao().getAllRecords().map { EntityMappers.toDomain(it) }
                        val localBudgets = db.budgetDao().getAllBudgets().map { EntityMappers.toDomain(it) }
                        val localSedekah = repository?.getSedekahSubuh()
                        val localRecurring = db.recurringTransactionDao().getAll().map { EntityMappers.toDomain(it) }
                        val localDistributions = db.infaqDistributionDao().getAll().map { EntityMappers.toDomain(it) }
                        var localRules = db.infaqRuleDao().getAll().map { EntityMappers.toDomain(it) }
                        if (localRules.isEmpty() && !isCleared) {
                            db.infaqRuleDao().insertAll(DEFAULT_INFAQ_RULES.map { EntityMappers.toEntity(it) })
                            localRules = DEFAULT_INFAQ_RULES
                        }

                        if (isCleared) {
                            // Pengguna telah memilih menghapus data dummy
                            _uiState.update { current ->
                                current.copy(
                                    journalEntries = localEntries,
                                    wallets = localWallets,
                                    ibadahGoals = localGoals,
                                    qardhRecords = localQardh,
                                    budgets = localBudgets,
                                    recurringTransactions = localRecurring,
                                    infaqDistributions = localDistributions,
                                    rules = if (localRules.isNotEmpty()) localRules else current.rules,
                                    sedekahSubuhState = localSedekah ?: SedekahSubuhStreakEngine.calculateStreak(emptyMap())
                                )
                            }
                        } else if (localEntries.isNotEmpty() || localWallets.isNotEmpty() || localRecurring.isNotEmpty() || localDistributions.isNotEmpty()) {
                            // Data telah tersimpan di Room
                            _uiState.update { current ->
                                current.copy(
                                    journalEntries = localEntries,
                                    wallets = localWallets,
                                    ibadahGoals = if (localGoals.isNotEmpty()) localGoals else current.ibadahGoals,
                                    qardhRecords = if (localQardh.isNotEmpty()) localQardh else current.qardhRecords,
                                    budgets = if (localBudgets.isNotEmpty()) localBudgets else current.budgets,
                                    recurringTransactions = if (localRecurring.isNotEmpty()) localRecurring else current.recurringTransactions,
                                    infaqDistributions = if (localDistributions.isNotEmpty()) localDistributions else current.infaqDistributions,
                                    rules = if (localRules.isNotEmpty()) localRules else current.rules,
                                    sedekahSubuhState = localSedekah ?: current.sedekahSubuhState
                                )
                            }
                        } else {
                            // Pengguna baru pertama kali membuka aplikasi: Tanam data dummy ke Room
                            seedDummyDataToRoom(db)
                            db.settingsDao().insertOrUpdate(SettingsEntity("dummy_data_version", "v4_multi_wallet_isolated"))
                        }
                    }

                    // Muat Shariah Rules & Rulings dari Repository / Room
                    val loadedShariahConfig = repository?.getShariahConfig() ?: ShariahRulesConfig()
                    var loadedRulings = repository?.getAllShariahRulings() ?: emptyList()
                    if (loadedRulings.isEmpty()) {
                        val defaults = ShariahDefaultRulings.defaults
                        for (r in defaults) {
                            repository?.saveShariahRuling(r)
                        }
                        loadedRulings = defaults
                    }
                    _uiState.update { current ->
                        current.copy(
                            shariahConfig = loadedShariahConfig,
                            shariahRulings = loadedRulings
                        )
                    }

                    // Muat Zakat Hub saved calculations
                    val savedProfesiJson = repository?.getCustomSetting("zakat_hub_profesi")
                    val savedPerniagaanJson = repository?.getCustomSetting("zakat_hub_perniagaan")
                    val savedFitrahJson = repository?.getCustomSetting("zakat_hub_fitrah")
                    val savedMaalJson = repository?.getCustomSetting("zakat_hub_maal")

                    _uiState.update { current ->
                        current.copy(
                            zakatProfesi = if (!savedProfesiJson.isNullOrBlank()) deserializeZakatProfesi(savedProfesiJson) else current.zakatProfesi,
                            zakatPerniagaan = if (!savedPerniagaanJson.isNullOrBlank()) deserializeZakatPerniagaan(savedPerniagaanJson) else current.zakatPerniagaan,
                            zakatFitrah = if (!savedFitrahJson.isNullOrBlank()) deserializeZakatFitrah(savedFitrahJson) else current.zakatFitrah,
                            zakatMaal = if (!savedMaalJson.isNullOrBlank()) deserializeZakatMaal(savedMaalJson, current.zakatMaal) else current.zakatMaal
                        )
                    }

                    val customAccJson = db.settingsDao().getSettingByKey("custom_asset_accounts")?.value
                    if (!customAccJson.isNullOrBlank()) {
                        try {
                            val jsonArr = org.json.JSONArray(customAccJson)
                            val loaded = mutableListOf<Account>()
                            for (i in 0 until jsonArr.length()) {
                                val obj = jsonArr.getJSONObject(i)
                                loaded.add(
                                    Account(
                                        id = obj.getString("id"),
                                        code = obj.getString("code"),
                                        name = obj.getString("name"),
                                        category = AccountCategory.ASSET,
                                        description = obj.optString("description", "")
                                    )
                                )
                            }
                            if (loaded.isNotEmpty()) {
                                _uiState.update { current ->
                                    val existingIds = current.accounts.map { it.id }.toSet()
                                    current.copy(accounts = current.accounts + loaded.filterNot { it.id in existingIds })
                                }
                            }
                        } catch (e: Exception) {
                            AppDebugLogger.w("AmanahWallet", "Gagal membaca akun kustom: ${e.message}")
                        }
                    }

                    // Muat kategori pengeluaran kustom
                    val customExpJson = db.settingsDao().getSettingByKey("custom_expense_accounts")?.value
                    if (!customExpJson.isNullOrBlank()) {
                        try {
                            val jsonArr = org.json.JSONArray(customExpJson)
                            val loaded = mutableListOf<Account>()
                            for (i in 0 until jsonArr.length()) {
                                val obj = jsonArr.getJSONObject(i)
                                loaded.add(
                                    Account(
                                        id = obj.getString("id"),
                                        code = obj.getString("code"),
                                        name = obj.getString("name"),
                                        category = AccountCategory.EXPENSE,
                                        description = obj.optString("description", ""),
                                        isCustom = true
                                    )
                                )
                            }
                            if (loaded.isNotEmpty()) {
                                _uiState.update { current ->
                                    val existingIds = current.accounts.map { it.id }.toSet()
                                    current.copy(accounts = current.accounts + loaded.filterNot { it.id in existingIds })
                                }
                            }
                        } catch (e: Exception) {
                            AppDebugLogger.w("AmanahExpense", "Gagal membaca kategori pengeluaran kustom: ${e.message}")
                        }
                    }

                    // Muat status kategori pengeluaran nonaktif
                    val disabledExpJson = db.settingsDao().getSettingByKey("disabled_expense_account_ids")?.value
                    if (!disabledExpJson.isNullOrBlank()) {
                        try {
                            val jsonArr = org.json.JSONArray(disabledExpJson)
                            val disabledSet = mutableSetOf<String>()
                            for (i in 0 until jsonArr.length()) {
                                disabledSet.add(jsonArr.getString(i))
                            }
                            _uiState.update { current ->
                                current.copy(disabledExpenseAccountIds = disabledSet)
                            }
                        } catch (e: Exception) {
                            AppDebugLogger.w("AmanahExpense", "Gagal membaca kategori pengeluaran nonaktif: ${e.message}")
                        }
                    }

                    // Muat riwayat mutasi antar-kantong tersimpan
                    val walletMutationsJson = db.settingsDao().getSettingByKey("wallet_mutations_json")?.value
                    if (!walletMutationsJson.isNullOrBlank()) {
                        try {
                            val jsonArr = org.json.JSONArray(walletMutationsJson)
                            val loadedMutations = mutableListOf<WalletMutationRecord>()
                            for (i in 0 until jsonArr.length()) {
                                val obj = jsonArr.getJSONObject(i)
                                loadedMutations.add(
                                    WalletMutationRecord(
                                        id = obj.getString("id"),
                                        fromWalletId = obj.getString("fromWalletId"),
                                        toWalletId = obj.getString("toWalletId"),
                                        amount = obj.getDouble("amount"),
                                        adminFee = obj.optDouble("adminFee", 0.0),
                                        note = obj.optString("note", ""),
                                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                                        date = Date(obj.optLong("timestampMillis", System.currentTimeMillis())),
                                        transactionId = obj.optString("transactionId", ""),
                                        linkedJournalEntryId = obj.optString("linkedJournalEntryId", "")
                                    )
                                )
                            }
                            _uiState.update { current ->
                                current.copy(walletMutations = loadedMutations)
                            }
                        } catch (e: Exception) {
                            AppDebugLogger.w("AmanahWallet", "Gagal membaca riwayat mutasi kantong: ${e.message}")
                        }
                    }

                    // Muat riwayat setoran target ibadah tersimpan
                    val ibadahDepositsJson = db.settingsDao().getSettingByKey("ibadah_deposits_json")?.value
                    if (!ibadahDepositsJson.isNullOrBlank()) {
                        try {
                            val jsonArr = org.json.JSONArray(ibadahDepositsJson)
                            val loadedDeposits = mutableListOf<IbadahDepositRecord>()
                            for (i in 0 until jsonArr.length()) {
                                val obj = jsonArr.getJSONObject(i)
                                val ts = obj.optLong("depositDateMillis", System.currentTimeMillis())
                                val srcWallet = obj.optString("sourceWalletId", obj.optString("fromWalletId", "acc_bank"))
                                val depNotes = obj.optString("notes", "")
                                loadedDeposits.add(
                                    IbadahDepositRecord(
                                        id = obj.getString("id"),
                                        goalId = obj.getString("goalId"),
                                        amount = obj.getDouble("amount"),
                                        sourceWalletId = srcWallet,
                                        fromWalletId = obj.optString("fromWalletId", srcWallet),
                                        depositDate = Date(ts),
                                        dateMillis = ts,
                                        notes = depNotes,
                                        note = depNotes
                                    )
                                )
                            }
                            _uiState.update { current ->
                                current.copy(ibadahDeposits = loadedDeposits)
                            }
                        } catch (e: Exception) {
                            AppDebugLogger.w("AmanahIbadah", "Gagal membaca riwayat setoran ibadah: ${e.message}")
                        }
                    }

                    // Eksekusi otomatis transaksi rutin yang sudah jatuh tempo saat aplikasi dibuka
                    if (!hasProcessedStartupRecurring) {
                        hasProcessedStartupRecurring = true
                        if (_uiState.value.autoExecuteRecurringEnabled) {
                            processAllDueRecurringTransactions()
                        }
                    }

                    // Sinkronisasi reaktif dari background worker (mis. Auto-Recurring Infaq Vault) ke UI
                    viewModelScope.launch {
                        db.journalDao().getAllEntriesFlow().collect { entities ->
                            val domainList = entities.map { EntityMappers.toDomain(it) }
                            _uiState.update { current ->
                                if (current.journalEntries != domainList) {
                                    current.copy(journalEntries = domainList)
                                } else current
                            }
                        }
                    }
                    viewModelScope.launch {
                        db.infaqDistributionDao().getAllFlow().collect { entities ->
                            val domainList = entities.map { EntityMappers.toDomain(it) }
                            _uiState.update { current ->
                                if (current.infaqDistributions != domainList) {
                                    current.copy(infaqDistributions = domainList)
                                } else current
                            }
                        }
                    }

                    AppDebugLogger.i("RoomDatabase", "Inisialisasi data Room selesai. Mode audit syariah siap.")

                    // Background cleanup cache gambar struk yatim / sementara
                    try {
                        val activePaths = _uiState.value.journalEntries
                            .mapNotNull { it.receiptAttachment?.imagePath }
                            .toSet()
                        ReceiptImageStorage.cleanupTemporaryCameraCache(context.applicationContext)
                        ReceiptImageStorage.cleanupOrphanReceiptImages(context.applicationContext, activePaths)
                    } catch (e: Exception) {
                        AppDebugLogger.w("AmanahVM", "Gagal pembersihan cache struk: ${e.message}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AmanahVM", "Room init load: ${e.message}")
                    AppDebugLogger.logHandledError("RoomDatabase", "Gagal memuat data awal Room DB: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun seedDummyDataToRoom(db: AmanahDatabase) {
        withContext(Dispatchers.IO) {
            try {
                val dummyEntries = AmanahDummyDataGenerator.getSampleJournalEntries()
                val dummyWallets = AmanahDummyDataGenerator.getSampleWallets()
                val dummyBudgets = AmanahDummyDataGenerator.getSampleBudgets()
                val dummyGoals = AmanahDummyDataGenerator.getSampleIbadahGoals()
                val dummyQardh = AmanahDummyDataGenerator.getSampleQardhRecords()
                val dummySedekah = AmanahDummyDataGenerator.getSampleSedekahSubuhState()
                val dummyRecurring = AmanahDummyDataGenerator.getSampleRecurringTransactions()
                val dummyDistributions = AmanahDummyDataGenerator.getSampleInfaqDistributions()

                db.journalDao().insertAll(dummyEntries.map { EntityMappers.toEntity(it) })
                db.walletDao().insertAll(dummyWallets.map { EntityMappers.toEntity(it) })
                db.budgetDao().insertAll(dummyBudgets.map { EntityMappers.toEntity(it) })
                db.ibadahGoalDao().insertAll(dummyGoals.map { EntityMappers.toEntity(it) })
                db.qardhDao().insertAll(dummyQardh.map { EntityMappers.toEntity(it) })
                db.recurringTransactionDao().insertAll(dummyRecurring.map { EntityMappers.toEntity(it) })
                db.infaqDistributionDao().insertAll(dummyDistributions.map { EntityMappers.toEntity(it) })
                if (db.infaqRuleDao().getAll().isEmpty()) {
                    db.infaqRuleDao().insertAll(DEFAULT_INFAQ_RULES.map { EntityMappers.toEntity(it) })
                }
                repository?.saveSedekahSubuh(dummySedekah)
                db.settingsDao().insertOrUpdate(SettingsEntity("dummy_data_version", "v4_multi_wallet_isolated"))
            } catch (e: Exception) {
                android.util.Log.e("AmanahVM", "Error seeding dummy data to Room: ${e.message}")
            }
        }
    }


    // ==========================================
    // PERSISTENT CONFIGURATION MUTATIONS (DATASTORE & ROOM)
    // ==========================================

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userNameKasMukmin = name) }
        viewModelScope.launch { repository?.saveUserName(name) }
    }

    fun updateCurrencySymbol(symbol: String) {
        _uiState.update { it.copy(primaryCurrencySymbol = symbol) }
        viewModelScope.launch { repository?.saveCurrencySymbol(symbol) }
    }

    fun updateStartDayOfMonth(day: Int) {
        _uiState.update { it.copy(startDayOfMonth = day.coerceIn(1, 28)) }
        viewModelScope.launch { repository?.saveStartDayOfMonth(day.coerceIn(1, 28)) }
    }

    fun setStartDayOfMonth(day: Int) = updateStartDayOfMonth(day)

    fun updateInitialLedgerDate(date: String) {
        _uiState.update { it.copy(initialLedgerDate = date) }
        viewModelScope.launch { repository?.saveInitialLedgerDate(date) }
        appStateNotifier.notify(
            title = "Tanggal Awal Buku Kas Diperbarui",
            message = "Pencatatan buku kas disinkronkan mulai $date.",
            severity = NotificationSeverity.INFO
        )
    }

    fun setInitialLedgerDate(date: String) = updateInitialLedgerDate(date)

    fun updateFiscalCycleType(cycleType: com.example.core.accounting.FiscalCycleType) {
        _uiState.update { it.copy(fiscalCycleType = cycleType) }
        viewModelScope.launch { repository?.saveFiscalCycleType(cycleType) }
        appStateNotifier.notify(
            title = "Siklus Pembukuan Diperbarui",
            message = "Siklus buku kas diatur ke ${cycleType.title}.",
            severity = NotificationSeverity.INFO
        )
    }

    fun setFiscalCycleType(cycleType: com.example.core.accounting.FiscalCycleType) = updateFiscalCycleType(cycleType)

    fun updateInitialLedgerBalance(balance: Double) {
        val cleanBalance = kotlin.math.round(balance.coerceAtLeast(0.0))
        val targetAccountId = _uiState.value.wallets.firstOrNull { it.isDefault }?.linkedAccountId
            ?: _uiState.value.wallets.firstOrNull()?.linkedAccountId
            ?: "acc_cash"
        val entryId = "j_opening_ledger_balance"

        if (cleanBalance > 0.0) {
            val parsedDate = try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(_uiState.value.initialLedgerDate)
            } catch (_: Exception) { null } ?: Date()
            val hijri = HijriCalendarEngine.fromGregorian(parsedDate)
            val openingEntry = JournalEntry(
                id = entryId,
                gregorianDate = parsedDate,
                hijriYear = hijri.year,
                hijriMonth = hijri.month,
                hijriDay = hijri.day,
                description = "Saldo Awal Buku Kas Utama",
                transactionType = "INFLOW",
                lines = listOf(
                    JournalLine(accountId = targetAccountId, debit = cleanBalance, credit = 0.0),
                    JournalLine(accountId = "acc_equity", debit = 0.0, credit = cleanBalance)
                )
            )
            _uiState.update { state ->
                val withoutOld = state.journalEntries.filterNot { it.id == entryId }
                state.copy(
                    initialLedgerBalance = cleanBalance,
                    journalEntries = listOf(openingEntry) + withoutOld
                )
            }
            persistEntryAsync(openingEntry)
        } else {
            _uiState.update { state ->
                state.copy(
                    initialLedgerBalance = 0.0,
                    journalEntries = state.journalEntries.filterNot { it.id == entryId }
                )
            }
            deleteEntryAsync(entryId)
        }
        viewModelScope.launch { repository?.saveInitialLedgerBalance(cleanBalance) }
    }

    fun setInitialLedgerBalance(balance: Double) = updateInitialLedgerBalance(balance)

    fun updateFiscalYearStartMonth(month: Int) {
        _uiState.update { it.copy(fiscalYearStartMonth = month.coerceIn(1, 12)) }
        viewModelScope.launch { repository?.saveFiscalYearStartMonth(month.coerceIn(1, 12)) }
    }

    fun setFiscalYearStartMonth(month: Int) = updateFiscalYearStartMonth(month)

    fun updateHijriOffset(offset: Int) {
        _uiState.update { it.copy(selectedHijriOffset = offset.coerceIn(-3, 3)) }
        viewModelScope.launch { repository?.saveHijriOffset(offset.coerceIn(-3, 3)) }
    }

    fun updateDefaultInfaqRate(rate: Double) {
        _uiState.update { it.copy(defaultInfaqRate = rate.coerceIn(0.01, 0.50)) }
        viewModelScope.launch { repository?.saveDefaultInfaqRate(rate.coerceIn(0.01, 0.50)) }
    }

    fun updateAutoDeductInfaq(enabled: Boolean) {
        _uiState.update { it.copy(isAutoDeductInfaqEnabled = enabled) }
        viewModelScope.launch { repository?.saveAutoDeductInfaq(enabled) }
    }

    fun updateVaultMonthlyTarget(target: Double) {
        _uiState.update { it.copy(vaultMonthlyTarget = target.coerceAtLeast(0.0)) }
        viewModelScope.launch { repository?.saveVaultMonthlyTarget(target.coerceAtLeast(0.0)) }
    }

    fun updateSedekahSubuhTargetDays(days: Int) {
        _uiState.update { it.copy(sedekahSubuhTargetDays = days.coerceIn(7, 365)) }
        viewModelScope.launch { repository?.saveSedekahSubuhTargetDays(days.coerceIn(7, 365)) }
    }

    fun updateIsrafWarningThreshold(percent: Int) {
        _uiState.update { it.copy(israfWarningThresholdPercent = percent.coerceIn(0, 100)) }
        viewModelScope.launch { repository?.saveIsrafWarningThreshold(percent.coerceIn(0, 100)) }
    }

    fun updateStrictBudgetEnforced(enforced: Boolean) {
        _uiState.update { it.copy(isStrictBudgetEnforced = enforced) }
        viewModelScope.launch { repository?.saveStrictBudgetEnforced(enforced) }
    }

    fun setDeficitProtectionEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isDeficitProtectionEnabled = enabled) }
        viewModelScope.launch { repository?.saveDeficitProtectionEnabled(enabled) }
    }

    fun updateDeficitProtectionEnabled(enabled: Boolean) = setDeficitProtectionEnabled(enabled)

    fun updateAutoExecuteRecurring(enabled: Boolean) {
        _uiState.update { it.copy(autoExecuteRecurringEnabled = enabled) }
        viewModelScope.launch { repository?.saveAutoExecuteRecurring(enabled) }
    }

    fun updateNotifyOnRecurringDue(notify: Boolean) {
        _uiState.update { it.copy(notifyOnRecurringDue = notify) }
        viewModelScope.launch { repository?.saveNotifyOnRecurringDue(notify) }
    }

    fun updateShowDailyHadith(show: Boolean) {
        _uiState.update { it.copy(showDailyHadith = show) }
        viewModelScope.launch { repository?.saveShowDailyHadith(show) }
    }

    fun updateShowQuickTutorial(show: Boolean) {
        _uiState.update { it.copy(showQuickTutorial = show) }
        viewModelScope.launch { repository?.saveShowQuickTutorial(show) }
    }

    fun setupPinLock(pin: String) {
        if (pin.length < 4) return
        val hashed = SecurityConfig.hashString(pin)
        val current = _uiState.value.securityConfig
        val newCfg = current.copy(
            isPinEnabled = true,
            pinHash = hashed,
            securityQuestion = current.securityQuestion,
            securityAnswerHash = current.securityAnswerHash,
            failedAttempts = 0,
            lockoutUntilTimestamp = 0L,
            isAppLocked = false
        )
        isSessionAuthenticated = true
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.PIN_ENABLED,
                description = "Kunci PIN 6-digit berhasil diaktifkan"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
        AppDebugLogger.i("AmanahSecurity", "Kunci PIN 6-digit berhasil diaktifkan.")
    }

    fun disablePinLock() {
        val newCfg = _uiState.value.securityConfig.copy(
            isPinEnabled = false,
            pinHash = "",
            failedAttempts = 0,
            lockoutUntilTimestamp = 0L,
            isAppLocked = false
        )
        isSessionAuthenticated = true
        _uiState.update { state ->
            val newLog = SecurityLogEntry(
                eventType = SecurityEventType.PIN_DISABLED,
                description = "Kunci PIN aplikasi dinonaktifkan"
            )
            state.copy(
                securityConfig = newCfg.copy(
                    logs = listOf(newLog) + state.securityConfig.logs
                )
            )
        }
        viewModelScope.launch { repository?.saveSecurityConfig(newCfg) }
        AppDebugLogger.w("AmanahSecurity", "Kunci PIN aplikasi dinonaktifkan oleh pengguna.")
    }

    fun toggleBiometric(enabled: Boolean) {
        setBiometricEnabled(enabled)
        AppDebugLogger.i("AmanahSecurity", "Biometrik otentikasi disetel: $enabled")
    }

    fun resetAllSettingsToDefault() {
        appContext?.let { ctx ->
            com.example.core.worker.InfaqVaultReminderWorker.cancel(ctx)
            AmanahReminderWorker.scheduleFromConfig(ctx, AmanahNotificationConfig())
        }
        viewModelScope.launch {
            repository?.clearAllPreferences()
            AppDebugLogger.w("AmanahSettings", "Mereset semua pengaturan aplikasi ke konfigurasi bawaan.")
            _uiState.update {
                it.copy(
                    themeMode = com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM,
                    isDarkMode = true,
                    isHighContrast = false,
                    goldPricePerGram = 1350000.0,
                    selectedHijriOffset = 0,
                    userNameKasMukmin = "Kas Keluarga Mukmin",
                    startDayOfMonth = 1,
                    initialLedgerDate = "01/01/2024",
                    fiscalCycleType = com.example.core.accounting.FiscalCycleType.MONTHLY_SALARY_DATE,
                    initialLedgerBalance = 0.0,
                    fiscalYearStartMonth = 1,
                    primaryCurrencySymbol = "Rp",
                    defaultInfaqRate = 0.05,
                    isAutoDeductInfaqEnabled = true,
                    vaultMonthlyTarget = 5000000.0,
                    sedekahSubuhTargetDays = 40,
                    israfWarningThresholdPercent = 80,
                    isStrictBudgetEnforced = false,
                    isDeficitProtectionEnabled = true,
                    autoExecuteRecurringEnabled = true,
                    notifyOnRecurringDue = true,
                    showDailyHadith = true,
                    showQuickTutorial = true,
                    selectedGoalMode = FinancialGoalMode.BALANCED_50_30_20,
                    uiScaleMode = com.example.ui.theme.UiScaleMode.DEFAULT,
                    uiScaleFactor = 1.0f,
                    isShakeFeedbackEnabled = true,
                    appDisplayMode = com.example.core.datastore.AppDisplayMode.SIMPLE,
                    featureVisibilityConfig = com.example.core.feature.AppFeatureVisibilityConfig(),
                    infaqVaultReminderConfig = InfaqVaultReminderConfig(),
                    notificationConfig = AmanahNotificationConfig(),
                    autoCloudBackupEnabled = false,
                    pdfEditorialConfig = PdfEditorialConfig(),
                    purifiedAuditEntryIds = emptySet(),
                    securityConfig = SecurityConfig()
                )
            }
        }
    }

    fun triggerCloudSync() {
        val se = syncEngine ?: return
        val currentAuth = _authState.value
        val userId = currentAuth.user.uid
        if (currentAuth.status != AuthStatus.AUTHENTICATED || userId.isBlank() || userId.startsWith("guest_")) {
            appStateNotifier.notify(
                title = "Login Google Diperlukan",
                message = "Masuk dengan akun Google terlebih dahulu untuk mencadangkan data ke Cloud Firestore.",
                severity = NotificationSeverity.WARNING
            )
            return
        }
        AppDebugLogger.i("AmanahSync", "Memulai sinkronisasi Cloud Firestore penuh (Pengguna: $userId)...")

        viewModelScope.launch {
            val state = _uiState.value
            val prefsSnapshot = buildPreferencesSnapshot()
            val res = se.syncAll(
                userId = userId,
                localEntries = state.journalEntries,
                localWallets = state.wallets,
                localGoals = state.ibadahGoals,
                localQardh = state.qardhRecords,
                localBudgets = state.budgets,
                sedekahState = state.sedekahSubuhState,
                localRules = state.rules,
                localRecurring = state.recurringTransactions,
                localDistributions = state.infaqDistributions,
                localRezeki = state.rezekiNonTunaiList,
                localWalletMutations = state.walletMutations,
                localIbadahDeposits = state.ibadahDeposits,
                preferences = prefsSnapshot
            )
            if (res.isSuccess) {
                refreshCloudSnapshotSummary()
                val total = res.getOrDefault(0)
                AppDebugLogger.i("AmanahSync", "Sinkronisasi Cloud sukses ($total entitas). Data & preferensi tersinkronkan.")
                appStateNotifier.notify(
                    title = "Cadangan Cloud Berhasil",
                    message = "$total entitas keuangan & seluruh konfigurasi telah dicadangkan aman ke Cloud Firestore.",
                    severity = NotificationSeverity.SUCCESS
                )
            } else {
                val errorMsg = res.exceptionOrNull()?.localizedMessage ?: "Koneksi internet atau layanan Cloud tidak tersedia."
                AppDebugLogger.logHandledError("AmanahSync", "Sinkronisasi Cloud gagal: $errorMsg", res.exceptionOrNull())
                appStateNotifier.notify(
                    title = "Sinkronisasi Cloud Gagal",
                    message = errorMsg,
                    severity = NotificationSeverity.WARNING
                )
            }
        }
    }

    fun restoreDataFromCloud(replaceLocal: Boolean = true) {
        val se = syncEngine ?: return
        val currentAuth = _authState.value
        val userId = currentAuth.user.uid
        if (currentAuth.status != AuthStatus.AUTHENTICATED || userId.isBlank() || userId.startsWith("guest_")) {
            appStateNotifier.notify(
                title = "Login Google Diperlukan",
                message = "Masuk dengan akun Google terlebih dahulu untuk memulihkan data dari Cloud.",
                severity = NotificationSeverity.WARNING
            )
            return
        }
        AppDebugLogger.i("AmanahSync", "Meminta pemulihan data cadangan dari Firestore untuk ID: $userId (replaceLocal=$replaceLocal)")

        viewModelScope.launch {
            val res = se.restoreFromCloud(
                userId = userId,
                replaceLocal = replaceLocal
            )
            val payload = res.getOrNull()
            if (res.isSuccess && payload != null) {
                _uiState.update { cur ->
                    cur.copy(
                        journalEntries = payload.entries,
                        wallets = payload.wallets,
                        ibadahGoals = payload.goals,
                        qardhRecords = payload.qardh,
                        budgets = payload.budgets,
                        sedekahSubuhState = payload.sedekahState,
                        rules = if (payload.rules.isNotEmpty()) payload.rules else cur.rules,
                        recurringTransactions = payload.recurring,
                        infaqDistributions = payload.distributions,
                        rezekiNonTunaiList = payload.rezekiNonTunai,
                        walletMutations = payload.walletMutations,
                        ibadahDeposits = payload.ibadahDeposits
                    )
                }
                payload.preferences?.let { applyPreferencesSnapshot(it) }
                syncZakatMaalFromDatabase()
                refreshCloudSnapshotSummary()
                recordBackupCompleted()
                AppDebugLogger.i("AmanahSync", "Pemulihan Cloud sukses: ${payload.entries.size} jurnal, ${payload.wallets.size} kantong.")
                appStateNotifier.notify(
                    title = "Pemulihan Cloud Berhasil",
                    message = "Berhasil memulihkan ${payload.entries.size} jurnal, ${payload.wallets.size} kantong, dan preferensi akun.",
                    severity = NotificationSeverity.SUCCESS
                )
            } else {
                val err = res.exceptionOrNull()?.localizedMessage ?: "Belum ada data cadangan di akun ini."
                AppDebugLogger.logHandledError("AmanahSync", "Gagal memulihkan data dari Cloud: $err", res.exceptionOrNull())
                appStateNotifier.notify(
                    title = "Gagal Memulihkan Cloud",
                    message = err,
                    severity = NotificationSeverity.WARNING
                )
            }
        }
    }

    fun trySilentGoogleSignIn(activityContext: android.content.Context) {
        val am = authManager ?: return
        viewModelScope.launch {
            val res = am.trySilentGoogleSignIn(activityContext)
            if (res.isSuccess) {
                refreshCloudSnapshotSummary()
            }
        }
    }

    fun signInWithGoogle(activityContext: android.content.Context, clientId: String = "") {
        val am = authManager ?: return
        viewModelScope.launch {
            val res = am.signInWithGoogleCredential(activityContext, clientId)
            val profile = res.getOrNull()
            if (res.isSuccess && profile != null) {
                refreshCloudSnapshotSummary()
                val summary = syncEngine?.fetchCloudSnapshotSummary(profile.uid)?.getOrNull()
                if (summary?.exists == true) {
                    appStateNotifier.notify(
                        title = "Google Sign-In Berhasil",
                        message = "Terhubung sebagai ${profile.email}. Ditemukan cadangan Cloud (${summary.totalBackedUpItems} item) siap dipulihkan.",
                        severity = NotificationSeverity.SUCCESS
                    )
                } else {
                    triggerCloudSync()
                    appStateNotifier.notify(
                        title = "Google Sign-In Berhasil",
                        message = "Terhubung dengan akun Google: ${profile.email}",
                        severity = NotificationSeverity.SUCCESS
                    )
                }
            } else {
                val err = res.exceptionOrNull()?.localizedMessage
                if (!err.isNullOrBlank()) {
                    appStateNotifier.notify(
                        title = "Google Sign-In Dibatalkan / Gagal",
                        message = err,
                        severity = NotificationSeverity.WARNING
                    )
                }
            }
        }
    }

    fun continueAsGuest() {
        val am = authManager ?: return
        viewModelScope.launch {
            am.continueAsGuest()
            appStateNotifier.notify(
                title = "Mode Tamu Aktif",
                message = "Anda menggunakan pencatatan lokal di perangkat ini.",
                severity = NotificationSeverity.INFO
            )
        }
    }

    fun signOutAuth() {
        val am = authManager ?: return
        viewModelScope.launch {
            am.signOut()
            appStateNotifier.notify(
                title = "Berhasil Keluar",
                message = "Sesi akun telah diakhiri dengan aman.",
                severity = NotificationSeverity.INFO
            )
        }
    }

    fun createBackupPackage(password: String = ""): String = generateBackupPackage(password)

    /**
     * Manajemen Direktori Amil Zakat & ZISWAF (CRUD)
     */
    fun addAmilInstitution(institution: AmilInstitution) {
        _uiState.update { st ->
            st.copy(amilInstitutions = listOf(institution) + st.amilInstitutions)
        }
        appStateNotifier.notify(
            title = "Lembaga Berhasil Ditambahkan",
            message = "Lembaga '${institution.name}' berhasil disimpan ke direktori.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun updateAmilInstitution(institution: AmilInstitution) {
        _uiState.update { st ->
            st.copy(
                amilInstitutions = st.amilInstitutions.map {
                    if (it.id == institution.id) institution else it
                }
            )
        }
        appStateNotifier.notify(
            title = "Lembaga Diperbarui",
            message = "Data dan rekening '${institution.name}' berhasil diperbarui.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun deleteAmilInstitution(institutionId: String) {
        val target = _uiState.value.amilInstitutions.firstOrNull { it.id == institutionId }
        _uiState.update { st ->
            st.copy(amilInstitutions = st.amilInstitutions.filter { it.id != institutionId })
        }
        appStateNotifier.notify(
            title = "Lembaga Dihapus",
            message = "Lembaga '${target?.name ?: institutionId}' telah dihapus dari direktori.",
            severity = NotificationSeverity.INFO
        )
    }

    fun resetAmilInstitutionsToDefault() {
        _uiState.update { st ->
            st.copy(amilInstitutions = AmilDirectoryRepository.verifiedInstitutions)
        }
        appStateNotifier.notify(
            title = "Direktori Direset",
            message = "Daftar lembaga amil dikembalikan ke data resmi terverifikasi standar.",
            severity = NotificationSeverity.INFO
        )
    }

    // ==============================================================
    // PUSAT KUSTOMISASI LOGIKA & PARAMETER SYARIAH (RULES ENGINE)
    // ==============================================================

    fun updateShariahConfig(config: ShariahRulesConfig) {
        _uiState.update { it.copy(shariahConfig = config) }
        viewModelScope.launch {
            repository?.saveShariahConfig(config)
        }
        AppDebugLogger.i("AmanahShariah", "Parameter syariah diperbarui: Nisab ${config.goldNisabGram}g emas (${config.selectedMazhab.displayName})")
        appStateNotifier.notify(
            title = "Parameter Syariah Diperbarui",
            message = "Kaidah hisab nisab ${config.goldNisabGram}g emas (${config.selectedMazhab.displayName}) aktif.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun saveShariahRuling(ruling: ShariahRuling) {
        _uiState.update { st ->
            val exists = st.shariahRulings.any { it.id == ruling.id }
            val updatedList = if (exists) {
                st.shariahRulings.map { if (it.id == ruling.id) ruling else it }
            } else {
                listOf(ruling) + st.shariahRulings
            }
            st.copy(shariahRulings = updatedList)
        }
        viewModelScope.launch {
            repository?.saveShariahRuling(ruling)
        }
        AppDebugLogger.i("AmanahShariah", "Menyimpan Kaidah/Fatwa: '${ruling.title}' (${ruling.authority})")
        appStateNotifier.notify(
            title = "Rujukan Hukum / Fatwa Disimpan",
            message = "${ruling.authority}: '${ruling.title}' berhasil disimpan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun deleteShariahRuling(rulingId: String) {
        val target = _uiState.value.shariahRulings.firstOrNull { it.id == rulingId }
        _uiState.update { st ->
            st.copy(shariahRulings = st.shariahRulings.filterNot { it.id == rulingId })
        }
        viewModelScope.launch {
            repository?.deleteShariahRuling(rulingId)
        }
        AppDebugLogger.w("AmanahShariah", "Menghapus rujukan hukum/fatwa: '${target?.title ?: rulingId}'")
        appStateNotifier.notify(
            title = "Rujukan Dihapus",
            message = "Entri rujukan hukum '${target?.title ?: rulingId}' telah dihapus.",
            severity = NotificationSeverity.INFO
        )
    }

    fun toggleRulingEnabled(rulingId: String) {
        val target = _uiState.value.shariahRulings.firstOrNull { it.id == rulingId } ?: return
        val updated = target.copy(isEnabled = !target.isEnabled)
        AppDebugLogger.i("AmanahShariah", "Mengubah status fatwa '${target.title}' menjadi: ${updated.isEnabled}")
        saveShariahRuling(updated)
    }

    fun resetShariahRulingsToDefault() {
        viewModelScope.launch {
            val defaults = ShariahDefaultRulings.defaults
            for (r in defaults) {
                repository?.saveShariahRuling(r)
            }
            _uiState.update { it.copy(shariahRulings = defaults) }
            AppDebugLogger.w("AmanahShariah", "Mereset daftar rujukan fatwa dan dalil ke konfigurasi standar DSN-MUI.")
            appStateNotifier.notify(
                title = "Kaidah & Fatwa Direset",
                message = "Rujukan hukum dan fatwa DSN-MUI dikembalikan ke standar acuan resmi.",
                severity = NotificationSeverity.INFO
            )
        }
    }

    companion object {
        val DEFAULT_INFAQ_RULES = listOf(
            InfaqRule(
                id = "r_kasab_std",
                title = "Infaq Kasab Gaji Bulanan (5%)",
                targetCategory = AccountCategory.INCOME_KASAB,
                calculationType = InfaqCalculationType.PERCENTAGE,
                rate = 0.05
            ),
            InfaqRule(
                id = "r_non_kasab_std",
                title = "Infaq Non-Kasab Hadiah (10%)",
                targetCategory = AccountCategory.INCOME_NON_KASAB,
                calculationType = InfaqCalculationType.PERCENTAGE,
                rate = 0.10
            ),
            InfaqRule(
                id = "r_roundup_living",
                title = "Round-Up Belanja Konsumsi (Rp 5.000)",
                targetCategory = AccountCategory.EXPENSE,
                calculationType = InfaqCalculationType.ROUND_UP,
                roundUpStep = 5000.0
            ),
            InfaqRule(
                id = "r_rikaz_auto",
                title = "Rikaz Temuan (20% Wajib)",
                targetCategory = AccountCategory.INCOME_NON_KASAB,
                calculationType = InfaqCalculationType.RIKAZ,
                rate = 0.20
            ),
            InfaqRule(
                id = "r_syubhat_purge",
                title = "Pembersihan Syubhat / Bunga (100%)",
                targetCategory = AccountCategory.INCOME_NON_KASAB,
                calculationType = InfaqCalculationType.SYUBHAT,
                rate = 1.00
            )
        )
    }

    private fun serializeZakatProfesi(c: ZakatProfesiCalculation): String {
        val o = org.json.JSONObject()
        o.put("gross", c.monthlyGrossSalary)
        o.put("allowance", c.monthlyAllowanceAndOther)
        o.put("expenses", c.monthlyEssentialExpenses)
        o.put("debt", c.monthlyDebtInstallment)
        o.put("isNet", c.isNetMethod)
        o.put("ricePrice", c.ricePricePerKg)
        return o.toString()
    }

    private fun deserializeZakatProfesi(s: String): ZakatProfesiCalculation {
        return try {
            val o = org.json.JSONObject(s)
            ZakatProfesiCalculation(
                monthlyGrossSalary = o.optDouble("gross", 10000000.0),
                monthlyAllowanceAndOther = o.optDouble("allowance", 2500000.0),
                monthlyEssentialExpenses = o.optDouble("expenses", 4000000.0),
                monthlyDebtInstallment = o.optDouble("debt", 1000000.0),
                isNetMethod = o.optBoolean("isNet", true),
                ricePricePerKg = o.optDouble("ricePrice", 15000.0)
            )
        } catch (_: Exception) {
            ZakatProfesiCalculation()
        }
    }

    private fun serializeZakatPerniagaan(c: ZakatPerniagaanCalculation): String {
        val o = org.json.JSONObject()
        o.put("cash", c.currentCashInBusiness)
        o.put("inventory", c.inventoryValue)
        o.put("receivable", c.receivableCollectible)
        o.put("payable", c.shortTermPayable)
        o.put("goldPrice", c.goldPricePerGram)
        o.put("isLunar", c.isLunarCalendar)
        return o.toString()
    }

    private fun deserializeZakatPerniagaan(s: String): ZakatPerniagaanCalculation {
        return try {
            val o = org.json.JSONObject(s)
            ZakatPerniagaanCalculation(
                currentCashInBusiness = o.optDouble("cash", 25000000.0),
                inventoryValue = o.optDouble("inventory", 90000000.0),
                receivableCollectible = o.optDouble("receivable", 15000000.0),
                shortTermPayable = o.optDouble("payable", 10000000.0),
                goldPricePerGram = o.optDouble("goldPrice", 1350000.0),
                isLunarCalendar = o.optBoolean("isLunar", true)
            )
        } catch (_: Exception) {
            ZakatPerniagaanCalculation()
        }
    }

    private fun serializeZakatFitrah(c: ZakatFitrahFamilyCalculation): String {
        val o = org.json.JSONObject()
        o.put("members", c.familyMembersCount)
        o.put("riceKg", c.riceAmountPerPersonKg)
        o.put("ricePrice", c.ricePricePerKg)
        o.put("cashRate", c.cashRatePerPerson)
        o.put("isCash", c.isPaymentInCash)
        o.put("stapleFoodName", c.stapleFoodName)
        o.put("regionName", c.regionName)
        o.put("measurementUnit", c.measurementUnit)
        o.put("useLocalStaplePrice", c.useLocalStaplePrice)
        val depsArray = org.json.JSONArray()
        c.dependents.forEach { dep ->
            val dObj = org.json.JSONObject()
            dObj.put("id", dep.id)
            dObj.put("name", dep.name)
            dObj.put("relation", dep.relation)
            dObj.put("isIncluded", dep.isIncluded)
            depsArray.put(dObj)
        }
        o.put("dependents", depsArray)
        return o.toString()
    }

    private fun deserializeZakatFitrah(s: String): ZakatFitrahFamilyCalculation {
        return try {
            val o = org.json.JSONObject(s)
            val membersCount = o.optInt("members", 4)
            val depsList = mutableListOf<ZakatFitrahDependent>()
            val depsArr = o.optJSONArray("dependents")
            if (depsArr != null) {
                for (i in 0 until depsArr.length()) {
                    val dObj = depsArr.getJSONObject(i)
                    depsList.add(
                        ZakatFitrahDependent(
                            id = dObj.optString("id", "dep_$i"),
                            name = dObj.optString("name", "Anggota #${i + 1}"),
                            relation = dObj.optString("relation", "Anggota Keluarga"),
                            isIncluded = dObj.optBoolean("isIncluded", true)
                        )
                    )
                }
            }
            val finalDeps = if (depsList.isNotEmpty() || o.has("dependents")) {
                depsList
            } else {
                ZakatFitrahDefaults.defaultDependents(membersCount)
            }
            ZakatFitrahFamilyCalculation(
                familyMembersCount = membersCount,
                riceAmountPerPersonKg = o.optDouble("riceKg", 2.5),
                ricePricePerKg = o.optDouble("ricePrice", 15000.0),
                cashRatePerPerson = o.optDouble("cashRate", 45000.0),
                isPaymentInCash = o.optBoolean("isCash", true),
                stapleFoodName = o.optString("stapleFoodName", "Beras Medium (Lokal)"),
                regionName = o.optString("regionName", "Jawa & Nasional"),
                measurementUnit = o.optString("measurementUnit", "kg"),
                useLocalStaplePrice = o.optBoolean("useLocalStaplePrice", true),
                dependents = finalDeps
            )
        } catch (_: Exception) {
            ZakatFitrahFamilyCalculation()
        }
    }

    private fun serializeZakatMaal(c: ZakatMaalCalculation): String {
        val o = org.json.JSONObject()
        o.put("goldWeight", c.goldWeightGrams)
        o.put("goldPrice", c.goldPricePerGram)
        o.put("isLunar", c.isLunarCalendar)
        o.put("isHaul", c.isHaulFulfilled)
        o.put("haulStartDateMillis", c.haulStartDateMillis)
        return o.toString()
    }

    private fun deserializeZakatMaal(s: String, base: ZakatMaalCalculation): ZakatMaalCalculation {
        return try {
            val o = org.json.JSONObject(s)
            base.copy(
                goldWeightGrams = o.optDouble("goldWeight", base.goldWeightGrams),
                goldPricePerGram = o.optDouble("goldPrice", base.goldPricePerGram),
                isLunarCalendar = o.optBoolean("isLunar", base.isLunarCalendar),
                isHaulFulfilled = o.optBoolean("isHaul", base.isHaulFulfilled),
                haulStartDateMillis = o.optLong("haulStartDateMillis", base.haulStartDateMillis)
            )
        } catch (_: Exception) {
            base
        }
    }
}


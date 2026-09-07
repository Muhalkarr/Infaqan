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
import com.example.core.infaq.SedekahSubuhState
import com.example.core.infaq.SedekahSubuhStreakEngine
import com.example.core.qardh.QardhInstallment
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.receipt.ReceiptAttachment
import com.example.core.report.DocumentExporter
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
import com.example.core.zakat.ZakatFitrahFamilyCalculation
import com.example.core.zakat.ZakatPerniagaanCalculation
import com.example.core.zakat.ZakatProfesiCalculation
import com.example.core.directory.AmilInstitution
import com.example.core.directory.AmilBankAccount
import com.example.core.directory.AmilCategory
import com.example.core.directory.AmilDirectoryRepository
import com.example.core.shariah.ShariahRulesConfig
import com.example.core.shariah.ShariahRuling
import com.example.core.shariah.ShariahDefaultRulings
import com.example.core.auth.AmanahAuthManager
import com.example.core.auth.AmanahAuthState
import com.example.core.auth.AmanahUserProfile
import com.example.core.auth.AuthStatus
import com.example.core.database.AmanahDatabase
import com.example.core.database.AmanahDummyDataGenerator
import com.example.core.database.EntityMappers
import com.example.core.database.SettingsEntity
import com.example.core.datastore.DataStoreManager
import com.example.core.repository.AmanahRepository
import com.example.core.repository.AmanahRepositoryImpl
import com.example.core.sync.FirestoreSyncEngine
import com.example.core.sync.SyncState
import com.example.core.sync.SyncStateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

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
    val zakatProfesi: ZakatProfesiCalculation = ZakatProfesiCalculation(),
    val zakatPerniagaan: ZakatPerniagaanCalculation = ZakatPerniagaanCalculation(),
    val zakatFitrah: ZakatFitrahFamilyCalculation = ZakatFitrahFamilyCalculation(),
    val selectedGoalMode: FinancialGoalMode = FinancialGoalMode.BALANCED_50_30_20,
    val isDarkMode: Boolean = true,
    val isHighContrast: Boolean = false,
    val goldPricePerGram: Double = 1350000.0, // Rp 1.350.000 per gram
    val selectedHijriOffset: Int = 0,
    val securityConfig: SecurityConfig = SecurityConfig(),
    val userNameKasMukmin: String = "Kas Keluarga Mukmin",
    val startDayOfMonth: Int = 1,
    val initialLedgerDate: String = "01/01/2024",
    val fiscalCycleType: com.example.core.accounting.FiscalCycleType = com.example.core.accounting.FiscalCycleType.MONTHLY_SALARY_DATE,
    val initialLedgerBalance: Double = 0.0,
    val fiscalYearStartMonth: Int = 1,
    val primaryCurrencySymbol: String = "Rp",
    val defaultInfaqRate: Double = 0.05,
    val isAutoDeductInfaqEnabled: Boolean = true,
    val vaultMonthlyTarget: Double = 5000000.0,
    val sedekahSubuhTargetDays: Int = 40,
    val israfWarningThresholdPercent: Int = 80,
    val isStrictBudgetEnforced: Boolean = false,
    val autoExecuteRecurringEnabled: Boolean = true,
    val notifyOnRecurringDue: Boolean = true,
    val showDailyHadith: Boolean = true,
    val showQuickTutorial: Boolean = true,
    val amilInstitutions: List<AmilInstitution> = emptyList(),
    val uiScaleMode: com.example.ui.theme.UiScaleMode = com.example.ui.theme.UiScaleMode.DEFAULT,
    val uiScaleFactor: Float = 1.0f,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false
) {
    val dueRecurringCount: Int
        get() = recurringTransactions.count { it.isDue() }
    val nisabThreshold: Double
        get() = 85.0 * goldPricePerGram // 85 gram emas (~Rp 114.750.000)

    val spendingPatternAnalysis: SpendingPatternAnalysis by lazy {
        BudgetOptimizerEngine.analyzeAndOptimize(accounts, budgets, journalEntries, selectedGoalMode)
    }

    fun getAccount(id: String): Account? = accounts.firstOrNull { it.id == id }

    fun getWallet(id: String): WalletAccount? = wallets.firstOrNull { it.id == id }

    // Fast O(1) single-pass pre-calculated balance lookup
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
            val bal = if (acc.category == AccountCategory.ASSET || acc.category == AccountCategory.EXPENSE) {
                d - c
            } else {
                c - d
            }
            result[acc.id] = bal
        }
        result
    }

    fun getWalletBalance(walletId: String): Double {
        val wallet = getWallet(walletId) ?: return 0.0
        return getAccountBalance(wallet.linkedAccountId)
    }

    val totalWalletBalance: Double
        get() = wallets.sumOf { getWalletBalance(it.id) }

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
            cal.time = entry.gregorianDate
            if (cal.get(Calendar.MONTH) == curMonth && cal.get(Calendar.YEAR) == curYear) {
                for (line in entry.lines) {
                    if (line.debit > 0.0) {
                        map[line.accountId] = (map[line.accountId] ?: 0.0) + line.debit
                    }
                }
            }
        }
        map
    }

    /**
     * Hitung total pengeluaran untuk akun tertentu pada bulan dan tahun berjalan
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
            cal.time = entry.gregorianDate
            if (cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear) {
                for (line in entry.lines) {
                    if (line.accountId == accountId) {
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
        get() = getAccountBalance("acc_cash") + getAccountBalance("acc_bank") + getAccountBalance("acc_gold")

    val virtualInfaqVaultBalance: Double
        get() = getAccountBalance("acc_vault")

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
            .filter { it.accountId == "acc_vault" }
            .sumOf { it.debit }

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

    val isNisabReached: Boolean
        get() = totalAssets >= nisabThreshold

    val zakatObligationLunar: Double
        get() = if (isNisabReached) totalAssets * 0.025 else 0.0 // 2.500% Lunar (354 Hari)

    val zakatObligationSolar: Double
        get() = if (isNisabReached) totalAssets * 0.02577 else 0.0 // 2.577% Solar (365 Hari)
}

class AmanahLedgerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AmanahLedgerUiState())
    val uiState: StateFlow<AmanahLedgerUiState> = _uiState.asStateFlow()
    val appStateNotifier: AppStateNotifier = AppStateNotifier()

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

    fun activateGuideMode(topicIndex: Int, topicTitle: String) {
        activeGuideTopicIndex = topicIndex
        activeGuideTopicTitle = topicTitle
        isGuideModeActive = true
    }

    fun deactivateGuideMode() {
        isGuideModeActive = false
    }

    init {
        initDefaultData()
    }

    private fun initDefaultData() {
        val defaultAccounts = listOf(
            Account("acc_cash", "101", "Kas Utama / Dompet Tunai", AccountCategory.ASSET, "Uang kas fisik di dompet"),
            Account("acc_bank", "102", "Rekening Bank Syariah (BSI/Muamalat)", AccountCategory.ASSET, "Tabungan mudharabah/wadiah"),
            Account("acc_gold", "103", "Tabungan Emas Fisik", AccountCategory.ASSET, "Logam mulia batangan"),
            Account("acc_vault", "201", "Virtual Infaq Vault (Titipan Amanah)", AccountCategory.LIABILITY, "Hak Mustahiq yang wajib disalurkan"),
            Account("acc_debt", "202", "Hutang Qardh / Cicilan", AccountCategory.LIABILITY, "Kewajiban pihak ketiga"),
            Account("acc_equity", "301", "Modal Awal / Harta Bersih", AccountCategory.EQUITY, "Saldo awal harta"),
            Account("acc_salary", "401", "Kasab: Ujrah / Gaji Pokok", AccountCategory.INCOME_KASAB, "Penghasilan kerja aktif"),
            Account("acc_trade", "402", "Kasab: Margin Usaha Dagang", AccountCategory.INCOME_KASAB, "Laba tijarah halal"),
            Account("acc_gift", "451", "Non-Kasab: Hibah & Hadiah", AccountCategory.INCOME_NON_KASAB, "Pemberian sukarela keluarga/rekan"),
            Account("acc_rikaz", "452", "Non-Kasab: Rikaz / Temuan", AccountCategory.INCOME_NON_KASAB, "Harta karun/windfall wajib 20%"),
            Account("acc_syubhat", "453", "Non-Kasab: Karantina Syubhat", AccountCategory.INCOME_NON_KASAB, "Dana bunga bank / tidak jelas halal"),
            Account("acc_living", "501", "Biaya Hidup & Pangan", AccountCategory.EXPENSE, "Belanja pangan, konsumsi & dapur"),
            Account("acc_transport", "502", "Transportasi & Bensin", AccountCategory.EXPENSE, "Bahan bakar, ojek, tol & servis"),
            Account("acc_utility", "503", "Tagihan Listrik, Air & Pulsa", AccountCategory.EXPENSE, "PLN, PDAM, pulsa & paket internet"),
            Account("acc_education", "504", "Pendidikan & Majelis Dakwah", AccountCategory.EXPENSE, "SPP sekolah, buku kajian & majelis ilmu"),
            Account("acc_health", "505", "Kesehatan & Pengobatan", AccountCategory.EXPENSE, "Obat, vitamin & medis keluarga"),
            Account("acc_other_exp", "506", "Pengeluaran Rutin Lainnya", AccountCategory.EXPENSE, "Kebutuhan operasional rumah tangga"),
            Account("acc_disbursed", "599", "Penyaluran Infaq Terbayar", AccountCategory.EXPENSE, "Realisasi distribusi ke mustahiq")
        )

        val defaultRules = listOf(
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

        appStateNotifier.notify(
            title = "Pemasukan Berhasil Dicatat",
            message = "Rezeki Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(grossAmount)} tercatat dengan alokasi infaq ${(customInfaqRate * 100).toInt()}%.",
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

        appStateNotifier.notify(
            title = "Pengeluaran Diperbarui",
            message = "Catatan pengeluaran '$cleanDesc' telah berhasil diubah.",
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

            state.copy(
                journalEntries = state.journalEntries.filterNot { it.id == entryId },
                infaqDistributions = updatedDistributions
            )
        }
        deleteEntryAsync(entryId)

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
        val hijri = HijriCalendarEngine.fromGregorian(date)
        val journalId = UUID.randomUUID().toString()
        val distId = "dist_${UUID.randomUUID().toString().take(8)}"
        val receiptNo = "INV-VAULT-${SimpleDateFormat("yyyyMMdd", Locale.US).format(date)}-${(1000..9999).random()}"

        val lines = listOf(
            // 1. Debit Virtual Vault (Kewajiban lunas)
            JournalLine(accountId = "acc_vault", debit = amount, credit = 0.0),
            // 2. Kredit Kas/Bank (Kas keluar ke mustahiq)
            JournalLine(accountId = fromAccountId, debit = 0.0, credit = amount)
        )

        val entry = JournalEntry(
            id = journalId,
            gregorianDate = date,
            hijriYear = hijri.year,
            hijriMonth = hijri.month,
            hijriDay = hijri.day,
            description = "Penyaluran Infaq: $recipientName (${asnafCategory.displayName})",
            transactionType = "INFAQ_PAYOUT",
            lines = lines
        )

        val distRecord = InfaqDistributionRecord(
            id = distId,
            amount = amount,
            recipientName = recipientName,
            asnafCategory = asnafCategory,
            distributionDate = date,
            hijriDateString = "${hijri.day} ${hijri.monthName} ${hijri.year} H",
            sourceAccountId = fromAccountId,
            programName = programName.ifBlank { "Penyaluran Tabarru'" },
            receiptNumber = receiptNo,
            notes = notes,
            isVerified = true
        )

        _uiState.update { state ->
            state.copy(
                journalEntries = listOf(entry) + state.journalEntries,
                infaqDistributions = listOf(distRecord) + state.infaqDistributions
            )
        }
        persistEntryAsync(entry)

        appStateNotifier.notify(
            title = "Penyaluran Infaq Berhasil",
            message = "Dana Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)} disalurkan ke $recipientName (${asnafCategory.displayName})",
            severity = NotificationSeverity.SUCCESS
        )
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

        appStateNotifier.notify(
            title = "Sedekah Subuh Tercatat",
            message = "Alhamdulillah, sedekah Rp ${java.text.NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)} tercatat. Streak hari ke-${updatedStreakState.currentStreak}!",
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
    }

    fun deleteBudget(budgetId: String) {
        _uiState.update { state ->
            state.copy(budgets = state.budgets.filterNot { it.id == budgetId })
        }
        deleteBudgetAsync(budgetId)
    }

    fun addRule(rule: InfaqRule) {
        _uiState.update { state ->
            state.copy(rules = state.rules + rule)
        }
    }

    fun deleteRule(id: String) {
        _uiState.update { state ->
            state.copy(rules = state.rules.filterNot { it.id == id })
        }
    }

    fun toggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        _uiState.update { state ->
            state.copy(isDarkMode = newMode)
        }
        viewModelScope.launch { repository?.saveDarkMode(newMode) }
    }

    fun setDarkMode(isDark: Boolean) {
        _uiState.update { state ->
            state.copy(isDarkMode = isDark)
        }
        viewModelScope.launch { repository?.saveDarkMode(isDark) }
    }

    fun toggleHighContrast() {
        val newContrast = !_uiState.value.isHighContrast
        _uiState.update { state ->
            state.copy(isHighContrast = newContrast)
        }
        viewModelScope.launch { repository?.saveHighContrast(newContrast) }
    }

    fun setHighContrast(isHigh: Boolean) {
        _uiState.update { state ->
            state.copy(isHighContrast = isHigh)
        }
        viewModelScope.launch { repository?.saveHighContrast(isHigh) }
    }

    fun setThemeMode(mode: com.example.ui.theme.AppThemeMode) {
        val (isDark, isHigh) = when (mode) {
            com.example.ui.theme.AppThemeMode.ELEGANT_DARK -> true to false
            com.example.ui.theme.AppThemeMode.LIGHT_MODE -> false to false
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT -> false to true
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK -> true to true
        }
        _uiState.update { state ->
            state.copy(isDarkMode = isDark, isHighContrast = isHigh)
        }
        viewModelScope.launch {
            repository?.saveDarkMode(isDark)
            repository?.saveHighContrast(isHigh)
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
    }

    fun deleteRecurringTransaction(id: String) {
        val target = _uiState.value.recurringTransactions.firstOrNull { it.id == id }
        _uiState.update { state ->
            state.copy(recurringTransactions = state.recurringTransactions.filterNot { it.id == id })
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
        _uiState.update { state ->
            val updated = state.recurringTransactions.map {
                if (it.id == id) it.copy(isActive = !it.isActive) else it
            }
            state.copy(recurringTransactions = updated)
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

        _uiState.update { state ->
            val updated = state.recurringTransactions.map {
                if (it.id == id) {
                    it.copy(
                        lastExecutedDate = date,
                        nextDueDate = nextDue
                    )
                } else it
            }
            state.copy(recurringTransactions = updated)
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
        if (newPrice > 0) {
            _uiState.update { state ->
                state.copy(goldPricePerGram = newPrice)
            }
        }
    }

    // ==========================================
    // SECURITY & APP LOCK MANAGEMENT
    // ==========================================

    private var lastBackgroundTimestamp: Long = 0L
    private var isSessionAuthenticated: Boolean = false

    fun onAppBackgrounded() {
        val current = _uiState.value.securityConfig
        if (!current.isPinEnabled) return
        lastBackgroundTimestamp = System.currentTimeMillis()
        if (current.autoLockInterval == AutoLockInterval.IMMEDIATE) {
            isSessionAuthenticated = false
            lockApp()
        }
    }

    fun onAppForegrounded() {
        val current = _uiState.value.securityConfig
        if (!current.isPinEnabled) return
        if (current.isAppLocked) return

        if (lastBackgroundTimestamp > 0L) {
            val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTimestamp) / 1000
            if (current.autoLockInterval == AutoLockInterval.IMMEDIATE) {
                isSessionAuthenticated = false
                lockApp()
            } else if (current.autoLockInterval != AutoLockInterval.NEVER && elapsedSeconds >= current.autoLockInterval.seconds) {
                isSessionAuthenticated = false
                lockApp()
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

    fun setUserNameKasMukmin(name: String) {
        _uiState.update { it.copy(userNameKasMukmin = name) }
    }

    fun setPrimaryCurrencySymbol(symbol: String) {
        _uiState.update { it.copy(primaryCurrencySymbol = symbol) }
    }

    fun setDefaultInfaqRate(rate: Double) {
        _uiState.update { it.copy(defaultInfaqRate = rate.coerceIn(0.01, 0.50)) }
    }

    fun setAutoDeductInfaqEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isAutoDeductInfaqEnabled = enabled) }
    }

    fun setVaultMonthlyTarget(target: Double) {
        _uiState.update { it.copy(vaultMonthlyTarget = target.coerceAtLeast(0.0)) }
    }

    fun setSedekahSubuhTargetDays(days: Int) {
        _uiState.update { it.copy(sedekahSubuhTargetDays = days.coerceIn(7, 365)) }
    }

    fun setGoldPrice(price: Double) {
        _uiState.update { it.copy(goldPricePerGram = price.coerceAtLeast(100000.0)) }
    }

    fun setHijriOffset(offset: Int) {
        _uiState.update { it.copy(selectedHijriOffset = offset.coerceIn(-3, 3)) }
    }

    fun setIsrafWarningThresholdPercent(percent: Int) {
        _uiState.update { it.copy(israfWarningThresholdPercent = percent.coerceIn(50, 100)) }
    }

    fun setStrictBudgetEnforced(enforced: Boolean) {
        _uiState.update { it.copy(isStrictBudgetEnforced = enforced) }
    }

    fun setAutoExecuteRecurringEnabled(enabled: Boolean) {
        _uiState.update { it.copy(autoExecuteRecurringEnabled = enabled) }
    }

    fun setNotifyOnRecurringDue(notify: Boolean) {
        _uiState.update { it.copy(notifyOnRecurringDue = notify) }
    }

    fun setShowDailyHadith(show: Boolean) {
        _uiState.update { it.copy(showDailyHadith = show) }
    }

    fun setShowQuickTutorial(show: Boolean) {
        _uiState.update { it.copy(showQuickTutorial = show) }
    }

    // ==========================================
    // MULTI-WALLET (PENGELOLAAN KANTONG REKENING)
    // ==========================================

    fun addWallet(wallet: WalletAccount) {
        _uiState.update { it.copy(wallets = it.wallets + wallet) }
        persistWalletAsync(wallet)
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
        appStateNotifier.notify(
            title = "Kantong Rekening Diperbarui",
            message = "Data rekening '${wallet.name}' telah disesuaikan.",
            severity = NotificationSeverity.SUCCESS
        )
    }

    fun deleteWallet(walletId: String) {
        val target = _uiState.value.wallets.firstOrNull { it.id == walletId }
        _uiState.update { state ->
            state.copy(wallets = state.wallets.filterNot { it.id == walletId })
        }
        deleteWalletAsync(walletId)
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
            state.copy(
                journalEntries = listOf(entry) + state.journalEntries,
                walletMutations = listOf(mutation) + state.walletMutations
            )
        }
        persistEntryAsync(entry)

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
        val wallet = _uiState.value.getWallet(sourceWalletId)
        val now = Date()

        val depositRecord = IbadahDepositRecord(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            amount = amount,
            depositDate = now,
            sourceWalletId = sourceWalletId,
            notes = notes,
            receipt = receipt
        )

        val newAccumulated = goal.currentAccumulated + amount
        val isNowCompleted = newAccumulated >= goal.targetAmount
        val updatedGoal = goal.copy(
            currentAccumulated = newAccumulated,
            isCompleted = isNowCompleted
        )

        _uiState.update { state ->
            state.copy(
                ibadahGoals = state.ibadahGoals.map { if (it.id == goalId) updatedGoal else it },
                ibadahDeposits = listOf(depositRecord) + state.ibadahDeposits
            )
        }
        persistGoalAsync(updatedGoal)

        val nf = java.text.NumberFormat.getNumberInstance(Locale("id", "ID"))
        appStateNotifier.notify(
            title = if (isNowCompleted) "Alhamdulillah! Target Tercapai" else "Setoran Ibadah Tercatat",
            message = "Setoran Rp ${nf.format(amount)} untuk '${goal.title}' berhasil disimpan.${if (isNowCompleted) " Target tabungan ibadah telah genap 100%!" else ""}",
            severity = NotificationSeverity.SUCCESS
        )
    }

    // ==========================================
    // ZAKAT HUB (PROFESI, PERNIAGAAN, FITRAH)
    // ==========================================

    fun updateZakatProfesi(calc: ZakatProfesiCalculation) {
        _uiState.update { it.copy(zakatProfesi = calc) }
    }

    fun updateZakatPerniagaan(calc: ZakatPerniagaanCalculation) {
        _uiState.update { it.copy(zakatPerniagaan = calc) }
    }

    fun updateZakatFitrah(calc: ZakatFitrahFamilyCalculation) {
        _uiState.update { it.copy(zakatFitrah = calc) }
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

    fun generateBackupPackage(password: String = ""): String {
        val state = _uiState.value
        return BackupEngine.createBackupPackage(
            journalEntries = state.journalEntries,
            wallets = state.wallets,
            goals = state.ibadahGoals,
            budgets = state.budgets,
            rules = state.rules,
            recurring = state.recurringTransactions,
            sedekahSubuhState = state.sedekahSubuhState,
            password = password
        )
    }

    fun restoreBackupPackage(backupPackage: String, password: String = ""): RestoreResult {
        val result = BackupEngine.parseAndRestore(backupPackage, password)
        if (result is RestoreResult.Success) {
            _uiState.update { state ->
                state.copy(
                    journalEntries = result.restoredEntries,
                    wallets = if (result.restoredWallets.isNotEmpty()) result.restoredWallets else state.wallets,
                    ibadahGoals = if (result.restoredGoals.isNotEmpty()) result.restoredGoals else state.ibadahGoals,
                    budgets = if (result.restoredBudgets.isNotEmpty()) result.restoredBudgets else state.budgets,
                    sedekahSubuhState = result.restoredSedekahState
                )
            }
            appStateNotifier.notify(
                title = "Pemulihan Data Sukses",
                message = "Berhasil memulihkan ${result.restoredEntries.size} transaksi buku kas & ${result.restoredWallets.size} kantong rekening.",
                severity = NotificationSeverity.SUCCESS
            )
        } else if (result is RestoreResult.Failure) {
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
                }
                repository?.clearAllTransactions()
                repository?.clearAllBudgets()
                repository?.clearAllWallets()
                repository?.clearAllGoals()
                repository?.clearAllQardh()
                repository?.clearSedekahSubuh()
                repository?.saveCustomSetting("user_has_cleared_dummy_data", "true")
            } catch (e: Exception) {
                android.util.Log.e("AmanahVM", "Error clearing dummy data: ${e.message}")
            }

            _uiState.update { current ->
                current.copy(
                    journalEntries = emptyList(),
                    wallets = emptyList(),
                    budgets = emptyList(),
                    ibadahGoals = emptyList(),
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
                zakatFitrah = dummyZakatFitrah
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
            } catch (e: Exception) {
                android.util.Log.e("AmanahVM", "Error saving dummy data to Room: ${e.message}")
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

    fun recordQardhInstallment(
        qardhId: String,
        amount: Double,
        walletId: String,
        note: String,
        receipt: ReceiptAttachment?
    ) {
        _uiState.update { current ->
            val updatedList = current.qardhRecords.map { q ->
                if (q.id == qardhId) {
                    val installment = QardhInstallment(
                        qardhId = qardhId,
                        amount = amount,
                        fromWalletId = walletId,
                        note = note,
                        receipt = receipt
                    )
                    val newRemaining = (q.remainingAmount - amount).coerceAtLeast(0.0)
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
            current.copy(qardhRecords = updatedList)
        }
        val updatedQ = _uiState.value.qardhRecords.firstOrNull { it.id == qardhId }
        if (updatedQ != null) persistQardhAsync(updatedQ)

        appStateNotifier.notify(
            title = "Pembayaran Cicilan Qardh",
            message = "Pembayaran cicilan sebesar Rp ${amount.toLong()} berhasil dicatat.",
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

    private var roomDatabase: AmanahDatabase? = null
    private var syncEngine: FirestoreSyncEngine? = null
    private var authManager: AmanahAuthManager? = null
    private var repository: AmanahRepository? = null

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
            }
        }
    }

    fun initContextDependencies(context: android.content.Context) {
        if (roomDatabase == null) {
            val db = AmanahDatabase.getDatabase(context)
            roomDatabase = db
            val se = FirestoreSyncEngine(context, db)
            syncEngine = se
            val am = AmanahAuthManager(context)
            authManager = am
            val repo = AmanahRepositoryImpl(db, DataStoreManager(context))
            repository = repo

            viewModelScope.launch {
                se.syncState.collect { s -> _syncState.value = s }
            }
            viewModelScope.launch {
                am.authState.collect { a -> _authState.value = a }
            }
            viewModelScope.launch {
                repo.userPreferencesFlow.collect { prefs ->
                    _uiState.update { current ->
                        val shouldLock = prefs.securityConfig.isPinEnabled && !isSessionAuthenticated
                        current.copy(
                            isDarkMode = prefs.isDarkMode,
                            isHighContrast = prefs.isHighContrast,
                            goldPricePerGram = prefs.goldPricePerGram,
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
                            autoExecuteRecurringEnabled = prefs.autoExecuteRecurringEnabled,
                            notifyOnRecurringDue = prefs.notifyOnRecurringDue,
                            showDailyHadith = prefs.showDailyHadith,
                            showQuickTutorial = prefs.showQuickTutorial,
                            selectedGoalMode = prefs.selectedGoalMode,
                            uiScaleMode = prefs.uiScaleMode,
                            uiScaleFactor = prefs.uiScaleFactor,
                            securityConfig = current.securityConfig.copy(
                                isPinEnabled = prefs.securityConfig.isPinEnabled,
                                pinHash = prefs.securityConfig.pinHash,
                                isBiometricEnabled = prefs.securityConfig.isBiometricEnabled,
                                isMaskBalance = prefs.securityConfig.isMaskBalance,
                                maskBalanceByDefault = prefs.securityConfig.maskBalanceByDefault,
                                isScreenshotProtected = prefs.securityConfig.isScreenshotProtected,
                                securityQuestion = prefs.securityConfig.securityQuestion,
                                securityAnswerHash = prefs.securityConfig.securityAnswerHash,
                                autoLockInterval = prefs.securityConfig.autoLockInterval,
                                isAppLocked = shouldLock
                            )
                        )
                    }
                }
            }

            // Load initial data from Room if available
            viewModelScope.launch {
                try {
                    val isCleared = db.settingsDao().getSettingByKey("user_has_cleared_dummy_data")?.value == "true"
                    val dummyVersion = db.settingsDao().getSettingByKey("dummy_data_version")?.value

                    if (!isCleared && dummyVersion != "v2_proportional") {
                        // Refresh dummy data to ensure proportional Sharia Maqashid budget 50/30/10/10
                        db.budgetDao().clearAll()
                        db.journalDao().clearAll()
                        val freshEntries = AmanahDummyDataGenerator.getSampleJournalEntries()
                        val freshBudgets = AmanahDummyDataGenerator.getSampleBudgets()
                        db.journalDao().insertAll(freshEntries.map { EntityMappers.toEntity(it) })
                        db.budgetDao().insertAll(freshBudgets.map { EntityMappers.toEntity(it) })
                        db.settingsDao().insertOrUpdate(SettingsEntity("dummy_data_version", "v2_proportional"))

                        val localWallets = db.walletDao().getAllWallets().map { EntityMappers.toDomain(it) }
                        val localGoals = db.ibadahGoalDao().getAllGoals().map { EntityMappers.toDomain(it) }
                        val localQardh = db.qardhDao().getAllRecords().map { EntityMappers.toDomain(it) }
                        val localSedekah = repository?.getSedekahSubuh()

                        _uiState.update { current ->
                            current.copy(
                                journalEntries = freshEntries,
                                budgets = freshBudgets,
                                wallets = if (localWallets.isNotEmpty()) localWallets else current.wallets,
                                ibadahGoals = if (localGoals.isNotEmpty()) localGoals else current.ibadahGoals,
                                qardhRecords = if (localQardh.isNotEmpty()) localQardh else current.qardhRecords,
                                sedekahSubuhState = localSedekah ?: current.sedekahSubuhState
                            )
                        }
                    } else {
                        val localEntries = db.journalDao().getAllEntries().map { EntityMappers.toDomain(it) }
                        val localWallets = db.walletDao().getAllWallets().map { EntityMappers.toDomain(it) }
                        val localGoals = db.ibadahGoalDao().getAllGoals().map { EntityMappers.toDomain(it) }
                        val localQardh = db.qardhDao().getAllRecords().map { EntityMappers.toDomain(it) }
                        val localBudgets = db.budgetDao().getAllBudgets().map { EntityMappers.toDomain(it) }
                        val localSedekah = repository?.getSedekahSubuh()

                        if (isCleared) {
                            // Pengguna telah memilih menghapus data dummy
                            _uiState.update { current ->
                                current.copy(
                                    journalEntries = localEntries,
                                    wallets = localWallets,
                                    ibadahGoals = localGoals,
                                    qardhRecords = localQardh,
                                    budgets = localBudgets,
                                    recurringTransactions = emptyList(),
                                    infaqDistributions = emptyList(),
                                    sedekahSubuhState = localSedekah ?: SedekahSubuhStreakEngine.calculateStreak(emptyMap())
                                )
                            }
                        } else if (localEntries.isNotEmpty() || localWallets.isNotEmpty()) {
                            // Data telah tersimpan di Room
                            _uiState.update { current ->
                                current.copy(
                                    journalEntries = localEntries,
                                    wallets = localWallets,
                                    ibadahGoals = if (localGoals.isNotEmpty()) localGoals else current.ibadahGoals,
                                    qardhRecords = if (localQardh.isNotEmpty()) localQardh else current.qardhRecords,
                                    budgets = if (localBudgets.isNotEmpty()) localBudgets else current.budgets,
                                    sedekahSubuhState = localSedekah ?: current.sedekahSubuhState
                                )
                            }
                        } else {
                            // Pengguna baru pertama kali membuka aplikasi: Tanam data dummy ke Room
                            seedDummyDataToRoom(db)
                            db.settingsDao().insertOrUpdate(SettingsEntity("dummy_data_version", "v2_proportional"))
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AmanahVM", "Room init load: ${e.message}")
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

                db.journalDao().insertAll(dummyEntries.map { EntityMappers.toEntity(it) })
                db.walletDao().insertAll(dummyWallets.map { EntityMappers.toEntity(it) })
                db.budgetDao().insertAll(dummyBudgets.map { EntityMappers.toEntity(it) })
                db.ibadahGoalDao().insertAll(dummyGoals.map { EntityMappers.toEntity(it) })
                db.qardhDao().insertAll(dummyQardh.map { EntityMappers.toEntity(it) })
                repository?.saveSedekahSubuh(dummySedekah)
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
        _uiState.update { it.copy(initialLedgerBalance = balance.coerceAtLeast(0.0)) }
        viewModelScope.launch { repository?.saveInitialLedgerBalance(balance.coerceAtLeast(0.0)) }
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
        val hashed = SecurityConfig.hashString(pin)
        val defaultQuestion = _uiState.value.securityConfig.securityQuestion.ifBlank { "Nama kota kelahiran Anda?" }
        val defaultAnswerHash = if (_uiState.value.securityConfig.securityAnswerHash.isNotBlank()) {
            _uiState.value.securityConfig.securityAnswerHash
        } else {
            SecurityConfig.hashString("indonesia")
        }
        val newCfg = _uiState.value.securityConfig.copy(
            isPinEnabled = true,
            pinHash = hashed,
            securityQuestion = defaultQuestion,
            securityAnswerHash = defaultAnswerHash,
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
    }

    fun toggleBiometric(enabled: Boolean) {
        setBiometricEnabled(enabled)
    }

    fun resetAllSettingsToDefault() {
        viewModelScope.launch {
            repository?.clearAllPreferences()
            _uiState.update {
                it.copy(
                    isDarkMode = true,
                    isHighContrast = false,
                    goldPricePerGram = 1350000.0,
                    selectedHijriOffset = 0,
                    userNameKasMukmin = "Kas Keluarga Mukmin",
                    startDayOfMonth = 1,
                    primaryCurrencySymbol = "Rp",
                    defaultInfaqRate = 0.05,
                    isAutoDeductInfaqEnabled = true,
                    vaultMonthlyTarget = 5000000.0,
                    sedekahSubuhTargetDays = 40,
                    israfWarningThresholdPercent = 80,
                    isStrictBudgetEnforced = false,
                    autoExecuteRecurringEnabled = true,
                    notifyOnRecurringDue = true,
                    showDailyHadith = true,
                    showQuickTutorial = true,
                    securityConfig = SecurityConfig()
                )
            }
        }
    }


    fun triggerCloudSync() {
        val se = syncEngine ?: return
        val currentAuth = _authState.value
        val userId = if (currentAuth.status == AuthStatus.AUTHENTICATED && currentAuth.user.uid.isNotBlank()) {
            currentAuth.user.uid
        } else {
            "guest_local_user"
        }

        viewModelScope.launch {
            val state = _uiState.value
            val res = se.syncAll(
                userId = userId,
                localEntries = state.journalEntries,
                localWallets = state.wallets,
                localGoals = state.ibadahGoals,
                localQardh = state.qardhRecords,
                localBudgets = state.budgets,
                sedekahState = state.sedekahSubuhState,
                onRestoreFromCloud = { restoredEntries, restoredWallets, restoredGoals, restoredQardh, _, restoredSedekah ->
                    _uiState.update { cur ->
                        cur.copy(
                            journalEntries = restoredEntries,
                            wallets = restoredWallets,
                            ibadahGoals = restoredGoals,
                            qardhRecords = restoredQardh,
                            sedekahSubuhState = restoredSedekah
                        )
                    }
                }
            )
            if (res.isSuccess) {
                appStateNotifier.notify(
                    title = "Sinkronisasi Cloud Berhasil",
                    message = "Data keuangan telah dicadangkan secara aman ke Cloud Firestore & Room Database.",
                    severity = NotificationSeverity.SUCCESS
                )
            } else {
                appStateNotifier.notify(
                    title = "Sinkronisasi Offline",
                    message = "Data tersimpan aman di Database Room lokal (mode offline).",
                    severity = NotificationSeverity.INFO
                )
            }
        }
    }

    fun restoreDataFromCloud() {
        val se = syncEngine ?: return
        val currentAuth = _authState.value
        val userId = if (currentAuth.status == AuthStatus.AUTHENTICATED && currentAuth.user.uid.isNotBlank()) {
            currentAuth.user.uid
        } else {
            "guest_local_user"
        }

        viewModelScope.launch {
            val res = se.restoreFromCloud(
                userId = userId,
                onSuccess = { entries, wallets, goals, qardh, _, sedekah ->
                    _uiState.update { cur ->
                        cur.copy(
                            journalEntries = entries,
                            wallets = wallets,
                            ibadahGoals = goals,
                            qardhRecords = qardh,
                            sedekahSubuhState = sedekah
                        )
                    }
                    appStateNotifier.notify(
                        title = "Pemulihan Cloud Berhasil",
                        message = "Seluruh catatan keuangan, kantong, dan target ibadah berhasil dipulihkan dari Firestore.",
                        severity = NotificationSeverity.SUCCESS
                    )
                }
            )
            if (res.isFailure) {
                appStateNotifier.notify(
                    title = "Gagal Memulihkan",
                    message = res.exceptionOrNull()?.localizedMessage ?: "Belum ada data cadangan di akun ini.",
                    severity = NotificationSeverity.WARNING
                )
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        val am = authManager ?: return
        viewModelScope.launch {
            val res = am.signInWithEmailPassword(email, pass)
            if (res.isSuccess) {
                triggerCloudSync()
                appStateNotifier.notify(
                    title = "Login Berhasil",
                    message = "Ahlan wa Sahlan, ${res.getOrNull()?.displayName ?: "Mukmin"}.",
                    severity = NotificationSeverity.SUCCESS
                )
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        val am = authManager ?: return
        viewModelScope.launch {
            val res = am.signUpWithEmailPassword(email, pass, name)
            if (res.isSuccess) {
                triggerCloudSync()
                appStateNotifier.notify(
                    title = "Pendaftaran Berhasil",
                    message = "Akun Amanah berhasil dibuat. Data langsung disinkronkan ke Cloud.",
                    severity = NotificationSeverity.SUCCESS
                )
            }
        }
    }

    fun signInWithGoogle(clientId: String = "") {
        val am = authManager ?: return
        viewModelScope.launch {
            val res = am.signInWithGoogleCredential(clientId)
            if (res.isSuccess) {
                triggerCloudSync()
                appStateNotifier.notify(
                    title = "Google Sign-In Berhasil",
                    message = "Terhubung dengan akun Google: ${res.getOrNull()?.email}",
                    severity = NotificationSeverity.SUCCESS
                )
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
}

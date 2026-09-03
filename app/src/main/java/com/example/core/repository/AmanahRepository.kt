package com.example.core.repository

import com.example.core.accounting.JournalEntry
import com.example.core.budget.BudgetAllocation
import com.example.core.budget.FinancialGoalMode
import com.example.core.database.AmanahDatabase
import com.example.core.database.EntityMappers
import com.example.core.database.SettingsEntity
import com.example.core.datastore.AppUserPreferences
import com.example.core.datastore.DataStoreManager
import com.example.core.ibadah.IbadahGoal
import com.example.core.infaq.SedekahSubuhState
import com.example.core.qardh.QardhRecord
import com.example.core.security.SecurityConfig
import com.example.core.wallet.WalletAccount
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface AmanahRepository {
    // User Preferences / Settings via DataStore
    val userPreferencesFlow: Flow<AppUserPreferences>
    suspend fun saveDarkMode(isDarkMode: Boolean)
    suspend fun saveHighContrast(isHighContrast: Boolean)
    suspend fun saveGoldPrice(price: Double)
    suspend fun saveHijriOffset(offset: Int)
    suspend fun saveUserName(name: String)
    suspend fun saveStartDayOfMonth(day: Int)
    suspend fun saveInitialLedgerDate(date: String)
    suspend fun saveFiscalCycleType(cycleType: com.example.core.accounting.FiscalCycleType)
    suspend fun saveInitialLedgerBalance(balance: Double)
    suspend fun saveFiscalYearStartMonth(month: Int)
    suspend fun saveUiScale(mode: com.example.ui.theme.UiScaleMode, factor: Float)
    suspend fun saveCurrencySymbol(symbol: String)
    suspend fun saveDefaultInfaqRate(rate: Double)
    suspend fun saveAutoDeductInfaq(enabled: Boolean)
    suspend fun saveVaultMonthlyTarget(target: Double)
    suspend fun saveSedekahSubuhTargetDays(days: Int)
    suspend fun saveIsrafWarningThreshold(percent: Int)
    suspend fun saveStrictBudgetEnforced(enforced: Boolean)
    suspend fun saveAutoExecuteRecurring(enabled: Boolean)
    suspend fun saveNotifyOnRecurringDue(enabled: Boolean)
    suspend fun saveShowDailyHadith(show: Boolean)
    suspend fun saveShowQuickTutorial(show: Boolean)
    suspend fun saveSelectedGoalMode(mode: FinancialGoalMode)
    suspend fun saveSecurityConfig(config: SecurityConfig)
    suspend fun saveMaskBalance(isMasked: Boolean)
    suspend fun clearAllPreferences()

    // Room Persistent Transactions / Journal Entries
    fun getAllTransactionsFlow(): Flow<List<JournalEntry>>
    suspend fun getAllTransactions(): List<JournalEntry>
    suspend fun saveTransaction(entry: JournalEntry)
    suspend fun saveAllTransactions(entries: List<JournalEntry>)
    suspend fun deleteTransaction(id: String)
    suspend fun clearAllTransactions()

    // Room Persistent Budget Allocations
    fun getAllBudgetsFlow(): Flow<List<BudgetAllocation>>
    suspend fun getAllBudgets(): List<BudgetAllocation>
    suspend fun saveBudget(budget: BudgetAllocation)
    suspend fun saveAllBudgets(budgets: List<BudgetAllocation>)
    suspend fun deleteBudget(id: String)
    suspend fun clearAllBudgets()

    // Room Persistent Wallets
    fun getAllWalletsFlow(): Flow<List<WalletAccount>>
    suspend fun getAllWallets(): List<WalletAccount>
    suspend fun saveWallet(wallet: WalletAccount)
    suspend fun saveAllWallets(wallets: List<WalletAccount>)
    suspend fun deleteWallet(id: String)
    suspend fun clearAllWallets()

    // Room Persistent Ibadah Goals
    fun getAllGoalsFlow(): Flow<List<IbadahGoal>>
    suspend fun getAllGoals(): List<IbadahGoal>
    suspend fun saveGoal(goal: IbadahGoal)
    suspend fun saveAllGoals(goals: List<IbadahGoal>)
    suspend fun deleteGoal(id: String)
    suspend fun clearAllGoals()

    // Room Persistent Qardh Records
    fun getAllQardhFlow(): Flow<List<QardhRecord>>
    suspend fun getAllQardh(): List<QardhRecord>
    suspend fun saveQardh(record: QardhRecord)
    suspend fun saveAllQardh(records: List<QardhRecord>)
    suspend fun deleteQardh(id: String)
    suspend fun clearAllQardh()

    // Room Persistent Sedekah Subuh
    fun getSedekahSubuhFlow(): Flow<SedekahSubuhState?>
    suspend fun getSedekahSubuh(): SedekahSubuhState?
    suspend fun saveSedekahSubuh(state: SedekahSubuhState)
    suspend fun clearSedekahSubuh()

    // Reset All Room Entities
    suspend fun resetAllAppData()

    // Room Settings Key-Value Store
    fun getAllCustomSettingsFlow(): Flow<List<SettingsEntity>>
    suspend fun saveCustomSetting(key: String, value: String)
    suspend fun getCustomSetting(key: String): String?
}

class AmanahRepositoryImpl(
    private val database: AmanahDatabase,
    private val dataStoreManager: DataStoreManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AmanahRepository {

    override val userPreferencesFlow: Flow<AppUserPreferences> =
        dataStoreManager.userPreferencesFlow.flowOn(ioDispatcher)

    override suspend fun saveDarkMode(isDarkMode: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveDarkMode(isDarkMode)
        database.settingsDao().insertOrUpdate(SettingsEntity("is_dark_mode", isDarkMode.toString()))
    }

    override suspend fun saveHighContrast(isHighContrast: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveHighContrast(isHighContrast)
        database.settingsDao().insertOrUpdate(SettingsEntity("is_high_contrast", isHighContrast.toString()))
    }

    override suspend fun saveGoldPrice(price: Double) = withContext(ioDispatcher) {
        dataStoreManager.saveGoldPrice(price)
        database.settingsDao().insertOrUpdate(SettingsEntity("gold_price_per_gram", price.toString()))
    }

    override suspend fun saveHijriOffset(offset: Int) = withContext(ioDispatcher) {
        dataStoreManager.saveHijriOffset(offset)
        database.settingsDao().insertOrUpdate(SettingsEntity("selected_hijri_offset", offset.toString()))
    }

    override suspend fun saveUserName(name: String) = withContext(ioDispatcher) {
        dataStoreManager.saveUserName(name)
        database.settingsDao().insertOrUpdate(SettingsEntity("user_name_kas_mukmin", name))
    }

    override suspend fun saveStartDayOfMonth(day: Int) = withContext(ioDispatcher) {
        dataStoreManager.saveStartDayOfMonth(day)
        database.settingsDao().insertOrUpdate(SettingsEntity("start_day_of_month", day.toString()))
    }

    override suspend fun saveInitialLedgerDate(date: String) = withContext(ioDispatcher) {
        dataStoreManager.saveInitialLedgerDate(date)
        database.settingsDao().insertOrUpdate(SettingsEntity("initial_ledger_date", date))
    }

    override suspend fun saveFiscalCycleType(cycleType: com.example.core.accounting.FiscalCycleType) = withContext(ioDispatcher) {
        dataStoreManager.saveFiscalCycleType(cycleType)
        database.settingsDao().insertOrUpdate(SettingsEntity("fiscal_cycle_type", cycleType.name))
    }

    override suspend fun saveInitialLedgerBalance(balance: Double) = withContext(ioDispatcher) {
        dataStoreManager.saveInitialLedgerBalance(balance)
        database.settingsDao().insertOrUpdate(SettingsEntity("initial_ledger_balance", balance.toString()))
    }

    override suspend fun saveFiscalYearStartMonth(month: Int) = withContext(ioDispatcher) {
        dataStoreManager.saveFiscalYearStartMonth(month)
        database.settingsDao().insertOrUpdate(SettingsEntity("fiscal_year_start_month", month.toString()))
    }

    override suspend fun saveUiScale(mode: com.example.ui.theme.UiScaleMode, factor: Float) = withContext(ioDispatcher) {
        dataStoreManager.saveUiScale(mode, factor)
        database.settingsDao().insertOrUpdate(SettingsEntity("ui_scale_mode", mode.name))
        database.settingsDao().insertOrUpdate(SettingsEntity("ui_scale_factor", factor.toString()))
    }

    override suspend fun saveCurrencySymbol(symbol: String) = withContext(ioDispatcher) {
        dataStoreManager.saveCurrencySymbol(symbol)
        database.settingsDao().insertOrUpdate(SettingsEntity("primary_currency_symbol", symbol))
    }

    override suspend fun saveDefaultInfaqRate(rate: Double) = withContext(ioDispatcher) {
        dataStoreManager.saveDefaultInfaqRate(rate)
        database.settingsDao().insertOrUpdate(SettingsEntity("default_infaq_rate", rate.toString()))
    }

    override suspend fun saveAutoDeductInfaq(enabled: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveAutoDeductInfaq(enabled)
        database.settingsDao().insertOrUpdate(SettingsEntity("is_auto_deduct_infaq_enabled", enabled.toString()))
    }

    override suspend fun saveVaultMonthlyTarget(target: Double) = withContext(ioDispatcher) {
        dataStoreManager.saveVaultMonthlyTarget(target)
        database.settingsDao().insertOrUpdate(SettingsEntity("vault_monthly_target", target.toString()))
    }

    override suspend fun saveSedekahSubuhTargetDays(days: Int) = withContext(ioDispatcher) {
        dataStoreManager.saveSedekahSubuhTargetDays(days)
        database.settingsDao().insertOrUpdate(SettingsEntity("sedekah_subuh_target_days", days.toString()))
    }

    override suspend fun saveIsrafWarningThreshold(percent: Int) = withContext(ioDispatcher) {
        dataStoreManager.saveIsrafWarningThreshold(percent)
        database.settingsDao().insertOrUpdate(SettingsEntity("israf_warning_threshold_percent", percent.toString()))
    }

    override suspend fun saveStrictBudgetEnforced(enforced: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveStrictBudgetEnforced(enforced)
        database.settingsDao().insertOrUpdate(SettingsEntity("is_strict_budget_enforced", enforced.toString()))
    }

    override suspend fun saveAutoExecuteRecurring(enabled: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveAutoExecuteRecurring(enabled)
        database.settingsDao().insertOrUpdate(SettingsEntity("auto_execute_recurring_enabled", enabled.toString()))
    }

    override suspend fun saveNotifyOnRecurringDue(enabled: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveNotifyOnRecurringDue(enabled)
        database.settingsDao().insertOrUpdate(SettingsEntity("notify_on_recurring_due", enabled.toString()))
    }

    override suspend fun saveShowDailyHadith(show: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveShowDailyHadith(show)
        database.settingsDao().insertOrUpdate(SettingsEntity("show_daily_hadith", show.toString()))
    }

    override suspend fun saveShowQuickTutorial(show: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveShowQuickTutorial(show)
        database.settingsDao().insertOrUpdate(SettingsEntity("show_quick_tutorial", show.toString()))
    }

    override suspend fun saveSelectedGoalMode(mode: FinancialGoalMode) = withContext(ioDispatcher) {
        dataStoreManager.saveSelectedGoalMode(mode)
        database.settingsDao().insertOrUpdate(SettingsEntity("selected_goal_mode", mode.name))
    }

    override suspend fun saveSecurityConfig(config: SecurityConfig) = withContext(ioDispatcher) {
        dataStoreManager.saveSecurityConfig(config)
        database.settingsDao().insertOrUpdate(SettingsEntity("security_is_pin_enabled", config.isPinEnabled.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("security_pin_hash", config.pinHash))
        database.settingsDao().insertOrUpdate(SettingsEntity("security_is_biometric_enabled", config.isBiometricEnabled.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("security_is_mask_balance", config.isMaskBalance.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("security_auto_lock_interval", config.autoLockInterval.name))
    }

    override suspend fun saveMaskBalance(isMasked: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveMaskBalance(isMasked)
    }

    override suspend fun clearAllPreferences() = withContext(ioDispatcher) {
        dataStoreManager.clearAllPreferences()
        database.settingsDao().clearAll()
    }

    // Transactions
    override fun getAllTransactionsFlow(): Flow<List<JournalEntry>> =
        database.journalDao().getAllEntriesFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllTransactions(): List<JournalEntry> = withContext(ioDispatcher) {
        database.journalDao().getAllEntries().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveTransaction(entry: JournalEntry) = withContext(ioDispatcher) {
        database.journalDao().insertOrUpdate(EntityMappers.toEntity(entry))
    }

    override suspend fun saveAllTransactions(entries: List<JournalEntry>) = withContext(ioDispatcher) {
        database.journalDao().insertAll(entries.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteTransaction(id: String) = withContext(ioDispatcher) {
        database.journalDao().deleteById(id)
    }

    override suspend fun clearAllTransactions() = withContext(ioDispatcher) {
        database.journalDao().clearAll()
    }

    // Budgets
    override fun getAllBudgetsFlow(): Flow<List<BudgetAllocation>> =
        database.budgetDao().getAllBudgetsFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllBudgets(): List<BudgetAllocation> = withContext(ioDispatcher) {
        database.budgetDao().getAllBudgets().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveBudget(budget: BudgetAllocation) = withContext(ioDispatcher) {
        database.budgetDao().insertOrUpdate(EntityMappers.toEntity(budget))
    }

    override suspend fun saveAllBudgets(budgets: List<BudgetAllocation>) = withContext(ioDispatcher) {
        database.budgetDao().insertAll(budgets.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteBudget(id: String) = withContext(ioDispatcher) {
        database.budgetDao().deleteById(id)
    }

    override suspend fun clearAllBudgets() = withContext(ioDispatcher) {
        database.budgetDao().clearAll()
    }

    // Wallets
    override fun getAllWalletsFlow(): Flow<List<WalletAccount>> =
        database.walletDao().getAllWalletsFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllWallets(): List<WalletAccount> = withContext(ioDispatcher) {
        database.walletDao().getAllWallets().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveWallet(wallet: WalletAccount) = withContext(ioDispatcher) {
        database.walletDao().insertOrUpdate(EntityMappers.toEntity(wallet))
    }

    override suspend fun saveAllWallets(wallets: List<WalletAccount>) = withContext(ioDispatcher) {
        database.walletDao().insertAll(wallets.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteWallet(id: String) = withContext(ioDispatcher) {
        database.walletDao().deleteById(id)
    }

    override suspend fun clearAllWallets() = withContext(ioDispatcher) {
        database.walletDao().clearAll()
    }

    // Goals
    override fun getAllGoalsFlow(): Flow<List<IbadahGoal>> =
        database.ibadahGoalDao().getAllGoalsFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllGoals(): List<IbadahGoal> = withContext(ioDispatcher) {
        database.ibadahGoalDao().getAllGoals().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveGoal(goal: IbadahGoal) = withContext(ioDispatcher) {
        database.ibadahGoalDao().insertOrUpdate(EntityMappers.toEntity(goal))
    }

    override suspend fun saveAllGoals(goals: List<IbadahGoal>) = withContext(ioDispatcher) {
        database.ibadahGoalDao().insertAll(goals.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteGoal(id: String) = withContext(ioDispatcher) {
        database.ibadahGoalDao().deleteById(id)
    }

    override suspend fun clearAllGoals() = withContext(ioDispatcher) {
        database.ibadahGoalDao().clearAll()
    }

    // Qardh
    override fun getAllQardhFlow(): Flow<List<QardhRecord>> =
        database.qardhDao().getAllRecordsFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllQardh(): List<QardhRecord> = withContext(ioDispatcher) {
        database.qardhDao().getAllRecords().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveQardh(record: QardhRecord) = withContext(ioDispatcher) {
        database.qardhDao().insertOrUpdate(EntityMappers.toEntity(record))
    }

    override suspend fun saveAllQardh(records: List<QardhRecord>) = withContext(ioDispatcher) {
        database.qardhDao().insertAll(records.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteQardh(id: String) = withContext(ioDispatcher) {
        database.qardhDao().deleteById(id)
    }

    override suspend fun clearAllQardh() = withContext(ioDispatcher) {
        database.qardhDao().clearAll()
    }

    // Sedekah Subuh
    override fun getSedekahSubuhFlow(): Flow<SedekahSubuhState?> =
        database.sedekahSubuhDao().getSedekahStateFlow().map { entity ->
            entity?.let {
                SedekahSubuhState(
                    currentStreak = it.currentStreak,
                    longestStreak = it.longestStreak,
                    totalContributions = it.totalContributions,
                    totalDaysGiven = it.totalDaysGiven,
                    lastContributionDate = if (it.lastContributionDateMillis > 0) java.util.Date(it.lastContributionDateMillis) else null
                )
            }
        }.flowOn(ioDispatcher)

    override suspend fun getSedekahSubuh(): SedekahSubuhState? = withContext(ioDispatcher) {
        database.sedekahSubuhDao().getSedekahState()?.let {
            SedekahSubuhState(
                currentStreak = it.currentStreak,
                longestStreak = it.longestStreak,
                totalContributions = it.totalContributions,
                totalDaysGiven = it.totalDaysGiven,
                lastContributionDate = if (it.lastContributionDateMillis > 0) java.util.Date(it.lastContributionDateMillis) else null
            )
        }
    }

    override suspend fun saveSedekahSubuh(state: SedekahSubuhState) = withContext(ioDispatcher) {
        database.sedekahSubuhDao().insertOrUpdate(
            com.example.core.database.SedekahSubuhEntity(
                id = 1,
                currentStreak = state.currentStreak,
                longestStreak = state.longestStreak,
                totalContributions = state.totalContributions,
                totalDaysGiven = state.totalDaysGiven,
                lastContributionDateMillis = state.lastContributionDate?.time ?: 0L,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearSedekahSubuh() = withContext(ioDispatcher) {
        database.sedekahSubuhDao().clearAll()
    }

    override suspend fun resetAllAppData() = withContext(ioDispatcher) {
        database.journalDao().clearAll()
        database.budgetDao().clearAll()
        database.walletDao().clearAll()
        database.ibadahGoalDao().clearAll()
        database.qardhDao().clearAll()
        database.sedekahSubuhDao().clearAll()
    }

    // Settings Dao Key-Value
    override fun getAllCustomSettingsFlow(): Flow<List<SettingsEntity>> =
        database.settingsDao().getAllSettingsFlow().flowOn(ioDispatcher)

    override suspend fun saveCustomSetting(key: String, value: String) = withContext(ioDispatcher) {
        database.settingsDao().insertOrUpdate(SettingsEntity(key, value))
    }

    override suspend fun getCustomSetting(key: String): String? = withContext(ioDispatcher) {
        database.settingsDao().getSettingByKey(key)?.value
    }
}

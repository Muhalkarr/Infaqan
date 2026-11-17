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
import com.example.core.scheduler.RecurringTransaction
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.InfaqDistributionRecord
import com.example.core.infaq.InfaqVaultReminderConfig
import com.example.core.report.PdfEditorialConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface AmanahRepository {
    // User Preferences / Settings via DataStore
    val userPreferencesFlow: Flow<AppUserPreferences>
    suspend fun saveThemeMode(mode: com.example.ui.theme.AppThemeMode)
    suspend fun saveDarkMode(isDarkMode: Boolean)
    suspend fun saveHighContrast(isHighContrast: Boolean)
    suspend fun saveGoldPrice(price: Double)
    suspend fun saveGoldPriceWithMetadata(price: Double, source: String = "Antam / Standar BAZNAS", timestampMillis: Long = System.currentTimeMillis())
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
    suspend fun saveDeficitProtectionEnabled(enabled: Boolean)
    suspend fun saveAutoExecuteRecurring(enabled: Boolean)
    suspend fun saveNotifyOnRecurringDue(enabled: Boolean)
    suspend fun saveShowDailyHadith(show: Boolean)
    suspend fun saveShowQuickTutorial(show: Boolean)
    suspend fun saveSelectedGoalMode(mode: FinancialGoalMode)
    suspend fun saveSecurityConfig(config: SecurityConfig)
    suspend fun saveMaskBalance(isMasked: Boolean)
    suspend fun saveShakeFeedbackEnabled(enabled: Boolean)
    suspend fun clearAllPreferences()

    // Room Persistent Transactions / Journal Entries
    fun getAllTransactionsFlow(): Flow<List<JournalEntry>>
    fun getPagedTransactionsFlow(limit: Int, offset: Int): Flow<List<JournalEntry>>
    suspend fun getAllTransactions(): List<JournalEntry>
    suspend fun getPagedTransactions(limit: Int, offset: Int): List<JournalEntry>
    suspend fun getTransactionsCount(): Int
    fun getRecentTransactionsFlow(startMillis: Long): Flow<List<JournalEntry>>
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

    // Shariah Rulings & Rules Engine
    fun getAllRulingsFlow(): Flow<List<com.example.core.shariah.ShariahRuling>>
    fun getActiveRulingsFlow(): Flow<List<com.example.core.shariah.ShariahRuling>>
    suspend fun saveRuling(ruling: com.example.core.shariah.ShariahRuling)
    suspend fun saveAllRulings(rulings: List<com.example.core.shariah.ShariahRuling>)
    suspend fun deleteRuling(id: String)
    suspend fun clearCustomRulings()
    suspend fun saveShariahRulesConfig(config: com.example.core.shariah.ShariahRulesConfig)
    suspend fun getShariahRulesConfig(): com.example.core.shariah.ShariahRulesConfig?
    suspend fun getAllShariahRulings(): List<com.example.core.shariah.ShariahRuling>
    suspend fun saveShariahRuling(ruling: com.example.core.shariah.ShariahRuling)
    suspend fun deleteShariahRuling(id: String)
    suspend fun getShariahConfig(): com.example.core.shariah.ShariahRulesConfig?
    suspend fun saveShariahConfig(config: com.example.core.shariah.ShariahRulesConfig)

    // Recurring Transactions
    fun getAllRecurringTransactionsFlow(): Flow<List<RecurringTransaction>>
    suspend fun getAllRecurringTransactions(): List<RecurringTransaction>
    suspend fun saveRecurringTransaction(transaction: RecurringTransaction)
    suspend fun saveAllRecurringTransactions(transactions: List<RecurringTransaction>)
    suspend fun deleteRecurringTransaction(id: String)
    suspend fun clearAllRecurringTransactions()

    // Infaq Rules Engine
    fun getAllInfaqRulesFlow(): Flow<List<InfaqRule>>
    suspend fun getAllInfaqRules(): List<InfaqRule>
    suspend fun saveInfaqRule(rule: InfaqRule)
    suspend fun saveAllInfaqRules(rules: List<InfaqRule>)
    suspend fun deleteInfaqRule(id: String)
    suspend fun clearAllInfaqRules()

    // Infaq Vault Distributions
    fun getAllInfaqDistributionsFlow(): Flow<List<InfaqDistributionRecord>>
    suspend fun getAllInfaqDistributions(): List<InfaqDistributionRecord>
    suspend fun saveInfaqDistribution(record: InfaqDistributionRecord)
    suspend fun saveAllInfaqDistributions(records: List<InfaqDistributionRecord>)
    suspend fun deleteInfaqDistribution(id: String)
    suspend fun clearAllInfaqDistributions()
    suspend fun markGuideCompleted(guideTitle: String)
    suspend fun setOnboardingCompleted()
    suspend fun saveLastBackupTime(timestampMillis: Long)
    suspend fun saveBackupNudgeDismissed(dismissed: Boolean)
    suspend fun saveAppDisplayMode(mode: com.example.core.datastore.AppDisplayMode)
    suspend fun saveFeatureVisibilityConfig(config: com.example.core.feature.AppFeatureVisibilityConfig)
    suspend fun saveInfaqVaultReminderConfig(config: InfaqVaultReminderConfig)
    suspend fun saveHaulStartDateMillis(timestampMillis: Long)
    suspend fun savePdfEditorialConfig(config: PdfEditorialConfig)
    suspend fun markAuditEntryPurified(entryId: String)
    suspend fun saveNotificationConfig(config: com.example.core.notification.AmanahNotificationConfig)
    suspend fun saveAutoCloudBackupEnabled(enabled: Boolean)
}


class AmanahRepositoryImpl(
    private val database: AmanahDatabase,
    private val dataStoreManager: DataStoreManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AmanahRepository {

    override val userPreferencesFlow: Flow<AppUserPreferences> =
        dataStoreManager.userPreferencesFlow.flowOn(ioDispatcher)

    override suspend fun saveThemeMode(mode: com.example.ui.theme.AppThemeMode) = withContext(ioDispatcher) {
        dataStoreManager.saveThemeMode(mode)
        database.settingsDao().insertOrUpdate(SettingsEntity("theme_mode", mode.name))
        when (mode) {
            com.example.ui.theme.AppThemeMode.TACTICAL_CYBER -> {
                database.settingsDao().insertOrUpdate(SettingsEntity("is_dark_mode", "true"))
                database.settingsDao().insertOrUpdate(SettingsEntity("is_high_contrast", "false"))
            }
            com.example.ui.theme.AppThemeMode.TACTICAL_LIGHT -> {
                database.settingsDao().insertOrUpdate(SettingsEntity("is_dark_mode", "false"))
                database.settingsDao().insertOrUpdate(SettingsEntity("is_high_contrast", "false"))
            }
            com.example.ui.theme.AppThemeMode.ELEGANT_DARK -> {
                database.settingsDao().insertOrUpdate(SettingsEntity("is_dark_mode", "true"))
                database.settingsDao().insertOrUpdate(SettingsEntity("is_high_contrast", "false"))
            }
            com.example.ui.theme.AppThemeMode.LIGHT_MODE -> {
                database.settingsDao().insertOrUpdate(SettingsEntity("is_dark_mode", "false"))
                database.settingsDao().insertOrUpdate(SettingsEntity("is_high_contrast", "false"))
            }
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_LIGHT -> {
                database.settingsDao().insertOrUpdate(SettingsEntity("is_dark_mode", "false"))
                database.settingsDao().insertOrUpdate(SettingsEntity("is_high_contrast", "true"))
            }
            com.example.ui.theme.AppThemeMode.HIGH_CONTRAST_DARK -> {
                database.settingsDao().insertOrUpdate(SettingsEntity("is_dark_mode", "true"))
                database.settingsDao().insertOrUpdate(SettingsEntity("is_high_contrast", "true"))
            }
            com.example.ui.theme.AppThemeMode.FOLLOW_SYSTEM -> {
                // Keep existing flags
            }
        }
    }

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

    override suspend fun saveGoldPriceWithMetadata(
        price: Double,
        source: String,
        timestampMillis: Long
    ) = withContext(ioDispatcher) {
        dataStoreManager.saveGoldPrice(price)
        database.settingsDao().insertOrUpdate(SettingsEntity("gold_price_per_gram", price.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("gold_price_source", source))
        database.settingsDao().insertOrUpdate(SettingsEntity("gold_price_last_updated", timestampMillis.toString()))
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

    override suspend fun saveDeficitProtectionEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveDeficitProtectionEnabled(enabled)
        database.settingsDao().insertOrUpdate(SettingsEntity("is_deficit_protection_enabled", enabled.toString()))
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
        database.settingsDao().insertOrUpdate(SettingsEntity("security_question", config.securityQuestion))
        database.settingsDao().insertOrUpdate(SettingsEntity("security_answer_hash", config.securityAnswerHash))
        database.settingsDao().insertOrUpdate(SettingsEntity("security_mask_balance_by_default", config.maskBalanceByDefault.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("security_is_screenshot_protected", config.isScreenshotProtected.toString()))
    }

    override suspend fun saveMaskBalance(isMasked: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveMaskBalance(isMasked)
    }

    override suspend fun saveShakeFeedbackEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveShakeFeedbackEnabled(enabled)
        database.settingsDao().insertOrUpdate(SettingsEntity("is_shake_feedback_enabled", enabled.toString()))
    }

    override suspend fun markGuideCompleted(guideTitle: String) = withContext(ioDispatcher) {
        dataStoreManager.markGuideCompleted(guideTitle)
    }

    override suspend fun markAuditEntryPurified(entryId: String) = withContext(ioDispatcher) {
        dataStoreManager.markAuditEntryPurified(entryId)
    }

    override suspend fun setOnboardingCompleted() = withContext(ioDispatcher) {
        dataStoreManager.setOnboardingCompleted()
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

    override fun getPagedTransactionsFlow(limit: Int, offset: Int): Flow<List<JournalEntry>> =
        database.journalDao().getPagedEntriesFlow(limit, offset).map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllTransactions(): List<JournalEntry> = withContext(ioDispatcher) {
        database.journalDao().getAllEntries().map { EntityMappers.toDomain(it) }
    }

    override suspend fun getPagedTransactions(limit: Int, offset: Int): List<JournalEntry> = withContext(ioDispatcher) {
        database.journalDao().getPagedEntries(limit, offset).map { EntityMappers.toDomain(it) }
    }

    override suspend fun getTransactionsCount(): Int = withContext(ioDispatcher) {
        database.journalDao().getEntriesCount()
    }

    override fun getRecentTransactionsFlow(startMillis: Long): Flow<List<JournalEntry>> =
        database.journalDao().getRecentEntriesFlow(startMillis).map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

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

    private fun serializeSedekahHistory(historyMap: Map<String, Double>): String {
        val obj = org.json.JSONObject()
        for ((k, v) in historyMap) {
            obj.put(k, v)
        }
        return obj.toString()
    }

    private fun deserializeSedekahHistory(json: String?): Map<String, Double> {
        if (json.isNullOrBlank()) return emptyMap()
        return try {
            val obj = org.json.JSONObject(json)
            val map = mutableMapOf<String, Double>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = obj.optDouble(k, 0.0)
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private suspend fun buildHydratedSedekahState(entity: com.example.core.database.SedekahSubuhEntity): SedekahSubuhState {
        val historyJson = database.settingsDao().getSettingByKey("sedekah_subuh_history_json")?.value
        val historyMap = deserializeSedekahHistory(historyJson)
        val lastDate = if (entity.lastContributionDateMillis > 0) java.util.Date(entity.lastContributionDateMillis) else null
        return if (historyMap.isNotEmpty()) {
            val calculated = com.example.core.infaq.SedekahSubuhStreakEngine.calculateStreak(historyMap)
            calculated.copy(
                currentStreak = maxOf(calculated.currentStreak, entity.currentStreak),
                longestStreak = maxOf(calculated.longestStreak, entity.longestStreak),
                totalContributions = maxOf(calculated.totalContributions, entity.totalContributions),
                totalDaysGiven = maxOf(calculated.totalDaysGiven, entity.totalDaysGiven),
                lastContributionDate = lastDate ?: calculated.lastContributionDate
            )
        } else {
            SedekahSubuhState(
                currentStreak = entity.currentStreak,
                longestStreak = entity.longestStreak,
                totalContributions = entity.totalContributions,
                totalDaysGiven = entity.totalDaysGiven,
                lastContributionDate = lastDate,
                historyMap = emptyMap(),
                badges = SedekahSubuhState.getDefaultBadges()
            )
        }
    }

    // Sedekah Subuh
    override fun getSedekahSubuhFlow(): Flow<SedekahSubuhState?> =
        database.sedekahSubuhDao().getSedekahStateFlow().map { entity ->
            entity?.let { buildHydratedSedekahState(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getSedekahSubuh(): SedekahSubuhState? = withContext(ioDispatcher) {
        database.sedekahSubuhDao().getSedekahState()?.let { buildHydratedSedekahState(it) }
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
        database.settingsDao().insertOrUpdate(
            SettingsEntity("sedekah_subuh_history_json", serializeSedekahHistory(state.historyMap))
        )
    }

    override suspend fun clearSedekahSubuh() = withContext(ioDispatcher) {
        database.sedekahSubuhDao().clearAll()
        database.settingsDao().deleteByKey("sedekah_subuh_history_json")
    }

    override suspend fun resetAllAppData() = withContext(ioDispatcher) {
        database.journalDao().clearAll()
        database.budgetDao().clearAll()
        database.walletDao().clearAll()
        database.ibadahGoalDao().clearAll()
        database.qardhDao().clearAll()
        database.sedekahSubuhDao().clearAll()
        database.recurringTransactionDao().clearAll()
        database.infaqRuleDao().clearAll()
        database.infaqDistributionDao().clearAll()
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

    // Shariah Rulings Implementation
    override fun getAllRulingsFlow(): Flow<List<com.example.core.shariah.ShariahRuling>> =
        database.rulingDao().getAllRulingsFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override fun getActiveRulingsFlow(): Flow<List<com.example.core.shariah.ShariahRuling>> =
        database.rulingDao().getActiveRulingsFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun saveRuling(ruling: com.example.core.shariah.ShariahRuling) = withContext(ioDispatcher) {
        database.rulingDao().insertOrUpdate(EntityMappers.toEntity(ruling))
    }

    override suspend fun saveAllRulings(rulings: List<com.example.core.shariah.ShariahRuling>) = withContext(ioDispatcher) {
        database.rulingDao().insertAll(rulings.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteRuling(id: String) = withContext(ioDispatcher) {
        database.rulingDao().deleteById(id)
    }

    override suspend fun clearCustomRulings() = withContext(ioDispatcher) {
        database.rulingDao().clearCustomRulings()
    }

    override suspend fun saveShariahRulesConfig(config: com.example.core.shariah.ShariahRulesConfig) = withContext(ioDispatcher) {
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_gold_nisab", config.goldNisabGram.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_silver_nisab", config.silverNisabGram.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_zakat_percentage", config.zakatPercentage.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_zakat_profesi_formula", config.zakatProfesiFormula.name))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_haul_calculation_method", config.haulCalculationMethod.name))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_selected_mazhab", config.selectedMazhab.name))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_custom_formula", config.customCalculationFormula))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_is_custom_formula_active", config.isCustomFormulaActive.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("shariah_custom_formula_name", config.customFormulaName))
    }

    override suspend fun getShariahRulesConfig(): com.example.core.shariah.ShariahRulesConfig? = withContext(ioDispatcher) {
        val goldStr = database.settingsDao().getSettingByKey("shariah_gold_nisab")?.value ?: return@withContext null
        val silverStr = database.settingsDao().getSettingByKey("shariah_silver_nisab")?.value
        val zakatPctStr = database.settingsDao().getSettingByKey("shariah_zakat_percentage")?.value
        val profesiFormulaStr = database.settingsDao().getSettingByKey("shariah_zakat_profesi_formula")?.value
        val haulMethodStr = database.settingsDao().getSettingByKey("shariah_haul_calculation_method")?.value
        val mazhabStr = database.settingsDao().getSettingByKey("shariah_selected_mazhab")?.value
        val formulaStr = database.settingsDao().getSettingByKey("shariah_custom_formula")?.value
        val isCustomActiveStr = database.settingsDao().getSettingByKey("shariah_is_custom_formula_active")?.value
        val formulaNameStr = database.settingsDao().getSettingByKey("shariah_custom_formula_name")?.value

        com.example.core.shariah.ShariahRulesConfig(
            goldNisabGram = goldStr.toDoubleOrNull() ?: 85.0,
            silverNisabGram = silverStr?.toDoubleOrNull() ?: 595.0,
            zakatPercentage = zakatPctStr?.toDoubleOrNull() ?: 2.5,
            zakatProfesiFormula = try {
                com.example.core.shariah.ZakatProfesiFormula.valueOf(profesiFormulaStr ?: "BRUTO")
            } catch (_: Exception) { com.example.core.shariah.ZakatProfesiFormula.BRUTO },
            haulCalculationMethod = try {
                com.example.core.shariah.HaulCalculationMethod.valueOf(haulMethodStr ?: "HIJRIAH")
            } catch (_: Exception) { com.example.core.shariah.HaulCalculationMethod.HIJRIAH },
            selectedMazhab = try {
                com.example.core.shariah.ShariahMazhab.valueOf(mazhabStr ?: "SYAFII")
            } catch (_: Exception) { com.example.core.shariah.ShariahMazhab.SYAFII },
            customCalculationFormula = formulaStr ?: "",
            isCustomFormulaActive = isCustomActiveStr?.toBooleanStrictOrNull() ?: false,
            customFormulaName = formulaNameStr ?: "Formula Pribadi"
        )
    }

    override suspend fun getAllShariahRulings(): List<com.example.core.shariah.ShariahRuling> = withContext(ioDispatcher) {
        database.rulingDao().getAllRulings().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveShariahRuling(ruling: com.example.core.shariah.ShariahRuling) = withContext(ioDispatcher) {
        saveRuling(ruling)
    }

    override suspend fun deleteShariahRuling(id: String) = withContext(ioDispatcher) {
        deleteRuling(id)
    }

    override suspend fun getShariahConfig(): com.example.core.shariah.ShariahRulesConfig? = withContext(ioDispatcher) {
        getShariahRulesConfig()
    }

    override suspend fun saveShariahConfig(config: com.example.core.shariah.ShariahRulesConfig) = withContext(ioDispatcher) {
        saveShariahRulesConfig(config)
    }

    // Recurring Transactions Implementation
    override fun getAllRecurringTransactionsFlow(): Flow<List<RecurringTransaction>> =
        database.recurringTransactionDao().getAllFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllRecurringTransactions(): List<RecurringTransaction> = withContext(ioDispatcher) {
        database.recurringTransactionDao().getAll().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveRecurringTransaction(transaction: RecurringTransaction) = withContext(ioDispatcher) {
        database.recurringTransactionDao().insertOrUpdate(EntityMappers.toEntity(transaction))
    }

    override suspend fun saveAllRecurringTransactions(transactions: List<RecurringTransaction>) = withContext(ioDispatcher) {
        database.recurringTransactionDao().insertAll(transactions.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteRecurringTransaction(id: String) = withContext(ioDispatcher) {
        database.recurringTransactionDao().deleteById(id)
    }

    override suspend fun clearAllRecurringTransactions() = withContext(ioDispatcher) {
        database.recurringTransactionDao().clearAll()
    }

    // Infaq Rules Engine Implementation
    override fun getAllInfaqRulesFlow(): Flow<List<InfaqRule>> =
        database.infaqRuleDao().getAllFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllInfaqRules(): List<InfaqRule> = withContext(ioDispatcher) {
        database.infaqRuleDao().getAll().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveInfaqRule(rule: InfaqRule) = withContext(ioDispatcher) {
        database.infaqRuleDao().insertOrUpdate(EntityMappers.toEntity(rule))
    }

    override suspend fun saveAllInfaqRules(rules: List<InfaqRule>) = withContext(ioDispatcher) {
        database.infaqRuleDao().insertAll(rules.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteInfaqRule(id: String) = withContext(ioDispatcher) {
        database.infaqRuleDao().deleteById(id)
    }

    override suspend fun clearAllInfaqRules() = withContext(ioDispatcher) {
        database.infaqRuleDao().clearAll()
    }

    // Infaq Vault Distributions Implementation
    override fun getAllInfaqDistributionsFlow(): Flow<List<InfaqDistributionRecord>> =
        database.infaqDistributionDao().getAllFlow().map { list ->
            list.map { EntityMappers.toDomain(it) }
        }.flowOn(ioDispatcher)

    override suspend fun getAllInfaqDistributions(): List<InfaqDistributionRecord> = withContext(ioDispatcher) {
        database.infaqDistributionDao().getAll().map { EntityMappers.toDomain(it) }
    }

    override suspend fun saveInfaqDistribution(record: InfaqDistributionRecord) = withContext(ioDispatcher) {
        database.infaqDistributionDao().insertOrUpdate(EntityMappers.toEntity(record))
    }

    override suspend fun saveAllInfaqDistributions(records: List<InfaqDistributionRecord>) = withContext(ioDispatcher) {
        database.infaqDistributionDao().insertAll(records.map { EntityMappers.toEntity(it) })
    }

    override suspend fun deleteInfaqDistribution(id: String) = withContext(ioDispatcher) {
        database.infaqDistributionDao().deleteById(id)
    }

    override suspend fun clearAllInfaqDistributions() = withContext(ioDispatcher) {
        database.infaqDistributionDao().clearAll()
    }

    override suspend fun saveLastBackupTime(timestampMillis: Long) = withContext(ioDispatcher) {
        dataStoreManager.saveLastBackupTime(timestampMillis)
        database.settingsDao().insertOrUpdate(SettingsEntity("last_backup_time_millis", timestampMillis.toString()))
    }

    override suspend fun saveBackupNudgeDismissed(dismissed: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveBackupNudgeDismissed(dismissed)
        database.settingsDao().insertOrUpdate(SettingsEntity("is_backup_nudge_dismissed", dismissed.toString()))
    }

    override suspend fun saveAppDisplayMode(mode: com.example.core.datastore.AppDisplayMode) = withContext(ioDispatcher) {
        dataStoreManager.saveAppDisplayMode(mode)
        database.settingsDao().insertOrUpdate(SettingsEntity("app_display_mode", mode.name))
    }

    override suspend fun saveFeatureVisibilityConfig(config: com.example.core.feature.AppFeatureVisibilityConfig) = withContext(ioDispatcher) {
        dataStoreManager.saveFeatureVisibilityConfig(config)
        database.settingsDao().insertOrUpdate(SettingsEntity("feature_visibility_config", config.toJson()))
    }

    override suspend fun saveInfaqVaultReminderConfig(config: InfaqVaultReminderConfig) = withContext(ioDispatcher) {
        dataStoreManager.saveInfaqVaultReminderConfig(config)
        database.settingsDao().insertOrUpdate(SettingsEntity("infaq_reminder_enabled", config.isEnabled.toString()))
        database.settingsDao().insertOrUpdate(SettingsEntity("infaq_reminder_schedule", config.scheduleSummaryText()))
    }

    override suspend fun saveHaulStartDateMillis(timestampMillis: Long) = withContext(ioDispatcher) {
        dataStoreManager.saveHaulStartDateMillis(timestampMillis)
        database.settingsDao().insertOrUpdate(SettingsEntity("haul_start_date_millis", timestampMillis.toString()))
    }

    override suspend fun savePdfEditorialConfig(config: PdfEditorialConfig) = withContext(ioDispatcher) {
        dataStoreManager.savePdfEditorialConfig(config)
        database.settingsDao().insertOrUpdate(SettingsEntity("pdf_editorial_config", config.toJson()))
    }

    override suspend fun saveNotificationConfig(config: com.example.core.notification.AmanahNotificationConfig) = withContext(ioDispatcher) {
        dataStoreManager.saveNotificationConfig(config)
        database.settingsDao().insertOrUpdate(SettingsEntity("notification_config_json", config.toJson()))
    }

    override suspend fun saveAutoCloudBackupEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        dataStoreManager.saveAutoCloudBackupEnabled(enabled)
        database.settingsDao().insertOrUpdate(SettingsEntity("auto_cloud_backup_enabled", enabled.toString()))
    }
}


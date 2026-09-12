package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.floatPreferencesKey
import com.example.core.accounting.FiscalCycleType
import com.example.core.budget.FinancialGoalMode
import com.example.core.security.AutoLockInterval
import com.example.core.security.SecurityConfig
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.UiScaleMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "amanah_user_preferences")

data class AppUserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.FOLLOW_SYSTEM,
    val isDarkMode: Boolean = true,
    val isHighContrast: Boolean = false,
    val goldPricePerGram: Double = 1350000.0,
    val goldPriceLastUpdated: Long = System.currentTimeMillis(),
    val selectedHijriOffset: Int = 0,
    val userNameKasMukmin: String = "Kas Keluarga Mukmin",
    val startDayOfMonth: Int = 1,
    val initialLedgerDate: String = "01/01/2024",
    val fiscalCycleType: FiscalCycleType = FiscalCycleType.MONTHLY_SALARY_DATE,
    val initialLedgerBalance: Double = 0.0,
    val fiscalYearStartMonth: Int = 1,
    val primaryCurrencySymbol: String = "Rp",
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
    val selectedGoalMode: FinancialGoalMode = FinancialGoalMode.BALANCED_50_30_20,
    val securityConfig: SecurityConfig = SecurityConfig(),
    val uiScaleMode: UiScaleMode = UiScaleMode.DEFAULT,
    val uiScaleFactor: Float = 1.0f
    , val completedGuides: Set<String> = emptySet(),
    val hasCompletedOnboarding: Boolean = false
)

class DataStoreManager(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val IS_HIGH_CONTRAST = booleanPreferencesKey("is_high_contrast")
        val GOLD_PRICE_PER_GRAM = doublePreferencesKey("gold_price_per_gram")
        val GOLD_PRICE_LAST_UPDATED = longPreferencesKey("gold_price_last_updated")
        val SELECTED_HIJRI_OFFSET = intPreferencesKey("selected_hijri_offset")
        val USER_NAME_KAS_MUKMIN = stringPreferencesKey("user_name_kas_mukmin")
        val START_DAY_OF_MONTH = intPreferencesKey("start_day_of_month")
        val INITIAL_LEDGER_DATE = stringPreferencesKey("initial_ledger_date")
        val FISCAL_CYCLE_TYPE = stringPreferencesKey("fiscal_cycle_type")
        val INITIAL_LEDGER_BALANCE = doublePreferencesKey("initial_ledger_balance")
        val FISCAL_YEAR_START_MONTH = intPreferencesKey("fiscal_year_start_month")
        val PRIMARY_CURRENCY_SYMBOL = stringPreferencesKey("primary_currency_symbol")
        val DEFAULT_INFAQ_RATE = doublePreferencesKey("default_infaq_rate")
        val IS_AUTO_DEDUCT_INFAQ_ENABLED = booleanPreferencesKey("is_auto_deduct_infaq_enabled")
        val VAULT_MONTHLY_TARGET = doublePreferencesKey("vault_monthly_target")
        val SEDEKAH_SUBUH_TARGET_DAYS = intPreferencesKey("sedekah_subuh_target_days")
        val ISRAF_WARNING_THRESHOLD_PERCENT = intPreferencesKey("israf_warning_threshold_percent")
        val IS_STRICT_BUDGET_ENFORCED = booleanPreferencesKey("is_strict_budget_enforced")
        val IS_DEFICIT_PROTECTION_ENABLED = booleanPreferencesKey("is_deficit_protection_enabled")
        val AUTO_EXECUTE_RECURRING_ENABLED = booleanPreferencesKey("auto_execute_recurring_enabled")
        val NOTIFY_ON_RECURRING_DUE = booleanPreferencesKey("notify_on_recurring_due")
        val SHOW_DAILY_HADITH = booleanPreferencesKey("show_daily_hadith")
        val SHOW_QUICK_TUTORIAL = booleanPreferencesKey("show_quick_tutorial")
        val SELECTED_GOAL_MODE = stringPreferencesKey("selected_goal_mode")
        val UI_SCALE_MODE = stringPreferencesKey("ui_scale_mode")
        val UI_SCALE_FACTOR = floatPreferencesKey("ui_scale_factor")
        val COMPLETED_GUIDES = stringSetPreferencesKey("completed_guides")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

        // Security Preferences
        val PIN_HASH = stringPreferencesKey("security_pin_hash")
        val IS_PIN_ENABLED = booleanPreferencesKey("security_is_pin_enabled")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("security_is_biometric_enabled")
        val IS_MASK_BALANCE = booleanPreferencesKey("security_is_mask_balance")
        val MASK_BALANCE_BY_DEFAULT = booleanPreferencesKey("security_mask_balance_by_default")
        val IS_SCREENSHOT_PROTECTED = booleanPreferencesKey("security_is_screenshot_protected")
        val SECURITY_QUESTION = stringPreferencesKey("security_question")
        val SECURITY_ANSWER_HASH = stringPreferencesKey("security_answer_hash")
        val AUTO_LOCK_INTERVAL = stringPreferencesKey("security_auto_lock_interval")
    }

    val userPreferencesFlow: Flow<AppUserPreferences> = context.appDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeModeStr = preferences[PreferencesKeys.THEME_MODE]
            val themeMode = if (themeModeStr != null) {
                try {
                    AppThemeMode.valueOf(themeModeStr)
                } catch (_: Exception) {
                    AppThemeMode.FOLLOW_SYSTEM
                }
            } else {
                AppThemeMode.FOLLOW_SYSTEM
            }
            val isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: true
            val isHighContrast = preferences[PreferencesKeys.IS_HIGH_CONTRAST] ?: false
            val goldPrice = preferences[PreferencesKeys.GOLD_PRICE_PER_GRAM] ?: 1350000.0
            val goldPriceLastUpdated = preferences[PreferencesKeys.GOLD_PRICE_LAST_UPDATED] ?: System.currentTimeMillis()
            val hijriOffset = preferences[PreferencesKeys.SELECTED_HIJRI_OFFSET] ?: 0
            val userName = preferences[PreferencesKeys.USER_NAME_KAS_MUKMIN] ?: "Kas Keluarga Mukmin"
            val startDay = preferences[PreferencesKeys.START_DAY_OF_MONTH] ?: 1
            val initialLedgerDate = preferences[PreferencesKeys.INITIAL_LEDGER_DATE] ?: "01/01/2024"
            val fiscalCycleStr = preferences[PreferencesKeys.FISCAL_CYCLE_TYPE] ?: FiscalCycleType.MONTHLY_SALARY_DATE.name
            val fiscalCycle = try {
                FiscalCycleType.valueOf(fiscalCycleStr)
            } catch (_: Exception) {
                FiscalCycleType.MONTHLY_SALARY_DATE
            }
            val initialBalance = preferences[PreferencesKeys.INITIAL_LEDGER_BALANCE] ?: 0.0
            val fiscalYearStartMonth = preferences[PreferencesKeys.FISCAL_YEAR_START_MONTH] ?: 1
            val currencySymbol = preferences[PreferencesKeys.PRIMARY_CURRENCY_SYMBOL] ?: "Rp"
            val infaqRate = preferences[PreferencesKeys.DEFAULT_INFAQ_RATE] ?: 0.05
            val autoInfaq = preferences[PreferencesKeys.IS_AUTO_DEDUCT_INFAQ_ENABLED] ?: true
            val vaultTarget = preferences[PreferencesKeys.VAULT_MONTHLY_TARGET] ?: 5000000.0
            val sedekahDays = preferences[PreferencesKeys.SEDEKAH_SUBUH_TARGET_DAYS] ?: 40
            val israfThreshold = preferences[PreferencesKeys.ISRAF_WARNING_THRESHOLD_PERCENT] ?: 80
            val strictBudget = preferences[PreferencesKeys.IS_STRICT_BUDGET_ENFORCED] ?: false
            val deficitProtection = preferences[PreferencesKeys.IS_DEFICIT_PROTECTION_ENABLED] ?: true
            val autoRecurring = preferences[PreferencesKeys.AUTO_EXECUTE_RECURRING_ENABLED] ?: true
            val notifyRecurring = preferences[PreferencesKeys.NOTIFY_ON_RECURRING_DUE] ?: true
            val showHadith = preferences[PreferencesKeys.SHOW_DAILY_HADITH] ?: true
            val showTutorial = preferences[PreferencesKeys.SHOW_QUICK_TUTORIAL] ?: true

            val scaleModeStr = preferences[PreferencesKeys.UI_SCALE_MODE] ?: UiScaleMode.DEFAULT.name
            val scaleMode = try {
                UiScaleMode.valueOf(scaleModeStr)
            } catch (_: Exception) {
                UiScaleMode.DEFAULT
            }
            val scaleFactor = preferences[PreferencesKeys.UI_SCALE_FACTOR] ?: 1.0f
            val completedGuides = preferences[PreferencesKeys.COMPLETED_GUIDES] ?: emptySet()
            val hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false

            val goalModeStr = preferences[PreferencesKeys.SELECTED_GOAL_MODE] ?: FinancialGoalMode.BALANCED_50_30_20.name
            val goalMode = try {
                FinancialGoalMode.valueOf(goalModeStr)
            } catch (_: Exception) {
                FinancialGoalMode.BALANCED_50_30_20
            }

            val pinHash = preferences[PreferencesKeys.PIN_HASH] ?: ""
            val isPinEnabled = preferences[PreferencesKeys.IS_PIN_ENABLED] ?: false
            val isBiometricEnabled = preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: true
            val isMaskBalance = preferences[PreferencesKeys.IS_MASK_BALANCE] ?: false
            val maskBalanceByDefault = preferences[PreferencesKeys.MASK_BALANCE_BY_DEFAULT] ?: false
            val isScreenshotProtected = preferences[PreferencesKeys.IS_SCREENSHOT_PROTECTED] ?: false
            val securityQuestion = preferences[PreferencesKeys.SECURITY_QUESTION] ?: "Nama kota kelahiran Anda?"
            val securityAnswerHash = preferences[PreferencesKeys.SECURITY_ANSWER_HASH] ?: ""
            val autoLockStr = preferences[PreferencesKeys.AUTO_LOCK_INTERVAL] ?: AutoLockInterval.IMMEDIATE.name
            val autoLock = try {
                AutoLockInterval.valueOf(autoLockStr)
            } catch (_: Exception) {
                AutoLockInterval.IMMEDIATE
            }

            val securityConfig = SecurityConfig(
                isPinEnabled = isPinEnabled,
                pinHash = pinHash,
                isBiometricEnabled = isBiometricEnabled,
                isMaskBalance = isMaskBalance,
                maskBalanceByDefault = maskBalanceByDefault,
                isScreenshotProtected = isScreenshotProtected,
                securityQuestion = securityQuestion,
                securityAnswerHash = securityAnswerHash,
                autoLockInterval = autoLock
            )

            AppUserPreferences(
                isDarkMode = isDarkMode,
                isHighContrast = isHighContrast,
                goldPricePerGram = goldPrice,
                goldPriceLastUpdated = goldPriceLastUpdated,
                selectedHijriOffset = hijriOffset,
                userNameKasMukmin = userName,
                startDayOfMonth = startDay,
                initialLedgerDate = initialLedgerDate,
                fiscalCycleType = fiscalCycle,
                initialLedgerBalance = initialBalance,
                fiscalYearStartMonth = fiscalYearStartMonth,
                primaryCurrencySymbol = currencySymbol,
                defaultInfaqRate = infaqRate,
                isAutoDeductInfaqEnabled = autoInfaq,
                vaultMonthlyTarget = vaultTarget,
                sedekahSubuhTargetDays = sedekahDays,
                israfWarningThresholdPercent = israfThreshold,
                isStrictBudgetEnforced = strictBudget,
                isDeficitProtectionEnabled = deficitProtection,
                autoExecuteRecurringEnabled = autoRecurring,
                notifyOnRecurringDue = notifyRecurring,
                showDailyHadith = showHadith,
                showQuickTutorial = showTutorial,
                selectedGoalMode = goalMode,
                securityConfig = securityConfig,
                uiScaleMode = scaleMode,
                uiScaleFactor = scaleFactor,
                completedGuides = completedGuides,
                hasCompletedOnboarding = hasCompletedOnboarding,
                themeMode = themeMode
            )
        }

    suspend fun saveThemeMode(mode: AppThemeMode) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
            when (mode) {
                AppThemeMode.FOLLOW_SYSTEM -> {
                    // Retain existing boolean flags
                }
                AppThemeMode.ELEGANT_DARK -> {
                    preferences[PreferencesKeys.IS_DARK_MODE] = true
                    preferences[PreferencesKeys.IS_HIGH_CONTRAST] = false
                }
                AppThemeMode.LIGHT_MODE -> {
                    preferences[PreferencesKeys.IS_DARK_MODE] = false
                    preferences[PreferencesKeys.IS_HIGH_CONTRAST] = false
                }
                AppThemeMode.HIGH_CONTRAST_LIGHT -> {
                    preferences[PreferencesKeys.IS_DARK_MODE] = false
                    preferences[PreferencesKeys.IS_HIGH_CONTRAST] = true
                }
                AppThemeMode.HIGH_CONTRAST_DARK -> {
                    preferences[PreferencesKeys.IS_DARK_MODE] = true
                    preferences[PreferencesKeys.IS_HIGH_CONTRAST] = true
                }
            }
        }
    }

    suspend fun saveDarkMode(isDarkMode: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
            preferences[PreferencesKeys.THEME_MODE] = if (isDarkMode) AppThemeMode.ELEGANT_DARK.name else AppThemeMode.LIGHT_MODE.name
        }
    }

    suspend fun saveHighContrast(isHighContrast: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_HIGH_CONTRAST] = isHighContrast
        }
    }

    suspend fun saveGoldPrice(price: Double, timestamp: Long = System.currentTimeMillis()) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.GOLD_PRICE_PER_GRAM] = price
            preferences[PreferencesKeys.GOLD_PRICE_LAST_UPDATED] = timestamp
        }
    }

    suspend fun saveHijriOffset(offset: Int) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_HIJRI_OFFSET] = offset
        }
    }

    suspend fun saveUserName(name: String) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME_KAS_MUKMIN] = name
        }
    }

    suspend fun saveStartDayOfMonth(day: Int) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.START_DAY_OF_MONTH] = day
        }
    }

    suspend fun saveCurrencySymbol(symbol: String) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.PRIMARY_CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun saveDefaultInfaqRate(rate: Double) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_INFAQ_RATE] = rate
        }
    }

    suspend fun saveAutoDeductInfaq(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AUTO_DEDUCT_INFAQ_ENABLED] = enabled
        }
    }

    suspend fun saveVaultMonthlyTarget(target: Double) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.VAULT_MONTHLY_TARGET] = target
        }
    }

    suspend fun saveSedekahSubuhTargetDays(days: Int) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.SEDEKAH_SUBUH_TARGET_DAYS] = days
        }
    }

    suspend fun saveIsrafWarningThreshold(percent: Int) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.ISRAF_WARNING_THRESHOLD_PERCENT] = percent
        }
    }

    suspend fun saveStrictBudgetEnforced(enforced: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_STRICT_BUDGET_ENFORCED] = enforced
        }
    }

    suspend fun saveDeficitProtectionEnabled(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DEFICIT_PROTECTION_ENABLED] = enabled
        }
    }

    suspend fun saveAutoExecuteRecurring(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_EXECUTE_RECURRING_ENABLED] = enabled
        }
    }

    suspend fun saveNotifyOnRecurringDue(enabled: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFY_ON_RECURRING_DUE] = enabled
        }
    }

    suspend fun saveShowDailyHadith(show: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_DAILY_HADITH] = show
        }
    }

    suspend fun saveShowQuickTutorial(show: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_QUICK_TUTORIAL] = show
        }
    }

    suspend fun saveSelectedGoalMode(mode: FinancialGoalMode) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_GOAL_MODE] = mode.name
        }
    }

    suspend fun saveInitialLedgerDate(date: String) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.INITIAL_LEDGER_DATE] = date
        }
    }

    suspend fun saveFiscalCycleType(cycleType: FiscalCycleType) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.FISCAL_CYCLE_TYPE] = cycleType.name
        }
    }

    suspend fun saveInitialLedgerBalance(balance: Double) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.INITIAL_LEDGER_BALANCE] = balance
        }
    }

    suspend fun saveFiscalYearStartMonth(month: Int) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.FISCAL_YEAR_START_MONTH] = month
        }
    }

    suspend fun saveUiScale(mode: UiScaleMode, factor: Float) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.UI_SCALE_MODE] = mode.name
            preferences[PreferencesKeys.UI_SCALE_FACTOR] = factor
        }
    }

    suspend fun saveSecurityConfig(config: SecurityConfig) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_HASH] = config.pinHash
            preferences[PreferencesKeys.IS_PIN_ENABLED] = config.isPinEnabled
            preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] = config.isBiometricEnabled
            preferences[PreferencesKeys.IS_MASK_BALANCE] = config.isMaskBalance
            preferences[PreferencesKeys.MASK_BALANCE_BY_DEFAULT] = config.maskBalanceByDefault
            preferences[PreferencesKeys.IS_SCREENSHOT_PROTECTED] = config.isScreenshotProtected
            preferences[PreferencesKeys.SECURITY_QUESTION] = config.securityQuestion
            preferences[PreferencesKeys.SECURITY_ANSWER_HASH] = config.securityAnswerHash
            preferences[PreferencesKeys.AUTO_LOCK_INTERVAL] = config.autoLockInterval.name
        }
    }

    suspend fun saveMaskBalance(isMasked: Boolean) {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_MASK_BALANCE] = isMasked
        }
    }

    suspend fun clearAllPreferences() {
        context.appDataStore.edit { preferences ->
            preferences.clear()
        }
    }


    
    suspend fun setOnboardingCompleted() {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }

    suspend fun markGuideCompleted(guideTitle: String) {
        context.appDataStore.edit { preferences ->
            val currentGuides = preferences[PreferencesKeys.COMPLETED_GUIDES] ?: emptySet()
            preferences[PreferencesKeys.COMPLETED_GUIDES] = currentGuides + guideTitle
        }
    }
}

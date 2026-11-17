package com.example.core.feature

import org.json.JSONObject

enum class FeaturePresetMode(val title: String, val subtitle: String, val iconDescription: String) {
    RINGKAS(
        title = "Ringkas (Esensial)",
        subtitle = "Fokus arus kas harian, sedekah subuh & hisab zakat dasar tanpa fitur kompleks.",
        iconDescription = "Simpel"
    ),
    BISNIS(
        title = "Bisnis & Usaha",
        subtitle = "Optimal untuk pedagang & pengusaha: zakat dagang, piutang qardh, anggaran & multi-kas.",
        iconDescription = "Perniagaan"
    ),
    LENGKAP(
        title = "Lengkap (Maksimal)",
        subtitle = "Mengaktifkan seluruh 23+ modul dan sub-fitur akuntansi syariah komprehensif.",
        iconDescription = "Komprehensif"
    )
}

data class AppFeatureVisibilityConfig(
    // 1. Modul Keuangan Syariah & ZISWAF
    val isSyariahModuleEnabled: Boolean = true,
    val showZakatHub: Boolean = true,
    val showSedekahSubuh: Boolean = true,
    val showInfaqVault: Boolean = true,
    val showHaulNisab: Boolean = true,
    val showGoldNisabTracker: Boolean = true,
    val showBusinessZakat: Boolean = true,
    val showShariahAudit: Boolean = true,
    val showAutoInfaqRules: Boolean = true,
    val showQardhHasan: Boolean = true,
    val showAmilDirectory: Boolean = true,
    val showFaraidhCalculator: Boolean = true,
    val showIslamicGrounding: Boolean = true,
    val showShariahRulesCustomization: Boolean = true,

    // 2. Modul Perencanaan & Laporan
    val isPlanningModuleEnabled: Boolean = true,
    val showIbadahGoals: Boolean = true,
    val showBudgetAllocation: Boolean = true,
    val showAnalytics: Boolean = true,
    val showMonthlyReport: Boolean = true,
    val showReceiptArchive: Boolean = true,
    val showExportReport: Boolean = true,
    val showZakatReport: Boolean = true,

    // 3. Modul Operasional & Pembukuan Kas
    val isOperationalModuleEnabled: Boolean = true,
    val showMultiWallet: Boolean = true,
    val showRecurringTransactions: Boolean = true,
    val showDualCalendar: Boolean = true,
    val showRezekiNonTunai: Boolean = true
) {
    val activeSyariahCount: Int
        get() = if (!isSyariahModuleEnabled) 0 else listOf(
            showZakatHub, showSedekahSubuh, showInfaqVault, showHaulNisab,
            showGoldNisabTracker, showBusinessZakat, showShariahAudit,
            showAutoInfaqRules, showQardhHasan, showAmilDirectory,
            showFaraidhCalculator, showIslamicGrounding, showShariahRulesCustomization
        ).count { it }

    val totalSyariahCount: Int = 13

    val activePlanningCount: Int
        get() = if (!isPlanningModuleEnabled) 0 else listOf(
            showIbadahGoals, showBudgetAllocation, showAnalytics,
            showMonthlyReport, showReceiptArchive, showExportReport, showZakatReport
        ).count { it }

    val totalPlanningCount: Int = 7

    val activeOperationalCount: Int
        get() = if (!isOperationalModuleEnabled) 0 else listOf(
            showMultiWallet, showRecurringTransactions, showDualCalendar, showRezekiNonTunai
        ).count { it }

    val totalOperationalCount: Int = 4

    val totalActiveFeatures: Int
        get() = activeSyariahCount + activePlanningCount + activeOperationalCount

    fun toJson(): String {
        val json = JSONObject()
        json.put("isSyariahModuleEnabled", isSyariahModuleEnabled)
        json.put("showZakatHub", showZakatHub)
        json.put("showSedekahSubuh", showSedekahSubuh)
        json.put("showInfaqVault", showInfaqVault)
        json.put("showHaulNisab", showHaulNisab)
        json.put("showGoldNisabTracker", showGoldNisabTracker)
        json.put("showBusinessZakat", showBusinessZakat)
        json.put("showShariahAudit", showShariahAudit)
        json.put("showAutoInfaqRules", showAutoInfaqRules)
        json.put("showQardhHasan", showQardhHasan)
        json.put("showAmilDirectory", showAmilDirectory)
        json.put("showFaraidhCalculator", showFaraidhCalculator)
        json.put("showIslamicGrounding", showIslamicGrounding)
        json.put("showShariahRulesCustomization", showShariahRulesCustomization)

        json.put("isPlanningModuleEnabled", isPlanningModuleEnabled)
        json.put("showIbadahGoals", showIbadahGoals)
        json.put("showBudgetAllocation", showBudgetAllocation)
        json.put("showAnalytics", showAnalytics)
        json.put("showMonthlyReport", showMonthlyReport)
        json.put("showReceiptArchive", showReceiptArchive)
        json.put("showExportReport", showExportReport)
        json.put("showZakatReport", showZakatReport)

        json.put("isOperationalModuleEnabled", isOperationalModuleEnabled)
        json.put("showMultiWallet", showMultiWallet)
        json.put("showRecurringTransactions", showRecurringTransactions)
        json.put("showDualCalendar", showDualCalendar)
        json.put("showRezekiNonTunai", showRezekiNonTunai)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String?): AppFeatureVisibilityConfig {
            if (jsonStr.isNullOrBlank()) return AppFeatureVisibilityConfig()
            return try {
                val json = JSONObject(jsonStr)
                AppFeatureVisibilityConfig(
                    isSyariahModuleEnabled = json.optBoolean("isSyariahModuleEnabled", true),
                    showZakatHub = json.optBoolean("showZakatHub", true),
                    showSedekahSubuh = json.optBoolean("showSedekahSubuh", true),
                    showInfaqVault = json.optBoolean("showInfaqVault", true),
                    showHaulNisab = json.optBoolean("showHaulNisab", true),
                    showGoldNisabTracker = json.optBoolean("showGoldNisabTracker", true),
                    showBusinessZakat = json.optBoolean("showBusinessZakat", true),
                    showShariahAudit = json.optBoolean("showShariahAudit", true),
                    showAutoInfaqRules = json.optBoolean("showAutoInfaqRules", true),
                    showQardhHasan = json.optBoolean("showQardhHasan", true),
                    showAmilDirectory = json.optBoolean("showAmilDirectory", true),
                    showFaraidhCalculator = json.optBoolean("showFaraidhCalculator", true),
                    showIslamicGrounding = json.optBoolean("showIslamicGrounding", true),
                    showShariahRulesCustomization = json.optBoolean("showShariahRulesCustomization", true),

                    isPlanningModuleEnabled = json.optBoolean("isPlanningModuleEnabled", true),
                    showIbadahGoals = json.optBoolean("showIbadahGoals", true),
                    showBudgetAllocation = json.optBoolean("showBudgetAllocation", true),
                    showAnalytics = json.optBoolean("showAnalytics", true),
                    showMonthlyReport = json.optBoolean("showMonthlyReport", true),
                    showReceiptArchive = json.optBoolean("showReceiptArchive", true),
                    showExportReport = json.optBoolean("showExportReport", true),
                    showZakatReport = json.optBoolean("showZakatReport", true),

                    isOperationalModuleEnabled = json.optBoolean("isOperationalModuleEnabled", true),
                    showMultiWallet = json.optBoolean("showMultiWallet", true),
                    showRecurringTransactions = json.optBoolean("showRecurringTransactions", true),
                    showDualCalendar = json.optBoolean("showDualCalendar", true),
                    showRezekiNonTunai = json.optBoolean("showRezekiNonTunai", true)
                )
            } catch (e: Exception) {
                AppFeatureVisibilityConfig()
            }
        }

        fun createPreset(mode: FeaturePresetMode): AppFeatureVisibilityConfig {
            return when (mode) {
                FeaturePresetMode.RINGKAS -> AppFeatureVisibilityConfig(
                    // Syariah esensial saja
                    isSyariahModuleEnabled = true,
                    showZakatHub = true,
                    showSedekahSubuh = true,
                    showInfaqVault = true,
                    showHaulNisab = false,
                    showGoldNisabTracker = false,
                    showBusinessZakat = false,
                    showShariahAudit = false,
                    showAutoInfaqRules = false,
                    showQardhHasan = false,
                    showAmilDirectory = true,
                    showFaraidhCalculator = false,
                    showIslamicGrounding = false,
                    showShariahRulesCustomization = false,

                    // Perencanaan ringkas
                    isPlanningModuleEnabled = true,
                    showIbadahGoals = true,
                    showBudgetAllocation = false,
                    showAnalytics = false,
                    showMonthlyReport = true,
                    showReceiptArchive = false,
                    showExportReport = false,
                    showZakatReport = true,

                    // Operasional esensial
                    isOperationalModuleEnabled = true,
                    showMultiWallet = true,
                    showRecurringTransactions = false,
                    showDualCalendar = true,
                    showRezekiNonTunai = false
                )
                FeaturePresetMode.BISNIS -> AppFeatureVisibilityConfig(
                    // Syariah bisnis & perniagaan
                    isSyariahModuleEnabled = true,
                    showZakatHub = true,
                    showSedekahSubuh = true,
                    showInfaqVault = true,
                    showHaulNisab = true,
                    showGoldNisabTracker = true,
                    showBusinessZakat = true,
                    showShariahAudit = true,
                    showAutoInfaqRules = true,
                    showQardhHasan = true,
                    showAmilDirectory = true,
                    showFaraidhCalculator = false,
                    showIslamicGrounding = true,
                    showShariahRulesCustomization = false,

                    // Perencanaan & laporan bisnis
                    isPlanningModuleEnabled = true,
                    showIbadahGoals = false,
                    showBudgetAllocation = true,
                    showAnalytics = true,
                    showMonthlyReport = true,
                    showReceiptArchive = true,
                    showExportReport = true,
                    showZakatReport = true,

                    // Operasional bisnis
                    isOperationalModuleEnabled = true,
                    showMultiWallet = true,
                    showRecurringTransactions = true,
                    showDualCalendar = true,
                    showRezekiNonTunai = true
                )
                FeaturePresetMode.LENGKAP -> AppFeatureVisibilityConfig()
            }
        }
    }
}

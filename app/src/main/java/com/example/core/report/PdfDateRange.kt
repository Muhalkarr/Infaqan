package com.example.core.report

import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.audit.ShariahAuditSummary
import com.example.core.audit.ShariahComplianceAuditEngine
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.core.ibadah.IbadahDepositRecord
import com.example.core.infaq.InfaqDistributionRecord
import com.example.core.infaq.InfaqSliAnalysisCalculator
import com.example.core.infaq.InfaqSliComprehensiveAnalysis
import com.example.core.infaq.SedekahSubuhState
import com.example.core.rezeki.RezekiNonTunaiRecord
import com.example.core.state.AmanahLedgerUiState
import com.example.core.wallet.WalletMutationRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class PdfCalendarMode(val label: String) {
    MASEHI("Masehi"),
    HIJRIYAH("Hijriyah")
}

/**
 * Menentukan rentang waktu laporan PDF ("dari kapan ke kapan") baik menggunakan
 * kalender Masehi maupun kalender Hijriyah, lengkap dengan konversi dua arah.
 */
data class PdfDateRange(
    val isCustomEnabled: Boolean = false,
    val calendarMode: PdfCalendarMode = PdfCalendarMode.MASEHI,
    val startMillis: Long? = null,
    val endMillis: Long? = null,
    val startHijri: HijriDate? = null,
    val endHijri: HijriDate? = null,
    val presetTag: String = PRESET_ALL_TIME
) {
    fun containsTimestamp(millis: Long): Boolean {
        if (!isCustomEnabled || startMillis == null || endMillis == null) return true
        return millis in startMillis..endMillis
    }

    fun containsDate(date: Date): Boolean = containsTimestamp(date.time)

    fun containsJournalEntry(
        entry: JournalEntry,
        manualOffsetDays: Int = 0
    ): Boolean {
        if (!isCustomEnabled) return true
        if (calendarMode == PdfCalendarMode.HIJRIYAH && startHijri != null && endHijri != null) {
            val h = HijriCalendarEngine.fromGregorian(
                date = entry.gregorianDate,
                maghribHour = 24,
                manualOffsetDays = manualOffsetDays
            )
            val key = h.toComparableInt()
            return key in startHijri.toComparableInt()..endHijri.toComparableInt()
        }
        return containsTimestamp(entry.gregorianDate.time)
    }

    fun formatPrimaryPeriodLabel(): String {
        if (!isCustomEnabled || startMillis == null || endMillis == null) {
            return "Semua Periode (Keseluruhan Data)"
        }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        return if (calendarMode == PdfCalendarMode.HIJRIYAH && startHijri != null && endHijri != null) {
            "${formatHijriShort(startHijri)} – ${formatHijriShort(endHijri)}"
        } else {
            "${sdf.format(Date(startMillis))} – ${sdf.format(Date(endMillis))}"
        }
    }

    fun formatDualCalendarSummary(): String {
        if (!isCustomEnabled || startMillis == null || endMillis == null) {
            return "Semua Waktu (Seluruh Riwayat Buku Kas)"
        }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val gregRange = "${sdf.format(Date(startMillis))} – ${sdf.format(Date(endMillis))}"
        val sH = startHijri ?: HijriCalendarEngine.fromGregorian(Date(startMillis), maghribHour = 24)
        val eH = endHijri ?: HijriCalendarEngine.fromGregorian(Date(endMillis), maghribHour = 24)
        val hijriRange = if (sH.year == eH.year && sH.month == eH.month) {
            "${sH.day}–${eH.day} ${eH.monthName} ${eH.year} H"
        } else {
            "${formatHijriShort(sH)} – ${formatHijriShort(eH)}"
        }
        return if (calendarMode == PdfCalendarMode.HIJRIYAH) {
            "$hijriRange ($gregRange)"
        } else {
            "$gregRange ($hijriRange)"
        }
    }

    fun formatPdfHeaderPeriod(): String {
        if (!isCustomEnabled || startMillis == null || endMillis == null) {
            return "Periode: Semua Waktu"
        }
        return "Periode: ${formatDualCalendarSummary()}"
    }

    fun formatShortFileTag(): String {
        if (!isCustomEnabled || startMillis == null || endMillis == null) return "Semua"
        return if (calendarMode == PdfCalendarMode.HIJRIYAH && startHijri != null && endHijri != null) {
            String.format(
                Locale.US,
                "%04d%02d%02dH-%04d%02d%02dH",
                startHijri.year, startHijri.month, startHijri.day,
                endHijri.year, endHijri.month, endHijri.day
            )
        } else {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
            "${sdf.format(Date(startMillis))}-${sdf.format(Date(endMillis))}"
        }
    }

    companion object {
        const val PRESET_ALL_TIME = "ALL_TIME"
        const val PRESET_MASEHI_THIS_MONTH = "MASEHI_THIS_MONTH"
        const val PRESET_MASEHI_LAST_30D = "MASEHI_LAST_30D"
        const val PRESET_MASEHI_THIS_YEAR = "MASEHI_THIS_YEAR"
        const val PRESET_MASEHI_CUSTOM = "MASEHI_CUSTOM"
        const val PRESET_HIJRI_THIS_MONTH = "HIJRI_THIS_MONTH"
        const val PRESET_HIJRI_RAMADAN = "HIJRI_RAMADAN"
        const val PRESET_HIJRI_THIS_YEAR = "HIJRI_THIS_YEAR"
        const val PRESET_HIJRI_CUSTOM = "HIJRI_CUSTOM"

        private fun formatHijriShort(h: HijriDate): String = "${h.day} ${h.monthName} ${h.year} H"

        fun allTime(mode: PdfCalendarMode = PdfCalendarMode.MASEHI): PdfDateRange =
            PdfDateRange(
                isCustomEnabled = false,
                calendarMode = mode,
                presetTag = PRESET_ALL_TIME
            )

        fun startOfDayMillis(millis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun endOfDayMillis(millis: Long): Long {
            val cal = Calendar.getInstance().apply {
                timeInMillis = millis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return cal.timeInMillis
        }

        /**
         * Mengonversi timestamp UTC dari Material 3 DatePicker ke waktu lokal (12:00 siang)
         * pada tanggal kalender yang sama agar tidak bergeser 1 hari karena zona waktu.
         */
        fun utcPickerMillisToLocalMidday(utcMillis: Long): Long {
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = utcMillis
            }
            val localCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return localCal.timeInMillis
        }

        fun localMillisToUtcPickerMillis(localMillis: Long): Long {
            val localCal = Calendar.getInstance().apply { timeInMillis = localMillis }
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.YEAR, localCal.get(Calendar.YEAR))
                set(Calendar.MONTH, localCal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, localCal.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return utcCal.timeInMillis
        }

        fun fromGregorianRange(
            rawStartMillis: Long,
            rawEndMillis: Long,
            manualOffsetDays: Int = 0,
            presetTag: String = PRESET_MASEHI_CUSTOM
        ): PdfDateRange {
            val s0 = startOfDayMillis(minOf(rawStartMillis, rawEndMillis))
            val e0 = endOfDayMillis(maxOf(rawStartMillis, rawEndMillis))
            val sMid = Calendar.getInstance().apply {
                timeInMillis = s0
                set(Calendar.HOUR_OF_DAY, 12)
            }.time
            val eMid = Calendar.getInstance().apply {
                timeInMillis = e0
                set(Calendar.HOUR_OF_DAY, 12)
            }.time
            return PdfDateRange(
                isCustomEnabled = true,
                calendarMode = PdfCalendarMode.MASEHI,
                startMillis = s0,
                endMillis = e0,
                startHijri = HijriCalendarEngine.fromGregorian(sMid, maghribHour = 24, manualOffsetDays = manualOffsetDays),
                endHijri = HijriCalendarEngine.fromGregorian(eMid, maghribHour = 24, manualOffsetDays = manualOffsetDays),
                presetTag = presetTag
            )
        }

        fun fromHijriRange(
            rawStart: HijriDate,
            rawEnd: HijriDate,
            manualOffsetDays: Int = 0,
            presetTag: String = PRESET_HIJRI_CUSTOM
        ): PdfDateRange {
            val orderedStart = if (rawStart <= rawEnd) rawStart else rawEnd
            val orderedEnd = if (rawStart <= rawEnd) rawEnd else rawStart
            val clampedStart = orderedStart.copy(
                day = orderedStart.day.coerceIn(1, HijriCalendarEngine.daysInHijriMonth(orderedStart.year, orderedStart.month, manualOffsetDays))
            )
            val clampedEnd = orderedEnd.copy(
                day = orderedEnd.day.coerceIn(1, HijriCalendarEngine.daysInHijriMonth(orderedEnd.year, orderedEnd.month, manualOffsetDays))
            )
            val startGreg = HijriCalendarEngine.toGregorian(
                hijriYear = clampedStart.year,
                hijriMonth = clampedStart.month,
                hijriDay = clampedStart.day,
                manualOffsetDays = manualOffsetDays
            )
            val endGreg = HijriCalendarEngine.toGregorian(
                hijriYear = clampedEnd.year,
                hijriMonth = clampedEnd.month,
                hijriDay = clampedEnd.day,
                manualOffsetDays = manualOffsetDays
            )
            return PdfDateRange(
                isCustomEnabled = true,
                calendarMode = PdfCalendarMode.HIJRIYAH,
                startMillis = startOfDayMillis(startGreg.time),
                endMillis = endOfDayMillis(endGreg.time),
                startHijri = clampedStart,
                endHijri = clampedEnd,
                presetTag = presetTag
            )
        }

        fun forGregorianMonth(
            monthIndex: Int,
            year: Int,
            manualOffsetDays: Int = 0
        ): PdfDateRange {
            val startCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val endCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            return fromGregorianRange(
                rawStartMillis = startCal.timeInMillis,
                rawEndMillis = endCal.timeInMillis,
                manualOffsetDays = manualOffsetDays,
                presetTag = PRESET_MASEHI_THIS_MONTH
            )
        }

        fun forCurrentGregorianMonth(manualOffsetDays: Int = 0, nowMillis: Long = System.currentTimeMillis()): PdfDateRange {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            return forGregorianMonth(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), manualOffsetDays)
        }

        fun forLast30Days(manualOffsetDays: Int = 0, nowMillis: Long = System.currentTimeMillis()): PdfDateRange {
            val startMillis = nowMillis - (29L * 24 * 60 * 60 * 1000)
            return fromGregorianRange(
                rawStartMillis = startMillis,
                rawEndMillis = nowMillis,
                manualOffsetDays = manualOffsetDays,
                presetTag = PRESET_MASEHI_LAST_30D
            )
        }

        fun forCurrentGregorianYear(manualOffsetDays: Int = 0, nowMillis: Long = System.currentTimeMillis()): PdfDateRange {
            val year = Calendar.getInstance().apply { timeInMillis = nowMillis }.get(Calendar.YEAR)
            val startCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val endCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 31)
            }
            return fromGregorianRange(
                rawStartMillis = startCal.timeInMillis,
                rawEndMillis = endCal.timeInMillis,
                manualOffsetDays = manualOffsetDays,
                presetTag = PRESET_MASEHI_THIS_YEAR
            )
        }

        fun forHijriMonth(
            hijriYear: Int,
            hijriMonth: Int,
            manualOffsetDays: Int = 0,
            presetTag: String = PRESET_HIJRI_THIS_MONTH
        ): PdfDateRange {
            val lastDay = HijriCalendarEngine.daysInHijriMonth(hijriYear, hijriMonth, manualOffsetDays)
            return fromHijriRange(
                rawStart = HijriDate(hijriYear, hijriMonth, 1),
                rawEnd = HijriDate(hijriYear, hijriMonth, lastDay),
                manualOffsetDays = manualOffsetDays,
                presetTag = presetTag
            )
        }

        fun forCurrentHijriMonth(manualOffsetDays: Int = 0, nowMillis: Long = System.currentTimeMillis()): PdfDateRange {
            val h = HijriCalendarEngine.fromGregorian(Date(nowMillis), maghribHour = 24, manualOffsetDays = manualOffsetDays)
            return forHijriMonth(h.year, h.month, manualOffsetDays, PRESET_HIJRI_THIS_MONTH)
        }

        fun forHijriYear(
            hijriYear: Int,
            manualOffsetDays: Int = 0
        ): PdfDateRange {
            val lastDay = HijriCalendarEngine.daysInHijriMonth(hijriYear, 12, manualOffsetDays)
            return fromHijriRange(
                rawStart = HijriDate(hijriYear, 1, 1),
                rawEnd = HijriDate(hijriYear, 12, lastDay),
                manualOffsetDays = manualOffsetDays,
                presetTag = PRESET_HIJRI_THIS_YEAR
            )
        }
    }
}

data class PdfScopedData(
    val dateRange: PdfDateRange,
    val filteredEntries: List<JournalEntry>,
    val filteredDistributions: List<InfaqDistributionRecord>,
    val filteredMutations: List<WalletMutationRecord>,
    val filteredRezeki: List<RezekiNonTunaiRecord>,
    val filteredIbadahDeposits: List<IbadahDepositRecord>,
    val filteredSubuhState: SedekahSubuhState,
    val totalIncomeKasab: Double,
    val totalIncomeNonKasab: Double,
    val totalConsumptionExpense: Double,
    val totalPurifiedInfaq: Double,
    val totalDisbursedInfaq: Double,
    val totalInternalTransfer: Double,
    val netSavingsAmount: Double,
    val spiritualLiquidityIndex: Double,
    val kasabBreakdown: List<Pair<Account, Double>>,
    val nonKasabBreakdown: List<Pair<Account, Double>>,
    val expenseBreakdown: List<Pair<Account, Double>>,
    val sliAnalysis: InfaqSliComprehensiveAnalysis,
    val auditSummary: ShariahAuditSummary,
    val budgetSpentByAccount: Map<String, Double>,
    val totalZakatDisbursedRecorded: Double
)

object PdfPeriodFilterHelper {

    fun compute(
        state: AmanahLedgerUiState,
        dateRange: PdfDateRange
    ): PdfScopedData {
        val offset = state.selectedHijriOffset
        if (!dateRange.isCustomEnabled || dateRange.startMillis == null || dateRange.endMillis == null) {
            return PdfScopedData(
                dateRange = dateRange,
                filteredEntries = state.journalEntries,
                filteredDistributions = state.infaqDistributions,
                filteredMutations = state.walletMutations,
                filteredRezeki = state.rezekiNonTunaiList,
                filteredIbadahDeposits = state.ibadahDeposits,
                filteredSubuhState = state.sedekahSubuhState,
                totalIncomeKasab = state.totalIncomeKasab,
                totalIncomeNonKasab = state.totalIncomeNonKasab,
                totalConsumptionExpense = state.totalConsumptionExpense,
                totalPurifiedInfaq = state.totalPurifiedInfaq,
                totalDisbursedInfaq = state.totalDisbursedInfaq,
                totalInternalTransfer = state.totalInternalTransfer,
                netSavingsAmount = state.netSavingsAmount,
                spiritualLiquidityIndex = state.spiritualLiquidityIndex,
                kasabBreakdown = state.getKasabBreakdown(),
                nonKasabBreakdown = state.getNonKasabBreakdown(),
                expenseBreakdown = state.getExpenseBreakdown(),
                sliAnalysis = state.infaqSliAnalysis,
                auditSummary = ShariahComplianceAuditEngine.auditAllEntries(state),
                budgetSpentByAccount = emptyMap(), // Gunakan spent bulanan default bila tanpa rentang kustom
                totalZakatDisbursedRecorded = state.totalZakatDisbursedRecorded
            )
        }

        val entries = state.journalEntries.filter { dateRange.containsJournalEntry(it, offset) }
        val distributions = state.infaqDistributions.filter { dateRange.containsDate(it.distributionDate) }
        val mutations = state.walletMutations.filter { dateRange.containsDate(it.date) }
        val rezeki = state.rezekiNonTunaiList.filter { dateRange.containsTimestamp(it.dateMillis) }
        val deposits = state.ibadahDeposits.filter { dateRange.containsTimestamp(it.dateMillis) }

        val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val filteredHistoryMap = state.sedekahSubuhState.historyMap.filterKeys { key ->
            try {
                val parsed = dateKeyFormat.parse(key) ?: return@filterKeys false
                dateRange.containsDate(parsed)
            } catch (_: Exception) {
                false
            }
        }
        val filteredSubuh = state.sedekahSubuhState.copy(
            historyMap = filteredHistoryMap,
            totalContributions = filteredHistoryMap.values.sum(),
            totalDaysGiven = filteredHistoryMap.count { it.value > 0.0 }
        )

        val accountMap = state.accounts.associateBy { it.id }

        val kasabTotal = entries
            .filter { it.transactionType == "INFLOW" }
            .flatMap { it.lines }
            .filter { accountMap[it.accountId]?.category == AccountCategory.INCOME_KASAB }
            .sumOf { it.credit }

        val nonKasabTotal = entries
            .filter { it.transactionType == "INFLOW" }
            .flatMap { it.lines }
            .filter { accountMap[it.accountId]?.category == AccountCategory.INCOME_NON_KASAB }
            .sumOf { it.credit }

        val consumptionTotal = entries
            .filter { it.transactionType == "EXPENSE" }
            .flatMap { it.lines }
            .filter { line ->
                val acc = accountMap[line.accountId]
                acc != null && acc.category == AccountCategory.EXPENSE && acc.id != "acc_disbursed"
            }
            .sumOf { it.debit }

        val purifiedInfaqTotal = entries
            .flatMap { it.lines }
            .filter { it.accountId == "acc_vault" }
            .sumOf { it.credit }

        val disbursedInfaqTotal = entries
            .filter { it.transactionType == "INFAQ_PAYOUT" }
            .flatMap { it.lines }
            .filter { it.accountId == "acc_vault" || it.accountId == "acc_disbursed" || it.accountId == "acc_sedekah_extra" }
            .sumOf { it.debit }

        val vaultCredits = entries.flatMap { it.lines }.filter { it.accountId == "acc_vault" }.sumOf { it.credit }
        val vaultDebits = entries.flatMap { it.lines }.filter { it.accountId == "acc_vault" }.sumOf { it.debit }
        val periodVaultNet = (vaultCredits - vaultDebits).coerceAtLeast(0.0)

        val internalTransfer = mutations.sumOf { it.amount }
        val netSavings = kasabTotal + nonKasabTotal - consumptionTotal - purifiedInfaqTotal
        val sli = if (consumptionTotal <= 0.0) {
            if (purifiedInfaqTotal > 0.0) 100.0 else 0.0
        } else {
            (purifiedInfaqTotal / consumptionTotal) * 100.0
        }

        val kasabList = state.accounts
            .filter { it.category == AccountCategory.INCOME_KASAB }
            .mapNotNull { acc ->
                val s = entries.filter { it.transactionType == "INFLOW" }.flatMap { it.lines }.filter { it.accountId == acc.id }.sumOf { it.credit }
                if (s > 0.0) acc to s else null
            }.sortedByDescending { it.second }

        val nonKasabList = state.accounts
            .filter { it.category == AccountCategory.INCOME_NON_KASAB }
            .mapNotNull { acc ->
                val s = entries.filter { it.transactionType == "INFLOW" }.flatMap { it.lines }.filter { it.accountId == acc.id }.sumOf { it.credit }
                if (s > 0.0) acc to s else null
            }.sortedByDescending { it.second }

        val expenseList = state.accounts
            .filter { it.category == AccountCategory.EXPENSE && it.id != "acc_disbursed" }
            .mapNotNull { acc ->
                val s = entries.filter { it.transactionType == "EXPENSE" }.flatMap { it.lines }.filter { it.accountId == acc.id }.sumOf { it.debit }
                if (s > 0.0) acc to s else null
            }.sortedByDescending { it.second }

        val budgetSpentMap = mutableMapOf<String, Double>()
        for (e in entries) {
            if (e.transactionType == "EXPENSE") {
                for (line in e.lines) {
                    val acc = accountMap[line.accountId]
                    if (line.debit > 0.0 && line.accountId != "acc_disbursed" && line.accountId != "acc_vault" && (acc == null || acc.category == AccountCategory.EXPENSE)) {
                        budgetSpentMap[line.accountId] = (budgetSpentMap[line.accountId] ?: 0.0) + line.debit
                    }
                }
            }
        }

        val sliAnalysis = InfaqSliAnalysisCalculator.analyze(
            accounts = state.accounts,
            journalEntries = entries,
            rules = state.rules,
            totalIncomeKasab = kasabTotal,
            totalIncomeNonKasab = nonKasabTotal,
            totalConsumptionExpense = consumptionTotal,
            totalPurifiedInfaq = purifiedInfaqTotal,
            virtualInfaqVaultBalance = periodVaultNet
        )

        val auditSummary = ShariahComplianceAuditEngine.auditAllEntries(state.copy(journalEntries = entries))
        val zakatDisbursed = distributions
            .filter { it.programName.contains("zakat", ignoreCase = true) || it.notes.contains("zakat", ignoreCase = true) }
            .sumOf { it.amount }

        return PdfScopedData(
            dateRange = dateRange,
            filteredEntries = entries,
            filteredDistributions = distributions,
            filteredMutations = mutations,
            filteredRezeki = rezeki,
            filteredIbadahDeposits = deposits,
            filteredSubuhState = filteredSubuh,
            totalIncomeKasab = kasabTotal,
            totalIncomeNonKasab = nonKasabTotal,
            totalConsumptionExpense = consumptionTotal,
            totalPurifiedInfaq = purifiedInfaqTotal,
            totalDisbursedInfaq = disbursedInfaqTotal,
            totalInternalTransfer = internalTransfer,
            netSavingsAmount = netSavings,
            spiritualLiquidityIndex = sli,
            kasabBreakdown = kasabList,
            nonKasabBreakdown = nonKasabList,
            expenseBreakdown = expenseList,
            sliAnalysis = sliAnalysis,
            auditSummary = auditSummary,
            budgetSpentByAccount = budgetSpentMap,
            totalZakatDisbursedRecorded = zakatDisbursed
        )
    }
}

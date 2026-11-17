package com.example.core.analytics

import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.state.AmanahLedgerUiState
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

enum class TracePeriodFilter(val label: String) {
    ALL_TIME("Semua Periode"),
    THIS_MONTH("Bulan Ini"),
    LAST_30_DAYS("30 Hari"),
    THIS_YEAR("Tahun Ini")
}

enum class TraceBucket(val label: String) {
    SEMUA("Semua"),
    KASAB("Kasab"),
    NON_KASAB("Non-Kasab"),
    KONSUMSI("Konsumsi"),
    INFAQ_ZAKAT("Infaq & Zakat"),
    TRANSFER_QARDH("Mutasi & Qardh")
}

data class PeriodDeltaMetric(
    val current: Double = 0.0,
    val previous: Double = 0.0
) {
    val deltaAmount: Double get() = current - previous
    val deltaPercent: Double?
        get() = when {
            previous > 0.0 -> ((current - previous) / previous) * 100.0
            current > 0.0 -> 100.0
            else -> null
        }
}

data class TraceableJournalItem(
    val id: String,
    val date: Date,
    val hijriLabel: String,
    val description: String,
    val transactionType: String,
    val bucket: TraceBucket,
    val amount: Double,
    val infaqAllocated: Double,
    val debitAccounts: String,
    val creditAccounts: String
)

data class FinancialHealthAndTraceReport(
    val periodFilter: TracePeriodFilter,
    val periodIncomeTotal: Double,
    val periodIncomeKasab: Double,
    val periodIncomeNonKasab: Double,
    val periodConsumptionTotal: Double,
    val periodInfaqAllocated: Double,
    val periodInfaqDisbursed: Double,
    val periodNetSurplus: Double,
    val savingsRatePercent: Double,
    val consumptionRatioPercent: Double,
    val kasabSharePercent: Double,
    val nonKasabSharePercent: Double,
    val infaqCompliancePercent: Double,
    val emergencyRunwayMonths: Double,
    val debtToAssetRatioPercent: Double,
    val activeReceivablesTotal: Double,
    val activePayablesTotal: Double,
    val topExpenseCategoryName: String,
    val topExpenseSharePercent: Double,
    val healthStatusLabel: String,
    val healthGuidance: String,
    val incomeMoM: PeriodDeltaMetric,
    val expenseMoM: PeriodDeltaMetric,
    val infaqMoM: PeriodDeltaMetric,
    val traceItems: List<TraceableJournalItem>,
    val bucketCounts: Map<TraceBucket, Int>
)

object FinancialTraceabilityEngine {

    private val HIJRI_MONTHS_SHORT = arrayOf(
        "", "Muh", "Saf", "R.Aw", "R.Ak", "J.Aw", "J.Ak",
        "Raj", "Sya", "Ram", "Syw", "DzQ", "DzH"
    )

    fun analyze(
        state: AmanahLedgerUiState,
        periodFilter: TracePeriodFilter = TracePeriodFilter.ALL_TIME,
        selectedBucket: TraceBucket = TraceBucket.SEMUA,
        searchQuery: String = "",
        nowMillis: Long = System.currentTimeMillis()
    ): FinancialHealthAndTraceReport {
        val accountMap = state.accounts.associateBy { it.id }
        val filteredEntries = filterByPeriod(state.journalEntries, periodFilter, nowMillis)

        var incomeKasab = 0.0
        var incomeNonKasab = 0.0
        var consumptionTotal = 0.0
        var infaqAllocated = 0.0
        var infaqDisbursed = 0.0
        val expenseByAccount = mutableMapOf<String, Double>()

        val allTraceItems = mutableListOf<TraceableJournalItem>()

        for (entry in filteredEntries) {
            val classified = classifyEntry(entry, accountMap)
            allTraceItems.add(classified.item)

            incomeKasab += classified.kasabAmount
            incomeNonKasab += classified.nonKasabAmount
            consumptionTotal += classified.consumptionAmount
            infaqAllocated += classified.infaqAllocatedAmount
            infaqDisbursed += classified.infaqDisbursedAmount

            for ((accId, amt) in classified.expenseLines) {
                expenseByAccount[accId] = (expenseByAccount[accId] ?: 0.0) + amt
            }
        }

        val totalIncome = incomeKasab + incomeNonKasab
        val netSurplus = totalIncome - consumptionTotal - infaqAllocated

        val savingsRatePercent = if (totalIncome > 0.0) (netSurplus / totalIncome) * 100.0 else 0.0
        val consumptionRatioPercent = if (totalIncome > 0.0) (consumptionTotal / totalIncome) * 100.0 else 0.0
        val kasabSharePercent = if (totalIncome > 0.0) (incomeKasab / totalIncome) * 100.0 else 0.0
        val nonKasabSharePercent = if (totalIncome > 0.0) (incomeNonKasab / totalIncome) * 100.0 else 0.0

        val sliAnalysis = state.infaqSliAnalysis
        val infaqCompliancePercent = if (sliAnalysis.expectedTotalInfaqFromIncomeRules > 0.0) {
            (sliAnalysis.totalAllocatedToVault / sliAnalysis.expectedTotalInfaqFromIncomeRules) * 100.0
        } else if (sliAnalysis.totalAllocatedToVault > 0.0) 100.0 else 100.0

        // Rata-rata pengeluaran bulanan 3 bulan terakhir (atau konsumsi periode jika lebih representatif)
        val avgMonthlyExpense = computeAverageMonthlyConsumption(state.journalEntries, accountMap, nowMillis)
            .let { if (it > 0.0) it else consumptionTotal }
        val liquidAssets = state.totalWalletBalance.coerceAtLeast(0.0)
        val emergencyRunwayMonths = if (avgMonthlyExpense > 0.0) {
            (liquidAssets / avgMonthlyExpense).coerceAtLeast(0.0)
        } else 0.0

        val activeReceivables = state.qardhRecords
            .filter { it.type == QardhType.PIUTANG_SAYA && (it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS) }
            .sumOf { it.remainingAmount }
        val activePayables = state.qardhRecords
            .filter { it.type == QardhType.HUTANG_SAYA && (it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS) }
            .sumOf { it.remainingAmount }

        val debtToAssetRatioPercent = if (liquidAssets > 0.0) {
            (activePayables / liquidAssets) * 100.0
        } else if (activePayables > 0.0) 100.0 else 0.0

        val topExpensePair = expenseByAccount.maxByOrNull { it.value }
        val topExpenseName = topExpensePair?.key?.let { accountMap[it]?.name } ?: "Belum Ada Pengeluaran"
        val topExpenseShare = if (consumptionTotal > 0.0 && topExpensePair != null) {
            (topExpensePair.value / consumptionTotal) * 100.0
        } else 0.0

        val (healthStatus, guidance) = evaluateHealthStatus(
            totalIncome = totalIncome,
            consumptionRatioPercent = consumptionRatioPercent,
            savingsRatePercent = savingsRatePercent,
            debtToAssetRatioPercent = debtToAssetRatioPercent,
            topExpenseCategoryName = topExpenseName
        )

        // MoM comparison (Bulan Berjalan vs Bulan Sebelumnya)
        val (incomeMoM, expenseMoM, infaqMoM) = computeMonthOverMonth(state.journalEntries, accountMap, nowMillis)

        // Filter items by bucket + search query & sort newest first
        val bucketCounts = TraceBucket.entries.associateWith { bucket ->
            if (bucket == TraceBucket.SEMUA) allTraceItems.size
            else allTraceItems.count { it.bucket == bucket }
        }

        val query = searchQuery.trim()
        val matchingTrace = allTraceItems
            .asSequence()
            .filter { selectedBucket == TraceBucket.SEMUA || it.bucket == selectedBucket }
            .filter { item ->
                if (query.isEmpty()) true
                else item.description.contains(query, ignoreCase = true) ||
                    item.debitAccounts.contains(query, ignoreCase = true) ||
                    item.creditAccounts.contains(query, ignoreCase = true) ||
                    item.hijriLabel.contains(query, ignoreCase = true)
            }
            .sortedByDescending { it.date.time }
            .toList()

        return FinancialHealthAndTraceReport(
            periodFilter = periodFilter,
            periodIncomeTotal = totalIncome,
            periodIncomeKasab = incomeKasab,
            periodIncomeNonKasab = incomeNonKasab,
            periodConsumptionTotal = consumptionTotal,
            periodInfaqAllocated = infaqAllocated,
            periodInfaqDisbursed = infaqDisbursed,
            periodNetSurplus = netSurplus,
            savingsRatePercent = savingsRatePercent,
            consumptionRatioPercent = consumptionRatioPercent,
            kasabSharePercent = kasabSharePercent,
            nonKasabSharePercent = nonKasabSharePercent,
            infaqCompliancePercent = infaqCompliancePercent,
            emergencyRunwayMonths = emergencyRunwayMonths,
            debtToAssetRatioPercent = debtToAssetRatioPercent,
            activeReceivablesTotal = activeReceivables,
            activePayablesTotal = activePayables,
            topExpenseCategoryName = topExpenseName,
            topExpenseSharePercent = topExpenseShare,
            healthStatusLabel = healthStatus,
            healthGuidance = guidance,
            incomeMoM = incomeMoM,
            expenseMoM = expenseMoM,
            infaqMoM = infaqMoM,
            traceItems = matchingTrace,
            bucketCounts = bucketCounts
        )
    }

    private data class ClassifiedEntry(
        val item: TraceableJournalItem,
        val kasabAmount: Double = 0.0,
        val nonKasabAmount: Double = 0.0,
        val consumptionAmount: Double = 0.0,
        val infaqAllocatedAmount: Double = 0.0,
        val infaqDisbursedAmount: Double = 0.0,
        val expenseLines: List<Pair<String, Double>> = emptyList()
    )

    private fun classifyEntry(
        entry: JournalEntry,
        accountMap: Map<String, Account>
    ): ClassifiedEntry {
        val hijriM = if (entry.hijriMonth in 1..12) HIJRI_MONTHS_SHORT[entry.hijriMonth] else "H"
        val hijriStr = "${entry.hijriDay} $hijriM ${entry.hijriYear}H"

        val debitNames = entry.lines
            .filter { it.debit > 0.0 }
            .map { accountMap[it.accountId]?.name ?: it.accountId }
            .distinct()
            .joinToString(", ")

        val creditNames = entry.lines
            .filter { it.credit > 0.0 }
            .map { accountMap[it.accountId]?.name ?: it.accountId }
            .distinct()
            .joinToString(", ")

        var kasab = 0.0
        var nonKasab = 0.0
        var consumption = 0.0
        var vaultIn = 0.0
        var disbursed = 0.0
        val expPairs = mutableListOf<Pair<String, Double>>()

        for (line in entry.lines) {
            val acc = accountMap[line.accountId]
            val cat = acc?.category
            if (entry.transactionType == "INFLOW" && line.credit > 0.0) {
                when (cat) {
                    AccountCategory.INCOME_KASAB -> kasab += line.credit
                    AccountCategory.INCOME_NON_KASAB -> nonKasab += line.credit
                    else -> Unit
                }
            }
            if (line.accountId == "acc_vault" && line.credit > 0.0) {
                vaultIn += line.credit
            }
            if (entry.transactionType == "EXPENSE" && line.debit > 0.0 && cat == AccountCategory.EXPENSE) {
                if (line.accountId == "acc_disbursed" || line.accountId == "acc_sedekah_extra") {
                    disbursed += line.debit
                } else {
                    consumption += line.debit
                    expPairs.add(line.accountId to line.debit)
                }
            }
            if (entry.transactionType == "INFAQ_PAYOUT" && line.debit > 0.0) {
                if (line.accountId == "acc_vault" || line.accountId == "acc_disbursed" ||
                    line.accountId == "acc_sedekah_extra" || line.accountId == "acc_infaq_expense"
                ) {
                    disbursed += line.debit
                }
            }
        }

        val bucket = when (entry.transactionType) {
            "INFLOW" -> if (nonKasab > kasab) TraceBucket.NON_KASAB else TraceBucket.KASAB
            "EXPENSE" -> if (disbursed > 0.0 && consumption == 0.0) TraceBucket.INFAQ_ZAKAT else TraceBucket.KONSUMSI
            "INFAQ_PAYOUT" -> TraceBucket.INFAQ_ZAKAT
            "TRANSFER", "QARDH" -> TraceBucket.TRANSFER_QARDH
            else -> TraceBucket.TRANSFER_QARDH
        }

        val primaryAmount = when (bucket) {
            TraceBucket.KASAB -> if (kasab > 0.0) kasab else entry.totalDebit
            TraceBucket.NON_KASAB -> if (nonKasab > 0.0) nonKasab else entry.totalDebit
            TraceBucket.KONSUMSI -> if (consumption > 0.0) consumption else entry.totalDebit
            TraceBucket.INFAQ_ZAKAT -> if (disbursed > 0.0) disbursed else entry.totalDebit
            TraceBucket.TRANSFER_QARDH, TraceBucket.SEMUA -> entry.totalDebit
        }

        return ClassifiedEntry(
            item = TraceableJournalItem(
                id = entry.id,
                date = entry.gregorianDate,
                hijriLabel = hijriStr,
                description = entry.description.ifBlank { entry.transactionType },
                transactionType = entry.transactionType,
                bucket = bucket,
                amount = primaryAmount,
                infaqAllocated = vaultIn,
                debitAccounts = debitNames.ifBlank { "-" },
                creditAccounts = creditNames.ifBlank { "-" }
            ),
            kasabAmount = kasab,
            nonKasabAmount = nonKasab,
            consumptionAmount = consumption,
            infaqAllocatedAmount = vaultIn,
            infaqDisbursedAmount = disbursed,
            expenseLines = expPairs
        )
    }

    private fun filterByPeriod(
        entries: List<JournalEntry>,
        filter: TracePeriodFilter,
        nowMillis: Long
    ): List<JournalEntry> {
        if (filter == TracePeriodFilter.ALL_TIME) return entries
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val cutoffMillis = when (filter) {
            TracePeriodFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            TracePeriodFilter.LAST_30_DAYS -> nowMillis - (30L * 24 * 60 * 60 * 1000)
            TracePeriodFilter.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            TracePeriodFilter.ALL_TIME -> 0L
        }
        return entries.filter { it.gregorianDate.time in cutoffMillis..nowMillis }
    }

    private fun computeAverageMonthlyConsumption(
        entries: List<JournalEntry>,
        accountMap: Map<String, Account>,
        nowMillis: Long
    ): Double {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val totals = mutableListOf<Double>()
        for (offset in 0..2) {
            val mCal = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                add(Calendar.MONTH, -offset)
            }
            val m = mCal.get(Calendar.MONTH)
            val y = mCal.get(Calendar.YEAR)
            var monthExp = 0.0
            for (e in entries) {
                if (e.transactionType == "EXPENSE") {
                    val eCal = Calendar.getInstance().apply { time = e.gregorianDate }
                    if (eCal.get(Calendar.MONTH) == m && eCal.get(Calendar.YEAR) == y) {
                        for (line in e.lines) {
                            if (line.debit > 0.0 &&
                                accountMap[line.accountId]?.category == AccountCategory.EXPENSE &&
                                line.accountId != "acc_disbursed" &&
                                line.accountId != "acc_sedekah_extra"
                            ) {
                                monthExp += line.debit
                            }
                        }
                    }
                }
            }
            if (monthExp > 0.0) totals.add(monthExp)
        }
        return if (totals.isEmpty()) 0.0 else totals.average()
    }

    private fun computeMonthOverMonth(
        entries: List<JournalEntry>,
        accountMap: Map<String, Account>,
        nowMillis: Long
    ): Triple<PeriodDeltaMetric, PeriodDeltaMetric, PeriodDeltaMetric> {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val curM = nowCal.get(Calendar.MONTH)
        val curY = nowCal.get(Calendar.YEAR)
        val prevCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.MONTH, -1)
        }
        val prevM = prevCal.get(Calendar.MONTH)
        val prevY = prevCal.get(Calendar.YEAR)

        var curInc = 0.0; var prevInc = 0.0
        var curExp = 0.0; var prevExp = 0.0
        var curInf = 0.0; var prevInf = 0.0

        for (e in entries) {
            val c = Calendar.getInstance().apply { time = e.gregorianDate }
            val em = c.get(Calendar.MONTH)
            val ey = c.get(Calendar.YEAR)
            val isCur = (em == curM && ey == curY)
            val isPrev = (em == prevM && ey == prevY)
            if (!isCur && !isPrev) continue

            val cls = classifyEntry(e, accountMap)
            val inc = cls.kasabAmount + cls.nonKasabAmount
            val exp = cls.consumptionAmount
            val inf = cls.infaqAllocatedAmount + cls.infaqDisbursedAmount
            if (isCur) {
                curInc += inc; curExp += exp; curInf += inf
            } else {
                prevInc += inc; prevExp += exp; prevInf += inf
            }
        }
        return Triple(
            PeriodDeltaMetric(curInc, prevInc),
            PeriodDeltaMetric(curExp, prevExp),
            PeriodDeltaMetric(curInf, prevInf)
        )
    }

    private fun evaluateHealthStatus(
        totalIncome: Double,
        consumptionRatioPercent: Double,
        savingsRatePercent: Double,
        debtToAssetRatioPercent: Double,
        topExpenseCategoryName: String
    ): Pair<String, String> {
        return when {
            totalIncome <= 0.0 && consumptionRatioPercent <= 0.0 ->
                "Belum Ada Data Arus Kas" to "Catat pemasukan atau pengeluaran agar rasio kesehatan keuangan dan jejak audit terhitung."
            savingsRatePercent < 0.0 || consumptionRatioPercent > 100.0 ->
                "Defisit Arus Kas" to "Belanja melampaui pemasukan pada periode ini. Fokus evaluasi pada pos $topExpenseCategoryName."
            consumptionRatioPercent > 80.0 ->
                "Waspada Israf (>80%)" to "Porsi konsumsi cukup tinggi; sisihkan tabungan lebih awal dan tinjau pos $topExpenseCategoryName."
            debtToAssetRatioPercent > 50.0 ->
                "Beban Kewajiban Tinggi" to "Rasio kewajiban Qardh terhadap kas likuid di atas 50%; prioritaskan pelunasan bertahap."
            abs(savingsRatePercent) >= 20.0 ->
                "Sangat Sehat & Produktif" to "Rasio tabungan bersih di atas 20% dengan pengendalian konsumsi yang baik."
            else ->
                "Seimbang & Terkendali" to "Arus kas berada dalam kondisi positif; pertahankan konsistensi alokasi infaq dan anggaran."
        }
    }
}

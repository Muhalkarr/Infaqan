package com.example.core.budget

import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import java.util.Calendar
import kotlin.math.roundToInt

enum class FinancialGoalMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val essentialWeight: Double,
    val discretionaryWeight: Double,
    val savingsInfaqWeight: Double
) {
    BALANCED_50_30_20(
        title = "Keseimbangan Syariah (50/30/20)",
        subtitle = "Pola Ideal Hidup Berkah",
        description = "50% Kebutuhan Pokok, 30% Kebutuhan Penunjang/Lifestyle Halal, 20% Penyucian (Infaq/Zakat) & Tabungan Emas.",
        essentialWeight = 0.50,
        discretionaryWeight = 0.30,
        savingsInfaqWeight = 0.20
    ),
    AGGRESSIVE_SAVINGS(
        title = "Tabungan Agresif & Porsi Haji",
        subtitle = "Akselerasi Dana Darurat & Haji",
        description = "Memangkas pos konsumsi sekunder hingga 20% untuk mengamankan biaya ibadah Haji/Umrah dan tabungan emas.",
        essentialWeight = 0.45,
        discretionaryWeight = 0.15,
        savingsInfaqWeight = 0.40
    ),
    MAX_SPIRITUAL_LIQUIDITY(
        title = "Kedermawanan & Tabarru' Maksimal",
        subtitle = "Optimasi Infaq & Berkah Kasab",
        description = "Mengurangi pengeluaran berlebih (anti-israf) dan mengalihkan kelebihan kas ke Virtual Infaq Vault & Sedekah Subuh.",
        essentialWeight = 0.45,
        discretionaryWeight = 0.25,
        savingsInfaqWeight = 0.30
    )
}

enum class SuggestionStatus {
    RECOMMEND_CUT,      // Spending exceeds optimal or high variance
    OPTIMAL,            // Well balanced
    RECOMMEND_EXPAND    // Underutilized or unrealistic cap
}

data class BudgetCapSuggestion(
    val accountId: String,
    val categoryName: String,
    val currentLimit: Double,
    val actualMonthlyAverage: Double,
    val suggestedCap: Double,
    val monthlyDelta: Double, // suggestedCap - currentLimit
    val projectedAnnualSavings: Double,
    val status: SuggestionStatus,
    val reasoning: String,
    val iconKey: String = "shopping"
)

data class SpendingPatternAnalysis(
    val selectedGoalMode: FinancialGoalMode,
    val totalIncomeEstimate: Double,
    val totalMonthlyBurnRate: Double,
    val essentialSpending: Double,
    val discretionarySpending: Double,
    val spiritualSpending: Double,
    val essentialRatio: Double,
    val discretionaryRatio: Double,
    val spiritualRatio: Double,
    val suggestions: List<BudgetCapSuggestion>,
    val totalOptimizedBudgetCap: Double,
    val potentialMonthlySavings: Double,
    val overallInsight: String
)

object BudgetOptimizerEngine {

    fun analyzeAndOptimize(
        accounts: List<Account>,
        budgets: List<BudgetAllocation>,
        journalEntries: List<JournalEntry>,
        goalMode: FinancialGoalMode = FinancialGoalMode.BALANCED_50_30_20
    ): SpendingPatternAnalysis {
        // 1. Hitung total pemasukan estimasi
        val totalIncome = journalEntries
            .filter { it.transactionType == "INFLOW" }
            .flatMap { it.lines }
            .filter { line ->
                val cat = accounts.firstOrNull { it.id == line.accountId }?.category
                cat == AccountCategory.INCOME_KASAB || cat == AccountCategory.INCOME_NON_KASAB
            }
            .sumOf { it.credit }
            .let { if (it <= 0.0) 15000000.0 else it }

        // 2. Hitung pengeluaran per kategori akun
        val expenseEntries = journalEntries.filter { it.transactionType == "EXPENSE" }
        val categorySpends = mutableMapOf<String, Double>()

        for (entry in expenseEntries) {
            for (line in entry.lines) {
                val acc = accounts.firstOrNull { it.id == line.accountId }
                if (acc != null && acc.category == AccountCategory.EXPENSE && acc.id != "acc_disbursed") {
                    categorySpends[acc.id] = (categorySpends[acc.id] ?: 0.0) + line.debit
                }
            }
        }

        // Hitung total pengeluaran infaq/tabarru'
        val spiritualSpending = journalEntries
            .flatMap { it.lines }
            .filter { it.accountId == "acc_vault" }
            .sumOf { it.credit }

        val totalBurnRate = categorySpends.values.sum()

        // Klasifikasi esensial vs diskresioner
        val essentialAccountIds = setOf("acc_living", "acc_utility", "acc_education", "acc_health")
        val essentialSpending = categorySpends.filterKeys { it in essentialAccountIds }.values.sum()
        val discretionarySpending = categorySpends.filterKeys { it !in essentialAccountIds }.values.sum()

        val totalAllOutflow = totalBurnRate + spiritualSpending
        val essentialRatio = if (totalAllOutflow > 0) (essentialSpending / totalAllOutflow) * 100 else 50.0
        val discretionaryRatio = if (totalAllOutflow > 0) (discretionarySpending / totalAllOutflow) * 100 else 30.0
        val spiritualRatio = if (totalAllOutflow > 0) (spiritualSpending / totalAllOutflow) * 100 else 20.0

        // 3. Bangun saran optimasi per kategori budget
        val suggestions = mutableListOf<BudgetCapSuggestion>()
        var potentialMonthlySavings = 0.0

        for (budget in budgets) {
            val actualSpent = categorySpends[budget.accountId] ?: (budget.monthlyLimit * 0.75)
            val isEssential = budget.accountId in essentialAccountIds

            val suggestedCap: Double
            val reasoning: String
            val status: SuggestionStatus

            when (goalMode) {
                FinancialGoalMode.BALANCED_50_30_20 -> {
                    if (isEssential) {
                        // Esensial: toleransi 1.1x dari actual rata-rata dengan batas atas proporsional
                        val optimal = roundToNearestTenThousand(maxOf(actualSpent * 1.10, budget.monthlyLimit * 0.95))
                        suggestedCap = optimal
                        if (suggestedCap < budget.monthlyLimit) {
                            val saved = budget.monthlyLimit - suggestedCap
                            reasoning = "Pengeluaran pangan & operasional stabil. Pagu dapat dioptimalkan turun Rp ${formatSimple(saved)} agar alokasi lebih presisi."
                            status = SuggestionStatus.RECOMMEND_CUT
                        } else {
                            reasoning = "Pagu sesuai dengan kebutuhan esensial keluarga untuk menjaga kelancaran konsumsi halal."
                            status = SuggestionStatus.OPTIMAL
                        }
                    } else {
                        // Diskresioner: optimalkan 0.85x dari limit saat ini
                        val optimal = roundToNearestTenThousand(minOf(actualSpent * 1.05, budget.monthlyLimit * 0.85))
                        suggestedCap = optimal
                        if (suggestedCap < budget.monthlyLimit) {
                            val saved = budget.monthlyLimit - suggestedCap
                            reasoning = "Pos belanja gaya hidup dapat dihemat Rp ${formatSimple(saved)}/bulan untuk memperkuat dana cadangan & sedekah."
                            status = SuggestionStatus.RECOMMEND_CUT
                        } else {
                            reasoning = "Anggaran gaya hidup berada dalam rentang wajar anti-israf."
                            status = SuggestionStatus.OPTIMAL
                        }
                    }
                }
                FinancialGoalMode.AGGRESSIVE_SAVINGS -> {
                    if (isEssential) {
                        val optimal = roundToNearestTenThousand(actualSpent * 1.02)
                        suggestedCap = optimal
                        reasoning = "Batas ketat untuk kebutuhan primer guna mengakselerasi porsi setoran Haji & tabungan emas."
                        status = if (suggestedCap < budget.monthlyLimit) SuggestionStatus.RECOMMEND_CUT else SuggestionStatus.OPTIMAL
                    } else {
                        val optimal = roundToNearestTenThousand(actualSpent * 0.75)
                        suggestedCap = optimal
                        val saved = (budget.monthlyLimit - suggestedCap).coerceAtLeast(0.0)
                        reasoning = "Pengurangan drastis 25% pada pos sekunder dapat menghasilkan surplus Rp ${formatSimple(saved)}/bln."
                        status = SuggestionStatus.RECOMMEND_CUT
                    }
                }
                FinancialGoalMode.MAX_SPIRITUAL_LIQUIDITY -> {
                    val optimal = roundToNearestTenThousand(actualSpent * 0.90)
                    suggestedCap = optimal
                    val saved = (budget.monthlyLimit - suggestedCap).coerceAtLeast(0.0)
                    reasoning = "Penyederhanaan konsumsi (zuhud) menyisakan kuota Rp ${formatSimple(saved)} untuk disalurkan ke Mustahiq Vault."
                    status = if (suggestedCap < budget.monthlyLimit) SuggestionStatus.RECOMMEND_CUT else SuggestionStatus.OPTIMAL
                }
            }

            val delta = suggestedCap - budget.monthlyLimit
            val annualSavings = if (delta < 0) -delta * 12 else 0.0
            if (delta < 0) {
                potentialMonthlySavings += -delta
            }

            suggestions.add(
                BudgetCapSuggestion(
                    accountId = budget.accountId,
                    categoryName = budget.categoryName,
                    currentLimit = budget.monthlyLimit,
                    actualMonthlyAverage = actualSpent,
                    suggestedCap = suggestedCap,
                    monthlyDelta = delta,
                    projectedAnnualSavings = annualSavings,
                    status = status,
                    reasoning = reasoning,
                    iconKey = budget.iconKey
                )
            )
        }

        val totalOptimizedCap = suggestions.sumOf { it.suggestedCap }

        val overallInsight = when (goalMode) {
            FinancialGoalMode.BALANCED_50_30_20 -> {
                "Pola pengeluaran Anda saat ini memiliki rasio ${essentialRatio.roundToInt()}% Kebutuhan, ${discretionaryRatio.roundToInt()}% Lifestyle, dan ${spiritualRatio.roundToInt()}% Spiritual. Menerapkan rekomendasi pagu di bawah dapat menghemat Rp ${formatSimple(potentialMonthlySavings)} per bulan untuk mempercepat pencapaian Nisab Zakat & Emas."
            }
            FinancialGoalMode.AGGRESSIVE_SAVINGS -> {
                "Strategi Tabungan Agresif memangkas pos non-esensial dan mengamankan potensi surplus Rp ${formatSimple(potentialMonthlySavings)} per bulan (Rp ${formatSimple(potentialMonthlySavings * 12)} / tahun) untuk tabungan Emas & Porsi Haji."
            }
            FinancialGoalMode.MAX_SPIRITUAL_LIQUIDITY -> {
                "Pola Kedermawanan Maksimal mengalihkan kelebihan konsumsi ke Virtual Infaq Vault, meningkatkan Spiritual Liquidity Index (SLI) Anda hingga di atas 15%."
            }
        }

        return SpendingPatternAnalysis(
            selectedGoalMode = goalMode,
            totalIncomeEstimate = totalIncome,
            totalMonthlyBurnRate = totalBurnRate,
            essentialSpending = essentialSpending,
            discretionarySpending = discretionarySpending,
            spiritualSpending = spiritualSpending,
            essentialRatio = essentialRatio,
            discretionaryRatio = discretionaryRatio,
            spiritualRatio = spiritualRatio,
            suggestions = suggestions,
            totalOptimizedBudgetCap = totalOptimizedCap,
            potentialMonthlySavings = potentialMonthlySavings,
            overallInsight = overallInsight
        )
    }

    private fun roundToNearestTenThousand(value: Double): Double {
        val step = 50000.0
        return (Math.round(value / step) * step).coerceAtLeast(100000.0)
    }

    private fun formatSimple(amount: Double): String {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(amount.toLong())
    }
}

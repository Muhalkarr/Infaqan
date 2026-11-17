package com.example.core.infaq

import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import kotlin.math.abs
import kotlin.math.round

/**
 * Analisa komprehensif Spiritual Liquidity Index (SLI), Rasio Infaq terhadap Pendapatan,
 * rincian asal nominal infaq, dan audit kewajiban infaq berdasarkan aturan aktif.
 */
data class InfaqSliComprehensiveAnalysis(
    // Basis Arus Kas
    val totalIncomeKasab: Double = 0.0,
    val totalIncomeNonKasab: Double = 0.0,
    val totalAllIncome: Double = 0.0,
    val totalConsumptionExpense: Double = 0.0,

    // Rincian Sumber Pembentuk Nominal Infaq (Vault & Non-Vault)
    val infaqFromIncomeAllocation: Double = 0.0,
    val infaqFromExpenseRoundUp: Double = 0.0,
    val infaqFromNonCashRezekiVault: Double = 0.0,
    val totalAllocatedToVault: Double = 0.0, // = totalPurifiedInfaq
    val vaultDisbursedAmount: Double = 0.0,
    val vaultRemainingPayable: Double = 0.0,

    // Kanal Kedermawanan Langsung (Di luar aliran masuk Vault)
    val directSedekahSubuhAmount: Double = 0.0,
    val directExtraAndRezekiInfaqAmount: Double = 0.0,
    val zakatDisbursedAmount: Double = 0.0,
    val totalInfaqAllChannels: Double = 0.0,

    // 4 Rasio Persentase Transparan (Menjawab perbedaan basis Konsumsi vs Pendapatan)
    val sliConsumptionBasedPercent: Double = 0.0,        // Infaq Vault / Belanja Konsumsi * 100
    val infaqToTotalIncomePercent: Double = 0.0,         // Infaq Vault / Total Pendapatan * 100
    val infaqToKasabIncomePercent: Double = 0.0,         // Infaq Vault / Pendapatan Kasab * 100
    val allChannelsToTotalIncomePercent: Double = 0.0,   // (Infaq Vault + Sedekah Langsung) / Total Pendapatan * 100
    val allChannelsConsumptionSliPercent: Double = 0.0,  // (Infaq Vault + Sedekah Langsung) / Belanja Konsumsi * 100

    // Audit Target & Nominal yang Harus Dibayarkan berdasarkan InfaqRule
    val kasabRuleRatePercent: Double = 5.0,
    val nonKasabRuleRatePercent: Double = 10.0,
    val expectedInfaqFromKasabRules: Double = 0.0,
    val expectedInfaqFromNonKasabRules: Double = 0.0,
    val expectedRikazAndSyubhatObligation: Double = 0.0,
    val expectedTotalInfaqFromIncomeRules: Double = 0.0,
    val expectedBlendedIncomeRatePercent: Double = 0.0,
    val allocationGapAgainstRules: Double = 0.0, // (+) lebih dari target aturan, (-) kurang dari target aturan
    val underAllocationShortfall: Double = 0.0,  // Kekurangan alokasi terhadap target aturan (>= 0)
    val combinedMustPayAmount: Double = 0.0      // Sisa Vault yang belum disalurkan + Kekurangan alokasi aturan
) {
    val isSliUsingZeroConsumptionFallback: Boolean
        get() = totalConsumptionExpense <= 0.0 && totalAllocatedToVault > 0.0

    val sliGapPercentagePoints: Double
        get() = sliConsumptionBasedPercent - infaqToTotalIncomePercent

    val isMeetingIncomeRuleTarget: Boolean
        get() = underAllocationShortfall <= 0.5
}

object InfaqSliAnalysisCalculator {

    fun analyze(
        accounts: List<Account>,
        journalEntries: List<JournalEntry>,
        rules: List<InfaqRule>,
        totalIncomeKasab: Double,
        totalIncomeNonKasab: Double,
        totalConsumptionExpense: Double,
        totalPurifiedInfaq: Double,
        virtualInfaqVaultBalance: Double
    ): InfaqSliComprehensiveAnalysis {
        val accountCategoryMap = accounts.associate { it.id to it.category }
        val totalAllIncome = (totalIncomeKasab + totalIncomeNonKasab).coerceAtLeast(0.0)

        var infaqFromIncomeAllocation = 0.0
        var infaqFromExpenseRoundUp = 0.0
        var infaqFromNonCashRezekiVault = 0.0
        var vaultDisbursedAmount = 0.0

        var directSedekahSubuhAmount = 0.0
        var directExtraAndRezekiInfaqAmount = 0.0
        var zakatDisbursedAmount = 0.0

        var rikazInflowAmount = 0.0
        var syubhatInflowAmount = 0.0
        var regularNonKasabInflowAmount = 0.0

        for (entry in journalEntries) {
            val isZakatEntry = entry.description.contains("zakat", ignoreCase = true)

            when (entry.transactionType) {
                "INFLOW" -> {
                    for (line in entry.lines) {
                        if (line.accountId == "acc_vault" && line.credit > 0.0) {
                            infaqFromIncomeAllocation += line.credit
                        }
                        if (line.credit > 0.0 && accountCategoryMap[line.accountId] == AccountCategory.INCOME_NON_KASAB) {
                            when (line.accountId) {
                                "acc_rikaz" -> rikazInflowAmount += line.credit
                                "acc_syubhat" -> syubhatInflowAmount += line.credit
                                else -> regularNonKasabInflowAmount += line.credit
                            }
                        }
                    }
                }

                "EXPENSE" -> {
                    for (line in entry.lines) {
                        if (line.accountId == "acc_vault" && line.credit > 0.0) {
                            infaqFromExpenseRoundUp += line.credit
                        }
                        if ((line.accountId == "acc_disbursed" || line.accountId == "acc_sedekah_extra") && line.debit > 0.0) {
                            directSedekahSubuhAmount += line.debit
                        }
                    }
                }

                "INFAQ_PAYOUT" -> {
                    for (line in entry.lines) {
                        if (line.accountId == "acc_vault") {
                            if (line.credit > 0.0) infaqFromNonCashRezekiVault += line.credit
                            if (line.debit > 0.0) vaultDisbursedAmount += line.debit
                        } else if (
                            (line.accountId == "acc_disbursed" ||
                                line.accountId == "acc_sedekah_extra" ||
                                line.accountId == "acc_infaq_expense") && line.debit > 0.0
                        ) {
                            if (isZakatEntry) {
                                zakatDisbursedAmount += line.debit
                            } else {
                                directExtraAndRezekiInfaqAmount += line.debit
                            }
                        }
                    }
                }
            }
        }

        val totalAllocatedToVault = totalPurifiedInfaq.coerceAtLeast(0.0)
        val vaultRemainingPayable = virtualInfaqVaultBalance.coerceAtLeast(0.0)
        val totalInfaqAllChannels = totalAllocatedToVault + directSedekahSubuhAmount + directExtraAndRezekiInfaqAmount

        // Rasio SLI (Infaq / Konsumsi)
        val sliConsumptionBasedPercent = if (totalConsumptionExpense <= 0.0) {
            if (totalAllocatedToVault > 0.0) 100.0 else 0.0
        } else {
            (totalAllocatedToVault / totalConsumptionExpense) * 100.0
        }

        val allChannelsConsumptionSliPercent = if (totalConsumptionExpense <= 0.0) {
            if (totalInfaqAllChannels > 0.0) 100.0 else 0.0
        } else {
            (totalInfaqAllChannels / totalConsumptionExpense) * 100.0
        }

        // Rasio terhadap Total Pendapatan (Kasab + Non-Kasab)
        val infaqToTotalIncomePercent = if (totalAllIncome > 0.0) {
            (totalAllocatedToVault / totalAllIncome) * 100.0
        } else 0.0

        val infaqToKasabIncomePercent = if (totalIncomeKasab > 0.0) {
            (totalAllocatedToVault / totalIncomeKasab) * 100.0
        } else 0.0

        val allChannelsToTotalIncomePercent = if (totalAllIncome > 0.0) {
            (totalInfaqAllChannels / totalAllIncome) * 100.0
        } else 0.0

        // Hitung target aturan aktif (InfaqRule)
        val kasabRuleRate = rules
            .firstOrNull { it.targetCategory == AccountCategory.INCOME_KASAB && it.calculationType == InfaqCalculationType.PERCENTAGE }
            ?.rate ?: 0.05

        val nonKasabRuleRate = rules
            .firstOrNull { it.targetCategory == AccountCategory.INCOME_NON_KASAB && it.calculationType == InfaqCalculationType.PERCENTAGE }
            ?.rate ?: 0.10

        val rikazRate = rules
            .firstOrNull { it.calculationType == InfaqCalculationType.RIKAZ }
            ?.rate ?: 0.20

        val syubhatRate = rules
            .firstOrNull { it.calculationType == InfaqCalculationType.SYUBHAT }
            ?.rate ?: 1.00

        // Jika `regularNonKasabInflowAmount` belum terakumulasi karena perbedaan mapping, fallback ke selisih totalIncomeNonKasab
        val nonRikazSyubhatNonKasab = if (regularNonKasabInflowAmount > 0.0) {
            regularNonKasabInflowAmount
        } else {
            (totalIncomeNonKasab - rikazInflowAmount - syubhatInflowAmount).coerceAtLeast(0.0)
        }

        val expectedFromKasab = round(totalIncomeKasab * kasabRuleRate)
        val expectedFromRegularNonKasab = round(nonRikazSyubhatNonKasab * nonKasabRuleRate)
        val expectedRikazAndSyubhat = round((rikazInflowAmount * rikazRate) + (syubhatInflowAmount * syubhatRate))
        val expectedTotalFromIncomeRules = expectedFromKasab + expectedFromRegularNonKasab + expectedRikazAndSyubhat

        val expectedBlendedIncomeRatePercent = if (totalAllIncome > 0.0) {
            (expectedTotalFromIncomeRules / totalAllIncome) * 100.0
        } else {
            kasabRuleRate * 100.0
        }

        val allocationGapAgainstRules = totalAllocatedToVault - expectedTotalFromIncomeRules
        val underAllocationShortfall = if (allocationGapAgainstRules < 0.0) abs(allocationGapAgainstRules) else 0.0
        val combinedMustPayAmount = vaultRemainingPayable + underAllocationShortfall

        return InfaqSliComprehensiveAnalysis(
            totalIncomeKasab = totalIncomeKasab,
            totalIncomeNonKasab = totalIncomeNonKasab,
            totalAllIncome = totalAllIncome,
            totalConsumptionExpense = totalConsumptionExpense,
            infaqFromIncomeAllocation = infaqFromIncomeAllocation,
            infaqFromExpenseRoundUp = infaqFromExpenseRoundUp,
            infaqFromNonCashRezekiVault = infaqFromNonCashRezekiVault,
            totalAllocatedToVault = totalAllocatedToVault,
            vaultDisbursedAmount = vaultDisbursedAmount,
            vaultRemainingPayable = vaultRemainingPayable,
            directSedekahSubuhAmount = directSedekahSubuhAmount,
            directExtraAndRezekiInfaqAmount = directExtraAndRezekiInfaqAmount,
            zakatDisbursedAmount = zakatDisbursedAmount,
            totalInfaqAllChannels = totalInfaqAllChannels,
            sliConsumptionBasedPercent = sliConsumptionBasedPercent,
            infaqToTotalIncomePercent = infaqToTotalIncomePercent,
            infaqToKasabIncomePercent = infaqToKasabIncomePercent,
            allChannelsToTotalIncomePercent = allChannelsToTotalIncomePercent,
            allChannelsConsumptionSliPercent = allChannelsConsumptionSliPercent,
            kasabRuleRatePercent = kasabRuleRate * 100.0,
            nonKasabRuleRatePercent = nonKasabRuleRate * 100.0,
            expectedInfaqFromKasabRules = expectedFromKasab,
            expectedInfaqFromNonKasabRules = expectedFromRegularNonKasab,
            expectedRikazAndSyubhatObligation = expectedRikazAndSyubhat,
            expectedTotalInfaqFromIncomeRules = expectedTotalFromIncomeRules,
            expectedBlendedIncomeRatePercent = expectedBlendedIncomeRatePercent,
            allocationGapAgainstRules = allocationGapAgainstRules,
            underAllocationShortfall = underAllocationShortfall,
            combinedMustPayAmount = combinedMustPayAmount
        )
    }
}

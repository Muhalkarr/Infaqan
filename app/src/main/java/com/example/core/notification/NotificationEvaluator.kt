package com.example.core.notification

import com.example.core.accounting.JournalEntry
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.infaq.InfaqCalculationType
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.SedekahSubuhState
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.scheduler.RecurringTransaction
import com.example.core.shariah.HaulCalculationMethod
import com.example.core.shariah.ShariahRulesConfig
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.round

data class DailyInfaqEvaluation(
    val shouldNotify: Boolean,
    val donatedTodayAmount: Double,
    val hasLoggedInfaqToday: Boolean,
    val vaultBalance: Double,
    val title: String,
    val shortBody: String,
    val expandedBody: String
)

data class DebtDueMatch(
    val record: QardhRecord,
    val daysRemaining: Int // negative = overdue
)

data class DebtAndRecurringEvaluation(
    val dueQardhMatches: List<DebtDueMatch>,
    val dueRecurringMatches: List<RecurringTransaction>,
    val shouldNotifyDebt: Boolean,
    val debtTitle: String,
    val debtShortBody: String,
    val debtExpandedBody: String,
    val debtDedupeKeys: List<String>,
    val shouldNotifyRecurring: Boolean,
    val recurringTitle: String,
    val recurringShortBody: String,
    val recurringExpandedBody: String,
    val recurringDedupeKeys: List<String>
)

enum class ZakatTriggerType {
    NONE,
    APPROACHING_NISAB,
    NISAB_REACHED,
    HAUL_MILESTONE,
    HAUL_DUE
}

data class ZakatHaulEvaluation(
    val netWealth: Double,
    val nisabThreshold: Double,
    val nisabProgressPercent: Int,
    val haulTargetDays: Int,
    val elapsedDays: Int,
    val daysRemaining: Int,
    val updatedHaulStartMillis: Long,
    val shouldResetHaul: Boolean,
    val triggerType: ZakatTriggerType,
    val triggerMilestoneDay: Int?,
    val title: String,
    val shortBody: String,
    val expandedBody: String
)

object NotificationEvaluator {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun startOfLocalDayMillis(timestampMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timestampMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun calculateVaultAndAssetWealth(entries: List<JournalEntry>): Pair<Double, Double> {
        var totalWalletBalance = 0.0
        var vaultCredit = 0.0
        var vaultDebit = 0.0
        val assetPrefixes = setOf(
            "acc_cash", "acc_bank", "acc_ewallet", "acc_gold",
            "acc_savings", "acc_other_asset", "acc_bank_konven", "acc_bank_special"
        )
        for (entry in entries) {
            for (line in entry.lines) {
                val accId = line.accountId
                if (accId == "acc_vault") {
                    vaultDebit += line.debit
                    vaultCredit += line.credit
                }
                if (assetPrefixes.contains(accId) || accId.startsWith("acc_wallet_") || accId.startsWith("acc_custom_")) {
                    totalWalletBalance += (line.debit - line.credit)
                }
            }
        }
        return Pair(totalWalletBalance.coerceAtLeast(0.0), round((vaultCredit - vaultDebit).coerceAtLeast(0.0)))
    }

    fun evaluateDailyInfaq(
        config: AmanahNotificationConfig,
        sedekahState: SedekahSubuhState,
        entries: List<JournalEntry>,
        rules: List<InfaqRule>,
        nowMillis: Long = System.currentTimeMillis(),
        isTest: Boolean = false
    ): DailyInfaqEvaluation {
        val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayKey = dayKeyFmt.format(Date(nowMillis))
        val todayStart = startOfLocalDayMillis(nowMillis)
        val tomorrowStart = todayStart + DAY_MS

        val sedekahToday = sedekahState.historyMap[todayKey] ?: 0.0
        val journalInfaqToday = entries
            .filter {
                it.gregorianDate.time in todayStart until tomorrowStart &&
                    (it.transactionType == "INFAQ_PAYOUT" ||
                        it.transactionType == "SEDEKAH_SUBUH" ||
                        it.description.contains("Sedekah Subuh", ignoreCase = true) ||
                        it.description.contains("Infaq", ignoreCase = true))
            }
            .sumOf { entry ->
                entry.lines.sumOf { it.debit }.takeIf { it > 0.0 } ?: 0.0
            }

        val donatedToday = sedekahToday + journalInfaqToday
        val hasLoggedToday = donatedToday > 0.0
        val (_, vaultBalance) = calculateVaultAndAssetWealth(entries)

        val shouldNotify = if (isTest) {
            true
        } else {
            config.isMasterEnabled &&
                config.dailyInfaq.enabled &&
                (!config.dailyInfaq.onlyIfNoInfaqLoggedToday || !hasLoggedToday)
        }

        val targetHint = config.dailyInfaq.targetDailyAmountHint
        val hijri = HijriCalendarEngine.fromGregorian(Date(nowMillis))
        val title = when {
            isTest -> "[Uji Coba] Pengingat Infaq Harian & Sedekah Subuh"
            hasLoggedToday -> "Alhamdulillah Infaq Hari Ini Tercatat (${AmanahNotificationConfig.formatRupiah(donatedToday)})"
            else -> "Waktunya Infaq Harian (${hijri.day} ${hijri.monthName})"
        }

        val shortBody = if (hasLoggedToday) {
            "Tercatat Rp ${AmanahNotificationConfig.formatRupiah(donatedToday).removePrefix("Rp ")} hari ini. Terus jaga keistiqomahan sedekah Anda."
        } else if (targetHint > 0.0) {
            "Mari awali hari ${hijri.day} ${hijri.monthName} dengan menyisihkan infaq minimal ${AmanahNotificationConfig.formatRupiah(targetHint)}."
        } else {
            "Jangan lewatkan keberkahan Sedekah Subuh & penyucian harta hari ini."
        }

        val expandedBody = buildString {
            append(shortBody)
            if (config.dailyInfaq.includeSedekahSubuhStreak) {
                append("\n🔥 Streak Sedekah Subuh: ${sedekahState.currentStreak} hari berturut-turut (Rekor: ${sedekahState.longestStreak} hari).")
            }
            if (config.dailyInfaq.includeVaultStatus && vaultBalance > 0.0) {
                append("\n🏦 Saldo Virtual Infaq Vault tersedia: ${AmanahNotificationConfig.formatRupiah(vaultBalance)}.")
            }
            if (config.dailyInfaq.includeActiveRulesSummary && rules.isNotEmpty()) {
                val activeRulesLabel = rules.take(2).joinToString(", ") { r ->
                    when (r.calculationType) {
                        InfaqCalculationType.PERCENTAGE -> "${r.title} (${(r.rate * 100).toInt()}%)"
                        InfaqCalculationType.ROUND_UP -> "${r.title} (Round-Up)"
                        InfaqCalculationType.FLAT -> "${r.title} (${AmanahNotificationConfig.formatRupiah(r.fixedAmount)})"
                        else -> r.title
                    }
                }
                append("\n⚙️ Aturan aktif: $activeRulesLabel.")
            }
            if (config.dailyInfaq.customNote.isNotBlank()) {
                append("\n📝 \"${config.dailyInfaq.customNote.trim()}\"")
            }
        }

        return DailyInfaqEvaluation(
            shouldNotify = shouldNotify,
            donatedTodayAmount = donatedToday,
            hasLoggedInfaqToday = hasLoggedToday,
            vaultBalance = vaultBalance,
            title = title,
            shortBody = shortBody,
            expandedBody = expandedBody
        )
    }

    fun evaluateDebtAndRecurring(
        config: AmanahNotificationConfig,
        notifyOnRecurringDue: Boolean,
        qardhRecords: List<QardhRecord>,
        recurringTransactions: List<RecurringTransaction>,
        nowMillis: Long = System.currentTimeMillis(),
        isTest: Boolean = false
    ): DebtAndRecurringEvaluation {
        val todayMidnight = startOfLocalDayMillis(nowMillis)
        val debtCfg = config.debt

        val scopedQardh = qardhRecords.filter { rec ->
            rec.status != QardhStatus.LUNAS &&
                rec.status != QardhStatus.DIIKHLASKAN_SEDEKAH &&
                rec.remainingAmount > 0.0 &&
                rec.dueDateMillis != null &&
                when (debtCfg.scope) {
                    DebtNotificationScope.BOTH -> true
                    DebtNotificationScope.HUTANG_ONLY -> rec.type == QardhType.HUTANG_SAYA
                    DebtNotificationScope.PIUTANG_ONLY -> rec.type == QardhType.PIUTANG_SAYA
                }
        }

        val matches = scopedQardh.mapNotNull { rec ->
            val dueMidnight = startOfLocalDayMillis(rec.dueDateMillis!!)
            val daysDiff = ((dueMidnight - todayMidnight) / DAY_MS).toInt()
            val matchesLead = daysDiff >= 0 && daysDiff in debtCfg.leadDays
            val matchesOverdue = daysDiff < 0 && debtCfg.remindOverdue
            if (isTest || matchesLead || matchesOverdue) {
                DebtDueMatch(record = rec, daysRemaining = daysDiff)
            } else null
        }.sortedBy { it.daysRemaining }

        val shouldNotifyDebt = if (isTest) true else {
            config.isMasterEnabled && debtCfg.enabled && matches.isNotEmpty()
        }

        val totalNominal = matches.sumOf { it.record.remainingAmount }
        val overdueCount = matches.count { it.daysRemaining < 0 }
        val debtTitle = when {
            isTest -> "[Uji Coba] Pengingat Amanah Hutang & Piutang"
            overdueCount > 0 -> "⚠️ $overdueCount Amanah Qardh Lewat Jatuh Tempo!"
            else -> "Pengingat Jatuh Tempo Qardh (${matches.size} Catatan)"
        }

        val debtShortBody = if (matches.isEmpty()) {
            "Semua catatan amanah Qardh (Hutang & Piutang) saat ini dalam kondisi terkendali."
        } else {
            val top = matches.first()
            val dirLabel = if (top.record.type == QardhType.HUTANG_SAYA) "Hutang ke" else "Piutang dari"
            val whenLabel = when {
                top.daysRemaining < 0 -> "telat ${-top.daysRemaining} hari"
                top.daysRemaining == 0 -> "jatuh tempo HARI INI"
                else -> "H-${top.daysRemaining}"
            }
            if (matches.size == 1) {
                "$dirLabel ${top.record.counterpartyName} (${AmanahNotificationConfig.formatRupiah(top.record.remainingAmount)}) — $whenLabel."
            } else {
                "$dirLabel ${top.record.counterpartyName} ($whenLabel) & ${matches.size - 1} lainnya. Total: ${AmanahNotificationConfig.formatRupiah(totalNominal)}."
            }
        }

        val debtExpandedBody = buildString {
            append(debtShortBody)
            matches.take(4).forEach { m ->
                val kind = if (m.record.type == QardhType.HUTANG_SAYA) "Hutang" else "Piutang"
                val timing = when {
                    m.daysRemaining < 0 -> "Lewat ${-m.daysRemaining}h"
                    m.daysRemaining == 0 -> "Hari Ini"
                    else -> "H-${m.daysRemaining}"
                }
                append("\n• [$kind] ${m.record.counterpartyName}: ${AmanahNotificationConfig.formatRupiah(m.record.remainingAmount)} ($timing)")
            }
            if (matches.size > 4) {
                append("\n• (+${matches.size - 4} catatan amanah lainnya)")
            }
            if (debtCfg.customNote.isNotBlank()) {
                append("\n📝 \"${debtCfg.customNote.trim()}\"")
            }
        }

        val debtDedupeKeys = matches.map { m ->
            if (m.daysRemaining < 0) {
                "qardh_${m.record.id}_overdue"
            } else {
                "qardh_${m.record.id}_h${m.daysRemaining}"
            }
        }

        val recThresholdMillis = todayMidnight + ((debtCfg.recurringLeadDays + 1).toLong() * DAY_MS) - 1L
        val dueRecurring = recurringTransactions.filter {
            it.isActive && it.nextDueDate.time <= recThresholdMillis
        }.sortedBy { it.nextDueDate.time }

        val shouldNotifyRecurring = if (isTest) false else {
            config.isMasterEnabled && debtCfg.enabled && debtCfg.includeRecurringBills && notifyOnRecurringDue && dueRecurring.isNotEmpty()
        }

        val recTitle = if (dueRecurring.size == 1) {
            "Tagihan Rutin: ${dueRecurring.first().title}"
        } else {
            "${dueRecurring.size} Transaksi Rutin Syariah Jatuh Tempo"
        }
        val recShortBody = if (dueRecurring.isNotEmpty()) {
            val totalRec = dueRecurring.sumOf { it.amount }
            "Terdapat ${dueRecurring.size} jadwal rutin (Total ${AmanahNotificationConfig.formatRupiah(totalRec)}) menunggu eksekusi."
        } else ""
        val recExpandedBody = buildString {
            append(recShortBody)
            dueRecurring.take(4).forEach { rec ->
                append("\n• ${rec.title} (${AmanahNotificationConfig.formatRupiah(rec.amount)})")
            }
        }
        val recDedupeKeys = dueRecurring.map { rec ->
            val dueDay = startOfLocalDayMillis(rec.nextDueDate.time)
            "rec_${rec.id}_$dueDay"
        }

        return DebtAndRecurringEvaluation(
            dueQardhMatches = matches,
            dueRecurringMatches = dueRecurring,
            shouldNotifyDebt = shouldNotifyDebt,
            debtTitle = debtTitle,
            debtShortBody = debtShortBody,
            debtExpandedBody = debtExpandedBody,
            debtDedupeKeys = debtDedupeKeys,
            shouldNotifyRecurring = shouldNotifyRecurring,
            recurringTitle = recTitle,
            recurringShortBody = recShortBody,
            recurringExpandedBody = recExpandedBody,
            recurringDedupeKeys = recDedupeKeys
        )
    }

    fun evaluateZakatHaul(
        config: AmanahNotificationConfig,
        shariahConfig: ShariahRulesConfig,
        goldPricePerGram: Double,
        walletBalance: Double,
        goalSavings: Double,
        qardhRecords: List<QardhRecord>,
        existingHaulStartMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        isTest: Boolean = false
    ): ZakatHaulEvaluation {
        val receivables = qardhRecords
            .filter { it.type == QardhType.PIUTANG_SAYA && it.status != QardhStatus.LUNAS && it.status != QardhStatus.DIIKHLASKAN_SEDEKAH }
            .sumOf { it.remainingAmount }
        val liabilities = qardhRecords
            .filter { it.type == QardhType.HUTANG_SAYA && it.status != QardhStatus.LUNAS && it.status != QardhStatus.DIIKHLASKAN_SEDEKAH }
            .sumOf { it.remainingAmount }

        val netWealth = (walletBalance + goalSavings + receivables - liabilities).coerceAtLeast(0.0)
        val goldGram = shariahConfig.goldNisabGram.takeIf { it > 0.0 } ?: 85.0
        val effectiveGoldPrice = goldPricePerGram.takeIf { it > 0.0 } ?: 1_350_000.0
        val nisabThreshold = goldGram * effectiveGoldPrice
        val progressPercent = if (nisabThreshold > 0.0) ((netWealth / nisabThreshold) * 100.0).toInt().coerceAtLeast(0) else 0

        val zakatCfg = config.zakatHaul
        val haulTargetDays = zakatCfg.calendarMode.fixedDays ?: when (shariahConfig.haulCalculationMethod) {
            HaulCalculationMethod.MASEHI -> 365
            HaulCalculationMethod.HIJRIAH -> 354
        }
        val effectiveRate = if (haulTargetDays >= 365) 0.02577 else 0.025
        val estimatedZakat = round(netWealth * effectiveRate)

        val approachingThreshold = (zakatCfg.approachingNisabPercent / 100.0) * nisabThreshold
        var updatedHaulStart = existingHaulStartMillis
        var shouldResetHaul = false

        if (netWealth >= nisabThreshold && updatedHaulStart <= 0L) {
            updatedHaulStart = nowMillis
        } else if (netWealth < approachingThreshold * 0.75 && updatedHaulStart > 0L) {
            updatedHaulStart = 0L
            shouldResetHaul = true
        }

        val elapsedDays = if (updatedHaulStart > 0L) {
            ((nowMillis - updatedHaulStart).coerceAtLeast(0L) / DAY_MS).toInt()
        } else 0
        val daysRemaining = if (updatedHaulStart > 0L) (haulTargetDays - elapsedDays) else haulTargetDays

        val triggerType: ZakatTriggerType
        var milestoneDay: Int? = null
        val isAllowedByConfig = config.isMasterEnabled && zakatCfg.enabled

        when {
            netWealth >= nisabThreshold && isAllowedByConfig -> {
                if (daysRemaining <= 0) {
                    triggerType = ZakatTriggerType.HAUL_DUE
                    milestoneDay = 0
                } else {
                    val matchedMilestone = zakatCfg.haulLeadDays
                        .filter { it > 0 }
                        .sorted()
                        .firstOrNull { lead -> daysRemaining <= lead }
                    if (matchedMilestone != null) {
                        triggerType = ZakatTriggerType.HAUL_MILESTONE
                        milestoneDay = matchedMilestone
                    } else if (existingHaulStartMillis <= 0L && updatedHaulStart > 0L) {
                        triggerType = ZakatTriggerType.NISAB_REACHED
                    } else {
                        triggerType = ZakatTriggerType.NONE
                    }
                }
            }
            zakatCfg.approachingNisabEnabled && isAllowedByConfig && netWealth >= approachingThreshold -> {
                triggerType = ZakatTriggerType.APPROACHING_NISAB
            }
            else -> {
                triggerType = ZakatTriggerType.NONE
            }
        }

        val calLabel = if (haulTargetDays >= 365) "365 hari Masehi (2.577%)" else "354 hari Hijriah (2.5%)"
        val title = when {
            isTest -> "[Uji Coba] Status Nisab & Jadwal Haul Zakat Maal"
            triggerType == ZakatTriggerType.HAUL_DUE -> "✨ Haul Zakat Maal Genap ($haulTargetDays Hari)!"
            triggerType == ZakatTriggerType.HAUL_MILESTONE -> "Pengingat Haul Zakat: $daysRemaining Hari Lagi (H-$milestoneDay)"
            triggerType == ZakatTriggerType.NISAB_REACHED -> "Harta Mencapai Nisab! Pelacakan Haul Dimulai"
            triggerType == ZakatTriggerType.APPROACHING_NISAB -> "Harta Mendekati Nisab Zakat ($progressPercent%)"
            else -> "Status Nisab & Haul Zakat Maal"
        }

        val shortBody = when {
            triggerType == ZakatTriggerType.HAUL_DUE ->
                "Harta bersih ${AmanahNotificationConfig.formatRupiah(netWealth)} telah genap $haulTargetDays hari. Estimasi zakat: ${AmanahNotificationConfig.formatRupiah(estimatedZakat)}."
            triggerType == ZakatTriggerType.HAUL_MILESTONE ->
                "Sisa $daysRemaining hari menuju haul genap ($calLabel). Harta bersih: ${AmanahNotificationConfig.formatRupiah(netWealth)}."
            triggerType == ZakatTriggerType.NISAB_REACHED ->
                "Harta bersih Anda (${AmanahNotificationConfig.formatRupiah(netWealth)}) menembus nisab ${AmanahNotificationConfig.formatRupiah(nisabThreshold)}. Hitung mundur $haulTargetDays hari dimulai."
            triggerType == ZakatTriggerType.APPROACHING_NISAB || isTest && netWealth < nisabThreshold ->
                "Harta bersih ${AmanahNotificationConfig.formatRupiah(netWealth)} ($progressPercent% dari nisab ${AmanahNotificationConfig.formatRupiah(nisabThreshold)})."
            else ->
                "Harta bersih ${AmanahNotificationConfig.formatRupiah(netWealth)} • Sisa $daysRemaining/$haulTargetDays hari haul."
        }

        val expandedBody = buildString {
            append(shortBody)
            append("\n📊 Rincian Hisab: Saldo Kas & Ibadah ${AmanahNotificationConfig.formatRupiah(walletBalance + goalSavings)}")
            if (receivables > 0.0 || liabilities > 0.0) {
                append(" | Piutang +${AmanahNotificationConfig.formatRupiah(receivables)} | Hutang -${AmanahNotificationConfig.formatRupiah(liabilities)}")
            }
            append("\n⚖️ Nisab Acuan (${goldGram.toInt()}g @ ${AmanahNotificationConfig.formatRupiah(effectiveGoldPrice)}/g): ${AmanahNotificationConfig.formatRupiah(nisabThreshold)}.")
            if (netWealth >= nisabThreshold) {
                append("\n💰 Estimasi Zakat ($calLabel): ${AmanahNotificationConfig.formatRupiah(estimatedZakat)}.")
            }
            if (zakatCfg.customNote.isNotBlank()) {
                append("\n📝 \"${zakatCfg.customNote.trim()}\"")
            }
        }

        return ZakatHaulEvaluation(
            netWealth = netWealth,
            nisabThreshold = nisabThreshold,
            nisabProgressPercent = progressPercent,
            haulTargetDays = haulTargetDays,
            elapsedDays = elapsedDays,
            daysRemaining = daysRemaining.coerceAtLeast(0),
            updatedHaulStartMillis = updatedHaulStart,
            shouldResetHaul = shouldResetHaul,
            triggerType = if (isTest && triggerType == ZakatTriggerType.NONE) {
                if (netWealth >= nisabThreshold) ZakatTriggerType.HAUL_MILESTONE else ZakatTriggerType.APPROACHING_NISAB
            } else triggerType,
            triggerMilestoneDay = milestoneDay,
            title = title,
            shortBody = shortBody,
            expandedBody = expandedBody
        )
    }
}

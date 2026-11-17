package com.example

import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
import com.example.core.backup.BackupEngine
import com.example.core.backup.BackupPreferencesSnapshot
import com.example.core.backup.RestoreResult
import com.example.core.budget.BudgetAllocation
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.infaq.InfaqCalculationType
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.SedekahSubuhState
import com.example.core.notification.AmanahNotificationConfig
import com.example.core.notification.DailyInfaqReminderConfig
import com.example.core.notification.DebtNotificationScope
import com.example.core.notification.DebtReminderConfig
import com.example.core.notification.NotificationEvaluator
import com.example.core.notification.ZakatHaulCalendarMode
import com.example.core.notification.ZakatHaulReminderConfig
import com.example.core.notification.ZakatTriggerType
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.rezeki.RezekiCategory
import com.example.core.rezeki.RezekiNonTunaiRecord
import com.example.core.scheduler.RecurringFrequency
import com.example.core.scheduler.RecurringTransaction
import com.example.core.scheduler.RecurringType
import com.example.core.shariah.HaulCalculationMethod
import com.example.core.shariah.ShariahRulesConfig
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletMutationRecord
import com.example.core.wallet.WalletType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NotificationAndBackupTest {

    @Test
    fun `quiet hours handles midnight wrap and daytime spans accurately`() {
        val wrapConfig = AmanahNotificationConfig(
            quietHoursEnabled = true,
            quietStartHour = 22,
            quietStartMinute = 0,
            quietEndHour = 4,
            quietEndMinute = 30
        )
        assertTrue(wrapConfig.isWithinQuietHours(22, 0))
        assertTrue(wrapConfig.isWithinQuietHours(23, 30))
        assertTrue(wrapConfig.isWithinQuietHours(0, 15))
        assertTrue(wrapConfig.isWithinQuietHours(3, 0))
        assertTrue(wrapConfig.isWithinQuietHours(4, 29))
        assertFalse(wrapConfig.isWithinQuietHours(4, 30))
        assertFalse(wrapConfig.isWithinQuietHours(12, 0))
        assertFalse(wrapConfig.isWithinQuietHours(21, 59))

        val dayConfig = AmanahNotificationConfig(
            quietHoursEnabled = true,
            quietStartHour = 13,
            quietStartMinute = 0,
            quietEndHour = 15,
            quietEndMinute = 0
        )
        assertTrue(dayConfig.isWithinQuietHours(13, 45))
        assertFalse(dayConfig.isWithinQuietHours(15, 0))
        assertFalse(dayConfig.isWithinQuietHours(23, 0))

        val disabled = wrapConfig.copy(quietHoursEnabled = false)
        assertFalse(disabled.isWithinQuietHours(23, 30))
    }

    @Test
    fun `notification config json round-trip preserves all custom settings`() {
        val original = AmanahNotificationConfig(
            isMasterEnabled = true,
            quietHoursEnabled = true,
            quietStartHour = 21,
            quietStartMinute = 45,
            quietEndHour = 4,
            quietEndMinute = 15,
            enableSound = false,
            enableVibration = true,
            useHighPriorityAlerts = false,
            dailyInfaq = DailyInfaqReminderConfig(
                enabled = true,
                hour = 4,
                minute = 55,
                onlyIfNoInfaqLoggedToday = false,
                includeSedekahSubuhStreak = true,
                includeVaultStatus = false,
                includeActiveRulesSummary = true,
                targetDailyAmountHint = 25000.0,
                customNote = "Niatkan untuk orang tua"
            ),
            debt = DebtReminderConfig(
                enabled = true,
                hour = 19,
                minute = 30,
                scope = DebtNotificationScope.HUTANG_ONLY,
                leadDays = setOf(14, 7, 1, 0),
                remindOverdue = false,
                overdueRepeatDays = 5,
                includeRecurringBills = true,
                recurringLeadDays = 3,
                customNote = "Lunasi hutang sebelum tidur"
            ),
            zakatHaul = ZakatHaulReminderConfig(
                enabled = true,
                hour = 10,
                minute = 15,
                approachingNisabEnabled = false,
                approachingNisabPercent = 90,
                approachingRepeatDays = 21,
                haulLeadDays = setOf(30, 7, 0),
                overdueRepeatDays = 10,
                calendarMode = ZakatHaulCalendarMode.MASEHI_365,
                customNote = "Salurkan ke BAZNAS terdekat"
            )
        )

        val json = original.toJson()
        val parsed = AmanahNotificationConfig.fromJson(json)
        assertEquals(original, parsed)
    }

    @Test
    fun `daily infaq evaluator respects logged infaq suppression and custom notes`() {
        val now = 1763352000000L // fixed timestamp
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
        val cfg = AmanahNotificationConfig(
            isMasterEnabled = true,
            dailyInfaq = DailyInfaqReminderConfig(
                enabled = true,
                onlyIfNoInfaqLoggedToday = true,
                customNote = "Istiqomah Subuh"
            )
        )
        val unloggedState = SedekahSubuhState(currentStreak = 3, longestStreak = 7)
        val rules = listOf(
            InfaqRule("r1", "Infaq Belanja", AccountCategory.EXPENSE, InfaqCalculationType.PERCENTAGE, rate = 0.05)
        )

        val evalUnlogged = NotificationEvaluator.evaluateDailyInfaq(
            config = cfg,
            sedekahState = unloggedState,
            entries = emptyList(),
            rules = rules,
            nowMillis = now
        )
        assertTrue(evalUnlogged.shouldNotify)
        assertFalse(evalUnlogged.hasLoggedInfaqToday)
        assertTrue(evalUnlogged.expandedBody.contains("Istiqomah Subuh"))

        val loggedState = unloggedState.copy(historyMap = mapOf(todayKey to 15000.0))
        val evalLoggedSuppressed = NotificationEvaluator.evaluateDailyInfaq(
            config = cfg,
            sedekahState = loggedState,
            entries = emptyList(),
            rules = rules,
            nowMillis = now
        )
        assertFalse(evalLoggedSuppressed.shouldNotify)
        assertTrue(evalLoggedSuppressed.hasLoggedInfaqToday)

        val evalAlwaysRemind = NotificationEvaluator.evaluateDailyInfaq(
            config = cfg.copy(dailyInfaq = cfg.dailyInfaq.copy(onlyIfNoInfaqLoggedToday = false)),
            sedekahState = loggedState,
            entries = emptyList(),
            rules = rules,
            nowMillis = now
        )
        assertTrue(evalAlwaysRemind.shouldNotify)
    }

    @Test
    fun `debt evaluator filters scope, lead days, overdue items, and recurring bills`() {
        val dayMs = 24L * 60L * 60L * 1000L
        val nowMidnight = NotificationEvaluator.startOfLocalDayMillis(System.currentTimeMillis())
        val now = nowMidnight + (8L * 60L * 60L * 1000L)

        val qardhHutangH3 = QardhRecord(
            id = "q_hutang",
            type = QardhType.HUTANG_SAYA,
            counterpartyName = "Ustadz Hasan",
            totalAmount = 1_000_000.0,
            remainingAmount = 500_000.0,
            startDateMillis = now - 10 * dayMs,
            dueDateMillis = nowMidnight + 3 * dayMs,
            status = QardhStatus.AKTIF
        )
        val qardhPiutangOverdue = QardhRecord(
            id = "q_piutang",
            type = QardhType.PIUTANG_SAYA,
            counterpartyName = "Fulan",
            totalAmount = 750_000.0,
            remainingAmount = 750_000.0,
            startDateMillis = now - 20 * dayMs,
            dueDateMillis = nowMidnight - 2 * dayMs,
            status = QardhStatus.AKTIF
        )
        val recDueTomorrow = RecurringTransaction(
            id = "rec_1",
            title = "SPP Sekolah Islam",
            type = RecurringType.EXPENSE,
            amount = 500_000.0,
            categoryAccountId = "acc_edu",
            assetAccountId = "acc_bank",
            frequency = RecurringFrequency.MONTHLY,
            dayOfMonthOrWeek = 10,
            nextDueDate = Date(nowMidnight + dayMs),
            isActive = true
        )

        val hutangOnlyCfg = AmanahNotificationConfig(
            debt = DebtReminderConfig(
                enabled = true,
                scope = DebtNotificationScope.HUTANG_ONLY,
                leadDays = setOf(7, 3, 1, 0),
                remindOverdue = true,
                includeRecurringBills = true,
                recurringLeadDays = 1
            )
        )
        val hutangEval = NotificationEvaluator.evaluateDebtAndRecurring(
            config = hutangOnlyCfg,
            notifyOnRecurringDue = true,
            qardhRecords = listOf(qardhHutangH3, qardhPiutangOverdue),
            recurringTransactions = listOf(recDueTomorrow),
            nowMillis = now
        )
        assertTrue(hutangEval.shouldNotifyDebt)
        assertEquals(1, hutangEval.dueQardhMatches.size)
        assertEquals("q_hutang", hutangEval.dueQardhMatches.first().record.id)
        assertEquals(3, hutangEval.dueQardhMatches.first().daysRemaining)
        assertTrue(hutangEval.shouldNotifyRecurring)
        assertEquals(1, hutangEval.dueRecurringMatches.size)

        val bothEval = NotificationEvaluator.evaluateDebtAndRecurring(
            config = hutangOnlyCfg.copy(debt = hutangOnlyCfg.debt.copy(scope = DebtNotificationScope.BOTH)),
            notifyOnRecurringDue = false,
            qardhRecords = listOf(qardhHutangH3, qardhPiutangOverdue),
            recurringTransactions = listOf(recDueTomorrow),
            nowMillis = now
        )
        assertEquals(2, bothEval.dueQardhMatches.size)
        assertFalse(bothEval.shouldNotifyRecurring)
    }

    @Test
    fun `zakat haul evaluator handles approaching nisab, haul start, hijri milestones, due, and reset`() {
        val dayMs = 24L * 60L * 60L * 1000L
        val now = 1763352000000L
        val goldPrice = 1_000_000.0 // Nisab 85g = 85,000,000
        val shariahCfg = ShariahRulesConfig(goldNisabGram = 85.0, haulCalculationMethod = HaulCalculationMethod.HIJRIAH)
        val notifCfg = AmanahNotificationConfig(
            zakatHaul = ZakatHaulReminderConfig(
                enabled = true,
                approachingNisabEnabled = true,
                approachingNisabPercent = 80,
                haulLeadDays = setOf(30, 14, 7, 3, 1, 0),
                calendarMode = ZakatHaulCalendarMode.FOLLOW_SHARIAH_CONFIG
            )
        )

        // 1. Approaching Nisab (82% = 70,000,000)
        val approaching = NotificationEvaluator.evaluateZakatHaul(
            config = notifCfg,
            shariahConfig = shariahCfg,
            goldPricePerGram = goldPrice,
            walletBalance = 70_000_000.0,
            goalSavings = 0.0,
            qardhRecords = emptyList(),
            existingHaulStartMillis = 0L,
            nowMillis = now
        )
        assertEquals(ZakatTriggerType.APPROACHING_NISAB, approaching.triggerType)
        assertEquals(0L, approaching.updatedHaulStartMillis)

        // 2. First time reaching Nisab (100,000,000)
        val reached = NotificationEvaluator.evaluateZakatHaul(
            config = notifCfg,
            shariahConfig = shariahCfg,
            goldPricePerGram = goldPrice,
            walletBalance = 100_000_000.0,
            goalSavings = 0.0,
            qardhRecords = emptyList(),
            existingHaulStartMillis = 0L,
            nowMillis = now
        )
        assertEquals(ZakatTriggerType.NISAB_REACHED, reached.triggerType)
        assertEquals(now, reached.updatedHaulStartMillis)
        assertEquals(354, reached.haulTargetDays)

        // 3. H-7 milestone (347 days elapsed out of 354)
        val milestone = NotificationEvaluator.evaluateZakatHaul(
            config = notifCfg,
            shariahConfig = shariahCfg,
            goldPricePerGram = goldPrice,
            walletBalance = 100_000_000.0,
            goalSavings = 0.0,
            qardhRecords = emptyList(),
            existingHaulStartMillis = now - (347L * dayMs),
            nowMillis = now
        )
        assertEquals(ZakatTriggerType.HAUL_MILESTONE, milestone.triggerType)
        assertEquals(7, milestone.daysRemaining)
        assertEquals(7, milestone.triggerMilestoneDay)

        // 4. Haul due (>= 354 days elapsed)
        val due = NotificationEvaluator.evaluateZakatHaul(
            config = notifCfg,
            shariahConfig = shariahCfg,
            goldPricePerGram = goldPrice,
            walletBalance = 100_000_000.0,
            goalSavings = 0.0,
            qardhRecords = emptyList(),
            existingHaulStartMillis = now - (355L * dayMs),
            nowMillis = now
        )
        assertEquals(ZakatTriggerType.HAUL_DUE, due.triggerType)

        // 5. Reset when net wealth drops significantly below nisab
        val reset = NotificationEvaluator.evaluateZakatHaul(
            config = notifCfg,
            shariahConfig = shariahCfg,
            goldPricePerGram = goldPrice,
            walletBalance = 20_000_000.0,
            goalSavings = 0.0,
            qardhRecords = emptyList(),
            existingHaulStartMillis = now - (100L * dayMs),
            nowMillis = now
        )
        assertTrue(reset.shouldResetHaul)
        assertEquals(0L, reset.updatedHaulStartMillis)
    }

    @Test
    fun `backup engine v4 encrypted round-trip preserves all data and notification preferences`() {
        val notifCfg = AmanahNotificationConfig(
            quietStartHour = 21,
            quietEndHour = 5,
            dailyInfaq = DailyInfaqReminderConfig(
                hour = 5,
                minute = 10,
                customNote = "Sedekah Subuh Keluarga"
            )
        )
        val prefs = BackupPreferencesSnapshot(
            userNameKasMukmin = "Keluarga Barokah",
            goldPricePerGram = 1480000.0,
            haulStartDateMillis = 1735689600000L,
            autoCloudBackupEnabled = true,
            notificationConfigJson = notifCfg.toJson()
        )
        val entries = listOf(
            JournalEntry(
                id = "j1",
                gregorianDate = Date(),
                hijriYear = 1448,
                hijriMonth = 5,
                hijriDay = 27,
                description = "Sedekah Pagi",
                transactionType = "INFAQ_PAYOUT",
                lines = listOf(
                    JournalLine("acc_infaq_expense", debit = 50000.0, credit = 0.0),
                    JournalLine("acc_cash", debit = 0.0, credit = 50000.0)
                )
            )
        )
        val wallets = listOf(WalletAccount("w1", "Dompet Tunai", WalletType.CASH))
        val goals = listOf(
            IbadahGoal(
                id = "g1",
                type = IbadahGoalType.QURBAN_KAMBING,
                title = "Qurban 1448H",
                targetAmount = 3500000.0,
                currentAccumulated = 1200000.0
            )
        )
        val budgets = listOf(BudgetAllocation("b1", "acc_living", "Konsumsi", 2000000.0))
        val rezeki = listOf(
            RezekiNonTunaiRecord(
                id = "rz1",
                title = "Oleh-oleh Kurma",
                giverName = "Tetangga",
                category = RezekiCategory.FOOD_BEVERAGE,
                estimatedValue = 120000.0,
                suggestedInfaqRate = 0.05,
                suggestedInfaqAmount = 6000.0,
                dateMillis = System.currentTimeMillis(),
                hijriDateString = "27 Jumadil Awal 1448"
            )
        )
        val mutations = listOf(
            WalletMutationRecord("m1", "w1", "acc_bank", 100000.0, note = "Setor tunai")
        )

        val pkg = BackupEngine.createBackupPackage(
            journalEntries = entries,
            wallets = wallets,
            goals = goals,
            budgets = budgets,
            rules = emptyList(),
            recurring = emptyList(),
            sedekahSubuhState = SedekahSubuhState(currentStreak = 4, longestStreak = 9, totalContributions = 200000.0),
            rezekiNonTunai = rezeki,
            walletMutations = mutations,
            preferences = prefs,
            password = "AmanahStrongPassword123!"
        )

        val manifest = BackupEngine.parseBackupPackage(pkg)
        assertNotNull(manifest)
        assertEquals(BackupEngine.CURRENT_SCHEMA_VERSION, manifest!!.schemaVersion)
        assertTrue(manifest.isPasswordProtected)
        assertTrue(manifest.hasPreferences)
        assertEquals(1, manifest.rezekiCount)
        assertEquals(1, manifest.walletMutationsCount)

        // Wrong password fails cleanly
        val wrongRestore = BackupEngine.parseAndRestore(pkg, "WrongPassword")
        assertTrue(wrongRestore is RestoreResult.Failure)

        // Correct password restores full data + preferences
        val restored = BackupEngine.parseAndRestore(pkg, "AmanahStrongPassword123!")
        assertTrue(restored is RestoreResult.Success)
        val success = restored as RestoreResult.Success
        assertEquals(1, success.restoredEntries.size)
        assertEquals(1, success.restoredRezekiNonTunai.size)
        assertEquals(1, success.restoredWalletMutations.size)
        assertNotNull(success.restoredPreferences)
        assertEquals("Keluarga Barokah", success.restoredPreferences!!.userNameKasMukmin)
        assertTrue(success.restoredPreferences!!.autoCloudBackupEnabled)
        val restoredNotifCfg = AmanahNotificationConfig.fromJson(success.restoredPreferences!!.notificationConfigJson)
        assertEquals(notifCfg, restoredNotifCfg)
    }

    @Test
    fun `backup engine parses legacy v1 backup seamlessly`() {
        val rawLegacyPayload = """{"journalEntries":[],"wallets":[{"id":"w_leg","name":"Kas Lama","type":"CASH"}],"ibadahGoals":[],"budgets":[],"infaqRules":[],"recurringTransactions":[],"sedekahSubuh":{"currentStreak":2,"longestStreak":2,"totalContributions":20000,"totalDaysGiven":2,"history":{}}}"""
        val payloadB64 = android.util.Base64.encodeToString(
            rawLegacyPayload.toByteArray(java.nio.charset.StandardCharsets.UTF_8),
            android.util.Base64.NO_WRAP
        )
        val checksum = BackupEngine.calculateSha256(rawLegacyPayload)
        val legacyPkg = org.json.JSONObject().apply {
            put("schemaVersion", 1)
            put("appName", "Amanah Ledger")
            put("exportedAt", "2024-12-01 08:00:00")
            put("entriesCount", 0)
            put("walletsCount", 1)
            put("goalsCount", 0)
            put("isEncrypted", false)
            put("checksum", checksum)
            put("payload", payloadB64)
        }.toString()

        val manifest = BackupEngine.parseBackupPackage(legacyPkg)
        assertNotNull(manifest)
        assertEquals(1, manifest!!.schemaVersion)
        assertTrue(manifest.isCompatible)
        assertTrue(manifest.migrationApplied)

        val res = BackupEngine.parseAndRestore(legacyPkg, "")
        assertTrue(res is RestoreResult.Success)
        val ok = res as RestoreResult.Success
        assertEquals(1, ok.restoredWallets.size)
        assertEquals("Kas Lama", ok.restoredWallets.first().name)
    }
}

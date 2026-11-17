package com.example

import com.example.core.changelog.ChangelogRegistry
import com.example.core.feedback.FeedbackRepository
import com.example.core.feedback.FeedbackType
import com.example.core.feedback.UserFeedback
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun numpad_zero_zero_zero_key_matching_logic() {
        // Test that "000" is correctly distinguished from "0".."9"
        val key = "000"
        var matchedBranch = ""

        when (key) {
            "000" -> matchedBranch = "ZERO_ZERO_ZERO"
            in "0".."9" -> matchedBranch = "SINGLE_DIGIT"
            else -> matchedBranch = "OTHER"
        }

        assertEquals("ZERO_ZERO_ZERO", matchedBranch)
    }

    @Test
    fun numpad_zero_zero_zero_expression_logic() {
        var expression = "5"
        val lastChar = expression.lastOrNull()
        if (lastChar != null && lastChar.isDigit() && expression.length < 22) {
            expression += "000"
        }
        assertEquals("5000", expression)

        // If expression is "0", pressing 000 should keep it "0"
        var zeroExpression = "0"
        if (zeroExpression == "0" || zeroExpression.isEmpty()) {
            zeroExpression = "0"
        }
        assertEquals("0", zeroExpression)
    }

    @Test
    fun user_feedback_formatting_contains_muhalkarr() {
        val feedback = UserFeedback(
            type = FeedbackType.FEATURE_SUGGESTION,
            rating = 5,
            message = "Aplikasi sangat bermanfaat dan amanah!",
            contact = "08123456789"
        )
        val formatted = FeedbackRepository.formatMessageText(feedback)
        assertTrue(formatted.contains("Muhalkarr"))
        assertTrue(formatted.contains("★★★★★"))
        assertTrue(formatted.contains("Saran Fitur Baru"))
    }

    @Test
    fun changelog_registry_contains_versions_from_v0_to_v2_6() {
        val versions = ChangelogRegistry.versions
        assertTrue(versions.isNotEmpty())
        assertEquals("v2.6", versions.first().version)
        assertTrue(versions.first().isLatest)
        assertTrue(versions.any { it.version == "v2.5" })
        assertTrue(versions.any { it.version == "v0.1" })
        val v25 = versions.first { it.version == "v2.5" }
        assertTrue(v25.highlights.any { it.contains("Muhalkarr") })
        assertTrue(v25.highlights.any { it.contains("Kamera") })
        assertTrue(v25.highlights.any { it.contains("000") })
        val v26 = versions.first()
        assertTrue(v26.highlights.any { it.contains("Center Docked") })
        assertTrue(v26.highlights.any { it.contains("Sidebar") })
        assertTrue(v26.highlights.any { it.contains("Rezeki") })
    }

    @Test
    fun user_feedback_draft_and_revision_state() {
        val draftFeedback = UserFeedback(
            type = FeedbackType.APPRECIATION,
            rating = 4,
            message = "Draf masukan perbaikan tata kelola kas",
            status = com.example.core.feedback.FeedbackStatus.DRAFT,
            revisionCount = 0
        )
        assertEquals(com.example.core.feedback.FeedbackStatus.DRAFT, draftFeedback.status)
        assertEquals(0, draftFeedback.revisionCount)

        val updatedSent = draftFeedback.copy(
            status = com.example.core.feedback.FeedbackStatus.SENT,
            revisionCount = draftFeedback.revisionCount + 1,
            channelSent = "WhatsApp"
        )
        assertEquals(com.example.core.feedback.FeedbackStatus.SENT, updatedSent.status)
        assertEquals(1, updatedSent.revisionCount)
        assertEquals("WhatsApp", updatedSent.channelSent)
    }

    @Test
    fun rikaz_20_percent_khumus_calculation_accuracy() {
        val rikazValue = 100_000_000.0 // 100 Juta
        val zakatRate = 0.20 // 20%
        val zakatObligation = rikazValue * zakatRate
        val finderHalalPortion = rikazValue * (1.0 - zakatRate)

        assertEquals(20_000_000.0, zakatObligation, 0.001)
        assertEquals(80_000_000.0, finderHalalPortion, 0.001)
    }

    @Test
    fun custom_ruling_data_integrity() {
        val customRuling = com.example.core.grounding.CustomGroundedRuling(
            title = "Fatwa Hukum P2P Lending Syariah",
            category = "INVESTASI",
            authority = "DSN-MUI",
            referenceNumber = "Fatwa No. 117/DSN-MUI/II/2018",
            summary = "Layanan pembiayaan berbasis teknologi informasi wajib menggunakan akad syariah (wakalah bil ujrah atau mudharabah).",
            detailedRuling = "Tidak boleh mengandung unsur riba, gharar, maysir, dan kedhaliman.",
            dalilSource = "QS. Al-Maidah: 2",
            dalilArabic = "وَتَعَاوَنُوا عَلَى الْبِرِّ وَالتَّقْوَىٰ ۖ وَلَا تَعَاوَنُوا عَلَى الْإِثْمِ وَالْعُدْوَانِ",
            dalilTranslation = "Dan tolong-menolonglah kamu dalam kebajikan dan takwa...",
            calculationFormula = "N/A"
        )

        assertEquals("Fatwa Hukum P2P Lending Syariah", customRuling.title)
        assertEquals("INVESTASI", customRuling.category)
        assertTrue(customRuling.dalilArabic!!.contains("وَتَعَاوَنُوا"))
        assertNotNull(customRuling.id)
    }

    @Test
    fun document_exporter_date_filtering_and_csv_json_structure() {
        val now = System.currentTimeMillis()
        val entry1 = com.example.core.accounting.JournalEntry(
            id = "tx_1",
            gregorianDate = java.util.Date(now - 100000),
            hijriYear = 1448,
            hijriMonth = 5,
            hijriDay = 12,
            description = "Gaji Masuk",
            transactionType = "INFLOW",
            lines = listOf(
                com.example.core.accounting.JournalLine("acc_cash", 5000000.0, 0.0),
                com.example.core.accounting.JournalLine("acc_income_salary", 0.0, 5000000.0)
            )
        )
        val entry2 = com.example.core.accounting.JournalEntry(
            id = "tx_2",
            gregorianDate = java.util.Date(now - 80000000L), // ~1 hari lalu
            hijriYear = 1448,
            hijriMonth = 5,
            hijriDay = 11,
            description = "Beli Beras",
            transactionType = "EXPENSE",
            lines = listOf(
                com.example.core.accounting.JournalLine("acc_food", 150000.0, 0.0),
                com.example.core.accounting.JournalLine("acc_cash", 0.0, 150000.0)
            )
        )
        val allEntries = listOf(entry1, entry2)

        // Filter: Hanya 1 jam terakhir
        val recentFiltered = com.example.core.report.DocumentExporter.filterJournalEntries(
            allEntries,
            now - 3600000,
            now
        )
        assertEquals(1, recentFiltered.size)
        assertEquals("tx_1", recentFiltered.first().id)

        // Test CSV output
        val accounts = listOf(
            com.example.core.accounting.Account("acc_cash", "101", "Kas Utama", com.example.core.accounting.AccountCategory.ASSET),
            com.example.core.accounting.Account("acc_income_salary", "401", "Gaji Pokok", com.example.core.accounting.AccountCategory.INCOME_KASAB),
            com.example.core.accounting.Account("acc_food", "501", "Bahan Pangan", com.example.core.accounting.AccountCategory.EXPENSE)
        )
        val csv = com.example.core.report.DocumentExporter.generateTransactionHistoryCsv(
            allEntries,
            accounts,
            null,
            null
        )
        assertTrue(csv.contains("RIWAYAT TRANSAKSI JURNAL"))
        assertTrue(csv.contains("tx_1"))
        assertTrue(csv.contains("tx_2"))

        // Test JSON output
        val json = com.example.core.report.DocumentExporter.generateTransactionHistoryJson(
            allEntries,
            accounts,
            null,
            null
        )
        assertTrue(json.contains("JOURNAL_TRANSACTION_HISTORY"))
        assertTrue(json.contains("tx_1"))
        assertTrue(json.contains("Kas Utama"))
    }

    @Test
    fun rezeki_non_tunai_infaq_vault_allocation_logic() {
        val estimatedValue = 100000.0 // Traktir makan Rp 100.000
        val infaqRate = 0.05 // 5%
        val infaqAmount = estimatedValue * infaqRate // Rp 5.000

        val record = com.example.core.rezeki.RezekiNonTunaiRecord(
            title = "Traktir Makan Siang",
            giverName = "Ahmad",
            category = com.example.core.rezeki.RezekiCategory.FOOD_BEVERAGE,
            estimatedValue = estimatedValue,
            suggestedInfaqRate = infaqRate,
            suggestedInfaqAmount = infaqAmount,
            isPaidInfaq = true,
            paidJournalEntryId = "vault_entry_abc123",
            notes = "Jazakallahu khairan\n[Infaq Vault]"
        )

        assertTrue(record.isPaidInfaq)
        assertTrue(record.isVaultAllocated)
        assertEquals(5000.0, record.suggestedInfaqAmount, 0.001)

        // Verifikasi pembentukan jurnal berpasangan (double-entry) untuk Virtual Infaq Vault
        val lines = listOf(
            com.example.core.accounting.JournalLine("acc_infaq_expense", infaqAmount, 0.0),
            com.example.core.accounting.JournalLine("acc_vault", 0.0, infaqAmount)
        )
        val entry = com.example.core.accounting.JournalEntry(
            id = record.paidJournalEntryId!!,
            gregorianDate = java.util.Date(),
            hijriYear = 1448,
            hijriMonth = 5,
            hijriDay = 15,
            description = "Infaq Syukur atas ${record.title} [Infaq Vault]",
            transactionType = "INFAQ_PAYOUT",
            lines = lines
        )

        assertTrue(entry.isBalanced)
        assertEquals(infaqAmount, entry.totalDebit, 0.001)
        assertEquals(infaqAmount, entry.totalCredit, 0.001)

        // Verifikasi akun vault sebagai kewajiban (LIABILITY) bertambah pada kredit
        val vaultCredit = entry.lines.first { it.accountId == "acc_vault" }.credit
        assertEquals(5000.0, vaultCredit, 0.001)
    }

    @Test
    fun infaq_vault_reminder_initial_delay_calculation_daily_and_weekly() {
        val baseCal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.NOVEMBER, 10, 10, 0, 0) // Tuesday 10:00
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val nowMillis = baseCal.timeInMillis

        // Daily later today (14:30 -> 4.5 hours later)
        val sameDayConfig = com.example.core.infaq.InfaqVaultReminderConfig(
            isEnabled = true,
            frequency = com.example.core.infaq.InfaqReminderFrequency.DAILY,
            hour = 14,
            minute = 30
        )
        val sameDayDelay = com.example.core.worker.InfaqVaultReminderWorker.calculateInitialDelayMillis(
            config = sameDayConfig,
            nowMillis = nowMillis
        )
        assertEquals(4 * 3600_000L + 30 * 60_000L, sameDayDelay)

        // Daily past today (06:00 -> next day 06:00 = 20 hours later)
        val nextDayConfig = sameDayConfig.copy(hour = 6, minute = 0)
        val nextDayDelay = com.example.core.worker.InfaqVaultReminderWorker.calculateInitialDelayMillis(
            config = nextDayConfig,
            nowMillis = nowMillis
        )
        assertEquals(20 * 3600_000L, nextDayDelay)

        // Weekly Friday (3 days after Tuesday at same 10:00 -> 72 hours)
        val fridayConfig = com.example.core.infaq.InfaqVaultReminderConfig(
            isEnabled = true,
            frequency = com.example.core.infaq.InfaqReminderFrequency.WEEKLY,
            dayOfWeek = java.util.Calendar.FRIDAY,
            hour = 10,
            minute = 0
        )
        val fridayDelay = com.example.core.worker.InfaqVaultReminderWorker.calculateInitialDelayMillis(
            config = fridayConfig,
            nowMillis = nowMillis
        )
        assertEquals(3 * 24 * 3600_000L, fridayDelay)
    }

    @Test
    fun zakat_maal_nisab_and_haul_elapsed_days_calculation() {
        val dayMs = 24L * 60L * 60L * 1000L
        val nowMillis = 1_800_000_000_000L
        val haulStartMillis = nowMillis - (200L * dayMs)

        val maal = com.example.core.zakat.ZakatMaalCalculation(
            totalWalletSavings = 100_000_000.0,
            totalIbadahSavings = 25_000_000.0,
            goldWeightGrams = 10.0,
            tradeableReceivables = 5_000_000.0,
            shortTermLiabilities = 10_000_000.0,
            goldPricePerGram = 1_400_000.0, // Nisab = 85 * 1.4M = 119M
            isLunarCalendar = true,
            isHaulFulfilled = true,
            haulStartDateMillis = haulStartMillis
        )

        // Gross = 100M + 25M + 14M + 5M = 144M; Net = 134M >= 119M
        assertEquals(144_000_000.0, maal.totalGrossAssets, 0.01)
        assertEquals(134_000_000.0, maal.netTaxableAssets, 0.01)
        assertEquals(119_000_000.0, maal.nisabGold85g, 0.01)
        assertTrue(maal.isNisabReached)
        assertTrue(maal.isObligatory)
        assertEquals(3_350_000.0, maal.zakatAmount, 0.01) // 2.5% of 134M
        assertEquals(354, maal.haulTargetDays)
        assertEquals(200, maal.elapsedHaulDays(nowMillis))
        assertEquals(154, maal.remainingHaulDays(nowMillis))
    }

    @Test
    fun zakat_fitrah_dependents_and_local_staple_food_calculation() {
        val dependents = listOf(
            com.example.core.zakat.ZakatFitrahDependent("d1", "Kepala Keluarga", "Diri Sendiri", true),
            com.example.core.zakat.ZakatFitrahDependent("d2", "Istri", "Pasangan", true),
            com.example.core.zakat.ZakatFitrahDependent("d3", "Anak 1", "Anak", false)
        )
        val localCalc = com.example.core.zakat.ZakatFitrahFamilyCalculation(
            familyMembersCount = 3,
            riceAmountPerPersonKg = 2.5,
            ricePricePerKg = 18_000.0, // 2.5 * 18.000 = 45.000/jiwa
            cashRatePerPerson = 50_000.0,
            isPaymentInCash = true,
            stapleFoodName = "Beras Premium Lokal",
            measurementUnit = "kg",
            useLocalStaplePrice = true,
            dependents = dependents
        )

        assertEquals(2, localCalc.activeDependentsCount)
        assertEquals(5.0, localCalc.totalRiceKg, 0.001)
        assertEquals(45_000.0, localCalc.ratePerSoulRupiah, 0.001)
        assertEquals(90_000.0, localCalc.totalFitrahRupiah, 0.001)

        // Switching to fixed SK BAZNAS rate
        val manualCalc = localCalc.copy(useLocalStaplePrice = false)
        assertEquals(50_000.0, manualCalc.ratePerSoulRupiah, 0.001)
        assertEquals(100_000.0, manualCalc.totalFitrahRupiah, 0.001)
    }

    @Test
    fun infaq_sli_comprehensive_analysis_explains_consumption_vs_income_ratio() {
        val accounts = listOf(
            com.example.core.accounting.Account("acc_cash", "101", "Kas Utama", com.example.core.accounting.AccountCategory.ASSET),
            com.example.core.accounting.Account("acc_vault", "201", "Virtual Infaq Vault", com.example.core.accounting.AccountCategory.LIABILITY),
            com.example.core.accounting.Account("acc_salary", "401", "Gaji Kasab", com.example.core.accounting.AccountCategory.INCOME_KASAB),
            com.example.core.accounting.Account("acc_living", "501", "Biaya Hidup", com.example.core.accounting.AccountCategory.EXPENSE),
            com.example.core.accounting.Account("acc_disbursed", "599", "Penyaluran Infaq", com.example.core.accounting.AccountCategory.EXPENSE)
        )
        val rules = listOf(
            com.example.core.infaq.InfaqRule(
                id = "r1",
                title = "Kasab 5%",
                targetCategory = com.example.core.accounting.AccountCategory.INCOME_KASAB,
                calculationType = com.example.core.infaq.InfaqCalculationType.PERCENTAGE,
                rate = 0.05
            )
        )
        val now = java.util.Date()
        val entries = listOf(
            com.example.core.accounting.JournalEntry(
                id = "in1",
                gregorianDate = now,
                hijriYear = 1448,
                hijriMonth = 5,
                hijriDay = 15,
                description = "Pemasukan 904rb",
                transactionType = "INFLOW",
                lines = listOf(
                    com.example.core.accounting.JournalLine("acc_cash", 904_000.0, 0.0),
                    com.example.core.accounting.JournalLine("acc_salary", 0.0, 904_000.0),
                    com.example.core.accounting.JournalLine("acc_disbursed", 31_100.0, 0.0),
                    com.example.core.accounting.JournalLine("acc_vault", 0.0, 31_100.0)
                )
            ),
            com.example.core.accounting.JournalEntry(
                id = "exp1",
                gregorianDate = now,
                hijriYear = 1448,
                hijriMonth = 5,
                hijriDay = 15,
                description = "Belanja Konsumsi",
                transactionType = "EXPENSE",
                lines = listOf(
                    com.example.core.accounting.JournalLine("acc_living", 797_436.0, 0.0),
                    com.example.core.accounting.JournalLine("acc_cash", 0.0, 797_436.0)
                )
            )
        )

        val analysis = com.example.core.infaq.InfaqSliAnalysisCalculator.analyze(
            accounts = accounts,
            journalEntries = entries,
            rules = rules,
            totalIncomeKasab = 904_000.0,
            totalIncomeNonKasab = 0.0,
            totalConsumptionExpense = 797_436.0,
            totalPurifiedInfaq = 31_100.0,
            virtualInfaqVaultBalance = 31_100.0
        )

        // SLI (31.100 / 797.436) = ~3.9%
        assertEquals(3.9, analysis.sliConsumptionBasedPercent, 0.05)
        // Income ratio (31.100 / 904.000) = ~3.44% (~3.4%)
        assertEquals(3.44, analysis.infaqToTotalIncomePercent, 0.05)
        // Rule target 5% * 904.000 = 45.200; shortfall = 14.100
        assertEquals(45_200.0, analysis.expectedTotalInfaqFromIncomeRules, 0.01)
        assertEquals(14_100.0, analysis.underAllocationShortfall, 0.01)
        assertEquals(31_100.0, analysis.vaultRemainingPayable, 0.01)
        assertEquals(45_200.0, analysis.combinedMustPayAmount, 0.01)
    }

    @Test
    fun journalEntryPrimaryAmount_excludesInternalInfaqAllocationLines() {
        val now = java.util.Date()
        val inflowWithInfaq = com.example.core.accounting.JournalEntry(
            id = "inflow_1",
            gregorianDate = now,
            hijriYear = 1448,
            hijriMonth = 5,
            hijriDay = 15,
            description = "Pamasihan kanggo makan",
            transactionType = "INFLOW",
            lines = listOf(
                com.example.core.accounting.JournalLine("acc_cash", 10_000.0, 0.0),
                com.example.core.accounting.JournalLine("acc_salary", 0.0, 10_000.0),
                com.example.core.accounting.JournalLine("acc_disbursed", 250.0, 0.0),
                com.example.core.accounting.JournalLine("acc_vault", 0.0, 250.0)
            )
        )
        assertEquals(10_250.0, inflowWithInfaq.totalDebit, 0.01)
        assertEquals(10_000.0, inflowWithInfaq.primaryAmount, 0.01)

        val expenseWithRoundUp = com.example.core.accounting.JournalEntry(
            id = "exp_1",
            gregorianDate = now,
            hijriYear = 1448,
            hijriMonth = 5,
            hijriDay = 15,
            description = "Beli minum (Round-up: Rp 1000)",
            transactionType = "EXPENSE",
            lines = listOf(
                com.example.core.accounting.JournalLine("acc_living", 9_000.0, 0.0),
                com.example.core.accounting.JournalLine("acc_cash", 0.0, 9_000.0),
                com.example.core.accounting.JournalLine("acc_disbursed", 1_000.0, 0.0),
                com.example.core.accounting.JournalLine("acc_vault", 0.0, 1_000.0)
            )
        )
        assertEquals(10_000.0, expenseWithRoundUp.totalDebit, 0.01)
        assertEquals(9_000.0, expenseWithRoundUp.primaryAmount, 0.01)
    }
}





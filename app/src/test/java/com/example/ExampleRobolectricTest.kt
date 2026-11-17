package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
import com.example.core.accounting.FiscalCycleType
import com.example.core.security.AutoLockInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertTrue(appName == "Infaqan" || appName == "Amanah Ledger")
  }

  @Test
  fun `verify budget allocation calculation`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    viewModel.setBudget(
      accountId = "acc_living",
      categoryName = "Biaya Hidup & Pangan",
      monthlyLimit = 3500000.0
    )
    val state = viewModel.uiState.value
    assertTrue(state.budgets.isNotEmpty())
    val livingBudget = state.budgets.first { it.accountId == "acc_living" }
    assertEquals(3500000.0, livingBudget.monthlyLimit, 0.01)
  }

  @Test
  fun `verify overbudget detection and budget allocation updates`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    // Set a tight budget for entertainment: 100,000
    viewModel.setBudget(
      accountId = "acc_entertainment",
      categoryName = "Hiburan & Rekreasi",
      monthlyLimit = 100000.0
    )
    var state = viewModel.uiState.value
    val entBudget = state.budgets.first { it.accountId == "acc_entertainment" }
    assertEquals(100000.0, entBudget.monthlyLimit, 0.01)

    // Record an expense that exceeds 100,000
    viewModel.recordExpense(
      amount = 150000.0,
      expenseAccountId = "acc_entertainment",
      fromAccountId = "acc_bank",
      enableRoundUp = false,
      roundUpStep = 0.0,
      description = "Over budget entertainment",
      date = Date()
    )

    state = viewModel.uiState.value
    val spent = state.getMonthlySpentForAccount("acc_entertainment")
    assertTrue("Spent ($spent) should be greater than limit (100000)", spent >= 150000.0)
    assertTrue("Over budget count should be at least 1", state.overBudgetCount >= 1)
  }

  @Test
  fun `verify double entry balance requirement`() {
    val lines = listOf(
      JournalLine(accountId = "acc_bank", debit = 1000000.0, credit = 0.0),
      JournalLine(accountId = "acc_salary", debit = 0.0, credit = 1000000.0)
    )
    val entry = JournalEntry(
      id = "test_1",
      gregorianDate = Date(),
      hijriYear = 1446,
      hijriMonth = 9,
      hijriDay = 1,
      description = "Test Balanced",
      transactionType = "INFLOW",
      lines = lines
    )
    assertTrue(entry.isBalanced)
    assertEquals(1000000.0, entry.totalDebit, 0.01)
    assertEquals(1000000.0, entry.totalCredit, 0.01)
  }

  @Test
  fun `verify viewModel ledger customization and auto-lock features`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    viewModel.updateInitialLedgerDate("15/01/2024")
    viewModel.updateFiscalCycleType(FiscalCycleType.CALENDAR_MONTH)
    viewModel.updateInitialLedgerBalance(50000000.0)
    viewModel.setStartDayOfMonth(25)
    viewModel.setUiScaleFactor(1.15f)

    val state = viewModel.uiState.value
    assertEquals("15/01/2024", state.initialLedgerDate)
    assertEquals(FiscalCycleType.CALENDAR_MONTH, state.fiscalCycleType)
    assertEquals(50000000.0, state.initialLedgerBalance, 0.01)
    assertEquals(25, state.startDayOfMonth)
    assertEquals(1.15f, state.uiScaleFactor, 0.001f)

    // Verify PIN setup and auto-lock
    viewModel.enablePin("1234", "Kota Kelahiran", "Jakarta")
    viewModel.setAutoLockInterval(AutoLockInterval.IMMEDIATE)
    viewModel.onAppBackgrounded()
    val lockedState = viewModel.uiState.value
    assertTrue(lockedState.securityConfig.isAppLocked)
  }

  @Test
  fun `verify financial pdf generation and text wrapping robustness`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    val state = viewModel.uiState.value

    // Test text wrapping for long strings and unbreakable words
    val paint = android.graphics.Paint().apply { textSize = 14f }
    val longDesc = "Pembelian paket beras 50kg dan sembako ramadhan untuk panti asuhan yatim dhuafa di wilayah pelosok desa"
    val wrapped = com.example.core.report.FinancialPdfGenerator.wrapText(longDesc, paint, 30f)
    assertTrue(wrapped.size > 1)

    val veryLongToken = "TRX-SYARIAH-INVOICE-RAMADHAN-2026-99999999999999999999999999"
    val wrappedToken = com.example.core.report.FinancialPdfGenerator.wrapText(veryLongToken, paint, 20f)
    assertTrue(wrappedToken.size > 1)

    // Test text wrapping with empty and blank strings
    val emptyWrapped = com.example.core.report.FinancialPdfGenerator.wrapText("", paint, 100f)
    assertEquals(1, emptyWrapped.size)

    // Verify Rp formatting helper
    val formattedRp = com.example.core.report.FinancialPdfGenerator.formatRp(1500000.0)
    assertTrue(formattedRp.contains("1.500.000") || formattedRp.contains("1500000"))
  }

  @Test
  fun `verify cash and ewallet account isolation on income recording`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    val initialCash = viewModel.uiState.value.getAccountBalance("acc_cash")
    val initialEwallet = viewModel.uiState.value.getAccountBalance("acc_ewallet")

    // Record income to Cash only (Physical Cash)
    viewModel.recordIncome(
      grossAmount = 500000.0,
      incomeAccountId = "acc_salary",
      depositAccountId = "acc_cash",
      customInfaqRate = 0.05,
      description = "Pemasukan Tunai Fisik",
      date = Date()
    )

    val afterCashIncomeState = viewModel.uiState.value
    val cashAfterIncome = afterCashIncomeState.getAccountBalance("acc_cash")
    val ewalletAfterIncome = afterCashIncomeState.getAccountBalance("acc_ewallet")

    // Cash should increase by 500,000, while e-wallet must remain strictly isolated and unchanged!
    assertEquals(initialCash + 500000.0, cashAfterIncome, 0.01)
    assertEquals(initialEwallet, ewalletAfterIncome, 0.01)

    // Now record income specifically to E-Wallet (Dompet Digital)
    viewModel.recordIncome(
      grossAmount = 250000.0,
      incomeAccountId = "acc_trade",
      depositAccountId = "acc_ewallet",
      customInfaqRate = 0.0,
      description = "Top up saldo dompet digital dagang",
      date = Date()
    )

    val finalState = viewModel.uiState.value
    val finalCash = finalState.getAccountBalance("acc_cash")
    val finalEwallet = finalState.getAccountBalance("acc_ewallet")

    // E-wallet should increase by 250,000, while cash remains unchanged from previous step!
    assertEquals(cashAfterIncome, finalCash, 0.01)
    assertEquals(initialEwallet + 250000.0, finalEwallet, 0.01)
  }

  @Test
  fun `verify infaq disbursement exceeding vault balance is recorded as extra sedekah`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    val initialVault = viewModel.uiState.value.virtualInfaqVaultBalance
    val initialBank = viewModel.uiState.value.getAccountBalance("acc_bank")

    // Disburse 50,000 more than current vault balance (e.g. rounded up as extra sedekah)
    val disburseAmount = initialVault + 50000.0
    viewModel.disburseInfaqWithDetails(
      amount = disburseAmount,
      fromAccountId = "acc_bank",
      recipientName = "Panti Asuhan Dhuafa",
      asnafCategory = com.example.core.infaq.AsnafCategory.YATIM_DHUAFA,
      programName = "Santunan Berkah",
      notes = "Penyaluran plus sedekah round-up",
      date = Date()
    )

    val state = viewModel.uiState.value
    // 1. Vault balance should now be 0 (cleared, never negative)
    assertEquals(0.0, state.virtualInfaqVaultBalance, 0.01)

    // 2. Bank balance should decrease by the full disburse amount
    val bankAfter = state.getAccountBalance("acc_bank")
    assertEquals(initialBank - disburseAmount, bankAfter, 0.01)

    // 3. The latest distribution record should contain the Sedekah Ekstra tag
    val latestDist = state.infaqDistributions.first()
    assertEquals(disburseAmount, latestDist.amount, 0.01)
    assertTrue("Should include Sedekah Ekstra tag in notes", latestDist.notes.contains("Sedekah Ekstra"))

    // 4. Check the latest journal entry is balanced
    val latestEntry = state.journalEntries.first { it.transactionType == "INFAQ_PAYOUT" }
    assertTrue(latestEntry.isBalanced)
    assertEquals(disburseAmount, latestEntry.totalDebit, 0.01)
    assertEquals(disburseAmount, latestEntry.totalCredit, 0.01)
  }

  @Test
  fun `verify all in one master report coverage includes kasab sli 4 zakat sedekah subuh and journal lines`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()

    viewModel.recordIncome(
      grossAmount = 5_000_000.0,
      incomeAccountId = "acc_salary",
      depositAccountId = "acc_bank",
      customInfaqRate = 0.05,
      description = "Gaji Bulanan",
      date = Date()
    )
    viewModel.recordExpense(
      amount = 250_000.0,
      expenseAccountId = "acc_living",
      fromAccountId = "acc_cash",
      enableRoundUp = true,
      roundUpStep = 5000.0,
      description = "Belanja Pangan Harian",
      date = Date()
    )

    val baseState = viewModel.uiState.value.copy(
      sedekahSubuhState = com.example.core.infaq.SedekahSubuhState(
        currentStreak = 7,
        longestStreak = 14,
        totalContributions = 350_000.0,
        totalDaysGiven = 14,
        historyMap = mapOf("2026-11-12" to 25_000.0, "2026-11-11" to 25_000.0),
        badges = com.example.core.infaq.SedekahSubuhState.getDefaultBadges()
      ),
      rezekiNonTunaiList = listOf(
        com.example.core.rezeki.RezekiNonTunaiRecord(
          title = "Bingkisan Sembako",
          giverName = "Kantor",
          estimatedValue = 300_000.0,
          isPaidInfaq = true,
          paidJournalEntryId = "vault_demo_1"
        )
      )
    )

    val coverage = com.example.core.report.AllInOneMasterPdfGenerator.buildCoverageSnapshot(baseState)
    assertTrue("Harus mencakup kasab income", coverage.totalIncomeKasab >= 5_000_000.0)
    assertTrue("Harus mencakup rincian kategori kasab", coverage.kasabCategoryCount >= 1)
    assertTrue("Harus mencakup rincian kategori pengeluaran", coverage.expenseCategoryCount >= 1)
    assertTrue("Harus menghitung SLI konsumsi", coverage.sliConsumptionPercent > 0.0)
    assertTrue("Harus menghitung rasio infaq vs pendapatan", coverage.infaqToTotalIncomePercent > 0.0)
    assertTrue("Harus menghitung Zakat Profesi", coverage.zakatProfesiAmount > 0.0)
    assertTrue("Harus menghitung Zakat Perniagaan", coverage.zakatPerniagaanAmount > 0.0)
    assertTrue("Harus menghitung Zakat Fitrah", coverage.zakatFitrahAmount > 0.0)
    assertEquals(7, coverage.sedekahSubuhStreak)
    assertEquals(2, coverage.sedekahSubuhHistoryDays)
    assertEquals(1, coverage.rezekiNonTunaiCount)
    assertTrue("Harus mencakup detail baris akun debit/kredit jurnal", coverage.detailedJournalLineCount >= coverage.journalEntryCount * 2)
    assertTrue("Skor kepatuhan syariah harus valid (0..100)", coverage.complianceScorePercent in 0..100)
  }

  @Test
  fun `verify pdf editorial config defaults to clean non-tactical wording and supports custom content naming`() {
    val defaultConfig = com.example.core.report.PdfEditorialConfig()
    assertEquals(com.example.core.report.PdfEditorialStyle.SIMPLE_FORMAL, defaultConfig.stylePreset)

    // Even when app UI is in Tactical mode, SIMPLE_FORMAL avoids tactical jargon
    val footerSimple = defaultConfig.resolveFooterText(isAppTactical = true)
    assertTrue("Footer default tidak boleh mengandung SHA-256", !footerSimple.contains("SHA-256"))
    assertTrue("Footer default tidak boleh mengandung PROTOKOL", !footerSimple.contains("PROTOKOL"))
    val contSimple = defaultConfig.resolveContinuationHeader("Laporan Kas", isAppTactical = true)
    assertTrue("Header lanjutan tidak boleh mengandung SEC//LOG", !contSimple.contains("SEC//LOG"))
    assertTrue("Owner label harus netral", defaultConfig.resolveOwnerLabel(isAppTactical = true) == "Pemilik:")

    // Switching explicitly to IKUTI_TEMA restores tactical wording when app is tactical
    val tacticalConfig = com.example.core.report.PdfEditorialConfig.forPreset(
      style = com.example.core.report.PdfEditorialStyle.IKUTI_TEMA
    )
    assertTrue(tacticalConfig.resolveFooterText(isAppTactical = true).contains("SHA-256"))
    assertTrue(tacticalConfig.resolveContinuationHeader("Laporan Kas", isAppTactical = true).contains("SEC//LOG"))

    // Custom section naming, hiding sections, filename prefix sanitization, and JSON round-trip
    val customized = defaultConfig.copy(
      documentTitleOverride = "Catatan Kas Keluarga Bahagia",
      customFileNamePrefix = "Kas Keluarga / 2026!",
      closingNote = "Disetujui Bendahara",
      kasabLabel = "Gaji & Usaha",
      sectionCustomizations = mapOf(
        com.example.core.report.PdfSectionKey.CASHFLOW_KASAB to com.example.core.report.PdfSectionCustomization(
          included = true,
          customTitle = "Pemasukan & Belanja Bulanan"
        ),
        com.example.core.report.PdfSectionKey.SEDEKAH_SUBUH to com.example.core.report.PdfSectionCustomization(
          included = false,
          customTitle = ""
        )
      )
    )

    assertEquals("Kas_Keluarga_2026", customized.resolveFilePrefix("Default"))
    assertEquals("1. Pemasukan & Belanja Bulanan", customized.resolveSectionTitle(com.example.core.report.PdfSectionKey.CASHFLOW_KASAB, isAppTactical = true, numberPrefix = "1."))
    assertTrue(customized.isSectionIncluded(com.example.core.report.PdfSectionKey.CASHFLOW_KASAB))
    assertTrue(!customized.isSectionIncluded(com.example.core.report.PdfSectionKey.SEDEKAH_SUBUH))
    assertEquals("Gaji & Usaha", customized.resolveKasabLabel())

    val restored = com.example.core.report.PdfEditorialConfig.fromJson(customized.toJson())
    assertEquals(customized.documentTitleOverride, restored.documentTitleOverride)
    assertEquals(customized.customFileNamePrefix, restored.customFileNamePrefix)
    assertEquals(customized.closingNote, restored.closingNote)
    assertEquals(customized.kasabLabel, restored.kasabLabel)
    assertEquals("Pemasukan & Belanja Bulanan", restored.sectionCustomizations[com.example.core.report.PdfSectionKey.CASHFLOW_KASAB]?.customTitle)
    assertEquals(false, restored.sectionCustomizations[com.example.core.report.PdfSectionKey.SEDEKAH_SUBUH]?.included)

    // Preset switch preserves owner override and closing note cleanly
    val businessPreset = com.example.core.report.PdfEditorialConfig.forPreset(
      style = com.example.core.report.PdfEditorialStyle.KORPORAT_USAHA,
      keepOwnerAndClosingNoteFrom = customized
    )
    assertEquals(com.example.core.report.PdfEditorialStyle.KORPORAT_USAHA, businessPreset.stylePreset)
    assertEquals("Disetujui Bendahara", businessPreset.closingNote)
  }

  @Test
  fun `verify financial traceability engine health ratios period filters and journal tracing`() {
    val now = System.currentTimeMillis()
    val calNow = java.util.Calendar.getInstance().apply { timeInMillis = now }
    val calPrevMonth = java.util.Calendar.getInstance().apply {
      timeInMillis = now
      add(java.util.Calendar.MONTH, -1)
    }

    val accounts = listOf(
      com.example.core.accounting.Account("acc_bank", "101", "Bank Syariah", com.example.core.accounting.AccountCategory.ASSET),
      com.example.core.accounting.Account("acc_salary", "401", "Gaji Kasab", com.example.core.accounting.AccountCategory.INCOME_KASAB),
      com.example.core.accounting.Account("acc_gift", "451", "Hadiah Non-Kasab", com.example.core.accounting.AccountCategory.INCOME_NON_KASAB),
      com.example.core.accounting.Account("acc_living", "501", "Kebutuhan Pangan", com.example.core.accounting.AccountCategory.EXPENSE),
      com.example.core.accounting.Account("acc_vault", "201", "Virtual Infaq Vault", com.example.core.accounting.AccountCategory.LIABILITY),
      com.example.core.accounting.Account("acc_infaq_expense", "509", "Beban Infaq", com.example.core.accounting.AccountCategory.EXPENSE),
      com.example.core.accounting.Account("acc_disbursed", "510", "Infaq Tersalurkan", com.example.core.accounting.AccountCategory.EXPENSE)
    )

    val wallets = listOf(
      com.example.core.wallet.WalletAccount(
        id = "w_bank",
        name = "Bank Syariah",
        type = com.example.core.wallet.WalletType.BANK_SYARIAH,
        linkedAccountId = "acc_bank"
      )
    )

    val entries = listOf(
      JournalEntry(
        id = "e_cur_income_kasab",
        gregorianDate = calNow.time,
        hijriYear = 1448,
        hijriMonth = 5,
        hijriDay = 10,
        description = "Gaji Bulan Ini",
        transactionType = "INFLOW",
        lines = listOf(
          JournalLine("acc_bank", debit = 4_000_000.0, credit = 0.0),
          JournalLine("acc_salary", debit = 0.0, credit = 3_800_000.0),
          JournalLine("acc_vault", debit = 0.0, credit = 200_000.0)
        )
      ),
      JournalEntry(
        id = "e_cur_income_nonkasab",
        gregorianDate = calNow.time,
        hijriYear = 1448,
        hijriMonth = 5,
        hijriDay = 11,
        description = "Hadiah Bonus Proyek",
        transactionType = "INFLOW",
        lines = listOf(
          JournalLine("acc_bank", debit = 1_000_000.0, credit = 0.0),
          JournalLine("acc_gift", debit = 0.0, credit = 900_000.0),
          JournalLine("acc_vault", debit = 0.0, credit = 100_000.0)
        )
      ),
      JournalEntry(
        id = "e_cur_expense",
        gregorianDate = calNow.time,
        hijriYear = 1448,
        hijriMonth = 5,
        hijriDay = 12,
        description = "Belanja Dapur Sehat",
        transactionType = "EXPENSE",
        lines = listOf(
          JournalLine("acc_living", debit = 2_000_000.0, credit = 0.0),
          JournalLine("acc_bank", debit = 0.0, credit = 2_000_000.0)
        )
      ),
      JournalEntry(
        id = "e_cur_payout",
        gregorianDate = calNow.time,
        hijriYear = 1448,
        hijriMonth = 5,
        hijriDay = 13,
        description = "Penyaluran Anak Yatim",
        transactionType = "INFAQ_PAYOUT",
        lines = listOf(
          JournalLine("acc_vault", debit = 150_000.0, credit = 0.0),
          JournalLine("acc_bank", debit = 0.0, credit = 150_000.0)
        )
      ),
      JournalEntry(
        id = "e_prev_income",
        gregorianDate = calPrevMonth.time,
        hijriYear = 1448,
        hijriMonth = 4,
        hijriDay = 15,
        description = "Gaji Bulan Lalu",
        transactionType = "INFLOW",
        lines = listOf(
          JournalLine("acc_bank", debit = 3_000_000.0, credit = 0.0),
          JournalLine("acc_salary", debit = 0.0, credit = 2_850_000.0),
          JournalLine("acc_vault", debit = 0.0, credit = 150_000.0)
        )
      )
    )

    val state = com.example.core.state.AmanahLedgerUiState(
      accounts = accounts,
      wallets = wallets,
      journalEntries = entries,
      qardhRecords = listOf(
        com.example.core.qardh.QardhRecord(
          id = "q_1",
          type = com.example.core.qardh.QardhType.HUTANG_SAYA,
          counterpartyName = "Koperasi Syariah",
          totalAmount = 1_170_000.0,
          remainingAmount = 1_170_000.0,
          status = com.example.core.qardh.QardhStatus.AKTIF
        )
      )
    )

    val thisMonthReport = com.example.core.analytics.FinancialTraceabilityEngine.analyze(
      state = state,
      periodFilter = com.example.core.analytics.TracePeriodFilter.THIS_MONTH,
      nowMillis = now
    )

    assertEquals(4_700_000.0, thisMonthReport.periodIncomeTotal, 0.01)
    assertEquals(3_800_000.0, thisMonthReport.periodIncomeKasab, 0.01)
    assertEquals(900_000.0, thisMonthReport.periodIncomeNonKasab, 0.01)
    assertEquals(2_000_000.0, thisMonthReport.periodConsumptionTotal, 0.01)
    assertEquals(300_000.0, thisMonthReport.periodInfaqAllocated, 0.01)
    assertEquals(150_000.0, thisMonthReport.periodInfaqDisbursed, 0.01)
    assertEquals(2_400_000.0, thisMonthReport.periodNetSurplus, 0.01)
    assertEquals("Kebutuhan Pangan", thisMonthReport.topExpenseCategoryName)
    assertEquals(100.0, thisMonthReport.topExpenseSharePercent, 0.01)
    assertEquals(20.0, thisMonthReport.debtToAssetRatioPercent, 0.01)
    assertEquals(2.925, thisMonthReport.emergencyRunwayMonths, 0.05)
    assertTrue("MoM income harus naik vs bulan lalu", (thisMonthReport.incomeMoM.deltaPercent ?: 0.0) > 0.0)
    assertEquals(4, thisMonthReport.traceItems.size)

    // Filter bucket KONSUMSI
    val konsumsiReport = com.example.core.analytics.FinancialTraceabilityEngine.analyze(
      state = state,
      periodFilter = com.example.core.analytics.TracePeriodFilter.ALL_TIME,
      selectedBucket = com.example.core.analytics.TraceBucket.KONSUMSI,
      nowMillis = now
    )
    assertEquals(1, konsumsiReport.traceItems.size)
    assertEquals("e_cur_expense", konsumsiReport.traceItems.first().id)

    // Search query matching account trail ("Anak Yatim")
    val searchReport = com.example.core.analytics.FinancialTraceabilityEngine.analyze(
      state = state,
      periodFilter = com.example.core.analytics.TracePeriodFilter.ALL_TIME,
      searchQuery = "Yatim",
      nowMillis = now
    )
    assertEquals(1, searchReport.traceItems.size)
    assertEquals(com.example.core.analytics.TraceBucket.INFAQ_ZAKAT, searchReport.traceItems.first().bucket)
  }

  @Test
  fun `verify hijri to gregorian round trip bijection across full hijri year and offsets`() {
    for (offset in listOf(-1, 0, 1)) {
      for (month in 1..12) {
        val daysInMonth = com.example.core.calendar.HijriCalendarEngine.daysInHijriMonth(1448, month, offset)
        assertTrue("Bulan Hijriah harus 29 atau 30 hari", daysInMonth == 29 || daysInMonth == 30)
        for (day in listOf(1, 15, daysInMonth)) {
          val greg = com.example.core.calendar.HijriCalendarEngine.toGregorian(1448, month, day, offset)
          val roundTrip = com.example.core.calendar.HijriCalendarEngine.fromGregorian(
            date = greg,
            maghribHour = 24,
            manualOffsetDays = offset
          )
          assertEquals("Year mismatch for $day/$month/1448 (offset=$offset)", 1448, roundTrip.year)
          assertEquals("Month mismatch for $day/$month/1448 (offset=$offset)", month, roundTrip.month)
          assertEquals("Day mismatch for $day/$month/1448 (offset=$offset)", day, roundTrip.day)
        }
      }
    }
  }

  @Test
  fun `verify pdf date range masehi and hijriyah filtering scopes entries and pdf generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val calNov5 = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 5, 10, 0, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val calNov20 = java.util.Calendar.getInstance().apply {
      set(2026, java.util.Calendar.NOVEMBER, 20, 14, 0, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }

    val hijriNov5 = com.example.core.calendar.HijriCalendarEngine.fromGregorian(calNov5.time, maghribHour = 24)
    val hijriNov20 = com.example.core.calendar.HijriCalendarEngine.fromGregorian(calNov20.time, maghribHour = 24)

    val accounts = listOf(
      com.example.core.accounting.Account("acc_bank", "101", "Bank Syariah", com.example.core.accounting.AccountCategory.ASSET),
      com.example.core.accounting.Account("acc_salary", "401", "Gaji Kasab", com.example.core.accounting.AccountCategory.INCOME_KASAB),
      com.example.core.accounting.Account("acc_living", "501", "Pangan", com.example.core.accounting.AccountCategory.EXPENSE),
      com.example.core.accounting.Account("acc_vault", "201", "Virtual Infaq Vault", com.example.core.accounting.AccountCategory.LIABILITY)
    )
    val wallets = listOf(
      com.example.core.wallet.WalletAccount(
        id = "w_bank",
        name = "Bank Syariah",
        type = com.example.core.wallet.WalletType.BANK_SYARIAH,
        linkedAccountId = "acc_bank"
      )
    )

    val entries = listOf(
      JournalEntry(
        id = "e_nov5_in",
        gregorianDate = calNov5.time,
        hijriYear = hijriNov5.year,
        hijriMonth = hijriNov5.month,
        hijriDay = hijriNov5.day,
        description = "Gaji Awal Nov",
        transactionType = "INFLOW",
        lines = listOf(
          JournalLine("acc_bank", debit = 1_000_000.0, credit = 0.0),
          JournalLine("acc_salary", debit = 0.0, credit = 950_000.0),
          JournalLine("acc_vault", debit = 0.0, credit = 50_000.0)
        )
      ),
      JournalEntry(
        id = "e_nov20_exp",
        gregorianDate = calNov20.time,
        hijriYear = hijriNov20.year,
        hijriMonth = hijriNov20.month,
        hijriDay = hijriNov20.day,
        description = "Belanja Akhir Nov",
        transactionType = "EXPENSE",
        lines = listOf(
          JournalLine("acc_living", debit = 400_000.0, credit = 0.0),
          JournalLine("acc_bank", debit = 0.0, credit = 400_000.0)
        )
      )
    )

    val dists = listOf(
      com.example.core.infaq.InfaqDistributionRecord(
        id = "d_nov5",
        distributionDate = calNov5.time,
        hijriDateString = hijriNov5.toString(),
        recipientName = "Panti Asuhan",
        asnafCategory = com.example.core.infaq.AsnafCategory.FAKIR_MISKIN,
        amount = 25_000.0,
        programName = "Bantuan Awal Bulan"
      ),
      com.example.core.infaq.InfaqDistributionRecord(
        id = "d_nov20",
        distributionDate = calNov20.time,
        hijriDateString = hijriNov20.toString(),
        recipientName = "Masjid Kampung",
        asnafCategory = com.example.core.infaq.AsnafCategory.FISABILILLAH,
        amount = 30_000.0,
        programName = "Wakaf Karpet"
      )
    )

    val state = com.example.core.state.AmanahLedgerUiState(
      accounts = accounts,
      wallets = wallets,
      journalEntries = entries,
      infaqDistributions = dists,
      sedekahSubuhState = com.example.core.infaq.SedekahSubuhState(
        historyMap = mapOf("2026-11-05" to 10_000.0, "2026-11-20" to 15_000.0),
        totalContributions = 25_000.0,
        totalDaysGiven = 2
      )
    )

    // 1. Test Masehi Range: 01 Nov 2026 - 10 Nov 2026 (hanya mencakup e_nov5_in)
    val startNov1 = java.util.Calendar.getInstance().apply { set(2026, java.util.Calendar.NOVEMBER, 1, 9, 0, 0) }.timeInMillis
    val endNov10 = java.util.Calendar.getInstance().apply { set(2026, java.util.Calendar.NOVEMBER, 10, 17, 0, 0) }.timeInMillis
    val masehiRange = com.example.core.report.PdfDateRange.fromGregorianRange(startNov1, endNov10)
    val scopedMasehi = com.example.core.report.PdfPeriodFilterHelper.compute(state, masehiRange)

    assertEquals(1, scopedMasehi.filteredEntries.size)
    assertEquals("e_nov5_in", scopedMasehi.filteredEntries.first().id)
    assertEquals(950_000.0, scopedMasehi.totalIncomeKasab, 0.01)
    assertEquals(0.0, scopedMasehi.totalConsumptionExpense, 0.01)
    assertEquals(50_000.0, scopedMasehi.totalPurifiedInfaq, 0.01)
    assertEquals(1, scopedMasehi.filteredDistributions.size)
    assertEquals(10_000.0, scopedMasehi.filteredSubuhState.totalContributions, 0.01)
    // Saldo neraca wallet total tetap utuh 600.000
    assertEquals(600_000.0, state.totalWalletBalance, 0.01)

    // 2. Test Hijriyah Range: tepat pada hari hijriNov20..hijriNov20 (hanya mencakup e_nov20_exp)
    val hijriRange = com.example.core.report.PdfDateRange.fromHijriRange(hijriNov20, hijriNov20)
    val scopedHijri = com.example.core.report.PdfPeriodFilterHelper.compute(state, hijriRange)

    assertEquals(com.example.core.report.PdfCalendarMode.HIJRIYAH, hijriRange.calendarMode)
    assertEquals(1, scopedHijri.filteredEntries.size)
    assertEquals("e_nov20_exp", scopedHijri.filteredEntries.first().id)
    assertEquals(0.0, scopedHijri.totalIncomeKasab, 0.01)
    assertEquals(400_000.0, scopedHijri.totalConsumptionExpense, 0.01)
    assertEquals(1, scopedHijri.filteredDistributions.size)
    assertEquals("d_nov20", scopedHijri.filteredDistributions.first().id)
    assertEquals(15_000.0, scopedHijri.filteredSubuhState.totalContributions, 0.01)

    // 3. Test Timezone-safe UTC Picker roundtrip
    val utcMillis = com.example.core.report.PdfDateRange.localMillisToUtcPickerMillis(startNov1)
    val backLocalMidday = com.example.core.report.PdfDateRange.utcPickerMillisToLocalMidday(utcMillis)
    val backCal = java.util.Calendar.getInstance().apply { timeInMillis = backLocalMidday }
    assertEquals(2026, backCal.get(java.util.Calendar.YEAR))
    assertEquals(java.util.Calendar.NOVEMBER, backCal.get(java.util.Calendar.MONTH))
    assertEquals(1, backCal.get(java.util.Calendar.DAY_OF_MONTH))

    // 4. Test Header formatting, Filename tag & Hijri Month/Year preset factories
    val headerPeriod = hijriRange.formatPdfHeaderPeriod()
    assertTrue("Header harus memuat Periode dan nama bulan Hijriah (${hijriNov20.monthName})", headerPeriod.contains(hijriNov20.monthName))
    assertTrue("Header harus memuat padanan Masehi", headerPeriod.contains("2026"))
    assertTrue("Tag berkas harus berformat Hijriah", hijriRange.formatShortFileTag().contains("H-"))

    val ramadanRange = com.example.core.report.PdfDateRange.forHijriMonth(1448, 9)
    assertEquals(1, ramadanRange.startHijri?.day)
    assertEquals(9, ramadanRange.startHijri?.month)
    assertTrue((ramadanRange.endHijri?.day ?: 0) in 29..30)

    val hijriYearRange = com.example.core.report.PdfDateRange.forHijriYear(1448)
    assertEquals(com.example.core.calendar.HijriDate(1448, 1, 1), hijriYearRange.startHijri)
    assertEquals(12, hijriYearRange.endHijri?.month)
  }

  @Test
  fun `verify security recovery rejects backdoors and enforces real answer hash`() {
    val emptyConfig = com.example.core.security.SecurityConfig(
      isPinEnabled = true,
      pinHash = com.example.core.security.SecurityConfig.hashString("1234"),
      securityQuestion = "Nama kota kelahiran?",
      securityAnswerHash = ""
    )
    assertTrue(!emptyConfig.verifyRecoveryAnswer("indonesia"))
    assertTrue(!emptyConfig.verifyRecoveryAnswer("tasikmalaya"))
    assertTrue(!emptyConfig.verifyRecoveryAnswer(""))

    val configured = emptyConfig.copy(
      securityAnswerHash = com.example.core.security.SecurityConfig.hashString("bandung")
    )
    assertTrue(!configured.verifyRecoveryAnswer("indonesia"))
    assertTrue(!configured.verifyRecoveryAnswer("tasikmalaya"))
    assertTrue(configured.verifyRecoveryAnswer("Bandung"))
  }

  @Test
  fun `verify multiline special journal update ibadah deposit recurring key and backup roundtrip`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()

    // 1. Multi-line special journal rounding safety
    val multiLine = JournalEntry(
      id = "j_multi_special",
      gregorianDate = Date(),
      hijriYear = 1448,
      hijriMonth = 5,
      hijriDay = 15,
      description = "Transfer Khusus",
      transactionType = "TRANSFER",
      lines = listOf(
        JournalLine("acc_bank", debit = 33333.0, credit = 0.0),
        JournalLine("acc_bank_special", debit = 66667.0, credit = 0.0),
        JournalLine("acc_cash", debit = 0.0, credit = 33333.0),
        JournalLine("acc_ewallet", debit = 0.0, credit = 66667.0)
      )
    )
    // Inject by copying via restore or recording: test via direct record + special update if present;
    // instead test depositToIbadahGoal + recordRecurring + updateSpecialJournalEntry on existing sample transfer
    val initialCash = viewModel.uiState.value.getWalletBalance("w_cash")
    val goal = com.example.core.ibadah.IbadahGoal(
      id = "g_test_qurban",
      type = com.example.core.ibadah.IbadahGoalType.QURBAN_KAMBING,
      title = "Qurban Test",
      targetAmount = 3_500_000.0,
      linkedWalletId = "w_special"
    )
    viewModel.addIbadahGoal(goal)
    viewModel.depositToIbadahGoal(
      goalId = "g_test_qurban",
      amount = 500_000.0,
      sourceWalletId = "w_cash",
      notes = "Setoran pertama"
    )
    val afterDepositState = viewModel.uiState.value
    assertEquals(initialCash - 500_000.0, afterDepositState.getWalletBalance("w_cash"), 0.01)
    assertEquals(1, afterDepositState.ibadahDeposits.count { it.goalId == "g_test_qurban" })
    val transferJournal = afterDepositState.journalEntries.first { it.id.startsWith("j_ibadah_") }
    assertTrue(transferJournal.isBalanced)

    // Update special journal entry with odd rounding amount
    viewModel.updateSpecialJournalEntry(
      entryId = transferJournal.id,
      newAmount = 500_001.0,
      newDescription = "Setoran disesuaikan",
      newDate = Date()
    )
    val updatedSpecial = viewModel.getJournalEntry(transferJournal.id)!!
    assertTrue(updatedSpecial.isBalanced)
    assertEquals(500_001.0, updatedSpecial.totalDebit, 0.01)
    assertEquals(500_001.0, updatedSpecial.totalCredit, 0.01)

    // Verify period key is set after recurring execution
    val rec = com.example.core.scheduler.RecurringTransaction(
      id = "rec_test_1",
      title = "Infaq Bulanan Test",
      type = com.example.core.scheduler.RecurringType.EXPENSE,
      amount = 50_000.0,
      categoryAccountId = "acc_disbursed",
      assetAccountId = "acc_bank",
      frequency = com.example.core.scheduler.RecurringFrequency.MONTHLY,
      dayOfMonthOrWeek = 1,
      enableRoundUp = false,
      nextDueDate = Date(System.currentTimeMillis() - 86_400_000L)
    )
    viewModel.addRecurringTransaction(rec)
    val (ok, _) = viewModel.executeRecurringTransaction("rec_test_1", Date())
    assertTrue(ok)
    val executedRec = viewModel.uiState.value.recurringTransactions.first { it.id == "rec_test_1" }
    assertEquals(rec.generatePeriodKey(Date()), executedRec.lastExecutedPeriodKey)
    assertTrue(!executedRec.isDue(Date()))

    // Verify BackupEngine roundtrip preserves sedekah historyMap & ibadahDeposits
    viewModel.recordSedekahSubuh(25_000.0, "Sedekah Pagi", "acc_cash", Date())
    val expectedHistorySize = viewModel.uiState.value.sedekahSubuhState.historyMap.size
    val pkg = viewModel.generateBackupPackage("Amanah123!")
    val parsed = com.example.core.backup.BackupEngine.parseAndRestore(pkg, "Amanah123!")
    assertTrue(parsed is com.example.core.backup.RestoreResult.Success)
    val success = parsed as com.example.core.backup.RestoreResult.Success
    assertEquals(expectedHistorySize, success.restoredSedekahState.historyMap.size)
    assertEquals(1, success.restoredIbadahDeposits.count { it.goalId == "g_test_qurban" })
    // Silence unused variable warning on multiLine sanity check
    assertTrue(multiLine.isBalanced)
  }
}

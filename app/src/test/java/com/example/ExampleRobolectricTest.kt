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
}


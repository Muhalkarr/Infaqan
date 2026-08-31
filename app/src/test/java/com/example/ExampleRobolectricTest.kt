package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
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
    assertEquals("Infaqan", appName)
  }

  @Test
  fun `verify budget allocation calculation`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    val state = viewModel.uiState.value
    assertTrue(state.budgets.isNotEmpty())
    val livingBudget = state.budgets.first { it.accountId == "acc_living" }
    val spent = state.getMonthlySpentForAccount(livingBudget.accountId)
    assertTrue(spent >= 0.0)
    assertTrue(livingBudget.monthlyLimit > 0.0)
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
  fun `verify viewModel initialization and seed history`() {
    val viewModel = com.example.core.state.AmanahLedgerViewModel()
    val state = viewModel.uiState.value
    assertTrue(state.journalEntries.isNotEmpty())
    assertTrue(state.totalAssets > 0)
    assertTrue(state.virtualInfaqVaultBalance >= 0)
    for (entry in state.journalEntries) {
      assertTrue("Entry ${entry.description} must be balanced", entry.isBalanced)
    }
  }
}

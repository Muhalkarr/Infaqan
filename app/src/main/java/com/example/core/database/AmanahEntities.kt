package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.core.accounting.JournalLine
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val dateMillis: Long,
    val hijriYear: Int,
    val hijriMonth: Int,
    val hijriDay: Int,
    val description: String,
    val transactionType: String,
    val linesJson: String, // serialized JSON array of JournalLine
    val receiptAttachmentJson: String? = null,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "wallets")
data class WalletAccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val institutionName: String,
    val accountNumber: String,
    val linkedAccountId: String,
    val colorHex: Long,
    val isDefault: Boolean,
    val notes: String,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "ibadah_goals")
data class IbadahGoalEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val targetAmount: Double,
    val currentAccumulated: Double,
    val targetHijriYearMonth: String,
    val targetMonthsRemaining: Int,
    val linkedWalletId: String,
    val notes: String,
    val isCompleted: Boolean,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "qardh_records")
data class QardhRecordEntity(
    @PrimaryKey val id: String,
    val type: String,
    val counterpartyName: String,
    val totalAmount: Double,
    val remainingAmount: Double,
    val agreementDateMillis: Long,
    val dueDateMillis: Long?,
    val witnessName: String,
    val notes: String,
    val status: String,
    val installmentsJson: String, // JSON array of QardhInstallment
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "budget_allocations")
data class BudgetAllocationEntity(
    @PrimaryKey val id: String,
    val accountId: String,
    val categoryName: String,
    val monthlyLimit: Double,
    val iconKey: String,
    val alertThresholdPercent: Double,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "sedekah_subuh_state")
data class SedekahSubuhEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalContributions: Double,
    val totalDaysGiven: Int,
    val lastContributionDateMillis: Long = 0L,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

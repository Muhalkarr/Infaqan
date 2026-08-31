package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
import com.example.core.budget.BudgetAllocation
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.infaq.SedekahSubuhState
import com.example.core.qardh.QardhInstallment
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletType
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

@Database(
    entities = [
        JournalEntryEntity::class,
        WalletAccountEntity::class,
        IbadahGoalEntity::class,
        QardhRecordEntity::class,
        BudgetAllocationEntity::class,
        SedekahSubuhEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AmanahDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalEntryDao
    abstract fun walletDao(): WalletDao
    abstract fun ibadahGoalDao(): IbadahGoalDao
    abstract fun qardhDao(): QardhDao
    abstract fun budgetDao(): BudgetDao
    abstract fun sedekahSubuhDao(): SedekahSubuhDao

    companion object {
        @Volatile
        private var INSTANCE: AmanahDatabase? = null

        fun getDatabase(context: Context): AmanahDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmanahDatabase::class.java,
                    "amanah_ledger_syariah.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object EntityMappers {

    fun toEntity(entry: JournalEntry): JournalEntryEntity {
        val linesArray = JSONArray()
        for (l in entry.lines) {
            val obj = JSONObject()
            obj.put("accountId", l.accountId)
            obj.put("debit", l.debit)
            obj.put("credit", l.credit)
            linesArray.put(obj)
        }

        val receiptJson = entry.receiptAttachment?.let { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("title", r.title)
            obj.put("receiptType", r.receiptType.name)
            obj.put("merchantName", r.merchantName)
            obj.put("referenceNumber", r.referenceNumber)
            obj.put("digitalVerificationHash", r.digitalVerificationHash)
            obj.put("notes", r.notes)
            obj.put("amount", r.amount)
            obj.put("createdAtMillis", r.createdAtMillis)
            obj.toString()
        }

        return JournalEntryEntity(
            id = entry.id,
            dateMillis = entry.gregorianDate.time,
            hijriYear = entry.hijriYear,
            hijriMonth = entry.hijriMonth,
            hijriDay = entry.hijriDay,
            description = entry.description,
            transactionType = entry.transactionType,
            linesJson = linesArray.toString(),
            receiptAttachmentJson = receiptJson,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    fun toDomain(entity: JournalEntryEntity): JournalEntry {
        val lines = mutableListOf<JournalLine>()
        try {
            val arr = JSONArray(entity.linesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                lines.add(
                    JournalLine(
                        accountId = obj.getString("accountId"),
                        debit = obj.optDouble("debit", 0.0),
                        credit = obj.optDouble("credit", 0.0)
                    )
                )
            }
        } catch (_: Exception) {}

        var receipt: ReceiptAttachment? = null
        if (!entity.receiptAttachmentJson.isNullOrBlank()) {
            try {
                val obj = JSONObject(entity.receiptAttachmentJson)
                receipt = ReceiptAttachment(
                    id = obj.optString("id"),
                    title = obj.optString("title", "Bukti"),
                    receiptType = try {
                        ReceiptType.valueOf(obj.optString("receiptType", "STORE_RECEIPT"))
                    } catch (_: Exception) { ReceiptType.STORE_RECEIPT },
                    merchantName = obj.optString("merchantName", ""),
                    referenceNumber = obj.optString("referenceNumber", ""),
                    digitalVerificationHash = obj.optString("digitalVerificationHash", ""),
                    notes = obj.optString("notes", ""),
                    amount = obj.optDouble("amount", 0.0),
                    createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis())
                )
            } catch (_: Exception) {}
        }

        return JournalEntry(
            id = entity.id,
            gregorianDate = Date(entity.dateMillis),
            hijriYear = entity.hijriYear,
            hijriMonth = entity.hijriMonth,
            hijriDay = entity.hijriDay,
            description = entity.description,
            transactionType = entity.transactionType,
            lines = lines,
            receiptAttachment = receipt
        )
    }

    fun toEntity(w: WalletAccount): WalletAccountEntity {
        return WalletAccountEntity(
            id = w.id,
            name = w.name,
            type = w.type.name,
            institutionName = w.institutionName,
            accountNumber = w.accountNumber,
            linkedAccountId = w.linkedAccountId,
            colorHex = w.colorHex,
            isDefault = w.isDefault,
            notes = w.notes
        )
    }

    fun toDomain(e: WalletAccountEntity): WalletAccount {
        return WalletAccount(
            id = e.id,
            name = e.name,
            type = try { WalletType.valueOf(e.type) } catch (_: Exception) { WalletType.CASH },
            institutionName = e.institutionName,
            accountNumber = e.accountNumber,
            linkedAccountId = e.linkedAccountId,
            colorHex = e.colorHex,
            isDefault = e.isDefault,
            notes = e.notes
        )
    }

    fun toEntity(g: IbadahGoal): IbadahGoalEntity {
        return IbadahGoalEntity(
            id = g.id,
            type = g.type.name,
            title = g.title,
            targetAmount = g.targetAmount,
            currentAccumulated = g.currentAccumulated,
            targetHijriYearMonth = g.targetHijriYearMonth,
            targetMonthsRemaining = g.targetMonthsRemaining,
            linkedWalletId = g.linkedWalletId,
            notes = g.notes,
            isCompleted = g.isCompleted
        )
    }

    fun toDomain(e: IbadahGoalEntity): IbadahGoal {
        return IbadahGoal(
            id = e.id,
            type = try { IbadahGoalType.valueOf(e.type) } catch (_: Exception) { IbadahGoalType.QURBAN_KAMBING },
            title = e.title,
            targetAmount = e.targetAmount,
            currentAccumulated = e.currentAccumulated,
            targetHijriYearMonth = e.targetHijriYearMonth,
            targetMonthsRemaining = e.targetMonthsRemaining,
            linkedWalletId = e.linkedWalletId,
            notes = e.notes,
            isCompleted = e.isCompleted
        )
    }

    fun toEntity(q: QardhRecord): QardhRecordEntity {
        val instArr = JSONArray()
        for (i in q.installments) {
            val obj = JSONObject()
            obj.put("id", i.id)
            obj.put("qardhId", i.qardhId)
            obj.put("dateMillis", i.dateMillis)
            obj.put("amount", i.amount)
            obj.put("note", i.note)
            obj.put("fromWalletId", i.fromWalletId)
            instArr.put(obj)
        }
        return QardhRecordEntity(
            id = q.id,
            type = q.type.name,
            counterpartyName = q.counterpartyName,
            totalAmount = q.totalAmount,
            remainingAmount = q.remainingAmount,
            agreementDateMillis = q.startDateMillis,
            dueDateMillis = q.dueDateMillis,
            witnessName = q.witnessName,
            notes = q.notes,
            status = q.status.name,
            installmentsJson = instArr.toString()
        )
    }

    fun toDomain(e: QardhRecordEntity): QardhRecord {
        val installments = mutableListOf<QardhInstallment>()
        try {
            val arr = JSONArray(e.installmentsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                installments.add(
                    QardhInstallment(
                        id = obj.getString("id"),
                        qardhId = obj.optString("qardhId", e.id),
                        amount = obj.getDouble("amount"),
                        dateMillis = obj.getLong("dateMillis"),
                        fromWalletId = obj.optString("fromWalletId", "acc_cash"),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (_: Exception) {}

        return QardhRecord(
            id = e.id,
            type = try { QardhType.valueOf(e.type) } catch (_: Exception) { QardhType.PIUTANG_SAYA },
            counterpartyName = e.counterpartyName,
            totalAmount = e.totalAmount,
            remainingAmount = e.remainingAmount,
            startDateMillis = e.agreementDateMillis,
            dueDateMillis = e.dueDateMillis,
            witnessName = e.witnessName,
            notes = e.notes,
            status = try { QardhStatus.valueOf(e.status) } catch (_: Exception) { QardhStatus.AKTIF },
            installments = installments
        )
    }

    fun toEntity(b: BudgetAllocation): BudgetAllocationEntity {
        return BudgetAllocationEntity(
            id = b.id,
            accountId = b.accountId,
            categoryName = b.categoryName,
            monthlyLimit = b.monthlyLimit,
            iconKey = b.iconKey,
            alertThresholdPercent = b.alertThresholdPercent
        )
    }

    fun toDomain(e: BudgetAllocationEntity): BudgetAllocation {
        return BudgetAllocation(
            id = e.id,
            accountId = e.accountId,
            categoryName = e.categoryName,
            monthlyLimit = e.monthlyLimit,
            iconKey = e.iconKey,
            alertThresholdPercent = e.alertThresholdPercent
        )
    }
}

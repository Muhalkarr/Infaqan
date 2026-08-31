package com.example.core.backup

import android.util.Base64
import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
import com.example.core.budget.BudgetAllocation
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.infaq.InfaqDistributionRecord
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.SedekahSubuhState
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.scheduler.RecurringTransaction
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletType
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupManifest(
    val schemaVersion: Int = 2,
    val appName: String = "Infaqan Syariah (Amanah Ledger)",
    val exportedAt: String,
    val journalEntriesCount: Int,
    val walletsCount: Int,
    val goalsCount: Int,
    val checksum: String,
    val isPasswordProtected: Boolean
) {
    val checksumSha256: String get() = checksum
}

sealed class RestoreResult {
    data class Success(
        val manifest: BackupManifest,
        val restoredEntries: List<JournalEntry>,
        val restoredWallets: List<WalletAccount>,
        val restoredGoals: List<IbadahGoal>,
        val restoredBudgets: List<BudgetAllocation>,
        val restoredRules: List<InfaqRule>,
        val restoredRecurring: List<RecurringTransaction>,
        val restoredSedekahState: SedekahSubuhState
    ) : RestoreResult()

    data class Failure(val errorMessage: String) : RestoreResult()
}

object BackupEngine {

    fun parseBackupPackage(backupText: String): BackupManifest? {
        return try {
            val root = JSONObject(backupText.trim())
            BackupManifest(
                schemaVersion = root.optInt("schemaVersion", 2),
                appName = root.optString("appName", "Infaqan Syariah (Amanah Ledger)"),
                exportedAt = root.optString("exportedAt", "N/A"),
                journalEntriesCount = root.optInt("entriesCount", 0),
                walletsCount = root.optInt("walletsCount", 0),
                goalsCount = root.optInt("goalsCount", 0),
                checksum = root.optString("checksum", "N/A"),
                isPasswordProtected = root.optBoolean("isEncrypted", false)
            )
        } catch (_: Exception) {
            null
        }
    }

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun calculateSha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun encryptDecryptXor(input: ByteArray, passKey: String): ByteArray {
        if (passKey.isEmpty()) return input
        val keyBytes = passKey.toByteArray(StandardCharsets.UTF_8)
        val result = ByteArray(input.size)
        for (i in input.indices) {
            result[i] = (input[i].toInt() xor keyBytes[i % keyBytes.size].toInt()).toByte()
        }
        return result
    }

    /**
     * Create an encrypted/encoded export backup string
     */
    fun createBackupPackage(
        journalEntries: List<JournalEntry>,
        wallets: List<WalletAccount>,
        goals: List<IbadahGoal>,
        budgets: List<BudgetAllocation>,
        rules: List<InfaqRule>,
        recurring: List<RecurringTransaction>,
        sedekahSubuhState: SedekahSubuhState,
        password: String = ""
    ): String {
        val rootJson = JSONObject()
        val dataJson = JSONObject()

        // 1. Journal entries
        val entriesArray = JSONArray()
        for (entry in journalEntries) {
            val eObj = JSONObject()
            eObj.put("id", entry.id)
            eObj.put("date", entry.gregorianDate.time)
            eObj.put("hijriYear", entry.hijriYear)
            eObj.put("hijriMonth", entry.hijriMonth)
            eObj.put("hijriDay", entry.hijriDay)
            eObj.put("description", entry.description)
            eObj.put("transactionType", entry.transactionType)

            val linesArray = JSONArray()
            for (line in entry.lines) {
                val lObj = JSONObject()
                lObj.put("accountId", line.accountId)
                lObj.put("debit", line.debit)
                lObj.put("credit", line.credit)
                linesArray.put(lObj)
            }
            eObj.put("lines", linesArray)

            // Receipt attachment
            entry.receiptAttachment?.let { receipt ->
                val rObj = JSONObject()
                rObj.put("id", receipt.id)
                rObj.put("title", receipt.title)
                rObj.put("receiptType", receipt.receiptType.name)
                rObj.put("merchantName", receipt.merchantName)
                rObj.put("referenceNumber", receipt.referenceNumber)
                rObj.put("digitalVerificationHash", receipt.digitalVerificationHash)
                rObj.put("notes", receipt.notes)
                rObj.put("amount", receipt.amount)
                rObj.put("createdAtMillis", receipt.createdAtMillis)
                eObj.put("receipt", rObj)
            }

            entriesArray.put(eObj)
        }
        dataJson.put("journalEntries", entriesArray)

        // 2. Wallets
        val walletsArray = JSONArray()
        for (w in wallets) {
            val wObj = JSONObject()
            wObj.put("id", w.id)
            wObj.put("name", w.name)
            wObj.put("type", w.type.name)
            wObj.put("institutionName", w.institutionName)
            wObj.put("accountNumber", w.accountNumber)
            wObj.put("linkedAccountId", w.linkedAccountId)
            wObj.put("colorHex", w.colorHex)
            wObj.put("isDefault", w.isDefault)
            wObj.put("notes", w.notes)
            walletsArray.put(wObj)
        }
        dataJson.put("wallets", walletsArray)

        // 3. Ibadah Goals
        val goalsArray = JSONArray()
        for (g in goals) {
            val gObj = JSONObject()
            gObj.put("id", g.id)
            gObj.put("type", g.type.name)
            gObj.put("title", g.title)
            gObj.put("targetAmount", g.targetAmount)
            gObj.put("currentAccumulated", g.currentAccumulated)
            gObj.put("targetHijriYearMonth", g.targetHijriYearMonth)
            gObj.put("targetMonthsRemaining", g.targetMonthsRemaining)
            gObj.put("linkedWalletId", g.linkedWalletId)
            gObj.put("notes", g.notes)
            gObj.put("isCompleted", g.isCompleted)
            goalsArray.put(gObj)
        }
        dataJson.put("goals", goalsArray)

        // 4. Budgets
        val budgetsArray = JSONArray()
        for (b in budgets) {
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("accountId", b.accountId)
            bObj.put("categoryName", b.categoryName)
            bObj.put("monthlyLimit", b.monthlyLimit)
            bObj.put("iconKey", b.iconKey)
            bObj.put("alertThresholdPercent", b.alertThresholdPercent)
            budgetsArray.put(bObj)
        }
        dataJson.put("budgets", budgetsArray)

        // 5. Sedekah Subuh State
        val sObj = JSONObject()
        sObj.put("currentStreak", sedekahSubuhState.currentStreak)
        sObj.put("longestStreak", sedekahSubuhState.longestStreak)
        sObj.put("totalContributions", sedekahSubuhState.totalContributions)
        sObj.put("totalDaysGiven", sedekahSubuhState.totalDaysGiven)
        dataJson.put("sedekahSubuh", sObj)

        val rawJsonString = dataJson.toString()
        val dataChecksum = calculateSha256(rawJsonString)

        val rawBytes = rawJsonString.toByteArray(StandardCharsets.UTF_8)
        val encryptedBytes = if (password.isNotEmpty()) {
            encryptDecryptXor(rawBytes, password)
        } else {
            rawBytes
        }
        val encodedPayload = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        rootJson.put("schemaVersion", 2)
        rootJson.put("appName", "Infaqan Syariah (Amanah Ledger)")
        rootJson.put("exportedAt", isoDateFormat.format(Date()))
        rootJson.put("entriesCount", journalEntries.size)
        rootJson.put("walletsCount", wallets.size)
        rootJson.put("goalsCount", goals.size)
        rootJson.put("checksum", dataChecksum)
        rootJson.put("isEncrypted", password.isNotEmpty())
        rootJson.put("payload", encodedPayload)

        return rootJson.toString(2)
    }

    /**
     * Restore and verify backup package
     */
    fun parseAndRestore(backupText: String, password: String = ""): RestoreResult {
        return try {
            val trimmed = backupText.trim()
            val rootJson = JSONObject(trimmed)

            val schemaVersion = rootJson.optInt("schemaVersion", 1)
            val exportedAt = rootJson.optString("exportedAt", "Tidak Diketahui")
            val entriesCount = rootJson.optInt("entriesCount", 0)
            val walletsCount = rootJson.optInt("walletsCount", 0)
            val goalsCount = rootJson.optInt("goalsCount", 0)
            val expectedChecksum = rootJson.getString("checksum")
            val isEncrypted = rootJson.optBoolean("isEncrypted", false)
            val payloadBase64 = rootJson.getString("payload")

            val encryptedBytes = Base64.decode(payloadBase64, Base64.DEFAULT)
            val decryptedBytes = if (isEncrypted) {
                if (password.isEmpty()) {
                    return RestoreResult.Failure("Berkas cadangan ini diproteksi sandi. Harap masukkan kata sandi enkripsi.")
                }
                encryptDecryptXor(encryptedBytes, password)
            } else {
                encryptedBytes
            }

            val decryptedJsonString = String(decryptedBytes, StandardCharsets.UTF_8)

            // Checksum verification
            val actualChecksum = calculateSha256(decryptedJsonString)
            if (actualChecksum != expectedChecksum) {
                return RestoreResult.Failure("Verifikasi Integritas Gagal (Checksum Mismatch). Sandi mungkin keliru atau berkas rusak/termodifikasi.")
            }

            val dataJson = JSONObject(decryptedJsonString)

            // Parse journal entries
            val restoredEntries = mutableListOf<JournalEntry>()
            val entriesArray = dataJson.optJSONArray("journalEntries") ?: JSONArray()
            for (i in 0 until entriesArray.length()) {
                val eObj = entriesArray.getJSONObject(i)
                val linesArray = eObj.getJSONArray("lines")
                val lines = mutableListOf<JournalLine>()
                for (j in 0 until linesArray.length()) {
                    val lObj = linesArray.getJSONObject(j)
                    lines.add(
                        JournalLine(
                            accountId = lObj.getString("accountId"),
                            debit = lObj.optDouble("debit", 0.0),
                            credit = lObj.optDouble("credit", 0.0)
                        )
                    )
                }

                var receipt: ReceiptAttachment? = null
                if (eObj.has("receipt")) {
                    val rObj = eObj.getJSONObject("receipt")
                    receipt = ReceiptAttachment(
                        id = rObj.optString("id"),
                        title = rObj.optString("title", "Bukti Transaksi"),
                        receiptType = try {
                            ReceiptType.valueOf(rObj.optString("receiptType", "STORE_RECEIPT"))
                        } catch (e: Exception) {
                            ReceiptType.STORE_RECEIPT
                        },
                        merchantName = rObj.optString("merchantName", ""),
                        referenceNumber = rObj.optString("referenceNumber", ""),
                        digitalVerificationHash = rObj.optString("digitalVerificationHash", ""),
                        notes = rObj.optString("notes", ""),
                        amount = rObj.optDouble("amount", 0.0),
                        createdAtMillis = rObj.optLong("createdAtMillis", System.currentTimeMillis())
                    )
                }

                restoredEntries.add(
                    JournalEntry(
                        id = eObj.getString("id"),
                        gregorianDate = Date(eObj.getLong("date")),
                        hijriYear = eObj.optInt("hijriYear", 1448),
                        hijriMonth = eObj.optInt("hijriMonth", 3),
                        hijriDay = eObj.optInt("hijriDay", 8),
                        description = eObj.getString("description"),
                        transactionType = eObj.optString("transactionType", "EXPENSE"),
                        lines = lines,
                        receiptAttachment = receipt
                    )
                )
            }

            // Parse Wallets
            val restoredWallets = mutableListOf<WalletAccount>()
            val walletsArray = dataJson.optJSONArray("wallets") ?: JSONArray()
            for (i in 0 until walletsArray.length()) {
                val wObj = walletsArray.getJSONObject(i)
                restoredWallets.add(
                    WalletAccount(
                        id = wObj.getString("id"),
                        name = wObj.getString("name"),
                        type = try {
                            WalletType.valueOf(wObj.optString("type", "CASH"))
                        } catch (e: Exception) {
                            WalletType.CASH
                        },
                        institutionName = wObj.optString("institutionName", "Tunai"),
                        accountNumber = wObj.optString("accountNumber", ""),
                        linkedAccountId = wObj.optString("linkedAccountId", "acc_cash"),
                        colorHex = wObj.optLong("colorHex", 0xFF10B981),
                        isDefault = wObj.optBoolean("isDefault", false),
                        notes = wObj.optString("notes", "")
                    )
                )
            }

            // Parse Ibadah Goals
            val restoredGoals = mutableListOf<IbadahGoal>()
            val goalsArray = dataJson.optJSONArray("goals") ?: JSONArray()
            for (i in 0 until goalsArray.length()) {
                val gObj = goalsArray.getJSONObject(i)
                restoredGoals.add(
                    IbadahGoal(
                        id = gObj.getString("id"),
                        type = try {
                            IbadahGoalType.valueOf(gObj.optString("type", "QURBAN_KAMBING"))
                        } catch (e: Exception) {
                            IbadahGoalType.QURBAN_KAMBING
                        },
                        title = gObj.getString("title"),
                        targetAmount = gObj.optDouble("targetAmount", 3500000.0),
                        currentAccumulated = gObj.optDouble("currentAccumulated", 0.0),
                        targetHijriYearMonth = gObj.optString("targetHijriYearMonth", "10 Dzulhijjah 1448 H"),
                        targetMonthsRemaining = gObj.optInt("targetMonthsRemaining", 10),
                        linkedWalletId = gObj.optString("linkedWalletId", "acc_bank"),
                        notes = gObj.optString("notes", ""),
                        isCompleted = gObj.optBoolean("isCompleted", false)
                    )
                )
            }

            // Parse Budgets
            val restoredBudgets = mutableListOf<BudgetAllocation>()
            val budgetsArray = dataJson.optJSONArray("budgets") ?: JSONArray()
            for (i in 0 until budgetsArray.length()) {
                val bObj = budgetsArray.getJSONObject(i)
                restoredBudgets.add(
                    BudgetAllocation(
                        id = bObj.getString("id"),
                        accountId = bObj.getString("accountId"),
                        categoryName = bObj.optString("categoryName", "Pos Anggaran"),
                        monthlyLimit = bObj.getDouble("monthlyLimit"),
                        iconKey = bObj.optString("iconKey", "shopping"),
                        alertThresholdPercent = bObj.optDouble("alertThresholdPercent", 0.8)
                    )
                )
            }

            // Parse Sedekah Subuh
            val sObj = dataJson.optJSONObject("sedekahSubuh")
            val restoredSedekah = if (sObj != null) {
                SedekahSubuhState(
                    currentStreak = sObj.optInt("currentStreak", 0),
                    longestStreak = sObj.optInt("longestStreak", 0),
                    totalContributions = sObj.optDouble("totalContributions", 0.0),
                    totalDaysGiven = sObj.optInt("totalDaysGiven", 0)
                )
            } else {
                SedekahSubuhState()
            }

            val manifest = BackupManifest(
                schemaVersion = schemaVersion,
                exportedAt = exportedAt,
                journalEntriesCount = restoredEntries.size,
                walletsCount = restoredWallets.size,
                goalsCount = restoredGoals.size,
                checksum = expectedChecksum,
                isPasswordProtected = isEncrypted
            )

            RestoreResult.Success(
                manifest = manifest,
                restoredEntries = restoredEntries,
                restoredWallets = restoredWallets,
                restoredGoals = restoredGoals,
                restoredBudgets = restoredBudgets,
                restoredRules = emptyList(),
                restoredRecurring = emptyList(),
                restoredSedekahState = restoredSedekah
            )
        } catch (e: Exception) {
            RestoreResult.Failure("Gagal memproses berkas cadangan: ${e.localizedMessage ?: e.message}")
        }
    }
}

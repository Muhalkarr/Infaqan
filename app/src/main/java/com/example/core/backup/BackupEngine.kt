package com.example.core.backup

import android.os.Build
import android.util.Base64
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
import com.example.core.budget.BudgetAllocation
import com.example.core.feature.AppFeatureVisibilityConfig
import com.example.core.ibadah.IbadahDepositRecord
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.infaq.AsnafCategory
import com.example.core.infaq.InfaqCalculationType
import com.example.core.infaq.InfaqDistributionRecord
import com.example.core.infaq.InfaqReminderFrequency
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.InfaqVaultReminderConfig
import com.example.core.infaq.SedekahSubuhState
import com.example.core.infaq.SedekahSubuhStreakEngine
import com.example.core.notification.AmanahNotificationConfig
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.report.PdfEditorialConfig
import com.example.core.rezeki.RezekiCategory
import com.example.core.rezeki.RezekiNonTunaiRecord
import com.example.core.scheduler.RecurringFrequency
import com.example.core.scheduler.RecurringTransaction
import com.example.core.scheduler.RecurringType
import com.example.core.shariah.HaulCalculationMethod
import com.example.core.shariah.ShariahMazhab
import com.example.core.shariah.ShariahRulesConfig
import com.example.core.shariah.ZakatProfesiFormula
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletMutationRecord
import com.example.core.wallet.WalletType
import org.json.JSONArray
import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupPreferencesSnapshot(
    val userNameKasMukmin: String = "Kas Keluarga Mukmin",
    val primaryCurrencySymbol: String = "Rp",
    val goldPricePerGram: Double = 1350000.0,
    val selectedHijriOffset: Int = 0,
    val startDayOfMonth: Int = 1,
    val initialLedgerDate: String = "01/01/2024",
    val fiscalCycleType: String = "MONTHLY_SALARY_DATE",
    val initialLedgerBalance: Double = 0.0,
    val fiscalYearStartMonth: Int = 1,
    val defaultInfaqRate: Double = 0.05,
    val isAutoDeductInfaqEnabled: Boolean = true,
    val vaultMonthlyTarget: Double = 5000000.0,
    val sedekahSubuhTargetDays: Int = 40,
    val israfWarningThresholdPercent: Int = 80,
    val isStrictBudgetEnforced: Boolean = false,
    val isDeficitProtectionEnabled: Boolean = true,
    val autoExecuteRecurringEnabled: Boolean = true,
    val notifyOnRecurringDue: Boolean = true,
    val showDailyHadith: Boolean = true,
    val showQuickTutorial: Boolean = true,
    val selectedGoalMode: String = "BALANCED_50_30_20",
    val themeMode: String = "FOLLOW_SYSTEM",
    val uiScaleMode: String = "DEFAULT",
    val uiScaleFactor: Float = 1.0f,
    val appDisplayMode: String = "SIMPLE",
    val haulStartDateMillis: Long = 0L,
    val isShakeFeedbackEnabled: Boolean = true,
    val autoCloudBackupEnabled: Boolean = false,
    val notificationConfigJson: String = AmanahNotificationConfig().toJson(),
    val infaqVaultReminderConfigJson: String = serializeInfaqVaultReminder(InfaqVaultReminderConfig()),
    val shariahConfigJson: String = serializeShariahConfig(ShariahRulesConfig()),
    val featureVisibilityConfigJson: String = AppFeatureVisibilityConfig().toJson(),
    val pdfEditorialConfigJson: String = PdfEditorialConfig().toJson(),
    val disabledExpenseAccountIds: Set<String> = emptySet(),
    val purifiedAuditEntryIds: Set<String> = emptySet()
) {
    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put("userNameKasMukmin", userNameKasMukmin)
        obj.put("primaryCurrencySymbol", primaryCurrencySymbol)
        obj.put("goldPricePerGram", goldPricePerGram)
        obj.put("selectedHijriOffset", selectedHijriOffset)
        obj.put("startDayOfMonth", startDayOfMonth)
        obj.put("initialLedgerDate", initialLedgerDate)
        obj.put("fiscalCycleType", fiscalCycleType)
        obj.put("initialLedgerBalance", initialLedgerBalance)
        obj.put("fiscalYearStartMonth", fiscalYearStartMonth)
        obj.put("defaultInfaqRate", defaultInfaqRate)
        obj.put("isAutoDeductInfaqEnabled", isAutoDeductInfaqEnabled)
        obj.put("vaultMonthlyTarget", vaultMonthlyTarget)
        obj.put("sedekahSubuhTargetDays", sedekahSubuhTargetDays)
        obj.put("israfWarningThresholdPercent", israfWarningThresholdPercent)
        obj.put("isStrictBudgetEnforced", isStrictBudgetEnforced)
        obj.put("isDeficitProtectionEnabled", isDeficitProtectionEnabled)
        obj.put("autoExecuteRecurringEnabled", autoExecuteRecurringEnabled)
        obj.put("notifyOnRecurringDue", notifyOnRecurringDue)
        obj.put("showDailyHadith", showDailyHadith)
        obj.put("showQuickTutorial", showQuickTutorial)
        obj.put("selectedGoalMode", selectedGoalMode)
        obj.put("themeMode", themeMode)
        obj.put("uiScaleMode", uiScaleMode)
        obj.put("uiScaleFactor", uiScaleFactor.toDouble())
        obj.put("appDisplayMode", appDisplayMode)
        obj.put("haulStartDateMillis", haulStartDateMillis)
        obj.put("isShakeFeedbackEnabled", isShakeFeedbackEnabled)
        obj.put("autoCloudBackupEnabled", autoCloudBackupEnabled)
        obj.put("notificationConfigJson", notificationConfigJson)
        obj.put("infaqVaultReminderConfigJson", infaqVaultReminderConfigJson)
        obj.put("shariahConfigJson", shariahConfigJson)
        obj.put("featureVisibilityConfigJson", featureVisibilityConfigJson)
        obj.put("pdfEditorialConfigJson", pdfEditorialConfigJson)
        obj.put("disabledExpenseAccountIds", JSONArray(disabledExpenseAccountIds.toList()))
        obj.put("purifiedAuditEntryIds", JSONArray(purifiedAuditEntryIds.toList()))
        return obj
    }

    companion object {
        fun serializeInfaqVaultReminder(config: InfaqVaultReminderConfig): String {
            return JSONObject().apply {
                put("isEnabled", config.isEnabled)
                put("frequency", config.frequency.name)
                put("hour", config.hour)
                put("minute", config.minute)
                put("dayOfWeek", config.dayOfWeek)
                put("minVaultBalanceThreshold", config.minVaultBalanceThreshold)
                put("isAutoRecurringEnabled", config.isAutoRecurringEnabled)
                put("recurringAmount", config.recurringAmount)
                put("recurringTargetAsnaf", config.recurringTargetAsnaf)
            }.toString()
        }

        fun deserializeInfaqVaultReminder(jsonStr: String?): InfaqVaultReminderConfig {
            if (jsonStr.isNullOrBlank()) return InfaqVaultReminderConfig()
            return try {
                val obj = JSONObject(jsonStr)
                InfaqVaultReminderConfig(
                    isEnabled = obj.optBoolean("isEnabled", false),
                    frequency = try {
                        InfaqReminderFrequency.valueOf(obj.optString("frequency", InfaqReminderFrequency.WEEKLY.name))
                    } catch (_: Exception) {
                        InfaqReminderFrequency.WEEKLY
                    },
                    hour = obj.optInt("hour", 6).coerceIn(0, 23),
                    minute = obj.optInt("minute", 0).coerceIn(0, 59),
                    dayOfWeek = obj.optInt("dayOfWeek", Calendar.FRIDAY),
                    minVaultBalanceThreshold = obj.optDouble("minVaultBalanceThreshold", 0.0).coerceAtLeast(0.0),
                    isAutoRecurringEnabled = obj.optBoolean("isAutoRecurringEnabled", false),
                    recurringAmount = obj.optDouble("recurringAmount", 50_000.0).coerceAtLeast(0.0),
                    recurringTargetAsnaf = obj.optString("recurringTargetAsnaf", "Fakir Miskin / 8 Asnaf")
                )
            } catch (_: Exception) {
                InfaqVaultReminderConfig()
            }
        }

        fun serializeShariahConfig(config: ShariahRulesConfig): String {
            return JSONObject().apply {
                put("goldNisabGram", config.goldNisabGram)
                put("silverNisabGram", config.silverNisabGram)
                put("zakatPercentage", config.zakatPercentage)
                put("zakatProfesiFormula", config.zakatProfesiFormula.name)
                put("haulCalculationMethod", config.haulCalculationMethod.name)
                put("selectedMazhab", config.selectedMazhab.name)
                put("customCalculationFormula", config.customCalculationFormula)
                put("isCustomFormulaActive", config.isCustomFormulaActive)
                put("customFormulaName", config.customFormulaName)
                put("defaultGoldPricePerGram", config.defaultGoldPricePerGram)
                put("defaultSilverPricePerGram", config.defaultSilverPricePerGram)
            }.toString()
        }

        fun deserializeShariahConfig(jsonStr: String?): ShariahRulesConfig {
            if (jsonStr.isNullOrBlank()) return ShariahRulesConfig()
            return try {
                val obj = JSONObject(jsonStr)
                ShariahRulesConfig(
                    goldNisabGram = obj.optDouble("goldNisabGram", 85.0),
                    silverNisabGram = obj.optDouble("silverNisabGram", 595.0),
                    zakatPercentage = obj.optDouble("zakatPercentage", 2.5),
                    zakatProfesiFormula = try {
                        ZakatProfesiFormula.valueOf(obj.optString("zakatProfesiFormula", ZakatProfesiFormula.BRUTO.name))
                    } catch (_: Exception) { ZakatProfesiFormula.BRUTO },
                    haulCalculationMethod = try {
                        HaulCalculationMethod.valueOf(obj.optString("haulCalculationMethod", HaulCalculationMethod.HIJRIAH.name))
                    } catch (_: Exception) { HaulCalculationMethod.HIJRIAH },
                    selectedMazhab = try {
                        ShariahMazhab.valueOf(obj.optString("selectedMazhab", ShariahMazhab.SYAFII.name))
                    } catch (_: Exception) { ShariahMazhab.SYAFII },
                    customCalculationFormula = obj.optString("customCalculationFormula", ""),
                    isCustomFormulaActive = obj.optBoolean("isCustomFormulaActive", false),
                    customFormulaName = obj.optString("customFormulaName", "Formula Pribadi"),
                    defaultGoldPricePerGram = obj.optDouble("defaultGoldPricePerGram", 1450000.0),
                    defaultSilverPricePerGram = obj.optDouble("defaultSilverPricePerGram", 18000.0)
                )
            } catch (_: Exception) {
                ShariahRulesConfig()
            }
        }

        fun fromJson(obj: JSONObject?): BackupPreferencesSnapshot? {
            if (obj == null) return null
            return try {
                val disabledIds = mutableSetOf<String>()
                val dArr = obj.optJSONArray("disabledExpenseAccountIds")
                if (dArr != null) {
                    for (i in 0 until dArr.length()) {
                        val id = dArr.optString(i, "")
                        if (id.isNotBlank()) disabledIds.add(id)
                    }
                }
                val purifiedIds = mutableSetOf<String>()
                val pArr = obj.optJSONArray("purifiedAuditEntryIds")
                if (pArr != null) {
                    for (i in 0 until pArr.length()) {
                        val id = pArr.optString(i, "")
                        if (id.isNotBlank()) purifiedIds.add(id)
                    }
                }
                BackupPreferencesSnapshot(
                    userNameKasMukmin = obj.optString("userNameKasMukmin", "Kas Keluarga Mukmin"),
                    primaryCurrencySymbol = obj.optString("primaryCurrencySymbol", "Rp"),
                    goldPricePerGram = obj.optDouble("goldPricePerGram", 1350000.0),
                    selectedHijriOffset = obj.optInt("selectedHijriOffset", 0),
                    startDayOfMonth = obj.optInt("startDayOfMonth", 1).coerceIn(1, 28),
                    initialLedgerDate = obj.optString("initialLedgerDate", "01/01/2024"),
                    fiscalCycleType = obj.optString("fiscalCycleType", "MONTHLY_SALARY_DATE"),
                    initialLedgerBalance = obj.optDouble("initialLedgerBalance", 0.0),
                    fiscalYearStartMonth = obj.optInt("fiscalYearStartMonth", 1).coerceIn(1, 12),
                    defaultInfaqRate = obj.optDouble("defaultInfaqRate", 0.05),
                    isAutoDeductInfaqEnabled = obj.optBoolean("isAutoDeductInfaqEnabled", true),
                    vaultMonthlyTarget = obj.optDouble("vaultMonthlyTarget", 5000000.0),
                    sedekahSubuhTargetDays = obj.optInt("sedekahSubuhTargetDays", 40).coerceIn(1, 365),
                    israfWarningThresholdPercent = obj.optInt("israfWarningThresholdPercent", 80).coerceIn(50, 100),
                    isStrictBudgetEnforced = obj.optBoolean("isStrictBudgetEnforced", false),
                    isDeficitProtectionEnabled = obj.optBoolean("isDeficitProtectionEnabled", true),
                    autoExecuteRecurringEnabled = obj.optBoolean("autoExecuteRecurringEnabled", true),
                    notifyOnRecurringDue = obj.optBoolean("notifyOnRecurringDue", true),
                    showDailyHadith = obj.optBoolean("showDailyHadith", true),
                    showQuickTutorial = obj.optBoolean("showQuickTutorial", true),
                    selectedGoalMode = obj.optString("selectedGoalMode", "BALANCED_50_30_20"),
                    themeMode = obj.optString("themeMode", "FOLLOW_SYSTEM"),
                    uiScaleMode = obj.optString("uiScaleMode", "DEFAULT"),
                    uiScaleFactor = obj.optDouble("uiScaleFactor", 1.0).toFloat(),
                    appDisplayMode = obj.optString("appDisplayMode", "SIMPLE"),
                    haulStartDateMillis = obj.optLong("haulStartDateMillis", 0L),
                    isShakeFeedbackEnabled = obj.optBoolean("isShakeFeedbackEnabled", true),
                    autoCloudBackupEnabled = obj.optBoolean("autoCloudBackupEnabled", false),
                    notificationConfigJson = obj.optString("notificationConfigJson", AmanahNotificationConfig().toJson()),
                    infaqVaultReminderConfigJson = obj.optString("infaqVaultReminderConfigJson", serializeInfaqVaultReminder(InfaqVaultReminderConfig())),
                    shariahConfigJson = obj.optString("shariahConfigJson", serializeShariahConfig(ShariahRulesConfig())),
                    featureVisibilityConfigJson = obj.optString("featureVisibilityConfigJson", AppFeatureVisibilityConfig().toJson()),
                    pdfEditorialConfigJson = obj.optString("pdfEditorialConfigJson", PdfEditorialConfig().toJson()),
                    disabledExpenseAccountIds = disabledIds,
                    purifiedAuditEntryIds = purifiedIds
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}

data class BackupManifest(
    val schemaVersion: Int = 4,
    val appName: String = "Infaqan Syariah (Amanah Ledger)",
    val exportedAt: String,
    val journalEntriesCount: Int,
    val walletsCount: Int,
    val goalsCount: Int,
    val qardhCount: Int = 0,
    val rulesCount: Int = 0,
    val recurringCount: Int = 0,
    val distributionsCount: Int = 0,
    val rezekiCount: Int = 0,
    val walletMutationsCount: Int = 0,
    val hasPreferences: Boolean = false,
    val deviceName: String = "Perangkat Android",
    val checksum: String,
    val isPasswordProtected: Boolean,
    val appVersion: String = "2.5.0",
    val isCompatible: Boolean = true,
    val migrationApplied: Boolean = false
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
        val restoredSedekahState: SedekahSubuhState,
        val restoredDistributions: List<InfaqDistributionRecord> = emptyList(),
        val restoredQardh: List<QardhRecord> = emptyList(),
        val restoredIbadahDeposits: List<IbadahDepositRecord> = emptyList(),
        val restoredRezekiNonTunai: List<RezekiNonTunaiRecord> = emptyList(),
        val restoredWalletMutations: List<WalletMutationRecord> = emptyList(),
        val restoredPreferences: BackupPreferencesSnapshot? = null
    ) : RestoreResult()

    data class Failure(val errorMessage: String) : RestoreResult()
}

object BackupEngine {
    const val CURRENT_SCHEMA_VERSION = 4

    private fun safeDeviceName(): String {
        return try {
            val manufacturer = Build.MANUFACTURER?.trim().orEmpty()
            val model = Build.MODEL?.trim().orEmpty()
            when {
                model.isBlank() -> "Perangkat Android"
                manufacturer.isNotBlank() && !model.startsWith(manufacturer, ignoreCase = true) -> "$manufacturer $model"
                else -> model
            }
        } catch (_: Throwable) {
            "Perangkat Android"
        }
    }

    fun parseBackupPackage(backupText: String): BackupManifest? {
        return try {
            val root = JSONObject(backupText.trim())
            val schema = root.optInt("schemaVersion", 1)
            BackupManifest(
                schemaVersion = schema,
                appName = root.optString("appName", "Infaqan Syariah (Amanah Ledger)"),
                exportedAt = root.optString("exportedAt", "N/A"),
                journalEntriesCount = root.optInt("entriesCount", 0),
                walletsCount = root.optInt("walletsCount", 0),
                goalsCount = root.optInt("goalsCount", 0),
                qardhCount = root.optInt("qardhCount", 0),
                rulesCount = root.optInt("rulesCount", 0),
                recurringCount = root.optInt("recurringCount", 0),
                distributionsCount = root.optInt("distributionsCount", 0),
                rezekiCount = root.optInt("rezekiCount", 0),
                walletMutationsCount = root.optInt("walletMutationsCount", 0),
                hasPreferences = root.optBoolean("hasPreferences", false),
                deviceName = root.optString("deviceName", "Perangkat Android"),
                checksum = root.optString("checksum", "N/A"),
                isPasswordProtected = root.optBoolean("isEncrypted", false),
                appVersion = root.optString("appVersion", "2.0.0"),
                isCompatible = schema in 1..CURRENT_SCHEMA_VERSION,
                migrationApplied = schema < CURRENT_SCHEMA_VERSION
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun formatIsoTimestamp(date: Date = Date()): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(date)
    }

    fun calculateSha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun encryptAesGcm(plainBytes: ByteArray, password: String): ByteArray {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) }

        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        val cipherBytes = cipher.doFinal(plainBytes)

        val output = ByteBuffer.allocate(4 + salt.size + iv.size + cipherBytes.size)
        output.put("A256".toByteArray(StandardCharsets.UTF_8))
        output.put(salt)
        output.put(iv)
        output.put(cipherBytes)
        return output.array()
    }

    private fun decryptAesGcm(encryptedBytes: ByteArray, password: String): ByteArray {
        val buffer = ByteBuffer.wrap(encryptedBytes)
        val tagBytes = ByteArray(4)
        buffer.get(tagBytes)
        val salt = ByteArray(16)
        buffer.get(salt)
        val iv = ByteArray(12)
        buffer.get(iv)
        val cipherBytes = ByteArray(buffer.remaining())
        buffer.get(cipherBytes)

        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = SecretKeySpec(factory.generateSecret(spec).encoded, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        return cipher.doFinal(cipherBytes)
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
     * Create an encrypted/encoded export backup package (schema v4) covering all ledger entities & preferences.
     */
    fun createBackupPackage(
        journalEntries: List<JournalEntry>,
        wallets: List<WalletAccount>,
        goals: List<IbadahGoal>,
        budgets: List<BudgetAllocation>,
        rules: List<InfaqRule>,
        recurring: List<RecurringTransaction>,
        sedekahSubuhState: SedekahSubuhState,
        distributions: List<InfaqDistributionRecord> = emptyList(),
        qardhRecords: List<QardhRecord> = emptyList(),
        ibadahDeposits: List<IbadahDepositRecord> = emptyList(),
        rezekiNonTunai: List<RezekiNonTunaiRecord> = emptyList(),
        walletMutations: List<WalletMutationRecord> = emptyList(),
        preferences: BackupPreferencesSnapshot? = null,
        deviceName: String = safeDeviceName(),
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
                if (!receipt.imagePath.isNullOrBlank()) {
                    rObj.put("imagePath", receipt.imagePath)
                }
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
        sObj.put("lastContributionDateMillis", sedekahSubuhState.lastContributionDate?.time ?: 0L)
        val historyJsonObj = JSONObject()
        for ((dateKey, amt) in sedekahSubuhState.historyMap) {
            historyJsonObj.put(dateKey, amt)
        }
        sObj.put("historyMap", historyJsonObj)
        dataJson.put("sedekahSubuh", sObj)

        // 6. Infaq Rules
        val rulesArray = JSONArray()
        for (r in rules) {
            val rObj = JSONObject()
            rObj.put("id", r.id)
            rObj.put("title", r.title)
            rObj.put("targetCategory", r.targetCategory.name)
            rObj.put("calculationType", r.calculationType.name)
            rObj.put("rate", r.rate)
            rObj.put("fixedAmount", r.fixedAmount)
            rObj.put("roundUpStep", r.roundUpStep)
            rObj.put("enableFridayMultiplier", r.enableFridayMultiplier)
            rObj.put("enableRamadanMultiplier", r.enableRamadanMultiplier)
            rulesArray.put(rObj)
        }
        dataJson.put("rules", rulesArray)

        // 7. Recurring Transactions
        val recArray = JSONArray()
        for (rec in recurring) {
            val rcObj = JSONObject()
            rcObj.put("id", rec.id)
            rcObj.put("title", rec.title)
            rcObj.put("type", rec.type.name)
            rcObj.put("amount", rec.amount)
            rcObj.put("categoryAccountId", rec.categoryAccountId)
            rcObj.put("assetAccountId", rec.assetAccountId)
            rcObj.put("frequency", rec.frequency.name)
            rcObj.put("dayOfMonthOrWeek", rec.dayOfMonthOrWeek)
            rcObj.put("customInfaqRate", rec.customInfaqRate)
            rcObj.put("enableRoundUp", rec.enableRoundUp)
            rcObj.put("roundUpStep", rec.roundUpStep)
            rcObj.put("isActive", rec.isActive)
            rcObj.put("autoExecute", rec.autoExecute)
            rcObj.put("lastExecutedDate", rec.lastExecutedDate?.time ?: -1L)
            rcObj.put("lastExecutedPeriodKey", rec.lastExecutedPeriodKey ?: "")
            rcObj.put("lastExecutionTimestamp", rec.lastExecutionTimestamp)
            rcObj.put("nextDueDate", rec.nextDueDate.time)
            rcObj.put("note", rec.note)
            recArray.put(rcObj)
        }
        dataJson.put("recurring", recArray)

        // 8. Infaq Distributions
        val distArray = JSONArray()
        for (d in distributions) {
            val dObj = JSONObject()
            dObj.put("id", d.id)
            dObj.put("amount", d.amount)
            dObj.put("recipientName", d.recipientName)
            dObj.put("asnafCategory", d.asnafCategory.name)
            dObj.put("distributionDate", d.distributionDate.time)
            dObj.put("hijriDateString", d.hijriDateString)
            dObj.put("sourceAccountId", d.sourceAccountId)
            dObj.put("programName", d.programName)
            dObj.put("receiptNumber", d.receiptNumber)
            dObj.put("notes", d.notes)
            dObj.put("isVerified", d.isVerified)
            distArray.put(dObj)
        }
        dataJson.put("distributions", distArray)

        // 9. Qardh Records
        val qardhArray = JSONArray()
        for (q in qardhRecords) {
            val qObj = JSONObject()
            qObj.put("id", q.id)
            qObj.put("counterpartyName", q.counterpartyName)
            qObj.put("type", q.type.name)
            qObj.put("totalAmount", q.totalAmount)
            qObj.put("remainingAmount", q.remainingAmount)
            qObj.put("startDateMillis", q.startDateMillis)
            qObj.put("dueDateMillis", q.dueDateMillis ?: 0L)
            qObj.put("notes", q.notes)
            qObj.put("status", q.status.name)
            qObj.put("agreementTerms", q.agreementTerms)
            qObj.put("witnessName", q.witnessName)
            qObj.put("witnessContact", q.witnessContact)
            qObj.put("walletId", q.walletId)
            qardhArray.put(qObj)
        }
        dataJson.put("qardh", qardhArray)

        // 10. Ibadah Deposits
        val depArray = JSONArray()
        for (dep in ibadahDeposits) {
            val dpObj = JSONObject()
            dpObj.put("id", dep.id)
            dpObj.put("goalId", dep.goalId)
            dpObj.put("amount", dep.amount)
            dpObj.put("depositDate", dep.depositDate.time)
            dpObj.put("sourceWalletId", dep.sourceWalletId)
            dpObj.put("fromWalletId", dep.fromWalletId)
            dpObj.put("notes", dep.notes)
            depArray.put(dpObj)
        }
        dataJson.put("ibadahDeposits", depArray)

        // 11. Rezeki Non-Tunai (v4)
        val rezekiArray = JSONArray()
        for (rz in rezekiNonTunai) {
            val rzObj = JSONObject()
            rzObj.put("id", rz.id)
            rzObj.put("title", rz.title)
            rzObj.put("giverName", rz.giverName)
            rzObj.put("category", rz.category.name)
            rzObj.put("estimatedValue", rz.estimatedValue)
            rzObj.put("suggestedInfaqRate", rz.suggestedInfaqRate)
            rzObj.put("suggestedInfaqAmount", rz.suggestedInfaqAmount)
            rzObj.put("isPaidInfaq", rz.isPaidInfaq)
            rzObj.put("paidJournalEntryId", rz.paidJournalEntryId ?: "")
            rzObj.put("dateMillis", rz.dateMillis)
            rzObj.put("hijriDateString", rz.hijriDateString)
            rzObj.put("notes", rz.notes)
            rzObj.put("createdAtMillis", rz.createdAtMillis)
            rezekiArray.put(rzObj)
        }
        dataJson.put("rezekiNonTunai", rezekiArray)

        // 12. Wallet Mutations (v4)
        val mutArray = JSONArray()
        for (m in walletMutations) {
            val mObj = JSONObject()
            mObj.put("id", m.id)
            mObj.put("fromWalletId", m.fromWalletId)
            mObj.put("toWalletId", m.toWalletId)
            mObj.put("amount", m.amount)
            mObj.put("adminFee", m.adminFee)
            mObj.put("note", m.note)
            mObj.put("timestampMillis", m.timestampMillis)
            mObj.put("transactionId", m.transactionId)
            mObj.put("linkedJournalEntryId", m.linkedJournalEntryId)
            mutArray.put(mObj)
        }
        dataJson.put("walletMutations", mutArray)

        // 13. Full App & Notification Preferences Snapshot (v4)
        if (preferences != null) {
            dataJson.put("preferences", preferences.toJson())
        }

        val rawJsonString = dataJson.toString()
        val dataChecksum = calculateSha256(rawJsonString)

        val rawBytes = rawJsonString.toByteArray(StandardCharsets.UTF_8)
        val encryptedBytes = if (password.isNotEmpty()) {
            encryptAesGcm(rawBytes, password)
        } else {
            rawBytes
        }
        val encodedPayload = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        rootJson.put("schemaVersion", CURRENT_SCHEMA_VERSION)
        rootJson.put("appVersion", "2.5.0")
        rootJson.put("appName", "Infaqan Syariah (Amanah Ledger)")
        rootJson.put("exportedAt", formatIsoTimestamp())
        rootJson.put("deviceName", deviceName)
        rootJson.put("entriesCount", journalEntries.size)
        rootJson.put("walletsCount", wallets.size)
        rootJson.put("goalsCount", goals.size)
        rootJson.put("qardhCount", qardhRecords.size)
        rootJson.put("rulesCount", rules.size)
        rootJson.put("recurringCount", recurring.size)
        rootJson.put("distributionsCount", distributions.size)
        rootJson.put("rezekiCount", rezekiNonTunai.size)
        rootJson.put("walletMutationsCount", walletMutations.size)
        rootJson.put("hasPreferences", preferences != null)
        rootJson.put("checksum", dataChecksum)
        rootJson.put("isEncrypted", password.isNotEmpty())
        rootJson.put("encryptionAlgorithm", if (password.isNotEmpty()) "AES-256-GCM" else "NONE")
        rootJson.put("payload", encodedPayload)

        return rootJson.toString(2)
    }

    /**
     * Automatically migrates payload schema from legacy versions (v1, v2, v3) to v4 cleanly.
     */
    fun migratePayload(dataJson: JSONObject, fromVersion: Int, targetVersion: Int): JSONObject {
        if (fromVersion < 2) {
            val wallets = dataJson.optJSONArray("wallets")
            if (wallets != null) {
                for (i in 0 until wallets.length()) {
                    val w = wallets.optJSONObject(i) ?: continue
                    if (!w.has("linkedAccountId")) {
                        val type = w.optString("type", "CASH")
                        val defaultAccount = when {
                            type.contains("BANK") -> "acc_bank"
                            type.contains("E_WALLET") -> "acc_ewallet"
                            type.contains("GOLD") -> "acc_gold"
                            type.contains("OTHER") -> "acc_other_asset"
                            else -> "acc_cash"
                        }
                        w.put("linkedAccountId", defaultAccount)
                    }
                }
            }
        }
        if (fromVersion < 3) {
            val recurring = dataJson.optJSONArray("recurring")
            if (recurring != null) {
                for (i in 0 until recurring.length()) {
                    val r = recurring.optJSONObject(i) ?: continue
                    if (!r.has("lastExecutedPeriodKey")) {
                        r.put("lastExecutedPeriodKey", "")
                    }
                    if (!r.has("lastExecutionTimestamp")) {
                        r.put("lastExecutionTimestamp", 0L)
                    }
                }
            }
        }
        if (fromVersion < 4 && targetVersion >= 4) {
            if (!dataJson.has("rezekiNonTunai")) {
                dataJson.put("rezekiNonTunai", JSONArray())
            }
            if (!dataJson.has("walletMutations")) {
                dataJson.put("walletMutations", JSONArray())
            }
        }
        return dataJson
    }

    /**
     * Restore and verify backup package (supports schema v1 through v4).
     */
    fun parseAndRestore(backupText: String, password: String = ""): RestoreResult {
        return try {
            val trimmed = backupText.trim()
            val rootJson = JSONObject(trimmed)

            val schemaVersion = rootJson.optInt("schemaVersion", 1)
            val appVersion = rootJson.optString("appVersion", "2.0.0")
            val exportedAt = rootJson.optString("exportedAt", "Tidak Diketahui")
            val deviceName = rootJson.optString("deviceName", "Perangkat Android")
            val expectedChecksum = rootJson.getString("checksum")
            val isEncrypted = rootJson.optBoolean("isEncrypted", false)
            val payloadBase64 = rootJson.getString("payload")

            val encryptedBytes = Base64.decode(payloadBase64, Base64.DEFAULT)
            val decryptedBytes = if (isEncrypted) {
                if (password.isEmpty()) {
                    return RestoreResult.Failure("Berkas cadangan ini diproteksi sandi. Harap masukkan kata sandi enkripsi.")
                }
                if (encryptedBytes.size > 4 &&
                    encryptedBytes[0] == 'A'.code.toByte() &&
                    encryptedBytes[1] == '2'.code.toByte() &&
                    encryptedBytes[2] == '5'.code.toByte() &&
                    encryptedBytes[3] == '6'.code.toByte()
                ) {
                    try {
                        decryptAesGcm(encryptedBytes, password)
                    } catch (e: Exception) {
                        return RestoreResult.Failure("Kata sandi salah atau berkas enkripsi AES rusak (${e.localizedMessage}).")
                    }
                } else {
                    encryptDecryptXor(encryptedBytes, password)
                }
            } else {
                encryptedBytes
            }

            val decryptedJsonString = String(decryptedBytes, StandardCharsets.UTF_8)

            // Checksum verification
            val actualChecksum = calculateSha256(decryptedJsonString)
            if (actualChecksum != expectedChecksum) {
                return RestoreResult.Failure("Verifikasi Integritas Gagal (Checksum Mismatch). Sandi mungkin keliru atau berkas rusak/termodifikasi.")
            }

            val rawDataJson = JSONObject(decryptedJsonString)
            val dataJson = migratePayload(rawDataJson, schemaVersion, CURRENT_SCHEMA_VERSION)

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
                        } catch (_: Exception) {
                            ReceiptType.STORE_RECEIPT
                        },
                        merchantName = rObj.optString("merchantName", ""),
                        referenceNumber = rObj.optString("referenceNumber", ""),
                        digitalVerificationHash = rObj.optString("digitalVerificationHash", ""),
                        notes = rObj.optString("notes", ""),
                        amount = rObj.optDouble("amount", 0.0),
                        createdAtMillis = rObj.optLong("createdAtMillis", System.currentTimeMillis()),
                        imagePath = if (rObj.has("imagePath") && rObj.getString("imagePath").isNotBlank()) rObj.getString("imagePath") else null
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
                        } catch (_: Exception) {
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
                        } catch (_: Exception) {
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
                val hMap = mutableMapOf<String, Double>()
                val hObj = sObj.optJSONObject("historyMap")
                if (hObj != null) {
                    val keys = hObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        hMap[k] = hObj.optDouble(k, 0.0)
                    }
                }
                val lastMillis = sObj.optLong("lastContributionDateMillis", 0L)
                val lastDate = if (lastMillis > 0L) Date(lastMillis) else null
                if (hMap.isNotEmpty()) {
                    val calc = SedekahSubuhStreakEngine.calculateStreak(hMap)
                    calc.copy(
                        currentStreak = maxOf(calc.currentStreak, sObj.optInt("currentStreak", 0)),
                        longestStreak = maxOf(calc.longestStreak, sObj.optInt("longestStreak", 0)),
                        totalContributions = maxOf(calc.totalContributions, sObj.optDouble("totalContributions", 0.0)),
                        totalDaysGiven = maxOf(calc.totalDaysGiven, sObj.optInt("totalDaysGiven", 0)),
                        lastContributionDate = lastDate ?: calc.lastContributionDate
                    )
                } else {
                    SedekahSubuhState(
                        currentStreak = sObj.optInt("currentStreak", 0),
                        longestStreak = sObj.optInt("longestStreak", 0),
                        totalContributions = sObj.optDouble("totalContributions", 0.0),
                        totalDaysGiven = sObj.optInt("totalDaysGiven", 0),
                        lastContributionDate = lastDate,
                        badges = SedekahSubuhState.getDefaultBadges()
                    )
                }
            } else {
                SedekahSubuhState()
            }

            // Parse Infaq Rules
            val restoredRules = mutableListOf<InfaqRule>()
            val rulesArray = dataJson.optJSONArray("rules") ?: JSONArray()
            for (i in 0 until rulesArray.length()) {
                val rObj = rulesArray.getJSONObject(i)
                restoredRules.add(
                    InfaqRule(
                        id = rObj.getString("id"),
                        title = rObj.getString("title"),
                        targetCategory = try { AccountCategory.valueOf(rObj.getString("targetCategory")) } catch (_: Exception) { AccountCategory.EXPENSE },
                        calculationType = try { InfaqCalculationType.valueOf(rObj.getString("calculationType")) } catch (_: Exception) { InfaqCalculationType.PERCENTAGE },
                        rate = rObj.optDouble("rate", 0.05),
                        fixedAmount = rObj.optDouble("fixedAmount", 0.0),
                        roundUpStep = rObj.optDouble("roundUpStep", 5000.0),
                        enableFridayMultiplier = rObj.optBoolean("enableFridayMultiplier", true),
                        enableRamadanMultiplier = rObj.optBoolean("enableRamadanMultiplier", true)
                    )
                )
            }

            // Parse Recurring Transactions
            val restoredRecurring = mutableListOf<RecurringTransaction>()
            val recArray = dataJson.optJSONArray("recurring") ?: JSONArray()
            for (i in 0 until recArray.length()) {
                val rcObj = recArray.getJSONObject(i)
                val lastExecMillis = rcObj.optLong("lastExecutedDate", -1L)
                restoredRecurring.add(
                    RecurringTransaction(
                        id = rcObj.getString("id"),
                        title = rcObj.getString("title"),
                        type = try { RecurringType.valueOf(rcObj.getString("type")) } catch (_: Exception) { RecurringType.EXPENSE },
                        amount = rcObj.getDouble("amount"),
                        categoryAccountId = rcObj.getString("categoryAccountId"),
                        assetAccountId = rcObj.getString("assetAccountId"),
                        frequency = try { RecurringFrequency.valueOf(rcObj.getString("frequency")) } catch (_: Exception) { RecurringFrequency.MONTHLY },
                        dayOfMonthOrWeek = rcObj.optInt("dayOfMonthOrWeek", 1),
                        customInfaqRate = rcObj.optDouble("customInfaqRate", 0.05),
                        enableRoundUp = rcObj.optBoolean("enableRoundUp", true),
                        roundUpStep = rcObj.optDouble("roundUpStep", 5000.0),
                        isActive = rcObj.optBoolean("isActive", true),
                        autoExecute = rcObj.optBoolean("autoExecute", true),
                        lastExecutedDate = if (lastExecMillis > 0) Date(lastExecMillis) else null,
                        lastExecutedPeriodKey = rcObj.optString("lastExecutedPeriodKey", "").ifBlank { null },
                        lastExecutionTimestamp = rcObj.optLong("lastExecutionTimestamp", 0L),
                        nextDueDate = Date(rcObj.optLong("nextDueDate", System.currentTimeMillis())),
                        note = rcObj.optString("note", "")
                    )
                )
            }

            // Parse Infaq Distributions
            val restoredDistributions = mutableListOf<InfaqDistributionRecord>()
            val distArray = dataJson.optJSONArray("distributions") ?: JSONArray()
            for (i in 0 until distArray.length()) {
                val dObj = distArray.getJSONObject(i)
                restoredDistributions.add(
                    InfaqDistributionRecord(
                        id = dObj.getString("id"),
                        amount = dObj.getDouble("amount"),
                        recipientName = dObj.getString("recipientName"),
                        asnafCategory = try { AsnafCategory.valueOf(dObj.getString("asnafCategory")) } catch (_: Exception) { AsnafCategory.UMUM },
                        distributionDate = Date(dObj.optLong("distributionDate", System.currentTimeMillis())),
                        hijriDateString = dObj.optString("hijriDateString", ""),
                        sourceAccountId = dObj.optString("sourceAccountId", "acc_bank"),
                        programName = dObj.optString("programName", "Penyaluran Mandiri"),
                        receiptNumber = dObj.optString("receiptNumber", ""),
                        notes = dObj.optString("notes", ""),
                        isVerified = dObj.optBoolean("isVerified", true)
                    )
                )
            }

            // Parse Qardh Records
            val restoredQardh = mutableListOf<QardhRecord>()
            val qardhArray = dataJson.optJSONArray("qardh") ?: JSONArray()
            for (i in 0 until qardhArray.length()) {
                val qObj = qardhArray.getJSONObject(i)
                val dueMillis = qObj.optLong("dueDateMillis", 0L)
                restoredQardh.add(
                    QardhRecord(
                        id = qObj.getString("id"),
                        counterpartyName = qObj.getString("counterpartyName"),
                        type = try { QardhType.valueOf(qObj.getString("type")) } catch (_: Exception) { QardhType.HUTANG_SAYA },
                        totalAmount = qObj.getDouble("totalAmount"),
                        remainingAmount = qObj.optDouble("remainingAmount", qObj.getDouble("totalAmount")),
                        startDateMillis = qObj.optLong("startDateMillis", System.currentTimeMillis()),
                        dueDateMillis = if (dueMillis > 0L) dueMillis else null,
                        notes = qObj.optString("notes", ""),
                        status = try { QardhStatus.valueOf(qObj.getString("status")) } catch (_: Exception) { QardhStatus.AKTIF },
                        agreementTerms = qObj.optString("agreementTerms", ""),
                        witnessName = qObj.optString("witnessName", ""),
                        witnessContact = qObj.optString("witnessContact", ""),
                        walletId = qObj.optString("walletId", "acc_cash")
                    )
                )
            }

            // Parse Ibadah Deposits
            val restoredIbadahDeposits = mutableListOf<IbadahDepositRecord>()
            val depArray = dataJson.optJSONArray("ibadahDeposits") ?: JSONArray()
            for (i in 0 until depArray.length()) {
                val dpObj = depArray.getJSONObject(i)
                val depDate = dpObj.optLong("depositDate", System.currentTimeMillis())
                val srcWallet = dpObj.optString("sourceWalletId", "acc_cash")
                val depNotes = dpObj.optString("notes", "")
                restoredIbadahDeposits.add(
                    IbadahDepositRecord(
                        id = dpObj.getString("id"),
                        goalId = dpObj.getString("goalId"),
                        amount = dpObj.getDouble("amount"),
                        depositDate = Date(depDate),
                        dateMillis = depDate,
                        sourceWalletId = srcWallet,
                        fromWalletId = dpObj.optString("fromWalletId", srcWallet),
                        notes = depNotes,
                        note = depNotes
                    )
                )
            }

            // Parse Rezeki Non-Tunai (v4)
            val restoredRezeki = mutableListOf<RezekiNonTunaiRecord>()
            val rezekiArray = dataJson.optJSONArray("rezekiNonTunai") ?: JSONArray()
            for (i in 0 until rezekiArray.length()) {
                val rzObj = rezekiArray.getJSONObject(i)
                val est = rzObj.optDouble("estimatedValue", 0.0)
                val rate = rzObj.optDouble("suggestedInfaqRate", 0.05)
                val paidEntry = rzObj.optString("paidJournalEntryId", "").ifBlank { null }
                restoredRezeki.add(
                    RezekiNonTunaiRecord(
                        id = rzObj.getString("id"),
                        title = rzObj.getString("title"),
                        giverName = rzObj.optString("giverName", ""),
                        category = try { RezekiCategory.valueOf(rzObj.optString("category")) } catch (_: Exception) { RezekiCategory.FOOD_BEVERAGE },
                        estimatedValue = est,
                        suggestedInfaqRate = rate,
                        suggestedInfaqAmount = rzObj.optDouble("suggestedInfaqAmount", est * rate),
                        isPaidInfaq = rzObj.optBoolean("isPaidInfaq", false),
                        paidJournalEntryId = paidEntry,
                        dateMillis = rzObj.optLong("dateMillis", System.currentTimeMillis()),
                        hijriDateString = rzObj.optString("hijriDateString", ""),
                        notes = rzObj.optString("notes", ""),
                        createdAtMillis = rzObj.optLong("createdAtMillis", System.currentTimeMillis())
                    )
                )
            }

            // Parse Wallet Mutations (v4)
            val restoredMutations = mutableListOf<WalletMutationRecord>()
            val mutArray = dataJson.optJSONArray("walletMutations") ?: JSONArray()
            for (i in 0 until mutArray.length()) {
                val mObj = mutArray.getJSONObject(i)
                val ts = mObj.optLong("timestampMillis", System.currentTimeMillis())
                restoredMutations.add(
                    WalletMutationRecord(
                        id = mObj.getString("id"),
                        fromWalletId = mObj.getString("fromWalletId"),
                        toWalletId = mObj.getString("toWalletId"),
                        amount = mObj.optDouble("amount", 0.0),
                        adminFee = mObj.optDouble("adminFee", 0.0),
                        note = mObj.optString("note", ""),
                        timestampMillis = ts,
                        date = Date(ts),
                        transactionId = mObj.optString("transactionId", ""),
                        linkedJournalEntryId = mObj.optString("linkedJournalEntryId", "")
                    )
                )
            }

            // Parse Preferences Snapshot (v4)
            val restoredPreferences = BackupPreferencesSnapshot.fromJson(dataJson.optJSONObject("preferences"))

            val manifest = BackupManifest(
                schemaVersion = schemaVersion,
                exportedAt = exportedAt,
                journalEntriesCount = restoredEntries.size,
                walletsCount = restoredWallets.size,
                goalsCount = restoredGoals.size,
                qardhCount = restoredQardh.size,
                rulesCount = restoredRules.size,
                recurringCount = restoredRecurring.size,
                distributionsCount = restoredDistributions.size,
                rezekiCount = restoredRezeki.size,
                walletMutationsCount = restoredMutations.size,
                hasPreferences = restoredPreferences != null,
                deviceName = deviceName,
                checksum = expectedChecksum,
                isPasswordProtected = isEncrypted,
                appVersion = appVersion,
                isCompatible = schemaVersion in 1..CURRENT_SCHEMA_VERSION,
                migrationApplied = schemaVersion < CURRENT_SCHEMA_VERSION
            )

            RestoreResult.Success(
                manifest = manifest,
                restoredEntries = restoredEntries,
                restoredWallets = restoredWallets,
                restoredGoals = restoredGoals,
                restoredBudgets = restoredBudgets,
                restoredRules = restoredRules,
                restoredRecurring = restoredRecurring,
                restoredSedekahState = restoredSedekah,
                restoredDistributions = restoredDistributions,
                restoredQardh = restoredQardh,
                restoredIbadahDeposits = restoredIbadahDeposits,
                restoredRezekiNonTunai = restoredRezeki,
                restoredWalletMutations = restoredMutations,
                restoredPreferences = restoredPreferences
            )
        } catch (e: Exception) {
            RestoreResult.Failure("Gagal memproses berkas cadangan: ${e.localizedMessage ?: e.message}")
        }
    }
}

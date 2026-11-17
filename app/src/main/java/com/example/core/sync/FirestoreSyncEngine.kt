package com.example.core.sync

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.room.withTransaction
import com.example.R
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.auth.FirebaseSafeInitializer
import com.example.core.backup.BackupEngine
import com.example.core.backup.BackupPreferencesSnapshot
import com.example.core.budget.BudgetAllocation
import com.example.core.database.AmanahDatabase
import com.example.core.database.EntityMappers
import com.example.core.database.SedekahSubuhEntity
import com.example.core.database.SettingsEntity
import com.example.core.ibadah.IbadahDepositRecord
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.infaq.AsnafCategory
import com.example.core.infaq.InfaqCalculationType
import com.example.core.infaq.InfaqDistributionRecord
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.SedekahSubuhState
import com.example.core.infaq.SedekahSubuhStreakEngine
import com.example.core.qardh.QardhRecord
import com.example.core.rezeki.RezekiCategory
import com.example.core.rezeki.RezekiNonTunaiRecord
import com.example.core.scheduler.RecurringFrequency
import com.example.core.scheduler.RecurringTransaction
import com.example.core.scheduler.RecurringType
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletMutationRecord
import com.example.core.wallet.WalletType
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncStateStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
    OFFLINE_ONLY
}

data class SyncState(
    val status: SyncStateStatus = SyncStateStatus.IDLE,
    val lastSyncedTimeMillis: Long = 0L,
    val lastSyncedTimeString: String = "Belum pernah disinkronkan",
    val itemsSyncedCount: Int = 0,
    val errorMessage: String? = null,
    val isAutoSyncEnabled: Boolean = true
)

data class CloudSnapshotSummary(
    val exists: Boolean = false,
    val updatedAtMillis: Long = 0L,
    val updatedAtFormatted: String = "Belum ada cadangan Cloud",
    val deviceName: String = "-",
    val totalBackedUpItems: Int = 0,
    val backupChecksum: String = "",
    val hasPreferences: Boolean = false
)

data class CloudRestorePayload(
    val entries: List<JournalEntry>,
    val wallets: List<WalletAccount>,
    val goals: List<IbadahGoal>,
    val qardh: List<QardhRecord>,
    val budgets: List<BudgetAllocation>,
    val sedekahState: SedekahSubuhState,
    val rules: List<InfaqRule>,
    val recurring: List<RecurringTransaction>,
    val distributions: List<InfaqDistributionRecord>,
    val rezekiNonTunai: List<RezekiNonTunaiRecord>,
    val walletMutations: List<WalletMutationRecord>,
    val ibadahDeposits: List<IbadahDepositRecord>,
    val preferences: BackupPreferencesSnapshot?
)

class FirestoreSyncEngine(
    private val context: Context,
    private val database: AmanahDatabase
) {
    private val appContext = context.applicationContext
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    companion object {
        private const val TAG = "FirestoreSyncEngine"
        private const val FIRESTORE_BATCH_LIMIT = 400
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseSafeInitializer.init(appContext)
            val dbId = appContext.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
            } else {
                Log.e(TAG, "firestore_database_id resource is blank.")
                null
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Firestore named database initialization failed: ${e.message}", e)
            null
        }
    }

    private val timeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))

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

    private fun logFirestoreError(operation: String, userId: String, error: Throwable) {
        val logJson = JSONObject().apply {
            put("event", "firestore_error")
            put("operation", operation)
            put("userIdPresent", userId.isNotBlank() && !userId.startsWith("guest_"))
            put("errorType", error::class.java.simpleName)
            put("message", error.message ?: "unknown")
        }.toString()
        Log.e(TAG, logJson, error)
    }

    private fun validateCloudPrerequisites(userId: String): Result<FirebaseFirestore> {
        val fs = firestore
            ?: return Result.failure(IllegalStateException("Layanan Cloud Firestore belum terkonfigurasi dengan database ID proyek."))
        if (userId.isBlank() || userId == "guest_local_user" || userId.startsWith("guest_")) {
            return Result.failure(IllegalStateException("Autentikasi Google diperlukan untuk mengakses Cloud Backup Firestore."))
        }
        return Result.success(fs)
    }

    private suspend fun executeChunkedBatchOps(
        fs: FirebaseFirestore,
        ops: List<(com.google.firebase.firestore.WriteBatch) -> Unit>
    ) {
        if (ops.isEmpty()) return
        for (chunk in ops.chunked(FIRESTORE_BATCH_LIMIT)) {
            val batch = fs.batch()
            chunk.forEach { applyOp -> applyOp(batch) }
            batch.commit().await()
        }
    }

    private fun serializeWalletMutations(mutations: List<WalletMutationRecord>): String {
        val arr = JSONArray()
        for (m in mutations.take(200)) {
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("fromWalletId", m.fromWalletId)
                put("toWalletId", m.toWalletId)
                put("amount", m.amount)
                put("adminFee", m.adminFee)
                put("note", m.note)
                put("timestampMillis", m.timestampMillis)
                put("transactionId", m.transactionId)
                put("linkedJournalEntryId", m.linkedJournalEntryId)
            })
        }
        return arr.toString()
    }

    private fun deserializeWalletMutations(raw: String?): List<WalletMutationRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<WalletMutationRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val ts = obj.optLong("timestampMillis", System.currentTimeMillis())
                list.add(
                    WalletMutationRecord(
                        id = obj.getString("id"),
                        fromWalletId = obj.getString("fromWalletId"),
                        toWalletId = obj.getString("toWalletId"),
                        amount = obj.optDouble("amount", 0.0),
                        adminFee = obj.optDouble("adminFee", 0.0),
                        note = obj.optString("note", ""),
                        timestampMillis = ts,
                        date = Date(ts),
                        transactionId = obj.optString("transactionId", ""),
                        linkedJournalEntryId = obj.optString("linkedJournalEntryId", "")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun serializeIbadahDeposits(deposits: List<IbadahDepositRecord>): String {
        val arr = JSONArray()
        for (dep in deposits.take(500)) {
            arr.put(JSONObject().apply {
                put("id", dep.id)
                put("goalId", dep.goalId)
                put("amount", dep.amount)
                put("sourceWalletId", dep.sourceWalletId)
                put("fromWalletId", dep.fromWalletId)
                put("depositDateMillis", dep.depositDate.time)
                put("notes", dep.notes)
            })
        }
        return arr.toString()
    }

    private fun deserializeIbadahDeposits(raw: String?): List<IbadahDepositRecord> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<IbadahDepositRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val ts = obj.optLong("depositDateMillis", System.currentTimeMillis())
                val srcWallet = obj.optString("sourceWalletId", obj.optString("fromWalletId", "acc_bank"))
                val notes = obj.optString("notes", "")
                list.add(
                    IbadahDepositRecord(
                        id = obj.getString("id"),
                        goalId = obj.getString("goalId"),
                        amount = obj.optDouble("amount", 0.0),
                        sourceWalletId = srcWallet,
                        fromWalletId = obj.optString("fromWalletId", srcWallet),
                        depositDate = Date(ts),
                        dateMillis = ts,
                        notes = notes,
                        note = notes
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCloudSnapshotSummary(userId: String): Result<CloudSnapshotSummary> = withContext(Dispatchers.IO) {
        val fs = validateCloudPrerequisites(userId).getOrElse { return@withContext Result.failure(it) }
        try {
            val metaSnap = fs.collection("amanah_users").document(userId).get().await()
            if (!metaSnap.exists()) {
                return@withContext Result.success(CloudSnapshotSummary(exists = false))
            }
            val updatedAtTimestamp = metaSnap.getTimestamp("updatedAt")?.toDate()?.time
                ?: metaSnap.getLong("updatedAtMillis")
                ?: 0L
            val formatted = if (updatedAtTimestamp > 0L) {
                timeFormat.format(Date(updatedAtTimestamp))
            } else {
                "Tersimpan di Cloud"
            }
            Result.success(
                CloudSnapshotSummary(
                    exists = true,
                    updatedAtMillis = updatedAtTimestamp,
                    updatedAtFormatted = formatted,
                    deviceName = metaSnap.getString("deviceName") ?: "Perangkat Android",
                    totalBackedUpItems = (metaSnap.getLong("totalBackedUpItems") ?: 0L).toInt(),
                    backupChecksum = metaSnap.getString("backupChecksum") ?: "",
                    hasPreferences = !metaSnap.getString("preferencesJson").isNullOrBlank()
                )
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logFirestoreError("fetchCloudSnapshotSummary", userId, e)
            Result.failure(e)
        }
    }

    suspend fun syncAll(
        userId: String,
        localEntries: List<JournalEntry>,
        localWallets: List<WalletAccount>,
        localGoals: List<IbadahGoal>,
        localQardh: List<QardhRecord>,
        localBudgets: List<BudgetAllocation>,
        sedekahState: SedekahSubuhState,
        localRules: List<InfaqRule> = emptyList(),
        localRecurring: List<RecurringTransaction> = emptyList(),
        localDistributions: List<InfaqDistributionRecord> = emptyList(),
        localRezeki: List<RezekiNonTunaiRecord> = emptyList(),
        localWalletMutations: List<WalletMutationRecord> = emptyList(),
        localIbadahDeposits: List<IbadahDepositRecord> = emptyList(),
        preferences: BackupPreferencesSnapshot? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        val fs = validateCloudPrerequisites(userId).getOrElse { err ->
            _syncState.value = _syncState.value.copy(
                status = SyncStateStatus.OFFLINE_ONLY,
                errorMessage = err.message
            )
            return@withContext Result.failure(err)
        }

        _syncState.value = _syncState.value.copy(
            status = SyncStateStatus.SYNCING,
            errorMessage = null
        )

        try {
            val userDocRef = fs.collection("amanah_users").document(userId)
            val ops = mutableListOf<(com.google.firebase.firestore.WriteBatch) -> Unit>()

            val totalSynced = localEntries.size + localWallets.size + localGoals.size +
                localQardh.size + localBudgets.size + localRules.size +
                localRecurring.size + localDistributions.size + localRezeki.size

            val historyJsonObj = JSONObject()
            for ((k, v) in sedekahState.historyMap) {
                historyJsonObj.put(k, v)
            }
            val walletMutationsJson = serializeWalletMutations(localWalletMutations)
            val ibadahDepositsJson = serializeIbadahDeposits(localIbadahDeposits)
            val preferencesJson = preferences?.toJson()?.toString() ?: ""
            val checksumSeed = "$totalSynced:${localEntries.firstOrNull()?.id.orEmpty()}:${preferencesJson.hashCode()}"
            val checksum = BackupEngine.calculateSha256(checksumSeed)

            val userMeta = hashMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp(),
                "updatedAtMillis" to System.currentTimeMillis(),
                "deviceName" to safeDeviceName(),
                "totalBackedUpItems" to totalSynced,
                "backupChecksum" to checksum,
                "sedekahStreak" to sedekahState.currentStreak,
                "sedekahLongest" to sedekahState.longestStreak,
                "sedekahTotal" to sedekahState.totalContributions,
                "sedekahDays" to sedekahState.totalDaysGiven,
                "sedekahLastMillis" to (sedekahState.lastContributionDate?.time ?: 0L),
                "sedekahHistoryJson" to historyJsonObj.toString(),
                "walletMutationsJson" to walletMutationsJson,
                "ibadahDepositsJson" to ibadahDepositsJson,
                "preferencesJson" to preferencesJson
            )
            ops.add { batch -> batch.set(userDocRef, userMeta, SetOptions.merge()) }

            suspend fun enqueueCollectionSync(
                subcollection: String,
                docs: List<Pair<String, HashMap<String, Any>>>
            ) {
                val coll = userDocRef.collection(subcollection)
                val existingIds = coll.get().await().documents.map { it.id }.toSet()
                val currentIds = docs.map { it.first }.toSet()

                for ((id, payload) in docs) {
                    ops.add { batch -> batch.set(coll.document(id), payload, SetOptions.merge()) }
                }
                for (staleId in existingIds - currentIds) {
                    ops.add { batch -> batch.delete(coll.document(staleId)) }
                }
            }

            enqueueCollectionSync(
                "wallets",
                localWallets.map { w ->
                    w.id to hashMapOf(
                        "id" to w.id,
                        "name" to w.name,
                        "type" to w.type.name,
                        "institutionName" to w.institutionName,
                        "accountNumber" to w.accountNumber,
                        "linkedAccountId" to w.linkedAccountId,
                        "colorHex" to w.colorHex,
                        "isDefault" to w.isDefault,
                        "notes" to w.notes,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "journal_entries",
                localEntries.map { e ->
                    val entity = EntityMappers.toEntity(e)
                    e.id to hashMapOf(
                        "id" to entity.id,
                        "dateMillis" to entity.dateMillis,
                        "hijriYear" to entity.hijriYear,
                        "hijriMonth" to entity.hijriMonth,
                        "hijriDay" to entity.hijriDay,
                        "description" to entity.description,
                        "transactionType" to entity.transactionType,
                        "linesJson" to entity.linesJson,
                        "receiptJson" to (entity.receiptAttachmentJson ?: ""),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "ibadah_goals",
                localGoals.map { g ->
                    g.id to hashMapOf(
                        "id" to g.id,
                        "type" to g.type.name,
                        "title" to g.title,
                        "targetAmount" to g.targetAmount,
                        "currentAccumulated" to g.currentAccumulated,
                        "targetHijri" to g.targetHijriYearMonth,
                        "monthsRemaining" to g.targetMonthsRemaining,
                        "linkedWalletId" to g.linkedWalletId,
                        "notes" to g.notes,
                        "isCompleted" to g.isCompleted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "qardh_records",
                localQardh.map { q ->
                    val entity = EntityMappers.toEntity(q)
                    q.id to hashMapOf(
                        "id" to entity.id,
                        "type" to entity.type,
                        "counterpartyName" to entity.counterpartyName,
                        "totalAmount" to entity.totalAmount,
                        "remainingAmount" to entity.remainingAmount,
                        "agreementDateMillis" to entity.agreementDateMillis,
                        "dueDateMillis" to (entity.dueDateMillis ?: 0L),
                        "witnessName" to entity.witnessName,
                        "notes" to entity.notes,
                        "status" to entity.status,
                        "installmentsJson" to entity.installmentsJson,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "budgets",
                localBudgets.map { b ->
                    b.id to hashMapOf(
                        "id" to b.id,
                        "accountId" to b.accountId,
                        "categoryName" to b.categoryName,
                        "monthlyLimit" to b.monthlyLimit,
                        "iconKey" to b.iconKey,
                        "alertThresholdPercent" to b.alertThresholdPercent,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "infaq_rules",
                localRules.map { r ->
                    r.id to hashMapOf(
                        "id" to r.id,
                        "title" to r.title,
                        "targetCategory" to r.targetCategory.name,
                        "calculationType" to r.calculationType.name,
                        "rate" to r.rate,
                        "fixedAmount" to r.fixedAmount,
                        "roundUpStep" to r.roundUpStep,
                        "enableFridayMultiplier" to r.enableFridayMultiplier,
                        "enableRamadanMultiplier" to r.enableRamadanMultiplier,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "recurring_transactions",
                localRecurring.map { rec ->
                    rec.id to hashMapOf(
                        "id" to rec.id,
                        "title" to rec.title,
                        "type" to rec.type.name,
                        "amount" to rec.amount,
                        "categoryAccountId" to rec.categoryAccountId,
                        "assetAccountId" to rec.assetAccountId,
                        "frequency" to rec.frequency.name,
                        "dayOfMonthOrWeek" to rec.dayOfMonthOrWeek,
                        "customInfaqRate" to rec.customInfaqRate,
                        "enableRoundUp" to rec.enableRoundUp,
                        "roundUpStep" to rec.roundUpStep,
                        "isActive" to rec.isActive,
                        "autoExecute" to rec.autoExecute,
                        "lastExecutedDateMillis" to (rec.lastExecutedDate?.time ?: -1L),
                        "lastExecutedPeriodKey" to (rec.lastExecutedPeriodKey ?: ""),
                        "lastExecutionTimestamp" to rec.lastExecutionTimestamp,
                        "nextDueDateMillis" to rec.nextDueDate.time,
                        "note" to rec.note,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "infaq_distributions",
                localDistributions.map { d ->
                    d.id to hashMapOf(
                        "id" to d.id,
                        "amount" to d.amount,
                        "recipientName" to d.recipientName,
                        "asnafCategory" to d.asnafCategory.name,
                        "distributionDateMillis" to d.distributionDate.time,
                        "hijriDateString" to d.hijriDateString,
                        "sourceAccountId" to d.sourceAccountId,
                        "programName" to d.programName,
                        "receiptNumber" to d.receiptNumber,
                        "notes" to d.notes,
                        "isVerified" to d.isVerified,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            enqueueCollectionSync(
                "rezeki_non_tunai",
                localRezeki.map { rz ->
                    rz.id to hashMapOf(
                        "id" to rz.id,
                        "title" to rz.title,
                        "giverName" to rz.giverName,
                        "category" to rz.category.name,
                        "estimatedValue" to rz.estimatedValue,
                        "suggestedInfaqRate" to rz.suggestedInfaqRate,
                        "suggestedInfaqAmount" to rz.suggestedInfaqAmount,
                        "isPaidInfaq" to rz.isPaidInfaq,
                        "paidJournalEntryId" to (rz.paidJournalEntryId ?: ""),
                        "dateMillis" to rz.dateMillis,
                        "hijriDateString" to rz.hijriDateString,
                        "notes" to rz.notes,
                        "createdAtMillis" to rz.createdAtMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                }
            )

            executeChunkedBatchOps(fs, ops)

            val now = System.currentTimeMillis()
            _syncState.value = SyncState(
                status = SyncStateStatus.SUCCESS,
                lastSyncedTimeMillis = now,
                lastSyncedTimeString = timeFormat.format(Date(now)),
                itemsSyncedCount = totalSynced,
                errorMessage = null
            )

            Log.d(TAG, "Cloud sync completed: $totalSynced items synced across 9 collections.")
            Result.success(totalSynced)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logFirestoreError("syncAll", userId, e)
            _syncState.value = _syncState.value.copy(
                status = SyncStateStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Gagal menyinkronkan dengan Cloud Firestore"
            )
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(
        userId: String,
        replaceLocal: Boolean = true
    ): Result<CloudRestorePayload> = withContext(Dispatchers.IO) {
        val fs = validateCloudPrerequisites(userId).getOrElse { return@withContext Result.failure(it) }

        _syncState.value = _syncState.value.copy(
            status = SyncStateStatus.SYNCING,
            errorMessage = null
        )

        try {
            val userDocRef: DocumentReference = fs.collection("amanah_users").document(userId)
            val metaSnap = userDocRef.get().await()
            if (!metaSnap.exists()) {
                val noDataEx = IllegalStateException("Belum ada data cadangan Cloud di akun Google ini.")
                _syncState.value = _syncState.value.copy(
                    status = SyncStateStatus.ERROR,
                    errorMessage = noDataEx.message
                )
                return@withContext Result.failure(noDataEx)
            }

            // 1. Metadata & Sedekah Subuh
            val rawSedekahHistoryJson = metaSnap.getString("sedekahHistoryJson")
            val hMap = mutableMapOf<String, Double>()
            if (!rawSedekahHistoryJson.isNullOrBlank()) {
                try {
                    val obj = JSONObject(rawSedekahHistoryJson)
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        hMap[k] = obj.optDouble(k, 0.0)
                    }
                } catch (_: Exception) {}
            }
            val lastMillis = metaSnap.getLong("sedekahLastMillis") ?: 0L
            val lastDate = if (lastMillis > 0L) Date(lastMillis) else null
            val baseStreak = (metaSnap.getLong("sedekahStreak") ?: 0L).toInt()
            val baseLongest = (metaSnap.getLong("sedekahLongest") ?: 0L).toInt()
            val baseTotal = metaSnap.getDouble("sedekahTotal") ?: 0.0
            val baseDays = (metaSnap.getLong("sedekahDays") ?: 0L).toInt()
            val sedekahState = if (hMap.isNotEmpty()) {
                val calc = SedekahSubuhStreakEngine.calculateStreak(hMap)
                calc.copy(
                    currentStreak = maxOf(calc.currentStreak, baseStreak),
                    longestStreak = maxOf(calc.longestStreak, baseLongest),
                    totalContributions = maxOf(calc.totalContributions, baseTotal),
                    totalDaysGiven = maxOf(calc.totalDaysGiven, baseDays),
                    lastContributionDate = lastDate ?: calc.lastContributionDate
                )
            } else {
                SedekahSubuhState(
                    currentStreak = baseStreak,
                    longestStreak = baseLongest,
                    totalContributions = baseTotal,
                    totalDaysGiven = baseDays,
                    lastContributionDate = lastDate,
                    badges = SedekahSubuhState.getDefaultBadges()
                )
            }

            val walletMutationsJson = metaSnap.getString("walletMutationsJson")
            val restoredMutations = deserializeWalletMutations(walletMutationsJson)
            val ibadahDepositsJson = metaSnap.getString("ibadahDepositsJson")
            val restoredDeposits = deserializeIbadahDeposits(ibadahDepositsJson)
            val restoredPreferences = metaSnap.getString("preferencesJson")
                ?.takeIf { it.isNotBlank() }
                ?.let { raw ->
                    try {
                        BackupPreferencesSnapshot.fromJson(JSONObject(raw))
                    } catch (_: Exception) { null }
                }

            // 2. Wallets
            val restoredWallets = userDocRef.collection("wallets").get().await().documents.mapNotNull { doc ->
                try {
                    WalletAccount(
                        id = doc.getString("id") ?: doc.id,
                        name = doc.getString("name") ?: "Kantong",
                        type = try { WalletType.valueOf(doc.getString("type") ?: "CASH") } catch (_: Exception) { WalletType.CASH },
                        institutionName = doc.getString("institutionName") ?: "",
                        accountNumber = doc.getString("accountNumber") ?: "",
                        linkedAccountId = doc.getString("linkedAccountId") ?: "acc_cash",
                        colorHex = doc.getLong("colorHex") ?: 0xFF10B981,
                        isDefault = doc.getBoolean("isDefault") ?: false,
                        notes = doc.getString("notes") ?: ""
                    )
                } catch (_: Exception) { null }
            }

            // 3. Journal Entries
            val restoredEntries = userDocRef.collection("journal_entries").get().await().documents.mapNotNull { doc ->
                try {
                    val entity = com.example.core.database.JournalEntryEntity(
                        id = doc.getString("id") ?: doc.id,
                        dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                        hijriYear = (doc.getLong("hijriYear") ?: 1448L).toInt(),
                        hijriMonth = (doc.getLong("hijriMonth") ?: 3L).toInt(),
                        hijriDay = (doc.getLong("hijriDay") ?: 8L).toInt(),
                        description = doc.getString("description") ?: "",
                        transactionType = doc.getString("transactionType") ?: "EXPENSE",
                        linesJson = doc.getString("linesJson") ?: "[]",
                        receiptAttachmentJson = doc.getString("receiptJson")?.takeIf { it.isNotBlank() }
                    )
                    EntityMappers.toDomain(entity)
                } catch (_: Exception) { null }
            }

            // 4. Ibadah Goals
            val restoredGoals = userDocRef.collection("ibadah_goals").get().await().documents.mapNotNull { doc ->
                try {
                    IbadahGoal(
                        id = doc.getString("id") ?: doc.id,
                        type = try { IbadahGoalType.valueOf(doc.getString("type") ?: "QURBAN_KAMBING") } catch (_: Exception) { IbadahGoalType.QURBAN_KAMBING },
                        title = doc.getString("title") ?: "Target",
                        targetAmount = doc.getDouble("targetAmount") ?: 0.0,
                        currentAccumulated = doc.getDouble("currentAccumulated") ?: 0.0,
                        targetHijriYearMonth = doc.getString("targetHijri") ?: "",
                        targetMonthsRemaining = (doc.getLong("monthsRemaining") ?: 12L).toInt(),
                        linkedWalletId = doc.getString("linkedWalletId") ?: "acc_bank",
                        notes = doc.getString("notes") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false
                    )
                } catch (_: Exception) { null }
            }

            // 5. Qardh Records
            val restoredQardh = userDocRef.collection("qardh_records").get().await().documents.mapNotNull { doc ->
                try {
                    val entity = com.example.core.database.QardhRecordEntity(
                        id = doc.getString("id") ?: doc.id,
                        type = doc.getString("type") ?: "HUTANG_SAYA",
                        counterpartyName = doc.getString("counterpartyName") ?: "",
                        totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                        remainingAmount = doc.getDouble("remainingAmount") ?: 0.0,
                        agreementDateMillis = doc.getLong("agreementDateMillis") ?: System.currentTimeMillis(),
                        dueDateMillis = doc.getLong("dueDateMillis")?.takeIf { it > 0L },
                        witnessName = doc.getString("witnessName") ?: "",
                        notes = doc.getString("notes") ?: "",
                        status = doc.getString("status") ?: "AKTIF",
                        installmentsJson = doc.getString("installmentsJson") ?: "[]"
                    )
                    EntityMappers.toDomain(entity)
                } catch (_: Exception) { null }
            }

            // 6. Budgets
            val restoredBudgets = userDocRef.collection("budgets").get().await().documents.mapNotNull { doc ->
                try {
                    val accountId = doc.getString("accountId") ?: return@mapNotNull null
                    BudgetAllocation(
                        id = doc.getString("id") ?: doc.id,
                        accountId = accountId,
                        categoryName = doc.getString("categoryName") ?: "Pos Anggaran",
                        monthlyLimit = doc.getDouble("monthlyLimit") ?: 0.0,
                        iconKey = doc.getString("iconKey") ?: "shopping",
                        alertThresholdPercent = doc.getDouble("alertThresholdPercent") ?: 0.8
                    )
                } catch (_: Exception) { null }
            }

            // 7. Infaq Rules
            val restoredRules = userDocRef.collection("infaq_rules").get().await().documents.mapNotNull { doc ->
                try {
                    InfaqRule(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "Aturan Infaq",
                        targetCategory = try { AccountCategory.valueOf(doc.getString("targetCategory") ?: "EXPENSE") } catch (_: Exception) { AccountCategory.EXPENSE },
                        calculationType = try { InfaqCalculationType.valueOf(doc.getString("calculationType") ?: "PERCENTAGE") } catch (_: Exception) { InfaqCalculationType.PERCENTAGE },
                        rate = doc.getDouble("rate") ?: 0.05,
                        fixedAmount = doc.getDouble("fixedAmount") ?: 0.0,
                        roundUpStep = doc.getDouble("roundUpStep") ?: 5000.0,
                        enableFridayMultiplier = doc.getBoolean("enableFridayMultiplier") ?: true,
                        enableRamadanMultiplier = doc.getBoolean("enableRamadanMultiplier") ?: true
                    )
                } catch (_: Exception) { null }
            }

            // 8. Recurring Transactions
            val restoredRecurring = userDocRef.collection("recurring_transactions").get().await().documents.mapNotNull { doc ->
                try {
                    val lastExec = doc.getLong("lastExecutedDateMillis")?.takeIf { it > 0L }
                    RecurringTransaction(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "Transaksi Rutin",
                        type = try { RecurringType.valueOf(doc.getString("type") ?: "EXPENSE") } catch (_: Exception) { RecurringType.EXPENSE },
                        amount = doc.getDouble("amount") ?: 0.0,
                        categoryAccountId = doc.getString("categoryAccountId") ?: "acc_living",
                        assetAccountId = doc.getString("assetAccountId") ?: "acc_cash",
                        frequency = try { RecurringFrequency.valueOf(doc.getString("frequency") ?: "MONTHLY") } catch (_: Exception) { RecurringFrequency.MONTHLY },
                        dayOfMonthOrWeek = (doc.getLong("dayOfMonthOrWeek") ?: 1L).toInt(),
                        customInfaqRate = doc.getDouble("customInfaqRate") ?: 0.05,
                        enableRoundUp = doc.getBoolean("enableRoundUp") ?: true,
                        roundUpStep = doc.getDouble("roundUpStep") ?: 5000.0,
                        isActive = doc.getBoolean("isActive") ?: true,
                        autoExecute = doc.getBoolean("autoExecute") ?: true,
                        lastExecutedDate = lastExec?.let { Date(it) },
                        lastExecutedPeriodKey = doc.getString("lastExecutedPeriodKey")?.ifBlank { null },
                        lastExecutionTimestamp = doc.getLong("lastExecutionTimestamp") ?: 0L,
                        nextDueDate = Date(doc.getLong("nextDueDateMillis") ?: System.currentTimeMillis()),
                        note = doc.getString("note") ?: ""
                    )
                } catch (_: Exception) { null }
            }

            // 9. Infaq Distributions
            val restoredDistributions = userDocRef.collection("infaq_distributions").get().await().documents.mapNotNull { doc ->
                try {
                    InfaqDistributionRecord(
                        id = doc.getString("id") ?: doc.id,
                        amount = doc.getDouble("amount") ?: 0.0,
                        recipientName = doc.getString("recipientName") ?: "",
                        asnafCategory = try { AsnafCategory.valueOf(doc.getString("asnafCategory") ?: "UMUM") } catch (_: Exception) { AsnafCategory.UMUM },
                        distributionDate = Date(doc.getLong("distributionDateMillis") ?: System.currentTimeMillis()),
                        hijriDateString = doc.getString("hijriDateString") ?: "",
                        sourceAccountId = doc.getString("sourceAccountId") ?: "acc_bank",
                        programName = doc.getString("programName") ?: "Penyaluran Mandiri",
                        receiptNumber = doc.getString("receiptNumber") ?: "",
                        notes = doc.getString("notes") ?: "",
                        isVerified = doc.getBoolean("isVerified") ?: true
                    )
                } catch (_: Exception) { null }
            }

            // 10. Rezeki Non-Tunai
            val restoredRezeki = userDocRef.collection("rezeki_non_tunai").get().await().documents.mapNotNull { doc ->
                try {
                    val est = doc.getDouble("estimatedValue") ?: 0.0
                    val rate = doc.getDouble("suggestedInfaqRate") ?: 0.05
                    RezekiNonTunaiRecord(
                        id = doc.getString("id") ?: doc.id,
                        title = doc.getString("title") ?: "Rezeki",
                        giverName = doc.getString("giverName") ?: "",
                        category = try { RezekiCategory.valueOf(doc.getString("category") ?: "FOOD_BEVERAGE") } catch (_: Exception) { RezekiCategory.FOOD_BEVERAGE },
                        estimatedValue = est,
                        suggestedInfaqRate = rate,
                        suggestedInfaqAmount = doc.getDouble("suggestedInfaqAmount") ?: (est * rate),
                        isPaidInfaq = doc.getBoolean("isPaidInfaq") ?: false,
                        paidJournalEntryId = doc.getString("paidJournalEntryId")?.ifBlank { null },
                        dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                        hijriDateString = doc.getString("hijriDateString") ?: "",
                        notes = doc.getString("notes") ?: "",
                        createdAtMillis = doc.getLong("createdAtMillis") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) { null }
            }

            // 11. Persist atomically into local Room database
            database.withTransaction {
                if (replaceLocal) {
                    database.journalDao().clearAll()
                    database.walletDao().clearAll()
                    database.ibadahGoalDao().clearAll()
                    database.qardhDao().clearAll()
                    database.budgetDao().clearAll()
                    database.infaqRuleDao().clearAll()
                    database.recurringTransactionDao().clearAll()
                    database.infaqDistributionDao().clearAll()
                    database.rezekiNonTunaiDao().deleteAll()
                    database.sedekahSubuhDao().clearAll()
                }
                if (restoredEntries.isNotEmpty()) database.journalDao().insertAll(restoredEntries.map { EntityMappers.toEntity(it) })
                if (restoredWallets.isNotEmpty()) database.walletDao().insertAll(restoredWallets.map { EntityMappers.toEntity(it) })
                if (restoredGoals.isNotEmpty()) database.ibadahGoalDao().insertAll(restoredGoals.map { EntityMappers.toEntity(it) })
                if (restoredQardh.isNotEmpty()) database.qardhDao().insertAll(restoredQardh.map { EntityMappers.toEntity(it) })
                if (restoredBudgets.isNotEmpty()) database.budgetDao().insertAll(restoredBudgets.map { EntityMappers.toEntity(it) })
                if (restoredRules.isNotEmpty()) database.infaqRuleDao().insertAll(restoredRules.map { EntityMappers.toEntity(it) })
                if (restoredRecurring.isNotEmpty()) database.recurringTransactionDao().insertAll(restoredRecurring.map { EntityMappers.toEntity(it) })
                if (restoredDistributions.isNotEmpty()) database.infaqDistributionDao().insertAll(restoredDistributions.map { EntityMappers.toEntity(it) })
                if (restoredRezeki.isNotEmpty()) database.rezekiNonTunaiDao().insertAll(restoredRezeki.map { EntityMappers.toEntity(it) })

                database.sedekahSubuhDao().insertOrUpdate(
                    SedekahSubuhEntity(
                        id = 1,
                        currentStreak = sedekahState.currentStreak,
                        longestStreak = sedekahState.longestStreak,
                        totalContributions = sedekahState.totalContributions,
                        totalDaysGiven = sedekahState.totalDaysGiven,
                        lastContributionDateMillis = sedekahState.lastContributionDate?.time ?: 0L,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
                val restoredHistObj = JSONObject()
                for ((k, v) in sedekahState.historyMap) {
                    restoredHistObj.put(k, v)
                }
                database.settingsDao().insertOrUpdate(
                    SettingsEntity("sedekah_subuh_history_json", restoredHistObj.toString())
                )
                database.settingsDao().insertOrUpdate(
                    SettingsEntity("wallet_mutations_json", serializeWalletMutations(restoredMutations))
                )
                database.settingsDao().insertOrUpdate(
                    SettingsEntity("ibadah_deposits_json", serializeIbadahDeposits(restoredDeposits))
                )
                database.settingsDao().insertOrUpdate(
                    SettingsEntity("dummy_data_version", "v4_multi_wallet_isolated")
                )
            }

            val totalRestored = restoredEntries.size + restoredWallets.size + restoredGoals.size +
                restoredQardh.size + restoredBudgets.size + restoredRules.size +
                restoredRecurring.size + restoredDistributions.size + restoredRezeki.size

            val now = System.currentTimeMillis()
            _syncState.value = SyncState(
                status = SyncStateStatus.SUCCESS,
                lastSyncedTimeMillis = now,
                lastSyncedTimeString = timeFormat.format(Date(now)),
                itemsSyncedCount = totalRestored,
                errorMessage = null
            )

            Result.success(
                CloudRestorePayload(
                    entries = restoredEntries,
                    wallets = restoredWallets,
                    goals = restoredGoals,
                    qardh = restoredQardh,
                    budgets = restoredBudgets,
                    sedekahState = sedekahState,
                    rules = restoredRules,
                    recurring = restoredRecurring,
                    distributions = restoredDistributions,
                    rezekiNonTunai = restoredRezeki,
                    walletMutations = restoredMutations,
                    ibadahDeposits = restoredDeposits,
                    preferences = restoredPreferences
                )
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logFirestoreError("restoreFromCloud", userId, e)
            _syncState.value = _syncState.value.copy(
                status = SyncStateStatus.ERROR,
                errorMessage = "Gagal memulihkan dari Cloud: ${e.localizedMessage}"
            )
            Result.failure(e)
        }
    }
}

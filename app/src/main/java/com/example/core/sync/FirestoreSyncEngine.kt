package com.example.core.sync

import android.content.Context
import android.util.Log
import com.example.core.accounting.JournalEntry
import com.example.core.budget.BudgetAllocation
import com.example.core.database.AmanahDatabase
import com.example.core.database.EntityMappers
import com.example.core.ibadah.IbadahGoal
import com.example.core.infaq.SedekahSubuhState
import com.example.core.qardh.QardhRecord
import com.example.core.wallet.WalletAccount
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

class FirestoreSyncEngine(
    private val context: Context,
    private val database: AmanahDatabase
) {
    private val TAG = "FirestoreSyncEngine"
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            com.example.core.auth.FirebaseSafeInitializer.init(context)
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore initialization fallback: ${e.message}")
            null
        }
    }

    private val timeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))

    suspend fun syncAll(
        userId: String,
        localEntries: List<JournalEntry>,
        localWallets: List<WalletAccount>,
        localGoals: List<IbadahGoal>,
        localQardh: List<QardhRecord>,
        localBudgets: List<BudgetAllocation>,
        sedekahState: SedekahSubuhState,
        onRestoreFromCloud: (List<JournalEntry>, List<WalletAccount>, List<IbadahGoal>, List<QardhRecord>, List<BudgetAllocation>, SedekahSubuhState) -> Unit
    ): Result<Int> = withContext(Dispatchers.IO) {
        // 1. Always persist to Room local database cache first
        try {
            database.journalDao().insertAll(localEntries.map { EntityMappers.toEntity(it) })
            database.walletDao().insertAll(localWallets.map { EntityMappers.toEntity(it) })
            database.ibadahGoalDao().insertAll(localGoals.map { EntityMappers.toEntity(it) })
            database.qardhDao().insertAll(localQardh.map { EntityMappers.toEntity(it) })
            database.budgetDao().insertAll(localBudgets.map { EntityMappers.toEntity(it) })
        } catch (dbEx: Exception) {
            Log.w(TAG, "Local Room persistence note: ${dbEx.message}")
        }

        val totalLocal = localEntries.size + localWallets.size + localGoals.size + localQardh.size
        val now = System.currentTimeMillis()

        val fs = firestore
        if (fs == null || userId.isBlank() || userId == "guest_local_user" || userId.startsWith("guest_")) {
            _syncState.value = SyncState(
                status = SyncStateStatus.SUCCESS,
                lastSyncedTimeMillis = now,
                lastSyncedTimeString = timeFormat.format(Date(now)),
                itemsSyncedCount = totalLocal,
                errorMessage = null
            )
            return@withContext Result.success(totalLocal)
        }

        _syncState.value = _syncState.value.copy(
            status = SyncStateStatus.SYNCING,
            errorMessage = null
        )

        try {
            val userDocRef = fs.collection("amanah_users").document(userId)

            // 2. Push local changes to Firestore
            val batch = fs.batch()

            // Metadata & Sedekah
            val userMeta = hashMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis(),
                "sedekahStreak" to sedekahState.currentStreak,
                "sedekahLongest" to sedekahState.longestStreak,
                "sedekahTotal" to sedekahState.totalContributions,
                "sedekahDays" to sedekahState.totalDaysGiven
            )
            batch.set(userDocRef, userMeta, SetOptions.merge())

            // Wallets
            val walletsColl = userDocRef.collection("wallets")
            for (w in localWallets) {
                val data = hashMapOf<String, Any>(
                    "id" to w.id,
                    "name" to w.name,
                    "type" to w.type.name,
                    "institutionName" to w.institutionName,
                    "accountNumber" to w.accountNumber,
                    "linkedAccountId" to w.linkedAccountId,
                    "colorHex" to w.colorHex,
                    "isDefault" to w.isDefault,
                    "notes" to w.notes,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(walletsColl.document(w.id), data, SetOptions.merge())
            }

            // Journal Entries
            val entriesColl = userDocRef.collection("journal_entries")
            for (e in localEntries) {
                val entity = EntityMappers.toEntity(e)
                val data = hashMapOf<String, Any>(
                    "id" to entity.id,
                    "dateMillis" to entity.dateMillis,
                    "hijriYear" to entity.hijriYear,
                    "hijriMonth" to entity.hijriMonth,
                    "hijriDay" to entity.hijriDay,
                    "description" to entity.description,
                    "transactionType" to entity.transactionType,
                    "linesJson" to entity.linesJson,
                    "receiptJson" to (entity.receiptAttachmentJson ?: ""),
                    "updatedAt" to entity.updatedAtMillis
                )
                batch.set(entriesColl.document(e.id), data, SetOptions.merge())
            }

            // Ibadah Goals
            val goalsColl = userDocRef.collection("ibadah_goals")
            for (g in localGoals) {
                val data = hashMapOf<String, Any>(
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
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(goalsColl.document(g.id), data, SetOptions.merge())
            }

            // Qardh Records
            val qardhColl = userDocRef.collection("qardh_records")
            for (q in localQardh) {
                val entity = EntityMappers.toEntity(q)
                val data = hashMapOf<String, Any>(
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
                    "updatedAt" to entity.updatedAtMillis
                )
                batch.set(qardhColl.document(q.id), data, SetOptions.merge())
            }

            // Commit push batch
            batch.commit().await()

            // 2. Persist to Room local database cache
            database.journalDao().insertAll(localEntries.map { EntityMappers.toEntity(it) })
            database.walletDao().insertAll(localWallets.map { EntityMappers.toEntity(it) })
            database.ibadahGoalDao().insertAll(localGoals.map { EntityMappers.toEntity(it) })
            database.qardhDao().insertAll(localQardh.map { EntityMappers.toEntity(it) })
            database.budgetDao().insertAll(localBudgets.map { EntityMappers.toEntity(it) })

            val now = System.currentTimeMillis()
            val totalSynced = localEntries.size + localWallets.size + localGoals.size + localQardh.size

            _syncState.value = SyncState(
                status = SyncStateStatus.SUCCESS,
                lastSyncedTimeMillis = now,
                lastSyncedTimeString = timeFormat.format(Date(now)),
                itemsSyncedCount = totalSynced,
                errorMessage = null
            )

            Log.d(TAG, "Sync finished successfully: $totalSynced items pushed.")
            Result.success(totalSynced)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            _syncState.value = _syncState.value.copy(
                status = SyncStateStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Gagal menyinkronkan dengan Cloud Firestore"
            )
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(
        userId: String,
        onSuccess: (List<JournalEntry>, List<WalletAccount>, List<IbadahGoal>, List<QardhRecord>, List<BudgetAllocation>, SedekahSubuhState) -> Unit
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val fs = firestore
        if (fs == null) {
            return@withContext Result.failure(Exception("Layanan Cloud Firestore tidak aktif (menggunakan database Room lokal)"))
        }
        if (userId.isBlank() || userId.startsWith("guest_")) {
            return@withContext Result.failure(Exception("Silakan masuk dengan akun untuk memulihkan dari Cloud"))
        }

        try {
            val userDocRef = fs.collection("amanah_users").document(userId)

            // Fetch metadata
            val metaSnap = userDocRef.get().await()
            val sedekahState = if (metaSnap.exists()) {
                SedekahSubuhState(
                    currentStreak = (metaSnap.getLong("sedekahStreak") ?: 0L).toInt(),
                    longestStreak = (metaSnap.getLong("sedekahLongest") ?: 0L).toInt(),
                    totalContributions = metaSnap.getDouble("sedekahTotal") ?: 0.0,
                    totalDaysGiven = (metaSnap.getLong("sedekahDays") ?: 0L).toInt()
                )
            } else {
                SedekahSubuhState()
            }

            // Fetch wallets
            val walletsSnap = userDocRef.collection("wallets").get().await()
            val restoredWallets = walletsSnap.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val name = doc.getString("name") ?: "Kantong"
                    val typeStr = doc.getString("type") ?: "CASH"
                    val inst = doc.getString("institutionName") ?: ""
                    val accNo = doc.getString("accountNumber") ?: ""
                    val linked = doc.getString("linkedAccountId") ?: "acc_cash"
                    val color = doc.getLong("colorHex") ?: 0xFF10B981
                    val isDef = doc.getBoolean("isDefault") ?: false
                    val notes = doc.getString("notes") ?: ""

                    com.example.core.wallet.WalletAccount(
                        id = id,
                        name = name,
                        type = try { com.example.core.wallet.WalletType.valueOf(typeStr) } catch (_: Exception) { com.example.core.wallet.WalletType.CASH },
                        institutionName = inst,
                        accountNumber = accNo,
                        linkedAccountId = linked,
                        colorHex = color,
                        isDefault = isDef,
                        notes = notes
                    )
                } catch (_: Exception) { null }
            }

            // Fetch journal entries
            val entriesSnap = userDocRef.collection("journal_entries").get().await()
            val restoredEntries = entriesSnap.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis()
                    val hijriYear = (doc.getLong("hijriYear") ?: 1448L).toInt()
                    val hijriMonth = (doc.getLong("hijriMonth") ?: 3L).toInt()
                    val hijriDay = (doc.getLong("hijriDay") ?: 8L).toInt()
                    val desc = doc.getString("description") ?: ""
                    val type = doc.getString("transactionType") ?: "EXPENSE"
                    val linesJson = doc.getString("linesJson") ?: "[]"
                    val receiptJson = doc.getString("receiptJson")

                    val entity = com.example.core.database.JournalEntryEntity(
                        id = id,
                        dateMillis = dateMillis,
                        hijriYear = hijriYear,
                        hijriMonth = hijriMonth,
                        hijriDay = hijriDay,
                        description = desc,
                        transactionType = type,
                        linesJson = linesJson,
                        receiptAttachmentJson = if (receiptJson.isNullOrBlank()) null else receiptJson
                    )
                    EntityMappers.toDomain(entity)
                } catch (_: Exception) { null }
            }

            // Fetch goals
            val goalsSnap = userDocRef.collection("ibadah_goals").get().await()
            val restoredGoals = goalsSnap.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val typeStr = doc.getString("type") ?: "QURBAN_KAMBING"
                    val title = doc.getString("title") ?: "Target"
                    val targetAmt = doc.getDouble("targetAmount") ?: 0.0
                    val currAmt = doc.getDouble("currentAccumulated") ?: 0.0
                    val hijri = doc.getString("targetHijri") ?: ""
                    val months = (doc.getLong("monthsRemaining") ?: 12L).toInt()
                    val walletId = doc.getString("linkedWalletId") ?: "acc_bank"
                    val notes = doc.getString("notes") ?: ""
                    val isComp = doc.getBoolean("isCompleted") ?: false

                    IbadahGoal(
                        id = id,
                        type = try { com.example.core.ibadah.IbadahGoalType.valueOf(typeStr) } catch (_: Exception) { com.example.core.ibadah.IbadahGoalType.QURBAN_KAMBING },
                        title = title,
                        targetAmount = targetAmt,
                        currentAccumulated = currAmt,
                        targetHijriYearMonth = hijri,
                        targetMonthsRemaining = months,
                        linkedWalletId = walletId,
                        notes = notes,
                        isCompleted = isComp
                    )
                } catch (_: Exception) { null }
            }

            // Fetch Qardh
            val qardhSnap = userDocRef.collection("qardh_records").get().await()
            val restoredQardh = qardhSnap.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val typeStr = doc.getString("type") ?: "RECEIVABLE_PIUTANG"
                    val party = doc.getString("counterpartyName") ?: ""
                    val total = doc.getDouble("totalAmount") ?: 0.0
                    val rem = doc.getDouble("remainingAmount") ?: total
                    val agreeDate = doc.getLong("agreementDateMillis") ?: System.currentTimeMillis()
                    val dueDate = doc.getLong("dueDateMillis")?.let { if (it > 0) it else null }
                    val witness = doc.getString("witnessName") ?: ""
                    val notes = doc.getString("notes") ?: ""
                    val status = doc.getString("status") ?: "ACTIVE"
                    val instJson = doc.getString("installmentsJson") ?: "[]"

                    val entity = com.example.core.database.QardhRecordEntity(
                        id = id,
                        type = typeStr,
                        counterpartyName = party,
                        totalAmount = total,
                        remainingAmount = rem,
                        agreementDateMillis = agreeDate,
                        dueDateMillis = dueDate,
                        witnessName = witness,
                        notes = notes,
                        status = status,
                        installmentsJson = instJson
                    )
                    EntityMappers.toDomain(entity)
                } catch (_: Exception) { null }
            }

            // Save to Room
            if (restoredEntries.isNotEmpty()) database.journalDao().insertAll(restoredEntries.map { EntityMappers.toEntity(it) })
            if (restoredWallets.isNotEmpty()) database.walletDao().insertAll(restoredWallets.map { EntityMappers.toEntity(it) })
            if (restoredGoals.isNotEmpty()) database.ibadahGoalDao().insertAll(restoredGoals.map { EntityMappers.toEntity(it) })
            if (restoredQardh.isNotEmpty()) database.qardhDao().insertAll(restoredQardh.map { EntityMappers.toEntity(it) })

            withContext(Dispatchers.Main) {
                onSuccess(restoredEntries, restoredWallets, restoredGoals, restoredQardh, emptyList(), sedekahState)
            }

            val now = System.currentTimeMillis()
            _syncState.value = SyncState(
                status = SyncStateStatus.SUCCESS,
                lastSyncedTimeMillis = now,
                lastSyncedTimeString = timeFormat.format(Date(now)),
                itemsSyncedCount = restoredEntries.size + restoredWallets.size + restoredGoals.size,
                errorMessage = null
            )
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Restore from Firestore failed: ${e.message}", e)
            _syncState.value = _syncState.value.copy(
                status = SyncStateStatus.ERROR,
                errorMessage = "Gagal memulihkan dari Cloud: ${e.localizedMessage}"
            )
            Result.failure(e)
        }
    }
}

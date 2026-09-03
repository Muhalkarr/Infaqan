package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC")
    fun getAllEntriesFlow(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC")
    suspend fun getAllEntries(): List<JournalEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: JournalEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<JournalEntryEntity>)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM journal_entries")
    suspend fun clearAll()

    @Query("UPDATE journal_entries SET isSynced = :synced WHERE id = :id")
    suspend fun markSynced(id: String, synced: Boolean = true)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallets ORDER BY isDefault DESC, name ASC")
    fun getAllWalletsFlow(): Flow<List<WalletAccountEntity>>

    @Query("SELECT * FROM wallets ORDER BY isDefault DESC, name ASC")
    suspend fun getAllWallets(): List<WalletAccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(wallet: WalletAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wallets: List<WalletAccountEntity>)

    @Query("DELETE FROM wallets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM wallets")
    suspend fun clearAll()
}

@Dao
interface IbadahGoalDao {
    @Query("SELECT * FROM ibadah_goals ORDER BY isCompleted ASC, currentAccumulated DESC")
    fun getAllGoalsFlow(): Flow<List<IbadahGoalEntity>>

    @Query("SELECT * FROM ibadah_goals ORDER BY isCompleted ASC, currentAccumulated DESC")
    suspend fun getAllGoals(): List<IbadahGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: IbadahGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<IbadahGoalEntity>)

    @Query("DELETE FROM ibadah_goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM ibadah_goals")
    suspend fun clearAll()
}

@Dao
interface QardhDao {
    @Query("SELECT * FROM qardh_records ORDER BY agreementDateMillis DESC")
    fun getAllRecordsFlow(): Flow<List<QardhRecordEntity>>

    @Query("SELECT * FROM qardh_records ORDER BY agreementDateMillis DESC")
    suspend fun getAllRecords(): List<QardhRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: QardhRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<QardhRecordEntity>)

    @Query("DELETE FROM qardh_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM qardh_records")
    suspend fun clearAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_allocations ORDER BY categoryName ASC")
    fun getAllBudgetsFlow(): Flow<List<BudgetAllocationEntity>>

    @Query("SELECT * FROM budget_allocations ORDER BY categoryName ASC")
    suspend fun getAllBudgets(): List<BudgetAllocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(budget: BudgetAllocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetAllocationEntity>)

    @Query("DELETE FROM budget_allocations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM budget_allocations")
    suspend fun clearAll()
}

@Dao
interface SedekahSubuhDao {
    @Query("SELECT * FROM sedekah_subuh_state WHERE id = 1")
    fun getSedekahStateFlow(): Flow<SedekahSubuhEntity?>

    @Query("SELECT * FROM sedekah_subuh_state WHERE id = 1")
    suspend fun getSedekahState(): SedekahSubuhEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: SedekahSubuhEntity)

    @Query("DELETE FROM sedekah_subuh_state")
    suspend fun clearAll()
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings")
    fun getAllSettingsFlow(): Flow<List<SettingsEntity>>

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingByKey(key: String): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: SettingsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(settings: List<SettingsEntity>)

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun deleteByKey(key: String)

    @Query("DELETE FROM app_settings")
    suspend fun clearAll()
}

typealias TransactionDao = JournalEntryDao


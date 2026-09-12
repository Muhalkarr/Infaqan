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

    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC LIMIT :limit OFFSET :offset")
    fun getPagedEntriesFlow(limit: Int, offset: Int): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC")
    suspend fun getAllEntries(): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries ORDER BY dateMillis DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedEntries(limit: Int, offset: Int): List<JournalEntryEntity>

    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun getEntriesCount(): Int

    @Query("SELECT * FROM journal_entries WHERE dateMillis >= :startMillis ORDER BY dateMillis DESC")
    fun getRecentEntriesFlow(startMillis: Long): Flow<List<JournalEntryEntity>>

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

@Dao
interface RulingDao {
    @Query("SELECT * FROM custom_rulings ORDER BY updatedAtMillis DESC")
    fun getAllRulingsFlow(): Flow<List<CustomRulingEntity>>

    @Query("SELECT * FROM custom_rulings ORDER BY updatedAtMillis DESC")
    suspend fun getAllRulings(): List<CustomRulingEntity>

    @Query("SELECT * FROM custom_rulings WHERE isEnabled = 1 ORDER BY updatedAtMillis DESC")
    fun getActiveRulingsFlow(): Flow<List<CustomRulingEntity>>

    @Query("SELECT * FROM custom_rulings WHERE id = :id LIMIT 1")
    suspend fun getRulingById(id: String): CustomRulingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(ruling: CustomRulingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rulings: List<CustomRulingEntity>)

    @Query("DELETE FROM custom_rulings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM custom_rulings WHERE isCustom = 1")
    suspend fun clearCustomRulings()
}

@Dao
interface RecurringTransactionDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY nextDueDateMillis ASC")
    fun getAllFlow(): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions ORDER BY nextDueDateMillis ASC")
    suspend fun getAll(): List<RecurringTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(transaction: RecurringTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<RecurringTransactionEntity>)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM recurring_transactions")
    suspend fun clearAll()
}

@Dao
interface InfaqRuleDao {
    @Query("SELECT * FROM infaq_rules ORDER BY updatedAtMillis DESC")
    fun getAllFlow(): Flow<List<InfaqRuleEntity>>

    @Query("SELECT * FROM infaq_rules ORDER BY updatedAtMillis DESC")
    suspend fun getAll(): List<InfaqRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: InfaqRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<InfaqRuleEntity>)

    @Query("DELETE FROM infaq_rules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM infaq_rules")
    suspend fun clearAll()
}

@Dao
interface InfaqDistributionDao {
    @Query("SELECT * FROM infaq_distributions ORDER BY distributionDateMillis DESC")
    fun getAllFlow(): Flow<List<InfaqDistributionEntity>>

    @Query("SELECT * FROM infaq_distributions ORDER BY distributionDateMillis DESC")
    suspend fun getAll(): List<InfaqDistributionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(distribution: InfaqDistributionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(distributions: List<InfaqDistributionEntity>)

    @Query("DELETE FROM infaq_distributions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM infaq_distributions")
    suspend fun clearAll()
}

typealias TransactionDao = JournalEntryDao



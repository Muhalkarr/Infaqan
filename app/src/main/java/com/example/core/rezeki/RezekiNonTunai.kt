package com.example.core.rezeki

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class RezekiCategory(val displayName: String, val iconName: String) {
    FOOD_BEVERAGE("Makanan & Minuman", "Restaurant"),
    GIFT_GOODS("Barang & Hadiah", "CardGiftcard"),
    SERVICE_RIDE("Jasa & Tumpangan", "DirectionsCar"),
    VOUCHER_PERK("Voucher & Fasilitas", "ConfirmationNumber"),
    OTHER("Manfaat Lainnya", "VolunteerActivism")
}

data class RezekiNonTunaiRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val giverName: String = "",
    val category: RezekiCategory = RezekiCategory.FOOD_BEVERAGE,
    val estimatedValue: Double,
    val suggestedInfaqRate: Double = 0.05, // 5% default
    val suggestedInfaqAmount: Double = estimatedValue * suggestedInfaqRate,
    val isPaidInfaq: Boolean = false,
    val paidJournalEntryId: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val hijriDateString: String = "",
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "rezeki_non_tunai")
data class RezekiNonTunaiEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val giverName: String,
    val category: String,
    val estimatedValue: Double,
    val suggestedInfaqRate: Double,
    val suggestedInfaqAmount: Double,
    val isPaidInfaq: Boolean,
    val paidJournalEntryId: String?,
    val dateMillis: Long,
    val hijriDateString: String,
    val notes: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Dao
interface RezekiNonTunaiDao {
    @Query("SELECT * FROM rezeki_non_tunai ORDER BY dateMillis DESC, createdAtMillis DESC")
    fun getAllRezekiFlow(): Flow<List<RezekiNonTunaiEntity>>

    @Query("SELECT * FROM rezeki_non_tunai ORDER BY dateMillis DESC")
    suspend fun getAllRezekiList(): List<RezekiNonTunaiEntity>

    @Query("SELECT * FROM rezeki_non_tunai WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RezekiNonTunaiEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: RezekiNonTunaiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<RezekiNonTunaiEntity>)

    @Update
    suspend fun update(entity: RezekiNonTunaiEntity)

    @Query("UPDATE rezeki_non_tunai SET isPaidInfaq = 1, paidJournalEntryId = :journalEntryId, updatedAtMillis = :updatedAt WHERE id = :id")
    suspend fun markInfaqPaid(id: String, journalEntryId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM rezeki_non_tunai WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM rezeki_non_tunai")
    suspend fun deleteAll()
}

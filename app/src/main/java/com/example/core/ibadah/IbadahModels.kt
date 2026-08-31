package com.example.core.ibadah

import com.example.core.receipt.ReceiptAttachment
import java.util.Date
import java.util.UUID

enum class IbadahGoalType(val title: String, val categoryBadge: String, val defaultEstimate: Double) {
    QURBAN_KAMBING("Qurban 1 Ekor Kambing / Domba", "Idul Adha", 3500000.0),
    QURBAN_SAPI_PATUNGAN("Qurban 1/7 Bagian Sapi", "Idul Adha", 3800000.0),
    QURBAN_SAPI_1_EKOR("Qurban 1 Ekor Sapi Utuh", "Idul Adha", 26000000.0),
    SETORAN_AWAL_HAJI("Setoran Awal Porsi Haji (BPIH)", "Haji Mabrur", 25000000.0),
    PAKET_UMRAH_MUKMIN("Tabungan Paket Umrah Syariah", "Baitullah", 32000000.0),
    WAKAF_PRODUKTIF("Wakaf Masjid & Lembaga Dakwah", "Amal Jariyah", 5000000.0),
    CUSTOM_IBADAH("Rencana Ibadah Kustom", "Hajat Mukmin", 10000000.0);

    val displayName: String
        get() = title
}

data class IbadahGoal(
    val id: String = UUID.randomUUID().toString(),
    val type: IbadahGoalType = IbadahGoalType.QURBAN_KAMBING,
    val title: String = type.title,
    val targetAmount: Double = type.defaultEstimate,
    val currentAccumulated: Double = 0.0,
    val targetHijriYearMonth: String = "10 Dzulhijjah 1448 H",
    val targetMonthsRemaining: Int = 10,
    val linkedWalletId: String = "acc_bank",
    val notes: String = "",
    val isCompleted: Boolean = false
) {
    val progressRatio: Float
        get() = if (targetAmount <= 0.0) 0f else (currentAccumulated / targetAmount).toFloat().coerceIn(0f, 1f)

    val remainingAmount: Double
        get() = (targetAmount - currentAccumulated).coerceAtLeast(0.0)

    val recommendedMonthlySaving: Double
        get() = if (targetMonthsRemaining <= 0) remainingAmount else remainingAmount / targetMonthsRemaining.coerceAtLeast(1)
}

data class IbadahDepositRecord(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amount: Double,
    val sourceWalletId: String = "acc_bank",
    val fromWalletId: String = sourceWalletId,
    val depositDate: Date = Date(),
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val note: String = notes,
    val receipt: ReceiptAttachment? = null
)


package com.example.core.wallet

import com.example.core.receipt.ReceiptAttachment
import java.util.Date
import java.util.UUID

enum class WalletType(val displayName: String, val categoryTag: String) {
    CASH("Uang Tunai (Cash)", "Tunai"),
    BANK_SYARIAH("Bank Syariah", "Perbankan"),
    E_WALLET("Dompet Digital / QRIS", "Fintech"),
    GOLD_ASSET("Simpanan Logam Mulia / Emas", "Komoditas"),
    SPECIAL_SAVINGS("Tabungan Khusus / Titipan", "Amanah"),
    OTHER("Lainnya / Khusus", "Lainnya")
}

data class WalletAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: WalletType = WalletType.CASH,
    val institutionName: String = "Tunai",
    val accountNumber: String = "",
    val linkedAccountId: String = "acc_cash",
    val colorHex: Long = 0xFF10B981, // Emerald default
    val isDefault: Boolean = false,
    val notes: String = ""
)

data class WalletMutationRecord(
    val id: String = UUID.randomUUID().toString(),
    val fromWalletId: String,
    val toWalletId: String,
    val amount: Double,
    val adminFee: Double = 0.0,
    val note: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val date: Date = Date(),
    val transactionId: String = "",
    val linkedJournalEntryId: String = "",
    val receipt: ReceiptAttachment? = null
)


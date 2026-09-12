package com.example.core.qardh

import com.example.core.receipt.ReceiptAttachment
import java.util.Date
import java.util.UUID

enum class QardhType(val title: String, val badge: String, val description: String) {
    HUTANG_SAYA("Hutang Saya (Kewajiban)", "Hutang", "Saya menerima pinjaman tanpa riba dan wajib melunasinya"),
    PIUTANG_SAYA("Piutang Saya (Tagihan)", "Piutang", "Saya meminjamkan harta tanpa bunga kepada kerabat/rekan")
}

enum class QardhStatus(val displayName: String) {
    AKTIF("Belum Lunas (Aktif)"),
    SEBAGIAN_LUNAS("Sebagian Terbayar"),
    LUNAS("Lunas Sepenuhnya"),
    DIIKHLASKAN_SEDEKAH("Diikhlaskan Menjadi Sedekah")
}

data class QardhInstallment(
    val id: String = UUID.randomUUID().toString(),
    val qardhId: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val fromWalletId: String = "acc_cash",
    val note: String = "",
    val receipt: ReceiptAttachment? = null,
    val linkedJournalEntryId: String? = null
)

data class QardhRecord(
    val id: String = UUID.randomUUID().toString(),
    val type: QardhType = QardhType.HUTANG_SAYA,
    val counterpartyName: String,
    val contactInfo: String = "",
    val totalAmount: Double,
    val remainingAmount: Double = totalAmount,
    val startDateMillis: Long = System.currentTimeMillis(),
    val dueDateMillis: Long? = null,
    val notes: String = "",
    val witnessName: String = "", // Saksi pencatatan (QS. Al-Baqarah: 282)
    val witnessContact: String = "",
    val agreementTerms: String = "", // Akad kesepakatan
    val installments: List<QardhInstallment> = emptyList(),
    val status: QardhStatus = QardhStatus.AKTIF,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val walletId: String = "acc_cash",
    val linkedJournalEntryId: String? = null
)

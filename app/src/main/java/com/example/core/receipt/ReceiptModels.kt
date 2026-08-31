package com.example.core.receipt

import java.util.Date
import java.util.UUID

enum class ReceiptType(val displayName: String, val iconKey: String) {
    STORE_RECEIPT("Struk Belanja / Nota Toko", "receipt"),
    BANK_TRANSFER("Bukti Transfer Bank Syariah", "account_balance"),
    BANK_TRANSFER_PROOF("Bukti Transfer Bank Syariah", "account_balance"),
    OFFICIAL_INVOICE("Kuitansi / Faktur Resmi", "description"),
    DONATION_VOUCHER("Tanda Terima / Akad Infaq", "favorite"),
    DIGITAL_PAYMENT("Bukti E-Wallet / QRIS", "qr_code"),
    AMIL_ZAKAT_RECEIPT("Bukti Setor Amil Zakat", "volunteer_activism"),
    OTHER("Lampiran Dokumen Lain", "attachment")
}

data class ReceiptAttachment(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val receiptType: ReceiptType = ReceiptType.STORE_RECEIPT,
    val merchantName: String = "",
    val referenceNumber: String = "",
    val digitalVerificationHash: String = "",
    val notes: String = "",
    val amount: Double = 0.0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isDigitalVerified: Boolean = false
)


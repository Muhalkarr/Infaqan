package com.example.core.infaq

import java.util.Date

enum class AsnafCategory(val displayName: String, val badgeColorHex: Long) {
    YATIM_DHUAFA("Yatim & Dhuafa", 0xFFE5A93C),
    FAKIR_MISKIN("Fakir & Miskin", 0xFF0D9488),
    FISABILILLAH("Fisabilillah & Dakwah", 0xFF2E7D32),
    IBNU_SABIL("Ibnu Sabil (Musafir)", 0xFF0284C7),
    GHARIMIN("Gharimin (Bebas Hutang)", 0xFFD97706),
    AMIL("Lembaga Amil Zakat", 0xFF7C3AED),
    MUALLAF("Muallaf & Pembinaan", 0xFFEC4899),
    UMUM("Kemanusiaan Umum", 0xFF475569)
}

data class InfaqDistributionRecord(
    val id: String,
    val amount: Double,
    val recipientName: String,
    val asnafCategory: AsnafCategory,
    val distributionDate: Date,
    val hijriDateString: String,
    val sourceAccountId: String = "acc_bank",
    val programName: String = "Penyaluran Mandiri",
    val receiptNumber: String = "",
    val notes: String = "",
    val isVerified: Boolean = true
)

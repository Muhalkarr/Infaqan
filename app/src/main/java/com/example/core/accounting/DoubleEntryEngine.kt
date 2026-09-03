package com.example.core.accounting

import com.example.core.receipt.ReceiptAttachment
import java.util.Date
import kotlin.math.abs

enum class FiscalCycleType(val title: String, val description: String) {
    MONTHLY_SALARY_DATE("Siklus Tanggal Gajian", "Periode pembukuan dihitung dari tanggal gajian (misal tgl 25 s/d 24 bulan berikutnya)"),
    CALENDAR_MONTH("Bulan Kalender Masehi", "Periode pembukuan standar tanggal 1 hingga akhir bulan kalender"),
    HIJRI_MONTH("Bulan Kalender Hijriah", "Periode pembukuan mengikuti siklus 1 hingga 29/30 setiap bulan Hijriah")
}

enum class AccountCategory(val displayName: String, val codePrefix: String) {
    ASSET("Aset (Kas, Bank, Emas)", "100"),
    LIABILITY("Kewajiban & Vault Amanah", "200"),
    EQUITY("Modal / Harta Bersih", "300"),
    INCOME_KASAB("Pendapatan Kasab (Aktif)", "400"),
    INCOME_NON_KASAB("Pendapatan Non-Kasab (Pasif/Hadiah/Temuan)", "450"),
    EXPENSE("Pengeluaran (Biaya Hidup & Konsumsi)", "500")
}

data class Account(
    val id: String,
    val code: String,
    val name: String,
    val category: AccountCategory,
    val description: String = ""
)

data class JournalLine(
    val accountId: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0
)

data class JournalEntry(
    val id: String,
    val gregorianDate: Date,
    val hijriYear: Int,
    val hijriMonth: Int,
    val hijriDay: Int,
    val description: String,
    val transactionType: String, // INFLOW, EXPENSE, INFAQ_PAYOUT, TRANSFER
    val lines: List<JournalLine>,
    val receiptAttachment: ReceiptAttachment? = null
) {
    val totalDebit: Double = lines.sumOf { it.debit }
    val totalCredit: Double = lines.sumOf { it.credit }
    val isBalanced: Boolean = abs(totalDebit - totalCredit) < 0.01

    init {
        require(isBalanced) {
            "Jurnal tidak seimbang: Total Debit ($totalDebit) != Total Credit ($totalCredit)"
        }
    }
}


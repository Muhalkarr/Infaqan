package com.example.core.audit

import com.example.core.accounting.JournalEntry
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.state.AmanahLedgerUiState
import java.util.Calendar
import java.util.Date

enum class ShariahAuditStatus {
    COMPLIANT,
    PURIFIED,
    SUSPICIOUS_RIBA,
    EXCESSIVE_ISRAF,
    SYUBHAT
}

data class AuditedTransactionItem(
    val entryId: String,
    val date: Date,
    val description: String,
    val amount: Double,
    val status: ShariahAuditStatus,
    val reasonNote: String,
    val recommendedAction: String
)

data class ShariahAuditSummary(
    val totalTransactionsAudited: Int,
    val compliantCount: Int,
    val purifiedCount: Int = 0,
    val flaggedCount: Int,
    val complianceScorePercent: Int,
    val haulDaysRemaining: Int,
    val isHaulCompleted: Boolean,
    val items: List<AuditedTransactionItem>
)

object ShariahComplianceAuditEngine {

    private val ribaKeywords = listOf(
        "bunga", "interest", "denda", "late fee", "penalti", "paylater", "kartu kredit",
        "riba", "kredit konven", "keterlambatan", "admin pinjaman"
    )

    private val negationPrefixes = listOf(
        "tanpa ", "bebas ", "tidak ", "tidak ada ", "bukan ", "nol ", "non ", "non-", "anti "
    )

    private val israfKeywords = listOf(
        "foya", "clubbing", "judi", "taruhan", "slot", "konsumtif berlebih", "rokok", "vape"
    )

    internal fun matchesKeywordNegationAware(textLower: String, keywords: List<String>): Boolean {
        return keywords.any { keyword ->
            val regex = Regex("""\b${Regex.escape(keyword)}\b""")
            regex.findAll(textLower).any { match ->
                val prefixWindow = textLower.substring((match.range.first - 14).coerceAtLeast(0), match.range.first)
                val isNegated = negationPrefixes.any { neg -> prefixWindow.endsWith(neg) }
                !isNegated
            }
        }
    }

    fun auditAllEntries(state: AmanahLedgerUiState): ShariahAuditSummary {
        val entries = state.journalEntries
        val purifiedIds = state.purifiedAuditEntryIds
        val auditedItems = mutableListOf<AuditedTransactionItem>()

        for (entry in entries) {
            val descLower = entry.description.lowercase()
            val totalAmount = entry.lines.sumOf { it.debit }

            val isAlreadyPurified = entry.id in purifiedIds
            val isDisbursementEntry = entry.transactionType == "INFAQ_PAYOUT"
            val containsRiba = !isDisbursementEntry && matchesKeywordNegationAware(descLower, ribaKeywords)
            val containsIsraf = !isDisbursementEntry && matchesKeywordNegationAware(descLower, israfKeywords)

            when {
                isAlreadyPurified -> {
                    auditedItems.add(
                        AuditedTransactionItem(
                            entryId = entry.id,
                            date = entry.gregorianDate,
                            description = entry.description,
                            amount = totalAmount,
                            status = ShariahAuditStatus.PURIFIED,
                            reasonNote = "Mutasi telah ditindaklanjuti (disucikan via Tathhir atau diklarifikasi non-riba oleh pengguna).",
                            recommendedAction = "Selesai disucikan / diklarifikasi. Jejak audit tetap tersimpan aman."
                        )
                    )
                }
                containsRiba -> {
                    auditedItems.add(
                        AuditedTransactionItem(
                            entryId = entry.id,
                            date = entry.gregorianDate,
                            description = entry.description,
                            amount = totalAmount,
                            status = ShariahAuditStatus.SUSPICIOUS_RIBA,
                            reasonNote = "Terindikasi memuat kata kunci bunga/denda/riba. Periksa apakah memang mengandung unsur riba atau sekadar salah ketik.",
                            recommendedAction = "Pilih 'Sucikan (Tathhir)' untuk menyalurkan nilai denda/bunga ke fasilitas umum, 'Edit Catatan' jika salah ketik, atau 'Tandai Aman' bila bukan riba."
                        )
                    )
                }
                containsIsraf -> {
                    auditedItems.add(
                        AuditedTransactionItem(
                            entryId = entry.id,
                            date = entry.gregorianDate,
                            description = entry.description,
                            amount = totalAmount,
                            status = ShariahAuditStatus.EXCESSIVE_ISRAF,
                            reasonNote = "Terindikasi pengeluaran konsumtif yang berpotensi israf atau syubhat.",
                            recommendedAction = "Evaluasi pos belanja, edit keterangan bila perlu, atau lakukan penyucian/sedekah."
                        )
                    )
                }
                else -> {
                    auditedItems.add(
                        AuditedTransactionItem(
                            entryId = entry.id,
                            date = entry.gregorianDate,
                            description = entry.description,
                            amount = totalAmount,
                            status = ShariahAuditStatus.COMPLIANT,
                            reasonNote = "Akad dan pencatatan muamalah bersih dan sesuai kaidah syariah.",
                            recommendedAction = "Pertahankan pencatatan berpasangan yang amanah."
                        )
                    )
                }
            }
        }

        val total = auditedItems.size
        val purifiedCount = auditedItems.count { it.status == ShariahAuditStatus.PURIFIED }
        val flagged = auditedItems.count { it.status != ShariahAuditStatus.COMPLIANT && it.status != ShariahAuditStatus.PURIFIED }
        val compliant = total - flagged
        val score = if (total > 0) ((compliant.toFloat() / total) * 100).toInt() else 100

        // Haul Calculation: 354 days Hijriah cycle
        val now = System.currentTimeMillis()
        val haulStartDate = state.haulStartDate ?: now
        val elapsedDays = ((now - haulStartDate) / (1000 * 60 * 60 * 24)).toInt()
        val daysRemaining = (354 - elapsedDays).coerceAtLeast(0)
        val isCompleted = elapsedDays >= 354

        return ShariahAuditSummary(
            totalTransactionsAudited = total,
            compliantCount = compliant,
            purifiedCount = purifiedCount,
            flaggedCount = flagged,
            complianceScorePercent = score,
            haulDaysRemaining = daysRemaining,
            isHaulCompleted = isCompleted,
            items = auditedItems.sortedByDescending { it.date.time }
        )
    }
}

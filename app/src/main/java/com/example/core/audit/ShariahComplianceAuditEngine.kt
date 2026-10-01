package com.example.core.audit

import com.example.core.accounting.JournalEntry
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.state.AmanahLedgerUiState
import java.util.Calendar
import java.util.Date

enum class ShariahAuditStatus {
    COMPLIANT,
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

    private val israfKeywords = listOf(
        "foya", "clubbing", "judi", "taruhan", "slot", "konsumtif berlebih", "rokok", "vape"
    )

    fun auditAllEntries(state: AmanahLedgerUiState): ShariahAuditSummary {
        val entries = state.journalEntries
        val auditedItems = mutableListOf<AuditedTransactionItem>()

        for (entry in entries) {
            val descLower = entry.description.lowercase()
            val totalAmount = entry.lines.sumOf { it.debit }

            val containsRiba = ribaKeywords.any { descLower.contains(it) }
            val containsIsraf = israfKeywords.any { descLower.contains(it) }

            when {
                containsRiba -> {
                    auditedItems.add(
                        AuditedTransactionItem(
                            entryId = entry.id,
                            date = entry.gregorianDate,
                            description = entry.description,
                            amount = totalAmount,
                            status = ShariahAuditStatus.SUSPICIOUS_RIBA,
                            reasonNote = "Terindikasi memuat unsur bunga tambahan / denda keterlambatan (riba nasi'ah).",
                            recommendedAction = "Salurkan nilai bunga/denda ini ke rekening kas syubhat atau infaq fasilitas umum."
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
                            recommendedAction = "Evaluasi pos belanja dan perbanyak istighfar serta sedekah penyuci harta."
                        )
                    )
                }
                else -> {
                    // Fully compliant
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
        val flagged = auditedItems.count { it.status != ShariahAuditStatus.COMPLIANT }
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
            flaggedCount = flagged,
            complianceScorePercent = score,
            haulDaysRemaining = daysRemaining,
            isHaulCompleted = isCompleted,
            items = auditedItems.sortedByDescending { it.date.time }
        )
    }
}

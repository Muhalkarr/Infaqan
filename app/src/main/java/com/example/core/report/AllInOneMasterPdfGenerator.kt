package com.example.core.report

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.core.audit.ShariahComplianceAuditEngine
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.isTactical
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Generator PDF Laporan Utama Lengkap (All-in-One) yang menghimpun seluruh data
 * keuangan, akuntansi double-entry, kasab/non-kasab, analisa SLI, 4 jenis zakat,
 * ibadah/sedekah subuh, rezeki non-tunai, Qardhul Hasan, otomatisasi, dan audit syariah
 * dalam satu dokumen A4 terpadu multi-halaman.
 */
data class AllInOneReportCoverage(
    val walletCount: Int,
    val walletMutationCount: Int,
    val kasabCategoryCount: Int,
    val nonKasabCategoryCount: Int,
    val expenseCategoryCount: Int,
    val budgetCount: Int,
    val totalIncomeKasab: Double,
    val totalIncomeNonKasab: Double,
    val sliConsumptionPercent: Double,
    val infaqToTotalIncomePercent: Double,
    val zakatMaalAmount: Double,
    val zakatProfesiAmount: Double,
    val zakatPerniagaanAmount: Double,
    val zakatFitrahAmount: Double,
    val sedekahSubuhStreak: Int,
    val sedekahSubuhHistoryDays: Int,
    val rezekiNonTunaiCount: Int,
    val asnafDistributionCount: Int,
    val ibadahGoalCount: Int,
    val ibadahDepositCount: Int,
    val qardhCount: Int,
    val infaqRuleCount: Int,
    val recurringCount: Int,
    val journalEntryCount: Int,
    val detailedJournalLineCount: Int,
    val receiptAttachmentCount: Int,
    val complianceScorePercent: Int
)

object AllInOneMasterPdfGenerator {

    fun buildCoverageSnapshot(state: AmanahLedgerUiState): AllInOneReportCoverage {
        val auditSummary = ShariahComplianceAuditEngine.auditAllEntries(state)
        val zm = state.zakatMaal
        val effectiveMaal = if (zm.zakatAmount > 0.0) zm.zakatAmount else state.zakatObligationLunar
        val sli = state.infaqSliAnalysis
        return AllInOneReportCoverage(
            walletCount = state.wallets.size,
            walletMutationCount = state.walletMutations.size,
            kasabCategoryCount = state.getKasabBreakdown().size,
            nonKasabCategoryCount = state.getNonKasabBreakdown().size,
            expenseCategoryCount = state.getExpenseBreakdown().size,
            budgetCount = state.budgets.size,
            totalIncomeKasab = state.totalIncomeKasab,
            totalIncomeNonKasab = state.totalIncomeNonKasab,
            sliConsumptionPercent = sli.sliConsumptionBasedPercent,
            infaqToTotalIncomePercent = sli.infaqToTotalIncomePercent,
            zakatMaalAmount = effectiveMaal,
            zakatProfesiAmount = state.zakatProfesi.zakatAmount,
            zakatPerniagaanAmount = state.zakatPerniagaan.zakatAmount,
            zakatFitrahAmount = state.zakatFitrah.totalFitrahRupiah,
            sedekahSubuhStreak = state.sedekahSubuhState.currentStreak,
            sedekahSubuhHistoryDays = state.sedekahSubuhState.historyMap.count { it.value > 0.0 },
            rezekiNonTunaiCount = state.rezekiNonTunaiList.size,
            asnafDistributionCount = state.infaqDistributions.size,
            ibadahGoalCount = state.ibadahGoals.size,
            ibadahDepositCount = state.ibadahDeposits.size,
            qardhCount = state.qardhRecords.size,
            infaqRuleCount = state.rules.size,
            recurringCount = state.recurringTransactions.size,
            journalEntryCount = state.journalEntries.size,
            detailedJournalLineCount = state.journalEntries.sumOf { it.lines.size },
            receiptAttachmentCount = state.journalEntries.count { it.receiptAttachment != null },
            complianceScorePercent = auditSummary.complianceScorePercent
        )
    }

    fun generateAllInOneMasterPdf(
        context: Context,
        state: AmanahLedgerUiState,
        targetMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
        targetYear: Int = Calendar.getInstance().get(Calendar.YEAR),
        editorialConfig: PdfEditorialConfig = state.pdfEditorialConfig,
        dateRange: PdfDateRange = PdfDateRange()
    ): File {
        val scoped = PdfPeriodFilterHelper.compute(state, dateRange)
        val document = PdfDocument()
        val isTactical = state.themeMode.isTactical
        val useTacticalText = editorialConfig.isTacticalTextEnabled(isTactical)
        val docTitle = editorialConfig.resolveDocumentTitle("Laporan Keuangan Utama Lengkap (All-in-One)")
        val session = FinancialPdfGenerator.PdfPageSession(
            document = document,
            documentTitle = docTitle,
            subtitle = editorialConfig.resolveHeaderSubtitle(
                isTactical,
                "Ringkasan Saldo, Arus Kas, Analisa Infaq, Zakat, Ibadah & Daftar Transaksi"
            ),
            isTactical = isTactical,
            editorialConfig = editorialConfig
        )

        val paint = session.paint

        val emeraldDark = session.emeraldDark
        val emeraldPrimary = session.emeraldPrimary
        val goldAccent = session.goldAccent
        val coralRed = Color.rgb(198, 40, 40)
        val blueInfo = if (useTacticalText) Color.rgb(0, 180, 216) else Color.rgb(21, 101, 192)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = session.textMuted
        val bgLight = Color.rgb(245, 248, 248)
        val bgCard = Color.rgb(235, 242, 241)

        val cal = Calendar.getInstance()
        val printDateStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(cal.time)
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time)
        val sdfShort = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

        // 1. HEADER BANNER UTAMA
        paint.color = emeraldDark
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 82f), 8f, 8f, paint)
        paint.color = goldAccent
        session.canvas.drawRect(RectF(32f, session.y + 78f, 563f, session.y + 82f), paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14.5f
        val defaultHeader = if (useTacticalText) {
            "MASTER DOSSIER // ALL-IN-ONE FINANCIAL LEDGER"
        } else {
            docTitle.uppercase(Locale("id", "ID"))
        }
        val headerTitle = editorialConfig.headerBrandTitle.trim().ifBlank { defaultHeader }
        session.canvas.drawText(headerTitle.take(48), 46f, session.y + 24f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(220, 245, 242)
        val headerSub = editorialConfig.resolveHeaderSubtitle(
            isTactical,
            "Ringkasan Lengkap: Saldo Kas, Pemasukan/Pengeluaran, Infaq, Zakat, Catatan Ibadah & Buku Besar"
        )
        session.canvas.drawText(headerSub.take(86), 46f, session.y + 40f, paint)

        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.2f
        val periodSuffix = if (dateRange.isCustomEnabled) {
            dateRange.formatPdfHeaderPeriod()
        } else {
            "Siklus: ${state.fiscalCycleType.title}"
        }
        val hijriStr = if (editorialConfig.includeHijriDate) "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} H" else ""
        val ownerLine = editorialConfig.resolveOwnerLine(
            stateUserName = state.userNameKasMukmin,
            isAppTactical = isTactical,
            hijriDateText = hijriStr,
            extraSuffix = periodSuffix
        )
        session.canvas.drawText(ownerLine.take(86), 46f, session.y + 60f, paint)

        paint.textSize = 7.5f
        paint.color = Color.rgb(200, 230, 225)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        session.canvas.drawText("Dicetak: $printDateStr WIB", 420f, session.y + 72f, paint)

        session.y += 96f

        // 2. RINGKASAN EKSEKUTIF (4 KPI UTAMA)
        val totalWalletBalance = state.totalWalletBalance
        val receivables = state.qardhRecords.filter { it.type == QardhType.PIUTANG_SAYA }
        val payables = state.qardhRecords.filter { it.type == QardhType.HUTANG_SAYA }
        val totalPiutang = receivables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }.sumOf { it.remainingAmount }
        val totalHutang = payables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }.sumOf { it.remainingAmount }
        val netWorth = totalWalletBalance + totalPiutang - totalHutang
        val totalAllIncome = scoped.totalIncomeKasab + scoped.totalIncomeNonKasab
        val auditSummary = scoped.auditSummary

        if (editorialConfig.includeExecutiveKpis) {
            val kpiLabels = editorialConfig.resolveKpiLabels()
            val cardW = 126f
            val cardH = 52f
            val gap = 8.6f
            FinancialPdfGenerator.drawSummaryCard(
                session.canvas, paint, 32f, session.y, cardW, cardH, bgCard,
                kpiLabels[0].take(22), "Rp ${FinancialPdfGenerator.formatRp(netWorth)}",
                if (netWorth >= 0) emeraldDark else coralRed
            )
            FinancialPdfGenerator.drawSummaryCard(
                session.canvas, paint, 32f + cardW + gap, session.y, cardW, cardH, bgCard,
                kpiLabels[1].take(22), "Rp ${FinancialPdfGenerator.formatRp(totalWalletBalance)}", emeraldPrimary
            )
            FinancialPdfGenerator.drawSummaryCard(
                session.canvas, paint, 32f + (cardW + gap) * 2, session.y, cardW, cardH, bgCard,
                kpiLabels[2].take(22), "Rp ${FinancialPdfGenerator.formatRp(totalAllIncome)}", emeraldDark
            )
            FinancialPdfGenerator.drawSummaryCard(
                session.canvas, paint, 32f + (cardW + gap) * 3, session.y, cardW, cardH, bgCard,
                kpiLabels[3].take(22),
                "${auditSummary.complianceScorePercent}% | ${String.format(Locale("id", "ID"), "%.1f%%", scoped.spiritualLiquidityIndex)}",
                goldAccent
            )
            session.y += 66f
        }

        var secNo = 1

        // 3. BAB: NERACA SALDO DOMPET, REKENING & MUTASI INTERNAL
        if (editorialConfig.isSectionIncluded(PdfSectionKey.WALLETS)) {
            PdfSectionWriters.drawSectionTitle(
                session,
                editorialConfig.resolveSectionTitle(PdfSectionKey.WALLETS, isTactical, "${secNo++}."),
                emeraldDark
            )

        fun drawWalletHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Nama Kantong / Rekening", 40f, topY + 12f, paint)
            c.drawText("Tipe Akun", 220f, topY + 12f, paint)
            c.drawText("Lembaga / No. Rek", 335f, topY + 12f, paint)
            c.drawText("Saldo Saat Ini (Rp)", 468f, topY + 12f, paint)
            return topY + 22f
        }
        session.y = drawWalletHeader(session.canvas, session.y)

        if (state.wallets.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada kantong kas yang terdaftar.", 40f, session.y + 12f, paint)
            session.y += 22f
        } else {
            for ((idx, w) in state.wallets.withIndex()) {
                session.ensureSpace(17f) { c, topY -> drawWalletHeader(c, topY) }
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)

                val bal = state.getWalletBalance(w.id)
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = emeraldDark
                session.canvas.drawText(w.name.take(34), 40f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textDark
                session.canvas.drawText(w.type.displayName, 220f, session.y + 11f, paint)
                val acctMeta = "${w.institutionName} ${w.accountNumber}".trim().ifBlank { "-" }
                session.canvas.drawText(acctMeta.take(22), 335f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (bal >= 0) emeraldPrimary else coralRed
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(bal)}", 468f, session.y + 11f, paint)
                session.y += 16f
            }
            session.ensureSpace(18f)
            paint.color = textMuted
            paint.textSize = 7.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText(
                "Total Mutasi Transfer Internal Antar-Dompet: ${scoped.filteredMutations.size} kali (Rp ${FinancialPdfGenerator.formatRp(scoped.totalInternalTransfer)})",
                40f, session.y + 12f, paint
            )
            session.y += 22f
            }
        }

        // 4. BAB: RINCIAN KASAB VS NON-KASAB, PENGELUARAN KATEGORI & ANGGARAN
        if (editorialConfig.isSectionIncluded(PdfSectionKey.CASHFLOW_KASAB)) {
            PdfSectionWriters.drawCashflowKasabAndExpenseSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.CASHFLOW_KASAB, isTactical, "${secNo++}."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                coralRed = coralRed,
                goldAccent = goldAccent,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight,
                editorialConfig = editorialConfig,
                scopedData = scoped
            )
        }

        // Sub-tabel Kepatuhan Anggaran Bulanan
        if (editorialConfig.isSectionIncluded(PdfSectionKey.BUDGETS)) {
            PdfSectionWriters.drawSectionTitle(
                session,
                editorialConfig.resolveSectionTitle(PdfSectionKey.BUDGETS, isTactical, "${secNo++}."),
                emeraldDark
            )
        fun drawBudgetHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Pos Kategori Anggaran", 40f, topY + 12f, paint)
            c.drawText("Batas Limit", 220f, topY + 12f, paint)
            c.drawText("Realisasi", 315f, topY + 12f, paint)
            c.drawText("Sisa Kuota", 415f, topY + 12f, paint)
            c.drawText("Status", 500f, topY + 12f, paint)
            return topY + 22f
        }
        session.y = drawBudgetHeader(session.canvas, session.y)

        if (state.budgets.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada alokasi batas anggaran bulanan yang ditetapkan.", 40f, session.y + 12f, paint)
            session.y += 22f
        } else {
            for ((idx, b) in state.budgets.withIndex()) {
                val spent = if (dateRange.isCustomEnabled) {
                    scoped.budgetSpentByAccount[b.accountId] ?: 0.0
                } else {
                    state.getMonthlySpentForAccount(b.accountId, targetMonth, targetYear)
                }
                val rem = b.monthlyLimit - spent
                val ratio = if (b.monthlyLimit > 0) (spent / b.monthlyLimit) * 100.0 else 0.0
                val isOver = spent > b.monthlyLimit

                session.ensureSpace(17f) { c, topY -> drawBudgetHeader(c, topY) }
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)

                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textDark
                session.canvas.drawText(b.categoryName.take(32), 40f, session.y + 11f, paint)
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(b.monthlyLimit)}", 220f, session.y + 11f, paint)
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(spent)}", 315f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (isOver) coralRed else emeraldDark
                val remStr = if (isOver) "-Rp ${FinancialPdfGenerator.formatRp(-rem)}" else "Rp ${FinancialPdfGenerator.formatRp(rem)}"
                session.canvas.drawText(remStr, 415f, session.y + 11f, paint)
                session.canvas.drawText(
                    "${String.format(Locale("id", "ID"), "%.0f%%", ratio)} ${if (isOver) "OVER" else "Aman"}",
                    500f, session.y + 11f, paint
                )
                session.y += 16f
            }
                session.y += 12f
            }
        }

        // 5. BAB: ANALISA SLI KOMPREHENSIF & AUDIT ATURAN INFAQ
        if (editorialConfig.isSectionIncluded(PdfSectionKey.SLI_ANALYSIS)) {
            PdfSectionWriters.drawComprehensiveSliAnalysisSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.SLI_ANALYSIS, isTactical, "${secNo++}."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                goldAccent = goldAccent,
                coralRed = coralRed,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight,
                bgCard = bgCard,
                scopedData = scoped
            )
        }

        // 6. BAB: REKAPITULASI LENGKAP ZAKAT MAAL & FITRAH & KARANTINA SYUBHAT
        if (editorialConfig.isSectionIncluded(PdfSectionKey.ZAKAT_MAAL_FITRAH)) {
            PdfSectionWriters.drawSectionTitle(
                session,
                editorialConfig.resolveSectionTitle(PdfSectionKey.ZAKAT_MAAL_FITRAH, isTactical, "${secNo++}."),
                emeraldDark
            )
        val zm = state.zakatMaal
        val zf = state.zakatFitrah
        val syubhatBalance = state.getAccountBalance("acc_syubhat")
        val haulStartStr = if (zm.haulStartDateMillis > 0L) sdfShort.format(Date(zm.haulStartDateMillis)) else "Manual"
        val effectiveMaal = if (zm.zakatAmount > 0.0) zm.zakatAmount else state.zakatObligationLunar

        val zakatMaalFitrahBoxH = 112f
        session.ensureSpace(zakatMaalFitrahBoxH + 10f)
        paint.color = bgLight
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + zakatMaalFitrahBoxH), 6f, 6f, paint)

        var zy = session.y + 16f
        paint.textSize = 8.2f
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        session.canvas.drawText(
            "• Zakat Maal: Nisab 85g = Rp ${FinancialPdfGenerator.formatRp(state.nisabThreshold)} | Aset Bersih Kena Zakat = Rp ${FinancialPdfGenerator.formatRp(zm.netTaxableAssets.let { if (it > 0) it else state.totalAssets })}",
            44f, zy, paint
        )
        zy += 13f
        session.canvas.drawText(
            "• Pelacakan Haul (Mulai $haulStartStr): ${zm.elapsedHaulDays()}/${zm.haulTargetDays} hari (${if (zm.isHaulFulfilled) "Haul Genap" else "Sisa ${zm.remainingHaulDays()} hari"})",
            44f, zy, paint
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = emeraldPrimary
        session.canvas.drawText("Zakat Maal: Rp ${FinancialPdfGenerator.formatRp(effectiveMaal)}", 415f, zy, paint)
        zy += 15f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = textDark
        val fitrahRateStr = if (zf.useLocalStaplePrice) "Rp ${FinancialPdfGenerator.formatRp(zf.ricePricePerKg)}/${zf.measurementUnit}" else "Tarif BAZNAS"
        session.canvas.drawText(
            "• Zakat Fitrah (${zf.activeDependentsCount} Jiwa x ${zf.riceAmountPerPersonKg} ${zf.measurementUnit} ${zf.stapleFoodName.take(20)} @ $fitrahRateStr)",
            44f, zy, paint
        )
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = emeraldDark
        session.canvas.drawText("Zakat Fitrah: Rp ${FinancialPdfGenerator.formatRp(zf.totalFitrahRupiah)}", 415f, zy, paint)
        zy += 15f

        val includedDeps = zf.dependents.filter { it.isIncluded }.joinToString(", ") { it.name }.ifBlank { "${zf.activeDependentsCount} Jiwa" }
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = textMuted
        session.canvas.drawText("• Tanggungan Fitrah (${zf.regionName}): ${includedDeps.take(72)}", 44f, zy, paint)
        zy += 15f

        paint.color = textDark
        session.canvas.drawText(
            "• Gabungan Zakat (Maal + Fitrah): Rp ${FinancialPdfGenerator.formatRp(state.totalZakatReportPayable)} | Telah Disalurkan: Rp ${FinancialPdfGenerator.formatRp(state.totalZakatDisbursedRecorded)}",
            44f, zy, paint
        )
        zy += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = if (syubhatBalance > 0) coralRed else emeraldPrimary
        session.canvas.drawText(
            "• Saldo Karantina Dana Syubhat / Non-Halal (Wajib Tathhir 100%): Rp ${FinancialPdfGenerator.formatRp(syubhatBalance)}",
            44f, zy, paint
        )
        session.y += zakatMaalFitrahBoxH + 14f
        }

        // Zakat Profesi & Zakat Perniagaan
        if (editorialConfig.isSectionIncluded(PdfSectionKey.ZAKAT_PROFESI_PERNIAGAAN)) {
            PdfSectionWriters.drawZakatProfesiAndPerniagaanSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.ZAKAT_PROFESI_PERNIAGAAN, isTactical, "${secNo++}."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                goldAccent = goldAccent,
                coralRed = coralRed,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight
            )
        }

        // 7. BAB: SEDEKAH SUBUH, REZEKI NON-TUNAI, 8 ASNAF & TARGET IBADAH
        if (editorialConfig.isSectionIncluded(PdfSectionKey.SEDEKAH_SUBUH)) {
            PdfSectionWriters.drawSedekahSubuhSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.SEDEKAH_SUBUH, isTactical, "${secNo++}."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                goldAccent = goldAccent,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight,
                scopedData = scoped
            )
        }

        // Rezeki Non-Tunai
        if (editorialConfig.isSectionIncluded(PdfSectionKey.REZEKI_NON_TUNAI)) {
            PdfSectionWriters.drawSectionTitle(
                session,
                editorialConfig.resolveSectionTitle(PdfSectionKey.REZEKI_NON_TUNAI, isTactical, "${secNo++}."),
                emeraldDark
            )
        fun drawRezekiHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Tanggal", 40f, topY + 12f, paint)
            c.drawText("Keterangan Rezeki & Pemberi", 105f, topY + 12f, paint)
            c.drawText("Taksiran Nilai", 335f, topY + 12f, paint)
            c.drawText("Saran Infaq", 425f, topY + 12f, paint)
            c.drawText("Status", 500f, topY + 12f, paint)
            return topY + 22f
        }
        session.y = drawRezekiHeader(session.canvas, session.y)

        if (scoped.filteredRezeki.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada catatan Rezeki Non-Tunai pada periode ini.", 40f, session.y + 12f, paint)
            session.y += 22f
        } else {
            for ((idx, rz) in scoped.filteredRezeki.withIndex()) {
                session.ensureSpace(17f) { c, topY -> drawRezekiHeader(c, topY) }
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)

                paint.textSize = 7.8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textDark
                session.canvas.drawText(sdfShort.format(Date(rz.dateMillis)), 40f, session.y + 11f, paint)
                val titleGiver = if (rz.giverName.isNotBlank()) "${rz.title} (${rz.giverName})" else rz.title
                session.canvas.drawText(titleGiver.take(38), 105f, session.y + 11f, paint)
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(rz.estimatedValue)}", 335f, session.y + 11f, paint)
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(rz.suggestedInfaqAmount)}", 425f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (rz.isPaidInfaq) emeraldPrimary else goldAccent
                val statusStr = when {
                    rz.isVaultAllocated -> "Ke Vault"
                    rz.isPaidInfaq -> "Ditunaikan"
                    else -> "Tercatat"
                }
                session.canvas.drawText(statusStr, 500f, session.y + 11f, paint)
                session.y += 16f
            }
            session.y += 12f
            }
        }

        // Penyaluran 8 Asnaf
        if (editorialConfig.isSectionIncluded(PdfSectionKey.ASNAF_DISTRIBUTION)) {
            PdfSectionWriters.drawSectionTitle(
                session,
                editorialConfig.resolveSectionTitle(PdfSectionKey.ASNAF_DISTRIBUTION, isTactical, "${secNo++}."),
                emeraldDark
            )
        fun drawDistHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Tanggal", 40f, topY + 12f, paint)
            c.drawText("Penerima / Lembaga", 110f, topY + 12f, paint)
            c.drawText("Asnaf Mustahiq", 260f, topY + 12f, paint)
            c.drawText("Program", 370f, topY + 12f, paint)
            c.drawText("Nominal (Rp)", 480f, topY + 12f, paint)
            return topY + 22f
        }
        session.y = drawDistHeader(session.canvas, session.y)

        if (scoped.filteredDistributions.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada riwayat penyaluran 8 Asnaf pada periode ini.", 40f, session.y + 12f, paint)
            session.y += 22f
        } else {
            for ((idx, dist) in scoped.filteredDistributions.withIndex()) {
                session.ensureSpace(17f) { c, topY -> drawDistHeader(c, topY) }
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)

                paint.textSize = 7.8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textDark
                session.canvas.drawText(sdfShort.format(dist.distributionDate), 40f, session.y + 11f, paint)
                session.canvas.drawText(dist.recipientName.take(26), 110f, session.y + 11f, paint)
                session.canvas.drawText(dist.asnafCategory.displayName.take(18), 260f, session.y + 11f, paint)
                session.canvas.drawText(dist.programName.ifBlank { dist.notes.ifBlank { "-" } }.take(18), 370f, session.y + 11f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = emeraldDark
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(dist.amount)}", 480f, session.y + 11f, paint)
                session.y += 16f
            }
            session.y += 12f
            }
        }

        // Target Perencanaan Ibadah & Setoran
        if (editorialConfig.isSectionIncluded(PdfSectionKey.IBADAH_GOALS)) {
            PdfSectionWriters.drawSectionTitle(
                session,
                editorialConfig.resolveSectionTitle(PdfSectionKey.IBADAH_GOALS, isTactical, "${secNo++}.") +
                    " (${state.ibadahGoals.size} Target, ${scoped.filteredIbadahDeposits.size} Setoran)",
                emeraldDark
            )
        if (state.ibadahGoals.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada target perencanaan ibadah/qurban yang dibuat.", 40f, session.y + 12f, paint)
            session.y += 22f
        } else {
            for ((idx, g) in state.ibadahGoals.withIndex()) {
                session.ensureSpace(17f)
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textDark
                val pct = (g.progressRatio * 100).toInt()
                session.canvas.drawText("• ${g.title.take(32)} (${g.type.title.take(16)})", 40f, session.y + 11f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (g.isCompleted) emeraldPrimary else emeraldDark
                session.canvas.drawText(
                    "Rp ${FinancialPdfGenerator.formatRp(g.currentAccumulated)} / Rp ${FinancialPdfGenerator.formatRp(g.targetAmount)} ($pct%)",
                    330f, session.y + 11f, paint
                )
                session.y += 16f
            }
            session.y += 12f
            }
        }

        // 8. BAB: BUKU HUTANG & PIUTANG SYARIAH (QARDHUL HASAN)
        if (editorialConfig.isSectionIncluded(PdfSectionKey.QARDH_HASAN)) {
            PdfSectionWriters.drawSectionTitle(
                session,
                editorialConfig.resolveSectionTitle(PdfSectionKey.QARDH_HASAN, isTactical, "${secNo++}."),
                emeraldDark
            )
        if (state.qardhRecords.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Tidak ada catatan akad Qardhul Hasan (hutang/piutang) saat ini.", 40f, session.y + 12f, paint)
            session.y += 22f
        } else {
            for ((idx, q) in state.qardhRecords.withIndex()) {
                session.ensureSpace(17f)
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)
                val due = q.dueDateMillis?.let { sdfShort.format(Date(it)) } ?: "Tanpa Batas"
                val kindLabel = if (q.type == QardhType.PIUTANG_SAYA) "[PIUTANG]" else "[HUTANG]"
                paint.textSize = 7.8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (q.type == QardhType.PIUTANG_SAYA) blueInfo else coralRed
                session.canvas.drawText(kindLabel, 40f, session.y + 11f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textDark
                session.canvas.drawText("${q.counterpartyName.take(22)} ($due)", 100f, session.y + 11f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                session.canvas.drawText(
                    "Sisa Rp ${FinancialPdfGenerator.formatRp(q.remainingAmount)} / Rp ${FinancialPdfGenerator.formatRp(q.totalAmount)} [${q.status.displayName}]",
                    315f, session.y + 11f, paint
                )
                session.y += 16f
            }
            session.y += 12f
            }
        }

        // 9. BAB: TATA KELOLA OTOMATISASI & RINGKASAN AUDIT SYARIAH
        if (editorialConfig.isSectionIncluded(PdfSectionKey.RULES_RECURRING_AUDIT)) {
            PdfSectionWriters.drawRulesAndRecurringSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.RULES_RECURRING_AUDIT, isTactical, "${secNo++}."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight
            )

            session.ensureSpace(52f)
            paint.color = bgCard
            session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 42f), 6f, 6f, paint)
            paint.color = emeraldDark
            paint.textSize = 7.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            session.canvas.drawText(
                "Audit Kepatuhan Syariah: Skor ${auditSummary.complianceScorePercent}% (${auditSummary.compliantCount}/${auditSummary.totalTransactionsAudited} transaksi patuh, ${auditSummary.flaggedCount} catatan)",
                42f, session.y + 17f, paint
            )
            val receiptCount = scoped.filteredEntries.count { it.receiptAttachment != null }
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = textMuted
            session.canvas.drawText(
                "Total Lampiran Bukti/Struk: $receiptCount entri | Direktori Lembaga Amil Terdaftar: ${state.amilInstitutions.size} lembaga | Mata Uang: ${state.primaryCurrencyCode}",
                42f, session.y + 32f, paint
            )
            session.y += 56f
        }

        // 10. BAB: BUKU BESAR JURNAL BERPASANGAN LENGKAP (DENGAN DETAIL AKUN DEBIT/KREDIT)
        if (editorialConfig.isSectionIncluded(PdfSectionKey.JOURNAL_LEDGER)) {
            PdfSectionWriters.drawJournalEntriesWithLinesTable(
                session = session,
                state = state,
                entries = scoped.filteredEntries,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.JOURNAL_LEDGER, isTactical, "${secNo++}.") +
                    " (${scoped.filteredEntries.size} Entri)",
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                coralRed = coralRed,
                blueInfo = blueInfo,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight,
                editorialConfig = editorialConfig
            )
        }

        PdfSectionWriters.drawClosingNoteSection(session, editorialConfig, emeraldDark, textDark, bgLight)

        session.finish()
        val basePrefix = editorialConfig.resolveFilePrefix("Laporan_Utama_AllInOne")
        val finalPrefix = if (dateRange.isCustomEnabled) "${basePrefix}_${dateRange.formatShortFileTag()}" else basePrefix
        return FinancialPdfGenerator.savePdfToFile(context, document, finalPrefix)
    }
}

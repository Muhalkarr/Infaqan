package com.example.core.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.state.AmanahLedgerUiState
import com.example.core.accounting.JournalEntry
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhType
import com.example.core.qardh.QardhStatus
import com.example.core.wallet.WalletAccount
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.ui.theme.isTactical

object FinancialPdfGenerator {

    /**
     * Memecah teks panjang menjadi beberapa baris yang pas di dalam batas lebar (maxWidth).
     * Mendukung pemecahan kata panjang secara karakter jika kata melebihi batas kolom
     * sehingga teks tidak pernah terpotong atau tumpang tindih ke kolom sebelah.
     */
    fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        val paragraphs = text.split("\n")
        val resultLines = mutableListOf<String>()

        for (paragraph in paragraphs) {
            val words = paragraph.split(" ").filter { it.isNotEmpty() }
            if (words.isEmpty()) {
                resultLines.add("")
                continue
            }
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) {
                        resultLines.add(currentLine)
                        currentLine = ""
                    }
                    if (paint.measureText(word) <= maxWidth) {
                        currentLine = word
                    } else {
                        // Kata tunggal lebih panjang dari batas kolom, pecah karakter per karakter
                        var subWord = ""
                        for (ch in word) {
                            if (paint.measureText(subWord + ch) <= maxWidth) {
                                subWord += ch
                            } else {
                                if (subWord.isNotEmpty()) {
                                    resultLines.add(subWord)
                                }
                                subWord = ch.toString()
                            }
                        }
                        currentLine = subWord
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                resultLines.add(currentLine)
            }
        }
        return if (resultLines.isEmpty()) listOf(text) else resultLines
    }

    fun formatRp(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    /**
     * Pengelola halaman PDF interaktif (PdfPageSession) untuk mendukung multi-halaman dinamis.
     * Mencegah pemotongan teks atau konten karena kehabisan ruang vertikal (out of bounds).
     */
    class PdfPageSession(
        val document: PdfDocument,
        val documentTitle: String,
        val subtitle: String,
        val isTactical: Boolean = false,
        val editorialConfig: PdfEditorialConfig = PdfEditorialConfig(),
        val emeraldDark: Int = if (isTactical && editorialConfig.isTacticalTextEnabled(isTactical)) Color.rgb(10, 24, 20) else Color.rgb(0, 77, 64),
        val emeraldPrimary: Int = if (isTactical && editorialConfig.isTacticalTextEnabled(isTactical)) Color.rgb(0, 200, 115) else Color.rgb(0, 137, 123),
        val goldAccent: Int = if (isTactical && editorialConfig.isTacticalTextEnabled(isTactical)) Color.rgb(245, 175, 25) else Color.rgb(218, 165, 32),
        val textMuted: Int = if (isTactical && editorialConfig.isTacticalTextEnabled(isTactical)) Color.rgb(75, 95, 90) else Color.rgb(90, 105, 110)
    ) {
        val pageInfo: PdfDocument.PageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standar A4
        var currentPage: PdfDocument.Page = document.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var pageNumber = 1
        var y = 36f
        val maxY = 770f // Batas bawah aman sebelum mencapai footer (792f)
        val paint = Paint().apply { isAntiAlias = true }

        /**
         * Memeriksa apakah sisa ruang halaman mencukupi tinggi yang dibutuhkan.
         * Jika tidak cukup, mencetak footer, menyelesaikan halaman, membuka halaman baru,
         * mencetak header lanjutan, dan memanggil callback table header jika ada.
         */
        fun ensureSpace(neededHeight: Float, onNewPageHeader: ((Canvas, Float) -> Float)? = null) {
            if (y + neededHeight > maxY) {
                drawPageFooter()
                document.finishPage(currentPage)
                pageNumber++
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 36f
                drawContinuationHeader()
                if (onNewPageHeader != null) {
                    y = onNewPageHeader(canvas, y)
                }
            }
        }

        private fun drawContinuationHeader() {
            paint.color = emeraldDark
            canvas.drawRoundRect(RectF(32f, y, 563f, y + 26f), 4f, 4f, paint)
            paint.color = goldAccent
            canvas.drawRect(RectF(32f, y + 24f, 563f, y + 26f), paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val headerTitle = editorialConfig.resolveContinuationHeader(documentTitle, isTactical).take(68)
            canvas.drawText(headerTitle, 42f, y + 17f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8f
            canvas.drawText("Halaman $pageNumber", 500f, y + 17f, paint)

            y += 36f
        }

        fun drawPageFooter() {
            paint.color = if (isTactical) emeraldPrimary else emeraldDark
            canvas.drawLine(32f, 792f, 563f, 792f, paint)

            paint.color = textMuted
            paint.textSize = 7f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            val footerText = editorialConfig.resolveFooterText(isTactical).take(95)
            canvas.drawText(
                footerText,
                32f,
                806f,
                paint
            )
            canvas.drawText("Hal. $pageNumber", 528f, 806f, paint)
        }

        fun finish() {
            drawPageFooter()
            document.finishPage(currentPage)
        }
    }

    /**
     * Menghasilkan file PDF Laporan Keuangan Bulanan, Alokasi Anggaran & Distribusi Tabarru'
     * Mendukung multi-halaman dinamis sehingga tidak ada anggaran atau transaksi yang terpotong.
     */
    fun generateMonthlyReportPdf(
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
        val docTitle = editorialConfig.resolveDocumentTitle("Laporan Keuangan Bulanan")
        val session = PdfPageSession(
            document = document,
            documentTitle = docTitle,
            subtitle = editorialConfig.resolveHeaderSubtitle(isTactical, "Realisasi Anggaran, Pemasukan & Catatan Transaksi"),
            isTactical = isTactical,
            editorialConfig = editorialConfig
        )

        val canvas = session.canvas
        val paint = session.paint

        val emeraldDark = session.emeraldDark
        val emeraldPrimary = session.emeraldPrimary
        val goldAccent = session.goldAccent
        val coralRed = Color.rgb(211, 47, 47)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = session.textMuted
        val bgLight = Color.rgb(245, 248, 248)
        val bgCard = Color.rgb(235, 242, 241)

        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, targetMonth)
            set(Calendar.YEAR, targetYear)
        }
        val monthName = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
        val periodSubLabel = if (dateRange.isCustomEnabled) dateRange.formatPrimaryPeriodLabel() else monthName
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time, manualOffsetDays = state.selectedHijriOffset)
        val printDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())

        // 1. HEADER BANNER HALAMAN PERTAMA
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 75f), 8f, 8f, paint)

        // Accent gold stripe
        paint.color = goldAccent
        canvas.drawRect(RectF(32f, session.y + 71f, 563f, session.y + 75f), paint)

        // Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        val defaultBrand = if (useTacticalText) "PROTOKOL TAKTIS // AMANAH v4.2" else docTitle.uppercase(Locale("id", "ID"))
        val brandTitle = editorialConfig.headerBrandTitle.trim().ifBlank { defaultBrand }
        canvas.drawText(brandTitle.take(44), 46f, session.y + 26f, paint)

        // Subtitle
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.rgb(220, 245, 242)
        val defaultSub = if (useTacticalText) "DOSIR KEUANGAN TAKTIS SYARIAH • SIKLUS BUKU BESAR $periodSubLabel" else "Ringkasan Keuangan, Anggaran & Distribusi Tabarru'"
        val brandSubtitle = editorialConfig.resolveHeaderSubtitle(isTactical, defaultSub)
        canvas.drawText(brandSubtitle.take(72), 46f, session.y + 42f, paint)

        // Owner & Periode badge
        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        val hijriText = if (editorialConfig.includeHijriDate && !dateRange.isCustomEnabled) "${hijriDate.monthName} ${hijriDate.year} H" else ""
        val ownerAndPeriod = editorialConfig.resolveOwnerLine(
            stateUserName = state.userNameKasMukmin,
            isAppTactical = isTactical,
            hijriDateText = hijriText,
            extraSuffix = if (dateRange.isCustomEnabled) dateRange.formatPdfHeaderPeriod() else "Periode: $monthName"
        )
        canvas.drawText(ownerAndPeriod.take(86), 46f, session.y + 60f, paint)

        // Timestamp
        paint.textSize = 8f
        paint.color = Color.rgb(200, 230, 225)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dicetak: $printDateStr WIB", 435f, session.y + 25f, paint)

        session.y += 88f

        // 2. EXECUTIVE SUMMARY (METRIC CARDS)
        if (editorialConfig.includeExecutiveKpis) {
            val cardWidth = 126f
            val cardHeight = 52f
            val cardSpacing = 8.6f

            drawSummaryCard(session.canvas, paint, 32f, session.y, cardWidth, cardHeight, bgCard, "TOTAL ASET BERSIH", "Rp ${formatRp(state.totalAssets)}", emeraldDark)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing), session.y, cardWidth, cardHeight, bgCard, editorialConfig.resolveExpenseLabel().uppercase(Locale("id", "ID")).take(20), "Rp ${formatRp(scoped.totalConsumptionExpense)}", coralRed)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 2, session.y, cardWidth, cardHeight, bgCard, "SALDO DANA INFAQ", "Rp ${formatRp(state.virtualInfaqVaultBalance)}", emeraldPrimary)
            val sliScore = String.format(Locale("id", "ID"), "%.1f%%", scoped.spiritualLiquidityIndex)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 3, session.y, cardWidth, cardHeight, bgCard, "RASIO INFAQ (SLI)", sliScore, goldAccent)

            session.y += 66f
        }

        var secNo = 1
        // 3. SECTION: STATUS ALOKASI ANGGARAN BULANAN
        if (editorialConfig.isSectionIncluded(PdfSectionKey.BUDGETS)) {
            paint.color = emeraldDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            session.canvas.drawText(
                editorialConfig.resolveSectionTitle(PdfSectionKey.BUDGETS, isTactical, "${secNo++}."),
                32f, session.y, paint
            )
            session.y += 14f

        fun drawBudgetTableHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Pos Kategori Anggaran", 40f, topY + 12f, paint)
            c.drawText("Batas Limit", 215f, topY + 12f, paint)
            c.drawText("Realisasi Belanja", 310f, topY + 12f, paint)
            c.drawText("Sisa Kuota", 415f, topY + 12f, paint)
            c.drawText("Status / %", 495f, topY + 12f, paint)
            return topY + 22f
        }

        session.y = drawBudgetTableHeader(session.canvas, session.y)

        if (state.budgets.isEmpty()) {
            paint.color = textMuted
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 8.5f
            session.canvas.drawText("Belum ada alokasi batas anggaran yang ditetapkan.", 40f, session.y + 12f, paint)
            session.y += 24f
        } else {
            var rowIndex = 0
            for (b in state.budgets) {
                val spent = if (dateRange.isCustomEnabled) {
                    scoped.budgetSpentByAccount[b.accountId] ?: 0.0
                } else {
                    state.getMonthlySpentForAccount(b.accountId, targetMonth, targetYear)
                }
                val remaining = b.monthlyLimit - spent
                val ratio = if (b.monthlyLimit > 0) (spent / b.monthlyLimit) * 100.0 else 0.0
                val isOver = spent > b.monthlyLimit

                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val lines = wrapText(b.categoryName, paint, 165f)
                val rowHeight = 16f + (lines.size - 1) * 11f + 6f

                // Cek ketersediaan ruang, pecah halaman jika perlu
                session.ensureSpace(rowHeight) { c, topY -> drawBudgetTableHeader(c, topY) }

                session.paint.color = if (rowIndex % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + rowHeight), session.paint)

                session.paint.color = textDark
                for (i in lines.indices) {
                    session.canvas.drawText(lines[i], 40f, session.y + 12f + (i * 11f), session.paint)
                }

                session.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                session.paint.color = textDark
                session.canvas.drawText("Rp ${formatRp(b.monthlyLimit)}", 215f, session.y + 12f, session.paint)
                session.canvas.drawText("Rp ${formatRp(spent)}", 310f, session.y + 12f, session.paint)

                session.paint.color = if (isOver) coralRed else emeraldDark
                session.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val remText = if (isOver) "-Rp ${formatRp(-remaining)}" else "Rp ${formatRp(remaining)}"
                session.canvas.drawText(remText, 415f, session.y + 12f, session.paint)

                val statusText = if (isOver) "OVER (${String.format(Locale("id", "ID"), "%.0f", ratio)}%)" else "${String.format(Locale("id", "ID"), "%.0f", ratio)}% (Aman)"
                session.canvas.drawText(statusText, 495f, session.y + 12f, session.paint)

                session.y += rowHeight
                rowIndex++
            }
            session.y += 10f
            }
        }

        // 4. SECTION: RINCIAN KASAB VS NON-KASAB & PENGELUARAN PER KATEGORI
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

        // 5. SECTION: ANALISA SLI KOMPREHENSIF & AUDIT ATURAN INFAQ
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

        // 6. SECTION: REKAPITULASI INFAQ, SEDEKAH & PEMBERSIHAN TATHHIR
        if (editorialConfig.isSectionIncluded(PdfSectionKey.ASNAF_DISTRIBUTION)) {
            session.ensureSpace(80f)
            paint.color = emeraldDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            session.canvas.drawText("${secNo++}. Rekapitulasi Infaq, Pembersihan Dana & Penyaluran", 32f, session.y, paint)
            session.y += 14f

            paint.color = bgLight
            session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 48f), 6f, 6f, paint)

            val syubhatBalance = state.getAccountBalance("acc_syubhat")
            val statBoxW = 126f

            drawMiniStat(session.canvas, paint, 44f, session.y, "TOTAL INFAQ TERKUMPUL", "Rp ${formatRp(scoped.totalPurifiedInfaq)}", emeraldDark)
            drawMiniStat(session.canvas, paint, 44f + statBoxW + 6f, session.y, "REALISASI PENYALURAN", "Rp ${formatRp(scoped.totalDisbursedInfaq)}", emeraldPrimary)
            drawMiniStat(session.canvas, paint, 44f + (statBoxW + 6f) * 2, session.y, "SISA DANA INFAQ", "Rp ${formatRp(state.virtualInfaqVaultBalance)}", if (state.virtualInfaqVaultBalance > 0) coralRed else emeraldDark)
            drawMiniStat(session.canvas, paint, 44f + (statBoxW + 6f) * 3, session.y, "DANA NON-HALAL/SYUBHAT", "Rp ${formatRp(syubhatBalance)}", if (syubhatBalance > 0) goldAccent else emeraldDark)

            session.y += 62f
        }

        // 7. SECTION: JURNAL TRANSAKSI BUKU BESAR BULANAN DENGAN RINCIAN AKUN DEBIT/KREDIT
        if (editorialConfig.isSectionIncluded(PdfSectionKey.JOURNAL_LEDGER)) {
            val targetEntries = if (dateRange.isCustomEnabled) {
                scoped.filteredEntries
            } else {
                state.journalEntries.filter {
                    val c = Calendar.getInstance().apply { time = it.gregorianDate }
                    c.get(Calendar.MONTH) == targetMonth && c.get(Calendar.YEAR) == targetYear
                }.let { if (it.isNotEmpty()) it else state.journalEntries }
            }

            PdfSectionWriters.drawJournalEntriesWithLinesTable(
                session = session,
                state = state,
                entries = targetEntries,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.JOURNAL_LEDGER, isTactical, "${secNo++}.") + " (${targetEntries.size} Entri)",
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                coralRed = coralRed,
                blueInfo = Color.rgb(30, 136, 229),
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight,
                editorialConfig = editorialConfig
            )
        }

        PdfSectionWriters.drawClosingNoteSection(session, editorialConfig, emeraldDark, textDark, bgLight)

        session.finish()
        val defaultBase = if (dateRange.isCustomEnabled) {
            "Laporan_Periode_${dateRange.formatShortFileTag()}"
        } else {
            "Laporan_Bulanan_${targetYear}_${targetMonth + 1}"
        }
        val basePrefix = editorialConfig.resolveFilePrefix(defaultBase)
        val finalPrefix = if (dateRange.isCustomEnabled && editorialConfig.customFileNamePrefix.isNotBlank()) {
            "${basePrefix}_${dateRange.formatShortFileTag()}"
        } else basePrefix
        return savePdfToFile(context, document, finalPrefix)
    }

    /**
     * Menghasilkan Laporan Komprehensif Zakat, Infaq, Tathhir & ZISWAF
     * Termasuk: Zakat Maal (Nisab 85g Emas & Haul), Zakat Fitrah keluarga, Zakat Perniagaan,
     * Pembersihan Harta Syubhat (Fatwa DSN-MUI 123/2018), Riwayat Penyaluran 8 Asnaf, dan Target Ibadah.
     * Dilengkapi multi-halaman dinamis sehingga seluruh tabel tercetak utuh tanpa teks terpotong.
     */
    fun generateZakatAndInfaqReportPdf(
        context: Context,
        state: AmanahLedgerUiState,
        editorialConfig: PdfEditorialConfig = state.pdfEditorialConfig,
        dateRange: PdfDateRange = PdfDateRange()
    ): File {
        val scoped = PdfPeriodFilterHelper.compute(state, dateRange)
        val document = PdfDocument()
        val isTactical = state.themeMode.isTactical
        val useTacticalText = editorialConfig.isTacticalTextEnabled(isTactical)
        val docTitle = editorialConfig.resolveDocumentTitle("Laporan Zakat, Infaq & Sedekah")
        val session = PdfPageSession(
            document = document,
            documentTitle = docTitle,
            subtitle = editorialConfig.resolveHeaderSubtitle(isTactical, "Kalkulasi Nisab 85g Emas, Haul & Penyaluran 8 Asnaf"),
            isTactical = isTactical,
            editorialConfig = editorialConfig
        )

        val canvas = session.canvas
        val paint = session.paint

        val emeraldDark = session.emeraldDark
        val emeraldPrimary = session.emeraldPrimary
        val goldAccent = session.goldAccent
        val coralRed = Color.rgb(211, 47, 47)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = session.textMuted
        val bgLight = Color.rgb(245, 248, 248)
        val bgCard = Color.rgb(235, 242, 241)

        val cal = Calendar.getInstance()
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time, manualOffsetDays = state.selectedHijriOffset)
        val printDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())

        // 1. HEADER BANNER
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 75f), 8f, 8f, paint)
        paint.color = goldAccent
        canvas.drawRect(RectF(32f, session.y + 71f, 563f, session.y + 75f), paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        val defaultBrand = if (useTacticalText) "REKAPITULASI TAKTIS ZAKAT & INFAQ [OP//ZKT]" else docTitle.uppercase(Locale("id", "ID"))
        val brandTitle = editorialConfig.headerBrandTitle.trim().ifBlank { defaultBrand }
        canvas.drawText(brandTitle.take(46), 46f, session.y + 26f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.rgb(220, 245, 242)
        val defaultSub = if (useTacticalText) "DOSIR KEUANGAN SYARIAH • RADAR NISAB 85g EMAS & ASNAF AUDIT" else "Nisab 85g Emas, Haul, Pembersihan Non-Halal & Penyaluran 8 Asnaf"
        val brandSubtitle = editorialConfig.resolveHeaderSubtitle(isTactical, defaultSub)
        canvas.drawText(brandSubtitle.take(72), 46f, session.y + 42f, paint)

        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        val hijriText = if (editorialConfig.includeHijriDate && !dateRange.isCustomEnabled) "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} H" else ""
        val ownerLine = editorialConfig.resolveOwnerLine(
            stateUserName = state.userNameKasMukmin,
            isAppTactical = isTactical,
            hijriDateText = hijriText,
            extraSuffix = if (dateRange.isCustomEnabled) dateRange.formatPdfHeaderPeriod() else "Cetak: $printDateStr"
        )
        canvas.drawText(ownerLine.take(86), 46f, session.y + 60f, paint)

        session.y += 88f

        // 2. EXECUTIVE METRIC CARDS
        val cardWidth = 126f
        val cardHeight = 52f
        val cardSpacing = 8.6f

        val zakatPayable = state.zakatObligationLunar
        val nisabStatusText = if (state.isNisabReached) "MENCAPAI NISAB" else "BELUM NISAB"
        val nisabColor = if (state.isNisabReached) goldAccent else textMuted
        val syubhatBalance = state.getAccountBalance("acc_syubhat")

        drawSummaryCard(session.canvas, paint, 32f, session.y, cardWidth, cardHeight, bgCard, "TOTAL HARTA ZAKAT", "Rp ${formatRp(state.totalAssets)}", emeraldDark)
        drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing), session.y, cardWidth, cardHeight, bgCard, "STATUS NISAB (85G)", nisabStatusText, nisabColor)
        drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 2, session.y, cardWidth, cardHeight, bgCard, "ESTIMASI ZAKAT MAAL", "Rp ${formatRp(zakatPayable)}", emeraldPrimary)
        drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 3, session.y, cardWidth, cardHeight, bgCard, "TOTAL INFAQ DISALURKAN", "Rp ${formatRp(scoped.totalDisbursedInfaq)}", emeraldDark)

        session.y += 66f

        // 3. SECTION 1: RINCIAN HISAB ZAKAT MAAL (QS. AT-TAUBAH: 103)
        session.ensureSpace(160f)
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        session.canvas.drawText("1. Rincian Hisab Zakat Maal & Pelacakan Haul Syariah", 32f, session.y, paint)
        session.y += 14f

        val zm = state.zakatMaal
        val sdfDateOnly = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val haulStartStr = if (zm.haulStartDateMillis > 0L) sdfDateOnly.format(Date(zm.haulStartDateMillis)) else "Belum ditetapkan (Manual)"
        val haulProgressStr = "${zm.elapsedHaulDays()}/${zm.haulTargetDays} hari (${if (zm.isHaulFulfilled) "Haul Genap" else "Sisa ${zm.remainingHaulDays()} hari"})"
        val effectiveMaalPayable = if (zm.zakatAmount > 0) zm.zakatAmount else zakatPayable

        val boxHeight = 148f
        paint.color = bgLight
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + boxHeight), 6f, 6f, paint)

        paint.color = textDark
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        val goldPerGramStr = formatRp(zm.goldPricePerGram)
        session.canvas.drawText("• Standar Nisab Emas Murni (85 Gram x Rp $goldPerGramStr):", 44f, session.y + 18f, paint)
        session.canvas.drawText("Rp ${formatRp(state.nisabThreshold)}", 410f, session.y + 18f, paint)

        session.canvas.drawText("• Saldo Seluruh Kantong Kas & Simpanan Likuid:", 44f, session.y + 34f, paint)
        session.canvas.drawText("Rp ${formatRp(zm.totalWalletSavings.ifZero(state.totalAssets))}", 410f, session.y + 34f, paint)

        session.canvas.drawText("• Tabungan Rencana Ibadah & Emas Simpanan (${zm.goldWeightGrams}g):", 44f, session.y + 50f, paint)
        session.canvas.drawText("Rp ${formatRp(zm.totalIbadahSavings + zm.preciousMetalsValue)}", 410f, session.y + 50f, paint)

        session.canvas.drawText("• Piutang Lancar Tagih dikurangi Hutang Jatuh Tempo:", 44f, session.y + 66f, paint)
        val netReceivable = (zm.tradeableReceivables - zm.shortTermLiabilities)
        val netRecText = if (netReceivable >= 0) "Rp ${formatRp(netReceivable)}" else "-Rp ${formatRp(-netReceivable)}"
        session.canvas.drawText(netRecText, 410f, session.y + 66f, paint)

        session.canvas.drawText("• Periode Haul (${if (zm.isLunarCalendar) "354H Hijriyah" else "365H Masehi"}) Mulai: $haulStartStr", 44f, session.y + 82f, paint)
        session.canvas.drawText(haulProgressStr, 410f, session.y + 82f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = emeraldDark
        session.canvas.drawText("• Kewajiban Zakat Maal (${if (zm.isLunarCalendar) "2.5% Hijriyah" else "2.577% Masehi"}):", 44f, session.y + 102f, paint)
        session.canvas.drawText("Rp ${formatRp(effectiveMaalPayable)}", 410f, session.y + 102f, paint)

        paint.color = textDark
        session.canvas.drawText("• Total Kewajiban Gabungan (Maal + Fitrah) / Telah Disalurkan:", 44f, session.y + 118f, paint)
        session.canvas.drawText("Rp ${formatRp(state.totalZakatReportPayable)} / Rp ${formatRp(state.totalZakatDisbursedRecorded)}", 410f, session.y + 118f, paint)

        paint.color = textMuted
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val haulNote = if (zm.isObligatory || state.isNisabReached) {
            "Kaidah Haul: Nisab terpenuhi & masa haul terpantau ($haulProgressStr). Wajib ditunaikan kepada amil resmi."
        } else {
            "Kaidah Haul: Harta bersih di bawah nisab, belum timbul kewajiban zakat maal."
        }
        session.canvas.drawText(haulNote, 44f, session.y + 136f, paint)

        session.y += boxHeight + 16f

        // 4. SECTION 2: ZAKAT FITRAH & TATHHIR DANA NON-HALAL (FATWA DSN-MUI 123/2018)
        session.ensureSpace(135f)
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        session.canvas.drawText("2. Zakat Fitrah (Tanggungan & Bahan Pokok) & Tathhir Non-Halal", 32f, session.y, paint)
        session.y += 14f

        val zf = state.zakatFitrah
        val includedNames = zf.dependents.filter { it.isIncluded }.joinToString(", ") { it.name }.ifBlank { "${zf.activeDependentsCount} Jiwa" }
        val fitrahBoxHeight = 112f
        paint.color = bgLight
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + fitrahBoxHeight), 6f, 6f, paint)

        paint.color = textDark
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val unitLabel = "${zf.riceAmountPerPersonKg} ${zf.measurementUnit}"
        val fitrahRateLabel = if (zf.useLocalStaplePrice) "Rp ${formatRp(zf.ricePricePerKg)}/${zf.measurementUnit}" else "Tarif BAZNAS"
        session.canvas.drawText("• Zakat Fitrah (${zf.activeDependentsCount} Jiwa x $unitLabel ${zf.stapleFoodName} @ $fitrahRateLabel):", 44f, session.y + 18f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        session.canvas.drawText("Rp ${formatRp(zf.totalFitrahRupiah)} (${zf.totalRiceKg} ${zf.measurementUnit})", 400f, session.y + 18f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        session.canvas.drawText("• Daftar Tanggungan Aktif (${zf.regionName}): ${includedNames.take(68)}${if (includedNames.length > 68) "..." else ""}", 44f, session.y + 34f, paint)

        session.canvas.drawText("• Saldo Karantina Syubhat / Bunga Konvensional (`acc_syubhat`):", 44f, session.y + 52f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = if (syubhatBalance > 0) coralRed else emeraldDark
        session.canvas.drawText("Rp ${formatRp(syubhatBalance)}", 400f, session.y + 52f, paint)

        paint.color = textMuted
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        session.canvas.drawText(
            "Fatwa DSN-MUI 123: Dana non-halal wajib disalurkan 100% untuk fasilitas umum / sanitasi dengan prinsip",
            44f,
            session.y + 74f,
            paint
        )
        session.canvas.drawText(
            "bara'ah adz-dzimmah (pelepasan tanggung jawab) tanpa niat mencari pahala sedekah.",
            44f,
            session.y + 88f,
            paint
        )

        session.y += fitrahBoxHeight + 16f

        // 5. SECTION 3: ZAKAT PROFESI & ZAKAT PERNIAGAAN
        if (editorialConfig.isSectionIncluded(PdfSectionKey.ZAKAT_PROFESI_PERNIAGAAN)) {
            PdfSectionWriters.drawZakatProfesiAndPerniagaanSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.ZAKAT_PROFESI_PERNIAGAAN, isTactical, "3."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                goldAccent = goldAccent,
                coralRed = coralRed,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight
            )
        }

        // 6. SECTION 4: AKTIVITAS & STREAK SEDEKAH SUBUH HARIAN
        if (editorialConfig.isSectionIncluded(PdfSectionKey.SEDEKAH_SUBUH)) {
            PdfSectionWriters.drawSedekahSubuhSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.SEDEKAH_SUBUH, isTactical, "4."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                goldAccent = goldAccent,
                textDark = textDark,
                textMuted = textMuted,
                bgLight = bgLight,
                scopedData = scoped
            )
        }

        // 7. SECTION 5: RIWAYAT DISTRIBUSI INFAQ & TATHHIR KE 8 ASNAF
        session.ensureSpace(40f)
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        session.canvas.drawText(editorialConfig.resolveSectionTitle(PdfSectionKey.ASNAF_DISTRIBUTION, isTactical, "5."), 32f, session.y, paint)
        session.y += 14f

        fun drawInfaqTableHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Tanggal", 40f, topY + 12f, paint)
            c.drawText("Nama Penerima / Lembaga", 115f, topY + 12f, paint)
            c.drawText("Asnaf Mustahiq", 255f, topY + 12f, paint)
            c.drawText("Program / Keterangan", 370f, topY + 12f, paint)
            c.drawText("Nominal (Rp)", 480f, topY + 12f, paint)
            return topY + 22f
        }

        session.y = drawInfaqTableHeader(session.canvas, session.y)

        if (scoped.filteredDistributions.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada catatan penyaluran infaq/tathhir pada periode ini.", 40f, session.y + 12f, paint)
            session.y += 24f
        } else {
            val sdf = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))
            for ((idx, dist) in scoped.filteredDistributions.withIndex()) {
                paint.textSize = 7.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                val nameLines = wrapText(dist.recipientName, paint, 130f)
                val progLines = wrapText(dist.programName.ifBlank { dist.notes.ifBlank { "-" } }, paint, 100f)
                val maxLines = maxOf(nameLines.size, progLines.size)
                val rowHeight = 16f + (maxLines - 1) * 11f + 5f

                session.ensureSpace(rowHeight) { c, topY -> drawInfaqTableHeader(c, topY) }

                session.paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + rowHeight), session.paint)

                session.paint.color = textDark
                session.canvas.drawText(sdf.format(dist.distributionDate), 40f, session.y + 11f, session.paint)

                for (i in nameLines.indices) {
                    session.canvas.drawText(nameLines[i], 115f, session.y + 11f + (i * 11f), session.paint)
                }

                session.canvas.drawText(dist.asnafCategory.displayName, 255f, session.y + 11f, session.paint)

                for (i in progLines.indices) {
                    session.canvas.drawText(progLines[i], 370f, session.y + 11f + (i * 11f), session.paint)
                }

                session.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                session.paint.color = emeraldDark
                session.canvas.drawText(formatRp(dist.amount), 480f, session.y + 11f, session.paint)

                session.y += rowHeight
            }
            session.y += 10f
        }

        // 8. SECTION 6: PROGRAM PERENCANAAN IBADAH & ZISWAF AKTIF
        if (editorialConfig.isSectionIncluded(PdfSectionKey.IBADAH_GOALS)) {
            session.ensureSpace(40f)
            paint.color = emeraldDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 11f
            session.canvas.drawText(editorialConfig.resolveSectionTitle(PdfSectionKey.IBADAH_GOALS, isTactical, "6."), 32f, session.y, paint)
            session.y += 14f

            fun drawGoalsTableHeader(c: Canvas, topY: Float): Float {
                paint.color = emeraldPrimary
                c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
                paint.color = Color.WHITE
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                c.drawText("Nama Program Target", 40f, topY + 12f, paint)
                c.drawText("Jenis Ibadah", 215f, topY + 12f, paint)
                c.drawText("Target (Rp)", 325f, topY + 12f, paint)
                c.drawText("Terkumpul (Rp)", 415f, topY + 12f, paint)
                c.drawText("Capaian", 510f, topY + 12f, paint)
                return topY + 22f
            }

            session.y = drawGoalsTableHeader(session.canvas, session.y)

            if (state.ibadahGoals.isEmpty()) {
                paint.color = textMuted
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                session.canvas.drawText("Belum ada target perencanaan ibadah/qurban yang dibuat.", 40f, session.y + 12f, paint)
                session.y += 24f
            } else {
                for ((idx, goal) in state.ibadahGoals.withIndex()) {
                    paint.textSize = 8f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val lines = wrapText(goal.title, paint, 165f)
                    val rowHeight = 16f + (lines.size - 1) * 11f + 5f

                    session.ensureSpace(rowHeight) { c, topY -> drawGoalsTableHeader(c, topY) }

                    session.paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                    session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + rowHeight), session.paint)

                    session.paint.color = textDark
                    for (i in lines.indices) {
                        session.canvas.drawText(lines[i], 40f, session.y + 11f + (i * 11f), session.paint)
                    }

                    session.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    session.canvas.drawText(goal.type.title, 215f, session.y + 11f, session.paint)
                    session.canvas.drawText(formatRp(goal.targetAmount), 325f, session.y + 11f, session.paint)
                    session.canvas.drawText(formatRp(goal.currentAccumulated), 415f, session.y + 11f, session.paint)

                    val pct = (goal.progressRatio * 100).toInt()
                    session.paint.color = if (goal.isCompleted) emeraldDark else goldAccent
                    session.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    session.canvas.drawText("$pct%", 510f, session.y + 11f, session.paint)

                    session.y += rowHeight
                }
            }
        }

        PdfSectionWriters.drawClosingNoteSection(session, editorialConfig, emeraldDark, textDark, bgLight)

        session.finish()
        val zakatBasePrefix = editorialConfig.resolveFilePrefix("Laporan_Zakat_Infaq")
        val zakatFinalPrefix = if (dateRange.isCustomEnabled) "${zakatBasePrefix}_${dateRange.formatShortFileTag()}" else zakatBasePrefix
        return savePdfToFile(context, document, zakatFinalPrefix)
    }

    /**
     * Menghasilkan Laporan Komprehensif Buku Besar Jurnal Syariah (Comprehensive Double-Entry Ledger)
     * Mencakup seluruh kantong kas, saldo awal, audit trail debit-kredit berpasangan,
     * serta validasi kepatuhan pembukuan (Zero-Balancing).
     * Didukung multi-halaman dinamis sehingga seluruh mutasi tercetak lengkap tanpa terpotong.
     */
    fun generateComprehensiveLedgerPdf(
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
        val docTitle = editorialConfig.resolveDocumentTitle("Buku Besar Jurnal Transaksi")
        val session = PdfPageSession(
            document = document,
            documentTitle = docTitle,
            subtitle = editorialConfig.resolveHeaderSubtitle(isTactical, "Audit Trail Double-Entry, Saldo Akun & Rekonsiliasi Kas"),
            isTactical = isTactical,
            editorialConfig = editorialConfig
        )

        val canvas = session.canvas
        val paint = session.paint

        val emeraldDark = session.emeraldDark
        val emeraldPrimary = session.emeraldPrimary
        val goldAccent = session.goldAccent
        val coralRed = Color.rgb(211, 47, 47)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = session.textMuted
        val bgLight = Color.rgb(245, 248, 248)
        val bgCard = Color.rgb(235, 242, 241)

        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, targetMonth)
            set(Calendar.YEAR, targetYear)
        }
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time, manualOffsetDays = state.selectedHijriOffset)
        val monthStr = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
        val printDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())

        // 1. HEADER BANNER
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 75f), 8f, 8f, paint)
        paint.color = goldAccent
        canvas.drawRect(RectF(32f, session.y + 71f, 563f, session.y + 75f), paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        val defaultBrand = if (useTacticalText) "AUDIT BUKU BESAR JURNAL [OP//LEDGER]" else docTitle.uppercase(Locale("id", "ID"))
        val brandTitle = editorialConfig.headerBrandTitle.trim().ifBlank { defaultBrand }
        canvas.drawText(brandTitle.take(46), 46f, session.y + 26f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.rgb(220, 245, 242)
        val defaultSub = if (useTacticalText) "DOUBLE-ENTRY AUDIT TRAIL SYARIAH • REKONSILIASI MULTI-KAS" else "Audit Trail Double-Entry, Saldo Rekening Kas & Rekonsiliasi"
        val brandSubtitle = editorialConfig.resolveHeaderSubtitle(isTactical, defaultSub)
        canvas.drawText(brandSubtitle.take(72), 46f, session.y + 42f, paint)

        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        val hijriPart = if (editorialConfig.includeHijriDate && !dateRange.isCustomEnabled) "${hijriDate.monthName} ${hijriDate.year} H" else ""
        val ownerAndPeriod = editorialConfig.resolveOwnerLine(
            stateUserName = state.userNameKasMukmin,
            isAppTactical = isTactical,
            hijriDateText = hijriPart,
            extraSuffix = if (dateRange.isCustomEnabled) dateRange.formatPdfHeaderPeriod() else "Periode: $monthStr • $printDateStr"
        )
        canvas.drawText(ownerAndPeriod.take(86), 46f, session.y + 60f, paint)

        session.y += 88f

        // 2. EXECUTIVE METRIC CARDS
        if (editorialConfig.includeExecutiveKpis) {
            val cardWidth = 126f
            val cardHeight = 52f
            val cardSpacing = 8.6f

            val totalDebitAll = scoped.filteredEntries.sumOf { it.totalDebit }
            val totalCreditAll = scoped.filteredEntries.sumOf { it.totalCredit }

            drawSummaryCard(session.canvas, paint, 32f, session.y, cardWidth, cardHeight, bgCard, "TOTAL SALDO KAS/BANK", "Rp ${formatRp(state.totalAssets)}", emeraldDark)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing), session.y, cardWidth, cardHeight, bgCard, "TOTAL MUTASI DEBIT", "Rp ${formatRp(totalDebitAll)}", emeraldDark)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 2, session.y, cardWidth, cardHeight, bgCard, "TOTAL MUTASI KREDIT", "Rp ${formatRp(totalCreditAll)}", emeraldPrimary)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 3, session.y, cardWidth, cardHeight, bgCard, "STATUS PEMBUKUAN", "SEIMBANG (100%)", goldAccent)

            session.y += 66f
        }

        var secNo = 1
        // 3. SECTION 1: DAFTAR REKENING KAS & KANTONG AMANAH
        session.ensureSpace(40f)
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        session.canvas.drawText(
            editorialConfig.resolveSectionTitle(PdfSectionKey.WALLETS, isTactical, "${secNo++}."),
            32f, session.y, paint
        )
        session.y += 14f

        fun drawWalletsTableHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Nama Kantong / Rekening", 40f, topY + 12f, paint)
            c.drawText("Tipe Akun", 215f, topY + 12f, paint)
            c.drawText("No. Rekening / Keterangan", 330f, topY + 12f, paint)
            c.drawText("Saldo Kas (Rp)", 470f, topY + 12f, paint)
            return topY + 22f
        }

        session.y = drawWalletsTableHeader(session.canvas, session.y)

        for ((idx, w) in state.wallets.withIndex()) {
            val bal = state.getWalletBalance(w.id)
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            val nameLines = wrapText(w.name, paint, 165f)
            val accLines = wrapText(w.accountNumber.ifBlank { "-" }, paint, 130f)
            val maxLines = maxOf(nameLines.size, accLines.size)
            val rowHeight = 16f + (maxLines - 1) * 11f + 5f

            session.ensureSpace(rowHeight) { c, topY -> drawWalletsTableHeader(c, topY) }

            session.paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
            session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + rowHeight), session.paint)

            session.paint.color = textDark
            for (i in nameLines.indices) {
                session.canvas.drawText(nameLines[i], 40f, session.y + 11f + (i * 11f), session.paint)
            }

            session.canvas.drawText(w.type.displayName, 215f, session.y + 11f, session.paint)

            for (i in accLines.indices) {
                session.canvas.drawText(accLines[i], 330f, session.y + 11f + (i * 11f), session.paint)
            }

            session.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            session.paint.color = emeraldDark
            session.canvas.drawText(formatRp(bal), 470f, session.y + 11f, session.paint)

            session.y += rowHeight
        }

        session.y += 16f

        // 4. SECTION 2: JURNAL MUTASI BERPASANGAN LENGKAP (AUDIT TRAIL + DETAIL AKUN DEBIT/KREDIT)
        PdfSectionWriters.drawJournalEntriesWithLinesTable(
            session = session,
            state = state,
            entries = scoped.filteredEntries,
            sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.JOURNAL_LEDGER, isTactical, "${secNo++}.") + " (${scoped.filteredEntries.size} Entri)",
            emeraldDark = emeraldDark,
            emeraldPrimary = emeraldPrimary,
            coralRed = coralRed,
            blueInfo = Color.rgb(30, 136, 229),
            textDark = textDark,
            textMuted = textMuted,
            bgLight = bgLight,
            editorialConfig = editorialConfig
        )

        PdfSectionWriters.drawClosingNoteSection(session, editorialConfig, emeraldDark, textDark, bgLight)

        session.finish()
        val ledgerBasePrefix = editorialConfig.resolveFilePrefix("Buku_Besar_Ledger")
        val ledgerFinalPrefix = if (dateRange.isCustomEnabled) "${ledgerBasePrefix}_${dateRange.formatShortFileTag()}" else ledgerBasePrefix
        return savePdfToFile(context, document, ledgerFinalPrefix)
    }

    internal fun drawSummaryCard(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        bgColor: Int,
        title: String,
        value: String,
        valColor: Int
    ) {
        paint.color = bgColor
        canvas.drawRoundRect(RectF(x, y, x + w, y + h), 6f, 6f, paint)

        paint.color = Color.rgb(90, 105, 110)
        paint.textSize = 6.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(title, x + 8f, y + 16f, paint)

        paint.color = valColor
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 8f, y + 36f, paint)
    }

    internal fun drawMiniStat(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        title: String,
        value: String,
        valColor: Int
    ) {
        paint.color = Color.rgb(90, 105, 110)
        paint.textSize = 6.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(title, x, y + 16f, paint)

        paint.color = valColor
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x, y + 34f, paint)
    }

    /**
     * Menghasilkan PDF Laporan Kondisi Finansial Komprehensif
     * Merangkum seluruh kondisi finansial pengguna: Saldo Dompet, Kekayaan Bersih (Net Worth),
     * Buku Hutang Piutang Qardhul Hasan, dan Status Kewajiban Zakat.
     */
    fun generateComprehensiveFinancialPdf(
        context: Context,
        state: AmanahLedgerUiState,
        editorialConfig: PdfEditorialConfig = state.pdfEditorialConfig,
        dateRange: PdfDateRange = PdfDateRange()
    ): File {
        val scoped = PdfPeriodFilterHelper.compute(state, dateRange)
        val document = PdfDocument()
        val isTactical = state.themeMode.isTactical
        val useTacticalText = editorialConfig.isTacticalTextEnabled(isTactical)
        val docTitle = editorialConfig.resolveDocumentTitle("Laporan Kondisi Finansial Komprehensif")
        val session = PdfPageSession(
            document = document,
            documentTitle = docTitle,
            subtitle = editorialConfig.resolveHeaderSubtitle(isTactical, "Neraca Saldo Dompet, Buku Hutang Piutang & Status Zakat"),
            isTactical = isTactical,
            editorialConfig = editorialConfig
        )

        val cal = Calendar.getInstance()
        val printDateStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(cal.time)
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time, manualOffsetDays = state.selectedHijriOffset)

        val emeraldDark = session.emeraldDark
        val emeraldPrimary = session.emeraldPrimary
        val goldAccent = session.goldAccent
        val coralRed = Color.rgb(198, 40, 40)
        val blueInfo = Color.rgb(21, 101, 192)
        val textMuted = session.textMuted
        val bgCard = Color.rgb(245, 247, 248)
        val bgRowAlt = Color.rgb(240, 245, 244)
        val paint = session.paint
        val canvas = session.canvas

        // 1. BANNER HEADER
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 80f), 8f, 8f, paint)

        // Accent gold stripe
        paint.color = goldAccent
        canvas.drawRect(RectF(32f, session.y + 76f, 563f, session.y + 80f), paint)

        // Judul Utama
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        val defaultBrand = if (useTacticalText) "DOSIR KEUANGAN TAKTIS AMANAH" else docTitle.uppercase(Locale("id", "ID"))
        val brandTitle = editorialConfig.headerBrandTitle.trim().ifBlank { defaultBrand }
        canvas.drawText(brandTitle.take(46), 46f, session.y + 24f, paint)

        // Sub-judul
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.color = Color.rgb(220, 245, 242)
        val defaultSub = if (useTacticalText) "COMPREHENSIVE FINANCIAL DOSSIER • MULTI-WALLET RECONCILIATION" else "Ringkasan Kondisi Finansial, Kas & Hutang Piutang"
        val brandSubtitle = editorialConfig.resolveHeaderSubtitle(isTactical, defaultSub)
        canvas.drawText(brandSubtitle.take(72), 46f, session.y + 40f, paint)

        // Info Pemilik & Tanggal
        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        val hijriPart = if (editorialConfig.includeHijriDate && !dateRange.isCustomEnabled) "${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} H" else ""
        val ownerLine = editorialConfig.resolveOwnerLine(
            stateUserName = state.userNameKasMukmin,
            isAppTactical = isTactical,
            hijriDateText = hijriPart,
            extraSuffix = if (dateRange.isCustomEnabled) dateRange.formatPdfHeaderPeriod() else ""
        )
        canvas.drawText(ownerLine.take(86), 46f, session.y + 60f, paint)

        // Timestamp
        paint.textSize = 7.5f
        paint.color = Color.rgb(200, 230, 225)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dicetak: $printDateStr WIB", 420f, session.y + 24f, paint)

        session.y += 94f

        // 2. KALKULASI NERACA EKSEKUTIF
        val totalWalletBalance = state.totalWalletBalance
        val receivables = state.qardhRecords.filter { it.type == QardhType.PIUTANG_SAYA }
        val payables = state.qardhRecords.filter { it.type == QardhType.HUTANG_SAYA }
        val activeReceivables = receivables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }
        val activePayables = payables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }
        val totalPiutang = activeReceivables.sumOf { it.remainingAmount }
        val totalHutang = activePayables.sumOf { it.remainingAmount }
        val netWorth = totalWalletBalance + totalPiutang - totalHutang

        if (editorialConfig.includeExecutiveKpis) {
            val cardWidth = 126f
            val cardHeight = 52f
            val cardSpacing = 8.6f

            drawSummaryCard(session.canvas, paint, 32f, session.y, cardWidth, cardHeight, bgCard, "TOTAL SALDO KAS", "Rp ${formatRp(totalWalletBalance)}", emeraldPrimary)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing), session.y, cardWidth, cardHeight, bgCard, "PIUTANG AKTIF", "Rp ${formatRp(totalPiutang)}", blueInfo)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 2, session.y, cardWidth, cardHeight, bgCard, "HUTANG KEWAJIBAN", "Rp ${formatRp(totalHutang)}", coralRed)
            drawSummaryCard(session.canvas, paint, 32f + (cardWidth + cardSpacing) * 3, session.y, cardWidth, cardHeight, bgCard, "KEKAYAAN BERSIH", "Rp ${formatRp(netWorth)}", if (netWorth >= 0) emeraldDark else coralRed)

            session.y += 66f
        }

        var secNo = 1
        // 3. SECTION 1: RINCIAN SALDO SELURUH DOMPET & REKENING
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        session.canvas.drawText(editorialConfig.resolveSectionTitle(PdfSectionKey.WALLETS, isTactical, "${secNo++}."), 32f, session.y, paint)
        session.y += 14f

        fun drawWalletTableHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Nama Kantong / Rekening", 40f, topY + 12f, paint)
            c.drawText("Tipe Akun", 230f, topY + 12f, paint)
            c.drawText("Nomor Rekening", 340f, topY + 12f, paint)
            c.drawText("Saldo Saat Ini (Rp)", 465f, topY + 12f, paint)
            return topY + 22f
        }

        session.y = drawWalletTableHeader(session.canvas, session.y)

        if (state.wallets.isEmpty()) {
            paint.color = textMuted
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 8.5f
            session.canvas.drawText("Belum ada akun dompet kas yang terdaftar.", 40f, session.y + 12f, paint)
            session.y += 24f
        } else {
            var rowIndex = 0
            for (w in state.wallets) {
                session.ensureSpace(18f) { c, topY -> drawWalletTableHeader(c, topY) }
                val balance = state.getWalletBalance(w.id)
                if (rowIndex % 2 == 1) {
                    paint.color = bgRowAlt
                    session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)
                }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = emeraldDark
                paint.textSize = 8.5f
                session.canvas.drawText(w.name, 40f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textMuted
                session.canvas.drawText(w.type.displayName, 230f, session.y + 11f, paint)
                session.canvas.drawText(w.accountNumber.ifBlank { "-" }, 340f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (balance >= 0) emeraldPrimary else coralRed
                session.canvas.drawText("Rp ${formatRp(balance)}", 465f, session.y + 11f, paint)

                session.y += 16f
                rowIndex++
            }
            // Garis total dompet
            session.ensureSpace(20f)
            paint.color = emeraldDark
            session.canvas.drawLine(32f, session.y + 2f, 563f, session.y + 2f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            session.canvas.drawText("TOTAL SALDO KAS TERSEDIA:", 280f, session.y + 14f, paint)
            session.canvas.drawText("Rp ${formatRp(totalWalletBalance)}", 465f, session.y + 14f, paint)
            session.y += 26f
        }

        // 4. SECTION 2: BUKU PIUTANG KEBAJIKAN (QARDHUL HASAN)
        session.ensureSpace(35f)
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        session.canvas.drawText("2. Rekapitulasi Piutang Kebajikan (Qardhul Hasan)", 32f, session.y, paint)
        session.y += 14f

        fun drawReceivableTableHeader(c: Canvas, topY: Float): Float {
            paint.color = blueInfo
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Nama Peminjam (Debitur)", 40f, topY + 12f, paint)
            c.drawText("Jatuh Tempo", 200f, topY + 12f, paint)
            c.drawText("Total Dipinjam", 295f, topY + 12f, paint)
            c.drawText("Sisa Piutang", 390f, topY + 12f, paint)
            c.drawText("Status Akad", 485f, topY + 12f, paint)
            return topY + 22f
        }

        session.y = drawReceivableTableHeader(session.canvas, session.y)

        if (receivables.isEmpty()) {
            paint.color = textMuted
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 8.5f
            session.canvas.drawText("Tidak ada data piutang kebajikan yang tercatat.", 40f, session.y + 12f, paint)
            session.y += 24f
        } else {
            var rowIndex = 0
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            for (q in receivables) {
                session.ensureSpace(18f) { c, topY -> drawReceivableTableHeader(c, topY) }
                if (rowIndex % 2 == 1) {
                    paint.color = bgRowAlt
                    session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)
                }

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = emeraldDark
                paint.textSize = 8.5f
                session.canvas.drawText(q.counterpartyName, 40f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textMuted
                val dueStr = q.dueDateMillis?.let { sdf.format(Date(it)) } ?: "Tanpa Batas"
                session.canvas.drawText(dueStr, 200f, session.y + 11f, paint)
                session.canvas.drawText("Rp ${formatRp(q.totalAmount)}", 295f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = blueInfo
                session.canvas.drawText("Rp ${formatRp(q.remainingAmount)}", 390f, session.y + 11f, paint)

                paint.textSize = 8f
                paint.color = if (q.status == QardhStatus.LUNAS) emeraldPrimary else textMuted
                session.canvas.drawText(q.status.displayName, 485f, session.y + 11f, paint)

                session.y += 16f
                rowIndex++
            }
            session.ensureSpace(20f)
            paint.color = blueInfo
            session.canvas.drawLine(32f, session.y + 2f, 563f, session.y + 2f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            session.canvas.drawText("TOTAL PIUTANG AKTIF BELUM TERTARIK:", 220f, session.y + 14f, paint)
            session.canvas.drawText("Rp ${formatRp(totalPiutang)}", 465f, session.y + 14f, paint)
            session.y += 26f
        }

        // 5. SECTION 3: BUKU HUTANG SYARIAH (KEWAJIBAN PENGGUNA)
        session.ensureSpace(35f)
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        session.canvas.drawText("3. Rekapitulasi Kewajiban Hutang Syariah Pengguna", 32f, session.y, paint)
        session.y += 14f

        fun drawPayableTableHeader(c: Canvas, topY: Float): Float {
            paint.color = coralRed
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Pemberi Pinjaman (Kreditur)", 40f, topY + 12f, paint)
            c.drawText("Jatuh Tempo", 200f, topY + 12f, paint)
            c.drawText("Total Pokok", 295f, topY + 12f, paint)
            c.drawText("Sisa Hutang", 390f, topY + 12f, paint)
            c.drawText("Status Kewajiban", 485f, topY + 12f, paint)
            return topY + 22f
        }

        session.y = drawPayableTableHeader(session.canvas, session.y)

        if (payables.isEmpty()) {
            paint.color = textMuted
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 8.5f
            session.canvas.drawText("Alhamdulillah, tidak ada catatan kewajiban hutang.", 40f, session.y + 12f, paint)
            session.y += 24f
        } else {
            var rowIndex = 0
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            for (q in payables) {
                session.ensureSpace(18f) { c, topY -> drawPayableTableHeader(c, topY) }
                if (rowIndex % 2 == 1) {
                    paint.color = bgRowAlt
                    session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)
                }

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = emeraldDark
                paint.textSize = 8.5f
                session.canvas.drawText(q.counterpartyName, 40f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textMuted
                val dueStr = q.dueDateMillis?.let { sdf.format(Date(it)) } ?: "Tanpa Batas"
                session.canvas.drawText(dueStr, 200f, session.y + 11f, paint)
                session.canvas.drawText("Rp ${formatRp(q.totalAmount)}", 295f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = coralRed
                session.canvas.drawText("Rp ${formatRp(q.remainingAmount)}", 390f, session.y + 11f, paint)

                paint.textSize = 8f
                paint.color = if (q.status == QardhStatus.LUNAS) emeraldPrimary else textMuted
                session.canvas.drawText(q.status.displayName, 485f, session.y + 11f, paint)

                session.y += 16f
                rowIndex++
            }
            session.ensureSpace(20f)
            paint.color = coralRed
            session.canvas.drawLine(32f, session.y + 2f, 563f, session.y + 2f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            session.canvas.drawText("TOTAL HUTANG YANG WAJIB DILUNASI:", 220f, session.y + 14f, paint)
            session.canvas.drawText("Rp ${formatRp(totalHutang)}", 465f, session.y + 14f, paint)
            session.y += 26f
        }

        // 6. SECTION: RINCIAN ARUS KAS KASAB VS NON-KASAB & PENGELUARAN PER KATEGORI
        if (editorialConfig.isSectionIncluded(PdfSectionKey.CASHFLOW_KASAB)) {
            PdfSectionWriters.drawCashflowKasabAndExpenseSection(
                session = session,
                state = state,
                sectionTitle = editorialConfig.resolveSectionTitle(PdfSectionKey.CASHFLOW_KASAB, isTactical, "${secNo++}."),
                emeraldDark = emeraldDark,
                emeraldPrimary = emeraldPrimary,
                coralRed = coralRed,
                goldAccent = goldAccent,
                textDark = emeraldDark,
                textMuted = textMuted,
                bgLight = bgCard,
                editorialConfig = editorialConfig,
                scopedData = scoped
            )
        }

        // 7. SECTION: STATUS KEWAJIBAN ZAKAT & TABARRU'
        if (editorialConfig.isSectionIncluded(PdfSectionKey.ZAKAT_MAAL_FITRAH)) {
            session.ensureSpace(80f)
            paint.color = emeraldDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10.5f
            session.canvas.drawText("${secNo++}. Ringkasan Status Zakat & Dana Sosial", 32f, session.y, paint)
            session.y += 14f

            val nisabValue = state.goldPricePerGram * 85.0
            val isZakatWajib = totalWalletBalance >= nisabValue
            val zakatEstimation = if (isZakatWajib) totalWalletBalance * 0.025 else 0.0

            paint.color = bgCard
            session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 60f), 6f, 6f, paint)
            paint.color = goldAccent
            session.canvas.drawRoundRect(RectF(32f, session.y, 36f, session.y + 60f), 2f, 2f, paint)

            paint.color = emeraldDark
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            session.canvas.drawText("Standar Nisab Emas (85 gram):", 46f, session.y + 18f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            session.canvas.drawText("Rp ${formatRp(nisabValue)} (@ Rp ${formatRp(state.goldPricePerGram)}/gram)", 220f, session.y + 18f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            session.canvas.drawText("Status Nisab Harta Kas:", 46f, session.y + 34f, paint)
            paint.color = if (isZakatWajib) emeraldPrimary else textMuted
            session.canvas.drawText(if (isZakatWajib) "Mencapai Nisab (Wajib Zakat jika Haul Genap)" else "Belum Mencapai Nisab", 220f, session.y + 34f, paint)

            paint.color = emeraldDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            session.canvas.drawText("Estimasi Zakat Maal (2.5%):", 46f, session.y + 50f, paint)
            paint.color = if (zakatEstimation > 0) goldAccent else textMuted
            session.canvas.drawText("Rp ${formatRp(zakatEstimation)}", 220f, session.y + 50f, paint)

            paint.color = emeraldDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            session.canvas.drawText("Saldo Dana Infaq:", 390f, session.y + 18f, paint)
            paint.color = emeraldPrimary
            session.canvas.drawText("Rp ${formatRp(state.virtualInfaqVaultBalance)}", 495f, session.y + 18f, paint)

            session.y += 74f
        }

        // Jaminan Syariah
        session.ensureSpace(40f)
        paint.color = emeraldDark
        session.canvas.drawLine(32f, session.y, 563f, session.y, paint)
        paint.color = textMuted
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        session.canvas.drawText("Prinsip Fiqih: 'Harta yang bersih adalah harta yang ditunaikan zakatnya dan disucikan dari riba.' (QS. At-Taubah: 103)", 32f, session.y + 14f, paint)
        session.canvas.drawText("Pencatatan hutang piutang selaras syariat QS. Al-Baqarah: 282 tanpa bunga dan tanpa denda riba.", 32f, session.y + 26f, paint)
        session.y += 34f

        PdfSectionWriters.drawClosingNoteSection(session, editorialConfig, emeraldDark, emeraldDark, bgCard)

        session.finish()
        val compBasePrefix = editorialConfig.resolveFilePrefix("Laporan_Finansial_Komprehensif")
        val compFinalPrefix = if (dateRange.isCustomEnabled) "${compBasePrefix}_${dateRange.formatShortFileTag()}" else compBasePrefix
        return savePdfToFile(context, document, compFinalPrefix)
    }

    internal fun Double.ifZero(fallback: Double): Double = if (this <= 0.0) fallback else this

    internal fun savePdfToFile(context: Context, document: PdfDocument, baseName: String): File {
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val fileName = "${baseName}_${System.currentTimeMillis()}.pdf"
        val outputFile = File(reportsDir, fileName)
        val outputStream = FileOutputStream(outputFile)
        document.writeTo(outputStream)
        outputStream.flush()
        outputStream.close()
        document.close()

        return outputFile
    }

    fun sharePdfReport(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan Syariah - Infaqan")
            putExtra(Intent.EXTRA_TEXT, "Berikut terlampir dokumen Laporan Keuangan Syariah & ZISWAF dari Amanah Ledger.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Bagikan Laporan PDF via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun viewPdfReport(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(intent, "Buka Laporan PDF dengan")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}

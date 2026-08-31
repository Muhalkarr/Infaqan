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
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FinancialPdfGenerator {

    private fun formatRp(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    /**
     * Menghasilkan file PDF Laporan Keuangan Bulanan & Distribusi Infaq
     */
    fun generateMonthlyReportPdf(
        context: Context,
        state: AmanahLedgerUiState,
        targetMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
        targetYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (72 dpi)
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        val emeraldDark = Color.rgb(0, 77, 64)
        val emeraldPrimary = Color.rgb(0, 137, 123)
        val goldAccent = Color.rgb(218, 165, 32)
        val coralRed = Color.rgb(211, 47, 47)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = Color.rgb(90, 105, 110)
        val bgLight = Color.rgb(245, 248, 248)
        val bgCard = Color.rgb(235, 242, 241)

        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, targetMonth)
        cal.set(Calendar.YEAR, targetYear)
        val monthName = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time)

        var y = 36f

        // 1. HEADER BANNER
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 75f), 10f, 10f, paint)

        // Accent gold stripe
        paint.color = goldAccent
        canvas.drawRect(RectF(30f, y + 71f, 565f, y + 75f), paint)

        // Bismillah & Title
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 17f
        canvas.drawText("INFAQAN SYARIAH (AMANAH LEDGER)", 48f, y + 26f, paint)

        // Subtitle
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.color = Color.rgb(220, 245, 242)
        canvas.drawText("Laporan Keuangan Bulanan, Alokasi Anggaran & Distribusi Tabarru'", 48f, y + 42f, paint)

        // Periode badge
        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("Periode: $monthName / ${hijriDate.monthName} ${hijriDate.year} H", 48f, y + 60f, paint)

        // Timestamp right-aligned
        val printDateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID")).format(Date())
        paint.textSize = 8f
        paint.color = Color.rgb(200, 230, 225)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Dicetak: $printDateStr WIB", 440f, y + 25f, paint)

        y += 88f

        // 2. EXECUTIVE SUMMARY 4-BOX METRIC GRID
        val boxWidth = 125f
        val boxHeight = 52f
        val boxMargin = 8f

        drawSummaryCard(canvas, paint, 30f, y, boxWidth, boxHeight, bgCard, "TOTAL ASET HALAL", "Rp ${formatRp(state.totalAssets)}", emeraldDark)
        drawSummaryCard(canvas, paint, 30f + (boxWidth + boxMargin), y, boxWidth, boxHeight, bgCard, "PENGELUARAN BULANAN", "Rp ${formatRp(state.totalConsumptionExpense)}", coralRed)
        drawSummaryCard(canvas, paint, 30f + (boxWidth + boxMargin) * 2, y, boxWidth, boxHeight, bgCard, "TITIPAN INFAQ VAULT", "Rp ${formatRp(state.virtualInfaqVaultBalance)}", emeraldPrimary)
        val sliScore = String.format(Locale("id", "ID"), "%.1f%%", state.spiritualLiquidityIndex)
        drawSummaryCard(canvas, paint, 30f + (boxWidth + boxMargin) * 3, y, boxWidth, boxHeight, bgCard, "INDEKS SPIRITUAL (SLI)", sliScore, goldAccent)

        y += 66f

        // 3. SECTION: STATUS ALOKASI ANGGARAN BULANAN
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("1. Realisasi & Kepatuhan Alokasi Anggaran Bulanan", 30f, y, paint)

        y += 14f

        // Table Header
        paint.color = emeraldPrimary
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 18f), 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Pos Kategori Anggaran", 38f, y + 12f, paint)
        canvas.drawText("Batas Limit", 210f, y + 12f, paint)
        canvas.drawText("Realisasi Belanja", 305f, y + 12f, paint)
        canvas.drawText("Sisa Kuota", 410f, y + 12f, paint)
        canvas.drawText("Status / %", 495f, y + 12f, paint)

        y += 22f

        if (state.budgets.isEmpty()) {
            paint.color = textMuted
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.textSize = 8.5f
            canvas.drawText("Belum ada alokasi anggaran yang ditetapkan untuk pos pengeluaran.", 38f, y + 12f, paint)
            y += 20f
        } else {
            var rowIndex = 0
            for (b in state.budgets) {
                val spent = state.getMonthlySpentForAccount(b.accountId, targetMonth, targetYear)
                val remaining = b.monthlyLimit - spent
                val ratio = if (b.monthlyLimit > 0) (spent / b.monthlyLimit) * 100.0 else 0.0
                val isOver = spent > b.monthlyLimit

                paint.color = if (rowIndex % 2 == 0) bgLight else Color.WHITE
                canvas.drawRect(RectF(30f, y, 565f, y + 16f), paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8f
                paint.color = textDark
                canvas.drawText(b.categoryName, 38f, y + 11f, paint)
                canvas.drawText("Rp ${formatRp(b.monthlyLimit)}", 210f, y + 11f, paint)
                canvas.drawText("Rp ${formatRp(spent)}", 305f, y + 11f, paint)

                paint.color = if (isOver) coralRed else emeraldDark
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val remText = if (isOver) "-Rp ${formatRp(-remaining)}" else "Rp ${formatRp(remaining)}"
                canvas.drawText(remText, 410f, y + 11f, paint)

                val statusText = if (isOver) "OVER (${String.format(Locale("id", "ID"), "%.0f", ratio)}%)" else "${String.format(Locale("id", "ID"), "%.0f", ratio)}% (Aman)"
                canvas.drawText(statusText, 495f, y + 11f, paint)

                y += 16f
                rowIndex++
            }
            y += 8f
        }

        // 4. SECTION: INFAQ, SEDEKAH & PENYALURAN MUSTAHIQ
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("2. Rekapitulasi Infaq, Pembersihan Syubhat & Distribusi Mustahiq", 30f, y, paint)

        y += 12f

        paint.color = bgLight
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 46f), 6f, 6f, paint)

        paint.color = textMuted
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("TOTAL INFAQ TERSUCIKAN", 42f, y + 15f, paint)
        paint.color = emeraldDark
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Rp ${formatRp(state.totalPurifiedInfaq)}", 42f, y + 32f, paint)

        paint.color = textMuted
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("REALISASI PENYALURAN", 230f, y + 15f, paint)
        paint.color = emeraldPrimary
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Rp ${formatRp(state.totalDisbursedInfaq)}", 230f, y + 32f, paint)

        paint.color = textMuted
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("SISA AMANAH VAULT", 410f, y + 15f, paint)
        paint.color = if (state.virtualInfaqVaultBalance > 0) coralRed else emeraldDark
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Rp ${formatRp(state.virtualInfaqVaultBalance)}", 410f, y + 32f, paint)

        y += 58f

        // 5. SECTION: JURNAL TRANSAKSI BUKU BESAR
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("3. Catatan Jurnal Transaksi Buku Besar (Double-Entry Ledger)", 30f, y, paint)

        y += 12f

        paint.color = Color.rgb(60, 80, 85)
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 16f), 4f, 4f, paint)
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Tanggal (Masehi / Hijriyah)", 38f, y + 11f, paint)
        canvas.drawText("Keterangan Transaksi", 170f, y + 11f, paint)
        canvas.drawText("Tipe", 380f, y + 11f, paint)
        canvas.drawText("Nominal", 470f, y + 11f, paint)

        y += 20f

        val recentEntries = state.journalEntries.take(8)
        val entryDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        for ((idx, entry) in recentEntries.withIndex()) {
            paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
            canvas.drawRect(RectF(30f, y, 565f, y + 15f), paint)

            paint.color = textDark
            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val hDate = HijriCalendarEngine.fromGregorian(entry.gregorianDate)
            canvas.drawText("${entryDateFormat.format(entry.gregorianDate)} (${hDate.day}/${hDate.month}H)", 38f, y + 10f, paint)

            val shortDesc = if (entry.description.length > 38) entry.description.take(36) + "..." else entry.description
            canvas.drawText(shortDesc, 170f, y + 10f, paint)

            val typeLabel = when (entry.transactionType) {
                "INFLOW" -> "Pemasukan"
                "EXPENSE" -> "Pengeluaran"
                "INFAQ_PAYOUT" -> "Penyaluran"
                else -> entry.transactionType
            }
            canvas.drawText(typeLabel, 380f, y + 10f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (entry.transactionType == "INFLOW") emeraldDark else if (entry.transactionType == "INFAQ_PAYOUT") emeraldPrimary else coralRed
            canvas.drawText("Rp ${formatRp(entry.totalDebit)}", 470f, y + 10f, paint)

            y += 15f
        }

        // Footer
        paint.color = emeraldDark
        canvas.drawLine(30f, 790f, 565f, 790f, paint)

        paint.color = textMuted
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Laporan Resmi Infaqan Syariah. Dicetak secara aman dari Buku Besar Amanah Ledger berprinsip syariah.", 30f, 804f, paint)
        canvas.drawText("Kaidah: Menjaga Keseimbangan Debet-Kredit, Melunasi Hak Fakir Miskin & Menghindari Riba/Syubhat.", 30f, 814f, paint)

        document.finishPage(page)

        return savePdfToFile(context, document, "Laporan_Bulanan_${targetYear}_${targetMonth + 1}")
    }

    /**
     * Menghasilkan Laporan Resmi Zakat, Infaq & Haul/Nisab
     */
    fun generateZakatAndInfaqReportPdf(
        context: Context,
        state: AmanahLedgerUiState
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        val emeraldDark = Color.rgb(0, 77, 64)
        val emeraldPrimary = Color.rgb(0, 137, 123)
        val goldAccent = Color.rgb(218, 165, 32)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = Color.rgb(90, 105, 110)
        val bgLight = Color.rgb(245, 248, 248)
        val bgCard = Color.rgb(235, 242, 241)

        val cal = Calendar.getInstance()
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time)

        var y = 36f

        // 1. Header Banner
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 75f), 10f, 10f, paint)
        paint.color = goldAccent
        canvas.drawRect(RectF(30f, y + 71f, 565f, y + 75f), paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 17f
        canvas.drawText("REKAPITULASI ZAKAT, INFAQ & ZISWAF", 48f, y + 26f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.color = Color.rgb(220, 245, 242)
        canvas.drawText("Kalkulasi Nisab 85g Emas, Haul Syariah & Penyaluran 8 Asnaf Mustahiq", 48f, y + 42f, paint)

        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("Tanggal Syariah: ${hijriDate.day} ${hijriDate.monthName} ${hijriDate.year} H", 48f, y + 60f, paint)

        y += 88f

        // 2. Summary Zakat Cards
        val boxWidth = 125f
        val boxHeight = 52f
        val boxMargin = 8f

        val zakatPayable = state.zakatPayableAmount
        val nisabStatusText = if (state.isNisabReached) "MENCAPAI NISAB" else "BELUM NISAB"
        val nisabColor = if (state.isNisabReached) goldAccent else textMuted

        drawSummaryCard(canvas, paint, 30f, y, boxWidth, boxHeight, bgCard, "TOTAL HARTA ZAKAT", "Rp ${formatRp(state.totalAssets)}", emeraldDark)
        drawSummaryCard(canvas, paint, 30f + (boxWidth + boxMargin), y, boxWidth, boxHeight, bgCard, "STATUS NISAB (85G)", nisabStatusText, nisabColor)
        drawSummaryCard(canvas, paint, 30f + (boxWidth + boxMargin) * 2, y, boxWidth, boxHeight, bgCard, "ESTIMASI ZAKAT MAL (2.5%)", "Rp ${formatRp(zakatPayable)}", emeraldPrimary)
        drawSummaryCard(canvas, paint, 30f + (boxWidth + boxMargin) * 3, y, boxWidth, boxHeight, bgCard, "TOTAL INFAQ DISALURKAN", "Rp ${formatRp(state.totalDisbursedInfaq)}", emeraldDark)

        y += 68f

        // 3. Section: Status Haul & Perhitungan Zakat
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("1. Rincian Nisab & Kewajiban Zakat Maal (Surat At-Taubah: 103)", 30f, y, paint)

        y += 14f

        paint.color = bgLight
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 80f), 6f, 6f, paint)

        paint.color = textDark
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("• Standar Nisab Emas Murni (85 Gram):", 42f, y + 18f, paint)
        canvas.drawText("Rp ${formatRp(state.nisabThresholdRupiah)}", 380f, y + 18f, paint)

        canvas.drawText("• Total Aset Wajib Zakat (Kas + Emas + Piutang Lancar):", 42f, y + 36f, paint)
        canvas.drawText("Rp ${formatRp(state.totalAssets)}", 380f, y + 36f, paint)

        canvas.drawText("• Kewajiban Zakat Bersih (2.5%):", 42f, y + 54f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = emeraldDark
        canvas.drawText("Rp ${formatRp(zakatPayable)}", 380f, y + 54f, paint)

        paint.color = textMuted
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        val haulNote = if (state.isHaulCompleted) "Haul 1 Tahun Hijriah telah terpenuhi. Dianjurkan segera ditunaikan." else "Periode Haul sedang berjalan dalam pemantauan otomatis."
        canvas.drawText(haulNote, 42f, y + 70f, paint)

        y += 94f

        // 4. Section: Target Ibadah & Penyaluran ZISWAF
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("2. Program Perencanaan Ibadah & ZISWAF Aktif", 30f, y, paint)

        y += 14f

        paint.color = emeraldPrimary
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 16f), 4f, 4f, paint)
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Nama Program Target", 38f, y + 11f, paint)
        canvas.drawText("Jenis Ibadah", 210f, y + 11f, paint)
        canvas.drawText("Target", 330f, y + 11f, paint)
        canvas.drawText("Terkumpul", 420f, y + 11f, paint)
        canvas.drawText("Progres", 510f, y + 11f, paint)

        y += 20f

        if (state.ibadahGoals.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Belum ada target perencanaan ibadah/qurban yang dibuat.", 38f, y + 12f, paint)
            y += 22f
        } else {
            for ((idx, goal) in state.ibadahGoals.take(6).withIndex()) {
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                canvas.drawRect(RectF(30f, y, 565f, y + 16f), paint)

                paint.color = textDark
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(goal.title, 38f, y + 11f, paint)
                canvas.drawText(goal.type.title, 210f, y + 11f, paint)
                canvas.drawText("Rp ${formatRp(goal.targetAmount)}", 330f, y + 11f, paint)
                canvas.drawText("Rp ${formatRp(goal.currentAccumulated)}", 420f, y + 11f, paint)

                val pct = (goal.progressRatio * 100).toInt()
                paint.color = if (goal.isCompleted) emeraldDark else goldAccent
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("$pct%", 510f, y + 11f, paint)

                y += 16f
            }
        }

        // Footer
        paint.color = emeraldDark
        canvas.drawLine(30f, 790f, 565f, 790f, paint)

        paint.color = textMuted
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Dokumen ini disahkan oleh Infaqan Syariah untuk keperluan pelaporan ZISWAF kepada Amil Resmi (BAZNAS/LAZ).", 30f, 804f, paint)
        canvas.drawText("Semoga Allah SWT memberkahi harta yang dizakatkan dan menjadikannya pembersih jiwa.", 30f, 814f, paint)

        document.finishPage(page)

        return savePdfToFile(context, document, "Laporan_Zakat_Infaq")
    }

    /**
     * Menghasilkan Laporan Lengkap Buku Besar Jurnal (Comprehensive Ledger PDF)
     */
    fun generateComprehensiveLedgerPdf(
        context: Context,
        state: AmanahLedgerUiState,
        targetMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
        targetYear: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        val emeraldDark = Color.rgb(0, 77, 64)
        val emeraldPrimary = Color.rgb(0, 137, 123)
        val goldAccent = Color.rgb(218, 165, 32)
        val coralRed = Color.rgb(211, 47, 47)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = Color.rgb(90, 105, 110)
        val bgLight = Color.rgb(245, 248, 248)

        var y = 36f

        // 1. Header Banner
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 75f), 10f, 10f, paint)
        paint.color = goldAccent
        canvas.drawRect(RectF(30f, y + 71f, 565f, y + 75f), paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 17f
        canvas.drawText("BUKU BESAR JURNAL SYARIAH (LEDGER)", 48f, y + 26f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        paint.color = Color.rgb(220, 245, 242)
        canvas.drawText("Audit Trail Pembukuan Double-Entry, Saldo Akun & Rekonsiliasi Kas", 48f, y + 42f, paint)

        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, targetMonth)
            set(Calendar.YEAR, targetYear)
        }
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time)
        val monthStr = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)

        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("Periode: $monthStr ($hijriDate)", 48f, y + 60f, paint)

        y += 88f

        // 2. Wallets Summary
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("1. Daftar Rekening Kas & Kantong Amanah", 30f, y, paint)

        y += 14f

        paint.color = emeraldPrimary
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 16f), 4f, 4f, paint)
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Nama Kantong / Rekening", 38f, y + 11f, paint)
        canvas.drawText("Tipe Akun", 210f, y + 11f, paint)
        canvas.drawText("No. Rekening / Info", 330f, y + 11f, paint)
        canvas.drawText("Saldo Kas", 470f, y + 11f, paint)

        y += 20f

        for ((idx, w) in state.wallets.take(4).withIndex()) {
            val bal = state.getWalletBalance(w.id)
            paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
            canvas.drawRect(RectF(30f, y, 565f, y + 16f), paint)

            paint.color = textDark
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(w.name, 38f, y + 11f, paint)
            canvas.drawText(w.type.displayName, 210f, y + 11f, paint)
            canvas.drawText(w.accountNumber.ifBlank { "-" }, 330f, y + 11f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = emeraldDark
            canvas.drawText("Rp ${formatRp(bal)}", 470f, y + 11f, paint)

            y += 16f
        }

        y += 14f

        // 3. Transactions Double-Entry Detail
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("2. Mutasi Jurnal Berpasangan (Debit / Credit Trail)", 30f, y, paint)

        y += 14f

        paint.color = Color.rgb(60, 80, 85)
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 16f), 4f, 4f, paint)
        paint.color = Color.WHITE
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Tanggal", 38f, y + 11f, paint)
        canvas.drawText("Keterangan & Pos Akun", 120f, y + 11f, paint)
        canvas.drawText("Debit (Rp)", 380f, y + 11f, paint)
        canvas.drawText("Kredit (Rp)", 470f, y + 11f, paint)

        y += 20f

        val entries = state.journalEntries.take(10)
        val sdf = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))
        for ((idx, e) in entries.withIndex()) {
            paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
            canvas.drawRect(RectF(30f, y, 565f, y + 16f), paint)

            paint.color = textDark
            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(sdf.format(e.gregorianDate), 38f, y + 11f, paint)

            val desc = if (e.description.length > 36) e.description.take(34) + "..." else e.description
            canvas.drawText(desc, 120f, y + 11f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (e.transactionType == "INFLOW") emeraldDark else coralRed
            canvas.drawText(formatRp(e.totalDebit), 380f, y + 11f, paint)
            canvas.drawText(formatRp(e.totalCredit), 470f, y + 11f, paint)

            y += 16f
        }

        // Footer
        paint.color = emeraldDark
        canvas.drawLine(30f, 790f, 565f, 790f, paint)

        paint.color = textMuted
        paint.textSize = 7f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Buku Besar Jurnal Infaqan Syariah. Diuji kepatuhan keseimbangan debit dan kredit (Zero Balancing).", 30f, 804f, paint)

        document.finishPage(page)

        return savePdfToFile(context, document, "Buku_Besar_Ledger")
    }

    private fun drawSummaryCard(
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

    private fun savePdfToFile(context: Context, document: PdfDocument, baseName: String): File {
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

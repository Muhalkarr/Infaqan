package com.example.core.report

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.core.accounting.AccountCategory
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.state.AmanahLedgerUiState
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AnnualBaznasZakatPdfGenerator {

    fun generateAnnualBaznasZakatReportPdf(
        context: Context,
        state: AmanahLedgerUiState,
        muzakkiName: String = "Hamba Allah (Muzakki Amanah)"
    ): File {
        val document = PdfDocument()
        val session = FinancialPdfGenerator.PdfPageSession(
            document = document,
            documentTitle = "Lembar Penetapan Zakat Maal BAZNAS",
            subtitle = "Hisab Tahunan Berbasis SK BAZNAS & Multi-Wallet"
        )

        val canvas = session.canvas
        val paint = session.paint

        val emeraldDark = Color.rgb(0, 77, 64)
        val goldAccent = Color.rgb(218, 165, 32)
        val textDark = Color.rgb(20, 30, 32)
        val textMuted = Color.rgb(90, 105, 110)
        val bgCard = Color.rgb(238, 245, 243)
        val borderLight = Color.rgb(200, 220, 215)

        val cal = Calendar.getInstance()
        val hijriDate = HijriCalendarEngine.fromGregorian(cal.time)
        val printDateStr = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

        // 1. OFFICIAL BAZNAS-STYLE HEADER
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 78f), 8f, 8f, paint)
        paint.color = goldAccent
        canvas.drawRect(RectF(32f, session.y + 74f, 563f, session.y + 78f), paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("LEMBAR PENETAPAN HISAB ZAKAT MAAL TAHUNAN", 46f, session.y + 26f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = Color.rgb(220, 245, 242)
        canvas.drawText("STANDAR BADAN AMIL ZAKAT NASIONAL (BAZNAS) & DSN-MUI", 46f, session.y + 42f, paint)

        paint.color = goldAccent
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("Tahun Buku: ${hijriDate.year} H / ${cal.get(Calendar.YEAR)} M • Diterbitkan: $printDateStr", 46f, session.y + 62f, paint)

        session.y += 92f

        // 2. MUZAKKI IDENTITY & HAUL STATUS BOX
        paint.color = bgCard
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 55f), 6f, 6f, paint)
        paint.color = borderLight
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 55f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("IDENTITAS MUZAKKI & STATUS HAUL SYARIAH", 44f, session.y + 18f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        paint.color = textMuted
        canvas.drawText("Nama Muzakki : $muzakkiName", 44f, session.y + 32f, paint)
        canvas.drawText("Mata Uang Acuan : ${state.primaryCurrencyCode} (${state.primaryCurrencySymbol})", 44f, session.y + 45f, paint)

        val goldPrice = state.goldPricePerGram
        val nisab85g = goldPrice * 85.0
        canvas.drawText("Harga Emas BAZNAS : Rp ${nf.format(goldPrice.toLong())} / gram", 320f, session.y + 32f, paint)
        canvas.drawText("Nisab 85g Emas : Rp ${nf.format(nisab85g.toLong())}", 320f, session.y + 45f, paint)

        session.y += 68f

        // 3. MULTI-WALLET BREAKDOWN TABLE
        session.ensureSpace(160f)
        paint.color = emeraldDark
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 22f), 4f, 4f, paint)
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("RINCIAN HARTA KAS PER KANTONG / REKENING AKTIF", 44f, session.y + 15f, paint)
        canvas.drawText("SALDO BUKU", 380f, session.y + 15f, paint)
        canvas.drawText("ZAKAT (2.5%)", 480f, session.y + 15f, paint)

        session.y += 26f

        val assetAccounts = state.accounts.filter { it.category == AccountCategory.ASSET }
        var totalAssetAmount = 0.0

        for (acc in assetAccounts) {
            session.ensureSpace(20f)
            val balance = state.getAccountBalance(acc.id)
            totalAssetAmount += balance
            val zakatShare = if (balance > 0) balance * 0.025 else 0.0

            paint.color = textDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText("${acc.name} (${acc.code})", 44f, session.y + 13f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Rp ${nf.format(balance.toLong())}", 445f, session.y + 13f, paint)
            paint.color = emeraldDark
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Rp ${nf.format(zakatShare.toLong())}", 550f, session.y + 13f, paint)
            paint.textAlign = Paint.Align.LEFT

            paint.color = borderLight
            canvas.drawLine(32f, session.y + 18f, 563f, session.y + 18f, paint)
            session.y += 20f
        }

        // 4. TOTAL REKAPITULASI & KELAYAKAN
        session.ensureSpace(70f)
        val isEligible = totalAssetAmount >= nisab85g
        val totalZakatObligation = if (isEligible) totalAssetAmount * 0.025 else 0.0

        paint.color = if (isEligible) Color.rgb(230, 248, 243) else Color.rgb(255, 248, 235)
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 55f), 6f, 6f, paint)
        paint.color = if (isEligible) emeraldDark else goldAccent
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 55f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("TOTAL ASSET LIKUID : Rp ${nf.format(totalAssetAmount.toLong())}", 44f, session.y + 20f, paint)

        paint.textSize = 9f
        paint.color = if (isEligible) emeraldDark else textMuted
        canvas.drawText(
            if (isEligible) "STATUS: MELEWATI AMBANG NISAB 85G EMAS (WAJIB DITUNAIKAN)"
            else "STATUS: DI BAWAH BATAS NISAB 85G EMAS (DIANJURKAN INFAQ)",
            44f,
            session.y + 35f,
            paint
        )

        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 12f
        paint.color = if (isEligible) emeraldDark else textMuted
        canvas.drawText("Zakat Bersih: Rp ${nf.format(totalZakatObligation.toLong())}", 550f, session.y + 32f, paint)
        paint.textAlign = Paint.Align.LEFT

        session.y += 70f

        // 5. NIAT IKHLAS BERZAKAT & TANDA TANGAN
        session.ensureSpace(120f)
        paint.color = bgCard
        canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 40f), 6f, 6f, paint)
        paint.color = emeraldDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("Lafaz Niat Zakat Maal (HR. Bukhari & Muslim):", 44f, session.y + 16f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 8.5f
        paint.color = textDark
        canvas.drawText("نَوَيْتُ أَنْ أُخْرِجَ زَكَاةَ مَالِي فَرْضًا لِلَّهِ تَعَالَى — Aku berniat mengeluarkan zakat hartaku fardhu karena Allah Ta'ala.", 44f, session.y + 30f, paint)

        session.y += 50f

        // Signatures column
        session.ensureSpace(70f)
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Muzakki Pembayar Zakat,", 60f, session.y + 12f, paint)
        canvas.drawText("Petugas Amil Penerima (BAZNAS/LAZ),", 360f, session.y + 12f, paint)

        canvas.drawLine(60f, session.y + 50f, 180f, session.y + 50f, paint)
        canvas.drawLine(360f, session.y + 50f, 500f, session.y + 50f, paint)

        paint.textSize = 8f
        paint.color = textMuted
        canvas.drawText("( $muzakkiName )", 60f, session.y + 60f, paint)
        canvas.drawText("( Amil Terverifikasi BAZNAS )", 360f, session.y + 60f, paint)

        session.finish()

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val outputFile = File(outputDir, "Penetapan_Zakat_BAZNAS_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }
}

package com.example.core.report

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.JournalEntry
import com.example.core.ibadah.IbadahGoal
import com.example.core.qardh.QardhRecord
import com.example.core.wallet.WalletAccount
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentExporter {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))

    fun generateCsvReport(
        entries: List<JournalEntry>,
        wallets: List<WalletAccount>,
        accounts: List<Account>,
        qardhRecords: List<QardhRecord>,
        ibadahGoals: List<IbadahGoal>,
        getWalletBalance: (String) -> Double
    ): String {
        val sb = StringBuilder()
        // Header
        sb.append("LAPORAN KEUANGAN SYARIAH AMANAH LEDGER (INFAQAN)\n")
        sb.append("Tanggal Ekspor;${dateFormat.format(Date())}\n\n")

        // 1. Wallets
        sb.append("=== DAFTAR KANTONG / REKENING KAS ===\n")
        sb.append("Nama Kantong;Tipe;Saldo Saat Ini;No Rekening;Catatan\n")
        for (w in wallets) {
            val balance = getWalletBalance(w.id)
            sb.append("${w.name};${w.type.displayName};$balance;${w.accountNumber};${w.notes}\n")
        }
        sb.append("\n")

        // 2. Transactions
        sb.append("=== JURNAL TRANSAKSI & MUTASI ===\n")
        sb.append("Tanggal;ID Transaksi;Deskripsi;Tipe Transaksi;Total Nominal (Rp);Status\n")
        for (e in entries) {
            val dateStr = shortDateFormat.format(e.gregorianDate)
            val desc = e.description.replace(";", ",")
            val type = e.transactionType
            val amt = e.totalDebit
            sb.append("$dateStr;${e.id};$desc;$type;$amt;${if (e.isBalanced) "SEIMBANG" else "INVALID"}\n")
        }
        sb.append("\n")

        // 3. Qardh (Hutang Piutang)
        sb.append("=== CATATAN HUTANG PIUTANG (QARDHUL HASAN) ===\n")
        sb.append("Jenis;Nama Pihak;Total Pinjaman;Sisa Belum Lunas;Status;Saksi;Keterangan\n")
        for (q in qardhRecords) {
            sb.append("${q.type.title};${q.counterpartyName};${q.totalAmount};${q.remainingAmount};${q.status.displayName};${q.witnessName};${q.notes}\n")
        }
        sb.append("\n")

        // 4. Ibadah Goals
        sb.append("=== PERENCANAAN TARGET IBADAH ===\n")
        sb.append("Nama Target;Jenis;Target Nominal;Terkumpul;Progres (%);Status\n")
        for (g in ibadahGoals) {
            val pct = (g.progressRatio * 100).toInt()
            sb.append("${g.title};${g.type.title};${g.targetAmount};${g.currentAccumulated};$pct%;${if (g.isCompleted) "TERCAPAI" else "BERJALAN"}\n")
        }

        return sb.toString()
    }

    fun generatePdfReportFile(
        context: Context,
        entries: List<JournalEntry>,
        wallets: List<WalletAccount>,
        getWalletBalance: (String) -> Double,
        totalInfaqDisbursed: Double,
        totalIncome: Double,
        totalExpense: Double
    ): File {
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
        var currentPage = pdfDoc.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var pageNum = 1
        val maxY = 770f

        val paintTitle = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(20, 90, 50) // Syariah deep green
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSubtitle = Paint().apply {
            isAntiAlias = true
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintHeader = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(30, 41, 59)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintBody = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 8.5f
        }
        val paintGreen = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(16, 149, 106)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintLine = Paint().apply {
            isAntiAlias = true
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val paintFooter = Paint().apply {
            isAntiAlias = true
            color = Color.GRAY
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }

        var y = 40f

        fun drawFooterOnPage(c: Canvas, pageNumber: Int) {
            c.drawLine(40f, 792f, 555f, 792f, paintLine)
            val footerText = "\"Wahai orang-orang yang beriman, bertakwalah kepada Allah dan hendaklah setiap orang memperhatikan apa yang telah diperbuatnya untuk hari esok.\" (QS. Al-Hasyr: 18) • Hal. $pageNumber"
            c.drawText(footerText, 595f / 2f, 808f, paintFooter)
        }

        fun ensureSpace(neededHeight: Float, onNewPageHeader: ((Canvas, Float) -> Float)? = null) {
            if (y + neededHeight > maxY) {
                drawFooterOnPage(canvas, pageNum)
                pdfDoc.finishPage(currentPage)
                pageNum++
                currentPage = pdfDoc.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 40f

                // Running continuation header
                canvas.drawText("AMANAH LEDGER SYARIAH (Lanjutan - Halaman $pageNum)", 40f, y, paintTitle)
                y += 16f
                canvas.drawLine(40f, y, 555f, y, paintLine)
                y += 18f

                if (onNewPageHeader != null) {
                    y = onNewPageHeader(canvas, y)
                }
            }
        }

        // Bismillah & Header
        val paintBismillah = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(20, 90, 50)
            textSize = 12f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", 595f / 2f, y, paintBismillah)
        y += 24f

        canvas.drawText("LAPORAN KEUANGAN AMANAH LEDGER & ZISWAF", 40f, y, paintTitle)
        y += 16f
        canvas.drawText("Sistem Pembukuan Syariah Double-Entry • Dicetak pada: ${dateFormat.format(Date())}", 40f, y, paintSubtitle)
        y += 12f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 20f

        // Ringkasan Keuangan
        canvas.drawText("RINGKASAN ARUS KAS & AMANAH", 40f, y, paintHeader)
        y += 18f

        val totalNetCash = wallets.sumOf { getWalletBalance(it.id) }
        canvas.drawText("• Total Saldo Kas & Rekening:", 45f, y, paintBody)
        canvas.drawText(rupiahFormat.format(totalNetCash), 360f, y, paintGreen)
        y += 15f

        canvas.drawText("• Total Pendapatan Halal:", 45f, y, paintBody)
        canvas.drawText(rupiahFormat.format(totalIncome), 360f, y, paintBody)
        y += 15f

        canvas.drawText("• Total Belanja & Pengeluaran:", 45f, y, paintBody)
        canvas.drawText(rupiahFormat.format(totalExpense), 360f, y, paintBody)
        y += 15f

        canvas.drawText("• Total Infaq/Zakat Tersalurkan:", 45f, y, paintBody)
        canvas.drawText(rupiahFormat.format(totalInfaqDisbursed), 360f, y, paintGreen)
        y += 22f

        // Saldo Per Kantong (Seluruh kantong)
        ensureSpace(40f)
        canvas.drawText("DISTRIBUSI SALDO PER KANTONG", 40f, y, paintHeader)
        y += 16f
        for (w in wallets) {
            val bal = getWalletBalance(w.id)
            val walletLine = "- ${w.name} (${w.type.displayName}):"
            val wrappedName = FinancialPdfGenerator.wrapText(walletLine, paintBody, 300f)
            val itemHeight = 15f + (wrappedName.size - 1) * 11f
            ensureSpace(itemHeight)
            for (i in wrappedName.indices) {
                canvas.drawText(wrappedName[i], 45f, y + (i * 11f), paintBody)
            }
            canvas.drawText(rupiahFormat.format(bal), 360f, y, paintBody)
            y += itemHeight
        }
        y += 10f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 20f

        // Transaksi Mutasi & Jurnal
        fun drawTransactionsHeader(c: Canvas, topY: Float): Float {
            paintBody.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Tanggal", 45f, topY, paintBody)
            c.drawText("Keterangan & Rincian Transaksi", 125f, topY, paintBody)
            c.drawText("Tipe", 370f, topY, paintBody)
            c.drawText("Nominal", 460f, topY, paintBody)
            paintBody.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val nextY = topY + 8f
            c.drawLine(40f, nextY, 555f, nextY, paintLine)
            return nextY + 12f
        }

        ensureSpace(40f)
        canvas.drawText("MUTASI & JURNAL TRANSAKSI BUKU BESAR (${entries.size} Transaksi)", 40f, y, paintHeader)
        y += 18f
        y = drawTransactionsHeader(canvas, y)

        val sdf = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))
        for (e in entries) {
            val dt = sdf.format(e.gregorianDate)
            val descLines = FinancialPdfGenerator.wrapText(e.description, paintBody, 235f)
            val type = when (e.transactionType) {
                "INFLOW" -> "Pemasukan"
                "EXPENSE" -> "Beban"
                "INFAQ_PAYOUT" -> "Infaq"
                "TRANSFER" -> "Transfer"
                else -> e.transactionType
            }
            val amt = rupiahFormat.format(e.totalDebit)
            val rowHeight = 15f + (descLines.size - 1) * 11f

            ensureSpace(rowHeight) { c, topY -> drawTransactionsHeader(c, topY) }

            canvas.drawText(dt, 45f, y, paintBody)
            for (i in descLines.indices) {
                canvas.drawText(descLines[i], 125f, y + (i * 11f), paintBody)
            }
            canvas.drawText(type, 370f, y, paintBody)
            canvas.drawText(amt, 460f, y, paintBody)
            y += rowHeight
        }

        drawFooterOnPage(canvas, pageNum)
        pdfDoc.finishPage(currentPage)

        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(outputDir, "Laporan_AmanahLedger_${System.currentTimeMillis()}.pdf")
        val fos = FileOutputStream(pdfFile)
        pdfDoc.writeTo(fos)
        fos.close()
        pdfDoc.close()

        return pdfFile
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }
}

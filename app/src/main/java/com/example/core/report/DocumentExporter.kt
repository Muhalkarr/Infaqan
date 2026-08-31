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
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.rgb(20, 90, 50) // Syariah deep green
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintSubtitle = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val paintHeader = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintBody = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
        }
        val paintGreen = Paint().apply {
            color = Color.rgb(16, 149, 106)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintLine = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 40f

        // Bismillah & Header
        val paintBismillah = Paint().apply {
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

        // Saldo Per Kantong
        canvas.drawText("DISTRIBUSI SALDO PER KANTONG", 40f, y, paintHeader)
        y += 16f
        for (w in wallets.take(5)) {
            val bal = getWalletBalance(w.id)
            canvas.drawText("- ${w.name} (${w.type.displayName}):", 45f, y, paintBody)
            canvas.drawText(rupiahFormat.format(bal), 360f, y, paintBody)
            y += 14f
        }
        y += 10f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 20f

        // Transaksi Terakhir
        canvas.drawText("MUTASI & JURNAL TERBARU (${entries.size.coerceAtMost(10)} Transaksi)", 40f, y, paintHeader)
        y += 18f

        // Table Header
        paintBody.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Tanggal", 45f, y, paintBody)
        canvas.drawText("Keterangan", 130f, y, paintBody)
        canvas.drawText("Tipe", 340f, y, paintBody)
        canvas.drawText("Nominal", 460f, y, paintBody)
        paintBody.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 14f

        for (e in entries.take(12)) {
            val dt = SimpleDateFormat("dd/MM/yy", Locale("id", "ID")).format(e.gregorianDate)
            val desc = if (e.description.length > 25) e.description.take(22) + "..." else e.description
            val type = e.transactionType
            val amt = rupiahFormat.format(e.totalDebit)

            canvas.drawText(dt, 45f, y, paintBody)
            canvas.drawText(desc, 130f, y, paintBody)
            canvas.drawText(type, 340f, y, paintBody)
            canvas.drawText(amt, 460f, y, paintBody)
            y += 14f
            if (y > 780f) break
        }

        // Footer Dalil
        y = 800f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 16f
        val paintFooter = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("\"Wahai orang-orang yang beriman, bertakwalah kepada Allah dan hendaklah setiap orang memperhatikan apa yang telah diperbuatnya untuk hari esok.\" (QS. Al-Hasyr: 18)", 595f / 2f, y, paintFooter)

        pdfDoc.finishPage(page)

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

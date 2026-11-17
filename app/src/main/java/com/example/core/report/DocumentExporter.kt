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
import com.example.core.qardh.QardhType
import com.example.core.qardh.QardhStatus
import com.example.core.state.AmanahLedgerUiState
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
            val amt = e.primaryAmount
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

    /**
     * Menghasilkan teks CSV Laporan Kondisi Finansial Komprehensif
     * Mencakup Neraca Kekayaan Bersih, Rincian Seluruh Dompet, Buku Hutang, Buku Piutang,
     * serta Status Kewajiban Zakat & Infaq.
     */
    fun generateComprehensiveFinancialCsv(state: AmanahLedgerUiState): String {
        val sb = StringBuilder()
        val totalWalletBalance = state.totalWalletBalance
        val receivables = state.qardhRecords.filter { it.type == QardhType.PIUTANG_SAYA }
        val payables = state.qardhRecords.filter { it.type == QardhType.HUTANG_SAYA }
        val activeReceivables = receivables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }
        val activePayables = payables.filter { it.status == QardhStatus.AKTIF || it.status == QardhStatus.SEBAGIAN_LUNAS }
        val totalPiutang = activeReceivables.sumOf { it.remainingAmount }
        val totalHutang = activePayables.sumOf { it.remainingAmount }
        val netWorth = totalWalletBalance + totalPiutang - totalHutang

        sb.append("=== LAPORAN KONDISI FINANSIAL KOMPREHENSIF SYARIAH ===\n")
        sb.append("Aplikasi;Amanah Ledger (Infaqan Syariah)\n")
        sb.append("Nama Pemilik;${state.userNameKasMukmin}\n")
        sb.append("Tanggal Ekspor;${dateFormat.format(Date())}\n\n")

        // 1. Executive Summary / Neraca
        sb.append("=== RINGKASAN NERACA KEKAYAAN BERSIH (NET WORTH) ===\n")
        sb.append("Komponen Keuangan;Total Nominal (Rp);Keterangan\n")
        sb.append("Total Saldo Seluruh Dompet / Bank;${totalWalletBalance.toLong()};Dana likuid tersedia\n")
        sb.append("Total Piutang Kebajikan Aktif (Hak Tagih);${totalPiutang.toLong()};Dana dipinjamkan tanpa bunga\n")
        sb.append("Total Hutang Kewajiban Aktif;${totalHutang.toLong()};Kewajiban bayar yang belum lunas\n")
        sb.append("Kekayaan Bersih Hakiki (Net Worth);${netWorth.toLong()};Aset Bersih (Saldo + Piutang - Hutang)\n\n")

        // 2. Wallets
        sb.append("=== RINCIAN SALDO KANTONG KAS & REKENING BANK ===\n")
        sb.append("Nama Kantong;Tipe Akun;Nomor Rekening;Saldo Saat Ini (Rp);Catatan\n")
        for (w in state.wallets) {
            val balance = state.getWalletBalance(w.id)
            sb.append("${w.name};${w.type.displayName};'${w.accountNumber};${balance.toLong()};${w.notes.replace(";", ",")}\n")
        }
        sb.append("TOTAL SALDO KAS;;;${totalWalletBalance.toLong()};\n\n")

        // 3. Receivables (Piutang Qardhul Hasan)
        sb.append("=== BUKU PIUTANG KEBAJIKAN (QARDHUL HASAN DIBERIKAN) ===\n")
        sb.append("Nama Peminjam;Kontak;Total Dipinjamkan (Rp);Sisa Piutang (Rp);Jatuh Tempo;Status Akad;Saksi;Keterangan\n")
        if (receivables.isEmpty()) {
            sb.append("Tidak ada piutang yang tercatat;;;;;;;\n")
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            for (q in receivables) {
                val due = q.dueDateMillis?.let { sdf.format(Date(it)) } ?: "Tanpa Batas"
                sb.append("${q.counterpartyName};${q.contactInfo};${q.totalAmount.toLong()};${q.remainingAmount.toLong()};$due;${q.status.displayName};${q.witnessName};${q.notes.replace(";", ",")}\n")
            }
            sb.append("TOTAL PIUTANG AKTIF;;;${totalPiutang.toLong()};;;;\n")
        }
        sb.append("\n")

        // 4. Payables (Hutang Kewajiban Pengguna)
        sb.append("=== BUKU HUTANG KEWAJIBAN SYARIAH PENGGUNA ===\n")
        sb.append("Nama Pemberi Pinjaman;Kontak;Total Pokok Hutang (Rp);Sisa Hutang (Rp);Jatuh Tempo;Status Kewajiban;Saksi;Keterangan\n")
        if (payables.isEmpty()) {
            sb.append("Alhamdulillah tidak ada hutang yang tercatat;;;;;;;\n")
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            for (q in payables) {
                val due = q.dueDateMillis?.let { sdf.format(Date(it)) } ?: "Tanpa Batas"
                sb.append("${q.counterpartyName};${q.contactInfo};${q.totalAmount.toLong()};${q.remainingAmount.toLong()};$due;${q.status.displayName};${q.witnessName};${q.notes.replace(";", ",")}\n")
            }
            sb.append("TOTAL HUTANG KEWAJIBAN;;;${totalHutang.toLong()};;;;\n")
        }
        sb.append("\n")

        // 5. Zakat & Infaq Status
        sb.append("=== STATUS KEWAJIBAN ZAKAT & TABARRU' ===\n")
        val nisabValue = state.goldPricePerGram * 85.0
        val isZakatWajib = totalWalletBalance >= nisabValue
        val zakatEstimation = if (isZakatWajib) totalWalletBalance * 0.025 else 0.0
        sb.append("Parameter;Nilai;Satuan / Keterangan\n")
        sb.append("Harga Emas Acuan (per gram);${state.goldPricePerGram.toLong()};Rupiah\n")
        sb.append("Standar Nisab Emas (85 gram);${nisabValue.toLong()};Rupiah\n")
        sb.append("Status Nisab Kas Pengguna;${if (isZakatWajib) "MENCAPAI NISAB" else "BELUM MENCAPAI NISAB"};Kewajiban zakat jika haul genap\n")
        sb.append("Estimasi Zakat Maal (2.5%);${zakatEstimation.toLong()};Rupiah\n")
        sb.append("Titipan Saldo Infaq Vault;${state.virtualInfaqVaultBalance.toLong()};Rupiah\n\n")

        // 6. Catatan Syariah
        sb.append("=== CATATAN INTEGRITAS SYARIAH ===\n")
        sb.append("Dokumen ini dihasilkan secara otomatis oleh Amanah Ledger.\n")
        sb.append("Sistem mengacu pada prinsip akuntansi berpasangan (double-entry) dan bebas bunga riba (QS. Al-Baqarah: 275-282).\n")

        return sb.toString()
    }

    fun exportComprehensiveFinancialCsvFile(context: Context, state: AmanahLedgerUiState): File {
        val outputDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val csvFile = File(outputDir, "Laporan_Finansial_Komprehensif_${System.currentTimeMillis()}.csv")
        csvFile.writeText(generateComprehensiveFinancialCsv(state))
        return csvFile
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

    fun createExportFile(
        context: Context,
        content: String,
        prefix: String,
        extension: String
    ): File {
        val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val ext = if (extension.startsWith(".")) extension else ".$extension"
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(outputDir, "${prefix}_$timestamp$ext")
        val contentWithBom = if (ext.equals(".csv", ignoreCase = true) && !content.startsWith("\uFEFF")) {
            "\uFEFF$content"
        } else {
            content
        }
        file.writeText(contentWithBom, Charsets.UTF_8)
        return file
    }

    /**
     * Menyaring entri jurnal berdasarkan rentang tanggal milidetik.
     */
    fun filterJournalEntries(
        entries: List<JournalEntry>,
        startMillis: Long?,
        endMillis: Long?
    ): List<JournalEntry> {
        return entries.filter { entry ->
            val t = entry.gregorianDate.time
            val afterStart = startMillis == null || t >= startMillis
            val beforeEnd = endMillis == null || t <= endMillis
            afterStart && beforeEnd
        }
    }

    /**
     * Menghasilkan teks CSV Riwayat Transaksi Jurnal (Entri Jurnal) Terfilter.
     */
    fun generateTransactionHistoryCsv(
        entries: List<JournalEntry>,
        accounts: List<Account>,
        startDate: Date?,
        endDate: Date?
    ): String {
        val accountMap = accounts.associateBy { it.id }
        val sb = StringBuilder()
        sb.append("=== RIWAYAT TRANSAKSI JURNAL (AMANAH LEDGER) ===\n")
        sb.append("Tanggal Ekspor;${dateFormat.format(Date())}\n")
        val periodStr = when {
            startDate != null && endDate != null -> "${dateFormat.format(startDate)} s/d ${dateFormat.format(endDate)}"
            startDate != null -> "Sejak ${dateFormat.format(startDate)}"
            endDate != null -> "Hingga ${dateFormat.format(endDate)}"
            else -> "Semua Waktu"
        }
        sb.append("Rentang Periode;$periodStr\n")
        sb.append("Total Transaksi;${entries.size} entri\n\n")

        sb.append("Tanggal;Waktu;ID Transaksi;Deskripsi;Tipe Transaksi;Akun Debit;Akun Kredit;Nominal Debit (Rp);Nominal Kredit (Rp);Status Seimbang\n")
        val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
        val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

        for (e in entries) {
            val dStr = dateOnlyFormat.format(e.gregorianDate)
            val tStr = timeFormat.format(e.gregorianDate)
            val desc = e.description.replace(";", ",").replace("\n", " ")
            val debitAccounts = e.lines.filter { it.debit > 0 }.joinToString(" + ") { accountMap[it.accountId]?.name ?: it.accountId }
            val creditAccounts = e.lines.filter { it.credit > 0 }.joinToString(" + ") { accountMap[it.accountId]?.name ?: it.accountId }
            val debitAmt = e.primaryAmount.toLong()
            val creditAmt = e.totalCredit.toLong()
            val isBalancedStr = if (e.isBalanced) "SEIMBANG" else "TIDAK SEIMBANG"
            sb.append("$dStr;$tStr;${e.id};$desc;${e.transactionType};$debitAccounts;$creditAccounts;$debitAmt;$creditAmt;$isBalancedStr\n")
        }

        return sb.toString()
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Menghasilkan teks JSON Riwayat Transaksi Jurnal Terfilter.
     */
    fun generateTransactionHistoryJson(
        entries: List<JournalEntry>,
        accounts: List<Account>,
        startDate: Date?,
        endDate: Date?
    ): String {
        val accountMap = accounts.associateBy { it.id }
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val isoTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val sDateStr = startDate?.let { "\"${isoFormat.format(it)}\"" } ?: "null"
        val eDateStr = endDate?.let { "\"${isoFormat.format(it)}\"" } ?: "null"

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"report_type\": \"JOURNAL_TRANSACTION_HISTORY\",\n")
        sb.append("  \"app\": \"Amanah Ledger\",\n")
        sb.append("  \"exported_at\": \"$isoTimestamp\",\n")
        sb.append("  \"period\": {\n")
        sb.append("    \"start_date\": $sDateStr,\n")
        sb.append("    \"end_date\": $eDateStr\n")
        sb.append("  },\n")
        sb.append("  \"total_records\": ${entries.size},\n")
        sb.append("  \"transactions\": [\n")

        val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        for (i in entries.indices) {
            val e = entries[i]
            val isLast = (i == entries.size - 1)
            sb.append("    {\n")
            sb.append("      \"id\": \"${escapeJson(e.id)}\",\n")
            sb.append("      \"timestamp\": ${e.gregorianDate.time},\n")
            sb.append("      \"date_formatted\": \"${displayFormat.format(e.gregorianDate)}\",\n")
            sb.append("      \"description\": \"${escapeJson(e.description)}\",\n")
            sb.append("      \"transaction_type\": \"${e.transactionType}\",\n")
            sb.append("      \"total_amount\": ${e.primaryAmount.toLong()},\n")
            sb.append("      \"is_balanced\": ${e.isBalanced},\n")
            sb.append("      \"journal_lines\": [\n")

            for (j in e.lines.indices) {
                val l = e.lines[j]
                val lineLast = (j == e.lines.size - 1)
                val accName = accountMap[l.accountId]?.name ?: l.accountId
                sb.append("        {\n")
                sb.append("          \"account_id\": \"${escapeJson(l.accountId)}\",\n")
                sb.append("          \"account_name\": \"${escapeJson(accName)}\",\n")
                sb.append("          \"debit\": ${l.debit.toLong()},\n")
                sb.append("          \"credit\": ${l.credit.toLong()}\n")
                sb.append("        }${if (lineLast) "" else ","}\n")
            }
            sb.append("      ]\n")
            sb.append("    }${if (isLast) "" else ","}\n")
        }

        sb.append("  ]\n")
        sb.append("}")
        return sb.toString()
    }

    /**
     * Menghasilkan teks CSV Laporan Ringkasan Finansial Kategori & Arus Kas.
     */
    fun generateFinancialSummaryCsv(
        state: AmanahLedgerUiState,
        startDate: Date?,
        endDate: Date?
    ): String {
        val filteredEntries = filterJournalEntries(state.journalEntries, startDate?.time, endDate?.time)
        val sb = StringBuilder()
        sb.append("=== LAPORAN RINGKASAN FINANSIAL KATEGORI (AMANAH LEDGER) ===\n")
        sb.append("Tanggal Ekspor;${dateFormat.format(Date())}\n")
        val periodStr = when {
            startDate != null && endDate != null -> "${dateFormat.format(startDate)} s/d ${dateFormat.format(endDate)}"
            startDate != null -> "Sejak ${dateFormat.format(startDate)}"
            endDate != null -> "Hingga ${dateFormat.format(endDate)}"
            else -> "Semua Waktu"
        }
        sb.append("Rentang Periode;$periodStr\n")
        sb.append("Nama Pemilik;${state.userNameKasMukmin}\n\n")

        // 1. Rekapitulasi Pendapatan
        var totalIncomeKasab = 0.0
        var totalIncomeNonKasab = 0.0
        for (entry in filteredEntries) {
            if (entry.transactionType == "INFLOW") {
                for (line in entry.lines) {
                    val acc = state.getAccount(line.accountId)
                    if (acc?.category == AccountCategory.INCOME_KASAB) totalIncomeKasab += line.credit
                    if (acc?.category == AccountCategory.INCOME_NON_KASAB) totalIncomeNonKasab += line.credit
                }
            }
        }
        val totalIncome = totalIncomeKasab + totalIncomeNonKasab

        sb.append("=== 1. REKAPITULASI PENDAPATAN (INFLOW) ===\n")
        sb.append("Kategori Pendapatan;Nominal (Rp);Keterangan\n")
        sb.append("Pendapatan Kasab (Gaji & Usaha Halal);${totalIncomeKasab.toLong()};Hasil ikhtiar/pekerjaan langsung\n")
        sb.append("Pendapatan Non-Kasab (Hadiah/Bagi Hasil);${totalIncomeNonKasab.toLong()};Pemberian, warisan, atau pasif\n")
        sb.append("TOTAL PENDAPATAN;${totalIncome.toLong()};Total penerimaan periode ini\n\n")

        // 2. Rincian Pengeluaran Berdasarkan Kategori Akun
        val expenseByCategory = mutableMapOf<String, Double>()
        var totalInfaqDisbursed = 0.0
        for (entry in filteredEntries) {
            if (entry.transactionType == "EXPENSE") {
                for (line in entry.lines) {
                    if (line.debit > 0.0 && line.accountId != "acc_vault" && line.accountId != "acc_disbursed") {
                        val accName = state.getAccount(line.accountId)?.name ?: line.accountId
                        expenseByCategory[accName] = (expenseByCategory[accName] ?: 0.0) + line.debit
                    }
                }
            } else if (entry.transactionType == "INFAQ_PAYOUT") {
                totalInfaqDisbursed += entry.totalDebit
            }
        }
        val totalConsumption = expenseByCategory.values.sum()
        val totalOverallExpense = totalConsumption + totalInfaqDisbursed

        sb.append("=== 2. RINCIAN PENGELUARAN & BEBAN (OUTFLOW) ===\n")
        sb.append("Kategori Beban/Pengeluaran;Nominal (Rp);Persentase (%)\n")
        for ((cat, amt) in expenseByCategory.entries.sortedByDescending { it.value }) {
            val pct = if (totalConsumption > 0) ((amt / totalConsumption) * 100).toInt() else 0
            sb.append("$cat;${amt.toLong()};$pct%\n")
        }
        sb.append("Subtotal Belanja & Kebutuhan;${totalConsumption.toLong()};100%\n")
        sb.append("Penyaluran Infaq & Zakat;${totalInfaqDisbursed.toLong()};Tabarru'\n")
        sb.append("TOTAL KESELURUHAN PENGELUARAN;${totalOverallExpense.toLong()};\n\n")

        // 3. Arus Kas Bersih (Net Cash Flow)
        val netCashFlow = totalIncome - totalOverallExpense
        sb.append("=== 3. ARUS KAS BERSIH (NET CASH FLOW) ===\n")
        sb.append("Indikator;Nominal (Rp);Status\n")
        sb.append("Arus Kas Bersih (Net Flow);${netCashFlow.toLong()};${if (netCashFlow >= 0) "SURPLUS (HEMAT)" else "DEFISIT"}\n\n")

        // 4. Saldo Kas & Posisi Aset Saat Ini
        sb.append("=== 4. POSISI SALDO KANTONG KAS SAAT INI ===\n")
        sb.append("Nama Kantong;Tipe Akun;Saldo Saat Ini (Rp)\n")
        for (w in state.wallets) {
            val bal = state.getWalletBalance(w.id)
            sb.append("${w.name};${w.type.displayName};${bal.toLong()}\n")
        }
        sb.append("TOTAL SALDO KAS;;${state.totalWalletBalance.toLong()}\n\n")

        // 5. Status Zakat Maal & Nisab
        val nisab = state.nisabThreshold
        val totalAssets = state.totalAssets
        val isNisab = state.isNisabReached
        sb.append("=== 5. HISAB NISAB ZAKAT MAAL ===\n")
        sb.append("Indikator Zakat;Nilai\n")
        sb.append("Total Harta Aset Terdata;Rp ${totalAssets.toLong()}\n")
        sb.append("Batas Nisab 85g Emas;Rp ${nisab.toLong()}\n")
        sb.append("Status Nisab;${if (isNisab) "MENCAPAI NISAB (Wajib Zakat jika Haul)" else "BELUM MENCAPAI NISAB"}\n")

        return sb.toString()
    }

    /**
     * Menghasilkan teks JSON Laporan Ringkasan Finansial Kategori & Arus Kas.
     */
    fun generateFinancialSummaryJson(
        state: AmanahLedgerUiState,
        startDate: Date?,
        endDate: Date?
    ): String {
        val filteredEntries = filterJournalEntries(state.journalEntries, startDate?.time, endDate?.time)
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val isoTimestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        val sDateStr = startDate?.let { "\"${isoFormat.format(it)}\"" } ?: "null"
        val eDateStr = endDate?.let { "\"${isoFormat.format(it)}\"" } ?: "null"

        // 1. Income summary
        var totalIncomeKasab = 0.0
        var totalIncomeNonKasab = 0.0
        for (entry in filteredEntries) {
            if (entry.transactionType == "INFLOW") {
                for (line in entry.lines) {
                    val acc = state.getAccount(line.accountId)
                    if (acc?.category == AccountCategory.INCOME_KASAB) totalIncomeKasab += line.credit
                    if (acc?.category == AccountCategory.INCOME_NON_KASAB) totalIncomeNonKasab += line.credit
                }
            }
        }
        val totalIncome = totalIncomeKasab + totalIncomeNonKasab

        // 2. Expense categories
        val expenseByCategory = mutableMapOf<String, Double>()
        var totalInfaqDisbursed = 0.0
        for (entry in filteredEntries) {
            if (entry.transactionType == "EXPENSE") {
                for (line in entry.lines) {
                    if (line.debit > 0.0 && line.accountId != "acc_vault" && line.accountId != "acc_disbursed") {
                        val accName = state.getAccount(line.accountId)?.name ?: line.accountId
                        expenseByCategory[accName] = (expenseByCategory[accName] ?: 0.0) + line.debit
                    }
                }
            } else if (entry.transactionType == "INFAQ_PAYOUT") {
                totalInfaqDisbursed += entry.totalDebit
            }
        }
        val totalConsumption = expenseByCategory.values.sum()
        val totalOverallExpense = totalConsumption + totalInfaqDisbursed
        val netCashFlow = totalIncome - totalOverallExpense

        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"report_type\": \"FINANCIAL_CATEGORY_SUMMARY\",\n")
        sb.append("  \"app\": \"Amanah Ledger\",\n")
        sb.append("  \"owner_name\": \"${escapeJson(state.userNameKasMukmin)}\",\n")
        sb.append("  \"exported_at\": \"$isoTimestamp\",\n")
        sb.append("  \"period\": {\n")
        sb.append("    \"start_date\": $sDateStr,\n")
        sb.append("    \"end_date\": $eDateStr\n")
        sb.append("  },\n")
        sb.append("  \"income_summary\": {\n")
        sb.append("    \"income_kasab\": ${totalIncomeKasab.toLong()},\n")
        sb.append("    \"income_non_kasab\": ${totalIncomeNonKasab.toLong()},\n")
        sb.append("    \"total_income\": ${totalIncome.toLong()}\n")
        sb.append("  },\n")
        sb.append("  \"expense_summary\": {\n")
        sb.append("    \"subtotal_consumption\": ${totalConsumption.toLong()},\n")
        sb.append("    \"infaq_disbursed\": ${totalInfaqDisbursed.toLong()},\n")
        sb.append("    \"total_expenses\": ${totalOverallExpense.toLong()},\n")
        sb.append("    \"categories\": [\n")

        val sortedExp = expenseByCategory.entries.sortedByDescending { it.value }
        for (i in sortedExp.indices) {
            val item = sortedExp[i]
            val isLast = (i == sortedExp.size - 1)
            val pct = if (totalConsumption > 0) ((item.value / totalConsumption) * 100).toInt() else 0
            sb.append("      {\n")
            sb.append("        \"category_name\": \"${escapeJson(item.key)}\",\n")
            sb.append("        \"amount\": ${item.value.toLong()},\n")
            sb.append("        \"percentage\": $pct\n")
            sb.append("      }${if (isLast) "" else ","}\n")
        }
        sb.append("    ]\n")
        sb.append("  },\n")
        sb.append("  \"cash_flow\": {\n")
        sb.append("    \"net_cash_flow\": ${netCashFlow.toLong()},\n")
        sb.append("    \"status\": \"${if (netCashFlow >= 0) "SURPLUS" else "DEFICIT"}\"\n")
        sb.append("  },\n")
        sb.append("  \"wallet_balances\": [\n")
        for (i in state.wallets.indices) {
            val w = state.wallets[i]
            val isLast = (i == state.wallets.size - 1)
            val bal = state.getWalletBalance(w.id)
            sb.append("    {\n")
            sb.append("      \"wallet_id\": \"${escapeJson(w.id)}\",\n")
            sb.append("      \"name\": \"${escapeJson(w.name)}\",\n")
            sb.append("      \"type\": \"${escapeJson(w.type.displayName)}\",\n")
            sb.append("      \"balance\": ${bal.toLong()}\n")
            sb.append("    }${if (isLast) "" else ","}\n")
        }
        sb.append("  ],\n")
        sb.append("  \"total_wallet_balance\": ${state.totalWalletBalance.toLong()},\n")
        sb.append("  \"zakat_maal_status\": {\n")
        sb.append("    \"total_assets\": ${state.totalAssets.toLong()},\n")
        sb.append("    \"gold_price_per_gram\": ${state.goldPricePerGram.toLong()},\n")
        sb.append("    \"nisab_threshold\": ${state.nisabThreshold.toLong()},\n")
        sb.append("    \"is_nisab_reached\": ${state.isNisabReached}\n")
        sb.append("  }\n")
        sb.append("}")
        return sb.toString()
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


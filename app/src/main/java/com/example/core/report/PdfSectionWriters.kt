package com.example.core.report

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.core.accounting.JournalEntry
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.infaq.InfaqCalculationType
import com.example.core.state.AmanahLedgerUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Kumpulan modul penulis bagian laporan PDF terstandarisasi A4.
 * Digunakan bersama oleh Laporan Bulanan, Laporan Zakat/Infaq, Buku Besar,
 * Neraca Komprehensif, dan Laporan Utama All-in-One agar tidak ada data yang terlewat.
 */
object PdfSectionWriters {

    fun drawSectionTitle(
        session: FinancialPdfGenerator.PdfPageSession,
        title: String,
        emeraldDark: Int
    ) {
        session.ensureSpace(34f)
        session.paint.color = emeraldDark
        session.paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        session.paint.textSize = 10.5f
        session.canvas.drawText(title, 32f, session.y, session.paint)
        session.y += 14f
    }

    /**
     * Rincian Pemasukan Kasab vs Non-Kasab, Pengeluaran per Kategori & Tabungan Bersih.
     */
    fun drawCashflowKasabAndExpenseSection(
        session: FinancialPdfGenerator.PdfPageSession,
        state: AmanahLedgerUiState,
        sectionTitle: String,
        emeraldDark: Int,
        emeraldPrimary: Int,
        coralRed: Int,
        goldAccent: Int,
        textDark: Int,
        textMuted: Int,
        bgLight: Int,
        editorialConfig: PdfEditorialConfig = state.pdfEditorialConfig,
        scopedData: PdfScopedData? = null
    ) {
        drawSectionTitle(session, sectionTitle, emeraldDark)

        val kasabLabel = editorialConfig.resolveKasabLabel()
        val nonKasabLabel = editorialConfig.resolveNonKasabLabel()
        val expenseLabel = editorialConfig.resolveExpenseLabel()
        val netSavingsLabel = editorialConfig.resolveNetSavingsLabel()

        val effIncomeKasab = scopedData?.totalIncomeKasab ?: state.totalIncomeKasab
        val effIncomeNonKasab = scopedData?.totalIncomeNonKasab ?: state.totalIncomeNonKasab
        val effConsumption = scopedData?.totalConsumptionExpense ?: state.totalConsumptionExpense
        val effNetSavings = scopedData?.netSavingsAmount ?: state.netSavingsAmount

        // 1. Kartu ringkas 4 angka arus kas
        session.ensureSpace(58f)
        val paint = session.paint
        paint.color = bgLight
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 46f), 6f, 6f, paint)
        val colW = 126f
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f, session.y,
            kasabLabel.uppercase(Locale("id", "ID")).take(22), "Rp ${FinancialPdfGenerator.formatRp(effIncomeKasab)}", emeraldDark
        )
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f + colW + 6f, session.y,
            nonKasabLabel.uppercase(Locale("id", "ID")).take(22), "Rp ${FinancialPdfGenerator.formatRp(effIncomeNonKasab)}", emeraldPrimary
        )
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f + (colW + 6f) * 2, session.y,
            expenseLabel.uppercase(Locale("id", "ID")).take(22), "Rp ${FinancialPdfGenerator.formatRp(effConsumption)}", coralRed
        )
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f + (colW + 6f) * 3, session.y,
            netSavingsLabel.uppercase(Locale("id", "ID")).take(22), "Rp ${FinancialPdfGenerator.formatRp(effNetSavings)}",
            if (effNetSavings >= 0) goldAccent else coralRed
        )
        session.y += 56f

        val kasabList = scopedData?.kasabBreakdown ?: state.getKasabBreakdown()
        val nonKasabList = scopedData?.nonKasabBreakdown ?: state.getNonKasabBreakdown()
        val expenseList = scopedData?.expenseBreakdown ?: state.getExpenseBreakdown()
        val totalAllIncome = (effIncomeKasab + effIncomeNonKasab).coerceAtLeast(1.0)
        val totalExp = effConsumption.coerceAtLeast(1.0)

        fun drawCategoryHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Kelompok", 40f, topY + 12f, paint)
            c.drawText("Nama Akun / Pos Arus Dana", 130f, topY + 12f, paint)
            c.drawText("Porsi", 385f, topY + 12f, paint)
            c.drawText("Total Nominal (Rp)", 455f, topY + 12f, paint)
            return topY + 22f
        }

        val rows = buildList {
            for ((acc, amount) in kasabList) {
                val pct = String.format(Locale("id", "ID"), "%.1f%%", (amount / totalAllIncome) * 100.0)
                add(Triple(kasabLabel.take(16), "${acc.code} - ${acc.name}", Pair(pct, amount)))
            }
            for ((acc, amount) in nonKasabList) {
                val pct = String.format(Locale("id", "ID"), "%.1f%%", (amount / totalAllIncome) * 100.0)
                add(Triple(nonKasabLabel.take(16), "${acc.code} - ${acc.name}", Pair(pct, amount)))
            }
            for ((acc, amount) in expenseList) {
                val pct = String.format(Locale("id", "ID"), "%.1f%%", (amount / totalExp) * 100.0)
                add(Triple(expenseLabel.take(16), "${acc.code} - ${acc.name}", Pair(pct, amount)))
            }
        }

        session.ensureSpace(36f)
        session.y = drawCategoryHeader(session.canvas, session.y)

        if (rows.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada rincian pemasukan maupun pengeluaran pada periode ini.", 40f, session.y + 12f, paint)
            session.y += 24f
        } else {
            val kasabTag = kasabLabel.take(16)
            val nonKasabTag = nonKasabLabel.take(16)
            for ((idx, row) in rows.withIndex()) {
                val (groupLabel, accName, metric) = row
                val (pctStr, nominal) = metric
                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val nameLines = FinancialPdfGenerator.wrapText(accName, paint, 240f)
                val rowHeight = 16f + (nameLines.size - 1) * 11f + 4f

                session.ensureSpace(rowHeight) { c, topY -> drawCategoryHeader(c, topY) }

                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + rowHeight), paint)

                paint.color = when (groupLabel) {
                    kasabTag -> emeraldDark
                    nonKasabTag -> emeraldPrimary
                    else -> coralRed
                }
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                session.canvas.drawText(groupLabel, 40f, session.y + 11f, paint)

                paint.color = textDark
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                for (i in nameLines.indices) {
                    session.canvas.drawText(nameLines[i], 130f, session.y + 11f + (i * 11f), paint)
                }

                paint.color = textMuted
                session.canvas.drawText(pctStr, 385f, session.y + 11f, paint)

                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = if (groupLabel != kasabTag && groupLabel != nonKasabTag) coralRed else emeraldDark
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(nominal)}", 455f, session.y + 11f, paint)

                session.y += rowHeight
            }
            session.y += 12f
        }
    }

    /**
     * Analisa Spiritual Liquidity Index (SLI) Komprehensif:
     * Membedah perbedaan SLI berbasis konsumsi vs % total pendapatan serta audit target aturan infaq.
     */
    fun drawComprehensiveSliAnalysisSection(
        session: FinancialPdfGenerator.PdfPageSession,
        state: AmanahLedgerUiState,
        sectionTitle: String,
        emeraldDark: Int,
        emeraldPrimary: Int,
        goldAccent: Int,
        coralRed: Int,
        textDark: Int,
        textMuted: Int,
        bgLight: Int,
        bgCard: Int,
        scopedData: PdfScopedData? = null
    ) {
        drawSectionTitle(session, sectionTitle, emeraldDark)
        val sli = scopedData?.sliAnalysis ?: state.infaqSliAnalysis
        val paint = session.paint
        fun fmtPct(v: Double): String = String.format(Locale("id", "ID"), "%.2f%%", v)

        data class PdfTextLine(val lines: List<String>, val color: Int, val bold: Boolean, val italic: Boolean = false, val indent: Float = 48f, val stepY: Float = 11.5f)

        paint.textSize = 7.8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val maxW = 502f

        val entries = listOf(
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText("A. Perbandingan Rasio Kedermawanan (Konsumsi vs Total Pendapatan):", paint, maxW),
                color = emeraldDark, bold = true, indent = 44f
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• SLI Berbasis Konsumsi: ${fmtPct(sli.sliConsumptionBasedPercent)} (Infaq Vault Rp ${FinancialPdfGenerator.formatRp(sli.totalAllocatedToVault)} / Belanja Rp ${FinancialPdfGenerator.formatRp(sli.totalConsumptionExpense)})",
                    paint, maxW
                ),
                color = goldAccent, bold = true
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Rasio Infaq Vault vs Total Pendapatan (Kasab + Non-Kasab): ${fmtPct(sli.infaqToTotalIncomePercent)} (Rp ${FinancialPdfGenerator.formatRp(sli.totalAllocatedToVault)} / Rp ${FinancialPdfGenerator.formatRp(sli.totalAllIncome)})",
                    paint, maxW
                ),
                color = emeraldDark, bold = true
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Rasio Seluruh Kanal (Vault + Sedekah Subuh + Ekstra) / Pendapatan: ${fmtPct(sli.allChannelsToTotalIncomePercent)} (Total Rp ${FinancialPdfGenerator.formatRp(sli.totalInfaqAllChannels)})",
                    paint, maxW
                ),
                color = emeraldPrimary, bold = false
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText("B. Sumber Pembentuk Infaq & Audit Kewajiban Aturan Aktif:", paint, maxW),
                color = emeraldDark, bold = true, indent = 44f
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Masuk Vault: Potong Pemasukan Rp ${FinancialPdfGenerator.formatRp(sli.infaqFromIncomeAllocation)} | Round-Up Rp ${FinancialPdfGenerator.formatRp(sli.infaqFromExpenseRoundUp)} | Rezeki Non-Tunai Rp ${FinancialPdfGenerator.formatRp(sli.infaqFromNonCashRezekiVault)}",
                    paint, maxW
                ),
                color = textDark, bold = false
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Kanal Langsung: Sedekah Subuh Rp ${FinancialPdfGenerator.formatRp(sli.directSedekahSubuhAmount)} | Infaq Ekstra Rp ${FinancialPdfGenerator.formatRp(sli.directExtraAndRezekiInfaqAmount)} | Zakat Rp ${FinancialPdfGenerator.formatRp(sli.zakatDisbursedAmount)}",
                    paint, maxW
                ),
                color = textDark, bold = false
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Target Aturan Aktif (Kasab ${fmtPct(sli.kasabRuleRatePercent)}, Non-Kasab ${fmtPct(sli.nonKasabRuleRatePercent)}, Blended ${fmtPct(sli.expectedBlendedIncomeRatePercent)}): Rp ${FinancialPdfGenerator.formatRp(sli.expectedTotalInfaqFromIncomeRules)}",
                    paint, maxW
                ),
                color = emeraldDark, bold = false
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Sisa Vault Belum Disalurkan: Rp ${FinancialPdfGenerator.formatRp(sli.vaultRemainingPayable)} | Kekurangan Alokasi Aturan: Rp ${FinancialPdfGenerator.formatRp(sli.underAllocationShortfall)}",
                    paint, maxW
                ),
                color = textDark, bold = false
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Total Kewajiban Infaq Perlu Ditunaikan (Sisa Vault + Kekurangan Target): Rp ${FinancialPdfGenerator.formatRp(sli.combinedMustPayAmount)}",
                    paint, maxW
                ),
                color = if (sli.combinedMustPayAmount > 0.0) coralRed else emeraldPrimary, bold = true
            ),
            PdfTextLine(
                lines = FinancialPdfGenerator.wrapText(
                    "Catatan: SLI mengukur infaq terhadap belanja konsumsi. Jika pendapatan melebihi belanja (ada tabungan), persentase terhadap total pendapatan secara matematis lebih kecil daripada SLI konsumsi.",
                    paint, maxW
                ),
                color = textMuted, bold = false, italic = true, stepY = 10f
            )
        )

        val contentH = entries.sumOf { (it.lines.size * it.stepY + 2.0) }.toFloat()
        val boxHeight = contentH + 18f
        session.ensureSpace(boxHeight + 10f)

        paint.color = bgCard
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + boxHeight), 6f, 6f, paint)
        paint.color = goldAccent
        session.canvas.drawRoundRect(RectF(32f, session.y, 36f, session.y + boxHeight), 2f, 2f, paint)

        var lineY = session.y + 15f
        for (item in entries) {
            paint.color = item.color
            paint.textSize = if (item.italic) 7.2f else 7.8f
            paint.typeface = when {
                item.bold -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                item.italic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                else -> Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            for (l in item.lines) {
                session.canvas.drawText(l, item.indent, lineY, paint)
                lineY += item.stepY
            }
            lineY += 2f
        }

        session.y += boxHeight + 14f
    }

    /**
     * Rincian Zakat Profesi (Penghasilan) & Zakat Perniagaan (Usaha/Bisnis).
     */
    fun drawZakatProfesiAndPerniagaanSection(
        session: FinancialPdfGenerator.PdfPageSession,
        state: AmanahLedgerUiState,
        sectionTitle: String,
        emeraldDark: Int,
        emeraldPrimary: Int,
        goldAccent: Int,
        coralRed: Int,
        textDark: Int,
        textMuted: Int,
        bgLight: Int
    ) {
        drawSectionTitle(session, sectionTitle, emeraldDark)
        val paint = session.paint
        val zp = state.zakatProfesi
        val zpn = state.zakatPerniagaan

        data class ZakatLine(val lines: List<String>, val color: Int, val bold: Boolean, val indent: Float = 48f)

        paint.textSize = 7.8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val maxW = 502f

        val rows = listOf(
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "A. Zakat Profesi / Penghasilan (Metode ${if (zp.isNetMethod) "Neto" else "Bruto"} • Nisab 524 kg Beras @ Rp ${FinancialPdfGenerator.formatRp(zp.ricePricePerKg)}):",
                    paint, maxW
                ),
                color = emeraldDark, bold = true, indent = 44f
            ),
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Gaji Pokok Rp ${FinancialPdfGenerator.formatRp(zp.monthlyGrossSalary)} + Tunjangan/Bonus Rp ${FinancialPdfGenerator.formatRp(zp.monthlyAllowanceAndOther)} = Bruto Rp ${FinancialPdfGenerator.formatRp(zp.totalIncome)}",
                    paint, maxW
                ),
                color = textDark, bold = false
            ),
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Pengurang (Kebutuhan Rp ${FinancialPdfGenerator.formatRp(zp.monthlyEssentialExpenses)} + Cicilan Rp ${FinancialPdfGenerator.formatRp(zp.monthlyDebtInstallment)}) | Dasar Zakat: Rp ${FinancialPdfGenerator.formatRp(zp.taxableBase)}",
                    paint, maxW
                ),
                color = textDark, bold = false
            ),
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Ambang Nisab: Rp ${FinancialPdfGenerator.formatRp(zp.nisabMonthly)} -> Status: ${if (zp.isObligatory) "WAJIB ZAKAT" else "Belum Nisab"} | Zakat Profesi (2,5%): Rp ${FinancialPdfGenerator.formatRp(zp.zakatAmount)}",
                    paint, maxW
                ),
                color = if (zp.isObligatory) emeraldPrimary else textMuted, bold = true
            ),
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "B. Zakat Perniagaan / Usaha (Tarif ${if (zpn.isLunarCalendar) "2,5% Hijriah" else "2,577% Masehi"} • Nisab 85g Emas):",
                    paint, maxW
                ),
                color = emeraldDark, bold = true, indent = 44f
            ),
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Kas Bisnis Rp ${FinancialPdfGenerator.formatRp(zpn.currentCashInBusiness)} + Persediaan/Stok Rp ${FinancialPdfGenerator.formatRp(zpn.inventoryValue)} + Piutang Lancar Rp ${FinancialPdfGenerator.formatRp(zpn.receivableCollectible)}",
                    paint, maxW
                ),
                color = textDark, bold = false
            ),
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Total Aset Lancar Rp ${FinancialPdfGenerator.formatRp(zpn.totalCurrentAssets)} - Hutang Jatuh Tempo Rp ${FinancialPdfGenerator.formatRp(zpn.shortTermPayable)} = Bersih Rp ${FinancialPdfGenerator.formatRp(zpn.netTaxableAssets)}",
                    paint, maxW
                ),
                color = textDark, bold = false
            ),
            ZakatLine(
                lines = FinancialPdfGenerator.wrapText(
                    "• Ambang Nisab: Rp ${FinancialPdfGenerator.formatRp(zpn.nisabGold85g)} -> Status: ${if (zpn.isObligatory) "WAJIB ZAKAT" else "Belum Nisab"} | Zakat Perniagaan: Rp ${FinancialPdfGenerator.formatRp(zpn.zakatAmount)}",
                    paint, maxW
                ),
                color = if (zpn.isObligatory) goldAccent else coralRed, bold = true
            )
        )

        val boxH = rows.sumOf { it.lines.size * 11.5 + 3.0 }.toFloat() + 16f
        session.ensureSpace(boxH + 10f)
        paint.color = bgLight
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + boxH), 6f, 6f, paint)

        var lineY = session.y + 15f
        paint.textSize = 7.8f
        for ((idx, item) in rows.withIndex()) {
            if (idx == 4) {
                paint.color = Color.rgb(205, 215, 213)
                session.canvas.drawLine(44f, lineY - 4f, 550f, lineY - 4f, paint)
                lineY += 4f
            }
            paint.color = item.color
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (item.bold) Typeface.BOLD else Typeface.NORMAL)
            for (l in item.lines) {
                session.canvas.drawText(l, item.indent, lineY, paint)
                lineY += 11.5f
            }
            lineY += 2.5f
        }

        session.y += boxH + 14f
    }

    /**
     * Aktivitas & Streak Sedekah Subuh Harian + Lencana + Riwayat Tanggal.
     */
    fun drawSedekahSubuhSection(
        session: FinancialPdfGenerator.PdfPageSession,
        state: AmanahLedgerUiState,
        sectionTitle: String,
        emeraldDark: Int,
        emeraldPrimary: Int,
        goldAccent: Int,
        textDark: Int,
        textMuted: Int,
        bgLight: Int,
        scopedData: PdfScopedData? = null
    ) {
        drawSectionTitle(session, sectionTitle, emeraldDark)
        val subuh = scopedData?.filteredSubuhState ?: state.sedekahSubuhState
        val paint = session.paint

        session.ensureSpace(58f)
        paint.color = bgLight
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + 46f), 6f, 6f, paint)
        val colW = 126f
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f, session.y,
            "STREAK SAAT INI", "${subuh.currentStreak} / ${state.sedekahSubuhTargetDays} Hari", emeraldPrimary
        )
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f + colW + 6f, session.y,
            "REKOR TERPANJANG", "${subuh.longestStreak} Hari (${subuh.totalDaysGiven}x)", emeraldDark
        )
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f + (colW + 6f) * 2, session.y,
            "TOTAL SEDEKAH SUBUH", "Rp ${FinancialPdfGenerator.formatRp(subuh.totalContributions)}", goldAccent
        )
        val unlockedCount = subuh.badges.count { it.isUnlocked }
        FinancialPdfGenerator.drawMiniStat(
            session.canvas, paint, 42f + (colW + 6f) * 3, session.y,
            "LENCANA TERBUKA", "$unlockedCount / ${subuh.badges.size} Lencana", emeraldDark
        )
        session.y += 56f

        val sortedHistory = subuh.historyMap.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.key }

        fun drawSubuhHeader(c: Canvas, topY: Float): Float {
            paint.color = emeraldPrimary
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Tanggal Subuh", 42f, topY + 12f, paint)
            c.drawText("Status Harian", 180f, topY + 12f, paint)
            c.drawText("Nominal Sedekah (Rp)", 420f, topY + 12f, paint)
            return topY + 22f
        }

        session.ensureSpace(34f)
        session.y = drawSubuhHeader(session.canvas, session.y)

        if (sortedHistory.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada riwayat setoran harian Sedekah Subuh yang tercatat.", 42f, session.y + 12f, paint)
            session.y += 24f
        } else {
            for ((idx, item) in sortedHistory.withIndex()) {
                session.ensureSpace(17f) { c, topY -> drawSubuhHeader(c, topY) }
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + 16f), paint)

                paint.textSize = 8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = textDark
                session.canvas.drawText(item.key, 42f, session.y + 11f, paint)
                paint.color = emeraldPrimary
                session.canvas.drawText("Tercatat Tepat Waktu", 180f, session.y + 11f, paint)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = emeraldDark
                session.canvas.drawText("Rp ${FinancialPdfGenerator.formatRp(item.value)}", 420f, session.y + 11f, paint)
                session.y += 16f
            }
            session.y += 12f
        }
    }

    /**
     * Tabel Jurnal Transaksi Lengkap dengan detail pasangan akun Debit & Kredit
     * serta metadata lampiran struk (bila tersedia).
     */
    fun drawJournalEntriesWithLinesTable(
        session: FinancialPdfGenerator.PdfPageSession,
        state: AmanahLedgerUiState,
        entries: List<JournalEntry>,
        sectionTitle: String,
        emeraldDark: Int,
        emeraldPrimary: Int,
        coralRed: Int,
        blueInfo: Int,
        textDark: Int,
        textMuted: Int,
        bgLight: Int,
        editorialConfig: PdfEditorialConfig = state.pdfEditorialConfig
    ) {
        drawSectionTitle(session, sectionTitle, emeraldDark)
        val paint = session.paint
        val accountMap = state.accounts.associateBy { it.id }

        fun drawJournalHeader(c: Canvas, topY: Float): Float {
            paint.color = Color.rgb(60, 80, 85)
            c.drawRoundRect(RectF(32f, topY, 563f, topY + 18f), 4f, 4f, paint)
            paint.color = Color.WHITE
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText(if (editorialConfig.includeHijriDate) "Tanggal (M/H)" else "Tanggal", 38f, topY + 12f, paint)
            c.drawText("Deskripsi & Pasangan Akun (Debit / Kredit)", 112f, topY + 12f, paint)
            c.drawText("Tipe", 358f, topY + 12f, paint)
            c.drawText("Debit (Rp)", 422f, topY + 12f, paint)
            c.drawText("Kredit (Rp)", 495f, topY + 12f, paint)
            return topY + 22f
        }

        session.ensureSpace(36f)
        session.y = drawJournalHeader(session.canvas, session.y)

        if (entries.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada entri jurnal yang tercatat.", 40f, session.y + 12f, paint)
            session.y += 24f
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))
        for ((idx, e) in entries.withIndex()) {
            val hDate = HijriCalendarEngine.fromGregorian(e.gregorianDate)
            val dateLine1 = sdf.format(e.gregorianDate)
            val dateLine2 = "(${hDate.day}/${hDate.month}H)"

            val debitDetail = e.lines.filter { it.debit > 0.0 }
                .joinToString("; ") { line ->
                    val name = accountMap[line.accountId]?.name ?: line.accountId
                    "$name (Rp ${FinancialPdfGenerator.formatRp(line.debit)})"
                }
            val creditDetail = e.lines.filter { it.credit > 0.0 }
                .joinToString("; ") { line ->
                    val name = accountMap[line.accountId]?.name ?: line.accountId
                    "$name (Rp ${FinancialPdfGenerator.formatRp(line.credit)})"
                }
            val receiptInfo = e.receiptAttachment?.let { r ->
                val merchantPart = r.merchantName.ifBlank { r.title }.ifBlank { r.receiptType.displayName }
                val refPart = if (r.referenceNumber.isNotBlank()) " #${r.referenceNumber}" else ""
                val photoPart = if (!r.imagePath.isNullOrBlank()) " [Foto Struk Tersimpan]" else ""
                "Bukti: $merchantPart$refPart$photoPart"
            }

            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val descLines = FinancialPdfGenerator.wrapText(e.description, paint, 236f)

            paint.textSize = 6.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val acctDetailText = buildString {
                if (debitDetail.isNotEmpty()) append("D: $debitDetail")
                if (creditDetail.isNotEmpty()) {
                    if (isNotEmpty()) append(" | ")
                    append("K: $creditDetail")
                }
                if (receiptInfo != null) {
                    if (isNotEmpty()) append("\n")
                    append(receiptInfo)
                }
            }
            val acctLines = if (acctDetailText.isNotEmpty()) {
                FinancialPdfGenerator.wrapText(acctDetailText, paint, 236f)
            } else emptyList()

            val leftContentHeight = (descLines.size * 10f) + (acctLines.size * 8.8f) + 8f
            val rowHeight = maxOf(24f, leftContentHeight)

            session.ensureSpace(rowHeight) { c, topY -> drawJournalHeader(c, topY) }

            paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
            session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + rowHeight), paint)

            // Tanggal M/H
            paint.textSize = 7.3f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = textDark
            session.canvas.drawText(dateLine1, 38f, session.y + 11f, paint)
            if (editorialConfig.includeHijriDate) {
                paint.color = textMuted
                paint.textSize = 6.8f
                session.canvas.drawText(dateLine2, 38f, session.y + 20f, paint)
            }

            // Deskripsi
            var curY = session.y + 11f
            paint.textSize = 7.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = textDark
            for (dl in descLines) {
                session.canvas.drawText(dl, 112f, curY, paint)
                curY += 10f
            }

            // Detail Akun Debit/Kredit + Bukti
            paint.textSize = 6.8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = textMuted
            for (al in acctLines) {
                session.canvas.drawText(al, 112f, curY, paint)
                curY += 8.8f
            }

            // Tipe & Nominal
            val typeLabel = when (e.transactionType) {
                "INFLOW" -> "Pemasukan"
                "EXPENSE" -> "Beban"
                "INFAQ_PAYOUT" -> "Infaq"
                "TRANSFER" -> "Transfer"
                else -> e.transactionType
            }
            paint.textSize = 7.3f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = when (e.transactionType) {
                "INFLOW" -> emeraldDark
                "INFAQ_PAYOUT" -> emeraldPrimary
                "TRANSFER" -> blueInfo
                else -> coralRed
            }
            session.canvas.drawText(typeLabel, 358f, session.y + 11f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = emeraldDark
            session.canvas.drawText(FinancialPdfGenerator.formatRp(e.totalDebit), 422f, session.y + 11f, paint)

            paint.color = if (e.transactionType == "INFLOW") emeraldDark else coralRed
            session.canvas.drawText(FinancialPdfGenerator.formatRp(e.totalCredit), 495f, session.y + 11f, paint)

            session.y += rowHeight
        }
        session.y += 10f
    }

    /**
     * Daftar Aturan Infaq Aktif & Jadwal Transaksi Berulang.
     */
    fun drawRulesAndRecurringSection(
        session: FinancialPdfGenerator.PdfPageSession,
        state: AmanahLedgerUiState,
        sectionTitle: String,
        emeraldDark: Int,
        emeraldPrimary: Int,
        textDark: Int,
        textMuted: Int,
        bgLight: Int
    ) {
        drawSectionTitle(session, sectionTitle, emeraldDark)
        val paint = session.paint
        val accountMap = state.accounts.associateBy { it.id }

        // A. Aturan Infaq Otomatis
        session.ensureSpace(24f)
        paint.color = emeraldPrimary
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        session.canvas.drawText("Aturan Pemotongan Infaq Otomatis (${state.rules.size} Aturan):", 32f, session.y, paint)
        session.y += 12f

        if (state.rules.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Tidak ada aturan infaq khusus yang dikonfigurasi.", 40f, session.y + 10f, paint)
            session.y += 18f
        } else {
            for ((idx, rule) in state.rules.withIndex()) {
                val rateLabel = when (rule.calculationType) {
                    InfaqCalculationType.FLAT -> "Rp ${FinancialPdfGenerator.formatRp(rule.fixedAmount)}"
                    InfaqCalculationType.ROUND_UP -> "Kelipatan Rp ${FinancialPdfGenerator.formatRp(rule.roundUpStep)}"
                    else -> String.format(Locale("id", "ID"), "%.1f%%", rule.rate * 100.0)
                }
                val bonusFlags = buildList {
                    if (rule.enableFridayMultiplier) add("Berkah Jumat")
                    if (rule.enableRamadanMultiplier) add("2x Ramadhan")
                }.joinToString(", ").ifBlank { "Standar" }
                val lineText = "• ${rule.title} (${rule.targetCategory.displayName}) — ${rule.calculationType.displayName}: $rateLabel [$bonusFlags]"
                paint.textSize = 7.8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val wrapped = FinancialPdfGenerator.wrapText(lineText, paint, 515f)
                val h = wrapped.size * 10.5f + 4f
                session.ensureSpace(h)
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + h), paint)
                paint.color = textDark
                for ((i, l) in wrapped.withIndex()) {
                    session.canvas.drawText(l, 38f, session.y + 10f + (i * 10.5f), paint)
                }
                session.y += h
            }
            session.y += 8f
        }

        // B. Jadwal Transaksi Berulang
        session.ensureSpace(24f)
        paint.color = emeraldPrimary
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        session.canvas.drawText("Jadwal Transaksi Berulang (${state.recurringTransactions.size} Jadwal):", 32f, session.y, paint)
        session.y += 12f

        if (state.recurringTransactions.isEmpty()) {
            paint.color = textMuted
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            session.canvas.drawText("Belum ada jadwal transaksi otomatis yang terdaftar.", 40f, session.y + 10f, paint)
            session.y += 18f
        } else {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            for ((idx, rec) in state.recurringTransactions.withIndex()) {
                val catName = accountMap[rec.categoryAccountId]?.name ?: rec.categoryAccountId
                val statusStr = if (rec.isActive) "Aktif" else "Nonaktif"
                val lineText = "• ${rec.title} (${rec.frequency.label}, $statusStr) — Rp ${FinancialPdfGenerator.formatRp(rec.amount)} [$catName] • Berikutnya: ${sdf.format(rec.nextDueDate)}"
                paint.textSize = 7.8f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val wrapped = FinancialPdfGenerator.wrapText(lineText, paint, 515f)
                val h = wrapped.size * 10.5f + 4f
                session.ensureSpace(h)
                paint.color = if (idx % 2 == 0) bgLight else Color.WHITE
                session.canvas.drawRect(RectF(32f, session.y, 563f, session.y + h), paint)
                paint.color = textDark
                for ((i, l) in wrapped.withIndex()) {
                    session.canvas.drawText(l, 38f, session.y + 10f + (i * 10.5f), paint)
                }
                session.y += h
            }
            session.y += 10f
        }
    }

    /**
     * Catatan Penutup / Tanda Tangan Kustom dari Redaksi Pengguna (bila diisi).
     */
    fun drawClosingNoteSection(
        session: FinancialPdfGenerator.PdfPageSession,
        editorialConfig: PdfEditorialConfig,
        emeraldDark: Int,
        textDark: Int,
        bgLight: Int
    ) {
        val note = editorialConfig.closingNote.trim()
        if (note.isEmpty()) return

        val paint = session.paint
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val wrapped = FinancialPdfGenerator.wrapText(note, paint, 505f)
        val boxH = (wrapped.size * 11.5f) + 28f
        session.ensureSpace(boxH + 8f)

        paint.color = bgLight
        session.canvas.drawRoundRect(RectF(32f, session.y, 563f, session.y + boxH), 6f, 6f, paint)
        paint.color = emeraldDark
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        session.canvas.drawText("Catatan / Keterangan Tambahan:", 42f, session.y + 15f, paint)

        paint.color = textDark
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        var cy = session.y + 28f
        for (line in wrapped) {
            session.canvas.drawText(line, 42f, cy, paint)
            cy += 11.5f
        }
        session.y += boxH + 10f
    }
}

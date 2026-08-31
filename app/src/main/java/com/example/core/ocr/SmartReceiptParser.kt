package com.example.core.ocr

import com.example.core.receipt.ReceiptType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedReceiptData(
    val merchantName: String = "",
    val amount: Double = 0.0,
    val date: Date? = null,
    val dateString: String = "",
    val referenceNumber: String = "",
    val suggestedCategoryAccountId: String = "acc_living",
    val isIncome: Boolean = false,
    val suggestedReceiptType: ReceiptType = ReceiptType.STORE_RECEIPT,
    val notes: String = "",
    val rawText: String = "",
    val confidenceScore: Float = 0.85f
)

object SmartReceiptParser {

    private val amountPatterns = listOf(
        Regex("""(?i)(?:total|grand\s*total|jumlah|nominal|tagihan|bayar|net\s*total|amount)\s*[:=]?\s*(?:rp\.?|idr)?\s*([0-9.,]+)"""),
        Regex("""(?i)(?:transfer\s*nominal|nominal\s*transfer|debet|kredit)\s*[:=]?\s*(?:rp\.?|idr)?\s*([0-9.,]+)"""),
        Regex("""(?:rp\.?|idr)\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?)"""),
        Regex("""([0-9]{1,3}(?:[.,][0-9]{3}){1,3})""")
    )

    private val datePatterns = listOf(
        Regex("""\b(\d{1,2})[/.-](\d{1,2})[/.-](\d{4})\b"""),
        Regex("""\b(\d{4})[/.-](\d{1,2})[/.-](\d{1,2})\b"""),
        Regex("""\b(\d{1,2})\s+(Jan|Feb|Mar|Apr|Mei|Jun|Jul|Ags|Agu|Sep|Okt|Nov|Des)[a-z]*\s+(\d{4})\b""", RegexOption.IGNORE_CASE)
    )

    private val refPatterns = listOf(
        Regex("""(?i)(?:no\.?\s*ref(?:erensi)?|ref\s*id|no\s*transaksi|no\s*struk|id\s*transaksi|invoice\s*no)\s*[:=]?\s*([A-Z0-9_-]{6,30})"""),
        Regex("""(?i)(?:trace\s*no|stan|rrn)\s*[:=]?\s*([0-9]{6,20})""")
    )

    private val merchantCategoryMappings = listOf(
        "INDOMARET" to "acc_living",
        "ALFAMART" to "acc_living",
        "SUPERINDO" to "acc_living",
        "HYPERMART" to "acc_living",
        "PERTAMINA" to "acc_transport",
        "SHELL" to "acc_transport",
        "GRAB" to "acc_transport",
        "GOJEK" to "acc_transport",
        "MAXIM" to "acc_transport",
        "PLN" to "acc_utility",
        "TELKOM" to "acc_utility",
        "INDIHOME" to "acc_utility",
        "BPJS" to "acc_health",
        "APOTEK" to "acc_health",
        "KIMIA FARMA" to "acc_health",
        "BAZNAS" to "acc_charity",
        "DOMPET DHUAFA" to "acc_charity",
        "RUMAH ZAKAT" to "acc_charity",
        "LAZISMU" to "acc_charity",
        "LAZISNU" to "acc_charity",
        "MASJID" to "acc_charity",
        "INFAQ" to "acc_charity",
        "ZAKAT" to "acc_charity",
        "WAKAF" to "acc_charity",
        "RESTORAN" to "acc_living",
        "KAFE" to "acc_living",
        "WARUNG" to "acc_living",
        "GAJI" to "acc_salary",
        "DIVIDEN" to "acc_business"
    )

    fun parseReceiptText(rawOcrText: String): ParsedReceiptData {
        val lines = rawOcrText.lines().map { it.trim() }.filter { it.isNotBlank() }
        var detectedAmount = 0.0
        var detectedMerchant = ""
        var detectedDateStr = ""
        var detectedDate: Date? = null
        var detectedRef = ""
        var suggestedCatAccountId = "acc_living"
        var isIncomeDetected = false
        var receiptTypeDetected = ReceiptType.STORE_RECEIPT

        // 1. Merchant & Category Detection
        for (line in lines.take(6)) {
            val upper = line.uppercase()
            for (mapping in merchantCategoryMappings) {
                if (upper.contains(mapping.first)) {
                    detectedMerchant = line
                    suggestedCatAccountId = mapping.second
                    if (mapping.first == "GAJI" || mapping.first == "DIVIDEN" || upper.contains("CR") || upper.contains("MASUK")) {
                        isIncomeDetected = true
                    }
                    if (upper.contains("TRANSFER") || upper.contains("BANK") || upper.contains("MUTASI")) {
                        receiptTypeDetected = ReceiptType.BANK_TRANSFER_PROOF
                    } else if (upper.contains("QRIS") || upper.contains("GOPAY") || upper.contains("OVO") || upper.contains("SHOPEE")) {
                        receiptTypeDetected = ReceiptType.DIGITAL_PAYMENT
                    }
                    break
                }
            }
            if (detectedMerchant.isNotBlank()) break
        }
        if (detectedMerchant.isBlank() && lines.isNotEmpty()) {
            detectedMerchant = lines.firstOrNull { it.length in 3..40 && !it.contains(Regex("""\d{4}""")) } ?: "Transaksi Struk"
        }

        // Check if bank mutation
        val fullUpper = rawOcrText.uppercase()
        if (fullUpper.contains("BERHASIL") || fullUpper.contains("TRANSFER") || fullUpper.contains("REKENING") || fullUpper.contains("MUTASI")) {
            receiptTypeDetected = ReceiptType.BANK_TRANSFER_PROOF
        } else if (fullUpper.contains("QRIS") || fullUpper.contains("DOMPET DIGITAL")) {
            receiptTypeDetected = ReceiptType.DIGITAL_PAYMENT
        }
        if (fullUpper.contains("BAZNAS") || fullUpper.contains("LAZ") || fullUpper.contains("ZAKAT") || fullUpper.contains("BSZ")) {
            receiptTypeDetected = ReceiptType.AMIL_ZAKAT_RECEIPT
        } else if (fullUpper.contains("KWITANSI") || fullUpper.contains("INVOICE") || fullUpper.contains("FAKTUR")) {
            receiptTypeDetected = ReceiptType.OFFICIAL_INVOICE
        }
        if (fullUpper.contains("CR") || fullUpper.contains("DITERIMA") || fullUpper.contains("DANA MASUK")) {
            isIncomeDetected = true
        }

        // 2. Amount Detection
        for (line in lines) {
            for (pattern in amountPatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    val rawNum = match.groupValues[1]
                    val cleaned = cleanAmountString(rawNum)
                    if (cleaned > detectedAmount && cleaned < 1_000_000_000_000.0) {
                        detectedAmount = cleaned
                    }
                }
            }
        }

        // 3. Date Detection
        val sdfFormats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.US),
            SimpleDateFormat("dd-MM-yyyy", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
            SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")),
            SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        )

        for (line in lines) {
            for (pattern in datePatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    val rawDateStr = match.value
                    detectedDateStr = rawDateStr
                    for (sdf in sdfFormats) {
                        try {
                            detectedDate = sdf.parse(rawDateStr)
                            if (detectedDate != null) break
                        } catch (_: Exception) {}
                    }
                    if (detectedDate != null) break
                }
            }
            if (detectedDate != null) break
        }

        // 4. Reference Number Detection
        for (line in lines) {
            for (pattern in refPatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    detectedRef = match.groupValues[1]
                    break
                }
            }
            if (detectedRef.isNotBlank()) break
        }

        val notes = buildString {
            if (detectedMerchant.isNotBlank()) append("Transaksi di $detectedMerchant. ")
            if (detectedRef.isNotBlank()) append("No. Ref: $detectedRef. ")
            append("Dipindai otomatis via OCR Amanah.")
        }

        return ParsedReceiptData(
            merchantName = detectedMerchant,
            amount = detectedAmount,
            date = detectedDate ?: Date(),
            dateString = detectedDateStr,
            referenceNumber = detectedRef,
            suggestedCategoryAccountId = suggestedCatAccountId,
            isIncome = isIncomeDetected,
            suggestedReceiptType = receiptTypeDetected,
            notes = notes,
            rawText = rawOcrText,
            confidenceScore = if (detectedAmount > 0) 0.92f else 0.65f
        )
    }

    private fun cleanAmountString(raw: String): Double {
        var s = raw.trim().replace(" ", "").replace("Rp", "", ignoreCase = true).replace("IDR", "", ignoreCase = true)
        val lastDot = s.lastIndexOf('.')
        val lastComma = s.lastIndexOf(',')

        s = if (lastDot != -1 && lastComma != -1) {
            if (lastDot > lastComma) {
                s.replace(",", "")
            } else {
                s.replace(".", "").replace(",", ".")
            }
        } else if (lastComma != -1 && s.length - 1 - lastComma == 2) {
            s.replace(".", "").replace(",", ".")
        } else if (lastDot != -1 && s.length - 1 - lastDot == 2) {
            s.replace(",", "")
        } else {
            s.replace(".", "").replace(",", "")
        }

        return s.toDoubleOrNull() ?: 0.0
    }
}

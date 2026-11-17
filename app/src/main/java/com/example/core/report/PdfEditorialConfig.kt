package com.example.core.report

import org.json.JSONObject

/**
 * Preset gaya bahasa & redaksi dokumen PDF agar pengguna tidak terpaku pada
 * gaya redaksi tema UI aplikasi (misalnya tema Taktis/Sci-Fi) saat mengekspor
 * laporan keuangan formal atau sederhana.
 */
enum class PdfEditorialStyle(
    val title: String,
    val description: String,
    val badge: String
) {
    SIMPLE_FORMAL(
        title = "Keuangan Sederhana & Resmi",
        description = "Bahasa lugas, netral, dan mudah dibaca untuk catatan pribadi atau keluarga tanpa istilah taktis.",
        badge = "Sederhana"
    ),
    SYARIAH_RESMI(
        title = "Akuntansi Syariah Resmi",
        description = "Menggunakan istilah resmi muamalah syariah (Kasab, Tathhir, ZISWAF, Qardhul Hasan, 8 Asnaf).",
        badge = "Syariah"
    ),
    KORPORAT_USAHA(
        title = "Laporan Bisnis & Usaha",
        description = "Redaksi profesional untuk pembukuan UMKM, toko, atau laporan kas usaha.",
        badge = "Bisnis"
    ),
    IKUTI_TEMA(
        title = "Ikuti Gaya Tema Aplikasi",
        description = "Mengikuti gaya bahasa tema aktif (termasuk gaya Taktis jika tema Taktis sedang dipilih).",
        badge = "Tema"
    )
}

/**
 * Daftar bagian konten yang dapat dinamai ulang atau dipilih/disembunyikan
 * oleh pengguna sebelum mengekspor PDF.
 */
enum class PdfSectionKey(
    val labelInEditor: String,
    val defaultSimpleTitle: String,
    val defaultSyariahTitle: String,
    val defaultBusinessTitle: String,
    val defaultTacticalTitle: String
) {
    WALLETS(
        labelInEditor = "Daftar Kantong Kas & Rekening",
        defaultSimpleTitle = "Saldo Dompet & Rekening Bank",
        defaultSyariahTitle = "Neraca Saldo Kantong Kas, Rekening & Mutasi Internal",
        defaultBusinessTitle = "Posisi Saldo Kas & Rekening Usaha",
        defaultTacticalTitle = "Matriks Saldo Kantong Kas & Mutasi Internal"
    ),
    CASHFLOW_KASAB(
        labelInEditor = "Rincian Pemasukan & Pengeluaran",
        defaultSimpleTitle = "Rincian Pemasukan, Pengeluaran & Tabungan Bersih",
        defaultSyariahTitle = "Rincian Arus Dana: Pemasukan Kasab vs Non-Kasab & Belanja Kategori",
        defaultBusinessTitle = "Laporan Arus Kas Masuk, Beban Operasional & Selisih Bersih",
        defaultTacticalTitle = "Aliran Dana Kasab, Non-Kasab & Konsumsi Kategori"
    ),
    BUDGETS(
        labelInEditor = "Realisasi Anggaran Bulanan",
        defaultSimpleTitle = "Realisasi Batas Anggaran Bulanan",
        defaultSyariahTitle = "Realisasi & Kepatuhan Batas Anggaran Bulanan",
        defaultBusinessTitle = "Realisasi & Pengendalian Anggaran Operasional",
        defaultTacticalTitle = "Kepatuhan Batas Anggaran Siklus Bulanan"
    ),
    SLI_ANALYSIS(
        labelInEditor = "Analisa Infaq & Kedermawanan (SLI)",
        defaultSimpleTitle = "Ringkasan Rasio Infaq terhadap Pendapatan & Belanja",
        defaultSyariahTitle = "Analisa Spiritual Liquidity Index (SLI) & Audit Kewajiban Infaq",
        defaultBusinessTitle = "Evaluasi Rasio Dana Sosial / CSR & Infaq Usaha",
        defaultTacticalTitle = "Audit Spiritual Liquidity Index (SLI) & Kewajiban Infaq"
    ),
    ZAKAT_MAAL_FITRAH(
        labelInEditor = "Zakat Maal, Fitrah & Dana Syubhat",
        defaultSimpleTitle = "Ringkasan Zakat Maal, Zakat Fitrah & Pembersihan Dana",
        defaultSyariahTitle = "Rekapitulasi Zakat Maal & Haul, Zakat Fitrah serta Dana Syubhat",
        defaultBusinessTitle = "Rekapitulasi Kewajiban Zakat Aset & Fitrah",
        defaultTacticalTitle = "Rekapitulasi Zakat Maal, Fitrah & Karantina Syubhat"
    ),
    ZAKAT_PROFESI_PERNIAGAAN(
        labelInEditor = "Zakat Profesi & Zakat Usaha",
        defaultSimpleTitle = "Perhitungan Zakat Penghasilan & Zakat Usaha",
        defaultSyariahTitle = "Kalkulasi Zakat Profesi (Penghasilan) & Zakat Perniagaan (Usaha)",
        defaultBusinessTitle = "Perhitungan Zakat Penghasilan & Zakat Perniagaan",
        defaultTacticalTitle = "Hisab Zakat Profesi & Zakat Perniagaan"
    ),
    SEDEKAH_SUBUH(
        labelInEditor = "Catatan Sedekah Subuh Harian",
        defaultSimpleTitle = "Catatan Konsistensi Sedekah Subuh Harian",
        defaultSyariahTitle = "Aktivitas & Streak Sedekah Subuh Harian",
        defaultBusinessTitle = "Catatan Sedekah Pagi / Subuh Harian",
        defaultTacticalTitle = "Log Aktivitas & Streak Sedekah Subuh Harian"
    ),
    REZEKI_NON_TUNAI(
        labelInEditor = "Catatan Rezeki Non-Tunai / Hadiah",
        defaultSimpleTitle = "Catatan Pemberian & Rezeki Non-Tunai",
        defaultSyariahTitle = "Catatan Rezeki Non-Tunai / Hadiah & Syukur Infaq",
        defaultBusinessTitle = "Catatan Aset / Pemberian Non-Tunai",
        defaultTacticalTitle = "Log Rezeki Non-Tunai & Alokasi Syukur"
    ),
    ASNAF_DISTRIBUTION(
        labelInEditor = "Riwayat Penyaluran Infaq & Zakat",
        defaultSimpleTitle = "Daftar Penyaluran Infaq & Zakat kepada Penerima",
        defaultSyariahTitle = "Riwayat Penyaluran Infaq & Tathhir ke 8 Asnaf",
        defaultBusinessTitle = "Riwayat Penyaluran Dana Zakat & Sosial",
        defaultTacticalTitle = "Riwayat Distribusi Infaq & Tathhir ke 8 Asnaf"
    ),
    IBADAH_GOALS(
        labelInEditor = "Target Tabungan & Rencana Ibadah",
        defaultSimpleTitle = "Daftar Target Tabungan & Rencana Ibadah",
        defaultSyariahTitle = "Program Target Tabungan Ibadah",
        defaultBusinessTitle = "Cadangan Dana & Target Rencana Ibadah",
        defaultTacticalTitle = "Target Tabungan Ibadah & Progres Capaian"
    ),
    QARDH_HASAN(
        labelInEditor = "Catatan Hutang & Piutang",
        defaultSimpleTitle = "Daftar Catatan Hutang & Piutang",
        defaultSyariahTitle = "Rekapitulasi Buku Piutang & Hutang Syariah (Qardhul Hasan)",
        defaultBusinessTitle = "Daftar Piutang & Kewajiban Hutang Usaha",
        defaultTacticalTitle = "Rekapitulasi Buku Piutang & Hutang (Qardhul Hasan)"
    ),
    RULES_RECURRING_AUDIT(
        labelInEditor = "Aturan Otomatis, Jadwal & Audit",
        defaultSimpleTitle = "Aturan Infaq Otomatis, Jadwal Berulang & Ringkasan Audit",
        defaultSyariahTitle = "Aturan Pemotongan Infaq Otomatis & Jadwal Transaksi Berulang",
        defaultBusinessTitle = "Otomasi Transaksi Berulang & Ringkasan Audit Pembukuan",
        defaultTacticalTitle = "Aturan Infaq Otomatis, Jadwal & Ringkasan Audit"
    ),
    JOURNAL_LEDGER(
        labelInEditor = "Daftar Transaksi / Buku Besar",
        defaultSimpleTitle = "Daftar Riwayat Transaksi Lengkap (Debit & Kredit)",
        defaultSyariahTitle = "Buku Besar Jurnal Berpasangan Lengkap (Rincian Akun Debit/Kredit)",
        defaultBusinessTitle = "Buku Besar Jurnal Akuntansi (Debit & Kredit)",
        defaultTacticalTitle = "Buku Besar Jurnal Berpasangan Lengkap"
    );

    fun defaultTitleFor(style: PdfEditorialStyle, isAppTactical: Boolean): String {
        return when (style) {
            PdfEditorialStyle.SIMPLE_FORMAL -> defaultSimpleTitle
            PdfEditorialStyle.SYARIAH_RESMI -> defaultSyariahTitle
            PdfEditorialStyle.KORPORAT_USAHA -> defaultBusinessTitle
            PdfEditorialStyle.IKUTI_TEMA -> if (isAppTactical) defaultTacticalTitle else defaultSyariahTitle
        }
    }
}

data class PdfSectionCustomization(
    val included: Boolean = true,
    val customTitle: String = ""
)

/**
 * Konfigurasi lengkap kostumisasi redaksi & penamaan konten PDF.
 * Nilai string kosong berarti menggunakan redaksi bersih sesuai preset aktif.
 */
data class PdfEditorialConfig(
    val stylePreset: PdfEditorialStyle = PdfEditorialStyle.SIMPLE_FORMAL,
    val documentTitleOverride: String = "",
    val headerBrandTitle: String = "",
    val headerSubtitle: String = "",
    val ownerLabel: String = "",
    val ownerNameOverride: String = "",
    val customFileNamePrefix: String = "",
    val footerText: String = "",
    val closingNote: String = "",
    val includeHijriDate: Boolean = true,
    val includeExecutiveKpis: Boolean = true,
    // Kustomisasi istilah arus kas & KPI
    val kasabLabel: String = "",
    val nonKasabLabel: String = "",
    val expenseLabel: String = "",
    val netSavingsLabel: String = "",
    val kpiNetWorthLabel: String = "",
    val kpiCashBalanceLabel: String = "",
    val kpiIncomeLabel: String = "",
    val kpiSecondaryLabel: String = "",
    // Kustomisasi per bagian konten (aktifkan/sembunyikan & ubah judul bebas)
    val sectionCustomizations: Map<PdfSectionKey, PdfSectionCustomization> = emptyMap()
) {
    fun isTacticalTextEnabled(isAppTactical: Boolean): Boolean =
        stylePreset == PdfEditorialStyle.IKUTI_TEMA && isAppTactical

    fun resolveDocumentTitle(defaultDocTitle: String): String =
        documentTitleOverride.trim().ifBlank { defaultDocTitle }

    fun resolveHeaderBrand(isAppTactical: Boolean, defaultSimpleBrand: String = "LAPORAN KEUANGAN PRIBADI"): String {
        val custom = headerBrandTitle.trim()
        if (custom.isNotEmpty()) return custom
        return when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> defaultSimpleBrand
            PdfEditorialStyle.SYARIAH_RESMI -> "AMANAH LEDGER SYARIAH"
            PdfEditorialStyle.KORPORAT_USAHA -> "LAPORAN KEUANGAN USAHA"
            PdfEditorialStyle.IKUTI_TEMA -> if (isAppTactical) "DOSIR KEUANGAN TAKTIS AMANAH" else "AMANAH LEDGER SYARIAH"
        }
    }

    fun resolveHeaderSubtitle(isAppTactical: Boolean, defaultFallback: String): String {
        val custom = headerSubtitle.trim()
        if (custom.isNotEmpty()) return custom
        return when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> defaultFallback
            PdfEditorialStyle.SYARIAH_RESMI -> defaultFallback
            PdfEditorialStyle.KORPORAT_USAHA -> "Ringkasan Kas, Arus Dana, Piutang/Kewajiban & Buku Besar"
            PdfEditorialStyle.IKUTI_TEMA -> if (isAppTactical) "AUDIT TRAIL DOUBLE-ENTRY & KEPATUHAN SYARIAH" else defaultFallback
        }
    }

    fun resolveOwnerLabel(isAppTactical: Boolean): String {
        val custom = ownerLabel.trim()
        if (custom.isNotEmpty()) return custom
        return when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> "Pemilik:"
            PdfEditorialStyle.SYARIAH_RESMI -> "Pemilik Kas:"
            PdfEditorialStyle.KORPORAT_USAHA -> "Entitas Usaha:"
            PdfEditorialStyle.IKUTI_TEMA -> if (isAppTactical) "OPERATOR:" else "Pemilik:"
        }
    }

    fun resolveOwnerName(stateUserName: String): String =
        ownerNameOverride.trim().ifBlank { stateUserName }

    fun resolveOwnerLine(
        stateUserName: String,
        isAppTactical: Boolean,
        hijriDateText: String,
        extraSuffix: String = ""
    ): String {
        val prefix = resolveOwnerLabel(isAppTactical)
        val ownerName = resolveOwnerName(stateUserName)
        return buildString {
            append("$prefix $ownerName")
            if (includeHijriDate && hijriDateText.isNotBlank()) {
                append(" • $hijriDateText")
            }
            if (extraSuffix.isNotBlank()) {
                append(" • $extraSuffix")
            }
        }
    }

    fun resolveFooterText(isAppTactical: Boolean): String {
        val custom = footerText.trim()
        if (custom.isNotEmpty()) return custom
        return when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> "Laporan Keuangan Pribadi • Dihasilkan oleh Amanah Ledger"
            PdfEditorialStyle.SYARIAH_RESMI -> "Laporan Resmi Amanah Ledger Syariah • Prinsip Keseimbangan Debit-Kredit & Transparansi"
            PdfEditorialStyle.KORPORAT_USAHA -> "Laporan Pembukuan Keuangan Resmi • Disusun Otomatis oleh Sistem Buku Kas"
            PdfEditorialStyle.IKUTI_TEMA -> if (isAppTactical) {
                "PROTOKOL AMANAH KEUANGAN SYARIAH • INTEGRITAS TERVERIFIKASI • SHA-256 SECURE"
            } else {
                "Laporan Resmi Amanah Ledger Syariah • Prinsip Bebas Riba, Keseimbangan Debit-Kredit & Transparansi"
            }
        }
    }

    fun resolveContinuationHeader(docTitle: String, isAppTactical: Boolean): String {
        val effectiveTitle = resolveDocumentTitle(docTitle)
        val brand = resolveHeaderBrand(isAppTactical)
        return if (isTacticalTextEnabled(isAppTactical)) {
            "$brand • $effectiveTitle [SEC//LOG]"
        } else {
            "$brand • $effectiveTitle (Lanjutan)"
        }
    }

    fun isSectionIncluded(key: PdfSectionKey): Boolean =
        sectionCustomizations[key]?.included ?: true

    fun resolveSectionTitle(key: PdfSectionKey, isAppTactical: Boolean, numberPrefix: String = ""): String {
        val custom = sectionCustomizations[key]?.customTitle?.trim().orEmpty()
        val rawTitle = custom.ifBlank { key.defaultTitleFor(stylePreset, isAppTactical) }
        return if (numberPrefix.isNotBlank()) "$numberPrefix $rawTitle" else rawTitle
    }

    fun resolveKasabLabel(): String = kasabLabel.trim().ifBlank {
        when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> "Pemasukan Utama (Kasab)"
            PdfEditorialStyle.KORPORAT_USAHA -> "Pendapatan Usaha"
            else -> "Pemasukan Kasab (Aktif)"
        }
    }

    fun resolveNonKasabLabel(): String = nonKasabLabel.trim().ifBlank {
        when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> "Pemasukan Lainnya"
            PdfEditorialStyle.KORPORAT_USAHA -> "Pendapatan Lain-Lain"
            else -> "Pemasukan Non-Kasab"
        }
    }

    fun resolveExpenseLabel(): String = expenseLabel.trim().ifBlank {
        when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> "Pengeluaran"
            PdfEditorialStyle.KORPORAT_USAHA -> "Beban & Pengeluaran"
            else -> "Konsumsi Beban"
        }
    }

    fun resolveNetSavingsLabel(): String = netSavingsLabel.trim().ifBlank {
        when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> "Selisih / Tabungan Bersih"
            PdfEditorialStyle.KORPORAT_USAHA -> "Surplus / Laba Bersih"
            else -> "Tabungan Bersih"
        }
    }

    fun resolveKpiLabels(): List<String> {
        val defaults = when (stylePreset) {
            PdfEditorialStyle.SIMPLE_FORMAL -> listOf("KEKAYAAN BERSIH", "TOTAL SALDO KAS", "TOTAL PEMASUKAN", "RASIO INFAQ / AUDIT")
            PdfEditorialStyle.KORPORAT_USAHA -> listOf("EKUITAS BERSIH", "SALDO KAS & BANK", "TOTAL PENDAPATAN", "AUDIT / RASIO SOSIAL")
            else -> listOf("KEKAYAAN BERSIH", "TOTAL SALDO KAS", "TOTAL PEMASUKAN", "SKOR SYARIAH / SLI")
        }
        return listOf(
            kpiNetWorthLabel.trim().ifBlank { defaults[0] },
            kpiCashBalanceLabel.trim().ifBlank { defaults[1] },
            kpiIncomeLabel.trim().ifBlank { defaults[2] },
            kpiSecondaryLabel.trim().ifBlank { defaults[3] }
        )
    }

    fun resolveFilePrefix(defaultBaseName: String): String {
        val raw = customFileNamePrefix.trim()
        if (raw.isEmpty()) return defaultBaseName
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9_\\- ]"), "")
            .trim()
            .replace(Regex("\\s+"), "_")
        return cleaned.ifBlank { defaultBaseName }
    }

    fun activeSectionCount(): Int = PdfSectionKey.entries.count { isSectionIncluded(it) }

    fun toJson(): String {
        val json = JSONObject()
        json.put("stylePreset", stylePreset.name)
        json.put("documentTitleOverride", documentTitleOverride)
        json.put("headerBrandTitle", headerBrandTitle)
        json.put("headerSubtitle", headerSubtitle)
        json.put("ownerLabel", ownerLabel)
        json.put("ownerNameOverride", ownerNameOverride)
        json.put("customFileNamePrefix", customFileNamePrefix)
        json.put("footerText", footerText)
        json.put("closingNote", closingNote)
        json.put("includeHijriDate", includeHijriDate)
        json.put("includeExecutiveKpis", includeExecutiveKpis)
        json.put("kasabLabel", kasabLabel)
        json.put("nonKasabLabel", nonKasabLabel)
        json.put("expenseLabel", expenseLabel)
        json.put("netSavingsLabel", netSavingsLabel)
        json.put("kpiNetWorthLabel", kpiNetWorthLabel)
        json.put("kpiCashBalanceLabel", kpiCashBalanceLabel)
        json.put("kpiIncomeLabel", kpiIncomeLabel)
        json.put("kpiSecondaryLabel", kpiSecondaryLabel)

        val secObj = JSONObject()
        for ((key, custom) in sectionCustomizations) {
            val itemObj = JSONObject()
            itemObj.put("included", custom.included)
            itemObj.put("customTitle", custom.customTitle)
            secObj.put(key.name, itemObj)
        }
        json.put("sectionCustomizations", secObj)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String?): PdfEditorialConfig {
            if (jsonStr.isNullOrBlank()) return PdfEditorialConfig()
            return try {
                val json = JSONObject(jsonStr)
                val styleName = json.optString("stylePreset", PdfEditorialStyle.SIMPLE_FORMAL.name)
                val style = try {
                    PdfEditorialStyle.valueOf(styleName)
                } catch (_: Exception) {
                    PdfEditorialStyle.SIMPLE_FORMAL
                }
                val secMap = mutableMapOf<PdfSectionKey, PdfSectionCustomization>()
                val secObj = json.optJSONObject("sectionCustomizations")
                if (secObj != null) {
                    for (key in PdfSectionKey.entries) {
                        val item = secObj.optJSONObject(key.name)
                        if (item != null) {
                            secMap[key] = PdfSectionCustomization(
                                included = item.optBoolean("included", true),
                                customTitle = item.optString("customTitle", "")
                            )
                        }
                    }
                }
                PdfEditorialConfig(
                    stylePreset = style,
                    documentTitleOverride = json.optString("documentTitleOverride", ""),
                    headerBrandTitle = json.optString("headerBrandTitle", ""),
                    headerSubtitle = json.optString("headerSubtitle", ""),
                    ownerLabel = json.optString("ownerLabel", ""),
                    ownerNameOverride = json.optString("ownerNameOverride", ""),
                    customFileNamePrefix = json.optString("customFileNamePrefix", ""),
                    footerText = json.optString("footerText", ""),
                    closingNote = json.optString("closingNote", ""),
                    includeHijriDate = json.optBoolean("includeHijriDate", true),
                    includeExecutiveKpis = json.optBoolean("includeExecutiveKpis", true),
                    kasabLabel = json.optString("kasabLabel", ""),
                    nonKasabLabel = json.optString("nonKasabLabel", ""),
                    expenseLabel = json.optString("expenseLabel", ""),
                    netSavingsLabel = json.optString("netSavingsLabel", ""),
                    kpiNetWorthLabel = json.optString("kpiNetWorthLabel", ""),
                    kpiCashBalanceLabel = json.optString("kpiCashBalanceLabel", ""),
                    kpiIncomeLabel = json.optString("kpiIncomeLabel", ""),
                    kpiSecondaryLabel = json.optString("kpiSecondaryLabel", ""),
                    sectionCustomizations = secMap
                )
            } catch (_: Exception) {
                PdfEditorialConfig()
            }
        }

        /**
         * Menerapkan preset redaksi bersih sekaligus mereset override judul
         * agar langsung selaras dengan gaya yang dipilih.
         */
        fun forPreset(style: PdfEditorialStyle, keepOwnerAndClosingNoteFrom: PdfEditorialConfig? = null): PdfEditorialConfig {
            return PdfEditorialConfig(
                stylePreset = style,
                ownerNameOverride = keepOwnerAndClosingNoteFrom?.ownerNameOverride.orEmpty(),
                closingNote = keepOwnerAndClosingNoteFrom?.closingNote.orEmpty(),
                includeHijriDate = style != PdfEditorialStyle.KORPORAT_USAHA && (keepOwnerAndClosingNoteFrom?.includeHijriDate ?: true)
            )
        }
    }
}

package com.example.core.shariah

import java.io.Serializable

enum class ShariahMazhab(val displayName: String, val description: String) : Serializable {
    SYAFII("Mazhab Syafi'i (Jumhur Nusantara)", "Nisab emas 85g murni, haul 1 tahun Hijriah penuh (354 hari), zakat profesi diqiyaskan pada zakat pertanian/emas, dan penentuan ashabah faraidh yang ketat."),
    HANAFI("Mazhab Hanafi", "Penggunaan nisab perak (595g/dirham) untuk kemaslahatan fakir miskin, zakat mencakup seluruh hasil bumi tanpa minimal volume, dan perhitungan haul berputar."),
    MALIKI("Mazhab Maliki", "Kewajiban zakat pada barang niaga perdagangan, pembebasan hutang produktif, penekanan 'amal ahl al-Madinah, dan ashabah keluarga luas."),
    HANBALI("Mazhab Hanbali", "Penetapan ketat nisab emas/perak, penegasan zakat madu dan tambang (rikaz/ma'din), penyesuaian kebutuhan hajjah ashliyah, dan ketentuan waris khusus.")
}

enum class ZakatProfesiFormula(val title: String, val formulaDesc: String) : Serializable {
    BRUTO("Metode Bruto (Pendapatan Kotor)", "Zakat = Pendapatan Kotor × Tarif Zakat (Prinsip ihtiyath/kehati-hatian, tanpa dikurangi kebutuhan bulanan)"),
    NETTO("Metode Netto (Pendapatan Bersih)", "Zakat = (Pendapatan Kotor - Biaya Hidup Pokok/Hutang Jatuh Tempo) × Tarif Zakat (Sesuai Fatwa MUI No. 3 Th 2003)")
}

enum class HaulCalculationMethod(val title: String, val days: Int, val rate: Double, val description: String) : Serializable {
    HIJRIAH("Tahun Hijriah (Qamariyah)", 354, 2.5, "Siklus tahun hijriah 354 hari dengan tarif zakat standar 2.5%."),
    MASEHI("Tahun Masehi (Syamsiyah / Standar AAOIFI)", 365, 2.577, "Siklus tahun masehi 365 hari dengan penyesuaian tarif 2.577% (2.5% × 365/354) sesuai Standar Syariah Internasional AAOIFI No. 35.")
}

data class ShariahRulesConfig(
    val goldNisabGram: Double = 85.0,
    val silverNisabGram: Double = 595.0,
    val zakatPercentage: Double = 2.5,
    val zakatProfesiFormula: ZakatProfesiFormula = ZakatProfesiFormula.BRUTO,
    val haulCalculationMethod: HaulCalculationMethod = HaulCalculationMethod.HIJRIAH,
    val selectedMazhab: ShariahMazhab = ShariahMazhab.SYAFII,
    val customCalculationFormula: String = "",
    val isCustomFormulaActive: Boolean = false,
    val customFormulaName: String = "Formula Pribadi",
    val defaultGoldPricePerGram: Double = 1450000.0,
    val defaultSilverPricePerGram: Double = 18000.0
) : Serializable

data class ShariahRuling(
    val id: String,
    val title: String,
    val category: String, // ZAKAT, FARAIDH, QARDH, INFAQ, TRANSAKSI, INVESTASI, LAINNYA
    val authority: String, // DSN-MUI, BAZNAS Daerah, Dewan Pengawas Syariah, Mazhab, Kemenag
    val referenceNumber: String, // e.g., "SK BAZNAS No. 01/2024" or "Fatwa DSN-MUI No. 116/2017"
    val summary: String,
    val detailedRuling: String,
    val calculationFormula: String = "",
    val dalilSource: String = "",
    val dalilArabic: String = "",
    val dalilTranslation: String = "",
    val isCustom: Boolean = false,
    val isEnabled: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
) : Serializable

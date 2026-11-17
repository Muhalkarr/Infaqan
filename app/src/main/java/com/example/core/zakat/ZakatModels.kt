package com.example.core.zakat

import java.util.UUID

enum class ZakatCategoryType(val title: String, val subtitle: String) {
    PROFESI("Zakat Profesi / Penghasilan", "Penghasilan bulanan (Nisab 524 kg beras)"),
    PERNIAGAAN("Zakat Perdagangan / Bisnis", "Aset lancar usaha dikurangi hutang jatuh tempo"),
    FITRAH("Zakat Fitrah Keluarga", "Kewajiban per jiwa (2.5 kg / 3.5 L beras) di bulan Ramadhan"),
    SIMPANAN_EMAS("Zakat Mal Simpanan & Emas", "Tabungan & emas yang telah mencapai haul 1 tahun")
}

data class ZakatProfesiCalculation(
    val monthlyGrossSalary: Double = 10000000.0,
    val monthlyAllowanceAndOther: Double = 2500000.0,
    val monthlyEssentialExpenses: Double = 4000000.0,
    val monthlyDebtInstallment: Double = 1000000.0,
    val isNetMethod: Boolean = true, // Fatwa MUI No. 3/2003 memperbolehkan bruto maupun neto setelah kebutuhan pokok
    val ricePricePerKg: Double = 15000.0 // Standar 524 kg beras
) {
    val monthlySalary: Double get() = monthlyGrossSalary
    val otherIncomeBonus: Double get() = monthlyAllowanceAndOther
    val basicNeedsExpense: Double get() = monthlyEssentialExpenses
    val debtPaymentMonthly: Double get() = monthlyDebtInstallment

    val totalIncome: Double = monthlyGrossSalary + monthlyAllowanceAndOther
    val netIncome: Double = (totalIncome - monthlyEssentialExpenses - monthlyDebtInstallment).coerceAtLeast(0.0)
    val netMonthlyIncome: Double get() = netIncome
    val taxableBase: Double = if (isNetMethod) netIncome else totalIncome
    val nisabMonthly: Double = 524.0 * ricePricePerKg // ~ Rp 7.860.000
    val nisabThreshold: Double get() = nisabMonthly
    val isObligatory: Boolean = taxableBase >= nisabMonthly
    val isObligated: Boolean get() = isObligatory
    val zakatAmount: Double = if (isObligatory) kotlin.math.round(taxableBase * 0.025) else 0.0
    val zakatPayableAmount: Double get() = zakatAmount
}

data class ZakatPerniagaanCalculation(
    val currentCashInBusiness: Double = 25000000.0,
    val inventoryValue: Double = 90000000.0,
    val receivableCollectible: Double = 15000000.0,
    val shortTermPayable: Double = 10000000.0,
    val goldPricePerGram: Double = 1350000.0,
    val isLunarCalendar: Boolean = true // 2.5% Hijriyah (354 hari) vs 2.577% Masehi (365 hari)
) {
    val currentCashAndBank: Double get() = currentCashInBusiness
    val inventoryStockValue: Double get() = inventoryValue
    val receivablesCollectable: Double get() = receivableCollectible
    val shortTermPayables: Double get() = shortTermPayable

    val totalCurrentAssets: Double = currentCashInBusiness + inventoryValue + receivableCollectible
    val netTaxableAssets: Double = (totalCurrentAssets - shortTermPayable).coerceAtLeast(0.0)
    val netZakatAsset: Double get() = netTaxableAssets
    val nisabGold85g: Double = 85.0 * goldPricePerGram
    val nisabGoldThreshold: Double get() = nisabGold85g
    val isObligatory: Boolean = netTaxableAssets >= nisabGold85g
    val isObligated: Boolean get() = isObligatory
    val rate: Double = if (isLunarCalendar) 0.025 else 0.02577
    val zakatAmount: Double = if (isObligatory) kotlin.math.round(netTaxableAssets * rate) else 0.0
    val zakatPayableAmount: Double get() = zakatAmount
}

data class ZakatFitrahDependent(
    val id: String = UUID.randomUUID().toString().take(8),
    val name: String,
    val relation: String = "Anggota Keluarga",
    val isIncluded: Boolean = true
)

data class LocalStapleFoodPreset(
    val id: String,
    val foodName: String,
    val regionName: String,
    val measurementUnit: String,
    val amountPerPerson: Double,
    val pricePerUnit: Double
)

object ZakatFitrahDefaults {
    fun defaultDependents(count: Int = 4): List<ZakatFitrahDependent> {
        val templates = listOf(
            ZakatFitrahDependent("dep_1", "Kepala Keluarga", "Diri Sendiri", true),
            ZakatFitrahDependent("dep_2", "Pasangan / Istri", "Tanggungan Inti", true),
            ZakatFitrahDependent("dep_3", "Anak Pertama", "Anak Tanggungan", true),
            ZakatFitrahDependent("dep_4", "Anak Kedua", "Anak Tanggungan", true),
            ZakatFitrahDependent("dep_5", "Orang Tua / Tanggungan", "Tanggungan Nafkah", true)
        )
        return when {
            count <= 0 -> emptyList()
            count <= templates.size -> templates.take(count)
            else -> templates + (templates.size + 1..count).map { idx ->
                ZakatFitrahDependent("dep_$idx", "Anggota Keluarga #$idx", "Tanggungan Nafkah", true)
            }
        }
    }

    val STAPLE_FOOD_PRESETS: List<LocalStapleFoodPreset> = listOf(
        LocalStapleFoodPreset(
            id = "beras_medium_lokal",
            foodName = "Beras Medium (Lokal)",
            regionName = "Jawa & Nasional",
            measurementUnit = "kg",
            amountPerPerson = 2.5,
            pricePerUnit = 15000.0
        ),
        LocalStapleFoodPreset(
            id = "beras_premium_baznas",
            foodName = "Beras Premium (Pandan Wangi/Setra)",
            regionName = "Jabodetabek / Kota Besar",
            measurementUnit = "kg",
            amountPerPerson = 2.5,
            pricePerUnit = 18800.0
        ),
        LocalStapleFoodPreset(
            id = "beras_organik",
            foodName = "Beras Organik / Khusus",
            regionName = "Standar Konsumsi Tinggi",
            measurementUnit = "kg",
            amountPerPerson = 2.5,
            pricePerUnit = 22000.0
        ),
        LocalStapleFoodPreset(
            id = "beras_liter",
            foodName = "Beras Lokal (Takaran 3.5 Liter)",
            regionName = "Pasar Tradisional Setempat",
            measurementUnit = "liter",
            amountPerPerson = 3.5,
            pricePerUnit = 12500.0
        ),
        LocalStapleFoodPreset(
            id = "jagung_sagu",
            foodName = "Sagu / Jagung Makanan Pokok",
            regionName = "Indonesia Timur / Pesisir",
            measurementUnit = "kg",
            amountPerPerson = 2.5,
            pricePerUnit = 12000.0
        )
    )
}

data class ZakatFitrahFamilyCalculation(
    val familyMembersCount: Int = 4,
    val riceAmountPerPersonKg: Double = 2.5,
    val ricePricePerKg: Double = 15000.0,
    val cashRatePerPerson: Double = 45000.0,
    val isPaymentInCash: Boolean = true,
    val stapleFoodName: String = "Beras Medium (Lokal)",
    val regionName: String = "Jawa & Nasional",
    val measurementUnit: String = "kg",
    val useLocalStaplePrice: Boolean = true,
    val dependents: List<ZakatFitrahDependent> = ZakatFitrahDefaults.defaultDependents(familyMembersCount)
) {
    val activeDependentsCount: Int
        get() = if (dependents.isNotEmpty()) dependents.count { it.isIncluded } else familyMembersCount.coerceAtLeast(0)

    val familyMemberCount: Int get() = activeDependentsCount

    val localCalculatedCashPerSoul: Double
        get() = kotlin.math.round(riceAmountPerPersonKg * ricePricePerKg)

    val ratePerSoulRupiah: Double
        get() = if (useLocalStaplePrice) localCalculatedCashPerSoul else cashRatePerPerson

    val totalRiceKg: Double
        get() = activeDependentsCount * riceAmountPerPersonKg

    val totalCashAmount: Double
        get() = activeDependentsCount * (if (isPaymentInCash) ratePerSoulRupiah else localCalculatedCashPerSoul)

    val totalFitrahRupiah: Double get() = totalCashAmount
}

/**
 * Kalkulasi Zakat Maal (Simpanan, Tabungan, Emas, dan Aset Lancar)
 * Mengintegrasikan data saldo dompet/kas dan tabungan target ibadah langsung dari database.
 */
data class ZakatMaalCalculation(
    val totalWalletSavings: Double = 0.0,         // Saldo seluruh kantong/rekening/kas dari Room Database
    val totalIbadahSavings: Double = 0.0,         // Total akumulasi tabungan ibadah dari Room Database
    val goldWeightGrams: Double = 0.0,            // Emas batangan / perhiasan simpanan dalam gram
    val tradeableReceivables: Double = 0.0,       // Piutang lancar yang berpotensi cair (Qardh diberikan)
    val shortTermLiabilities: Double = 0.0,       // Hutang jatuh tempo yang harus dibayar (Qardh diterima)
    val goldPricePerGram: Double = 1350000.0,     // Standar harga emas per gram (acuan BAZNAS/Antam)
    val isLunarCalendar: Boolean = true,          // 2.5% Hijriyah (354 hari) vs 2.577% Masehi (365 hari)
    val isHaulFulfilled: Boolean = true,          // Masa kepemilikan telah genap 1 tahun (haul)
    val lastSyncedFromDatabaseMillis: Long = 0L,  // Timestamp terakhir sinkronisasi dari database
    val haulStartDateMillis: Long = 0L            // Tanggal awal harta mencapai nisab (pelacakan haul)
) {
    // Nilai emas simpanan dalam rupiah
    val preciousMetalsValue: Double get() = goldWeightGrams * goldPricePerGram

    // Total harta simpanan dan aset lancar bruto
    val totalGrossAssets: Double get() = totalWalletSavings + totalIbadahSavings + preciousMetalsValue + tradeableReceivables

    // Harta bersih kena zakat (dikurangi hutang jangka pendek jatuh tempo)
    val netTaxableAssets: Double get() = (totalGrossAssets - shortTermLiabilities).coerceAtLeast(0.0)

    // Ambang batas Nisab (setara 85 gram emas murni)
    val nisabGold85g: Double get() = 85.0 * goldPricePerGram

    // Status pencapaian nisab
    val isNisabReached: Boolean get() = netTaxableAssets >= nisabGold85g

    // Target jumlah hari haul (354 hari Hijriyah vs 365 hari Masehi)
    val haulTargetDays: Int get() = if (isLunarCalendar) 354 else 365

    fun elapsedHaulDays(nowMillis: Long = System.currentTimeMillis()): Int {
        if (haulStartDateMillis <= 0L) {
            return if (isHaulFulfilled) haulTargetDays else 0
        }
        return ((nowMillis - haulStartDateMillis).coerceAtLeast(0L) / (1000L * 60 * 60 * 24)).toInt()
    }

    fun remainingHaulDays(nowMillis: Long = System.currentTimeMillis()): Int {
        if (haulStartDateMillis <= 0L) {
            return if (isHaulFulfilled) 0 else haulTargetDays
        }
        return (haulTargetDays - elapsedHaulDays(nowMillis)).coerceAtLeast(0)
    }

    fun haulProgressRatio(nowMillis: Long = System.currentTimeMillis()): Float {
        if (haulTargetDays <= 0) return 1f
        return (elapsedHaulDays(nowMillis).toFloat() / haulTargetDays.toFloat()).coerceIn(0f, 1f)
    }

    // Status kewajiban zakat (harus mencapai nisab DAN haul 1 tahun terpenuhi)
    val isObligatory: Boolean get() = isNisabReached && isHaulFulfilled

    // Selisih kekurangan menuju nisab
    val shortageToNisab: Double get() = (nisabGold85g - netTaxableAssets).coerceAtLeast(0.0)

    // Tarif zakat (2.5% Hijriyah atau 2.577% Masehi)
    val zakatRate: Double get() = if (isLunarCalendar) 0.025 else 0.02577

    // Nominal zakat maal yang wajib dikeluarkan
    val zakatAmount: Double get() = if (isObligatory) kotlin.math.round(netTaxableAssets * zakatRate) else 0.0
}

package com.example.core.zakat

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

data class ZakatFitrahFamilyCalculation(
    val familyMembersCount: Int = 4,
    val riceAmountPerPersonKg: Double = 2.5,
    val ricePricePerKg: Double = 15000.0,
    val cashRatePerPerson: Double = 45000.0,
    val isPaymentInCash: Boolean = true
) {
    val familyMemberCount: Int get() = familyMembersCount
    val ratePerSoulRupiah: Double get() = cashRatePerPerson
    val totalRiceKg: Double = familyMembersCount * riceAmountPerPersonKg
    val totalCashAmount: Double = familyMembersCount * (if (isPaymentInCash) cashRatePerPerson else (riceAmountPerPersonKg * ricePricePerKg))
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
    val lastSyncedFromDatabaseMillis: Long = 0L    // Timestamp terakhir sinkronisasi dari database
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

    // Status kewajiban zakat (harus mencapai nisab DAN haul 1 tahun terpenuhi)
    val isObligatory: Boolean get() = isNisabReached && isHaulFulfilled

    // Selisih kekurangan menuju nisab
    val shortageToNisab: Double get() = (nisabGold85g - netTaxableAssets).coerceAtLeast(0.0)

    // Tarif zakat (2.5% Hijriyah atau 2.577% Masehi)
    val zakatRate: Double get() = if (isLunarCalendar) 0.025 else 0.02577

    // Nominal zakat maal yang wajib dikeluarkan
    val zakatAmount: Double get() = if (isObligatory) kotlin.math.round(netTaxableAssets * zakatRate) else 0.0
}



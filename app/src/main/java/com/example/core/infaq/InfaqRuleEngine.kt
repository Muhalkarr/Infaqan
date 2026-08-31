package com.example.core.infaq

import com.example.core.accounting.AccountCategory

enum class InfaqCalculationType(val displayName: String) {
    PERCENTAGE("Persentase Kustom"),
    ROUND_UP("Pembulatan Belanja (Round-Up)"),
    FLAT("Nominal Tetap / Sedekah Rutin"),
    RIKAZ("Harta Karun / Rikaz (20%)"),
    SYUBHAT("Karantina Syubhat (100%)")
}

data class InfaqRule(
    val id: String,
    val title: String,
    val targetCategory: AccountCategory,
    val calculationType: InfaqCalculationType,
    val rate: Double = 0.05,
    val fixedAmount: Double = 0.0,
    val roundUpStep: Double = 5000.0,
    val enableFridayMultiplier: Boolean = true,
    val enableRamadanMultiplier: Boolean = true
) {
    fun calculateObligation(
        amount: Double,
        isFriday: Boolean,
        isRamadan: Boolean
    ): Double {
        val base = when (calculationType) {
            InfaqCalculationType.PERCENTAGE -> amount * rate
            InfaqCalculationType.ROUND_UP -> {
                if (roundUpStep <= 0.0) 0.0
                else {
                    val remainder = amount % roundUpStep
                    if (remainder == 0.0) 0.0 else roundUpStep - remainder
                }
            }
            InfaqCalculationType.FLAT -> fixedAmount
            InfaqCalculationType.RIKAZ -> amount * 0.20 // 20% Rikaz
            InfaqCalculationType.SYUBHAT -> amount * 1.00 // 100% Karantina Fasilitas Umum
        }

        var multiplier = 1.0
        if (isFriday && enableFridayMultiplier && calculationType == InfaqCalculationType.PERCENTAGE) {
            multiplier += 0.5 // +50% Berkah Jumat
        }
        if (isRamadan && enableRamadanMultiplier && calculationType == InfaqCalculationType.PERCENTAGE) {
            multiplier += 1.0 // 2x lipat bulan suci Ramadan
        }

        return base * multiplier
    }
}

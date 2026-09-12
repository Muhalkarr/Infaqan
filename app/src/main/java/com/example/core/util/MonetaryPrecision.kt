package com.example.core.util

import kotlin.math.round

/**
 * Utilitas presisi kalkulasi moneter syariah.
 * Mencegah akumulasi residu desimal mikroskopis IEEE 754 floating-point (misal 10000.000000000002)
 * dengan membulatkan nilai ke satuan bulat Rupiah terdekat secara konsisten di seluruh aplikasi.
 */
object MonetaryPrecision {

    /**
     * Membulatkan nilai Double ke integer Rupiah terdekat.
     */
    fun roundRupiah(amount: Double): Double {
        return round(amount)
    }

    /**
     * Mengonversi nilai Double ke Long Rupiah bulat.
     */
    fun toRupiahLong(amount: Double): Long {
        return round(amount).toLong()
    }

    /**
     * Menghitung persentase infaq atau zakat dengan presisi bulat Rupiah.
     */
    fun calculatePercentage(baseAmount: Double, rate: Double): Double {
        return round(baseAmount * rate)
    }

    /**
     * Menghitung selisih pembulatan belanja (round-up) dengan presisi bulat.
     */
    fun calculateRoundUp(amount: Double, roundStep: Double = 5000.0): Double {
        if (amount <= 0.0 || roundStep <= 0.0) return 0.0
        val cleanAmount = round(amount)
        val remainder = cleanAmount % roundStep
        return if (remainder == 0.0) 0.0 else round(roundStep - remainder)
    }
}

fun Double.roundRupiah(): Double = MonetaryPrecision.roundRupiah(this)
fun Double.toRupiahLong(): Long = MonetaryPrecision.toRupiahLong(this)

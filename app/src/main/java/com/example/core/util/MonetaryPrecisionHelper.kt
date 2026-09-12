package com.example.core.util

import kotlin.math.round

/**
 * Utilitas pembulatan presisi moneter syariah untuk mata uang Rupiah (IDR).
 * Mencegah akumulasi residu desimal mikroskopis (IEEE 754 floating-point drift)
 * pada komputasi persentase infaq, round-up, bagi hasil, dan mutasi saldo.
 */
object MonetaryPrecisionHelper {

    /**
     * Membulatkan nilai nominal ke bilangan bulat Rupiah terdekat.
     */
    fun roundRupiah(amount: Double): Double {
        if (amount.isNaN() || amount.isInfinite()) return 0.0
        return round(amount)
    }

    /**
     * Menghitung nilai persentase dan membulatkannya ke satuan Rupiah terdekat.
     */
    fun roundPercentage(amount: Double, rate: Double): Double {
        return roundRupiah(amount * rate)
    }

    /**
     * Memastikan selisih antara debet dan kredit tidak meninggalkan residu pecahan.
     */
    fun sanitizeDouble(amount: Double): Double {
        return round(amount * 100.0) / 100.0
    }
}

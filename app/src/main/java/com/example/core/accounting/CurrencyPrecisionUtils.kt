package com.example.core.accounting

import kotlin.math.round

/**
 * Utilitas Presisi Numerik Moneter Syariah
 * Mencegah akumulasi residu desimal IEEE 754 floating-point
 * dengan menjamin pembulatan bulat pada setiap kalkulasi Rupiah.
 */
object CurrencyPrecisionUtils {

    /**
     * Membulatkan nilai moneter ke Rupiah utuh terdekat (Double bulat tanpa pecahan desimal)
     */
    fun roundRupiah(value: Double): Double {
        if (value.isNaN() || value.isInfinite()) return 0.0
        return round(value)
    }

    /**
     * Mengonversi dan membulatkan ke Long untuk penyimpanan dan komputasi satuan terkecil
     */
    fun roundRupiahToLong(value: Double): Long {
        if (value.isNaN() || value.isInfinite()) return 0L
        return round(value).toLong()
    }

    /**
     * Menghitung potongan Infaq/Sedekah otomatis berbasis persentase secara presisi
     */
    fun calculateInfaq(grossAmount: Double, rate: Double): Double {
        if (grossAmount <= 0.0 || rate <= 0.0) return 0.0
        return round(grossAmount * rate)
    }

    /**
     * Menghitung pembulatan transaksi belanja (Round-up Infaq) ke kelipatan terdekat
     */
    fun calculateRoundUp(amount: Double, step: Double): Double {
        if (amount <= 0.0 || step <= 0.0) return 0.0
        val rem = amount % step
        return if (rem > 0.0) round(step - rem) else 0.0
    }

    /**
     * Menghitung kewajiban Zakat Maal / Profesi / Tijarah (2.5% atau rate kustom)
     */
    fun calculateZakat(taxableBase: Double, rate: Double = 0.025): Double {
        if (taxableBase <= 0.0 || rate <= 0.0) return 0.0
        return round(taxableBase * rate)
    }
}

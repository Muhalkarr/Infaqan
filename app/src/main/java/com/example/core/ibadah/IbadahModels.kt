package com.example.core.ibadah

import com.example.core.receipt.ReceiptAttachment
import java.util.Date
import java.util.UUID

enum class GoalCategory(val displayName: String, val badgeText: String) {
    IBADAH("Ibadah", "Ibadah & Spiritual"),
    DANA_DARURAT("Dana Darurat", "Jaga-Jaga & Medis"),
    PENDIDIKAN("Pendidikan", "Pendidikan & Karir"),
    RUMAH("Rumah", "Papan & Hunian"),
    KENDARAAN("Kendaraan", "Transportasi"),
    BISNIS("Bisnis", "Usaha & Modal"),
    PRIBADI("Pribadi", "Gaya Hidup & Hajat")
}

enum class IbadahGoalType(
    val title: String,
    val categoryBadge: String,
    val defaultEstimate: Double,
    val category: GoalCategory = GoalCategory.IBADAH
) {
    // 1. IBADAH
    QURBAN_KAMBING("Qurban 1 Ekor Kambing / Domba", "Idul Adha", 3500000.0, GoalCategory.IBADAH),
    QURBAN_SAPI_PATUNGAN("Qurban 1/7 Bagian Sapi", "Idul Adha", 3800000.0, GoalCategory.IBADAH),
    QURBAN_SAPI_1_EKOR("Qurban 1 Ekor Sapi Utuh", "Idul Adha", 26000000.0, GoalCategory.IBADAH),
    SETORAN_AWAL_HAJI("Setoran Awal Porsi Haji (BPIH)", "Haji Mabrur", 25000000.0, GoalCategory.IBADAH),
    PAKET_UMRAH_MUKMIN("Tabungan Paket Umrah Syariah", "Baitullah", 32000000.0, GoalCategory.IBADAH),
    WAKAF_PRODUKTIF("Wakaf Masjid & Lembaga Dakwah", "Amal Jariyah", 5000000.0, GoalCategory.IBADAH),
    CUSTOM_IBADAH("Rencana Ibadah Kustom", "Hajat Mukmin", 10000000.0, GoalCategory.IBADAH),

    // 2. DANA DARURAT
    DANA_DARURAT_3_BULAN("Dana Darurat 3 Bulan Pengeluaran", "Dana Darurat", 15000000.0, GoalCategory.DANA_DARURAT),
    DANA_DARURAT_6_BULAN("Dana Darurat 6 Bulan Pengeluaran", "Dana Darurat", 30000000.0, GoalCategory.DANA_DARURAT),
    TABUNGAN_KESEHATAN("Tabungan Kesehatan & Medis Darurat", "Kesehatan", 10000000.0, GoalCategory.DANA_DARURAT),

    // 3. PENDIDIKAN
    BIAYA_SEKOLAH_ANAK("Biaya Masuk Sekolah / Kuliah", "Pendidikan", 20000000.0, GoalCategory.PENDIDIKAN),
    PELATIHAN_SERTIFIKASI("Kursus, Sertifikasi & Buku", "Pengembangan Diri", 5000000.0, GoalCategory.PENDIDIKAN),

    // 4. RUMAH
    DP_RUMAH_IDAMAN("DP Rumah / KPR Syariah", "Properti", 50000000.0, GoalCategory.RUMAH),
    RENOVASI_RUMAH("Renovasi & Perbaikan Hunian", "Renovasi", 25000000.0, GoalCategory.RUMAH),
    PERLENGKAPAN_RUMAH("Perabotan & Elektronik Rumah", "Perlengkapan", 10000000.0, GoalCategory.RUMAH),

    // 5. KENDARAAN
    BELI_MOTOR_TUNAI("Beli Motor Tunai Tanpa Riba", "Kendaraan", 22000000.0, GoalCategory.KENDARAAN),
    DP_MOBIL_HALAL("Tabungan Beli Mobil Tunai/Syariah", "Kendaraan", 80000000.0, GoalCategory.KENDARAAN),
    SERVIS_PERAWATAN_KENDARAAN("Perawatan Besar & Pajak Kendaraan", "Pemeliharaan", 5000000.0, GoalCategory.KENDARAAN),

    // 6. BISNIS
    MODAL_USAHA_BAROKAH("Modal Awal Usaha Barokah", "Wirausaha", 25000000.0, GoalCategory.BISNIS),
    PENGADAAN_STOK_USAHA("Stok Dagangan & Bahan Baku", "Operasional", 15000000.0, GoalCategory.BISNIS),
    ALAT_PRODUKSI_BISNIS("Peralatan & Mesin Usaha", "Aset Bisnis", 20000000.0, GoalCategory.BISNIS),

    // 7. PRIBADI
    PERNIKAHAN_SYARI("Tabungan Walimah & Pernikahan", "Pernikahan", 35000000.0, GoalCategory.PRIBADI),
    LIBURAN_KELUARGA("Wisata Halal & Silaturahmi Keluarga", "Liburan", 12000000.0, GoalCategory.PRIBADI),
    RENCANA_PRIBADI_LAINNYA("Rencana Impian Pribadi Lainnya", "Cita-Cita", 10000000.0, GoalCategory.PRIBADI);

    val displayName: String
        get() = title
}

data class IbadahGoal(
    val id: String = UUID.randomUUID().toString(),
    val type: IbadahGoalType = IbadahGoalType.QURBAN_KAMBING,
    val category: GoalCategory = type.category,
    val title: String = type.title,
    val targetAmount: Double = type.defaultEstimate,
    val currentAccumulated: Double = 0.0,
    val targetHijriYearMonth: String = "10 Dzulhijjah 1448 H",
    val targetMonthsRemaining: Int = 10,
    val linkedWalletId: String = "acc_bank",
    val notes: String = "",
    val isCompleted: Boolean = false
) {
    val progressRatio: Float
        get() = if (targetAmount <= 0.0) 0f else (currentAccumulated / targetAmount).toFloat().coerceIn(0f, 1f)

    val remainingAmount: Double
        get() = (targetAmount - currentAccumulated).coerceAtLeast(0.0)

    val recommendedMonthlySaving: Double
        get() = if (targetMonthsRemaining <= 0) remainingAmount else remainingAmount / targetMonthsRemaining.coerceAtLeast(1)
}

data class IbadahDepositRecord(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amount: Double,
    val sourceWalletId: String = "acc_bank",
    val fromWalletId: String = sourceWalletId,
    val depositDate: Date = Date(),
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = "",
    val note: String = notes,
    val receipt: ReceiptAttachment? = null
)

package com.example.core.accounting

/**
 * Pilihan preset cepat untuk membatasi atau mengaktifkan kategori pengeluaran
 * guna mengurangi kelelahan memilih (decision fatigue) saat mencatat transaksi harian.
 */
enum class ExpenseCategoryPreset(
    val title: String,
    val subtitle: String,
    val description: String
) {
    MINIMAL(
        title = "Ringkas (4)",
        subtitle = "Esensial",
        description = "Hanya 4 kategori primer: Pangan, Transportasi, Tagihan, dan Lainnya"
    ),
    FAMILY(
        title = "Keluarga (6)",
        subtitle = "Rumah Tangga",
        description = "6 kategori pokok: Pangan, Transport, Tagihan, Pendidikan, Kesehatan, dan Lainnya"
    ),
    FULL(
        title = "Lengkap (Semua)",
        subtitle = "Seluruh Kategori",
        description = "Aktifkan seluruh 9 kategori syariah bawaan serta akun kustom"
    )
}

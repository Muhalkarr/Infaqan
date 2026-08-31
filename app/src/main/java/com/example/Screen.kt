package com.example

enum class Screen(val title: String) {
    DASHBOARD("Beranda & Ringkasan"),
    ADD_TRANSACTION("Catat Transaksi"),
    BUDGET_ALLOCATION("Alokasi Anggaran"),
    ANALYTICS("Analisis Grafik & Arus Kas"),
    HAUL_NISAB("Haul & Nisab Zakat"),
    INFAQ_RULES("Aturan Infaq Otomatis"),
    RECURRING_TRANSACTIONS("Transaksi Berulang"),
    MONTHLY_REPORT("Laporan Bulanan & PDF"),
    VAULT_HISTORY("Riwayat Penyaluran Infaq"),
    SEDEKAH_SUBUH("Sedekah Subuh Berantai"),
    INTERACTIVE_GUIDE("Panduan Syariah"),
    SECURITY_SETTINGS("Keamanan & PIN"),
    CENTRAL_SETTINGS("Pengaturan Kas Mukmin"),
    MULTI_WALLET("Multi-Wallet Kas"),
    IBADAH_GOALS("Target Tabungan Ibadah"),
    ZAKAT_HUB("Zakat Hub & Penyaluran"),
    BACKUP_RESTORE("Cadangan & Pemulihan"),
    QARDH("Hutang Piutang Syariah"),
    AMIL_DIRECTORY("Direktori Lembaga Amil"),
    FARAIDH_CALCULATOR("Kalkulator Waris Faraidh"),
    EXPORT_REPORT("Ekspor Laporan Resmi")
}

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
    EXPORT_REPORT("Ekspor Laporan Resmi"),
    ISLAMIC_GROUNDING("Fatwa & Fiqih Muamalah");

    fun toRoute(): String = when (this) {
        DASHBOARD -> com.example.navigation.AmanahRoutes.DASHBOARD
        ADD_TRANSACTION -> com.example.navigation.AmanahRoutes.ADD_TRANSACTION
        BUDGET_ALLOCATION -> com.example.navigation.AmanahRoutes.BUDGET_ALLOCATION
        ANALYTICS -> com.example.navigation.AmanahRoutes.ANALYTICS
        HAUL_NISAB -> com.example.navigation.AmanahRoutes.HAUL_NISAB
        INFAQ_RULES -> com.example.navigation.AmanahRoutes.INFAQ_RULES
        RECURRING_TRANSACTIONS -> com.example.navigation.AmanahRoutes.RECURRING_TRANSACTIONS
        MONTHLY_REPORT -> com.example.navigation.AmanahRoutes.MONTHLY_REPORT
        VAULT_HISTORY -> com.example.navigation.AmanahRoutes.VAULT_HISTORY
        SEDEKAH_SUBUH -> com.example.navigation.AmanahRoutes.SEDEKAH_SUBUH
        INTERACTIVE_GUIDE -> com.example.navigation.AmanahRoutes.INTERACTIVE_GUIDE
        SECURITY_SETTINGS -> com.example.navigation.AmanahRoutes.SECURITY_SETTINGS
        CENTRAL_SETTINGS -> com.example.navigation.AmanahRoutes.CENTRAL_SETTINGS
        MULTI_WALLET -> com.example.navigation.AmanahRoutes.MULTI_WALLET
        IBADAH_GOALS -> com.example.navigation.AmanahRoutes.IBADAH_GOALS
        ZAKAT_HUB -> com.example.navigation.AmanahRoutes.ZAKAT_HUB
        BACKUP_RESTORE -> com.example.navigation.AmanahRoutes.BACKUP_RESTORE
        QARDH -> com.example.navigation.AmanahRoutes.QARDH
        AMIL_DIRECTORY -> com.example.navigation.AmanahRoutes.AMIL_DIRECTORY
        FARAIDH_CALCULATOR -> com.example.navigation.AmanahRoutes.FARAIDH
        EXPORT_REPORT -> com.example.navigation.AmanahRoutes.EXPORT_REPORT
        ISLAMIC_GROUNDING -> com.example.navigation.AmanahRoutes.ISLAMIC_GROUNDING
    }

    companion object {
        fun fromRoute(route: String?): Screen = when (route) {
            com.example.navigation.AmanahRoutes.DASHBOARD -> DASHBOARD
            com.example.navigation.AmanahRoutes.ADD_TRANSACTION -> ADD_TRANSACTION
            com.example.navigation.AmanahRoutes.BUDGET_ALLOCATION -> BUDGET_ALLOCATION
            com.example.navigation.AmanahRoutes.ANALYTICS -> ANALYTICS
            com.example.navigation.AmanahRoutes.HAUL_NISAB -> HAUL_NISAB
            com.example.navigation.AmanahRoutes.INFAQ_RULES -> INFAQ_RULES
            com.example.navigation.AmanahRoutes.RECURRING_TRANSACTIONS -> RECURRING_TRANSACTIONS
            com.example.navigation.AmanahRoutes.MONTHLY_REPORT, com.example.navigation.AmanahRoutes.LEDGER -> MONTHLY_REPORT
            com.example.navigation.AmanahRoutes.VAULT_HISTORY -> VAULT_HISTORY
            com.example.navigation.AmanahRoutes.SEDEKAH_SUBUH -> SEDEKAH_SUBUH
            com.example.navigation.AmanahRoutes.INTERACTIVE_GUIDE -> INTERACTIVE_GUIDE
            com.example.navigation.AmanahRoutes.SECURITY_SETTINGS -> SECURITY_SETTINGS
            com.example.navigation.AmanahRoutes.CENTRAL_SETTINGS -> CENTRAL_SETTINGS
            com.example.navigation.AmanahRoutes.MULTI_WALLET -> MULTI_WALLET
            com.example.navigation.AmanahRoutes.IBADAH_GOALS -> IBADAH_GOALS
            com.example.navigation.AmanahRoutes.ZAKAT_HUB, com.example.navigation.AmanahRoutes.INFAQ_VAULT -> ZAKAT_HUB
            com.example.navigation.AmanahRoutes.BACKUP_RESTORE -> BACKUP_RESTORE
            com.example.navigation.AmanahRoutes.QARDH -> QARDH
            com.example.navigation.AmanahRoutes.AMIL_DIRECTORY -> AMIL_DIRECTORY
            com.example.navigation.AmanahRoutes.FARAIDH -> FARAIDH_CALCULATOR
            com.example.navigation.AmanahRoutes.EXPORT_REPORT -> EXPORT_REPORT
            com.example.navigation.AmanahRoutes.ISLAMIC_GROUNDING -> ISLAMIC_GROUNDING
            else -> DASHBOARD
        }
    }
}

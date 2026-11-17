package com.example

enum class Screen(
    val title: String,
    val isExperimental: Boolean = false,
    val experimentalNotice: String? = null
) {
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
    NOTIFICATION_SETTINGS("Pengaturan Notifikasi"),
    CENTRAL_SETTINGS("Pengaturan Kas Mukmin"),
    MULTI_WALLET("Multi-Wallet Kas"),
    IBADAH_GOALS("Target Finansial dan Ibadah"),
    ZAKAT_HUB("Zakat Hub & Penyaluran"),
    ZAKAT_REPORT("Laporan Zakat Maal & Fitrah"),
    BACKUP_RESTORE("Cadangan & Pemulihan"),
    QARDH("Hutang Piutang Syariah"),
    AMIL_DIRECTORY("Direktori Lembaga Amil"),
    FARAIDH_CALCULATOR(
        title = "Kalkulator Waris Faraidh",
        isExperimental = true,
        experimentalNotice = "Simulasi pembagian waris syar'i untuk edukasi awal. Wajib dikonfirmasikan ke Pengadilan Agama atau Ulama Faraidh sebelum eksekusi pembagian harta riil."
    ),
    EXPORT_REPORT("Ekspor Laporan Resmi"),
    ISLAMIC_GROUNDING("Fatwa & Fiqih Muamalah"),
    SHARIAH_RULES_CUSTOMIZATION(
        title = "Kustomisasi Logika Syariah",
        isExperimental = true,
        experimentalNotice = "Pengaturan aturan hisab dan parameter kustom. Penyesuaian di modul ini berdampak langsung pada seluruh formula zakat dan infaq di akun Anda."
    ),
    RECEIPT_ARCHIVE(
        title = "Arsip Bukti Struk",
        isExperimental = true,
        experimentalNotice = "Ekstraksi data struk/nota digital dipengaruhi kejernihan foto. Mohon selalu verifikasi ulang nominal dan tanggal transaksi sebelum dicatat."
    ),
    DEBUG_TERMINAL("Terminal Log & Diagnostik"),
    GOLD_NISAB_TRACKER(
        title = "Pelacak Emas & Nisab BAZNAS",
        isExperimental = true,
        experimentalNotice = "Fluktuasi harga emas dan nisab BAZNAS bersifat estimasi pasar. Selalu periksa harga emas batangan fisik resmi (Antam/Pegadaian) saat haul zakat tiba."
    ),
    BUSINESS_ZAKAT(
        title = "Zakat Bisnis & Saham Syariah",
        isExperimental = true,
        experimentalNotice = "Perhitungan zakat perniagaan dan portofolio berbasis estimasi aktiva lancar dan utang lancar. Periksa kesesuaian dengan laporan keuangan usaha Anda."
    ),
    SHARIAH_AUDIT(
        title = "Audit Kepatuhan Syariah",
        isExperimental = true,
        experimentalNotice = "Pemindaian transaksi berbasis analisis heuristik otomatis untuk mendeteksi anomali syubhat atau riba. Keputusan akhir tetap pada ketelitian pengguna."
    ),
    DUAL_CALENDAR("Kalender Hijriyah & Masehi");

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
        NOTIFICATION_SETTINGS -> com.example.navigation.AmanahRoutes.NOTIFICATION_SETTINGS
        CENTRAL_SETTINGS -> com.example.navigation.AmanahRoutes.CENTRAL_SETTINGS
        MULTI_WALLET -> com.example.navigation.AmanahRoutes.MULTI_WALLET
        IBADAH_GOALS -> com.example.navigation.AmanahRoutes.IBADAH_GOALS
        ZAKAT_HUB -> com.example.navigation.AmanahRoutes.ZAKAT_HUB
        ZAKAT_REPORT -> com.example.navigation.AmanahRoutes.ZAKAT_REPORT
        BACKUP_RESTORE -> com.example.navigation.AmanahRoutes.BACKUP_RESTORE
        QARDH -> com.example.navigation.AmanahRoutes.QARDH
        AMIL_DIRECTORY -> com.example.navigation.AmanahRoutes.AMIL_DIRECTORY
        FARAIDH_CALCULATOR -> com.example.navigation.AmanahRoutes.FARAIDH
        EXPORT_REPORT -> com.example.navigation.AmanahRoutes.EXPORT_REPORT
        ISLAMIC_GROUNDING -> com.example.navigation.AmanahRoutes.ISLAMIC_GROUNDING
        SHARIAH_RULES_CUSTOMIZATION -> com.example.navigation.AmanahRoutes.SHARIAH_RULES_CUSTOMIZATION
        RECEIPT_ARCHIVE -> com.example.navigation.AmanahRoutes.RECEIPT_ARCHIVE
        DEBUG_TERMINAL -> com.example.navigation.AmanahRoutes.DEBUG_TERMINAL
        GOLD_NISAB_TRACKER -> com.example.navigation.AmanahRoutes.GOLD_NISAB_TRACKER
        BUSINESS_ZAKAT -> com.example.navigation.AmanahRoutes.BUSINESS_ZAKAT
        SHARIAH_AUDIT -> com.example.navigation.AmanahRoutes.SHARIAH_AUDIT
        DUAL_CALENDAR -> com.example.navigation.AmanahRoutes.DUAL_CALENDAR
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
            com.example.navigation.AmanahRoutes.NOTIFICATION_SETTINGS -> NOTIFICATION_SETTINGS
            com.example.navigation.AmanahRoutes.CENTRAL_SETTINGS -> CENTRAL_SETTINGS
            com.example.navigation.AmanahRoutes.MULTI_WALLET -> MULTI_WALLET
            com.example.navigation.AmanahRoutes.IBADAH_GOALS -> IBADAH_GOALS
            com.example.navigation.AmanahRoutes.ZAKAT_HUB, com.example.navigation.AmanahRoutes.INFAQ_VAULT -> ZAKAT_HUB
            com.example.navigation.AmanahRoutes.ZAKAT_REPORT -> ZAKAT_REPORT
            com.example.navigation.AmanahRoutes.BACKUP_RESTORE -> BACKUP_RESTORE
            com.example.navigation.AmanahRoutes.QARDH -> QARDH
            com.example.navigation.AmanahRoutes.AMIL_DIRECTORY -> AMIL_DIRECTORY
            com.example.navigation.AmanahRoutes.FARAIDH -> FARAIDH_CALCULATOR
            com.example.navigation.AmanahRoutes.EXPORT_REPORT -> EXPORT_REPORT
            com.example.navigation.AmanahRoutes.ISLAMIC_GROUNDING -> ISLAMIC_GROUNDING
            com.example.navigation.AmanahRoutes.SHARIAH_RULES_CUSTOMIZATION -> SHARIAH_RULES_CUSTOMIZATION
            com.example.navigation.AmanahRoutes.RECEIPT_ARCHIVE -> RECEIPT_ARCHIVE
            com.example.navigation.AmanahRoutes.DEBUG_TERMINAL -> DEBUG_TERMINAL
            com.example.navigation.AmanahRoutes.GOLD_NISAB_TRACKER -> GOLD_NISAB_TRACKER
            com.example.navigation.AmanahRoutes.BUSINESS_ZAKAT -> BUSINESS_ZAKAT
            com.example.navigation.AmanahRoutes.SHARIAH_AUDIT -> SHARIAH_AUDIT
            com.example.navigation.AmanahRoutes.DUAL_CALENDAR -> DUAL_CALENDAR
            else -> DASHBOARD
        }
    }
}

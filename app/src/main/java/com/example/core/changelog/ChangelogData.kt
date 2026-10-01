package com.example.core.changelog

data class ChangelogVersion(
    val version: String,
    val releaseDate: String,
    val title: String,
    val isLatest: Boolean = false,
    val highlights: List<String>,
    val tag: String // "Major", "Feature", "Stabil", "Inisiasi"
)

object ChangelogRegistry {
    val versions: List<ChangelogVersion> = listOf(
        ChangelogVersion(
            version = "v2.5",
            releaseDate = "September 2026",
            title = "Kamera Struk, Identitas Muhalkarr, Shake Feedback & Perbaikan Numpad",
            isLatest = true,
            tag = "Pembaruan Terkini",
            highlights = listOf(
                "📸 Integrasi Kamera Struk Belanja Langsung: Memotret nota/struk kas fisik saat pencatatan pengeluaran dengan kompresi hemat memori otomatis.",
                "✍️ Pembaruan Identitas Pengembang: Atribusi resmi developer diperbarui menjadi Muhalkarr.",
                "📱 Fitur Goyang Ponsel (Shake-to-Feedback): Akses cepat masukan pengguna dan ide perbaikan hanya dengan menggoyangkan ponsel Anda.",
                "📜 Log Perubahan Terintegrasi: Riwayat komprehensif evolusi Amanah Ledger sedari versi awal hingga rilis sekarang.",
                "🔢 Perbaikan Tombol 000 Numpad: Evaluasi string diperbaiki sehingga input kelipatan ribuan tidak lagi tertelan menjadi nol tunggal."
            )
        ),
        ChangelogVersion(
            version = "v2.4",
            releaseDate = "Agustus 2026",
            title = "Multi-Widget Beranda, Peringatan Israf & Audit Syariah Real-Time",
            isLatest = false,
            tag = "Stabil",
            highlights = listOf(
                "📊 App Widget Beranda: Tiga widget interaktif untuk Sedekah Subuh kilat, saldo kas ringkas, dan progres Ibadah Goals.",
                "⚠️ Sistem Deteksi Israf Real-Time: Peringatan dini jika pengeluaran kebutuhan tersier mendekati ambang batas anggaran bulanan.",
                "🛡️ Fitur Sensor Privasi Saldo (Masking): Perlindungan kerahasiaan nominal saat membuka aplikasi di tempat ramai."
            )
        ),
        ChangelogVersion(
            version = "v2.2",
            releaseDate = "Juli 2026",
            title = "Kuitansi Struk Fisik, Transaksi Berulang & Backup PDF Terenkripsi",
            isLatest = false,
            tag = "Fitur",
            highlights = listOf(
                "🧾 Manajemen Arsip Bukti Transaksi: Penyimpanan foto kuitansi lokal terstruktur dengan pencarian nomor referensi.",
                "🔄 Transaksi Berulang (Recurring): Otomasi pengingat dan eksekusi tagihan rutin syariah mingguan/bulanan.",
                "📑 Ekspor Laporan Finansial PDF & Cadangan Database SQLite terenkripsi."
            )
        ),
        ChangelogVersion(
            version = "v2.0",
            releaseDate = "Mei 2026",
            title = "Ekosistem Fiqh Syariah: Kalkulator Faraidh & Infaq Round-Up Vault",
            isLatest = false,
            tag = "Rilis Mayor",
            highlights = listOf(
                "⚖️ Modul Faraidh (Waris Islam): Perhitungan hak waris komprehensif (Ashabah, Dzawil Furudh, Kalalah) berlandaskan Al-Qur'an Surat An-Nisa.",
                "💰 Infaq Round-Up (Virtual Vault): Pembulatan sisa transaksi belanja harian ke celengan infaq otomatis.",
                "🌙 Islamic Knowledge Grounding: Basis pengetahuan fiqh muamalah, fatwa DSN-MUI, dan edukasi harta non-halal."
            )
        ),
        ChangelogVersion(
            version = "v1.5",
            releaseDate = "Maret 2026",
            title = "Qardh Al-Hasan, Sedekah Subuh Celengan Digital & Keamanan PIN",
            isLatest = false,
            tag = "Fitur",
            highlights = listOf(
                "🤝 Modul Qardh Al-Hasan: Pencatatan pinjaman kebajikan tanpa bunga dan tanpa denda keterlambatan.",
                "🌅 Sedekah Subuh 40 Hari: Tracker celengan sedekah harian dengan hadits pengingat doa malaikat di waktu fajar.",
                "🔒 Keamanan Berlapis: Autentikasi biometrik fingerprint dan enkripsi PIN lokal."
            )
        ),
        ChangelogVersion(
            version = "v1.0",
            releaseDate = "Januari 2026",
            title = "Zakat Hub, Multi-Dompet & Fondasi Akuntansi Syariah",
            isLatest = false,
            tag = "Rilis Mayor",
            highlights = listOf(
                "🕌 Zakat Hub Terpadu: Kalkulator Zakat Maal (Nisab 85g Emas Antam), Zakat Fitrah keluarga, Zakat Profesi, dan Zakat Perniagaan.",
                "💳 Multi-Dompet Terpisah: Pengelolaan dompet kas tunai, rekening bank syariah, dan e-wallet secara mandiri.",
                "📖 Direktori Lembaga Amil Zakat resmi terverifikasi BAZNAS."
            )
        ),
        ChangelogVersion(
            version = "v0.1",
            releaseDate = "Desember 2025",
            title = "Inisiasi Alpha & Buku Kas Syariah Sederhana",
            isLatest = false,
            tag = "Inisiasi",
            highlights = listOf(
                "📝 Buku Kas Masuk & Kas Keluar Berbasis Syariah (Amanah Ledger).",
                "🏷️ Pemisahan Transaksi Kasab (Penghasilan Usaha Halal) dan Non-Kasab.",
                "🧮 Kalkulator Nominal Numpad Transaksi Pertama."
            )
        )
    )
}

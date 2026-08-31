package com.example.core.directory

enum class AmilCategory(val title: String) {
    BAZNAS("BAZNAS (Badan Amil Zakat Nasional)"),
    LAZ_NASIONAL("LAZ Nasional Terakreditasi"),
    LEMBAGA_WAKAF("Lembaga Pengelola Wakaf Uang")
}

data class AmilBankAccount(
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val category: String = "Zakat / Infaq"
)

data class AmilInstitution(
    val id: String,
    val name: String,
    val shortName: String,
    val category: AmilCategory,
    val skLegalNumber: String,
    val verifiedBadge: String,
    val description: String,
    val websiteUrl: String,
    val callCenterWhatsapp: String,
    val address: String,
    val supportedPrograms: List<String>,
    val bankAccounts: List<AmilBankAccount>,
    val confirmationGuide: String
)

object AmilDirectoryRepository {
    val verifiedInstitutions: List<AmilInstitution> = listOf(
        AmilInstitution(
            id = "baznas_ri",
            name = "Badan Amil Zakat Nasional (BAZNAS RI)",
            shortName = "BAZNAS RI",
            category = AmilCategory.BAZNAS,
            skLegalNumber = "UU No. 23 Tahun 2011 / Kepres RI",
            verifiedBadge = "Lembaga Resmi Pemerintah",
            description = "Lembaga pemerintah nonstruktural yang berwenang mengelola zakat secara nasional dengan transparansi dan audit syariah resmi.",
            websiteUrl = "https://baznas.go.id",
            callCenterWhatsapp = "+628111555555",
            address = "Jl. Matraman Raya No.134, Jakarta Timur",
            supportedPrograms = listOf("Zakat Maal", "Zakat Profesi", "Zakat Fitrah", "Infaq Kemanusiaan", "Beasiswa Cendekia", "Kafalah Mustahiq"),
            bankAccounts = listOf(
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "1001001004", "BAZNAS REK ZAKAT", "Zakat"),
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "2002002005", "BAZNAS REK INFAQ", "Infaq / Sedekah"),
                AmilBankAccount("Bank Muamalat", "3010070752", "BAZNAS ZAKAT NASIONAL", "Zakat"),
                AmilBankAccount("BCA Syariah", "0010001000", "BAZNAS RI ZAKAT", "Zakat")
            ),
            confirmationGuide = "Kirim bukti transfer ke WhatsApp Layanan BAZNAS RI untuk memperoleh Bukti Setor Zakat (BSZ) resmi pengurang pajak penghasilan."
        ),
        AmilInstitution(
            id = "dompet_dhuafa",
            name = "Dompet Dhuafa Republika",
            shortName = "Dompet Dhuafa",
            category = AmilCategory.LAZ_NASIONAL,
            skLegalNumber = "SK Kemenag RI No. 439 Tahun 2016",
            verifiedBadge = "LAZ Nasional Resmi",
            description = "Lembaga Filantropi Islam terkemuka yang berfokus pada pemberdayaan kaum dhuafa melalui program pendidikan, kesehatan, ekonomi, dan kebencanaan.",
            websiteUrl = "https://dompetdhuafa.org",
            callCenterWhatsapp = "+628111617101",
            address = "Gedung Philanthropy, Jl. Warung Jati Barat No.14, Pasar Minggu, Jakarta Selatan",
            supportedPrograms = listOf("Zakat Profesi", "Zakat Perniagaan", "Infaq Kesehatan Gratis", "Sekolah Guru Indonesia", "Kemitraan UMKM Dhuafa"),
            bankAccounts = listOf(
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "3403506665", "Yayasan Dompet Dhuafa Republika", "Zakat"),
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "2881115555", "Yayasan Dompet Dhuafa Republika", "Infaq Kemanusiaan"),
                AmilBankAccount("Bank Muamalat", "3040031122", "Dompet Dhuafa Republika", "Zakat"),
                AmilBankAccount("Mandiri Syariah", "7000511119", "Dompet Dhuafa Wakaf", "Wakaf Uang")
            ),
            confirmationGuide = "Layanan konfirmasi donasi otomatis 24 jam melalui WhatsApp bot Dompet Dhuafa."
        ),
        AmilInstitution(
            id = "rumah_zakat",
            name = "Rumah Zakat Indonesia",
            shortName = "Rumah Zakat",
            category = AmilCategory.LAZ_NASIONAL,
            skLegalNumber = "SK Kemenag RI No. 42 Tahun 2007",
            verifiedBadge = "LAZ Nasional Terakreditasi A",
            description = "Lembaga amil zakat yang mengelola dana ZISWAF secara profesional dengan inovasi Desa Berdaya dan program pemenuhan gizi Nusantara.",
            websiteUrl = "https://rumahzakat.org",
            callCenterWhatsapp = "+6281577000100",
            address = "Jl. Turangga No.33, Lengkong, Bandung, Jawa Barat",
            supportedPrograms = listOf("Zakat Emas & Maal", "Zakat Penghasilan", "Superqurban Kornet", "Desa Berdaya", "Bantuan Bencana Alam"),
            bankAccounts = listOf(
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7001211118", "Rumah Zakat", "Zakat"),
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7001333338", "Rumah Zakat Infaq", "Infaq"),
                AmilBankAccount("Bank Muamalat", "1010080000", "Rumah Zakat", "Zakat"),
                AmilBankAccount("BCA Syariah", "0020088888", "Rumah Zakat", "Zakat")
            ),
            confirmationGuide = "Kirim SMS/WA konfirmasi transfer ke nomor layanan Rumah Zakat untuk aktivasi laporan donatur berkala."
        ),
        AmilInstitution(
            id = "lazismu",
            name = "Lembaga Amil Zakat Infak dan Sedekah Muhammadiyah (LAZISMU)",
            shortName = "LAZISMU",
            category = AmilCategory.LAZ_NASIONAL,
            skLegalNumber = "SK Kemenag RI No. 730 Tahun 2016",
            verifiedBadge = "LAZ Terakreditasi Nasional",
            description = "Lembaga ZIS berskala nasional di bawah Pimpinan Pusat Muhammadiyah yang berkhidmat dalam pemberdayaan masyarakat, pendidikan, dan kesehatan.",
            websiteUrl = "https://lazismu.org",
            callCenterWhatsapp = "+628561626000",
            address = "Gedung Pusat Dakwah Muhammadiyah, Jl. Menteng Raya No.62, Jakarta Pusat",
            supportedPrograms = listOf("Zakat Pertanian", "Zakat Profesi", "Rendangmu Ketahanan Pangan", "Beasiswa Sang Surya", "Peduli Palestina"),
            bankAccounts = listOf(
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7039999997", "Lazismu Pusat Zakat", "Zakat"),
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7039999989", "Lazismu Pusat Infaq", "Infaq"),
                AmilBankAccount("Bank Muamalat", "3010111111", "Lazismu Pusat", "Zakat")
            ),
            confirmationGuide = "Sertakan kode unik transfer atau konfirmasikan ke call center Lazismu Pusat."
        ),
        AmilInstitution(
            id = "lazisnu",
            name = "NU Care - Lembaga Amil Zakat Infak dan Sedekah NU (LAZISNU)",
            shortName = "NU Care - LAZISNU",
            category = AmilCategory.LAZ_NASIONAL,
            skLegalNumber = "SK Kemenag RI No. 65 Tahun 2005",
            verifiedBadge = "LAZ Terakreditasi Nasional",
            description = "Lembaga nirlaba di bawah naungan PBNU yang berfokus pada pengentasan kemiskinan, kemandirian ekonomi umat, dan santunan anak yatim.",
            websiteUrl = "https://nucare.id",
            callCenterWhatsapp = "+6281398009800",
            address = "Gedung PBNU Lt.2, Jl. Kramat Raya No.164, Senen, Jakarta Pusat",
            supportedPrograms = listOf("Koin Muktamar", "Zakat Maal & Fitrah", "Santunan Santri Yatim", "Bantuan Siaga Bencana", "Pesantren Berdaya"),
            bankAccounts = listOf(
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7015654321", "PP LAZISNU ZAKAT", "Zakat"),
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7015654339", "PP LAZISNU INFAQ", "Infaq"),
                AmilBankAccount("Bank Mega Syariah", "1000000088", "LAZISNU PUSAT", "Zakat")
            ),
            confirmationGuide = "Konfirmasi dapat dilakukan via website resmi nucare.id/konfirmasi atau WhatsApp layanan donatur."
        ),
        AmilInstitution(
            id = "bsi_maslahat",
            name = "BSI Maslahat (Yayasan Bangun Sejahtera Mitra Umat)",
            shortName = "BSI Maslahat",
            category = AmilCategory.LAZ_NASIONAL,
            skLegalNumber = "SK Kemenag RI No. 562 Tahun 2022",
            verifiedBadge = "Mitra Utama Bank Syariah Indonesia",
            description = "Lembaga pengelola ZISWAF terpercaya yang bersinergi erat dengan ekosistem perbankan syariah nasional untuk kemaslahatan umat.",
            websiteUrl = "https://bsimaslahat.org",
            callCenterWhatsapp = "+628111888353",
            address = "Wisma Mandiri 1 Lt.4, Jl. MH Thamrin No.5, Jakarta Pusat",
            supportedPrograms = listOf("Sahabat Pelajar", "BSI Scholarship", "Bantuan UMKM Binaan", "Wakaf Rumah Sakit", "Zakat Penghasilan"),
            bankAccounts = listOf(
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7000005211", "BSI Maslahat Rek Zakat", "Zakat"),
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7000005229", "BSI Maslahat Rek Infaq", "Infaq"),
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "7000005237", "BSI Maslahat Rek Wakaf", "Wakaf Uang")
            ),
            confirmationGuide = "Terhubung langsung dengan mutasi mobile banking BSI atau konfirmasi ke customer care BSI Maslahat."
        )
    )
}

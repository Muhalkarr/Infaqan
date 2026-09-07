package com.example.core.shariah

object ShariahDefaultRulings {

    fun getDefaultRulings(): List<ShariahRuling> = listOf(
        ShariahRuling(
            id = "ruling_zakat_maal_emas",
            title = "Zakat Maal (Emas, Perak & Simpanan Kas)",
            category = "ZAKAT",
            authority = "Fatwa DSN-MUI & Peraturan BAZNAS No. 1/2024",
            referenceNumber = "Peraturan BAZNAS No. 1/2024 & Fatwa DSN-MUI No. 3/2003",
            summary = "Kewajiban zakat harta simpanan dan tabungan sebesar 2.5% jika telah memenuhi nisab setara 85 gram emas murni dan mengendap selama 1 tahun Hijriah (Haul).",
            detailedRuling = "Syarat wajib zakat maal: 1) Milik penuh (al-milkut taam), 2) Berkembang (an-namaa'), 3) Mencapai nisab setara 85 gram emas murni, 4) Melebihi kebutuhan pokok primer (al-hajah al-ashliyyah), 5) Bebas dari hutang jatuh tempo yang mengurangi nisab, 6) Berlalu haul 1 tahun Hijriah. Aset yang digabungkan: uang tunai, tabungan bank syariah, emas simpanan, dan deposito.",
            calculationFormula = "Total Nilai Simpanan Kas & Logam Mulia × 2.5% (Wajib jika >= 85 gram × Harga Pasar Emas)",
            dalilSource = "QS. At-Taubah: 34-35 & HR. Abu Dawud No. 1573",
            dalilArabic = "وَالَّذِينَ يَكْنِزُونَ الذَّهَبَ وَالْفِضَّةَ وَلَا يُنفِقُونَهَا فِي سَبِيلِ اللَّهِ فَبَشِّرْهُم بِعَذَابٍ أَلِيمٍ ۝ يَوْمَ يُحْمَىٰ عَلَيْهَا فِي نَارِ جَهَنَّمَ فَتُكْوَىٰ بِهَا جِبَاهُهُمْ وَجُنُوبُهُمْ وَظُهُورُهُمْ ۖ هَٰذَا مَا كَنَزْتُمْ لِأَنفُسِكُمْ فَذُوقُوا مَا كُنتُمْ تَكْنِزُونَ",
            dalilTranslation = "Dan orang-orang yang menyimpan emas dan perak dan tidak menafkahkannya di jalan Allah, maka beritahukanlah kepada mereka, (bahwa mereka akan mendapat) siksa yang pedih. (QS. At-Taubah: 34-35). Sabda Nabi SAW: Apabila engkau memiliki 20 dinar (85g emas) dan berlalu satu haul, wajib zakat setengah dinar (2.5%). (HR. Abu Dawud No. 1573).",
            isCustom = false,
            isEnabled = true
        ),
        ShariahRuling(
            id = "ruling_zakat_penghasilan",
            title = "Zakat Profesi, Gaji & Pendapatan Kasab",
            category = "ZAKAT",
            authority = "Fatwa Komisi Fatwa MUI No. 3/2003 & SK BAZNAS",
            referenceNumber = "Fatwa MUI No. 3 Tahun 2003",
            summary = "Kewajiban zakat atas penghasilan profesi seperti gaji, honorarium, upah, deviden, dan bonus saat diterima, apabila total penerimaan setahun mencapai nisab 85 gram emas.",
            detailedRuling = "Ketentuan Hukum Fatwa MUI No. 3/2003: Semua bentuk penghasilan halal dari pekerjaan atau keahlian wajib dikeluarkan zakatnya dengan syarat mencapai nisab 85 gram emas per tahun. Pengeluaran zakat profesi dapat ditunaikan saat menerima penghasilan setiap bulan (qiyas atas zakat hasil panen, dengan kadar zakat 2.5%). Pendekatan hisab: Bruto (ihtiyath) atau Netto (setelah biaya pokok primer dan cicilan mendesak).",
            calculationFormula = "Penghasilan Bruto/Netto Bulanan × 2.5% (Nisab: [85 × Harga Emas] / 12)",
            dalilSource = "QS. Al-Baqarah: 267 & Fatwa MUI No. 3/2003",
            dalilArabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا أَنفِقُوا مِن طَيِّبَاتِ مَا كَسَبْتُمْ وَمِمَّا أَخْرَجْنَا لَكُم مِّنَ الْأَرْضِ ۖ وَلَا تَيَمَّمُوا الْخَبِيثَ مِنْهُ تُنفِقُونَ وَلَسْتُم بِآخِذِيهِ إِلَّا أَن تُغْمِضُوا فِيهِ ۚ وَاعْلَمُوا أَنَّ اللَّهَ غَنِيٌّ حَمِيدٌ",
            dalilTranslation = "Wahai orang-orang yang beriman! Infakkanlah sebagian dari hasil usahamu yang baik-baik dan sebagian dari apa yang Kami keluarkan dari bumi untuk kamu. (QS. Al-Baqarah: 267).",
            isCustom = false,
            isEnabled = true
        ),
        ShariahRuling(
            id = "ruling_zakat_perniagaan",
            title = "Zakat Perniagaan / Usaha Dagang Syariah",
            category = "ZAKAT",
            authority = "Fiqih 4 Mazhab & SK BAZNAS",
            referenceNumber = "Kompilasi Hukum Islam & Panduan Zakat BAZNAS",
            summary = "Zakat atas perputaran kekayaan aset lancar kegiatan usaha perdagangan yang telah berjalan selama satu tahun haul dengan nisab 85 gram emas murni.",
            detailedRuling = "Objek perhitungan zakat perniagaan: 1) Nilai riil persediaan barang dagangan (stock), 2) Uang kas & saldo bank operasional usaha, 3) Piutang lancar yang berpeluang kuat tertagih. Dikurangi hutang jatuh tempo jangka pendek. Aset tetap/peralatan toko tidak dihitung zakatnya.",
            calculationFormula = "[(Persediaan Dagang + Kas/Bank Usaha + Piutang Lancar) - Hutang Jatuh Tempo] × 2.5%",
            dalilSource = "HR. Abu Dawud No. 1562 & Atsar Shahabat",
            dalilArabic = "كَانَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يَأْمُرُنَا أَنْ نُخْرِجَ الصَّدَقَةَ مِنَ الَّذِي نُعِدُّهُ لِلْبَيْعِ",
            dalilTranslation = "Dari Samurah bin Jundub RA: Rasulullah SAW memerintahkan kami agar mengeluarkan zakat dari apa saja yang kami sediakan untuk diperjualbelikan. (HR. Abu Dawud No. 1562).",
            isCustom = false,
            isEnabled = true
        ),
        ShariahRuling(
            id = "ruling_faraidh_waris",
            title = "Kaidah Hukum Waris Syariah (Faraidh)",
            category = "FARAIDH",
            authority = "Al-Qur'anul Karim & Ijma' Sahabat",
            referenceNumber = "QS. An-Nisa: 11-12 & Kompilasi Hukum Islam Buku II",
            summary = "Kaidah pembagian harta tirkah warisan secara adil dan qath'i setelah disucikan dari biaya tajhiz jenazah, pelunasan hutang, dan wasiat (maksimal 1/3).",
            detailedRuling = "Urutan hak harta peninggalan: 1) Biaya pengurusan jenazah (tajhiz), 2) Pelunasan hutang muwassa'ah dan dharuriyah, 3) Pelaksanaan wasiat (maks 1/3 kepada non-ahli waris), 4) Pembagian kepada Ashabul Furudh dan Ashabah (anak laki-laki, anak perempuan 2:1, orang tua, pasangan hidup, dan saudara kandung jika tidak terhijab).",
            calculationFormula = "Harta Bersih Waris = Total Tirkah - Biaya Jenazah - Hutang - Wasiat",
            dalilSource = "QS. An-Nisa: 11",
            dalilArabic = "يُوصِيكُمُ اللَّهُ فِي أَوْلَادِكُمْ ۖ لِلذَّكَرِ مِثْلُ حَظِّ الْأُنثَيَيْنِ ۚ فَإِن كُنَّ نِسَاءً فَوْقَ اثْنَتَيْنِ فَلَهُنَّ ثُلُثَا مَا تَرَكَ ۖ وَإِن كَانَتْ وَاحِدَةً فَلَهَا النِّصْفُ ۚ وَلِأَبَوَيْهِ لِكُلِّ وَاحِدٍ مِّنْهُمَا السُّدُسُ مِمَّا تَرَكَ إِن كَانَ لَهُ وَلَدٌ",
            dalilTranslation = "Allah mensyariatkan bagimu tentang pembagian warisan untuk anak-anakmu: bagian seorang anak laki-laki sama dengan bagian dua orang anak perempuan. Dan jika anak itu semuanya perempuan lebih dari dua, maka bagi mereka dua pertiga dari harta yang ditinggalkan. Jika dia seorang anak perempuan saja, maka dia memperoleh separuh. Dan untuk kedua ibu-bapak, masing-masing mendapat seperenam dari harta yang ditinggalkan, jika dia mempunyai anak... (QS. An-Nisa: 11).",
            isCustom = false,
            isEnabled = true
        ),
        ShariahRuling(
            id = "ruling_qardh_pencatatan",
            title = "Fiqih Hutang Piutang (Qardh Hasan) & Ayat Mudayanah",
            category = "QARDH",
            authority = "Al-Qur'anul Karim & Fatwa DSN-MUI No. 19/2001",
            referenceNumber = "QS. Al-Baqarah: 282 & Fatwa DSN-MUI No. 19/DSN-MUI/IV/2001",
            summary = "Kewajiban pencatatan tertulis, penentuan batas tempo, persaksian yang adil, serta pengharaman riba dalam transaksi hutang piutang.",
            detailedRuling = "Ketentuan Syariah Transaksi Qardh: 1) Wajib dicatat tertulis agar tidak menimbulkan perselisihan atau lupa, 2) Menghadirkan saksi yang adil, 3) Menentukan tempo pengembalian dengan jelas, 4) Dilarang keras mensyaratkan bunga/manfaat komersial, 5) Wajib memberi kelonggaran jika peminjam dalam kesulitan nyata.",
            calculationFormula = "Pengembalian Pokok = Jumlah Pinjaman Awal (Tanpa Bunga / Denda)",
            dalilSource = "QS. Al-Baqarah: 282",
            dalilArabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا تَدَايَنتُم بِدَيْنٍ إِلَىٰ أَجَلٍ مُّسَمًّى فَاكْتُبُوهُ ۚ وَلْيَكْتُب بَّيْنَكُمْ كَاتِبٌ بِالْعَدْلِ ۚ وَلَا يَأْبَ كَاتِبٌ أَن يَكْتُبَ كَمَا عَلَّمَهُ اللَّهُ ۚ فَلْيَكْتُبْ وَلْيُمْلِلِ الَّذِي عَلَيْهِ الْحَقُّ وَلْيَتَّقِ اللَّهَ رَبَّهُ وَلَا يَبْخَسْ مِنْهُ شَيْئًا",
            dalilTranslation = "Wahai orang-orang yang beriman! Apabila kamu melakukan utang-piutang untuk waktu yang ditentukan, hendaklah kamu menuliskannya. Dan hendaklah seorang penulis di antara kamu menuliskannya dengan adil. Janganlah penulis enggan menuliskannya sebagaimana Allah telah mengajarkannya... dan janganlah orang yang berutang itu mengurangi sedikit pun dari utangnya. (QS. Al-Baqarah: 282).",
            isCustom = false,
            isEnabled = true
        ),
        ShariahRuling(
            id = "ruling_sedekah_subuh",
            title = "Keutamaan & Hikmah Sedekah Subuh",
            category = "INFAQ",
            authority = "Hadits Shahih Bukhari & Muslim",
            referenceNumber = "HR. Bukhari No. 1442 & Muslim No. 1010",
            summary = "Doa mustajab dari dua malaikat yang turun setiap pagi mendoakan keberkahan bagi orang yang berinfaq dan kebinasaan bagi orang yang kikir.",
            detailedRuling = "Sedekah di awal waktu pagi (Subuh) memiliki keutamaan khusus karena malaikat khusus turun mendoakan kelapangan rezeki pengganti (khalafan) bagi mereka yang dermawan, sekaligus sebagai benteng spiritual dari bahaya dan musibah sepanjang hari.",
            calculationFormula = "Alokasi Harian Istiqamah (Minimal Rp 1.000 - Rp 50.000+)",
            dalilSource = "HR. Al-Bukhari No. 1442 & Muslim No. 1010",
            dalilArabic = "مَا مِنْ يَوْمٍ يُصْبِحُ الْعِبَادُ فِيهِ إِلاَّ مَلَكَانِ يَنْزِلاَنِ فَيَقُولُ أَحَدُهُمَا: اللَّهُمَّ أَعْطِ مُنْفِقًا خَلَفًا، وَيَقُولُ الآخَرُ: اللَّهُمَّ أَعْطِ مُمْسِكًا تَلَفًا",
            dalilTranslation = "Tidak ada satu hari pun di mana seorang hamba berada di pagi hari melainkan dua malaikat turun kepadanya. Salah satu malaikat itu berdoa: 'Ya Allah, berikanlah ganti bagi orang yang berinfaq.' Sedangkan malaikat yang satu lagi berdoa: 'Ya Allah, berikanlah kehancuran/kebinasaan bagi orang yang menahan hartanya (kikir).' (HR. Al-Bukhari No. 1442 dan Muslim No. 1010 dari Abu Hurairah RA).",
            isCustom = false,
            isEnabled = true
        ),
        ShariahRuling(
            id = "ruling_larangan_riba",
            title = "Pengharaman Riba & Bunga Bank Konvensional",
            category = "TRANSAKSI",
            authority = "Fatwa Majelis Ulama Indonesia No. 1/2004",
            referenceNumber = "Keputusan Fatwa MUI Nasional No. 1 Tahun 2004",
            summary = "Penetapan keharaman bunga pada segala bentuk pinjaman komersial dan simpanan konvensional karena tergolong Riba Nasi'ah yang dilarang tegas syariat.",
            detailedRuling = "Kaidah syariah: 'Kullu qardhin jarra manfa'atan fahuwa riba' (Setiap akad pinjaman yang mensyaratkan keuntungan bagi pemberi pinjaman adalah riba). Solusi syar'i adalah menggunakan akad mudharabah, murabahah, atau musyarakah pada bank syariah.",
            calculationFormula = "Bunga Riba Wajib Disucikan 100% ke Dana Sosial (Gharim / Fakir)",
            dalilSource = "QS. Al-Baqarah: 275 & HR. Muslim No. 1598",
            dalilArabic = "الَّذِينَ يَأْكُلُونَ الرِّبَا لَا يَقُومُونَ إِلَّا كَمَا يَقُومُ الَّذِي يَتَخَبَّطُهُ الشَّيْطَانُ مِنَ الْمَسِّ ۚ ذَٰلِكَ بِأَنَّهُمْ قَالُوا إِنَّمَا الْبَيْعُ مِثْلُ الرِّبَا ۗ وَأَحَلَّ اللَّهُ الْبَيْعَ وَحَرَّمَ الرِّبَا",
            dalilTranslation = "Orang-orang yang memakan riba tidak dapat berdiri melainkan seperti berdirinya orang yang kemasukan setan karena gila... Padahal Allah telah menghalalkan jual beli dan mengharamkan riba. (QS. Al-Baqarah: 275).",
            isCustom = false,
            isEnabled = true
        )
    )
}

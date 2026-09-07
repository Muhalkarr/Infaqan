package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import java.text.NumberFormat
import java.util.Locale

data class GroundedRuling(
    val id: String,
    val title: String,
    val category: String, // ZAKAT, RIBA_DAN_HUTANG, INVESTASI, ISRAF_KONSUMSI, SYUBHAT
    val authority: String, // Fatwa DSN-MUI, SK BAZNAS, Ijma' Fiqih
    val referenceNumber: String,
    val summary: String,
    val detailedRuling: String,
    val calculationFormula: String? = null,
    val tags: List<String>,
    val dalilSource: String? = null,
    val dalilArabic: String? = null,
    val dalilTranslation: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslamicKnowledgeGroundingScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Ensiklopedia Fatwa, 1: Verifikator Hitung Zakat
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Knowledge database grounded in DSN-MUI & BAZNAS
    val rulingsDatabase = remember {
        listOf(
            GroundedRuling(
                id = "zakat_maal_emas",
                title = "Zakat Maal (Harta Emas, Perak & Simpanan Kas)",
                category = "ZAKAT",
                authority = "Fatwa DSN-MUI & Peraturan BAZNAS No. 1/2024",
                referenceNumber = "Peraturan BAZNAS No. 1 / 2024 & Fatwa MUI No. 3 / 2003",
                summary = "Kewajiban zakat harta simpanan dan tabungan sebesar 2.5% jika telah memenuhi nisab setara 85 gram emas murni dan mengendap sempurna selama 1 tahun Hijriah (Haul).",
                detailedRuling = "Berdasarkan Ijma' Ulama dan Peraturan BAZNAS No. 1 Tahun 2024 Pasal 13: Syarat wajib zakat maal adalah: 1) Milik penuh (al-milkut taam), 2) Berkembang secara riil maupun potensial (an-namaa'), 3) Mencapai nisab setara 85 gram emas murni, 4) Melebihi kebutuhan pokok primer (al-hajah al-ashliyyah), 5) Bebas dari hutang jatuh tempo yang mengurangi jumlah nisab, 6) Berlalu masa kepemilikan selama satu tahun Hijriah (Haul). Aset yang digabungkan perhitungannya meliputi: uang tunai, saldo tabungan giro/bank, deposito syariah, emas batangan murni, dan perhiasan yang disimpan (bukan perhiasan yang dipakai sehari-hari dalam batas kewajaran).",
                calculationFormula = "Total Nilai Simpanan Kas & Emas x 2.5% (Wajib jika Total Nilai >= 85 gram x Harga Pasar Emas)",
                tags = listOf("zakat maal", "emas", "tabungan", "nisab 85g", "haul 1 tahun"),
                dalilSource = "QS. At-Taubah: 34-35 & HR. Abu Dawud No. 1573",
                dalilArabic = "وَالَّذِينَ يَكْنِزُونَ الذَّهَبَ وَالْفِضَّةَ وَلَا يُنفِقُونَهَا فِي سَبِيلِ اللَّهِ فَبَشِّرْهُم بِعَذَابٍ أَلِيمٍ ۝ يَوْمَ يُحْمَىٰ عَلَيْهَا فِي نَارِ جَهَنَّمَ فَتُكْوَىٰ بِهَا جِبَاهُهُمْ وَجُنُوبُهُمْ وَظُهُورُهُمْ ۖ هَٰذَا مَا كَنَزْتُمْ لِأَنفُسِكُمْ فَذُوقُوا مَا كُنتُمْ تَكْنِزُونَ",
                dalilTranslation = "Dan orang-orang yang menyimpan emas dan perak dan tidak menafkahkannya di jalan Allah, maka beritahukanlah kepada mereka, (bahwa mereka akan mendapat) siksa yang pedih, pada hari dipanaskan emas perak itu dalam neraka Jahannam, lalu dibakar dengannya dahi mereka, lambung dan punggung mereka (lalu dikatakan) kepada mereka: 'Inilah harta bendamu yang kamu simpan untuk dirimu sendiri, maka rasakanlah sekarang (akibat dari) apa yang kamu simpan itu.' (QS. At-Taubah: 34-35). Rasulullah SAW bersabda: 'فَإِذَا كَانَتْ لَكَ مِائَتَا دِرْهَمٍ وَحَالَ عَلَيْهَا الْحَوْلُ فَفِيهَا خَمْسَةُ دَرَاهِمَ وَلَيْسَ عَلَيْكَ شَيْءٌ حَتَّى يَكُونَ لَكَ عِشْرُونَ دِينَارًا فَإِذَا كَانَتْ لَكَ عِشْرُونَ دِينَارًا وَحَالَ عَلَيْهَا الْحَوْلُ فَفِيهَا نِصْفُ دِينَارٍ' (Apabila engkau memiliki 20 dinar [setara 85 gram emas] dan telah berlalu satu haul, maka padanya wajib zakat setengah dinar [2.5%]) — HR. Abu Dawud (No. 1573)."
            ),
            GroundedRuling(
                id = "zakat_penghasilan",
                title = "Zakat Profesi, Gaji & Pendapatan Bulanan",
                category = "ZAKAT",
                authority = "Fatwa MUI No. 3 / 2003 & SK BAZNAS No. 1 / 2024",
                referenceNumber = "Fatwa Komisi Fatwa MUI No. 3/2003",
                summary = "Kewajiban zakat atas penghasilan profesi seperti gaji, honorarium, upah, deviden, dan bonus saat diterima, apabila total penerimaan setahun mencapai nisab setara 85 gram emas.",
                detailedRuling = "Ketentuan Hukum Fatwa MUI No. 3 Tahun 2003: 1) Semua bentuk penghasilan halal dari pekerjaan atau keahlian wajib dikeluarkan zakatnya dengan syarat mencapai nisab 85 gram emas per tahun. 2) Pengeluaran zakat profesi dapat ditunaikan pada saat menerima penghasilan setiap bulan (qiyas atas zakat hasil bumi pada waktu panen 'wa aatuu haqqahu yawma hashadih', dengan kadar zakat disamakan dengan zakat emas yaitu 2.5%). 3) Pendekatan hisab: Pendekatan Bruto (menghitung langsung 2.5% dari seluruh penghasilan kotor) adalah yang paling dianjurkan demi kehati-hatian (ihtiyath), atau Pendekatan Netto (dikeluarkan 2.5% setelah dikurangi biaya kebutuhan pokok primer diri dan keluarga yang nyata).",
                calculationFormula = "Penghasilan Bruto Bulanan x 2.5% (Nisab Bulanan: (85 x Harga Emas Saat Ini) / 12)",
                tags = listOf("zakat profesi", "gaji", "penghasilan", "freelance", "sk baznas"),
                dalilSource = "QS. Al-Baqarah: 267 & Fatwa MUI No. 3 Tahun 2003",
                dalilArabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا أَنفِقُوا مِن طَيِّبَاتِ مَا كَسَبْتُمْ وَمِمَّا أَخْرَجْنَا لَكُم مِّنَ الْأَرْضِ ۖ وَلَا تَيَمَّمُوا الْخَبِيثَ مِنْهُ تُنفِقُونَ وَلَسْتُم بِآخِذِيهِ إِلَّا أَن تُغْمِضُوا فِيهِ ۚ وَاعْلَمُوا أَنَّ اللَّهَ غَنِيٌّ حَمِيدٌ",
                dalilTranslation = "Wahai orang-orang yang beriman! Infakkanlah sebagian dari hasil usahamu yang baik-baik dan sebagian dari apa yang Kami keluarkan dari bumi untuk kamu. Janganlah kamu memilih yang buruk untuk kamu keluarkan, padahal kamu sendiri tidak mau mengambilnya melainkan dengan memicingkan mata (enggan) terhadapnya. Dan ketahuilah bahwa Allah Mahakaya, Maha Terpuji."
            ),
            GroundedRuling(
                id = "zakat_perniagaan",
                title = "Zakat Perniagaan / Usaha Dagang Syariah",
                category = "ZAKAT",
                authority = "Ketentuan Fiqih Muamalah 4 Mazhab & SK BAZNAS",
                referenceNumber = "Kompilasi Hukum Islam & Panduan Zakat BAZNAS",
                summary = "Zakat atas perputaran kekayaan lancar kegiatan usaha perniagaan yang telah berjalan selama satu tahun haul dengan nisab 85 gram emas murni.",
                detailedRuling = "Sesuai kesepakatan jumhur ulama fiqih dan ketentuan BAZNAS: Objek yang dihitung dalam zakat perniagaan hanyalah aset lancar usaha (current assets), yaitu: 1) Nilai riil persediaan barang dagangan (stock) pada akhir haul, 2) Uang kas tunai dan saldo bank yang merupakan kas operasional dagang, 3) Piutang lancar dagang yang berpeluang kuat untuk tertagih (ar-rajuu). Total aset lancar tersebut kemudian dikurangkan dengan hutang dagang jatuh tempo jangka pendek. Adapun aset tetap/sarana produksi (seperti gedung, tanah toko, etalase, mesin kasir, kendaraan kurir) TIDAK dihitung zakatnya karena bukan barang yang diperjualbelikan.",
                calculationFormula = "[(Nilai Persediaan Barang Dagang + Kas & Saldo Bank Usaha + Piutang Lancar) - Hutang Jatuh Tempo] x 2.5%",
                tags = listOf("zakat dagang", "perniagaan", "persediaan", "piutang", "bisnis"),
                dalilSource = "HR. Abu Dawud No. 1562 & Atsar Sahabat",
                dalilArabic = "كَانَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ يَأْمُرُنَا أَنْ نُخْرِجَ الصَّدَقَةَ مِنَ الَّذِي نُعِدُّهُ لِلْبَيْعِ",
                dalilTranslation = "Dari Samurah bin Jundub RA, ia berkata: 'Rasulullah SAW memerintahkan kami agar mengeluarkan zakat dari apa saja yang kami sediakan untuk diperjualbelikan.' — HR. Abu Dawud (No. 1562) dan dishahihkan oleh para ulama."
            ),
            GroundedRuling(
                id = "larangan_riba_bunga",
                title = "Pengharaman Riba & Bunga Bank Konvensional",
                category = "RIBA_DAN_HUTANG",
                authority = "Fatwa MUI No. 1 / 2004",
                referenceNumber = "Keputusan Fatwa MUI Nasional No. 1/2004",
                summary = "Bunga (interest) yang dipersyaratkan dalam transaksi simpan-pinjam pada bank atau lembaga keuangan konvensional hukumnya HARAM secara mutlak karena tergolong Riba Nasi'ah.",
                detailedRuling = "Keputusan Fatwa Majelis Ulama Indonesia No. 1 Tahun 2004 menetapkan: 1) Praktek pembungaan uang yang berlaku pada bank konvensional, asuransi konvensional, pegadaian konvensional, dan rentenir termasuk perbuatan riba yang diharamkan secara tegas oleh nash syariat. 2) Riba yang terjadi dalam pinjaman berbunga tersebut adalah Riba Nasi'ah (tambahan atas pokok pinjaman karena penangguhan waktu pembayaran). 3) Kaidah syariah menetapkan: 'Kullu qardhin jarra manfa'atan fahuwa riba' (Setiap akad pinjaman yang mensyaratkan keuntungan bagi pemberi pinjaman adalah riba). 4) Solusi syar'i adalah bermuamalah dengan lembaga keuangan syariah yang menggunakan akad bebas riba: Murabahah (jual beli dengan margin jelas), Ijarah (sewa manfaat), Mudharabah dan Musyarakah (kemitraan bagi hasil untung dan rugi).",
                calculationFormula = null,
                tags = listOf("riba", "bunga bank", "pinjaman", "paylater", "murabahah"),
                dalilSource = "QS. Al-Baqarah: 275 & HR. Muslim No. 1598",
                dalilArabic = "الَّذِينَ يَأْكُلُونَ الرِّبَا لَا يَقُومُونَ إِلَّا كَمَا يَقُومُ الَّذِي يَتَخَبَّطُهُ الشَّيْطَانُ مِنَ الْمَسِّ ۚ ذَٰلِكَ بِأَنَّهُمْ قَالُوا إِنَّمَا الْبَيْعُ مِثْلُ الرِّبَا ۗ وَأَحَلَّ اللَّهُ الْبَيْعَ وَحَرَّمَ الرِّبَا ۚ فَمَن جَاءَهُ مَوْعِظَةٌ مِّن رَّبِّهِ فَانتَهَىٰ فَلَهُ مَا سَلَفَ وَأَمْرُهُ إِلَى اللَّهِ ۖ وَمَنْ عَادَ فَأُولَٰئِكَ أَصْحَابُ النَّارِ ۖ هُمْ فِيهَا خَالِدُونَ",
                dalilTranslation = "Orang-orang yang memakan riba tidak dapat berdiri melainkan seperti berdirinya orang yang kemasukan setan karena gila. Yang demikian itu karena mereka berkata bahwa jual beli itu sama dengan riba. Padahal Allah telah menghalalkan jual beli dan mengharamkan riba. Barangsiapa mendapat peringatan dari Tuhannya, lalu dia berhenti, maka apa yang telah diperolehnya dahulu menjadi miliknya dan urusannya (terserah) kepada Allah. Dan barangsiapa mengulangi, maka mereka itu penghuni neraka, mereka kekal di dalamnya. (QS. Al-Baqarah: 275). Dari Jabir RA: 'لَعَنَ رَسُولُ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ آكِلَ الرِّبَا، وَمُؤْكِلَهُ، وَكَاتِبَهُ، وَشَاهِدَيْهِ، وَقَالَ: هُمْ سَوَاءٌ' (Rasulullah SAW melaknat pemakan riba, penyetor riba, penulisnya, dan kedua saksinya. Beliau bersabda: 'Mereka itu semuanya sama dosanya') — HR. Muslim (No. 1598)."
            ),
            GroundedRuling(
                id = "qardh_hasan",
                title = "Fiqih Qardh Hasan (Pinjaman Kebajikan)",
                category = "RIBA_DAN_HUTANG",
                authority = "Fatwa DSN-MUI No. 19/DSN-MUI/IV/2001",
                referenceNumber = "Fatwa DSN-MUI No. 19/2001",
                summary = "Pinjaman murni tanpa bunga yang didasari prinsip tolong-menolong sosial tanpa komersialisasi, denda ganti rugi bunga, atau syarat penambahan pokok.",
                detailedRuling = "Ketentuan Fatwa DSN-MUI No. 19/2001 menyatakan: 1) Akad al-Qardh adalah akad pinjaman dana kepada nasabah/peminjam dengan ketentuan bahwa peminjam wajib mengembalikan dana yang diterimanya kepada pihak yang meminjamkan pada waktu yang disepakati dengan jumlah yang sama. 2) Pihak pemberi pinjaman dilarang keras meminta atau mensyaratkan imbalan atau tambahan atas pokok pinjaman. 3) Peminjam diperbolehkan memberikan tambahan nominal atau hadiah kepada pemberi pinjaman atas kemauannya sendiri secara sukarela pada saat pembayaran pelunasan ('Husnul Qadha'), asalkan tidak diperjanjikan sebelumnya dalam bentuk apapun. 4) Apabila peminjam mengalami kesulitan ekonomi yang nyata (mu'sir), maka pemberi pinjaman diwajibkan memberikan kelonggaran waktu atau dianjurkan mengikhlaskannya sebagian atau seluruhnya.",
                calculationFormula = null,
                tags = listOf("qardh hasan", "pinjaman syariah", "tanpa bunga", "tolong menolong"),
                dalilSource = "QS. Al-Baqarah: 280 & HR. Muslim No. 3014",
                dalilArabic = "وَإِن كَانَ ذُو عُسْرَةٍ فَنَظِرَةٌ إِلَىٰ مَيْسَرَةٍ ۚ وَأَن تَصَدَّقُوا خَيْرٌ لَّكُمْ ۖ إِن كُنتُمْ تَعْلَمُونَ",
                dalilTranslation = "Dan jika (orang yang berutang itu) dalam kesulitan, maka berilah tenggang waktu sampai dia memperoleh kelapangan. Dan menyedekahkan (sebagian atau semua utang itu) lebih baik bagimu jika kamu mengetahui. (QS. Al-Baqarah: 280). Rasulullah SAW bersabda: 'مَنْ أَنْظَرَ مُعْسِرًا أَوْ وَضَعَ عَنْهُ أَظَلَّهُ اللَّهُ فِي ظِلِّهِ يَوْمَ لَا ظِلَّ إِلَّا ظِلُّهُ' (Barangsiapa memberi tenggang waktu kepada orang yang kesulitan membayar utang atau membebaskan utangnya, maka Allah akan menaunginya dalam naungan-Nya pada hari yang tiada naungan selain naungan-Nya) — HR. Muslim (No. 3014)."
            ),
            GroundedRuling(
                id = "israf_tabdzir",
                title = "Batasan Syar'i Israf (Berlebihan) & Tabdzir (Sia-Sia)",
                category = "ISRAF_KONSUMSI",
                authority = "Al-Qur'an (QS. Al-A'raf: 31, Al-Isra: 26-27)",
                referenceNumber = "Kaidah Fiqih Maqashid Syariah Konsumsi",
                summary = "Larangan membelanjakan harta melebihi batas kebutuhan yang wajar (Israf) atau membelanjakannya untuk perkara maksiat dan sia-sia tanpa kemaslahatan (Tabdzir).",
                detailedRuling = "Dalam kaidah Maqashid Asy-Syari'ah Imam Asy-Syathibi, pengelolaan pengeluaran harta dikelompokkan ke dalam 3 hierarki: 1) Dharuriyyat (kebutuhan primer yang mutlak diperlukan untuk menjaga agama, jiwa, akal, keturunan, dan harta seperti makanan pokok sehat, pakaian layak, tempat tinggal aman, dan ibadah wajib), 2) Hajiyyat (kebutuhan sekunder yang mempermudah hidup dan menghilangkan kesempitan, seperti sarana transportasi dan alat komunikasi), 3) Tahsiniyyat (kebutuhan tersier/estetika yang memperindah kehidupan tanpa berlebih-lebihan). Seseorang dianggap melakukan Israf apabila membelanjakan harta pada hal mubah namun melebihi batas kemampuan dan kewajaran finansialnya. Seseorang dianggap melakukan Tabdzir apabila menyalurkan harta untuk hal yang diharamkan atau membiarkan harta lenyap tanpa manfaat syar'i.",
                calculationFormula = null,
                tags = listOf("israf", "tabdzir", "anggaran", "konsumsi", "maqashid syariah"),
                dalilSource = "QS. Al-A'raf: 31 & QS. Al-Isra: 26-27",
                dalilArabic = "يَا بَنِي آدَمَ خُذُوا زِينَتَكُمْ عِندَ كُلِّ مَسْجِدٍ وَكُلُوا وَاشْرَبُوا وَلَا تُسْرِفُوا ۚ إِنَّهُ لَا يُحِبُّ الْمُسْرِفِينَ ۝ وَآتِ ذَا الْقُرْبَىٰ حَقَّهُ وَالْمِسْكِينَ وَابْنَ السَّبِيلِ وَلَا تُبَذِّرْ تَبْذِيرًا ۝ إِنَّ الْمُبَذِّرِينَ كَانُوا إِخْوَانَ الشَّيَاطِينِ ۖ وَكَانَ الشَّيْطَانُ لِرَبِّهِ كَفُورًا",
                dalilTranslation = "Wahai anak cucu Adam! Pakailah pakaianmu yang bagus pada setiap (memasuki) masjid, makan dan minumlah, tetapi jangan berlebih-lebihan. Sungguh, Allah tidak menyukai orang yang berlebih-lebihan. (QS. Al-A'raf: 31). Dan berikanlah haknya kepada kerabat dekat, juga kepada orang miskin dan orang yang dalam perjalanan; dan janganlah kamu menghambur-hamburkan (hartamu) secara boros. Sesungguhnya orang-orang yang pemboros itu adalah saudara-saudara setan dan setan itu sangat ingkar kepada Tuhannya. (QS. Al-Isra: 26-27)."
            ),
            GroundedRuling(
                id = "harta_syubhat",
                title = "Pembersihan & Penyaluran Harta Non-Halal / Syubhat",
                category = "SYUBHAT",
                authority = "Fatwa DSN-MUI No. 123/DSN-MUI/XI/2018",
                referenceNumber = "Fatwa DSN-MUI No. 123/2018",
                summary = "Pendapatan dari aktivitas non-halal (seperti bunga bank konvensional, jasa giro konvensional, atau denda) wajib dipisahkan dan disalurkan ke sarana kemaslahatan umum.",
                detailedRuling = "Sesuai Fatwa DSN-MUI No. 123/2018 tentang Penggunaan Dana Sosial Keagamaan dari Dana Non-Halal: 1) Harta non-halal yang tidak sengaja diperoleh (seperti bunga tabungan pada rekening bank konvensional yang terpaksa dimiliki) tidak boleh diakui sebagai hak milik pribadi penerima dan tidak boleh dipergunakan untuk kebutuhan konsumsi diri sendiri, keluarga, maupun modal usaha. 2) Harta non-halal tersebut wajib dikeluarkan dan dialokasikan semata-mata untuk tujuan kemaslahatan umat, seperti: perbaikan jalan raya umum, pembangunan jembatan penyeberangan, pengadaan sarana sanitasi/toilet umum, penanganan korban bencana alam, atau santunan darurat fakir miskin. 3) Penyaluran dana non-halal ini dilakukan dengan niat tazkiyah/pembersihan (takhallush minal haram), BUKAN dengan niat sedekah yang mengharapkan pahala sedekah biasa.",
                calculationFormula = null,
                tags = listOf("harta syubhat", "bunga bank", "pembersihan harta", "fasilitas umum"),
                dalilSource = "HR. Muslim No. 1015 & HR. At-Tirmidzi No. 1",
                dalilArabic = "إِنَّ اللَّهَ طَيِّبٌ لَا يَقْبَلُ إِلَّا طَيِّبًا، وَإِنَّ اللَّهَ أَمَرَ الْمُؤْمِنِينَ بِمَا أَمَرَ بِهِ الْمُرْسَلِينَ... وَلَا تُقْبَلُ صَدَقَةٌ مِنْ غُلُولٍ",
                dalilTranslation = "Dari Abu Hurairah RA, Rasulullah SAW bersabda: 'Sesungguhnya Allah itu Maha Baik dan tidak menerima kecuali yang baik. Dan sesungguhnya Allah memerintahkan kepada kaum mukminin apa yang Dia perintahkan kepada para Rasul...' (HR. Muslim No. 1015). Serta sabda Nabi SAW: 'لَا تُقْبَلُ صَلَاةٌ بِغَيْرِ طُهُورٍ وَلَا صَدَقَةٌ مِنْ غُلُولٍ' (Tidak akan diterima shalat tanpa bersuci dan tidak akan diterima sedekah dari harta yang haram/khianat) — HR. Muslim (No. 224) dan At-Tirmidzi (No. 1)."
            )
        )
    }

    // Filter rulings
    val filteredRulings = remember(searchQuery, selectedCategoryFilter, rulingsDatabase) {
        rulingsDatabase.filter { ruling ->
            val matchQuery = searchQuery.isBlank() ||
                    ruling.title.contains(searchQuery, ignoreCase = true) ||
                    ruling.summary.contains(searchQuery, ignoreCase = true) ||
                    ruling.detailedRuling.contains(searchQuery, ignoreCase = true) ||
                    ruling.authority.contains(searchQuery, ignoreCase = true) ||
                    ruling.tags.any { it.contains(searchQuery, ignoreCase = true) }

            val matchCategory = selectedCategoryFilter == null || ruling.category == selectedCategoryFilter

            matchQuery && matchCategory
        }
    }

    // Interactive Zakat Verification State
    var zakatTypeIndex by remember { mutableIntStateOf(0) } // 0: Maal (Simpanan), 1: Profesi/Gaji, 2: Perdagangan
    var inputAmountText by remember { mutableStateOf("") }
    var inputGoldPriceText by remember(state.goldPricePerGram) {
        mutableStateOf(state.goldPricePerGram.toLong().toString())
    }
    var inputDebtText by remember { mutableStateOf("") }

    val goldPrice = inputGoldPriceText.toDoubleOrNull() ?: state.goldPricePerGram
    val inputAmount = inputAmountText.toDoubleOrNull() ?: 0.0
    val inputDebt = inputDebtText.toDoubleOrNull() ?: 0.0

    // Verification engine results
    val nisabThreshold = remember(zakatTypeIndex, goldPrice) {
        when (zakatTypeIndex) {
            0 -> 85.0 * goldPrice // Zakat Maal: 85g Emas
            1 -> (85.0 * goldPrice) / 12.0 // Zakat Profesi Bulanan: Setara 85g / 12
            2 -> 85.0 * goldPrice // Zakat Perniagaan: 85g Emas
            else -> 85.0 * goldPrice
        }
    }

    val netAssetCalculated = remember(zakatTypeIndex, inputAmount, inputDebt) {
        when (zakatTypeIndex) {
            2 -> (inputAmount - inputDebt).coerceAtLeast(0.0) // Perniagaan: Aset Lancar - Hutang
            else -> (inputAmount - inputDebt).coerceAtLeast(0.0)
        }
    }

    val isWajibZakat = remember(netAssetCalculated, nisabThreshold) {
        netAssetCalculated >= nisabThreshold && netAssetCalculated > 0
    }

    val calculatedZakatObligation = remember(isWajibZakat, netAssetCalculated) {
        if (isWajibZakat) netAssetCalculated * 0.025 else 0.0
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("islamic_grounding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Pencarian Fatwa & Zakat Grounding",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Terverifikasi Syariah",
                                tint = EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Rujukan Resmi Fatwa DSN-MUI & Standar BAZNAS",
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("islamic_grounding_sidebar_burger_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldLight,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = EmeraldPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        selectedTab = 0
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Ensiklopedia Fatwa", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        selectedTab = 1
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Verifikator Zakat", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // TAB 0: Grounded Ruling Search & Encyclopedia
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Search input
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari hukum (contoh: zakat emas, bunga bank, israf)...", fontSize = 13.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Cari", tint = EmeraldLight)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = Color.White.copy(alpha = 0.6f))
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grounding_search_field")
                        )
                    }

                    // Category Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf(
                                null to "Semua Rujukan",
                                "ZAKAT" to "Zakat & Nisab",
                                "RIBA_DAN_HUTANG" to "Riba & Qardh",
                                "ISRAF_KONSUMSI" to "Israf & Belanja",
                                "SYUBHAT" to "Penyucian Harta"
                            )

                            items(categories) { (key, label) ->
                                val isSelected = selectedCategoryFilter == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        selectedCategoryFilter = if (isSelected) null else key
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = EmeraldPrimary.copy(alpha = 0.25f),
                                        selectedLabelColor = EmeraldLight,
                                        containerColor = DarkSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Results list
                    items(filteredRulings, key = { it.id }) { ruling ->
                        GroundedRulingCard(ruling = ruling)
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                // TAB 1: Grounded Zakat Verification Engine
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Header Info Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = EmeraldLight)
                                }
                                Column {
                                    Text(
                                        text = "Mesin Verifikasi Zakat Terstandar",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Dihitung berdasarkan parameter resmi SK BAZNAS & Fatwa MUI.",
                                        fontSize = 11.sp,
                                        color = GoldLight
                                    )
                                }
                            }
                        }
                    }

                    // Zakat Type Selector
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            listOf("Zakat Maal", "Zakat Profesi", "Perniagaan").forEachIndexed { index, label ->
                                val isSelected = zakatTypeIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                        .clickable {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            zakatTypeIndex = index
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Form Inputs
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val amountLabel = when (zakatTypeIndex) {
                                    0 -> "Total Harta Simpanan (Emas + Saldo Tabungan + Kas)"
                                    1 -> "Total Penghasilan / Gaji Bulanan"
                                    2 -> "Nilai Persediaan Dagang + Kas Lancar Usaha"
                                    else -> "Total Aset"
                                }

                                OutlinedTextField(
                                    value = inputAmountText,
                                    onValueChange = { inputAmountText = it },
                                    label = { Text(amountLabel, fontSize = 12.sp) },
                                    placeholder = { Text("Contoh: 120000000", fontSize = 12.sp) },
                                    prefix = { Text("${state.primaryCurrencySymbol} ", fontWeight = FontWeight.Bold, color = EmeraldLight) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("verifier_amount_input")
                                )

                                if (zakatTypeIndex == 2 || zakatTypeIndex == 1) {
                                    OutlinedTextField(
                                        value = inputDebtText,
                                        onValueChange = { inputDebtText = it },
                                        label = { Text(if (zakatTypeIndex == 2) "Hutang Jatuh Tempo Usaha" else "Kebutuhan Pokok / Hutang Bulanan", fontSize = 12.sp) },
                                        placeholder = { Text("0", fontSize = 12.sp) },
                                        prefix = { Text("${state.primaryCurrencySymbol} ", fontWeight = FontWeight.Bold, color = ExpenseCoral) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = EmeraldPrimary,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                OutlinedTextField(
                                    value = inputGoldPriceText,
                                    onValueChange = { inputGoldPriceText = it },
                                    label = { Text("Acuan Harga Emas Per Gram", fontSize = 12.sp) },
                                    prefix = { Text("${state.primaryCurrencySymbol} ", fontWeight = FontWeight.Bold, color = GoldAccent) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Verification Output Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWajibZakat) EmeraldDark.copy(alpha = 0.35f) else DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isWajibZakat) EmeraldPrimary else DarkBorder
                            ),
                            modifier = Modifier.testTag("zakat_verification_result_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isWajibZakat) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isWajibZakat) EmeraldLight else GoldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (isWajibZakat) "WAJIB KELUARKAN ZAKAT" else "BELUM MENCAPAI NISAB",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWajibZakat) EmeraldLight else GoldAccent
                                        )
                                    }

                                    Surface(
                                        color = if (isWajibZakat) EmeraldPrimary.copy(alpha = 0.3f) else GoldAccent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isWajibZakat) "Tarif 2.5%" else "Bebas Zakat",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWajibZakat) EmeraldLight else GoldAccent,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Batas Nisab Standar:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text("${state.primaryCurrencySymbol} ${nf.format(nisabThreshold.toLong())}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Aset Bersih Dihitung:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text("${state.primaryCurrencySymbol} ${nf.format(netAssetCalculated.toLong())}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }

                                androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Kewajiban Zakat Bersih:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        text = "${state.primaryCurrencySymbol} ${nf.format(calculatedZakatObligation.toLong())}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isWajibZakat) EmeraldLight else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun GroundedRulingCard(ruling: GroundedRuling) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("grounded_ruling_${ruling.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = ruling.authority,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    color = GoldAccent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GoldAccent.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = ruling.referenceNumber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldLight
                        )
                    }
                }
            }

            Text(
                text = ruling.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = ruling.summary,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                lineHeight = 17.sp
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Kaidah Fiqih Terperinci:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ruling.detailedRuling,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (ruling.dalilSource != null && ruling.dalilArabic != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F241A))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Nash Dalil Lengkap: ${ruling.dalilSource}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                                Text(
                                    text = ruling.dalilArabic,
                                    fontSize = 13.sp,
                                    lineHeight = 22.sp,
                                    color = GoldLight,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (ruling.dalilTranslation != null) {
                                    Text(
                                        text = "\"${ruling.dalilTranslation}\"",
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }

                    if (ruling.calculationFormula != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldDark.copy(alpha = 0.3f))
                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Rumus Hisab Syariah:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ruling.calculationFormula,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (isExpanded) "Tutup Rujukan ▲" else "Baca Ketentuan Lengkap ▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldLight
                )
            }
        }
    }
}

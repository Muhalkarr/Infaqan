package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.core.debug.AppDebugLogger
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.core.accounting.AccountCategory
import com.example.core.grounding.CustomGroundedRuling
import com.example.core.grounding.CustomRulingRepository
import com.example.core.grounding.FatwaConsultationMessage
import com.example.core.grounding.GeminiFatwaRagService
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class GroundedRuling(
    val id: String,
    val title: String,
    val category: String, // ZAKAT, RIBA_DAN_HUTANG, INVESTASI, ISRAF_KONSUMSI, SYUBHAT, TATA_KELOLA
    val authority: String, // Fatwa DSN-MUI, SK BAZNAS, Ijma' Fiqih
    val referenceNumber: String,
    val summary: String,
    val detailedRuling: String,
    val calculationFormula: String? = null,
    val tags: List<String>,
    val dalilSource: String? = null,
    val dalilArabic: String? = null,
    val dalilTranslation: String? = null,
    val isCustomUserRuling: Boolean = false
)

data class FinancialLiterature(
    val id: String,
    val title: String,
    val author: String,
    val era: String,
    val category: String, // KITAB_KLASIK, FATWA_REGULASI, MANAJEMEN_MODERN
    val primaryTopics: List<String>,
    val corePrinciple: String,
    val detailedSummary: String,
    val keyQuotesOrHadith: String? = null,
    val arabicDalil: String? = null,
    val dalilTranslation: String? = null,
    val practicalImplementation: String? = null,
    val referenceCitation: String
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
            ),
            GroundedRuling(
                id = "zakat_rikaz_temuan",
                title = "Zakat Rikaz (Harta Karun / Temuan Purbakala 20% / Khumus)",
                category = "ZAKAT",
                authority = "Hadits Shahih Bukhari & Muslim, PMA Kemenag No. 52/2014 & Fiqih 4 Mazhab",
                referenceNumber = "HR. Bukhari No. 1499, HR. Muslim No. 1710 & PMA No. 52/2014 Pasal 24",
                summary = "Kewajiban zakat 20% (seperlima/Al-Khumus) atas harta peninggalan purbakala tak bertuan yang ditemukan terpendam di dalam tanah, wajib dikeluarkan seketika tanpa syarat haul satu tahun.",
                detailedRuling = "Berdasarkan nash hadits shahih dan ketetapan 4 Mazhab Fiqih serta PMA No. 52/2014 Pasal 24:\n1) Definisi Rikaz: Harta peninggalan orang-orang zaman dahulu/purbakala (jahiliyyah) yang terpendam di dalam tanah bebas tak bertuan dan didapatkan tanpa memerlukan biaya eksploitasi besar atau teknologi berat.\n2) Kadar Kewajiban: Wajib dikeluarkan 1/5 (20%) atau Al-Khumus untuk mustahiq zakat / kemaslahatan sosial fakir miskin, sedangkan 80% sisanya adalah hak halal bagi pihak yang menemukannya.\n3) Waktu Pengeluaran: Wajib dikeluarkan seketika pada saat harta rikaz tersebut ditemukan dan diangkat, tanpa menunggu berlalunya haul 1 tahun.\n4) Ketentuan Nisab: Sesuai PMA No. 52/2014 Pasal 24 ayat (2) huruf b, zakat rikaz tidak mensyaratkan nisab minimal (wajib dizakati 20% berapapun nilainya).\n5) Pembeda Tegas Rikaz vs Luqathah:\n- Rikaz: Harta purbakala kuno tak bertuan -> Hak zakat 20% seketika, 80% milik penemu.\n- Luqathah: Barang berharga tercecer/hilang milik orang lain di masa kini -> Diharamkan langsung diambil atau dipotong 20%, melainkan wajib disimpan dan diumumkan (ta'rif) selama 1 tahun penuh di tempat umum.",
                calculationFormula = "Nilai Pasar Harta Temuan (Rikaz) x 20% (Dikeluarkan seketika saat penemuan)",
                tags = listOf("rikaz", "harta karun", "temuan purbakala", "20%", "khumus", "hadits shahih", "luqathah", "pma 52"),
                dalilSource = "HR. Bukhari No. 1499 & HR. Muslim No. 1710",
                dalilArabic = "عَنْ أَبِي هُرَيْرَةَ رَضِيَ اللَّهُ عَنْهُ، أَنَّ رَسُولَ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: «... وَفِي الرِّكَازِ الْخُمُسُ»",
                dalilTranslation = "Dari Abu Hurairah RA, bahwasanya Rasulullah SAW bersabda: '... Dan pada rikaz (harta temuan purbakala/harta karun) kewajiban zakatnya adalah seperlima (20%).' — Hadits Shahih Muttafaq 'Alaih (HR. Bukhari No. 1499 & HR. Muslim No. 1710)."
            ),
            GroundedRuling(
                id = "pencatatan_keuangan_mudayanah",
                title = "Kewajiban Tata Kelola & Pencatatan Transaksi Finansial",
                category = "TATA_KELOLA",
                authority = "Al-Qur'an (Ayat Terpanjang QS. Al-Baqarah: 282) & Standar AAOIFI",
                referenceNumber = "Ayat Mudayanah QS. Al-Baqarah: 282 & Standar Akuntansi Syariah",
                summary = "Prinsip akuntabilitas, transparansi, dan kewajiban syar'i menuliskan setiap transaksi non-tunai, mutasi kas, dan amanah hutang piutang oleh pencatat yang adil.",
                detailedRuling = "Ayat 282 Surat Al-Baqarah merupakan ayat terpanjang dalam Al-Qur'an yang secara khusus meletakkan fondasi tata kelola keuangan (Financial Governance): 1) Kewajiban menuliskan transaksi bertempo dan hutang piutang (idza tadaayantum bidainin... faktubuuh), 2) Prinsip integritas dan keadilan juru catat/akuntan keuangan (walyaktub bainakum kaatibun bil 'adl), 3) Larangan memanipulasi atau mengurangi hak orang lain walau sedikit (wala yabkhas minhu syai'an), 4) Kewajiban menyertakan saksi terpercaya, 5) Anjuran tegas agar tidak malas mencatat transaksi sekecil maupun sebesar apa pun (wala tas'amuu an taktubuuhu shaghiiran aw kabiiran ilaa ajalih) demi menegakkan keadilan dan mencegah keraguan/sengketa di kemudian hari.",
                calculationFormula = "Prinsip Berimbang: Pencatatan Rinci Hak & Kewajiban (Debit = Kredit)",
                tags = listOf("tata kelola", "pencatatan", "mudayanah", "akuntansi syariah", "transparansi", "ayat 282"),
                dalilSource = "QS. Al-Baqarah: 282",
                dalilArabic = "يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا تَدَايَنتُم بِدَيْنٍ إِلَىٰ أَجَلٍ مُّسَمًّى فَاكْتُبُوهُ ۚ وَلْيَكْتُب بَّيْنَكُمْ كَاتِبٌ بِالْعَدْلِ... وَلَا تَسْأَمُوا أَن تَكْتُبُوهُ صَغِيرًا أَوْ كَبِيرًا إِلَىٰ أَجَلِهِ ۚ ذَٰلِكُمْ أَقْسَطُ عِندَ اللَّهِ وَأَقْوَمُ لِلشَّهَادَةِ وَأَدْنَىٰ أَلَّا تَرْتَابُوا",
                dalilTranslation = "Wahai orang-orang yang beriman! Apabila kamu melakukan utang piutang untuk waktu yang ditentukan, hendaklah kamu menuliskannya. Dan hendaklah seorang penulis di antara kamu menuliskannya dengan adil... Dan janganlah kamu bosan menuliskannya, baik kecil maupun besar sampai batas waktu pembayarannya. Yang demikian itu lebih adil di sisi Allah, lebih dapat menguatkan kesaksian, dan lebih mendekatkan kamu kepada ketidakraguan... (QS. Al-Baqarah: 282)."
            ),
            GroundedRuling(
                id = "tata_kelola_kas_keluarga",
                title = "Kaidah Perencanaan Anggaran & Manajemen Kas Islami",
                category = "TATA_KELOLA",
                authority = "Atsar Sahabat & Kitab Ihya' Ulumiddin Imam Al-Ghazali",
                referenceNumber = "Prinsip Maqashid Syariah & Fiqih Muamalah Keuangan",
                summary = "Kaidah proporsionalitas pembagian pendapatan: alokasi nafkah kebutuhan pokok (Dharuriyyat), tabungan cadangan darurat (Iddikhar), dan sedekah jariyah.",
                detailedRuling = "Tata kelola keuangan mukmin berpedoman pada kaidah keseimbangan (iqtishad): 1) Larangan berlaku kikir (bakhil) dan larangan berlaku boros (israf/tabdzir), sebagaimana firman Allah: 'Dan orang-orang yang apabila membelanjakan (harta), mereka tidak berlebihan, dan tidak (pula) kikir, di antara keduanya secara wajar' (QS. Al-Furqan: 67). 2) Prioritas nafkah keluarga sebagai sedekah paling utama (HR. Muslim No. 995). 3) Sunnah menyisihkan dana cadangan pangan dan darurat (Iddikhar) sebagaimana praktik Nabi SAW yang menyisihkan perbekalan tahunan untuk keluarganya (HR. Bukhari No. 5357). 4) Pembagian pos ideal dalam literatur fiqih: Pos Kebutuhan Hidup (Dharuriyyat & Hajiyyat wajar), Pos Dana Jaga-Jaga & Masa Depan (Tabungan Bebas Riba), serta Pos Investasi Akhirat (Zakat, Infaq, Wakaf).",
                calculationFormula = "Alokasi Bijak: 50% Pos Kebutuhan Pokok, 20-30% Tabungan/Investasi Halal, 10-20% ZISWAF & Sosial",
                tags = listOf("anggaran", "cash flow", "al-ghazali", "iddikhar", "dana darurat", "manajemen kas"),
                dalilSource = "QS. Al-Furqan: 67 & HR. Bukhari No. 5357",
                dalilArabic = "وَالَّذِينَ إِذَا أَنفَقُوا لَمْ يُسْرِفُوا وَلَمْ يَقْتُرُوا وَكَانَ بَيْنَ ذَٰلِكَ قَوَامًا",
                dalilTranslation = "Dan (termasuk hamba-hamba Tuhan Yang Maha Pengasih) orang-orang yang apabila menginfakkan (harta), mereka tidak berlebihan, dan tidak (pula) kikir, di antara keduanya adalah tengah-tengah yang lurus. (QS. Al-Furqan: 67)."
            ),
            GroundedRuling(
                id = "investasi_pasar_modal_syariah",
                title = "Prinsip Investasi Saham, Sukuk & Reksadana Syariah",
                category = "INVESTASI",
                authority = "Fatwa DSN-MUI No. 40/2003 & No. 135/2020",
                referenceNumber = "Fatwa DSN-MUI No. 40/DSN-MUI/X/2003 & Standar OJK (DES)",
                summary = "Kriteria emiten halal bebas riba, maysir, dan gharar dengan batas rasio hutang berbasis bunga maksimal 45% dan pendapatan non-halal maksimal 10%.",
                detailedRuling = "Kaidah investasi syariah dalam pasar modal menurut Fatwa DSN-MUI: 1) Kegiatan usaha emiten tidak bertentangan dengan syariat (bukan industri miras, perjudian, rokok/pornografi, perbankan ribawi, atau makanan haram). 2) Financial Screening Ratio: Total hutang berbasis bunga dibandingkan total aset tidak lebih dari 45%, dan total pendapatan bunga/non-halal dibandingkan total pendapatan usaha tidak lebih dari 10%. 3) Larangan transaksi spekulatif: Dilarang short selling, margin trading berbunga, insider trading, dan transaksi semu (najsy).",
                calculationFormula = "Screening Rasio: Hutang Berbunga / Total Aset <= 45% & Pendapatan Non-Halal / Total Revenue <= 10%",
                tags = listOf("investasi syariah", "saham syariah", "sukuk", "dsn mui", "screening", "pasar modal"),
                dalilSource = "Fatwa DSN-MUI No. 40/2003 & HR. Abu Dawud No. 3383",
                dalilArabic = "إِنَّ اللَّهَ يَقُولُ: أَنَا ثَالِثُ الشَّرِيكَيْنِ مَا لَمْ يَخُنْ أَحَدُهُمَا صَاحِبَهُ، فَإِذَا خَانَهُ خَرَجْتُ مِنْ بَيْنِهِمَا",
                dalilTranslation = "Dari Abu Hurairah RA, Rasulullah SAW bersabda dalam Hadits Qudsi: 'Sesungguhnya Allah berfirman: Aku adalah pihak ketiga dari dua orang yang berserikat (bermitra dagang) selama salah seorang di antara mereka tidak mengkhianati kawannya. Apabila salah seorang berkhianat, maka Aku keluar dari perserikatan mereka.' — HR. Abu Dawud (No. 3383) & Al-Hakim."
            ),
            GroundedRuling(
                id = "zakat_pertanian_perkebunan",
                title = "Zakat Hasil Pertanian, Perkebunan & Tanaman Pangan",
                category = "ZAKAT",
                authority = "HR. Bukhari No. 1483 & Peraturan BAZNAS",
                referenceNumber = "Kitab Al-Umm Imam Syafi'i & PMA No. 52/2014",
                summary = "Zakat hasil bumi yang wajib dikeluarkan saat panen dengan kadar 5% (jika memakai pengairan berbayar/mekanik) atau 10% (jika tadah hujan/alami) bila mencapai nisab 5 wasaq (653 kg beras).",
                detailedRuling = "Ketentuan zakat hasil pertanian: 1) Nisab adalah 5 wasaq atau setara dengan 653 kg gabah/beras. 2) Dikeluarkan pada waktu panen ('wa aatuu haqqahu yawma hashadih'). 3) Kadar zakat: 10% (sepersepuluh/al-'usyr) untuk tanaman yang dialiri air hujan, sungai, atau mata air tanpa biaya operasional pengairan; dan 5% (setengah dari sepersepuluh/nisfu al-'usyr) untuk tanaman yang disiram menggunakan kincir, pompa mesin, atau biaya irigasi berbayar.",
                calculationFormula = "Hasil Panen x 10% (Pengairan Alami) ATAU Hasil Panen x 5% (Pengairan Berbayar/Mesin)",
                tags = listOf("zakat pertanian", "panen", "5%", "10%", "wasaq", "hasil bumi"),
                dalilSource = "QS. Al-An'am: 141 & HR. Bukhari No. 1483",
                dalilArabic = "كُلُوا مِن ثَمَرِهِ إِذَا أَثْمَرَ وَآتُوا حَقَّهُ يَوْمَ حَصَادِهِ ۖ وَلَا تُسْرِفُوا... فِيمَا سَقَتِ السَّمَاءُ وَالْعُيُونُ أَوْ كَانَ عَثَرِيًّا الْعُشْرُ، وَمَا سُقِيَ بِالنَّضْحِ نِصْفُ الْعُشْرِ",
                dalilTranslation = "'Makanlah dari buahnya apabila ia berbuah dan berikanlah haknya (zakatnya) pada hari memetik hasilnya...' (QS. Al-An'am: 141). Rasulullah SAW bersabda: 'Pada tanaman yang diairi oleh air hujan, mata air, atau air resapan tanah padanya wajib sepersepuluh (10%), dan pada tanaman yang diairi dengan penyiraman berbiaya padanya wajib setengah dari sepersepuluh (5%).' — HR. Bukhari (No. 1483)."
            )
        )
    }

    // Comprehensive Financial Literature Database
    val literatureDatabase = remember {
        listOf(
            FinancialLiterature(
                id = "kitab_al_amwal",
                title = "Kitab Al-Amwal (Buku Induk Harta & Keuangan Publik Islam)",
                author = "Imam Abu 'Ubaid Al-Qasim bin Sallam (154 - 224 H)",
                era = "Masa Salafush Shalih (Abad ke-2 H)",
                category = "KITAB_KLASIK",
                primaryTopics = listOf("Zakat Rikaz & Ma'din", "Kadar Khumus 20%", "Baitul Mal", "Keadilan Fiskal", "Arus Kas Publik"),
                corePrinciple = "Menetapkan dalil qath'i kewajiban zakat 20% (Al-Khumus) untuk Rikaz (harta temuan purbakala) dan keharusan transparansi kas baitul mal yang tidak boleh ditimbun melainkan disalurkan segera kepada fakir miskin.",
                detailedSummary = "Kitab Al-Amwal merupakan karya babon paling otoritatif dalam sejarah peradaban Islam mengenai tata kelola keuangan publik, perlindungan hak milik individu, dan mekanisme distribusi zakat. Pada Bab 'Fi Ma Yajibu fihil Khumus minar Rikaz', Abu 'Ubaid meriwayatkan sanad hadits shahih muttashil dari Abu Hurairah dan menjelaskan secara rinci bahwa rikaz adalah harta purbakala jahiliyah yang ditemukan terpendam di tanah tak bertuan, dan kewajiban zakatnya adalah 20% seketika tanpa syarat haul dan nisab.",
                keyQuotesOrHadith = "وَفِي الرِّكَازِ الْخُمُسُ — 'Dan pada rikaz (harta temuan purbakala) wajib zakat seperlima (20%).' (Kitab Al-Amwal No. 858-865).",
                arabicDalil = "عَنْ أَبِي هُرَيْرَةَ رَضِيَ اللَّهُ عَنْهُ، أَنَّ رَسُولَ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: «... وَفِي الرِّكَازِ الْخُمُسُ»",
                dalilTranslation = "Dari Abu Hurairah RA, bahwasanya Rasulullah SAW bersabda: '... Dan pada rikaz (harta temuan purbakala/harta karun zaman jahiliyyah) kewajiban zakatnya adalah seperlima (20%).' — Hadits Shahih Muttafaq 'Alaih (HR. Bukhari No. 1499, HR. Muslim No. 1710, Kitab Al-Amwal Hal. 340).",
                practicalImplementation = "Dalam aplikasi Amanah, setiap transaksi penerimaan kategori Rikaz / Harta Temuan otomatis dialokasikan 20% untuk pos zakat/sosial mustahiq fakir miskin, dan 80% sisanya menjadi hak halal bersih pengguna.",
                referenceCitation = "Kitab Al-Amwal, Tahqiq Dr. Muhammad Khalil Harras, Dar al-Kutub al-'Ilmiyyah, Beirut, Hal. 340-365."
            ),
            FinancialLiterature(
                id = "tafsir_ayat_mudayanah",
                title = "Tafsir Ayat Mudayanah (Ayat Terpanjang QS. Al-Baqarah: 282)",
                author = "Imam Al-Qurthubi & Ibnu Katsir (Ushul Fiqih Muamalah)",
                era = "Khazanah Tafsir Al-Qur'an Klasik",
                category = "KITAB_KLASIK",
                primaryTopics = listOf("Pencatatan Keuangan", "Transparansi Utang-Piutang", "Integritas Akuntan", "Pencegahan Keraguan/Sengketa", "Ayat 282"),
                corePrinciple = "Kewajiban dan sunnah muakkadah mencatat setiap transaksi non-tunai, mutasi kas, dan amanah hutang piutang dengan teliti oleh pencatat yang adil tanpa mengurangi hak siapapun walau sekecil apa pun.",
                detailedSummary = "Ayat ke-282 Surat Al-Baqarah menguraikan 18 hukum fiqih fundamental tata laksana keuangan: kewajiban dokumentasi tertulis transaksi bertempo, larangan menolak bagi pencatat yang diminta bantuan, kewajiban pihak yang berhutang mendiktekan secara jujur, anjuran tidak pernah bosan mencatat transaksi kecil maupun besar ('wala tas'amu an taktubuuhu shaghiiran aw kabiiran ilaa ajalih'), serta fungsi pembukuan sebagai benteng keadilan dan penangkal keraguan finansial.",
                keyQuotesOrHadith = "وَلَا تَسْأَمُوا أَن تَكْتُبُوهُ صَغِيرًا أَوْ كَبِيرًا إِلَىٰ أَجَلِهِ ۚ ذَٰلِكُمْ أَقْسَطُ عِندَ اللَّهِ وَأَقْوَمُ لِلشَّهَادَةِ وَأَدْنَىٰ أَلَّا تَرْتَابُوا",
                arabicDalil = "يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا تَدَايَنتُم بِدَيْنٍ إِلَىٰ أَجَلٍ مُّسَمًّى فَاكْتُبُوهُ ۚ وَلْيَكْتُب بَّيْنَكُمْ كَاتِبٌ بِالْعَدْلِ... وَلَا تَسْأَمُوا أَن تَكْتُبُوهُ صَغِيرًا أَوْ كَبِيرًا إِلَىٰ أَجَلِهِ",
                dalilTranslation = "'Wahai orang-orang yang beriman! Apabila kamu melakukan utang piutang untuk waktu yang ditentukan, hendaklah kamu menuliskannya. Dan hendaklah seorang penulis di antara kamu menuliskannya dengan adil... Dan janganlah kamu jemu menuliskannya, baik kecil maupun besar sampai batas waktu pembayarannya...' (QS. Al-Baqarah: 282).",
                practicalImplementation = "Aplikasi Amanah menerapkan kaidah ayat ini melalui sistem buku besar berpasangan (double-entry ledger), riwayat mutasi kas lengkap, rincian piutang jatuh tempo, serta pembukuan arus kas yang transparan.",
                referenceCitation = "Al-Jami' li Ahkam al-Qur'an (Tafsir Al-Qurthubi), Jilid 3 Hal. 378-412 & Tafsir Al-Qur'an Al-'Azhim (Ibnu Katsir)."
            ),
            FinancialLiterature(
                id = "bidayat_al_mujtahid",
                title = "Bidayat al-Mujtahid wa Nihayat al-Muqtashid",
                author = "Imam Al-Qadhi Ibnu Rusyd Al-Qurthubi (520 - 595 H)",
                era = "Khilafah Andalusia / Abad Pertengahan",
                category = "KITAB_KLASIK",
                primaryTopics = listOf("Fiqih Muamalah 4 Mazhab", "Akad Hutang Qardh", "Larangan Riba Nasi'ah", "Zakat Rikaz & Emas", "Kemitraan Syirkah"),
                corePrinciple = "Mengkomparasikan ijtihad ulama 4 Mazhab secara adil dan ilmiah mengenai akad-akad keuangan, menetapkan konsensus bahwa setiap pinjaman yang mensyaratkan bunga tambahan adalah riba yang batil, serta menjabarkan rukun sah transaksi jual beli.",
                detailedSummary = "Dalam Kitab Al-Buyu' (Jual Beli) dan Kitab Az-Zakah, Ibnu Rusyd menguraikan perdebatan mazhab fikih dengan metodologi ushul fiqih yang cemerlang. Beliau menegaskan kesepakatan jumhur ulama atas kewajiban zakat 20% rikaz dan membedakannya dari ma'din (tambang mineral yang memerlukan biaya eksplorasi berat), serta menegaskan bahwa barang temuan modern (luqathah) haram langsung dipotong melainkan wajib diumumkan.",
                keyQuotesOrHadith = "كُلُّ قَرْضٍ جَرَّ مَنْفَعَةً فَهُوَ رِبًا — 'Setiap akad pinjaman yang menarik keuntungan/bunga bagi pemberi pinjaman adalah riba.' (Kaidah Ijma' Fiqih).",
                arabicDalil = "كُلُّ قَرْضٍ جَرَّ مَنْفَعَةً فَهُوَ رِبًا... وَأَجْمَعَ الْعُلَمَاءُ عَلَى أَنَّ فِي الرِّكَازِ الْخُمُسَ لِحَدِيثِ رَسُولِ اللَّهِ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ",
                dalilTranslation = "'Setiap akad pinjaman yang menarik keuntungan/bunga bagi pihak pemberi pinjaman adalah riba yang batil' (Kaidah Ijma' Fiqih). 'Dan para ulama telah berijma bahwa pada harta rikaz wajib dikeluarkan seperlima (20%) berdasar hadits Rasulullah SAW' (Kitab Az-Zakah).",
                practicalImplementation = "Menjadi dasar aplikasi untuk memisahkan secara tegas antara rekening Bank Syariah dan Bank Konvensional, serta menolak perhitungan bunga pada mutasi kas.",
                referenceCitation = "Bidayatul Mujtahid wa Nihayatul Muqtashid, Dar Ibnu Hazm, Jilid 2 Bab Az-Zakah wal Buyu'."
            ),
            FinancialLiterature(
                id = "ihya_ulumiddin_kasab",
                title = "Ihya' 'Ulumiddin: Kitab Adab al-Kasb wal Ma'isyah",
                author = "Hujjatul Islam Imam Abu Hamid Al-Ghazali (450 - 505 H)",
                era = "Masa Keemasan Islam Klasik",
                category = "KITAB_KLASIK",
                primaryTopics = listOf("Manajemen Anggaran Keluarga", "Tingkatan Harta Halal & Syubhat", "Keadilan Takaran", "Pencegahan Monopoli", "Keseimbangan Finansial"),
                corePrinciple = "Menetapkan prinsip tata kelola kas mukmin yang seimbang (iqtishad): harta adalah sarana ibadah menuju akhirat, mencari nafkah halal adalah kewajiban fardhu, dan pengeluaran harus diatur proporsional sesuai hierarki Dharuriyyat, Hajiyyat, dan Tahsiniyyat.",
                detailedSummary = "Imam Al-Ghazali menjabarkan 5 adab pokok seorang pengusaha dan pengelola keuangan: 1) Niat ibadah dan menjaga kehormatan diri dari meminta-minta, 2) Menjauhi segala bentuk gharar dan penipuan (tadlis), 3) Berlaku jujur dalam mencatat hutang piutang, 4) Membatasi konsumsi agar tidak terjatuh dalam israf, 5) Menyisihkan tabungan darurat (iddikhar) untuk ketahanan keluarga menghadapi masa depan.",
                keyQuotesOrHadith = "طَلَبُ الْحَلَالِ فَرِيضَةٌ بَعْدَ الْفَرِيضَةِ — 'Mencari rezeki yang halal adalah kewajiban setelah kewajiban pokok ibadah.' (HR. Ath-Thabrani).",
                arabicDalil = "طَلَبُ الْحَلَالِ فَرِيضَةٌ بَعْدَ الْفَرِيضَةِ... وَمَا كَانَ مِنْ فُضُولِ الْأَمْوَالِ فَلْيُدَّخَرْ لِنَوَائِبِ الزَّمَانِ وَحَاجَاتِ الْعِيَالِ",
                dalilTranslation = "'Mencari rezeki halal adalah kewajiban setelah kewajiban pokok ibadah' (HR. Ath-Thabrani). Imam Al-Ghazali menegaskan: 'Kelebihan dari kebutuhan primer hendaknya disimpan (iddikhar) sebagai cadangan menghadapi musibah waktu dan kebutuhan masa depan keluarga'.",
                practicalImplementation = "Diterapkan langsung dalam fitur Pos Anggaran Amanah, Target Finansial Dana Darurat 3-6 bulan, dan pengingat batas belanja bulanan.",
                referenceCitation = "Ihya' 'Ulumiddin, Jilid 2, Rub'u al-'Adat, Kitab Adab al-Kasbi wal Ma'isyah, Maktabah At-Taufiqiyyah."
            ),
            FinancialLiterature(
                id = "pma_kemenag_52_2014",
                title = "Peraturan Menteri Agama (PMA) RI No. 52 Tahun 2014",
                author = "Kementerian Agama Republik Indonesia",
                era = "Regulasi Positif Hukum Nasional RI",
                category = "FATWA_REGULASI",
                primaryTopics = listOf("Syarat & Tata Cara Hisab Zakat Mal", "Ketentuan Zakat Rikaz 20% (Pasal 24)", "Nisab Standar Emas", "Pendayagunaan Zakat Produktif"),
                corePrinciple = "Regulasi yuridis positif resmi negara Indonesia yang menetapkan tata cara penghitungan zakat mal, zakat fitrah, dan zakat rikaz (Pasal 24 menetapkan tarif 20% seketika tanpa haul).",
                detailedSummary = "PMA No. 52/2014 menjadi payung hukum formal bagi seluruh Lembaga Amil Zakat (BAZNAS & LAZ) di Indonesia. Pada Bagian Kelima Pasal 24 secara eksplisit mengatur: 'Zakat rikaz merupakan zakat yang dikenakan atas harta temuan, dengan ketentuan: a) tidak mensyaratkan haul; b) tidak mensyaratkan nisab; c) kadar zakat sebesar 20% (dua puluh persen)'.",
                keyQuotesOrHadith = "Pasal 24 PMA No. 52/2014: 'Zakat rikaz dikenakan atas harta temuan tidak mensyaratkan haul, tidak mensyaratkan nisab, dan kadar zakat sebesar 20% (dua puluh persen).'",
                arabicDalil = "مَادَّة ٢٤ مِنَ اللَّائِحَةِ الْوِزَارِيَّةِ: زَكَاةُ الرِّكَازِ لَا تُشْتَرَطُ فِيهَا الْحَوْلُ وَلَا النِّصَابُ، وَقَدْرُهَا عِشْرُونَ بِالْمِائَةِ (٢٠٪)",
                dalilTranslation = "Pasal 24 PMA RI No. 52/2014: 'Zakat rikaz merupakan zakat yang dikenakan atas harta temuan, dengan ketentuan: tidak mensyaratkan haul, tidak mensyaratkan nisab, dan kadar zakat sebesar 20% (dua puluh persen).'",
                practicalImplementation = "Menjadi rujukan legal formal saat aplikasi Amanah menampilkan pemotongan 20% pada pendapatan Non-Kasab: Rikaz/Temuan.",
                referenceCitation = "Berita Negara Republik Indonesia Tahun 2014 No. 1716, Kementerian Agama RI."
            ),
            FinancialLiterature(
                id = "himpunan_fatwa_dsn_mui",
                title = "Himpunan Fatwa Dewan Syariah Nasional MUI (DSN-MUI)",
                author = "Dewan Syariah Nasional Majelis Ulama Indonesia",
                era = "Regulasi Kontemporer Indonesia (2000 - Sekarang)",
                category = "FATWA_REGULASI",
                primaryTopics = listOf("Perbankan Syariah (Wadiah & Mudharabah)", "Pasar Modal & Saham Halal", "Pinjaman Kebajikan Qardhul Hasan", "Asuransi Syariah", "Pembersihan Dana Non-Halal"),
                corePrinciple = "Rujukan tertinggi hukum muamalah maliyyah di Indonesia yang menjadi landasan regulasi Bank Indonesia (BI), Otoritas Jasa Keuangan (OJK), dan Dewan Pengawas Syariah (DPS) nasional.",
                detailedSummary = "Memuat lebih dari 150 fatwa yang mengkodifikasi akad-akad modern. Di antaranya Fatwa No. 01/2000 (Giro Wadiah), Fatwa No. 02/2000 (Tabungan Mudharabah), Fatwa No. 04/2000 (Murabahah), Fatwa No. 19/2001 (Qardh Hasan), Fatwa No. 40/2003 (Pasar Modal Syariah), Fatwa No. 123/2018 (Penggunaan Dana Sosial Non-Halal untuk Fasilitas Umum), dan Fatwa No. 135/2020 (Saham Syariah).",
                keyQuotesOrHadith = "Fatwa DSN-MUI No. 1/2004: 'Praktek pembungaan uang saat ini telah memenuhi kriteria riba yang diharamkan syariat Islam.'",
                arabicDalil = "الْأَصْلُ فِي الْمُعَامَلَاتِ الْإِبَاحَةُ إِلَّا أَنْ يَدُلَّ دَلِيلٌ عَلَى تَحْرِيمِهَا... وَكُلُّ شَرْطٍ لَيْسَ فِي كِتَابِ اللَّهِ فَهُوَ بَاطِلٌ",
                dalilTranslation = "'Kaidah Fiqih: Hukum asal dalam seluruh muamalah adalah boleh (mubah) kecuali ada dalil shahih yang mengharamkannya.' Sabda Nabi SAW: 'Orang-orang muslim terikat dengan syarat-syarat yang disepakati, kecuali syarat yang mengharamkan yang halal atau menghalalkan yang haram' (HR. At-Tirmidzi).",
                practicalImplementation = "Pemisahan kantong kas Bank Syariah vs Bank Konvensional pada Multi-Wallet dan pengalihan denda/bunga ke pos penyaluran fasilitas umum.",
                referenceCitation = "Buku Himpunan Fatwa DSN-MUI Jilid 1 - 4, Penerbit DSN-MUI & Bank Indonesia."
            ),
            FinancialLiterature(
                id = "fatwa_uang_elektronik_dsn_116",
                title = "Fatwa DSN-MUI No. 116/DSN-MUI/IX/2017 (Uang Elektronik Syariah)",
                author = "Dewan Syariah Nasional Majelis Ulama Indonesia",
                era = "Regulasi Fintech & Dompet Digital Syariah",
                category = "FATWA_REGULASI",
                primaryTopics = listOf("Uang Elektronik / E-Wallet", "Akad Wadiah Saldo Digital", "Akad Qardh Dana Talangan", "Larangan Bunga Saldo", "Cashback Halal"),
                corePrinciple = "Menetapkan akad penempatan dana uang elektronik syariah berlandaskan wadiah (titipan) atau qardh (pinjaman), serta melarang keras bunga saldo atau cashback bersyarat riba.",
                detailedSummary = "Fatwa No. 116/2017 mengatur transaksi digital modern: dana nasabah pada penerbit uang elektronik wajib ditempatkan di bank syariah, penyelenggara dilarang memberikan fasilitas kredit berbunga atas saldo kas, dan promosi cashback hanya sah bila murni hadiah (hibah/tabarru') tanpa syarat transaksi ribawi.",
                keyQuotesOrHadith = "Fatwa No. 116/2017 Pasal 3: 'Penempatan dana pada uang elektronik menggunakan akad Wadi'ah atau Qardh, dan bebas dari riba, gharar, maysir, tadlis, dan risywah.'",
                arabicDalil = "لَا يَحِلُّ مَالُ امْرِئٍ مُسْلِمٍ إِلَّا بِطِيبِ نَفْسٍ مِنْهُ... وَالْأَمَانَةُ تُؤَدَّى إِلَى مَنِ ائْتَمَنَكَ",
                dalilTranslation = "'Tidak halal harta seorang muslim melainkan dengan kerelaan hatinya' (HR. Ahmad). 'Tunaikanlah amanah kepada orang yang mempercayaimu dan janganlah berkhianat kepada orang yang mengkhianatimu' (HR. Abu Dawud No. 3535).",
                practicalImplementation = "Petunjuk tata kelola kantong dompet digital (E-Wallet) di aplikasi Amanah agar terhindar dari pengendapan riba dan biaya siluman.",
                referenceCitation = "Fatwa DSN-MUI No. 116/DSN-MUI/IX/2017, Dewan Syariah Nasional Majelis Ulama Indonesia."
            ),
            FinancialLiterature(
                id = "standar_akuntansi_aaoifi",
                title = "Standar Akuntansi & Syariah AAOIFI (Manama, Bahrain)",
                author = "AAOIFI (Accounting & Auditing Organization for Islamic Financial Institutions)",
                era = "Standar Internasional Global",
                category = "FATWA_REGULASI",
                primaryTopics = listOf("Financial Accounting Standard (FAS)", "Governance & Ethics", "Prinsip Akuntansi Transparan", "Audit Kepatuhan Syariah", "Pencatatan Mudayanah"),
                corePrinciple = "Standar global yang menyelaraskan praktik akuntansi keuangan internasional dengan hukum fiqih muamalah Islam, mengadopsi prinsip ayat mudayanah (QS. Al-Baqarah: 282) untuk pencatatan debit-kredit yang akuntabel.",
                detailedSummary = "AAOIFI menerbitkan standar syariah dan akuntansi yang diadopsi oleh lebih dari 45 negara di dunia. Menetapkan tata cara penyusunan neraca keuangan syariah (Balance Sheet), pemisahan dana kas titipan (wadi'ah), dana investasi bagi hasil (mudharabah muthlaqah), dan dana kebajikan (qardhul hasan).",
                keyQuotesOrHadith = "AAOIFI Shari'ah Standard No. 35: 'All credit and debt obligations must be documented with verified agreement terms and clear settlement deadlines.'",
                arabicDalil = "وَأَوْفُوا الْكَيْلَ وَالْمِيزَانَ بِالْقِسْطِ ۖ لَا نُكَلِّفُ نَفْسًا إِلَّا وُسْعَهَا",
                dalilTranslation = "'Dan sempurnakanlah takaran dan timbangan dengan adil. Kami tidak membebani seseorang melainkan menurut kesanggupannya...' (QS. Al-An'am: 152). Standar AAOIFI No. 35 menegaskan pencatatan kewajiban finansial secara tertulis dan akurat.",
                practicalImplementation = "Mendasari arsitektur double-entry ledger Amanah: pencatatan setiap mutasi kas memiliki debit dan kredit yang selalu seimbang dan akuntabel.",
                referenceCitation = "AAOIFI Shari'ah Standards & Financial Accounting Standards (FAS), Edition 2023, Manama, Bahrain."
            ),
            FinancialLiterature(
                id = "fiqih_zakat_qaradhawi",
                title = "Fiqh az-Zakah: Studi Komparatif Hukum & Filosofi Zakat",
                author = "Prof. Dr. Yusuf Al-Qaradhawi",
                era = "Kontemporer Modern",
                category = "MANAJEMEN_MODERN",
                primaryTopics = listOf("Zakat Profesi & Penghasilan", "Zakat Rikaz & Barang Tambang", "Pemberdayaan Ekonomi Mustahiq", "Hisab Zakat Aset Finansial"),
                corePrinciple = "Karya monumental setebal 2 jilid yang memadukan dalil nash klasik salaf dengan dinamika sistem moneter modern, menjadi pijakan ijma' kontemporer bagi penetapan zakat profesi dan tata kelola baitul mal modern.",
                detailedSummary = "Al-Qaradhawi meneliti seluruh riwayat hadits mengenai rikaz dan menyimpulkan bahwa rikaz mencakup segala kekayaan berharga purbakala yang terpendam tanpa biaya eksploitasi besar dan wajib dikeluarkan 20% seketika demi mengikis ketimpangan sosial dan menyantuni asnaf fakir miskin.",
                keyQuotesOrHadith = "'Zakat bukan sekadar kedermawanan sukarela, melainkan pilar sistem jaminan sosial dan redistribusi keadilan kekayaan dalam Islam.'",
                arabicDalil = "كَيْ لَا يَكُونَ دُولَةً بَيْنَ الْأَغْنِيَاءِ مِنكُمْ... وَفِي أَمْوَالِهِمْ حَقٌّ مَّعْلُومٌ ۝ لِّلسَّائِلِ وَالْمَحْرُومِ",
                dalilTranslation = "'Supaya harta itu jangan hanya beredar di antara orang-orang kaya saja di antara kamu' (QS. Al-Hasyr: 7). 'Dan pada harta-harta mereka ada hak tertentu untuk orang yang meminta dan orang yang berkekurangan' (QS. Al-Ma'arij: 24-25).",
                practicalImplementation = "Landasan edukasi kewajiban zakat rikaz 20% pada aplikasi dan modul hisab zakat profesi bulanan.",
                referenceCitation = "Fiqh az-Zakah (Hukum Zakat), Pustaka Litera AntarNusa, Jilid 1 Bab Zakat Rikaz & Ma'din."
            ),
            FinancialLiterature(
                id = "ekonomi_keuangan_islam_adiwarman",
                title = "Ekonomi Makro & Mikro Islam: Tata Kelola Arus Kas & Investasi",
                author = "Prof. Dr. Adiwarman Azwar Karim, S.E., M.B.A., M.A.E.P.",
                era = "Praktisi & Akademisi Indonesia Kontemporer",
                category = "MANAJEMEN_MODERN",
                primaryTopics = listOf("Manajemen Likuiditas Kas", "Portofolio Bebas Riba", "Mitigasi Risiko Finansial", "Perilaku Konsumsi Islami"),
                corePrinciple = "Menjelaskan secara matematis dan aplikatif bagaimana teori ekonomi Islam diterapkan dalam tata kelola keuangan rumah tangga, perusahaan, hingga perbankan nasional.",
                detailedSummary = "Buku rujukan utama di perguruan tinggi Indonesia ini mengulas teori moneter Islam: uang adalah alat tukar (flow concept) dan bukan komoditas untuk dibungakan (stock concept). Menekankan pentingnya menjaga likuiditas kas, dana darurat, dan menghindari jebakan hutang konsumtif ribawi.",
                keyQuotesOrHadith = "'Uang dalam Islam adalah modal produktif yang harus berputar di sektor riil, bukan ditimbun dan bukan dijadikan alat mencari keuntungan melalui riba bunga.'",
                arabicDalil = "الْغُنْمُ بِالْغُرْمِ... وَالْخَرَاجُ بِالضَّمَانِ",
                dalilTranslation = "'Keuntungan sebanding dengan kesiapan menanggung risiko kerugian' (Kaidah Fiqih). Hadits Nabi SAW: 'Hak mendapatkan keuntungan bersandar pada kewajiban memikul risiko tanggungan' (HR. Abu Dawud No. 3508 & Tirmidzi No. 1285).",
                practicalImplementation = "Diterapkan pada indikator rasio likuiditas kas, pemantauan saldo riil kantong dompet, dan evaluasi portofolio tabungan bebas riba.",
                referenceCitation = "Ekonomi Mikro & Makro Islam, PT RajaGrafindo Persada, Jakarta."
            )
        )
    }

    // State for Literature Tab
    var literatureSearchQuery by remember { mutableStateOf("") }
    var selectedLiteratureCategory by remember { mutableStateOf<String?>(null) }

    val filteredLiterature = remember(literatureSearchQuery, selectedLiteratureCategory, literatureDatabase) {
        literatureDatabase.filter { lit ->
            val matchQuery = literatureSearchQuery.isBlank() ||
                    lit.title.contains(literatureSearchQuery, ignoreCase = true) ||
                    lit.author.contains(literatureSearchQuery, ignoreCase = true) ||
                    lit.corePrinciple.contains(literatureSearchQuery, ignoreCase = true) ||
                    lit.detailedSummary.contains(literatureSearchQuery, ignoreCase = true) ||
                    lit.primaryTopics.any { it.contains(literatureSearchQuery, ignoreCase = true) }

            val matchCategory = selectedLiteratureCategory == null || lit.category == selectedLiteratureCategory
            matchQuery && matchCategory
        }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val customRulingsList by CustomRulingRepository.customRulingsState.collectAsState()

    LaunchedEffect(Unit) {
        CustomRulingRepository.loadAll(context)
    }

    var showAddCustomRulingDialog by remember { mutableStateOf(false) }
    var editingCustomRuling by remember { mutableStateOf<CustomGroundedRuling?>(null) }

    // Combined rulings: User Custom Rulings + Built-in Grounded Database
    val combinedRulings = remember(rulingsDatabase, customRulingsList) {
        val customMapped = customRulingsList.map {
            GroundedRuling(
                id = it.id,
                title = it.title,
                category = it.category,
                authority = it.authority,
                referenceNumber = it.referenceNumber,
                summary = it.summary,
                detailedRuling = it.detailedRuling,
                calculationFormula = it.calculationFormula,
                tags = it.tags,
                dalilSource = it.dalilSource,
                dalilArabic = it.dalilArabic,
                dalilTranslation = it.dalilTranslation,
                isCustomUserRuling = true
            )
        }
        customMapped + rulingsDatabase
    }

    // Filter rulings
    val filteredRulings = remember(searchQuery, selectedCategoryFilter, combinedRulings) {
        combinedRulings.filter { ruling ->
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

    // Live liquid cash calculation across all active wallets
    val liveCashBalance = remember(state.accounts, state.journalEntries) {
        state.accounts.filter { it.category == AccountCategory.ASSET }.sumOf { state.getAccountBalance(it.id) }
    }

    // Interactive Zakat Verification State
    var zakatTypeIndex by remember { mutableIntStateOf(0) } // 0: Maal (Simpanan), 1: Profesi/Gaji, 2: Perdagangan, 3: Rikaz (20%)
    var inputAmountText by remember { mutableStateOf("") }
    var inputGoldPriceText by remember(state.goldPricePerGram) {
        mutableStateOf(state.goldPricePerGram.toLong().toString())
    }
    var inputDebtText by remember { mutableStateOf("") }

    // Gemini AI RAG Consultation State
    var consultationInputText by remember { mutableStateOf("") }
    var isConsultingAi by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    val consultationMessages = remember {
        mutableStateListOf(
            FatwaConsultationMessage(
                sender = "AI",
                text = "Assalamu'alaikum warahmatullahi wabarakatuh. Selamat datang di Konsultan Fatwa & Muamalah Syariah Amanah Ledger. Anda dapat menanyakan hukum transaksi, hisab zakat maal, rikaz 20%, qardh hasan, bunga bank, atau tata kelola kas yang terhubung dengan rujukan fatwa DSN-MUI, SK BAZNAS, dan pustaka fiqih.",
                groundedReferences = listOf("Fatwa DSN-MUI", "SK BAZNAS No. 1/2024", "PMA No. 52/2014")
            )
        )
    }

    val goldPrice = inputGoldPriceText.toDoubleOrNull() ?: state.goldPricePerGram
    val inputAmount = inputAmountText.toDoubleOrNull() ?: 0.0
    val inputDebt = inputDebtText.toDoubleOrNull() ?: 0.0

    // Verification engine results
    val nisabThreshold = remember(zakatTypeIndex, goldPrice) {
        when (zakatTypeIndex) {
            0 -> 85.0 * goldPrice // Zakat Maal: 85g Emas
            1 -> (85.0 * goldPrice) / 12.0 // Zakat Profesi Bulanan: Setara 85g / 12
            2 -> 85.0 * goldPrice // Zakat Perniagaan: 85g Emas
            3 -> 0.0 // Rikaz: Bebas nisab (wajib dikeluarkan 20% seketika tanpa batas nisab)
            else -> 85.0 * goldPrice
        }
    }

    val netAssetCalculated = remember(zakatTypeIndex, inputAmount, inputDebt) {
        when (zakatTypeIndex) {
            2 -> (inputAmount - inputDebt).coerceAtLeast(0.0) // Perniagaan: Aset Lancar - Hutang
            3 -> inputAmount.coerceAtLeast(0.0) // Rikaz: Nilai temuan
            else -> (inputAmount - inputDebt).coerceAtLeast(0.0)
        }
    }

    val isWajibZakat = remember(zakatTypeIndex, netAssetCalculated, nisabThreshold) {
        if (zakatTypeIndex == 3) netAssetCalculated > 0
        else netAssetCalculated >= nisabThreshold && netAssetCalculated > 0
    }

    val calculatedZakatObligation = remember(zakatTypeIndex, isWajibZakat, netAssetCalculated) {
        if (!isWajibZakat) 0.0
        else if (zakatTypeIndex == 3) netAssetCalculated * 0.20 // 20% Rikaz Khumus
        else netAssetCalculated * 0.025
    }

    LaunchedEffect(selectedTab) {
        val tabName = when (selectedTab) {
            0 -> "Ensiklopedia Fatwa & Dalil"
            1 -> "Pustaka Tata Kelola Keuangan"
            2 -> "Verifikator Hisab Zakat & Rikaz"
            else -> "Konsultasi Fatwa AI (RAG)"
        }
        AppDebugLogger.logUserAction(
            "Fatwa & Zakat Grounding",
            "Membuka tab: $tabName"
        )
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            AppDebugLogger.logUserAction(
                "Pencarian Fatwa",
                "Kueri: '$searchQuery' (Ditemukan ${filteredRulings.size} rujukan relevan)"
            )
        }
    }

    LaunchedEffect(isWajibZakat, netAssetCalculated, zakatTypeIndex) {
        if (netAssetCalculated > 0.0) {
            val typeName = when (zakatTypeIndex) {
                0 -> "Zakat Maal / Simpanan"
                1 -> "Zakat Profesi"
                2 -> "Zakat Perniagaan"
                3 -> "Zakat Rikaz (20%)"
                else -> "Zakat"
            }
            AppDebugLogger.logUserAction(
                "Verifikator Zakat",
                "Simulasi $typeName: Bersih Rp ${nf.format(netAssetCalculated.toLong())} -> ${if (isWajibZakat) "WAJIB ZAKAT (Kewajiban: Rp ${nf.format(calculatedZakatObligation.toLong())})" else "Belum Capai Nisab"}"
            )
        }
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Rujukan Resmi Fatwa DSN-MUI & Standar BAZNAS",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
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
            // Tab Selector (4 Tabs)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
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
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Ensiklopedia", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        selectedTab = 1
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Pustaka", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        selectedTab = 2
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Hisab Zakat", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        selectedTab = 3
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Fatwa AI", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal)
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

                    // Add Custom Ruling Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tambah Sumber Fatwa Mandiri",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Dapati fatwa atau ketentuan baru? Tambahkan lengkap dengan teks Arab dan nomor rujukan.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        editingCustomRuling = null
                                        showAddCustomRulingDialog = true
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_custom_ruling_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Search input
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari hukum (contoh: rikaz 20%, zakat emas, bunga bank)...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                                "ZAKAT" to "Zakat & Rikaz 20%",
                                "TATA_KELOLA" to "Tata Kelola & Pembukuan",
                                "RIBA_DAN_HUTANG" to "Riba & Qardh",
                                "INVESTASI" to "Investasi Syariah",
                                "ISRAF_KONSUMSI" to "Israf & Belanja",
                                "SYUBHAT" to "Penyucian Harta"
                            )

                            items(categories, key = { it.second }) { (key, label) ->
                                val isSelected = selectedCategoryFilter == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        selectedCategoryFilter = if (isSelected) null else key
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Results list
                    items(filteredRulings, key = { it.id }) { ruling ->
                        GroundedRulingCard(
                            ruling = ruling,
                            onEdit = if (ruling.isCustomUserRuling) {
                                {
                                    val custom = customRulingsList.find { it.id == ruling.id }
                                    editingCustomRuling = custom
                                    showAddCustomRulingDialog = true
                                }
                            } else null,
                            onDelete = if (ruling.isCustomUserRuling) {
                                {
                                    CustomRulingRepository.delete(context, ruling.id)
                                    Toast.makeText(context, "Ketentuan fatwa berhasil dihapus", Toast.LENGTH_SHORT).show()
                                }
                            } else null
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else if (selectedTab == 1) {
                // TAB 1: Pustaka Tata Kelola Keuangan Syariah & Artikel Berdalil
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Pustaka Tata Kelola Keuangan Syariah",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Artikel rujukan lengkap berdalil teks Arab, terjemahan, dan rujukan kitab klasik hingga fatwa resmi DSN-MUI & Kemenag.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Search input for literature
                    item {
                        OutlinedTextField(
                            value = literatureSearchQuery,
                            onValueChange = { literatureSearchQuery = it },
                            placeholder = { Text("Cari rujukan (contoh: rikaz 20%, al-amwal, kas keluarga, dsn mui)...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Cari", tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                if (literatureSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { literatureSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("literature_search_input")
                        )
                    }

                    // Filter chips for literature
                    item {
                        val literatureCategories = listOf(
                            null to "Semua Literatur",
                            "KITAB_KLASIK" to "Kitab Klasik Muamalah",
                            "FATWA_REGULASI" to "Fatwa & Regulasi Resmi",
                            "MANAJEMEN_MODERN" to "Manajemen Keuangan Modern"
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(literatureCategories, key = { it.second }) { (key, label) ->
                                val isSelected = selectedLiteratureCategory == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                        selectedLiteratureCategory = if (isSelected) null else key
                                    },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Literature list
                    items(filteredLiterature, key = { it.id }) { literature ->
                        FinancialLiteratureCard(literature = literature)
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else if (selectedTab == 2) {
                // TAB 2: Grounded Zakat & Rikaz Verification Engine with Live Balance Auto-fill
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text(
                                        text = "Kalkulator Zakat & Rikaz Otomatis",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Dihitung dari saldo riil dompet kas, nisab 85g emas BAZNAS & dalil rikaz 20% PMA 52/2014.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }

                    // Live Cash Balances & Auto-Fill Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Text("Integrasi Saldo Riil Kas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("${state.primaryCurrencySymbol} ${nf.format(liveCashBalance.toLong())}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }

                                Text(
                                    text = "Mengagregasi seluruh rekening kas tunai, bank syariah, dan e-wallet Anda secara langsung tanpa perlu input manual.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = {
                                        inputAmountText = liveCashBalance.toLong().coerceAtLeast(0L).toString()
                                        Toast.makeText(context, "Saldo riil kas berhasil disalin ke kalkulator!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().testTag("auto_fill_live_balance_btn")
                                ) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("⚡ Gunakan Saldo Riil Kas (${state.primaryCurrencySymbol} ${nf.format(liveCashBalance.toLong())})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4 Prinsip Fiqih Zakat Maal Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("📖 4 Kaidah Syariat Wajib Zakat Maal (Ijma' & BAZNAS):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text("1. Al-Milkut Taam: Kepemilikan penuh secara sah tanpa sengketa.\n2. An-Namaa': Harta berkembang secara riil atau potensial.\n3. Nisab 85g Emas: Telah mencapai batas minimal kewajiban.\n4. Al-Hawl: Berlalu masa 1 tahun Hijriah (354 hari) di atas batas nisab.", fontSize = 10.sp, lineHeight = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Zakat Type Selector (Including Rikaz 20%)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            listOf("Zakat Maal", "Profesi", "Perniagaan", "Rikaz 20%").forEachIndexed { index, label ->
                                val isSelected = zakatTypeIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
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
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Form Inputs
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (zakatTypeIndex == 3) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Ketentuan Syariat Zakat Rikaz 20%:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "«... وَفِي الرِّكَازِ الْخُمُسُ»\n\"Dan pada rikaz (harta temuan purbakala jahiliyah), kewajiban zakatnya adalah seperlima (20%).\" (HR. Bukhari No. 1499 & Muslim No. 1710)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Sesuai PMA Kemenag No. 52/2014 Pasal 24: Zakat rikaz TIDAK mensyaratkan haul dan TIDAK mensyaratkan nisab. Wajib ditunaikan seketika 20% saat ditemukan, dan 80% sisanya adalah hak halal penemu.",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }

                                val amountLabel = when (zakatTypeIndex) {
                                    0 -> "Total Harta Simpanan (Emas + Saldo Tabungan + Kas)"
                                    1 -> "Total Penghasilan / Gaji Bulanan"
                                    2 -> "Nilai Persediaan Dagang + Kas Lancar Usaha"
                                    3 -> "Nilai Pasar Harta Temuan Purbakala (Rikaz)"
                                    else -> "Total Aset"
                                }

                                OutlinedTextField(
                                    value = inputAmountText,
                                    onValueChange = { inputAmountText = it },
                                    label = { Text(amountLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    placeholder = { Text(if (zakatTypeIndex == 3) "Contoh: 50000000" else "Contoh: 120000000", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                                    prefix = { Text("${state.primaryCurrencySymbol} ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                                        label = { Text(if (zakatTypeIndex == 2) "Hutang Jatuh Tempo Usaha" else "Kebutuhan Pokok / Hutang Bulanan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                        placeholder = { Text("0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                                        prefix = { Text("${state.primaryCurrencySymbol} ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (zakatTypeIndex != 3) {
                                    OutlinedTextField(
                                        value = inputGoldPriceText,
                                        onValueChange = { inputGoldPriceText = it },
                                        label = { Text("Acuan Harga Emas Per Gram", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                        prefix = { Text("${state.primaryCurrencySymbol} ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // Verification Output Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWajibZakat) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isWajibZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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
                                            tint = if (isWajibZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (zakatTypeIndex == 3) {
                                                if (isWajibZakat) "WAJIB ZAKAT RIKAZ (20%)" else "MASUKKAN NILAI TEMUAN"
                                            } else {
                                                if (isWajibZakat) "WAJIB KELUARKAN ZAKAT" else "BELUM MENCAPAI NISAB"
                                            },
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWajibZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Surface(
                                        color = if (isWajibZakat) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (zakatTypeIndex == 3) "Tarif 20% (Khumus)" else if (isWajibZakat) "Tarif 2.5%" else "Bebas Zakat",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWajibZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Batas Nisab Standar:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (zakatTypeIndex == 3) {
                                        Text("Bebas Nisab (Wajib Seketika Tanpa Haul)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    } else {
                                        Text("${state.primaryCurrencySymbol} ${nf.format(nisabThreshold.toLong())}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(if (zakatTypeIndex == 3) "Nilai Rikaz Ditemukan:" else "Aset Bersih Dihitung:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${state.primaryCurrencySymbol} ${nf.format(netAssetCalculated.toLong())}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }

                                if (zakatTypeIndex == 3 && isWajibZakat) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Hak Halal Penemu (80%):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${state.primaryCurrencySymbol} ${nf.format((netAssetCalculated * 0.80).toLong())}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(if (zakatTypeIndex == 3) "Kewajiban Zakat Rikaz (20%):" else "Kewajiban Zakat Bersih:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = "${state.primaryCurrencySymbol} ${nf.format(calculatedZakatObligation.toLong())}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isWajibZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else if (selectedTab == 3) {
                // TAB 3: Konsultasi Fatwa Syariah AI (RAG Grounding)
                FatwaAiConsultationTab(
                    context = context,
                    messages = consultationMessages,
                    inputText = consultationInputText,
                    onInputTextChange = { consultationInputText = it },
                    isConsulting = isConsultingAi,
                    onSendMessage = { query ->
                        if (query.isNotBlank() && !isConsultingAi) {
                            consultationMessages.add(
                                FatwaConsultationMessage(
                                    sender = "USER",
                                    text = query.trim()
                                )
                            )
                            consultationInputText = ""
                            isConsultingAi = true
                            coroutineScope.launch {
                                try {
                                    val result = GeminiFatwaRagService.consultFatwa(
                                        context = context,
                                        userQuery = query.trim(),
                                        rulingsDatabase = combinedRulings.map { it.title to "${it.authority} (${it.referenceNumber}): ${it.summary}\n${it.detailedRuling}\n${it.dalilArabic ?: ""} ${it.dalilTranslation ?: ""}" },
                                        literatureDatabase = literatureDatabase.map { it.title to "${it.era} (${it.author}): ${it.corePrinciple}\n${it.detailedSummary}\n${it.practicalImplementation ?: ""}\n${it.referenceCitation}" }
                                    )
                                    val responseText = result.getOrElse {
                                        "Mohon maaf, terjadi kendala saat menghubungi asisten AI syariah: ${it.localizedMessage}. Silakan periksa koneksi internet atau gunakan rujukan pada tab Ensiklopedia dan Pustaka."
                                    }
                                    consultationMessages.add(
                                        FatwaConsultationMessage(
                                            sender = "AI",
                                            text = responseText,
                                            groundedReferences = listOf("Fatwa DSN-MUI", "PMA No. 52/2014", "Standar Fiqih Muamalah")
                                        )
                                    )
                                } finally {
                                    isConsultingAi = false
                                }
                            }
                        }
                    },
                    onOpenApiKeyDialog = { showApiKeyDialog = true }
                )
            }
        }

        // Custom Ruling Dialog
        if (showAddCustomRulingDialog) {
            AddOrEditCustomRulingDialog(
                initialRuling = editingCustomRuling,
                onDismiss = {
                    showAddCustomRulingDialog = false
                    editingCustomRuling = null
                },
                onSave = { ruling ->
                    CustomRulingRepository.save(context, ruling)
                    showAddCustomRulingDialog = false
                    editingCustomRuling = null
                    Toast.makeText(context, "Ketentuan fatwa berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Gemini API Key Dialog
        if (showApiKeyDialog) {
            GeminiApiKeyDialog(
                currentKey = GeminiFatwaRagService.getCustomApiKey(context),
                onDismiss = { showApiKeyDialog = false },
                onSaveKey = { newKey ->
                    GeminiFatwaRagService.setCustomApiKey(context, newKey)
                    showApiKeyDialog = false
                    Toast.makeText(context, if (newKey.isBlank()) "Kunci API dikosongkan (menggunakan bawaan)" else "Kunci API tersimpan", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun GroundedRulingCard(
    ruling: GroundedRuling,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (showDeleteConfirmDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hapus Sumber Fatwa?", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus ketentuan \"${ruling.title}\" dari daftar rujukan kustom?", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hapus", fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Batal", fontSize = 12.sp)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("grounded_ruling_${ruling.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = ruling.authority,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (ruling.isCustomUserRuling) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "✨ Kustom Mandiri",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (ruling.isCustomUserRuling && (onEdit != null || onDelete != null)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (onEdit != null) {
                                IconButton(
                                    onClick = onEdit,
                                    modifier = Modifier.size(28.dp).testTag("edit_custom_ruling_${ruling.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Fatwa",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (onDelete != null) {
                                IconButton(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier.size(28.dp).testTag("delete_custom_ruling_${ruling.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus Fatwa",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = ruling.referenceNumber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Text(
                text = ruling.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = ruling.summary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Kaidah Fiqih Terperinci:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ruling.detailedRuling,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (ruling.dalilSource != null && ruling.dalilArabic != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Nash Dalil Lengkap: ${ruling.dalilSource}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = ruling.dalilArabic,
                                    fontSize = 13.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (ruling.dalilTranslation != null) {
                                    Text(
                                        text = "\"${ruling.dalilTranslation}\"",
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Rumus Hisab Syariah:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ruling.calculationFormula,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
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
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FinancialLiteratureCard(literature: FinancialLiterature) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("financial_literature_${literature.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category Badge & Era Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (catLabel, catColor) = when (literature.category) {
                    "KITAB_KLASIK" -> "Kitab Klasik Babon" to MaterialTheme.colorScheme.primary
                    "FATWA_REGULASI" -> "Fatwa & Regulasi Resmi" to MaterialTheme.colorScheme.primary
                    else -> "Manajemen Keuangan Modern" to MaterialTheme.colorScheme.secondary
                }
                Surface(
                    color = catColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = catLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = catColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = literature.era,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Title & Author
            Text(
                text = literature.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = literature.author,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Core Principle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                    .border(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Prinsip Pokok Tata Kelola:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = literature.corePrinciple,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Primary Topics Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(literature.primaryTopics) { topic ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "#$topic",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Expanded Section (Complete Article Details)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Detailed Summary / Full Article
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Kajian & Ulasan Ilmiah Terperinci:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = literature.detailedSummary,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    // Arabic Dalil & Translation
                    if (literature.arabicDalil != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Nash Dalil Arab & Takhrij:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = literature.arabicDalil,
                                    fontSize = 13.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (literature.dalilTranslation != null) {
                                    Text(
                                        text = literature.dalilTranslation,
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Practical Implementation in Amanah App
                    if (literature.practicalImplementation != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                                .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Implementasi Tata Kelola pada Aplikasi Amanah:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Text(
                                    text = literature.practicalImplementation,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Reference Citation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Rujukan: ${literature.referenceCitation}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Expand / Collapse Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (isExpanded) "Tutup Artikel ▲" else "Baca Artikel & Dalil Lengkap ▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

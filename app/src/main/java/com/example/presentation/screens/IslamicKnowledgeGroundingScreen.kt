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
    val tags: List<String>
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
                title = "Zakat Maal (Harta Emas, Perak & Simpanan)",
                category = "ZAKAT",
                authority = "Fatwa DSN-MUI & SK BAZNAS",
                referenceNumber = "Peraturan BAZNAS No. 1 / 2024",
                summary = "Kewajiban zakat harta simpanan dan tabungan sebesar 2.5% jika telah memenuhi nisab setara 85 gram emas murni dan mengendap selama 1 tahun Hijriah (Haul).",
                detailedRuling = "Syarat wajib: 1) Milik sempurna (al-milkut taam), 2) Berkembang (an-namaa'), 3) Mencapai nisab (85g emas), 4) Melebihi kebutuhan pokok, 5) Bebas hutang jatuh tempo, 6) Mencapai Haul (1 tahun Hijriah). Harta yang digabungkan: Uang tunai, tabungan bank, deposito, emas batangan, perhiasan non-pakai wajar.",
                calculationFormula = "Total Aset Simpanan (Emas + Saldo Kas + Tabungan) x 2.5% (jika Total >= 85 gram x Harga Emas)",
                tags = listOf("zakat maal", "emas", "tabungan", "nisab 85g", "haul 1 tahun")
            ),
            GroundedRuling(
                id = "zakat_penghasilan",
                title = "Zakat Profesi & Penghasilan Bulanan",
                category = "ZAKAT",
                authority = "Fatwa MUI No. 3 / 2003 & SK BAZNAS 2024",
                referenceNumber = "Fatwa Komisi Fatwa MUI No. 3/2003",
                summary = "Zakat atas penghasilan gaji, honorarium, bonus, dan dividen profesi dikeluarkan saat menerima jika mencapai nisab setara 85 gram emas per tahun (atau setara 524 kg beras).",
                detailedRuling = "Kadar zakat penghasilan adalah 2.5%. Pendekatan perhitungan dapat menggunakan bruto (langsung dari total pemasukan) atau netto (setelah dikurangi kebutuhan pokok riil keluarga dan hutang konsumtif bulanan). BAZNAS menyarankan pendekatan bruto untuk kehati-hatian syariah.",
                calculationFormula = "Penghasilan Bruto Bulanan x 2.5% (Nisab Bulanan: (85 x Harga Emas) / 12)",
                tags = listOf("zakat profesi", "gaji", "penghasilan", "freelance", "sk baznas")
            ),
            GroundedRuling(
                id = "zakat_perniagaan",
                title = "Zakat Perniagaan / Usaha Dagang",
                category = "ZAKAT",
                authority = "Ketentuan Fiqih Muamalah 4 Mazhab",
                referenceNumber = "Kompilasi Hukum Islam & BAZNAS",
                summary = "Zakat atas perputaran aset lancar usaha dagang yang telah berjalan selama 1 tahun haul.",
                detailedRuling = "Rumus perniagaan syariah: (Nilai Barang Dagangan / Persediaan Akhir + Kas Usaha + Piutang Lancar Tertagih - Hutang Usaha Jatuh Tempo) x 2.5%. Aset tetap seperti ruko, etalase, dan kendaraan operasional TIDAK dihitung zakatnya.",
                calculationFormula = "[Persediaan + Kas Usaha + Piutang Lancar - Hutang Jatuh Tempo] x 2.5%",
                tags = listOf("zakat dagang", "perniagaan", "persediaan", "piutang", "bisnis")
            ),
            GroundedRuling(
                id = "larangan_riba_bunga",
                title = "Pengharaman Riba & Bunga Bank Konvensional",
                category = "RIBA_DAN_HUTANG",
                authority = "Fatwa MUI No. 1 / 2004",
                referenceNumber = "Keputusan Fatwa MUI No. 1/2004",
                summary = "Bunga (interest) yang dikenakan dalam transaksi pinjaman uang pada bank/kredit konvensional hukumnya HARAM karena termasuk kategori Riba Nasi'ah.",
                detailedRuling = "Setiap tambahan yang disyaratkan di awal dalam akad pinjam-meminjam (Qardh) adalah Riba ('Kullu qardhin jarra manfa'atan fahuwa riba'). Alternatif syariah: Akad Jual-Beli Murabahah (margin jelas), Ijarah Multijasa (ujrah sewa manfaat), atau Bagi Hasil Mudharabah/Musyarakah.",
                calculationFormula = null,
                tags = listOf("riba", "bunga bank", "pinjaman", "paylater", "murabahah")
            ),
            GroundedRuling(
                id = "qardh_hasan",
                title = "Fiqih Qardh Hasan (Pinjaman Kebajikan)",
                category = "RIBA_DAN_HUTANG",
                authority = "Fatwa DSN-MUI No. 19/DSN-MUI/IV/2001",
                referenceNumber = "Fatwa DSN-MUI No. 19/2001",
                summary = "Pinjaman murni tolong-menolong tanpa mengambil keuntungan komersial, denda keterlambatan, atau bunga tambahan.",
                detailedRuling = "Pemberi pinjaman hanya berhak menerima kembali pokok yang dipinjamkan. Peminjam boleh memberikan hadiah atau kelebihan secara sukarela pada saat pelunasan tanpa perjanjian di awal ('Husnul Qadha'). Jika peminjam kesulitan, sunnah memberi tenggang waktu atau mengikhlaskannya sebagai sedekah.",
                calculationFormula = null,
                tags = listOf("qardh hasan", "pinjaman syariah", "tanpa bunga", "tolong menolong")
            ),
            GroundedRuling(
                id = "israf_tabdzir",
                title = "Batasan Israf (Berlebihan) & Tabdzir (Sia-Sia)",
                category = "ISRAF_KONSUMSI",
                authority = "Al-Qur'an (QS. Al-A'raf: 31, Al-Isra: 26-27)",
                referenceNumber = "Kaidah Fiqih Konsumsi Islam",
                summary = "Larangan membelanjakan harta secara berlebihan melebihi batas kepatutan atau membelanjakan pada hal yang tidak diridhai Allah.",
                detailedRuling = "Prioritas konsumsi dalam Maqashid Syariah: 1) Dharuriyyat (Pokok untuk kelangsungan hidup), 2) Hajiyyat (Kenyamanan kerja dan efisiensi), 3) Tahsiniyyat (Pelengkap/Estetika halal). Mengutamakan Tahsiniyyat sambil mengabaikan Dharuriyyat atau zakat/infaq adalah pelanggaran kaidah skala prioritas fiqih.",
                calculationFormula = null,
                tags = listOf("israf", "tabdzir", "anggaran", "konsumsi", "maqashid syariah")
            ),
            GroundedRuling(
                id = "harta_syubhat",
                title = "Penyaluran Harta Non-Halal / Syubhat",
                category = "SYUBHAT",
                authority = "Fatwa DSN-MUI & Keputusan Ulama Fiqih Kontemporer",
                referenceNumber = "Fatwa DSN-MUI No. 123/2018",
                summary = "Pendapatan non-halal (misal: bunga bank konvensional, sanksi keterlambatan) harus dibersihkan dan disalurkan ke fasilitas umum/dhuafa tanpa niat sedekah berpahala.",
                detailedRuling = "Penyaluran harta non-halal wajib diarahkan untuk: Pembangunan fasilitas umum (jalan, jembatan, toilet umum), penanggulangan bencana, atau bantuan fakir miskin. Harta ini tidak boleh digunakan untuk konsumsi pribadi, membangun masjid, atau mencetak mushaf Al-Qur'an.",
                calculationFormula = null,
                tags = listOf("harta syubhat", "bunga bank", "pembersihan harta", "fasilitas umum")
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

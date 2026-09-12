package com.example.presentation.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.faraidh.DeceasedGender
import com.example.core.faraidh.FaraidhEngine
import com.example.core.faraidh.FaraidhShare
import com.example.core.faraidh.HeirInput
import com.example.core.state.AmanahLedgerViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaraidhScreen(
    viewModel: AmanahLedgerViewModel,
    onBackClick: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }

    var grossEstateText by remember { mutableStateOf(uiState.totalAssets.toLong().toString()) }
    var funeralCostText by remember { mutableStateOf("5000000") }
    var debtsText by remember { mutableStateOf("0") }
    var wasiatText by remember { mutableStateOf("0") }

    var gender by remember { mutableStateOf(DeceasedGender.PRIA) }
    var hasSpouse by remember { mutableStateOf(true) }
    var wifeCount by remember { mutableIntStateOf(1) }
    var hasFather by remember { mutableStateOf(true) }
    var hasMother by remember { mutableStateOf(true) }
    var sonCount by remember { mutableIntStateOf(1) }
    var daughterCount by remember { mutableIntStateOf(2) }
    var brotherCount by remember { mutableIntStateOf(0) }
    var sisterCount by remember { mutableIntStateOf(0) }

    val calculationResult = remember(
        grossEstateText, funeralCostText, debtsText, wasiatText,
        gender, hasSpouse, wifeCount, hasFather, hasMother,
        sonCount, daughterCount, brotherCount, sisterCount
    ) {
        val gross = grossEstateText.toDoubleOrNull() ?: 0.0
        val funeral = funeralCostText.toDoubleOrNull() ?: 0.0
        val debts = debtsText.toDoubleOrNull() ?: 0.0
        val wasiat = wasiatText.toDoubleOrNull() ?: 0.0

        val input = HeirInput(
            deceasedGender = gender,
            hasSpouse = hasSpouse,
            wifeCount = wifeCount,
            sonCount = sonCount,
            daughterCount = daughterCount,
            hasFather = hasFather,
            hasMother = hasMother,
            fullBrotherCount = brotherCount,
            fullSisterCount = sisterCount,
            funeralExpenses = funeral,
            deceasedDebts = debts,
            wasiatBequest = wasiat
        )
        FaraidhEngine.calculate(gross, input)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kalkulator Waris Syariah", fontWeight = FontWeight.Bold)
                        Text(
                            "Faraidh Engine (QS. An-Nisa: 11-12, 176)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("faraidh_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("faraidh_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // Dalil Card Lengkap Fiqih Waris (Al-Qur'an & As-Sunnah)
            item {
                var isDalilExpanded by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Balance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Ketetapan Dalil Hukum Waris Islam",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            TextButton(onClick = { isDalilExpanded = !isDalilExpanded }) {
                                Text(if (isDalilExpanded) "Ringkas" else "Buka Dalil Lengkap", fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "\"(Pembagian tersebut adalah) ketetapan dari Allah. Sungguh, Allah Maha Mengetahui, Maha Bijaksana.\" (QS. An-Nisa: 11)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (isDalilExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. QS. An-Nisa: 11 (Hak Anak & Orang Tua)
                            Text("1. QS. An-Nisa: 11 (Hak Anak & Orang Tua)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "يُوصِيكُمُ اللَّهُ فِي أَوْلَادِكُمْ ۖ لِلذَّكَرِ مِثْلُ حَظِّ الْأُنثَيَيْنِ ۚ فَإِن كُنَّ نِسَاءً فَوْقَ اثْنَتَيْنِ فَلَهُنَّ ثُلُثَا مَا تَرَكَ ۖ وَإِن كَانَتْ وَاحِدَةً فَلَهَا النِّصْفُ ۚ وَلِأَبَوَيْهِ لِكُلِّ وَاحِدٍ مِّنْهُمَا السُّدُسُ مِمَّا تَرَكَ إِن كَانَ لَهُ وَلَدٌ",
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "\"Allah mensyariatkan bagimu tentang pembagian warisan untuk anak-anakmu: bagian seorang anak laki-laki sama dengan bagian dua orang anak perempuan. Dan jika anak itu semuanya perempuan lebih dari dua, maka bagi mereka dua pertiga dari harta yang ditinggalkan. Jika dia seorang anak perempuan saja, maka dia memperoleh separuh. Dan untuk kedua ibu-bapak, masing-masing mendapat seperenam jika dia mempunyai anak...\"",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. QS. An-Nisa: 12 (Hak Pasangan Hidup)
                            Text("2. QS. An-Nisa: 12 (Hak Suami & Istri)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "وَلَكُمْ نِصْفُ مَا تَرَكَ أَزْوَاجُكُمْ إِن لَّمْ يَكُن لَّهُنَّ وَلَدٌ ۚ فَإِن كَانَ لَهُنَّ وَلَدٌ فَلَكُمُ الرُّبُعُ مِمَّا تَرَكْنَ ۚ ... وَلَهُنَّ الرُّبُعُ مِمَّا تَرَكْتُمْ إِن لَّمْ يَكُن لَّكُمْ وَلَدٌ ۚ فَإِن كَانَ لَكُمْ وَلَدٌ فَلَهُنَّ الثُّمنُ مِمَّا تَرَكْتُم",
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "\"Dan bagianmu (suami-suami) adalah seperdua dari harta yang ditinggalkan oleh istri-istrimu jika mereka tidak mempunyai anak. Jika mereka mempunyai anak, maka kamu mendapat seperempat... Para istri memperoleh seperempat jika kamu tidak mempunyai anak. Jika kamu mempunyai anak, maka para istri memperoleh seperdelapan...\"",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. QS. An-Nisa: 176 (Hak Saudara & Kalalah)
                            Text("3. QS. An-Nisa: 176 (Ayat Kalalah & Ashabah Saudara)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "يَسْتَفْتُونَكَ قُلِ اللَّهُ يُفْتِيكُمْ فِي الْكَلَالَةِ ۚ إِنِ امْرُؤٌ هَلَكَ لَيْسَ لَهُ وَلَدٌ وَلَهُ أُخْتٌ فَلَهَا نِصْفُ مَا تَرَكَ ۚ وَهُوَ يَرِثُهَا إِن لَّمْ يَكُن لَّهَا وَلَدٌ ۚ فَإِن كَانَتَا اثْنَتَيْنِ فَلَهُمَا الثُّلُثَانِ مِمَّا تَرَكَ ۚ وَإِن كَانُوا إِخْوَةً رِّجَالًا وَنِسَاءً فَلِلذَّكَرِ مِثْلُ حَظِّ الْأُنثَيَيْنِ",
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "\"Mereka meminta fatwa kepadamu tentang kalalah. Katakanlah: Allah memberi fatwa kepadamu: Jika seseorang meninggal tanpa anak dan mempunyai seorang saudara perempuan, maka bagiannya adalah separuh dari harta yang ditinggalkan. Dan saudaranya yang laki-laki mewarisi seluruh hartanya jika dia tidak mempunyai anak. Tetapi jika saudara perempuan itu dua orang, bagi mereka dua pertiga. Dan jika mereka sekumpulan saudara laki-laki dan perempuan, maka bagian seorang saudara laki-laki sebanyak bagian dua orang saudara perempuan...\"",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4. Hadits Shahih Bukhari & Muslim tentang Ashabah
                            Text("4. As-Sunnah: Kaidah Pembagian Ashabah", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "أَلْحِقُوا الْفَرَائِضَ بِأَهْلِهَا فَمَا بَقِيَ فَهُوَ لِأَوْلَى رَجُلٍ ذَكَرٍ",
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "\"Bagikanlah bagian-bagian warisan yang ditentukan (fardh) kepada mereka yang berhak. Adapun sisanya, maka berikanlah kepada kerabat laki-laki yang paling dekat.\" (HR. Al-Bukhari No. 6732 & Muslim No. 1614)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "\"اجْعَلُوا الأَخَوَاتِ مَعَ البَنَاتِ عَصَبَةً\" (Jadikanlah saudara-saudara perempuan bersama anak-anak perempuan sebagai 'ashabah) — HR. Al-Bukhari No. 6742.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Input Harta Tirkah
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "1. Harta Tirkah & Kewajiban Jenazah",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = grossEstateText,
                            onValueChange = { grossEstateText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Total Harta Kotor / Aset Tirkah (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FilledTonalButton(
                            onClick = {
                                grossEstateText = uiState.totalAssets.toLong().toString()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gunakan Total Kas & Aset Saat Ini (${rupiahFormat.format(uiState.totalAssets)})", style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = funeralCostText,
                            onValueChange = { funeralCostText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Biaya Pemulasaran Jenazah (Tajhiz)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = debtsText,
                            onValueChange = { debtsText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Hutang Pewaris yang Wajib Dilunasi (Dain)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = wasiatText,
                            onValueChange = { wasiatText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Wasiat Pewaris (Maksimal 1/3 Harta Bersih)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Input Ahli Waris
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "2. Profil Pewaris & Ahli Waris",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Jenis Kelamin Pewaris (Al-Mayyit):", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = gender == DeceasedGender.PRIA,
                                onClick = { gender = DeceasedGender.PRIA },
                                label = { Text("Laki-laki (Suami / Ayah)") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = gender == DeceasedGender.WANITA,
                                onClick = { gender = DeceasedGender.WANITA },
                                label = { Text("Perempuan (Istri / Ibu)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pasangan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (gender == DeceasedGender.PRIA) "Ada Istri yang Ditinggalkan" else "Ada Suami yang Ditinggalkan",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(checked = hasSpouse, onCheckedChange = { hasSpouse = it })
                        }

                        // Orang Tua
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ayah Kandung Masih Hidup", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = hasFather, onCheckedChange = { hasFather = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ibu Kandung Masih Hidup", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = hasMother, onCheckedChange = { hasMother = it })
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Keturunan (Anak Kandung):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Counter Anak
                        CounterRow(
                            label = "Jumlah Anak Laki-Laki",
                            count = sonCount,
                            onIncrement = { sonCount++ },
                            onDecrement = { if (sonCount > 0) sonCount-- }
                        )

                        CounterRow(
                            label = "Jumlah Anak Perempuan",
                            count = daughterCount,
                            onIncrement = { daughterCount++ },
                            onDecrement = { if (daughterCount > 0) daughterCount-- }
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Kelompok Saudara (Ashabah)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Saudara Kandung (Ashabah):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val siblingStatusText = when {
                                sonCount > 0 -> "Terhijab Anak Laki-Laki"
                                hasFather -> "Terhijab Ayah"
                                else -> "Berhak (Ashabah)"
                            }
                            val isBlocked = sonCount > 0 || hasFather
                            Surface(
                                color = if (isBlocked) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = siblingStatusText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        CounterRow(
                            label = "Saudara Laki-Laki Kandung (Ashabah)",
                            count = brotherCount,
                            onIncrement = { brotherCount++ },
                            onDecrement = { if (brotherCount > 0) brotherCount-- }
                        )

                        CounterRow(
                            label = "Saudara Perempuan Kandung (Ashabah)",
                            count = sisterCount,
                            onIncrement = { sisterCount++ },
                            onDecrement = { if (sisterCount > 0) sisterCount-- }
                        )
                    }
                }
            }

            // Explanation Notes if any
            if (calculationResult.explanationNotes.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Catatan Kaidah Hukum Fiqih", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            calculationResult.explanationNotes.forEach { note ->
                                Text("• $note", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
                }
            }

            // Calculation Results
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "3. Ringkasan Harta Bersih Siap Bagi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Harta Bersih Warisan:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                rupiahFormat.format(calculationResult.netDistributableEstate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Ahli Waris Portions
            items(calculationResult.shares) { share ->
                FaraidhShareCard(share = share, rupiahFormat = rupiahFormat)
            }
        }
    }
}

@Composable
fun CounterRow(
    label: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
            }
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            FilledTonalIconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FaraidhShareCard(
    share: FaraidhShare,
    rupiahFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    share.heirGroup,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = share.portionFractionText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Total Bagian Kelompok:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        rupiahFormat.format(share.totalNominal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (share.heirCount > 1) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Per Orang (${share.heirCount} Ahli Waris):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            rupiahFormat.format(share.perPersonNominal),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                share.dalilSyariah,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

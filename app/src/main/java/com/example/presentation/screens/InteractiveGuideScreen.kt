package com.example.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

data class GuideTopic(
    val id: String,
    val title: String,
    val shortSubtitle: String,
    val icon: ImageVector,
    val badge: String,
    val actionButtonText: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveGuideScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToHaulNisab: () -> Unit,
    onNavigateToSedekahSubuh: () -> Unit,
    onNavigateToVaultHistory: () -> Unit
) {
    val topics = remember {
        listOf(
            GuideTopic(
                id = "topic_double_entry",
                title = "1. Akuntansi Syariah Double-Entry",
                shortSubtitle = "Neraca seimbang & pemisahan akun",
                icon = Icons.Default.AccountBalance,
                badge = "Fundamental",
                actionButtonText = "Buka Dashboard Utama"
            ),
            GuideTopic(
                id = "topic_infaq_kasab",
                title = "2. Penyucian Harta (Infaq Kasab)",
                shortSubtitle = "Alokasi rezeki halal otomatis",
                icon = Icons.Default.Savings,
                badge = "Simulator Interaktif",
                actionButtonText = "Coba Catat Pemasukan"
            ),
            GuideTopic(
                id = "topic_roundup",
                title = "3. Round-Up Micro Infaq Belanja",
                shortSubtitle = "Sedekah receh pembulatan transaksi",
                icon = Icons.Default.ShoppingBag,
                badge = "Simulator Interaktif",
                actionButtonText = "Coba Catat Pengeluaran"
            ),
            GuideTopic(
                id = "topic_vault",
                title = "4. Virtual Infaq Vault & 8 Asnaf",
                shortSubtitle = "Titipan hak mustahiq & invoice",
                icon = Icons.Default.VolunteerActivism,
                badge = "Amanah",
                actionButtonText = "Lihat Riwayat Vault"
            ),
            GuideTopic(
                id = "topic_sedekah_subuh",
                title = "5. Sedekah Subuh & Doa Fajar",
                shortSubtitle = "Streak berkah & donasi kilat",
                icon = Icons.Default.WbSunny,
                badge = "Habit Tracker",
                actionButtonText = "Buka Sedekah Subuh"
            ),
            GuideTopic(
                id = "topic_budget",
                title = "6. Pemisahan Anggaran & AI Optimizer",
                shortSubtitle = "Kaidah 50/30/20 & cegah israf",
                icon = Icons.Default.PieChart,
                badge = "Optimizer",
                actionButtonText = "Kelola Anggaran Pagu"
            ),
            GuideTopic(
                id = "topic_haul_nisab",
                title = "7. Kalkulator Haul & Nisab Dinamis",
                shortSubtitle = "Zakat Mal 85g emas Hijriyah vs Masehi",
                icon = Icons.Default.MonetizationOn,
                badge = "Simulator Zakat",
                actionButtonText = "Cek Status Nisab"
            ),
            GuideTopic(
                id = "topic_modify_delete",
                title = "8. Modifikasi & Hapus Transaksi",
                shortSubtitle = "Kelola & perbaiki data aman",
                icon = Icons.Default.Edit,
                badge = "Fitur Baru",
                actionButtonText = "Buka Buku Besar"
            )
        )
    }

    var currentTopicIndex by remember { mutableIntStateOf(0) }
    val completedTopics = remember { mutableSetOf(0) }

    val currentTopic = topics[currentTopicIndex]
    val progressPercent = ((completedTopics.size.toFloat() / topics.size) * 100).toInt()

    fun markCurrentCompleted() {
        completedTopics.add(currentTopicIndex)
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1718)),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("guide_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Panduan Interaktif Pengguna",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Kuasai Seluruh Fitur Amanah Ledger",
                            fontSize = 11.sp,
                            color = EmeraldLight
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "$progressPercent% Selesai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
            // 1. Progress Bar
            LinearProgressIndicator(
                progress = { (currentTopicIndex + 1).toFloat() / topics.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = GoldAccent,
                trackColor = DarkBorder
            )

            // 2. Horizontal Topic Carousel Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0E1E20))
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(topics) { idx, topic ->
                    val isSelected = idx == currentTopicIndex
                    val isDone = completedTopics.contains(idx)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) EmeraldPrimary else Color(0xFF14272A),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) GoldAccent else if (isDone) EmeraldLight.copy(alpha = 0.5f) else DarkBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                currentTopicIndex = idx
                                completedTopics.add(idx)
                            }
                            .testTag("guide_topic_chip_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDone && !isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "${idx + 1}. ${topic.title.substringAfter(". ")}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.White70
                            )
                        }
                    }
                }
            }

            // 3. Main Content for Selected Topic
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentTopicIndex,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "guide_topic_transition"
                ) { targetIdx ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header Card
                        TopicHeaderCard(topic = topics[targetIdx], stepNumber = targetIdx + 1, totalSteps = topics.size)

                        // Interactive Content Body per topic
                        when (targetIdx) {
                            0 -> TopicDoubleEntryContent()
                            1 -> TopicInfaqKasabInteractiveContent(onTryAction = onNavigateToAddTransaction)
                            2 -> TopicRoundUpInteractiveContent(onTryAction = onNavigateToAddTransaction)
                            3 -> TopicVaultContent(onTryAction = onNavigateToVaultHistory)
                            4 -> TopicSedekahSubuhContent(onTryAction = onNavigateToSedekahSubuh)
                            5 -> TopicBudgetContent(onTryAction = onNavigateToBudget)
                            6 -> TopicHaulNisabInteractiveContent(onTryAction = onNavigateToHaulNisab)
                            7 -> TopicModifyDeleteContent(onTryAction = onNavigateBack)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // 4. Bottom Sticky Navigation Controls
            Surface(
                color = Color(0xFF0B1718),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentTopicIndex > 0) {
                        OutlinedButton(
                            onClick = { currentTopicIndex-- },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White70),
                            modifier = Modifier.testTag("guide_prev_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sebelumnya", fontSize = 12.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentTopicIndex < topics.size - 1) {
                        Button(
                            onClick = {
                                markCurrentCompleted()
                                currentTopicIndex++
                                markCurrentCompleted()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.testTag("guide_next_button")
                        ) {
                            Text("Lanjut ke Modul ${currentTopicIndex + 2}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                markCurrentCompleted()
                                onNavigateBack()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            modifier = Modifier.testTag("guide_finish_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Selesai & Buka Aplikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicHeaderCard(topic: GuideTopic, stepNumber: Int, totalSteps: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = topic.icon,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MODUL $stepNumber DARI $totalSteps",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldAccent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = topic.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = topic.title.substringAfter(". "),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = topic.shortSubtitle,
                    fontSize = 11.sp,
                    color = Color.White60
                )
            }
        }
    }
}

// ----------------------------------------------------
// TOPIC 1: AKUNTANSI SYARIAH DOUBLE-ENTRY
// ----------------------------------------------------
@Composable
fun TopicDoubleEntryContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF102022)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konsep Dasar Neraca Seimbang Syariah", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                }
                Text(
                    text = "Amanah Ledger menggunakan sistem pembukuan berpasangan (Double-Entry Bookkeeping) standar akuntansi internasional yang diselaraskan dengan syariat Islam:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 18.sp
                )

                // Visual Double-entry explanation box
                Surface(
                    color = Color(0xFF081314),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📐 Rumus Keseimbangan Neraca:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        Text("Aset (Kas/Bank/Emas) = Kewajiban (Vault Infaq/Hutang) + Modal Bersih", fontSize = 11.sp, color = Color.White)
                        androidx.compose.material3.HorizontalDivider(color = DarkBorder)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("1. Saat Pemasukan:", fontSize = 11.sp, color = Color.White70)
                            Text("Debit Kas (Aset +) | Kredit Rezeki (Pendapatan +)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("2. Saat Pengeluaran:", fontSize = 11.sp, color = Color.White70)
                            Text("Debit Beban (Pengeluaran +) | Kredit Kas (Aset -)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ExpenseCoral)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌟 Manfaat Utama untuk Anda:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                listOf(
                    "Pemisahan Ketat: Harta pribadi tidak akan pernah tercampur dengan dana titipan infaq/zakat.",
                    "Audit Kapan Saja: Setiap rupiah memiliki riwayat asal (kredit) dan penempatan (debit) yang transparan.",
                    "Bebas Selisih: Aplikasi menjamin jumlah ∑Debit selalu sama persis dengan ∑Kredit."
                ).forEach { point ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ", color = GoldAccent, fontWeight = FontWeight.Bold)
                        Text(point, fontSize = 11.sp, color = Color.White70, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TOPIC 2: PENYUCIAN HARTA (INFAQ KASAB) + SIMULATOR
// ----------------------------------------------------
@Composable
fun TopicInfaqKasabInteractiveContent(onTryAction: () -> Unit) {
    var testIncomeText by remember { mutableStateOf("10000000") }
    var testRate by remember { mutableDoubleStateOf(0.05) }

    val incomeVal = testIncomeText.toDoubleOrNull() ?: 0.0
    val infaqVal = incomeVal * testRate
    val netIncome = incomeVal - infaqVal

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Setiap rezeki yang masuk disucikan dengan menyisihkan sebagian nominal secara otomatis ke Virtual Vault.",
            fontSize = 12.sp,
            color = Color.White70,
            lineHeight = 18.sp
        )

        // Interactive Simulator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2426)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, EmeraldPrimary)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎮 Simulator Alokasi Infaq Interaktif", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text("Coba masukkan nominal gaji/rezeki dan geser persentase infaq:", fontSize = 11.sp, color = Color.White60)

                OutlinedTextField(
                    value = testIncomeText,
                    onValueChange = { testIncomeText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal Rezeki (Rp)", color = Color.White70) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.0 to "0%", 0.025 to "2.5%", 0.05 to "5%", 0.10 to "10%", 0.20 to "20%").forEach { (r, l) ->
                        val isSel = Math.abs(testRate - r) < 0.001
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) EmeraldPrimary else Color(0xFF14272A),
                            border = BorderStroke(1.dp, if (isSel) GoldAccent else DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { testRate = r }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(l, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = Color.White)
                            }
                        }
                    }
                }

                // Slider
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tingkat Infaq:", fontSize = 11.sp, color = Color.White60)
                    Text("${String.format(Locale.US, "%.1f", testRate * 100).removeSuffix(".0")}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                }
                Slider(
                    value = testRate.toFloat(),
                    onValueChange = { testRate = (Math.round(it * 200.0) / 200.0).coerceIn(0.0, 1.0) },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = EmeraldLight, activeTrackColor = EmeraldPrimary)
                )

                // Result Box
                Surface(
                    color = Color(0xFF081416),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dana Masuk Rekening (Bersih):", fontSize = 11.sp, color = Color.White70)
                            Text("Rp ${formatNumber(netIncome)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Otomatis ke Vault Amanah:", fontSize = 11.sp, color = Color.White70)
                            Text("Rp ${formatNumber(infaqVal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Coba Catat Pemasukan Nyata", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TOPIC 3: ROUND-UP MICRO INFAQ + SIMULATOR
// ----------------------------------------------------
@Composable
fun TopicRoundUpInteractiveContent(onTryAction: () -> Unit) {
    var expenseInput by remember { mutableStateOf("43200") }
    var roundStep by remember { mutableDoubleStateOf(5000.0) }

    val expVal = expenseInput.toDoubleOrNull() ?: 0.0
    val remainder = if (roundStep > 0) expVal % roundStep else 0.0
    val roundUpInfaq = if (remainder > 0) roundStep - remainder else 0.0
    val totalDebited = expVal + roundUpInfaq

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Ubah sisa belanja harian menjadi tabungan akhirat. Nominal ganjil dibulatkan ke kelipatan terdekat (misal: Rp 5.000 atau Rp 10.000), dan selisihnya otomatis disedekahkan!",
            fontSize = 12.sp,
            color = Color.White70,
            lineHeight = 18.sp
        )

        // Interactive Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1A24)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF7E57C2))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎮 Simulator Round-Up Interaktif", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedTextField(
                    value = expenseInput,
                    onValueChange = { expenseInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Contoh Belanja Belanjaan (Rp)", color = Color.White70) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF9575CD),
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pilih Kelipatan Pembulatan:", fontSize = 11.sp, color = Color.White60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1000.0 to "Rp 1.000", 2000.0 to "Rp 2.000", 5000.0 to "Rp 5.000", 10000.0 to "Rp 10.000").forEach { (step, lbl) ->
                        val isSel = roundStep == step
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF5E35B1) else Color(0xFF14131C),
                            border = BorderStroke(1.dp, if (isSel) GoldAccent else DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { roundStep = step }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(lbl, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = Color.White)
                            }
                        }
                    }
                }

                Surface(
                    color = Color(0xFF0E0D14),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bayar ke Merchant (Murni Belanja):", fontSize = 11.sp, color = Color.White70)
                            Text("Rp ${formatNumber(expVal)}", fontSize = 12.sp, color = ExpenseCoral)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sedekah Round-up ke Vault:", fontSize = 11.sp, color = Color.White70)
                            Text("+Rp ${formatNumber(roundUpInfaq)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                        androidx.compose.material3.HorizontalDivider(color = DarkBorder)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Terpotong dari Rekening:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Rp ${formatNumber(totalDebited)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Coba Catat Belanja dengan Round-Up", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TOPIC 4: VIRTUAL INFAQ VAULT & 8 ASNAF
// ----------------------------------------------------
@Composable
fun TopicVaultContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🏦 Virtual Vault = Titipan Hak Mustahiq", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                Text(
                    text = "Dana infaq yang terakumulasi di Vault adalah pos titipan (Liability) yang statusnya bukan lagi milik kita. Dana ini harus segera disalurkan kepada mereka yang berhak.",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 18.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF102022)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📋 Fitur Penyaluran Berita Acara & Invoice:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                listOf(
                    "Pilihan 8 Asnaf: Fakir, Miskin, Amil, Mu'allaf, Riqab, Gharimin, Fisabilillah, Ibnu Sabil, serta Yatim Dhuafa.",
                    "Nomor Resi Resmi: Otomatis menerbitkan kode kwitansi (e.g. INV-VAULT-2026-XXXX) untuk audit akuntabilitas.",
                    "Filter & Ekspor: Pantau riwayat penyaluran per asnaf dan program sosial."
                ).forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("✓ ", color = EmeraldLight, fontWeight = FontWeight.Bold)
                        Text(p, fontSize = 11.sp, color = Color.White70, lineHeight = 16.sp)
                    }
                }
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Riwayat Penyaluran Vault", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TOPIC 5: SEDEKAH SUBUH & STREAK
// ----------------------------------------------------
@Composable
fun TopicSedekahSubuhContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C14)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌅 Keutamaan Sedekah di Waktu Subuh", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                Text(
                    text = "«Tidak ada satu subuh pun melainkan ada dua malaikat yang turun, salah satunya berdoa: 'Ya Allah, berikanlah ganti bagi orang yang berinfak.'» (HR. Bukhari & Muslim)",
                    fontSize = 12.sp,
                    color = White80,
                    lineHeight = 18.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🔥 Fitur Pelacak Habit Sedekah Subuh:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                listOf(
                    "Streak Harian: Memotivasi istiqomah sedekah tanpa terputus setiap subuh.",
                    "Pemberian Kilat (+5rb & +10rb): Tombol cepat satu ketukan langsung dari layar utama.",
                    "Badge Berkah: Capai target 3 hari, 7 hari, 30 hari hingga Pejuang Subuh Istiqomah.",
                    "Doa Fajar Khusus: Dilengkapi kumpulan doa mustajab pembuka rezeki."
                ).forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ", color = GoldAccent, fontWeight = FontWeight.Bold)
                        Text(p, fontSize = 11.sp, color = Color.White70, lineHeight = 16.sp)
                    }
                }
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Modul Sedekah Subuh", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TOPIC 6: PEMISAHAN ANGGARAN & AI OPTIMIZER
// ----------------------------------------------------
@Composable
fun TopicBudgetContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📊 Kaidah 50/30/20 Syariah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                Text(
                    text = "Amanah Ledger membantu Anda membatasi pengeluaran konsumsi agar tidak berlebih-lebihan (Israf) melalui pembagian pagu pos anggaran:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 18.sp
                )
                listOf(
                    "50% Kebutuhan Pokok (Dharuriyyat): Pangan, Tempat Tinggal, Utilitas, Sekolah.",
                    "30% Keinginan Terukur (Hajiyyat): Transportasi, Rekreasi Halal, Servis.",
                    "20% Investasi & Tabungan Akhirat (Tahsiniyyat): Zakat, Infaq, Dana Darurat & Emas."
                ).forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ", color = EmeraldLight, fontWeight = FontWeight.Bold)
                        Text(p, fontSize = 11.sp, color = Color.White70, lineHeight = 16.sp)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF102220)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🤖 Fitur AI Budget Optimizer:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                Text(
                    text = "Aplikasi otomatis menganalisis pola transaksi historis Anda dan memberikan rekomendasi penyesuaian batas pagu yang dapat diterapkan hanya dalam satu kali klik!",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 18.sp
                )
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kelola & Optimasi Pagu Anggaran", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TOPIC 7: KALKULATOR HAUL & NISAB DINAMIS + SIMULATOR
// ----------------------------------------------------
@Composable
fun TopicHaulNisabInteractiveContent(onTryAction: () -> Unit) {
    var goldPriceText by remember { mutableStateOf("1350000") }
    var assetSimText by remember { mutableStateOf("120000000") }

    val goldPrice = goldPriceText.toDoubleOrNull() ?: 1350000.0
    val totalSimAsset = assetSimText.toDoubleOrNull() ?: 0.0
    val nisab85g = 85.0 * goldPrice
    val isNisabReached = totalSimAsset >= nisab85g
    val zakatLunar = if (isNisabReached) totalSimAsset * 0.025 else 0.0
    val zakatSolar = if (isNisabReached) totalSimAsset * 0.02577 else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Zakat Mal wajib dikeluarkan sebesar 2.5% apabila total harta simpanan telah mencapai batas Nisab (setara 85 gram emas murni) dan telah mengendap selama 1 tahun (Haul).",
            fontSize = 12.sp,
            color = Color.White70,
            lineHeight = 18.sp
        )

        // Interactive Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF221F10)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, GoldAccent)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎮 Simulator Nisab & Zakat Mal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = assetSimText,
                        onValueChange = { assetSimText = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Aset (Rp)", color = Color.White70, fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = goldPriceText,
                        onValueChange = { goldPriceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Harga Emas/g", color = Color.White70, fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = Color(0xFF141208),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Batas Nisab 85g Emas:", fontSize = 11.sp, color = Color.White70)
                            Text("Rp ${formatNumber(nisab85g)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status Harta:", fontSize = 11.sp, color = Color.White70)
                            Text(
                                text = if (isNisabReached) "✅ WAJIB ZAKAT MAL" else "❌ BELUM MENCAPAI NISAB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNisabReached) GoldAccent else Color.White60
                            )
                        }
                        if (isNisabReached) {
                            androidx.compose.material3.HorizontalDivider(color = DarkBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zakat Hijriyah (2.500% / 354 hari):", fontSize = 11.sp, color = Color.White70)
                                Text("Rp ${formatNumber(zakatLunar)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zakat Masehi (2.577% / 365 hari):", fontSize = 11.sp, color = Color.White70)
                                Text("Rp ${formatNumber(zakatSolar)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Audit Haul & Nisab Lengkap", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

// ----------------------------------------------------
// TOPIC 8: MODIFIKASI & HAPUS TRANSAKSI
// ----------------------------------------------------
@Composable
fun TopicModifyDeleteContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fleksibilitas Penuh: Ubah & Hapus Kapan Saja", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Jika Anda salah memasukkan nominal, tanggal, kategori, atau ingin menghapus transaksi yang dibatalkan, Amanah Ledger menyediakan fitur pengelolaan penuh:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 18.sp
                )

                // Step by step guide
                Surface(
                    color = Color(0xFF0A1416),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(EmeraldPrimary), contentAlignment = Alignment.Center) {
                                Text("1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Buka menu Buku Besar Terkini pada Dashboard.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(EmeraldPrimary), contentAlignment = Alignment.Center) {
                                Text("2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Ketuk kartu transaksi atau ikon Edit / Hapus.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(EmeraldPrimary), contentAlignment = Alignment.Center) {
                                Text("3", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sesuaikan nominal, alokasi infaq, lalu simpan perubahan.", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1414)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ExpenseCoral.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseCoral, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jaminan Keamanan Saldo & Neraca", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseCoral)
                }
                Text(
                    text = "Saat transaksi diubah atau dihapus, sistem otomatis memperbarui saldo rekening aset, posisi kewajiban vault infaq, dan batas pagu bulanan secara instan dan presisi.",
                    fontSize = 11.sp,
                    color = Color.White70,
                    lineHeight = 16.sp
                )
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kembali ke Dashboard Utama", fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatNumber(value: Double): String {
    return NumberFormat.getNumberInstance(Locale("id", "ID")).format(value.toLong())
}

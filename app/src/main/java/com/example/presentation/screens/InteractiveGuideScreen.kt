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
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.LaunchedEffect
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
    onNavigateToVaultHistory: () -> Unit,
    onNavigateToAmilDirectory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFaraidh: () -> Unit = {},
    onNavigateToQardh: () -> Unit = {},
    onNavigateToZakatHub: () -> Unit = {},
    onNavigateToMultiWallet: () -> Unit = {},
    onNavigateToRecurring: () -> Unit = {},
    onNavigateToIbadahGoals: () -> Unit = {},
    onNavigateToIslamicGrounding: () -> Unit = {},
    onNavigateToExportReport: () -> Unit = {},
    onOpenDrawer: () -> Unit = {}
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
                badge = "Fitur",
                actionButtonText = "Buka Buku Besar"
            ),
            GuideTopic(
                id = "topic_amil_directory",
                title = "9. Direktori Lembaga Amil (CRUD)",
                shortSubtitle = "Kelola rekening resmi ZISWAF & perizinan",
                icon = Icons.Default.VolunteerActivism,
                badge = "Fitur Baru",
                actionButtonText = "Buka Direktori Amil"
            ),
            GuideTopic(
                id = "topic_room_persistence",
                title = "10. Penyimpanan Lokal Room & Backup",
                shortSubtitle = "Jaminan data tersimpan permanen & offline",
                icon = Icons.Default.Storage,
                badge = "Privasi & Integritas",
                actionButtonText = "Cek Status Database"
            ),
            GuideTopic(
                id = "topic_faraidh",
                title = "11. Kalkulator Waris Islam (Faraidh)",
                shortSubtitle = "Hak ashabul furudh & ashabah QS An-Nisa",
                icon = Icons.Default.School,
                badge = "Fiqih Mawarith",
                actionButtonText = "Buka Kalkulator Waris"
            ),
            GuideTopic(
                id = "topic_qardh",
                title = "12. Akad Qardh Hasan (Bebas Riba)",
                shortSubtitle = "Pencatatan hutang piutang QS Al-Baqarah 282",
                icon = Icons.Default.Description,
                badge = "Anti Riba",
                actionButtonText = "Buka Qardh Hasan"
            ),
            GuideTopic(
                id = "topic_zakat_hub",
                title = "13. Zakat Hub & Penyaluran 8 Asnaf",
                shortSubtitle = "Hisab zakat maal/profesi & disburse amanah",
                icon = Icons.Default.VolunteerActivism,
                badge = "8 Asnaf",
                actionButtonText = "Buka Zakat Hub"
            ),
            GuideTopic(
                id = "topic_multi_wallet",
                title = "14. Multi-Wallet & Rekening Kas Syariah",
                shortSubtitle = "Pemisahan bank, e-wallet, kas tunai & brankas",
                icon = Icons.Default.AccountBalanceWallet,
                badge = "Aset Syariah",
                actionButtonText = "Kelola Multi-Wallet"
            ),
            GuideTopic(
                id = "topic_recurring",
                title = "15. Otomasi Transaksi Rutin (Recurring)",
                shortSubtitle = "Istiqamah nafkah, infaq & tagihan terjadwal",
                icon = Icons.Default.AutoMode,
                badge = "Istiqamah",
                actionButtonText = "Buka Transaksi Rutin"
            ),
            GuideTopic(
                id = "topic_ibadah_goals",
                title = "16. Tabungan Target Ibadah (Goals)",
                shortSubtitle = "Rencana tabungan Qurban, Umroh, Haji & Aqiqah",
                icon = Icons.Default.CardGiftcard,
                badge = "Ibadah Goals",
                actionButtonText = "Buka Target Ibadah"
            ),
            GuideTopic(
                id = "topic_islamic_grounding",
                title = "17. Ensiklopedia Fatwa & Rujukan DSN-MUI",
                shortSubtitle = "Pencarian fatwa muamalah & uji bebas riba",
                icon = Icons.Default.Verified,
                badge = "DSN-MUI",
                actionButtonText = "Buka Ensiklopedia Fatwa"
            ),
            GuideTopic(
                id = "topic_export_report",
                title = "18. Laporan Keuangan Syariah (PDF/CSV)",
                shortSubtitle = "Audit hisab neraca, laba rugi & unduh dokumen",
                icon = Icons.Default.PictureAsPdf,
                badge = "Transparansi & Hisab",
                actionButtonText = "Buka Ekspor Laporan"
            )
        )
    }

    val state by viewModel.uiState.collectAsState()
    var currentTopicIndex by remember { mutableIntStateOf(viewModel.activeGuideTopicIndex.coerceIn(0, topics.size - 1)) }
    val completedTopics = state.completedGuides

    LaunchedEffect(currentTopicIndex) {
        viewModel.setGuideTopic(currentTopicIndex, topics[currentTopicIndex].title)
    }

    val currentTopic = topics[currentTopicIndex]
    val progressPercent = ((completedTopics.size.toFloat() / topics.size) * 100).toInt()

    fun markCurrentCompleted() {
        viewModel.markGuideTopicCompleted(topics[currentTopicIndex].id)
    }

    val openModule: (Int, () -> Unit) -> Unit = { idx, action ->
        viewModel.activateGuideMode(idx, topics[idx].title)
        viewModel.markGuideTopicCompleted(topics[idx].id)
        action()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.deactivateGuideMode()
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("guide_back_button")
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
                        Text(
                            text = "Panduan Interaktif Pengguna",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kuasai Seluruh Fitur Amanah Ledger",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "$progressPercent% Selesai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("guide_menu_sidebar_button")
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
            // 1. Progress Bar
            LinearProgressIndicator(
                progress = { (currentTopicIndex + 1).toFloat() / topics.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // 2. Horizontal Topic Carousel Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(topics) { idx, topic ->
                    val isSelected = idx == currentTopicIndex
                    val isDone = state.completedGuides.contains(topic.id)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.secondary else if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .clickable {
                                currentTopicIndex = idx
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "${idx + 1}. ${topic.title.substringAfter(". ")}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
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
                            0 -> TopicDoubleEntryContent(onTryAction = { openModule(0, onNavigateBack) })
                            1 -> TopicInfaqKasabInteractiveContent(onTryAction = { openModule(1, onNavigateToAddTransaction) })
                            2 -> TopicRoundUpInteractiveContent(onTryAction = { openModule(2, onNavigateToAddTransaction) })
                            3 -> TopicVaultContent(onTryAction = { openModule(3, onNavigateToVaultHistory) })
                            4 -> TopicSedekahSubuhContent(onTryAction = { openModule(4, onNavigateToSedekahSubuh) })
                            5 -> TopicBudgetContent(onTryAction = { openModule(5, onNavigateToBudget) })
                            6 -> TopicHaulNisabInteractiveContent(
                                initialGoldPrice = state.goldPricePerGram,
                                onTryAction = { openModule(6, onNavigateToHaulNisab) }
                            )
                            7 -> TopicModifyDeleteContent(onTryAction = { openModule(7, onNavigateBack) })
                            8 -> TopicAmilDirectoryContent(onTryAction = { openModule(8, onNavigateToAmilDirectory) })
                            9 -> TopicRoomPersistenceContent(onTryAction = { openModule(9, onNavigateToSettings) })
                            10 -> TopicFaraidhContent(onTryAction = { openModule(10, onNavigateToFaraidh) })
                            11 -> TopicQardhContent(onTryAction = { openModule(11, onNavigateToQardh) })
                            12 -> TopicZakatHubContent(onTryAction = { openModule(12, onNavigateToZakatHub) })
                            13 -> TopicMultiWalletContent(onTryAction = { openModule(13, onNavigateToMultiWallet) })
                            14 -> TopicRecurringContent(onTryAction = { openModule(14, onNavigateToRecurring) })
                            15 -> TopicIbadahGoalsContent(onTryAction = { openModule(15, onNavigateToIbadahGoals) })
                            16 -> TopicIslamicGroundingContent(onTryAction = { openModule(16, onNavigateToIslamicGrounding) })
                            17 -> TopicExportReportContent(onTryAction = { openModule(17, onNavigateToExportReport) })
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // 4. Bottom Sticky Navigation Controls
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
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
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                                viewModel.deactivateGuideMode()
                                onNavigateBack()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.testTag("guide_finish_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Selesai & Buka Aplikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = topic.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
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
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = topic.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = topic.title.substringAfter(". "),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = topic.shortSubtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ----------------------------------------------------
// TOPIC 1: AKUNTANSI SYARIAH DOUBLE-ENTRY
// ----------------------------------------------------
@Composable
fun TopicDoubleEntryContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. Al-Baqarah: 282",
            arabicText = "يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا تَدَايَنتُم بِدَيْنٍ إِلَىٰ أَجَلٍ مُّسَمًّى فَاكْتُبُوهُ ۚ وَلْيَكْتُب بَّيْنَكُمْ كَاتِبٌ بِالْعَدْلِ ۚ وَلَا يَأْبَ كَاتِبٌ أَن يَكْتُبَ كَمَا عَلَّمَهُ اللَّهُ ۚ فَلْيَكْتُبْ وَلْيُمْلِلِ الَّذِي عَلَيْهِ الْحَقُّ وَلْيَتَّقِ اللَّهَ رَبَّهُ وَلَا يَبْخَسْ مِنْهُ شَيْئًا ۚ فَإِن كَانَ الَّذِي عَلَيْهِ الْحَقُّ سَفِيهًا أَوْ ضَعِيفًا أَوْ لَا يَسْتَطِيعُ أَن يُمِلَّ هُوَ فَلْيُمْلِلْ وَلِيُّهُ بِالْعَدْلِ ۚ وَاسْتَشْهِدُوا شَهِيدَيْنِ مِن رِّجَالِكُمْ ۖ فَإِن لَّمْ يَكُونَا رَجُلَيْنِ فَرَجُلٌ وَامْرَأَتَانِ مِمَّن تَرْضَوْنَ مِنَ الشُّهَدَاءِ أَن تَضِلَّ إِحْدَاهُمَا فَتُذَكِّرَ إِحْدَاهُمَا الْأُخْرَىٰ ۚ وَلَا يَأْبَ الشُّهَدَاءُ إِذَا مَا دُعُوا ۚ وَلَا تَسْأَمُوا أَن تَكْتُبُوهُ صَغِيرًا أَوْ كَبِيرًا إِلَىٰ أَجَلِهِ ۚ ذَٰلِكُمْ أَقْسَطُ عِندَ اللَّهِ وَأَقْوَمُ لِلشَّهَادَةِ وَأَدْنَىٰ أَلَّا تَرْتَابُوا ۖ إِلَّا أَن تَكُونَ تِجَارَةً حَاضِرَةً تُدِيرُونَهَا بَيْنَكُمْ فَلَيْسَ عَلَيْكُمْ جُنَاحٌ أَلَّا تَكْتُبُوهَا ۗ وَأَشْهِدُوا إِذَا تَبَايَعْتُمْ ۚ وَلَا يُضَارَّ كَاتِبٌ وَلَا شَهِيدٌ ۚ وَإِن تَفْعَلُوا فَإِنَّهُ فُسُوقٌ بِكُمْ ۗ وَاتَّقُوا اللَّهَ ۖ وَيُعَلِّمُكُمُ اللَّهُ ۗ وَاللَّهُ بِكُلِّ شَيْءٍ عَلِيمٌ",
            translation = "Wahai orang-orang yang beriman! Apabila kamu melakukan utang piutang untuk waktu yang ditentukan, hendaklah kamu menuliskannya. Dan hendaklah seorang penulis di antara kamu menuliskannya dengan benar. Janganlah penulis menolak untuk menuliskannya sebagaimana Allah telah mengajarkan kepadanya, maka hendaklah dia menuliskan. Dan hendaklah orang yang berutang itu mendiktekan, dan hendaklah dia bertakwa kepada Allah Tuhannya, dan janganlah dia mengurangi sedikit pun daripadanya. Jika yang berutang itu orang yang kurang akalnya atau lemah (keadaannya), atau tidak mampu mendiktekan sendiri, maka hendaklah walinya mendiktekan dengan benar. Dan persaksikanlah dengan dua orang saksi laki-laki di antara kamu. Jika tidak ada (dua orang laki-laki), maka (boleh) seorang laki-laki dan dua orang perempuan di antara orang-orang yang kamu sukai dari para saksi (yang ada), agar jika yang seorang lupa maka yang seorang lagi mengingatkannya. Dan janganlah saksi-saksi itu menolak apabila dipanggil. Dan janganlah kamu jemu menuliskannya, baik kecil maupun besar sampai batas waktu pembayarannya. Yang demikian itu lebih adil di sisi Allah, lebih dapat menguatkan kesaksian, dan lebih mendekatkan kamu kepada ketidakraguan, kecuali jika hal itu merupakan perdagangan tunai yang kamu jalankan di antara kamu, maka tidak ada dosa bagi kamu jika kamu tidak menuliskannya. Dan ambillah saksi apabila kamu berjual beli, dan janganlah penulis dipersulit dan jangan pula saksi. Jika kamu lakukan (yang demikian), maka sungguh, hal itu suatu kefasikan pada kamu. Dan bertakwalah kepada Allah, Allah memberikan pengajaran kepadamu, dan Allah Maha Mengetahui segala sesuatu.",
            fiqhNote = "Sistem pencatatan berpasangan (Double-Entry) menjamin akuntabilitas mutlak: setiap rupiah ada sumbernya (kredit) dan jelas penempatannya (debit), selaras dengan prinsip kejujuran (shiddiq) dan amanah."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konsep Dasar Neraca Seimbang Syariah", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "Amanah Ledger menggunakan sistem pembukuan berpasangan (Double-Entry Bookkeeping) standar akuntansi internasional yang diselaraskan dengan syariat Islam:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                // Visual Double-entry explanation box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📐 Rumus Keseimbangan Neraca:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text("Aset (Kas/Bank/Emas) = Kewajiban (Vault Infaq/Hutang) + Modal Bersih", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("1. Saat Pemasukan:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Debit Kas (Aset +) | Kredit Rezeki (Pendapatan +)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("2. Saat Pengeluaran:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Debit Beban (Pengeluaran +) | Kredit Kas (Aset -)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Prinsip Akuntansi Syariah dalam Praktik:",
            steps = listOf(
                "Setiap transaksi pemasukan diakui sebagai rezeki halal dan dicatat pada sisi Debit Kas dan Kredit Pendapatan.",
                "Infaq kasab otomatis memotong hak Allah/mustahiq ke akun kewajiban Virtual Vault.",
                "Setiap pengeluaran diverifikasi agar tidak mengandung unsur riba, gharar, atau maysir.",
                "Neraca saldo harian selalu dipastikan seimbang: Total Aset = Total Kewajiban + Ekuitas Bersih."
            )
        )

        IslamicQACard(
            question = "Mengapa akuntansi syariah memisahkan dana infaq sebagai kewajiban (liability)?",
            answer = "Karena begitu kita berniat dan menyisihkan dana untuk infaq/zakat, dana tersebut bukan lagi milik pribadi kita, melainkan hak fakir miskin (mustahiq) yang dititipkan kepada kita sampai tersalurkan.",
            reference = "Standar Akuntansi Syariah PSAK 109 & Fiqih Zakat Yusuf Al-Qardhawi"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Neraca Keuangan Dashboard", fontWeight = FontWeight.Bold)
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
        IslamicDalilCard(
            source = "QS. Al-Baqarah: 267",
            arabicText = "يَا أَيُّهَا الَّذِينَ آمَنُوا أَنفِقُوا مِن طَيِّبَاتِ مَا كَسَبْتُمْ وَمِمَّا أَخْرَجْنَا لَكُم مِّنَ الْأَرْضِ ۖ وَلَا تَيَمَّمُوا الْخَبِيثَ مِنْهُ تُنفِقُونَ وَلَسْتُم بِآخِذِيهِ إِلَّا أَن تُغْمِضُوا فِيهِ ۚ وَاعْلَمُوا أَنَّ اللَّهَ غَنِيٌّ حَمِيدٌ",
            translation = "Wahai orang-orang yang beriman! Infakkanlah sebagian dari hasil usahamu yang baik-baik dan sebagian dari apa yang Kami keluarkan dari bumi untuk kamu. Janganlah kamu memilih yang buruk untuk kamu keluarkan, padahal kamu sendiri tidak mau mengambilnya melainkan dengan memicingkan mata (enggan) terhadapnya. Dan ketahuilah bahwa Allah Mahakaya, Maha Terpuji.",
            fiqhNote = "Infaq kasab adalah wujud syukur atas rezeki halal yang kita peroleh dengan menyisihkan sebagian nominal secara langsung saat penerimaan dana, menjaga harta tetap bersih dan penuh barokah."
        )

        Text(
            text = "Setiap rezeki yang masuk disucikan dengan menyisihkan sebagian nominal secara otomatis ke Virtual Vault.",
            fontSize = 12.sp,
            
            lineHeight = 18.sp
        )

        // Interactive Simulator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎮 Simulator Alokasi Infaq Interaktif", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Text("Coba masukkan nominal gaji/rezeki dan geser persentase infaq:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = testIncomeText,
                    onValueChange = { testIncomeText = it.filter { c -> c.isDigit() } },
                    label = { Text("Nominal Rezeki (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { testRate = r }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(l, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Slider
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tingkat Infaq:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format(Locale.US, "%.1f", testRate * 100).removeSuffix(".0")}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = testRate.toFloat(),
                    onValueChange = { testRate = (Math.round(it * 200.0) / 200.0).coerceIn(0.0, 1.0) },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                )

                // Result Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Dana Masuk Rekening (Bersih):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatNumber(netIncome)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Otomatis ke Vault Amanah:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatNumber(infaqVal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Praktis Infaq Kasab:",
            steps = listOf(
                "Pilih menu 'Catat Pemasukan' di layar utama.",
                "Masukkan nominal rezeki atau gaji yang diterima.",
                "Tentukan persentase infaq penyucian (misal 2.5%, 5%, atau 10%).",
                "Aplikasi otomatis menghitung dana bersih yang masuk ke rekening kas dan dana yang dialokasikan ke Virtual Vault.",
                "Simpan transaksi, dan saldo amanah infaq Anda langsung bertambah secara otomatis."
            )
        )

        IslamicQACard(
            question = "Apakah infaq kasab menggantikan kewajiban zakat profesi?",
            answer = "Infaq kasab adalah sedekah sunnah untuk membersihkan rezeki harian. Jika total penghasilan setahun Anda mencapai nisab (85 gram emas), Anda tetap memiliki kewajiban zakat profesi sebesar 2.5%. Namun infaq yang telah disalurkan dengan niat zakat dapat dihitung sebagai pemenuhan zakat.",
            reference = "Fatwa MUI No. 3 Tahun 2003 tentang Zakat Penghasilan"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
        IslamicDalilCard(
            source = "HR. Bukhari (No. 1417) & Muslim (No. 1016)",
            arabicText = "مَا مِنْكُمْ مِنْ أَحَدٍ إِلَّا سَيُكَلِّمُهُ رَبُّهُ، لَيْسَ بَيْنَهُ وَبَيْنَهُ تُرْجُمَانٌ، فَيَنْظُرُ أَيْمَنَ مِنْهُ فَلَا يَرَى إِلَّا مَا قَدَّمَ مِنْ عَمَلِهِ، وَيَنْظُرُ أَشْأَمَ مِنْهُ فَلَا يَرَى إِلَّا مَا قَدَّمَ، وَيَنْظُرُ بَيْنَ يَدَيْهِ فَلَا يَرَى إِلَّا النَّارَ تِلْقَاءَ وَجْهِهِ، فَاتَّقُوا النَّارَ وَلَوْ بِشِقِّ تَمْرَةٍ، فَمَنْ لَمْ يَجِدْ فَبِكَلِمَةٍ طَيِّبَةٍ",
            translation = "Tidak ada seorang pun di antara kalian melainkan Tuhannya akan berbicara langsung kepadanya tanpa ada perantara penerjemah antara dirinya dan Allah. Lalu dia melihat ke arah kanannya, dan tidak ada yang dilihatnya kecuali amal yang telah dia perbuat. Dia melihat ke arah kirinya, dan tidak ada yang dilihatnya kecuali amal yang telah dia perbuat. Dan dia melihat ke arah depannya, maka tidak ada yang dilihatnya melainkan neraka di hadapan wajahnya. Maka jagalah diri kalian dari api neraka walaupun hanya dengan menyedekahkan separuh butir kurma. Dan barangsiapa tidak mendapatinya, maka dengan tutur kata yang baik.",
            fiqhNote = "Sedekah receh melalui pembulatan transaksi belanja membiasakan jiwa kita untuk senantiasa dermawan dalam setiap aktivitas konsumsi harian."
        )

        Text(
            text = "Ubah sisa belanja harian menjadi tabungan akhirat. Nominal ganjil dibulatkan ke kelipatan terdekat (misal: Rp 5.000 atau Rp 10.000), dan selisihnya otomatis disedekahkan!",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        // Interactive Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF7E57C2))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎮 Simulator Round-Up Interaktif", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedTextField(
                    value = expenseInput,
                    onValueChange = { expenseInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Contoh Belanja Belanjaan (Rp)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = Color(0xFF9575CD),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedLabelColor = Color(0xFF9575CD),
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pilih Kelipatan Pembulatan:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1000.0 to "Rp 1.000", 2000.0 to "Rp 2.000", 5000.0 to "Rp 5.000", 10000.0 to "Rp 10.000").forEach { (step, lbl) ->
                        val isSel = roundStep == step
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF5E35B1) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { roundStep = step }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(lbl, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Bayar ke Merchant (Murni Belanja):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatNumber(expVal)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sedekah Round-up ke Vault:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+Rp ${formatNumber(roundUpInfaq)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Terpotong dari Rekening:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Rp ${formatNumber(totalDebited)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Praktis Round-Up Infaq:",
            steps = listOf(
                "Pilih menu 'Catat Pengeluaran' saat berbelanja keperluan harian.",
                "Ketik nominal belanja (misal: Rp 43.200).",
                "Aktifkan opsi 'Round-Up Infaq' dan tentukan kelipatan (Rp 1.000, Rp 5.000, atau Rp 10.000).",
                "Sistem otomatis membulatkan pembayaran (menjadi Rp 45.000 atau Rp 50.000) dan mengalirkan selisihnya ke Virtual Vault.",
                "Belanja kebutuhan terpenuhi sekaligus menabung pahala jariyah di sisi Allah SWT."
            )
        )

        IslamicQACard(
            question = "Apakah pembulatan belanja ini termasuk riba atau pemborosan?",
            answer = "Tidak. Karena pembulatan dilakukan atas keridhaan penuh dan akad tabarru' (donasi sukarela), di mana dana selisih tidak diambil oleh penjual melainkan dialokasikan ke pos titipan sedekah untuk mustahiq.",
            reference = "Kaidah Fiqih Muamalah: Al-Ashlu fil 'uquud ar-ridha"
        )

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
        IslamicDalilCard(
            source = "QS. At-Taubah: 60",
            arabicText = "إِنَّمَا الصَّدَقَاتُ لِلْفُقَرَاءِ وَالْمَسَاكِينِ وَالْعَامِلِينَ عَلَيْهَا وَالْمُؤَلَّفَةِ قُلُوبُهُمْ وَفِي الرِّقَابِ وَالْغَارِمِينَ وَفِي سَبِيلِ اللَّهِ وَابْنِ السَّبِيلِ ۖ فَرِيضَةً مِّنَ اللَّهِ ۗ وَاللَّهُ عَلِيمٌ حَكِيمٌ",
            translation = "Sesungguhnya zakat itu hanyalah untuk orang-orang fakir, orang miskin, amil zakat, orang yang dilunakkan hatinya (mualaf), untuk (memerdekakan) hamba sahaya, untuk (membebaskan) orang yang berutang, untuk jalan Allah dan untuk orang yang sedang dalam perjalanan, sebagai kewajiban dari Allah. Dan Allah Maha Mengetahui, Maha Bijaksana.",
            fiqhNote = "Virtual Vault menampung amanah dana umat dan memastikan penyalurannya strictly terarah kepada asnaf yang telah ditetapkan syariat secara akuntabel."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🏦 Virtual Vault = Titipan Hak Mustahiq", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "Dana infaq yang terakumulasi di Vault adalah pos titipan (Liability) yang statusnya bukan lagi milik kita. Dana ini harus segera disalurkan kepada mereka yang berhak.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📋 Fitur Penyaluran Berita Acara & Invoice:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                listOf(
                    "Pilihan 8 Asnaf: Fakir, Miskin, Amil, Mu'allaf, Riqab, Gharimin, Fisabilillah, Ibnu Sabil, serta Yatim Dhuafa.",
                    "Nomor Resi Resmi: Otomatis menerbitkan kode kwitansi (e.g. INV-VAULT-2026-XXXX) untuk audit akuntabilitas.",
                    "Filter & Ekspor: Pantau riwayat penyaluran per asnaf dan program sosial."
                ).forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("✓ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(p, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Praktis Penyaluran Dana Vault:",
            steps = listOf(
                "Buka menu Virtual Vault untuk melihat akumulasi saldo titipan infaq.",
                "Klik 'Salurkan Dana' untuk memulai proses distribusi amanah.",
                "Pilih asnaf sasaran (misal: Fakir Miskin, Anak Yatim, atau Fisabilillah) dan lembaga amil penyalur.",
                "Masukkan nominal yang akan dicairkan dan lampirkan catatan berita acara.",
                "Sistem menerbitkan invoice resmi dan mendebit saldo kewajiban Vault secara akuntabel."
            )
        )

        IslamicQACard(
            question = "Bolehkah memakai dana Virtual Vault untuk keperluan pribadi mendesak sementara waktu?",
            answer = "Tidak boleh. Dana di Virtual Vault berstatus amanah titipan (wadiah yad amanah). Menggunakannya untuk kepentingan pribadi tanpa izin syar'i termasuk perbuatan khianat atas hak mustahiq.",
            reference = "Kaidah Amanah & QS. Al-Anfal: 27"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌅 Keutamaan Sedekah di Waktu Subuh", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "«Tidak ada satu subuh pun melainkan ada dua malaikat yang turun, salah satunya berdoa: 'Ya Allah, berikanlah ganti bagi orang yang berinfak.'» (HR. Bukhari & Muslim)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🔥 Fitur Pelacak Habit Sedekah Subuh:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                listOf(
                    "Streak Harian: Memotivasi istiqomah sedekah tanpa terputus setiap subuh.",
                    "Pemberian Kilat (+5rb & +10rb): Tombol cepat satu ketukan langsung dari layar utama.",
                    "Badge Berkah: Capai target 3 hari, 7 hari, 30 hari hingga Pejuang Subuh Istiqomah.",
                    "Doa Fajar Khusus: Dilengkapi kumpulan doa mustajab pembuka rezeki."
                ).forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Text(p, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Praktis Istiqamah Sedekah Subuh:",
            steps = listOf(
                "Buka aplikasi saat fajar setelah sholat subuh.",
                "Tekan tombol kilat '+Rp 5.000' atau '+Rp 10.000' pada modul Sedekah Subuh.",
                "Baca doa fajar pembuka berkah yang tampil di layar.",
                "Streak harian Anda akan bertambah, memperkuat kebiasaan istiqomah.",
                "Dana otomatis masuk ke Virtual Vault dan siap disalurkan ke asnaf dhuafa."
            )
        )

        IslamicQACard(
            question = "Mengapa sedekah subuh memiliki keistimewaan doa malaikat?",
            answer = "Setiap subuh dua malaikat turun mendoakan orang yang berinfak agar diganti dengan kebaikan berlipat ganda, sedangkan orang yang menahan hartanya didoakan agar hartanya binasa. Istiqomah setiap hari mengundang keberkahan hidup dan keselamatan.",
            reference = "HR. Bukhari No. 1442 dan Muslim No. 1010"
        )

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
        IslamicDalilCard(
            source = "QS. Al-Isra: 26-27",
            arabicText = "وَآتِ ذَا الْقُرْبَىٰ حَقَّهُ وَالْمِسْكِينَ وَابْنَ السَّبِيلِ وَلَا تُبَذِّرْ تَبْذِيرًا ۝ إِنَّ الْمُبَذِّرِينَ كَانُوا إِخْوَانَ الشَّيَاطِينِ ۖ وَكَانَ الشَّيْطَانُ لِرَبِّهِ كَفُورًا",
            translation = "Dan berikanlah haknya kepada kerabat dekat, juga kepada orang miskin dan orang yang dalam perjalanan; dan janganlah kamu menghambur-hamburkan (hartamu) secara boros. Sesungguhnya orang-orang yang pemboros itu adalah saudara-saudara setan dan setan itu sangat ingkar kepada Tuhannya.",
            fiqhNote = "Mengatur anggaran adalah kewajiban syar'i untuk mencegah perilaku konsumtif yang berlebihan dan menjamin nafkah keluarga tetap terpenuhi secara seimbang."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📊 Kaidah 50/30/20 Syariah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Amanah Ledger membantu Anda membatasi pengeluaran konsumsi agar tidak berlebih-lebihan (Israf) melalui pembagian pagu pos anggaran:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                listOf(
                    "50% Kebutuhan Pokok (Dharuriyyat): Pangan, Tempat Tinggal, Utilitas, Sekolah.",
                    "30% Keinginan Terukur (Hajiyyat): Transportasi, Rekreasi Halal, Servis.",
                    "20% Investasi & Tabungan Akhirat (Tahsiniyyat): Zakat, Infaq, Dana Darurat & Emas."
                ).forEach { p ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(p, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🤖 Fitur AI Budget Optimizer:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "Aplikasi otomatis menganalisis pola transaksi historis Anda dan memberikan rekomendasi penyesuaian batas pagu yang dapat diterapkan hanya dalam satu kali klik!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Praktis Mengatur Pagu Anggaran:",
            steps = listOf(
                "Buka menu Pagu Anggaran dari tombol di bawah.",
                "Tinjau pembagian pagu untuk Kebutuhan Pokok (Dharuriyyat), Keinginan (Hajiyyat), dan Tabungan Ibadah (Tahsiniyyat).",
                "Gunakan 'AI Budget Optimizer' untuk mendapatkan rekomendasi batas belanja ideal berdasarkan riwayat mutasi Anda.",
                "Aktifkan notifikasi peringatan jika pengeluaran mendekati 80% dari batas pagu bulanan.",
                "Evaluasi realisasi anggaran setiap akhir bulan untuk menjaga kedisiplinan finansial."
            )
        )

        IslamicQACard(
            question = "Bagaimana fiqih membedakan antara kebutuhan (hajah) dan kemewahan (israf)?",
            answer = "Imam Asy-Syathibi membagi tingkatan kebutuhan menjadi Dharuriyyat (mutlak untuk kelangsungan agama, jiwa, akal, keturunan, harta), Hajiyyat (memudahkan kesulitan), dan Tahsiniyyat (pelengkap dan etika). Segala pengeluaran di luar ketiga hal ini yang sia-sia dikategorikan sebagai Israf (berlebih-lebihan).",
            reference = "Al-Muwafaqat fi Ushulisy Syari'ah karya Imam Asy-Syathibi"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
fun TopicHaulNisabInteractiveContent(
    initialGoldPrice: Double = 1350000.0,
    onTryAction: () -> Unit
) {
    var goldPriceText by remember(initialGoldPrice) { mutableStateOf(initialGoldPrice.toLong().toString()) }
    var assetSimText by remember { mutableStateOf("120000000") }

    val goldPrice = goldPriceText.toDoubleOrNull() ?: initialGoldPrice
    val totalSimAsset = assetSimText.toDoubleOrNull() ?: 0.0
    val nisab85g = 85.0 * goldPrice
    val isNisabReached = totalSimAsset >= nisab85g
    val zakatLunar = if (isNisabReached) totalSimAsset * 0.025 else 0.0
    val zakatSolar = if (isNisabReached) totalSimAsset * 0.02577 else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "HR. Abu Dawud & Standar Haul & Nisab",
            arabicText = "لَيْسَ فِي مَالٍ زَكَاةٌ حَتَّى يَحُولَ عَلَيْهِ الْحَوْلُ",
            translation = "Tidak ada kewajiban zakat pada suatu harta sampai genap berputar satu tahun (haul).",
            fiqhNote = "Kewajiban zakat mal berlaku mutlak ketika harta simpanan telah mencapai nisab (setara 85 gram emas) dan bertahan di atas batas tersebut selama satu tahun penuh."
        )

        Text(
            text = "Zakat Mal wajib dikeluarkan sebesar 2.5% apabila total harta simpanan telah mencapai batas Nisab (setara 85 gram emas murni) dan telah mengendap selama 1 tahun (Haul).",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )

        // Interactive Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🎮 Simulator Nisab & Zakat Mal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = assetSimText,
                        onValueChange = { assetSimText = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Aset (Rp)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = goldPriceText,
                        onValueChange = { goldPriceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Harga Emas/g", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedLabelColor = MaterialTheme.colorScheme.secondary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Batas Nisab 85g Emas:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${formatNumber(nisab85g)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status Harta:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (isNisabReached) "✅ WAJIB ZAKAT MAL" else "❌ BELUM MENCAPAI NISAB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNisabReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        if (isNisabReached) {
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zakat Hijriyah (2.500% / 354 hari):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatNumber(zakatLunar)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Zakat Masehi (2.577% / 365 hari):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatNumber(zakatSolar)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Praktis Audit Haul & Nisab:",
            steps = listOf(
                "Masukkan total aset lancar simpanan Anda (tabungan, deposito syariah, emas batangan).",
                "Periksa harga emas terbaru per gram (default otomatis diperbarui).",
                "Jika total aset ≥ nilai 85 gram emas, mulailah mencatat tanggal dimulainya Haul.",
                "Setelah genap 1 tahun Hijriyah (354 hari) atau Masehi (365 hari), tunaikan zakat 2.5% atau 2.577%.",
                "Tekan tombol 'Salurkan ke Zakat Hub' untuk menunaikan kewajiban melalui amil terpercaya."
            )
        )

        IslamicQACard(
            question = "Mengapa tarif zakat kalender Masehi adalah 2.577% bukan 2.5%?",
            answer = "Tarif standar syariat 2.5% berlaku untuk 1 tahun Hijriyah (354 hari). Satu tahun Masehi berjumlah 365 hari (lebih panjang 11 hari). Berdasarkan standar AAOIFI No. 35 dan Fatwa Ulama, jika menggunakan tahun Masehi maka zakatnya adalah 2.5% × (365 / 354) = 2.577% agar hak fakir miskin tidak terkurangi.",
            reference = "Standar Syariah AAOIFI No. 35 tentang Zakat"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
        ) {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buka Audit Haul & Nisab Lengkap", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary, fontSize = 14.sp)
        }
    }
}

// ----------------------------------------------------
// TOPIC 8: MODIFIKASI & HAPUS TRANSAKSI
// ----------------------------------------------------
@Composable
fun TopicModifyDeleteContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "Kaidah Fiqih Koreksi & Pertanggungjawaban",
            arabicText = "الرُّجُوعُ إِلَى الْحَقِّ خَيْرٌ مِنَ التَّمَادِي فِي الْبَاطِلِ",
            translation = "Kembali kepada kebenaran itu jauh lebih baik daripada terus menerus di dalam kekeliruan.",
            fiqhNote = "Mengoreksi kesalahan pencatatan transaksi sesegera mungkin adalah wujud ketelitian (itqan) dan kejujuran dalam menjaga kebenaran laporan keuangan."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fleksibilitas Penuh: Ubah & Hapus Kapan Saja", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = "Jika Anda salah memasukkan nominal, tanggal, kategori, atau ingin menghapus transaksi yang dibatalkan, Amanah Ledger menyediakan fitur pengelolaan penuh tanpa merusak integritas neraca:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Praktis Mengubah atau Menghapus Transaksi:",
            steps = listOf(
                "Buka riwayat mutasi pada Buku Besar di Beranda Utama.",
                "Cari transaksi yang keliru atau ingin dibatalkan.",
                "Klik ikon Pensil untuk mengedit nominal, tanggal, atau kategori; atau ikon Tempat Sampah untuk menghapus.",
                "Sistem secara otomatis menyesuaikan kembali saldo rekening kas dan posisi kewajiban Virtual Vault.",
                "Neraca keuangan kembali akurat dan seimbang secara real-time."
            )
        )

        IslamicQACard(
            question = "Jika saya membatalkan transaksi yang sudah terkena potongan infaq kasab, apa yang terjadi?",
            answer = "Sistem akuntansi Amanah Ledger secara cerdas akan membatalkan kedua sisi jurnal: saldo kas dikembalikan dan alokasi infaq di Virtual Vault disesuaikan, sehingga tidak timbul selisih pembukuan.",
            reference = "Prinsip Reversing Entries dalam Akuntansi Syariah"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jaminan Keamanan Saldo & Neraca", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
                Text(
                    text = "Saat transaksi diubah atau dihapus, sistem otomatis memperbarui saldo rekening aset, posisi kewajiban vault infaq, dan batas pagu bulanan secara instan dan presisi.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kembali ke Dashboard Utama", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TOPIC 9: DIREKTORI AMIL & REKENING ZISWAF (CRUD)
// ----------------------------------------------------
@Composable
fun TopicAmilDirectoryContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. At-Taubah: 103",
            arabicText = "خُذْ مِنْ أَمْوَالِهِمْ صَدَقَةً تُطَهِّرُهُمْ وَتُزَكِّيهِم بِهَا وَصَلِّ عَلَيْهِمْ ۖ إِنَّ صَلَاتَكَ سَكَنٌ لَّهُمْ ۗ وَاللَّهُ سَمِيعٌ عَلِيمٌ",
            translation = "Ambillah zakat dari sebagian harta mereka, dengan zakat itu kamu membersihkan dan menyucikan mereka dan berdoalah untuk mereka. Sesungguhnya doa kamu itu (menumbuhkan) ketenteraman jiwa bagi mereka. Dan Allah Maha Mendengar, Maha Mengetahui.",
            fiqhNote = "Menyalurkan zakat melalui amil yang amanah dan resmi (BAZNAS/LAZ) lebih utama karena memastikan pendistribusian tepat sasaran sesuai skala prioritas syariah."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kelola Rekening & Lembaga Resmi (CRUD)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = "Amanah Ledger menyediakan direktori lembaga amil zakat resmi terakreditasi Kemenag RI (BAZNAS, LAZ Nasional, Lembaga Wakaf). Anda memiliki kendali penuh untuk memperbarui data:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Pengelolaan Direktori Lembaga Amil:",
            steps = listOf(
                "Buka menu Direktori Amil ZISWAF.",
                "Gunakan tombol '+' untuk menambahkan lembaga baru (misal BAZNAS Kota atau DKM Masjid Anda).",
                "Klik ikon Edit untuk memperbarui nama bank, nomor rekening, atau kontak amil.",
                "Ketuk tombol Salin untuk meng-copy nomor rekening ke clipboard saat hendak transfer donasi.",
                "Simpan bukti transfer pada berita acara penyaluran Virtual Vault."
            )
        )

        IslamicQACard(
            question = "Apa kelebihan menyalurkan zakat via Amil dibanding langsung ke fakir miskin?",
            answer = "Amil zakat memiliki data mustahiq yang komprehensif, mampu memberdayakan fakir miskin secara produktif (tidak hanya konsumtif), serta menjaga kemuliaan (izzah) penerima zakat agar tidak merasa rendah diri.",
            reference = "Fiqih Sunnah Sayyid Sabiq jilid 1 & Fatwa MUI No. 4 Tahun 2003"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Direktori Lembaga Amil", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TOPIC 10: PENYIMPANAN LOKAL ROOM & SINKRONISASI
// ----------------------------------------------------
@Composable
fun TopicRoomPersistenceContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "Prinsip Hifzhul Mal (Menjaga Harta)",
            arabicText = "إِنَّ خَيْرَ مَنِ اسْتَأْجَرْتَ الْقَوِيُّ الْأَمِينُ",
            translation = "...Sesungguhnya orang yang paling baik yang kamu ambil untuk bekerja ialah orang yang kuat lagi dapat dipercaya (amanah).",
            fiqhNote = "Menjaga keamanan catatan keuangan dan privasi data finansial keluarga adalah bagian integral dari maqashid syariah (hifzhul mal) agar terhindar dari kebocoran dan penyalahgunaan."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jaminan Data Tersimpan Permanen (Room SQLite)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = "Amanah Ledger dibangun dengan arsitektur Offline-First yang andal. Seluruh mutasi jurnal, kantong rekening, target ibadah, dan pengaturan langsung disimpan secara permanen di database lokal SQLite Room.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        IslamicStepByStepCard(
            title = "Fitur Keamanan & Penyimpanan Data:",
            steps = listOf(
                "Semua transaksi otomatis tersimpan ke memori internal perangkat saat tombol simpan diklik.",
                "Tidak ada data finansial Anda yang dikirim ke server pihak ketiga tanpa izin (100% aman dan privat).",
                "Aplikasi tetap beroperasi optimal tanpa kuota internet atau saat mode pesawat aktif.",
                "Gunakan fitur Ekspor Cadangan berkala di menu Pengaturan untuk mem-backup data ke file aman."
            )
        )

        IslamicQACard(
            question = "Apakah data keuangan saya aman jika berganti perangkat hp?",
            answer = "Aman. Anda dapat membuat file cadangan (backup export) dari menu Pengaturan, lalu memindahkannya ke perangkat baru dan memilih fitur Impor Cadangan untuk memulihkan seluruh mutasi keuangan tanpa ada yang hilang.",
            reference = "Praktik Manajemen Risiko & Perlindungan Aset Digital"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Pengaturan & Status Database", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

private fun formatNumber(value: Double): String {
    return NumberFormat.getNumberInstance(Locale("id", "ID")).format(value.toLong())
}

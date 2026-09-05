package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.infaq.SedekahSubuhState
import com.example.core.infaq.SedekahSubuhStreakEngine
import com.example.core.infaq.StreakBadge
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SedekahSubuhScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val subuhState = state.sedekahSubuhState

    var showCustomGiveDialog by remember { mutableStateOf(false) }
    var celebrationBadge by remember { mutableStateOf<StreakBadge?>(null) }

    val daysStrip = remember(subuhState.historyMap) {
        SedekahSubuhStreakEngine.getLast7DaysStrip(subuhState)
    }

    val isGivenToday = subuhState.isCompletedToday()
    val nextBadge = subuhState.getNextBadge()
    val daysToNextBadge = subuhState.getDaysUntilNextBadge()

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
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sedekah_subuh_back_button")
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
                            text = "Sedekah Subuh Streak",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Konsistensi Kebaikan di Awal Fajar",
                            fontSize = 11.sp,
                            color = GoldAccent
                        )
                    }
                },
                actions = {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${subuhState.currentStreak} Hari",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("sedekah_subuh_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Hero Streak Visualizer Card
            item {
                SubuhStreakHeroCard(
                    subuhState = subuhState,
                    isGivenToday = isGivenToday,
                    nextBadge = nextBadge,
                    daysToNext = daysToNextBadge
                )
            }

            // 2. 7-Day Interactive Weekly Strip
            item {
                SubuhWeeklyStripSection(daysStrip = daysStrip)
            }

            // 3. Quick Give Sedekah Subuh Action Panel
            item {
                QuickGiveSubuhPanel(
                    isGivenToday = isGivenToday,
                    onQuickGive = { amount ->
                        viewModel.recordSedekahSubuh(amount = amount)
                        val afterState = viewModel.uiState.value.sedekahSubuhState
                        val newlyUnlocked = afterState.badges.firstOrNull { it.isUnlocked && it.requiredDays == afterState.currentStreak }
                        if (newlyUnlocked != null) {
                            celebrationBadge = newlyUnlocked
                        }
                    },
                    onCustomGiveClick = { showCustomGiveDialog = true }
                )
            }

            // 4. Milestones & Badges Grid Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pencapaian & Lencana Istiqomah",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${subuhState.badges.count { it.isUnlocked }} / ${subuhState.badges.size} Terbuka",
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 5. Badges List
            items(subuhState.badges, key = { it.id }) { badge ->
                SubuhBadgeCard(badge = badge, currentStreak = subuhState.currentStreak)
            }

            // 6. Hadith Morning Sadaqah Wisdom Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Doa Malaikat di Waktu Subuh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "مَا مِنْ يَوْمٍ يُصْبِحُ الْعِبَادُ فِيهِ إِلاَّ مَلَكَانِ يَنْزِلاَنِ فَيَقُولُ أَحَدُهُمَا اللَّهُمَّ أَعْطِ مُنْفِقًا خَلَفًا",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"Tidak ada satu subuh pun yang dialami hamba-hamba Allah kecuali turun dua malaikat. Salah satu di antaranya berdoa: 'Ya Allah, berikanlah ganti bagi orang yang berinfak...'\" (HR. Bukhari no. 1442 & Muslim no. 1010)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Custom Amount Dialog
    if (showCustomGiveDialog) {
        CustomSedekahSubuhDialog(
            onDismiss = { showCustomGiveDialog = false },
            onConfirm = { amount, note ->
                viewModel.recordSedekahSubuh(amount = amount, note = note)
                showCustomGiveDialog = false
                val afterState = viewModel.uiState.value.sedekahSubuhState
                val newlyUnlocked = afterState.badges.firstOrNull { it.isUnlocked && it.requiredDays == afterState.currentStreak }
                if (newlyUnlocked != null) {
                    celebrationBadge = newlyUnlocked
                }
            }
        )
    }

    // Badge Unlock Celebration Dialog
    if (celebrationBadge != null) {
        val badge = celebrationBadge!!
        AlertDialog(
            onDismissRequest = { celebrationBadge = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text = badge.iconEmoji, fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lencana Terbuka!",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = badge.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Istiqomah ${badge.requiredDays} Hari Berturut-turut",
                            color = EmeraldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { celebrationBadge = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Alhamdulillah", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SubuhStreakHeroCard(
    subuhState: SedekahSubuhState,
    isGivenToday: Boolean,
    nextBadge: StreakBadge?,
    daysToNext: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E3A2B), Color(0xFF0F1E17), Color(0xFF081410))
                )
            )
            .border(1.dp, GoldAccent.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.2f))
                            .border(1.dp, GoldAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${subuhState.currentStreak} HARI STREAK",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isGivenToday) "Subuh Hari Ini Telah Ditunaikan ✨" else "Waktunya Menghidupkan Subuh Hari Ini",
                            fontSize = 11.sp,
                            color = if (isGivenToday) EmeraldLight else White70
                        )
                    }
                }

                Surface(
                    color = if (isGivenToday) EmeraldPrimary.copy(alpha = 0.25f) else GoldAccent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isGivenToday) EmeraldLight else GoldAccent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGivenToday) Icons.Default.CheckCircle else Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (isGivenToday) EmeraldLight else GoldAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGivenToday) "Tercatat" else "Belum",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGivenToday) EmeraldLight else GoldAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics row: Total Rp Sedekah, Total Hari, Rekor Tertinggi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF071512))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Sedekah", fontSize = 10.sp, color = White60)
                    Text("Rp ${formatRupiah(subuhState.totalContributions)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Hari", fontSize = 10.sp, color = White60)
                    Text("${subuhState.totalDaysGiven} Hari", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rekor Streak", fontSize = 10.sp, color = White60)
                    Text("${subuhState.longestStreak} Hari", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                }
            }

            if (nextBadge != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lencana Berikutnya: ${nextBadge.iconEmoji} ${nextBadge.title}",
                            fontSize = 11.sp,
                            color = White70
                        )
                        Text(
                            text = "$daysToNext hari lagi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = (subuhState.currentStreak.toFloat() / nextBadge.requiredDays.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldAccent,
                        trackColor = Color.Black.copy(alpha = 0.5f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun SubuhWeeklyStripSection(daysStrip: List<com.example.core.infaq.DayStripItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Kalender Istiqomah 7 Hari Terakhir",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysStrip.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.dayName,
                            fontSize = 10.sp,
                            color = if (item.isToday) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        item.isCompleted -> EmeraldPrimary
                                        item.isToday -> GoldAccent.copy(alpha = 0.25f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        item.isCompleted -> GoldAccent
                                        item.isToday -> GoldAccent
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = item.dayNumber,
                                    fontSize = 11.sp,
                                    color = if (item.isToday) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (item.isCompleted) {
                            Text(
                                text = "Rp ${(item.amount / 1000).toInt()}k",
                                fontSize = 8.sp,
                                color = EmeraldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (item.isToday) {
                            Text(
                                text = "Hari Ini",
                                fontSize = 8.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "-",
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickGiveSubuhPanel(
    isGivenToday: Boolean,
    onQuickGive: (Double) -> Unit,
    onCustomGiveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isGivenToday) MaterialTheme.colorScheme.outline else EmeraldPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sedekah Subuh Kilat",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (isGivenToday) {
                    Surface(
                        color = EmeraldPrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "+ Tambah Lagi",
                            fontSize = 10.sp,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick preset pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(2000.0 to "Rp 2rb", 5000.0 to "Rp 5rb", 10000.0 to "Rp 10rb", 20000.0 to "Rp 20rb")
                presets.forEach { (amt, label) ->
                    Button(
                        onClick = { onQuickGive(amt) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 38.dp)
                            .testTag("quick_subuh_${amt.toInt()}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F2620),
                            contentColor = EmeraldLight
                        ),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCustomGiveClick,
                modifier = Modifier.fillMaxWidth().testTag("custom_subuh_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nominal Bebas & Catatan Doa Khusus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SubuhBadgeCard(badge: StreakBadge, currentStreak: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("badge_card_${badge.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (badge.isUnlocked) GoldAccent.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    .border(
                        1.dp,
                        if (badge.isUnlocked) GoldAccent else MaterialTheme.colorScheme.outline,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (badge.isUnlocked) {
                    Text(badge.iconEmoji, fontSize = 22.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badge.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (badge.isUnlocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badge.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = if (badge.isUnlocked) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(
                    1.dp,
                    if (badge.isUnlocked) EmeraldLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = if (badge.isUnlocked) "Terbuka" else "${badge.requiredDays} Hari",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun CustomSedekahSubuhDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("10000") }
    var noteText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Sedekah Subuh Khusus",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Nominal Sedekah (Rp)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    prefix = { Text("Rp ", color = GoldAccent, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_subuh_amount_input")
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Hajat Doa / Catatan Keberkahan", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    placeholder = { Text("Contoh: Doa kelancaran rezeki & kesembuhan orang tua", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    singleLine = false,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_subuh_note_input")
                )

                if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = Color(0xFFEF5350), fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt <= 0) {
                        errorMessage = "Masukkan nominal sedekah yang valid"
                    } else {
                        onConfirm(amt, noteText.ifBlank { "Sedekah Subuh Fajar" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Tunaikan Sedekah", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

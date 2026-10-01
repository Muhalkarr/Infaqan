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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.core.ibadah.GoalCategory
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import com.example.core.ui.AmanahHapticEngine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.EmeraldDark
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IbadahGoalsScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var depositingGoal by remember { mutableStateOf<IbadahGoal?>(null) }
    var editingGoal by remember { mutableStateOf<IbadahGoal?>(null) }
    var completedGoalCelebration by remember { mutableStateOf<IbadahGoal?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<GoalCategory?>(null) }
    var selectedStatusFilter by remember { mutableStateOf(0) } // 0: Semua, 1: Berjalan, 2: Tercapai

    val totalTarget = state.ibadahGoals.sumOf { it.targetAmount }
    val totalAccumulated = state.ibadahGoals.sumOf { it.currentAccumulated }
    val overallPercent = if (totalTarget > 0) ((totalAccumulated / totalTarget) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Target Finansial dan Ibadah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Ibadah, Dana Darurat, Pendidikan, Papan, Usaha & Pribadi",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("ibadah_screen_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("ibadah_screen_menu_sidebar_button")
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_ibadah_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target Finansial & Ibadah")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Header Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL AKUMULASI TARGET FINANSIAL & IBADAH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (state.securityConfig.isMaskBalance) "Rp ••••••••" else "Rp ${nf.format(totalAccumulated)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$overallPercent% Terkumpul",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (totalAccumulated / totalTarget.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Total: Rp ${nf.format(totalTarget)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${state.ibadahGoals.count { it.isCompleted }} dari ${state.ibadahGoals.size} Tercapai",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Daftar Target (${state.ibadahGoals.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Riwayat Setoran (${state.ibadahDeposits.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTabIndex == 0) {
                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("Semua (${state.ibadahGoals.size})") }
                    )
                    GoalCategory.entries.forEach { cat ->
                        val count = state.ibadahGoals.count { it.category == cat }
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = { Text("${cat.displayName} ($count)") }
                        )
                    }
                }

                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == 0,
                        onClick = { selectedStatusFilter = 0 },
                        label = { Text("Semua Status") }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == 1,
                        onClick = { selectedStatusFilter = 1 },
                        label = { Text("Berjalan (${state.ibadahGoals.count { !it.isCompleted }})") }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == 2,
                        onClick = { selectedStatusFilter = 2 },
                        label = { Text("Tercapai (${state.ibadahGoals.count { it.isCompleted }})") }
                    )
                }

                val filteredGoals = state.ibadahGoals.filter { goal ->
                    val matchCat = selectedCategoryFilter == null || goal.category == selectedCategoryFilter
                    val matchStatus = when (selectedStatusFilter) {
                        1 -> !goal.isCompleted
                        2 -> goal.isCompleted
                        else -> true
                    }
                    matchCat && matchStatus
                }

                // Goals List Tab
                if (filteredGoals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Savings,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (state.ibadahGoals.isEmpty()) "Belum Ada Target Finansial & Ibadah" else "Tidak Ada Target yang Cocok",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (state.ibadahGoals.isEmpty())
                                    "Mulai rencanakan tabungan ibadah (Qurban/Haji/Umrah), dana darurat, pendidikan, rumah, kendaraan, hingga modal usaha."
                                else
                                    "Tidak ada target pada kategori atau status yang dipilih. Anda dapat mereset filter atau membuat target baru.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (state.ibadahGoals.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        selectedCategoryFilter = null
                                        selectedStatusFilter = 0
                                    }
                                ) {
                                    Text("Reset Filter")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Button(
                                onClick = { showAddGoalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Buat Target Baru")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredGoals, key = { it.id }) { goal ->
                            IbadahGoalCard(
                                goal = goal,
                                isMasked = state.securityConfig.isMaskBalance,
                                onDeposit = { depositingGoal = goal },
                                onEdit = { editingGoal = goal },
                                onDelete = { viewModel.deleteIbadahGoal(goal.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            } else {
                // Deposits History Tab
                if (state.ibadahDeposits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada riwayat setoran tabungan ibadah.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.ibadahDeposits, key = { it.id }) { deposit ->
                            val goal = state.ibadahGoals.firstOrNull { it.id == deposit.goalId }
                            val wallet = state.getWallet(deposit.sourceWalletId)
                            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = goal?.title ?: "Tabungan Ibadah",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${sdf.format(deposit.depositDate)} • Sumber: ${wallet?.name ?: "Kas"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (deposit.notes.isNotBlank()) {
                                            Text(
                                                text = deposit.notes,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Text(
                                        text = "+ Rp ${nf.format(deposit.amount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Goal Dialog
    if (showAddGoalDialog || editingGoal != null) {
        IbadahGoalFormDialog(
            initialGoal = editingGoal,
            wallets = state.wallets,
            onDismiss = {
                showAddGoalDialog = false
                editingGoal = null
            },
            onSave = { goal ->
                if (editingGoal != null) {
                    viewModel.updateIbadahGoal(goal)
                } else {
                    viewModel.addIbadahGoal(goal)
                }
                showAddGoalDialog = false
                editingGoal = null
            }
        )
    }

    // Deposit to Goal Dialog
    depositingGoal?.let { goal ->
        IbadahDepositDialog(
            goal = goal,
            wallets = state.wallets,
            onDismiss = { depositingGoal = null },
            onDeposit = { amount, walletId, note, receipt ->
                val isCompleting = (goal.currentAccumulated + amount) >= goal.targetAmount
                if (isCompleting) {
                    AmanahHapticEngine.performGoalCompletionCelebration(haptic, context)
                    completedGoalCelebration = goal.copy(currentAccumulated = (goal.currentAccumulated + amount).coerceAtMost(goal.targetAmount))
                } else {
                    AmanahHapticEngine.performButtonClick(haptic, context)
                }

                viewModel.depositToIbadahGoal(
                    goalId = goal.id,
                    amount = amount,
                    sourceWalletId = walletId,
                    notes = note,
                    receipt = receipt
                )
                depositingGoal = null
            }
        )
    }

    // Celebratory Dialog on Goal Completion
    completedGoalCelebration?.let { goal ->
        AlertDialog(
            onDismissRequest = { completedGoalCelebration = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = null,
                    tint = EmeraldDark,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Alhamdulillah! Target Tercapai!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = EmeraldDark
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selamat! Tabungan untuk \"${goal.title}\" telah genap mencapai target 100% sebesar Rp ${nf.format(goal.targetAmount)}.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Semoga Allah SWT memberkahi harta Anda dan mempermudah niat suci ibadah ini.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        completedGoalCelebration = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                ) {
                    Text("Masya Allah, Alhamdulillah")
                }
            }
        )
    }
}

@Composable
private fun IbadahGoalCard(
    goal: IbadahGoal,
    isMasked: Boolean,
    onDeposit: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val progress = (goal.currentAccumulated / goal.targetAmount.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val percent = (progress * 100).toInt()

    val (icon, badgeColor, categoryLabel) = when (goal.category) {
        GoalCategory.IBADAH -> when (goal.type) {
            IbadahGoalType.QURBAN_KAMBING, IbadahGoalType.QURBAN_SAPI_PATUNGAN, IbadahGoalType.QURBAN_SAPI_1_EKOR ->
                Triple(Icons.Default.Pets, EmeraldDark, "Ibadah • Qurban")
            IbadahGoalType.SETORAN_AWAL_HAJI ->
                Triple(Icons.Default.Mosque, EmeraldDark, "Ibadah • Haji")
            IbadahGoalType.PAKET_UMRAH_MUKMIN ->
                Triple(Icons.Default.FlightTakeoff, EmeraldDark, "Ibadah • Umrah")
            IbadahGoalType.WAKAF_PRODUKTIF ->
                Triple(Icons.Default.VolunteerActivism, EmeraldDark, "Ibadah • Wakaf")
            else ->
                Triple(Icons.Default.Savings, EmeraldDark, "Ibadah • Spiritual")
        }
        GoalCategory.DANA_DARURAT -> Triple(Icons.Default.Shield, Color(0xFFD97706), "Dana Darurat")
        GoalCategory.PENDIDIKAN -> Triple(Icons.Default.School, Color(0xFF4F46E5), "Pendidikan")
        GoalCategory.RUMAH -> Triple(Icons.Default.Home, Color(0xFF0891B2), "Rumah & Papan")
        GoalCategory.KENDARAAN -> Triple(Icons.Default.DirectionsCar, Color(0xFF2563EB), "Kendaraan")
        GoalCategory.BISNIS -> Triple(Icons.Default.Storefront, Color(0xFF7C3AED), "Bisnis & Usaha")
        GoalCategory.PRIBADI -> Triple(Icons.Default.Favorite, Color(0xFFE11D48), "Rencana Pribadi")
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ibadah_goal_card_${goal.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Surface(
                            color = badgeColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = categoryLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = goal.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Target: ${goal.targetHijriYearMonth}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) MaterialTheme.colorScheme.primary else badgeColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Terkumpul $percent%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (goal.isCompleted) MaterialTheme.colorScheme.primary else badgeColor
                    )
                    Text(
                        text = if (isMasked) "Rp •••••••• / Rp ••••••••" else "Rp ${nf.format(goal.currentAccumulated)} / Rp ${nf.format(goal.targetAmount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onDeposit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (goal.isCompleted) MaterialTheme.colorScheme.primary else badgeColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("goal_deposit_btn_${goal.id}")
                ) {
                    Icon(
                        if (goal.isCompleted) Icons.Default.CheckCircle else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (goal.isCompleted) "Selesai" else "+ Setor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (goal.targetMonthsRemaining > 0 && !goal.isCompleted) {
                val remainingAmount = (goal.targetAmount - goal.currentAccumulated).coerceAtLeast(0.0)
                val monthlyPace = remainingAmount / goal.targetMonthsRemaining
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Perlu nabung ~Rp ${nf.format(monthlyPace)} / bulan (sisa ${goal.targetMonthsRemaining} bulan)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IbadahGoalFormDialog(
    initialGoal: IbadahGoal?,
    wallets: List<com.example.core.wallet.WalletAccount>,
    onDismiss: () -> Unit,
    onSave: (IbadahGoal) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(initialGoal?.category ?: GoalCategory.IBADAH) }
    var selectedType by remember { mutableStateOf(initialGoal?.type ?: IbadahGoalType.QURBAN_KAMBING) }
    var title by remember { mutableStateOf(initialGoal?.title ?: selectedType.title) }
    var targetAmountText by remember { mutableStateOf(initialGoal?.targetAmount?.toLong()?.toString() ?: selectedType.defaultEstimate.toLong().toString()) }
    var initialAccumulatedText by remember { mutableStateOf(initialGoal?.currentAccumulated?.toLong()?.toString() ?: "0") }
    var targetHijriYearMonth by remember { mutableStateOf(initialGoal?.targetHijriYearMonth ?: "10 Dzulhijjah 1448 H") }
    var monthsRemainingText by remember { mutableStateOf(initialGoal?.targetMonthsRemaining?.toString() ?: "10") }
    var linkedWalletId by remember { mutableStateOf(initialGoal?.linkedWalletId ?: wallets.firstOrNull()?.id ?: "acc_bank") }
    var notes by remember { mutableStateOf(initialGoal?.notes ?: "") }
    var isManuallyCompleted by remember { mutableStateOf(initialGoal?.isCompleted ?: false) }

    var typeExpanded by remember { mutableStateOf(false) }
    var walletExpanded by remember { mutableStateOf(false) }

    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialGoal != null) "Edit Target Finansial & Ibadah" else "Tambah Target Finansial & Ibadah Baru", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Selection
                Column {
                    Text("Kategori Rencana:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GoalCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    val availableTypes = IbadahGoalType.entries.filter { it.category == cat }
                                    if (selectedType.category != cat && availableTypes.isNotEmpty()) {
                                        val firstType = availableTypes.first()
                                        selectedType = firstType
                                        title = firstType.title
                                        targetAmountText = firstType.defaultEstimate.toLong().toString()
                                    }
                                },
                                label = { Text(cat.displayName) }
                            )
                        }
                    }
                }

                // Type Dropdown (Filtered by selected category)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Template / Jenis Rencana") },
                        trailingIcon = {
                            IconButton(onClick = { typeExpanded = !typeExpanded }) {
                                Icon(
                                    imageVector = if (typeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        val availableTypes = IbadahGoalType.entries.filter { it.category == selectedCategory }
                        availableTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(type.title, fontWeight = FontWeight.Medium)
                                        Text(
                                            "Estimasi: Rp ${nf.format(type.defaultEstimate.toLong())}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    title = type.title
                                    targetAmountText = type.defaultEstimate.toLong().toString()
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Rencana Target") },
                    placeholder = { Text("cth: Qurban Kambing, DP Rumah, Dana Darurat") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Linked Wallet Dropdown
                val selectedWallet = wallets.firstOrNull { it.id == linkedWalletId }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedWallet?.name ?: "Pilih Kantong / Rekening Simpanan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kantong / Rekening Terhubung") },
                        trailingIcon = {
                            IconButton(onClick = { walletExpanded = !walletExpanded }) {
                                Icon(
                                    imageVector = if (walletExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = walletExpanded,
                        onDismissRequest = { walletExpanded = false }
                    ) {
                        wallets.forEach { w ->
                            DropdownMenuItem(
                                text = { Text("${w.name} (${w.institutionName})") },
                                onClick = {
                                    linkedWalletId = w.id
                                    walletExpanded = false
                                }
                            )
                        }
                    }
                }

                // Target Amount and Presets
                Column {
                    OutlinedTextField(
                        value = targetAmountText,
                        onValueChange = { targetAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Target Biaya (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Pilihan Cepat Target Biaya:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(1000000L to "1 Jt", 3500000L to "3.5 Jt", 5000000L to "5 Jt", 25000000L to "25 Jt", 35000000L to "35 Jt").forEach { (amt, lbl) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { targetAmountText = amt.toString() }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                    Text(lbl, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = initialAccumulatedText,
                    onValueChange = { initialAccumulatedText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Sudah Terkumpul Saat Ini (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Months Remaining and Presets
                Column {
                    OutlinedTextField(
                        value = monthsRemainingText,
                        onValueChange = { monthsRemainingText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Estimasi Bulan Tersisa Menabung") },
                        placeholder = { Text("cth: 12") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(3, 6, 10, 12, 24, 36).forEach { m ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { monthsRemainingText = m.toString() }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                    Text("${m}bln", fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Live calculation preview
                val curTarget = targetAmountText.toDoubleOrNull() ?: 0.0
                val curAccum = initialAccumulatedText.toDoubleOrNull() ?: 0.0
                val curMonths = (monthsRemainingText.toIntOrNull() ?: 1).coerceAtLeast(1)
                val curRemaining = (curTarget - curAccum).coerceAtLeast(0.0)
                val monthlyReco = curRemaining / curMonths

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "💡 Simulasi Tabungan Bulanan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sisa kebutuhan: Rp ${nf.format(curRemaining.toLong())} dalam $curMonths bulan",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rekomendasi Setor: Rp ${nf.format(monthlyReco.toLong())} / bulan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = targetHijriYearMonth,
                    onValueChange = { targetHijriYearMonth = it },
                    label = { Text("Target Waktu Pencapaian") },
                    placeholder = { Text("cth: 10 Dzulhijjah 1448 H / Des 2026") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Rencana / Hajat") },
                    placeholder = { Text("cth: Pembelian tunai bebas riba / biaya uang pangkal") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Status Selesai / Tercapai", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Tandai jika target sudah ditunaikan", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isManuallyCompleted,
                        onCheckedChange = { isManuallyCompleted = it }
                    )
                }
            }
        },
        confirmButton = {
            val targetAmount = targetAmountText.toDoubleOrNull() ?: 0.0
            val accumulated = initialAccumulatedText.toDoubleOrNull() ?: 0.0
            val months = monthsRemainingText.toIntOrNull() ?: 1

            Button(
                onClick = {
                    if (title.isNotBlank() && targetAmount > 0.0) {
                        val goal = IbadahGoal(
                            id = initialGoal?.id ?: "goal_${UUID.randomUUID().toString().take(8)}",
                            type = selectedType,
                            category = selectedCategory,
                            title = title,
                            targetAmount = targetAmount,
                            currentAccumulated = accumulated,
                            targetHijriYearMonth = targetHijriYearMonth,
                            targetMonthsRemaining = months,
                            linkedWalletId = linkedWalletId,
                            notes = notes,
                            isCompleted = isManuallyCompleted || accumulated >= targetAmount
                        )
                        onSave(goal)
                    }
                },
                enabled = title.isNotBlank() && targetAmount > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan Target")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IbadahDepositDialog(
    goal: IbadahGoal,
    wallets: List<com.example.core.wallet.WalletAccount>,
    onDismiss: () -> Unit,
    onDeposit: (amount: Double, sourceWalletId: String, note: String, receipt: ReceiptAttachment?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var note by remember { mutableStateOf("") }
    var walletExpanded by remember { mutableStateOf(false) }

    var attachReceipt by remember { mutableStateOf(false) }
    var refNumber by remember { mutableStateOf("") }

    val remaining = (goal.targetAmount - goal.currentAccumulated).coerceAtLeast(0.0)
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Setor Tabungan: ${goal.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kekurangan target: Rp ${nf.format(remaining)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Setoran (Rp)") },
                    placeholder = { Text("cth: 500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (wallets.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentWallet = wallets.firstOrNull { it.id == selectedWalletId }
                        OutlinedTextField(
                            value = currentWallet?.name ?: "Pilih Kantong Sumber",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kantong Sumber Dana") },
                            trailingIcon = {
                                IconButton(onClick = { walletExpanded = !walletExpanded }) {
                                    Icon(
                                        imageVector = if (walletExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = walletExpanded,
                            onDismissRequest = { walletExpanded = false }
                        ) {
                            wallets.forEach { w ->
                                DropdownMenuItem(
                                    text = { Text("${w.name} (${w.institutionName})") },
                                    onClick = {
                                        selectedWalletId = w.id
                                        walletExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Setoran (Opsional)") },
                    placeholder = { Text("cth: Tabungan rutin awal bulan") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lampirkan Bukti Setoran", fontSize = 13.sp)
                    androidx.compose.material3.Switch(
                        checked = attachReceipt,
                        onCheckedChange = { attachReceipt = it }
                    )
                }

                if (attachReceipt) {
                    OutlinedTextField(
                        value = refNumber,
                        onValueChange = { refNumber = it },
                        label = { Text("No. Resi / Validasi Bank") },
                        placeholder = { Text("cth: SETOR-BSI-9921") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (amount > 0.0) {
                        val receipt = if (attachReceipt) {
                            ReceiptAttachment(
                                title = "Setoran Tabungan ${goal.title}",
                                receiptType = ReceiptType.BANK_TRANSFER_PROOF,
                                referenceNumber = refNumber.ifBlank { "DEP-${System.currentTimeMillis() % 100000}" },
                                amount = amount,
                                notes = note
                            )
                        } else null

                        onDeposit(amount, selectedWalletId, note, receipt)
                    }
                },
                enabled = amount > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Konfirmasi Setor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

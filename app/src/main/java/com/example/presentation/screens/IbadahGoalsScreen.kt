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
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
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
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var depositingGoal by remember { mutableStateOf<IbadahGoal?>(null) }
    var editingGoal by remember { mutableStateOf<IbadahGoal?>(null) }

    val totalTarget = state.ibadahGoals.sumOf { it.targetAmount }
    val totalAccumulated = state.ibadahGoals.sumOf { it.currentAccumulated }
    val overallPercent = if (totalTarget > 0) ((totalAccumulated / totalTarget) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Perencana Target Ibadah",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Tabungan Qurban, Haji BPIH, Umrah & Waqaf",
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
                containerColor = GoldAccent,
                contentColor = Color.Black,
                modifier = Modifier.testTag("add_ibadah_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target Ibadah")
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
                                text = "TOTAL AKUMULASI DANA IBADAH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (state.securityConfig.isMaskBalance) "Rp ••••••••" else "Rp ${nf.format(totalAccumulated)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                        }
                        Surface(
                            color = GoldAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$overallPercent% Terkumpul",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
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
                        color = GoldAccent,
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
                            color = EmeraldLight
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
                    text = { Text("Program Aktif (${state.ibadahGoals.count { !it.isCompleted }})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Riwayat Setoran (${state.ibadahDeposits.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTabIndex == 0) {
                // Goals List Tab
                if (state.ibadahGoals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Mosque,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = GoldAccent.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Target Ibadah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Mulai rencanakan tabungan Qurban, porsi Haji, atau Umrah keluarga dengan istiqomah.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 6.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddGoalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("Buat Target Ibadah")
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
                        items(state.ibadahGoals, key = { it.id }) { goal ->
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
                                        color = EmeraldLight
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

    val icon = when (goal.type) {
        IbadahGoalType.QURBAN_KAMBING, IbadahGoalType.QURBAN_SAPI_PATUNGAN, IbadahGoalType.QURBAN_SAPI_1_EKOR -> Icons.Default.Pets
        IbadahGoalType.SETORAN_AWAL_HAJI -> Icons.Default.Mosque
        IbadahGoalType.PAKET_UMRAH_MUKMIN -> Icons.Default.FlightTakeoff
        IbadahGoalType.WAKAF_PRODUKTIF -> Icons.Default.VolunteerActivism
        IbadahGoalType.CUSTOM_IBADAH -> Icons.Default.Savings
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldAccent.copy(alpha = 0.18f))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
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
                color = if (goal.isCompleted) EmeraldLight else GoldAccent,
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
                        color = if (goal.isCompleted) EmeraldLight else GoldAccent
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
                        containerColor = if (goal.isCompleted) EmeraldPrimary else GoldAccent,
                        contentColor = if (goal.isCompleted) Color.White else Color.Black
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
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var selectedType by remember { mutableStateOf(initialGoal?.type ?: IbadahGoalType.QURBAN_KAMBING) }
    var targetAmountText by remember { mutableStateOf(initialGoal?.targetAmount?.toLong()?.toString() ?: "3500000") }
    var initialAccumulatedText by remember { mutableStateOf(initialGoal?.currentAccumulated?.toLong()?.toString() ?: "0") }
    var targetHijriYearMonth by remember { mutableStateOf(initialGoal?.targetHijriYearMonth ?: "10 Dzulhijjah 1448 H") }
    var monthsRemainingText by remember { mutableStateOf(initialGoal?.targetMonthsRemaining?.toString() ?: "10") }
    var linkedWalletId by remember { mutableStateOf(initialGoal?.linkedWalletId ?: wallets.firstOrNull()?.id ?: "") }
    var notes by remember { mutableStateOf(initialGoal?.notes ?: "") }

    var typeExpanded by remember { mutableStateOf(false) }
    var walletExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialGoal != null) "Edit Target Ibadah" else "Tambah Target Ibadah Baru", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Rencana Ibadah") },
                    placeholder = { Text("cth: Qurban Kambing 1448 H, Setoran Haji") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Type Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedType.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jenis Ibadah") },
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
                        IbadahGoalType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.title) },
                                onClick = {
                                    selectedType = type
                                    if (title.isBlank() || title == selectedType.title) {
                                        title = type.title
                                    }
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target Biaya (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialAccumulatedText,
                    onValueChange = { initialAccumulatedText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Sudah Terkumpul Saat Ini (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetHijriYearMonth,
                    onValueChange = { targetHijriYearMonth = it },
                    label = { Text("Target Waktu Hijriah") },
                    placeholder = { Text("cth: 10 Dzulhijjah 1448 H / Syawal 1449 H") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = monthsRemainingText,
                    onValueChange = { monthsRemainingText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Estimasi Bulan Tersisa") },
                    placeholder = { Text("cth: 12") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Niat / Pelaksanaan") },
                    placeholder = { Text("cth: Disalurkan ke amil qurban pedalaman") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                            title = title,
                            targetAmount = targetAmount,
                            currentAccumulated = accumulated,
                            targetHijriYearMonth = targetHijriYearMonth,
                            targetMonthsRemaining = months,
                            linkedWalletId = linkedWalletId,
                            notes = notes,
                            isCompleted = accumulated >= targetAmount
                        )
                        onSave(goal)
                    }
                },
                enabled = title.isNotBlank() && targetAmount > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
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
                    color = GoldAccent,
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
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
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

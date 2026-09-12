package com.example.presentation.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.AccountCategory
import com.example.core.budget.BudgetAllocation
import com.example.core.budget.BudgetCapSuggestion
import com.example.core.budget.FinancialGoalMode
import com.example.core.budget.SpendingPatternAnalysis
import com.example.core.budget.SuggestionStatus
import com.example.core.state.AmanahLedgerUiState
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAllocationScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetAllocation?>(null) }
    var deletingBudget by remember { mutableStateOf<BudgetAllocation?>(null) }

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
                        modifier = Modifier.testTag("budget_back_button")
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
                            text = "Pemisahan Anggaran Bulanan",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Batas Maksimal Pengeluaran & Anti-Israf",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            editingBudget = null
                            showAddEditDialog = true
                        },
                        modifier = Modifier.testTag("add_budget_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Anggaran",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("budget_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingBudget = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_budget_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Anggaran")
            }
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

            // 1. Total Monthly Budget Overview Card
            item {
                BudgetSummaryCard(state = state)
            }

            // 2. Spending Pattern Analysis & Smart Budget Caps Optimizer
            item {
                SpendingPatternOptimizerSection(
                    analysis = state.spendingPatternAnalysis,
                    selectedGoalMode = state.selectedGoalMode,
                    onSelectGoalMode = { viewModel.setFinancialGoalMode(it) },
                    onApplyAllSuggestions = { viewModel.applyBudgetCapSuggestions(it) },
                    onApplySingleSuggestion = { viewModel.applySingleBudgetCapSuggestion(it) }
                )
            }

            // 3. Overbudget Warning Banner (if any)
            if (state.overBudgetCount > 0) {
                item {
                    OverBudgetAlertBanner(overBudgetCount = state.overBudgetCount)
                }
            }

            // 4. Section Title & Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alokasi per Kategori Akun (${state.budgets.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Realisasi Bulan Ini",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4. Budget Items
            if (state.budgets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum ada alokasi anggaran",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tetapkan batas maksimal pengeluaran bulanan agar keuangan tetap berkah & terkendali.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            } else {
                items(state.budgets, key = { it.id }) { budget ->
                    val spent = state.getMonthlySpentForAccount(budget.accountId)
                    BudgetItemCard(
                        budget = budget,
                        spentAmount = spent,
                        onEdit = {
                            editingBudget = budget
                            showAddEditDialog = true
                        },
                        onDelete = {
                            deletingBudget = budget
                        }
                    )
                }
            }

            // 5. Islamic Guidance on Budgeting & Anti-Israf
            item {
                IslamicBudgetWisdomCard()
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Add / Edit Budget Dialog
    if (showAddEditDialog) {
        AddEditBudgetDialog(
            existingBudget = editingBudget,
            state = state,
            onDismiss = {
                showAddEditDialog = false
                editingBudget = null
            },
            onSave = { accountId, categoryName, limit, alertPercent, iconKey ->
                viewModel.setBudget(
                    accountId = accountId,
                    categoryName = categoryName,
                    monthlyLimit = limit,
                    alertThresholdPercent = alertPercent,
                    iconKey = iconKey
                )
                showAddEditDialog = false
                editingBudget = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingBudget != null) {
        val budgetToDelete = deletingBudget!!
        AlertDialog(
            onDismissRequest = { deletingBudget = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Hapus Alokasi Anggaran?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus anggaran untuk \"${budgetToDelete.categoryName}\"? Riwayat transaksi akun tidak akan terhapus.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBudget(budgetToDelete.id)
                        deletingBudget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingBudget = null }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun BudgetSummaryCard(state: AmanahLedgerUiState) {
    val totalLimit = state.totalMonthlyBudgetLimit
    val totalSpent = state.totalMonthlyBudgetSpent
    val usageRatio = if (totalLimit > 0.0) (totalSpent / totalLimit).coerceIn(0.0, 1.0) else 0.0
    val usagePercentage = if (totalLimit > 0.0) (totalSpent / totalLimit) * 100.0 else 0.0
    val remainingBudget = (totalLimit - totalSpent).coerceAtLeast(0.0)

    val progressColor = when {
        usagePercentage >= 100.0 -> MaterialTheme.colorScheme.error
        usagePercentage >= 80.0 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOTAL ANGGARAN BULAN INI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.8.sp
                    )
                }

                Surface(
                    color = progressColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, progressColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${String.format("%.1f", usagePercentage)}% Terpakai",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Realisasi Pengeluaran",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rp ${formatRupiah(totalSpent)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Plafon Anggaran",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rp ${formatRupiah(totalLimit)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { usageRatio.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sisa Kuota Belanja: Rp ${formatRupiah(remainingBudget)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (remainingBudget > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "${state.budgets.size} Kategori",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OverBudgetAlertBanner(overBudgetCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Peringatan Pemborosan (Over-Budget)",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "$overBudgetCount kategori akun telah melampaui batas maksimal pengeluaran bulan ini. Evaluasi pos belanja untuk menjaga amanah.",
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun BudgetItemCard(
    budget: BudgetAllocation,
    spentAmount: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val usageRatio = budget.getUsagePercentage(spentAmount).coerceIn(0.0, 1.0)
    val usagePercentage = if (budget.monthlyLimit > 0) (spentAmount / budget.monthlyLimit) * 100.0 else 0.0
    val isOver = budget.isOverBudget(spentAmount)
    val isNear = budget.isNearLimit(spentAmount)
    val remaining = budget.getRemainingAmount(spentAmount)

    val itemColor = when {
        isOver -> MaterialTheme.colorScheme.error
        isNear -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    val iconVector = getIconForBudget(budget.iconKey)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_card_${budget.accountId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isOver) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(itemColor.copy(alpha = 0.12f))
                            .border(1.dp, itemColor.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = itemColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = budget.categoryName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Batas: Rp ${formatRupiah(budget.monthlyLimit)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = itemColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isOver) "Melebihi Batas!" else "${String.format("%.1f", usagePercentage)}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = itemColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Anggaran",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Anggaran",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { usageRatio.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = itemColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Terpakai: Rp ${formatRupiah(spentAmount)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isOver) {
                        "Minus: -Rp ${formatRupiah(spentAmount - budget.monthlyLimit)}"
                    } else {
                        "Sisa Kuota: Rp ${formatRupiah(remaining)}"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    existingBudget: BudgetAllocation?,
    state: AmanahLedgerUiState,
    onDismiss: () -> Unit,
    onSave: (accountId: String, categoryName: String, limit: Double, alertPercent: Double, iconKey: String) -> Unit
) {
    val isEdit = existingBudget != null
    val expenseAccounts = state.accounts.filter { it.category == AccountCategory.EXPENSE && it.id != "acc_disbursed" }

    var selectedAccountId by remember {
        mutableStateOf(existingBudget?.accountId ?: (expenseAccounts.firstOrNull()?.id ?: "acc_living"))
    }
    var customCategoryName by remember {
        mutableStateOf(existingBudget?.categoryName ?: (expenseAccounts.firstOrNull { it.id == selectedAccountId }?.name ?: "Biaya Hidup & Pangan"))
    }
    var limitText by remember {
        mutableStateOf(existingBudget?.let { String.format("%.0f", it.monthlyLimit) } ?: "1000000")
    }
    var alertThreshold by remember {
        mutableDoubleStateOf(existingBudget?.alertThresholdPercent ?: 0.8)
    }
    var selectedIconKey by remember {
        mutableStateOf(existingBudget?.iconKey ?: "food")
    }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (isEdit) "Sesuaikan Anggaran" else "Tambah Alokasi Anggaran",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Account Dropdown
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    val currentAccount = state.getAccount(selectedAccountId)
                    OutlinedTextField(
                        value = currentAccount?.name ?: "Pilih Akun Pengeluaran",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pos Akun Pengeluaran", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        expenseAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(acc.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                        Text(acc.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                    }
                                },
                                onClick = {
                                    selectedAccountId = acc.id
                                    customCategoryName = acc.name
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. Limit Amount Field
                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it.filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Batas Maksimal Bulanan (Rp)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    prefix = { Text("Rp ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Icon Selection
                Text(
                    text = "Pilih Ikon Kategori",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val icons = listOf(
                        "food" to Icons.Default.Fastfood,
                        "commute" to Icons.Default.Commute,
                        "bolt" to Icons.Default.Bolt,
                        "school" to Icons.Default.School,
                        "health" to Icons.Default.LocalHospital,
                        "more" to Icons.Default.MoreHoriz
                    )

                    icons.forEach { (key, icon) ->
                        val isSelected = selectedIconKey == key
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedIconKey = key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = key,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 4. Alert Threshold Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Batas Peringatan Awal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(alertThreshold * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Slider(
                        value = alertThreshold.toFloat(),
                        onValueChange = { alertThreshold = it.toDouble() },
                        valueRange = 0.5f..0.95f,
                        steps = 8
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limitVal = limitText.toDoubleOrNull() ?: 0.0
                    if (limitVal <= 0.0) {
                        errorMessage = "Nominal batas anggaran harus lebih besar dari 0"
                        return@Button
                    }
                    onSave(selectedAccountId, customCategoryName, limitVal, alertThreshold, selectedIconKey)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Simpan Alokasi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun IslamicBudgetWisdomCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prinsip Syariah: Anti-Israf & Keseimbangan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "وَالَّذِينَ إِذَا أَنْفَقُوا لَمْ يُسْرِفُوا وَلَمْ يَقْتُرُوا وَكَانَ بَيْنَ ذَٰلِكَ قَوَامًا",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\"Dan orang-orang yang apabila membelanjakan (harta), mereka tidak berlebihan, dan tidak (pula) kikir, dan adalah (pembelanjaan itu) di tengah-tengah antara yang demikian.\" (QS. Al-Furqan: 67)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

fun getIconForBudget(key: String): ImageVector {
    return when (key) {
        "food" -> Icons.Default.Fastfood
        "commute" -> Icons.Default.Commute
        "bolt" -> Icons.Default.Bolt
        "school" -> Icons.Default.School
        "health" -> Icons.Default.LocalHospital
        else -> Icons.Default.ShoppingBag
    }
}

@Composable
fun SpendingPatternOptimizerSection(
    analysis: SpendingPatternAnalysis,
    selectedGoalMode: FinancialGoalMode,
    onSelectGoalMode: (FinancialGoalMode) -> Unit,
    onApplyAllSuggestions: (List<BudgetCapSuggestion>) -> Unit,
    onApplySingleSuggestion: (BudgetCapSuggestion) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_optimizer_section"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with AI / Smart Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Analisis Pola & Optimasi Pagu",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rekomendasi Cerdas Bebas Riba & Israf",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                ) {
                    Text(
                        text = if (isExpanded) "Tutup" else "Buka (${analysis.suggestions.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))

                // Financial Goal Mode Selector Pills
                Text(
                    text = "Pilih Target Finansial Syariah",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FinancialGoalMode.values().forEach { mode ->
                        val isSelected = selectedGoalMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectGoalMode(mode) }
                                .testTag("goal_mode_${mode.name}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = mode.title.split("(").first().trim(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${(mode.essentialWeight * 100).toInt()}/${(mode.discretionaryWeight * 100).toInt()}/${(mode.savingsInfaqWeight * 100).toInt()}",
                                    fontSize = 8.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Pattern Breakdown Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Distribusi Pola Belanja Saat Ini", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Total: Rp ${formatRupiah(analysis.totalMonthlyBurnRate)}/bln", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Multi-segment color bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            val essentialWeight = analysis.essentialRatio.toFloat().coerceAtLeast(0.01f)
                            val discretionaryWeight = analysis.discretionaryRatio.toFloat().coerceAtLeast(0.01f)
                            val spiritualWeight = analysis.spiritualRatio.toFloat().coerceAtLeast(0.01f)

                            Box(modifier = Modifier.weight(essentialWeight).fillMaxSize().background(Color(0xFF29B6F6)))
                            Box(modifier = Modifier.weight(discretionaryWeight).fillMaxSize().background(Color(0xFFFFB74D)))
                            Box(modifier = Modifier.weight(spiritualWeight).fillMaxSize().background(MaterialTheme.colorScheme.primary))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF29B6F6)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Esensial ${(analysis.essentialRatio * 100).toInt()}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFFB74D)))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gaya Hidup ${(analysis.discretionaryRatio * 100).toInt()}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Spiritual/Tab ${(analysis.spiritualRatio * 100).toInt()}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion Cards Title
                Text(
                    text = "Saran Pagu Optimal per Kategori",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestions List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    analysis.suggestions.forEach { suggestion ->
                        SuggestionItemCard(
                            suggestion = suggestion,
                            onApply = { onApplySingleSuggestion(suggestion) }
                        )
                    }
                }

                if (analysis.suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onApplyAllSuggestions(analysis.suggestions) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("apply_all_suggestions_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (analysis.potentialMonthlySavings > 0)
                                "Terapkan Semua Pagu Optimal (Hemat Rp ${formatRupiah(analysis.potentialMonthlySavings)}/bln)"
                            else
                                "Terapkan Semua Saran Pagu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionItemCard(
    suggestion: BudgetCapSuggestion,
    onApply: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth().testTag("suggestion_card_${suggestion.accountId}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getIconForBudget(suggestion.iconKey),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = suggestion.categoryName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = when (suggestion.status) {
                        SuggestionStatus.RECOMMEND_CUT -> Color(0xFFEF5350).copy(alpha = 0.2f)
                        SuggestionStatus.OPTIMAL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        SuggestionStatus.RECOMMEND_EXPAND -> Color(0xFF42A5F5).copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when (suggestion.status) {
                            SuggestionStatus.RECOMMEND_CUT -> "Pangkas Pagu"
                            SuggestionStatus.OPTIMAL -> "Optimal"
                            SuggestionStatus.RECOMMEND_EXPAND -> "Perbesar Pagu"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (suggestion.status) {
                            SuggestionStatus.RECOMMEND_CUT -> Color(0xFFFF8A80)
                            SuggestionStatus.OPTIMAL -> MaterialTheme.colorScheme.primary
                            SuggestionStatus.RECOMMEND_EXPAND -> Color(0xFF90CAF9)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Values comparison row: Saat ini vs Realisasi vs Saran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Pagu Saat Ini", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Rp ${formatRupiah(suggestion.currentLimit)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text("Rata-rata Belanja", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Rp ${formatRupiah(suggestion.actualMonthlyAverage)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Saran Pagu Cerdas", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Text("Rp ${formatRupiah(suggestion.suggestedCap)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = suggestion.reasoning,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            if (suggestion.currentLimit != suggestion.suggestedCap) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = onApply,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .testTag("apply_suggestion_${suggestion.accountId}")
                    ) {
                        Icon(
                            imageVector = if (suggestion.currentLimit <= 0.0) Icons.Default.Add else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (suggestion.currentLimit <= 0.0) "Tambahkan Pagu" else "Terapkan Pagu",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}


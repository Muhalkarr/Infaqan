package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.state.AmanahLedgerViewModel
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletType
import com.example.ui.theme.EmeraldDark
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiWalletScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddWalletDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var editingWallet by remember { mutableStateOf<WalletAccount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Multi-Kantong Rekening",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Pemisahan Fisik Saldo & Mutasi Antar-Akun",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("wallet_screen_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.testTag("wallet_top_transfer_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Mutasi Saldo", tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("wallet_menu_sidebar_button")
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
            ExtendedFloatingActionButton(
                onClick = { showAddWalletDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Kantong", fontWeight = FontWeight.Bold) },
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("add_wallet_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Summary Card
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
                                text = "TOTAL SALDO SELURUH KANTONG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = if (state.securityConfig.isMaskBalance) "Rp ••••••••" else "Rp ${nf.format(state.totalWalletBalance)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${state.wallets.size} Kantong",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showTransferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wallet_quick_transfer_btn")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mutasi Dana", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { showAddWalletDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("wallet_quick_add_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Kantong", fontSize = 13.sp)
                        }
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
                    text = { Text("Daftar Kantong (${state.wallets.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Riwayat Mutasi (${state.walletMutations.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTabIndex == 0) {
                // Wallet List Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.wallets, key = { it.id }) { wallet ->
                        val balance = state.getWalletBalance(wallet.id)
                        val linkedAcc = state.getAccount(wallet.linkedAccountId)
                        WalletCardItem(
                            wallet = wallet,
                            balance = balance,
                            linkedAccount = linkedAcc,
                            isMasked = state.securityConfig.isMaskBalance,
                            onEdit = { editingWallet = wallet },
                            onDelete = { viewModel.deleteWallet(wallet.id) },
                            onTransferFrom = {
                                showTransferDialog = true
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // Mutation History Tab
                if (state.walletMutations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Mutasi Antar-Kantong",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Pindah dana antar bank, e-wallet, atau tarik tunai tanpa mengubah beban konsumsi.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.walletMutations, key = { it.id }) { mutation ->
                            val fromWallet = state.getWallet(mutation.fromWalletId)
                            val toWallet = state.getWallet(mutation.toWalletId)
                            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.SwapHoriz,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "${fromWallet?.name ?: "Kantong Asal"} ➔ ${toWallet?.name ?: "Kantong Tujuan"}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = sdf.format(Date(mutation.timestampMillis)),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Text(
                                            text = "Rp ${nf.format(mutation.amount)}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (mutation.note.isNotBlank() || mutation.adminFee > 0.0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            if (mutation.note.isNotBlank()) {
                                                Text(
                                                    text = "Catatan: ${mutation.note}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (mutation.adminFee > 0.0) {
                                                Text(
                                                    text = "Biaya Admin: Rp ${nf.format(mutation.adminFee)}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
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

    // Add / Edit Wallet Modal Bottom Sheet
    if (showAddWalletDialog || editingWallet != null) {
        val curBal = if (editingWallet != null) state.getWalletBalance(editingWallet!!.id) else 0.0
        val assetAccounts = state.accounts.filter { it.category == AccountCategory.ASSET }
        WalletFormBottomSheet(
            initialWallet = editingWallet,
            initialBalance = curBal,
            assetAccounts = assetAccounts,
            onCreateAccount = { name, code ->
                viewModel.addNewAssetAccount(name, code)
            },
            onDismiss = {
                showAddWalletDialog = false
                editingWallet = null
            },
            onSave = { wallet, targetBalance ->
                viewModel.saveWalletWithBalance(wallet, targetBalance, isEdit = editingWallet != null)
                showAddWalletDialog = false
                editingWallet = null
            }
        )
    }

    // Transfer Modal Bottom Sheet
    if (showTransferDialog) {
        WalletTransferBottomSheet(
            wallets = state.wallets,
            getWalletBalance = { state.getWalletBalance(it) },
            isDeficitProtectionEnabled = state.isDeficitProtectionEnabled,
            onDismiss = { showTransferDialog = false },
            onTransfer = { fromId, toId, amount, adminFee, note, receipt ->
                viewModel.transferBetweenWallets(
                    fromWalletId = fromId,
                    toWalletId = toId,
                    amount = amount,
                    adminFee = adminFee,
                    note = note,
                    receiptAttachment = receipt
                )
                showTransferDialog = false
            }
        )
    }
}

@Composable
private fun WalletCardItem(
    wallet: WalletAccount,
    balance: Double,
    linkedAccount: Account? = null,
    isMasked: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTransferFrom: () -> Unit
) {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val walletColor = Color(wallet.colorHex)

    val icon = when (wallet.type) {
        WalletType.CASH -> Icons.Default.MonetizationOn
        WalletType.BANK_SYARIAH -> Icons.Default.AccountBalance
        WalletType.BANK_KONVENSIONAL -> Icons.Default.AccountBalance
        WalletType.E_WALLET -> Icons.Default.QrCode
        WalletType.GOLD_ASSET -> Icons.Default.Savings
        WalletType.SPECIAL_SAVINGS -> Icons.Default.CreditCard
        WalletType.OTHER -> Icons.Default.AccountBalanceWallet
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_item_${wallet.id}")
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
                            .background(walletColor.copy(alpha = 0.18f))
                            .border(1.dp, walletColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = walletColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wallet.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (wallet.isDefault) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "UTAMA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${wallet.institutionName} • ${wallet.type.displayName}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (linkedAccount != null) {
                            Surface(
                                color = walletColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "Akun: [${linkedAccount.code}] ${linkedAccount.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = walletColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Saldo Akun Terkait",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isMasked) "Rp ••••••••" else "Rp ${nf.format(balance)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = if (balance >= 0) walletColor else MaterialTheme.colorScheme.error
                    )
                }

                if (wallet.accountNumber.isNotBlank()) {
                    Text(
                        text = "No: ${wallet.accountNumber}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletFormBottomSheet(
    initialWallet: WalletAccount?,
    initialBalance: Double,
    assetAccounts: List<Account>,
    onCreateAccount: (name: String, code: String) -> String,
    onDismiss: () -> Unit,
    onSave: (WalletAccount, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val defaultAccountForType: (WalletType) -> String = { type ->
        when (type) {
            WalletType.CASH -> "acc_cash"
            WalletType.E_WALLET -> "acc_ewallet"
            WalletType.BANK_SYARIAH -> "acc_bank"
            WalletType.BANK_KONVENSIONAL -> assetAccounts.firstOrNull { it.id == "acc_bank_konven" }?.id ?: "acc_bank"
            WalletType.SPECIAL_SAVINGS -> assetAccounts.firstOrNull { it.id == "acc_bank_special" }?.id ?: "acc_bank"
            WalletType.GOLD_ASSET -> "acc_gold"
            WalletType.OTHER -> assetAccounts.firstOrNull { it.id == "acc_other_asset" }?.id ?: "acc_other_asset"
        }
    }

    var name by remember { mutableStateOf(initialWallet?.name ?: "") }
    var institutionName by remember { mutableStateOf(initialWallet?.institutionName ?: "") }
    var accountNumber by remember { mutableStateOf(initialWallet?.accountNumber ?: "") }
    var selectedType by remember { mutableStateOf(initialWallet?.type ?: WalletType.CASH) }
    var linkedAccountId by remember {
        mutableStateOf(initialWallet?.linkedAccountId ?: defaultAccountForType(selectedType))
    }
    var balanceText by remember {
        mutableStateOf(
            if (initialWallet != null) {
                if (initialBalance > 0.0) initialBalance.toLong().toString() else "0"
            } else ""
        )
    }
    var notes by remember { mutableStateOf(initialWallet?.notes ?: "") }
    var isDefault by remember { mutableStateOf(initialWallet?.isDefault ?: false) }

    var typeExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    var showCreateAccountDialog by remember { mutableStateOf(false) }
    var newAccountName by remember { mutableStateOf("") }
    var newAccountCode by remember { mutableStateOf("") }

    val colorHex = when (selectedType) {
        WalletType.CASH -> 0xFF10B981
        WalletType.BANK_SYARIAH -> 0xFF059669
        WalletType.BANK_KONVENSIONAL -> 0xFF2563EB
        WalletType.E_WALLET -> 0xFF0284C7
        WalletType.GOLD_ASSET -> 0xFFF59E0B
        WalletType.SPECIAL_SAVINGS -> 0xFF8B5CF6
        WalletType.OTHER -> 0xFF64748B
    }

    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val currentNumericBalance = balanceText.toDoubleOrNull() ?: 0.0

    if (showCreateAccountDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAccountDialog = false },
            title = { Text("Buat Akun Buku Besar Aset Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tambahkan akun buku besar mandiri untuk mengisolasi saldo kantong ini secara penuh.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newAccountName,
                        onValueChange = { newAccountName = it },
                        label = { Text("Nama Akun Buku Besar") },
                        placeholder = { Text("cth: Bank Mandiri Tabungan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAccountCode,
                        onValueChange = { newAccountCode = it },
                        label = { Text("Kode Akun (Opsional)") },
                        placeholder = { Text("cth: 102.3 atau kosongkan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAccountName.isNotBlank()) {
                            val createdId = onCreateAccount(newAccountName.trim(), newAccountCode.trim())
                            linkedAccountId = createdId
                            showCreateAccountDialog = false
                            newAccountName = ""
                            newAccountCode = ""
                        }
                    },
                    enabled = newAccountName.isNotBlank()
                ) {
                    Text("Buat & Tautkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateAccountDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (initialWallet != null) Icons.Default.Edit else Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(colorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (initialWallet != null) "Edit Kantong Rekening" else "Tambah Kantong Baru",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kelola rekening fisik & isolasi saldo buku besar",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Form Inputs Scrollable Container
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kantong") },
                    placeholder = { Text("cth: Tabungan Haji BSI, GoPay Syariah") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_form_name_input")
                )

                // 1. Tipe Kantong
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipe Kantong") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        WalletType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = {
                                    selectedType = type
                                    linkedAccountId = defaultAccountForType(type)
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                // 2. Akun Buku Besar Tertaut (Ledger Account)
                val curLinkedAcc = assetAccounts.firstOrNull { it.id == linkedAccountId }
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (curLinkedAcc != null) "[${curLinkedAcc.code}] ${curLinkedAcc.name}" else linkedAccountId,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Akun Buku Besar Tertaut (Ledger)") },
                        supportingText = {
                            Text(
                                text = "Akun aset buku besar berpasangan untuk isolasi saldo riil",
                                fontSize = 11.sp
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        assetAccounts.forEach { acc ->
                            val isSelected = acc.id == linkedAccountId
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "[${acc.code}] ${acc.name}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (acc.description.isNotBlank()) {
                                            Text(
                                                text = acc.description,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    linkedAccountId = acc.id
                                    accountExpanded = false
                                }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "+ Buat Akun Buku Besar Baru",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                accountExpanded = false
                                showCreateAccountDialog = true
                            }
                        )
                    }
                }

                // 3. Menentukan / Mengedit Nominal Saldo Kantong
                Column {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { input ->
                            balanceText = input.filter { it.isDigit() }
                        },
                        label = {
                            Text(if (initialWallet != null) "Nominal Saldo Kantong (Rp)" else "Saldo Awal Kantong (Rp)")
                        },
                        placeholder = { Text("0") },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        supportingText = {
                            if (currentNumericBalance > 0.0) {
                                Text(
                                    text = "Terbaca: Rp ${nf.format(currentNumericBalance)} (Dicatat seimbang ke Modal Awal/Ekuitas)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Isi nominal saldo bila kantong ini sudah memiliki dana saat didaftarkan",
                                    fontSize = 11.sp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    // Quick Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val quickAmounts = listOf(
                            100_000L to "+100rb",
                            500_000L to "+500rb",
                            1_000_000L to "+1jt",
                            5_000_000L to "+5jt"
                        )
                        quickAmounts.forEach { (amt, label) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val cur = balanceText.toLongOrNull() ?: 0L
                                        balanceText = (cur + amt).toString()
                                    },
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { balanceText = "0" },
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // 4. Institusi / Bank
                OutlinedTextField(
                    value = institutionName,
                    onValueChange = { institutionName = it },
                    label = { Text("Institusi / Penyedia") },
                    placeholder = { Text("cth: Bank Syariah Indonesia, Dompet Fisik, Antam") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Nomor Rekening
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Nomor Rekening / Identitas") },
                    placeholder = { Text("cth: 7128394012 / 0812xxxx") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. Catatan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Peruntukan") },
                    placeholder = { Text("cth: Khusus operasional rumah tangga") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Bottom Action Buttons
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val wallet = WalletAccount(
                            id = initialWallet?.id ?: "w_${UUID.randomUUID().toString().take(8)}",
                            name = name.trim(),
                            type = selectedType,
                            institutionName = if (institutionName.isNotBlank()) institutionName.trim() else selectedType.displayName,
                            accountNumber = accountNumber.trim(),
                            linkedAccountId = linkedAccountId,
                            colorHex = colorHex,
                            isDefault = isDefault,
                            notes = notes.trim()
                        )
                        val targetBal = balanceText.toDoubleOrNull() ?: 0.0
                        onSave(wallet, targetBal)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_wallet_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (initialWallet != null) "Simpan Perubahan" else "Simpan Kantong",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletTransferBottomSheet(
    wallets: List<WalletAccount>,
    getWalletBalance: (String) -> Double,
    isDeficitProtectionEnabled: Boolean,
    onDismiss: () -> Unit,
    onTransfer: (fromId: String, toId: String, amount: Double, adminFee: Double, note: String, receipt: ReceiptAttachment?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        if (wallets.size < 2) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Kantong Tidak Mencukupi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Anda membutuhkan minimal 2 kantong rekening untuk melakukan transfer/mutasi saldo antar-kantong.",
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mengerti")
                }
            }
            return@ModalBottomSheet
        }

        var fromWalletId by remember { mutableStateOf(wallets[0].id) }
        var toWalletId by remember { mutableStateOf(wallets.getOrNull(1)?.id ?: wallets[0].id) }
        var amountText by remember { mutableStateOf("") }
        var adminFeeText by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        var fromExpanded by remember { mutableStateOf(false) }
        var toExpanded by remember { mutableStateOf(false) }

        // Receipt Attachment fields
        var attachReceipt by remember { mutableStateOf(false) }
        var receiptRefNumber by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mutasi Antar-Kantong",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Pindahkan saldo fisik tanpa membebani pos belanja riil",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Form Content Scrollable
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // From Wallet Selector
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = it }
                ) {
                    val fromWallet = wallets.firstOrNull { it.id == fromWalletId }
                    OutlinedTextField(
                        value = fromWallet?.name ?: "Pilih Asal",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dari Kantong (Sumber)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fromExpanded,
                        onDismissRequest = { fromExpanded = false }
                    ) {
                        wallets.forEach { w ->
                            val bal = getWalletBalance(w.id)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(w.name, fontSize = 13.sp)
                                        Text(
                                            text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(bal)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (bal < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    fromWalletId = w.id
                                    fromExpanded = false
                                }
                            )
                        }
                    }
                }

                // To Wallet Selector
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = it }
                ) {
                    val toWallet = wallets.firstOrNull { it.id == toWalletId }
                    OutlinedTextField(
                        value = toWallet?.name ?: "Pilih Tujuan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ke Kantong (Tujuan)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = toExpanded,
                        onDismissRequest = { toExpanded = false }
                    ) {
                        wallets.filter { it.id != fromWalletId }.forEach { w ->
                            DropdownMenuItem(
                                text = { Text(w.name) },
                                onClick = {
                                    toWalletId = w.id
                                    toExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Mutasi (Rp)") },
                    placeholder = { Text("cth: 500000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transfer_amount_input")
                )

                OutlinedTextField(
                    value = adminFeeText,
                    onValueChange = { adminFeeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Biaya Admin Transfer (Jika Ada)") },
                    placeholder = { Text("cth: 2500 atau 6500 (opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
                val curAmount = amountText.toDoubleOrNull() ?: 0.0
                val curAdminFee = adminFeeText.toDoubleOrNull() ?: 0.0
                val totalDeduction = curAmount + curAdminFee
                val fromWalletBal = getWalletBalance(fromWalletId)
                val willWalletDeficit = totalDeduction > fromWalletBal

                if (curAmount > 0.0 && willWalletDeficit) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDeficitProtectionEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDeficitProtectionEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isDeficitProtectionEnabled) "⚠️ Proteksi Defisit: Saldo Tidak Cukup" else "⚠️ Peringatan: Saldo Kantong Asal Menjadi Minus",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDeficitProtectionEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Saldo kantong: Rp ${nf.format(fromWalletBal)} | Total mutasi: Rp ${nf.format(totalDeduction)} (Defisit: -Rp ${nf.format(totalDeduction - fromWalletBal)}).",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isDeficitProtectionEnabled) {
                                Text(
                                    text = "Mutasi saldo ditolak karena proteksi saldo minus diaktifkan.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                } else if (fromWalletId.isNotBlank()) {
                    Text(
                        text = "Saldo kantong asal: Rp ${nf.format(fromWalletBal)}" + if (curAmount > 0) " (Sisa: Rp ${nf.format(fromWalletBal - totalDeduction)})" else "",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keterangan Mutasi") },
                    placeholder = { Text("cth: Penarikan ATM tunai dari BSI") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Optional Receipt
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lampirkan Bukti Transfer", fontSize = 13.sp)
                    androidx.compose.material3.Switch(
                        checked = attachReceipt,
                        onCheckedChange = { attachReceipt = it }
                    )
                }

                if (attachReceipt) {
                    OutlinedTextField(
                        value = receiptRefNumber,
                        onValueChange = { receiptRefNumber = it },
                        label = { Text("No. Referensi / ID Transaksi") },
                        placeholder = { Text("cth: TRX-2026-0831-9921") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val amount = amountText.toDoubleOrNull() ?: 0.0
            val adminFee = adminFeeText.toDoubleOrNull() ?: 0.0
            val totalDeduction = amount + adminFee
            val fromWalletBal = getWalletBalance(fromWalletId)
            val willWalletDeficit = totalDeduction > fromWalletBal
            val isValid = fromWalletId != toWalletId && amount > 0.0 && (!isDeficitProtectionEnabled || !willWalletDeficit)

            Button(
                onClick = {
                    if (isValid) {
                        val receipt = if (attachReceipt) {
                            ReceiptAttachment(
                                title = "Bukti Mutasi Saldo",
                                receiptType = ReceiptType.BANK_TRANSFER_PROOF,
                                referenceNumber = receiptRefNumber.ifBlank { "TRX-${System.currentTimeMillis() % 100000}" },
                                amount = amount,
                                notes = note
                            )
                        } else null

                        onTransfer(fromWalletId, toWalletId, amount, adminFee, note, receipt)
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("execute_transfer_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Eksekusi Mutasi Saldo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

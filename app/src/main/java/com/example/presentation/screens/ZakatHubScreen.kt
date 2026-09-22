package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.infaq.AsnafCategory
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.state.AmanahLedgerViewModel
import com.example.core.ui.AmanahHapticEngine
import com.example.core.wallet.WalletAccount
import com.example.core.zakat.ZakatFitrahFamilyCalculation
import com.example.core.zakat.ZakatMaalCalculation
import com.example.core.zakat.ZakatPerniagaanCalculation
import com.example.core.zakat.ZakatProfesiCalculation
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatHubScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    var selectedTab by remember { mutableStateOf(0) }
    var showDisburseModal by remember { mutableStateOf(false) }
    var showGoldEditDialog by remember { mutableStateOf(false) }
    var disburseAmountPreset by remember { mutableStateOf(0.0) }
    var disburseTitlePreset by remember { mutableStateOf("Zakat Maal (Simpanan)") }

    LaunchedEffect(Unit) {
        viewModel.syncZakatMaalFromDatabase()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kalkulator Zakat Komprehensif",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Zakat Maal (Aset/Tabungan), Profesi, Perniagaan, & Fitrah",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("zakat_screen_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("zakat_screen_menu_sidebar_button")
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Gold Reference Header Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Acuan Emas: Rp ${nf.format(state.goldPricePerGram)}/g",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Nisab 85g: Rp ${nf.format(state.goldPricePerGram * 85.0)} • ${state.goldPriceSource}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = { showGoldEditDialog = true },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
                            .testTag("edit_gold_price_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Ubah Acuan Emas", modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        selectedTab = 0
                    },
                    text = { Text("Zakat Maal", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_zakat_maal")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        selectedTab = 1
                    },
                    text = { Text("Zakat Profesi", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_zakat_profesi")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        selectedTab = 2
                    },
                    text = { Text("Perniagaan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_zakat_perniagaan")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        selectedTab = 3
                    },
                    text = { Text("Zakat Fitrah", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_zakat_fitrah")
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        selectedTab = 4
                    },
                    text = { Text("Tathhir (Non-Halal)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    modifier = Modifier.testTag("tab_tathhir")
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> ZakatMaalTab(
                        calc = state.zakatMaal,
                        totalWalletBalance = state.totalWalletBalance,
                        walletsCount = state.wallets.size,
                        ibadahGoals = state.ibadahGoals,
                        onUpdate = { viewModel.updateZakatMaal(it) },
                        onSyncFromDatabase = { viewModel.syncZakatMaalFromDatabase() },
                        onTunaikan = { amt ->
                            disburseAmountPreset = amt
                            disburseTitlePreset = "Zakat Maal (Simpanan & Tabungan)"
                            showDisburseModal = true
                        }
                    )
                    1 -> ZakatProfesiTab(
                        calc = state.zakatProfesi,
                        onUpdate = { viewModel.updateZakatProfesi(it) },
                        onTunaikan = { amt ->
                            disburseAmountPreset = amt
                            disburseTitlePreset = "Zakat Penghasilan / Profesi"
                            showDisburseModal = true
                        }
                    )
                    2 -> ZakatPerniagaanTab(
                        calc = state.zakatPerniagaan,
                        onUpdate = { viewModel.updateZakatPerniagaan(it) },
                        onTunaikan = { amt ->
                            disburseAmountPreset = amt
                            disburseTitlePreset = "Zakat Perniagaan (Tijarah)"
                            showDisburseModal = true
                        }
                    )
                    3 -> ZakatFitrahTab(
                        calc = state.zakatFitrah,
                        onUpdate = { viewModel.updateZakatFitrah(it) },
                        onTunaikan = { amt ->
                            disburseAmountPreset = amt
                            disburseTitlePreset = "Zakat Fitrah Keluarga (${state.zakatFitrah.familyMemberCount} Jiwa)"
                            showDisburseModal = true
                        }
                    )
                    4 -> TathhirCleansingTab(
                        viewModel = viewModel,
                        quarantineBalance = state.quarantineSyubhatBalance,
                        wallets = state.wallets
                    )
                }
            }
        }
    }

    if (showDisburseModal) {
        DisburseZakatDialog(
            presetTitle = disburseTitlePreset,
            presetAmount = disburseAmountPreset,
            wallets = state.wallets,
            onDismiss = { showDisburseModal = false },
            onConfirm = { title, amt, asnaf, accountId, recipient, notes, receipt ->
                viewModel.disburseZakat(
                    title = title,
                    amount = amt,
                    asnafCategory = asnaf,
                    sourceAccountId = accountId,
                    recipientName = recipient,
                    notes = notes,
                    receipt = receipt
                )
                showDisburseModal = false
            }
        )
    }

    if (showGoldEditDialog) {
        EditGoldPriceDialog(
            currentPrice = state.goldPricePerGram,
            currentSource = state.goldPriceSource,
            onDismiss = { showGoldEditDialog = false },
            onConfirm = { newPrice, source ->
                viewModel.updateGoldPriceWithMetadata(newPrice, source)
                showGoldEditDialog = false
            }
        )
    }
}

@Composable
fun EditGoldPriceDialog(
    currentPrice: Double,
    currentSource: String,
    onDismiss: () -> Unit,
    onConfirm: (newPrice: Double, source: String) -> Unit
) {
    var priceText by remember { mutableStateOf(currentPrice.toLong().toString()) }
    var selectedSource by remember { mutableStateOf(currentSource) }

    val presetSources = listOf(
        "Antam (Standar Logam Mulia)",
        "BAZNAS RI (SK Hisab Zakat)",
        "Pegadaian Syariah",
        "Input Manual Pengguna"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sesuaikan Acuan Harga Emas",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Harga emas per gram digunakan untuk menghitung batas nisab Zakat Maal, Perniagaan (85 gram emas), dan Haul secara offline.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Harga Emas per Gram (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Sumber Acuan Referensi:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    presetSources.forEach { source ->
                        val isSelected = selectedSource == source
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSource = source },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Text(
                                text = source,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    if (p > 0) {
                        onConfirm(p, selectedSource)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan Acuan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun ZakatProfesiTab(
    calc: ZakatProfesiCalculation,
    onUpdate: (ZakatProfesiCalculation) -> Unit,
    onTunaikan: (Double) -> Unit
) {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    var monthlySalaryText by remember(calc) { mutableStateOf(calc.monthlySalary.toLong().toString()) }
    var bonusText by remember(calc) { mutableStateOf(calc.otherIncomeBonus.toLong().toString()) }
    var basicExpenseText by remember(calc) { mutableStateOf(calc.basicNeedsExpense.toLong().toString()) }
    var debtPaymentText by remember(calc) { mutableStateOf(calc.debtPaymentMonthly.toLong().toString()) }
    var ricePriceText by remember(calc) { mutableStateOf(calc.ricePricePerKg.toLong().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Result Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (calc.isObligated) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
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
                    Column {
                        Text(
                            text = if (calc.isObligated) "WAJIB ZAKAT PROFESI (2.5%)" else "BELUM MENCAPAI NISAB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (calc.isObligated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rp ${nf.format(calc.zakatPayableAmount)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (calc.isObligated) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (calc.isObligated) {
                        Button(
                            onClick = { onTunaikan(calc.zakatPayableAmount) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("tunaikan_zakat_profesi_btn")
                        ) {
                            Text("Tunaikan", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Netto: Rp ${nf.format(calc.netMonthlyIncome)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nisab: Rp ${nf.format(calc.nisabThreshold)} (524 kg beras)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text("Komponen Pendapatan & Beban Pokok", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        OutlinedTextField(
            value = monthlySalaryText,
            onValueChange = {
                monthlySalaryText = it.filter { ch -> ch.isDigit() }
                val salary = monthlySalaryText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(monthlyGrossSalary = salary))
            },
            label = { Text("Gaji Pokok & Tunjangan Rutin Bulanan") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = bonusText,
            onValueChange = {
                bonusText = it.filter { ch -> ch.isDigit() }
                val bonus = bonusText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(monthlyAllowanceAndOther = bonus))
            },
            label = { Text("Pendapatan Tambahan / Bonus / THR") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = basicExpenseText,
            onValueChange = {
                basicExpenseText = it.filter { ch -> ch.isDigit() }
                val basic = basicExpenseText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(monthlyEssentialExpenses = basic))
            },
            label = { Text("Pengeluaran Kebutuhan Pokok Primer (Haajah Ashliyyah)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = debtPaymentText,
            onValueChange = {
                debtPaymentText = it.filter { ch -> ch.isDigit() }
                val debt = debtPaymentText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(monthlyDebtInstallment = debt))
            },
            label = { Text("Cicilan Hutang Jatuh Tempo Bulanan") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ricePriceText,
            onValueChange = {
                ricePriceText = it.filter { ch -> ch.isDigit() }
                val price = ricePriceText.toDoubleOrNull() ?: 15000.0
                onUpdate(calc.copy(ricePricePerKg = price))
            },
            label = { Text("Harga Beras per Kg (Standar Nisab 524 Kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ZakatPerniagaanTab(
    calc: ZakatPerniagaanCalculation,
    onUpdate: (ZakatPerniagaanCalculation) -> Unit,
    onTunaikan: (Double) -> Unit
) {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    var cashText by remember(calc) { mutableStateOf(calc.currentCashAndBank.toLong().toString()) }
    var stockText by remember(calc) { mutableStateOf(calc.inventoryStockValue.toLong().toString()) }
    var receivableText by remember(calc) { mutableStateOf(calc.receivablesCollectable.toLong().toString()) }
    var shortDebtText by remember(calc) { mutableStateOf(calc.shortTermPayables.toLong().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Result Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (calc.isObligated) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
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
                    Column {
                        Text(
                            text = if (calc.isObligated) "WAJIB ZAKAT TIJARAH (2.5%)" else "BELUM MENCAPAI NISAB 85g EMAS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (calc.isObligated) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rp ${nf.format(calc.zakatPayableAmount)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (calc.isObligated) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (calc.isObligated) {
                        Button(
                            onClick = { onTunaikan(calc.zakatPayableAmount) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary),
                            modifier = Modifier.testTag("tunaikan_zakat_tijarah_btn")
                        ) {
                            Text("Tunaikan", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Aset Lancar Bersih: Rp ${nf.format(calc.netZakatAsset)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nisab: Rp ${nf.format(calc.nisabGoldThreshold)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text("Neraca Aset Lancar Usaha / Bisnis (1 Tahun Haul)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        OutlinedTextField(
            value = cashText,
            onValueChange = {
                cashText = it.filter { ch -> ch.isDigit() }
                val cash = cashText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(currentCashInBusiness = cash))
            },
            label = { Text("Kas & Rekening Operasional Usaha") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stockText,
            onValueChange = {
                stockText = it.filter { ch -> ch.isDigit() }
                val stock = stockText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(inventoryValue = stock))
            },
            label = { Text("Nilai Persediaan Barang Dagangan Siap Jual") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = receivableText,
            onValueChange = {
                receivableText = it.filter { ch -> ch.isDigit() }
                val rec = receivableText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(receivableCollectible = rec))
            },
            label = { Text("Piutang Lancar yang Dapat Ditagih") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = shortDebtText,
            onValueChange = {
                shortDebtText = it.filter { ch -> ch.isDigit() }
                val debt = shortDebtText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(shortTermPayable = debt))
            },
            label = { Text("Hutang Dagang / Operasional Jatuh Tempo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun ZakatFitrahTab(
    calc: ZakatFitrahFamilyCalculation,
    onUpdate: (ZakatFitrahFamilyCalculation) -> Unit,
    onTunaikan: (Double) -> Unit
) {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    var memberCountText by remember(calc) { mutableStateOf(calc.familyMemberCount.toString()) }
    var pricePerSoulText by remember(calc) { mutableStateOf(calc.ratePerSoulRupiah.toLong().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Result Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
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
                    Column {
                        Text(
                            text = "TOTAL ZAKAT FITRAH KELUARGA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Rp ${nf.format(calc.totalFitrahRupiah)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { onTunaikan(calc.totalFitrahRupiah) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("tunaikan_zakat_fitrah_btn")
                    ) {
                        Text("Tunaikan", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Beras: ${String.format(Locale.US, "%.1f", calc.totalRiceKg)} kg (${calc.familyMemberCount} Jiwa)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@ 2.5 kg atau Rp ${nf.format(calc.ratePerSoulRupiah)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text("Rincian Tanggungan Anggota Keluarga", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        OutlinedTextField(
            value = memberCountText,
            onValueChange = {
                memberCountText = it.filter { ch -> ch.isDigit() }
                val count = memberCountText.toIntOrNull() ?: 1
                onUpdate(calc.copy(familyMembersCount = count))
            },
            label = { Text("Jumlah Anggota Keluarga (Jiwa)") },
            placeholder = { Text("cth: 4 (Kepala Keluarga, Istri, 2 Anak)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pricePerSoulText,
            onValueChange = {
                pricePerSoulText = it.filter { ch -> ch.isDigit() }
                val rate = pricePerSoulText.toDoubleOrNull() ?: 45000.0
                onUpdate(calc.copy(cashRatePerPerson = rate))
            },
            label = { Text("Standar Rupiah per Jiwa (SK BAZNAS Wilayah)") },
            placeholder = { Text("cth: 45000") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("📌 Fiqh Zakat Fitrah", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zakat Fitrah diwajibkan bagi setiap muslim yang menjumpai tenggelamnya matahari di akhir Ramadhan dan memiliki kelebihan makanan untuk sehari semalam. Waktu terbaik menunaikannya adalah sebelum shalat Idul Fitri.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun DisburseZakatDialog(
    presetTitle: String,
    presetAmount: Double,
    wallets: List<com.example.core.wallet.WalletAccount>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, asnaf: AsnafCategory, accountId: String, recipient: String, notes: String, receipt: ReceiptAttachment?) -> Unit
) {
    var title by remember { mutableStateOf(presetTitle) }
    var amountText by remember { mutableStateOf(presetAmount.toLong().toString()) }
    var selectedAsnaf by remember { mutableStateOf(AsnafCategory.FAKIR_MISKIN) }
    var recipientName by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var notes by remember { mutableStateOf("") }

    var asnafExpanded by remember { mutableStateOf(false) }
    var walletExpanded by remember { mutableStateOf(false) }

    var attachReceipt by remember { mutableStateOf(false) }
    var receiptRef by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tunaikan & Salurkan Zakat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
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
                    label = { Text("Nama Program Penyaluran") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Zakat (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Asnaf Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAsnaf.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Golongan Mustahiq (8 Asnaf)") },
                        trailingIcon = {
                            IconButton(onClick = { asnafExpanded = !asnafExpanded }) {
                                Icon(
                                    imageVector = if (asnafExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = asnafExpanded,
                        onDismissRequest = { asnafExpanded = false }
                    ) {
                        AsnafCategory.entries.forEach { asnaf ->
                            DropdownMenuItem(
                                text = { Text(asnaf.displayName) },
                                onClick = {
                                    selectedAsnaf = asnaf
                                    asnafExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Nama Penerima / Lembaga Amil") },
                    placeholder = { Text("cth: BAZNAS, LAZISMU, Yayasan Yatim Dhuafa") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (wallets.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentWallet = wallets.firstOrNull { it.id == selectedWalletId }
                        OutlinedTextField(
                            value = currentWallet?.name ?: "Pilih Kantong Sumber",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dibayar dari Kantong") },
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
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Keterangan & Niat Penyaluran") },
                    placeholder = { Text("cth: Nawaitu an ukhrija zakata...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lampirkan Tanda Terima Amil", fontSize = 13.sp)
                    androidx.compose.material3.Switch(
                        checked = attachReceipt,
                        onCheckedChange = { attachReceipt = it }
                    )
                }

                if (attachReceipt) {
                    OutlinedTextField(
                        value = receiptRef,
                        onValueChange = { receiptRef = it },
                        label = { Text("No. Kuitansi BAZNAS/LAZ") },
                        placeholder = { Text("cth: KW-BAZNAS-2026-0912") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            val isValid = amount > 0.0 && recipientName.isNotBlank()

            Button(
                onClick = {
                    if (isValid) {
                        val wallet = wallets.firstOrNull { it.id == selectedWalletId }
                        val accountId = wallet?.linkedAccountId ?: "acc_cash"

                        val receipt = if (attachReceipt) {
                            ReceiptAttachment(
                                title = "Kuitansi Zakat: $title",
                                receiptType = ReceiptType.AMIL_ZAKAT_RECEIPT,
                                referenceNumber = receiptRef.ifBlank { "KW-ZAKAT-${System.currentTimeMillis() % 100000}" },
                                amount = amount,
                                notes = "Penyaluran zakat via $recipientName"
                            )
                        } else null

                        onConfirm(title, amount, selectedAsnaf, accountId, recipientName, notes, receipt)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Tunaikan Zakat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun ZakatMaalTab(
    calc: ZakatMaalCalculation,
    totalWalletBalance: Double,
    walletsCount: Int,
    ibadahGoals: List<com.example.core.ibadah.IbadahGoal>,
    onUpdate: (ZakatMaalCalculation) -> Unit,
    onSyncFromDatabase: () -> Unit,
    onTunaikan: (Double) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    var walletSavingsText by remember(calc.totalWalletSavings) { mutableStateOf(calc.totalWalletSavings.toLong().toString()) }
    var ibadahSavingsText by remember(calc.totalIbadahSavings) { mutableStateOf(calc.totalIbadahSavings.toLong().toString()) }
    var goldGramsText by remember(calc.goldWeightGrams) { mutableStateOf(if (calc.goldWeightGrams == 0.0) "" else calc.goldWeightGrams.toString()) }
    var receivablesText by remember(calc.tradeableReceivables) { mutableStateOf(calc.tradeableReceivables.toLong().toString()) }
    var liabilitiesText by remember(calc.shortTermLiabilities) { mutableStateOf(calc.shortTermLiabilities.toLong().toString()) }

    val activeWalletsTotal = totalWalletBalance.coerceAtLeast(0.0)
    val totalIbadahAccumulated = ibadahGoals.sumOf { it.currentAccumulated }.coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Result Status Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (calc.isObligatory) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                calc.isObligatory -> "WAJIB ZAKAT MAAL (${if (calc.isLunarCalendar) "2.5% Hijriyah" else "2.577% Masehi"})"
                                !calc.isNisabReached -> "BELUM MENCAPAI NISAB"
                                else -> "BELUM GENAP 1 TAHUN (HAUL)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (calc.isObligatory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rp ${nf.format(calc.zakatAmount)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (calc.isObligatory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (calc.isObligatory) {
                        Button(
                            onClick = {
                                AmanahHapticEngine.performButtonClick(haptic, context)
                                onTunaikan(calc.zakatAmount)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("tunaikan_zakat_maal_btn")
                        ) {
                            Text("Tunaikan", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Harta Bersih:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Rp ${nf.format(calc.netTaxableAssets)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Ambang Nisab (85g Emas):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Rp ${nf.format(calc.nisabGold85g)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (!calc.isNisabReached) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kurang Rp ${nf.format(calc.shortageToNisab)} lagi untuk mencapai syarat hisab nisab 85g emas.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                } else if (!calc.isHaulFulfilled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nisab telah tercapai. Kewajiban zakat aktif setelah harta disimpan selama 1 tahun haul penuh.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 2. Database Auto-Sync Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Integrasi Aset & Tabungan Database",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Menghitung zakat dari saldo riil dompet kas & tabungan Room DB",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Kas/Rekening di Database ($walletsCount akun):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${nf.format(activeWalletsTotal)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("• Tabungan Target Ibadah (${ibadahGoals.size} program):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${nf.format(totalIbadahAccumulated)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        if (calc.lastSyncedFromDatabaseMillis > 0L) {
                            Text(
                                text = "Terakhir disinkronkan: ${sdf.format(Date(calc.lastSyncedFromDatabaseMillis))}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        AmanahHapticEngine.performButtonClick(haptic, context)
                        onSyncFromDatabase()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_zakat_maal_db_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sinkronkan", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sinkronkan dari Kas & Tabungan Database", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Asset Breakdown Fields (Rincian Aset Harta Simpanan)
        Text("Rincian Harta Simpanan & Aset:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

        // Saldo Tabungan / Kas Bank
        OutlinedTextField(
            value = walletSavingsText,
            onValueChange = { input ->
                walletSavingsText = input.filter { it.isDigit() }
                val v = walletSavingsText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(totalWalletSavings = v))
            },
            label = { Text("1. Saldo Kas & Tabungan Bank (Rp)") },
            supportingText = { Text("Uang tunai fisik, saldo rekening tabungan/giro/e-wallet") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("zakat_maal_wallet_savings_input")
        )

        // Tabungan Khusus / Target Ibadah
        OutlinedTextField(
            value = ibadahSavingsText,
            onValueChange = { input ->
                ibadahSavingsText = input.filter { it.isDigit() }
                val v = ibadahSavingsText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(totalIbadahSavings = v))
            },
            label = { Text("2. Dana Tabungan Terencana & Ibadah (Rp)") },
            supportingText = { Text("Tabungan qurban, haji, umroh, dan tabungan khusus yang tersimpan") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("zakat_maal_ibadah_savings_input")
        )

        // Logam Mulia / Emas Simpanan
        OutlinedTextField(
            value = goldGramsText,
            onValueChange = { input ->
                goldGramsText = input.filter { it.isDigit() || it == '.' }
                val v = goldGramsText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(goldWeightGrams = v))
            },
            label = { Text("3. Simpanan Emas / Logam Mulia (Gram)") },
            supportingText = {
                val goldVal = (goldGramsText.toDoubleOrNull() ?: 0.0) * calc.goldPricePerGram
                Text("Setara Rp ${nf.format(goldVal)} (Acuan: Rp ${nf.format(calc.goldPricePerGram)}/g)")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("zakat_maal_gold_input")
        )

        // Piutang Lancar
        OutlinedTextField(
            value = receivablesText,
            onValueChange = { input ->
                receivablesText = input.filter { it.isDigit() }
                val v = receivablesText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(tradeableReceivables = v))
            },
            label = { Text("4. Piutang Lancar yang Berpotensi Kembali (Rp)") },
            supportingText = { Text("Pinjaman kebajikan (Qardh Diberikan) yang diharapkan segera lunas") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("zakat_maal_receivables_input")
        )

        // Pengurang: Hutang Jatuh Tempo
        OutlinedTextField(
            value = liabilitiesText,
            onValueChange = { input ->
                liabilitiesText = input.filter { it.isDigit() }
                val v = liabilitiesText.toDoubleOrNull() ?: 0.0
                onUpdate(calc.copy(shortTermLiabilities = v))
            },
            label = { Text("5. Pengurang: Hutang Jatuh Tempo (Rp)") },
            supportingText = { Text("Kewajiban hutang jangka pendek yang harus dibayar dalam tahun berjalan") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("zakat_maal_liabilities_input")
        )

        // 4. Shariah Settings (Haul & Kalender)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Ketentuan & Parameter Syariah:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Telah Mencapai Haul (1 Tahun)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Harta simpanan telah genap tersimpan selama 1 tahun penuh tanpa berkurang di bawah nisab.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    androidx.compose.material3.Switch(
                        checked = calc.isHaulFulfilled,
                        onCheckedChange = {
                            AmanahHapticEngine.performKeyClick(haptic, context)
                            onUpdate(calc.copy(isHaulFulfilled = it))
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (calc.isLunarCalendar) "Metode Hisab: Kalender Hijriyah (2.5%)" else "Metode Hisab: Kalender Masehi (2.577%)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (calc.isLunarCalendar) "Tahun Qamariyah (354 hari) dengan tarif standar 2.5%."
                            else "Tahun Syamsiyah (365 hari) disesuaikan 2.577% sesuai fatwa hisab BAZNAS RI.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = calc.isLunarCalendar,
                        onCheckedChange = {
                            AmanahHapticEngine.performKeyClick(haptic, context)
                            onUpdate(calc.copy(isLunarCalendar = it))
                        }
                    )
                }
            }
        }

        // 5. Landasan Syariah Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Kaidah Fiqih Zakat Maal:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Nisab Zakat Maal (simpanan kas, tabungan, perhiasan emas/perak) adalah setara 85 gram emas murni.\n" +
                            "2. Harta dikenakan zakat sebesar 2.5% (Hijriyah) atau 2.577% (Masehi) apabila telah genap mencapai haul (1 tahun kepemilikan).\n" +
                            "3. Tabungan yang dialokasikan untuk kebutuhan ibadah (qurban/haji) jika masih mengendap dan genap haul tetap dihitung sebagai aset maal hingga saat direalisasikan.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun TathhirCleansingTab(
    viewModel: AmanahLedgerViewModel,
    quarantineBalance: Double,
    wallets: List<WalletAccount>
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    val state by viewModel.uiState.collectAsState()

    var amountInput by remember { mutableStateOf(if (quarantineBalance > 0) quarantineBalance.toLong().toString() else "") }
    var selectedPurpose by remember { mutableStateOf("Fasilitas MCK & Sanitasi Umum") }
    var customPurpose by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "acc_cash") }
    var notesInput by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val purposeOptions = listOf(
        "Fasilitas MCK & Sanitasi Umum",
        "Perbaikan Jalan / Jembatan Rusak Umum",
        "Pengadaan Sarana Air Bersih Dhuafa",
        "Penerangan Jalan Lingkungan Pedesaan",
        "Pengelolaan Tempat Pembuangan Sampah",
        "Kebutuhan Sosial Maslahat Lainnya"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Saldo Syubhat
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (quarantineBalance > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (quarantineBalance > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo Terindikasi Syubhat / Bunga",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (quarantineBalance > 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = if (quarantineBalance > 0) "Perlu Pembersihan" else "Bersih",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rp ${nf.format(quarantineBalance)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (quarantineBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Akun Karantina Syubhat (acc_syubhat). Dana non-halal wajib dikeluarkan untuk fasilitas umum.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Edukasi Fatwa Syariah
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Pedoman Syariah Tathhir (Pembersihan Dana Non-Halal):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Berdasarkan Fatwa DSN-MUI No. 123/DSN-MUI/XI/2018: Bunga bank atau penghasilan non-halal tidak boleh dimanfaatkan untuk kepentingan pribadi atau keluarga.\n" +
                            "• Dana ini wajib dibersihkan dengan cara disalurkan 100% untuk kemaslahatan sarana publik atau fasilitas sosial.\n" +
                            "• Penyaluran ini berstatus pembersihan (bara'ah adz-dzimmah), bukan sedekah berpahala kebajikan.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        // Formulir Penyaluran Tathhir
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Formulir Pembersihan (Tathhir)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Input Nominal
                Text(
                    text = "Nominal Dana Non-Halal (Rp)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tathhir_amount_input"),
                    placeholder = { Text("Contoh: 150000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                if (quarantineBalance > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            amountInput = quarantineBalance.toLong().toString()
                            AmanahHapticEngine.performKeyClick(haptic, context)
                        },
                        modifier = Modifier.testTag("tathhir_clean_all_btn")
                    ) {
                        Text(
                            "Bersihkan Semua Saldo Syubhat (Rp ${nf.format(quarantineBalance)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pilih Tujuan Kemaslahatan
                Text(
                    text = "Tujuan Kemaslahatan Sarana Publik",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))

                purposeOptions.forEach { opt ->
                    val isSelected = selectedPurpose == opt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedPurpose = opt
                                AmanahHapticEngine.performKeyClick(haptic, context)
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = isSelected,
                            onClick = {
                                selectedPurpose = opt
                                AmanahHapticEngine.performKeyClick(haptic, context)
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = opt,
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (selectedPurpose.contains("Lainnya")) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customPurpose,
                        onValueChange = { customPurpose = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tathhir_custom_purpose_input"),
                        placeholder = { Text("Tuliskan fasilitas publik tujuan...") },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dompet Sumber Dana
                Text(
                    text = "Dompet / Rekening Sumber Dana",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wallets.take(3).forEach { wallet ->
                        val isSelected = selectedWalletId == wallet.id
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedWalletId = wallet.id
                                    AmanahHapticEngine.performKeyClick(haptic, context)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = wallet.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Rp ${nf.format(state.getWalletBalance(wallet.id))}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Catatan
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Catatan tambahan (mis. Bunga Bank Konvensional...") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Eksekusi
                val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        showConfirmDialog = true
                    },
                    enabled = amountDouble > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("tathhir_submit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Eksekusi Penyaluran Tathhir (Rp ${nf.format(amountDouble)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        val amountDouble = amountInput.toDoubleOrNull() ?: 0.0
        val finalPurpose = if (selectedPurpose.contains("Lainnya") && customPurpose.isNotBlank()) customPurpose else selectedPurpose

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Tathhir Dana Non-Halal") },
            text = {
                Text(
                    "Anda akan menyalurkan dana sebesar Rp ${nf.format(amountDouble)} untuk: $finalPurpose.\n\n" +
                            "Penyaluran ini dicatat sebagai pelepasan dana non-halal/syubhat untuk kemaslahatan umum sesuai Fatwa DSN-MUI No. 123/2018."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disburseTathhirFund(
                            amount = amountDouble,
                            publicPurpose = finalPurpose,
                            fromWalletId = selectedWalletId,
                            notes = notesInput
                        )
                        showConfirmDialog = false
                        amountInput = ""
                        customPurpose = ""
                        notesInput = ""
                        AmanahHapticEngine.performButtonClick(haptic, context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Salurkan Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.infaq.AsnafCategory
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.state.AmanahLedgerViewModel
import com.example.core.zakat.ZakatFitrahFamilyCalculation
import com.example.core.zakat.ZakatPerniagaanCalculation
import com.example.core.zakat.ZakatProfesiCalculation
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatHubScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))

    var selectedTab by remember { mutableStateOf(0) }
    var showDisburseModal by remember { mutableStateOf(false) }
    var disburseAmountPreset by remember { mutableStateOf(0.0) }
    var disburseTitlePreset by remember { mutableStateOf("Zakat Maal") }

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
                            text = "Zakat Profesi, Perniagaan (Tijarah), & Fitrah",
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
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Zakat Profesi", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Perniagaan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Zakat Fitrah", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> ZakatProfesiTab(
                        calc = state.zakatProfesi,
                        onUpdate = { viewModel.updateZakatProfesi(it) },
                        onTunaikan = { amt ->
                            disburseAmountPreset = amt
                            disburseTitlePreset = "Zakat Penghasilan / Profesi"
                            showDisburseModal = true
                        }
                    )
                    1 -> ZakatPerniagaanTab(
                        calc = state.zakatPerniagaan,
                        onUpdate = { viewModel.updateZakatPerniagaan(it) },
                        onTunaikan = { amt ->
                            disburseAmountPreset = amt
                            disburseTitlePreset = "Zakat Perniagaan (Tijarah)"
                            showDisburseModal = true
                        }
                    )
                    2 -> ZakatFitrahTab(
                        calc = state.zakatFitrah,
                        onUpdate = { viewModel.updateZakatFitrah(it) },
                        onTunaikan = { amt ->
                            disburseAmountPreset = amt
                            disburseTitlePreset = "Zakat Fitrah Keluarga (${state.zakatFitrah.familyMemberCount} Jiwa)"
                            showDisburseModal = true
                        }
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
                containerColor = if (calc.isObligated) EmeraldPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
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
                            color = if (calc.isObligated) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rp ${nf.format(calc.zakatPayableAmount)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (calc.isObligated) EmeraldLight else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (calc.isObligated) {
                        Button(
                            onClick = { onTunaikan(calc.zakatPayableAmount) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
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
                containerColor = if (calc.isObligated) GoldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
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
                            color = if (calc.isObligated) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rp ${nf.format(calc.zakatPayableAmount)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = if (calc.isObligated) GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (calc.isObligated) {
                        Button(
                            onClick = { onTunaikan(calc.zakatPayableAmount) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
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
                containerColor = EmeraldPrimary.copy(alpha = 0.12f)
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
                            color = EmeraldLight
                        )
                        Text(
                            text = "Rp ${nf.format(calc.totalFitrahRupiah)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldLight
                        )
                    }

                    Button(
                        onClick = { onTunaikan(calc.totalFitrahRupiah) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
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
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = EmeraldLight)
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
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
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

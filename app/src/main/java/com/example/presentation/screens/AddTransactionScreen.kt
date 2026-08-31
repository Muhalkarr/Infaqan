package com.example.presentation.screens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.core.camera.CameraXReceiptScanner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.AccountCategory
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.ocr.SmartReceiptParser
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.White12
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AmanahLedgerViewModel,
    editEntryId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val existingEntry = remember(editEntryId, state.journalEntries) {
        if (editEntryId != null) viewModel.getJournalEntry(editEntryId) else null
    }
    val isEditMode = existingEntry != null

    // Extract values if editing
    val initialIsIncome = remember(existingEntry) {
        existingEntry?.transactionType != "EXPENSE"
    }
    val initialDate = remember(existingEntry) {
        existingEntry?.gregorianDate ?: Date()
    }
    val initialAmountText = remember(existingEntry) {
        if (existingEntry == null) ""
        else {
            if (existingEntry.transactionType == "INFLOW") {
                val gross = existingEntry.lines.firstOrNull { it.accountId in listOf("acc_cash", "acc_bank", "acc_gold") }?.debit
                    ?: existingEntry.lines.firstOrNull { it.accountId in listOf("acc_salary", "acc_trade", "acc_gift", "acc_rikaz", "acc_syubhat") }?.credit
                    ?: 0.0
                if (gross > 0) {
                    if (gross % 1.0 == 0.0) gross.toLong().toString() else gross.toString()
                } else ""
            } else {
                val exp = existingEntry.lines.firstOrNull { it.debit > 0 && it.accountId != "acc_disbursed" }?.debit ?: 0.0
                if (exp > 0) {
                    if (exp % 1.0 == 0.0) exp.toLong().toString() else exp.toString()
                } else ""
            }
        }
    }
    val initialDesc = remember(existingEntry) {
        existingEntry?.description?.replace(Regex("""\s*\(Round-up:.*?\)$"""), "") ?: ""
    }
    val initialCategory = remember(existingEntry) {
        if (existingEntry == null) "acc_salary"
        else {
            if (existingEntry.transactionType == "INFLOW") {
                existingEntry.lines.firstOrNull { it.credit > 0 && it.accountId in listOf("acc_salary", "acc_trade", "acc_gift", "acc_rikaz", "acc_syubhat") }?.accountId ?: "acc_salary"
            } else {
                existingEntry.lines.firstOrNull { it.debit > 0 && it.accountId != "acc_disbursed" }?.accountId ?: "acc_living"
            }
        }
    }
    val initialAsset = remember(existingEntry) {
        if (existingEntry == null) "acc_bank"
        else {
            if (existingEntry.transactionType == "INFLOW") {
                existingEntry.lines.firstOrNull { it.debit > 0 && it.accountId in listOf("acc_cash", "acc_bank", "acc_gold") }?.accountId ?: "acc_bank"
            } else {
                existingEntry.lines.firstOrNull { it.credit > 0 && it.accountId in listOf("acc_cash", "acc_bank", "acc_gold") }?.accountId ?: "acc_cash"
            }
        }
    }
    val initialRate = remember(existingEntry) {
        if (existingEntry != null && existingEntry.transactionType == "INFLOW") {
            val gross = existingEntry.lines.firstOrNull { it.accountId in listOf("acc_cash", "acc_bank", "acc_gold") }?.debit ?: 0.0
            val infaq = existingEntry.lines.firstOrNull { it.accountId == "acc_vault" }?.credit ?: 0.0
            if (gross > 0) (infaq / gross) else 0.05
        } else 0.05
    }
    val initialEnableRoundUp = remember(existingEntry) {
        if (existingEntry != null && existingEntry.transactionType == "EXPENSE") {
            (existingEntry.lines.firstOrNull { it.accountId == "acc_vault" }?.credit ?: 0.0) > 0.0
        } else true
    }

    var isIncome by remember(initialIsIncome) { mutableStateOf(initialIsIncome) }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var amountText by remember(initialAmountText) { mutableStateOf(initialAmountText) }
    var descriptionText by remember(initialDesc) { mutableStateOf(initialDesc) }

    // Dropdown States
    var selectedCategoryAccountId by remember(initialCategory) { mutableStateOf(initialCategory) }
    var selectedAssetAccountId by remember(initialAsset) { mutableStateOf(initialAsset) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var assetDropdownExpanded by remember { mutableStateOf(false) }

    // Infaq options for income
    var infaqRate by remember(initialRate) { mutableDoubleStateOf(initialRate) } // 5%

    // Round-up options for expense
    var enableRoundUp by remember(initialEnableRoundUp) { mutableStateOf(initialEnableRoundUp) }
    var roundUpStep by remember { mutableDoubleStateOf(5000.0) }

    // Receipt Attachment State
    var attachReceipt by remember { mutableStateOf(existingEntry?.receiptAttachment != null) }
    var receiptMerchant by remember { mutableStateOf(existingEntry?.receiptAttachment?.merchantName ?: "") }
    var receiptRefNumber by remember { mutableStateOf(existingEntry?.receiptAttachment?.referenceNumber ?: "") }
    var receiptType by remember { mutableStateOf(existingEntry?.receiptAttachment?.receiptType ?: ReceiptType.STORE_RECEIPT) }
    var receiptNotes by remember { mutableStateOf(existingEntry?.receiptAttachment?.notes ?: "") }
    var receiptTypeDropdownExpanded by remember { mutableStateOf(false) }

    // Error & Warning Dialogs
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOverBudgetWarningDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showOcrScannerDialog by remember { mutableStateOf(false) }
    var showLiveCameraScanner by remember { mutableStateOf(false) }

    val hijriDate = remember(selectedDate) {
        HijriCalendarEngine.fromGregorian(selectedDate)
    }
    val isFriday = remember(selectedDate) {
        HijriCalendarEngine.isFriday(selectedDate)
    }

    val amountValue = amountText.toDoubleOrNull() ?: 0.0

    // Auto-adjust infaq rate when special category is picked
    val calculatedInfaqPreview by remember(selectedCategoryAccountId, amountValue, infaqRate, isIncome, enableRoundUp, roundUpStep) {
        derivedStateOf {
            if (isIncome) {
                when (selectedCategoryAccountId) {
                    "acc_rikaz" -> amountValue * 0.20
                    "acc_syubhat" -> amountValue * 1.00
                    else -> amountValue * infaqRate
                }
            } else {
                if (enableRoundUp && roundUpStep > 0.0 && amountValue > 0.0) {
                    val rem = amountValue % roundUpStep
                    if (rem > 0.0) roundUpStep - rem else 0.0
                } else 0.0
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val budgetForCategory = if (!isIncome) state.budgets.firstOrNull { it.accountId == selectedCategoryAccountId } else null
    val currentSpent = if (!isIncome) state.getMonthlySpentForAccount(selectedCategoryAccountId) else 0.0
    val willExceedBudget = budgetForCategory != null && (currentSpent + amountValue) > budgetForCategory.monthlyLimit

    fun executeSaveTransaction() {
        val desc = descriptionText.ifBlank {
            if (isIncome) "Pemasukan Rezeki Halal" else "Pengeluaran Konsumsi"
        }

        val receipt = if (attachReceipt) {
            ReceiptAttachment(
                title = if (receiptMerchant.isNotBlank()) "Bukti: $receiptMerchant" else "Bukti Transaksi $desc",
                receiptType = receiptType,
                referenceNumber = receiptRefNumber.ifBlank { "KW-${System.currentTimeMillis() % 100000}" },
                amount = amountValue,
                merchantName = receiptMerchant,
                notes = receiptNotes,
                isDigitalVerified = true
            )
        } else null

        if (isIncome) {
            val rateToUse = when (selectedCategoryAccountId) {
                "acc_rikaz" -> 0.20
                "acc_syubhat" -> 1.00
                else -> infaqRate
            }
            if (isEditMode && editEntryId != null) {
                viewModel.updateIncome(
                    entryId = editEntryId,
                    grossAmount = amountValue,
                    incomeAccountId = selectedCategoryAccountId,
                    depositAccountId = selectedAssetAccountId,
                    customInfaqRate = rateToUse,
                    description = desc,
                    date = selectedDate,
                    receiptAttachment = receipt
                )
            } else {
                viewModel.recordIncome(
                    grossAmount = amountValue,
                    incomeAccountId = selectedCategoryAccountId,
                    depositAccountId = selectedAssetAccountId,
                    customInfaqRate = rateToUse,
                    description = desc,
                    date = selectedDate,
                    receiptAttachment = receipt
                )
            }
        } else {
            if (isEditMode && editEntryId != null) {
                viewModel.updateExpense(
                    entryId = editEntryId,
                    amount = amountValue,
                    expenseAccountId = selectedCategoryAccountId,
                    fromAccountId = selectedAssetAccountId,
                    enableRoundUp = enableRoundUp,
                    roundUpStep = roundUpStep,
                    description = desc,
                    date = selectedDate,
                    receiptAttachment = receipt
                )
            } else {
                viewModel.recordExpense(
                    amount = amountValue,
                    expenseAccountId = selectedCategoryAccountId,
                    fromAccountId = selectedAssetAccountId,
                    enableRoundUp = enableRoundUp,
                    roundUpStep = roundUpStep,
                    description = desc,
                    date = selectedDate,
                    receiptAttachment = receipt
                )
            }
        }
        onNavigateBack()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0E1A1C)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
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
                            text = if (isEditMode) "Ubah Transaksi Syariah" else "Catat Transaksi Berpasangan",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (isEditMode) {
                            Text(
                                text = "Mode Modifikasi Jurnal & Neraca",
                                fontSize = 11.sp,
                                color = GoldAccent
                            )
                        }
                    }
                },
                actions = {
                    if (isEditMode && editEntryId != null) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("delete_transaction_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Transaksi",
                                tint = ExpenseCoral
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Segmented Control: Pemasukan (Rezeki) vs Pengeluaran (Konsumsi)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIncome) EmeraldPrimary else Color.Transparent)
                        .clickable {
                            isIncome = true
                            selectedCategoryAccountId = "acc_salary"
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pemasukan (Rezeki)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) Color.White else Color.White60
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isIncome) ExpenseCoral else Color.Transparent)
                        .clickable {
                            isIncome = false
                            selectedCategoryAccountId = "acc_living"
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pengeluaran (Konsumsi)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isIncome) Color.White else Color.White60
                    )
                }
            }

            // Quick OCR Receipt Scanner Button
            FilledTonalButton(
                onClick = { showOcrScannerDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_ocr_scanner_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📸 Pindai Struk & Mutasi Otomatis (OCR)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldLight
                )
            }

            // 2. Dual-Calendar Temporal Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { time = selectedDate }
                        DatePickerDialog(
                            context,
                            { _, y, m, d ->
                                val newCal = Calendar.getInstance().apply {
                                    set(y, m, d)
                                }
                                selectedDate = newCal.time
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pilih Tanggal",
                        tint = EmeraldLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Masehi: ${dateFormat.format(selectedDate)}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hijriah: $hijriDate (Batas Maghrib 18:00)",
                            color = GoldAccent,
                            fontSize = 11.sp
                        )
                    }
                    if (isFriday) {
                        Text(
                            text = "Jumat Berkah",
                            fontSize = 10.sp,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { char -> char.isDigit() }
                    errorMessage = null
                },
                label = { Text("Nominal Transaksi (Rp)", color = Color.White70) },
                prefix = { Text("Rp ", color = GoldAccent, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )

            // 4. Category Dropdown
            val availableCategories = if (isIncome) {
                state.accounts.filter {
                    it.category == AccountCategory.INCOME_KASAB || it.category == AccountCategory.INCOME_NON_KASAB
                }
            } else {
                state.accounts.filter { it.category == AccountCategory.EXPENSE }
            }

            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
            ) {
                val currentCategory = state.getAccount(selectedCategoryAccountId)
                OutlinedTextField(
                    value = currentCategory?.name ?: "Pilih Kategori",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori Akun", color = Color.White70) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("category_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    availableCategories.forEach { acc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(acc.name, color = Color.White, fontSize = 13.sp)
                                    Text(acc.description, color = Color.White60, fontSize = 10.sp)
                                }
                            },
                            onClick = {
                                selectedCategoryAccountId = acc.id
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 5. Asset Account (Kas / Bank / Emas)
            val assetAccounts = state.accounts.filter { it.category == AccountCategory.ASSET }
            ExposedDropdownMenuBox(
                expanded = assetDropdownExpanded,
                onExpandedChange = { assetDropdownExpanded = !assetDropdownExpanded }
            ) {
                val currentAsset = state.getAccount(selectedAssetAccountId)
                OutlinedTextField(
                    value = currentAsset?.name ?: "Pilih Akun Aset",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isIncome) "Masuk ke Rekening/Kas" else "Sumber Dana (Kas/Bank)", color = Color.White70) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("asset_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = assetDropdownExpanded,
                    onDismissRequest = { assetDropdownExpanded = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    assetAccounts.forEach { acc ->
                        DropdownMenuItem(
                            text = { Text(acc.name, color = Color.White, fontSize = 13.sp) },
                            onClick = {
                                selectedAssetAccountId = acc.id
                                assetDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 6. Infaq & Purification Settings Card
            if (isIncome) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alokasi Infaq Hak Mustahiq",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            if (selectedCategoryAccountId == "acc_rikaz") {
                                Text("Wajib 20% (Rikaz)", fontSize = 11.sp, color = EmeraldLight)
                            } else if (selectedCategoryAccountId == "acc_syubhat") {
                                Text("Wajib 100% (Syubhat)", fontSize = 11.sp, color = ExpenseCoral)
                            } else {
                                Text("${String.format(Locale.US, "%.1f", infaqRate * 100).removeSuffix(".0")}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                            }
                        }

                        if (selectedCategoryAccountId != "acc_rikaz" && selectedCategoryAccountId != "acc_syubhat") {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Preset percentage chips
                            Text("Pilihan Persentase Infaq:", fontSize = 11.sp, color = Color.White70)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    0.0 to "0%",
                                    0.025 to "2.5%",
                                    0.05 to "5%",
                                    0.10 to "10%",
                                    0.20 to "20%"
                                ).forEach { (rate, label) ->
                                    val isSelected = Math.abs(infaqRate - rate) < 0.001
                                    androidx.compose.material3.Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) EmeraldPrimary else Color(0xFF0E1A1C),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldAccent else DarkBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { infaqRate = rate }
                                            .testTag("infaq_preset_${label}")
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color.White70
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Custom Percentage Slider & Direct Input Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Persentase Kustom:", fontSize = 11.sp, color = Color.White70, modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = if (infaqRate == 0.0) "0" else String.format(Locale.US, "%.1f", infaqRate * 100).removeSuffix(".0"),
                                    onValueChange = { input ->
                                        val clean = input.filter { it.isDigit() || it == '.' }
                                        val parsed = clean.toDoubleOrNull()
                                        if (parsed != null) {
                                            infaqRate = (parsed.coerceIn(0.0, 100.0)) / 100.0
                                        } else if (clean.isEmpty()) {
                                            infaqRate = 0.0
                                        }
                                    },
                                    trailingIcon = { Text("%", color = GoldAccent, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = DarkBorder,
                                        focusedContainerColor = Color(0xFF0E1A1C),
                                        unfocusedContainerColor = Color(0xFF0E1A1C)
                                    ),
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(48.dp)
                                        .testTag("custom_infaq_percent_input")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Slider(
                                value = infaqRate.toFloat(),
                                onValueChange = { infaqRate = (Math.round(it * 200.0) / 200.0).coerceIn(0.0, 1.0) },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldLight,
                                    activeTrackColor = EmeraldPrimary,
                                    inactiveTrackColor = DarkBorder
                                ),
                                modifier = Modifier.testTag("infaq_slider")
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val netAmount = (amountValue - calculatedInfaqPreview).coerceAtLeast(0.0)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Infaq dialokasikan ke Vault:",
                                fontSize = 11.sp,
                                color = Color.White70
                            )
                            Text(
                                text = "Rp ${formatRupiah(calculatedInfaqPreview)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                        if (amountValue > 0.0) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bersih Masuk Rekening:",
                                    fontSize = 11.sp,
                                    color = Color.White60
                                )
                                Text(
                                    text = "Rp ${formatRupiah(netAmount)}",
                                    fontSize = 11.sp,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }
                }
            } else {
                // Live Budget Allocation Status Card for selected Expense Account
                val budgetForCategory = state.budgets.firstOrNull { it.accountId == selectedCategoryAccountId }
                if (budgetForCategory != null) {
                    val currentSpent = state.getMonthlySpentForAccount(selectedCategoryAccountId)
                    val projectedSpent = currentSpent + amountValue
                    val isWillBeOver = projectedSpent > budgetForCategory.monthlyLimit
                    val remainingAfter = (budgetForCategory.monthlyLimit - projectedSpent).coerceAtLeast(0.0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWillBeOver) Color(0xFF2A1515) else Color(0xFF102421)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isWillBeOver) ExpenseCoral.copy(alpha = 0.7f) else DarkBorder
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Anggaran: ${budgetForCategory.categoryName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWillBeOver) ExpenseCoral else GoldAccent
                                )
                                Text(
                                    text = "Batas: Rp ${formatRupiah(budgetForCategory.monthlyLimit)}",
                                    fontSize = 11.sp,
                                    color = Color.White60
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Terpakai: Rp ${formatRupiah(currentSpent)} -> Rp ${formatRupiah(projectedSpent)}",
                                    fontSize = 11.sp,
                                    color = Color.White70
                                )
                                Text(
                                    text = if (isWillBeOver) {
                                        "Melebihi Rp ${formatRupiah(projectedSpent - budgetForCategory.monthlyLimit)}"
                                    } else {
                                        "Sisa: Rp ${formatRupiah(remainingAfter)}"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWillBeOver) ExpenseCoral else EmeraldLight
                                )
                            }
                        }
                    }
                }

                // Micro-infaq round-up for expense
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Round-Up Micro Infaq Belanja",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                                Text(
                                    text = "Bulatkan belanjaan ke kelipatan Rp ${formatRupiah(roundUpStep)}",
                                    fontSize = 10.sp,
                                    color = Color.White60
                                )
                            }
                            Switch(
                                checked = enableRoundUp,
                                onCheckedChange = { enableRoundUp = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldLight,
                                    checkedTrackColor = EmeraldDark
                                ),
                                modifier = Modifier.testTag("roundup_switch")
                            )
                        }

                        if (enableRoundUp && calculatedInfaqPreview > 0.0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sedekah pembulatan otomatis: Rp ${formatRupiah(calculatedInfaqPreview)}",
                                fontSize = 12.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 7. Description Field
            OutlinedTextField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = { Text("Keterangan Transaksi", color = Color.White70) },
                placeholder = { Text("Contoh: Gaji freelance, Belanja dapur", color = Color.White38) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input")
            )

            // 7b. Bukti Transaksi & Kuitansi Digital (Receipt Attachment)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (attachReceipt) EmeraldPrimary.copy(alpha = 0.5f) else DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🧾 Bukti Transaksi / Kuitansi Digital",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (attachReceipt) EmeraldLight else Color.White
                            )
                            Text(
                                text = "Lampirkan nota, bukti transfer, atau struk QRIS",
                                fontSize = 10.sp,
                                color = Color.White60
                            )
                        }
                        Switch(
                            checked = attachReceipt,
                            onCheckedChange = { attachReceipt = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldLight,
                                checkedTrackColor = EmeraldDark
                            ),
                            modifier = Modifier.testTag("attach_receipt_switch")
                        )
                    }

                    if (attachReceipt) {
                        Spacer(modifier = Modifier.height(10.dp))

                        ExposedDropdownMenuBox(
                            expanded = receiptTypeDropdownExpanded,
                            onExpandedChange = { receiptTypeDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = receiptType.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipe Bukti", color = Color.White70) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = receiptTypeDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkBackground,
                                    unfocusedContainerColor = DarkBackground
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = receiptTypeDropdownExpanded,
                                onDismissRequest = { receiptTypeDropdownExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                ReceiptType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.displayName, color = Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            receiptType = type
                                            receiptTypeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = receiptMerchant,
                            onValueChange = { receiptMerchant = it },
                            label = { Text("Nama Toko / Merchant / Pihak Kedua", color = Color.White70) },
                            placeholder = { Text("cth: Toko Berkah Barakah, BAZNAS, PT PLN", color = Color.White38) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("receipt_merchant_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = receiptRefNumber,
                            onValueChange = { receiptRefNumber = it },
                            label = { Text("No. Resi / Invoice / Ref Transaksi", color = Color.White70) },
                            placeholder = { Text("cth: INV/2026/08/99812", color = Color.White38) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkBackground,
                                unfocusedContainerColor = DarkBackground
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("receipt_ref_input")
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFEF5350),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 8. Submit Button
            Button(
                onClick = {
                    if (amountValue <= 0.0) {
                        errorMessage = "Masukkan nominal transaksi yang lebih besar dari 0"
                        return@Button
                    }

                    if (!isIncome && willExceedBudget) {
                        showOverBudgetWarningDialog = true
                    } else {
                        executeSaveTransaction()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) EmeraldPrimary else ExpenseCoral,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_transaction_button")
            ) {
                Icon(if (isEditMode) Icons.Default.Edit else Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditMode) "Simpan Perubahan Transaksi" else "Posting Jurnal Berpasangan (Balance)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isEditMode && editEntryId != null) {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ExpenseCoral
                    ),
                    border = BorderStroke(1.dp, ExpenseCoral.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_transaction_bottom_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseCoral)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Transaksi (Pulihkan Saldo)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseCoral
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog && editEntryId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = Color(0xFF1E1414),
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = ExpenseCoral,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Transaksi Jurnal Ini?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tindakan ini akan menghapus transaksi dari buku besar syariah. Seluruh saldo kas/bank dan kewajiban vault infaq terkait akan otomatis dikembalikan ke posisi semula.",
                        fontSize = 13.sp,
                        color = Color.White70,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "⚠️ Catatan: Penghapusan bersifat permanen.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldAccent
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteJournalEntry(editEntryId)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseCoral),
                    modifier = Modifier.testTag("confirm_delete_dialog_button")
                ) {
                    Text("Ya, Hapus Transaksi", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_delete_dialog_button")
                ) {
                    Text("Batal", color = Color.White70)
                }
            },
            modifier = Modifier.testTag("delete_confirmation_dialog")
        )
    }

    if (showOverBudgetWarningDialog && budgetForCategory != null) {
        val projectedTotal = currentSpent + amountValue
        val excessAmount = projectedTotal - budgetForCategory.monthlyLimit

        AlertDialog(
            onDismissRequest = { showOverBudgetWarningDialog = false },
            containerColor = Color(0xFF1E1414),
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ExpenseCoral,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Peringatan: Anggaran Terlampaui!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Transaksi ini akan menyebabkan pos pengeluaran '${budgetForCategory.categoryName}' melampaui batas anggaran bulanan yang telah ditentukan.",
                        fontSize = 13.sp,
                        color = Color.White70
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1616)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Batas Anggaran:", fontSize = 12.sp, color = Color.White60)
                                Text("Rp ${formatRupiah(budgetForCategory.monthlyLimit)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pengeluaran Saat Ini:", fontSize = 12.sp, color = Color.White60)
                                Text("Rp ${formatRupiah(currentSpent)}", fontSize = 12.sp, color = Color.White)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nominal Transaksi Ini:", fontSize = 12.sp, color = Color.White60)
                                Text("+Rp ${formatRupiah(amountValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseCoral)
                            }
                            androidx.compose.material3.HorizontalDivider(color = DarkBorder)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Proyeksi:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White70)
                                Text("Rp ${formatRupiah(projectedTotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseCoral)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Melebihi Kuota Sebesar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseCoral)
                                Text("Rp ${formatRupiah(excessAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ExpenseCoral)
                            }
                        }
                    }

                    Text(
                        text = "💡 Prinsip Syariah: Jagalah pola konsumsi dari berlebih-lebihan (Israf) agar keberkahan harta tetap terjaga.",
                        fontSize = 11.sp,
                        color = GoldAccent
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverBudgetWarningDialog = false
                        executeSaveTransaction()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseCoral),
                    modifier = Modifier.testTag("budget_warning_confirm_button")
                ) {
                    Text("Tetap Lanjutkan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverBudgetWarningDialog = false },
                    modifier = Modifier.testTag("budget_warning_cancel_button")
                ) {
                    Text("Batal & Sesuaikan", color = Color.White70)
                }
            },
            modifier = Modifier.testTag("budget_warning_dialog")
        )
    }

    if (showLiveCameraScanner) {
        CameraXReceiptScanner(
            onReceiptCaptured = { parsed, file ->
                if (parsed.amount > 0) {
                    amountText = parsed.amount.toLong().toString()
                }
                if (parsed.merchantName.isNotBlank()) {
                    receiptMerchant = parsed.merchantName
                    if (descriptionText.isBlank()) {
                        descriptionText = "${if (parsed.isIncome) "Pemasukan dari" else "Belanja di"} ${parsed.merchantName}"
                    }
                }
                if (parsed.referenceNumber.isNotBlank()) {
                    receiptRefNumber = parsed.referenceNumber
                }
                attachReceipt = true
                receiptType = parsed.suggestedReceiptType
                isIncome = parsed.isIncome
                if (parsed.suggestedCategoryAccountId.isNotBlank()) {
                    selectedCategoryAccountId = parsed.suggestedCategoryAccountId
                }
                if (parsed.date != null) {
                    selectedDate = parsed.date
                }
                showLiveCameraScanner = false
            },
            onDismiss = { showLiveCameraScanner = false }
        )
    }

    if (showOcrScannerDialog) {
        OcrReceiptScannerDialog(
            onDismiss = { showOcrScannerDialog = false },
            onOpenLiveCamera = {
                showOcrScannerDialog = false
                showLiveCameraScanner = true
            },
            onApplyParsedData = { parsed ->
                if (parsed.amount > 0) {
                    amountText = parsed.amount.toLong().toString()
                }
                if (parsed.merchantName.isNotBlank()) {
                    receiptMerchant = parsed.merchantName
                    if (descriptionText.isBlank()) {
                        descriptionText = "${if (parsed.isIncome) "Pemasukan dari" else "Belanja di"} ${parsed.merchantName}"
                    }
                }
                if (parsed.referenceNumber.isNotBlank()) {
                    receiptRefNumber = parsed.referenceNumber
                }
                attachReceipt = true
                receiptType = parsed.suggestedReceiptType
                isIncome = parsed.isIncome
                if (parsed.suggestedCategoryAccountId.isNotBlank()) {
                    selectedCategoryAccountId = parsed.suggestedCategoryAccountId
                }
                if (parsed.date != null) {
                    selectedDate = parsed.date
                }
                showOcrScannerDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrReceiptScannerDialog(
    onDismiss: () -> Unit,
    onOpenLiveCamera: () -> Unit = {},
    onApplyParsedData: (com.example.core.ocr.ParsedReceiptData) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var parsedPreview by remember { mutableStateOf<com.example.core.ocr.ParsedReceiptData?>(null) }

    val sampleReceipts = remember {
        listOf(
            "Struk Minimarket" to """
                SUPERMARKET SAKINAH MART
                JL. SURAPATI NO 45 BANDUNG
                TGL: 28-08-2026 09:30
                NO. STRUK: STR-20260828-9812
                =============================
                BERAS ORGANIK 5KG     68.500
                MINYAK GORENG 2L      34.000
                TELUR AYAM 1KG        28.500
                KURMA AJWA 500G       85.000
                =============================
                TOTAL BELANJA     Rp 216.000
                TUNAI             Rp 220.000
                KEMBALIAN         Rp   4.000
                TERIMA KASIH ATAS KUNJUNGANNYA
            """.trimIndent(),
            "Mutasi Bank BSI" to """
                BANK SYARIAH INDONESIA
                BUKTI TRANSFER / MUTASI MASUK
                TANGGAL: 25/08/2026 14:15:00
                REFF: BSI-TRX-88192031
                DARI: PT BERKAH AMANAH INDONESIA
                UNTUK: KAS MUKMIN
                NOMINAL: Rp 12.500.000
                BERITA: Gaji Bulanan Periode Agustus 2026
                STATUS: BERHASIL
            """.trimIndent(),
            "Kwitansi Donasi Amil" to """
                LEMBAGA AMIL ZAKAT BAZNAS
                BUKTI SETOR ZAKAT (BSZ)
                NO. BUKTI: BAZNAS-ZKT-2026-00412
                TANGGAL: 22/08/2026
                NAMA: MUHAMMAD IRFAN
                JENIS DANA: INFAQ & SEDEKAH SHUBUH
                JUMLAH DANA: Rp 500.000
                TERBILANG: Lima Ratus Ribu Rupiah
                SEMOGA BERKAH DAN MENJADI PEMBERSIH HARTA
            """.trimIndent()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pemindai Struk & Mutasi Otomatis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Live Camera Button
                Button(
                    onClick = onOpenLiveCamera,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_live_camerax_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka Kamera Langsung (CameraX)", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "Atau tempel teks struk / OCR atau pilih sampel mutasi di bawah untuk ekstraksi otomatis nominal, toko, dan kategori akun syariah.",
                    fontSize = 12.sp,
                    color = Color.White70
                )

                // Quick sample buttons
                Text(
                    text = "Pilih Sampel Format Struk:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sampleReceipts.forEach { (label, content) ->
                        FilledTonalButton(
                            onClick = {
                                rawText = content
                                parsedPreview = SmartReceiptParser.parseReceiptText(content)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                }

                OutlinedTextField(
                    value = rawText,
                    onValueChange = {
                        rawText = it
                        parsedPreview = SmartReceiptParser.parseReceiptText(it)
                    },
                    label = { Text("Teks Hasil Scan Struk / Mutasi") },
                    placeholder = { Text("Paste teks struk minimarket, mutasi m-banking, kwitansi di sini...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                parsedPreview?.let { preview ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "⚡ Hasil Ekstraksi Otomatis:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Text("• Nominal: Rp ${preview.amount.toLong()}", fontSize = 12.sp, color = Color.White)
                            if (preview.merchantName.isNotBlank()) {
                                Text("• Merchant/Pihak: ${preview.merchantName}", fontSize = 12.sp, color = Color.White70)
                            }
                            if (preview.referenceNumber.isNotBlank()) {
                                Text("• No. Ref: ${preview.referenceNumber}", fontSize = 12.sp, color = Color.White70)
                            }
                            Text("• Jenis: ${if (preview.isIncome) "Pemasukan (Rezeki)" else "Pengeluaran (Konsumsi)"}", fontSize = 12.sp, color = Color.White70)
                            Text("• Kategori Akun: ${preview.suggestedCategoryAccountId}", fontSize = 12.sp, color = Color.White70)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    parsedPreview?.let { onApplyParsedData(it) }
                },
                enabled = parsedPreview != null && parsedPreview!!.amount > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("Terapkan ke Form", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.White70)
            }
        }
    )
}

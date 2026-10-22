package com.example.presentation.screens

import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusChanged
import com.example.presentation.components.AmanahNumpad
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import com.example.core.debug.AppDebugLogger
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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptImageStorage
import com.example.core.receipt.ReceiptType
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.White12
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.io.File
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.core.ui.AmanahHapticEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AmanahLedgerViewModel,
    editEntryId: String? = null,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    onNavigateToFatwa: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val existingEntry = remember(editEntryId, state.journalEntries) {
        if (editEntryId != null) viewModel.getJournalEntry(editEntryId) else null
    }
    val isEditMode = existingEntry != null

    val draft by viewModel.transactionDraft.collectAsState()
    val hasActiveDraft = remember(existingEntry, draft) {
        existingEntry == null && draft.isDraftActive
    }

    // Extract values if editing or restoring draft
    val initialIsIncome = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft) draft.isIncome
        else existingEntry?.transactionType != "EXPENSE"
    }
    val initialDate = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft && draft.dateEpochMillis > 0L) Date(draft.dateEpochMillis)
        else existingEntry?.gregorianDate ?: Date()
    }
    val initialAmountText = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft) draft.amountText
        else if (existingEntry == null) ""
        else {
            if (existingEntry.transactionType == "INFLOW") {
                val gross = existingEntry.lines.firstOrNull { state.getAccount(it.accountId)?.category == AccountCategory.ASSET || it.accountId in listOf("acc_cash", "acc_bank", "acc_gold", "acc_ewallet", "acc_other_asset") }?.debit
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
    val initialDesc = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft) draft.descriptionText
        else existingEntry?.description?.replace(Regex("""\s*\(Round-up:.*?\)$"""), "") ?: ""
    }
    val initialCategory = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft) draft.selectedCategoryAccountId
        else if (existingEntry == null) "acc_salary"
        else {
            if (existingEntry.transactionType == "INFLOW") {
                existingEntry.lines.firstOrNull { it.credit > 0 && it.accountId in listOf("acc_salary", "acc_trade", "acc_gift", "acc_rikaz", "acc_syubhat") }?.accountId ?: "acc_salary"
            } else {
                existingEntry.lines.firstOrNull { it.debit > 0 && it.accountId != "acc_disbursed" }?.accountId ?: "acc_living"
            }
        }
    }
    val initialAsset = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft) draft.selectedAssetAccountId
        else if (existingEntry == null) "acc_bank"
        else {
            if (existingEntry.transactionType == "INFLOW") {
                existingEntry.lines.firstOrNull { it.debit > 0 && (state.getAccount(it.accountId)?.category == AccountCategory.ASSET || it.accountId in listOf("acc_cash", "acc_bank", "acc_gold", "acc_ewallet", "acc_other_asset")) }?.accountId ?: "acc_bank"
            } else {
                existingEntry.lines.firstOrNull { it.credit > 0 && (state.getAccount(it.accountId)?.category == AccountCategory.ASSET || it.accountId in listOf("acc_cash", "acc_bank", "acc_gold", "acc_ewallet", "acc_other_asset")) }?.accountId ?: "acc_cash"
            }
        }
    }
    val initialRate = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft) draft.infaqRate
        else if (existingEntry != null && existingEntry.transactionType == "INFLOW") {
            val gross = existingEntry.lines.firstOrNull { state.getAccount(it.accountId)?.category == AccountCategory.ASSET || it.accountId in listOf("acc_cash", "acc_bank", "acc_gold", "acc_ewallet", "acc_other_asset") }?.debit ?: 0.0
            val infaq = existingEntry.lines.firstOrNull { it.accountId == "acc_vault" }?.credit ?: 0.0
            if (gross > 0) (infaq / gross) else 0.05
        } else 0.05
    }
    val initialEnableRoundUp = remember(existingEntry, hasActiveDraft) {
        if (hasActiveDraft) draft.enableRoundUp
        else if (existingEntry != null && existingEntry.transactionType == "EXPENSE") {
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
    var showRikazDalilDialog by remember { mutableStateOf(false) }

    // Round-up options for expense
    var enableRoundUp by remember(initialEnableRoundUp) { mutableStateOf(initialEnableRoundUp) }
    var roundUpStep by remember(hasActiveDraft) { mutableDoubleStateOf(if (hasActiveDraft) draft.roundUpStep else 5000.0) }

    // Receipt Attachment State
    var attachReceipt by remember(hasActiveDraft) { mutableStateOf(if (hasActiveDraft) draft.attachReceipt else (existingEntry?.receiptAttachment != null)) }
    var receiptMerchant by remember(hasActiveDraft) { mutableStateOf(if (hasActiveDraft) draft.receiptMerchant else (existingEntry?.receiptAttachment?.merchantName ?: "")) }
    var receiptRefNumber by remember(hasActiveDraft) { mutableStateOf(if (hasActiveDraft) draft.receiptRefNumber else (existingEntry?.receiptAttachment?.referenceNumber ?: "")) }
    var receiptType by remember(hasActiveDraft) { mutableStateOf(if (hasActiveDraft) draft.receiptType else (existingEntry?.receiptAttachment?.receiptType ?: ReceiptType.STORE_RECEIPT)) }
    var receiptNotes by remember(hasActiveDraft) { mutableStateOf(if (hasActiveDraft) draft.receiptNotes else (existingEntry?.receiptAttachment?.notes ?: "")) }
    var receiptImagePath by remember(hasActiveDraft) { mutableStateOf(if (hasActiveDraft) draft.receiptImagePath else existingEntry?.receiptAttachment?.imagePath) }
    var receiptTypeDropdownExpanded by remember { mutableStateOf(false) }

    // Auto-save form draft whenever fields change to prevent data loss on app-switch or backgrounding
    LaunchedEffect(
        amountText, descriptionText, isIncome, selectedCategoryAccountId,
        selectedAssetAccountId, infaqRate, enableRoundUp, roundUpStep,
        attachReceipt, receiptMerchant, receiptRefNumber, receiptType,
        receiptNotes, receiptImagePath, selectedDate
    ) {
        if (!isEditMode) {
            val hasContent = amountText.isNotBlank() || descriptionText.isNotBlank() || !receiptImagePath.isNullOrBlank()
            if (hasContent) {
                viewModel.updateTransactionDraft(
                    com.example.core.state.TransactionDraftState(
                        isDraftActive = true,
                        isIncome = isIncome,
                        dateEpochMillis = selectedDate.time,
                        amountText = amountText,
                        descriptionText = descriptionText,
                        selectedCategoryAccountId = selectedCategoryAccountId,
                        selectedAssetAccountId = selectedAssetAccountId,
                        infaqRate = infaqRate,
                        enableRoundUp = enableRoundUp,
                        roundUpStep = roundUpStep,
                        attachReceipt = attachReceipt,
                        receiptMerchant = receiptMerchant,
                        receiptRefNumber = receiptRefNumber,
                        receiptType = receiptType,
                        receiptNotes = receiptNotes,
                        receiptImagePath = receiptImagePath,
                        editEntryId = null
                    )
                )
            }
        }
    }

    // Error & Warning Dialogs
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOverBudgetWarningDialog by remember { mutableStateOf(false) }
    var showDeficitBlockedDialog by remember { mutableStateOf(false) }
    var showDeficitWarningConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    
    val focusManager = LocalFocusManager.current
    var showNumpadBottomSheet by remember { mutableStateOf(false) }
    
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
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("id", "ID")) }

    val budgetForCategory = if (!isIncome) state.budgets.firstOrNull { it.accountId == selectedCategoryAccountId } else null
    val currentSpent = if (!isIncome) state.getMonthlySpentForAccount(selectedCategoryAccountId) else 0.0
    val willExceedBudget = budgetForCategory != null && (currentSpent + amountValue) > budgetForCategory.monthlyLimit

    val currentAssetBalance = state.getAccountBalance(selectedAssetAccountId)
    val originalExpenseForThisAccount = if (isEditMode && existingEntry?.transactionType == "EXPENSE") {
        existingEntry.lines.firstOrNull { it.credit > 0 && it.accountId == selectedAssetAccountId }?.credit ?: 0.0
    } else 0.0
    val effectiveAvailableBalance = currentAssetBalance + originalExpenseForThisAccount
    val willDeficitBalance = !isIncome && (amountValue > effectiveAvailableBalance)
    val deficitAmount = if (willDeficitBalance) amountValue - effectiveAvailableBalance else 0.0
    val projectedAssetBalance = effectiveAvailableBalance - amountValue

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
                isDigitalVerified = true,
                imagePath = receiptImagePath
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
        AppDebugLogger.logUserAction(
            "Pencatatan Transaksi",
            "Menyimpan ${if (isIncome) "Pemasukan" else "Pengeluaran"}: Rp ${amountValue.toLong()} ('$desc')"
        )
        viewModel.clearTransactionDraft()
        onNavigateBack()
    }

    // Pembersihan berkas foto sementara jika pengguna membatalkan pembuatan transaksi baru
    val handleCancelAndExit = {
        if (!isEditMode && !receiptImagePath.isNullOrBlank()) {
            ReceiptImageStorage.deleteTemporaryFile(receiptImagePath)
        }
        viewModel.clearTransactionDraft()
        onNavigateBack()
    }

    LaunchedEffect(Unit) {
        ReceiptImageStorage.cleanupTemporaryCameraCache(context)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(
                        onClick = handleCancelAndExit,
                        modifier = Modifier.testTag("back_button")
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
                            text = if (isEditMode) "Ubah Transaksi Syariah" else "Catat Transaksi Berpasangan",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isEditMode) {
                            Text(
                                text = "Mode Modifikasi Jurnal & Neraca",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else if (hasActiveDraft) {
                            Text(
                                text = "Draf input otomatis dipulihkan",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
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
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("add_tx_menu_sidebar_button")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Segmented Control: Pemasukan (Rezeki) vs Pengeluaran (Konsumsi)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIncome) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable {
                            AmanahHapticEngine.performKeyClick(haptic, context)
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
                        color = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isIncome) MaterialTheme.colorScheme.error else Color.Transparent)
                        .clickable {
                            AmanahHapticEngine.performKeyClick(haptic, context)
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
                        color = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            // 2. Dual-Calendar & Time Temporal Card (Tanggal & Waktu Transaksi)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("temporal_datetime_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Tanggal (Masehi & Hijriah)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance().apply { time = selectedDate }
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val newCal = Calendar.getInstance().apply {
                                            time = selectedDate
                                            set(Calendar.YEAR, y)
                                            set(Calendar.MONTH, m)
                                            set(Calendar.DAY_OF_MONTH, d)
                                        }
                                        selectedDate = newCal.time
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .testTag("pick_date_row"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Pilih Tanggal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dateFormat.format(selectedDate),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(Ketuk ubah tanggal)",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = "Hijriah: $hijriDate",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 11.sp
                            )
                        }
                        if (isFriday) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Jumat Berkah",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Row 2: Waktu / Jam Transaksi (TimePicker Trigger & Info)
                    val calCurrent = Calendar.getInstance().apply { time = selectedDate }
                    val currentHour = calCurrent.get(Calendar.HOUR_OF_DAY)
                    val isAfterMaghrib = currentHour >= 18
                    val isSubuhTime = currentHour in 4..5

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance().apply { time = selectedDate }
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val newCal = Calendar.getInstance().apply {
                                            time = selectedDate
                                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            set(Calendar.MINUTE, minute)
                                            set(Calendar.SECOND, 0)
                                        }
                                        selectedDate = newCal.time
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                            .testTag("pick_time_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Pilih Waktu",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Waktu: ",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${timeFormat.format(selectedDate)} WIB",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = if (isSubuhTime) {
                                        "🌅 Waktu Subuh (Tercatat sebagai Sedekah Subuh)"
                                    } else if (isAfterMaghrib) {
                                        "🌙 Setelah Maghrib (Batas Masuk Hari Hijriah Baru)"
                                    } else {
                                        "Ketuk untuk menyesuaikan jam transaksi riil"
                                    },
                                    fontSize = 10.sp,
                                    color = if (isSubuhTime) MaterialTheme.colorScheme.primary else if (isAfterMaghrib) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "Ubah Jam",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Row 3: Preset Pintasan Jam Cepat (Quick Time Chips)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val quickTimePresets = listOf(
                            Triple("Sekarang", -1, -1),
                            Triple("Subuh (05:15)", 5, 15),
                            Triple("Dzuhur (12:30)", 12, 30),
                            Triple("Sore (16:45)", 16, 45),
                            Triple("Malam (20:00)", 20, 0)
                        )

                        quickTimePresets.forEach { (label, hr, min) ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val newCal = Calendar.getInstance().apply {
                                            if (hr == -1) {
                                                val now = Calendar.getInstance()
                                                time = selectedDate
                                                set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
                                                set(Calendar.MINUTE, now.get(Calendar.MINUTE))
                                                set(Calendar.SECOND, now.get(Calendar.SECOND))
                                            } else {
                                                time = selectedDate
                                                set(Calendar.HOUR_OF_DAY, hr)
                                                set(Calendar.MINUTE, min)
                                                set(Calendar.SECOND, 0)
                                            }
                                        }
                                        selectedDate = newCal.time
                                    },
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 5.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 3. Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { },
                readOnly = true,
                label = { Text("Nominal Transaksi (Rp)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                prefix = { Text("Rp ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            AmanahHapticEngine.performKeyClick(haptic, context)
                            showNumpadBottomSheet = true
                        },
                        modifier = Modifier.testTag("open_calculator_keyboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Buka Keyboard Kalkulator",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_transaction_amount_input")
                    .clickable {
                        AmanahHapticEngine.performKeyClick(haptic, context)
                        showNumpadBottomSheet = true
                    }
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            AmanahHapticEngine.performKeyClick(haptic, context)
                            showNumpadBottomSheet = true
                            focusManager.clearFocus() // Prevent soft keyboard from showing
                        }
                    },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
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
                    label = { Text("Kategori Akun", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("category_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    availableCategories.forEach { acc ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(acc.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    Text(acc.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                }
                            },
                            onClick = {
                                AmanahHapticEngine.performKeyClick(haptic, context)
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
                    label = { Text(if (isIncome) "Masuk ke Rekening/Kas" else "Sumber Dana (Kas/Bank)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("asset_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = assetDropdownExpanded,
                    onDismissRequest = { assetDropdownExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    assetAccounts.forEach { acc ->
                        val accBal = state.getAccountBalance(acc.id)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(acc.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                                    Text(
                                        text = "Rp ${formatRupiah(accBal)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (accBal < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            onClick = {
                                AmanahHapticEngine.performKeyClick(haptic, context)
                                selectedAssetAccountId = acc.id
                                assetDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            if (!isIncome) {
                Spacer(modifier = Modifier.height(6.dp))
                val currentAsset = state.getAccount(selectedAssetAccountId)
                val isDeficit = willDeficitBalance && amountValue > 0.0

                if (isDeficit) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deficit_warning_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.isDeficitProtectionEnabled)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (state.isDeficitProtectionEnabled) MaterialTheme.colorScheme.error.copy(alpha = 0.45f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (state.isDeficitProtectionEnabled) Icons.Default.Block else Icons.Default.Warning,
                                contentDescription = "Peringatan Defisit",
                                tint = if (state.isDeficitProtectionEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (state.isDeficitProtectionEnabled)
                                        "Proteksi Defisit Aktif: Saldo Tidak Cukup"
                                    else
                                        "Peringatan: Saldo Akan Menjadi Minus",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isDeficitProtectionEnabled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Saldo ${currentAsset?.name ?: "Akun"}: Rp ${formatRupiah(effectiveAvailableBalance)} | Belanja: Rp ${formatRupiah(amountValue)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (state.isDeficitProtectionEnabled)
                                        "Kekurangan dana Rp ${formatRupiah(deficitAmount)}. Transaksi tidak dapat disimpan karena proteksi saldo minus aktif."
                                    else
                                        "Setelah transaksi disimpan, saldo akun akan bernilai minus (-Rp ${formatRupiah(deficitAmount)}).",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Saldo Tersedia: Rp ${formatRupiah(effectiveAvailableBalance)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (amountValue > 0.0) {
                            Text(
                                text = "Sisa: Rp ${formatRupiah(projectedAssetBalance)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 6. Infaq & Purification Settings Card
            if (isIncome) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (selectedCategoryAccountId == "acc_rikaz") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Wajib 20% (Rikaz)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { showRikazDalilDialog = true },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Dalil Hukum Rikaz 20%",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else if (selectedCategoryAccountId == "acc_syubhat") {
                                Text("Wajib 100% (Syubhat)", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("${String.format(Locale.US, "%.1f", infaqRate * 100).removeSuffix(".0")}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (selectedCategoryAccountId == "acc_rikaz") {
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showRikazDalilDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Landasan Fiqih: HR. Bukhari No. 1499 & PMA No. 52/2014",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Kewajiban zakat harta temuan purbakala (rikaz) adalah 20% (Al-Khumus) seketika tanpa haul. Ketuk untuk membaca dalil & ketentuan lengkap.",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (selectedCategoryAccountId != "acc_rikaz" && selectedCategoryAccountId != "acc_syubhat") {
                            Spacer(modifier = Modifier.height(10.dp))

                            // Preset percentage chips
                            Text("Pilihan Persentase Infaq:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
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
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
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
                                Text("Persentase Kustom:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
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
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                                    ),
                                    trailingIcon = { Text("%", color = MaterialTheme.colorScheme.secondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 6.dp)) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier
                                        .width(105.dp)
                                        .height(44.dp)
                                        .testTag("custom_infaq_percent_input")
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Slider(
                                value = infaqRate.toFloat(),
                                onValueChange = { infaqRate = (Math.round(it * 200.0) / 200.0).coerceIn(0.0, 1.0) },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Rp ${formatRupiah(calculatedInfaqPreview)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Rp ${formatRupiah(netAmount)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
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
                            containerColor = if (isWillBeOver) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isWillBeOver) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                                    color = if (isWillBeOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Batas: Rp ${formatRupiah(budgetForCategory.monthlyLimit)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isWillBeOver) {
                                        "Melebihi Rp ${formatRupiah(projectedSpent - budgetForCategory.monthlyLimit)}"
                                    } else {
                                        "Sisa: Rp ${formatRupiah(remainingAfter)}"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWillBeOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Micro-infaq round-up for expense
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Bulatkan belanjaan ke kelipatan Rp ${formatRupiah(roundUpStep)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = enableRoundUp,
                                onCheckedChange = { enableRoundUp = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = EmeraldDark
                                ),
                                modifier = Modifier.testTag("roundup_switch")
                            )
                        }

                        if (enableRoundUp) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Pilih / Tentukan Kelipatan Pembulatan (Round-up):",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(100.0, 500.0, 1000.0, 5000.0, 10000.0).forEach { stepVal ->
                                    val isSelected = roundUpStep == stepVal
                                    Surface(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { roundUpStep = stepVal }
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (stepVal >= 1000.0) "${(stepVal / 1000).toInt()}k" else "${stepVal.toInt()}",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            var customStepText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customStepText,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() }
                                        customStepText = filtered
                                        filtered.toDoubleOrNull()?.let { customVal ->
                                            if (customVal > 0) roundUpStep = customVal
                                        }
                                    },
                                    placeholder = { Text("Kustom kelipatan (cth: 2000, 2500)", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }

                        if (enableRoundUp && calculatedInfaqPreview > 0.0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sedekah pembulatan otomatis: Rp ${formatRupiah(calculatedInfaqPreview)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
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
                label = { Text("Keterangan Transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                placeholder = { Text("Contoh: Gaji freelance, Belanja dapur", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("description_input")
            )

            // 7b. Bukti Transaksi & Kuitansi Digital (Receipt Attachment)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (attachReceipt) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                                color = if (attachReceipt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Lampirkan nota, bukti transfer, atau struk QRIS",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = attachReceipt,
                            onCheckedChange = { attachReceipt = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
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
                                label = { Text("Tipe Bukti", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = receiptTypeDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = receiptTypeDropdownExpanded,
                                onDismissRequest = { receiptTypeDropdownExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                ReceiptType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.displayName, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp) },
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
                            label = { Text("Nama Toko / Merchant / Pihak Kedua", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            placeholder = { Text("cth: Toko Berkah Barakah, BAZNAS, PT PLN", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("receipt_merchant_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = receiptRefNumber,
                            onValueChange = { receiptRefNumber = it },
                            label = { Text("No. Resi / Invoice / Ref Transaksi", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            placeholder = { Text("cth: INV/2026/08/99812", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("receipt_ref_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Physical Receipt Photo Preview & Dual Pickers (Camera & Gallery)
                        var tempCameraFile by remember { mutableStateOf<File?>(null) }
                        val cameraLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.TakePicture()
                        ) { success ->
                            if (success && tempCameraFile != null && tempCameraFile!!.exists()) {
                                val savedPath = ReceiptImageStorage.saveImageFile(context, tempCameraFile!!)
                                if (savedPath != null) {
                                    receiptImagePath = savedPath
                                }
                                tempCameraFile?.delete()
                            }
                        }

                        val cameraPermissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (isGranted) {
                                val pair = ReceiptImageStorage.createCameraTempUri(context)
                                if (pair != null) {
                                    tempCameraFile = pair.second
                                    cameraLauncher.launch(pair.first)
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Izin kamera diperlukan untuk memotret struk belanja", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                        val onLaunchCamera: () -> Unit = {
                            val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (hasCameraPermission) {
                                val pair = ReceiptImageStorage.createCameraTempUri(context)
                                if (pair != null) {
                                    tempCameraFile = pair.second
                                    cameraLauncher.launch(pair.first)
                                }
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }

                        val directImagePicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.PickVisualMedia()
                        ) { uri: Uri? ->
                            if (uri != null) {
                                val savedPath = ReceiptImageStorage.saveImageFromUri(context, uri)
                                if (savedPath != null) {
                                    receiptImagePath = savedPath
                                }
                            }
                        }

                        if (!receiptImagePath.isNullOrBlank() && File(receiptImagePath!!).exists()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Foto Fisik Tersimpan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = onLaunchCamera,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.CameraAlt, contentDescription = "Foto Ulang Kamera", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    directImagePicker.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoLibrary, contentDescription = "Ganti dari Galeri", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    ReceiptImageStorage.deleteImageFile(receiptImagePath)
                                                    receiptImagePath = null
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus Foto", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AsyncImage(
                                        model = File(receiptImagePath!!),
                                        contentDescription = "Foto Struk Fisik",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onLaunchCamera,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("take_receipt_photo_camera_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Foto Kamera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        directImagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("attach_receipt_photo_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pilih Galeri", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 8. Submit Button
            Button(
                onClick = {
                    AmanahHapticEngine.performButtonClick(haptic, context)
                    if (amountValue <= 0.0) {
                        errorMessage = "Masukkan nominal transaksi yang lebih besar dari 0"
                        return@Button
                    }

                    if (!isIncome && willDeficitBalance) {
                        if (state.isDeficitProtectionEnabled) {
                            showDeficitBlockedDialog = true
                        } else {
                            showDeficitWarningConfirmDialog = true
                        }
                    } else if (!isIncome && willExceedBudget) {
                        showOverBudgetWarningDialog = true
                    } else {
                        executeSaveTransaction()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
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
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_transaction_bottom_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hapus Transaksi (Pulihkan Saldo)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog && editEntryId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Transaksi Jurnal Ini?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tindakan ini akan menghapus transaksi dari buku besar syariah. Seluruh saldo kas/bank dan kewajiban vault infaq terkait akan otomatis dikembalikan ke posisi semula.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "⚠️ Catatan: Penghapusan bersifat permanen.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_dialog_button")
                ) {
                    Text("Ya, Hapus Transaksi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_delete_dialog_button")
                ) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Peringatan: Anggaran Terlampaui!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Transaksi ini akan menyebabkan pos pengeluaran '${budgetForCategory.categoryName}' melampaui batas anggaran bulanan yang telah ditentukan.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Batas Anggaran:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(budgetForCategory.monthlyLimit)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pengeluaran Saat Ini:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(currentSpent)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nominal Transaksi Ini:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("+Rp ${formatRupiah(amountValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Proyeksi:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Rp ${formatRupiah(projectedTotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Melebihi Kuota Sebesar:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("Rp ${formatRupiah(excessAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text(
                        text = "💡 Prinsip Syariah: Jagalah pola konsumsi dari berlebih-lebihan (Israf) agar keberkahan harta tetap terjaga.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverBudgetWarningDialog = false
                        executeSaveTransaction()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("budget_warning_confirm_button")
                ) {
                    Text("Tetap Lanjutkan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showOverBudgetWarningDialog = false },
                    modifier = Modifier.testTag("budget_warning_cancel_button")
                ) {
                    Text("Batal & Sesuaikan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            modifier = Modifier.testTag("budget_warning_dialog")
        )
    }

    if (showDeficitBlockedDialog) {
        val currentAsset = state.getAccount(selectedAssetAccountId)
        AlertDialog(
            onDismissRequest = { showDeficitBlockedDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Saldo Kas/Bank Tidak Mencukupi!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pengeluaran ini tidak dapat disimpan karena 'Proteksi Saldo Defisit' aktif dan nominal melebihi sisa saldo fisik pada akun kas/bank yang dipilih.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sumber Dana:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentAsset?.name ?: "Akun Kas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo Tersedia:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(effectiveAvailableBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nominal Pengeluaran:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(amountValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Kekurangan Dana (Defisit):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("-Rp ${formatRupiah(deficitAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text(
                        text = "💡 Prinsip Syariah: Kas riil tidak dapat dibelanjakan melebihi saldo fisik yang ada tanpa adanya akad pinjaman/talangan (Qardh). Proteksi Saldo Defisit menjaga akuntabilitas keuangan Anda.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "ℹ️ Jika Anda ingin mengizinkan pencatatan hingga saldo bernilai minus, Anda dapat menonaktifkan proteksi ini di menu Pengaturan Kas Mukmin.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeficitBlockedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("deficit_blocked_dismiss_button")
                ) {
                    Text("Mengerti & Sesuaikan", fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier.testTag("deficit_blocked_dialog")
        )
    }

    if (showDeficitWarningConfirmDialog) {
        val currentAsset = state.getAccount(selectedAssetAccountId)
        AlertDialog(
            onDismissRequest = { showDeficitWarningConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Konfirmasi: Saldo Akan Bernilai Minus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pengeluaran ini melebihi saldo kas/bank fisik yang ada. Karena Proteksi Saldo Defisit dinonaktifkan, transaksi ini akan mengakibatkan saldo akun menjadi minus.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sumber Dana:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(currentAsset?.name ?: "Akun Kas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo Saat Ini:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(effectiveAvailableBalance)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nominal Pengeluaran:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Rp ${formatRupiah(amountValue)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Saldo Setelah Transaksi:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("-Rp ${formatRupiah(deficitAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text(
                        text = "Apakah Anda yakin ingin tetap menyimpan transaksi ini dan membiarkan akun kas bersaldo negatif?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeficitWarningConfirmDialog = false
                        if (willExceedBudget) {
                            showOverBudgetWarningDialog = true
                        } else {
                            executeSaveTransaction()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("deficit_confirm_proceed_button")
                ) {
                    Text("Tetap Lanjutkan (Izinkan Minus)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeficitWarningConfirmDialog = false },
                    modifier = Modifier.testTag("deficit_confirm_cancel_button")
                ) {
                    Text("Batal & Sesuaikan", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            modifier = Modifier.testTag("deficit_warning_confirm_dialog")
        )
    }

    if (showRikazDalilDialog) {
        AlertDialog(
            onDismissRequest = { showRikazDalilDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Dasar Hukum: Kewajiban 20% Harta Rikaz",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Hadits Shahih Bukhari & Muslim",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "«... وَفِي الرِّكَازِ الْخُمُسُ»",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "\"Dan pada rikaz (harta temuan/karun purbakala), kewajiban zakatnya adalah seperlima (20%).\"\n— HR. Bukhari No. 1499 & HR. Muslim No. 1710",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Kaidah Fiqih & Regulasi Resmi:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "1. Sesuai Peraturan Menteri Agama (PMA) No. 52/2014 Pasal 24 & Kesepakatan 4 Mazhab, zakat rikaz adalah 20% (Al-Khumus).\n2. Wajib ditunaikan seketika saat harta ditemukan tanpa mensyaratkan berlalunya waktu satu tahun (Haul).\n3. Sisa 80% adalah hak halal penemu setelah 20% disalurkan kepada mustahiq/fakir miskin.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    androidx.compose.material3.Surface(
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Catatan Penting: Rikaz vs Luqathah",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Ketentuan 20% ini khusus untuk harta karun kuno tak bertuan (Rikaz). Adapun barang hilang/tercecer milik orang lain di masa sekarang disebut Luqathah dan wajib diumumkan selama 1 tahun, bukan dipotong 20%.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRikazDalilDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Saya Paham")
                }
            },
            dismissButton = {
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        showRikazDalilDialog = false
                        onNavigateToFatwa()
                    }
                ) {
                    Text("Buka Ensiklopedia Fiqih")
                }
            }
        )
    }


if (showNumpadBottomSheet) {
        // Local state for the calculator
        var expression by remember { mutableStateOf(amountText.ifEmpty { "0" }) }
        
        fun evaluateMath(expr: String): String {
            try {
                // Remove spaces and evaluate basic left-to-right math
                var cleanExpr = expr.replace(" ", "").replace("Rp", "")
                if (cleanExpr.isEmpty()) return "0"
                
                // Very basic sequential evaluator
                val tokens = mutableListOf<String>()
                var currentNum = ""
                for (char in cleanExpr) {
                    if (char == '+' || char == '-' || char == '×' || char == '÷') {
                        if (currentNum.isNotEmpty()) tokens.add(currentNum)
                        tokens.add(char.toString())
                        currentNum = ""
                    } else {
                        currentNum += char
                    }
                }
                if (currentNum.isNotEmpty()) tokens.add(currentNum)
                
                if (tokens.isEmpty()) return "0"
                
                var result = tokens[0].toDoubleOrNull() ?: 0.0
                var i = 1
                while (i < tokens.size - 1) {
                    val op = tokens[i]
                    val nextNum = tokens[i+1].toDoubleOrNull() ?: 0.0
                    when (op) {
                        "+" -> result += nextNum
                        "-" -> result -= nextNum
                        "×" -> result *= nextNum
                        "÷" -> if (nextNum != 0.0) result /= nextNum
                    }
                    i += 2
                }
                // Return as long string to avoid scientific notation
                val longRes = result.toLong()
                return if (longRes < 0) "0" else longRes.toString()
            } catch (e: Exception) {
                return "0"
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showNumpadBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = expression,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                AmanahNumpad(
                    onNumberClick = { num ->
                        if (expression == "0") {
                            expression = num.toString()
                        } else {
                            if (expression.length < 25) {
                                expression += num.toString()
                            }
                        }
                    },
                    onZeroZeroZeroClick = {
                        if (expression == "0" || expression.isEmpty()) {
                            expression = "0"
                        } else {
                            val lastChar = expression.lastOrNull()
                            if (lastChar != null && lastChar.isDigit() && expression.length < 22) {
                                expression += "000"
                            }
                        }
                    },
                    onClearClick = {
                        expression = "0"
                    },
                    onBackspaceClick = {
                        if (expression.isNotEmpty()) {
                            expression = expression.dropLast(1)
                            if (expression.isEmpty()) expression = "0"
                        }
                    },
                    onOperatorClick = { op ->
                        val lastChar = expression.lastOrNull()
                        if (lastChar != null && lastChar.isDigit()) {
                            expression += op
                        } else if (lastChar != null && lastChar in listOf('+', '-', '×', '÷')) {
                            // Replace last operator
                            expression = expression.dropLast(1) + op
                        }
                    },
                    onDoneClick = { 
                        val finalResult = evaluateMath(expression)
                        amountText = finalResult
                        errorMessage = null
                        showNumpadBottomSheet = false 
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }


}


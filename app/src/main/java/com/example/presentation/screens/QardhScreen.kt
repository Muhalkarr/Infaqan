package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.qardh.QardhInstallment
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.state.AmanahLedgerViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QardhScreen(
    viewModel: AmanahLedgerViewModel,
    onBackClick: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedQardhForInstallment by remember { mutableStateOf<QardhRecord?>(null) }
    var filterType by remember { mutableStateOf<QardhType?>(null) }

    val totalPiutang = uiState.qardhRecords.filter { it.type == QardhType.PIUTANG_SAYA }.sumOf { it.remainingAmount }
    val totalHutang = uiState.qardhRecords.filter { it.type == QardhType.HUTANG_SAYA }.sumOf { it.remainingAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hutang Piutang Syariah", fontWeight = FontWeight.Bold)
                        Text(
                            "Akad Qardhul Hasan (QS. Al-Baqarah: 282)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("qardh_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("qardh_menu_sidebar_button")
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
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_qardh_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Catat Akad Baru")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // Dalil Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ayat Mudayanah (Pencatatan Hutang)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "\"Wahai orang-orang yang beriman, apabila kamu bermuamalah tidak secara tunai untuk waktu yang ditentukan, hendaklah kamu menuliskannya... dan persaksikanlah dengan dua orang saksi...\" (QS. Al-Baqarah: 282)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Summary Header Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Piutang (Aset Tagihan)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CallMade,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Piutang Saya", style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                rupiahFormat.format(totalPiutang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Text("Tagihan Hak Anda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Hutang (Kewajiban)
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CallReceived,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hutang Saya", style = MaterialTheme.typography.labelMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                rupiahFormat.format(totalHutang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text("Amanah Pelunasan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == null,
                        onClick = { filterType = null },
                        label = { Text("Semua (${uiState.qardhRecords.size})") }
                    )
                    FilterChip(
                        selected = filterType == QardhType.PIUTANG_SAYA,
                        onClick = { filterType = QardhType.PIUTANG_SAYA },
                        label = { Text("Piutang Tagihan") }
                    )
                    FilterChip(
                        selected = filterType == QardhType.HUTANG_SAYA,
                        onClick = { filterType = QardhType.HUTANG_SAYA },
                        label = { Text("Hutang Kewajiban") }
                    )
                }
            }

            // Records List
            val displayedRecords = uiState.qardhRecords.filter { filterType == null || it.type == filterType }
            if (displayedRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada catatan hutang/piutang aktif.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Klik tombol (+) di bawah untuk mencatat akad qardh baru.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(displayedRecords, key = { it.id }) { record ->
                    QardhCardItem(
                        record = record,
                        rupiahFormat = rupiahFormat,
                        onPayInstallment = { selectedQardhForInstallment = record },
                        onForgiveSedekah = { viewModel.forgiveQardhAsSedekah(record.id) },
                        onDelete = { viewModel.deleteQardhRecord(record.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddQardhDialog(
            wallets = uiState.wallets,
            onDismiss = { showAddDialog = false },
            onSave = { newRecord ->
                viewModel.addQardhRecord(newRecord)
                showAddDialog = false
            }
        )
    }

    selectedQardhForInstallment?.let { record ->
        RecordInstallmentDialog(
            qardh = record,
            wallets = uiState.wallets,
            rupiahFormat = rupiahFormat,
            onDismiss = { selectedQardhForInstallment = null },
            onConfirm = { amount, walletId, note ->
                viewModel.recordQardhInstallment(
                    qardhId = record.id,
                    amount = amount,
                    walletId = walletId,
                    note = note,
                    receipt = null
                )
                selectedQardhForInstallment = null
            }
        )
    }
}

@Composable
fun QardhCardItem(
    record: QardhRecord,
    rupiahFormat: NumberFormat,
    onPayInstallment: () -> Unit,
    onForgiveSedekah: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    val isPiutang = record.type == QardhType.PIUTANG_SAYA
    val statusColor = when (record.status) {
        QardhStatus.LUNAS -> Color(0xFF10B981)
        QardhStatus.DIIKHLASKAN_SEDEKAH -> Color(0xFF6366F1)
        QardhStatus.SEBAGIAN_LUNAS -> Color(0xFFF59E0B)
        QardhStatus.AKTIF -> if (isPiutang) Color(0xFF10B981) else MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPiutang) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = record.type.badge,
                        color = if (isPiutang) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = record.status.displayName,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        record.counterpartyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (record.contactInfo.isNotBlank()) {
                        Text(
                            "Kontak: ${record.contactInfo}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        rupiahFormat.format(record.remainingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        "dari ${rupiahFormat.format(record.totalAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (record.dueDateMillis != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Jatuh Tempo: ${dateFormat.format(Date(record.dueDateMillis))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (record.witnessName.isNotBlank()) {
                        Text(
                            "Saksi Akad (QS. 2:282): ${record.witnessName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (record.agreementTerms.isNotBlank()) {
                        Text(
                            "Kesepakatan Akad: ${record.agreementTerms}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (record.notes.isNotBlank()) {
                        Text(
                            "Catatan: ${record.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Installment History
                    if (record.installments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Riwayat Cicilan (${record.installments.size} kali):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        record.installments.forEach { inst ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${dateFormat.format(Date(inst.dateMillis))} • ${inst.note.ifBlank { "Cicilan" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    rupiahFormat.format(inst.amount),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (record.remainingAmount > 0) {
                            Button(
                                onClick = onPayInstallment,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bayar Cicilan")
                            }

                            if (isPiutang) {
                                OutlinedButton(
                                    onClick = onForgiveSedekah,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6366F1))
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ikhlaskan Sedekah")
                                }
                            }
                        }

                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQardhDialog(
    wallets: List<com.example.core.wallet.WalletAccount>,
    onDismiss: () -> Unit,
    onSave: (QardhRecord) -> Unit
) {
    var type by remember { mutableStateOf(QardhType.PIUTANG_SAYA) }
    var counterpartyName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var witnessName by remember { mutableStateOf("") }
    var agreementTerms by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dueDaysText by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pencatatan Akad Qardh Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type selector
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = type == QardhType.PIUTANG_SAYA,
                        onClick = { type = QardhType.PIUTANG_SAYA },
                        label = { Text("Piutang (Saya Meminjamkan)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = type == QardhType.HUTANG_SAYA,
                        onClick = { type = QardhType.HUTANG_SAYA },
                        label = { Text("Hutang (Saya Meminjam)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = counterpartyName,
                    onValueChange = { counterpartyName = it },
                    label = { Text("Nama Pihak Terkait (Teman / Kerabat)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("No. HP / Kontak (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Pinjaman (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDaysText,
                    onValueChange = { dueDaysText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target Pelunasan (Berapa Hari)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = witnessName,
                    onValueChange = { witnessName = it },
                    label = { Text("Nama Saksi Akad (QS. 2:282)") },
                    placeholder = { Text("cth: Ustadz Ahmad / Kerabat") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = agreementTerms,
                    onValueChange = { agreementTerms = it },
                    label = { Text("Catatan Akad / Kesepakatan") },
                    placeholder = { Text("cth: Pengembalian cicilan tanpa bunga/riba") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val dueDays = dueDaysText.toIntOrNull() ?: 30
                    if (counterpartyName.isNotBlank() && amount > 0) {
                        val dueMillis = System.currentTimeMillis() + (dueDays * 86400000L)
                        onSave(
                            QardhRecord(
                                type = type,
                                counterpartyName = counterpartyName,
                                contactInfo = contactInfo,
                                totalAmount = amount,
                                remainingAmount = amount,
                                dueDateMillis = dueMillis,
                                witnessName = witnessName,
                                agreementTerms = agreementTerms,
                                notes = notes,
                                status = QardhStatus.AKTIF
                            )
                        )
                    }
                },
                enabled = counterpartyName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Simpan Akad")
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
fun RecordInstallmentDialog(
    qardh: QardhRecord,
    wallets: List<com.example.core.wallet.WalletAccount>,
    rupiahFormat: NumberFormat,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, walletId: String, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf(qardh.remainingAmount.toLong().toString()) }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "acc_cash") }
    var note by remember { mutableStateOf("Cicilan pelunasan hutang/piutang") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Pembayaran Cicilan", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Pihak: ${qardh.counterpartyName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sisa Belum Lunas: ${rupiahFormat.format(qardh.remainingAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Bayar (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keterangan Pembayaran") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt, selectedWalletId, note)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Konfirmasi Pembayaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

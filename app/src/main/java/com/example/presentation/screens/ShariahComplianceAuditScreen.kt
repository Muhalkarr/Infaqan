package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.audit.AuditedTransactionItem
import com.example.core.audit.ShariahAuditStatus
import com.example.core.audit.ShariahComplianceAuditEngine
import com.example.core.state.AmanahLedgerViewModel
import com.example.core.wallet.WalletAccount
import com.example.presentation.components.ExperimentalBadge
import com.example.presentation.components.ExperimentalNoticeCard
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShariahComplianceAuditScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSedekahSubuh: () -> Unit,
    onNavigateToInfaqVault: () -> Unit,
    onEditTransaction: (String) -> Unit = {},
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val auditSummary = remember(state.journalEntries, state.haulStartDate, state.purifiedAuditEntryIds) {
        ShariahComplianceAuditEngine.auditAllEntries(state)
    }

    var selectedFilter by remember { mutableStateOf<ShariahAuditStatus?>(null) } // null: Semua
    var itemToPurify by remember { mutableStateOf<AuditedTransactionItem?>(null) }
    var itemToClarify by remember { mutableStateOf<AuditedTransactionItem?>(null) }

    val filteredItems = remember(selectedFilter, auditSummary.items) {
        when (selectedFilter) {
            null -> auditSummary.items
            ShariahAuditStatus.SUSPICIOUS_RIBA -> auditSummary.items.filter {
                it.status != ShariahAuditStatus.COMPLIANT && it.status != ShariahAuditStatus.PURIFIED
            }
            else -> auditSummary.items.filter { it.status == selectedFilter }
        }
    }

    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("audit_screen_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Audit Kepatuhan Syariah", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            ExperimentalBadge(text = "🧪 Eksperimen")
                        }
                        Text("Deteksi Mutasi Syubhat, Tathhir, Pelacak Haul 354 Hari", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("audit_screen_drawer_btn")) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Notice Banner Fitur Eksperimen
            item {
                ExperimentalNoticeCard(
                    title = "Audit Kepatuhan Syariah & Penyucian (Tathhir)",
                    subtitle = "Deteksi Cerdas Kata Kunci & Tindakan Penyucian Langsung",
                    description = "Sistem menandai transaksi yang memuat kata seperti bunga, denda, atau penalti agar Anda dapat meninjau: jika salah ketik silakan pilih 'Edit Catatan', jika bukan riba pilih 'Tandai Aman', dan jika benar ada porsi syubhat pilih 'Sucikan (Tathhir)' untuk langsung menyalurkannya ke fasilitas umum (terpisah dari Infaq Vault biasa)."
                )
            }

            // 1. Shariah Health Score Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { auditSummary.complianceScorePercent / 100f },
                                modifier = Modifier.size(68.dp),
                                strokeWidth = 7.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Text(
                                "${auditSummary.complianceScorePercent}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                            Text("Indeks Kepatuhan Muamalah", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = "${auditSummary.compliantCount} dari ${auditSummary.totalTransactionsAudited} mutasi bersih/telah disucikan.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                lineHeight = 15.sp
                            )
                            if (auditSummary.flaggedCount > 0) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "⚠️ ${auditSummary.flaggedCount} mutasi perlu ditinjau / disucikan",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "✓ Seluruh catatan telah bersih atau disucikan",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (auditSummary.purifiedCount > 0) {
                                Text(
                                    text = "${auditSummary.purifiedCount} catatan sudah disucikan / diklarifikasi aman.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Haul Cycle Hijriah Countdown Card (354 days)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text("Pelacak Haul 1 Tahun Hijriah (354 Hari)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    if (auditSummary.isHaulCompleted) "HAUL GENAP!" else "${auditSummary.haulDaysRemaining} Hari Lagi",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val progressHaul = ((354 - auditSummary.haulDaysRemaining) / 354f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progressHaul },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = if (auditSummary.isHaulCompleted)
                                "Siklus haul 354 hari telah terpenuhi sempurna. Pastikan zakat maal Anda telah dikeluarkan dari total saldo kas yang tersimpan."
                            else
                                "Masa haul sedang berjalan (${(progressHaul * 100).toInt()}% terlalui). Zakat maal wajib ditunaikan saat genap 354 hari jika saldo bertahan di atas batas nisab 85g emas.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // 3. Shortcut Tathhir & Sedekah Subuh Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CleanHands, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text("Kanal Penyucian Dana (Tathhir)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = "Salurkan bunga/denda ke fasilitas umum di tab Tathhir (Zakat Hub) tanpa memotong Infaq Vault.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = onNavigateToInfaqVault,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("open_tathhir_hub_btn")
                            ) {
                                Text("Tab Tathhir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text("Jadwal Rutin Sedekah Subuh", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Doa dua malaikat setiap fajar: 'Ya Allah, berikanlah pengganti bagi yang berinfaq'", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Button(
                                onClick = onNavigateToSedekahSubuh,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("open_sedekah_subuh_btn")
                            ) {
                                Text("Buka", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Filter Chips for Transactions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Riwayat Mutasi yang Diaudit", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("Semua (${auditSummary.totalTransactionsAudited})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == ShariahAuditStatus.SUSPICIOUS_RIBA,
                            onClick = {
                                selectedFilter = if (selectedFilter == ShariahAuditStatus.SUSPICIOUS_RIBA) null else ShariahAuditStatus.SUSPICIOUS_RIBA
                            },
                            label = { Text("Perlu Ditinjau (${auditSummary.flaggedCount})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == ShariahAuditStatus.PURIFIED,
                            onClick = {
                                selectedFilter = if (selectedFilter == ShariahAuditStatus.PURIFIED) null else ShariahAuditStatus.PURIFIED
                            },
                            label = { Text("Sudah Disucikan (${auditSummary.purifiedCount})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == ShariahAuditStatus.COMPLIANT,
                            onClick = {
                                selectedFilter = if (selectedFilter == ShariahAuditStatus.COMPLIANT) null else ShariahAuditStatus.COMPLIANT
                            },
                            label = { Text("Sesuai Syariah", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // 5. Audited Transactions List
            if (filteredItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada transaksi pada kategori ini.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredItems, key = { it.entryId }) { item ->
                    val isPurified = item.status == ShariahAuditStatus.PURIFIED
                    val isCompliant = item.status == ShariahAuditStatus.COMPLIANT
                    val isFlagged = !isCompliant && !isPurified

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isFlagged -> MaterialTheme.colorScheme.error.copy(alpha = 0.55f)
                                isPurified -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.description, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(sdf.format(item.date), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = when {
                                        isPurified -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                                        isCompliant -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isPurified -> "✓ SUDAH DISUCIKAN / DIKLARIFIKASI"
                                            isCompliant -> "✓ SYARIAH COMPLIANT"
                                            else -> "⚠️ PERLU DITINJAU (SYUBHAT/RIBA)"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isPurified -> MaterialTheme.colorScheme.secondary
                                            isCompliant -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.error
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("Rp ${nf.format(item.amount.toLong())}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Text(item.reasonNote, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 14.sp)

                            if (isFlagged) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Langkah Penyelesaian: ${item.recommendedAction}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 14.sp
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { onEditTransaction(item.entryId) },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .testTag("audit_edit_btn_${item.entryId}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Edit Catatan", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Button(
                                                onClick = { itemToPurify = item },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .testTag("audit_purify_btn_${item.entryId}")
                                            ) {
                                                Icon(Icons.Default.CleanHands, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Sucikan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        TextButton(
                                            onClick = { itemToClarify = item },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .testTag("audit_mark_safe_btn_${item.entryId}")
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Bukan Riba / Tandai Aman", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { onEditTransaction(item.entryId) },
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit Catatan", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    itemToPurify?.let { target ->
        TathhirPurificationDialog(
            item = target,
            wallets = state.wallets,
            getWalletBalance = { state.getWalletBalance(it) },
            nf = nf,
            onDismiss = { itemToPurify = null },
            onConfirm = { amount, purpose, walletId, notes ->
                viewModel.purifyFlaggedTransaction(
                    entryId = target.entryId,
                    amount = amount,
                    publicPurpose = purpose,
                    fromWalletId = walletId,
                    notes = notes
                )
                itemToPurify = null
            }
        )
    }

    itemToClarify?.let { target ->
        AlertDialog(
            onDismissRequest = { itemToClarify = null },
            icon = {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text("Klarifikasi Catatan Aman?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Catatan '${target.description}' akan ditandai sebagai transaksi non-riba yang telah diklarifikasi aman, sehingga tidak memunculkan peringatan penyucian lagi. Jika sebenarnya hanya ada salah ketik kata, Anda juga dapat memakai tombol Edit Catatan.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAuditEntryAsCleared(target.entryId, isPurification = false)
                        itemToClarify = null
                    },
                    modifier = Modifier.testTag("confirm_mark_safe_dialog_btn")
                ) {
                    Text("Ya, Tandai Aman")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToClarify = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun TathhirPurificationDialog(
    item: AuditedTransactionItem,
    wallets: List<WalletAccount>,
    getWalletBalance: (String) -> Double,
    nf: NumberFormat,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, purpose: String, walletId: String, notes: String) -> Unit
) {
    val defaultAmount = if (item.amount > 0.0) item.amount.toLong().toString() else ""
    var amountText by remember(item.entryId) { mutableStateOf(defaultAmount) }
    val purposeOptions = listOf(
        "Perbaikan Jalan, Jembatan & Sarana Publik",
        "Fasilitas Sanitasi, MCK & Air Bersih Umum",
        "Penghijauan Lingkungan & Pengelolaan Sampah",
        "Santunan Bencana Sosial & Kemanusiaan Non-Ibadah",
        "Lainnya (Tuliskan Manual)"
    )
    var selectedPurpose by remember { mutableStateOf(purposeOptions.first()) }
    var customPurpose by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "acc_bank") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CleanHands, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Sucikan Dana (Tathhir)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Catatan Terindikasi: ${item.description}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Penyucian (Tathhir) mengeluarkan nominal bunga/denda untuk fasilitas umum (Fatwa DSN-MUI No. 123/2018) dari dompet yang Anda pilih — tidak memotong saldo Virtual Infaq Vault.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { ch -> ch.isDigit() }
                        validationError = null
                    },
                    label = { Text("Nominal Porsi Denda/Syubhat (Rp)", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tathhir_dialog_amount_input")
                )

                Text("Sumber Dompet Pembayar:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                if (wallets.isEmpty()) {
                    Text("Menggunakan Kas Utama", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        wallets.forEach { wallet ->
                            val isSelected = selectedWalletId == wallet.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedWalletId = wallet.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isSelected, onClick = { selectedWalletId = wallet.id })
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(wallet.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    Text("Saldo: Rp ${nf.format(getWalletBalance(wallet.id).toLong())}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text("Tujuan Kemaslahatan Fasilitas Umum:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    purposeOptions.forEach { opt ->
                        val isSelected = selectedPurpose == opt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedPurpose = opt }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = { selectedPurpose = opt })
                            Text(opt, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (selectedPurpose.contains("Lainnya")) {
                    OutlinedTextField(
                        value = customPurpose,
                        onValueChange = { customPurpose = it },
                        label = { Text("Sebutkan fasilitas umum tujuan", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Penyucian (Opsional)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(validationError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val finalPurpose = if (selectedPurpose.contains("Lainnya")) {
                        customPurpose.trim().ifBlank { "Fasilitas Sosial & Kemaslahatan Umum" }
                    } else {
                        selectedPurpose
                    }
                    if (amt <= 0.0) {
                        validationError = "Masukkan nominal penyucian yang lebih besar dari 0"
                    } else {
                        onConfirm(amt, finalPurpose, selectedWalletId, notes)
                    }
                },
                modifier = Modifier.testTag("confirm_tathhir_purification_btn")
            ) {
                Text("Sucikan & Tandai Selesai", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}


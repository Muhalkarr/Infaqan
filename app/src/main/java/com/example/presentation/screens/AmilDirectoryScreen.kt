package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.directory.AmilBankAccount
import com.example.core.directory.AmilCategory
import com.example.core.directory.AmilDirectoryRepository
import com.example.core.directory.AmilInstitution
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmilDirectoryScreen(
    viewModel: AmanahLedgerViewModel,
    onBackClick: () -> Unit,
    onNavigateToDisburse: (institutionName: String) -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<AmilCategory?>(null) }

    // Dialog States
    var showAddEditDialog by remember { mutableStateOf(false) }
    var institutionToEdit by remember { mutableStateOf<AmilInstitution?>(null) }
    var institutionToDelete by remember { mutableStateOf<AmilInstitution?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val rawInstitutions = if (state.amilInstitutions.isNotEmpty()) {
        state.amilInstitutions
    } else {
        AmilDirectoryRepository.verifiedInstitutions
    }

    val institutions = remember(rawInstitutions, searchQuery, selectedCategory) {
        rawInstitutions.filter { inst ->
            val matchQuery = searchQuery.isBlank() ||
                    inst.name.contains(searchQuery, ignoreCase = true) ||
                    inst.shortName.contains(searchQuery, ignoreCase = true) ||
                    inst.supportedPrograms.any { it.contains(searchQuery, ignoreCase = true) } ||
                    inst.bankAccounts.any { it.bankName.contains(searchQuery, ignoreCase = true) || it.accountNumber.contains(searchQuery) }
            val matchCat = selectedCategory == null || inst.category == selectedCategory
            matchQuery && matchCat
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Direktori Amil Zakat & ZISWAF",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Lembaga Resmi & Kelola Rekening Penyaluran",
                            fontSize = 11.sp,
                            color = EmeraldLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("amil_dir_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.testTag("btn_reset_amil_dir")
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Reset ke Data Default",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("amil_dir_menu_sidebar_button")
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    institutionToEdit = null
                    showAddEditDialog = true
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_amil")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Lembaga")
                    Text("Tambah Lembaga", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
        ) {
            // Intro Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Penyaluran Aman & Bergaransi Syariah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Salurkan zakat, infaq, & wakaf langsung ke rekening resmi lembaga berizin. Anda dapat menambah, mengedit, atau memperbarui nomor rekening sewaktu-waktu.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari lembaga, bank, no. rekening, atau program...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldLight) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_amil_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Semua (${rawInstitutions.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    FilterChip(
                        selected = selectedCategory == AmilCategory.BAZNAS,
                        onClick = { selectedCategory = AmilCategory.BAZNAS },
                        label = { Text("BAZNAS", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    FilterChip(
                        selected = selectedCategory == AmilCategory.LAZ_NASIONAL,
                        onClick = { selectedCategory = AmilCategory.LAZ_NASIONAL },
                        label = { Text("LAZ Nasional", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    FilterChip(
                        selected = selectedCategory == AmilCategory.LEMBAGA_WAKAF,
                        onClick = { selectedCategory = AmilCategory.LEMBAGA_WAKAF },
                        label = { Text("Wakaf", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            if (institutions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                "Tidak ada lembaga yang sesuai",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                // Institution Cards
                items(institutions, key = { it.id }) { inst ->
                    AmilInstitutionCard(
                        institution = inst,
                        onEditClick = {
                            institutionToEdit = inst
                            showAddEditDialog = true
                        },
                        onDeleteClick = {
                            institutionToDelete = inst
                        },
                        onCopyAccount = { account ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("No Rekening", account.accountNumber)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                "Nomor Rekening ${account.bankName} (${account.accountNumber}) berhasil disalin!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDisburseClick = {
                            onNavigateToDisburse(inst.name)
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AmilInstitutionFormDialog(
            initialInstitution = institutionToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { savedInstitution ->
                if (institutionToEdit == null) {
                    viewModel.addAmilInstitution(savedInstitution)
                } else {
                    viewModel.updateAmilInstitution(savedInstitution)
                }
                showAddEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    institutionToDelete?.let { inst ->
        AlertDialog(
            onDismissRequest = { institutionToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseCoral)
                    Text("Hapus Lembaga Amil?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus '${inst.name}' dari direktori? Tindakan ini akan menghapus semua daftar rekening bank terkait dari direktori lokal Anda.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAmilInstitution(inst.id)
                        institutionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseCoral)
                ) {
                    Text("Hapus Lembaga", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { institutionToDelete = null }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Reset Direktori ke Default?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
            },
            text = {
                Text(
                    "Daftar lembaga amil akan dikembalikan ke data resmi terverifikasi standar (BAZNAS, Dompet Dhuafa, Rumah Zakat, Lazismu, Lazisnu, BSI Maslahat). Perubahan kustom yang Anda buat akan diatur ulang.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAmilInstitutionsToDefault()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Ya, Reset Default", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun AmilInstitutionCard(
    institution: AmilInstitution,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyAccount: (AmilBankAccount) -> Unit,
    onDisburseClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("amil_card_${institution.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top badges and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldPrimary.copy(alpha = 0.18f),
                    border = BorderStroke(0.5.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = institution.verifiedBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_amil_${institution.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Lembaga",
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_amil_${institution.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Lembaga",
                            tint = ExpenseCoral.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = institution.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (institution.skLegalNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = institution.skLegalNumber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (institution.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = institution.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bank Accounts Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rekening Resmi (${institution.bankAccounts.size} Rekening):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            institution.bankAccounts.forEach { acc ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = acc.bankName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    color = GoldAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = acc.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = acc.accountNumber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "a.n ${acc.accountHolder}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onCopyAccount(acc) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary.copy(alpha = 0.25f))
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Salin",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable details (contact, address, confirmation guide)
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    if (institution.confirmationGuide.isNotBlank()) {
                        Column {
                            Text(
                                text = "Panduan Konfirmasi Donasi:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = institution.confirmationGuide,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    if (institution.callCenterWhatsapp.isNotBlank() || institution.websiteUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (institution.callCenterWhatsapp.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldDark.copy(alpha = 0.3f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(14.dp))
                                        Text(institution.callCenterWhatsapp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                            if (institution.websiteUrl.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                                        Text(institution.websiteUrl.removePrefix("https://"), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    if (institution.address.isNotBlank()) {
                        Text(
                            text = "Alamat: ${institution.address}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onDisburseClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salurkan Zakat / Infaq ke Lembaga Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Toggle Expand Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Sembunyikan Informasi Lengkap ▲" else "Lihat Info Kontak & Detail ▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldLight
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmilInstitutionFormDialog(
    initialInstitution: AmilInstitution?,
    onDismiss: () -> Unit,
    onSave: (AmilInstitution) -> Unit
) {
    var name by remember { mutableStateOf(initialInstitution?.name ?: "") }
    var shortName by remember { mutableStateOf(initialInstitution?.shortName ?: "") }
    var selectedCategory by remember { mutableStateOf(initialInstitution?.category ?: AmilCategory.LAZ_NASIONAL) }
    var skLegalNumber by remember { mutableStateOf(initialInstitution?.skLegalNumber ?: "") }
    var verifiedBadge by remember { mutableStateOf(initialInstitution?.verifiedBadge ?: "Lembaga Terverifikasi") }
    var description by remember { mutableStateOf(initialInstitution?.description ?: "") }
    var websiteUrl by remember { mutableStateOf(initialInstitution?.websiteUrl ?: "") }
    var callCenterWhatsapp by remember { mutableStateOf(initialInstitution?.callCenterWhatsapp ?: "") }
    var address by remember { mutableStateOf(initialInstitution?.address ?: "") }
    var confirmationGuide by remember { mutableStateOf(initialInstitution?.confirmationGuide ?: "") }

    // Bank Accounts sub-form state
    var bankAccounts by remember {
        mutableStateOf(
            initialInstitution?.bankAccounts ?: listOf(
                AmilBankAccount("Bank Syariah Indonesia (BSI)", "", "Kas ZISWAF", "Zakat")
            )
        )
    }

    var newBankName by remember { mutableStateOf("") }
    var newAccountNumber by remember { mutableStateOf("") }
    var newAccountHolder by remember { mutableStateOf("") }
    var newAccountCategory by remember { mutableStateOf("Zakat / Infaq") }
    var showAddAccountFields by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialInstitution == null) "Tambah Lembaga Amil" else "Edit Lembaga Amil",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Selection
                    Text("Kategori Lembaga:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AmilCategory.values().forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategory = cat },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = when (cat) {
                                        AmilCategory.BAZNAS -> "BAZNAS"
                                        AmilCategory.LAZ_NASIONAL -> "LAZ"
                                        AmilCategory.LEMBAGA_WAKAF -> "Wakaf"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }

                    val inputColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Institution Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (shortName.isBlank()) shortName = it
                        },
                        label = { Text("Nama Lengkap Lembaga *", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: BAZNAS Kab. Bandung", fontSize = 12.sp) },
                        colors = inputColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_amil_name")
                    )

                    // Short Name & Verified Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = shortName,
                            onValueChange = { shortName = it },
                            label = { Text("Nama Singkat", fontSize = 12.sp) },
                            colors = inputColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = verifiedBadge,
                            onValueChange = { verifiedBadge = it },
                            label = { Text("Label Status", fontSize = 12.sp) },
                            colors = inputColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Legal SK Number
                    OutlinedTextField(
                        value = skLegalNumber,
                        onValueChange = { skLegalNumber = it },
                        label = { Text("SK Kemenag / Akta Izin Legal", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: SK Kemenag RI No. 123/2023", fontSize = 12.sp) },
                        colors = inputColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi & Program Utama", fontSize = 12.sp) },
                        colors = inputColors,
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Bank Accounts Sub-Section
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daftar Rekening Bank (${bankAccounts.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight
                        )
                        TextButton(
                            onClick = { showAddAccountFields = !showAddAccountFields },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(if (showAddAccountFields) Icons.Default.Close else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showAddAccountFields) "Tutup Form" else "+ Tambah Rekening", fontSize = 11.sp, color = EmeraldLight)
                        }
                    }

                    // Bank Account List
                    bankAccounts.forEachIndexed { index, acc ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${acc.bankName} (${acc.category})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(acc.accountNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                                    Text("a.n ${acc.accountHolder}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = {
                                        bankAccounts = bankAccounts.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus Rekening", tint = ExpenseCoral, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Add Bank Account Inline Form
                    if (showAddAccountFields) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Entri Rekening Bank Baru:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                                OutlinedTextField(
                                    value = newBankName,
                                    onValueChange = { newBankName = it },
                                    label = { Text("Nama Bank (misal: BSI / Muamalat / BCA)", fontSize = 11.sp) },
                                    colors = inputColors,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = newAccountNumber,
                                    onValueChange = { newAccountNumber = it },
                                    label = { Text("Nomor Rekening", fontSize = 11.sp) },
                                    colors = inputColors,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = newAccountHolder,
                                        onValueChange = { newAccountHolder = it },
                                        label = { Text("Atas Nama (a.n)", fontSize = 11.sp) },
                                        colors = inputColors,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = newAccountCategory,
                                        onValueChange = { newAccountCategory = it },
                                        label = { Text("Peruntukan", fontSize = 11.sp) },
                                        colors = inputColors,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (newBankName.isNotBlank() && newAccountNumber.isNotBlank()) {
                                            val holder = if (newAccountHolder.isBlank()) name else newAccountHolder
                                            bankAccounts = bankAccounts + AmilBankAccount(
                                                bankName = newBankName.trim(),
                                                accountNumber = newAccountNumber.trim(),
                                                accountHolder = holder.trim(),
                                                category = if (newAccountCategory.isBlank()) "Zakat / Infaq" else newAccountCategory.trim()
                                            )
                                            newBankName = ""
                                            newAccountNumber = ""
                                            newAccountHolder = ""
                                            showAddAccountFields = false
                                        }
                                    },
                                    enabled = newBankName.isNotBlank() && newAccountNumber.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tambahkan ke Daftar Rekening", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Contact & Details
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                    OutlinedTextField(
                        value = callCenterWhatsapp,
                        onValueChange = { callCenterWhatsapp = it },
                        label = { Text("No. WhatsApp / Call Center", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: +628123456789", fontSize = 12.sp) },
                        colors = inputColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = websiteUrl,
                        onValueChange = { websiteUrl = it },
                        label = { Text("Situs Web Resmi / Medsos", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: https://baznas.go.id", fontSize = 12.sp) },
                        colors = inputColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Kantor", fontSize = 12.sp) },
                        colors = inputColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmationGuide,
                        onValueChange = { confirmationGuide = it },
                        label = { Text("Panduan Konfirmasi Donasi", fontSize = 12.sp) },
                        placeholder = { Text("Kirim bukti transfer ke WA resmi untuk mendapatkan BSZ...", fontSize = 12.sp) },
                        colors = inputColors,
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val item = AmilInstitution(
                                    id = initialInstitution?.id ?: "amil_${UUID.randomUUID().toString().take(8)}",
                                    name = name.trim(),
                                    shortName = if (shortName.isBlank()) name.trim() else shortName.trim(),
                                    category = selectedCategory,
                                    skLegalNumber = skLegalNumber.trim(),
                                    verifiedBadge = if (verifiedBadge.isBlank()) "Lembaga Resmi" else verifiedBadge.trim(),
                                    description = description.trim(),
                                    websiteUrl = websiteUrl.trim(),
                                    callCenterWhatsapp = callCenterWhatsapp.trim(),
                                    address = address.trim(),
                                    supportedPrograms = initialInstitution?.supportedPrograms ?: listOf("Zakat Maal", "Zakat Profesi", "Infaq"),
                                    bankAccounts = bankAccounts,
                                    confirmationGuide = if (confirmationGuide.isBlank()) "Konfirmasikan bukti transfer ke pengurus/layanan amil terkait." else confirmationGuide.trim()
                                )
                                onSave(item)
                            }
                        },
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_save_amil"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (initialInstitution == null) "Simpan Lembaga" else "Simpan Perubahan",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

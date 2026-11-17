package com.example.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.Account
import com.example.core.accounting.AccountCategory
import com.example.core.accounting.ExpenseCategoryPreset
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberTacticalGold
import com.example.ui.theme.isTactical

@Composable
fun ExpenseCategorySettingsSection(
    state: AmanahLedgerUiState,
    onToggleCategory: (String) -> Unit,
    onApplyPreset: (ExpenseCategoryPreset) -> Unit,
    onAddNewCategory: (String, String) -> Unit,
    onDeleteCustomCategory: (String) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTactical = state.themeMode.isTactical
    val cardShape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp)
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)

    val expenseAccounts = state.accounts.filter { it.category == AccountCategory.EXPENSE }
    val visibleCount = expenseAccounts.count { it.id !in state.disabledExpenseAccountIds }
    val totalCount = expenseAccounts.size

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Account?>(null) }
    var isAccordionExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_category_settings_card"),
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isTactical) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isAccordionExpanded = !isAccordionExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(if (isTactical) CutCornerShape(4.dp) else CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Ikon Kategori Pengeluaran",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isTactical) "// ATUR KATEGORI PENGELUARAN" else "Kategori Pengeluaran",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isTactical) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Active Count Badge
                        Surface(
                            shape = pillShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.testTag("expense_category_count_badge")
                        ) {
                            Text(
                                text = "$visibleCount / $totalCount Aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Saring opsi agar dropdown pengeluaran cepat dan ringkas",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { isAccordionExpanded = !isAccordionExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isAccordionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Accordion",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isAccordionExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Presets Bar
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Preset Cepat Kategori:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExpenseCategoryPreset.entries.forEach { preset ->
                                val isPresetActive = when (preset) {
                                    ExpenseCategoryPreset.MINIMAL -> {
                                        val minimalExpected = setOf("acc_living", "acc_transport", "acc_utility", "acc_other_exp")
                                        val activeBuiltIn = expenseAccounts.filter { !it.isCustom && it.id !in state.disabledExpenseAccountIds }.map { it.id }.toSet()
                                        activeBuiltIn == minimalExpected
                                    }
                                    ExpenseCategoryPreset.FAMILY -> {
                                        val familyExpected = setOf("acc_living", "acc_transport", "acc_utility", "acc_education", "acc_health", "acc_other_exp")
                                        val activeBuiltIn = expenseAccounts.filter { !it.isCustom && it.id !in state.disabledExpenseAccountIds }.map { it.id }.toSet()
                                        activeBuiltIn == familyExpected
                                    }
                                    ExpenseCategoryPreset.FULL -> {
                                        state.disabledExpenseAccountIds.isEmpty()
                                    }
                                }

                                FilterChip(
                                    selected = isPresetActive,
                                    onClick = { onApplyPreset(preset) },
                                    label = {
                                        Text(
                                            text = preset.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isPresetActive) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (isPresetActive) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    shape = pillShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("expense_preset_${preset.name.lowercase()}")
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Action buttons: Tambah Kategori Baru & Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showAddDialog = true },
                            shape = pillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("add_custom_expense_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Tambah Kategori", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = onResetToDefault,
                            modifier = Modifier.testTag("reset_expense_categories_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Penuh", fontSize = 11.sp)
                        }
                    }

                    // List of Expense Categories
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        expenseAccounts.forEach { acc ->
                            val isVisible = acc.id !in state.disabledExpenseAccountIds
                            ExpenseCategoryItemRow(
                                account = acc,
                                isVisible = isVisible,
                                isTactical = isTactical,
                                onToggle = { onToggleCategory(acc.id) },
                                onDelete = if (acc.isCustom || acc.id.startsWith("acc_custom_exp_")) {
                                    { categoryToDelete = acc }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog Tambah Kategori Kustom
    if (showAddDialog) {
        AddExpenseCategoryDialog(
            isTactical = isTactical,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc ->
                onAddNewCategory(name, desc)
                showAddDialog = false
            }
        )
    }

    // Dialog Konfirmasi Hapus Kategori Kustom
    if (categoryToDelete != null) {
        val target = categoryToDelete!!
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Hapus Kategori '${target.name}'?") },
            text = { Text("Kategori kustom ini akan dihapus dari daftar pilihan. Transaksi lampau yang telah tercatat dengan kategori ini tetap aman tersimpan di riwayat.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCustomCategory(target.id)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus Kategori")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun ExpenseCategoryItemRow(
    account: Account,
    isVisible: Boolean,
    isTactical: Boolean,
    onToggle: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val pillShape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp)
    val isCustom = account.isCustom || account.id.startsWith("acc_custom_exp_")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_category_row_${account.id}"),
        shape = pillShape,
        color = if (isVisible) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isTactical) 0.75f else 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(
            1.dp,
            if (isVisible) {
                if (isTactical) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            } else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            val icon = getExpenseIcon(account.id)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(if (isTactical) CutCornerShape(3.dp) else CircleShape)
                    .background(
                        if (isVisible) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name & Description
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.name,
                        fontSize = 13.sp,
                        fontWeight = if (isVisible) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isVisible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                    if (isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = pillShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Kustom",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                if (account.description.isNotBlank()) {
                    Text(
                        text = account.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isVisible) 0.85f else 0.4f)
                    )
                }
            }

            // Delete button for custom categories
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_category_${account.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Kategori Kustom",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Switch
            Switch(
                checked = isVisible,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("switch_category_${account.id}")
            )
        }
    }
}

@Composable
fun AddExpenseCategoryDialog(
    isTactical: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(16.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isTactical) "// TAMBAH KATEGORI BARU" else "Tambah Kategori Pengeluaran",
                fontWeight = FontWeight.Bold,
                color = if (isTactical) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Buat kategori pengeluaran spesifik sesuai kebutuhan pribadi Anda (misal: 'Belanja Online', 'Cicilan Motor', 'Hobi & Game').",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        if (it.isNotBlank()) errorMessage = null
                    },
                    label = { Text("Nama Kategori (Wajib)") },
                    placeholder = { Text("Contoh: Belanja Online") },
                    isError = errorMessage != null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_category_name")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Keterangan Singkat (Opsional)") },
                    placeholder = { Text("Contoh: E-commerce, fashion, & kurir") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_custom_category_desc")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.trim().isBlank()) {
                        errorMessage = "Nama kategori tidak boleh kosong"
                    } else {
                        onConfirm(nameText.trim(), descText.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("submit_custom_category_button")
            ) {
                Text("Simpan Kategori")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = shape
    )
}

/**
 * BottomSheet interaktif yang dapat dipanggil langsung dari layar Catat Transaksi
 * agar pengguna tidak perlu berpindah ke pengaturan untuk memfilter atau menambah kategori.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCategoryQuickBottomSheet(
    state: AmanahLedgerUiState,
    onDismiss: () -> Unit,
    onToggleCategory: (String) -> Unit,
    onApplyPreset: (ExpenseCategoryPreset) -> Unit,
    onAddNewCategory: (String, String) -> Unit,
    onDeleteCustomCategory: (String) -> Unit,
    onResetToDefault: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isTactical = state.themeMode.isTactical

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("expense_category_quick_bottom_sheet")
        ) {
            ExpenseCategorySettingsSection(
                state = state,
                onToggleCategory = onToggleCategory,
                onApplyPreset = onApplyPreset,
                onAddNewCategory = onAddNewCategory,
                onDeleteCustomCategory = onDeleteCustomCategory,
                onResetToDefault = onResetToDefault
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_category_quick_sheet_button")
            ) {
                Text("Selesai & Gunakan Pilihan", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun getExpenseIcon(accountId: String): ImageVector {
    return when (accountId) {
        "acc_living" -> Icons.Default.Restaurant
        "acc_transport" -> Icons.Default.DirectionsCar
        "acc_utility" -> Icons.Default.FlashOn
        "acc_education" -> Icons.Default.School
        "acc_health" -> Icons.Default.LocalHospital
        "acc_entertainment" -> Icons.Default.SportsEsports
        "acc_other_exp" -> Icons.Default.Category
        "acc_disbursed" -> Icons.Default.VolunteerActivism
        "acc_sedekah_extra" -> Icons.Default.CardGiftcard
        else -> Icons.Default.Label
    }
}

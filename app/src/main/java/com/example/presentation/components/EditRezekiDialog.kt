package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.isTactical
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.rezeki.RezekiCategory
import com.example.core.rezeki.RezekiNonTunaiRecord
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditRezekiDialog(
    record: RezekiNonTunaiRecord,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        giverName: String,
        category: RezekiCategory,
        estimatedValue: Double,
        notes: String
    ) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember(record) { mutableStateOf(record.title) }
    var giverName by remember(record) { mutableStateOf(record.giverName) }
    var category by remember(record) { mutableStateOf(record.category) }
    var estimatedValueText by remember(record) {
        val v = record.estimatedValue
        mutableStateOf(if (v % 1.0 == 0.0) v.toLong().toString() else v.toString())
    }
    var notes by remember(record) { mutableStateOf(record.notes) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    val parsedValue = estimatedValueText.toDoubleOrNull() ?: 0.0
    val suggestedInfaq = parsedValue * record.suggestedInfaqRate

    val isTactical = LocalAppThemeMode.current.isTactical

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = if (isTactical) CutCornerShape(12.dp) else RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp)
                .testTag("edit_rezeki_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(if (isTactical) CutCornerShape(4.dp) else CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Koreksi Catatan Rezeki",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ubah taksiran nominal, nama, atau hapus",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Info status jika sudah ada infaq
                if (record.isPaidInfaq) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Text(
                                text = if (record.isVaultAllocated) {
                                    "Catatan ini telah dialokasikan ke Infaq Vault. Mengubah nominal taksiran akan otomatis mengoreksi saldo komitmen Infaq Vault Anda."
                                } else {
                                    "Infaq syukur atas catatan ini telah ditunaikan secara tunai."
                                },
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Field 1: Nama Rezeki
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Rezeki / Traktiran / Hadiah *") },
                    placeholder = { Text("Misal: Makan Siang Nasi Padang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_rezeki_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Field 2: Diberikan Oleh
                OutlinedTextField(
                    value = giverName,
                    onValueChange = { giverName = it },
                    label = { Text("Diberikan Oleh (Opsional)") },
                    placeholder = { Text("Misal: Rekan Kerja Ahmad") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_rezeki_giver_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Field 3: Kategori
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Kategori Pemberian",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RezekiCategory.values().forEach { cat ->
                            val isSelected = category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                leadingIcon = {
                                    val catIcon = when (cat) {
                                        RezekiCategory.FOOD_BEVERAGE -> Icons.Default.Restaurant
                                        RezekiCategory.GIFT_GOODS -> Icons.Default.CardGiftcard
                                        RezekiCategory.SERVICE_RIDE -> Icons.Default.DirectionsCar
                                        RezekiCategory.VOUCHER_PERK -> Icons.Default.ConfirmationNumber
                                        RezekiCategory.OTHER -> Icons.Default.VolunteerActivism
                                    }
                                    Icon(catIcon, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }
                }

                // Field 4: Taksiran Nilai / Nominal Pasar (Koreksi Utama)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = estimatedValueText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() || it == '.' }) {
                                estimatedValueText = input
                            }
                        },
                        label = { Text("Taksiran Nilai Pasar (Nominal Rp) *") },
                        placeholder = { Text("0") },
                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_rezeki_value_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    if (parsedValue > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Terbaca: Rp ${nf.format(parsedValue.toLong())}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Infaq Syukur (${(record.suggestedInfaqRate * 100).toInt()}%): Rp ${nf.format(suggestedInfaq.toLong())}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Field 5: Catatan
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Doa untuk Pemberi") },
                    placeholder = { Text("Jazakallahu khairan...") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("edit_rezeki_notes_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Simpan & Hapus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tombol Hapus
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("delete_rezeki_btn")
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Tombol Simpan Perubahan
                    Button(
                        onClick = {
                            if (title.isNotBlank() && parsedValue > 0) {
                                onSave(title, giverName, category, parsedValue, notes)
                            }
                        },
                        enabled = title.isNotBlank() && parsedValue > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_rezeki_edit_btn")
                    ) {
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Hapus Catatan Rezeki?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus catatan '${record.title}' senilai Rp ${nf.format(record.estimatedValue.toLong())}? Tindakan ini tidak dapat dibatalkan, dan saldo komitmen Infaq Vault terkait akan dinormalkan kembali.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_rezeki_btn")
                ) {
                    Text("Hapus Permanen", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

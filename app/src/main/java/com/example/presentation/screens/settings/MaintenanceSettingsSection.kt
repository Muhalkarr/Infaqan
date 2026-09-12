package com.example.presentation.screens.settings


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AmanahLedgerUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaintenanceSettingsSection(
    
    state: AmanahLedgerUiState,
    context: Context,
    clipboardManager: ClipboardManager,
    summaryText: String,
    onSetShowDailyHadith: (Boolean) -> Unit,
    onNavigateToInteractiveGuide: () -> Unit,
    onNavigateToBackupRestore: () -> Unit,
    onNavigateToDebugTerminal: () -> Unit,
    onRequestResetConfirmation: () -> Unit,
    onLoadDummyData: () -> Unit
) {
    
    var showAboutDialog by remember { mutableStateOf(false) }
    
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Tentang Aplikasi", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Infaqan Syariah (Amanah Ledger)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Versi: 2.4 (Stable Release)\nArsitektur: 100% Offline & Syariah-Compliant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Dikembangkan Oleh:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Muhammad Abdul Kholik Arrasyid",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "muhammadabdulkholikarrasyid@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Copyright, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "2026 Hak Cipta Dilindungi Undang-Undang.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
    SettingsSectionCard(
        title = "Fiqh Edukasi & Pemeliharaan Data",
        subtitle = "Panduan interaktif pengguna dan pencadangan data kas",
        icon = Icons.Default.MenuBook
    ) {
        SettingsSwitchRow(
            title = "Tampilkan Hadits & Hikmah Harian di Beranda",
            subtitle = "Mutiara nasehat amanah harta dan motivasi kedermawanan",
            checked = state.showDailyHadith,
            testTag = "settings_show_hadith_switch",
            onCheckedChange = onSetShowDailyHadith
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Tombol Akses Panduan Interaktif
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToInteractiveGuide() }
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Buka Panduan Interaktif Pengguna Baru",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pelajari konsep Double-Entry Syariah & Virtual Vault",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "Buka Panduan",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Backup & Restore
        Button(
            onClick = onNavigateToBackupRestore,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_backup_restore_button")
        ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pencadangan & Pemulihan Terenkripsi", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Terminal Log & Diagnostik Sistem
        OutlinedButton(
            onClick = onNavigateToDebugTerminal,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF38BDF8)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_debug_terminal_button")
        ) {
            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Terminal Log Debug & Rekam Galat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Spacer(modifier = Modifier.height(6.dp))

        // Tentang & Hak Cipta
        OutlinedButton(
            onClick = { showAboutDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_about_button")
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Identitas Pengembang & Hak Cipta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }


        // Salin Ringkasan Jurnal
        OutlinedButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(summaryText))
                Toast.makeText(context, "Ringkasan Buku Kas disalin ke Clipboard!", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_copy_summary_button")
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salin Ringkasan Buku Kas ke Clipboard", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Hapus Semua Data Dummy (Buku Kas Bersih)
        OutlinedButton(
            onClick = onRequestResetConfirmation,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_reset_data_button")
        ) {
            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hapus Seluruh Data Dummy (Mulai Buku Kas Bersih)", fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Muat Ulang Data Sampel / Dummy
        OutlinedButton(
            onClick = onLoadDummyData,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_load_dummy_button")
        ) {
            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Muat Ulang Data Contoh / Dummy", fontSize = 12.sp)
        }
    }
}

@Composable
fun EditProfileDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var tempName by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Nama Akun Kas") },
        text = {
            Column {
                Text(
                    text = "Masukkan nama entitas, keluarga, atau yayasan pemilik buku kas:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    placeholder = { Text("Contoh: Kas Keluarga Mukmin") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_profile_name")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tempName.isNotBlank()) onSave(tempName.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan")
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
fun EditInitialDateDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var tempDate by remember { mutableStateOf(initialDate) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Tanggal Awal Buku Kas") },
        text = {
            Column {
                Text(
                    text = "Tentukan tanggal pembukaan / cutoff awal pembukuan kas (format: DD/MM/YYYY):",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempDate,
                    onValueChange = { tempDate = it },
                    singleLine = true,
                    placeholder = { Text("DD/MM/YYYY") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_initial_date")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val todayStr = sdf.format(Date())
                    val startOfMonthStr = "01/" + SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
                    val startOfYearStr = "01/01/" + SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())

                    listOf("Hari Ini" to todayStr, "Awal Bulan" to startOfMonthStr, "Awal Tahun" to startOfYearStr).forEach { (label, dateVal) ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { tempDate = dateVal }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tempDate.isNotBlank()) onSave(tempDate.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan")
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
fun EditInitialBalanceDialog(
    initialBalance: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var tempBalanceStr by remember { mutableStateOf(initialBalance.toLong().toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Saldo Awal (Modal Awal)") },
        text = {
            Column {
                Text(
                    text = "Masukkan total ekuitas / saldo awal kas saat pertama kali memulai pembukuan:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempBalanceStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) tempBalanceStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_initial_balance")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = tempBalanceStr.toDoubleOrNull()
                    if (parsed != null && parsed >= 0) onSave(parsed)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Terapkan")
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
fun EditGoldPriceDialog(
    currentGoldPrice: Double,
    nf: NumberFormat,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var tempPriceStr by remember { mutableStateOf(currentGoldPrice.toLong().toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Harga Acuan Emas") },
        text = {
            Column {
                Text(
                    text = "Harga emas murni per gram saat ini untuk menentukan nisab zakat:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tempPriceStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) tempPriceStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_input_gold_price")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(1450000L, 1485000L, 1500000L).forEach { p ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { tempPriceStr = p.toString() }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Rp ${nf.format(p / 1000)}k",
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = tempPriceStr.toDoubleOrNull()
                    if (parsed != null && parsed > 0) onSave(parsed)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Terapkan")
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
fun ResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmReset: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Hapus Semua Data Dummy?") },
        text = {
            Text(
                "Tindakan ini akan menghapus seluruh data contoh/dummy (transaksi jurnal kas, kantong rekening, anggaran belanja, target ibadah, hutang/piutang qardh, dan riwayat sedekah subuh) sehingga buku kas Anda menjadi bersih 100% untuk mulai mencatat keuangan pribadi secara riil.\n\nCatatan: Anda dapat memuat ulang data contoh kapan saja melalui tombol 'Muat Ulang Data Contoh'.",
                fontSize = 13.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmReset()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_reset_dummy_button")
            ) {
                Text("Ya, Hapus Data Dummy")
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
fun ExportSummaryDialog(
    summaryText: String,
    clipboardManager: ClipboardManager,
    context: Context,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ringkasan Buku Kas") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = summaryText,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(summaryText))
                    Toast.makeText(context, "Ringkasan Buku Kas berhasil disalin!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Salin Teks")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

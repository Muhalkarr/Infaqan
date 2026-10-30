package com.example.presentation.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.core.rezeki.RezekiNonTunaiRecord
import com.example.core.wallet.WalletAccount
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TunaikanInfaqSyukurDialog(
    record: RezekiNonTunaiRecord,
    wallets: List<WalletAccount>,
    getWalletBalance: (String) -> Double,
    onDismiss: () -> Unit,
    onConfirm: (walletId: String, amount: Double, note: String) -> Unit
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    var selectedWalletId by remember {
        mutableStateOf(
            wallets.find { it.isDefault }?.id ?: wallets.firstOrNull()?.id ?: ""
        )
    }

    var infaqAmountText by remember {
        mutableStateOf(record.suggestedInfaqAmount.toLong().toString())
    }

    var noteText by remember {
        mutableStateOf("Infaq Syukur atas ${record.title}")
    }

    val currentAmount = infaqAmountText.toDoubleOrNull() ?: 0.0
    val selectedWalletBalance = getWalletBalance(selectedWalletId)
    val isInsufficientBalance = currentAmount > selectedWalletBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_tunaikan_infaq_syukur"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Tunaikan Infaq Syukur",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Wujud syukur atas rezeki non-tunai",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Rezeki
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = record.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (record.giverName.isNotBlank()) {
                            Text(
                                text = "Pemberi: ${record.giverName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Kategori: ${record.category.displayName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Estimasi Manfaat:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Rp ${nf.format(record.estimatedValue.toLong())}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Nominal Infaq
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Nominal Infaq yang Dikeluarkan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = infaqAmountText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                infaqAmountText = input
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("infaq_syukur_amount_input"),
                        leadingIcon = {
                            Text(
                                text = "Rp",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Quick chip recommendations
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = currentAmount == record.suggestedInfaqAmount,
                            onClick = {
                                infaqAmountText = record.suggestedInfaqAmount.toLong().toString()
                            },
                            label = { Text("Saran: Rp ${nf.format(record.suggestedInfaqAmount.toLong())}", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors()
                        )
                        if (record.suggestedInfaqAmount != 10000.0) {
                            FilterChip(
                                selected = currentAmount == 10000.0,
                                onClick = { infaqAmountText = "10000" },
                                label = { Text("Rp 10 rb", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Pilih Dompet Sumber Dana
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Keluarkan dari Dompet / Kas:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    wallets.forEach { wallet ->
                        val balance = getWalletBalance(wallet.id)
                        val isSelected = wallet.id == selectedWalletId

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedWalletId = wallet.id }
                                .testTag("wallet_option_${wallet.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedWalletId = wallet.id }
                                    )
                                    Column {
                                        Text(
                                            text = wallet.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = wallet.institutionName,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "Rp ${nf.format(balance.toLong())}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (balance < currentAmount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (isInsufficientBalance) {
                        Text(
                            text = "⚠️ Saldo dompet terpilih tidak mencukupi untuk nominal infaq ini.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Catatan
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Keterangan Jurnal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Text(
                    text = "💡 Transaksi ini akan mencatat pengeluaran Infaq riil berpasangan (Beban Infaq & Kas Dompet) tanpa mengubah catatan riwayat penerimaan rezeki non-tunai.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedWalletId.isNotBlank() && currentAmount > 0) {
                        onConfirm(selectedWalletId, currentAmount, noteText)
                    }
                },
                enabled = selectedWalletId.isNotBlank() && currentAmount > 0 && !isInsufficientBalance,
                modifier = Modifier.testTag("confirm_tunaikan_infaq_button")
            ) {
                Text("Tunaikan Infaq")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_tunaikan_infaq_button")
            ) {
                Text("Tunda / Nanti")
            }
        }
    )
}

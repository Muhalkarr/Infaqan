package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.core.infaq.AsnafCategory
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@Composable
fun DisburseInfaqDialog(
    vaultBalance: Double,
    onDismiss: () -> Unit,
    onConfirmWithDetails: (
        amount: Double,
        recipientName: String,
        asnafCategory: AsnafCategory,
        sourceAccountId: String,
        programName: String,
        notes: String
    ) -> Unit
) {
    var amountText by remember { mutableStateOf(if (vaultBalance > 0) vaultBalance.toLong().toString() else "") }
    var recipientName by remember { mutableStateOf("") }
    var selectedAsnaf by remember { mutableStateOf(AsnafCategory.YATIM_DHUAFA) }
    var selectedSourceAccount by remember { mutableStateOf("acc_bank") }
    var programName by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(18.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.VolunteerActivism,
                contentDescription = "Penyaluran Infaq",
                tint = GoldAccent,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Salurkan Dana Infaq & Titipan Amanah",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Vault Balance Info
                Surface(
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saldo Vault Tersedia",
                            fontSize = 12.sp,
                            color = Color.White70
                        )
                        Text(
                            text = "Rp ${formatRupiah(vaultBalance)}",
                            color = GoldAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick percentage pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.25 to "25%", 0.50 to "50%", 1.00 to "100%").forEach { (ratio, label) ->
                        OutlinedButton(
                            onClick = {
                                val calc = (vaultBalance * ratio).toLong()
                                amountText = calc.toString()
                                errorMessage = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, DarkBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = EmeraldLight
                            )
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 1. Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { char -> char.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Nominal Penyaluran (Rp)", color = Color.White70) },
                    prefix = { Text("Rp ", color = GoldAccent, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = Color(0xFF0E1A1C),
                        unfocusedContainerColor = Color(0xFF0E1A1C)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("disburse_amount_input")
                )

                // 2. Recipient Name Field
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = {
                        recipientName = it
                        errorMessage = null
                    },
                    label = { Text("Nama Penerima / Mustahiq / Lembaga", color = Color.White70) },
                    placeholder = { Text("Contoh: Panti Yatim Nurul Huda / Pak Joko", color = Color.White38) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = Color(0xFF0E1A1C),
                        unfocusedContainerColor = Color(0xFF0E1A1C)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("disburse_recipient_input")
                )

                // 3. Asnaf Category Selector
                Text(
                    text = "Golongan Asnaf / Target Mustahiq",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White70
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AsnafCategory.values()) { asnaf ->
                        val isSelected = selectedAsnaf == asnaf
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldPrimary else Color(0xFF0E1A1C),
                            border = BorderStroke(1.dp, if (isSelected) GoldAccent else DarkBorder),
                            modifier = Modifier.clickable { selectedAsnaf = asnaf }
                        ) {
                            Text(
                                text = asnaf.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.White70,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // 4. Source Wallet Account
                Text(
                    text = "Keluarkan Dana Dari Rekening/Kas",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White70
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sources = listOf(
                        "acc_bank" to ("Bank Syariah" to Icons.Default.AccountBalance),
                        "acc_cash" to ("Kas Tunai" to Icons.Default.AccountBalanceWallet)
                    )
                    sources.forEach { (accId, pair) ->
                        val isSelected = selectedSourceAccount == accId
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedSourceAccount = accId },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else Color(0xFF0E1A1C),
                            border = BorderStroke(1.dp, if (isSelected) EmeraldLight else DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = pair.second,
                                    contentDescription = null,
                                    tint = if (isSelected) EmeraldLight else Color.White60,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pair.first,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else Color.White70,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // 5. Program Name / Notes
                OutlinedTextField(
                    value = programName,
                    onValueChange = { programName = it },
                    label = { Text("Nama Program / Keterangan Penyaluran", color = Color.White70) },
                    placeholder = { Text("Contoh: Santunan Paket Sembako Bulanan", color = Color.White38) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = Color(0xFF0E1A1C),
                        unfocusedContainerColor = Color(0xFF0E1A1C)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("disburse_program_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt <= 0.0) {
                        errorMessage = "Masukkan nominal penyaluran yang valid"
                    } else if (amt > vaultBalance) {
                        errorMessage = "Nominal melebihi saldo vault amanah"
                    } else if (recipientName.isBlank()) {
                        errorMessage = "Masukkan nama penerima / mustahiq"
                    } else {
                        onConfirmWithDetails(
                            amt,
                            recipientName.trim(),
                            selectedAsnaf,
                            selectedSourceAccount,
                            programName.ifBlank { "Penyaluran Infaq Mandiri" },
                            notesText
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_disburse_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tunaikan Amanah", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White70)
            ) {
                Text("Batal")
            }
        }
    )
}

fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return formatter.format(amount.toLong())
}

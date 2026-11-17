package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.infaq.AsnafCategory
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.state.AmanahLedgerUiState
import com.example.core.wallet.WalletAccount
import com.example.core.zakat.ZakatFitrahDefaults
import com.example.core.zakat.ZakatFitrahFamilyCalculation
import com.example.core.zakat.ZakatMaalCalculation
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.isTactical
import com.example.ui.theme.rememberAppCardShape
import com.example.ui.theme.rememberAppShape
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ZakatReportExecutiveSummaryCard(
    state: AmanahLedgerUiState,
    nf: NumberFormat,
    onExportPdf: () -> Unit,
    onCopySummary: () -> Unit
) {
    val isTactical = LocalAppThemeMode.current.isTactical
    val cardShape = rememberAppCardShape(16)
    val buttonShape = rememberAppShape(10)

    val totalPayable = state.totalZakatReportPayable
    val disbursed = state.totalZakatDisbursedRecorded
    val remaining = (totalPayable - disbursed).coerceAtLeast(0.0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .border(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), cardShape)
            .padding(16.dp)
            .testTag("zakat_report_summary_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTactical) "ZAKAT COMPLIANCE DOSSIER" else "RINGKASAN KEWAJIBAN ZAKAT",
                    fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.6.sp
                )
                Surface(
                    color = if (remaining <= 0.0 && totalPayable > 0.0) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                    shape = rememberAppShape(6),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = if (totalPayable <= 0.0) "BELUM ADA KEWAJIBAN"
                        else if (remaining <= 0.0) "LUNAS TERCATAT"
                        else "WAJIB DITUNAIKAN",
                        fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Column {
                Text(
                    text = "Total Kewajiban Zakat Gabungan (Maal + Fitrah)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Rp ${nf.format(totalPayable.toLong())}",
                    fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Zakat Maal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Rp ${nf.format(state.zakatMaal.zakatAmount.toLong())}",
                        fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Zakat Fitrah", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Rp ${nf.format(state.zakatFitrah.totalFitrahRupiah.toLong())}",
                        fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Sisa Belum Lunas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "Rp ${nf.format(remaining.toLong())}",
                        fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Sudah ditunaikan tercatat: Rp ${nf.format(disbursed.toLong())}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportPdf,
                    shape = buttonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1.2f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("zakat_report_export_pdf_btn")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Ekspor PDF", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ekspor PDF Resmi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCopySummary,
                    shape = buttonShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("zakat_report_copy_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Salin Ringkasan", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Teks", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZakatMaalReportSection(
    calc: ZakatMaalCalculation,
    nf: NumberFormat,
    onUpdateMaal: (ZakatMaalCalculation) -> Unit,
    onUpdateHaulStartDate: (Long) -> Unit,
    onSyncDatabase: () -> Unit,
    onDisburseMaal: (Double) -> Unit
) {
    val isTactical = LocalAppThemeMode.current.isTactical
    val cardShape = rememberAppCardShape(14)
    val chipShape = rememberAppShape(8)
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")) }

    var showHaulDatePicker by remember { mutableStateOf(false) }
    var goldGramsInput by remember(calc.goldWeightGrams) {
        mutableStateOf(if (calc.goldWeightGrams == 0.0) "" else calc.goldWeightGrams.toString())
    }

    val nisabRatio = if (calc.nisabGold85g > 0) (calc.netTaxableAssets / calc.nisabGold85g).toFloat().coerceIn(0f, 1f) else 0f
    val elapsedDays = calc.elapsedHaulDays()
    val remainingDays = calc.remainingHaulDays()
    val haulProgress = calc.haulProgressRatio()

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("zakat_maal_report_section")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Laporan Rinci Zakat Maal",
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kalkulasi Nisab 85g Emas & Pelacak Haul",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                TextButton(
                    onClick = onSyncDatabase,
                    modifier = Modifier.testTag("zakat_report_sync_db_btn")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Sinkron DB", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sinkron DB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Nisab Status Block
            Surface(
                shape = chipShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, (if (calc.isNisabReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline).copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Nisab (85g Emas)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Surface(
                            color = if (calc.isNisabReached) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            shape = rememberAppShape(4)
                        ) {
                            Text(
                                text = if (calc.isNisabReached) "NISAB TERCAPAI" else "BELUM NISAB",
                                fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (calc.isNisabReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Acuan: Rp ${nf.format(calc.goldPricePerGram.toLong())}/g • Ambang Nisab: Rp ${nf.format(calc.nisabGold85g.toLong())}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LinearProgressIndicator(
                        progress = { nisabRatio },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(chipShape),
                        color = if (calc.isNisabReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Text(
                        text = if (calc.isNisabReached) "Harta bersih Rp ${nf.format(calc.netTaxableAssets.toLong())} (${(nisabRatio * 100).toInt()}% dari nisab)."
                        else "Kurang Rp ${nf.format(calc.shortageToNisab.toLong())} lagi menuju batas nisab.",
                        fontSize = 11.sp,
                        color = if (calc.isNisabReached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Haul Tracker Block
            Surface(
                shape = chipShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pelacak Masa Haul (${calc.haulTargetDays} Hari)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                text = if (calc.haulStartDateMillis > 0L) "Mulai: ${sdf.format(Date(calc.haulStartDateMillis))}" else "Tanggal awal haul belum disetel",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { showHaulDatePicker = true },
                            shape = chipShape,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("zakat_report_haul_date_btn")
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal Haul", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pilih Tanggal", fontSize = 11.sp)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { haulProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(chipShape),
                        color = if (calc.isHaulFulfilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Berjalan: $elapsedDays / ${calc.haulTargetDays} hari", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = if (calc.isHaulFulfilled || remainingDays == 0) "Haul Genap" else "Sisa $remainingDays hari",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (calc.isHaulFulfilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val now = System.currentTimeMillis()
                        val dayMs = 86_400_000L
                        listOf(
                            "Hari Ini" to now,
                            "6 Bln Lalu" to (now - 180L * dayMs),
                            "1 Thn (354H)" to (now - 354L * dayMs),
                            "Reset" to 0L
                        ).forEach { (label, ts) ->
                            Surface(
                                shape = chipShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .clip(chipShape)
                                    .clickable { onUpdateHaulStartDate(ts) }
                            ) {
                                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status Genap Haul (1 Tahun)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = calc.isHaulFulfilled,
                            onCheckedChange = { onUpdateMaal(calc.copy(isHaulFulfilled = it)) },
                            modifier = Modifier.testTag("zakat_report_haul_fulfilled_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (calc.isLunarCalendar) "Kalender Hijriyah (2.5% / 354H)" else "Kalender Masehi (2.577% / 365H)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Switch(
                            checked = calc.isLunarCalendar,
                            onCheckedChange = { onUpdateMaal(calc.copy(isLunarCalendar = it)) },
                            modifier = Modifier.testTag("zakat_report_lunar_switch")
                        )
                    }
                }
            }

            // Neraca Harta Wajib Zakat
            Text("Neraca Harta Wajib Zakat (Tersinkron DB)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            LedgerAssetRow("1. Saldo Kas & Rekening Dompet", "Rp ${nf.format(calc.totalWalletSavings.toLong())}")
            LedgerAssetRow("2. Tabungan Rencana Ibadah", "Rp ${nf.format(calc.totalIbadahSavings.toLong())}")
            LedgerAssetRow("3. Simpanan Emas (${calc.goldWeightGrams}g)", "Rp ${nf.format(calc.preciousMetalsValue.toLong())}")
            LedgerAssetRow("4. Piutang Qardh Lancar", "Rp ${nf.format(calc.tradeableReceivables.toLong())}")
            LedgerAssetRow("5. (-) Hutang Qardh Jatuh Tempo", "-Rp ${nf.format(calc.shortTermLiabilities.toLong())}", isNegative = calc.shortTermLiabilities > 0)

            OutlinedTextField(
                value = goldGramsInput,
                onValueChange = { input ->
                    goldGramsInput = input.filter { it.isDigit() || it == '.' }
                    val g = goldGramsInput.toDoubleOrNull() ?: 0.0
                    onUpdateMaal(calc.copy(goldWeightGrams = g))
                },
                label = { Text("Ubah Berat Emas Simpanan Fisik (Gram)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("zakat_report_gold_grams_input")
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Harta Bersih Kena Zakat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Rp ${nf.format(calc.netTaxableAssets.toLong())}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Zakat Maal Terhitung (${if (calc.isLunarCalendar) "2.5%" else "2.577%"})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Rp ${nf.format(calc.zakatAmount.toLong())}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = { onDisburseMaal(calc.zakatAmount) },
                enabled = calc.zakatAmount > 0.0,
                shape = rememberAppShape(10),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).testTag("zakat_report_disburse_maal_btn")
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tunaikan Zakat Maal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showHaulDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = calc.haulStartDateMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showHaulDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onUpdateHaulStartDate(it) }
                        showHaulDatePicker = false
                    }
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showHaulDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun LedgerAssetRow(label: String, value: String, isNegative: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ZakatFitrahReportSection(
    calc: ZakatFitrahFamilyCalculation,
    nf: NumberFormat,
    onUpdateFitrah: (ZakatFitrahFamilyCalculation) -> Unit,
    onAddDependent: (String, String) -> Unit,
    onToggleDependent: (String) -> Unit,
    onRemoveDependent: (String) -> Unit,
    onDisburseFitrah: (Double) -> Unit
) {
    val isTactical = LocalAppThemeMode.current.isTactical
    val cardShape = rememberAppCardShape(14)
    val chipShape = rememberAppShape(8)

    var showAddDependentDialog by remember { mutableStateOf(false) }
    var foodNameInput by remember(calc.stapleFoodName) { mutableStateOf(calc.stapleFoodName) }
    var regionInput by remember(calc.regionName) { mutableStateOf(calc.regionName) }
    var pricePerUnitInput by remember(calc.ricePricePerKg) { mutableStateOf(calc.ricePricePerKg.toLong().toString()) }
    var baznasCashRateInput by remember(calc.cashRatePerPerson) { mutableStateOf(calc.cashRatePerPerson.toLong().toString()) }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("zakat_fitrah_report_section")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Laporan Zakat Fitrah Keluarga",
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tanggungan Jiwa & Harga Bahan Pokok Lokal",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    shape = rememberAppShape(6),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "${calc.activeDependentsCount} Jiwa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Staple Food Regional Presets
            Text("Preset Bahan Pokok & Harga Lokal:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ZakatFitrahDefaults.STAPLE_FOOD_PRESETS.forEach { preset ->
                    val isSelected = calc.stapleFoodName == preset.foodName && calc.measurementUnit == preset.measurementUnit
                    Surface(
                        shape = chipShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .clip(chipShape)
                            .clickable {
                                onUpdateFitrah(
                                    calc.copy(
                                        stapleFoodName = preset.foodName,
                                        regionName = preset.regionName,
                                        measurementUnit = preset.measurementUnit,
                                        riceAmountPerPersonKg = preset.amountPerPerson,
                                        ricePricePerKg = preset.pricePerUnit,
                                        useLocalStaplePrice = true
                                    )
                                )
                            }
                            .testTag("fitrah_preset_${preset.id}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Text(preset.foodName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            Text("Rp ${nf.format(preset.pricePerUnit.toLong())}/${preset.measurementUnit} • ${preset.regionName}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Unit toggle (2.5 kg vs 3.5 liter)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("kg" to 2.5 to "Takaran 2.5 kg", "liter" to 3.5 to "Takaran 3.5 Liter").forEach { (pair, label) ->
                    val (unit, amt) = pair
                    val isSel = calc.measurementUnit == unit
                    Surface(
                        shape = chipShape,
                        color = if (isSel) MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                            .clip(chipShape)
                            .clickable { onUpdateFitrah(calc.copy(measurementUnit = unit, riceAmountPerPersonKg = amt)) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Text(label, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, color = if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = foodNameInput,
                onValueChange = {
                    foodNameInput = it
                    onUpdateFitrah(calc.copy(stapleFoodName = it))
                },
                label = { Text("Nama Bahan Pokok Konsumsi") },
                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = regionInput,
                onValueChange = {
                    regionInput = it
                    onUpdateFitrah(calc.copy(regionName = it))
                },
                label = { Text("Wilayah / Pasar Acuan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hitung dari Harga Bahan Pokok Lokal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Otomatis mengalikan takaran (${calc.riceAmountPerPersonKg} ${calc.measurementUnit}) x harga pasar lokal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = calc.useLocalStaplePrice,
                    onCheckedChange = { onUpdateFitrah(calc.copy(useLocalStaplePrice = it)) },
                    modifier = Modifier.testTag("fitrah_local_price_switch")
                )
            }

            if (calc.useLocalStaplePrice) {
                OutlinedTextField(
                    value = pricePerUnitInput,
                    onValueChange = {
                        pricePerUnitInput = it.filter { ch -> ch.isDigit() }
                        val p = pricePerUnitInput.toDoubleOrNull() ?: 0.0
                        onUpdateFitrah(calc.copy(ricePricePerKg = p))
                    },
                    label = { Text("Harga Bahan Pokok Lokal (Rp / ${calc.measurementUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("fitrah_local_price_input")
                )
            } else {
                OutlinedTextField(
                    value = baznasCashRateInput,
                    onValueChange = {
                        baznasCashRateInput = it.filter { ch -> ch.isDigit() }
                        val p = baznasCashRateInput.toDoubleOrNull() ?: 0.0
                        onUpdateFitrah(calc.copy(cashRatePerPerson = p))
                    },
                    label = { Text("Tarif Tetap Zakat Fitrah / Jiwa (SK BAZNAS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("fitrah_baznas_rate_input")
                )
            }

            // Formula preview
            Surface(
                shape = chipShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (calc.useLocalStaplePrice) {
                        "Formula/Jiwa: ${calc.riceAmountPerPersonKg} ${calc.measurementUnit} × Rp ${nf.format(calc.ricePricePerKg.toLong())} = Rp ${nf.format(calc.ratePerSoulRupiah.toLong())} / jiwa"
                    } else {
                        "Tarif Tetap BAZNAS: Rp ${nf.format(calc.ratePerSoulRupiah.toLong())} / jiwa (setara ${calc.riceAmountPerPersonKg} ${calc.measurementUnit})"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Dependents Header & Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daftar Tanggungan Jiwa (${calc.activeDependentsCount}/${calc.dependents.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { showAddDependentDialog = true },
                    shape = chipShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("fitrah_add_dependent_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Tanggungan", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (calc.dependents.isEmpty()) {
                Text(
                    text = "Belum ada anggota tanggungan terdaftar. Ketuk Tambah untuk mendaftarkan diri & keluarga.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    calc.dependents.forEach { dep ->
                        Surface(
                            shape = chipShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (dep.isIncluded) 0.6f else 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth().testTag("fitrah_dependent_${dep.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = dep.isIncluded,
                                    onCheckedChange = { onToggleDependent(dep.id) }
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dep.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (dep.isIncluded) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(dep.relation, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = { onRemoveDependent(dep.id) },
                                    modifier = Modifier.size(40.dp).testTag("fitrah_remove_dep_${dep.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus ${dep.name}", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Kebutuhan Bahan Pokok", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${String.format(Locale.US, "%.1f", calc.totalRiceKg)} ${calc.measurementUnit} ${calc.stapleFoodName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Zakat Fitrah Tunai", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Text("Rp ${nf.format(calc.totalFitrahRupiah.toLong())}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.secondary)
            }

            Button(
                onClick = { onDisburseFitrah(calc.totalFitrahRupiah) },
                enabled = calc.totalFitrahRupiah > 0.0,
                shape = rememberAppShape(10),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp).testTag("zakat_report_disburse_fitrah_btn")
            ) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tunaikan Zakat Fitrah (${calc.activeDependentsCount} Jiwa)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    if (showAddDependentDialog) {
        var newName by remember { mutableStateOf("") }
        var newRelation by remember { mutableStateOf("Anak Tanggungan") }

        AlertDialog(
            onDismissRequest = { showAddDependentDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Tambah Tanggungan Zakat Fitrah", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nama Anggota Keluarga") },
                        placeholder = { Text("cth: Ahmad / Ibu Siti") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("fitrah_new_dep_name_input")
                    )
                    OutlinedTextField(
                        value = newRelation,
                        onValueChange = { newRelation = it },
                        label = { Text("Hubungan / Keterangan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Diri Sendiri", "Pasangan", "Anak", "Orang Tua", "Tanggungan Lain").forEach { rel ->
                            Surface(
                                shape = chipShape,
                                color = if (newRelation == rel) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clip(chipShape).clickable { newRelation = rel }
                            ) {
                                Text(rel, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddDependent(newName, newRelation)
                        showAddDependentDialog = false
                    },
                    enabled = newName.isNotBlank(),
                    modifier = Modifier.testTag("fitrah_confirm_add_dep_btn")
                ) { Text("Tambahkan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDependentDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun ZakatReportDisburseDialog(
    presetTitle: String,
    presetAmount: Double,
    wallets: List<WalletAccount>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, asnaf: AsnafCategory, accountId: String, recipient: String, notes: String, receipt: ReceiptAttachment?) -> Unit
) {
    var title by remember { mutableStateOf(presetTitle) }
    var amountText by remember { mutableStateOf(presetAmount.toLong().toString()) }
    var selectedAsnaf by remember { mutableStateOf(AsnafCategory.FAKIR_MISKIN) }
    var recipientName by remember { mutableStateOf("") }
    var selectedWalletId by remember { mutableStateOf(wallets.firstOrNull()?.id ?: "") }
    var notes by remember { mutableStateOf("") }

    var asnafExpanded by remember { mutableStateOf(false) }
    var walletExpanded by remember { mutableStateOf(false) }

    var attachReceipt by remember { mutableStateOf(false) }
    var receiptRef by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tunaikan & Salurkan Zakat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nama Program Penyaluran") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Zakat (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedAsnaf.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Golongan Mustahiq (8 Asnaf)") },
                        trailingIcon = {
                            IconButton(onClick = { asnafExpanded = !asnafExpanded }) {
                                Icon(if (asnafExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Pilih Asnaf")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(expanded = asnafExpanded, onDismissRequest = { asnafExpanded = false }) {
                        AsnafCategory.entries.forEach { asnaf ->
                            DropdownMenuItem(text = { Text(asnaf.displayName) }, onClick = { selectedAsnaf = asnaf; asnafExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Nama Penerima / Lembaga Amil") },
                    placeholder = { Text("cth: BAZNAS, LAZISMU, Masjid Al-Ikhlas") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (wallets.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentWallet = wallets.firstOrNull { it.id == selectedWalletId }
                        OutlinedTextField(
                            value = currentWallet?.name ?: "Pilih Kantong Sumber",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dibayar dari Kantong") },
                            trailingIcon = {
                                IconButton(onClick = { walletExpanded = !walletExpanded }) {
                                    Icon(if (walletExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Pilih Kantong")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = walletExpanded, onDismissRequest = { walletExpanded = false }) {
                            wallets.forEach { w ->
                                DropdownMenuItem(text = { Text("${w.name} (${w.institutionName})") }, onClick = { selectedWalletId = w.id; walletExpanded = false })
                            }
                        }
                    }
                }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Keterangan & Niat Penyaluran") }, modifier = Modifier.fillMaxWidth())
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Lampirkan Tanda Terima Amil", fontSize = 13.sp)
                    Switch(checked = attachReceipt, onCheckedChange = { attachReceipt = it })
                }
                if (attachReceipt) {
                    OutlinedTextField(value = receiptRef, onValueChange = { receiptRef = it }, label = { Text("No. Kuitansi BAZNAS/LAZ") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull() ?: 0.0
            val isValid = amount > 0.0 && recipientName.isNotBlank()
            Button(
                onClick = {
                    val wallet = wallets.firstOrNull { it.id == selectedWalletId }
                    val accountId = wallet?.linkedAccountId ?: "acc_cash"
                    val receipt = if (attachReceipt) {
                        ReceiptAttachment(
                            title = "Kuitansi Zakat: $title",
                            receiptType = ReceiptType.AMIL_ZAKAT_RECEIPT,
                            referenceNumber = receiptRef.ifBlank { "KW-ZAKAT-${System.currentTimeMillis() % 100000}" },
                            amount = amount,
                            notes = "Penyaluran zakat via $recipientName"
                        )
                    } else null
                    onConfirm(title, amount, selectedAsnaf, accountId, recipientName, notes, receipt)
                },
                enabled = isValid
            ) { Text("Tunaikan Zakat") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

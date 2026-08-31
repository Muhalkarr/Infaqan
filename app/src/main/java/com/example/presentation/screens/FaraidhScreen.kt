package com.example.presentation.screens

import androidx.compose.foundation.background
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
import com.example.core.faraidh.DeceasedGender
import com.example.core.faraidh.FaraidhEngine
import com.example.core.faraidh.FaraidhShare
import com.example.core.faraidh.HeirInput
import com.example.core.state.AmanahLedgerViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaraidhScreen(
    viewModel: AmanahLedgerViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val rupiahFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply { maximumFractionDigits = 0 }
    }

    var grossEstateText by remember { mutableStateOf(uiState.totalAssets.toLong().toString()) }
    var funeralCostText by remember { mutableStateOf("5000000") }
    var debtsText by remember { mutableStateOf("0") }
    var wasiatText by remember { mutableStateOf("0") }

    var gender by remember { mutableStateOf(DeceasedGender.PRIA) }
    var hasSpouse by remember { mutableStateOf(true) }
    var wifeCount by remember { mutableIntStateOf(1) }
    var hasFather by remember { mutableStateOf(true) }
    var hasMother by remember { mutableStateOf(true) }
    var sonCount by remember { mutableIntStateOf(1) }
    var daughterCount by remember { mutableIntStateOf(2) }
    var brotherCount by remember { mutableIntStateOf(0) }
    var sisterCount by remember { mutableIntStateOf(0) }

    val calculationResult = remember(
        grossEstateText, funeralCostText, debtsText, wasiatText,
        gender, hasSpouse, wifeCount, hasFather, hasMother,
        sonCount, daughterCount, brotherCount, sisterCount
    ) {
        val gross = grossEstateText.toDoubleOrNull() ?: 0.0
        val funeral = funeralCostText.toDoubleOrNull() ?: 0.0
        val debts = debtsText.toDoubleOrNull() ?: 0.0
        val wasiat = wasiatText.toDoubleOrNull() ?: 0.0

        val input = HeirInput(
            deceasedGender = gender,
            hasSpouse = hasSpouse,
            wifeCount = wifeCount,
            sonCount = sonCount,
            daughterCount = daughterCount,
            hasFather = hasFather,
            hasMother = hasMother,
            fullBrotherCount = brotherCount,
            fullSisterCount = sisterCount,
            funeralExpenses = funeral,
            deceasedDebts = debts,
            wasiatBequest = wasiat
        )
        FaraidhEngine.calculate(gross, input)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kalkulator Waris Syariah", fontWeight = FontWeight.Bold)
                        Text(
                            "Faraidh Engine (QS. An-Nisa: 11-12, 176)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("faraidh_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
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
                                Icons.Default.Balance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ketetapan Hukum Waris Islam",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "\"(Pembagian tersebut adalah) ketetapan dari Allah. Sungguh, Allah Maha Mengetahui, Maha Bijaksana.\" (QS. An-Nisa: 11)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Input Harta Tirkah
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "1. Harta Tirkah & Kewajiban Jenazah",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = grossEstateText,
                            onValueChange = { grossEstateText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Total Harta Kotor / Aset Tirkah (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        FilledTonalButton(
                            onClick = {
                                grossEstateText = uiState.totalAssets.toLong().toString()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gunakan Total Kas & Aset Saat Ini (${rupiahFormat.format(uiState.totalAssets)})", style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = funeralCostText,
                            onValueChange = { funeralCostText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Biaya Pemulasaran Jenazah (Tajhiz)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = debtsText,
                            onValueChange = { debtsText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Hutang Pewaris yang Wajib Dilunasi (Dain)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = wasiatText,
                            onValueChange = { wasiatText = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Wasiat Pewaris (Maksimal 1/3 Harta Bersih)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Input Ahli Waris
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "2. Profil Pewaris & Ahli Waris",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Jenis Kelamin Pewaris (Al-Mayyit):", style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = gender == DeceasedGender.PRIA,
                                onClick = { gender = DeceasedGender.PRIA },
                                label = { Text("Laki-laki (Suami / Ayah)") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = gender == DeceasedGender.WANITA,
                                onClick = { gender = DeceasedGender.WANITA },
                                label = { Text("Perempuan (Istri / Ibu)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pasangan
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (gender == DeceasedGender.PRIA) "Ada Istri yang Ditinggalkan" else "Ada Suami yang Ditinggalkan",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(checked = hasSpouse, onCheckedChange = { hasSpouse = it })
                        }

                        // Orang Tua
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ayah Kandung Masih Hidup", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = hasFather, onCheckedChange = { hasFather = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ibu Kandung Masih Hidup", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = hasMother, onCheckedChange = { hasMother = it })
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Counter Anak
                        CounterRow(
                            label = "Jumlah Anak Laki-Laki",
                            count = sonCount,
                            onIncrement = { sonCount++ },
                            onDecrement = { if (sonCount > 0) sonCount-- }
                        )

                        CounterRow(
                            label = "Jumlah Anak Perempuan",
                            count = daughterCount,
                            onIncrement = { daughterCount++ },
                            onDecrement = { if (daughterCount > 0) daughterCount-- }
                        )

                        CounterRow(
                            label = "Saudara Laki-Laki Kandung",
                            count = brotherCount,
                            onIncrement = { brotherCount++ },
                            onDecrement = { if (brotherCount > 0) brotherCount-- }
                        )

                        CounterRow(
                            label = "Saudara Perempuan Kandung",
                            count = sisterCount,
                            onIncrement = { sisterCount++ },
                            onDecrement = { if (sisterCount > 0) sisterCount-- }
                        )
                    }
                }
            }

            // Calculation Results
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "3. Ringkasan Harta Bersih Siap Bagi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Harta Bersih Warisan:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                rupiahFormat.format(calculationResult.netDistributableEstate),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Ahli Waris Portions
            items(calculationResult.shares) { share ->
                FaraidhShareCard(share = share, rupiahFormat = rupiahFormat)
            }
        }
    }
}

@Composable
fun CounterRow(
    label: String,
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
            }
            Text(
                count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            FilledTonalIconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FaraidhShareCard(
    share: FaraidhShare,
    rupiahFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    share.heirGroup,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = share.portionFractionText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "Total Bagian Kelompok:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        rupiahFormat.format(share.totalNominal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }

                if (share.heirCount > 1) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Per Orang (${share.heirCount} Ahli Waris):",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            rupiahFormat.format(share.perPersonNominal),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                share.dalilSyariah,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

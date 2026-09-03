package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.AccountCategory
import com.example.core.infaq.InfaqCalculationType
import com.example.core.infaq.InfaqRule
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.White12
import com.example.ui.theme.White38
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfaqRulesScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0E1A1C)
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("rules_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Aturan Otomasi Infaq & Penyucian",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("rules_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldAccent,
                contentColor = Color(0xFF1E1A00),
                modifier = Modifier.testTag("add_rule_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Aturan Infaq")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Konfigurasi IFTTT Ibadah Finansial",
                    fontSize = 13.sp,
                    color = Color.White60
                )
            }

            items(state.rules, key = { it.id }) { rule ->
                InfaqRuleItemCard(
                    rule = rule,
                    onDelete = { viewModel.deleteRule(rule.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    if (showAddDialog) {
        AddRuleDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newRule ->
                viewModel.addRule(newRule)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun InfaqRuleItemCard(
    rule: InfaqRule,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Target: ${rule.targetCategory.displayName}",
                    fontSize = 10.sp,
                    color = Color.White60
                )
                Text(
                    text = "Tipe: ${rule.calculationType.displayName}",
                    fontSize = 10.sp,
                    color = EmeraldLight
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Aturan",
                    tint = Color.White38,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (InfaqRule) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(AccountCategory.INCOME_KASAB) }
    var categoryExpanded by remember { mutableStateOf(false) }

    var selectedCalcType by remember { mutableStateOf(InfaqCalculationType.PERCENTAGE) }
    var calcTypeExpanded by remember { mutableStateOf(false) }

    var percentageRate by remember { mutableDoubleStateOf(0.05) }
    var roundUpStep by remember { mutableDoubleStateOf(5000.0) }
    var enableFriday by remember { mutableStateOf(true) }
    var enableRamadan by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "Tambah Aturan Infaq Baru",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Aturan", color = Color.White70) },
                    placeholder = { Text("Contoh: Infaq Freelance 7%", color = Color.White38) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedContainerColor = Color(0xFF0E1A1C),
                        unfocusedContainerColor = Color(0xFF0E1A1C)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("rule_title_input")
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Target", color = Color.White70) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = Color(0xFF0E1A1C),
                            unfocusedContainerColor = Color(0xFF0E1A1C)
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        listOf(AccountCategory.INCOME_KASAB, AccountCategory.INCOME_NON_KASAB, AccountCategory.EXPENSE).forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Calculation Type dropdown
                ExposedDropdownMenuBox(
                    expanded = calcTypeExpanded,
                    onExpandedChange = { calcTypeExpanded = !calcTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCalcType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Formula Perhitungan", color = Color.White70) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = calcTypeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = Color(0xFF0E1A1C),
                            unfocusedContainerColor = Color(0xFF0E1A1C)
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = calcTypeExpanded,
                        onDismissRequest = { calcTypeExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        InfaqCalculationType.entries.forEach { ct ->
                            DropdownMenuItem(
                                text = { Text(ct.displayName, color = Color.White, fontSize = 12.sp) },
                                onClick = {
                                    selectedCalcType = ct
                                    calcTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedCalcType == InfaqCalculationType.PERCENTAGE) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Persentase Infaq:",
                            color = Color.White70,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", percentageRate * 100).removeSuffix(".0")}%",
                            color = EmeraldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.025 to "2.5%", 0.05 to "5%", 0.10 to "10%", 0.20 to "20%").forEach { (rate, label) ->
                            val isSel = Math.abs(percentageRate - rate) < 0.001
                            androidx.compose.material3.Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) EmeraldPrimary else Color(0xFF142426),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) GoldAccent else DarkBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { percentageRate = rate }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else Color.White70
                                    )
                                }
                            }
                        }
                    }

                    Slider(
                        value = percentageRate.toFloat(),
                        onValueChange = { percentageRate = (Math.round(it * 200.0) / 200.0).coerceIn(0.01, 1.0) },
                        valueRange = 0.01f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldLight,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = DarkBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = InfaqRule(
                        id = "rule_${UUID.randomUUID().toString().take(6)}",
                        title = title.ifBlank { "Aturan Infaq Kustom" },
                        targetCategory = selectedCategory,
                        calculationType = selectedCalcType,
                        rate = percentageRate,
                        roundUpStep = roundUpStep,
                        enableFridayMultiplier = enableFriday,
                        enableRamadanMultiplier = enableRamadan
                    )
                    onAdd(r)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Simpan Aturan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.White60)
            }
        }
    )
}

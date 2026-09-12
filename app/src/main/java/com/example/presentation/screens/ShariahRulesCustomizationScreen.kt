package com.example.presentation.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.shariah.*
import com.example.core.state.AmanahLedgerViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShariahRulesCustomizationScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val shariahConfig = uiState.shariahConfig
    val rulings = uiState.shariahRulings

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Variabel Hisab, 1: Acuan Dalil & Fatwa
    var showAddRulingDialog by remember { mutableStateOf(false) }
    var editingRuling by remember { mutableStateOf<ShariahRuling?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pusat Kustomisasi Syariah",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Rules Engine & Acuan Hukum Islam",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("shariah_rules_back_button")
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
                        modifier = Modifier.testTag("reset_shariah_rules_button")
                    ) {
                        Icon(
                            Icons.Default.RestartAlt,
                            contentDescription = "Reset Standar",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddRulingDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_shariah_ruling_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Fatwa/Dalil")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selector
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Variabel Hisab", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    modifier = Modifier.testTag("tab_hisab_variables")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dalil & Fatwa (${rulings.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    modifier = Modifier.testTag("tab_dalil_rulings")
                )
            }

            if (selectedTab == 0) {
                HisabVariablesCustomizationContent(
                    config = shariahConfig,
                    onSaveConfig = { updated -> viewModel.updateShariahConfig(updated) }
                )
            } else {
                DalilAndFatwaCatalogContent(
                    rulings = rulings,
                    onToggleEnabled = { viewModel.toggleRulingEnabled(it) },
                    onEditRuling = { editingRuling = it },
                    onDeleteRuling = { viewModel.deleteShariahRuling(it) }
                )
            }
        }
    }

    // Dialog Tambah / Edit Fatwa
    if (showAddRulingDialog) {
        AddEditRulingDialog(
            initialRuling = null,
            onDismiss = { showAddRulingDialog = false },
            onSave = { newRuling ->
                viewModel.saveShariahRuling(newRuling)
                showAddRulingDialog = false
            }
        )
    }

    if (editingRuling != null) {
        AddEditRulingDialog(
            initialRuling = editingRuling,
            onDismiss = { editingRuling = null },
            onSave = { updatedRuling ->
                viewModel.saveShariahRuling(updatedRuling)
                editingRuling = null
            }
        )
    }

    // Dialog Konfirmasi Reset
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Kembalikan ke Standar Syariah?") },
            text = {
                Text("Semua variabel hisab (nisab 85g emas, tarif 2.5%, Mazhab Syafi'i) dan katalog fatwa DSN-MUI akan dikembalikan ke pengaturan acuan resmi.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateShariahConfig(ShariahRulesConfig())
                        viewModel.resetShariahRulingsToDefault()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Reset ke Default")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Tab 1: Kustomisasi Variabel & Parameter Hisab
 */
@Composable
fun HisabVariablesCustomizationContent(
    config: ShariahRulesConfig,
    onSaveConfig: (ShariahRulesConfig) -> Unit
) {
    var currentConfig by remember(config) { mutableStateOf(config) }

    var goldNisabInput by remember(config.goldNisabGram) { mutableStateOf(config.goldNisabGram.toString()) }
    var silverNisabInput by remember(config.silverNisabGram) { mutableStateOf(config.silverNisabGram.toString()) }
    var zakatRateInput by remember(config.zakatPercentage) { mutableStateOf(config.zakatPercentage.toString()) }

    var selectedMazhab by remember(config.selectedMazhab) { mutableStateOf(config.selectedMazhab) }
    var profesiFormula by remember(config.zakatProfesiFormula) { mutableStateOf(config.zakatProfesiFormula) }
    var haulMethod by remember(config.haulCalculationMethod) { mutableStateOf(config.haulCalculationMethod) }

    var isCustomFormulaActive by remember(config.isCustomFormulaActive) { mutableStateOf(config.isCustomFormulaActive) }
    var customFormulaName by remember(config.customFormulaName) { mutableStateOf(config.customFormulaName) }
    var customFormulaText by remember(config.customCalculationFormula) { mutableStateOf(config.customCalculationFormula) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner Info
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Balance, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Kemandirian Ijtihad & Kustomisasi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Aplikasi mengizinkan adaptasi parameter hisab sesuai fatwa daerah atau preferensi mazhab Anda.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bagian 1: Pilihan Mazhab Fikih
        Text(
            "1. PILIHAN KETENTUAN MAZHAB FIKIH",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 0.5.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShariahMazhab.entries.forEach { mazhab ->
                    val isSelected = (mazhab == selectedMazhab)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedMazhab = mazhab
                                // Auto-adjust parameters according to mazhab defaults if appropriate
                                if (mazhab == ShariahMazhab.HANAFI) {
                                    silverNisabInput = "595.0"
                                }
                            }
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedMazhab = mazhab }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                mazhab.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                mazhab.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Bagian 2: Parameter Bobot Gram Nisab & Tarif
        Text(
            "2. VARIABEL BOBOT NISAB & PERSENTASE TARIF",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 0.5.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Nisab Emas
                OutlinedTextField(
                    value = goldNisabInput,
                    onValueChange = { goldNisabInput = it },
                    label = { Text("Bobot Nisab Emas (Gram)") },
                    supportingText = { Text("Standar Jumhur: 85 gram emas 24 karat (20 Dinar)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("Gram", modifier = Modifier.padding(end = 12.dp), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("input_gold_nisab_gram")
                )

                // Nisab Perak
                OutlinedTextField(
                    value = silverNisabInput,
                    onValueChange = { silverNisabInput = it },
                    label = { Text("Bobot Nisab Perak (Gram)") },
                    supportingText = { Text("Standar Jumhur: 595 gram perak murni (200 Dirham)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("Gram", modifier = Modifier.padding(end = 12.dp), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("input_silver_nisab_gram")
                )

                // Tarif Zakat %
                OutlinedTextField(
                    value = zakatRateInput,
                    onValueChange = { zakatRateInput = it },
                    label = { Text("Tarif Zakat Standar (%)") },
                    supportingText = { Text("Standar Syariah: 2.5% (Tahun Hijriah) atau 2.577% (Tahun Masehi)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("%", modifier = Modifier.padding(end = 12.dp), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("input_zakat_rate_percent")
                )
            }
        }

        // Bagian 3: Formula Zakat Profesi (Bruto vs Netto)
        Text(
            "3. METODE FORMULA ZAKAT PROFESI / PENGHASILAN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 0.5.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ZakatProfesiFormula.entries.forEach { f ->
                    val isChosen = (f == profesiFormula)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { profesiFormula = f }
                            .background(if (isChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isChosen,
                            onClick = { profesiFormula = f }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                f.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                f.formulaDesc,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Bagian 4: Metode Haul (Hijriah vs Masehi)
        Text(
            "4. METODE PERHITUNGAN HAUL",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 0.5.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HaulCalculationMethod.entries.forEach { h ->
                    val isChosen = (h == haulMethod)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haulMethod = h
                                if (h == HaulCalculationMethod.MASEHI) {
                                    zakatRateInput = "2.577"
                                } else {
                                    zakatRateInput = "2.5"
                                }
                            }
                            .background(if (isChosen) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isChosen,
                            onClick = {
                                haulMethod = h
                                if (h == HaulCalculationMethod.MASEHI) zakatRateInput = "2.577" else zakatRateInput = "2.5"
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                h.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                h.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Bagian 5: Formula Kalkulasi Kustom Pribadi
        Text(
            "5. FORMULA RUMUS KALKULASI KUSTOM",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 0.5.sp
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gunakan Rumus Kustom Sendiri", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Terapkan formula matematika spesifik untuk zakat/infaq", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isCustomFormulaActive,
                        onCheckedChange = { isCustomFormulaActive = it },
                        modifier = Modifier.testTag("toggle_custom_formula_switch")
                    )
                }

                AnimatedVisibility(visible = isCustomFormulaActive) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = customFormulaName,
                            onValueChange = { customFormulaName = it },
                            label = { Text("Nama Formula / Aturan") },
                            placeholder = { Text("Contoh: Zakat Omzet Bisnis UMKM") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customFormulaText,
                            onValueChange = { customFormulaText = it },
                            label = { Text("Ekspresi Rumus") },
                            placeholder = { Text("Contoh: (pendapatan_kotor - hutang_operasional) * 0.025") },
                            supportingText = { Text("Variabel tersedia: pendapatan, pengeluaran, hutang, nisab, aset") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Tombol Simpan
        Button(
            onClick = {
                val goldGram = goldNisabInput.toDoubleOrNull() ?: 85.0
                val silverGram = silverNisabInput.toDoubleOrNull() ?: 595.0
                val rate = zakatRateInput.toDoubleOrNull() ?: 2.5

                val newConfig = ShariahRulesConfig(
                    goldNisabGram = goldGram,
                    silverNisabGram = silverGram,
                    zakatPercentage = rate,
                    zakatProfesiFormula = profesiFormula,
                    haulCalculationMethod = haulMethod,
                    selectedMazhab = selectedMazhab,
                    customCalculationFormula = customFormulaText,
                    isCustomFormulaActive = isCustomFormulaActive,
                    customFormulaName = customFormulaName
                )
                onSaveConfig(newConfig)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("save_shariah_config_button")
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Perubahan Parameter", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Tab 2: Katalog Dalil & Fatwa Lembaga (DSN-MUI, BAZNAS, DPS)
 */
@Composable
fun DalilAndFatwaCatalogContent(
    rulings: List<ShariahRuling>,
    onToggleEnabled: (String) -> Unit,
    onEditRuling: (ShariahRuling) -> Unit,
    onDeleteRuling: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("SEMUA") }
    val categories = listOf("SEMUA", "ZAKAT", "FARAIDH", "QARDH", "INFAQ", "TRANSAKSI", "LAINNYA")

    val filteredRulings = remember(rulings, selectedCategory) {
        if (selectedCategory == "SEMUA") rulings else rulings.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }

        if (filteredRulings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada rujukan hukum dalam kategori ini.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredRulings, key = { it.id }) { ruling ->
                ShariahRulingCard(
                    ruling = ruling,
                    onToggle = { onToggleEnabled(ruling.id) },
                    onEdit = { onEditRuling(ruling) },
                    onDelete = { onDeleteRuling(ruling.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}

@Composable
fun ShariahRulingCard(
    ruling: ShariahRuling,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (ruling.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        border = if (ruling.isEnabled) borderStrokeEmerald() else null,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header: Category, Authority, Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (ruling.authority) {
                            "DSN-MUI" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            "BAZNAS Daerah", "BAZNAS" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = ruling.authority,
                            color = when (ruling.authority) {
                                "DSN-MUI" -> MaterialTheme.colorScheme.primary
                                "BAZNAS Daerah", "BAZNAS" -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ruling.category,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = ruling.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Title & Reference No
            Text(
                text = ruling.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (ruling.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            if (ruling.referenceNumber.isNotBlank()) {
                Text(
                    text = "No. SK / Referensi: ${ruling.referenceNumber}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Summary
            Text(
                text = ruling.summary,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 17.sp
            )

            // Arabic text (if present)
            if (ruling.dalilArabic.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = ruling.dalilArabic,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (ruling.dalilTranslation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Artinya: \"${ruling.dalilTranslation}\"",
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Detailed Ruling
            if (ruling.detailedRuling.isNotBlank()) {
                Text(
                    text = ruling.detailedRuling,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // Formula (if present)
            if (ruling.calculationFormula.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Functions, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kaidah Formula: ${ruling.calculationFormula}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Dalil Source
            if (ruling.dalilSource.isNotBlank()) {
                Text(
                    text = "Sumber: ${ruling.dalilSource}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons (Edit & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sunting", fontSize = 11.sp)
                }

                if (ruling.isCustom) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun borderStrokeEmerald(): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
}

/**
 * Dialog Tambah / Sunting Fatwa & Rujukan Hukum
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditRulingDialog(
    initialRuling: ShariahRuling?,
    onDismiss: () -> Unit,
    onSave: (ShariahRuling) -> Unit
) {
    var title by remember { mutableStateOf(initialRuling?.title ?: "") }
    var category by remember { mutableStateOf(initialRuling?.category ?: "ZAKAT") }
    var authority by remember { mutableStateOf(initialRuling?.authority ?: "BAZNAS Daerah") }
    var referenceNumber by remember { mutableStateOf(initialRuling?.referenceNumber ?: "") }
    var summary by remember { mutableStateOf(initialRuling?.summary ?: "") }
    var detailedRuling by remember { mutableStateOf(initialRuling?.detailedRuling ?: "") }
    var calculationFormula by remember { mutableStateOf(initialRuling?.calculationFormula ?: "") }
    var dalilSource by remember { mutableStateOf(initialRuling?.dalilSource ?: "") }
    var dalilArabic by remember { mutableStateOf(initialRuling?.dalilArabic ?: "") }
    var dalilTranslation by remember { mutableStateOf(initialRuling?.dalilTranslation ?: "") }

    var error by remember { mutableStateOf<String?>(null) }

    val authorityPresets = listOf("DSN-MUI", "BAZNAS Daerah", "BAZNAS Pusat", "Dewan Pengawas Syariah", "Kemenag", "Mazhab Fikih", "Lainnya")
    val categoryPresets = listOf("ZAKAT", "FARAIDH", "QARDH", "INFAQ", "TRANSAKSI", "INVESTASI", "LAINNYA")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                if (initialRuling == null) "Tambah Rujukan / Fatwa Baru" else "Sunting Rujukan Hukum",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Ketentuan / Fatwa *") },
                    placeholder = { Text("Misal: Keputusan Nisab Zakat Fitrah 2024") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Kategori Selector
                Text("Kategori Fikih:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categoryPresets.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Lembaga / Authority Selector
                Text("Lembaga / Otoritas Penerbit:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    authorityPresets.forEach { auth ->
                        FilterChip(
                            selected = authority == auth,
                            onClick = { authority = auth },
                            label = { Text(auth, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = authority,
                    onValueChange = { authority = it },
                    label = { Text("Nama Lembaga / Dewan") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = { Text("Nomor SK / Surat Ketetapan") },
                    placeholder = { Text("Misal: SK BAZNAS Kab. No. 04/2024") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Ringkasan Kaidah Hukum *") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = calculationFormula,
                    onValueChange = { calculationFormula = it },
                    label = { Text("Kaidah Formula Hisab (Opsional)") },
                    placeholder = { Text("Misal: 2.5 kg beras @ Rp 16.000 = Rp 40.000 / jiwa") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dalilArabic,
                    onValueChange = { dalilArabic = it },
                    label = { Text("Teks Dalil Arab (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dalilTranslation,
                    onValueChange = { dalilTranslation = it },
                    label = { Text("Terjemahan Dalil (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dalilSource,
                    onValueChange = { dalilSource = it },
                    label = { Text("Sumber Rujukan Kitab / Hadith / Perda") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Judul fatwa tidak boleh kosong"
                        return@Button
                    }
                    if (summary.isBlank()) {
                        error = "Ringkasan kaidah tidak boleh kosong"
                        return@Button
                    }
                    val item = ShariahRuling(
                        id = initialRuling?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        category = category,
                        authority = authority.trim(),
                        referenceNumber = referenceNumber.trim(),
                        summary = summary.trim(),
                        detailedRuling = detailedRuling.trim(),
                        calculationFormula = calculationFormula.trim(),
                        dalilSource = dalilSource.trim(),
                        dalilArabic = dalilArabic.trim(),
                        dalilTranslation = dalilTranslation.trim(),
                        isCustom = true,
                        isEnabled = initialRuling?.isEnabled ?: true,
                        updatedAtMillis = System.currentTimeMillis()
                    )
                    onSave(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Simpan Entri")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

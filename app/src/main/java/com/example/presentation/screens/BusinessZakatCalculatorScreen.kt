package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.AccountCategory
import com.example.core.gold.GoldPriceManager
import com.example.core.state.AmanahLedgerViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessZakatCalculatorScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToInfaqVault: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val goldPrice by GoldPriceManager.goldPriceFlow.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Zakat Perniagaan, 1: Saham & Portofolio Syariah
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    val nisab85gThreshold = goldPrice * 85.0

    // Tab 0 State: Zakat Perniagaan
    var businessCashInput by remember { mutableStateOf("") }
    var businessReceivableInput by remember { mutableStateOf("") }
    var businessInventoryInput by remember { mutableStateOf("") }
    var businessShortDebtInput by remember { mutableStateOf("") }

    // Tab 1 State: Zakat Saham & Reksadana
    var stockMarketValueInput by remember { mutableStateOf("") }
    var stockDividendInput by remember { mutableStateOf("") }
    var nonHalalPercentageInput by remember { mutableStateOf("1.5") } // Default ~1.5% non-halal income ratio
    var investmentStrategyIndex by remember { mutableIntStateOf(0) } // 0: Trading Aktif (Kapital + Dividen), 1: Investor Jangka Panjang (Hanya Dividen)

    // Calculate auto-fill suggestions from business accounts in state
    val businessAccounts = remember(state.accounts) {
        state.accounts.filter { it.name.contains("usaha", ignoreCase = true) || it.name.contains("bisnis", ignoreCase = true) || it.code.startsWith("102") }
    }
    val autoFillBusinessCash = remember(businessAccounts, state.journalEntries) {
        businessAccounts.sumOf { state.getAccountBalance(it.id) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("business_zakat_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Zakat Bisnis & Saham Syariah", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        Text("Kaidah 'Urudh At-Tijarah, Skrining DES OJK & Pembersihan Dividen", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenDrawer, modifier = Modifier.testTag("business_zakat_drawer_button")) {
                        Icon(Icons.Default.Menu, contentDescription = "Buka Menu", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Zakat Perniagaan", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Saham & Reksadana", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // ==================== TAB 0: ZAKAT PERNIAGAAN ('URUDH TIJARAH) ====================
                val cash = businessCashInput.toDoubleOrNull() ?: 0.0
                val receivable = businessReceivableInput.toDoubleOrNull() ?: 0.0
                val inventory = businessInventoryInput.toDoubleOrNull() ?: 0.0
                val shortDebt = businessShortDebtInput.toDoubleOrNull() ?: 0.0

                val totalCurrentAssets = cash + receivable + inventory
                val netBusinessAsset = (totalCurrentAssets - shortDebt).coerceAtLeast(0.0)
                val isWajibZakatBusiness = netBusinessAsset >= nisab85gThreshold && netBusinessAsset > 0.0
                val calculatedBusinessZakat = if (isWajibZakatBusiness) netBusinessAsset * 0.025 else 0.0

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    // Dalil & Rule Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("📖 Kaidah Fiqih Zakat Perniagaan (HR. Abu Dawud No. 1562 & DSN-MUI):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("«كَانَ رَسُولُ اللَّهِ ﷺ يَأْمُرُنَا أَنْ نُخْرِجَ الصَّدَقَةَ مِنَ الَّذِي نُعِدُّهُ لِلْبَيْعِ»\n\"Rasulullah SAW memerintahkan kami agar mengeluarkan zakat dari harta yang kami persiapkan untuk diperjualbelikan (komoditas dagang).\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Rumus Hisab: [Kas Usaha + Piutang Lancar Tagih + Nilai Persediaan Dagang] - [Hutang Usaha Jatuh Tempo]. Tarif zakat adalah 2.5% jika melewati nisab 85g emas (Rp ${nf.format(nisab85gThreshold.toLong())}).", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 14.sp)
                            }
                        }
                    }

                    // Auto-fill suggestion card if business account exists
                    if (autoFillBusinessCash > 0) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Saldo Kas Usaha Terdeteksi:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Rp ${nf.format(autoFillBusinessCash.toLong())} dari pos rekening usaha", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Button(
                                        onClick = {
                                            businessCashInput = autoFillBusinessCash.toLong().toString()
                                            Toast.makeText(context, "Kas usaha berhasil disalin ke hisab", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Gunakan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Input Form Fields
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Komponen Neraca Bisnis / Perniagaan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                                OutlinedTextField(
                                    value = businessCashInput,
                                    onValueChange = { businessCashInput = it },
                                    label = { Text("1. Uang Kas Usaha & Saldo Rekening Bisnis", fontSize = 12.sp) },
                                    placeholder = { Text("Contoh: 50000000", fontSize = 11.sp) },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("business_cash_input")
                                )

                                OutlinedTextField(
                                    value = businessReceivableInput,
                                    onValueChange = { businessReceivableInput = it },
                                    label = { Text("2. Piutang Dagang Lancar (Bisa Ditagih)", fontSize = 12.sp) },
                                    placeholder = { Text("0", fontSize = 11.sp) },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("business_receivable_input")
                                )

                                OutlinedTextField(
                                    value = businessInventoryInput,
                                    onValueChange = { businessInventoryInput = it },
                                    label = { Text("3. Nilai Pasar Persediaan Barang / Stok Dagangan", fontSize = 12.sp) },
                                    placeholder = { Text("Contoh: 80000000", fontSize = 11.sp) },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("business_inventory_input")
                                )

                                OutlinedTextField(
                                    value = businessShortDebtInput,
                                    onValueChange = { businessShortDebtInput = it },
                                    label = { Text("4. Hutang Usaha Jatuh Tempo (Pengurang)", fontSize = 12.sp) },
                                    placeholder = { Text("0", fontSize = 11.sp) },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("business_debt_input")
                                )
                            }
                        }
                    }

                    // Calculation Result Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isWajibZakatBusiness) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(
                                            imageVector = if (isWajibZakatBusiness) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isWajibZakatBusiness) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (isWajibZakatBusiness) "WAJIB ZAKAT PERNIAGAAN" else "BELUM MENCAPAI NISAB",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWajibZakatBusiness) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Surface(
                                        color = if (isWajibZakatBusiness) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (isWajibZakatBusiness) "Tarif 2.5%" else "Nisab: Rp ${nf.format(nisab85gThreshold.toLong())}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWajibZakatBusiness) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Aktiva Lancar Usaha:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Rp ${nf.format(totalCurrentAssets.toLong())}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Hutang Lancar Jatuh Tempo:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("- Rp ${nf.format(shortDebt.toLong())}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Harta Bersih Usaha (Net):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Rp ${nf.format(netBusinessAsset.toLong())}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Kewajiban Zakat Perniagaan (2.5%):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        text = "Rp ${nf.format(calculatedBusinessZakat.toLong())}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isWajibZakatBusiness) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                // ==================== TAB 1: SAHAM & REKSADANA SYARIAH ====================
                val stockValue = stockMarketValueInput.toDoubleOrNull() ?: 0.0
                val dividendValue = stockDividendInput.toDoubleOrNull() ?: 0.0
                val nonHalalPct = (nonHalalPercentageInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)

                // Cleansing calculation: percentage of non-halal income on dividends
                val nonHalalCleansingAmount = dividendValue * (nonHalalPct / 100.0)

                val stockBaseAsset = if (investmentStrategyIndex == 0) {
                    stockValue + dividendValue // Trading aktif: Nilai pasar + dividen
                } else {
                    dividendValue // Jangka panjang: Hanya dividen
                }

                val isWajibStockZakat = stockBaseAsset >= (if (investmentStrategyIndex == 0) nisab85gThreshold else (nisab85gThreshold / 12.0)) && stockBaseAsset > 0.0
                val calculatedStockZakat = if (isWajibStockZakat) stockBaseAsset * 0.025 else 0.0

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(6.dp)) }

                    // Shariah Stock Screening Info
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                    Text("Kriteria Saham Syariah (DES OJK & Fatwa DSN-MUI No. 80):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                }
                                Text("1. Bisnis halal (bukan perbankan riba, miras, rokok, perjudian).\n2. Total utang berbasis bunga < 45% dari total aset.\n3. Total pendapatan non-halal < 10% dari total pendapatan usaha.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 14.sp)
                            }
                        }
                    }

                    // Investment Strategy Mode
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(4.dp)
                        ) {
                            listOf("Trading / Capital Gain", "Investasi Dividen").forEachIndexed { idx, label ->
                                val isSelected = investmentStrategyIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { investmentStrategyIndex = idx }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Inputs for Stock & Dividends
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    if (investmentStrategyIndex == 0) "Komponen Hisab Trading Saham" else "Komponen Hisab Dividen",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (investmentStrategyIndex == 0) {
                                    OutlinedTextField(
                                        value = stockMarketValueInput,
                                        onValueChange = { stockMarketValueInput = it },
                                        label = { Text("Total Nilai Pasar Portofolio Saham Akhir Tahun", fontSize = 12.sp) },
                                        placeholder = { Text("Contoh: 150000000", fontSize = 11.sp) },
                                        prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("stock_market_value_input")
                                    )
                                }

                                OutlinedTextField(
                                    value = stockDividendInput,
                                    onValueChange = { stockDividendInput = it },
                                    label = { Text("Total Dividen Bersih yang Diterima", fontSize = 12.sp) },
                                    placeholder = { Text("Contoh: 12000000", fontSize = 11.sp) },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("stock_dividend_input")
                                )

                                OutlinedTextField(
                                    value = nonHalalPercentageInput,
                                    onValueChange = { nonHalalPercentageInput = it },
                                    label = { Text("Rasio Pendapatan Non-Halal Emiten (%) - Pembersihan", fontSize = 12.sp) },
                                    placeholder = { Text("1.5", fontSize = 11.sp) },
                                    suffix = { Text("%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("non_halal_percentage_input")
                                )
                                Text("Rasio bunga jasa giro/bunga bank emiten yang wajib disucikan (tathhir) ke pos sosial/infaq.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Tathhirul Amwal (Cleansing) Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CleanHands, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Text("Pembersihan Dividen (Tathhirul Amwal)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("Rp ${nf.format(nonHalalCleansingAmount.toLong())}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                                }
                                Text("Porsi bunga giro emiten ($nonHalalPct%) dari dividen yang diperoleh HARUS disalurkan ke fasilitas umum / infaq sosial (bukan zakat).", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Button(
                                    onClick = onNavigateToInfaqVault,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("open_infaq_vault_btn")
                                ) {
                                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salurkan Pembersihan ke Vault Infaq", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Stock Zakat Summary
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isWajibStockZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Status Kewajiban Zakat Saham:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        if (isWajibStockZakat) "WAJIB ZAKAT (2.5%)" else "BELUM CAPAI NISAB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWajibStockZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dasar Pengenaan Zakat:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Rp ${nf.format(stockBaseAsset.toLong())}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Kewajiban Zakat Saham:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        "Rp ${nf.format(calculatedStockZakat.toLong())}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isWajibStockZakat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

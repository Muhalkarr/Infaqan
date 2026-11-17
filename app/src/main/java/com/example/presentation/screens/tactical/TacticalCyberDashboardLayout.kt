package com.example.presentation.screens.tactical

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.accounting.JournalEntry
import com.example.core.rezeki.RezekiNonTunaiRecord
import com.example.core.state.AmanahLedgerUiState
import com.example.core.state.AmanahLedgerViewModel
import com.example.presentation.components.BudgetCategoryAllocationChart
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.CyberNeonGreen
import com.example.ui.theme.CyberNeonGreenContainer
import com.example.ui.theme.CyberTacticalBorder
import com.example.ui.theme.CyberTacticalGold
import com.example.ui.theme.CyberTerminalBackground
import com.example.ui.theme.CyberTerminalSurface
import com.example.ui.theme.CyberTerminalSurfaceVariant
import com.example.ui.theme.CyberTerminalTextPrimary
import com.example.ui.theme.CyberTerminalTextSecondary
import com.example.ui.theme.ShareTechMonoFontFamily
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * TAMPILAN KHUSUS PROTOKOL TAKTIS (STRONGHOLD PROTOCOL HUD)
 * Menghadirkan transformasi total:
 * 1. Command Console Header dengan LED sequence lights.
 * 2. Sonar Radar Hologram Canvas dengan sweep scanning live.
 * 3. Asset Matrix Diagnostic Panel dengan multi-meter bar.
 * 4. Hexagonal Tactical Action Dock (Node OP//01 sampai OP//06).
 * 5. Telemetry Transaction Event Stream.
 */
@Composable
fun TacticalCyberDashboardView(
    state: AmanahLedgerUiState,
    viewModel: AmanahLedgerViewModel,
    listState: LazyListState,
    onOpenDrawer: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToRezeki: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToHaulNisab: () -> Unit,
    onNavigateToVaultHistory: () -> Unit,
    onNavigateToFaraidh: () -> Unit,
    onSelectEntryForDetail: (JournalEntry) -> Unit,
    onEditEntry: (String) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit,
    onEditRezeki: (RezekiNonTunaiRecord) -> Unit,
    onDeleteRezeki: (RezekiNonTunaiRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    var currentClock by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            currentClock = sdf.format(Date())
            delay(1000)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 14.dp,
            end = 14.dp,
            top = 10.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        // 1. COMMAND CONSOLE HEADER (Bukan AppBar biasa)
        item {
            TacticalCommandHeader(
                currentClock = currentClock,
                isMasked = state.securityConfig.isMaskBalance,
                appDisplayMode = state.appDisplayMode,
                onOpenDrawer = onOpenDrawer,
                onOpenThemeDialog = onOpenThemeDialog,
                onTogglePrivacy = { viewModel.toggleBalancePrivacy() },
                onToggleDisplayMode = { viewModel.toggleAppDisplayMode() }
            )
        }

        // 2. SONAR RADAR HOLO-GRID & NISAB HAUL TARGET MONITOR (Detik 0:21 - 0:25)
        if (state.featureVisibilityConfig.isSyariahModuleEnabled && state.featureVisibilityConfig.showHaulNisab) {
            item {
                TacticalSonarRadarCard(
                    state = state,
                    nf = nf,
                    onClick = onNavigateToHaulNisab
                )
            }
        }

        // 3. ASSET MATRIX DIAGNOSTIC TERMINAL (Kartu Saldo Taktis Bersegmen LED)
        item {
            TacticalAssetMatrixCard(
                state = state,
                nf = nf,
                onNavigateToVaultHistory = onNavigateToVaultHistory
            )
        }

        // 4. HEXAGONAL ACTION DOCK (Modular Action Nodes OP//01 - OP//06)
        item {
            TacticalActionDock(
                onAddTransaction = onNavigateToAddTransaction,
                onAddRezeki = onNavigateToRezeki,
                onManageBudget = onNavigateToBudget,
                onAuditSyariah = onNavigateToRules,
                onHaulNisab = onNavigateToHaulNisab,
                onFaraidh = onNavigateToFaraidh
            )
        }

        // 5. VISUALISASI ALOKASI ANGGARAN MAQASHID SYARIAH
        if (state.featureVisibilityConfig.isOperationalModuleEnabled && state.featureVisibilityConfig.showBudgetAllocation) {
            item {
                BudgetCategoryAllocationChart(
                    budgetCategories = state.budgets,
                    journalEntries = state.journalEntries,
                    currencySymbol = state.primaryCurrencySymbol,
                    onNavigateToBudget = onNavigateToBudget
                )
            }
        }

        // 6. REZEKI NON-TUNAI TACTICAL MANIFEST
        if (state.featureVisibilityConfig.isOperationalModuleEnabled && state.featureVisibilityConfig.showRezekiNonTunai && state.rezekiNonTunaiList.isNotEmpty()) {
            item {
                TacticalRezekiManifestCard(
                    rezekiList = state.rezekiNonTunaiList,
                    nf = nf,
                    onAddRezeki = onNavigateToRezeki,
                    onEditRezeki = onEditRezeki,
                    onDeleteRezeki = onDeleteRezeki
                )
            }
        }

        // 6. TELEMETRY TRANSACTION EVENT STREAM (Daftar Log Mutasi Jurnal Terminal)
        item {
            TacticalTransactionStreamSection(
                entries = state.journalEntries,
                nf = nf,
                onSelectEntry = onSelectEntryForDetail,
                onEditEntry = onEditEntry,
                onDeleteEntry = onDeleteEntry
            )
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

/**
 * 1. Tactical Command Header dengan Lampu LED Sequence
 */
@Composable
private fun TacticalCommandHeader(
    currentClock: String,
    isMasked: Boolean,
    appDisplayMode: com.example.core.datastore.AppDisplayMode,
    onOpenDrawer: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onTogglePrivacy: () -> Unit,
    onToggleDisplayMode: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "led_sequence")
    val ledProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "led_phase"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CutCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CutCornerShape(4.dp))
                            .clickable { onOpenDrawer() }
                            .testTag("tactical_sidebar_menu_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = "KONSOL KEUANGAN SYARIAH",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.3.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f), CutCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("v4.2", fontFamily = ShareTechMonoFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, maxLines = 1, softWrap = false)
                            }
                        }
                        Text(
                            text = "STRONGHOLD PROTOCOL: AMANAH ALLIANCE",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 8.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onTogglePrivacy,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("tactical_privacy_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Sembunyikan Saldo",
                            tint = if (isMasked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Surface(
                        onClick = onOpenThemeDialog,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = CutCornerShape(4.dp),
                        border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
                        modifier = Modifier.testTag("tactical_theme_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Pilih Tema", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text(
                                text = "THEME",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))

            // Sequence Indicator LED (01, 02, 03, 04) & Military Clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0..3) {
                        val active = ledProgress.toInt() == i
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                                        CutCornerShape(1.dp)
                                    )
                            )
                            Text(
                                text = "0${i + 1}",
                                fontFamily = ShareTechMonoFontFamily,
                                fontSize = 8.5.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                    }
                }

                Text(
                    text = "ZULU: ${if (currentClock.isNotBlank()) currentClock else "12:00:00"} // SYC",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }

            // Mode Tampilan Switcher (Ringkas vs Lengkap)
            val isSimpleMode = appDisplayMode == com.example.core.datastore.AppDisplayMode.SIMPLE
            Surface(
                onClick = onToggleDisplayMode,
                shape = CutCornerShape(4.dp),
                color = if (isSimpleMode)
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = BorderStroke(
                    0.9.dp,
                    if (isSimpleMode)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp)
                    .testTag("dashboard_mode_switch_chip")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isSimpleMode) Icons.Default.AutoAwesome else Icons.Default.Nightlight,
                            contentDescription = null,
                            tint = if (isSimpleMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isSimpleMode) "MODE TAMPILAN: RINGKAS" else "MODE TAMPILAN: LENGKAP SYARIAH",
                            fontFamily = ChakraPetchFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isSimpleMode) "KE LENGKAP" else "RINGKASKAN",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimpleMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Ubah Mode Tampilan",
                            tint = if (isSimpleMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Sonar Radar Hologram Card (Detik 0:21 - 0:25)
 */
@Composable
private fun TacticalSonarRadarCard(
    state: AmanahLedgerUiState,
    nf: NumberFormat,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_anim")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    val haulDays = remember(state.haulStartDate) {
        if (state.haulStartDate != null) {
            val daysElapsed = ((System.currentTimeMillis() - state.haulStartDate) / (1000L * 60 * 60 * 24)).toInt()
            maxOf(0, 354 - daysElapsed)
        } else {
            354
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tactical_sonar_radar_card"),
        shape = CutCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CutCornerShape(1.dp)))
                    Text(
                        text = "TARGET POINT // HAUL & NISAB RADAR",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = if (state.isNisabReached) "[NISAB: REACHED]" else "[NISAB: SCANNING]",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isNisabReached) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val radarOutlineColor = MaterialTheme.colorScheme.outline
                val radarSweepColor = MaterialTheme.colorScheme.primary
                val radarBlipColor = MaterialTheme.colorScheme.secondary

                // Square Matrix Radar Canvas (Geometric Tactical HUD)
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CutCornerShape(6.dp))
                        .border(1.dp, radarOutlineColor.copy(alpha = 0.6f), CutCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.width / 2f

                        // Concentric geometric square radar rings (sharp sci-fi grid)
                        drawRect(radarOutlineColor, style = Stroke(width = 1.2f))
                        drawRect(
                            color = radarOutlineColor.copy(alpha = 0.65f),
                            topLeft = Offset(size.width * 0.18f, size.height * 0.18f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.64f),
                            style = Stroke(width = 1f)
                        )
                        drawRect(
                            color = radarOutlineColor.copy(alpha = 0.45f),
                            topLeft = Offset(size.width * 0.35f, size.height * 0.35f),
                            size = androidx.compose.ui.geometry.Size(size.width * 0.30f, size.height * 0.30f),
                            style = Stroke(width = 1f)
                        )

                        // Crosshairs
                        drawLine(radarOutlineColor.copy(alpha = 0.7f), Offset(center.x, 0f), Offset(center.x, size.height), 1f)
                        drawLine(radarOutlineColor.copy(alpha = 0.7f), Offset(0f, center.y), Offset(size.width, center.y), 1f)

                        // Sweep radar line
                        val rad = Math.toRadians(sweepAngle.toDouble())
                        val endX = center.x + radius * cos(rad).toFloat()
                        val endY = center.y + radius * sin(rad).toFloat()
                        drawLine(
                            color = radarSweepColor,
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 2.4f,
                            cap = StrokeCap.Square
                        )

                        // Target blip (Emas Nisab) - Square Tactical Diamond
                        val blipCenter = Offset(center.x + radius * 0.5f, center.y - radius * 0.3f)
                        drawRect(
                            color = radarBlipColor,
                            topLeft = Offset(blipCenter.x - 4f, blipCenter.y - 4f),
                            size = androidx.compose.ui.geometry.Size(8f, 8f)
                        )
                    }
                }

                // Data telemetry samping radar
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Ambang Nisab (85g Emas):",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Rp ${nf.format(state.nisabThreshold.toLong())}",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Sisa Haul Hijriah (354 Hari):",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "$haulDays HARI TERSISA",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CutCornerShape(2.dp))
                                .border(0.7.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f), CutCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("LUNAR", fontFamily = ShareTechMonoFontFamily, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. Tactical Asset Matrix Card (Panel Multimeter Diagnostik Saldo)
 */
@Composable
private fun TacticalAssetMatrixCard(
    state: AmanahLedgerUiState,
    nf: NumberFormat,
    onNavigateToVaultHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("tactical_asset_matrix_card"),
        shape = CutCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "// ASSET MATRIX DIAGNOSTIC",
                    fontFamily = ChakraPetchFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "DOUBLE-ENTRY // BALANCED",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Big Balance Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, CutCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CutCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "TOTAL SALDO KAS LIKUID TERSEDIA:",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (state.securityConfig.isMaskBalance) "[ Rp •••••••••• ]" else "Rp ${nf.format(state.totalWalletBalance)}",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Multimeter Bar Segments
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "LIQUIDITY SIGNAL METER:",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val segments = 16
                    for (i in 0 until segments) {
                        val active = i < 11
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(
                                    if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.88f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    CutCornerShape(1.dp)
                                )
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

            // Sub-Vault Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("INFAQ VAULT COMMITMENT:", fontFamily = ShareTechMonoFontFamily, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (state.securityConfig.isMaskBalance) "Rp •••••" else "Rp ${nf.format(state.virtualInfaqVaultBalance)}",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                    shape = CutCornerShape(4.dp),
                    border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { onNavigateToVaultHistory() }
                ) {
                    Text(
                        text = "[VAULT LOGS]",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 4. Hexagonal Action Dock (Node OP//01 sampai OP//06)
 */
@Composable
private fun TacticalActionDock(
    onAddTransaction: () -> Unit,
    onAddRezeki: () -> Unit,
    onManageBudget: () -> Unit,
    onAuditSyariah: () -> Unit,
    onHaulNisab: () -> Unit,
    onFaraidh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "// TACTICAL ACTION NODES",
                fontFamily = ChakraPetchFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Text(
                text = "HEX//GRID READY",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalNodeButton(
                code = "OP//01",
                label = "CATAT MUTASI",
                icon = Icons.Default.Add,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onAddTransaction
            )
            TacticalNodeButton(
                code = "OP//02",
                label = "REZEKI HADIAH",
                icon = Icons.Default.CardGiftcard,
                accentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = onAddRezeki
            )
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalNodeButton(
                code = "OP//03",
                label = "AUDIT SYARIAH",
                icon = Icons.Default.Shield,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onAuditSyariah
            )
            TacticalNodeButton(
                code = "OP//04",
                label = "BUDGET MATRIX",
                icon = Icons.Default.Savings,
                accentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = onManageBudget
            )
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalNodeButton(
                code = "OP//05",
                label = "PELACAK NISAB",
                icon = Icons.Default.AutoAwesome,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onHaulNisab
            )
            TacticalNodeButton(
                code = "OP//06",
                label = "HISAB FARAIDH",
                icon = Icons.Default.AccountBalance,
                accentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = onFaraidh
            )
        }
    }
}

@Composable
private fun TacticalNodeButton(
    code: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val dynamicAccent = if (accentColor == CyberNeonGreen) MaterialTheme.colorScheme.primary else if (accentColor == CyberTacticalGold) MaterialTheme.colorScheme.secondary else accentColor

    Surface(
        modifier = modifier.clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = CutCornerShape(6.dp),
        border = BorderStroke(1.dp, dynamicAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(dynamicAccent.copy(alpha = 0.15f), CutCornerShape(4.dp))
                    .border(0.8.dp, dynamicAccent.copy(alpha = 0.5f), CutCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = dynamicAccent, modifier = Modifier.size(17.dp))
            }
            Column {
                Text(text = code, fontFamily = ShareTechMonoFontFamily, fontSize = 8.5.sp, color = dynamicAccent, fontWeight = FontWeight.Bold)
                Text(text = label, fontFamily = ChakraPetchFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * 5. Tactical Rezeki Manifest Card
 */
@Composable
private fun TacticalRezekiManifestCard(
    rezekiList: List<RezekiNonTunaiRecord>,
    nf: NumberFormat,
    onAddRezeki: () -> Unit,
    onEditRezeki: (RezekiNonTunaiRecord) -> Unit,
    onDeleteRezeki: (RezekiNonTunaiRecord) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CutCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "// REZEKI NON-TUNAI MANIFEST",
                    fontFamily = ChakraPetchFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${rezekiList.size} ITEMS",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rezekiList.take(3).forEach { record ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CutCornerShape(4.dp),
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().clickable { onEditRezeki(record) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = record.title,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "DARI: ${record.giverName.ifBlank { "Anonim" }} // SYUKUR",
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 8.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rp ${nf.format(record.estimatedValue.toLong())}",
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onEditRezeki(record) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Rezeki",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteRezeki(record) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Rezeki",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 6. Telemetry Transaction Event Stream (Daftar Log Mutasi Jurnal Terminal)
 */
@Composable
private fun TacticalTransactionStreamSection(
    entries: List<JournalEntry>,
    nf: NumberFormat,
    onSelectEntry: (JournalEntry) -> Unit,
    onEditEntry: (String) -> Unit,
    onDeleteEntry: (JournalEntry) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var displayLimit by remember { mutableIntStateOf(15) }

    val filteredEntries = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) entries
        else entries.filter {
            it.description.contains(searchQuery, ignoreCase = true) ||
                it.transactionType.contains(searchQuery, ignoreCase = true) ||
                it.primaryAmount.toLong().toString().contains(searchQuery.trim()) ||
                it.totalDebit.toLong().toString().contains(searchQuery.trim())
        }
    }
    val visibleEntries = filteredEntries.take(displayLimit)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "// TELEMETRY JOURNAL LOG STREAM",
                fontFamily = ChakraPetchFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Text(
                text = "${visibleEntries.size}/${filteredEntries.size} RECORDS",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (entries.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Cari catatan lama / nominal...",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = CutCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tactical_journal_search_input")
            )
        }

        if (filteredEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, CutCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CutCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isBlank()) "[NO TELEMETRY LOGS RECORDED]" else "[NO MATCHING LOGS FOR '$searchQuery']",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val sdf = SimpleDateFormat("dd/MM/yy HH:mm", Locale("id", "ID"))
                visibleEntries.forEach { entry ->
                    val isIncome = entry.transactionType == "INFLOW" || entry.transactionType == "INCOME"
                    val isPayout = entry.transactionType == "INFAQ_PAYOUT"
                    val amountColor = when {
                        isIncome -> MaterialTheme.colorScheme.primary
                        isPayout -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                    val typeCode = when (entry.transactionType) {
                        "INFLOW", "INCOME" -> "IN//KASAB"
                        "EXPENSE" -> "OUT//EXPENSE"
                        "INFAQ_PAYOUT" -> "ZIS//DISBURSE"
                        "TRANSFER" -> "MUT//TRANSFER"
                        else -> entry.transactionType
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectEntry(entry) },
                        color = MaterialTheme.colorScheme.surface,
                        shape = CutCornerShape(4.dp),
                        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "[${sdf.format(entry.gregorianDate)}]",
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontSize = 8.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(amountColor.copy(alpha = 0.15f), CutCornerShape(2.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = typeCode,
                                            fontFamily = ShareTechMonoFontFamily,
                                            fontSize = 8.sp,
                                            color = amountColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = entry.description,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${if (isIncome) "+" else "-"}Rp ${nf.format(entry.primaryAmount.toLong())}",
                                    fontFamily = ShareTechMonoFontFamily,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onEditEntry(entry.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("tactical_edit_entry_button_${entry.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Transaksi",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteEntry(entry) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("tactical_delete_entry_button_${entry.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Transaksi",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredEntries.size > 15) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (displayLimit < filteredEntries.size) {
                            OutlinedButton(
                                onClick = { displayLimit += 30 },
                                shape = CutCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("tactical_load_more_btn")
                            ) {
                                Text("MUAT +30", fontFamily = ShareTechMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { displayLimit = filteredEntries.size },
                                shape = CutCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("tactical_show_all_btn")
                            ) {
                                Text("SEMUA (${filteredEntries.size})", fontFamily = ShareTechMonoFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (displayLimit > 15) {
                            OutlinedButton(
                                onClick = { displayLimit = 15 },
                                shape = CutCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("RINGKAS KE 15 TERBARU", fontFamily = ShareTechMonoFontFamily, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

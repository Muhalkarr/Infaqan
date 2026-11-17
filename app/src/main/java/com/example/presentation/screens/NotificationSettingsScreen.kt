package com.example.presentation.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.notification.AmanahNotificationConfig
import com.example.core.notification.DebtNotificationScope
import com.example.core.notification.NotificationCategoryType
import com.example.core.notification.ZakatHaulCalendarMode
import com.example.core.shariah.HaulCalculationMethod
import com.example.core.state.AmanahLedgerViewModel
import com.example.presentation.components.InfaqVaultReminderCard
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.TacticalSubScreenHeader
import com.example.ui.theme.isTactical
import com.example.ui.theme.rememberAppCardShape
import com.example.ui.theme.rememberAppShape
import java.text.NumberFormat
import java.util.Locale

private enum class NotificationTimeTarget {
    QUIET_START,
    QUIET_END,
    DAILY_INFAQ,
    DEBT_QARDH,
    ZAKAT_HAUL
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val config = state.notificationConfig
    val context = LocalContext.current
    val isTactical = state.themeMode.isTactical
    val cardShape = rememberAppCardShape(16)
    val pillShape = rememberAppShape(10)
    val nf = remember { NumberFormat.getIntegerInstance(Locale("id", "ID")) }

    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    var permissionGranted by remember { mutableStateOf(checkNotificationPermission()) }
    var pendingCategoryTestAfterPerm by remember { mutableStateOf<NotificationCategoryType?>(null) }
    var activeTimeTarget by remember { mutableStateOf<NotificationTimeTarget?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        val pendingTest = pendingCategoryTestAfterPerm
        pendingCategoryTestAfterPerm = null
        if (granted) {
            if (pendingTest != null) {
                viewModel.sendTestNotification(context, pendingTest) { sent ->
                    Toast.makeText(
                        context,
                        if (sent) "Notifikasi uji '${pendingTest.label}' dikirim!"
                        else "Tidak ada notifikasi uji terkirim.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(context, "Izin notifikasi diaktifkan!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                context,
                "Izin notifikasi ditolak. Aktifkan melalui Setelan Sistem agar pengingat muncul.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun triggerCategoryTest(category: NotificationCategoryType) {
        if (!checkNotificationPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingCategoryTestAfterPerm = category
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        viewModel.sendTestNotification(context, category) { sent ->
            Toast.makeText(
                context,
                if (sent) "Notifikasi uji '${category.label}' terkirim ke panel notifikasi!"
                else "Pastikan izin notifikasi aktif di perangkat.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isTactical) {
                TacticalSubScreenHeader(
                    title = "Pusat Notifikasi Syariah",
                    subCode = "ALERTS//NOTIFY-CONFIG",
                    onBack = onNavigateBack,
                    trailingActions = {
                        IconButton(
                            onClick = {
                                viewModel.updateNotificationConfig(AmanahNotificationConfig())
                                Toast.makeText(context, "Konfigurasi notifikasi dikembalikan ke bawaan.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("notif_reset_default_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Default Notifikasi", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("notif_menu_sidebar_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka Menu Sidebar", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            } else {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                    title = {
                        Column {
                            Text(
                                text = "Pusat Kustomisasi Notifikasi",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = config.summarySubtitle(),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("notif_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.updateNotificationConfig(AmanahNotificationConfig())
                                Toast.makeText(context, "Konfigurasi notifikasi dikembalikan ke bawaan.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("notif_reset_default_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Default Notifikasi", tint = MaterialTheme.colorScheme.secondary)
                        }
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("notif_menu_sidebar_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Buka Menu Sidebar")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. ANDROID 13+ PERMISSION CARD
            if (!permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f),
                    shape = cardShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Izin Notifikasi Android Belum Aktif",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aktifkan izin notifikasi sistem agar pengingat Infaq Harian, Jatuh Tempo Hutang, dan Haul Zakat dapat tampil.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                shape = pillShape,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("notif_grant_permission_button")
                            ) {
                                Text("Izinkan Notifikasi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                },
                                shape = pillShape,
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("Setelan App", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 1. MASTER SWITCH, DELIVERY CHANNELS & QUIET HOURS
            Card(
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = if (isTactical) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (config.isMasterEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aktifkan Pengingat Push Syariah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${config.activeChannelCount()} dari 3 kanal utama aktif • Jam Tenang: ${config.formattedQuietHours()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = config.isMasterEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateNotificationConfig(config.copy(isMasterEnabled = enabled))
                            },
                            modifier = Modifier.testTag("notif_master_switch")
                        )
                    }

                    AnimatedVisibility(visible = config.isMasterEnabled) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Preferensi Pengiriman & Jam Tenang",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = config.enableSound,
                                    onClick = { viewModel.updateNotificationConfig(config.copy(enableSound = !config.enableSound)) },
                                    label = { Text("Suara Dering", fontSize = 12.sp) },
                                    shape = pillShape,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("notif_sound_chip")
                                )
                                FilterChip(
                                    selected = config.enableVibration,
                                    onClick = { viewModel.updateNotificationConfig(config.copy(enableVibration = !config.enableVibration)) },
                                    label = { Text("Getar", fontSize = 12.sp) },
                                    shape = pillShape,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("notif_vibration_chip")
                                )
                                FilterChip(
                                    selected = config.useHighPriorityAlerts,
                                    onClick = { viewModel.updateNotificationConfig(config.copy(useHighPriorityAlerts = !config.useHighPriorityAlerts)) },
                                    label = { Text("Prioritas Tinggi (Heads-Up)", fontSize = 12.sp) },
                                    shape = pillShape,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("notif_high_priority_chip")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            NotificationSwitchRow(
                                title = "Jam Tenang Otomatis (Quiet Hours)",
                                subtitle = "Tahan notifikasi terjadwal di waktu istirahat (${config.formattedQuietHours()})",
                                checked = config.quietHoursEnabled,
                                testTag = "notif_quiet_hours_switch",
                                onCheckedChange = { qEnabled ->
                                    viewModel.updateNotificationConfig(config.copy(quietHoursEnabled = qEnabled))
                                }
                            )

                            AnimatedVisibility(visible = config.quietHoursEnabled) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TimeSelectorPill(
                                        label = "Mulai Tenang",
                                        timeText = String.format(Locale.US, "%02d:%02d", config.quietStartHour, config.quietStartMinute),
                                        icon = Icons.Default.Bedtime,
                                        shape = pillShape,
                                        testTag = "notif_quiet_start_btn",
                                        modifier = Modifier.weight(1f),
                                        onClick = { activeTimeTarget = NotificationTimeTarget.QUIET_START }
                                    )
                                    TimeSelectorPill(
                                        label = "Selesai Tenang",
                                        timeText = String.format(Locale.US, "%02d:%02d", config.quietEndHour, config.quietEndMinute),
                                        icon = Icons.Default.WbSunny,
                                        shape = pillShape,
                                        testTag = "notif_quiet_end_btn",
                                        modifier = Modifier.weight(1f),
                                        onClick = { activeTimeTarget = NotificationTimeTarget.QUIET_END }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. KATEGORI 1: KEWAJIBAN INFAQ HARIAN & SEDEKAH SUBUH
            AnimatedVisibility(visible = config.isMasterEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    NotificationCategoryCard(
                        title = "Pengingat Infaq Harian & Sedekah Subuh",
                        subtitle = "Ingatkan amalan infaq harian, streak subuh & alokasi brankas infaq",
                        icon = Icons.Default.WbSunny,
                        enabled = config.dailyInfaq.enabled,
                        switchTag = "notif_daily_infaq_switch",
                        onToggle = { enabled ->
                            viewModel.updateNotificationConfig(
                                config.copy(dailyInfaq = config.dailyInfaq.copy(enabled = enabled))
                            )
                        },
                        isTactical = isTactical,
                        cardShape = cardShape
                    ) {
                        val daily = config.dailyInfaq
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeSelectorPill(
                                label = "Jam Pengingat Harian",
                                timeText = daily.formattedTime(),
                                icon = Icons.Default.Schedule,
                                shape = pillShape,
                                testTag = "notif_daily_infaq_time_btn",
                                modifier = Modifier.weight(1f),
                                onClick = { activeTimeTarget = NotificationTimeTarget.DAILY_INFAQ }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { triggerCategoryTest(NotificationCategoryType.DAILY_INFAQ) },
                                shape = pillShape,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("notif_test_daily_infaq_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Uji", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Preset Waktu Utama:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple("Subuh 05:00", 5, 0),
                                Triple("Fajar 05:30", 5, 30),
                                Triple("Dzuhur 12:30", 12, 30),
                                Triple("Isya 20:00", 20, 0)
                            ).forEach { (label, h, m) ->
                                val selected = daily.hour == h && daily.minute == m
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        viewModel.updateNotificationConfig(
                                            config.copy(dailyInfaq = daily.copy(hour = h, minute = m))
                                        )
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = pillShape,
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationSwitchRow(
                            title = "Hanya Ingatkan Jika Belum Berinfaq Hari Ini",
                            subtitle = "Otomatis melewati notifikasi bila sudah ada mutasi Sedekah/Infaq hari ini",
                            checked = daily.onlyIfNoInfaqLoggedToday,
                            testTag = "notif_daily_only_unlogged_switch",
                            onCheckedChange = {
                                viewModel.updateNotificationConfig(
                                    config.copy(dailyInfaq = daily.copy(onlyIfNoInfaqLoggedToday = it))
                                )
                            }
                        )
                        NotificationSwitchRow(
                            title = "Sertakan Streak Sedekah Subuh",
                            subtitle = "Tampilkan progres ${state.sedekahSubuhState.currentStreak} hari beruntun di notifikasi",
                            checked = daily.includeSedekahSubuhStreak,
                            testTag = "notif_daily_streak_switch",
                            onCheckedChange = {
                                viewModel.updateNotificationConfig(
                                    config.copy(dailyInfaq = daily.copy(includeSedekahSubuhStreak = it))
                                )
                            }
                        )
                        NotificationSwitchRow(
                            title = "Sertakan Saldo Virtual Infaq Vault",
                            subtitle = "Tampilkan titipan infaq (Rp ${nf.format(state.virtualInfaqVaultBalance.toLong())}) siap salur",
                            checked = daily.includeVaultStatus,
                            testTag = "notif_daily_vault_switch",
                            onCheckedChange = {
                                viewModel.updateNotificationConfig(
                                    config.copy(dailyInfaq = daily.copy(includeVaultStatus = it))
                                )
                            }
                        )
                        NotificationSwitchRow(
                            title = "Ringkas Aturan Infaq Otomatis Aktif",
                            subtitle = "Cantumkan jumlah aturan (${state.rules.size}) yang berjalan",
                            checked = daily.includeActiveRulesSummary,
                            testTag = "notif_daily_rules_switch",
                            onCheckedChange = {
                                viewModel.updateNotificationConfig(
                                    config.copy(dailyInfaq = daily.copy(includeActiveRulesSummary = it))
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        var targetHintText by remember(daily.targetDailyAmountHint) {
                            mutableStateOf(daily.targetDailyAmountHint.toLong().toString())
                        }
                        OutlinedTextField(
                            value = targetHintText,
                            onValueChange = { raw ->
                                val digits = raw.filter { it.isDigit() }
                                targetHintText = digits
                                digits.toDoubleOrNull()?.let { amt ->
                                    viewModel.updateNotificationConfig(
                                        config.copy(dailyInfaq = daily.copy(targetDailyAmountHint = amt.coerceAtLeast(0.0)))
                                    )
                                }
                            },
                            label = { Text("Patokan Minimal Infaq Harian (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = pillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notif_daily_target_amount_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        var dailyCustomNote by remember(daily.customNote) { mutableStateOf(daily.customNote) }
                        OutlinedTextField(
                            value = dailyCustomNote,
                            onValueChange = { note ->
                                dailyCustomNote = note
                                viewModel.updateNotificationConfig(
                                    config.copy(dailyInfaq = daily.copy(customNote = note))
                                )
                            },
                            label = { Text("Catatan / Niat Harian Kustom (Opsional)") },
                            placeholder = { Text("Cth: Niatkan untuk keberkahan keluarga & orang tua") },
                            singleLine = true,
                            shape = pillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notif_daily_custom_note_input")
                        )
                    }

                    // 3. KATEGORI 2: JATUH TEMPO HUTANG (QARDH HASAN) & TAGIHAN RUTIN
                    NotificationCategoryCard(
                        title = "Jatuh Tempo Hutang, Piutang & Tagihan",
                        subtitle = "Pantau amanah Qardhul Hasan (${state.qardhRecords.size} akad) & jadwal rutin (${state.recurringTransactions.size})",
                        icon = Icons.Default.Handshake,
                        enabled = config.debt.enabled,
                        switchTag = "notif_debt_switch",
                        onToggle = { enabled ->
                            viewModel.updateNotificationConfig(
                                config.copy(debt = config.debt.copy(enabled = enabled))
                            )
                        },
                        isTactical = isTactical,
                        cardShape = cardShape
                    ) {
                        val debt = config.debt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeSelectorPill(
                                label = "Jam Cek Jatuh Tempo",
                                timeText = debt.formattedTime(),
                                icon = Icons.Default.Schedule,
                                shape = pillShape,
                                testTag = "notif_debt_time_btn",
                                modifier = Modifier.weight(1f),
                                onClick = { activeTimeTarget = NotificationTimeTarget.DEBT_QARDH }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { triggerCategoryTest(NotificationCategoryType.DEBT_QARDH) },
                                shape = pillShape,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("notif_test_debt_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Uji", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Cakupan Catatan Qardh:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DebtNotificationScope.entries.forEach { scopeOption ->
                                FilterChip(
                                    selected = debt.scope == scopeOption,
                                    onClick = {
                                        viewModel.updateNotificationConfig(
                                            config.copy(debt = debt.copy(scope = scopeOption))
                                        )
                                    },
                                    label = { Text(scopeOption.title, fontSize = 11.sp) },
                                    shape = pillShape,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("notif_debt_scope_${scopeOption.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Ingatkan Pada Hari-H / Sebelum Jatuh Tempo (${debt.leadDaysSummary()}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmanahNotificationConfig.AVAILABLE_DEBT_LEAD_DAYS.forEach { lead ->
                                val selected = lead in debt.leadDays
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val nextSet = if (selected) debt.leadDays - lead else debt.leadDays + lead
                                        viewModel.updateNotificationConfig(
                                            config.copy(debt = debt.copy(leadDays = nextSet))
                                        )
                                    },
                                    label = {
                                        Text(if (lead == 0) "Hari-H" else "H-$lead", fontSize = 11.sp)
                                    },
                                    shape = pillShape,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("notif_debt_lead_$lead")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationSwitchRow(
                            title = "Ulangi Pengingat Jika Terlewat (Overdue)",
                            subtitle = "Kirim ulang tiap ${debt.overdueRepeatDays} hari selama hutang/piutang belum lunas",
                            checked = debt.remindOverdue,
                            testTag = "notif_debt_overdue_switch",
                            onCheckedChange = {
                                viewModel.updateNotificationConfig(
                                    config.copy(debt = debt.copy(remindOverdue = it))
                                )
                            }
                        )
                        AnimatedVisibility(visible = debt.remindOverdue) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                            ) {
                                listOf(1, 2, 3, 7).forEach { repeatDays ->
                                    FilterChip(
                                        selected = debt.overdueRepeatDays == repeatDays,
                                        onClick = {
                                            viewModel.updateNotificationConfig(
                                                config.copy(debt = debt.copy(overdueRepeatDays = repeatDays))
                                            )
                                        },
                                        label = { Text("Tiap $repeatDays Hari", fontSize = 11.sp) },
                                        shape = pillShape,
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    )
                                }
                            }
                        }

                        NotificationSwitchRow(
                            title = "Sertakan Pengingat Tagihan & Transaksi Rutin",
                            subtitle = "Pantau jadwal rutin aktif (${state.recurringTransactions.count { it.isActive }} jadwal)",
                            checked = debt.includeRecurringBills,
                            testTag = "notif_debt_recurring_switch",
                            onCheckedChange = {
                                viewModel.updateNotificationConfig(
                                    config.copy(debt = debt.copy(includeRecurringBills = it))
                                )
                            }
                        )
                        AnimatedVisibility(visible = debt.includeRecurringBills) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                            ) {
                                listOf(0, 1, 3, 7).forEach { recLead ->
                                    FilterChip(
                                        selected = debt.recurringLeadDays == recLead,
                                        onClick = {
                                            viewModel.updateNotificationConfig(
                                                config.copy(debt = debt.copy(recurringLeadDays = recLead))
                                            )
                                        },
                                        label = { Text(if (recLead == 0) "Rutin: Hari-H" else "Rutin: H-$recLead", fontSize = 11.sp) },
                                        shape = pillShape,
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        var debtCustomNote by remember(debt.customNote) { mutableStateOf(debt.customNote) }
                        OutlinedTextField(
                            value = debtCustomNote,
                            onValueChange = { note ->
                                debtCustomNote = note
                                viewModel.updateNotificationConfig(
                                    config.copy(debt = debt.copy(customNote = note))
                                )
                            },
                            label = { Text("Catatan Tambahan Pengingat Hutang (Opsional)") },
                            placeholder = { Text("Cth: Hubungi pemberi pinjaman sebelum tanggal jatuh tempo") },
                            singleLine = true,
                            shape = pillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notif_debt_custom_note_input")
                        )
                    }

                    // 4. KATEGORI 3: NISAB & JADWAL HAUL ZAKAT
                    NotificationCategoryCard(
                        title = "Jadwal Haul Zakat & Ambang Nisab",
                        subtitle = "Pantau batas nisab ${state.shariahConfig.goldNisabGram.toInt()}g emas & hitung mundur haul tahunan",
                        icon = Icons.Default.VolunteerActivism,
                        enabled = config.zakatHaul.enabled,
                        switchTag = "notif_zakat_switch",
                        onToggle = { enabled ->
                            viewModel.updateNotificationConfig(
                                config.copy(zakatHaul = config.zakatHaul.copy(enabled = enabled))
                            )
                        },
                        isTactical = isTactical,
                        cardShape = cardShape
                    ) {
                        val zakat = config.zakatHaul
                        val resolvedCycleDays = when (zakat.calendarMode) {
                            ZakatHaulCalendarMode.FOLLOW_SHARIAH_CONFIG ->
                                if (state.shariahConfig.haulCalculationMethod == HaulCalculationMethod.MASEHI) 365 else 354
                            ZakatHaulCalendarMode.HIJRI_354 -> 354
                            ZakatHaulCalendarMode.MASEHI_365 -> 365
                        }
                        val haulStartTs = state.zakatMaal.haulStartDateMillis.takeIf { it > 0L }
                            ?: state.haulStartDate
                            ?: 0L
                        val haulStatusText = if (haulStartTs > 0L) {
                            val elapsedDays = ((System.currentTimeMillis() - haulStartTs).coerceAtLeast(0L) / (24L * 3600L * 1000L)).toInt()
                            val rem = (resolvedCycleDays - elapsedDays).coerceAtLeast(0)
                            if (rem == 0) "Genap Haul ($resolvedCycleDays hari)" else "Sisa $rem hari dari $resolvedCycleDays hari"
                        } else {
                            "Mulai hitung saat kekayaan mencapai Nisab (Rp ${nf.format(state.nisabThreshold.toLong())})"
                        }

                        // Live Haul & Nisab Banner
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isTactical) 0.2f else 0.45f),
                            shape = pillShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Status Zakat Maal Terkini",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Nisab: Rp ${nf.format(state.nisabThreshold.toLong())} • $haulStatusText",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TimeSelectorPill(
                                label = "Jam Cek Nisab & Haul",
                                timeText = zakat.formattedTime(),
                                icon = Icons.Default.Schedule,
                                shape = pillShape,
                                testTag = "notif_zakat_time_btn",
                                modifier = Modifier.weight(1f),
                                onClick = { activeTimeTarget = NotificationTimeTarget.ZAKAT_HAUL }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { triggerCategoryTest(NotificationCategoryType.ZAKAT_HAUL) },
                                shape = pillShape,
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("notif_test_zakat_btn")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Uji", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Kalender Hisab Haul:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ZakatHaulCalendarMode.entries.forEach { calMode ->
                                FilterChip(
                                    selected = zakat.calendarMode == calMode,
                                    onClick = {
                                        viewModel.updateNotificationConfig(
                                            config.copy(zakatHaul = zakat.copy(calendarMode = calMode))
                                        )
                                    },
                                    label = { Text(calMode.title, fontSize = 11.sp) },
                                    shape = pillShape,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("notif_zakat_cal_${calMode.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationSwitchRow(
                            title = "Peringatan Mendekati Ambang Nisab",
                            subtitle = "Beri kabar saat kekayaan bersih mencapai ${zakat.approachingNisabPercent}% nisab",
                            checked = zakat.approachingNisabEnabled,
                            testTag = "notif_zakat_approaching_switch",
                            onCheckedChange = {
                                viewModel.updateNotificationConfig(
                                    config.copy(zakatHaul = zakat.copy(approachingNisabEnabled = it))
                                )
                            }
                        )
                        AnimatedVisibility(visible = zakat.approachingNisabEnabled) {
                            Column(modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(70, 80, 90, 95).forEach { pct ->
                                        FilterChip(
                                            selected = zakat.approachingNisabPercent == pct,
                                            onClick = {
                                                viewModel.updateNotificationConfig(
                                                    config.copy(zakatHaul = zakat.copy(approachingNisabPercent = pct))
                                                )
                                            },
                                            label = { Text("$pct% Nisab", fontSize = 11.sp) },
                                            shape = pillShape,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        )
                                    }
                                    listOf(7, 14, 30).forEach { rep ->
                                        FilterChip(
                                            selected = zakat.approachingRepeatDays == rep,
                                            onClick = {
                                                viewModel.updateNotificationConfig(
                                                    config.copy(zakatHaul = zakat.copy(approachingRepeatDays = rep))
                                                )
                                            },
                                            label = { Text("Ulang /$rep Hari", fontSize = 11.sp) },
                                            shape = pillShape,
                                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pengingat Menjelang Genap Haul (${zakat.leadDaysSummary()}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmanahNotificationConfig.AVAILABLE_ZAKAT_LEAD_DAYS.forEach { lead ->
                                val selected = lead in zakat.haulLeadDays
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val nextSet = if (selected) zakat.haulLeadDays - lead else zakat.haulLeadDays + lead
                                        viewModel.updateNotificationConfig(
                                            config.copy(zakatHaul = zakat.copy(haulLeadDays = nextSet))
                                        )
                                    },
                                    label = { Text(if (lead == 0) "Genap Haul" else "H-$lead", fontSize = 11.sp) },
                                    shape = pillShape,
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("notif_zakat_lead_$lead")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Frekuensi Pengingat Jika Sudah Genap Haul:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(3, 7, 14, 30).forEach { overdueDays ->
                                FilterChip(
                                    selected = zakat.overdueRepeatDays == overdueDays,
                                    onClick = {
                                        viewModel.updateNotificationConfig(
                                            config.copy(zakatHaul = zakat.copy(overdueRepeatDays = overdueDays))
                                        )
                                    },
                                    label = { Text("Tiap $overdueDays Hari", fontSize = 11.sp) },
                                    shape = pillShape,
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        var zakatCustomNote by remember(zakat.customNote) { mutableStateOf(zakat.customNote) }
                        OutlinedTextField(
                            value = zakatCustomNote,
                            onValueChange = { note ->
                                zakatCustomNote = note
                                viewModel.updateNotificationConfig(
                                    config.copy(zakatHaul = zakat.copy(customNote = note))
                                )
                            },
                            label = { Text("Catatan Kustom Zakat / Mustahiq Prioritas (Opsional)") },
                            placeholder = { Text("Cth: Prioritaskan penyaluran ke kerabat dhuafa & BAZNAS") },
                            singleLine = true,
                            shape = pillShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("notif_zakat_custom_note_input")
                        )
                    }

                    // 5. KARTU PENJADWALAN INFAQ VAULT (TERINTEGRASI)
                    InfaqVaultReminderCard(
                        config = state.infaqVaultReminderConfig,
                        vaultBalance = state.virtualInfaqVaultBalance,
                        rules = state.rules,
                        onUpdateConfig = { viewModel.updateInfaqVaultReminderConfig(it) },
                        onSendTestNotification = { triggerCategoryTest(NotificationCategoryType.INFAQ_VAULT) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // M3 TimePicker Dialog for all 5 customizable time slots
    activeTimeTarget?.let { target ->
        val (initHour, initMinute, titleText) = when (target) {
            NotificationTimeTarget.QUIET_START -> Triple(config.quietStartHour, config.quietStartMinute, "Mulai Jam Tenang")
            NotificationTimeTarget.QUIET_END -> Triple(config.quietEndHour, config.quietEndMinute, "Selesai Jam Tenang")
            NotificationTimeTarget.DAILY_INFAQ -> Triple(config.dailyInfaq.hour, config.dailyInfaq.minute, "Jam Pengingat Infaq Harian")
            NotificationTimeTarget.DEBT_QARDH -> Triple(config.debt.hour, config.debt.minute, "Jam Pengingat Hutang & Rutin")
            NotificationTimeTarget.ZAKAT_HAUL -> Triple(config.zakatHaul.hour, config.zakatHaul.minute, "Jam Pengingat Haul & Nisab Zakat")
        }
        val pickerState = rememberTimePickerState(
            initialHour = initHour.coerceIn(0, 23),
            initialMinute = initMinute.coerceIn(0, 59),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { activeTimeTarget = null },
            shape = cardShape,
            title = { Text(titleText, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = pickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = pickerState.hour
                        val m = pickerState.minute
                        val updated = when (target) {
                            NotificationTimeTarget.QUIET_START -> config.copy(quietStartHour = h, quietStartMinute = m)
                            NotificationTimeTarget.QUIET_END -> config.copy(quietEndHour = h, quietEndMinute = m)
                            NotificationTimeTarget.DAILY_INFAQ -> config.copy(dailyInfaq = config.dailyInfaq.copy(hour = h, minute = m))
                            NotificationTimeTarget.DEBT_QARDH -> config.copy(debt = config.debt.copy(hour = h, minute = m))
                            NotificationTimeTarget.ZAKAT_HAUL -> config.copy(zakatHaul = config.zakatHaul.copy(hour = h, minute = m))
                        }
                        viewModel.updateNotificationConfig(updated)
                        activeTimeTarget = null
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Simpan Jam")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { activeTimeTarget = null },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun NotificationCategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    switchTag: String,
    onToggle: (Boolean) -> Unit,
    isTactical: Boolean,
    cardShape: androidx.compose.ui.graphics.Shape,
    content: @Composable () -> Unit
) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isTactical) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(if (isTactical) CutCornerShape(6.dp) else CircleShape)
                        .background(
                            if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag(switchTag)
                )
            }

            AnimatedVisibility(visible = enabled) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun TimeSelectorPill(
    label: String,
    timeText: String,
    icon: ImageVector,
    shape: androidx.compose.ui.graphics.Shape,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(timeText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun NotificationSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

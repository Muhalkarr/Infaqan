package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.AutoLockInterval
import com.example.core.security.SecurityEventType
import com.example.core.security.SecurityLogEntry
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.White60
import com.example.ui.theme.White70

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val securityConfig = state.securityConfig

    var showEnablePinDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var showUpdateRecoveryDialog by remember { mutableStateOf(false) }
    var showAutoLockDialog by remember { mutableStateOf(false) }
    var showClearLogsDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pengamanan & Privasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("security_settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Dashboard",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier.testTag("security_menu_sidebar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Buka Menu Sidebar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Security Status Card Hero
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("security_status_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (securityConfig.isPinEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (securityConfig.isPinEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (securityConfig.isPinEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (securityConfig.isPinEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (securityConfig.isPinEnabled) "Proteksi Aktif (PIN Terpasang)" else "Proteksi Belum Diaktifkan",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (securityConfig.isPinEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (securityConfig.isPinEnabled)
                                    "Buku besar & data kas terlindungi dengan enkripsi PIN & biometrik."
                                else
                                    "Aktifkan PIN untuk mencegah pihak lain membuka buku besar keuangan Anda.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Section 1: Kunci Aplikasi & Biometrik
            item {
                Text(
                    text = "KUNCI APLIKASI (APP LOCK)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Toggle PIN Protection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Password, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Gunakan Kunci PIN 6-Digit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Kunci layar meminta PIN saat membuka aplikasi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = securityConfig.isPinEnabled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        showEnablePinDialog = true
                                    } else {
                                        showDisablePinDialog = true
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.DarkGray
                                ),
                                modifier = Modifier.testTag("toggle_pin_switch")
                            )
                        }

                        if (securityConfig.isPinEnabled) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            // Ubah PIN
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showChangePinDialog = true }
                                    .padding(vertical = 4.dp)
                                    .testTag("change_pin_row"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Ubah PIN Keamanan", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Ganti kombinasi 6-digit PIN Anda", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Icon(Icons.Default.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            // Pertanyaan Pemulihan Darurat PIN
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showUpdateRecoveryDialog = true }
                                    .padding(vertical = 4.dp)
                                    .testTag("update_recovery_question_row"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Pertanyaan Pemulihan PIN", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        Text(
                                            text = if (securityConfig.securityQuestion.isNotBlank()) securityConfig.securityQuestion else "Belum disetel (Ketuk untuk atur)",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            // Biometric Unlock Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Buka dengan Sidik Jari (Biometrik)", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        Text("Gunakan sensor fingerprint untuk login instan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(
                                    checked = securityConfig.isBiometricEnabled,
                                    onCheckedChange = { viewModel.setBiometricEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("toggle_biometric_switch")
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            // Auto-Lock Timeout
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAutoLockDialog = true }
                                    .padding(vertical = 4.dp)
                                    .testTag("auto_lock_interval_row"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Kunci Otomatis", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        Text(securityConfig.autoLockInterval.label, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Text("Pilih", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 2: Privasi Tampilan & Penyamaran Nilai Saldo
            item {
                Text(
                    text = "PRIVASI & PENYAMARAN SALDO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Sembunyikan Saldo (Masking)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (securityConfig.isMaskBalance) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Mode Privasi Nilai Saldo", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Samarkan nominal harta (Rp ••••••••) di layar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = securityConfig.isMaskBalance,
                                onCheckedChange = { viewModel.toggleBalancePrivacy() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.testTag("toggle_mask_balance_switch")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        // Privasi Layar & Screenshot Prevention
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Perlindungan Tangkapan Layar", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Cegah perekaman layar & pratinjau saat ganti aplikasi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = securityConfig.isScreenshotProtected,
                                onCheckedChange = { viewModel.setScreenshotProtection(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("toggle_screenshot_protection_switch")
                            )
                        }
                    }
                }
            }

            // Section 3: Aksi Keamanan Cepat
            item {
                Text(
                    text = "AKSI CEPAT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 0.8.sp
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (securityConfig.isPinEnabled) {
                                viewModel.lockApp()
                            } else {
                                showEnablePinDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quick_lock_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kunci Aplikasi Sekarang", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 4: Riwayat & Log Audit Keamanan
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOG AKTIVITAS KEAMANAN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 0.8.sp
                        )
                    }

                    if (securityConfig.logs.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearLogsDialog = true },
                            modifier = Modifier.testTag("clear_security_logs_button")
                        ) {
                            Text("Bersihkan", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            items(securityConfig.logs.take(15), key = { it.id }) { log ->
                SecurityLogCard(log = log)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialogs
    if (showEnablePinDialog) {
        EnablePinDialog(
            onDismiss = { showEnablePinDialog = false },
            onConfirm = { pin, question, answer ->
                val ok = viewModel.enablePin(pin, question, answer)
                if (ok) showEnablePinDialog = false
                ok
            }
        )
    }

    if (showDisablePinDialog) {
        DisablePinDialog(
            onDismiss = { showDisablePinDialog = false },
            onConfirm = { pin ->
                val ok = viewModel.disablePin(pin)
                if (ok) showDisablePinDialog = false
                ok
            }
        )
    }

    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onConfirm = { oldPin, newPin ->
                val ok = viewModel.changePin(oldPin, newPin)
                if (ok) showChangePinDialog = false
                ok
            }
        )
    }

    if (showUpdateRecoveryDialog) {
        UpdateRecoveryQuestionDialog(
            currentQuestion = securityConfig.securityQuestion,
            onDismiss = { showUpdateRecoveryDialog = false },
            onConfirm = { pin, question, answer ->
                val ok = viewModel.updateSecurityRecovery(pin, question, answer)
                if (ok) showUpdateRecoveryDialog = false
                ok
            }
        )
    }

    if (showAutoLockDialog) {
        AutoLockIntervalDialog(
            currentInterval = securityConfig.autoLockInterval,
            onDismiss = { showAutoLockDialog = false },
            onSelect = { interval ->
                viewModel.setAutoLockInterval(interval)
                showAutoLockDialog = false
            }
        )
    }

    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Bersihkan Log Keamanan?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Riwayat aktivitas audit keamanan akan dihapus dari memori.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearSecurityLogs()
                        showClearLogsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_clear_logs_button")
                ) {
                    Text("Bersihkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearLogsDialog = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
fun SecurityLogCard(log: SecurityLogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (log.isWarning) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (log.isWarning) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (log.eventType) {
                        SecurityEventType.PIN_SUCCESS, SecurityEventType.BIOMETRIC_SUCCESS -> Icons.Default.CheckCircle
                        SecurityEventType.PIN_FAILED -> Icons.Default.Warning
                        SecurityEventType.PIN_ENABLED, SecurityEventType.PIN_CHANGED -> Icons.Default.Key
                        SecurityEventType.PIN_DISABLED -> Icons.Default.LockOpen
                        SecurityEventType.BALANCE_PRIVACY_TOGGLED -> Icons.Default.Visibility
                        SecurityEventType.APP_LOCKED -> Icons.Default.Lock
                        else -> Icons.Default.Shield
                    },
                    contentDescription = null,
                    tint = if (log.isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (log.isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = log.formattedTime,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EnablePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (pin: String, question: String, answer: String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("Nama kota kelahiran Anda?") }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atur Kunci PIN 6-Digit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Masukkan 6-digit angka untuk mengamankan akses ke buku besar keuangan.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("PIN 6-Digit") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("setup_pin_field")
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmPin = it },
                    label = { Text("Konfirmasi PIN") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("setup_confirm_pin_field")
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Pertanyaan Pemulihan") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("setup_question_field")
                )

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Jawaban Pemulihan") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("setup_answer_field")
                )

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length != 6) {
                        error = "PIN harus 6 digit angka"
                        return@Button
                    }
                    if (pin != confirmPin) {
                        error = "Konfirmasi PIN tidak sesuai"
                        return@Button
                    }
                    if (answer.trim().isEmpty()) {
                        error = "Jawaban pemulihan wajib diisi"
                        return@Button
                    }
                    val ok = onConfirm(pin, question, answer)
                    if (!ok) error = "Gagal menyimpan konfigurasi PIN"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("submit_setup_pin_button")
            ) {
                Text("Aktifkan PIN", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
fun DisablePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (pin: String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nonaktifkan Kunci PIN?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Masukkan PIN saat ini untuk menonaktifkan proteksi kunci aplikasi.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("PIN Saat Ini") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("disable_pin_field")
                )
                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ok = onConfirm(pin)
                    if (!ok) error = "PIN salah. Gagal menonaktifkan."
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("submit_disable_pin_button")
            ) {
                Text("Nonaktifkan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPin: String, newPin: String) -> Boolean
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ganti PIN Keamanan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) oldPin = it },
                    label = { Text("PIN Lama") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("change_old_pin_field")
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("PIN Baru (6 Digit)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("change_new_pin_field")
                )

                OutlinedTextField(
                    value = confirmNewPin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) confirmNewPin = it },
                    label = { Text("Konfirmasi PIN Baru") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("change_confirm_new_pin_field")
                )

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin.length != 6) {
                        error = "PIN baru harus 6 digit angka"
                        return@Button
                    }
                    if (newPin != confirmNewPin) {
                        error = "Konfirmasi PIN baru tidak sesuai"
                        return@Button
                    }
                    val ok = onConfirm(oldPin, newPin)
                    if (!ok) error = "PIN lama tidak sesuai"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("submit_change_pin_button")
            ) {
                Text("Simpan PIN Baru", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

@Composable
fun UpdateRecoveryQuestionDialog(
    currentQuestion: String,
    onDismiss: () -> Unit,
    onConfirm: (pin: String, newQuestion: String, newAnswer: String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf(if (currentQuestion.isNotBlank()) currentQuestion else "Apa nama kota kelahiran Anda?") }
    var customQuestion by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val presetQuestions = listOf(
        "Apa nama kota kelahiran Anda?",
        "Siapa nama guru mengaji pertama Anda?",
        "Apa nama masjid terdekat di masa kecil?",
        "Apa nama jalan tempat tinggal pertama Anda?",
        "Pertanyaan Kustom Sendiri..."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        title = {
            Text(
                "Atur Pertanyaan Pemulihan PIN",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Digunakan untuk mereset PIN jika Anda lupa kombinasi angka.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pin = it },
                    label = { Text("Verifikasi PIN Saat Ini") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recovery_current_pin_field")
                )

                Text("Pilih Pertanyaan Keamanan:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

                presetQuestions.forEach { q ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (q == "Pertanyaan Kustom Sendiri...") {
                                    isCustom = true
                                } else {
                                    isCustom = false
                                    selectedQuestion = q
                                }
                            }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = if (isCustom) q == "Pertanyaan Kustom Sendiri..." else selectedQuestion == q,
                            onClick = {
                                if (q == "Pertanyaan Kustom Sendiri...") {
                                    isCustom = true
                                } else {
                                    isCustom = false
                                    selectedQuestion = q
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(q, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (isCustom) {
                    OutlinedTextField(
                        value = customQuestion,
                        onValueChange = { customQuestion = it },
                        label = { Text("Tulis Pertanyaan Anda Sendiri") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("custom_recovery_question_field")
                    )
                }

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Kunci Jawaban Rahasia") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recovery_answer_field")
                )

                if (error != null) {
                    Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length < 4) {
                        error = "Masukkan PIN otentikasi saat ini"
                        return@Button
                    }
                    val finalQ = if (isCustom) customQuestion.trim() else selectedQuestion.trim()
                    if (finalQ.isBlank()) {
                        error = "Pertanyaan tidak boleh kosong"
                        return@Button
                    }
                    if (answer.trim().isBlank()) {
                        error = "Kunci jawaban rahasia tidak boleh kosong"
                        return@Button
                    }
                    val ok = onConfirm(pin, finalQ, answer.trim())
                    if (!ok) {
                        error = "PIN otentikasi salah!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("submit_update_recovery_button")
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AutoLockIntervalDialog(
    currentInterval: AutoLockInterval,
    onDismiss: () -> Unit,
    onSelect: (AutoLockInterval) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        title = { Text("Waktu Kunci Otomatis", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AutoLockInterval.entries.forEach { interval ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(interval) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (interval == currentInterval),
                            onClick = { onSelect(interval) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(interval.label, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}

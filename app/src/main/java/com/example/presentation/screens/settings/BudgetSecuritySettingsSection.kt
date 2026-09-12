package com.example.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.budget.FinancialGoalMode
import com.example.core.security.AutoLockInterval
import com.example.core.state.AmanahLedgerUiState

@Composable
fun BudgetSettingsSection(
    state: AmanahLedgerUiState,
    onSetFinancialGoalMode: (FinancialGoalMode) -> Unit,
    onSetIsrafWarningThresholdPercent: (Int) -> Unit,
    onSetStrictBudgetEnforced: (Boolean) -> Unit
) {
    SettingsSectionCard(
        title = "Anggaran & Batas Pengeluaran Syariah",
        subtitle = "Strategi alokasi kebutuhan pokok dan pengawasan israf",
        icon = Icons.Default.PieChart
    ) {
        Text(
            text = "Model Alokasi Anggaran Syariah",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        FinancialGoalMode.entries.forEach { mode ->
            val isSelected = state.selectedGoalMode == mode
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSetFinancialGoalMode(mode) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.PieChart,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = mode.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Slider Ambang Israf
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ambang Batas Peringatan Israf",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${state.israfWarningThresholdPercent}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Slider(
                value = state.israfWarningThresholdPercent.toFloat(),
                onValueChange = { onSetIsrafWarningThresholdPercent(it.toInt()) },
                valueRange = 50f..100f,
                steps = 9,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.error,
                    activeTrackColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("settings_israf_slider")
            )
            Text(
                text = "⚠️ Peringatan israf (pemborosan) akan muncul bila pengeluaran pos melampaui ${state.israfWarningThresholdPercent}% dari plafon bulanan.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Switch: Peringatan Ketat
        SettingsSwitchRow(
            title = "Peringatan Ketat Over-Budget",
            subtitle = "Berikan notifikasi keras jika suatu pos melewati 100% batas bulanan",
            checked = state.isStrictBudgetEnforced,
            testTag = "settings_strict_budget_switch",
            onCheckedChange = onSetStrictBudgetEnforced
        )
    }
}

@Composable
fun RecurringSettingsSection(
    state: AmanahLedgerUiState,
    onSetAutoExecuteRecurringEnabled: (Boolean) -> Unit,
    onSetNotifyOnRecurringDue: (Boolean) -> Unit
) {
    SettingsSectionCard(
        title = "Transaksi Berulang & Otomasi",
        subtitle = "Pengelolaan eksekusi transaksi terjadwal otomatis",
        icon = Icons.Default.Schedule
    ) {
        SettingsSwitchRow(
            title = "Eksekusi Otomatis saat Jatuh Tempo",
            subtitle = "Catat otomatis transaksi rutin (gaji, tagihan, SPP) saat tanggal tiba",
            checked = state.autoExecuteRecurringEnabled,
            testTag = "settings_auto_recurring_switch",
            onCheckedChange = onSetAutoExecuteRecurringEnabled
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        SettingsSwitchRow(
            title = "Notifikasi Pengingat Jatuh Tempo",
            subtitle = "Berikan banner peringatan untuk transaksi berulang yang perlu dieksekusi",
            checked = state.notifyOnRecurringDue,
            testTag = "settings_notify_recurring_switch",
            onCheckedChange = onSetNotifyOnRecurringDue
        )
    }
}

@Composable
fun SecuritySettingsSection(
    state: AmanahLedgerUiState,
    onNavigateToSecuritySettings: () -> Unit,
    onSetBiometricEnabled: (Boolean) -> Unit,
    onSetAutoLockInterval: (AutoLockInterval) -> Unit,
    onLockAppNow: () -> Unit
) {
    SettingsSectionCard(
        title = "Keamanan & Kunci Aplikasi",
        subtitle = "Proteksi PIN 6-digit, sidik jari, dan interval auto-lock",
        icon = Icons.Default.Shield
    ) {
        // Status PIN
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Status Kunci PIN 6-Digit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (state.securityConfig.isPinEnabled) "Aktif • Enkripsi SHA-256" else "Belum Diaktifkan",
                    fontSize = 11.sp,
                    color = if (state.securityConfig.isPinEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = onNavigateToSecuritySettings,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.securityConfig.isPinEnabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("settings_open_security_center_button")
            ) {
                Text(
                    text = if (state.securityConfig.isPinEnabled) "Kelola PIN" else "Atur PIN",
                    fontSize = 12.sp,
                    color = if (state.securityConfig.isPinEnabled) MaterialTheme.colorScheme.onSurface else Color.White
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Switch: Biometrik
        SettingsSwitchRow(
            title = "Otentikasi Biometrik / Sidik Jari",
            subtitle = "Gunakan pemindai sidik jari perangkat untuk membuka kunci",
            checked = state.securityConfig.isBiometricEnabled,
            testTag = "settings_biometric_switch",
            onCheckedChange = onSetBiometricEnabled
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Interval Auto-Lock
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text = "Interval Kunci Otomatis (Auto-Lock)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AutoLockInterval.entries.forEach { interval ->
                    val isSelected = state.securityConfig.autoLockInterval == interval
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onSetAutoLockInterval(interval) }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = when (interval) {
                                AutoLockInterval.IMMEDIATE -> "Segera"
                                AutoLockInterval.SECONDS_15 -> "15s"
                                AutoLockInterval.MINUTE_1 -> "1m"
                                AutoLockInterval.MINUTES_5 -> "5m"
                                AutoLockInterval.NEVER -> "Mati"
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (state.securityConfig.isPinEnabled) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onLockAppNow,
                modifier = Modifier.fillMaxWidth().testTag("settings_lock_now_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kunci Aplikasi Sekarang", fontSize = 12.sp)
            }
        }
    }
}

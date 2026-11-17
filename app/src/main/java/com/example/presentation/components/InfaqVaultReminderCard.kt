package com.example.presentation.components

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.infaq.InfaqReminderFrequency
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.InfaqVaultReminderConfig
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.isTactical
import com.example.ui.theme.rememberAppCardShape
import com.example.ui.theme.rememberAppShape
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfaqVaultReminderCard(
    config: InfaqVaultReminderConfig,
    vaultBalance: Double,
    rules: List<InfaqRule>,
    onUpdateConfig: (InfaqVaultReminderConfig) -> Unit,
    onSendTestNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeMode = LocalAppThemeMode.current
    val isTactical = themeMode.isTactical
    val cardShape = rememberAppCardShape(16)
    val pillShape = rememberAppShape(8)
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var pendingActionAfterPermission by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    var permissionGranted by remember { mutableStateOf(hasNotificationPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) {
            pendingActionAfterPermission?.invoke()
            pendingActionAfterPermission = null
        } else {
            pendingActionAfterPermission = null
            Toast.makeText(
                context,
                "Izin notifikasi ditolak. Aktifkan izin notifikasi di Setelan Aplikasi agar pengingat Vault dapat tampil.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun ensurePermissionAndRun(action: () -> Unit) {
        if (hasNotificationPermission()) {
            permissionGranted = true
            action()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingActionAfterPermission = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("infaq_vault_reminder_card"),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (config.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header + Master Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(pillShape)
                            .background(
                                if (config.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (config.isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Notifikasi Pengingat Infaq Vault",
                            tint = if (config.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isTactical) "REMINDER // INFAQ VAULT PUSH" else "Notifikasi Pengingat Penyaluran Vault",
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = config.scheduleSummaryText(),
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            fontSize = 11.sp,
                            color = if (config.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = config.isEnabled,
                    onCheckedChange = { wantEnable ->
                        if (wantEnable) {
                            ensurePermissionAndRun {
                                onUpdateConfig(config.copy(isEnabled = true))
                                Toast.makeText(
                                    context,
                                    "Pengingat Vault diaktifkan: ${config.copy(isEnabled = true).scheduleSummaryText()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            onUpdateConfig(config.copy(isEnabled = false))
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("infaq_reminder_enable_switch")
                )
            }

            // Permission Warning Banner (Android 13+)
            if (!permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Surface(
                    shape = pillShape,
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Izin notifikasi sistem belum aktif. Izinkan agar pengingat rutin bisa muncul.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        TextButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Setelan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Rule & Vault Balance Context Info
            Surface(
                shape = pillShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saldo Virtual Infaq Vault Saat Ini:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rp ${nf.format(vaultBalance.toLong())}",
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Aturan Otomasi Infaq Terhubung:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${rules.size} Aturan Aktif",
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (rules.isNotEmpty()) {
                        Text(
                            text = "Ringkasan aturan: ${rules.take(2).joinToString(", ") { it.title }}${if (rules.size > 2) " (+${rules.size - 2} lainnya)" else ""}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Detailed Schedule Controls (Always editable so users can pre-configure or tweak)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 1. Frequency Selector (Harian / Mingguan)
            Text(
                text = "Frekuensi Pengingat",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfaqReminderFrequency.entries.forEach { freq ->
                    val isSelected = config.frequency == freq
                    Surface(
                        shape = pillShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.2.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(pillShape)
                            .clickable { onUpdateConfig(config.copy(frequency = freq)) }
                            .testTag("infaq_reminder_freq_${freq.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = freq.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (freq == InfaqReminderFrequency.DAILY) "Setiap Hari" else "Sepekan Sekali",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Day of Week Selector (when Weekly)
            AnimatedVisibility(visible = config.frequency == InfaqReminderFrequency.WEEKLY) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Pilih Hari Pengingat Mingguan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfaqVaultReminderConfig.DAYS_OF_WEEK.forEach { (dayInt, dayName) ->
                            val isSelected = config.dayOfWeek == dayInt
                            Surface(
                                shape = pillShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 54.dp, minHeight = 40.dp)
                                    .clip(pillShape)
                                    .clickable { onUpdateConfig(config.copy(dayOfWeek = dayInt)) }
                                    .testTag("infaq_reminder_day_$dayInt")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Specific Time Selector + Presets
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Waktu Notifikasi Spesifik",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = pillShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(pillShape)
                            .clickable { showTimePickerDialog = true }
                            .testTag("infaq_reminder_time_picker_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Ubah Jam Pengingat",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Pukul ${config.formattedTime()} WIB",
                                    fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Ubah Jam",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Quick time presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple(5, 30, "Subuh 05:30"),
                        Triple(9, 0, "Duha 09:00"),
                        Triple(12, 30, "Zuhur 12:30"),
                        Triple(19, 30, "Isya 19:30")
                    ).forEach { (h, m, label) ->
                        val isPreset = config.hour == h && config.minute == m
                        Surface(
                            shape = pillShape,
                            color = if (isPreset) MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isPreset) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(pillShape)
                                .clickable { onUpdateConfig(config.copy(hour = h, minute = m)) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isPreset) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPreset) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 4. Minimum Vault Threshold Filter
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Ambang Minimal Saldo Vault untuk Notifikasi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        0.0 to "> Rp 0",
                        50_000.0 to "≥ 50rb",
                        100_000.0 to "≥ 100rb",
                        250_000.0 to "≥ 250rb",
                        500_000.0 to "≥ 500rb"
                    ).forEach { (amt, lbl) ->
                        val isSel = kotlin.math.abs(config.minVaultBalanceThreshold - amt) < 1.0
                        Surface(
                            shape = pillShape,
                            color = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(pillShape)
                                .clickable { onUpdateConfig(config.copy(minVaultBalanceThreshold = amt)) }
                        ) {
                            Text(
                                text = lbl,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 6. Recurring Payment (Auto-Donation) Feature Section
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Penyaluran Otomatis Berkala (Auto-Recurring)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = config.recurringSummaryText(),
                        fontSize = 10.sp,
                        color = if (config.isAutoRecurringEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.isAutoRecurringEnabled,
                    onCheckedChange = { onUpdateConfig(config.copy(isAutoRecurringEnabled = it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("infaq_recurring_switch")
                )
            }

            AnimatedVisibility(visible = config.isAutoRecurringEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Nominal Penyaluran Otomatis Per Jadwal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(20_000.0 to "20rb", 50_000.0 to "50rb", 100_000.0 to "100rb", 250_000.0 to "250rb").forEach { (amt, lbl) ->
                            val isSel = kotlin.math.abs(config.recurringAmount - amt) < 1.0
                            Surface(
                                shape = pillShape,
                                color = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(pillShape)
                                    .clickable { onUpdateConfig(config.copy(recurringAmount = amt)) }
                            ) {
                                Text(
                                    text = lbl,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = "Target Penyaluran Otomatis",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Fakir Miskin / 8 Asnaf",
                            "Yatim Piatu",
                            "Fisabilillah",
                            "Masjid & Pendidikan"
                        ).forEach { target ->
                            val isTargetSel = config.recurringTargetAsnaf == target
                            Surface(
                                shape = pillShape,
                                color = if (isTargetSel) MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (isTargetSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .clip(pillShape)
                                    .clickable { onUpdateConfig(config.copy(recurringTargetAsnaf = target)) }
                            ) {
                                Text(
                                    text = target,
                                    fontSize = 10.sp,
                                    fontWeight = if (isTargetSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isTargetSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Immediate Test Notification Button
            OutlinedButton(
                onClick = {
                    ensurePermissionAndRun {
                        onSendTestNotification()
                    }
                },
                shape = pillShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("infaq_reminder_test_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Kirim Notifikasi Uji Coba",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Kirim Notifikasi Uji Coba Sekarang",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showTimePickerDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = config.hour.coerceIn(0, 23),
            initialMinute = config.minute.coerceIn(0, 59),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Pilih Jam Pengingat Vault",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateConfig(
                            config.copy(
                                hour = timePickerState.hour,
                                minute = timePickerState.minute
                            )
                        )
                        showTimePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Simpan Jam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

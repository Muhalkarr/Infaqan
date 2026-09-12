package com.example.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.state.AmanahLedgerUiState
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.UiScaleMode

@Composable
fun ThemeOptionBox(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ThemeContrastSettingsSection(
    state: AmanahLedgerUiState,
    onSetThemeMode: (AppThemeMode) -> Unit,
    onSetMaskBalanceByDefault: (Boolean) -> Unit,
    onSetScreenshotProtection: (Boolean) -> Unit
) {
    SettingsSectionCard(
        title = "Tema & Kontras Tampilan",
        subtitle = "Preferensi mode visual terang/gelap dan privasi tampilan",
        icon = Icons.Default.Palette
    ) {
        Text(
            text = "Mode Tema Visual",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Opsi 1: Ikuti Sistem Ponsel (Otomatis)
        ThemeOptionBox(
            label = "Ikuti Sistem Ponsel (Otomatis)",
            icon = Icons.Default.BrightnessAuto,
            isSelected = state.themeMode == AppThemeMode.FOLLOW_SYSTEM,
            modifier = Modifier.fillMaxWidth(),
            testTag = "settings_theme_follow_system"
        ) {
            onSetThemeMode(AppThemeMode.FOLLOW_SYSTEM)
        }

        if (state.themeMode == AppThemeMode.FOLLOW_SYSTEM) {
            val systemIsDark = isSystemInDarkTheme()
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (systemIsDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = if (systemIsDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Status HP Anda: " + (if (systemIsDark) "Mode Gelap (Dark Mode)" else "Mode Cerah (Light Mode)") + " • Aplikasi aktif mengikuti sistem secara otomatis.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Theme Mode Selector Manual
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionBox(
                label = "Gelap Elegan",
                icon = Icons.Default.DarkMode,
                isSelected = state.themeMode == AppThemeMode.ELEGANT_DARK,
                modifier = Modifier.weight(1f),
                testTag = "settings_theme_dark"
            ) {
                onSetThemeMode(AppThemeMode.ELEGANT_DARK)
            }
            ThemeOptionBox(
                label = "Terang Bersih",
                icon = Icons.Default.LightMode,
                isSelected = state.themeMode == AppThemeMode.LIGHT_MODE,
                modifier = Modifier.weight(1f),
                testTag = "settings_theme_light"
            ) {
                onSetThemeMode(AppThemeMode.LIGHT_MODE)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeOptionBox(
                label = "Kontras Tinggi (Terang)",
                icon = Icons.Default.Visibility,
                isSelected = state.themeMode == AppThemeMode.HIGH_CONTRAST_LIGHT,
                modifier = Modifier.weight(1f),
                testTag = "settings_theme_hc_light"
            ) {
                onSetThemeMode(AppThemeMode.HIGH_CONTRAST_LIGHT)
            }
            ThemeOptionBox(
                label = "Kontras Tinggi (Gelap)",
                icon = Icons.Default.Visibility,
                isSelected = state.themeMode == AppThemeMode.HIGH_CONTRAST_DARK,
                modifier = Modifier.weight(1f),
                testTag = "settings_theme_hc_dark"
            ) {
                onSetThemeMode(AppThemeMode.HIGH_CONTRAST_DARK)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Switch: Samarkan Saldo Bawaan
        SettingsSwitchRow(
            title = "Samarkan Nilai Saldo secara Bawaan",
            subtitle = "Tampilkan 'Rp ••••••••' saat aplikasi baru dibuka",
            checked = state.securityConfig.maskBalanceByDefault,
            testTag = "settings_mask_balance_default_switch",
            onCheckedChange = onSetMaskBalanceByDefault
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

        // Switch: Perlindungan Screenshot & Pengalih Aplikasi
        SettingsSwitchRow(
            title = "Perlindungan Layar & Anti-Screenshot",
            subtitle = "Cegah tangkapan layar dan samarkan preview di recent apps",
            checked = state.securityConfig.isScreenshotProtected,
            testTag = "settings_screenshot_protection_switch",
            onCheckedChange = onSetScreenshotProtection
        )
    }
}

@Composable
fun TextScaleSettingsSection(
    state: AmanahLedgerUiState,
    onSetUiScaleMode: (UiScaleMode) -> Unit,
    onSetUiScaleFactor: (Float) -> Unit
) {
    SettingsSectionCard(
        title = "Skala Tampilan & Ukuran Teks",
        subtitle = "Pengaturan ukuran font, keterbacaan, dan kenyamanan visual",
        icon = Icons.Default.FormatSize
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ukuran Huruf & Skala Antarmuka",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mode: ${state.uiScaleMode.displayName} (${(state.uiScaleFactor * 100).toInt()}%)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${(state.uiScaleFactor * 100).toInt()}%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4 preset mode buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val modes = listOf(
                UiScaleMode.COMPACT to "85%",
                UiScaleMode.DEFAULT to "100%",
                UiScaleMode.LARGE to "115%",
                UiScaleMode.EXTRA_LARGE to "130%"
            )

            modes.forEach { (m, pctLabel) ->
                val isSelected = state.uiScaleMode == m
                Surface(
                    onClick = { onSetUiScaleMode(m) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1f).testTag("btn_ui_scale_${m.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = pctLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when(m) {
                                UiScaleMode.COMPACT -> "Ringkas"
                                UiScaleMode.DEFAULT -> "Standar"
                                UiScaleMode.LARGE -> "Besar"
                                UiScaleMode.EXTRA_LARGE -> "Ekstra"
                            },
                            fontSize = 9.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Penyesuaian Halus Skala:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Fine slider control
        Slider(
            value = state.uiScaleFactor,
            onValueChange = onSetUiScaleFactor,
            valueRange = 0.80f..1.35f,
            steps = 10,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth().testTag("slider_ui_scale_factor")
        )

        // Interactive live text preview
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatSize, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pratinjau Keterbacaan Teks Skala ${(state.uiScaleFactor * 100).toInt()}%:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Buku Besar Syariah Amanah: Catatan Transaksi Keuangan Halal & Berkah",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Kas Tunai: Rp 1.500.000 (Kasab) • Infaq Rutin: Rp 37.500",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

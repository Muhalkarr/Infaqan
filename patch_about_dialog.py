import re

with open('app/src/main/java/com/example/presentation/screens/settings/MaintenanceSettingsSection.kt', 'r') as f:
    content = f.read()

# Add icons to imports if needed
if "import androidx.compose.material.icons.filled.Info" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Warning", "import androidx.compose.material.icons.filled.Warning\nimport androidx.compose.material.icons.filled.Info\nimport androidx.compose.material.icons.filled.Copyright")

# Add state
if "var showAboutDialog by remember" not in content:
    content = content.replace("fun MaintenanceSettingsSection(", "fun MaintenanceSettingsSection(\n    ")
    state_injection = """
    var showAboutDialog by remember { mutableStateOf(false) }
    
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Tentang Aplikasi", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Infaqan Syariah (Amanah Ledger)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Versi: 2.4 (Stable Release)\\nArsitektur: 100% Offline & Syariah-Compliant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Dikembangkan Oleh:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Muhammad Abdul Kholik Arrasyid",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "muhammadabdulkholikarrasyid@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Copyright, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "2026 Hak Cipta Dilindungi Undang-Undang.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
"""
    content = content.replace("SettingsSectionCard(", state_injection + "    SettingsSectionCard(")

# Add the button to open it
button_injection = """
        Spacer(modifier = Modifier.height(6.dp))

        // Tentang & Hak Cipta
        OutlinedButton(
            onClick = { showAboutDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("settings_about_button")
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Identitas Pengembang & Hak Cipta", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
"""
content = re.sub(r'(\s*// Salin Ringkasan Jurnal)', button_injection + r'\1', content)

with open('app/src/main/java/com/example/presentation/screens/settings/MaintenanceSettingsSection.kt', 'w') as f:
    f.write(content)


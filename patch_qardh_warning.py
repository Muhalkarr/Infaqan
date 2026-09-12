import re

with open('app/src/main/java/com/example/presentation/screens/QardhScreen.kt', 'r') as f:
    content = f.read()

# Add a warning badge if due date is passed or within 3 days
warning_code = """
            if (record.dueDateMillis != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val now = System.currentTimeMillis()
                val daysLeft = ((record.dueDateMillis - now) / 86400000L).toInt()
                val isWarning = daysLeft <= 3 && record.status != QardhStatus.LUNAS && record.status != QardhStatus.DIIKHLASKAN_SEDEKAH
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isWarning) Icons.Default.Warning else Icons.Default.Event,
                        contentDescription = null,
                        tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (daysLeft < 0) "Jatuh Tempo: Terlewat ${-daysLeft} hari!" else if (daysLeft == 0) "Jatuh Tempo: HARI INI!" else "Jatuh Tempo: ${dateFormat.format(Date(record.dueDateMillis))} ($daysLeft hari lagi)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isWarning) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
"""

content = re.sub(
    r'            if \(record\.dueDateMillis != null\) \{\s*Spacer\(modifier = Modifier\.height\(6\.dp\)\)\s*Row\(verticalAlignment = Alignment\.CenterVertically\) \{\s*Icon\(\s*Icons\.Default\.Event,\s*contentDescription = null,\s*tint = MaterialTheme\.colorScheme\.primary,\s*modifier = Modifier\.size\(14\.dp\)\s*\)\s*Spacer\(modifier = Modifier\.width\(4\.dp\)\)\s*Text\(\s*"Jatuh Tempo: \$\{dateFormat\.format\(Date\(record\.dueDateMillis\)\)\}",\s*style = MaterialTheme\.typography\.labelSmall,\s*color = MaterialTheme\.colorScheme\.onSurfaceVariant\s*\)\s*\}\s*\}',
    warning_code.strip(),
    content
)

with open('app/src/main/java/com/example/presentation/screens/QardhScreen.kt', 'w') as f:
    f.write(content)


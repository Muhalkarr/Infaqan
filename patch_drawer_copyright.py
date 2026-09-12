import re

with open('app/src/main/java/com/example/presentation/components/AppNavigationDrawer.kt', 'r') as f:
    content = f.read()

old_text = 'text = "Amanah Ledger v2.4 • 100% Syariah-Compliant"'
new_text = 'text = "Amanah Ledger v2.4\\n© 2026 Muhammad Abdul Kholik Arrasyid\\n100% Syariah-Compliant", textAlign = androidx.compose.ui.text.style.TextAlign.Center'

content = content.replace(old_text, new_text)

with open('app/src/main/java/com/example/presentation/components/AppNavigationDrawer.kt', 'w') as f:
    f.write(content)


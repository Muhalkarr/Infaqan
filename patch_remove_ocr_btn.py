import re

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

btn_pattern = r'            // Quick OCR Receipt Scanner Button\s*FilledTonalButton\(\s*onClick = \{ showOcrScannerDialog = true \},[\s\S]*?\n            \}\n'
content = re.sub(btn_pattern, '', content)
content = re.sub(r'\s*var showOcrScannerDialog by remember \{ mutableStateOf\(false\) \}\n', '\n', content)
content = re.sub(r'\s*var showLiveCameraScanner by remember \{ mutableStateOf\(false\) \}\n', '\n', content)

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


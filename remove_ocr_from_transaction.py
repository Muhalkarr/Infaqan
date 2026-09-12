import re

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

# Remove imports
content = re.sub(r'import com\.example\.core\.camera\.CameraXReceiptScanner\n?', '', content)
content = re.sub(r'import com\.example\.core\.ocr\.OcrReceiptScannerDialog\n?', '', content)
content = re.sub(r'import com\.example\.core\.ocr\.ParsedReceiptData\n?', '', content)

# Remove the camera and OCR dialog code block
# To be safe, we'll just replace the calls and the OcrReceiptScannerDialog composable
# Actually, the OcrReceiptScannerDialog is defined at the end of the file
dialog_pattern = r'@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun OcrReceiptScannerDialog\([\s\S]*'
content = re.sub(dialog_pattern, '', content)

# Remove the usages
# 1. showLiveCameraScanner
content = re.sub(r'    if \(showLiveCameraScanner\) \{[\s\S]*?            onDismiss = \{ showLiveCameraScanner = false \}\n        \)\n    \}\n', '', content)

# 2. showOcrScannerDialog
content = re.sub(r'    if \(showOcrScannerDialog\) \{[\s\S]*?            onApplyParsedData = \{[\s\S]*?            \}\n        \)\n    \}\n', '', content)

# Remove the button that triggers OCR
# The button might be inside a Row or IconButton. Let's find "showOcrScannerDialog = true"
content = re.sub(r'(\s*)IconButton\(onClick = \{ showOcrScannerDialog = true \}\) \{\s*Icon\(imageVector = Icons\.Default\.DocumentScanner[\s\S]*?\}', '', content)

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


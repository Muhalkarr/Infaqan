import re

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

# Add imports for Numpad and BottomSheet
if "import com.example.presentation.components.AmanahNumpad" not in content:
    content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport com.example.presentation.components.AmanahNumpad\nimport androidx.compose.ui.focus.onFocusChanged\nimport androidx.compose.ui.platform.LocalFocusManager")

# Find the start of the Composable to add state
state_code = """
    val focusManager = LocalFocusManager.current
    var showNumpadBottomSheet by remember { mutableStateOf(false) }
    
    val hijriDate = remember(selectedDate) {
"""
content = content.replace("val hijriDate = remember(selectedDate) {", state_code)

# Replace the OutlinedTextField for Amount
old_textfield = """            // 3. Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { char -> char.isDigit() }
                    errorMessage = null
                },
                label = { Text("Nominal Transaksi (Rp)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                prefix = { Text("Rp ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,"""

new_textfield = """            // 3. Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { },
                readOnly = true,
                label = { Text("Nominal Transaksi (Rp)", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                prefix = { Text("Rp ", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_transaction_amount_input")
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            showNumpadBottomSheet = true
                            focusManager.clearFocus() // Prevent soft keyboard from showing
                        }
                    },"""

content = content.replace(old_textfield, new_textfield)

# Add the BottomSheet at the end of the Scaffold
numpad_sheet = """
    }

    if (showNumpadBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNumpadBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rp $amountText",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                AmanahNumpad(
                    onNumberClick = { num ->
                        if (amountText == "0") {
                            amountText = num.toString()
                        } else {
                            // Max 15 digits
                            if (amountText.length < 15) {
                                amountText += num.toString()
                            }
                        }
                        errorMessage = null
                    },
                    onBackspaceClick = {
                        if (amountText.isNotEmpty()) {
                            amountText = amountText.dropLast(1)
                            if (amountText.isEmpty()) amountText = "0"
                        }
                    },
                    onOperatorClick = { op ->
                        // Simplified implementation: ignore operations or implement basic arithmetic in string evaluation
                    },
                    onDoneClick = { showNumpadBottomSheet = false }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
"""
content = re.sub(r'    \}\n\}\n$', numpad_sheet, content)

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


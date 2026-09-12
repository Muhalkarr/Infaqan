import re

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

numpad_sheet = """
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
                        if (amountText == "0" || amountText.isEmpty()) {
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
                        // Operation functionality can be implemented if needed
                    },
                    onDoneClick = { showNumpadBottomSheet = false }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showLiveCameraScanner) {
"""

content = content.replace("    if (showLiveCameraScanner) {", numpad_sheet)

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


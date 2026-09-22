import re

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

# We need to add an expression string state to the BottomSheet scope, 
# evaluate it when Done is clicked or Operator is clicked.

calc_logic = """
    if (showNumpadBottomSheet) {
        // Local state for the calculator
        var expression by remember { mutableStateOf(amountText.ifEmpty { "0" }) }
        
        fun evaluateMath(expr: String): String {
            try {
                // Remove spaces and evaluate basic left-to-right math
                var cleanExpr = expr.replace(" ", "").replace("Rp", "")
                if (cleanExpr.isEmpty()) return "0"
                
                // Very basic sequential evaluator
                val tokens = mutableListOf<String>()
                var currentNum = ""
                for (char in cleanExpr) {
                    if (char == '+' || char == '-' || char == '×' || char == '÷') {
                        if (currentNum.isNotEmpty()) tokens.add(currentNum)
                        tokens.add(char.toString())
                        currentNum = ""
                    } else {
                        currentNum += char
                    }
                }
                if (currentNum.isNotEmpty()) tokens.add(currentNum)
                
                if (tokens.isEmpty()) return "0"
                
                var result = tokens[0].toDoubleOrNull() ?: 0.0
                var i = 1
                while (i < tokens.size - 1) {
                    val op = tokens[i]
                    val nextNum = tokens[i+1].toDoubleOrNull() ?: 0.0
                    when (op) {
                        "+" -> result += nextNum
                        "-" -> result -= nextNum
                        "×" -> result *= nextNum
                        "÷" -> if (nextNum != 0.0) result /= nextNum
                    }
                    i += 2
                }
                // Return as long string to avoid scientific notation
                val longRes = result.toLong()
                return if (longRes < 0) "0" else longRes.toString()
            } catch (e: Exception) {
                return "0"
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showNumpadBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = expression,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                AmanahNumpad(
                    onNumberClick = { num ->
                        if (expression == "0") {
                            expression = num.toString()
                        } else {
                            if (expression.length < 25) {
                                expression += num.toString()
                            }
                        }
                    },
                    onBackspaceClick = {
                        if (expression.isNotEmpty()) {
                            expression = expression.dropLast(1)
                            if (expression.isEmpty()) expression = "0"
                        }
                    },
                    onOperatorClick = { op ->
                        val lastChar = expression.lastOrNull()
                        if (lastChar != null && lastChar.isDigit()) {
                            expression += op
                        } else if (lastChar != null && lastChar in listOf('+', '-', '×', '÷')) {
                            // Replace last operator
                            expression = expression.dropLast(1) + op
                        }
                    },
                    onDoneClick = { 
                        val finalResult = evaluateMath(expression)
                        amountText = finalResult
                        errorMessage = null
                        showNumpadBottomSheet = false 
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
"""

old_sheet_pattern = r'    if \(showNumpadBottomSheet\) \{[\s\S]*?Spacer\(modifier = Modifier\.height\(32\.dp\)\)\n            \}\n        \}\n    \}'

content = re.sub(old_sheet_pattern, calc_logic.strip(), content)

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


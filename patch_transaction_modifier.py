import re

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

# Replace the duplicate modifier
old_modifier = """                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input")
            )"""

new_modifier = """                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )"""

content = content.replace(old_modifier, new_modifier)

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


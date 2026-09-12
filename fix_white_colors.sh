#!/bin/bash
# Replaces hardcoded whites with onPrimary in buttons and badges, or onSurface/onBackground elsewhere.
# Let's inspect AddTransactionScreen where a lot of whites are.
grep -n "color = Color.White" app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt

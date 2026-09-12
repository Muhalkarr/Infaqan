#!/bin/bash
sed -i 's/Color.Red/MaterialTheme.colorScheme.error/g' app/src/main/java/com/example/presentation/screens/SettingsScreen.kt
sed -i 's/Color(0xFFEF5350)/MaterialTheme.colorScheme.error/g' app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt
sed -i 's/Color(0xFFEF5350)/MaterialTheme.colorScheme.error/g' app/src/main/java/com/example/presentation/screens/SedekahSubuhScreen.kt
sed -i 's/Color(0xFFE11D48)/MaterialTheme.colorScheme.error/g' app/src/main/java/com/example/presentation/screens/ExportReportScreen.kt
sed -i 's/Color(0xFFFFB74D)/MaterialTheme.colorScheme.secondary/g' app/src/main/java/com/example/presentation/screens/MultiWalletScreen.kt
sed -i 's/Color(0xFF7C3AED)/MaterialTheme.colorScheme.secondary/g' app/src/main/java/com/example/presentation/screens/DebugTerminalScreen.kt
sed -i 's/Color.Gray.copy(alpha = 0.15f)/MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/Color.Black.copy(alpha = 0.25f)/MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)/g' app/src/main/java/com/example/presentation/components/AppNavigationDrawer.kt

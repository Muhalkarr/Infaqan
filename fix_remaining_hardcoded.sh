#!/bin/bash
sed -i 's/Color(0xFFFF7043)/MaterialTheme.colorScheme.error/g' app/src/main/java/com/example/presentation/screens/settings/BudgetSecuritySettingsSection.kt
sed -i 's/tint = Color.White/tint = MaterialTheme.colorScheme.onPrimary/g' app/src/main/java/com/example/presentation/screens/settings/ZakatInfaqSettingsSection.kt
sed -i 's/tint = Color.White/tint = MaterialTheme.colorScheme.onPrimary/g' app/src/main/java/com/example/presentation/screens/MonthlyReportScreen.kt
sed -i 's/color = Color.Black/color = MaterialTheme.colorScheme.onSurface/g' app/src/main/java/com/example/presentation/screens/RecurringTransactionsScreen.kt
sed -i 's/tint = Color.White/tint = MaterialTheme.colorScheme.onPrimary/g' app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt
sed -i 's/tint = Color.White/tint = MaterialTheme.colorScheme.onPrimary/g' app/src/main/java/com/example/presentation/screens/SedekahSubuhScreen.kt
sed -i 's/tint = Color.White.copy/tint = MaterialTheme.colorScheme.onPrimary.copy/g' app/src/main/java/com/example/presentation/components/AppNavigationDrawer.kt

# Chart colors - use M3 theme colors instead of hardcoded hex
sed -i 's/Color(0xFF00B4D8)/MaterialTheme.colorScheme.tertiary/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/Color(0xFFAB47BC)/MaterialTheme.colorScheme.secondaryContainer/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt

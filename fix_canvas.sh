#!/bin/bash
sed -i '347i \                val outlineColor = MaterialTheme.colorScheme.outline' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i '361s/MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)/outlineColor.copy(alpha = 0.15f)/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt

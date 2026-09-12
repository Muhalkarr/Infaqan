#!/bin/bash
sed -i 's/val outlineColor = MaterialTheme.colorScheme.outline//g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/val tertiaryColor = MaterialTheme.colorScheme.tertiary//g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer//g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt

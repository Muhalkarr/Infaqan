#!/bin/bash
sed -i '156i \    val tertiaryColor = MaterialTheme.colorScheme.tertiary\n    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/color = MaterialTheme.colorScheme.tertiary,/color = tertiaryColor,/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/color = MaterialTheme.colorScheme.secondaryContainer,/color = secondaryContainerColor,/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/MaterialTheme.colorScheme.tertiary to 108f/tertiaryColor to 108f/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i 's/MaterialTheme.colorScheme.secondaryContainer to 36f/secondaryContainerColor to 36f/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i '360s/MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)/outlineColor.copy(alpha = 0.15f)/g' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt
sed -i '156i \    val outlineColor = MaterialTheme.colorScheme.outline' app/src/main/java/com/example/presentation/components/BudgetCategoryAllocationChart.kt

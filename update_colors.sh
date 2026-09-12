#!/bin/bash
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/EmeraldPrimary/MaterialTheme.colorScheme.primary/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/EmeraldLight/MaterialTheme.colorScheme.primary/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/GoldAccent/MaterialTheme.colorScheme.secondary/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/ExpenseCoral/MaterialTheme.colorScheme.error/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/DarkSurfaceVariant/MaterialTheme.colorScheme.surfaceVariant/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/DarkSurface/MaterialTheme.colorScheme.surface/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/DarkBorder/MaterialTheme.colorScheme.outline/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/DarkBackground/MaterialTheme.colorScheme.background/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.EmeraldPrimary/d' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.EmeraldLight/d' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.GoldAccent/d' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.ExpenseCoral/d' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.DarkSurfaceVariant/d' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.DarkSurface/d' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.DarkBorder/d' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i '/import com.example.ui.theme.DarkBackground/d' {} +

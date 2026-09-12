#!/bin/bash
sed -i 's/color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant/color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant/g' app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt

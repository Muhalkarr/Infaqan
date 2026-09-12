#!/bin/bash
sed -i 's/import androidx.compose.ui.graphics.Color/import androidx.compose.ui.graphics.Color\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.ui.graphics.toArgb/g' app/src/main/java/com/example/presentation/components/SankeyFlowDiagram.kt
sed -i 's/color = android.graphics.Color.WHITE/color = MaterialTheme.colorScheme.onSurface.toArgb()/g' app/src/main/java/com/example/presentation/components/SankeyFlowDiagram.kt
sed -i 's/color = android.graphics.Color.argb(180, 200, 220, 220)/color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()/g' app/src/main/java/com/example/presentation/components/SankeyFlowDiagram.kt

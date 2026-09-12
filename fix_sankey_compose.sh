#!/bin/bash
sed -i '40i \    val onSurfaceColor = MaterialTheme.colorScheme.onSurface\n    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant' app/src/main/java/com/example/presentation/components/SankeyFlowDiagram.kt
sed -i 's/color = MaterialTheme.colorScheme.onSurface.toArgb()/color = onSurfaceColor.toArgb()/g' app/src/main/java/com/example/presentation/components/SankeyFlowDiagram.kt
sed -i 's/color = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()/color = onSurfaceVariantColor.toArgb()/g' app/src/main/java/com/example/presentation/components/SankeyFlowDiagram.kt

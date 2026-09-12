#!/bin/bash
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/Color(0xFF10B981)/MaterialTheme.colorScheme.primary/g' {} +

#!/bin/bash
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/color = Color.White/color = MaterialTheme.colorScheme.onSurface/g' {} +
find app/src/main/java/com/example/presentation -name "*.kt" -type f -exec sed -i 's/color = Color.White.copy([^)]*)//g' {} +

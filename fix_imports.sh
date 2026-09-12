#!/bin/bash
for file in $(find app/src/main/java/com/example/presentation -name "*.kt" -type f); do
    if grep -q "MaterialTheme" "$file"; then
        if ! grep -q "import androidx.compose.material3.MaterialTheme" "$file"; then
            sed -i '1s/^/import androidx.compose.material3.MaterialTheme\n/' "$file"
        fi
    fi
done

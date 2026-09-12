#!/bin/bash
for file in $(find app/src/main/java/com/example/presentation -name "*.kt" -type f); do
    if grep -q "MaterialTheme" "$file"; then
        if ! grep -q "import androidx.compose.material3.MaterialTheme" "$file"; then
            # Insert after the first line (package declaration)
            sed -i '1 a import androidx.compose.material3.MaterialTheme' "$file"
        fi
    fi
done

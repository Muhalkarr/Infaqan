#!/bin/bash
for file in $(find app/src/main/java/com/example/presentation -name "*.kt" -type f); do
    # Check if file has MaterialTheme imported before package
    if head -n 1 "$file" | grep -q "^import androidx.compose.material3.MaterialTheme$"; then
        # Delete first line
        sed -i '1d' "$file"
        # Insert it after the new first line
        sed -i '1 a import androidx.compose.material3.MaterialTheme' "$file"
    fi
done

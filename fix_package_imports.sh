#!/bin/bash
for file in $(find app/src/main/java/com/example/presentation -name "*.kt" -type f); do
    if head -n 1 "$file" | grep -q "^import"; then
        # The file starts with an import, which means we messed up the package line
        # Actually, let's just delete the line "import androidx.compose.material3.MaterialTheme" from the very top
        sed -i '1{/^import androidx.compose.material3.MaterialTheme$/d}' "$file"
        
        # Now insert it safely on line 3 (after package declaration)
        sed -i '2 a import androidx.compose.material3.MaterialTheme' "$file"
    fi
done

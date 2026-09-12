#!/bin/bash
# 1. Update AppUserPreferences
sed -i '/val uiScaleFactor: Float = 1.0f/a \    , val completedGuides: Set<String> = emptySet()' app/src/main/java/com/example/core/datastore/DataStoreManager.kt

# 2. Add stringSetPreferencesKey import
sed -i 's/import androidx.datastore.preferences.core.stringPreferencesKey/import androidx.datastore.preferences.core.stringPreferencesKey\nimport androidx.datastore.preferences.core.stringSetPreferencesKey/g' app/src/main/java/com/example/core/datastore/DataStoreManager.kt

# 3. Add to PreferencesKeys
sed -i '/val UI_SCALE_FACTOR = floatPreferencesKey("ui_scale_factor")/a \        val COMPLETED_GUIDES = stringSetPreferencesKey("completed_guides")' app/src/main/java/com/example/core/datastore/DataStoreManager.kt

# 4. Map it in userPreferencesFlow
sed -i '/val scaleFactor = preferences\[PreferencesKeys.UI_SCALE_FACTOR\] ?: 1.0f/a \            val completedGuides = preferences\[PreferencesKeys.COMPLETED_GUIDES\] ?: emptySet()' app/src/main/java/com/example/core/datastore/DataStoreManager.kt

# 5. Add to AppUserPreferences initialization
sed -i 's/uiScaleFactor = scaleFactor/uiScaleFactor = scaleFactor,\n                completedGuides = completedGuides/g' app/src/main/java/com/example/core/datastore/DataStoreManager.kt


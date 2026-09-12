#!/bin/bash
cat << 'INNER_EOF' >> app/src/main/java/com/example/core/datastore/DataStoreManager.kt

    suspend fun markGuideCompleted(guideTitle: String) {
        context.appDataStore.edit { preferences ->
            val currentGuides = preferences[PreferencesKeys.COMPLETED_GUIDES] ?: emptySet()
            preferences[PreferencesKeys.COMPLETED_GUIDES] = currentGuides + guideTitle
        }
    }
}
INNER_EOF
# We need to remove the last '}' before appending.
sed -i '$d' app/src/main/java/com/example/core/datastore/DataStoreManager.kt
cat add_func_datastore.sh

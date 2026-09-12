#!/bin/bash
# Remove the extra '}' added previously
sed -i '/suspend fun markGuideCompleted/i \' app/src/main/java/com/example/core/datastore/DataStoreManager.kt
sed -i 's/^}$//g' app/src/main/java/com/example/core/datastore/DataStoreManager.kt
echo "}" >> app/src/main/java/com/example/core/datastore/DataStoreManager.kt

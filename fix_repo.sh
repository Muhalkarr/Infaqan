#!/bin/bash
# Remove the messed up lines
sed -i '344,347d' app/src/main/java/com/example/core/repository/AmanahRepository.kt
# Add them correctly
sed -i '/suspend fun clearAllInfaqDistributions()/a \    suspend fun markGuideCompleted(guideTitle: String)' app/src/main/java/com/example/core/repository/AmanahRepository.kt
sed -i '/override suspend fun clearAllPreferences() = withContext(ioDispatcher) {/i \    override suspend fun markGuideCompleted(guideTitle: String) = withContext(ioDispatcher) {\n        dataStoreManager.markGuideCompleted(guideTitle)\n    }\n' app/src/main/java/com/example/core/repository/AmanahRepository.kt

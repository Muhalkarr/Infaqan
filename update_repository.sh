#!/bin/bash
sed -i '/suspend fun clearAllPreferences()/a \    suspend fun markGuideCompleted(guideTitle: String)' app/src/main/java/com/example/core/repository/AmanahRepository.kt
sed -i '/suspend fun clearAllPreferences() = withContext(ioDispatcher) {/a \    override suspend fun markGuideCompleted(guideTitle: String) = withContext(ioDispatcher) {\n        dataStoreManager.markGuideCompleted(guideTitle)\n    }' app/src/main/java/com/example/core/repository/AmanahRepository.kt

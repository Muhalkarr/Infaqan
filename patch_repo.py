import re

with open('app/src/main/java/com/example/core/repository/AmanahRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("suspend fun markGuideCompleted(guideTitle: String)", "suspend fun markGuideCompleted(guideTitle: String)\n    suspend fun setOnboardingCompleted()")
content = content.replace("override suspend fun markGuideCompleted(guideTitle: String) = withContext(ioDispatcher) {\n        dataStoreManager.markGuideCompleted(guideTitle)\n    }", "override suspend fun markGuideCompleted(guideTitle: String) = withContext(ioDispatcher) {\n        dataStoreManager.markGuideCompleted(guideTitle)\n    }\n\n    override suspend fun setOnboardingCompleted() = withContext(ioDispatcher) {\n        dataStoreManager.setOnboardingCompleted()\n    }")

with open('app/src/main/java/com/example/core/repository/AmanahRepository.kt', 'w') as f:
    f.write(content)

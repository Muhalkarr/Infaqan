import re

with open('app/src/main/java/com/example/core/repository/AmanahRepository.kt', 'r') as f:
    content = f.read()

bad_code = """
    override suspend fun markGuideCompleted(guideTitle: String)
    suspend fun setOnboardingCompleted() = withContext(ioDispatcher) {
        dataStoreManager.markGuideCompleted(guideTitle)
    }
"""

good_code = """
    override suspend fun markGuideCompleted(guideTitle: String) = withContext(ioDispatcher) {
        dataStoreManager.markGuideCompleted(guideTitle)
    }

    override suspend fun setOnboardingCompleted() = withContext(ioDispatcher) {
        dataStoreManager.setOnboardingCompleted()
    }
"""

content = content.replace(bad_code.strip(), good_code.strip())

with open('app/src/main/java/com/example/core/repository/AmanahRepository.kt', 'w') as f:
    f.write(content)

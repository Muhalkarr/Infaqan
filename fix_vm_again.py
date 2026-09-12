import re

with open('app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt', 'r') as f:
    content = f.read()

add_func = """
    fun completeOnboarding() {
        viewModelScope.launch {
            repository?.setOnboardingCompleted()
        }
    }
"""
content = content.replace("fun markGuideTopicCompleted(title: String) {", add_func + "\n    fun markGuideTopicCompleted(title: String) {")

with open('app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt', 'w') as f:
    f.write(content)

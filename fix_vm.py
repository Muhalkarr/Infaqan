import re

with open('app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt', 'r') as f:
    content = f.read()

if "fun completeOnboarding" not in content:
    add_func = """
    fun completeOnboarding() {
        viewModelScope.launch {
            repository.setOnboardingCompleted()
        }
    }
"""
    content = content.replace("fun markGuideCompleted(guideTitle: String) {", add_func + "\n    fun markGuideCompleted(guideTitle: String) {")

with open('app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt', 'w') as f:
    f.write(content)

import re

with open('app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("val completedGuides: Set<String> = emptySet(),\n    val isLoading: Boolean = false", "val completedGuides: Set<String> = emptySet(),\n    val hasCompletedOnboarding: Boolean = false,\n    val isLoading: Boolean = false")

content = content.replace("completedGuides = prefs.completedGuides", "completedGuides = prefs.completedGuides,\n                hasCompletedOnboarding = prefs.hasCompletedOnboarding")

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

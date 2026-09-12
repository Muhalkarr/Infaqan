#!/bin/bash
sed -i '/val uiScaleFactor: Float = 1.0f/a \    , val completedGuides: Set<String> = emptySet()' app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt
sed -i '/isDarkMode = prefs.isDarkMode,/a \                            completedGuides = prefs.completedGuides,' app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt
sed -i '/fun deactivateGuideMode() {/a \    fun markGuideTopicCompleted(title: String) {\n        viewModelScope.launch {\n            repository.markGuideCompleted(title)\n        }\n    }' app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt

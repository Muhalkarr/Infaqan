#!/bin/bash
sed -i '390,397d' app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt
sed -i '/fun activateGuideMode(topicIndex/i \    fun deactivateGuideMode() {\n        isGuideModeActive = false\n    }\n\n    fun markGuideTopicCompleted(title: String) {\n        viewModelScope.launch {\n            repository?.markGuideCompleted(title)\n        }\n    }\n' app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt

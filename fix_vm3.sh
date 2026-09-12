#!/bin/bash
sed -i 's/val uiScaleFactor: Float = 1.0f,,/val uiScaleFactor: Float = 1.0f,/g' app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt
sed -i 's/val completedGuides: Set<String> = emptySet()/val completedGuides: Set<String> = emptySet(),/g' app/src/main/java/com/example/core/state/AmanahLedgerViewModel.kt

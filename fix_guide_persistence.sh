#!/bin/bash
sed -i 's/val completedTopics = remember { mutableSetOf(0) }/val completedTopics = state.completedGuides/g' app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt
sed -i 's/completedTopics.add(currentTopicIndex)/viewModel.markGuideTopicCompleted(topics[currentTopicIndex].id)/g' app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt

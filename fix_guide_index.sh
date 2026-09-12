#!/bin/bash
sed -i 's/val isDone = completedTopics.contains(idx)/val isDone = completedTopics.contains(topic.id)/g' app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt
sed -i '/completedTopics.add(idx)/d' app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt

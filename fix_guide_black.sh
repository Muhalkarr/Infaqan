#!/bin/bash
sed -i 's/tint = Color.Black/tint = MaterialTheme.colorScheme.onSecondary/g' app/src/main/java/com/example/presentation/screens/GuideAdditionalTopics.kt
sed -i 's/color = Color.Black/color = MaterialTheme.colorScheme.onSecondary/g' app/src/main/java/com/example/presentation/screens/GuideAdditionalTopics.kt

sed -i 's/tint = Color.Black/tint = MaterialTheme.colorScheme.onSecondary/g' app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt
sed -i 's/color = Color.Black/color = MaterialTheme.colorScheme.onSecondary/g' app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt

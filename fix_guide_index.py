import re

with open('app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("val isDone = completedTopics.contains(topic.id)", "val isDone = state.completedGuides.contains(topic.id)")

with open('app/src/main/java/com/example/presentation/screens/InteractiveGuideScreen.kt', 'w') as f:
    f.write(content)

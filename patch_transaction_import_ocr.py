import re

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'import com\.example\.core\.ocr.*?\n', '', content)

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


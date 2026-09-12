import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

# Comment out camera and mlkit dependencies
content = re.sub(r'(\s*implementation\(libs\.androidx\.camera\.camera2\))', r'//\1', content)
content = re.sub(r'(\s*implementation\(libs\.androidx\.camera\.core\))', r'//\1', content)
content = re.sub(r'(\s*implementation\(libs\.androidx\.camera\.lifecycle\))', r'//\1', content)
content = re.sub(r'(\s*implementation\(libs\.androidx\.camera\.view\))', r'//\1', content)
content = re.sub(r'(\s*implementation\(libs\.mlkit\.text\.recognition\))', r'//\1', content)
content = re.sub(r'(\s*implementation\(libs\.firebase\.ai\))', r'//\1', content)
# We might not be using Moshi or okhttp/retrofit either if there are no other APIs
# content = re.sub(r'(\s*implementation\(libs\.retrofit\))', r'//\1', content)
# content = re.sub(r'(\s*implementation\(libs\.okhttp\))', r'//\1', content)
# content = re.sub(r'(\s*implementation\(libs\.logging\.interceptor\))', r'//\1', content)
# content = re.sub(r'(\s*implementation\(libs\.moshi\.kotlin\))', r'//\1', content)
# content = re.sub(r'(\s*implementation\(libs\.converter\.moshi\))', r'//\1', content)
# content = re.sub(r'(\s*"ksp"\(libs\.moshi\.kotlin\.codegen\))', r'//\1', content)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)


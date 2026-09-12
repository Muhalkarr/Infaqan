import re

with open('app/src/main/java/com/example/presentation/components/AppNavigationDrawer.kt', 'r') as f:
    content = f.read()

# Replace AMANAH LEDGER text
content = content.replace('''Text(
                                    text = "AMANAH LEDGER",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    
                                    letterSpacing = 0.5.sp
                                )''', '''Text(
                                    text = "AMANAH LEDGER",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )''')

# Replace Sistem Akuntansi Syariah
content = content.replace('''Text(
                                    text = "Sistem Akuntansi Syariah",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )''', '''Text(
                                    text = "Sistem Akuntansi Syariah",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )''')

# Replace userNameKasMukmin
content = content.replace('''Text(
                                    text = uiState.userNameKasMukmin,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )''', '''Text(
                                    text = uiState.userNameKasMukmin,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )''')

# Replace hijriDate
content = content.replace('''Text(
                                    text = hijriDate.toString(),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )''', '''Text(
                                    text = hijriDate.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )''')

with open('app/src/main/java/com/example/presentation/components/AppNavigationDrawer.kt', 'w') as f:
    f.write(content)


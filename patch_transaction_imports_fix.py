with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusChanged
import com.example.presentation.components.AmanahNumpad
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
"""

if "import androidx.compose.ui.platform.LocalFocusManager" not in content:
    content = content.replace("package com.example.presentation.screens", "package com.example.presentation.screens\n" + imports)

if "@OptIn(ExperimentalMaterial3Api::class)" not in content:
    content = content.replace("fun AddTransactionScreen(", "@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun AddTransactionScreen(")

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


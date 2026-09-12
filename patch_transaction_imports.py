with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'r') as f:
    content = f.read()

imports = """
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusChanged
import com.example.presentation.components.AmanahNumpad
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
"""

content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\n" + imports)

# We also need to add @OptIn(ExperimentalMaterial3Api::class) to the function if not present
if "@OptIn(ExperimentalMaterial3Api::class)" not in content:
    content = content.replace("fun AddTransactionScreen(", "@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun AddTransactionScreen(")

# Let's fix the @Composable invocations error:
# e: file:///app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt:629:27 @Composable invocations can only happen from the context of a @Composable function
# Wait, why was there an error at line 629?

with open('app/src/main/java/com/example/presentation/screens/AddTransactionScreen.kt', 'w') as f:
    f.write(content)


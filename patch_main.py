import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Make sure to import the new screen
if "import com.example.presentation.screens.OnboardingScreen" not in content:
    content = content.replace("import com.example.presentation.screens.AppLockScreen", "import com.example.presentation.screens.AppLockScreen\nimport com.example.presentation.screens.OnboardingScreen")

# Apply the overlay
overlay_code = """
                    AmanahMainApp(viewModel = viewModel)

                    if (!state.hasCompletedOnboarding) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onComplete = { viewModel.completeOnboarding() }
                        )
                    }

                    if (state.securityConfig.isPinEnabled && state.securityConfig.isAppLocked) {
                        AppLockScreen(viewModel = viewModel)
                    }
"""

content = re.sub(
    r'AmanahMainApp\(viewModel = viewModel\)\s*if \(state\.securityConfig\.isPinEnabled && state\.securityConfig\.isAppLocked\) \{\s*AppLockScreen\(viewModel = viewModel\)\s*\}',
    overlay_code.strip(),
    content
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

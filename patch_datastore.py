import re

with open('app/src/main/java/com/example/core/datastore/DataStoreManager.kt', 'r') as f:
    content = f.read()

content = content.replace("val completedGuides: Set<String> = emptySet()", "val completedGuides: Set<String> = emptySet(),\n    val hasCompletedOnboarding: Boolean = false")
content = content.replace("val COMPLETED_GUIDES = stringSetPreferencesKey(\"completed_guides\")", "val COMPLETED_GUIDES = stringSetPreferencesKey(\"completed_guides\")\n        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey(\"has_completed_onboarding\")")
content = content.replace("val completedGuides = preferences[PreferencesKeys.COMPLETED_GUIDES] ?: emptySet()", "val completedGuides = preferences[PreferencesKeys.COMPLETED_GUIDES] ?: emptySet()\n            val hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false")
content = content.replace("completedGuides = completedGuides", "completedGuides = completedGuides,\n                hasCompletedOnboarding = hasCompletedOnboarding")

add_func = """
    suspend fun setOnboardingCompleted() {
        context.appDataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = true
        }
    }
"""
content = content.replace("suspend fun markGuideCompleted", add_func + "\n    suspend fun markGuideCompleted")

with open('app/src/main/java/com/example/core/datastore/DataStoreManager.kt', 'w') as f:
    f.write(content)

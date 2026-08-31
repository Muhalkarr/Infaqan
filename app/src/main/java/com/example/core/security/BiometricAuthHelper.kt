package com.example.core.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthHelper {
    private const val TAG = "BiometricAuthHelper"

    enum class BiometricAvailability {
        AVAILABLE,
        NOT_ENROLLED,
        NO_HARDWARE,
        UNAVAILABLE
    }

    fun checkBiometricAvailability(context: Context): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            else -> BiometricAvailability.UNAVAILABLE
        }
    }

    fun promptBiometricUnlock(
        activity: FragmentActivity,
        title: String = "Autentikasi Biometrik Amanah",
        subtitle: String = "Gunakan sidik jari atau wajah untuk membuka Buku Kas",
        negativeButtonText: String = "Gunakan PIN",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        try {
            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Biometric authentication successful")
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.w(TAG, "Biometric error $errorCode: $errString")
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onCancel()
                    } else {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Biometric recognition failed")
                    onError("Sidik jari tidak dikenali. Silakan coba lagi.")
                }
            }

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to launch BiometricPrompt: ${e.message}")
            onError(e.localizedMessage ?: "Biometrik tidak dapat diakses.")
        }
    }
}

package com.example.core.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class AuthStatus {
    UNAUTHENTICATED,
    AUTHENTICATING,
    AUTHENTICATED,
    GUEST_MODE,
    ERROR
}

data class AmanahUserProfile(
    val uid: String = "",
    val displayName: String = "Hamba Allah",
    val email: String = "",
    val photoUrl: String? = null,
    val isAnonymous: Boolean = true,
    val isCloudConnected: Boolean = false
)

data class AmanahAuthState(
    val status: AuthStatus = AuthStatus.UNAUTHENTICATED,
    val user: AmanahUserProfile = AmanahUserProfile(),
    val errorMessage: String? = null,
    val isSuccessFeedback: Boolean = false
)

class AmanahAuthManager(private val context: Context) {
    private val TAG = "AmanahAuthManager"

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseSafeInitializer.init(context)
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth initialization fallback: ${e.message}")
            null
        }
    }

    private val credentialManager by lazy {
        try {
            CredentialManager.create(context)
        } catch (e: Throwable) {
            Log.w(TAG, "CredentialManager init fallback: ${e.message}")
            null
        }
    }

    private val _authState = MutableStateFlow(AmanahAuthState())
    val authState: StateFlow<AmanahAuthState> = _authState.asStateFlow()

    init {
        FirebaseSafeInitializer.init(context)
        checkCurrentAuthSession()
    }

    private fun checkCurrentAuthSession() {
        try {
            val auth = firebaseAuth
            val currentUser = auth?.currentUser
            if (currentUser != null) {
                _authState.value = AmanahAuthState(
                    status = AuthStatus.AUTHENTICATED,
                    user = mapFirebaseUser(currentUser)
                )
            } else {
                _authState.value = AmanahAuthState(
                    status = AuthStatus.GUEST_MODE,
                    user = AmanahUserProfile(
                        uid = "guest_mukmin_local",
                        displayName = "Mukmin (Mode Offline)",
                        email = "offline@amanah.local",
                        isAnonymous = true,
                        isCloudConnected = false
                    )
                )
            }
        } catch (e: Throwable) {
            Log.i(TAG, "Offline guest mode active: ${e.message}")
            _authState.value = AmanahAuthState(
                status = AuthStatus.GUEST_MODE,
                user = AmanahUserProfile(
                    uid = "guest_mukmin_local",
                    displayName = "Mukmin (Mode Offline)",
                    isAnonymous = true,
                    isCloudConnected = false
                )
            )
        }
    }

    private fun mapFirebaseUser(user: FirebaseUser): AmanahUserProfile {
        return AmanahUserProfile(
            uid = user.uid,
            displayName = user.displayName ?: if (!user.email.isNullOrBlank()) user.email!!.substringBefore("@") else "Hamba Allah",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString(),
            isAnonymous = user.isAnonymous,
            isCloudConnected = true
        )
    }

    suspend fun signInWithEmailPassword(email: String, pass: String): Result<AmanahUserProfile> = withContext(Dispatchers.IO) {
        if (email.isBlank() || pass.length < 6) {
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = "Format email atau kata sandi tidak valid (min. 6 karakter)."
            )
            return@withContext Result.failure(Exception("Email/password tidak valid"))
        }

        val auth = firebaseAuth
        if (auth == null) {
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = "Layanan Cloud Firebase tidak aktif (berjalan pada database lokal)."
            )
            return@withContext Result.failure(Exception("FirebaseAuth tidak tersedia"))
        }

        _authState.value = _authState.value.copy(
            status = AuthStatus.AUTHENTICATING,
            errorMessage = null
        )

        try {
            val authResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw Exception("User tidak ditemukan")
            val profile = mapFirebaseUser(user)

            _authState.value = AmanahAuthState(
                status = AuthStatus.AUTHENTICATED,
                user = profile,
                isSuccessFeedback = true
            )
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Login email failed: ${e.message}", e)
            val friendlyMsg = when {
                e.message?.contains("password", ignoreCase = true) == true -> "Kata sandi salah. Silakan coba lagi."
                e.message?.contains("no user", ignoreCase = true) == true -> "Akun tidak ditemukan. Silakan daftar akun baru."
                else -> e.localizedMessage ?: "Gagal masuk ke akun Amanah"
            }
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = friendlyMsg
            )
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmailPassword(email: String, pass: String, name: String): Result<AmanahUserProfile> = withContext(Dispatchers.IO) {
        if (email.isBlank() || pass.length < 6) {
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = "Kata sandi minimal 6 karakter."
            )
            return@withContext Result.failure(Exception("Format input tidak valid"))
        }

        val auth = firebaseAuth
        if (auth == null) {
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = "Layanan Cloud Firebase tidak aktif (berjalan pada database lokal)."
            )
            return@withContext Result.failure(Exception("FirebaseAuth tidak tersedia"))
        }

        _authState.value = _authState.value.copy(
            status = AuthStatus.AUTHENTICATING,
            errorMessage = null
        )

        try {
            val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: throw Exception("Pendaftaran akun gagal")
            
            // Update profile display name
            val profileUpdate = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name.ifBlank { "Mukmin Amanah" })
                .build()
            user.updateProfile(profileUpdate).await()

            val profile = mapFirebaseUser(user)
            _authState.value = AmanahAuthState(
                status = AuthStatus.AUTHENTICATED,
                user = profile,
                isSuccessFeedback = true
            )
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed: ${e.message}", e)
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = e.localizedMessage ?: "Gagal mendaftar akun baru"
            )
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleCredential(webClientId: String = ""): Result<AmanahUserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        val credMan = credentialManager
        if (auth == null || credMan == null) {
            val fallbackMsg = "Layanan Google Auth tidak tersedia di lingkungan ini."
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = fallbackMsg
            )
            return@withContext Result.failure(Exception(fallbackMsg))
        }

        _authState.value = _authState.value.copy(
            status = AuthStatus.AUTHENTICATING,
            errorMessage = null
        )

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(if (webClientId.isNotBlank()) webClientId else "1028308803138-amanah.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credMan.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCred).await()
                val user = authResult.user ?: throw Exception("Google auth user null")

                val profile = mapFirebaseUser(user)
                _authState.value = AmanahAuthState(
                    status = AuthStatus.AUTHENTICATED,
                    user = profile,
                    isSuccessFeedback = true
                )
                Result.success(profile)
            } else {
                throw Exception("Jenis kredensial tidak didukung")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google Sign In via CredentialManager skipped/failed: ${e.message}")
            val fallbackMsg = e.localizedMessage ?: "Google Sign-In dibatalkan atau belum dikonfigurasi."
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = fallbackMsg
            )
            Result.failure(e)
        }
    }

    suspend fun continueAsGuest() {
        _authState.value = AmanahAuthState(
            status = AuthStatus.GUEST_MODE,
            user = AmanahUserProfile(
                uid = "guest_mukmin_local",
                displayName = "Kas Mukmin (Lokal)",
                email = "offline@amanah.syariah",
                isAnonymous = true,
                isCloudConnected = false
            ),
            errorMessage = null
        )
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            firebaseAuth?.signOut()
            credentialManager?.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}")
        }
        _authState.value = AmanahAuthState(
            status = AuthStatus.GUEST_MODE,
            user = AmanahUserProfile(
                uid = "guest_mukmin_local",
                displayName = "Kas Mukmin (Lokal)",
                isAnonymous = true,
                isCloudConnected = false
            ),
            errorMessage = null
        )
    }
}

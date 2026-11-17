package com.example.core.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
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
    val isAnonymous: Boolean = false,
    val isCloudConnected: Boolean = false
)

data class AmanahAuthState(
    val status: AuthStatus = AuthStatus.UNAUTHENTICATED,
    val user: AmanahUserProfile = AmanahUserProfile(),
    val errorMessage: String? = null,
    val isSuccessFeedback: Boolean = false
)

class AmanahAuthManager(private val context: Context) {
    private val appContext: Context = context.applicationContext

    companion object {
        private const val TAG = "AmanahAuthManager"
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseSafeInitializer.init(appContext)
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth initialization fallback: ${e.message}")
            null
        }
    }

    private val appCredentialManager: CredentialManager? by lazy {
        try {
            CredentialManager.create(appContext)
        } catch (e: Throwable) {
            Log.w(TAG, "CredentialManager init fallback: ${e.message}")
            null
        }
    }

    private val _authState = MutableStateFlow(AmanahAuthState())
    val authState: StateFlow<AmanahAuthState> = _authState.asStateFlow()

    init {
        FirebaseSafeInitializer.init(appContext)
        checkCurrentAuthSession()
    }

    private fun checkCurrentAuthSession() {
        try {
            val currentUser = firebaseAuth?.currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                _authState.value = AmanahAuthState(
                    status = AuthStatus.AUTHENTICATED,
                    user = mapFirebaseUser(currentUser)
                )
            } else {
                _authState.value = createGuestAuthState()
            }
        } catch (e: Throwable) {
            Log.i(TAG, "Offline guest mode active: ${e.message}")
            _authState.value = createGuestAuthState()
        }
    }

    private fun createGuestAuthState(errorMessage: String? = null): AmanahAuthState {
        return AmanahAuthState(
            status = AuthStatus.GUEST_MODE,
            user = AmanahUserProfile(
                uid = "guest_mukmin_local",
                displayName = "Kas Mukmin (Lokal)",
                email = "offline@amanah.syariah",
                isAnonymous = true,
                isCloudConnected = false
            ),
            errorMessage = errorMessage
        )
    }

    private fun mapFirebaseUser(user: FirebaseUser): AmanahUserProfile {
        return AmanahUserProfile(
            uid = user.uid,
            displayName = user.displayName ?: if (!user.email.isNullOrBlank()) user.email!!.substringBefore("@") else "Hamba Allah",
            email = user.email ?: "",
            photoUrl = user.photoUrl?.toString(),
            isAnonymous = user.isAnonymous,
            isCloudConnected = !user.isAnonymous
        )
    }

    private fun resolveServerClientId(explicitClientId: String = ""): String {
        if (explicitClientId.isNotBlank()) return explicitClientId
        val fromResources = try {
            appContext.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id resource lookup failed: ${e.message}")
            ""
        }
        if (fromResources.isBlank()) {
            throw IllegalStateException("Konfigurasi OAuth Google (default_web_client_id) tidak ditemukan.")
        }
        return fromResources
    }

    /**
     * Silent One-Tap / previously authorized Google Sign-In attempt at app startup.
     */
    suspend fun trySilentGoogleSignIn(activityContext: Context): Result<AmanahUserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext Result.failure(IllegalStateException("FirebaseAuth tidak tersedia"))
        if (auth.currentUser != null && !auth.currentUser!!.isAnonymous) {
            val profile = mapFirebaseUser(auth.currentUser!!)
            _authState.value = AmanahAuthState(status = AuthStatus.AUTHENTICATED, user = profile)
            return@withContext Result.success(profile)
        }

        try {
            val serverClientId = resolveServerClientId()
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .setServerClientId(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credMan = CredentialManager.create(activityContext)
            val result: GetCredentialResponse = credMan.getCredential(
                context = activityContext,
                request = request
            )
            exchangeGoogleCredential(auth, result)
        } catch (ce: CancellationException) {
            throw ce
        } catch (noCred: NoCredentialException) {
            Log.d(TAG, "No previously authorized Google credential for silent sign-in: ${noCred.message}")
            Result.failure(noCred)
        } catch (e: Exception) {
            Log.d(TAG, "Silent Google Sign-In skipped: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Explicit interactive Google Sign-In button flow using GetSignInWithGoogleOption and Activity context.
     */
    suspend fun signInWithGoogleCredential(
        activityContext: Context,
        webClientId: String = ""
    ): Result<AmanahUserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        if (auth == null) {
            val fallbackMsg = "Layanan Firebase Auth tidak tersedia di perangkat ini."
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = fallbackMsg
            )
            return@withContext Result.failure(IllegalStateException(fallbackMsg))
        }

        _authState.value = _authState.value.copy(
            status = AuthStatus.AUTHENTICATING,
            errorMessage = null
        )

        try {
            val resolvedClientId = resolveServerClientId(webClientId)
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(resolvedClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val credMan = CredentialManager.create(activityContext)
            val result: GetCredentialResponse = credMan.getCredential(
                context = activityContext,
                request = request
            )

            exchangeGoogleCredential(auth, result)
        } catch (ce: CancellationException) {
            throw ce
        } catch (c: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In cancelled by user: ${c.message}")
            _authState.value = createGuestAuthState(errorMessage = null)
            Result.failure(c)
        } catch (noCred: NoCredentialException) {
            Log.w(TAG, "No Google accounts/credentials found on device: ${noCred.message}")
            val msg = "Akun Google tidak ditemukan di perangkat ini. Pastikan Anda sudah masuk ke akun Google di setelan perangkat."
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = msg
            )
            Result.failure(noCred)
        } catch (e: Exception) {
            Log.w(TAG, "Google Sign-In via CredentialManager failed: ${e.message}", e)
            val fallbackMsg = e.localizedMessage ?: "Google Sign-In gagal atau dibatalkan."
            _authState.value = _authState.value.copy(
                status = AuthStatus.ERROR,
                errorMessage = fallbackMsg
            )
            Result.failure(e)
        }
    }

    private suspend fun exchangeGoogleCredential(
        auth: FirebaseAuth,
        result: GetCredentialResponse
    ): Result<AmanahUserProfile> {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCred).await()
            val user = authResult.user ?: throw IllegalStateException("Profil pengguna Firebase kosong setelah autentikasi Google.")

            val profile = mapFirebaseUser(user)
            _authState.value = AmanahAuthState(
                status = AuthStatus.AUTHENTICATED,
                user = profile,
                errorMessage = null,
                isSuccessFeedback = true
            )
            return Result.success(profile)
        } else {
            throw IllegalStateException("Jenis kredensial tidak didukung oleh Autentikasi Google.")
        }
    }

    suspend fun continueAsGuest() {
        _authState.value = createGuestAuthState()
    }

    suspend fun signOut() = withContext(Dispatchers.IO + NonCancellable) {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase signOut error: ${e.message}")
        }
        try {
            appCredentialManager?.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(TAG, "ClearCredentialState error: ${e.message}")
        }
        _authState.value = createGuestAuthState()
    }
}

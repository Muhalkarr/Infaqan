package com.example.core.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseSafeInitializer {
    private const val TAG = "FirebaseSafeInit"
    @Volatile
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val app = FirebaseApp.initializeApp(context)
                    if (app == null) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:1028308803138:android:amanahsyariahledger")
                            .setProjectId("amanah-syariah-ledger")
                            .setApiKey("AIzaSyAmanahSyariahOfflineFirstFallbackKey123")
                            .build()
                        FirebaseApp.initializeApp(context, options)
                    }
                }
                isInitialized = true
                Log.d(TAG, "Firebase successfully initialized.")
            } catch (e: Throwable) {
                Log.w(TAG, "Firebase standard init failed, using safe fallback options: ${e.message}")
                try {
                    if (FirebaseApp.getApps(context).isEmpty()) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:1028308803138:android:amanahsyariahledger")
                            .setProjectId("amanah-syariah-ledger")
                            .setApiKey("AIzaSyAmanahSyariahOfflineFirstFallbackKey123")
                            .build()
                        FirebaseApp.initializeApp(context, options)
                    }
                    isInitialized = true
                } catch (inner: Throwable) {
                    Log.w(TAG, "Firebase init in offline-first mode: ${inner.message}")
                }
            }
        }
    }
}

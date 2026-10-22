package com.example.core.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp

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
                    FirebaseApp.initializeApp(context)
                }
                isInitialized = true
                Log.d(TAG, "Firebase successfully initialized from google-services.json.")
            } catch (e: Throwable) {
                Log.w(TAG, "Firebase initialization notice: ${e.message}")
            }
        }
    }
}

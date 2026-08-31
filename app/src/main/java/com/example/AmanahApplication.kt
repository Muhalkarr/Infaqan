package com.example

import android.app.Application
import com.example.core.auth.FirebaseSafeInitializer

class AmanahApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseSafeInitializer.init(this)
    }
}

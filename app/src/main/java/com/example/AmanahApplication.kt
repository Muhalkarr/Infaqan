package com.example

import android.app.Application
import com.example.core.auth.FirebaseSafeInitializer
import com.example.core.debug.AppDebugLogger
import com.example.core.debug.GlobalCrashHandler
import com.example.core.worker.AmanahReminderWorker

class AmanahApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDebugLogger.init(this)
        GlobalCrashHandler.init(this)
        AppDebugLogger.i("AmanahApplication", "Aplikasi Amanah Ledger dimulai.")
        FirebaseSafeInitializer.init(this)
        AmanahReminderWorker.schedulePeriodicCheck(this)
    }
}

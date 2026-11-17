package com.example

import android.app.Application
import com.example.core.auth.FirebaseSafeInitializer
import com.example.core.debug.AppDebugLogger
import com.example.core.debug.GlobalCrashHandler
import com.example.core.worker.AmanahReminderWorker
import com.example.core.worker.InfaqVaultReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AmanahApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppDebugLogger.init(this)
        GlobalCrashHandler.init(this)
        AppDebugLogger.i("AmanahApplication", "Aplikasi Amanah Ledger dimulai.")
        FirebaseSafeInitializer.init(this)
        AmanahReminderWorker.schedulePeriodicCheck(this)
        appScope.launch {
            AmanahReminderWorker.syncFromSavedPreferences(this@AmanahApplication)
            InfaqVaultReminderWorker.syncFromSavedPreferences(this@AmanahApplication)
        }
    }
}

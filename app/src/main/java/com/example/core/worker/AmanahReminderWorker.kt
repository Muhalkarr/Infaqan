package com.example.core.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.core.database.AmanahDatabase
import com.example.core.debug.AppDebugLogger
import java.util.concurrent.TimeUnit

/**
 * AmanahReminderWorker
 * 
 * Pengelola Latar Belakang Hemat Baterai (WorkManager).
 * Dijalankan secara periodik dengan batasan ketat (RequiresBatteryNotLow & RequiresStorageNotLow)
 * untuk memeriksa jatuh tempo amanah Qardh (hutang/piutang) dan transaksi rutin
 * tanpa menguras baterai saat perangkat berada dalam mode tidur (Doze Mode).
 */
class AmanahReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            AppDebugLogger.i("AmanahReminderWorker", "Memulai pengecekan latar belakang hemat baterai...")
            val db = AmanahDatabase.getDatabase(appContext)

            // 1. Periksa Hutang-Piutang (Qardh) yang mendekati jatuh tempo (3 hari ke depan) atau lewat tempo
            val threeDaysAhead = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000)
            val pendingDebts = db.qardhDao().getPendingDueRecords(threeDaysAhead)

            if (pendingDebts.isNotEmpty()) {
                val count = pendingDebts.size
                showNotification(
                    notificationId = 1001,
                    title = "Pengingat Qardh (Hutang-Piutang)",
                    message = "Ada $count catatan amanah qardh yang mendekati atau telah melewati batas jatuh tempo."
                )
                AppDebugLogger.i("AmanahReminderWorker", "Notifikasi pengingat qardh berhasil dikirim ($count catatan).")
            }

            // 2. Periksa Transaksi Rutin yang aktif dalam 24 jam ke depan
            val next24Hours = System.currentTimeMillis() + (24L * 60 * 60 * 1000)
            val dueRecurring = db.recurringTransactionDao().getActiveDueTransactions(next24Hours)
            if (dueRecurring.isNotEmpty()) {
                AppDebugLogger.i("AmanahReminderWorker", "Ditemukan ${dueRecurring.size} transaksi rutin syariah mendekati jadwal.")
            }

            // 3. Periksa Kewajiban & Jatuh Tempo Haul Zakat Maal (Nisab 85g Emas)
            val entries = db.journalDao().getAllEntries()
            var totalWalletBalance = 0.0
            val assetPrefixes = setOf("acc_cash", "acc_bank", "acc_ewallet", "acc_gold", "acc_savings", "acc_other_asset", "acc_bank_konven", "acc_bank_special")
            for (entry in entries) {
                try {
                    val arr = org.json.JSONArray(entry.linesJson)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val accId = obj.optString("accountId")
                        if (assetPrefixes.contains(accId) || accId.startsWith("acc_wallet_") || accId.startsWith("acc_custom_")) {
                            val debit = obj.optDouble("debit", 0.0)
                            val credit = obj.optDouble("credit", 0.0)
                            totalWalletBalance += (debit - credit)
                        }
                    }
                } catch (_: Exception) {}
            }
            totalWalletBalance = totalWalletBalance.coerceAtLeast(0.0)

            val goals = db.ibadahGoalDao().getAllGoals()
            val totalGoalSavings: Double = goals.sumOf { it.currentAccumulated }
            val totalWealth: Double = totalWalletBalance + totalGoalSavings
            val goldPrice: Double = 1350000.0 // Acuan rata-rata BAZNAS & Antam
            val nisabGold85g: Double = 85.0 * goldPrice // ~ Rp 114.750.000

            if (totalWealth >= nisabGold85g) {
                val prefs = appContext.getSharedPreferences("amanah_zakat_reminders", Context.MODE_PRIVATE)
                val lastNotified = prefs.getLong("last_haul_notification_ts", 0L)
                val thirtyDays = 30L * 24 * 60 * 60 * 1000
                if (System.currentTimeMillis() - lastNotified > thirtyDays) {
                    showNotification(
                        notificationId = 1003,
                        title = "Pengingat Haul Zakat Maal (Wajib)",
                        message = "Total kekayaan Anda telah mencapai nisab 85g emas. Pastikan memeriksa masa kepemilikan (haul 1 tahun hijriah) dan tunaikan 2.5% zakat maal."
                    )
                    prefs.edit().putLong("last_haul_notification_ts", System.currentTimeMillis()).apply()
                    AppDebugLogger.i("AmanahReminderWorker", "Notifikasi pengingat haul zakat maal terkirim.")
                }
            }

            Result.success()
        } catch (e: Exception) {
            AppDebugLogger.e("AmanahReminderWorker", "Terjadi kesalahan pada worker: ${e.message}", e)
            Result.retry()
        }
    }

    private fun showNotification(notificationId: Int, title: String, message: String) {
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val channelId = "amanah_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Amanah Syariah",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifikasi pengingat jatuh tempo qardh dan transaksi syariah"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            notificationManager.notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Izin notifikasi belum diberikan oleh user
        }
    }

    companion object {
        private const val WORK_NAME = "AmanahPeriodicReminderWork"

        /**
         * Menjadwalkan pengecekan periodik hemat daya (interval 12 jam).
         */
        fun schedulePeriodicCheck(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build()

                val workRequest = PeriodicWorkRequestBuilder<AmanahReminderWorker>(
                    12, TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
                AppDebugLogger.i("AmanahReminderWorker", "Penjadwalan WorkManager hemat baterai diaktifkan.")
            } catch (e: Throwable) {
                AppDebugLogger.w("AmanahReminderWorker", "WorkManager tidak aktif atau sedang berjalan di lingkungan unit test: ${e.message}")
            }
        }
    }
}

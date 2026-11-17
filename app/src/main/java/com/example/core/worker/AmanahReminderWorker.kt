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
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.core.database.AmanahDatabase
import com.example.core.database.EntityMappers
import com.example.core.database.SettingsEntity
import com.example.core.datastore.DataStoreManager
import com.example.core.debug.AppDebugLogger
import com.example.core.infaq.SedekahSubuhState
import com.example.core.infaq.SedekahSubuhStreakEngine
import com.example.core.notification.AmanahNotificationConfig
import com.example.core.notification.NotificationCategoryType
import com.example.core.notification.NotificationEvaluator
import com.example.core.notification.ZakatTriggerType
import com.example.core.repository.AmanahRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AmanahReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val categoryArg = inputData.getString(KEY_CATEGORY) ?: CATEGORY_ALL
        return try {
            AppDebugLogger.i("AmanahReminderWorker", "Menjalankan evaluasi pengingat ($categoryArg)...")
            runEvaluationAndNotify(
                context = appContext,
                categoryFilter = categoryArg,
                isTest = false
            )
            Result.success()
        } catch (e: Exception) {
            AppDebugLogger.e("AmanahReminderWorker", "Terjadi kesalahan pada worker: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_CATEGORY = "reminder_category"
        const val CATEGORY_ALL = "ALL"
        const val CATEGORY_DAILY_INFAQ = "DAILY_INFAQ"
        const val CATEGORY_DEBT = "DEBT"
        const val CATEGORY_ZAKAT_HAUL = "ZAKAT_HAUL"

        private const val WORK_PERIODIC_FALLBACK = "AmanahPeriodicReminderWork"
        private const val WORK_DAILY_INFAQ = "AmanahDailyInfaqReminderWork"
        private const val WORK_DEBT = "AmanahDebtReminderWork"
        private const val WORK_ZAKAT_HAUL = "AmanahZakatHaulReminderWork"

        const val CHANNEL_DAILY_INFAQ = "amanah_daily_infaq_channel"
        const val CHANNEL_DEBT = "amanah_debt_qardh_channel"
        const val CHANNEL_ZAKAT_HAUL = "amanah_zakat_haul_channel"

        private const val NOTIF_ID_DAILY_INFAQ = 3001
        private const val NOTIF_ID_DEBT_QARDH = 3002
        private const val NOTIF_ID_RECURRING = 3003
        private const val NOTIF_ID_ZAKAT_HAUL = 3004

        private const val DEDUPE_PREFS = "amanah_zakat_reminders"
        private const val DAY_MS = 24L * 60L * 60L * 1000L

        fun calculateDailyInitialDelayMillis(
            hour: Int,
            minute: Int,
            nowMillis: Long = System.currentTimeMillis()
        ): Long {
            val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val targetCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
                set(Calendar.MINUTE, minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (targetCal.timeInMillis <= nowCal.timeInMillis) {
                targetCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return (targetCal.timeInMillis - nowCal.timeInMillis).coerceAtLeast(60_000L)
        }

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
                    .setInputData(Data.Builder().putString(KEY_CATEGORY, CATEGORY_ALL).build())
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_PERIODIC_FALLBACK,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            } catch (e: Throwable) {
                AppDebugLogger.w("AmanahReminderWorker", "Penjadwalan WorkManager dilewati: ${e.message}")
            }
        }

        fun scheduleFromConfig(context: Context, config: AmanahNotificationConfig) {
            try {
                val wm = WorkManager.getInstance(context)
                if (!config.isMasterEnabled) {
                    wm.cancelUniqueWork(WORK_DAILY_INFAQ)
                    wm.cancelUniqueWork(WORK_DEBT)
                    wm.cancelUniqueWork(WORK_ZAKAT_HAUL)
                    AppDebugLogger.i("AmanahReminderWorker", "Semua jadwal notifikasi terpusat dinonaktifkan oleh pengguna.")
                    return
                }

                // 1. Daily Infaq Obligation worker
                if (config.dailyInfaq.enabled) {
                    val delay = calculateDailyInitialDelayMillis(config.dailyInfaq.hour, config.dailyInfaq.minute)
                    val req = PeriodicWorkRequestBuilder<AmanahReminderWorker>(24, TimeUnit.HOURS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(Data.Builder().putString(KEY_CATEGORY, CATEGORY_DAILY_INFAQ).build())
                        .build()
                    wm.enqueueUniquePeriodicWork(WORK_DAILY_INFAQ, ExistingPeriodicWorkPolicy.UPDATE, req)
                } else {
                    wm.cancelUniqueWork(WORK_DAILY_INFAQ)
                }

                // 2. Debt & Recurring worker
                if (config.debt.enabled) {
                    val delay = calculateDailyInitialDelayMillis(config.debt.hour, config.debt.minute)
                    val req = PeriodicWorkRequestBuilder<AmanahReminderWorker>(24, TimeUnit.HOURS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(Data.Builder().putString(KEY_CATEGORY, CATEGORY_DEBT).build())
                        .build()
                    wm.enqueueUniquePeriodicWork(WORK_DEBT, ExistingPeriodicWorkPolicy.UPDATE, req)
                } else {
                    wm.cancelUniqueWork(WORK_DEBT)
                }

                // 3. Zakat Haul & Nisab worker
                if (config.zakatHaul.enabled) {
                    val delay = calculateDailyInitialDelayMillis(config.zakatHaul.hour, config.zakatHaul.minute)
                    val req = PeriodicWorkRequestBuilder<AmanahReminderWorker>(24, TimeUnit.HOURS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(Data.Builder().putString(KEY_CATEGORY, CATEGORY_ZAKAT_HAUL).build())
                        .build()
                    wm.enqueueUniquePeriodicWork(WORK_ZAKAT_HAUL, ExistingPeriodicWorkPolicy.UPDATE, req)
                } else {
                    wm.cancelUniqueWork(WORK_ZAKAT_HAUL)
                }

                AppDebugLogger.i(
                    "AmanahReminderWorker",
                    "Jadwal notifikasi kustom diperbarui: ${config.summarySubtitle()}"
                )
            } catch (e: Throwable) {
                AppDebugLogger.w("AmanahReminderWorker", "Penjadwalan WorkManager kustom dilewati: ${e.message}")
            }
        }

        suspend fun syncFromSavedPreferences(context: Context) {
            try {
                val prefs = DataStoreManager(context).userPreferencesFlow.first()
                scheduleFromConfig(context, prefs.notificationConfig)
            } catch (e: Throwable) {
                AppDebugLogger.w("AmanahReminderWorker", "Gagal sinkronisasi jadwal notifikasi awal: ${e.message}")
            }
        }

        suspend fun sendImmediateTestNotification(
            context: Context,
            category: NotificationCategoryType
        ): Boolean = withContext(Dispatchers.IO) {
            if (category == NotificationCategoryType.INFAQ_VAULT) {
                return@withContext InfaqVaultReminderWorker.sendImmediateTestNotification(context)
            }
            val filter = when (category) {
                NotificationCategoryType.DAILY_INFAQ -> CATEGORY_DAILY_INFAQ
                NotificationCategoryType.DEBT_QARDH -> CATEGORY_DEBT
                NotificationCategoryType.ZAKAT_HAUL -> CATEGORY_ZAKAT_HAUL
                NotificationCategoryType.INFAQ_VAULT -> CATEGORY_DAILY_INFAQ
            }
            runEvaluationAndNotify(
                context = context,
                categoryFilter = filter,
                isTest = true
            )
        }

        suspend fun runEvaluationAndNotify(
            context: Context,
            categoryFilter: String = CATEGORY_ALL,
            isTest: Boolean = false
        ): Boolean = withContext(Dispatchers.IO) {
            val dataStore = DataStoreManager(context)
            val prefs = dataStore.userPreferencesFlow.first()
            val config = prefs.notificationConfig

            if (!isTest) {
                if (!config.isMasterEnabled) return@withContext false
                val calNow = Calendar.getInstance()
                if (config.isWithinQuietHours(calNow.get(Calendar.HOUR_OF_DAY), calNow.get(Calendar.MINUTE))) {
                    AppDebugLogger.i("AmanahReminderWorker", "Jam Tenang aktif (${config.formattedQuietHours()}), notifikasi $categoryFilter ditunda.")
                    return@withContext false
                }
            }

            val db = AmanahDatabase.getDatabase(context)
            val repo = AmanahRepositoryImpl(db, dataStore)
            val entries = db.journalDao().getAllEntries().map { EntityMappers.toDomain(it) }
            val qardhRecords = db.qardhDao().getAllRecords().map { EntityMappers.toDomain(it) }
            val rules = db.infaqRuleDao().getAll().map { EntityMappers.toDomain(it) }
            val recurring = db.recurringTransactionDao().getAll().map { EntityMappers.toDomain(it) }
            val goals = db.ibadahGoalDao().getAllGoals().map { EntityMappers.toDomain(it) }
            val sedekahState = loadSedekahState(db)
            val shariahConfig = repo.getShariahRulesConfig() ?: com.example.core.shariah.ShariahRulesConfig()

            val dedupePrefs = context.getSharedPreferences(DEDUPE_PREFS, Context.MODE_PRIVATE)
            val now = System.currentTimeMillis()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
            var anySent = false

            // 1. Daily Infaq Obligation
            if (categoryFilter == CATEGORY_ALL || categoryFilter == CATEGORY_DAILY_INFAQ) {
                val eval = NotificationEvaluator.evaluateDailyInfaq(
                    config = config,
                    sedekahState = sedekahState,
                    entries = entries,
                    rules = rules,
                    nowMillis = now,
                    isTest = isTest
                )
                val lastDailyDay = dedupePrefs.getString("last_daily_infaq_day", "")
                if (eval.shouldNotify && (isTest || lastDailyDay != todayStr)) {
                    val sent = showNotification(
                        context = context,
                        channelId = CHANNEL_DAILY_INFAQ,
                        channelName = "Kewajiban Infaq Harian & Sedekah Subuh",
                        channelDesc = "Pengingat harian sedekah subuh dan kewajiban infaq syariah",
                        notificationId = NOTIF_ID_DAILY_INFAQ,
                        title = eval.title,
                        shortText = eval.shortBody,
                        expandedText = eval.expandedBody,
                        navigateTo = "SEDEKAH_SUBUH",
                        actionTitle = "Tunaikan Sekarang",
                        config = config
                    )
                    if (sent && !isTest) {
                        dedupePrefs.edit().putString("last_daily_infaq_day", todayStr).apply()
                    }
                    anySent = anySent || sent
                }
            }

            // 2. Debt (Qardh) & Recurring Bills
            if (categoryFilter == CATEGORY_ALL || categoryFilter == CATEGORY_DEBT) {
                val eval = NotificationEvaluator.evaluateDebtAndRecurring(
                    config = config,
                    notifyOnRecurringDue = prefs.notifyOnRecurringDue,
                    qardhRecords = qardhRecords,
                    recurringTransactions = recurring,
                    nowMillis = now,
                    isTest = isTest
                )

                val overdueCooldownMs = config.debt.overdueRepeatDays.coerceAtLeast(1).toLong() * DAY_MS
                val shouldDispatchDebt = if (isTest) {
                    true
                } else if (eval.shouldNotifyDebt) {
                    eval.debtDedupeKeys.any { key ->
                        if (key.endsWith("_overdue")) {
                            val lastOverdue = dedupePrefs.getLong("dedupe_ts_$key", 0L)
                            (now - lastOverdue) >= overdueCooldownMs
                        } else {
                            dedupePrefs.getString("dedupe_day_$key", "") != todayStr
                        }
                    }
                } else false

                if (shouldDispatchDebt) {
                    val sent = showNotification(
                        context = context,
                        channelId = CHANNEL_DEBT,
                        channelName = "Jatuh Tempo Qardh & Tagihan",
                        channelDesc = "Pengingat jatuh tempo hutang, piutang Qardh Hasan, dan transaksi rutin",
                        notificationId = NOTIF_ID_DEBT_QARDH,
                        title = eval.debtTitle,
                        shortText = eval.debtShortBody,
                        expandedText = eval.debtExpandedBody,
                        navigateTo = "QARDH",
                        actionTitle = "Lihat Qardh",
                        config = config
                    )
                    if (sent && !isTest) {
                        val editor = dedupePrefs.edit()
                        eval.debtDedupeKeys.forEach { key ->
                            if (key.endsWith("_overdue")) {
                                editor.putLong("dedupe_ts_$key", now)
                            } else {
                                editor.putString("dedupe_day_$key", todayStr)
                            }
                        }
                        editor.apply()
                    }
                    anySent = anySent || sent
                }

                if (eval.shouldNotifyRecurring) {
                    val hasFreshRecurring = eval.recurringDedupeKeys.any { key ->
                        dedupePrefs.getString("dedupe_day_$key", "") != todayStr
                    }
                    if (hasFreshRecurring) {
                        val sentRec = showNotification(
                            context = context,
                            channelId = CHANNEL_DEBT,
                            channelName = "Jatuh Tempo Qardh & Tagihan",
                            channelDesc = "Pengingat jatuh tempo hutang, piutang Qardh Hasan, dan transaksi rutin",
                            notificationId = NOTIF_ID_RECURRING,
                            title = eval.recurringTitle,
                            shortText = eval.recurringShortBody,
                            expandedText = eval.recurringExpandedBody,
                            navigateTo = "RECURRING_TRANSACTIONS",
                            actionTitle = "Buka Jadwal Rutin",
                            config = config
                        )
                        if (sentRec) {
                            val editor = dedupePrefs.edit()
                            eval.recurringDedupeKeys.forEach { key ->
                                editor.putString("dedupe_day_$key", todayStr)
                            }
                            editor.apply()
                        }
                        anySent = anySent || sentRec
                    }
                }
            }

            // 3. Zakat Haul & Nisab
            if (categoryFilter == CATEGORY_ALL || categoryFilter == CATEGORY_ZAKAT_HAUL) {
                val (walletBalance, _) = NotificationEvaluator.calculateVaultAndAssetWealth(entries)
                val goalSavings = goals.sumOf { it.currentAccumulated }.coerceAtLeast(0.0)
                val storedHaulStart = prefs.haulStartDateMillis.takeIf { it > 0L }
                    ?: dedupePrefs.getLong("haul_start_date_ts", 0L)

                val eval = NotificationEvaluator.evaluateZakatHaul(
                    config = config,
                    shariahConfig = shariahConfig,
                    goldPricePerGram = prefs.goldPricePerGram,
                    walletBalance = walletBalance,
                    goalSavings = goalSavings,
                    qardhRecords = qardhRecords,
                    existingHaulStartMillis = storedHaulStart,
                    nowMillis = now,
                    isTest = isTest
                )

                // Sync Haul Start / Reset back to DataStore & Settings
                if (!isTest && eval.updatedHaulStartMillis != storedHaulStart) {
                    dataStore.saveHaulStartDateMillis(eval.updatedHaulStartMillis)
                    db.settingsDao().insertOrUpdate(
                        SettingsEntity("haul_start_date_millis", eval.updatedHaulStartMillis.toString())
                    )
                    if (eval.shouldResetHaul) {
                        dedupePrefs.edit().remove("haul_start_date_ts").apply()
                    } else {
                        dedupePrefs.edit().putLong("haul_start_date_ts", eval.updatedHaulStartMillis).apply()
                    }
                }

                val shouldDispatchZakat = if (isTest) {
                    true
                } else when (eval.triggerType) {
                    ZakatTriggerType.NONE -> false
                    ZakatTriggerType.APPROACHING_NISAB -> {
                        val lastTs = dedupePrefs.getLong("last_approaching_notification_ts", 0L)
                        (now - lastTs) >= config.zakatHaul.approachingRepeatDays.coerceAtLeast(1).toLong() * DAY_MS
                    }
                    ZakatTriggerType.NISAB_REACHED -> {
                        val lastDay = dedupePrefs.getString("last_nisab_reached_day", "")
                        lastDay != todayStr
                    }
                    ZakatTriggerType.HAUL_MILESTONE -> {
                        val milestoneKey = "zakat_milestone_h${eval.triggerMilestoneDay ?: eval.daysRemaining}"
                        dedupePrefs.getString("dedupe_day_$milestoneKey", "") != todayStr
                    }
                    ZakatTriggerType.HAUL_DUE -> {
                        val lastTs = dedupePrefs.getLong("last_haul_completed_notification_ts", 0L)
                        (now - lastTs) >= config.zakatHaul.overdueRepeatDays.coerceAtLeast(1).toLong() * DAY_MS
                    }
                }

                if (shouldDispatchZakat) {
                    val sent = showNotification(
                        context = context,
                        channelId = CHANNEL_ZAKAT_HAUL,
                        channelName = "Nisab & Jadwal Haul Zakat Maal",
                        channelDesc = "Pengingat ambang nisab emas 85g dan jatuh tempo haul zakat tahunan",
                        notificationId = NOTIF_ID_ZAKAT_HAUL,
                        title = eval.title,
                        shortText = eval.shortBody,
                        expandedText = eval.expandedBody,
                        navigateTo = "ZAKAT_HUB",
                        actionTitle = "Buka Zakat Hub",
                        config = config
                    )
                    if (sent && !isTest) {
                        val editor = dedupePrefs.edit()
                        when (eval.triggerType) {
                            ZakatTriggerType.APPROACHING_NISAB -> editor.putLong("last_approaching_notification_ts", now)
                            ZakatTriggerType.NISAB_REACHED -> editor.putString("last_nisab_reached_day", todayStr)
                            ZakatTriggerType.HAUL_MILESTONE -> {
                                val milestoneKey = "zakat_milestone_h${eval.triggerMilestoneDay ?: eval.daysRemaining}"
                                editor.putString("dedupe_day_$milestoneKey", todayStr)
                            }
                            ZakatTriggerType.HAUL_DUE -> editor.putLong("last_haul_completed_notification_ts", now)
                            ZakatTriggerType.NONE -> {}
                        }
                        editor.apply()
                    }
                    anySent = anySent || sent
                }
            }

            anySent
        }

        private suspend fun loadSedekahState(db: AmanahDatabase): SedekahSubuhState {
            val entity = db.sedekahSubuhDao().getSedekahState()
            val historyJson = db.settingsDao().getSettingByKey("sedekah_subuh_history_json")?.value
            val hMap = mutableMapOf<String, Double>()
            if (!historyJson.isNullOrBlank()) {
                try {
                    val obj = JSONObject(historyJson)
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        hMap[k] = obj.optDouble(k, 0.0)
                    }
                } catch (_: Exception) {}
            }
            val calc = SedekahSubuhStreakEngine.calculateStreak(hMap)
            return if (entity != null) {
                calc.copy(
                    currentStreak = maxOf(calc.currentStreak, entity.currentStreak),
                    longestStreak = maxOf(calc.longestStreak, entity.longestStreak),
                    totalContributions = maxOf(calc.totalContributions, entity.totalContributions),
                    totalDaysGiven = maxOf(calc.totalDaysGiven, entity.totalDaysGiven)
                )
            } else calc
        }

        fun showNotification(
            context: Context,
            channelId: String,
            channelName: String,
            channelDesc: String,
            notificationId: Int,
            title: String,
            shortText: String,
            expandedText: String,
            navigateTo: String,
            actionTitle: String,
            config: AmanahNotificationConfig
        ): Boolean {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = if (config.useHighPriorityAlerts) {
                    NotificationManager.IMPORTANCE_HIGH
                } else {
                    NotificationManager.IMPORTANCE_DEFAULT
                }
                val channel = NotificationChannel(channelId, channelName, importance).apply {
                    description = channelDesc
                    enableVibration(config.enableVibration)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", navigateTo)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(shortText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.ic_menu_view, actionTitle, pendingIntent)
                .setPriority(
                    if (config.useHighPriorityAlerts) NotificationCompat.PRIORITY_HIGH
                    else NotificationCompat.PRIORITY_DEFAULT
                )

            if (!config.enableSound && !config.enableVibration) {
                builder.setSilent(true)
            } else {
                var defaults = 0
                if (config.enableSound) defaults = defaults or NotificationCompat.DEFAULT_SOUND
                if (config.enableVibration) defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
                builder.setDefaults(defaults)
                if (!config.enableSound) builder.setSound(null)
                if (!config.enableVibration) builder.setVibrate(longArrayOf(0L))
            }

            return try {
                notificationManager.notify(notificationId, builder.build())
                AppDebugLogger.i("AmanahReminderWorker", "Notifikasi '$title' terkirim (-> $navigateTo)")
                true
            } catch (e: SecurityException) {
                AppDebugLogger.w("AmanahReminderWorker", "Izin POST_NOTIFICATIONS belum diberikan: ${e.message}")
                false
            }
        }
    }
}

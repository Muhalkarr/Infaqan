package com.example.core.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.core.database.AmanahDatabase
import com.example.core.database.EntityMappers
import com.example.core.datastore.DataStoreManager
import com.example.core.debug.AppDebugLogger
import com.example.core.infaq.InfaqCalculationType
import com.example.core.infaq.InfaqReminderFrequency
import com.example.core.infaq.InfaqRule
import com.example.core.infaq.InfaqVaultReminderConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.infaq.AsnafCategory
import com.example.core.accounting.JournalLine
import com.example.core.accounting.JournalEntry
import com.example.core.infaq.InfaqDistributionRecord
import kotlin.math.round

/**
 * InfaqVaultReminderWorker
 *
 * Worker periodik yang mengingatkan pengguna untuk menyalurkan dana dari Virtual Infaq Vault
 * berdasarkan aturan otomatisasi infaq (InfaqRule) yang telah dikonfigurasi, frekuensi (Harian/Mingguan),
 * serta jam spesifik pilihan pengguna.
 */
class InfaqVaultReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = DataStoreManager(appContext).userPreferencesFlow.first()
            val notifConfig = prefs.notificationConfig
            val config = prefs.infaqVaultReminderConfig
            if (!config.isEnabled && !config.isAutoRecurringEnabled) {
                return Result.success()
            }

            if (config.isAutoRecurringEnabled) {
                executeAutoRecurringPaymentIfDue(appContext, config)
            }

            val calNow = Calendar.getInstance()
            val suppressedByGlobal = !notifConfig.isMasterEnabled ||
                notifConfig.isWithinQuietHours(calNow.get(Calendar.HOUR_OF_DAY), calNow.get(Calendar.MINUTE))

            if (config.isEnabled && !suppressedByGlobal) {
                dispatchVaultReminderNotification(
                    context = appContext,
                    config = config,
                    vaultMonthlyTarget = prefs.vaultMonthlyTarget,
                    isTest = false
                )
            }
            Result.success()
        } catch (e: Exception) {
            AppDebugLogger.e("InfaqVaultReminderWorker", "Gagal mengeksekusi pengingat Infaq Vault: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "AmanahInfaqVaultReminderWork"
        const val CHANNEL_ID = "amanah_infaq_vault_channel"
        private const val NOTIFICATION_ID = 2001

        fun calculateInitialDelayMillis(
            config: InfaqVaultReminderConfig,
            nowMillis: Long = System.currentTimeMillis()
        ): Long {
            val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
            val targetCal = Calendar.getInstance().apply {
                timeInMillis = nowMillis
                set(Calendar.HOUR_OF_DAY, config.hour.coerceIn(0, 23))
                set(Calendar.MINUTE, config.minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            when (config.frequency) {
                InfaqReminderFrequency.DAILY -> {
                    if (targetCal.timeInMillis <= nowCal.timeInMillis) {
                        targetCal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
                InfaqReminderFrequency.WEEKLY -> {
                    targetCal.set(Calendar.DAY_OF_WEEK, config.dayOfWeek)
                    if (targetCal.timeInMillis <= nowCal.timeInMillis) {
                        targetCal.add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }
            }

            return (targetCal.timeInMillis - nowCal.timeInMillis).coerceAtLeast(60_000L)
        }

        fun scheduleOrUpdate(context: Context, config: InfaqVaultReminderConfig) {
            try {
                if (!config.isEnabled && !config.isAutoRecurringEnabled) {
                    cancel(context)
                    return
                }

                val repeatIntervalHours = when (config.frequency) {
                    InfaqReminderFrequency.DAILY -> 24L
                    InfaqReminderFrequency.WEEKLY -> 168L
                }
                val initialDelayMillis = calculateInitialDelayMillis(config)

                val workRequest = PeriodicWorkRequestBuilder<InfaqVaultReminderWorker>(
                    repeatIntervalHours, TimeUnit.HOURS
                )
                    .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
                AppDebugLogger.i(
                    "InfaqVaultReminderWorker",
                    "Jadwal pengingat Infaq Vault diperbarui: ${config.scheduleSummaryText()} (delay ${(initialDelayMillis / 60000)} mnt)"
                )
            } catch (e: Throwable) {
                AppDebugLogger.w("InfaqVaultReminderWorker", "Penjadwalan WorkManager dilewati: ${e.message}")
            }
        }

        fun cancel(context: Context) {
            try {
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                AppDebugLogger.i("InfaqVaultReminderWorker", "Jadwal pengingat Infaq Vault dinonaktifkan.")
            } catch (e: Throwable) {
                AppDebugLogger.w("InfaqVaultReminderWorker", "Gagal membatalkan WorkManager: ${e.message}")
            }
        }

        suspend fun syncFromSavedPreferences(context: Context) {
            try {
                val prefs = DataStoreManager(context).userPreferencesFlow.first()
                scheduleOrUpdate(context, prefs.infaqVaultReminderConfig)
            } catch (e: Throwable) {
                AppDebugLogger.w("InfaqVaultReminderWorker", "Gagal sinkronisasi jadwal awal: ${e.message}")
            }
        }

        suspend fun sendImmediateTestNotification(context: Context): Boolean = withContext(Dispatchers.IO) {
            val prefs = DataStoreManager(context).userPreferencesFlow.first()
            dispatchVaultReminderNotification(
                context = context,
                config = prefs.infaqVaultReminderConfig,
                vaultMonthlyTarget = prefs.vaultMonthlyTarget,
                isTest = true
            )
        }

        suspend fun dispatchVaultReminderNotification(
            context: Context,
            config: InfaqVaultReminderConfig,
            vaultMonthlyTarget: Double,
            isTest: Boolean
        ): Boolean = withContext(Dispatchers.IO) {
            val db = AmanahDatabase.getDatabase(context)
            val entries = db.journalDao().getAllEntries()
            var vaultCredit = 0.0
            var vaultDebit = 0.0
            for (entry in entries) {
                try {
                    val arr = JSONArray(entry.linesJson)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        if (obj.optString("accountId") == "acc_vault") {
                            vaultDebit += obj.optDouble("debit", 0.0)
                            vaultCredit += obj.optDouble("credit", 0.0)
                        }
                    }
                } catch (_: Exception) {}
            }
            val vaultBalance = round((vaultCredit - vaultDebit).coerceAtLeast(0.0))

            if (!isTest && (vaultBalance <= 0.0 || vaultBalance < config.minVaultBalanceThreshold)) {
                AppDebugLogger.i(
                    "InfaqVaultReminderWorker",
                    "Lewati notifikasi rutin: saldo Vault (Rp $vaultBalance) di bawah ambang Rp ${config.minVaultBalanceThreshold}."
                )
                return@withContext false
            }

            val rules: List<InfaqRule> = db.infaqRuleDao().getAll().map { EntityMappers.toDomain(it) }
            val nf = NumberFormat.getIntegerInstance(Locale("id", "ID"))
            val formattedBalance = nf.format(vaultBalance.toLong())

            val rulesSummary = if (rules.isNotEmpty()) {
                val ruleDescriptions = rules.take(3).joinToString(" • ") { rule ->
                    val detail = when (rule.calculationType) {
                        InfaqCalculationType.PERCENTAGE -> "${String.format(Locale.US, "%.1f", rule.rate * 100).removeSuffix(".0")}%"
                        InfaqCalculationType.ROUND_UP -> "Round-Up Rp ${nf.format(rule.roundUpStep.toLong())}"
                        InfaqCalculationType.FLAT -> "Tetap Rp ${nf.format(rule.fixedAmount.toLong())}"
                        InfaqCalculationType.RIKAZ -> "20% Rikaz"
                        InfaqCalculationType.SYUBHAT -> "100% Syubhat"
                    }
                    "${rule.title} ($detail)"
                }
                val extraCount = if (rules.size > 3) " (+${rules.size - 3} aturan lain)" else ""
                "Aturan aktif: $ruleDescriptions$extraCount."
            } else {
                "Aturan otomatisasi penyucian harta siap digunakan."
            }

            val targetPct = if (vaultMonthlyTarget > 0.0) {
                ((vaultBalance / vaultMonthlyTarget) * 100).toInt().coerceAtLeast(0)
            } else 0

            val title = if (isTest) {
                "[Uji Coba] Penyaluran Virtual Infaq Vault"
            } else if (vaultBalance > 0.0) {
                "Amanah Penyaluran Infaq Vault: Rp $formattedBalance"
            } else {
                "Evaluasi Aturan Infaq Otomatis Anda"
            }

            val shortBody = if (vaultBalance > 0.0) {
                "Dana titipan mustahiq sebesar Rp $formattedBalance ($targetPct% dari target bulanan) menunggu disalurkan."
            } else {
                "Saldo Vault saat ini Rp 0. Periksa aturan infaq aktif Anda agar penyisihan berkah berjalan rutin."
            }

            val expandedBody = buildString {
                append(shortBody)
                append("\n")
                append(rulesSummary)
                append("\nJadwal pengingat: ${config.scheduleSummaryText()}.")
                if (vaultBalance > 0.0) {
                    append(" Ketuk untuk membuka Brankas Infaq & menyalurkan ke 8 Asnaf.")
                }
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return@withContext false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Pengingat Penyaluran Infaq Vault",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifikasi terjadwal (harian/mingguan) untuk menyalurkan dana Virtual Infaq Vault sesuai aturan infaq."
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openVaultIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_TO", "VAULT_HISTORY")
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                openVaultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(shortBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(expandedBody))
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)
                .addAction(
                    android.R.drawable.ic_menu_send,
                    "Salurkan Sekarang",
                    contentPendingIntent
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            try {
                notificationManager.notify(NOTIFICATION_ID, notification)
                AppDebugLogger.i("InfaqVaultReminderWorker", "Notifikasi Infaq Vault terkirim (Vault=Rp $formattedBalance, isTest=$isTest).")
                true
            } catch (e: SecurityException) {
                AppDebugLogger.w("InfaqVaultReminderWorker", "Izin POST_NOTIFICATIONS belum diberikan: ${e.message}")
                false
            }
        }

        suspend fun executeAutoRecurringPaymentIfDue(context: Context, config: InfaqVaultReminderConfig) {
            if (!config.isAutoRecurringEnabled || config.recurringAmount <= 0.0) return
            val db = AmanahDatabase.getDatabase(context)
            val now = Date()

            // Idempotency guard: prevent duplicate auto-recurring payouts in the same schedule period
            val existingRecurring = db.infaqDistributionDao().getAll()
                .filter { it.programName == "Auto-Recurring Infaq Vault" }
            val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayKey = dayKeyFmt.format(now)
            val alreadyRanInPeriod = when (config.frequency) {
                InfaqReminderFrequency.DAILY -> existingRecurring.any { dayKeyFmt.format(Date(it.distributionDateMillis)) == todayKey }
                InfaqReminderFrequency.WEEKLY -> existingRecurring.any { (now.time - it.distributionDateMillis) < (6L * 24L * 3600L * 1000L) }
            }
            if (alreadyRanInPeriod) {
                AppDebugLogger.i("InfaqVaultReminderWorker", "Lewati Auto-Recurring: Sudah tereksekusi pada periode ${config.frequency.name} ini.")
                return
            }

            val entries = db.journalDao().getAllEntries()
            var vaultCredit = 0.0
            var vaultDebit = 0.0
            val assetBalances = mutableMapOf<String, Double>()
            for (entry in entries) {
                try {
                    val arr = JSONArray(entry.linesJson)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val accId = obj.optString("accountId")
                        val d = obj.optDouble("debit", 0.0)
                        val c = obj.optDouble("credit", 0.0)
                        if (accId == "acc_vault") {
                            vaultDebit += d
                            vaultCredit += c
                        } else if (accId.isNotBlank()) {
                            assetBalances[accId] = (assetBalances[accId] ?: 0.0) + (d - c)
                        }
                    }
                } catch (_: Exception) {}
            }
            val vaultBalance = round((vaultCredit - vaultDebit).coerceAtLeast(0.0))
            val amount = round(config.recurringAmount)
            if (vaultBalance < amount) {
                AppDebugLogger.i("InfaqVaultReminderWorker", "Lewati Auto-Recurring: Saldo Vault (Rp $vaultBalance) di bawah nominal recurring (Rp $amount).")
                return
            }

            val wallets = db.walletDao().getAllWallets()
            val defaultWallet = wallets.firstOrNull { it.isDefault } ?: wallets.firstOrNull()
            val defaultLinkedId = defaultWallet?.linkedAccountId ?: "acc_cash"
            val sourceAccountId = if ((assetBalances[defaultLinkedId] ?: 0.0) >= amount) {
                defaultLinkedId
            } else {
                wallets
                    .map { it.linkedAccountId }
                    .distinct()
                    .filter { (assetBalances[it] ?: 0.0) >= amount }
                    .maxByOrNull { assetBalances[it] ?: 0.0 }
            }
            if (sourceAccountId == null) {
                AppDebugLogger.i(
                    "InfaqVaultReminderWorker",
                    "Lewati Auto-Recurring: Tidak ada kantong sumber dengan saldo mencukupi (butuh Rp $amount)."
                )
                return
            }

            val hijri = HijriCalendarEngine.fromGregorian(now)
            val journalId = UUID.randomUUID().toString()
            val distId = "dist_rec_${UUID.randomUUID().toString().take(8)}"
            val receiptNo = "REC-AUTO-${SimpleDateFormat("yyyyMMdd", Locale.US).format(now)}-${(1000..9999).random()}"

            val lines = listOf(
                JournalLine(accountId = "acc_vault", debit = amount, credit = 0.0),
                JournalLine(accountId = sourceAccountId, debit = 0.0, credit = amount)
            )

            val entry = JournalEntry(
                id = journalId,
                gregorianDate = now,
                hijriYear = hijri.year,
                hijriMonth = hijri.month,
                hijriDay = hijri.day,
                description = "Penyaluran Otomatis Berkala (Auto-Recurring): ${config.recurringTargetAsnaf}",
                transactionType = "INFAQ_PAYOUT",
                lines = lines
            )

            val mappedAsnaf = when {
                config.recurringTargetAsnaf.contains("Yatim", ignoreCase = true) -> AsnafCategory.FAKIR_MISKIN
                config.recurringTargetAsnaf.contains("Fisabilillah", ignoreCase = true) -> AsnafCategory.FISABILILLAH
                config.recurringTargetAsnaf.contains("Masjid", ignoreCase = true) -> AsnafCategory.FISABILILLAH
                else -> AsnafCategory.FAKIR_MISKIN
            }

            val distRecord = InfaqDistributionRecord(
                id = distId,
                amount = amount,
                recipientName = config.recurringTargetAsnaf,
                asnafCategory = mappedAsnaf,
                distributionDate = now,
                hijriDateString = "${hijri.day} ${hijri.monthName} ${hijri.year} H",
                sourceAccountId = sourceAccountId,
                programName = "Auto-Recurring Infaq Vault",
                receiptNumber = receiptNo,
                notes = "Penyaluran otomatis terjadwal rutin dari Brankas Infaq Amanah",
                isVerified = true
            )

            db.journalDao().insertOrUpdate(EntityMappers.toEntity(entry))
            db.infaqDistributionDao().insertOrUpdate(EntityMappers.toEntity(distRecord))
            AppDebugLogger.i("InfaqVaultReminderWorker", "Auto-Recurring Infaq Vault Berhasil Dieksekusi: Rp ${amount.toLong()} dari $sourceAccountId ke ${config.recurringTargetAsnaf}")
        }
    }
}

package com.example.core.notification

import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

enum class DebtNotificationScope(val title: String, val subtitle: String) {
    BOTH("Hutang & Piutang", "Ingatkan amanah hutang saya dan piutang dari orang lain"),
    HUTANG_ONLY("Hutang Saya Saja", "Fokus pada kewajiban pelunasan hutang saya ke pihak lain"),
    PIUTANG_ONLY("Piutang Saya Saja", "Fokus pada penagihan / pelunasan piutang yang dipinjamkan")
}

enum class ZakatHaulCalendarMode(val title: String, val subtitle: String, val fixedDays: Int?) {
    FOLLOW_SHARIAH_CONFIG("Ikuti Logika Syariah", "Otomatis mengikuti pengaturan Mazhab / AAOIFI di Logika Syariah", null),
    HIJRI_354("Tahun Hijriah (354 Hari)", "Siklus Qamariyah 354 hari (tarif standar 2.5%)", 354),
    MASEHI_365("Tahun Masehi / AAOIFI (365 Hari)", "Siklus Syamsiyah 365 hari (penyesuaian 2.577%)", 365)
}

enum class NotificationCategoryType(val id: String, val label: String) {
    DAILY_INFAQ("DAILY_INFAQ", "Infaq Harian & Sedekah Subuh"),
    DEBT_QARDH("DEBT", "Jatuh Tempo Hutang / Qardh & Rutin"),
    ZAKAT_HAUL("ZAKAT_HAUL", "Nisab & Haul Zakat Maal"),
    INFAQ_VAULT("INFAQ_VAULT", "Penyaluran Virtual Infaq Vault")
}

data class DailyInfaqReminderConfig(
    val enabled: Boolean = true,
    val hour: Int = 5,
    val minute: Int = 15,
    val onlyIfNoInfaqLoggedToday: Boolean = true,
    val includeSedekahSubuhStreak: Boolean = true,
    val includeVaultStatus: Boolean = true,
    val includeActiveRulesSummary: Boolean = true,
    val targetDailyAmountHint: Double = 10_000.0,
    val customNote: String = ""
) {
    fun formattedTime(): String =
        String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
}

data class DebtReminderConfig(
    val enabled: Boolean = true,
    val hour: Int = 8,
    val minute: Int = 0,
    val scope: DebtNotificationScope = DebtNotificationScope.BOTH,
    val leadDays: Set<Int> = setOf(7, 3, 1, 0),
    val remindOverdue: Boolean = true,
    val overdueRepeatDays: Int = 2,
    val includeRecurringBills: Boolean = true,
    val recurringLeadDays: Int = 1,
    val customNote: String = ""
) {
    fun formattedTime(): String =
        String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))

    fun leadDaysSummary(): String {
        val sorted = leadDays.sortedDescending()
        if (sorted.isEmpty()) return "Tidak ada hari H"
        return sorted.joinToString(", ") { if (it == 0) "Hari-H" else "H-$it" }
    }
}

data class ZakatHaulReminderConfig(
    val enabled: Boolean = true,
    val hour: Int = 9,
    val minute: Int = 0,
    val approachingNisabEnabled: Boolean = true,
    val approachingNisabPercent: Int = 80,
    val approachingRepeatDays: Int = 14,
    val haulLeadDays: Set<Int> = setOf(30, 14, 7, 3, 1, 0),
    val overdueRepeatDays: Int = 7,
    val calendarMode: ZakatHaulCalendarMode = ZakatHaulCalendarMode.FOLLOW_SHARIAH_CONFIG,
    val customNote: String = ""
) {
    fun formattedTime(): String =
        String.format(Locale.US, "%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))

    fun leadDaysSummary(): String {
        val sorted = haulLeadDays.sortedDescending()
        if (sorted.isEmpty()) return "Hanya saat haul genap"
        return sorted.joinToString(", ") { if (it == 0) "Genap Haul" else "H-$it" }
    }
}

data class AmanahNotificationConfig(
    val isMasterEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = true,
    val quietStartHour: Int = 22,
    val quietStartMinute: Int = 0,
    val quietEndHour: Int = 4,
    val quietEndMinute: Int = 30,
    val enableSound: Boolean = true,
    val enableVibration: Boolean = true,
    val useHighPriorityAlerts: Boolean = true,
    val dailyInfaq: DailyInfaqReminderConfig = DailyInfaqReminderConfig(),
    val debt: DebtReminderConfig = DebtReminderConfig(),
    val zakatHaul: ZakatHaulReminderConfig = ZakatHaulReminderConfig()
) {
    fun formattedQuietHours(): String = if (!quietHoursEnabled) {
        "Nonaktif"
    } else {
        String.format(
            Locale.US,
            "%02d:%02d – %02d:%02d",
            quietStartHour.coerceIn(0, 23),
            quietStartMinute.coerceIn(0, 59),
            quietEndHour.coerceIn(0, 23),
            quietEndMinute.coerceIn(0, 59)
        )
    }

    fun isWithinQuietHours(hourOfDay: Int, minuteOfHour: Int): Boolean {
        if (!quietHoursEnabled) return false
        val currentMins = hourOfDay.coerceIn(0, 23) * 60 + minuteOfHour.coerceIn(0, 59)
        val startMins = quietStartHour.coerceIn(0, 23) * 60 + quietStartMinute.coerceIn(0, 59)
        val endMins = quietEndHour.coerceIn(0, 23) * 60 + quietEndMinute.coerceIn(0, 59)
        if (startMins == endMins) return false
        return if (startMins < endMins) {
            currentMins in startMins until endMins
        } else {
            // Spans midnight (e.g., 22:00 to 04:30)
            currentMins >= startMins || currentMins < endMins
        }
    }

    fun activeChannelCount(): Int {
        if (!isMasterEnabled) return 0
        var count = 0
        if (dailyInfaq.enabled) count++
        if (debt.enabled) count++
        if (zakatHaul.enabled) count++
        return count
    }

    fun summarySubtitle(): String {
        if (!isMasterEnabled) return "Semua pengingat notifikasi nonaktif"
        val parts = mutableListOf<String>()
        if (dailyInfaq.enabled) parts.add("Infaq ${dailyInfaq.formattedTime()}")
        if (debt.enabled) parts.add("Hutang ${debt.formattedTime()}")
        if (zakatHaul.enabled) parts.add("Haul Zakat ${zakatHaul.formattedTime()}")
        return if (parts.isEmpty()) {
            "Master aktif • Belum ada kategori pengingat yang diaktifkan"
        } else {
            parts.joinToString(" • ")
        }
    }

    fun toJson(): String {
        val root = JSONObject()
        root.put("isMasterEnabled", isMasterEnabled)
        root.put("quietHoursEnabled", quietHoursEnabled)
        root.put("quietStartHour", quietStartHour.coerceIn(0, 23))
        root.put("quietStartMinute", quietStartMinute.coerceIn(0, 59))
        root.put("quietEndHour", quietEndHour.coerceIn(0, 23))
        root.put("quietEndMinute", quietEndMinute.coerceIn(0, 59))
        root.put("enableSound", enableSound)
        root.put("enableVibration", enableVibration)
        root.put("useHighPriorityAlerts", useHighPriorityAlerts)

        val dObj = JSONObject().apply {
            put("enabled", dailyInfaq.enabled)
            put("hour", dailyInfaq.hour.coerceIn(0, 23))
            put("minute", dailyInfaq.minute.coerceIn(0, 59))
            put("onlyIfNoInfaqLoggedToday", dailyInfaq.onlyIfNoInfaqLoggedToday)
            put("includeSedekahSubuhStreak", dailyInfaq.includeSedekahSubuhStreak)
            put("includeVaultStatus", dailyInfaq.includeVaultStatus)
            put("includeActiveRulesSummary", dailyInfaq.includeActiveRulesSummary)
            put("targetDailyAmountHint", dailyInfaq.targetDailyAmountHint.coerceAtLeast(0.0))
            put("customNote", dailyInfaq.customNote)
        }
        root.put("dailyInfaq", dObj)

        val debtObj = JSONObject().apply {
            put("enabled", debt.enabled)
            put("hour", debt.hour.coerceIn(0, 23))
            put("minute", debt.minute.coerceIn(0, 59))
            put("scope", debt.scope.name)
            put("leadDays", JSONArray(debt.leadDays.sortedDescending()))
            put("remindOverdue", debt.remindOverdue)
            put("overdueRepeatDays", debt.overdueRepeatDays.coerceIn(1, 30))
            put("includeRecurringBills", debt.includeRecurringBills)
            put("recurringLeadDays", debt.recurringLeadDays.coerceIn(0, 14))
            put("customNote", debt.customNote)
        }
        root.put("debt", debtObj)

        val zakatObj = JSONObject().apply {
            put("enabled", zakatHaul.enabled)
            put("hour", zakatHaul.hour.coerceIn(0, 23))
            put("minute", zakatHaul.minute.coerceIn(0, 59))
            put("approachingNisabEnabled", zakatHaul.approachingNisabEnabled)
            put("approachingNisabPercent", zakatHaul.approachingNisabPercent.coerceIn(50, 99))
            put("approachingRepeatDays", zakatHaul.approachingRepeatDays.coerceIn(1, 60))
            put("haulLeadDays", JSONArray(zakatHaul.haulLeadDays.sortedDescending()))
            put("overdueRepeatDays", zakatHaul.overdueRepeatDays.coerceIn(1, 60))
            put("calendarMode", zakatHaul.calendarMode.name)
            put("customNote", zakatHaul.customNote)
        }
        root.put("zakatHaul", zakatObj)

        return root.toString()
    }

    companion object {
        val AVAILABLE_DEBT_LEAD_DAYS = listOf(30, 14, 7, 3, 1, 0)
        val AVAILABLE_ZAKAT_LEAD_DAYS = listOf(30, 14, 7, 3, 1, 0)

        fun formatRupiah(amount: Double): String {
            val nf = NumberFormat.getIntegerInstance(Locale("id", "ID"))
            return "Rp ${nf.format(amount.toLong())}"
        }

        fun fromJson(json: String?): AmanahNotificationConfig {
            if (json.isNullOrBlank()) return AmanahNotificationConfig()
            return try {
                val root = JSONObject(json)
                val dObj = root.optJSONObject("dailyInfaq")
                val dailyInfaq = if (dObj != null) {
                    DailyInfaqReminderConfig(
                        enabled = dObj.optBoolean("enabled", true),
                        hour = dObj.optInt("hour", 5).coerceIn(0, 23),
                        minute = dObj.optInt("minute", 15).coerceIn(0, 59),
                        onlyIfNoInfaqLoggedToday = dObj.optBoolean("onlyIfNoInfaqLoggedToday", true),
                        includeSedekahSubuhStreak = dObj.optBoolean("includeSedekahSubuhStreak", true),
                        includeVaultStatus = dObj.optBoolean("includeVaultStatus", true),
                        includeActiveRulesSummary = dObj.optBoolean("includeActiveRulesSummary", true),
                        targetDailyAmountHint = dObj.optDouble("targetDailyAmountHint", 10_000.0).coerceAtLeast(0.0),
                        customNote = dObj.optString("customNote", "")
                    )
                } else DailyInfaqReminderConfig()

                val debtObj = root.optJSONObject("debt")
                val debt = if (debtObj != null) {
                    val leadSet = mutableSetOf<Int>()
                    val arr = debtObj.optJSONArray("leadDays")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            leadSet.add(arr.optInt(i))
                        }
                    }
                    DebtReminderConfig(
                        enabled = debtObj.optBoolean("enabled", true),
                        hour = debtObj.optInt("hour", 8).coerceIn(0, 23),
                        minute = debtObj.optInt("minute", 0).coerceIn(0, 59),
                        scope = try {
                            DebtNotificationScope.valueOf(debtObj.optString("scope", DebtNotificationScope.BOTH.name))
                        } catch (_: Exception) {
                            DebtNotificationScope.BOTH
                        },
                        leadDays = if (leadSet.isNotEmpty() || arr != null) leadSet else setOf(7, 3, 1, 0),
                        remindOverdue = debtObj.optBoolean("remindOverdue", true),
                        overdueRepeatDays = debtObj.optInt("overdueRepeatDays", 2).coerceIn(1, 30),
                        includeRecurringBills = debtObj.optBoolean("includeRecurringBills", true),
                        recurringLeadDays = debtObj.optInt("recurringLeadDays", 1).coerceIn(0, 14),
                        customNote = debtObj.optString("customNote", "")
                    )
                } else DebtReminderConfig()

                val zObj = root.optJSONObject("zakatHaul")
                val zakatHaul = if (zObj != null) {
                    val haulLeadSet = mutableSetOf<Int>()
                    val arr = zObj.optJSONArray("haulLeadDays")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            haulLeadSet.add(arr.optInt(i))
                        }
                    }
                    ZakatHaulReminderConfig(
                        enabled = zObj.optBoolean("enabled", true),
                        hour = zObj.optInt("hour", 9).coerceIn(0, 23),
                        minute = zObj.optInt("minute", 0).coerceIn(0, 59),
                        approachingNisabEnabled = zObj.optBoolean("approachingNisabEnabled", true),
                        approachingNisabPercent = zObj.optInt("approachingNisabPercent", 80).coerceIn(50, 99),
                        approachingRepeatDays = zObj.optInt("approachingRepeatDays", 14).coerceIn(1, 60),
                        haulLeadDays = if (haulLeadSet.isNotEmpty() || arr != null) haulLeadSet else setOf(30, 14, 7, 3, 1, 0),
                        overdueRepeatDays = zObj.optInt("overdueRepeatDays", 7).coerceIn(1, 60),
                        calendarMode = try {
                            ZakatHaulCalendarMode.valueOf(zObj.optString("calendarMode", ZakatHaulCalendarMode.FOLLOW_SHARIAH_CONFIG.name))
                        } catch (_: Exception) {
                            ZakatHaulCalendarMode.FOLLOW_SHARIAH_CONFIG
                        },
                        customNote = zObj.optString("customNote", "")
                    )
                } else ZakatHaulReminderConfig()

                AmanahNotificationConfig(
                    isMasterEnabled = root.optBoolean("isMasterEnabled", true),
                    quietHoursEnabled = root.optBoolean("quietHoursEnabled", true),
                    quietStartHour = root.optInt("quietStartHour", 22).coerceIn(0, 23),
                    quietStartMinute = root.optInt("quietStartMinute", 0).coerceIn(0, 59),
                    quietEndHour = root.optInt("quietEndHour", 4).coerceIn(0, 23),
                    quietEndMinute = root.optInt("quietEndMinute", 30).coerceIn(0, 59),
                    enableSound = root.optBoolean("enableSound", true),
                    enableVibration = root.optBoolean("enableVibration", true),
                    useHighPriorityAlerts = root.optBoolean("useHighPriorityAlerts", true),
                    dailyInfaq = dailyInfaq,
                    debt = debt,
                    zakatHaul = zakatHaul
                )
            } catch (_: Exception) {
                AmanahNotificationConfig()
            }
        }
    }
}

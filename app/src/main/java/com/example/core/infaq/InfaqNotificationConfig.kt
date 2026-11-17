package com.example.core.infaq

import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

enum class InfaqReminderFrequency(val title: String, val subtitle: String) {
    DAILY("Harian", "Pengingat rutin setiap hari pada jam tertentu"),
    WEEKLY("Mingguan", "Pengingat rutin sepekan sekali pada hari & jam pilihan")
}

data class InfaqVaultReminderConfig(
    val isEnabled: Boolean = false,
    val frequency: InfaqReminderFrequency = InfaqReminderFrequency.WEEKLY,
    val hour: Int = 6,
    val minute: Int = 0,
    val dayOfWeek: Int = Calendar.FRIDAY,
    val minVaultBalanceThreshold: Double = 0.0,
    val isAutoRecurringEnabled: Boolean = false,
    val recurringAmount: Double = 50_000.0,
    val recurringTargetAsnaf: String = "Fakir Miskin / 8 Asnaf"
) {
    fun formattedTime(): String {
        val safeHour = hour.coerceIn(0, 23)
        val safeMin = minute.coerceIn(0, 59)
        return String.format(Locale.US, "%02d:%02d", safeHour, safeMin)
    }

    fun dayOfWeekLabel(): String = dayOfWeekToLabel(dayOfWeek)

    fun scheduleSummaryText(): String {
        if (!isEnabled) return "Pengingat penyaluran nonaktif"
        return when (frequency) {
            InfaqReminderFrequency.DAILY -> "Setiap hari pukul ${formattedTime()} WIB"
            InfaqReminderFrequency.WEEKLY -> "Setiap ${dayOfWeekLabel()} pukul ${formattedTime()} WIB"
        }
    }

    fun recurringSummaryText(): String {
        if (!isAutoRecurringEnabled) return "Penyaluran otomatis berkala nonaktif"
        val nf = NumberFormat.getIntegerInstance(Locale("id", "ID"))
        return "Auto-Infaq Rp ${nf.format(recurringAmount.toLong())} ($recurringTargetAsnaf)"
    }

    companion object {
        val DAYS_OF_WEEK: List<Pair<Int, String>> = listOf(
            Calendar.FRIDAY to "Jumat",
            Calendar.MONDAY to "Senin",
            Calendar.TUESDAY to "Selasa",
            Calendar.WEDNESDAY to "Rabu",
            Calendar.THURSDAY to "Kamis",
            Calendar.SATURDAY to "Sabtu",
            Calendar.SUNDAY to "Ahad"
        )

        fun dayOfWeekToLabel(day: Int): String = when (day) {
            Calendar.SUNDAY -> "Ahad"
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            else -> "Jumat"
        }
    }
}

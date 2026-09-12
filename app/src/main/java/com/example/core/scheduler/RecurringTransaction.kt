package com.example.core.scheduler

import java.util.Calendar
import java.util.Date
import java.util.UUID

enum class RecurringType {
    INCOME,
    EXPENSE
}

enum class RecurringFrequency(val label: String) {
    DAILY("Harian"),
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan")
}

data class RecurringTransaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: RecurringType,
    val amount: Double,
    val categoryAccountId: String,
    val assetAccountId: String,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val dayOfMonthOrWeek: Int = 1, // 1-31 for monthly, 1-7 for weekly
    val customInfaqRate: Double = 0.05,
    val enableRoundUp: Boolean = true,
    val roundUpStep: Double = 5000.0,
    val isActive: Boolean = true,
    val autoExecute: Boolean = true,
    val lastExecutedDate: Date? = null,
    val lastExecutedPeriodKey: String? = null,
    val lastExecutionTimestamp: Long = 0L,
    val nextDueDate: Date = calculateInitialDueDate(frequency, dayOfMonthOrWeek),
    val note: String = ""
) {
    /**
     * Menghasilkan kunci periode unik (misal: "2026-09" untuk bulanan, "2026-W37" untuk mingguan, "2026-09-11" untuk harian)
     * Kunci ini mencegah double-posting jika aplikasi dibuka berulang kali pada periode yang sama.
     */
    fun generatePeriodKey(date: Date = Date()): String {
        val cal = Calendar.getInstance().apply { time = date }
        val year = cal.get(Calendar.YEAR)
        return when (frequency) {
            RecurringFrequency.MONTHLY -> String.format(java.util.Locale.US, "%04d-%02d", year, cal.get(Calendar.MONTH) + 1)
            RecurringFrequency.WEEKLY -> String.format(java.util.Locale.US, "%04d-W%02d", year, cal.get(Calendar.WEEK_OF_YEAR))
            RecurringFrequency.DAILY -> String.format(java.util.Locale.US, "%04d-%02d-%02d", year, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    fun isDue(currentDate: Date = Date()): Boolean {
        if (!isActive) return false
        val currentPeriodKey = generatePeriodKey(currentDate)
        if (lastExecutedPeriodKey == currentPeriodKey) return false
        return currentDate.after(nextDueDate) || isSameDay(currentDate, nextDueDate)
    }

    companion object {
        fun calculateInitialDueDate(frequency: RecurringFrequency, dayValue: Int): Date {
            val cal = Calendar.getInstance()
            when (frequency) {
                RecurringFrequency.MONTHLY -> {
                    val currentDay = cal.get(Calendar.DAY_OF_MONTH)
                    val targetDay = dayValue.coerceIn(1, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.DAY_OF_MONTH, targetDay)
                    cal.set(Calendar.HOUR_OF_DAY, 8)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    if (currentDay > targetDay) {
                        cal.add(Calendar.MONTH, 1)
                    }
                }
                RecurringFrequency.WEEKLY -> {
                    val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    val targetDayOfWeek = dayValue.coerceIn(1, 7) // Calendar.SUNDAY = 1 to SATURDAY = 7
                    cal.set(Calendar.DAY_OF_WEEK, targetDayOfWeek)
                    cal.set(Calendar.HOUR_OF_DAY, 8)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    if (currentDayOfWeek > targetDayOfWeek) {
                        cal.add(Calendar.WEEK_OF_YEAR, 1)
                    }
                }
                RecurringFrequency.DAILY -> {
                    cal.set(Calendar.HOUR_OF_DAY, 8)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                }
            }
            return cal.time
        }

        fun calculateNextDueDate(lastDate: Date, frequency: RecurringFrequency, dayValue: Int): Date {
            val cal = Calendar.getInstance()
            cal.time = lastDate
            when (frequency) {
                RecurringFrequency.MONTHLY -> {
                    cal.add(Calendar.MONTH, 1)
                    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    cal.set(Calendar.DAY_OF_MONTH, dayValue.coerceIn(1, maxDay))
                }
                RecurringFrequency.WEEKLY -> {
                    cal.add(Calendar.WEEK_OF_YEAR, 1)
                }
                RecurringFrequency.DAILY -> {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            cal.set(Calendar.HOUR_OF_DAY, 8)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        private fun isSameDay(d1: Date, d2: Date): Boolean {
            val c1 = Calendar.getInstance().apply { time = d1 }
            val c2 = Calendar.getInstance().apply { time = d2 }
            return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                    c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
        }
    }
}

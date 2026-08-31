package com.example.core.calendar

import java.util.Calendar
import java.util.Date

data class HijriDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    val monthName: String
        get() = monthNames.getOrElse(month - 1) { "Bulan $month" }

    val isRamadan: Boolean
        get() = month == 9

    override fun toString(): String = "$day $monthName $year H"

    companion object {
        val monthNames = listOf(
            "Muharram", "Safar", "Rabiul Awwal", "Rabiul Akhir",
            "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadan", "Syawwal", "Dzulqa'dah", "Dzulhijjah"
        )
    }
}

object HijriCalendarEngine {

    /**
     * Konversi Calendar/Date Masehi ke Hijriah menggunakan Algoritma Astronomis
     * Julian Day Number (JDN) dengan pergantian hari saat Maghrib (default 18:00).
     */
    fun fromGregorian(
        date: Date = Date(),
        maghribHour: Int = 18,
        manualOffsetDays: Int = 0
    ): HijriDate {
        val cal = Calendar.getInstance().apply { time = date }
        
        // Cek pergantian hari saat Maghrib
        if (cal.get(Calendar.HOUR_OF_DAY) >= maghribHour) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        if (manualOffsetDays != 0) {
            cal.add(Calendar.DAY_OF_YEAR, manualOffsetDays)
        }

        var y = cal.get(Calendar.YEAR)
        var m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)

        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = (y / 100.0).toInt()
        val b = 2 - a + (a / 4.0).toInt()
        val jd = (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + d + b - 1524

        // Epoch Hijriah standar (16 Juli 622 Masehi)
        var l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631.0).toInt()
        l = l - 10631 * n + 354
        val j = ((10985 - l) / 5316.0).toInt() * ((50 * l) / 17719.0).toInt() +
                ((l / 5670.0).toInt() * ((43 * l) / 15238.0).toInt())
        l = l - ((30 - j) / 15.0).toInt() * ((17719 * j) / 50.0).toInt() -
                ((j / 16.0).toInt() * ((15238 * j) / 43.0).toInt()) + 29

        val hijriMonth = ((24 * l) / 709.0).toInt()
        val hijriDay = l - ((709 * hijriMonth) / 24.0).toInt()
        val hijriYear = 30 * n + j - 30

        return HijriDate(
            year = hijriYear,
            month = hijriMonth.coerceIn(1, 12),
            day = hijriDay.coerceIn(1, 30)
        )
    }

    fun isFriday(date: Date = Date()): Boolean {
        val cal = Calendar.getInstance().apply { time = date }
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
    }
}

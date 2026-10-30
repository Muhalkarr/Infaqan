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

    val monthNameArabic: String
        get() = monthNamesArabic.getOrElse(month - 1) { "" }

    val isRamadan: Boolean
        get() = month == 9

    val isSacredMonth: Boolean
        get() = month in listOf(1, 7, 11, 12) // Muharram, Rajab, Dzulqa'dah, Dzulhijjah

    val isAyyamulBidh: Boolean
        get() = (day in 13..15) && !(month == 12 && day == 13)

    val isTasyrik: Boolean
        get() = month == 12 && day in 11..13

    val isEidFitri: Boolean
        get() = month == 10 && day == 1

    val isEidAdha: Boolean
        get() = month == 12 && day == 10

    val isForbiddenToFast: Boolean
        get() = isEidFitri || isEidAdha || isTasyrik

    val eventName: String?
        get() = when {
            month == 1 && day == 1 -> "Tahun Baru Islam 1 Muharram"
            month == 1 && day == 9 -> "Puasa Tasu'a"
            month == 1 && day == 10 -> "Puasa Asyura (10 Muharram)"
            month == 3 && day == 12 -> "Maulid Nabi Muhammad SAW"
            month == 7 && day == 27 -> "Isra' Mi'raj Nabi Muhammad SAW"
            month == 8 && day == 15 -> "Nisfu Sya'ban"
            month == 9 && day == 1 -> "Awal Ramadhan (Puasa Wajib)"
            month == 9 && day == 17 -> "Nuzulul Qur'an"
            month == 9 && day in listOf(21, 23, 25, 27, 29) -> "Malam Ganjil Lailatul Qadr"
            month == 9 -> "Bulan Suci Ramadhan"
            month == 10 && day == 1 -> "Hari Raya Idul Fitri"
            month == 10 && day in 2..7 -> "Sunnah Puasa 6 Hari Syawwal"
            month == 12 && day in 1..8 -> "Keutamaan 10 Hari Awal Dzulhijjah"
            month == 12 && day == 9 -> "Puasa Sunnah Arafah"
            month == 12 && day == 10 -> "Hari Raya Idul Adha (Nahr)"
            month == 12 && day in 11..13 -> "Hari Tasyrik (Dilarang Berpuasa)"
            isAyyamulBidh -> "Puasa Sunnah Ayyamul Bidh"
            else -> null
        }

    override fun toString(): String = "$day $monthName $year H"

    companion object {
        val monthNames = listOf(
            "Muharram", "Safar", "Rabiul Awwal", "Rabiul Akhir",
            "Jumadil Awwal", "Jumadil Akhir", "Rajab", "Sya'ban",
            "Ramadan", "Syawwal", "Dzulqa'dah", "Dzulhijjah"
        )
        val monthNamesArabic = listOf(
            "محرّم", "صفر", "ربيع الأوّل", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوّال", "ذو القعدة", "ذو الحجّة"
        )
    }
}

data class DualCalendarDay(
    val gregorianDate: Date,
    val gregorianDay: Int,
    val gregorianMonth: Int, // 0-indexed (0 = Jan)
    val gregorianYear: Int,
    val dayOfWeek: Int, // Calendar.SUNDAY = 1 .. Calendar.SATURDAY = 7
    val hijriDate: HijriDate,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val isMondayOrThursday: Boolean,
    val isFriday: Boolean,
    val fastingTag: String?,
    val eventTag: String?
)

object HijriCalendarEngine {

    val gregorianMonthNames = listOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )

    val dayNamesIndonesian = listOf(
        "Ahad", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"
    )

    /**
     * Konversi Calendar/Date Masehi ke Hijriah menggunakan Algoritma Astronomis
     * Julian Day Number (JDN) dengan pergantian hari saat Maghrib (default 18:00).
     * 100% kompatibel di seluruh versi Android tanpa dependensi java.time.
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

    fun getFastingTag(date: Date, hijriDate: HijriDate): String? {
        val cal = Calendar.getInstance().apply { time = date }
        val dow = cal.get(Calendar.DAY_OF_WEEK)

        if (hijriDate.isForbiddenToFast) {
            return "Haram Berpuasa"
        }
        if (hijriDate.isRamadan) {
            return "Puasa Ramadhan"
        }
        if (hijriDate.month == 12 && hijriDate.day == 9) {
            return "Puasa Arafah"
        }
        if (hijriDate.month == 1 && hijriDate.day == 10) {
            return "Puasa Asyura"
        }
        if (hijriDate.month == 1 && hijriDate.day == 9) {
            return "Puasa Tasu'a"
        }
        if (hijriDate.isAyyamulBidh) {
            return "Puasa Ayyamul Bidh"
        }
        if (dow == Calendar.MONDAY) {
            return "Puasa Sunnah Senin"
        }
        if (dow == Calendar.THURSDAY) {
            return "Puasa Sunnah Kamis"
        }
        return null
    }

    /**
     * Membangun daftar kotak kalender (grid days) untuk tampilan bulan Masehi tertentu
     * lengkap dengan padding hari sebelum dan sesudahnya agar genap kelipatan 7.
     */
    fun buildMonthDays(
        year: Int,
        monthIndex: Int, // 0 = Jan .. 11 = Des
        manualOffsetDays: Int = 0
    ): List<DualCalendarDay> {
        val todayCal = Calendar.getInstance()
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayMonth = todayCal.get(Calendar.MONTH)
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, etc.
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<DualCalendarDay>()

        // 1. Hari-hari dari bulan sebelumnya untuk padding awal
        val prevDaysCount = firstDayOfWeek - Calendar.SUNDAY
        if (prevDaysCount > 0) {
            val prevCal = (cal.clone() as Calendar).apply {
                add(Calendar.MONTH, -1)
            }
            val maxPrevDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (i in (maxPrevDays - prevDaysCount + 1)..maxPrevDays) {
                prevCal.set(Calendar.DAY_OF_MONTH, i)
                val date = prevCal.time
                val hijri = fromGregorian(date, manualOffsetDays = manualOffsetDays)
                val dow = prevCal.get(Calendar.DAY_OF_WEEK)
                days.add(
                    DualCalendarDay(
                        gregorianDate = date,
                        gregorianDay = i,
                        gregorianMonth = prevCal.get(Calendar.MONTH),
                        gregorianYear = prevCal.get(Calendar.YEAR),
                        dayOfWeek = dow,
                        hijriDate = hijri,
                        isToday = false,
                        isCurrentMonth = false,
                        isMondayOrThursday = dow == Calendar.MONDAY || dow == Calendar.THURSDAY,
                        isFriday = dow == Calendar.FRIDAY,
                        fastingTag = getFastingTag(date, hijri),
                        eventTag = hijri.eventName
                    )
                )
            }
        }

        // 2. Hari-hari di bulan aktif saat ini
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val date = cal.time
            val hijri = fromGregorian(date, manualOffsetDays = manualOffsetDays)
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val isToday = (year == todayYear && monthIndex == todayMonth && day == todayDay)
            days.add(
                DualCalendarDay(
                    gregorianDate = date,
                    gregorianDay = day,
                    gregorianMonth = monthIndex,
                    gregorianYear = year,
                    dayOfWeek = dow,
                    hijriDate = hijri,
                    isToday = isToday,
                    isCurrentMonth = true,
                    isMondayOrThursday = dow == Calendar.MONDAY || dow == Calendar.THURSDAY,
                    isFriday = dow == Calendar.FRIDAY,
                    fastingTag = getFastingTag(date, hijri),
                    eventTag = hijri.eventName
                )
            )
        }

        // 3. Hari-hari dari bulan berikutnya untuk padding akhir agar kelipatan 7
        val remaining = 7 - (days.size % 7)
        if (remaining in 1..6) {
            val nextCal = (cal.clone() as Calendar).apply {
                add(Calendar.MONTH, 1)
            }
            for (day in 1..remaining) {
                nextCal.set(Calendar.DAY_OF_MONTH, day)
                val date = nextCal.time
                val hijri = fromGregorian(date, manualOffsetDays = manualOffsetDays)
                val dow = nextCal.get(Calendar.DAY_OF_WEEK)
                days.add(
                    DualCalendarDay(
                        gregorianDate = date,
                        gregorianDay = day,
                        gregorianMonth = nextCal.get(Calendar.MONTH),
                        gregorianYear = nextCal.get(Calendar.YEAR),
                        dayOfWeek = dow,
                        hijriDate = hijri,
                        isToday = false,
                        isCurrentMonth = false,
                        isMondayOrThursday = dow == Calendar.MONDAY || dow == Calendar.THURSDAY,
                        isFriday = dow == Calendar.FRIDAY,
                        fastingTag = getFastingTag(date, hijri),
                        eventTag = hijri.eventName
                    )
                )
            }
        }

        return days
    }
}

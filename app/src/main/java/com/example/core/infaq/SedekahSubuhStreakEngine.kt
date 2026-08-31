package com.example.core.infaq

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StreakBadge(
    val id: String,
    val title: String,
    val description: String,
    val requiredDays: Int,
    val iconEmoji: String,
    val isUnlocked: Boolean = false,
    val unlockedDate: Date? = null
)

data class SedekahSubuhState(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalContributions: Double = 0.0,
    val totalDaysGiven: Int = 0,
    val lastContributionDate: Date? = null,
    val historyMap: Map<String, Double> = emptyMap(), // Key: "yyyy-MM-dd", Value: Total Rp given that day
    val badges: List<StreakBadge> = emptyList()
) {
    fun isCompletedToday(): Boolean {
        val todayKey = getTodayKey()
        return historyMap.containsKey(todayKey) && (historyMap[todayKey] ?: 0.0) > 0.0
    }

    fun isCompletedDate(dateKey: String): Boolean {
        return historyMap.containsKey(dateKey) && (historyMap[dateKey] ?: 0.0) > 0.0
    }

    fun getNextBadge(): StreakBadge? {
        return badges.firstOrNull { !it.isUnlocked }
    }

    fun getDaysUntilNextBadge(): Int {
        val next = getNextBadge() ?: return 0
        return (next.requiredDays - currentStreak).coerceAtLeast(0)
    }

    companion object {
        fun getTodayKey(date: Date = Date()): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(date)
        }

        fun getDefaultBadges(): List<StreakBadge> = listOf(
            StreakBadge(
                id = "b_subuh_1",
                title = "Pejuang Fajar Pemula",
                description = "Langkah awal memulai sedekah di waktu subuh yang penuh berkah.",
                requiredDays = 1,
                iconEmoji = "🌅"
            ),
            StreakBadge(
                id = "b_subuh_3",
                title = "Fajar Bertasbih",
                description = "Konsisten bersedekah subuh selama 3 hari berturut-turut.",
                requiredDays = 3,
                iconEmoji = "⚡"
            ),
            StreakBadge(
                id = "b_subuh_7",
                title = "Penebar Cahaya Subuh",
                description = "1 Pekan penuh istiqomah menghidupkan doa malaikat di pagi hari.",
                requiredDays = 7,
                iconEmoji = "🌟"
            ),
            StreakBadge(
                id = "b_subuh_14",
                title = "Sahabat Yatim & Dhuafa",
                description = "2 Pekan istiqomah mengalirkan kebaikan untuk sesama.",
                requiredDays = 14,
                iconEmoji = "🏆"
            ),
            StreakBadge(
                id = "b_subuh_30",
                title = "Bintang Dermawan Istiqomah",
                description = "1 Bulan penuh konsisten bersedekah subuh tanpa terputus.",
                requiredDays = 30,
                iconEmoji = "💎"
            ),
            StreakBadge(
                id = "b_subuh_40",
                title = "Ksatria Tabarru' 40 Hari",
                description = "40 Hari istiqomah subuh mencapai puncak ketulusan amalan fajar.",
                requiredDays = 40,
                iconEmoji = "👑"
            )
        )
    }
}

object SedekahSubuhStreakEngine {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Hitung streak harian berdasarkan riwayat pemberian
     */
    fun calculateStreak(
        historyMap: Map<String, Double>,
        baseBadges: List<StreakBadge> = SedekahSubuhState.getDefaultBadges()
    ): SedekahSubuhState {
        val totalContributions = historyMap.values.sum()
        val totalDays = historyMap.count { it.value > 0.0 }

        if (historyMap.isEmpty()) {
            return SedekahSubuhState(
                currentStreak = 0,
                longestStreak = 0,
                totalContributions = 0.0,
                totalDaysGiven = 0,
                lastContributionDate = null,
                historyMap = emptyMap(),
                badges = baseBadges
            )
        }

        val cal = Calendar.getInstance()
        val today = cal.time
        val todayKey = dateFormat.format(today)

        // Cek kemarin
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = dateFormat.format(cal.time)

        var streak = 0
        var checkCal = Calendar.getInstance()

        // Jika hari ini sudah sedekah, hitung mundur mulai hari ini. Jika belum, mulai dari kemarin
        if (historyMap.containsKey(todayKey) && (historyMap[todayKey] ?: 0.0) > 0.0) {
            checkCal = Calendar.getInstance()
        } else if (historyMap.containsKey(yesterdayKey) && (historyMap[yesterdayKey] ?: 0.0) > 0.0) {
            checkCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        } else {
            streak = 0
        }

        if (streak == 0 && ((historyMap[todayKey] ?: 0.0) > 0 || (historyMap[yesterdayKey] ?: 0.0) > 0)) {
            while (true) {
                val key = dateFormat.format(checkCal.time)
                if (historyMap.containsKey(key) && (historyMap[key] ?: 0.0) > 0.0) {
                    streak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }

        // Hitung longest streak dari riwayat
        var maxStreak = streak
        var tempStreak = 0
        val sortedKeys = historyMap.keys.filter { (historyMap[it] ?: 0.0) > 0.0 }.sorted()
        
        // Evaluasi unlocking badges
        val updatedBadges = baseBadges.map { badge ->
            val unlocked = streak >= badge.requiredDays || maxStreak >= badge.requiredDays
            badge.copy(
                isUnlocked = unlocked,
                unlockedDate = if (unlocked) badge.unlockedDate ?: today else null
            )
        }

        return SedekahSubuhState(
            currentStreak = streak,
            longestStreak = maxOf(streak, maxStreak),
            totalContributions = totalContributions,
            totalDaysGiven = totalDays,
            lastContributionDate = today,
            historyMap = historyMap,
            badges = updatedBadges
        )
    }

    /**
     * Dapatkan 7 hari terakhir (tanggal string, label hari, apakah selesai)
     */
    fun getLast7DaysStrip(state: SedekahSubuhState): List<DayStripItem> {
        val list = mutableListOf<DayStripItem>()
        val cal = Calendar.getInstance()
        val dayNameFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
        val dayNumFormat = SimpleDateFormat("d", Locale("id", "ID"))

        for (i in 6 downTo 0) {
            val dateCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
            val key = dateFormat.format(dateCal.time)
            val isCompleted = state.isCompletedDate(key)
            val amount = state.historyMap[key] ?: 0.0
            val isToday = i == 0

            list.add(
                DayStripItem(
                    dateKey = key,
                    dayName = dayNameFormat.format(dateCal.time),
                    dayNumber = dayNumFormat.format(dateCal.time),
                    isCompleted = isCompleted,
                    amount = amount,
                    isToday = isToday
                )
            )
        }
        return list
    }
}

data class DayStripItem(
    val dateKey: String,
    val dayName: String,
    val dayNumber: String,
    val isCompleted: Boolean,
    val amount: Double,
    val isToday: Boolean
)

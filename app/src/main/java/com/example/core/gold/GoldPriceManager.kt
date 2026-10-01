package com.example.core.gold

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GoldPricePoint(
    val dateLabel: String,
    val timestamp: Long,
    val pricePerGram: Double,
    val source: String = "Standar SK BAZNAS / Logam Mulia"
)

data class CurrencyConversionItem(
    val unitName: String,
    val weightDescription: String,
    val weightInGrams: Double,
    val estimatedIdrValue: Double,
    val fiqhRuleDescription: String
)

object GoldPriceManager {
    private const val PREFS_NAME = "amanah_gold_prefs"
    private const val KEY_CUSTOM_GOLD_PRICE = "custom_gold_price"
    private const val KEY_PRICE_HISTORY = "gold_price_history"

    // Default BAZNAS standard gold price: Rp 1.450.000 / gram (2025/2026 reference)
    const val DEFAULT_GOLD_PRICE = 1_450_000.0
    // Perak reference: Rp 18.500 / gram
    const val DEFAULT_SILVER_PRICE = 18_500.0

    // Classic Islamic Currency Standards:
    // 1 Dinar = 4.25 gram gold (20 Dinar = 85g gold = Nisab Maal)
    const val DINAR_WEIGHT_GRAMS = 4.25
    const val DINAR_NISAB_COUNT = 20.0
    const val GOLD_NISAB_GRAMS = 85.0

    // 1 Dirham = 2.975 gram silver (200 Dirham = 595g silver = Nisab Perak)
    const val DIRHAM_WEIGHT_GRAMS = 2.975
    const val DIRHAM_NISAB_COUNT = 200.0
    const val SILVER_NISAB_GRAMS = 595.0

    private val _goldPriceFlow = MutableStateFlow(DEFAULT_GOLD_PRICE)
    val goldPriceFlow: StateFlow<Double> = _goldPriceFlow.asStateFlow()

    private val _historyFlow = MutableStateFlow<List<GoldPricePoint>>(emptyList())
    val historyFlow: StateFlow<List<GoldPricePoint>> = _historyFlow.asStateFlow()

    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPrice = sp.getFloat(KEY_CUSTOM_GOLD_PRICE, DEFAULT_GOLD_PRICE.toFloat()).toDouble()
        _goldPriceFlow.value = savedPrice

        val historyJson = sp.getString(KEY_PRICE_HISTORY, null)
        if (!historyJson.isNullOrBlank()) {
            try {
                val array = JSONArray(historyJson)
                val list = mutableListOf<GoldPricePoint>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        GoldPricePoint(
                            dateLabel = obj.getString("dateLabel"),
                            timestamp = obj.getLong("timestamp"),
                            pricePerGram = obj.getDouble("pricePerGram"),
                            source = obj.optString("source", "SK BAZNAS")
                        )
                    )
                }
                _historyFlow.value = list.sortedBy { it.timestamp }
                return
            } catch (_: Exception) {}
        }

        // Initialize with default 7-day realistic price trend
        val defaultHistory = generateDefaultHistory(savedPrice)
        _historyFlow.value = defaultHistory
        saveHistory(context, defaultHistory)
    }

    private fun generateDefaultHistory(basePrice: Double): List<GoldPricePoint> {
        val cal = java.util.Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMM", Locale("id", "ID"))
        val deltas = listOf(-18_000.0, -12_000.0, -5_000.0, +8_000.0, +15_000.0, -4_000.0, 0.0)
        val list = mutableListOf<GoldPricePoint>()

        for (i in 6 downTo 0) {
            val pointCal = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_YEAR, -i)
            }
            val price = (basePrice + deltas[6 - i]).coerceAtLeast(1_000_000.0)
            list.add(
                GoldPricePoint(
                    dateLabel = sdf.format(pointCal.time),
                    timestamp = pointCal.timeInMillis,
                    pricePerGram = price,
                    source = if (i == 0) "SK BAZNAS Terkini" else "Historis BAZNAS"
                )
            )
        }
        return list
    }

    fun updateGoldPrice(context: Context, newPrice: Double) {
        if (newPrice <= 0.0) return
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putFloat(KEY_CUSTOM_GOLD_PRICE, newPrice.toFloat()).apply()
        _goldPriceFlow.value = newPrice

        // Append to history
        val sdf = SimpleDateFormat("dd MMM", Locale("id", "ID"))
        val now = Date()
        val newPoint = GoldPricePoint(
            dateLabel = sdf.format(now),
            timestamp = now.time,
            pricePerGram = newPrice,
            source = "Pembaruan Pengguna"
        )
        val currentHistory = _historyFlow.value.toMutableList()
        // Replace today's point if exists or add
        val todayLabel = sdf.format(now)
        val existingIdx = currentHistory.indexOfFirst { it.dateLabel == todayLabel }
        if (existingIdx >= 0) {
            currentHistory[existingIdx] = newPoint
        } else {
            currentHistory.add(newPoint)
        }
        val trimmed = if (currentHistory.size > 14) currentHistory.takeLast(14) else currentHistory
        _historyFlow.value = trimmed
        saveHistory(context, trimmed)
    }

    private fun saveHistory(context: Context, list: List<GoldPricePoint>) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("dateLabel", item.dateLabel)
                put("timestamp", item.timestamp)
                put("pricePerGram", item.pricePerGram)
                put("source", item.source)
            }
            array.put(obj)
        }
        sp.edit().putString(KEY_PRICE_HISTORY, array.toString()).apply()
    }

    fun getClassicConversions(goldPrice: Double = _goldPriceFlow.value): List<CurrencyConversionItem> {
        val dinarValue = DINAR_WEIGHT_GRAMS * goldPrice
        val dirhamValue = DIRHAM_WEIGHT_GRAMS * DEFAULT_SILVER_PRICE
        val nisabGoldValue = GOLD_NISAB_GRAMS * goldPrice
        val nisabSilverValue = SILVER_NISAB_GRAMS * DEFAULT_SILVER_PRICE

        return listOf(
            CurrencyConversionItem(
                unitName = "1 Dinar Syariah",
                weightDescription = "4.25 Gram Emas 24 Karat (99.99%)",
                weightInGrams = DINAR_WEIGHT_GRAMS,
                estimatedIdrValue = dinarValue,
                fiqhRuleDescription = "Standar mata uang khalifah Umar bin Khattab & standar hisab DSN-MUI."
            ),
            CurrencyConversionItem(
                unitName = "20 Dinar (Nisab Maal)",
                weightDescription = "85.0 Gram Emas Murni (20 Dinar)",
                weightInGrams = GOLD_NISAB_GRAMS,
                estimatedIdrValue = nisabGoldValue,
                fiqhRuleDescription = "Ambang batas kewajiban zakat harta simpanan, emas, & tabungan setelah genap haul 1 tahun."
            ),
            CurrencyConversionItem(
                unitName = "1 Dirham Syariah",
                weightDescription = "2.975 Gram Perak Murni (99.9%)",
                weightInGrams = DIRHAM_WEIGHT_GRAMS,
                estimatedIdrValue = dirhamValue,
                fiqhRuleDescription = "Standar perak klasik masa Nabi SAW (7 Dinar sebanding dengan 10 Dirham)."
            ),
            CurrencyConversionItem(
                unitName = "200 Dirham (Nisab Perak)",
                weightDescription = "595.0 Gram Perak Murni",
                weightInGrams = SILVER_NISAB_GRAMS,
                estimatedIdrValue = nisabSilverValue,
                fiqhRuleDescription = "Batas nisab perak menurut hadits shahih riwayat Bukhari & Muslim."
            )
        )
    }

    fun convertCustomGoldWeight(grams: Double, goldPrice: Double = _goldPriceFlow.value): Double {
        return (grams * goldPrice).coerceAtLeast(0.0)
    }

    fun convertCustomDinar(dinarCount: Double, goldPrice: Double = _goldPriceFlow.value): Double {
        return (dinarCount * DINAR_WEIGHT_GRAMS * goldPrice).coerceAtLeast(0.0)
    }
}

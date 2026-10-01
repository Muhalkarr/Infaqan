package com.example.core.grounding

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class CustomGroundedRuling(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String, // ZAKAT, RIBA_DAN_HUTANG, INVESTASI, ISRAF_KONSUMSI, SYUBHAT, TATA_KELOLA
    val authority: String, // e.g. DSN-MUI, BAZNAS, Majelis Tarjih, Lajnah Bahtsul Masail, Fatwa Kustom
    val referenceNumber: String,
    val summary: String,
    val detailedRuling: String,
    val calculationFormula: String? = null,
    val tags: List<String> = emptyList(),
    val dalilSource: String? = null,
    val dalilArabic: String? = null,
    val dalilTranslation: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

object CustomRulingRepository {
    private const val FILE_NAME = "custom_rulings.json"

    private val _customRulingsState = MutableStateFlow<List<CustomGroundedRuling>>(emptyList())
    val customRulingsState: StateFlow<List<CustomGroundedRuling>> = _customRulingsState.asStateFlow()

    fun loadAll(context: Context): List<CustomGroundedRuling> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            _customRulingsState.value = emptyList()
            return emptyList()
        }
        return try {
            val jsonStr = file.readText()
            val array = JSONArray(jsonStr)
            val list = mutableListOf<CustomGroundedRuling>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val tagsList = mutableListOf<String>()
                val tagsArr = obj.optJSONArray("tags")
                if (tagsArr != null) {
                    for (t in 0 until tagsArr.length()) {
                        tagsList.add(tagsArr.getString(t))
                    }
                }
                list.add(
                    CustomGroundedRuling(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        title = obj.optString("title", ""),
                        category = obj.optString("category", "TATA_KELOLA"),
                        authority = obj.optString("authority", "Rujukan Kustom Pengguna"),
                        referenceNumber = obj.optString("referenceNumber", "-"),
                        summary = obj.optString("summary", ""),
                        detailedRuling = obj.optString("detailedRuling", ""),
                        calculationFormula = obj.optString("calculationFormula", "").ifBlank { null },
                        tags = tagsList,
                        dalilSource = obj.optString("dalilSource", "").ifBlank { null },
                        dalilArabic = obj.optString("dalilArabic", "").ifBlank { null },
                        dalilTranslation = obj.optString("dalilTranslation", "").ifBlank { null },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            val sorted = list.sortedByDescending { it.createdAt }
            _customRulingsState.value = sorted
            sorted
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, ruling: CustomGroundedRuling) {
        val current = loadAll(context).toMutableList()
        current.removeAll { it.id == ruling.id }
        current.add(0, ruling)
        persist(context, current)
        _customRulingsState.value = current
    }

    fun delete(context: Context, id: String) {
        val current = loadAll(context).toMutableList()
        current.removeAll { it.id == id }
        persist(context, current)
        _customRulingsState.value = current
    }

    private fun persist(context: Context, list: List<CustomGroundedRuling>) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("category", item.category)
                    put("authority", item.authority)
                    put("referenceNumber", item.referenceNumber)
                    put("summary", item.summary)
                    put("detailedRuling", item.detailedRuling)
                    put("calculationFormula", item.calculationFormula ?: "")
                    val tagsArr = JSONArray()
                    item.tags.forEach { tagsArr.put(it) }
                    put("tags", tagsArr)
                    put("dalilSource", item.dalilSource ?: "")
                    put("dalilArabic", item.dalilArabic ?: "")
                    put("dalilTranslation", item.dalilTranslation ?: "")
                    put("createdAt", item.createdAt)
                }
                array.put(obj)
            }
            file.writeText(array.toString())
        } catch (_: Exception) {
        }
    }
}

package com.example.core.grounding

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class FatwaConsultationMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val groundedReferences: List<String> = emptyList()
)

object GeminiFatwaRagService {
    private const val PREFS_NAME = "gemini_rag_prefs"
    private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(context: Context): String {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = sp.getString(KEY_CUSTOM_API_KEY, null)?.trim()
        if (!customKey.isNullOrBlank()) {
            return customKey
        }
        return try {
            val buildConfigKey = BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String
            if (!buildConfigKey.isNullOrBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    fun getCustomApiKey(context: Context): String {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_CUSTOM_API_KEY, "") ?: ""
    }

    fun setCustomApiKey(context: Context, key: String) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    suspend fun consultFatwa(
        context: Context,
        userQuery: String,
        rulingsDatabase: List<Pair<String, String>>, // list of (title, summary + detailedRuling + dalil)
        literatureDatabase: List<Pair<String, String>> // list of (title, summary + citation)
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey(context)

        // Find relevant grounding references from query keywords
        val keywords = userQuery.lowercase().split(" ", ",", ".", "?").filter { it.length > 2 }
        val matchedRulings = rulingsDatabase.filter { (title, detail) ->
            keywords.any { kw -> title.lowercase().contains(kw) || detail.lowercase().contains(kw) }
        }.take(5)

        val matchedLiterature = literatureDatabase.filter { (title, detail) ->
            keywords.any { kw -> title.lowercase().contains(kw) || detail.lowercase().contains(kw) }
        }.take(3)

        // Build Grounding Context
        val groundingContextBuilder = StringBuilder()
        groundingContextBuilder.append("Berikut basis rujukan fiqih dan pustaka keuangan Islam resmi yang relevan dalam aplikasi Amanah Ledger:\n\n")

        if (matchedRulings.isNotEmpty()) {
            groundingContextBuilder.append("=== RUJUKAN FATWA & REGULASI RESMI ===\n")
            matchedRulings.forEachIndexed { idx, (title, content) ->
                groundingContextBuilder.append("${idx + 1}. $title:\n$content\n\n")
            }
        } else {
            // Include core rulings
            groundingContextBuilder.append("=== RUJUKAN FATWA UTAMA ===\n")
            rulingsDatabase.take(3).forEachIndexed { idx, (title, content) ->
                groundingContextBuilder.append("${idx + 1}. $title:\n$content\n\n")
            }
        }

        if (matchedLiterature.isNotEmpty()) {
            groundingContextBuilder.append("=== RUJUKAN KITAB KLASIK & TATA KELOLA KAS ===\n")
            matchedLiterature.forEachIndexed { idx, (title, content) ->
                groundingContextBuilder.append("${idx + 1}. $title:\n$content\n\n")
            }
        }

        val systemPrompt = """
Anda adalah "Amanah Syariah Advisor", asisten AI konsultan hukum fiqih muamalah dan tata kelola keuangan Islam pada aplikasi Amanah Ledger.
Tugas Anda adalah memberikan jawaban fatwa yang komprehensif, terstruktur, santun, dan menenangkan hati pengguna berdasarkan tuntunan Al-Qur'an, As-Sunnah, Ijma' Sahabat, Kitab Fiqih Klasik, serta Fatwa DSN-MUI / BAZNAS.

Aturan Penting:
1. Sertakan ayat Al-Qur'an atau Hadits dengan teks Arab asli (lengkap harakat) dan terjemahan bahasa Indonesianya.
2. Sebutkan nomor rujukan fatwa resmi (misal Fatwa DSN-MUI No. xx/DSN-MUI/..., SK BAZNAS, atau PMA Kemenag No. 52/2014).
3. Berikan rincian hisab/tata kelola praktis langkah demi langkah jika pertanyaan menyangkut perhitungan uang, zakat, rikaz 20%, hutang, atau investasi.
4. Gunakan konteks grounding yang diberikan di bawah ini sebagai rujukan primer.
5. Jawaban harus rapi menggunakan poin-poin penjelasan.

$groundingContextBuilder
        """.trimIndent()

        // If no API key configured or if call fails, fallback to intelligent grounded response
        if (apiKey.isBlank()) {
            val fallbackResponse = buildIntelligentLocalAnswer(userQuery, matchedRulings, matchedLiterature)
            return@withContext Result.success(fallbackResponse)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val requestJson = JSONObject().apply {
                val contentsArr = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArr = JSONArray()
                    partsArr.put(JSONObject().apply {
                        put("text", "$systemPrompt\n\nPertanyaan Pengguna:\n\"$userQuery\"")
                    })
                    put("parts", partsArr)
                }
                contentsArr.put(contentObj)
                put("contents", contentsArr)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // If API returned error (e.g. quota or invalid key), provide rich grounded local fallback
                val fallbackResponse = buildIntelligentLocalAnswer(userQuery, matchedRulings, matchedLiterature)
                return@withContext Result.success(fallbackResponse)
            }

            val responseJson = JSONObject(responseBody)
            val candidates = responseJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "")

            if (text.isNullOrBlank()) {
                val fallbackResponse = buildIntelligentLocalAnswer(userQuery, matchedRulings, matchedLiterature)
                Result.success(fallbackResponse)
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            val fallbackResponse = buildIntelligentLocalAnswer(userQuery, matchedRulings, matchedLiterature)
            Result.success(fallbackResponse)
        }
    }

    private fun buildIntelligentLocalAnswer(
        query: String,
        matchedRulings: List<Pair<String, String>>,
        matchedLiterature: List<Pair<String, String>>
    ): String {
        val qLower = query.lowercase()
        return when {
            qLower.contains("rikaz") || qLower.contains("temuan") || qLower.contains("karun") || qLower.contains("20%") -> {
                """
📌 *PANDUAN HUKUM RIKAZ (HARTA TEMUAN PURBAKALA) 20%*
--------------------------------------------------
Berdasarkan ketentuan syariah dan rujukan PMA Kemenag No. 52/2014 Pasal 24:

1. *Kewajiban Khumus (20% Seketika):*
Harta rikaz adalah harta berharga peninggalan purbakala zaman jahiliyah yang ditemukan terpendam di dalam tanah tanpa adanya klaim pemilik sah. Terhadap rikaz, wajib dikeluarkan zakatnya sebesar **20% (Khumus)** seketika saat ditemukan, **tanpa syarat haul (1 tahun)** dan **tanpa syarat nisab**.

2. *Dalil Shahih Hadits Rasulullah SAW:*
«وَفِي الرِّكَازِ الْخُمُسُ»
*“Dan pada harta rikaz (temuan terpendam), wajib dikeluarkan seperlima (20%).”*
— **HR. Bukhari (No. 1499) & HR. Muslim (No. 1710)**

3. *Pembeda Tegas dengan Luqathah (Barang Tercecer):*
- **Rikaz:** Harta kuno purbakala terpendam -> Wajib zakat 20% seketika, 80% sisa menjadi hak sah penemu.
- **Luqathah:** Barang tercecer milik orang masa kini -> Wajib diumumkan (ta'rif) selama 1 tahun di tempat umum, tidak boleh langsung diambil hak miliknya.

4. *Penyaluran Zakat Rikaz:*
Disalurkan kepada mustahiq 8 asnaf atau melalui Lembaga Amil Zakat resmi (BAZNAS / LAZ terdaftar).
                """.trimIndent()
            }
            qLower.contains("emas") || qLower.contains("tabungan") || qLower.contains("maal") -> {
                """
📌 *PANDUAN ZAKAT MAAL (EMAS & SIMPANAN KAS)*
--------------------------------------------------
Berdasarkan Fatwa MUI No. 3/2003 & Peraturan BAZNAS No. 1/2024:

1. *Ketentuan Nisab & Kadar Zakat:*
- **Nisab:** Setara **85 gram emas murni**.
- **Kadar Zakat:** **2.5%** dari total saldo simpanan kas, tabungan, dan nilai emas.
- **Syarat Haul:** Telah mengendap sempurna selama **1 tahun Hijriah (354 hari)** atau disesuaikan tahun Masehi (2.577%).

2. *Dalil Dalil Shahih:*
«فَإِذَا كَانَتْ لَكَ عِشْرُونَ دِينَارًا وَحَالَ عَلَيْهَا الْحَوْلُ فَفِيهَا نِصْفُ دِينَارٍ»
*“Apabila engkau memiliki 20 dinar (setara 85 gram emas) dan telah berlalu satu haul, maka padanya wajib zakat setengah dinar (2.5%).”*
— **HR. Abu Dawud (No. 1573)**

3. *Cara Hisab Otomatis Saldo Riil:*
Gunakan tab **Kalkulator Zakat Otomatis** pada layar ini untuk mengagregasi saldo seluruh dompet kas Anda secara real-time.
                """.trimIndent()
            }
            qLower.contains("gaji") || qLower.contains("profesi") || qLower.contains("penghasilan") -> {
                """
📌 *PANDUAN ZAKAT PROFESI & PENGHASILAN BULANAN*
--------------------------------------------------
Berdasarkan Fatwa MUI No. 3 Tahun 2003 & SK BAZNAS No. 1 Tahun 2024:

1. *Nisab & Waktu Penunaian:*
- **Nisab Tahunan:** 85 gram emas (atau dibagi 12 bulan untuk hisab bulanan, setara ~7.08 gram emas/bulan atau kisaran Rp 6.850.000,-/bulan).
- **Tarif Zakat:** **2.5%** ditunaikan pada saat penerimaan gaji/pendapatan.

2. *Dalil Al-Qur'an:*
يَا أَيُّهَا الَّذِينَ آمَنُوا أَنفِقُوا مِن طَيِّبَاتِ مَا كَسَبْتُمْ
*“Wahai orang-orang yang beriman, infakkanlah sebagian dari hasil usahamu yang baik-baik...”*
— **QS. Al-Baqarah: 267**

3. *Metode Perhitungan:*
- **Metode Bruto:** 2.5% x Total Penghasilan Kotor (lebih utama & ikhtiyath).
- **Metode Netto:** 2.5% x (Penghasilan Kotor - Kebutuhan Pokok Primer Bulanan).
                """.trimIndent()
            }
            matchedRulings.isNotEmpty() -> {
                val ruling = matchedRulings.first()
                """
📌 *PANDUAN HUKUM: ${ruling.first.uppercase()}*
--------------------------------------------------
Berdasarkan rujukan resmi pada basis pengetahuan Amanah Ledger:

${ruling.second}

💡 *Saran Praktis:* Anda dapat mencatat dan menguji transaksi terkait pada menu Catat Transaksi untuk memastikan kepatuhan akad syariah bebas riba, gharar, dan maisir.
                """.trimIndent()
            }
            else -> {
                """
📌 *KONSULTASI FATWA & TATA KELOLA KEUANGAN SYARIAH*
--------------------------------------------------
Terima kasih atas pertanyaan Anda mengenai muamalah syariah. 

Kaidah Fiqih Fundamental:
«الأَصْلُ فِي الْمُعَامَلَاتِ الإِبَاحَةُ حَتَّى يَدُلَّ الدَّلِيلُ عَلَى التَّحْرِيمِ»
*“Hukum asal dalam muamalah adalah boleh (halal), sampai ada dalil shahih yang melarangnya.”*

Asas Transaksi Bebas Riba, Maisir & Gharar:
1. Setiap transaksi wajib berlandaskan keridhaan timbal balik (*an taradhin minkum*, QS. An-Nisa: 29).
2. Dilarang mengambil tambahan atas piutang uang (*kullu qardhin jarra manfa'atan fahuwa riba*).
3. Transaksi hutang piutang wajib dicatat secara transparan dan amanah (QS. Al-Baqarah: 282).
4. Gunakan tab Ensiklopedia Fatwa dan Pustaka Tata Kelola pada aplikasi ini untuk membaca rujukan kitab induk dan fatwa DSN-MUI selengkapnya.
                """.trimIndent()
            }
        }
    }
}

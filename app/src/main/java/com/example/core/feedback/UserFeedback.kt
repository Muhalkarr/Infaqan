package com.example.core.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class FeedbackType(val label: String, val iconName: String) {
    FEATURE_SUGGESTION("Saran Fitur Baru", "💡"),
    BUG_REPORT("Laporan Kendala / Bug", "🐞"),
    APPRECIATION("Apresiasi / Kritik Umum", "⭐")
}

enum class FeedbackStatus(val label: String) {
    DRAFT("Draf Belum Terkirim"),
    SENT("Telah Terkirim")
}

data class UserFeedback(
    val id: String = UUID.randomUUID().toString(),
    val type: FeedbackType,
    val rating: Int, // 1..5
    val message: String,
    val contact: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val channelSent: String = "Lokal", // "WhatsApp", "Email", "Draf", "Lokal"
    val status: FeedbackStatus = FeedbackStatus.SENT,
    val updatedAt: Long = System.currentTimeMillis(),
    val revisionCount: Int = 0
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formattedUpdatedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(updatedAt))
    }
}

object FeedbackRepository {
    private const val FILE_NAME = "user_feedbacks.json"
    private const val DEV_EMAIL = "muhammadabdulkholikarrasyid@gmail.com"
    private const val DEV_NAME = "Muhalkarr"

    private val _feedbacksState = MutableStateFlow<List<UserFeedback>>(emptyList())
    val feedbacksState: StateFlow<List<UserFeedback>> = _feedbacksState.asStateFlow()

    fun loadAll(context: Context): List<UserFeedback> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            _feedbacksState.value = emptyList()
            return emptyList()
        }
        return try {
            val jsonStr = file.readText()
            val array = JSONArray(jsonStr)
            val list = mutableListOf<UserFeedback>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", FeedbackType.FEATURE_SUGGESTION.name)
                val type = try {
                    FeedbackType.valueOf(typeStr)
                } catch (_: Exception) {
                    FeedbackType.FEATURE_SUGGESTION
                }
                val statusStr = obj.optString("status", FeedbackStatus.SENT.name)
                val status = try {
                    FeedbackStatus.valueOf(statusStr)
                } catch (_: Exception) {
                    FeedbackStatus.SENT
                }
                list.add(
                    UserFeedback(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        type = type,
                        rating = obj.optInt("rating", 5),
                        message = obj.optString("message", ""),
                        contact = obj.optString("contact", "").ifBlank { null },
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        channelSent = obj.optString("channelSent", "Lokal"),
                        status = status,
                        updatedAt = obj.optLong("updatedAt", obj.optLong("timestamp", System.currentTimeMillis())),
                        revisionCount = obj.optInt("revisionCount", 0)
                    )
                )
            }
            val sorted = list.sortedByDescending { it.updatedAt }
            _feedbacksState.value = sorted
            sorted
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDraft(context: Context, feedback: UserFeedback): UserFeedback {
        val draftFeedback = feedback.copy(
            status = FeedbackStatus.DRAFT,
            channelSent = "Draf",
            updatedAt = System.currentTimeMillis()
        )
        save(context, draftFeedback)
        return draftFeedback
    }

    fun save(context: Context, feedback: UserFeedback) {
        val current = loadAll(context).toMutableList()
        current.removeAll { it.id == feedback.id }
        current.add(0, feedback)
        persist(context, current)
        _feedbacksState.value = current
    }

    fun delete(context: Context, id: String) {
        val current = loadAll(context).toMutableList()
        current.removeAll { it.id == id }
        persist(context, current)
        _feedbacksState.value = current
    }

    private fun persist(context: Context, list: List<UserFeedback>) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("type", item.type.name)
                    put("rating", item.rating)
                    put("message", item.message)
                    put("contact", item.contact ?: "")
                    put("timestamp", item.timestamp)
                    put("channelSent", item.channelSent)
                    put("status", item.status.name)
                    put("updatedAt", item.updatedAt)
                    put("revisionCount", item.revisionCount)
                }
                array.put(obj)
            }
            file.writeText(array.toString())
        } catch (_: Exception) {
        }
    }

    fun formatMessageText(feedback: UserFeedback): String {
        val stars = "★".repeat(feedback.rating) + "☆".repeat(5 - feedback.rating)
        val contactLine = if (!feedback.contact.isNullOrBlank()) "\n👤 Kontak/Pengirim: ${feedback.contact}" else ""
        return """
📢 *MASUKAN PENGGUNA AMANAH LEDGER v2.5*
---------------------------------------
📌 Kategori: ${feedback.type.label}
⭐ Rating Kepuasan: $stars (${feedback.rating}/5)$contactLine
🕒 Waktu: ${feedback.formattedDate()}
📱 Perangkat: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})

💬 *Pesan & Masukan:*
"${feedback.message}"

Terima kasih kepada Developer $DEV_NAME atas pengembangan berkelanjutan aplikasi ini!
        """.trimIndent()
    }

    fun sendViaWhatsApp(context: Context, feedback: UserFeedback): Boolean {
        val updatedFeedback = feedback.copy(
            channelSent = "WhatsApp",
            status = FeedbackStatus.SENT,
            updatedAt = System.currentTimeMillis()
        )
        save(context, updatedFeedback)
        val formatted = formatMessageText(updatedFeedback)
        return try {
            val encoded = URLEncoder.encode(formatted, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?text=$encoded")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback generic send
            try {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, formatted)
                    type = "text/plain"
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(sendIntent, "Bagikan via WhatsApp").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            } catch (err: Exception) {
                Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    fun sendViaEmail(context: Context, feedback: UserFeedback): Boolean {
        val updatedFeedback = feedback.copy(
            channelSent = "Email",
            status = FeedbackStatus.SENT,
            updatedAt = System.currentTimeMillis()
        )
        save(context, updatedFeedback)
        val subject = "[Masukan Amanah Ledger] ${feedback.type.label} - dari Pengguna"
        val body = formatMessageText(updatedFeedback)
        return try {
            val uri = Uri.parse("mailto:$DEV_EMAIL?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(DEV_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(emailIntent, "Kirim Masukan via Email").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                true
            } catch (err: Exception) {
                Toast.makeText(context, "Tidak ada aplikasi email yang tersedia", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }
}

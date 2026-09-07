package com.example.core.debug

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

enum class DebugLogLevel(val label: String) {
    VERBOSE("VRB"),
    DEBUG("DBG"),
    INFO("INF"),
    WARN("WRN"),
    ERROR("ERR"),
    CRASH("FATAL")
}

data class DebugLogEntry(
    val id: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String,
    val level: DebugLogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null
) {
    fun toTerminalString(): String {
        val base = "[$timeFormatted] [${level.label}] [$tag] $message"
        return if (stackTrace != null) "$base\n$stackTrace" else base
    }
}

object AppDebugLogger {
    private const val MAX_MEMORY_LOGS = 600
    private const val LOG_FILE_NAME = "amanah_terminal.log"
    private const val MAX_LOG_FILE_BYTES = 2 * 1024 * 1024 // 2 MB

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val logQueue = ConcurrentLinkedQueue<DebugLogEntry>()
    private var appContext: Context? = null
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var sequenceId = 0L

    private val _logsState = MutableStateFlow<List<DebugLogEntry>>(emptyList())
    val logsState: StateFlow<List<DebugLogEntry>> = _logsState.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        i("AppDebugLogger", "Amanah Ledger Debug Terminal diinisialisasi. Mode audit aktif.")
    }

    fun v(tag: String, message: String) = log(DebugLogLevel.VERBOSE, tag, message)
    fun d(tag: String, message: String) = log(DebugLogLevel.DEBUG, tag, message)
    fun i(tag: String, message: String) = log(DebugLogLevel.INFO, tag, message)
    fun w(tag: String, message: String, tr: Throwable? = null) = log(DebugLogLevel.WARN, tag, message, tr)
    fun e(tag: String, message: String, tr: Throwable? = null) = log(DebugLogLevel.ERROR, tag, message, tr)
    fun fatal(tag: String, message: String, tr: Throwable? = null) = log(DebugLogLevel.CRASH, tag, message, tr)

    fun log(level: DebugLogLevel, tag: String, message: String, tr: Throwable? = null) {
        val now = System.currentTimeMillis()
        val formattedTime = synchronized(timeFormat) {
            timeFormat.format(Date(now))
        }

        val stackTrace = tr?.let {
            val sw = StringWriter()
            it.printStackTrace(PrintWriter(sw))
            sw.toString()
        }

        val entry = DebugLogEntry(
            id = ++sequenceId,
            timestamp = now,
            timeFormatted = formattedTime,
            level = level,
            tag = tag,
            message = message,
            stackTrace = stackTrace
        )

        // Log to Android Logcat
        when (level) {
            DebugLogLevel.VERBOSE -> Log.v(tag, message, tr)
            DebugLogLevel.DEBUG -> Log.d(tag, message, tr)
            DebugLogLevel.INFO -> Log.i(tag, message, tr)
            DebugLogLevel.WARN -> Log.w(tag, message, tr)
            DebugLogLevel.ERROR -> Log.e(tag, message, tr)
            DebugLogLevel.CRASH -> Log.wtf(tag, message, tr)
        }

        // Memory buffer
        logQueue.add(entry)
        while (logQueue.size > MAX_MEMORY_LOGS) {
            logQueue.poll()
        }

        // Emit to StateFlow for live UI update
        val snapshot = logQueue.toList()
        _logsState.update { snapshot }

        // Append to file in background
        appContext?.let { ctx ->
            ioScope.launch {
                appendToFile(ctx, entry)
            }
        }
    }

    private fun appendToFile(context: Context, entry: DebugLogEntry) {
        try {
            val logsDir = File(context.filesDir, "logs").apply { if (!exists()) mkdirs() }
            val logFile = File(logsDir, LOG_FILE_NAME)

            // Rotate file if too large
            if (logFile.exists() && logFile.length() > MAX_LOG_FILE_BYTES) {
                val oldFile = File(logsDir, "amanah_terminal_old.log")
                if (oldFile.exists()) oldFile.delete()
                logFile.renameTo(oldFile)
            }

            FileWriter(logFile, true).use { fw ->
                fw.write(entry.toTerminalString() + "\n")
            }
        } catch (e: Exception) {
            Log.e("AppDebugLogger", "Gagal menulis log ke disk: ${e.message}")
        }
    }

    fun getRecentLogs(limit: Int = 50): List<DebugLogEntry> {
        val list = logQueue.toList()
        return if (list.size > limit) list.takeLast(limit) else list
    }

    fun getFormattedLogsText(): String {
        val builder = StringBuilder()
        val list = logQueue.toList()
        builder.append("=== AMANAH LEDGER SYSTEM TERMINAL LOGS (${list.size} BARIS) ===\n")
        builder.append("Waktu Ekspor: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        builder.append("------------------------------------------------------------------\n")
        for (entry in list) {
            builder.append(entry.toTerminalString()).append("\n")
        }
        return builder.toString()
    }

    fun clearLogs() {
        logQueue.clear()
        _logsState.value = emptyList()
        appContext?.let { ctx ->
            ioScope.launch {
                try {
                    val logsDir = File(ctx.filesDir, "logs")
                    val logFile = File(logsDir, LOG_FILE_NAME)
                    if (logFile.exists()) logFile.delete()
                } catch (e: Exception) {
                    Log.e("AppDebugLogger", "Gagal menghapus file log: ${e.message}")
                }
            }
        }
        i("AppDebugLogger", "Buffer terminal log telah dibersihkan oleh pengguna.")
    }

    fun getLogFile(context: Context): File? {
        val file = File(File(context.filesDir, "logs"), LOG_FILE_NAME)
        return if (file.exists()) file else null
    }
}

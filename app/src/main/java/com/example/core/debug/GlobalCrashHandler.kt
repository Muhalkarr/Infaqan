package com.example.core.debug

import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GlobalCrashHandler : Thread.UncaughtExceptionHandler {
    private const val TAG = "GlobalCrashHandler"
    private const val LATEST_CRASH_FILE = "latest_crash_report.txt"
    private const val CRASH_HISTORY_FILE = "crash_history.log"

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
        AppDebugLogger.i(TAG, "GlobalCrashHandler aktif. Siap menangkap & merekam fatal error.")
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            AppDebugLogger.fatal(TAG, "FATAL CRASH TERDETEKSI pada Thread [${thread.name}] (ID: ${thread.id}): ${throwable.localizedMessage}", throwable)

            appContext?.let { ctx ->
                val report = generateCrashReport(thread, throwable)
                saveCrashReport(ctx, report)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memproses crash handler: ${e.message}", e)
        } finally {
            // Serahkan ke default handler sistem agar aplikasi menutup secara wajar
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun generateCrashReport(thread: Thread, throwable: Throwable): String {
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val fullStackTrace = sw.toString()

        // Identifikasi lokasi error spesifik di kode aplikasi (com.example.*)
        val appTraceElement = throwable.stackTrace.firstOrNull { element ->
            element.className.startsWith("com.example")
        } ?: throwable.stackTrace.firstOrNull()

        val locationInfo = appTraceElement?.let {
            "${it.fileName ?: "Unknown"}:${it.lineNumber} (Method: ${it.methodName} di ${it.className})"
        } ?: "Lokasi tidak diketahui"

        // Ambil log terminal terakhir sebelum crash
        val recentLogs = AppDebugLogger.getRecentLogs(40)

        val sb = StringBuilder()
        sb.append("=================================================================\n")
        sb.append("           🚨 LAPORAN KERUSAKAN SISTEM (CRASH REPORT)            \n")
        sb.append("                    AMANAH LEDGER SYARIAH                        \n")
        sb.append("=================================================================\n\n")
        sb.append("WAKTU CRASH       : $now\n")
        sb.append("TIPE EXCEPTION    : ${throwable.javaClass.name}\n")
        sb.append("PESAN ERROR       : ${throwable.message ?: "(Tanpa Pesan Error)"}\n")
        sb.append("LOKASI PENYEBAB   : $locationInfo\n")
        sb.append("THREAD CRASH      : ${thread.name} (ID: ${thread.id}, Priority: ${thread.priority})\n\n")

        sb.append("--- INFORMASI PERANGKAT ---\n")
        sb.append("Produsen & Model  : ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})\n")
        sb.append("Versi Android     : Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
        sb.append("Brand & Board     : ${Build.BRAND} / ${Build.BOARD}\n")
        sb.append("Arsitektur CPU    : ${Build.SUPPORTED_ABIS.joinToString(", ")}\n\n")

        sb.append("--- FULL STACK TRACE ---\n")
        sb.append(fullStackTrace).append("\n\n")

        sb.append("--- AKTIVITAS LOG TERMINAL TERAKHIR SEBELUM CRASH (${recentLogs.size} Baris) ---\n")
        for (log in recentLogs) {
            sb.append(log.toTerminalString()).append("\n")
        }
        sb.append("\n===================== AKHIR LAPORAN CRASH =====================\n")

        return sb.toString()
    }

    private fun saveCrashReport(context: Context, report: String) {
        try {
            val logsDir = File(context.filesDir, "logs").apply { if (!exists()) mkdirs() }

            // 1. Simpan laporan crash terbaru (untuk dibaca saat app dibuka kembali)
            val latestFile = File(logsDir, LATEST_CRASH_FILE)
            FileOutputStream(latestFile).use { fos ->
                fos.write(report.toByteArray())
                fos.fd.sync() // Pastikan langsung terdorong ke media penyimpanan
            }

            // 2. Append ke riwayat crash jangka panjang
            val historyFile = File(logsDir, CRASH_HISTORY_FILE)
            FileOutputStream(historyFile, true).use { fos ->
                fos.write((report + "\n\n").toByteArray())
                fos.fd.sync()
            }
            Log.i(TAG, "Laporan crash berhasil disimpan secara permanen di ${latestFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menulis file crash report: ${e.message}", e)
        }
    }

    fun hasCrashReport(context: Context): Boolean {
        val file = File(File(context.filesDir, "logs"), LATEST_CRASH_FILE)
        return file.exists() && file.length() > 0
    }

    fun getLatestCrashReport(context: Context): String? {
        val file = File(File(context.filesDir, "logs"), LATEST_CRASH_FILE)
        return if (file.exists()) {
            try {
                file.readText()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun clearLatestCrashReport(context: Context) {
        try {
            val file = File(File(context.filesDir, "logs"), LATEST_CRASH_FILE)
            if (file.exists()) file.delete()
            AppDebugLogger.i(TAG, "Laporan crash terakhir telah diarsipkan/dibersihkan oleh pengguna.")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menghapus file crash report: ${e.message}")
        }
    }

    /**
     * Memungkinkan pengujian crash reporting secara manual untuk memastikan
     * berkas crash log dan stack trace berhasil terekam ke ponsel.
     */
    fun simulateTestCrash() {
        AppDebugLogger.w(TAG, "Pengguna memicu Simulasi Crash Pengujian...")
        throw RuntimeException("SIMULASI PENGUJIAN CRASH: Sengaja dipicu oleh pengguna untuk memverifikasi perekaman log crash lokal.")
    }
}

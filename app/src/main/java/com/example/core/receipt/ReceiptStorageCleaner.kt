package com.example.core.receipt

import android.content.Context
import android.util.Log
import java.io.File

object ReceiptStorageCleaner {
    private const val TAG = "ReceiptStorageCleaner"

    /**
     * Menghapus file gambar struk fisik berdasarkan path absolutnya secara aman.
     */
    fun deleteReceiptFile(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return try {
            val file = File(filePath)
            if (file.exists() && file.isFile) {
                val deleted = file.delete()
                Log.d(TAG, "File bukti transaksi dihapus: $filePath (success: $deleted)")
                deleted
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menghapus file bukti transaksi: $filePath", e)
            false
        }
    }

    /**
     * Membersihkan cache gambar struk sementara (orphaned files) yang tidak lagi tercatat pada database
     * atau transaksi yang dibatalkan oleh pengguna saat scanning OCR / Camera.
     */
    fun cleanupOrphanedReceiptCache(
        context: Context,
        activeReceiptPaths: Set<String>,
        olderThanMillis: Long = 5 * 60 * 1000L // 5 menit toleransi jeda
    ): Int {
        var deletedCount = 0
        val now = System.currentTimeMillis()
        try {
            val cacheDir = context.cacheDir
            val files = cacheDir.listFiles { file ->
                file.isFile && file.name.startsWith("receipt_") && (file.name.endsWith(".jpg") || file.name.endsWith(".png"))
            } ?: emptyArray()

            for (file in files) {
                val path = file.absolutePath
                val age = now - file.lastModified()
                if (!activeReceiptPaths.contains(path) && age > olderThanMillis) {
                    if (file.delete()) {
                        deletedCount++
                        Log.d(TAG, "Membersihkan file bukti transaksi orphaned: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saat membersihkan orphaned receipt cache", e)
        }
        return deletedCount
    }
}

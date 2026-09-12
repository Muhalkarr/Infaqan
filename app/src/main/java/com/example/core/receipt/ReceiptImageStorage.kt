package com.example.core.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ReceiptImageStorage {
    private const val TAG = "ReceiptStorage"
    private const val RECEIPTS_DIR = "receipt_images"

    /**
     * Memastikan folder penyimpanan privat struk tersedia di internal storage aplikasi.
     */
    fun getReceiptsDirectory(context: Context): File {
        val dir = File(context.filesDir, RECEIPTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Menyimpan file gambar sementara (misalnya dari camera output file) ke direktori permanen internal.
     * Mengembalikan absolute path file yang tersimpan.
     */
    fun saveImageFile(context: Context, sourceFile: File, receiptId: String = UUID.randomUUID().toString()): String? {
        return try {
            val dir = getReceiptsDirectory(context)
            val destFile = File(dir, "receipt_${receiptId}.jpg")
            sourceFile.inputStream().use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.i(TAG, "Foto struk berhasil disimpan: ${destFile.absolutePath} (${destFile.length()} bytes)")
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menyalin file foto struk: ${e.message}", e)
            null
        }
    }

    /**
     * Menyimpan gambar dari Uri (misalnya hasil pilihan galeri foto PhotoPicker) ke direktori privat.
     * Gambar otomatis dikompresi proporsional (max 1920px, JPEG 85%) untuk menghemat kapasitas penyimpanan internal.
     */
    fun saveImageFromUri(context: Context, uri: Uri, receiptId: String = UUID.randomUUID().toString()): String? {
        return try {
            val dir = getReceiptsDirectory(context)
            val destFile = File(dir, "receipt_${receiptId}.jpg")
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "Tidak dapat membuka input stream dari Uri: $uri")
                return null
            }

            // Decode and optimize bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) {
                Log.e(TAG, "Gagal mendecode bitmap dari Uri: $uri")
                return null
            }

            val maxDimension = 1920
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val ratio = width.toFloat() / height.toFloat()
                val targetW = if (width >= height) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (height > width) maxDimension else (maxDimension / ratio).toInt()
                Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            } else {
                originalBitmap
            }

            FileOutputStream(destFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            Log.i(TAG, "Foto struk dari galeri disimpan: ${destFile.absolutePath} (${destFile.length()} bytes)")
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menyimpan foto struk dari Uri: ${e.message}", e)
            null
        }
    }

    /**
     * Menghapus file gambar struk dari internal storage jika ada.
     */
    fun deleteImageFile(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val deleted = file.delete()
                Log.i(TAG, "File foto struk dihapus: $filePath, status: $deleted")
                deleted
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menghapus file foto struk: ${e.message}", e)
            false
        }
    }

    /**
     * Alias helper untuk menghapus file sementara
     */
    fun deleteTemporaryFile(filePath: String?): Boolean {
        return deleteImageFile(filePath)
    }

    /**
     * Memeriksa apakah file gambar fisik benar-benar ada di storage.
     */
    fun isFileExists(filePath: String?): Boolean {
        if (filePath.isNullOrBlank()) return false
        return try {
            val f = File(filePath)
            f.exists() && f.length() > 0
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Membersihkan file gambar struk yang sudah tidak lagi direferensikan oleh transaksi/mutasi manapun.
     * Mengembalikan jumlah file yatim (orphan) yang berhasil dihapus.
     */
    fun cleanupOrphanReceiptImages(context: Context, validReferencedPaths: Set<String>): Int {
        return try {
            val dir = getReceiptsDirectory(context)
            val files = dir.listFiles() ?: return 0
            var deletedCount = 0
            for (file in files) {
                if (file.isFile && !validReferencedPaths.contains(file.absolutePath)) {
                    val deleted = file.delete()
                    if (deleted) {
                        deletedCount++
                        Log.i(TAG, "File foto struk yatim dihapus: ${file.name}")
                    }
                }
            }
            Log.i(TAG, "Total file foto struk yatim dibersihkan: $deletedCount file")
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membersihkan foto struk yatim: ${e.message}", e)
            0
        }
    }

    /**
     * Membersihkan file gambar sementara di context.cacheDir
     * (misalnya dari jepretan kamera yang dibatalkan oleh pengguna).
     */
    fun cleanupTemporaryCameraCache(context: Context, maxAgeMillis: Long = 15 * 60 * 1000L): Int {
        return try {
            val cacheDir = context.cacheDir
            val files = cacheDir.listFiles { file ->
                file.isFile && file.name.startsWith("receipt_") && file.name.endsWith(".jpg")
            } ?: return 0
            val now = System.currentTimeMillis()
            var deletedCount = 0
            for (file in files) {
                if (now - file.lastModified() > maxAgeMillis) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            if (deletedCount > 0) {
                Log.i(TAG, "Dibersihkan $deletedCount file struk sementara di cache")
            }
            deletedCount
        } catch (e: Exception) {
            Log.e(TAG, "Gagal membersihkan cache struk kamera: ${e.message}", e)
            0
        }
    }
}

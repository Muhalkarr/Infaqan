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
     * Menyimpan dan mengompresi file gambar (misalnya dari camera capture) ke direktori permanen internal.
     * Menggunakan downsampling dan penskalaan cerdas (max 1280px, JPEG 80%) untuk menghemat penyimpanan internal.
     */
    fun saveImageFile(context: Context, sourceFile: File, receiptId: String = UUID.randomUUID().toString()): String? {
        return try {
            val dir = getReceiptsDirectory(context)
            val destFile = File(dir, "receipt_${receiptId}.jpg")
            val originalLength = sourceFile.length()

            val success = compressBitmapToFile(sourceFile.absolutePath, destFile, maxDimension = 1280, quality = 80)
            if (!success) {
                // Fallback copy jika decode bitmap gagal
                sourceFile.inputStream().use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            val savedLength = destFile.length()
            val ratio = if (originalLength > 0) ((originalLength - savedLength).toDouble() / originalLength * 100).toInt() else 0
            Log.i(TAG, "Foto struk berhasil dikompresi: ${destFile.absolutePath} ($originalLength bytes -> $savedLength bytes, hemat $ratio%)")
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengompresi file foto struk: ${e.message}", e)
            null
        }
    }

    /**
     * Menyimpan gambar dari Uri (misalnya hasil pilihan galeri foto PhotoPicker) ke direktori privat.
     * Gambar otomatis dikompresi proporsional (max 1280px, JPEG 80%) untuk menghemat kapasitas penyimpanan internal.
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

            // Baca bounds terlebih dahulu untuk kalkulasi downsampling
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Hitung inSampleSize
            val maxDimension = 1280
            var sampleSize = 1
            var w = options.outWidth
            var h = options.outHeight
            while (w / 2 >= maxDimension || h / 2 >= maxDimension) {
                w /= 2
                h /= 2
                sampleSize *= 2
            }

            val decodeStream = context.contentResolver.openInputStream(uri) ?: return null
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val sampledBitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
            decodeStream.close()

            if (sampledBitmap == null) {
                Log.e(TAG, "Gagal mendecode bitmap dari Uri: $uri")
                return null
            }

            val curW = sampledBitmap.width
            val curH = sampledBitmap.height
            val scaledBitmap = if (curW > maxDimension || curH > maxDimension) {
                val ratio = curW.toFloat() / curH.toFloat()
                val targetW = if (curW >= curH) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (curH > curW) maxDimension else (maxDimension / ratio).toInt()
                Bitmap.createScaledBitmap(sampledBitmap, targetW, targetH, true)
            } else {
                sampledBitmap
            }

            FileOutputStream(destFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            if (scaledBitmap != sampledBitmap) {
                scaledBitmap.recycle()
            }
            sampledBitmap.recycle()

            Log.i(TAG, "Foto struk dari galeri dikompresi: ${destFile.absolutePath} (${destFile.length()} bytes)")
            destFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menyimpan foto struk dari Uri: ${e.message}", e)
            null
        }
    }

    /**
     * Helper internal untuk mengompresi bitmap dari path file lokal ke file tujuan dengan downsampling aman OOM.
     */
    private fun compressBitmapToFile(sourcePath: String, destFile: File, maxDimension: Int = 1280, quality: Int = 80): Boolean {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(sourcePath, boundsOptions)

            var sampleSize = 1
            var w = boundsOptions.outWidth
            var h = boundsOptions.outHeight
            while (w / 2 >= maxDimension || h / 2 >= maxDimension) {
                w /= 2
                h /= 2
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val sampledBitmap = BitmapFactory.decodeFile(sourcePath, decodeOptions) ?: return false

            val curW = sampledBitmap.width
            val curH = sampledBitmap.height
            val scaledBitmap = if (curW > maxDimension || curH > maxDimension) {
                val ratio = curW.toFloat() / curH.toFloat()
                val targetW = if (curW >= curH) maxDimension else (maxDimension * ratio).toInt()
                val targetH = if (curH > curW) maxDimension else (maxDimension / ratio).toInt()
                Bitmap.createScaledBitmap(sampledBitmap, targetW, targetH, true)
            } else {
                sampledBitmap
            }

            FileOutputStream(destFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            if (scaledBitmap != sampledBitmap) {
                scaledBitmap.recycle()
            }
            sampledBitmap.recycle()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Gagal compressBitmapToFile: ${e.message}", e)
            false
        }
    }

    /**
     * Menghitung total ukuran file struk kuitansi yang tersimpan di direktori internal.
     */
    fun getTotalReceiptsStorageBytes(context: Context): Long {
        return try {
            val dir = getReceiptsDirectory(context)
            dir.listFiles()?.filter { it.isFile }?.sumOf { it.length() } ?: 0L
        } catch (_: Exception) {
            0L
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

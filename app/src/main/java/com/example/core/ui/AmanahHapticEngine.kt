package com.example.core.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Mesin Haptic Feedback Sentuhan Taktil (Tactile UX Engine)
 * Mengatur respon getar halus pada interaksi kunci seperti tombol kalkulator numpad,
 * chip seleksi kategori, tombol simpan transaksi, dan perayaan target tabungan tercapai.
 */
object AmanahHapticEngine {

    /**
     * Respon getar sentuhan sangat halus untuk tombol angka, operator kalkulator, dan chip.
     */
    fun performKeyClick(haptic: HapticFeedback?, context: Context? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Throwable) {}

        vibrateLight(context, 15)
    }

    /**
     * Respon getar tombol aksi utama (seperti Simpan Transaksi, Konfirmasi Setor).
     */
    fun performButtonClick(haptic: HapticFeedback?, context: Context? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Throwable) {}

        vibrateMedium(context, 35)
    }

    /**
     * Respon haptic getar selebrasi istimewa saat target tabungan ibadah / simpanan berhasil tercapai (100%).
     * Menghasilkan pola getaran ganda ritmis yang memberikan kepuasan psikologis dan taktil.
     */
    fun performGoalCompletionCelebration(haptic: HapticFeedback?, context: Context? = null) {
        try {
            haptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Throwable) {}

        if (context == null) return
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Pola ritmis perayaan pencapaian: getar pendek, jeda, getar sedang, jeda, getar panjang mantap
                    val timings = longArrayOf(0, 60, 50, 90, 50, 180)
                    val amplitudes = intArrayOf(0, 160, 0, 200, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 70, 60, 100, 60, 200), -1)
                }
            }
        } catch (_: Throwable) {}
    }

    private fun vibrateLight(context: Context?, durationMs: Long) {
        if (context == null) return
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Throwable) {}
    }

    private fun vibrateMedium(context: Context?, durationMs: Long) {
        if (context == null) return
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Throwable) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }
}

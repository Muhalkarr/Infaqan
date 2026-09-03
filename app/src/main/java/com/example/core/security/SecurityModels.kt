package com.example.core.security

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class SecurityEventType {
    PIN_SUCCESS,
    PIN_FAILED,
    BIOMETRIC_SUCCESS,
    PIN_CHANGED,
    PIN_ENABLED,
    PIN_DISABLED,
    SECURITY_ALERT,
    BALANCE_PRIVACY_TOGGLED,
    APP_LOCKED,
    SECURITY_QUESTION_RESET
}

data class SecurityLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: SecurityEventType,
    val description: String,
    val isWarning: Boolean = false
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("id", "ID"))
            return sdf.format(Date(timestamp))
        }
}

enum class AutoLockInterval(val seconds: Int, val label: String) {
    IMMEDIATE(0, "Segera saat keluar"),
    SECONDS_15(15, "15 Detik"),
    MINUTE_1(60, "1 Menit"),
    MINUTES_5(300, "5 Menit"),
    NEVER(-1, "Jangan Pernah Kunci Otomatis")
}

data class SecurityConfig(
    val isPinEnabled: Boolean = false,
    val pinHash: String = "", // SHA-256 hash
    val isBiometricEnabled: Boolean = true,
    val autoLockInterval: AutoLockInterval = AutoLockInterval.IMMEDIATE,
    val isMaskBalance: Boolean = false,
    val maskBalanceByDefault: Boolean = false,
    val isScreenshotProtected: Boolean = false,
    val securityQuestion: String = "Nama kota kelahiran Anda?",
    val securityAnswerHash: String = "",
    val failedAttempts: Int = 0,
    val lockoutUntilTimestamp: Long = 0L,
    val isAppLocked: Boolean = false,
    val logs: List<SecurityLogEntry> = listOf(
        SecurityLogEntry(
            eventType = SecurityEventType.SECURITY_ALERT,
            description = "Sistem keamanan Amanah Ledger diinisialisasi"
        )
    )
) {
    companion object {
        fun hashString(input: String): String {
            if (input.isEmpty()) return ""
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    fun isCurrentlyLockedOut(): Boolean {
        return System.currentTimeMillis() < lockoutUntilTimestamp
    }

    fun remainingLockoutSeconds(): Int {
        val diff = (lockoutUntilTimestamp - System.currentTimeMillis()) / 1000
        return if (diff > 0) diff.toInt() else 0
    }

    fun verifyPin(enteredPin: String): Boolean {
        if (!isPinEnabled) return true
        val hashed = hashString(enteredPin)
        return hashed == pinHash
    }

    fun verifyRecoveryAnswer(answer: String): Boolean {
        val trimmed = answer.trim().lowercase(Locale.ROOT)
        if (trimmed.isEmpty()) return false
        val hashed = hashString(trimmed)
        return hashed == securityAnswerHash
    }
}

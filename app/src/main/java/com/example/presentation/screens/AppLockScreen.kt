package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.compose.ui.platform.LocalContext
import com.example.core.security.BiometricAuthHelper
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.White60
import com.example.ui.theme.White70
import com.example.ui.theme.White80
import kotlinx.coroutines.delay

@Composable
fun AppLockScreen(
    viewModel: AmanahLedgerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val securityConfig = state.securityConfig
    val context = LocalContext.current

    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var biometricSuccessAnimation by remember { mutableStateOf(false) }

    // Function to trigger native biometric prompt
    val triggerBiometricPrompt: () -> Unit = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            BiometricAuthHelper.promptBiometricUnlock(
                activity = activity,
                title = "Buka Amanah Ledger",
                subtitle = "Verifikasi sidik jari untuk mengakses data keuangan syariah",
                negativeButtonText = "Gunakan PIN",
                onSuccess = {
                    biometricSuccessAnimation = true
                    viewModel.unlockWithBiometric()
                },
                onError = { err ->
                    errorMessage = err
                },
                onCancel = {
                    // User canceled biometric prompt, stay on PIN screen
                }
            )
        } else {
            viewModel.unlockWithBiometric()
        }
    }

    // Auto-prompt biometrics once on screen launch if enabled
    LaunchedEffect(Unit) {
        if (securityConfig.isBiometricEnabled && !securityConfig.isCurrentlyLockedOut()) {
            delay(300L)
            triggerBiometricPrompt()
        }
    }

    // Lockout countdown timer
    var lockoutSeconds by remember { mutableIntStateOf(securityConfig.remainingLockoutSeconds()) }

    LaunchedEffect(securityConfig.lockoutUntilTimestamp) {
        while (securityConfig.isCurrentlyLockedOut()) {
            lockoutSeconds = securityConfig.remainingLockoutSeconds()
            delay(1000L)
        }
        lockoutSeconds = 0
    }

    // Auto-verify when 6 digits entered (or when length matches)
    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 6) {
            val success = viewModel.unlockWithPin(enteredPin)
            if (!success) {
                errorMessage = "PIN salah. Silakan coba lagi."
                enteredPin = ""
            } else {
                errorMessage = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF0D1B17),
                        Color(0xFF07120F)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Islamic Shield Icon & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(EmeraldPrimary.copy(alpha = 0.3f), GoldAccent.copy(alpha = 0.2f))
                            )
                        )
                        .border(2.dp, GoldAccent.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (biometricSuccessAnimation) Icons.Default.CheckCircle else Icons.Default.Shield,
                        contentDescription = "Amanah Security Shield",
                        tint = if (biometricSuccessAnimation) EmeraldLight else GoldAccent,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Amanah Ledger Guard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Buku Besar Keuangan Syariah Terproteksi",
                    fontSize = 12.sp,
                    color = White70
                )

                if (lockoutSeconds > 0) {
                    Surface(
                        color = ExpenseCoral.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseCoral.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ExpenseCoral, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Terlalu banyak percobaan. Tunggu $lockoutSeconds detik.",
                                fontSize = 12.sp,
                                color = ExpenseCoral,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.sp,
                        color = ExpenseCoral,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        text = "Masukkan 6-Digit PIN Anda",
                        fontSize = 13.sp,
                        color = EmeraldLight,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Indicator Dots (6 Dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    val isFilled = index < enteredPin.length
                    val scale by animateFloatAsState(
                        targetValue = if (isFilled) 1.2f else 1.0f,
                        animationSpec = tween(150),
                        label = "dot_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) GoldAccent else Color.Transparent
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isFilled) GoldAccent else White60,
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val isLockedOut = lockoutSeconds > 0

                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIOMETRIC", "0", "BACKSPACE")
                )

                for (row in keypadRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            when (key) {
                                "BIOMETRIC" -> {
                                    if (securityConfig.isBiometricEnabled) {
                                        Box(
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)
                                                .background(EmeraldDark.copy(alpha = 0.5f))
                                                .border(1.dp, EmeraldPrimary.copy(alpha = 0.6f), CircleShape)
                                                .clickable(enabled = !isLockedOut) {
                                                    triggerBiometricPrompt()
                                                }
                                                .testTag("biometric_unlock_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Buka dengan Sidik Jari",
                                                tint = GoldAccent,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    } else {
                                        Box(modifier = Modifier.size(68.dp))
                                    }
                                }
                                "BACKSPACE" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .clickable(enabled = !isLockedOut && enteredPin.isNotEmpty()) {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                    errorMessage = null
                                                }
                                            }
                                            .testTag("pin_backspace_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Hapus Angka",
                                            tint = if (enteredPin.isNotEmpty()) White80 else White60.copy(alpha = 0.3f),
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurface.copy(alpha = 0.8f))
                                            .border(1.dp, DarkBorder, CircleShape)
                                            .clickable(enabled = !isLockedOut && enteredPin.length < 6) {
                                                if (enteredPin.length < 6) {
                                                    enteredPin += key
                                                    errorMessage = null
                                                }
                                            }
                                            .testTag("pin_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Forgot PIN / Recovery Option
            TextButton(
                onClick = { showRecoveryDialog = true },
                modifier = Modifier.testTag("forgot_pin_button")
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Lupa PIN? Pulihkan dengan Jawaban Keamanan",
                    fontSize = 12.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Emergency Recovery Dialog
    if (showRecoveryDialog) {
        RecoveryPinDialog(
            question = securityConfig.securityQuestion,
            onDismiss = { showRecoveryDialog = false },
            onReset = { answer, newPin ->
                val resetOk = viewModel.resetPinWithRecovery(answer, newPin)
                if (resetOk) {
                    showRecoveryDialog = false
                    enteredPin = ""
                    errorMessage = null
                }
                resetOk
            }
        )
    }
}

@Composable
fun RecoveryPinDialog(
    question: String,
    onDismiss: () -> Unit,
    onReset: (String, String) -> Boolean
) {
    var answerInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var recoveryError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pemulihan PIN Keamanan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Jawab pertanyaan verifikasi keamanan di bawah ini untuk mengatur ulang PIN Anda.",
                    fontSize = 12.sp,
                    color = White70
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBackground),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Pertanyaan Keamanan:", fontSize = 10.sp, color = EmeraldLight)
                        Text(question, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }

                OutlinedTextField(
                    value = answerInput,
                    onValueChange = {
                        answerInput = it
                        recoveryError = null
                    },
                    label = { Text("Jawaban Keamanan") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldLight,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recovery_answer_field")
                )

                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            newPinInput = it
                            recoveryError = null
                        }
                    },
                    label = { Text("PIN Baru (6 Digit Angka)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldLight,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recovery_new_pin_field")
                )

                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            confirmPinInput = it
                            recoveryError = null
                        }
                    },
                    label = { Text("Konfirmasi PIN Baru") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = EmeraldLight,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("recovery_confirm_pin_field")
                )

                if (recoveryError != null) {
                    Text(
                        text = recoveryError ?: "",
                        fontSize = 12.sp,
                        color = ExpenseCoral,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answerInput.trim().isEmpty()) {
                        recoveryError = "Jawaban keamanan wajib diisi"
                        return@Button
                    }
                    if (newPinInput.length != 6) {
                        recoveryError = "PIN baru harus 6 digit angka"
                        return@Button
                    }
                    if (newPinInput != confirmPinInput) {
                        recoveryError = "Konfirmasi PIN tidak cocok"
                        return@Button
                    }

                    val ok = onReset(answerInput, newPinInput)
                    if (!ok) {
                        recoveryError = "Jawaban keamanan salah. Gagal mereset PIN."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("submit_recovery_button")
            ) {
                Text("Simpan PIN Baru", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_recovery_button")
            ) {
                Text("Batal", color = White70)
            }
        },
        modifier = Modifier.testTag("recovery_pin_dialog")
    )
}

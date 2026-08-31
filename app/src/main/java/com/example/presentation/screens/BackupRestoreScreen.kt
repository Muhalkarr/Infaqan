package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.auth.AuthStatus
import com.example.core.backup.BackupEngine
import com.example.core.backup.RestoreResult
import com.example.core.state.AmanahLedgerViewModel
import com.example.core.sync.SyncStateStatus
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseCoral
import com.example.ui.theme.GoldAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: AmanahLedgerViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }

    // Cloud Auth Form States
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var displayNameInput by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }

    // Export State
    var exportPassword by remember { mutableStateOf("") }
    var generatedBackupJson by remember { mutableStateOf("") }
    var generatedChecksum by remember { mutableStateOf("") }
    var exportCopied by remember { mutableStateOf(false) }

    // Import / Restore State
    var importJsonText by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }
    var restoreStatusMessage by remember { mutableStateOf<String?>(null) }
    var isRestoreSuccess by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pusat Sinkronisasi & Cadangan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Room SQLite Lokal • Cloud Firestore • SHA-256",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("backup_screen_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("☁️ Cloud Sync", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("💾 Cadangan JSON", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("📥 Pulihkan JSON", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: CLOUD SYNC & AUTH
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Account Profile Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (authState.status == AuthStatus.AUTHENTICATED) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = if (authState.status == AuthStatus.AUTHENTICATED) EmeraldLight else GoldAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = authState.user.displayName.ifBlank { "Kas Mukmin Syariah" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (authState.status == AuthStatus.AUTHENTICATED) {
                                                authState.user.email.ifBlank { "Akun Terverifikasi" }
                                            } else {
                                                "Mode Lokal / Offline (Belum Terhubung Cloud)"
                                            },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (authState.status == AuthStatus.AUTHENTICATED) {
                                        OutlinedButton(
                                            onClick = { viewModel.signOutAuth() },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Keluar", fontSize = 11.sp)
                                        }
                                    }
                                }

                                if (authState.status != AuthStatus.AUTHENTICATED) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = if (isSignUpMode) "Daftar Akun Cloud Amanah Baru:" else "Masuk ke Akun Cloud Amanah:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldLight
                                    )

                                    if (isSignUpMode) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = displayNameInput,
                                            onValueChange = { displayNameInput = it },
                                            label = { Text("Nama Lengkap / Nama Kas") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = emailInput,
                                        onValueChange = { emailInput = it },
                                        label = { Text("Email") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = passwordInput,
                                        onValueChange = { passwordInput = it },
                                        label = { Text("Kata Sandi (min. 6 karakter)") },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    if (!authState.errorMessage.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = authState.errorMessage!!,
                                            fontSize = 11.sp,
                                            color = ExpenseCoral
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (isSignUpMode) {
                                                    viewModel.signUpWithEmail(emailInput, passwordInput, displayNameInput)
                                                } else {
                                                    viewModel.signInWithEmail(emailInput, passwordInput)
                                                }
                                            },
                                            enabled = authState.status != AuthStatus.AUTHENTICATING,
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (authState.status == AuthStatus.AUTHENTICATING) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            } else {
                                                Text(if (isSignUpMode) "Daftar Akun" else "Masuk", fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.signInWithGoogle()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Google Sign-In", fontSize = 12.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(
                                        onClick = { isSignUpMode = !isSignUpMode },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(
                                            text = if (isSignUpMode) "Sudah punya akun? Masuk di sini" else "Belum punya akun? Daftar gratis",
                                            fontSize = 12.sp,
                                            color = GoldAccent
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Room & Cloud Sync Status Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sinkronisasi Room & Firestore",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Status Sinkronisasi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = when (syncState.status) {
                                            SyncStateStatus.SYNCING -> "Sedang Menyinkronkan..."
                                            SyncStateStatus.SUCCESS -> "Tersinkronisasi ✅"
                                            SyncStateStatus.ERROR -> "Gagal Sync ❌"
                                            SyncStateStatus.OFFLINE_ONLY -> "Offline Lokal 🔒"
                                            SyncStateStatus.IDLE -> "Siap"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (syncState.status == SyncStateStatus.SUCCESS) EmeraldLight else GoldAccent
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Terakhir Sinkron:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(syncState.lastSyncedTimeString, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Database Lokal Room:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${state.journalEntries.size} transaksi, ${state.wallets.size} kantong", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                if (!syncState.errorMessage.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Catatan: ${syncState.errorMessage}",
                                        fontSize = 11.sp,
                                        color = ExpenseCoral
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.triggerCloudSync() },
                                        enabled = syncState.status != SyncStateStatus.SYNCING,
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (syncState.status == SyncStateStatus.SYNCING) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Menyimpan...")
                                        } else {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Cadangkan ke Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.restoreDataFromCloud() },
                                        enabled = syncState.status != SyncStateStatus.SYNCING,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pulihkan dari Cloud", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: EXPORT JSON
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Security,
                                            contentDescription = null,
                                            tint = EmeraldLight,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Cadangan Data Offline Mandiri",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Seluruh data transaksi, mutasi kantong & aturan tersimpan lokal.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Jumlah Transaksi Jurnal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${state.journalEntries.size} transaksi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Jumlah Kantong Kas:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${state.wallets.size} kantong", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Target Tabungan Ibadah:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${state.ibadahGoals.size} program", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Password Encryption
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Kunci Enkripsi Cadangan (Opsional):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Jika diisi, berkas JSON akan dilindungi dengan sandi rahasia.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = exportPassword,
                                    onValueChange = { exportPassword = it },
                                    label = { Text("Kata Sandi Enkripsi Cadangan") },
                                    placeholder = { Text("Kosongkan jika tanpa sandi") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("export_password_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val backupJson = viewModel.createBackupPackage(exportPassword)
                                        generatedBackupJson = backupJson
                                        val pkg = BackupEngine.parseBackupPackage(backupJson)
                                        generatedChecksum = pkg?.checksumSha256 ?: "N/A"
                                        exportCopied = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("generate_backup_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Buat Berkas Cadangan JSON", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        AnimatedVisibility(visible = generatedBackupJson.isNotBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldLight)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Cadangan Berhasil Dibuat!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = EmeraldLight
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "SHA-256 Checksum: $generatedChecksum",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = GoldAccent
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Amanah Ledger Backup JSON", generatedBackupJson)
                                                clipboard.setPrimaryClip(clip)
                                                exportCopied = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("copy_backup_json_button")
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (exportCopied) "Tersalin!" else "Salin JSON", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                val sendIntent = android.content.Intent().apply {
                                                    action = android.content.Intent.ACTION_SEND
                                                    putExtra(android.content.Intent.EXTRA_TEXT, generatedBackupJson)
                                                    type = "application/json"
                                                }
                                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Bagikan Berkas Cadangan Amanah")
                                                context.startActivity(shareIntent)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("share_backup_json_button")
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Bagikan", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: RESTORE JSON
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Restore, contentDescription = null, tint = GoldAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Tempel Berkas Cadangan JSON",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Tempel teks cadangan JSON yang telah diekspor sebelumnya untuk memulihkan seluruh data.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = importJsonText,
                                    onValueChange = { importJsonText = it },
                                    label = { Text("Teks Cadangan JSON") },
                                    placeholder = { Text("{\"version\": 1, \"exportTimestamp\": ... }") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .testTag("import_json_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            importJsonText = clip.getItemAt(0).text.toString()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tempel dari Clipboard", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = importPassword,
                                    onValueChange = { importPassword = it },
                                    label = { Text("Kata Sandi Cadangan (Jika Ada)") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("import_password_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (importJsonText.isNotBlank()) {
                                            showRestoreConfirmDialog = true
                                        }
                                    },
                                    enabled = importJsonText.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("confirm_restore_button")
                                ) {
                                    Icon(Icons.Default.Restore, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mulai Pemulihan Data", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        restoreStatusMessage?.let { msg ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isRestoreSuccess) EmeraldPrimary.copy(alpha = 0.15f) else ExpenseCoral.copy(alpha = 0.15f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isRestoreSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isRestoreSuccess) EmeraldLight else ExpenseCoral
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = msg,
                                        fontSize = 12.sp,
                                        color = if (isRestoreSuccess) EmeraldLight else ExpenseCoral,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konfirmasi Pemulihan", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Pemulihan berkas cadangan akan memperbarui catatan buku kas, kantong rekening, dan rencana ibadah dengan data dari cadangan. Pastikan data cadangan valid.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = viewModel.restoreBackupPackage(importJsonText, importPassword)
                        when (res) {
                            is RestoreResult.Success -> {
                                isRestoreSuccess = true
                                restoreStatusMessage = "Sukses memulihkan ${res.restoredEntries.size} transaksi buku kas & ${res.restoredWallets.size} kantong rekening."
                            }
                            is RestoreResult.Failure -> {
                                isRestoreSuccess = false
                                restoreStatusMessage = res.errorMessage
                            }
                        }
                        showRestoreConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Lanjutkan Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

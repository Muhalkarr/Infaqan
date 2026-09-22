with open('README.md', 'r') as f:
    content = f.read()

# Replace the "Struktur Direktori Proyek" section with a more detailed one.
old_structure = """---
## 📂 Struktur Direktori Proyek (Project Structure)
Berikut adalah panduan direktori untuk memudahkan Anda melakukan navigasi kode:
```text
app/src/main/java/com/example/
│
├── core/                       # ⚙️ Utilitas Core & Infrastruktur
│   ├── ext/                    # Extension functions (misal: String formatter, Date parser)
│   ├── state/                  # Kelas utilitas untuk UI State (Error, Success, Loading)
│   └── utils/                  # Helper classes (Kalkulator Zakat, Kalender Hijriah)
│
├── data/                       # 💾 Layer Data (Database & Penyimpanan)
│   ├── local/                  # Konfigurasi Room Database
│   │   ├── dao/                # Data Access Objects (Query SQL)
│   │   └── entity/             # Tabel Database (TransactionEntity, WalletEntity, dll)
│   └── repository/             # Implementasi Repository
│
├── domain/                     # 🧠 Layer Domain (Logika Bisnis)
│   ├── model/                  # Data class murni (UI Models)
│   ├── repository/             # Interface kontrak repository
│   └── usecase/                # Spesifik bisnis logik (Opsional, untuk kompleksitas tinggi)
│
└── presentation/               # 📱 Layer Presentasi (UI - Jetpack Compose)
    ├── components/             # Komponen UI Reusable (Chart, Card, Buttons, Dialogs)
    ├── navigation/             # Definisi Rute (NavHost, Routes)
    ├── screens/                # Layar utama aplikasi (Tiap layar berisi Screen.kt & ViewModel.kt)
    │   ├── dashboard/          # DashboardScreen.kt, DashboardViewModel.kt
    │   ├── add_transaction/    # Layar tambah transaksi
    │   └── ...                 # Layar lainnya (Zakat, Analytics, Settings, dll)
    └── theme/                  # 🎨 Material 3 Theming (Color.kt, Theme.kt, Type.kt)
```"""

new_structure = """---
## 📂 Struktur Direktori Proyek & Penjelasan Berkas (Codebase Overview)

Aplikasi Infaqan Syariah (Amanah Ledger) ini dirancang dengan struktur monolitik modular berlandaskan Clean Architecture dan state-management reaktif (Jetpack Compose + Coroutines/Flow). Berikut adalah penjelasan detail mengenai fungsi masing-masing berkas dan direktori utama di dalam proyek ini:

### 🚀 Entry Point (Titik Masuk Aplikasi)
- **`MainActivity.kt`**: Ini adalah "pintu gerbang" utama aplikasi Android. Bertugas menginisialisasi lingkungan aplikasi (WindowInsets, Biometrik/Keamanan), memanggil konfigurasi tema (`AmanahTheme`), dan memuat Composable Root (`AmanahMainApp`). Layar *Onboarding* juga ditahan (di-overlay) dari berkas ini.
- **`AmanahMainApp.kt`**: Pusat perakitan antarmuka utama. File ini membungkus keseluruhan struktur aplikasi (Scaffold utama), mendaftarkan *Sidebar* (`AppNavigationDrawer`), dan meletakkan `NavHost` yang mengatur perpindahan antar layar.

### 🧠 Core Module (`com/example/core/`)
Modul ini berisi seluruh aturan bisnis murni, manajemen status (State), pengolahan data lokal, sinkronisasi awan, utilitas keamanan, dan kalkulasi algoritmik. Di sinilah "otak" aplikasi berada.

#### `core/accounting/` (Mesin Akuntansi Jurnal)
- **`AmanahLedgerEngine.kt`**: Jantung sistem keuangan! Mengandung logika kalkulasi saldo aset harian, pembuatan laporan laba-rugi, dan validasi *Double-Entry Bookkeeping* (keseimbangan Debit dan Kredit).
- **`JournalModels.kt`**: Model data dasar yang mendefinisikan bentuk Transaksi, Garis Jurnal (Journal Line), serta tipe-tipe Buku Besar (Aset, Beban, Ekuitas, Kewajiban).

#### `core/database/` (Penyimpanan Lokal - Room)
- **`AmanahDatabase.kt`**: Konfigurasi inti Room SQLite Database (Tabel dan Versi).
- **`AmanahDao.kt`**: *Data Access Object*. Berisi seluruh kueri SQL/Room (seperti `SELECT * FROM`, `INSERT`, `UPDATE`) untuk mengambil dan menyimpan data secara lokal.
- **`AmanahEntities.kt`**: Representasi kolom dan tabel database (menggunakan anotasi `@Entity` dan `@PrimaryKey`).
- **`EntityMappers.kt`**: Utilitas untuk menerjemahkan objek tabel database (Entitas) menjadi objek aplikasi yang lebih mudah dibaca (Model), dan sebaliknya.

#### `core/repository/` (Penyedia Data Terpadu)
- **`AmanahRepository.kt`**: Lapisan perantara (Bumper) antara Database (DAO) dan ViewModels. Memastikan semua pengambilan data berjalan di jalurnya masing-masing (*Coroutine Dispatchers*) tanpa membebani antarmuka.

#### `core/state/` (Manajemen Status Ui)
- **`AmanahLedgerViewModel.kt`**: Satu-satunya ViewModel utama yang mengkoordinasikan dan menggabungkan data dari repository (Database, Datastore, Sinkronisasi Awan, Keamanan) untuk kemudian diekspos sebagai status reaktif (`StateFlow`) yang diserap oleh lapisan presentasi/layar UI.
- **`UiStateModels.kt`**: Kelas data *wrapper* (pembungkus) yang menyimpan keadaan terkini aplikasi (seperti: Status saldo, status kunci PIN, visibilitas nominal).

#### `core/datastore/` (Penyimpanan Preferensi Ringan)
- **`DataStoreManager.kt`**: Penyimpanan berbasis key-value yang menyimpan pengaturan aplikasi seperti (PIN Hash, status Onboarding, mode *Strict Budget*, preferensi Biometrik).

#### `core/sync/` & `core/auth/` & `core/backup/` (Integrasi Firebase & Keamanan Lanjut)
- **`FirestoreSyncEngine.kt`**: Mesin yang menangani proses unggah (Backup) dan unduh (Restore) status terenkripsi ke Cloud/Firebase.
- **`AmanahAuthManager.kt`**: Mengatur sesi akun (Sign In/Sign Out) dan Google Identity Services agar pencadangan tersimpan dengan aman pada kepemilikan masing-masing pengguna.
- **`BackupEngine.kt` & `EncryptionEngine.kt`**: Memastikan pencadangan berkas JSON yang diunduh ke ponsel terenkripsi dan terlindungi.

#### Paket Algoritma Spesifik (Domain Models)
- **`core/budget/`**: Mengelola aturan *Budgeting* (batas anggaran).
- **`core/ibadah/`**: Manajemen target poin pahala dan *streak* sedekah harian.
- **`core/qardh/`**: Logika Hutang-Piutang (Pencatatan pelunasan, termin jatuh tempo).
- **`core/wallet/`**: Logika Mutasi Multi-Rekening (Transfer antar kantong kas).
- **`core/pdf/FinancialPdfGenerator.kt`**: Mesin penulisan *Canvas* untuk mencetak laporan transaksi ke format berkas .PDF yang rapi, lengkap dengan penyesuaian tata letak tabel (Wrap text).

---

### 🗺️ Navigation Module (`com/example/navigation/`)
- **`AmanahNavigation.kt`**: Daftar Rute/Peta jalan aplikasi. Berisi rute *String Constants* yang merepresentasikan setiap layar (seperti `"dashboard"`, `"add_transaction"`).

---

### 🎨 Presentation Module (`com/example/presentation/`)
Modul ini bertugas semata-mata untuk merender bentuk visual aplikasi (Jetpack Compose). Tidak mengandung kueri database.

#### `presentation/components/` (Elemen Visual Reusable)
Berisi komponen kepingan *lego* yang bisa dipakai berulang kali di berbagai layar:
- **`AmanahNumpad.kt`**: Papan ketik angka kustom (seperti kalkulator ATM).
- **`AppNavigationDrawer.kt`**: Panel laci menu navigasi (Sidebar) di sebelah kiri layar.

#### `presentation/screens/` (Halaman/Layar Utama Aplikasi)
Berikut adalah daftar beberapa berkas penting:
- **`OnboardingScreen.kt`**: Halaman pemaparan/presentasi bagi pengguna yang pertama kali menginstal aplikasi (berisi edukasi Privasi dan Data Dummy).
- **`DashboardScreen.kt`**: Halaman beranda utama yang menampilkan ikhtisar saldo, *progress bar* anggaran, peringatan, dan ringkasan mutasi terakhir.
- **`AddTransactionScreen.kt`**: Formulir pencatatan pemasukan/pengeluaran baru yang pintar (Dilengkapi dengan *BottomSheet Numpad* kustom dan dialog peringatan *Over-budget*).
- **`MultiWalletScreen.kt`**: Halaman Manajemen Kantong & Rekening, menampilkan daftar aset likuid.
- **`QardhScreen.kt`**: Layar pencatatan janji Hutang/Piutang. Menampilkan indikator alarm jatuh tempo dan histori pelunasan parsial.
- **`FaraidhScreen.kt`**: Layar Kalkulator Hukum Waris.
- **`ZakatHubScreen.kt`**: Layar Kalkulator Zakat Mal dan pengingat Haul & Nisab Emas.
- **`AnalyticsScreen.kt` & `ExportReportScreen.kt`**: Menampilkan Diagram Statistik visual (*Pie Chart*) dan fungsi ekspor ke .PDF.
- **`SettingsScreen.kt` & `CentralSettingsScreen.kt`**: Layar Pengaturan Utama dan pengaturan spesifik (Pencadangan, Penghapusan Data Dummy, Akses PIN).
- **`AppLockScreen.kt`**: Tampilan "Layar Kunci" yang menahan akses pengguna sebelum memasukkan PIN keamanan atau sidik jari biometrik.

#### `presentation/theme/` (Desain & Warna)
- **`Color.kt`**: Palet daftar warna kustom (misal: `EmeraldPrimary`, `CoralRed`, `GoldAccent`).
- **`Theme.kt`**: Menerapkan pengaturan *Dark Mode/Light Mode* secara dinamis ke seluruh komponen Material 3.
- **`Type.kt`**: Mengatur ukuran teks, jenis fon (*Typography*)."""

if old_structure in content:
    content = content.replace(old_structure, new_structure)
else:
    # If the exact old structure isn't found, we append the new structure to the end
    content += "\n\n" + new_structure

with open('README.md', 'w') as f:
    f.write(content)


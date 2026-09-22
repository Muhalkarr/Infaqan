# 🕌 Islamic Finance & Ibadah Tracker (Baitul Mal Pribadi)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4.svg?logo=android)
![Material 3](https://img.shields.io/badge/Material%20Design-3-F6B26B.svg?logo=materialdesign)
![Room Database](https://img.shields.io/badge/Room-Database-4CAF50.svg?logo=sqlite)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-purple.svg)

Aplikasi Android modern, *offline-first*, komprehensif yang dirancang untuk membantu pengguna muslim dalam mengelola keuangan pribadi secara syariah sekaligus melacak rutinitas ibadah harian. Aplikasi ini tidak hanya berfungsi sebagai pencatat pengeluaran/pemasukan, tetapi juga sebagai asisten pencapaian spiritual (Gamifikasi Sedekah, Cek Nisab/Haul Zakat, dan Manajemen Infaq).

Aplikasi ini dibangun menggunakan kaidah pengembangan perangkat lunak modern (**Clean Architecture** & **MVVM**) agar sangat *developer-friendly*, mudah dibaca, dikembangkan (scale), dan dimodifikasi (customize).

---

## 🌟 Fitur Utama (Features)

### 💰 1. Manajemen Keuangan Syariah
*   **Pencatatan Transaksi:** Catat pemasukan dan pengeluaran dengan kategori spesifik.
*   **Virtual Infaq Vault:** Dompet virtual khusus untuk memisahkan dana titipan atau dana yang dialokasikan khusus untuk disedekahkan.
*   **Distribusi Asnaf:** Penyaluran infaq yang terorganisir ke berbagai asnaf (Fakir, Miskin, Amil, Muallaf, dll).
*   **Pemantauan Anggaran (Budgeting):** Pantau batas pengeluaran bulanan dengan peringatan visual (warna) saat melebihi anggaran (*Over-budget Warning*).

### 🕋 2. Alat Bantu Ibadah & Zakat
*   **Zakat & Nisab Tracker:** Kalkulasi otomatis apakah total kekayaan sudah mencapai batas *Nisab* dan fitur pengingat *Haul* tahunan.
*   **Penanggalan Hijriah Terintegrasi:** Setiap transaksi dicatat dengan sinkronisasi kalender Masehi dan Hijriah secara bersamaan.
*   **Sedekah Subuh & Ibadah Goals:** Pelacakan *streak* (hari berturut-turut) untuk rutinitas Sedekah Subuh dan pencapaian lencana (*badges*) ibadah harian.

### 🎨 3. UI/UX Modern & Aksesibilitas
*   **Material Design 3 (M3):** Menggunakan *Dynamic Color*, *Typography*, dan komponen M3 modern.
*   **Dark/Light Mode:** Dukungan tema gelap dan terang secara otomatis mengikuti sistem OS.
*   **Privasi Saldo (Masking):** Fitur *hide/show* saldo (Rp ••••••••) hanya dengan satu sentuhan.
*   **Aksesibilitas (A11y):** Target sentuhan (*touch targets*) disesuaikan pada ukuran standar minimum 48dp.
*   **Tactile Haptic Feedback:** Umpan balik getaran mikro (*haptic*) saat menekan kalkulator numpad, tombol aksi simpan, dan efek getar ritmis khusus saat target tabungan ibadah tercapai (100%).

### ⚖️ 4. Kalkulator Hukum Waris Islam (Faraidh Engine)
*   **Penetapan Porsi Syar\'i:** Pembagian tirkah otomatis mengacu pada ketentuan Al-Qur\'an Surah An-Nisa ayat 11, 12, dan 176.
*   **Kaidah Ashabul Furudh & Ashabah:** Mendukung perhitungan porsi pasti (1/2, 1/3, 1/4, 2/3, 1/6, 1/8) serta sisa harta untuk ahli waris ashabah (bin-nafs, bil-ghair, ma\'al-ghair).
*   **Penyelesaian Hijab, Awal & Radd:** Algoritma cerdas yang menghitung penghalangan ahli waris (*mahjub*), penyesuaian penyebut jika total porsi melebihi harta (*Awal*), serta pengembalian kelebihan harta secara adil (*Radd*).
*   **Pembersihan Tirkah:** Kalkulasi tirkah bersih setelah dikurangi hak mendahului (biaya tajhiz pengurusan jenazah, pelunasan hutang mu\'amalah, dan wasiat syar\'i maksimal sepertiga).

### 🛡️ 5. Pembersihan Harta & Tathhir (Fatwa DSN-MUI No. 123/2018)
*   **Karantina Dana Syubhat:** Akun terisolasi khusus (`acc_syubhat`) untuk mencatat penerimaan non-halal seperti bunga bank konvensional atau kasab yang diragukan.
*   **Penyaluran Kemaslahatan Sosial:** Fasilitas eksekusi penyaluran dana 100% untuk fasilitas umum (sarana MCK & sanitasi, perbaikan jembatan/jalan umum rusak, pengadaan air bersih dhuafa) dengan prinsip *bara\'ah adz-dzimmah* (pelepasan tanggung jawab) tanpa klaim pahala sedekah.
*   **Audit Trail & Tanda Bukti:** Setiap tindakan pembersihan tercatat di buku besar akuntansi ganda dan menerbitkan nomor kuitansi resmi.

### 📱 6. Integrasi Sistem Android (Shortcuts, Widgets & Background Worker)
*   **Android App Shortcuts:** Akses cepat langsung dari ikon aplikasi di beranda (Home Screen) untuk:
    *   *Catat Beban*: Akses cepat form pengeluaran harian.
    *   *Catat Kasab*: Akses cepat pencatatan pemasukan halal.
    *   *Sedekah Subuh*: Pintasan instan setor fajar berkah.
    *   *Zakat Maal*: Buka kalkulator nisab dan haul zakat.
*   **App Widgets Interaktif:**
    *   *Widget Ringkasan Kas Syariah*: Pantau total saldo likuid (kas, bank, e-wallet) beserta tombol pintas transaksi.
    *   *Widget Sedekah Subuh*: Memantau streak rutinitas sedekah subuh harian.
    *   *Widget Ibadah Goals*: Memantau capaian target tabungan qurban dan umrah.
*   **Background Worker Hemat Daya (`WorkManager`):** Berjalan berkala dengan optimasi *Doze Mode* untuk memeriksa jatuh tempo hutang/piutang (Qardh), memeriksa ambang batas nisab & haul zakat, dan mengingatkan transaksi rutin tanpa menguras baterai.

---

## 🏗️ Arsitektur Aplikasi (Architecture)

Proyek ini sangat mengedepankan **pemisahan tanggung jawab (Separation of Concerns)** melalui pendekatan **Model-View-ViewModel (MVVM)** yang dipadukan dengan prinsip **Clean Architecture**.

1.  **Presentation Layer (View & ViewModel):**
    Menangani seluruh antarmuka (Jetpack Compose). Tidak ada logika bisnis di dalam UI. UI hanya mengamati (observe) `StateFlow` dari ViewModel dan mengirimkan *event* interaksi ke ViewModel.
2.  **Domain Layer (Use Cases / Bisnis Logika):**
    Layer independen yang berisi aturan bisnis (misal: aturan perhitungan Nisab Zakat, atau logika penambahan *streak* ibadah).
3.  **Data Layer (Repository & Data Source):**
    Menangani komunikasi dengan penyimpanan lokal (Room Database/SQLite) atau *DataStore* (Preferences).

---

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
```

---

## 🚀 Cara Instalasi & Menjalankan di Lokal (Getting Started)

### Prasyarat (Prerequisites)
*   **Android Studio:** Versi *Jellyfish*, *Koala*, atau yang lebih baru.
*   **JDK:** Java Development Kit versi 17 (disarankan bawaan Android Studio).
*   **Android SDK:** Minimal SDK API 26 (Android 8.0), Target SDK 34 (Android 14).

### Langkah-langkah (Steps to Run)
1.  **Clone Repositori:**
    ```bash
    git clone https://github.com/username/islamic-finance-tracker.git
    cd islamic-finance-tracker
    ```
2.  **Buka di Android Studio:**
    Pilih *File* > *Open* dan arahkan ke direktori proyek yang baru saja di-clone.
3.  **Sinkronisasi Proyek (Gradle Sync):**
    Tunggu Android Studio mengunduh seluruh dependensi (*library*) Gradle. (Lihat bilah bawah untuk progres).
4.  **Jalankan (Build & Run):**
    Pilih perangkat fisik (via USB Debugging) atau Emulator dari menu atas, lalu klik ikon **Run (▶)** atau tekan `Shift + F10`.

---

## 🛠️ Panduan Pengembangan (Developer Guide)

### 1. Bagaimana Cara Menambah Layar (Screen) Baru?
1.  Buat *package* baru di dalam `presentation/screens/` (misal: `presentation/screens/profile/`).
2.  Buat file `ProfileScreen.kt` dan buat fungsi `@Composable` dasar.
3.  Buat file `ProfileViewModel.kt` jika membutuhkan pengolahan data atau manajemen state.
4.  Buka file Navigasi Utama (misal: `AppNavigation.kt` atau di `MainActivity.kt`), lalu daftarkan rute baru Anda di dalam `NavHost`.

### 2. Bagaimana Cara Mengubah Tema & Warna (Theming)?
Aplikasi ini sudah mengimplementasikan **Material Design 3**. Jangan melakukan *hardcode* warna heksadesimal langsung di komponen.
*   Buka `presentation/theme/Color.kt` untuk melihat definisi palet warna murni.
*   Buka `presentation/theme/Theme.kt` untuk menyesuaikan pemetaan skema warna terang (`LightColorScheme`) dan gelap (`DarkColorScheme`).
*   Mengubah warna *primary* di `Theme.kt` akan otomatis memperbarui warna tombol, *header*, dan status di seluruh aplikasi.

### 3. Bagaimana Cara Menambah/Memodifikasi Tabel Database (Room)?
1.  Buka `data/local/entity/` dan tambahkan/ubah `@Entity` *data class*.
2.  Buka `data/local/dao/` dan perbarui operasi CRUD SQL jika perlu.
3.  **PENTING (Migrasi):** Jika Anda sudah merilis aplikasi atau ada data yang tersimpan, buka konfigurasi `AppDatabase.kt`, tingkatkan `version = X + 1`, dan tuliskan strategi `Migration` agar aplikasi pengguna tidak *crash* saat diperbarui.

### 4. Bagaimana Membaca/Mengambil Data dari Database ke UI?
Alur Data (Data Flow):
*   **DAO** mengembalikan `Flow<List<DataEntity>>`.
*   **Repository** memetakan (*map*) `DataEntity` ke `DomainModel`.
*   **ViewModel** menampung (*collect*) aliran tersebut menggunakan `viewModelScope`, mengubahnya menjadi `StateFlow<UIState>`.
*   **UI (Compose)** mengamati (*observe*) dengan `uiState.collectAsStateWithLifecycle()` dan secara reaktif merender komponen.

---

## 📦 Dependensi Utama (Libraries Used)

*   **[Jetpack Compose](https://developer.android.com/jetpack/compose):** Toolkit modern untuk membangun UI native deklaratif.
*   **[Jetpack Room](https://developer.android.com/training/data-storage/room):** Abstraksi SQLite yang andal dan aman.
*   **[Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-overview.html):** Manajemen operasi asinkron (pemrosesan di *background thread* yang mulus tanpa mengunci UI).
*   **[Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/nav/tutorial):** Perpindahan antar halaman secara dinamis.
*   **[Material 3](https://m3.material.io/):** Standar sistem desain visual terbaru dari Google.

---

## 🤝 Kontribusi (Contributing)

Kami menyambut sangat baik kontribusi dari komunitas! Jika Anda ingin menambahkan fitur (seperti notifikasi adzan, pelacakan puasa, laporan PDF, dll) atau memperbaiki *bug*:
1.  Lakukan *Fork* proyek ini.
2.  Buat *branch* fitur Anda (`git checkout -b feature/FiturKerenSaya`).
3.  Lakukan *commit* perubahan Anda (`git commit -m 'Menambahkan Fitur Keren'`).
4.  Lakukan *push* ke branch tersebut (`git push origin feature/FiturKerenSaya`).
5.  Buka *Pull Request* (PR).

## 📄 Lisensi (License)

Proyek ini dilisensikan di bawah [MIT License](LICENSE). Anda bebas untuk menggunakan, menyalin, memodifikasi, menggabungkan, menerbitkan, mendistribusikan, dan menjual salinan perangkat lunak ini, dengan syarat pemberitahuan hak cipta dan izin ini disertakan dalam semua salinan.

> *"Sebaik-baik manusia adalah yang paling bermanfaat bagi manusia lainnya."*


---
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
- **`core/budget/`**: Mengelola aturan *Budgeting* (batas anggaran dan peringatan over-budget).
- **`core/ibadah/`**: Manajemen target poin pahala dan *streak* sedekah harian.
- **`core/qardh/`**: Logika Hutang-Piutang (Pencatatan pelunasan, termin jatuh tempo, status lunas).
- **`core/wallet/`**: Logika Mutasi Multi-Rekening (Transfer antar kantong kas/bank/e-wallet).
- **`core/faraidh/`**: Mesin kalkulasi waris Islam komprehensif (`FaraidhCalculator.kt`, `FaraidhModels.kt`) yang menghitung hak Ashabul Furudh, Ashabah, Hijab/Mahjub, Awal, dan Radd.
- **`core/zakat/`**: Kalkulator Hisab Zakat Maal (Nisab 85g Emas), Zakat Fitrah keluarga, Zakat Perniagaan, dan Zakat Profesi/Penghasilan.
- **`core/infaq/`**: Manajemen dana Virtual Infaq Vault dan riwayat penyaluran ke 8 Asnaf mustahiq (`InfaqDistributionRecord.kt`, `InfaqRule.kt`).
- **`core/worker/`**: `AmanahReminderWorker.kt` (WorkManager periodik hemat baterai untuk notifikasi jatuh tempo Qardh, haul Zakat Maal, dan jadwal transaksi rutin).
- **`core/ui/`**: `AmanahHapticEngine.kt` (Mesin getaran sentuhan taktil halus pada keyboard numpad dan selebrasi pencapaian target).
- **`core/receipt/`**: `ReceiptAttachment.kt`, `ReceiptType.kt` (Pengelolaan bukti nota struk digital dan integrasi FileProvider).
- **`core/debug/`**: `AppDebugLogger.kt` (Log audit diagnostik status buku besar dan rekaman event).
- **`core/report/` & `core/pdf/`**: `FinancialPdfGenerator.kt` & `DocumentExporter.kt` (Pencetakan laporan Laba-Rugi, Neraca Keuangan Syariah, Buku Besar Jurnal, dan ekspor CSV/JSON).

#### `ui/widget/` (Android App Widgets Home Screen)
- **`CashSummaryWidgetProvider.kt`**: Widget beranda untuk memantau ringkasan saldo kas & aset likuid syariah beserta tombol pintas transaksi.
- **`SedekahSubuhWidgetProvider.kt`**: Widget beranda untuk pencatatan instan sedekah subuh harian dan pemantauan streak.
- **`IbadahGoalWidgetProvider.kt`**: Widget beranda untuk ringkasan progres tabungan qurban dan ibadah haji/umrah.

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
- **`FaraidhScreen.kt`**: Layar Kalkulator Hukum Waris Islam interaktif dengan rincian Ashabul Furudh, Ashabah, Awal, dan Radd.
- **`ZakatHubScreen.kt`**: Layar sentral Zakat, Infaq & Tathhir (Zakat Maal, Zakat Fitrah keluarga, Zakat Perniagaan, Penyaluran Infaq Asnaf, dan Tab Pembersihan Dana Non-Halal sesuai Fatwa DSN-MUI No. 123/2018).
- **`SedekahSubuhScreen.kt`**: Layar khusus pencatatan sedekah subuh berkah fajar dengan pelacakan streak harian dan kutipan hadits.
- **`IbadahGoalsScreen.kt`**: Layar target tabungan ibadah (Tabungan Qurban, Umrah, Haji, Wakaf) dengan visualisasi progres dan selebrasi capaian.
- **`RecurringTransactionsScreen.kt`**: Layar automasi transaksi rutin berkala (infaq bulanan, sewa, tagihan listrik).
- **`RulingsScreen.kt`**: Pusat ensiklopedia kaidah fikih muamalah dan fatwa-fatwa DSN-MUI terkait keuangan pribadi.
- **`ReceiptViewerDialog.kt`**: Dialog penampil berkas lampiran kuitansi/nota transaksi digital dengan fitur zoom dan rotasi.
- **`AnalyticsScreen.kt` & `ExportReportScreen.kt`**: Menampilkan Diagram Statistik visual (*Pie Chart*) dan fungsi ekspor ke .PDF / .CSV.
- **`SettingsScreen.kt` & `CentralSettingsScreen.kt`**: Layar Pengaturan Utama dan pengaturan spesifik (Pencadangan, Penghapusan Data Dummy, Akses PIN).
- **`AppLockScreen.kt`**: Tampilan "Layar Kunci" yang menahan akses pengguna sebelum memasukkan PIN keamanan atau sidik jari biometrik.

#### `presentation/theme/` (Desain & Warna)
- **`Color.kt`**: Palet daftar warna kustom (misal: `EmeraldPrimary`, `CoralRed`, `GoldAccent`).
- **`Theme.kt`**: Menerapkan pengaturan *Dark Mode/Light Mode* secara dinamis ke seluruh komponen Material 3.
- **`Type.kt`**: Mengatur ukuran teks, jenis fon (*Typography*).

---

## 📜 Landasan Fikih & Referensi Syariah (Sharia Governance)

Seluruh logika perhitungan dan arsitektur keuangan di dalam aplikasi dibangun berdasarkan rujukan terpercaya ulama dan otoritas syariah resmi:

1.  **Akuntansi Syariah & Pembukuan Ganda (Double-Entry Bookkeeping):**
    *   Mengacu pada standar **AAOIFI** (*Accounting and Auditing Organization for Islamic Financial Institutions*) untuk pengakuan aset, kasab (pendapatan halal), beban, dan kewajiban.
2.  **Pembersihan Dana Non-Halal (Tathhir):**
    *   **Fatwa DSN-MUI No. 123/DSN-MUI/XI/2018** tentang *Pemanfaatan Dana Sosial / Non-Halal*: Bunga bank konvensional atau pendapatan non-halal wajib disalurkan 100% untuk fasilitas sosial dan sarana kemaslahatan umum tanpa niat mencari pahala sedekah.
3.  **Kaidah Zakat Maal & Zakat Profesi:**
    *   Nisab Emas 85 gram untuk Zakat Maal (Surah At-Taubah: 34 & 103, Hadits Riwayat Abu Dawud).
    *   **Fatwa MUI No. 3 Tahun 2003** tentang *Zakat Penghasilan*: Nisab setara 524 kg beras / 85 gram emas, dikeluarkan sebesar 2.5% saat menerima pendapatan.
    *   Kaidah Haul 1 tahun Hijriah (354 hari).
4.  **Hukum Kewarisan Islam (Faraidh):**
    *   Surah An-Nisa ayat 11, 12, dan 176, didukung oleh kaidah Fiqh Mawarith dan Kompilasi Hukum Islam (KHI) Buku II.
    *   Prioritas penyelesaian hak atas tirkah: (1) Biaya Tajhiz Jenazah, (2) Pelunasan Hutang Muamalah, (3) Wasiat Syar\'i (maks. 1/3), (4) Pembagian ke Ahli Waris.
5.  **Penyaluran Infaq 8 Asnaf:**
    *   Surah At-Taubah ayat 60 (Fakir, Miskin, Amil, Muallaf, Riqab, Gharimin, Fisabilillah, Ibnu Sabil).

---

## 🔒 Keamanan, Privasi & Kepatuhan Android (Security & Privacy)

*   **Penyimpanan Lokal Murni (Offline-First):** Seluruh data transaksi, rekening, dan mutasi tersimpan secara privat di dalam perangkat menggunakan basis data Room SQLite terenkripsi.
*   **Keamanan Biometrik & PIN:** Dilengkapi penguncian ganda menggunakan `BiometricPrompt` resmi Android dan hashing PIN aman melalui Jetpack DataStore.
*   **Android Photo Picker & FileProvider:** Pengelolaan bukti nota kuitansi menggunakan `FileProvider` internal yang aman tanpa meminta izin berbahaya (*zero-permission* `READ_EXTERNAL_STORAGE`).
*   **Battery-Friendly Background Processing:** Menggunakan `WorkManager` yang patuh pada *Doze Mode* dan batasan daya sistem untuk pengingat jatuh tempo tanpa memboroskan baterai.

---

## 🧪 Pengujian & Jaminan Mutu (Testing & QA)

Aplikasi dilengkapi pengujian unit lokal untuk memverifikasi keakuratan kalkulasi syariah:

*   **Menjalankan Unit Test (Robolectric):**
    ```bash
    gradle :app:testDebugUnitTest
    ```
    Memverifikasi Critical User Journeys (CUJ), keseimbangan jurnal debit-kredit, hisab zakat maal, dan pembagian waris faraidh.
*   **Verifikasi Kompilasi Kode:**
    Pastikan seluruh berkas terkompilasi bersih tanpa *warning* fatal sebelum pembuatan paket APK/AAB rilis.
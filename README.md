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

# BekalKu
**Aplikasi Perencanaan dan Pengelolaan Menu Bekal Harian Berbasis Android**

UAS Mobile Programming — Muhammad Raihann Azhari (NIM 240170129)

Kotlin • XML + Material Design 3 • MVVM (ViewModel + Repository) • SQLite • ViewPager2 + BottomNavigationView

## Cara menjalankan
1. Buka folder ini di Android Studio (versi yang mendukung AGP 9.4.1), tunggu Gradle sync selesai.
2. Jalankan di emulator / HP (minSdk 24).
3. Daftar akun baru di layar Registrasi, lalu mulai menambah menu bekal.

## Fitur & pemetaan checklist soal
| No | Fitur wajib | Implementasi |
|----|-------------|--------------|
| 1 | Autentikasi | **Firebase Authentication** (email/password) bila `google-services.json` terpasang; bila tidak, otomatis memakai akun lokal (PBKDF2 + salt) agar app tetap jalan. `LoginActivity`, `RegisterActivity`, `AuthViewModel`, sesi tersimpan, logout di tab Profil, pesan error jelas (termasuk tanpa internet). |
| 2 | CRUD + persisten | Entitas `BekalMenu` (11 atribut) di SQLite (`BekalDbHelper`, `MenuRepository`). Create/Update di `FormActivity`, Read via RecyclerView (`MenuAdapter`), Delete dengan dialog konfirmasi. |
| 3 | Pencarian/filter | Tab **Menu**: pencarian real-time (nama/bahan) + filter kategori; pesan "Menu tidak ditemukan". Tab **Rencana**: filter per hari. |
| 4 | Detail & navigasi | 5 layar: Login, Register, Main (5 tab), Detail, Form. Navigasi `BottomNavigationView` + `ViewPager2` (bisa swipe, transisi halus). Data aman saat layar diputar (ViewModel + saved state). |
| 5 | Integrasi layanan/perangkat | (c) **API eksternal** TheMealDB: tombol 💡 di tab Menu membuka *Inspirasi Bekal* (resep acak, bisa dipakai sebagai menu baru) dengan state loading & pesan error jaringan/server, (a) Kamera & galeri (Photo Picker + FileProvider) untuk foto menu, (b) notifikasi lokal terjadwal harian (`AlarmManager`) dengan izin `POST_NOTIFICATIONS` diminta saat runtime. Jika izin ditolak aplikasi tetap normal. |
| 6 | UI/UX Material | Material 3, palet oranye, ConstraintLayout/dp/sp, state loading / empty / error (`view_state.xml`), semua teks di `strings.xml`. |
| 7 | Validasi & stabilitas | Semua form tervalidasi (kosong, format email, panjang, angka & rentang). Kegagalan jaringan (login Firebase & API inspirasi) menampilkan pesan jelas + tombol "Coba Lagi", bukan layar kosong. Error DB juga ditangani. |
| 8 | Signed APK | Lihat bagian di bawah. |

Bonus: **dark mode** (Sistem/Terang/Gelap, diatur di tab Profil), penjadwalan ulang pengingat setelah reboot (`BootReceiver`), data terpisah per pengguna, kompresi foto otomatis.

## Mengaktifkan Firebase Authentication
1. Buat project di [console.firebase.google.com](https://console.firebase.google.com) → tambahkan aplikasi Android dengan package name `com.unimal.uas_240170129_muhammad_raihann_azhari` (tambahkan SHA-1 debug/release bila diminta).
2. Menu **Build > Authentication > Sign-in method** → aktifkan **Email/Password**.
3. Unduh `google-services.json` dan letakkan di folder `app/` (sejajar `app/build.gradle.kts`), lalu **Sync Gradle**.
4. Selesai. Tanpa file itu aplikasi memakai autentikasi lokal. Catatan: akun lokal dan akun Firebase terpisah.

## Struktur proyek
```
com.unimal.uas_240170129_muhammad_raihann_azhari
├── data/        model, local (SQLiteOpenHelper), remote (MealApi), repository
├── ui/
│   ├── auth/    Login, Register, AuthViewModel
│   ├── main/    MainActivity, MenuViewModel, tabs/ (Home, Menu, Planner, Stats, Profile)
│   ├── inspiration/  Inspirasi Bekal (API TheMealDB)
│   ├── detail/  DetailActivity, DetailViewModel
│   ├── form/    FormActivity, FormViewModel
│   └── common/  MenuAdapter, StateController, UiState
└── util/        SessionManager, ImageUtils, ReminderScheduler/Receiver, dll.
```

## Build signed APK
1. Android Studio → **Build > Generate Signed App Bundle / APK > APK**, buat keystore baru, pilih `release`.
2. Atau lewat command line: buat file `keystore.properties` di root proyek (sudah di-`.gitignore`):
   ```
   storeFile=bekalku.jks
   storePassword=******
   keyAlias=bekalku
   keyPassword=******
   ```
   lalu jalankan `./gradlew assembleRelease` → APK di `app/build/outputs/apk/release/`.
3. Upload APK ke **GitHub Releases** (atau Firebase App Distribution) dan masukkan link ke lembar identitas.

# BAREZGO Downloader (Android starter project)

## Isi versi 1.0
- Kolom tautan dan tombol Tempel Link dari clipboard
- Preview URL di WebView
- Pilihan label kualitas: Otomatis, 360p, 480p, 720p, 1080p
- Download melalui Android DownloadManager ke `Download/BAREZGO`

## Batasan penting
Versi ini mengunduh URL file video langsung yang dapat diakses publik/diizinkan (misalnya MP4). Ia bukan pengunduh universal untuk semua halaman situs. Banyak situs memakai player berbasis streaming, HLS/DASH, autentikasi, token sementara, DRM, atau pembatasan hak cipta. Aplikasi tidak melewati proteksi tersebut. Pemilihan resolusi belum mengekstrak pilihan kualitas dari halaman; resolusi harus sudah tersedia sebagai URL langsung dari sumber.

## Membuka di Android Studio
1. Ekstrak ZIP.
2. Buka folder `BAREZGO-Android` di Android Studio.
3. Tunggu Gradle Sync dan instal Android SDK Platform 35 jika diminta.
4. Jalankan ke perangkat Android atau emulator.
5. Untuk membuat APK: Build > Build Bundle(s) / APK(s) > Build APK(s).

Gunakan hanya untuk video milik sendiri atau yang memang diizinkan untuk diunduh.

# 🎵 Music Sheet Builder

Aplikasi web modern untuk membuat, mengedit, dan mengelola partitur musik piano. Dibangun dengan Kotlin, Spring Boot, dan Vaadin.

## ✨ Fitur

### Fitur Utama

- 📝 **Parser Notasi Berbasis Teks** - Konversi notasi teks sederhana menjadi partitur musik terstruktur
- 👁 **Live Preview** - Pratinjau real-time partitur musik saat Anda mengetik
- 🎵 **Audio Preview** - Dengarkan partitur musik Anda dengan pemutaran audio bawaan
- 📄 **Ekspor PDF** - Ekspor partitur Anda ke dokumen PDF yang profesional
- 💾 **Manajemen Perpustakaan** - Simpan dan atur partitur musik Anda dalam database
- 🎼 **Transpose** - Transposisi seluruh partitur naik atau turun berdasarkan semitone
- 🔍 **Pencarian** - Temukan partitur Anda dengan cepat berdasarkan judul

### Fitur Teknis

- ✅ **Validasi** - Validasi otomatis untuk time signature dan jumlah beat
- 🎨 **UI Modern** - Antarmuka yang bersih dan responsif dibangun dengan Vaadin
- 🗄 **Penyimpanan Database** - Database H2 untuk penyimpanan persisten
- 📊 **Model Data Terstruktur** - Data class yang terorganisir dengan baik untuk representasi musik
- 🔧 **Penanganan Error** - Penanganan error yang komprehensif dan feedback pengguna

## 🚀 Memulai

### Prasyarat

**Opsi 1: Menggunakan Docker (Recommended)**
- Docker Desktop atau Docker Engine 20.10+
- Docker Compose 2.0+

**Opsi 2: Development Lokal**
- JDK 17 atau lebih tinggi
- Gradle (sudah termasuk via wrapper)

### Menjalankan dengan Docker 🐳

```bash
# Clone repository
git clone <url-repo-anda>
cd music-sheet-builder

# Build dan jalankan dengan Docker Compose
docker-compose up -d

# Atau gunakan script helper (Windows)
docker-run.bat

# Lihat logs
docker-compose logs -f

# Stop aplikasi
docker-compose down
```

Aplikasi akan berjalan di `http://localhost:8080`

📖 **Dokumentasi lengkap Docker**: Lihat [DOCKER.md](DOCKER.md)

### Menjalankan Lokal (Development)

```bash
# Clone repository
git clone <url-repo-anda>
cd music-sheet-builder

# Jalankan aplikasi
./gradlew bootRun

# Atau di Windows
gradlew.bat bootRun
```

Aplikasi akan berjalan di `http://localhost:8080`

### Build untuk Production

```bash
# Build JAR production
./gradlew build -Pvaadin.productionMode=true

# Jalankan JAR
java -jar build/libs/music-sheet-builder-0.0.1-SNAPSHOT.jar

# Atau build Docker image
docker build -t music-sheet-builder:latest .
```

## 📖 Panduan Penggunaan

### 1. Membuat Partitur Musik

1. Buka halaman **Editor**
2. Masukkan judul lagu, komposer, dan arranger (opsional)
3. Pilih salah satu:
   - Upload file `.txt` dengan notasi Anda, atau
   - Ketik/paste notasi Anda langsung di area teks
4. Klik **"Parse & Preview"** untuk melihat partitur Anda

### 2. Format Notasi

```
BAGIAN A (Time Signature: 4/4) - Intro

BAR 1 (Chord: Cmaj7)
Right Hand (RH): C E G B
Left Hand (LH): C . C .

BAR 2 (Chord: Am7)
Right Hand (RH): A C E G
Left Hand (LH): A . A .
```

**Aturan Notasi:**

- `BAGIAN X (Time Signature: N/M)` - Mendefinisikan sebuah bagian
- `BAR N (Chord: NamaChord)` - Mendefinisikan bar dengan chord
- `Right Hand (RH):` - Not untuk tangan kanan
- `Left Hand (LH):` - Not untuk tangan kiri
- `.` atau `·` - Rest (diam/istirahat)
- `-` atau `—` - Hold (tahan not sebelumnya)
- `C-E-G` - Chord (beberapa not bersamaan)
- `|` - Pemisah pengelompokan beat

### 3. Audio Preview

1. Parse partitur Anda terlebih dahulu
2. Sesuaikan tempo (BPM) jika diinginkan (default: 120)
3. Klik tombol **Play** (▶) untuk mendengarkan partitur Anda
4. Gunakan **Pause** (⏸) untuk menjeda pemutaran
5. Gunakan **Stop** (⏹) untuk menghentikan dan mereset

**Catatan:** Audio player menggunakan Web Audio API untuk mensintesis suara piano langsung di browser Anda.

### 4. Transpose

1. Parse partitur Anda terlebih dahulu
2. Pilih jumlah semitone untuk transpose (+/- 12)
3. Klik **"Apply Transpose"**
4. Partitur Anda akan ditranspose ke kunci baru

### 5. Ekspor ke PDF

1. Parse partitur Anda
2. Klik **"Download PDF"**
3. Browser Anda akan mengunduh file PDF yang terformat

### 6. Simpan ke Perpustakaan

1. Parse partitur Anda
2. Klik **"Save to Library"**
3. Akses partitur yang tersimpan dari halaman **Library**

### 7. Mengelola Perpustakaan Anda

- Navigasi ke halaman **Library**
- Cari partitur berdasarkan judul
- Lihat, ekspor, atau hapus partitur yang tersimpan

## 🏗 Struktur Proyek

```
src/main/kotlin/com/utaaa/music_sheet_builder/
├── model/                      # Data classes
│   ├── Beat.kt                # Representasi beat tunggal
│   ├── Bar.kt                 # Bar (measure) dengan beats
│   ├── Section.kt             # Section dengan bars
│   ├── MusicSheet.kt          # Partitur lengkap
│   └── ValidationResult.kt    # Hasil validasi
│
├── entity/                     # JPA entities
│   └── MusicSheetEntity.kt    # Entity database
│
├── repository/                 # Akses data
│   └── MusicSheetRepository.kt
│
├── service/                    # Business logic
│   ├── ParserService.kt       # Parser Text → MusicSheet
│   ├── ValidationService.kt   # Validasi partitur
│   ├── PdfExportService.kt    # Generasi PDF
│   ├── TransposeService.kt    # Transposisi kunci
│   ├── AudioService.kt        # Layanan audio preview
│   └── MusicSheetStorageService.kt  # Operasi database
│
├── ui/                         # Vaadin UI
│   ├── MainLayout.kt          # Layout utama dengan navigasi
│   ├── EditorView.kt          # Halaman editor
│   ├── LibraryView.kt         # Halaman perpustakaan
│   └── component/
│       ├── SheetPreviewComponent.kt  # Komponen preview
│       └── AudioPlayerComponent.kt   # Komponen audio player
│
├── exception/                  # Custom exceptions
│   └── MusicSheetException.kt
│
└── MusicSheetBuilderApplication.kt  # Aplikasi utama
```

## 🛠 Stack Teknologi

| Layer     | Teknologi           | Tujuan                   |
| --------- | ------------------- | ------------------------ |
| Language  | Kotlin 1.9.25       | Bahasa JVM modern        |
| Framework | Spring Boot 3.5.14  | Framework backend        |
| UI        | Vaadin 24.10.4      | Framework Web UI         |
| PDF       | iText 7 (8.0.5)     | Generasi PDF             |
| Database  | H2                  | Database embedded        |
| Build     | Gradle (Kotlin DSL) | Otomasi build            |
| Logging   | kotlin-logging      | Logging terstruktur      |
| Styling   | Tailwind-inspired   | Modern CSS design system |

## 🎯 Roadmap

### Phase 1 - MVP ✅

- [x] Parser notasi teks
- [x] Live preview
- [x] Ekspor PDF
- [x] Penyimpanan database
- [x] Fungsi transpose
- [x] Manajemen perpustakaan
- [x] Audio preview dengan Web Audio API
- [x] UI/UX modern dengan Tailwind-inspired design

### Phase 2 - Enhanced UI ✅

- [x] Tab-based editor interface
- [x] Code editor-style notation input
- [x] Modern gradient design
- [x] Responsive layout
- [ ] Visual note rendering (VexFlow integration)
- [ ] Drag-and-drop note editing
- [ ] Zoom controls
- [ ] Print layout optimization

### Phase 3 - Advanced Features

- [ ] User authentication
- [ ] Share sheets via link
- [ ] Export to MusicXML
- [ ] Export to MIDI
- [ ] Import from MIDI
- [ ] Collaborative editing

### Phase 4 - Audio Enhancement

- [ ] MIDI playback
- [ ] Metronome
- [ ] Audio recording
- [ ] Practice mode

## 🧪 Testing

```bash
# Jalankan semua test
./gradlew test

# Jalankan dengan coverage
./gradlew test jacocoTestReport
```

## 📝 File Contoh

Lihat `sample-audio-test.txt` untuk contoh format notasi.

## 🎨 Fitur UI/UX

### Desain Modern

- **Gradient Color Scheme**: Purple to Pink (#7c6ff2 → #c026d3)
- **Dark Code Editor**: Background #1e1e1e untuk textarea notasi
- **Glass Effects**: Backdrop blur dan transparency
- **Smooth Animations**: Transisi 200ms cubic-bezier
- **Professional Typography**: Inter font + JetBrains Mono untuk code

### Layout

- **Tab-based Interface**: Switch antara Editor dan Preview
- **Sidebar Navigation**: Form input dan action buttons
- **Large Editor Area**: Textarea luas untuk menulis notasi
- **Responsive Design**: Adaptif untuk berbagai ukuran layar

## 🤝 Kontribusi

Kontribusi sangat diterima! Silakan submit Pull Request.

## 📄 Lisensi

Proyek ini dilisensikan di bawah MIT License.

## 🙏 Acknowledgments

- Dibangun dengan [Vaadin](https://vaadin.com/)
- Generasi PDF oleh [iText](https://itextpdf.com/)
- Powered by [Spring Boot](https://spring.io/projects/spring-boot)
- UI Design inspired by modern design systems

## 📧 Kontak

Untuk pertanyaan atau feedback, silakan buka issue di GitHub.

---

Dibuat dengan ❤️ dan ☕ oleh tim Music Sheet Builder

# 🎵 Music Sheet Builder — Kotlin + Spring Boot + Vaadin

## Stack Teknologi

| Layer        | Pilihan                 | Keterangan                      |
| ------------ | ----------------------- | ------------------------------- |
| Language     | **Kotlin 2.3**          | Ringkas, null-safe, 100% JVM    |
| Framework    | **Spring Boot 3.x**     | Backend & DI container          |
| UI           | **Vaadin 24**           | Web UI full Kotlin, no HTML/CSS |
| PDF Export   | **iText 7 (Community)** | Generate PDF server-side        |
| Build Tool   | **Gradle (Kotlin DSL)** | `build.gradle.kts`              |
| Java Version | **JDK 17+**             | Minimum untuk Spring Boot 3     |

---

## Struktur Folder

```
music-sheet-builder/
├── src/
│   └── main/
│       ├── kotlin/com/musicsheet/
│       │   ├── MusicSheetApplication.kt     # Entry point Spring Boot
│       │   │
│       │   ├── model/                       # Data classes
│       │   │   ├── Beat.kt
│       │   │   ├── Bar.kt
│       │   │   ├── Section.kt
│       │   │   └── MusicSheet.kt
│       │   │
│       │   ├── service/                     # Business logic
│       │   │   ├── ParserService.kt         # Parse .txt → MusicSheet
│       │   │   └── PdfExportService.kt      # MusicSheet → PDF bytes
│       │   │
│       │   └── ui/                          # Vaadin Views
│       │       ├── MainLayout.kt            # Layout utama (sidebar + konten)
│       │       ├── EditorView.kt            # Halaman editor & upload
│       │       └── SheetPreviewView.kt      # Komponen preview sheet
│       │
│       └── resources/
│           └── application.properties       # Konfigurasi Spring Boot
│
├── build.gradle.kts                         # Gradle dependencies
└── settings.gradle.kts
```

---

## Setup Project — start.spring.io

Buka [start.spring.io](https://start.spring.io) dan isi:

| Field       | Nilai               |
| ----------- | ------------------- |
| Project     | Gradle - Kotlin     |
| Language    | Kotlin              |
| Spring Boot | 3.2.x               |
| Group       | com.musicsheet      |
| Artifact    | music-sheet-builder |
| Java        | 21                  |

**Dependencies yang dipilih:**

- ✅ Vaadin
- ✅ Spring Web
- ✅ Spring Boot DevTools

Lalu tambah iText manual di `build.gradle.kts`.

---

## build.gradle.kts

```kotlin
plugins {
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.vaadin") version "24.3.0"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
}

group = "com.musicsheet"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:24.3.0")
    }
}

dependencies {
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // PDF Export
    implementation("com.itextpdf:itext-core:8.0.2")
    implementation("com.itextpdf:layout:8.0.2")
    implementation("com.itextpdf:kernel:8.0.2")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

---

## Model (Data Classes)

```kotlin
// model/Beat.kt
data class Beat(
    val notes: List<String> = emptyList(),  // ["F", "Ab", "C"] atau []
    val isRest: Boolean = false,
    val isHeld: Boolean = false
)

// model/Bar.kt
data class Bar(
    val number: Int,
    val chord: String,
    val annotation: String? = null,
    val rightHand: List<Beat> = emptyList(),
    val leftHand: List<Beat> = emptyList()
)

// model/Section.kt
data class Section(
    val id: String,                         // "A", "B"
    val name: String,                       // "BAGIAN A"
    val timeSigTop: Int,                    // 7
    val timeSigBottom: Int,                 // 8
    val style: String? = null,              // "Staccato", "Groove"
    val grouping: List<Int> = emptyList(),  // [2, 2, 3]
    val bars: MutableList<Bar> = mutableListOf()
)

// model/MusicSheet.kt
data class MusicSheet(
    val title: String = "Untitled",
    val sections: MutableList<Section> = mutableListOf()
)
```

---

## ParserService.kt

```kotlin
// service/ParserService.kt
@Service
class ParserService {

    fun parse(text: String, title: String = "Untitled"): MusicSheet {
        val sheet = MusicSheet(title = title)
        val lines = text.lines()
        var currentSection: Section? = null
        var currentBar: Bar? = null
        var rhBeats: List<Beat>? = null

        for (line in lines) {
            val trimmed = line.trim()

            // Skip separator & hitungan lines
            if (trimmed.isBlank() || trimmed.startsWith("===") ||
                trimmed.startsWith("Hitungan") || trimmed.startsWith("Pengelompokan")) continue

            // Deteksi BAGIAN baru
            val sectionMatch = Regex("""BAGIAN\s+(\w+)\s*\(Time Signature:\s*(\d+)/(\d+)\)(?:\s*-\s*(.+))?""")
                .find(trimmed)
            if (sectionMatch != null) {
                currentSection = Section(
                    id = sectionMatch.groupValues[1],
                    name = "BAGIAN ${sectionMatch.groupValues[1]}",
                    timeSigTop = sectionMatch.groupValues[2].toInt(),
                    timeSigBottom = sectionMatch.groupValues[3].toInt(),
                    style = sectionMatch.groupValues[4].trim().takeIf { it.isNotBlank() }
                )
                sheet.sections.add(currentSection)
                continue
            }

            // Deteksi BAR baru
            val barMatch = Regex("""BAR\s+(\d+)\s*\(Chord:\s*([^)]+)\)(?:\s*[-–>]+\s*(.+))?""")
                .find(trimmed)
            if (barMatch != null && currentSection != null) {
                currentBar = Bar(
                    number = barMatch.groupValues[1].toInt(),
                    chord = barMatch.groupValues[2].trim(),
                    annotation = barMatch.groupValues[3].trim().takeIf { it.isNotBlank() }
                )
                currentSection.bars.add(currentBar)
                rhBeats = null
                continue
            }

            // Right Hand
            val rhMatch = Regex("""Right Hand\s*\(RH\)\s*:\s*(.+)""").find(trimmed)
            if (rhMatch != null && currentBar != null) {
                rhBeats = parseHandLine(rhMatch.groupValues[1])
                currentSection?.bars?.let { bars ->
                    val idx = bars.indexOfFirst { it.number == currentBar!!.number }
                    if (idx >= 0) bars[idx] = bars[idx].copy(rightHand = rhBeats!!)
                }
                continue
            }

            // Left Hand
            val lhMatch = Regex("""Left Hand\s*\(LH\)\s*:\s*(.+)""").find(trimmed)
            if (lhMatch != null && currentBar != null) {
                val lhBeats = parseHandLine(lhMatch.groupValues[1])
                currentSection?.bars?.let { bars ->
                    val idx = bars.indexOfFirst { it.number == currentBar!!.number }
                    if (idx >= 0) bars[idx] = bars[idx].copy(leftHand = lhBeats)
                }
            }
        }
        return sheet
    }

    private fun parseHandLine(line: String): List<Beat> {
        // Buang annotation dalam kurung, pisah per |
        val clean = line.replace(Regex("""\([^)]+\)"""), "").trim()
        val parts = clean.split("|")
        return parts.flatMap { part ->
            part.trim().split(Regex("""\s+""")).filter { it.isNotBlank() }.map { token ->
                when {
                    token == "." -> Beat(isRest = true)
                    token.contains("-") -> Beat(notes = token.split("-"))
                    else -> Beat(notes = listOf(token))
                }
            }
        }
    }
}
```

---

## PdfExportService.kt

```kotlin
// service/PdfExportService.kt
@Service
class PdfExportService {

    fun export(sheet: MusicSheet): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf, PageSize.A4)
        document.setMargins(30f, 36f, 30f, 36f)

        val titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)

        // Judul Sheet
        document.add(
            Paragraph(sheet.title)
                .setFont(titleFont).setFontSize(22f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(6f)
        )
        document.add(
            Paragraph("PIANO SHEET MUSIC")
                .setFont(bodyFont).setFontSize(9f)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
        )

        // Keterangan
        document.add(
            Paragraph("RH = Right Hand (Tangan Kanan)   |   LH = Left Hand (Tangan Kiri)   |   · = Rest / Diam")
                .setFont(bodyFont).setFontSize(8f)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(16f)
        )

        // Render tiap section
        for (section in sheet.sections) {
            // Section header
            val sectionTitle = "${section.name}  •  ${section.timeSigTop}/${section.timeSigBottom}" +
                (section.style?.let { "  •  $it" } ?: "")
            document.add(
                Paragraph(sectionTitle)
                    .setFont(headerFont).setFontSize(12f)
                    .setBorderBottom(SolidBorder(2f))
                    .setMarginBottom(10f)
                    .setMarginTop(12f)
            )

            // Render tiap bar sebagai tabel
            for (bar in section.bars) {
                renderBarToPdf(document, bar, bodyFont, headerFont)
            }
        }

        document.add(
            Paragraph("Generated with Music Sheet Builder")
                .setFont(bodyFont).setFontSize(8f)
                .setFontColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20f)
        )

        document.close()
        return outputStream.toByteArray()
    }

    private fun renderBarToPdf(
        document: Document,
        bar: Bar,
        bodyFont: PdfFont,
        headerFont: PdfFont
    ) {
        // Chord + nomor bar
        val label = "${bar.chord}   Bar ${bar.number}" +
            (bar.annotation?.let { "  —  $it" } ?: "")
        document.add(
            Paragraph(label)
                .setFont(headerFont).setFontSize(11f)
                .setFontColor(DeviceRgb(30, 30, 80))
                .setMarginBottom(4f).setMarginTop(10f)
        )

        // Tabel beat
        val beatCount = maxOf(bar.rightHand.size, bar.leftHand.size)
        val table = Table(UnitValue.createPercentArray(FloatArray(beatCount + 1) {
            if (it == 0) 1.5f else 1f
        })).useAllAvailableWidth().setMarginBottom(8f)

        // Header nomor beat
        table.addHeaderCell(Cell().setBorder(Border.NO_BORDER))
        for (i in 1..beatCount) {
            table.addHeaderCell(
                Cell().add(Paragraph("$i").setFont(bodyFont).setFontSize(8f)
                    .setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GRAY))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(DeviceRgb(245, 243, 238))
            )
        }

        // Right Hand row
        table.addCell(
            Cell().add(Paragraph("RH").setFont(headerFont).setFontSize(9f)
                .setFontColor(DeviceRgb(180, 30, 30)))
                .setBackgroundColor(DeviceRgb(255, 245, 245))
        )
        for (i in 0 until beatCount) {
            val beat = bar.rightHand.getOrNull(i)
            table.addCell(beatCell(beat, bodyFont))
        }

        // Left Hand row
        table.addCell(
            Cell().add(Paragraph("LH").setFont(headerFont).setFontSize(9f)
                .setFontColor(DeviceRgb(30, 80, 180)))
                .setBackgroundColor(DeviceRgb(245, 245, 255))
        )
        for (i in 0 until beatCount) {
            val beat = bar.leftHand.getOrNull(i)
            table.addCell(beatCell(beat, bodyFont))
        }

        document.add(table)
    }

    private fun beatCell(beat: Beat?, bodyFont: PdfFont): Cell {
        val text = when {
            beat == null -> "—"
            beat.isRest -> "·"
            else -> beat.notes.joinToString("\n")
        }
        val color = when {
            beat == null || beat.isRest -> ColorConstants.LIGHT_GRAY
            else -> ColorConstants.BLACK
        }
        return Cell().add(
            Paragraph(text).setFont(bodyFont).setFontSize(9f)
                .setTextAlignment(TextAlignment.CENTER).setFontColor(color)
        ).setTextAlignment(TextAlignment.CENTER)
    }
}
```

---

## MainLayout.kt (Vaadin)

```kotlin
// ui/MainLayout.kt
@Theme("my-theme")
class MainLayout : AppLayout() {

    init {
        val navbar = HorizontalLayout().apply {
            isPadding = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            add(
                Span("🎹").apply { style.set("font-size", "24px") },
                H3("Music Sheet Builder").apply {
                    style.set("margin", "0").set("color", "white")
                }
            )
            style.set("background", "#1a1a3a").set("width", "100%")
        }
        addToNavbar(navbar)
    }
}
```

---

## EditorView.kt (Vaadin) — Halaman Utama

```kotlin
// ui/EditorView.kt
@Route("", layout = MainLayout::class)
class EditorView(
    private val parserService: ParserService,
    private val pdfExportService: PdfExportService
) : VerticalLayout() {

    private var currentSheet: MusicSheet? = null
    private val titleField = TextField("Judul Lagu").apply { value = "My Music Sheet" }
    private val previewDiv = Div()

    init {
        setSizeFull()
        style.set("background", "#f5f3ee").set("padding", "24px")

        val mainContent = HorizontalLayout().apply {
            setSizeFull()
            isPadding = false
            isSpacing = true
        }

        mainContent.add(buildSidebar(), buildPreviewPanel())
        add(mainContent)
    }

    private fun buildSidebar(): VerticalLayout {
        val sidebar = VerticalLayout().apply {
            width = "320px"
            style.set("background", "#0d0d20").set("border-radius", "12px")
                .set("padding", "20px").set("gap", "16px")
            isSpacing = true
        }

        // Title field styling
        titleField.apply {
            style.set("--vaadin-input-field-background", "#13132a")
                .set("--vaadin-input-field-value-color", "#e0e0f0")
            setWidthFull()
        }

        // Upload area
        val upload = Upload(MemoryBuffer()).apply {
            setAcceptedFileTypes(".txt")
            addSucceededListener { event ->
                val buffer = receiver as MemoryBuffer
                val content = buffer.inputStream.bufferedReader().readText()
                currentSheet = parserService.parse(content, titleField.value)
                refreshPreview()
            }
            style.set("background", "#13132a").set("border", "2px dashed #2a2a50")
                .set("border-radius", "8px")
            uploadButton.text = "📂 Upload File .txt"
        }

        // Text area untuk edit manual
        val textArea = TextArea("Edit Notasi Manual").apply {
            setWidthFull()
            style.set("font-family", "monospace").set("font-size", "11px")
            height = "300px"
        }

        // Parse button
        val parseBtn = Button("▶ Parse & Preview") {
            currentSheet = parserService.parse(textArea.value, titleField.value)
            refreshPreview()
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            setWidthFull()
        }

        // Export PDF button
        val exportBtn = Button("⬇ Download PDF") {
            currentSheet?.let { sheet ->
                val bytes = pdfExportService.export(sheet)
                val resource = StreamResource("${sheet.title}.pdf") {
                    bytes.inputStream()
                }
                val anchor = Anchor(resource, "").apply {
                    element.setAttribute("download", true)
                }
                add(anchor)
                anchor.element.callJsFunction("click")
                remove(anchor)
            } ?: Notification.show("Parse dulu sebelum export!", 3000, Notification.Position.MIDDLE)
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
            setWidthFull()
            style.set("font-weight", "700")
        }

        sidebar.add(
            H4("🎵 Music Sheet Builder").apply { style.set("color", "#8888ff") },
            titleField,
            H5("Import File").apply { style.set("color", "#555580") },
            upload,
            H5("atau Edit Manual").apply { style.set("color", "#555580") },
            textArea,
            parseBtn,
            exportBtn
        )

        return sidebar
    }

    private fun buildPreviewPanel(): VerticalLayout {
        val panel = VerticalLayout().apply {
            style.set("background", "#FAF8F4").set("border-radius", "12px")
                .set("padding", "32px").set("overflow-y", "auto")
            setSizeFull()
        }
        previewDiv.setWidthFull()
        panel.add(previewDiv)
        return panel
    }

    private fun refreshPreview() {
        previewDiv.removeAll()
        val sheet = currentSheet ?: return

        // Judul
        previewDiv.add(
            H2(sheet.title).apply {
                style.set("text-align", "center").set("font-family", "Georgia, serif")
            }
        )

        // Render tiap section
        for (section in sheet.sections) {
            previewDiv.add(buildSectionComponent(section))
        }
    }

    private fun buildSectionComponent(section: Section): Component {
        val div = VerticalLayout().apply { isPadding = false }

        // Section header
        div.add(
            H3("${section.name}  •  ${section.timeSigTop}/${section.timeSigBottom}" +
                (section.style?.let { "  •  $it" } ?: ""))
                .apply {
                    style.set("border-bottom", "2px solid #333")
                        .set("padding-bottom", "8px")
                        .set("font-family", "Georgia, serif")
                }
        )

        // Tiap bar
        for (bar in section.bars) {
            div.add(buildBarComponent(bar))
        }

        return div
    }

    private fun buildBarComponent(bar: Bar): Component {
        val div = VerticalLayout().apply {
            style.set("margin-bottom", "20px").set("page-break-inside", "avoid")
            isPadding = false
        }

        // Chord label
        div.add(
            HorizontalLayout(
                Span(bar.chord).apply {
                    style.set("font-size", "22px").set("font-weight", "900")
                        .set("font-family", "Georgia, serif")
                },
                Span("Bar ${bar.number}").apply {
                    style.set("background", "#f0ede8").set("padding", "2px 8px")
                        .set("border-radius", "10px").set("font-size", "11px")
                        .set("color", "#aaa")
                }
            )
        )

        // Tabel beat
        val beatCount = maxOf(bar.rightHand.size, bar.leftHand.size)
        val grid = Grid<BeatRow>().apply {
            setItems(
                BeatRow("RH", bar.rightHand, beatCount),
                BeatRow("LH", bar.leftHand, beatCount)
            )
            addColumn { it.hand }.setHeader("").setWidth("50px").setFlexGrow(0)
            for (i in 0 until beatCount) {
                addColumn { row -> row.beatsText.getOrElse(i) { "—" } }
                    .setHeader("${i + 1}").setTextAlign(ColumnTextAlign.CENTER)
            }
            style.set("font-size", "12px")
            setAllRowsVisible(true)
        }

        div.add(grid)
        return div
    }
}

// Helper data class untuk Grid
data class BeatRow(val hand: String, val beats: List<Beat>, val total: Int) {
    val beatsText: List<String> = (0 until total).map { i ->
        when {
            i >= beats.size -> "—"
            beats[i].isRest -> "·"
            else -> beats[i].notes.joinToString("\n")
        }
    }
}
```

---

## MusicSheetApplication.kt

```kotlin
@SpringBootApplication
class MusicSheetApplication

fun main(args: Array<String>) {
    runApplication<MusicSheetApplication>(*args)
}
```

---

## application.properties

```properties
# Server
server.port=8080

# Vaadin
vaadin.launch-browser=true
vaadin.devmode.live-reload.enabled=true

# Upload file size
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

---

## Cara Menjalankan

```bash
# Clone / buat project dari start.spring.io
# Salin semua file ke struktur yang sesuai

# Jalankan
./gradlew bootRun

# Buka di browser
# http://localhost:8080
```

---

## Alur Penggunaan

```
User buka http://localhost:8080
        │
        ▼
Upload .txt atau ketik manual di TextArea
        │
        ▼
Klik "Parse & Preview"
        │
        ├─→ ParserService.parse(text)
        │         └─→ MusicSheet (data class)
        │
        ▼
Preview tampil di panel kanan (Vaadin Grid per bar)
        │
        ▼
Klik "Download PDF"
        │
        ├─→ PdfExportService.export(sheet)
        │         └─→ ByteArray → StreamResource
        │
        ▼
Browser download file "JudulLagu.pdf"
```

---

## Rencana Deploy (Nanti)

Ketika siap deploy:

```bash
# Build JAR
./gradlew build -Pvaadin.productionMode=true

# Hasilnya
build/libs/music-sheet-builder-0.0.1-SNAPSHOT.jar

# Jalankan di server
java -jar music-sheet-builder-0.0.1-SNAPSHOT.jar
```

Bisa deploy ke:

- **Railway / Render** — gratis, support JAR langsung
- **VPS (DigitalOcean, Vultr)** — lebih kontrol
- **Docker** — tinggal wrap JAR dalam Dockerfile

---

## Phase Pengembangan

### Phase 1 — MVP ✅ (dokumentasi ini)

- Upload `.txt` & parse
- Preview sheet di browser (Vaadin)
- Export PDF dengan iText

### Phase 2 — Peningkatan UI

- Render not musik visual (integrasi VexFlow via JavaScript bridge Vaadin)
- Transpose semua nada (+/- semitone)
- Zoom in/out preview

### Phase 3 — Fitur Lanjutan

- Simpan ke database (H2 → PostgreSQL)
- User login (Spring Security)
- History daftar sheet yang pernah dibuat
- Share link sheet

### Phase 4 — Audio

- Preview MIDI playback (Web Audio API via Vaadin JS)

package com.utaaa.music_sheet_builder.ui

import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.service.AudioService
import com.utaaa.music_sheet_builder.service.MusicSheetStorageService
import com.utaaa.music_sheet_builder.service.ParserService
import com.utaaa.music_sheet_builder.service.PdfExportService
import com.utaaa.music_sheet_builder.service.TransposeService
import com.utaaa.music_sheet_builder.ui.component.AudioPlayerComponent
import com.utaaa.music_sheet_builder.ui.component.SheetPreviewComponent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MemoryBuffer
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.StreamResource

/**
 * Main editor view for creating and editing music sheets
 */
@Route("", layout = MainLayout::class)
@PageTitle("Editor | Music Sheet Builder")
class EditorView(
    private val parserService: ParserService,
    private val pdfExportService: PdfExportService,
    private val transposeService: TransposeService,
    private val storageService: MusicSheetStorageService,
    private val audioService: AudioService
) : VerticalLayout() {
    
    private var currentSheet: MusicSheet? = null
    private val audioPlayer = AudioPlayerComponent(audioService)
    private val titleField = TextField("Judul Lagu").apply { 
        value = "My Music Sheet"
        setWidthFull()
    }
    private val composerField = TextField("Composer (Optional)").apply { setWidthFull() }
    private val arrangerField = TextField("Arranger (Optional)").apply { setWidthFull() }
    private val textArea = TextArea("Edit Notasi Manual").apply {
        setWidthFull()
        height = "400px"
        style.set("font-family", "monospace")
        style.set("font-size", "12px")
        placeholder = "Paste your music notation here or upload a .txt file..."
    }
    private val previewComponent = SheetPreviewComponent()
    
    init {
        setSizeFull()
        style.set("background", "linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%)")
        style.set("padding", "32px")
        
        val mainContent = HorizontalLayout().apply {
            setSizeFull()
            isSpacing = true
            style.set("gap", "24px")
        }
        
        mainContent.add(buildSidebar(), buildEditorAndPreviewPanel())
        add(mainContent)
    }
    
    private fun buildSidebar(): VerticalLayout {
        val sidebar = VerticalLayout().apply {
            width = "420px"
            addClassName("modern-card")
            style.set("background", "white")
            style.set("border-radius", "24px")
            style.set("padding", "32px")
            style.set("box-shadow", "0 10px 40px rgba(0,0,0,0.08)")
            style.set("border", "1px solid #e5e7eb")
            isSpacing = true
            style.set("gap", "20px")
        }
        
        // Modern Header with gradient
        val headerIcon = com.vaadin.flow.component.icon.VaadinIcon.MUSIC.create().apply {
            style.set("color", "#7c6ff2")
            style.set("width", "32px")
            style.set("height", "32px")
        }
        
        val header = HorizontalLayout().apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            isSpacing = true
            style.set("gap", "12px")
            style.set("margin-bottom", "8px")
            
            add(
                headerIcon,
                H2("Music Editor").apply {
                    style.set("margin", "0")
                    addClassName("text-gradient")
                    style.set("font-size", "24px")
                    style.set("font-weight", "800")
                }
            )
        }
        
        // Styled input fields
        titleField.apply {
            style.set("--vaadin-input-field-border-radius", "12px")
            label = "🎵 Song Title"
        }
        
        composerField.apply {
            style.set("--vaadin-input-field-border-radius", "12px")
            label = "👤 Composer"
        }
        
        arrangerField.apply {
            style.set("--vaadin-input-field-border-radius", "12px")
            label = "🎼 Arranger"
        }
        
        // Upload section with modern styling
        val uploadSection = buildUploadSection()
        
        // Action buttons with icons
        val parseBtn = Button("Parse & Preview", com.vaadin.flow.component.icon.VaadinIcon.PLAY.create()) {
            parseAndPreview()
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            setWidthFull()
            style.set("height", "48px")
            style.set("font-weight", "600")
            style.set("font-size", "15px")
        }
        
        // Divider
        val divider = Div().apply {
            addClassName("divider")
        }
        
        // Transpose section (moved below divider)
        val transposeSection = buildTransposeSection()
        
        // Bottom action buttons
        val actionButtons = HorizontalLayout().apply {
            setWidthFull()
            isSpacing = true
            style.set("gap", "12px")
        }
        
        val saveBtn = Button("Save", com.vaadin.flow.component.icon.VaadinIcon.DATABASE.create()) {
            saveToLibrary()
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
            style.set("flex", "1")
            style.set("height", "44px")
            style.set("font-weight", "600")
        }
        
        val exportBtn = Button("Export PDF", com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD.create()) {
            exportToPdf()
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY)
            style.set("flex", "1")
            style.set("height", "44px")
            style.set("font-weight", "600")
        }
        
        actionButtons.add(saveBtn, exportBtn)
        
        sidebar.add(
            header,
            titleField,
            composerField,
            arrangerField,
            uploadSection,
            parseBtn,
            divider,
            transposeSection,
            actionButtons
        )
        
        return sidebar
    }
    
    private fun buildEditorAndPreviewPanel(): VerticalLayout {
        val container = VerticalLayout().apply {
            setSizeFull()
            isPadding = false
            isSpacing = false
            style.set("gap", "24px")
        }
        
        // Tab-like switcher
        val tabBar = HorizontalLayout().apply {
            isSpacing = false
            style.set("gap", "8px")
            style.set("margin-bottom", "16px")
        }
        
        val editorTab = Button("📝 Editor", com.vaadin.flow.component.icon.VaadinIcon.EDIT.create()) {
            showEditorPanel()
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            style.set("border-radius", "12px 12px 0 0")
            style.set("flex", "1")
        }
        
        val previewTab = Button("👁 Preview", com.vaadin.flow.component.icon.VaadinIcon.EYE.create()) {
            showPreviewPanel()
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_CONTRAST)
            style.set("border-radius", "12px 12px 0 0")
            style.set("flex", "1")
        }
        
        tabBar.add(editorTab, previewTab)
        
        // Editor Panel (Manual Notation)
        val editorPanel = VerticalLayout().apply {
            setSizeFull()
            addClassName("modern-card")
            style.set("background", "white")
            style.set("border-radius", "0 24px 24px 24px")
            style.set("padding", "32px")
            style.set("box-shadow", "0 10px 40px rgba(0,0,0,0.08)")
            style.set("border", "1px solid #e5e7eb")
        }
        
        val editorHeader = HorizontalLayout().apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style.set("margin-bottom", "20px")
        }
        
        editorHeader.add(
            H2("✍️ Manual Notation Editor").apply {
                style.set("margin", "0")
                addClassName("text-gradient")
                style.set("font-size", "24px")
                style.set("font-weight", "800")
            },
            Span("Format: BAGIAN X (Time Signature: N/M)").apply {
                addClassName("badge")
                style.set("background", "#f0f4ff")
                style.set("color", "#7c6ff2")
                style.set("padding", "8px 16px")
                style.set("border-radius", "10px")
                style.set("font-size", "12px")
                style.set("font-weight", "600")
            }
        )
        
        textArea.apply {
            setWidthFull()
            height = "calc(100vh - 400px)"
            style.set("--vaadin-input-field-border-radius", "12px")
            style.set("--vaadin-input-field-background", "#1e1e1e")
            style.set("--vaadin-input-field-value-color", "#d4d4d4")
            style.set("border", "2px solid #374151")
            addClassName("mono")
            label = null
            
            element.style.set("font-family", "'JetBrains Mono', 'Fira Code', 'Consolas', monospace")
            element.style.set("font-size", "14px")
            element.style.set("line-height", "1.7")
            element.style.set("letter-spacing", "0.02em")
            
            placeholder = """Example format:
BAGIAN A (Time Signature: 4/4) - Intro

BAR 1 (Chord: Cmaj7)
Right Hand (RH): C E G B
Left Hand (LH): C . C .

BAR 2 (Chord: Am7)
Right Hand (RH): A C E G
Left Hand (LH): A . A .

═══════════════════════════════════════════════

Tips:
• Use . for rest (silence)
• Use - for hold (sustain)
• Use C-E-G for chords
• Use | for beat grouping

═══════════════════════════════════════════════"""
        }
        
        // Helper info card
        val helperCard = Div().apply {
            style.set("background", "linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%)")
            style.set("border-radius", "12px")
            style.set("padding", "16px 20px")
            style.set("border", "1px solid #bfdbfe")
            style.set("margin-top", "16px")
        }
        
        val helperText = Span().apply {
            element.setProperty("innerHTML", """
                <div style="display: flex; gap: 16px; align-items: start;">
                    <span style="font-size: 24px;">💡</span>
                    <div style="font-size: 13px; color: #1e40af; line-height: 1.6;">
                        <strong style="font-size: 14px;">Quick Guide:</strong><br/>
                        <span style="color: #3b82f6; font-weight: 600;">BAGIAN</span> = Section &nbsp;•&nbsp; 
                        <span style="color: #3b82f6; font-weight: 600;">BAR</span> = Measure &nbsp;•&nbsp; 
                        <span style="color: #3b82f6; font-weight: 600;">RH/LH</span> = Right/Left Hand<br/>
                        <span style="color: #3b82f6; font-weight: 600;">.</span> = Rest &nbsp;•&nbsp; 
                        <span style="color: #3b82f6; font-weight: 600;">-</span> = Hold &nbsp;•&nbsp; 
                        <span style="color: #3b82f6; font-weight: 600;">C-E-G</span> = Chord &nbsp;•&nbsp; 
                        <span style="color: #3b82f6; font-weight: 600;">|</span> = Beat grouping
                    </div>
                </div>
            """.trimIndent())
        }
        
        helperCard.add(helperText)
        
        editorPanel.add(editorHeader, textArea, helperCard)
        
        // Preview Panel
        val previewPanel = buildPreviewPanel()
        previewPanel.isVisible = false
        
        // Store references for tab switching
        editorTab.addClickListener {
            editorPanel.isVisible = true
            previewPanel.isVisible = false
            editorTab.removeThemeVariants(ButtonVariant.LUMO_CONTRAST)
            editorTab.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            previewTab.removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            previewTab.addThemeVariants(ButtonVariant.LUMO_CONTRAST)
        }
        
        previewTab.addClickListener {
            editorPanel.isVisible = false
            previewPanel.isVisible = true
            previewTab.removeThemeVariants(ButtonVariant.LUMO_CONTRAST)
            previewTab.addThemeVariants(ButtonVariant.LUMO_PRIMARY)
            editorTab.removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            editorTab.addThemeVariants(ButtonVariant.LUMO_CONTRAST)
        }
        
        container.add(tabBar, editorPanel, previewPanel)
        return container
    }
    
    private fun showEditorPanel() {
        // Handled by button click listeners
    }
    
    private fun showPreviewPanel() {
        // Handled by button click listeners
    }
    
    private fun buildUploadSection(): VerticalLayout {
        val section = VerticalLayout().apply {
            isPadding = false
            isSpacing = false
            style.set("gap", "8px")
        }
        
        val sectionLabel = Span("📁 Import File").apply {
            style.set("font-weight", "600")
            style.set("color", "#374151")
            style.set("font-size", "14px")
            style.set("margin-bottom", "8px")
        }
        
        val buffer = MemoryBuffer()
        val upload = Upload(buffer).apply {
            setAcceptedFileTypes(".txt", ".musicsheet")
            maxFiles = 1
            
            addSucceededListener { event ->
                val content = buffer.inputStream.bufferedReader().readText()
                textArea.value = content
                
                Notification.show(
                    "✅ File uploaded successfully!",
                    3000,
                    Notification.Position.TOP_END
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            }
            
            addFailedListener {
                Notification.show(
                    "❌ Upload failed!",
                    3000,
                    Notification.Position.TOP_END
                ).addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
            
            setWidthFull()
            style.set("border-radius", "12px")
        }
        
        section.add(sectionLabel, upload)
        return section
    }
    
    private fun buildTransposeSection(): VerticalLayout {
        val section = VerticalLayout().apply {
            isPadding = false
            style.set("gap", "12px")
            style.set("background", "linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)")
            style.set("padding", "20px")
            style.set("border-radius", "16px")
            style.set("border", "1px solid #e0eaff")
        }
        
        val sectionHeader = HorizontalLayout().apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            isSpacing = true
            style.set("gap", "8px")
            
            add(
                com.vaadin.flow.component.icon.VaadinIcon.ARROWS_LONG_V.create().apply {
                    style.set("color", "#7c6ff2")
                },
                Span("Transpose").apply {
                    style.set("font-weight", "600")
                    style.set("color", "#374151")
                    style.set("font-size", "14px")
                }
            )
        }
        
        val transposeSelect = Select<Int>().apply {
            setItems(-12, -11, -10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
            value = 0
            label = "Semitones"
            setWidthFull()
            style.set("--vaadin-input-field-border-radius", "10px")
        }
        
        val transposeBtn = Button("Apply Transpose", com.vaadin.flow.component.icon.VaadinIcon.REFRESH.create()) {
            val semitones = transposeSelect.value
            if (semitones != 0) {
                currentSheet?.let { sheet ->
                    currentSheet = transposeService.transpose(sheet, semitones)
                    previewComponent.updateSheet(currentSheet!!)
                    audioPlayer.loadSheet(currentSheet!!)
                    
                    Notification.show(
                        "✅ Transposed by $semitones semitones",
                        3000,
                        Notification.Position.TOP_END
                    ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
                } ?: run {
                    Notification.show(
                        "⚠ Please parse the sheet first",
                        3000,
                        Notification.Position.MIDDLE
                    ).addThemeVariants(NotificationVariant.LUMO_CONTRAST)
                }
            }
        }.apply {
            setWidthFull()
            style.set("height", "40px")
        }
        
        section.add(sectionHeader, transposeSelect, transposeBtn)
        return section
    }
    
    private fun buildPreviewPanel(): VerticalLayout {
        val panel = VerticalLayout().apply {
            addClassName("modern-card")
            style.set("background", "white")
            style.set("border-radius", "24px")
            style.set("padding", "32px")
            style.set("box-shadow", "0 10px 40px rgba(0,0,0,0.08)")
            style.set("overflow-y", "auto")
            style.set("border", "1px solid #e5e7eb")
            setSizeFull()
        }
        
        val headerContainer = HorizontalLayout().apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style.set("margin-bottom", "24px")
            style.set("padding-bottom", "20px")
            style.set("border-bottom", "2px solid #f3f4f6")
        }
        
        val headerLeft = HorizontalLayout().apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            isSpacing = true
            style.set("gap", "12px")
            
            add(
                com.vaadin.flow.component.icon.VaadinIcon.EYE.create().apply {
                    style.set("color", "#7c6ff2")
                    style.set("width", "28px")
                    style.set("height", "28px")
                },
                H2("Live Preview").apply {
                    style.set("margin", "0")
                    addClassName("text-gradient")
                    style.set("font-size", "24px")
                    style.set("font-weight", "800")
                }
            )
        }
        
        headerContainer.add(headerLeft, audioPlayer)
        
        previewComponent.setWidthFull()
        previewComponent.style.set("animation", "fadeIn 0.5s ease-out")
        
        panel.add(headerContainer, previewComponent)
        return panel
    }
    
    private fun parseAndPreview() {
        val text = textArea.value
        if (text.isBlank()) {
            Notification.show(
                "⚠ Please enter or upload notation text",
                3000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_CONTRAST)
            return
        }
        
        val result = parserService.parse(text, titleField.value)
        
        result.onSuccess { sheet ->
            currentSheet = sheet.copy(
                composer = composerField.value.takeIf { it.isNotBlank() },
                arranger = arrangerField.value.takeIf { it.isNotBlank() }
            )
            previewComponent.updateSheet(currentSheet!!)
            audioPlayer.loadSheet(currentSheet!!)
            
            Notification.show(
                "✅ Parsed successfully! ${sheet.getSectionCount()} sections, ${sheet.getTotalBarCount()} bars",
                3000,
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
        }
        
        result.onFailure { error ->
            Notification.show(
                "❌ Parse error: ${error.message}",
                5000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }
    
    private fun saveToLibrary() {
        currentSheet?.let { sheet ->
            try {
                val id = storageService.save(sheet)
                Notification.show(
                    "✅ Saved to library! ID: $id",
                    3000,
                    Notification.Position.TOP_END
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            } catch (e: Exception) {
                Notification.show(
                    "❌ Save failed: ${e.message}",
                    5000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
        } ?: run {
            Notification.show(
                "⚠ Please parse the sheet first",
                3000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_CONTRAST)
        }
    }
    
    private fun exportToPdf() {
        currentSheet?.let { sheet ->
            try {
                val bytes = pdfExportService.export(sheet)
                val resource = StreamResource("${sheet.title}.pdf", 
                    com.vaadin.flow.server.InputStreamFactory { bytes.inputStream() }
                )
                
                val anchor = Anchor(resource as com.vaadin.flow.server.AbstractStreamResource, "").apply {
                    element.setAttribute("download", true)
                }
                this.add(anchor as com.vaadin.flow.component.Component)
                anchor.element.callJsFunction("click")
                this.remove(anchor as com.vaadin.flow.component.Component)
                
                Notification.show(
                    "✅ PDF exported successfully!",
                    3000,
                    Notification.Position.TOP_END
                ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
                
            } catch (e: Exception) {
                Notification.show(
                    "❌ Export failed: ${e.message}",
                    5000,
                    Notification.Position.MIDDLE
                ).addThemeVariants(NotificationVariant.LUMO_ERROR)
            }
        } ?: run {
            Notification.show(
                "⚠ Please parse the sheet first",
                3000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_CONTRAST)
        }
    }
}

package com.utaaa.music_sheet_builder.ui

import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.service.AudioService
import com.utaaa.music_sheet_builder.service.MusicSheetStorageService
import com.utaaa.music_sheet_builder.service.PdfExportService
import com.utaaa.music_sheet_builder.ui.component.AudioPlayerComponent
import com.utaaa.music_sheet_builder.ui.component.SheetPreviewComponent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.value.ValueChangeMode
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.StreamResource

/**
 * Library view for browsing saved music sheets
 */
@Route("library", layout = MainLayout::class)
@PageTitle("Library | Music Sheet Builder")
class LibraryView(
    private val storageService: MusicSheetStorageService,
    private val pdfExportService: PdfExportService,
    private val audioService: AudioService
) : VerticalLayout() {
    
    private val grid = Grid<SheetItem>()
    private val searchField = TextField().apply {
        placeholder = "Search by title..."
        prefixComponent = com.vaadin.flow.component.icon.VaadinIcon.SEARCH.create()
        valueChangeMode = ValueChangeMode.LAZY
        valueChangeTimeout = 500
        setWidthFull()
    }
    
    init {
        setSizeFull()
        style.set("background", "linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%)")
        style.set("padding", "32px")
        
        val container = VerticalLayout().apply {
            setSizeFull()
            addClassName("modern-card")
            style.set("background", "white")
            style.set("border-radius", "24px")
            style.set("padding", "40px")
            style.set("box-shadow", "0 10px 40px rgba(0,0,0,0.08)")
            style.set("border", "1px solid #e5e7eb")
        }
        
        // Modern header with gradient
        val headerContainer = HorizontalLayout().apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style.set("margin-bottom", "32px")
            style.set("padding-bottom", "24px")
            style.set("border-bottom", "2px solid #f3f4f6")
        }
        
        val headerLeft = HorizontalLayout().apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            isSpacing = true
            style.set("gap", "16px")
        }
        
        val headerIcon = com.vaadin.flow.component.icon.VaadinIcon.BOOK.create().apply {
            style.set("color", "#7c6ff2")
            style.set("width", "36px")
            style.set("height", "36px")
        }
        
        val headerTitle = VerticalLayout().apply {
            isPadding = false
            isSpacing = false
            style.set("gap", "4px")
            
            add(
                H2("Music Library").apply {
                    style.set("margin", "0")
                    addClassName("text-gradient")
                    style.set("font-size", "28px")
                    style.set("font-weight", "800")
                },
                Span("Browse and manage your saved music sheets").apply {
                    style.set("color", "#6b7280")
                    style.set("font-size", "14px")
                }
            )
        }
        
        headerLeft.add(headerIcon, headerTitle)
        
        // Stats badge
        val statsBadge = Div().apply {
            addClassName("badge")
            style.set("background", "linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)")
            style.set("color", "#7c6ff2")
            style.set("padding", "8px 16px")
            style.set("border-radius", "20px")
            style.set("font-weight", "600")
            style.set("font-size", "13px")
            style.set("border", "1px solid #e0eaff")
            text = "📊 ${storageService.findAll().size} Sheets"
        }
        
        headerContainer.add(headerLeft, statsBadge)
        
        // Modern search field
        searchField.apply {
            placeholder = "🔍 Search by title..."
            style.set("--vaadin-input-field-border-radius", "12px")
            style.set("margin-bottom", "24px")
            prefixComponent = com.vaadin.flow.component.icon.VaadinIcon.SEARCH.create().apply {
                style.set("color", "#9ca3af")
            }
        }
        
        setupModernGrid()
        loadAllSheets()
        
        container.add(headerContainer, searchField, grid)
        add(container)
    }
    
    private fun setupModernGrid() {
        grid.apply {
            addColumn { it.title }
                .setHeader("Title")
                .setAutoWidth(true)
                .setFlexGrow(2)
                .setRenderer(com.vaadin.flow.data.renderer.ComponentRenderer { item ->
                    VerticalLayout().apply {
                        isPadding = false
                        isSpacing = false
                        style.set("gap", "4px")
                        
                        add(
                            Span(item.title).apply {
                                style.set("font-weight", "600")
                                style.set("color", "#111827")
                                style.set("font-size", "15px")
                            },
                            Span("${item.sectionCount} sections • ${item.barCount} bars").apply {
                                style.set("color", "#9ca3af")
                                style.set("font-size", "12px")
                            }
                        )
                    }
                })
            
            addColumn { item ->
                item.composer ?: "Unknown"
            }
                .setHeader("Composer")
                .setAutoWidth(true)
                .setFlexGrow(1)
                .setRenderer(com.vaadin.flow.data.renderer.ComponentRenderer { item ->
                    Span(item.composer ?: "Unknown").apply {
                        style.set("color", "#6b7280")
                        style.set("font-size", "14px")
                    }
                })
            
            addComponentColumn { item ->
                HorizontalLayout().apply {
                    isSpacing = true
                    style.set("gap", "8px")
                    defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
                    
                    val viewBtn = Button("View", com.vaadin.flow.component.icon.VaadinIcon.EYE.create()) {
                        showPreviewDialog(item)
                    }.apply {
                        addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY)
                        style.set("border-radius", "8px")
                    }
                    
                    val exportBtn = Button(com.vaadin.flow.component.icon.VaadinIcon.DOWNLOAD.create()) {
                        exportSheet(item)
                    }.apply {
                        addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS)
                        style.set("border-radius", "8px")
                    }
                    
                    val deleteBtn = Button(com.vaadin.flow.component.icon.VaadinIcon.TRASH.create()) {
                        deleteSheet(item)
                    }.apply {
                        addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR)
                        style.set("border-radius", "8px")
                    }
                    
                    add(viewBtn, exportBtn, deleteBtn)
                }
            }
                .setHeader("Actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
            
            addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT)
            setHeightFull()
            style.set("border-radius", "16px")
            style.set("overflow", "hidden")
        }
        
        searchField.addValueChangeListener { event ->
            if (event.value.isBlank()) {
                loadAllSheets()
            } else {
                searchSheets(event.value)
            }
        }
    }
    
    private fun loadAllSheets() {
        val sheets = storageService.findAll()
        grid.setItems(sheets.map { (id, sheet) ->
            SheetItem(
                id = id,
                title = sheet.title,
                composer = sheet.composer,
                sectionCount = sheet.getSectionCount(),
                barCount = sheet.getTotalBarCount(),
                sheet = sheet
            )
        })
    }
    
    private fun searchSheets(query: String) {
        val sheets = storageService.searchByTitle(query)
        grid.setItems(sheets.map { (id, sheet) ->
            SheetItem(
                id = id,
                title = sheet.title,
                composer = sheet.composer,
                sectionCount = sheet.getSectionCount(),
                barCount = sheet.getTotalBarCount(),
                sheet = sheet
            )
        })
    }
    
    private fun showPreviewDialog(item: SheetItem) {
        val dialog = Dialog().apply {
            width = "85%"
            height = "85%"
            addClassName("modern-card")
        }
        
        val content = VerticalLayout().apply {
            setSizeFull()
            isPadding = false
            style.set("gap", "20px")
        }
        
        val header = HorizontalLayout().apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style.set("padding", "24px")
            style.set("background", "linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)")
            style.set("border-radius", "16px 16px 0 0")
            style.set("border-bottom", "2px solid #e0eaff")
        }
        
        val titleSection = VerticalLayout().apply {
            isPadding = false
            isSpacing = false
            style.set("gap", "4px")
            
            add(
                H2(item.title).apply {
                    style.set("margin", "0")
                    addClassName("text-gradient")
                    style.set("font-size", "24px")
                    style.set("font-weight", "800")
                },
                Span("${item.composer ?: "Unknown Composer"} • ${item.sectionCount} sections • ${item.barCount} bars").apply {
                    style.set("color", "#6b7280")
                    style.set("font-size", "14px")
                }
            )
        }
        
        val audioPlayer = AudioPlayerComponent(audioService).apply {
            loadSheet(item.sheet)
        }
        
        header.add(titleSection, audioPlayer)
        
        val preview = SheetPreviewComponent().apply {
            updateSheet(item.sheet)
            setHeightFull()
            style.set("padding", "24px")
            style.set("overflow-y", "auto")
        }
        
        val footer = HorizontalLayout().apply {
            setWidthFull()
            justifyContentMode = FlexComponent.JustifyContentMode.END
            style.set("padding", "20px 24px")
            style.set("background", "#f9fafb")
            style.set("border-radius", "0 0 16px 16px")
            style.set("gap", "12px")
        }
        
        val closeBtn = Button("Close", com.vaadin.flow.component.icon.VaadinIcon.CLOSE.create()) {
            dialog.close()
        }.apply {
            addThemeVariants(ButtonVariant.LUMO_CONTRAST)
            style.set("border-radius", "10px")
        }
        
        footer.add(closeBtn)
        
        content.add(header, preview, footer)
        dialog.add(content)
        dialog.open()
    }
    
    private fun exportSheet(item: SheetItem) {
        try {
            val bytes = pdfExportService.export(item.sheet)
            val resource = StreamResource("${item.title}.pdf",
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
    }
    
    private fun deleteSheet(item: SheetItem) {
        try {
            storageService.delete(item.id)
            loadAllSheets()
            
            Notification.show(
                "✅ Sheet deleted successfully!",
                3000,
                Notification.Position.TOP_END
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS)
            
        } catch (e: Exception) {
            Notification.show(
                "❌ Delete failed: ${e.message}",
                5000,
                Notification.Position.MIDDLE
            ).addThemeVariants(NotificationVariant.LUMO_ERROR)
        }
    }
    
    data class SheetItem(
        val id: Long,
        val title: String,
        val composer: String?,
        val sectionCount: Int,
        val barCount: Int,
        val sheet: MusicSheet
    )
}

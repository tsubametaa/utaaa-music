package com.utaaa.music_sheet_builder.ui.component

import com.utaaa.music_sheet_builder.model.Bar
import com.utaaa.music_sheet_builder.model.Beat
import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.model.Section
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout

/**
 * Component for previewing a music sheet
 */
class SheetPreviewComponent : VerticalLayout() {
    
    private var currentSheet: MusicSheet? = null
    
    init {
        isPadding = false
        isSpacing = true
        setWidthFull()
    }
    
    /**
     * Update the displayed sheet
     */
    fun updateSheet(sheet: MusicSheet) {
        currentSheet = sheet
        refresh()
    }
    
    /**
     * Clear the preview
     */
    fun clear() {
        removeAll()
        currentSheet = null
    }
    
    private fun refresh() {
        removeAll()
        
        val sheet = currentSheet ?: return
        
        // Modern Title Section
        val titleContainer = VerticalLayout().apply {
            isPadding = false
            isSpacing = false
            style.set("gap", "8px")
            style.set("text-align", "center")
            style.set("margin-bottom", "32px")
            style.set("padding", "24px")
            style.set("background", "linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)")
            style.set("border-radius", "20px")
            style.set("border", "1px solid #e0eaff")
        }
        
        // Title with gradient
        titleContainer.add(
            H1(sheet.title).apply {
                style.set("margin", "0")
                addClassName("text-gradient")
                style.set("font-size", "32px")
                style.set("font-weight", "800")
                style.set("letter-spacing", "-0.02em")
            }
        )
        
        // Composer/Arranger with modern badges
        if (sheet.composer != null || sheet.arranger != null) {
            val creditsLayout = HorizontalLayout().apply {
                justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER
                isSpacing = true
                style.set("gap", "12px")
                style.set("margin-top", "12px")
            }
            
            sheet.composer?.let { composer ->
                creditsLayout.add(
                    Span("👤 $composer").apply {
                        addClassName("badge")
                        style.set("background", "white")
                        style.set("color", "#374151")
                        style.set("padding", "6px 14px")
                        style.set("border-radius", "10px")
                        style.set("font-size", "13px")
                        style.set("font-weight", "500")
                        style.set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                    }
                )
            }
            
            sheet.arranger?.let { arranger ->
                creditsLayout.add(
                    Span("🎼 $arranger").apply {
                        addClassName("badge")
                        style.set("background", "white")
                        style.set("color", "#374151")
                        style.set("padding", "6px 14px")
                        style.set("border-radius", "10px")
                        style.set("font-size", "13px")
                        style.set("font-weight", "500")
                        style.set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                    }
                )
            }
            
            titleContainer.add(creditsLayout)
        }
        
        // Stats with icons
        val statsLayout = HorizontalLayout().apply {
            justifyContentMode = com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER
            isSpacing = true
            style.set("gap", "16px")
            style.set("margin-top", "12px")
        }
        
        statsLayout.add(
            Span("📑 ${sheet.getSectionCount()} sections").apply {
                style.set("color", "#6b7280")
                style.set("font-size", "13px")
                style.set("font-weight", "500")
            },
            Span("•").apply {
                style.set("color", "#d1d5db")
            },
            Span("🎹 ${sheet.getTotalBarCount()} bars").apply {
                style.set("color", "#6b7280")
                style.set("font-size", "13px")
                style.set("font-weight", "500")
            }
        )
        
        titleContainer.add(statsLayout)
        add(titleContainer)
        
        // Render each section
        for (section in sheet.sections) {
            add(buildSectionComponent(section))
        }
    }
    
    private fun buildSectionComponent(section: Section): Component {
        val div = VerticalLayout().apply {
            isPadding = false
            style.set("margin-bottom", "40px")
            style.set("background", "linear-gradient(135deg, #f9fafb 0%, #ffffff 100%)")
            style.set("border-radius", "20px")
            style.set("padding", "24px")
            style.set("border", "1px solid #e5e7eb")
            style.set("box-shadow", "0 4px 12px rgba(0,0,0,0.05)")
        }
        
        // Section header with modern styling
        val headerText = buildString {
            append(section.name)
            append("  •  ${section.getTimeSignature()}")
            section.style?.let { append("  •  $it") }
        }
        
        val headerContainer = HorizontalLayout().apply {
            setWidthFull()
            defaultVerticalComponentAlignment = com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER
            style.set("margin-bottom", "20px")
            style.set("padding-bottom", "16px")
            style.set("border-bottom", "2px solid #e0eaff")
        }
        
        val sectionIcon = com.vaadin.flow.component.icon.VaadinIcon.MUSIC.create().apply {
            style.set("color", "#7c6ff2")
            style.set("margin-right", "12px")
        }
        
        val sectionTitle = H3(headerText).apply {
            style.set("margin", "0")
            style.set("font-family", "Inter, sans-serif")
            style.set("font-weight", "700")
            style.set("font-size", "20px")
            addClassName("text-gradient")
        }
        
        headerContainer.add(sectionIcon, sectionTitle)
        div.add(headerContainer)
        
        // Render each bar
        for (bar in section.bars) {
            div.add(buildBarComponent(bar))
        }
        
        return div
    }
    
    private fun buildBarComponent(bar: Bar): Component {
        val div = VerticalLayout().apply {
            style.set("margin-bottom", "20px")
            style.set("page-break-inside", "avoid")
            style.set("background", "white")
            style.set("border-radius", "12px")
            style.set("padding", "16px")
            style.set("border", "1px solid #e5e7eb")
            isPadding = false
        }
        
        // Bar header with chord - modern badge style
        val chordBadge = Span(bar.chord).apply {
            addClassName("badge")
            style.set("background", "linear-gradient(135deg, #7c6ff2 0%, #c026d3 100%)")
            style.set("color", "white")
            style.set("padding", "8px 16px")
            style.set("border-radius", "10px")
            style.set("font-size", "16px")
            style.set("font-weight", "700")
            style.set("box-shadow", "0 4px 12px rgba(124, 111, 242, 0.3)")
        }
        
        val barNumberBadge = Span("Bar ${bar.number}").apply {
            addClassName("badge")
            style.set("background", "#f3f4f6")
            style.set("color", "#6b7280")
            style.set("padding", "6px 12px")
            style.set("border-radius", "8px")
            style.set("font-size", "12px")
            style.set("font-weight", "600")
            style.set("margin-left", "12px")
        }
        
        val headerLayout = HorizontalLayout(chordBadge, barNumberBadge).apply {
            defaultVerticalComponentAlignment = com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER
            style.set("margin-bottom", "12px")
        }
        
        div.add(headerLayout)
        
        // Annotation if present
        bar.annotation?.let { annotation ->
            div.add(
                Paragraph("→ $annotation").apply {
                    style.set("color", "#9ca3af")
                    style.set("font-style", "italic")
                    style.set("font-size", "13px")
                    style.set("margin", "0 0 12px 0")
                    style.set("padding-left", "8px")
                    style.set("border-left", "3px solid #e5e7eb")
                }
            )
        }
        
        // Beat grid
        div.add(buildBeatGrid(bar))
        
        return div
    }
    
    private fun buildBeatGrid(bar: Bar): Component {
        val beatCount = bar.getBeatCount()
        
        val grid = Grid<BeatRow>().apply {
            setItems(
                BeatRow("RH", bar.rightHand, beatCount),
                BeatRow("LH", bar.leftHand, beatCount)
            )
            
            // Hand column with modern styling
            addColumn { it.hand }
                .setHeader("")
                .setWidth("70px")
                .setFlexGrow(0)
                .setRenderer(com.vaadin.flow.data.renderer.ComponentRenderer { row ->
                    Span(row.hand).apply {
                        addClassName("badge")
                        if (row.hand == "RH") {
                            style.set("background", "#fef2f2")
                            style.set("color", "#dc2626")
                        } else {
                            style.set("background", "#eff6ff")
                            style.set("color", "#2563eb")
                        }
                        style.set("padding", "6px 12px")
                        style.set("border-radius", "8px")
                        style.set("font-weight", "700")
                        style.set("font-size", "12px")
                    }
                })
            
            // Beat columns
            for (i in 0 until beatCount) {
                addColumn { row -> row.beatsText.getOrElse(i) { "—" } }
                    .setHeader("${i + 1}")
                    .setAutoWidth(true)
                    .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.CENTER)
            }
            
            addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES)
            style.set("font-size", "14px")
            addClassName("mono")
            style.set("border-radius", "10px")
            style.set("overflow", "hidden")
            setAllRowsVisible(true)
        }
        
        return grid
    }
    
    /**
     * Helper data class for Grid rows
     */
    data class BeatRow(
        val hand: String,
        val beats: List<Beat>,
        val total: Int
    ) {
        val beatsText: List<String> = (0 until total).map { i ->
            when {
                i >= beats.size -> "—"
                beats[i].isRest -> "·"
                beats[i].isHeld -> "—"
                else -> beats[i].notes.joinToString("-")
            }
        }
    }
}

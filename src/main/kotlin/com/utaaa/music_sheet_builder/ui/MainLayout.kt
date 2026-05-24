package com.utaaa.music_sheet_builder.ui

import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.theme.lumo.LumoUtility

/**
 * Modern main layout with enhanced navigation and visual design
 */
class MainLayout : AppLayout() {
    
    init {
        createModernHeader()
        createModernDrawer()
    }
    
    private fun createModernHeader() {
        // Logo with gradient background
        val logoContainer = Div().apply {
            style.set("width", "40px")
            style.set("height", "40px")
            style.set("background", "linear-gradient(135deg, rgba(255,255,255,0.2) 0%, rgba(255,255,255,0.1) 100%)")
            style.set("border-radius", "12px")
            style.set("display", "flex")
            style.set("align-items", "center")
            style.set("justify-content", "center")
            style.set("font-size", "24px")
            style.set("backdrop-filter", "blur(10px)")
            style.set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)")
            add(Span("🎹"))
        }
        
        val title = H2("Music Sheet Builder").apply {
            style.set("margin", "0")
            style.set("color", "white")
            style.set("font-size", "20px")
            style.set("font-weight", "700")
            style.set("letter-spacing", "-0.02em")
        }
        
        val subtitle = Span("Create & Manage Your Music").apply {
            style.set("color", "rgba(255,255,255,0.8)")
            style.set("font-size", "12px")
            style.set("font-weight", "400")
        }
        
        val titleContainer = VerticalLayout(title, subtitle).apply {
            isPadding = false
            isSpacing = false
            style.set("gap", "2px")
        }
        
        // Version badge
        val versionBadge = Span("v1.0").apply {
            addClassNames("badge")
            style.set("background", "rgba(255,255,255,0.2)")
            style.set("color", "white")
            style.set("padding", "4px 12px")
            style.set("border-radius", "20px")
            style.set("font-size", "11px")
            style.set("font-weight", "600")
            style.set("backdrop-filter", "blur(10px)")
        }
        
        val header = HorizontalLayout(DrawerToggle(), logoContainer, titleContainer).apply {
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            isPadding = true
            isSpacing = true
            setWidthFull()
            style.set("background", "linear-gradient(135deg, #7c6ff2 0%, #c026d3 100%)")
            style.set("box-shadow", "0 4px 20px rgba(124, 111, 242, 0.3)")
            style.set("padding", "16px 24px")
            expand(titleContainer)
            add(versionBadge)
        }
        
        addToNavbar(header)
    }
    
    private fun createModernDrawer() {
        val drawer = VerticalLayout().apply {
            isPadding = false
            isSpacing = false
            style.set("background", "linear-gradient(180deg, #ffffff 0%, #f9fafb 100%)")
            style.set("height", "100%")
            style.set("padding", "24px 16px")
        }
        
        // Navigation header
        val navHeader = Span("NAVIGATION").apply {
            style.set("font-size", "11px")
            style.set("font-weight", "700")
            style.set("color", "#9ca3af")
            style.set("letter-spacing", "0.1em")
            style.set("padding", "0 12px")
            style.set("margin-bottom", "12px")
        }
        
        // Editor link with icon
        val editorIcon = VaadinIcon.EDIT.create().apply {
            style.set("color", "#7c6ff2")
        }
        
        val editorLink = RouterLink("Editor", EditorView::class.java).apply {
            style.set("text-decoration", "none")
            style.set("color", "#374151")
            style.set("font-weight", "500")
            style.set("padding", "12px 16px")
            style.set("border-radius", "12px")
            style.set("display", "flex")
            style.set("align-items", "center")
            style.set("gap", "12px")
            style.set("transition", "all 0.2s")
            style.set("margin-bottom", "8px")
            
            element.addEventListener("mouseenter") {
                style.set("background", "linear-gradient(135deg, #f0f4ff 0%, #fdf4ff 100%)")
                style.set("transform", "translateX(4px)")
                style.set("box-shadow", "0 4px 12px rgba(124, 111, 242, 0.15)")
            }
            
            element.addEventListener("mouseleave") {
                style.set("background", "transparent")
                style.set("transform", "translateX(0)")
                style.set("box-shadow", "none")
            }
        }
        
        val editorLinkContent = HorizontalLayout(editorIcon, Span("Editor")).apply {
            isPadding = false
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style.set("gap", "12px")
        }
        editorLink.add(editorLinkContent)
        
        // Library link with icon
        val libraryIcon = VaadinIcon.BOOK.create().apply {
            style.set("color", "#c026d3")
        }
        
        val libraryLink = RouterLink("Library", LibraryView::class.java).apply {
            style.set("text-decoration", "none")
            style.set("color", "#374151")
            style.set("font-weight", "500")
            style.set("padding", "12px 16px")
            style.set("border-radius", "12px")
            style.set("display", "flex")
            style.set("align-items", "center")
            style.set("gap", "12px")
            style.set("transition", "all 0.2s")
            style.set("margin-bottom", "8px")
            
            element.addEventListener("mouseenter") {
                style.set("background", "linear-gradient(135deg, #fdf4ff 0%, #f0f4ff 100%)")
                style.set("transform", "translateX(4px)")
                style.set("box-shadow", "0 4px 12px rgba(192, 38, 211, 0.15)")
            }
            
            element.addEventListener("mouseleave") {
                style.set("background", "transparent")
                style.set("transform", "translateX(0)")
                style.set("box-shadow", "none")
            }
        }
        
        val libraryLinkContent = HorizontalLayout(libraryIcon, Span("Library")).apply {
            isPadding = false
            isSpacing = true
            defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            style.set("gap", "12px")
        }
        libraryLink.add(libraryLinkContent)
        
        // Divider
        val divider = Hr().apply {
            style.set("border", "none")
            style.set("height", "1px")
            style.set("background", "linear-gradient(90deg, transparent 0%, #e5e7eb 50%, transparent 100%)")
            style.set("margin", "20px 0")
        }
        
        // Info card
        val infoCard = Div().apply {
            style.set("background", "linear-gradient(135deg, #7c6ff2 0%, #c026d3 100%)")
            style.set("border-radius", "16px")
            style.set("padding", "20px")
            style.set("margin-top", "auto")
            style.set("box-shadow", "0 10px 25px rgba(124, 111, 242, 0.3)")
        }
        
        val infoIcon = Span("💡").apply {
            style.set("font-size", "32px")
            style.set("margin-bottom", "12px")
            style.set("display", "block")
        }
        
        val infoTitle = H4("Quick Tip").apply {
            style.set("margin", "0 0 8px 0")
            style.set("color", "white")
            style.set("font-size", "16px")
            style.set("font-weight", "700")
        }
        
        val infoText = Paragraph("Upload your music notation files or create them manually in the editor.").apply {
            style.set("margin", "0")
            style.set("color", "rgba(255,255,255,0.9)")
            style.set("font-size", "13px")
            style.set("line-height", "1.6")
        }
        
        infoCard.add(infoIcon, infoTitle, infoText)
        
        drawer.add(navHeader, editorLink, libraryLink, divider, infoCard)
        addToDrawer(drawer)
    }
}

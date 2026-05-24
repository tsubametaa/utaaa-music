package com.utaaa.music_sheet_builder.service

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.utaaa.music_sheet_builder.exception.MusicSheetException
import com.utaaa.music_sheet_builder.model.Bar
import com.utaaa.music_sheet_builder.model.Beat
import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.model.Section
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

/**
 * Service for exporting MusicSheet to PDF format
 */
@Service
class PdfExportService {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Export a music sheet to PDF
     * @param sheet The music sheet to export
     * @return ByteArray containing the PDF data
     */
    fun export(sheet: MusicSheet): ByteArray {
        return try {
            logger.info { "Exporting music sheet to PDF: ${sheet.title}" }
            
            val outputStream = ByteArrayOutputStream()
            val writer = PdfWriter(outputStream)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)
            document.setMargins(30f, 36f, 30f, 36f)
            
            // Create fonts
            val titleFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val headerFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val bodyFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            
            // Add title
            document.add(
                Paragraph(sheet.title)
                    .setFont(titleFont)
                    .setFontSize(22f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(6f)
            )
            
            // Add subtitle
            document.add(
                Paragraph("PIANO SHEET MUSIC")
                    .setFont(bodyFont)
                    .setFontSize(9f)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20f)
            )
            
            // Add composer/arranger if available
            if (sheet.composer != null || sheet.arranger != null) {
                val credits = buildString {
                    sheet.composer?.let { append("Composer: $it") }
                    if (sheet.composer != null && sheet.arranger != null) append("  |  ")
                    sheet.arranger?.let { append("Arranger: $it") }
                }
                document.add(
                    Paragraph(credits)
                        .setFont(bodyFont)
                        .setFontSize(9f)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(16f)
                )
            }
            
            // Add legend
            document.add(
                Paragraph("RH = Right Hand (Tangan Kanan)   |   LH = Left Hand (Tangan Kiri)   |   · = Rest / Diam")
                    .setFont(bodyFont)
                    .setFontSize(8f)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(16f)
            )
            
            // Render each section
            for (section in sheet.sections) {
                renderSection(document, section, headerFont, bodyFont)
            }
            
            // Add footer
            document.add(
                Paragraph("Generated with Music Sheet Builder")
                    .setFont(bodyFont)
                    .setFontSize(8f)
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20f)
            )
            
            document.close()
            
            logger.info { "Successfully exported PDF: ${outputStream.size()} bytes" }
            outputStream.toByteArray()
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to export PDF for sheet: ${sheet.title}" }
            throw MusicSheetException.ExportException("Failed to export PDF: ${e.message}", e)
        }
    }
    
    private fun renderSection(
        document: Document,
        section: Section,
        headerFont: com.itextpdf.kernel.font.PdfFont,
        bodyFont: com.itextpdf.kernel.font.PdfFont
    ) {
        // Section header
        val sectionTitle = buildString {
            append(section.name)
            append("  •  ${section.getTimeSignature()}")
            section.style?.let { append("  •  $it") }
        }
        
        document.add(
            Paragraph(sectionTitle)
                .setFont(headerFont)
                .setFontSize(12f)
                .setBorderBottom(SolidBorder(2f))
                .setMarginBottom(10f)
                .setMarginTop(12f)
        )
        
        // Render each bar
        for (bar in section.bars) {
            renderBar(document, bar, bodyFont, headerFont)
        }
    }
    
    private fun renderBar(
        document: Document,
        bar: Bar,
        bodyFont: com.itextpdf.kernel.font.PdfFont,
        headerFont: com.itextpdf.kernel.font.PdfFont
    ) {
        // Bar label with chord
        val label = buildString {
            append(bar.chord)
            append("   Bar ${bar.number}")
            bar.annotation?.let { append("  —  $it") }
        }
        
        document.add(
            Paragraph(label)
                .setFont(headerFont)
                .setFontSize(11f)
                .setFontColor(DeviceRgb(30, 30, 80))
                .setMarginBottom(4f)
                .setMarginTop(10f)
        )
        
        // Create beat table
        val beatCount = bar.getBeatCount()
        val columnWidths = FloatArray(beatCount + 1) { if (it == 0) 1.5f else 1f }
        val table = Table(UnitValue.createPercentArray(columnWidths))
            .useAllAvailableWidth()
            .setMarginBottom(8f)
        
        // Header row with beat numbers
        table.addHeaderCell(Cell().setBorder(Border.NO_BORDER))
        for (i in 1..beatCount) {
            table.addHeaderCell(
                Cell()
                    .add(
                        Paragraph("$i")
                            .setFont(bodyFont)
                            .setFontSize(8f)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontColor(ColorConstants.GRAY)
                    )
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(DeviceRgb(245, 243, 238))
            )
        }
        
        // Right Hand row
        table.addCell(
            Cell()
                .add(
                    Paragraph("RH")
                        .setFont(headerFont)
                        .setFontSize(9f)
                        .setFontColor(DeviceRgb(180, 30, 30))
                )
                .setBackgroundColor(DeviceRgb(255, 245, 245))
        )
        for (i in 0 until beatCount) {
            val beat = bar.rightHand.getOrNull(i)
            table.addCell(createBeatCell(beat, bodyFont))
        }
        
        // Left Hand row
        table.addCell(
            Cell()
                .add(
                    Paragraph("LH")
                        .setFont(headerFont)
                        .setFontSize(9f)
                        .setFontColor(DeviceRgb(30, 80, 180))
                )
                .setBackgroundColor(DeviceRgb(245, 245, 255))
        )
        for (i in 0 until beatCount) {
            val beat = bar.leftHand.getOrNull(i)
            table.addCell(createBeatCell(beat, bodyFont))
        }
        
        document.add(table)
    }
    
    private fun createBeatCell(
        beat: Beat?,
        bodyFont: com.itextpdf.kernel.font.PdfFont
    ): Cell {
        val text = when {
            beat == null -> "—"
            beat.isRest -> "·"
            beat.isHeld -> "—"
            else -> beat.notes.joinToString("\n")
        }
        
        val color = when {
            beat == null || beat.isRest || beat.isHeld -> ColorConstants.LIGHT_GRAY
            else -> ColorConstants.BLACK
        }
        
        return Cell()
            .add(
                Paragraph(text)
                    .setFont(bodyFont)
                    .setFontSize(9f)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(color)
            )
            .setTextAlignment(TextAlignment.CENTER)
    }
}

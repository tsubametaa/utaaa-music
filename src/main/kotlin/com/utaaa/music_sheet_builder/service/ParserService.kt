package com.utaaa.music_sheet_builder.service

import com.utaaa.music_sheet_builder.exception.MusicSheetException
import com.utaaa.music_sheet_builder.model.*
import mu.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Service for parsing text files into MusicSheet objects
 */
@Service
class ParserService(
    private val validationService: ValidationService
) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Parse text content into a MusicSheet
     * @param text The text content to parse
     * @param title The title for the music sheet
     * @return Result containing either the parsed MusicSheet or an exception
     */
    fun parse(text: String, title: String = "Untitled"): Result<MusicSheet> {
        return try {
            logger.debug { "Starting to parse music sheet: $title" }
            val sheet = parseInternal(text, title)
            
            // Validate the parsed sheet
            val validation = validationService.validate(sheet)
            
            when (validation) {
                is ValidationResult.Success -> {
                    logger.info { "Successfully parsed music sheet: $title with ${sheet.getSectionCount()} sections" }
                    Result.success(sheet)
                }
                is ValidationResult.Error -> {
                    logger.warn { "Validation failed for sheet: $title - ${validation.errors}" }
                    Result.failure(
                        MusicSheetException.ValidationException(validation.getErrorMessage())
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse music sheet: $title" }
            Result.failure(
                MusicSheetException.ParseException("Failed to parse music sheet: ${e.message}", e)
            )
        }
    }
    
    private fun parseInternal(text: String, title: String): MusicSheet {
        val sheet = MusicSheet(title = title)
        val lines = text.lines()
        var currentSection: Section? = null
        var currentBar: Bar? = null
        
        for (line in lines) {
            val trimmed = line.trim()
            
            // Skip empty lines, separators, and comment lines
            if (trimmed.isBlank() || 
                trimmed.startsWith("===") || 
                trimmed.startsWith("Hitungan") || 
                trimmed.startsWith("Pengelompokan") ||
                trimmed.startsWith("#")) {
                continue
            }
            
            // Try to parse as section header
            val section = parseSectionHeader(trimmed)
            if (section != null) {
                currentSection = section
                sheet.sections.add(section)
                logger.debug { "Parsed section: ${section.name}" }
                continue
            }
            
            // Try to parse as bar header
            val bar = parseBarHeader(trimmed)
            if (bar != null && currentSection != null) {
                currentBar = bar
                currentSection.bars.add(bar)
                logger.debug { "Parsed bar ${bar.number} with chord ${bar.chord}" }
                continue
            }
            
            // Try to parse as right hand
            val rhBeats = parseRightHand(trimmed)
            if (rhBeats != null && currentSection != null && currentBar != null) {
                val bars = currentSection.bars
                val idx = bars.indexOfFirst { it.number == currentBar.number }
                if (idx >= 0) {
                    bars[idx] = bars[idx].copy(rightHand = rhBeats)
                    logger.debug { "Added ${rhBeats.size} beats to RH of bar ${currentBar.number}" }
                }
                continue
            }
            
            // Try to parse as left hand
            val lhBeats = parseLeftHand(trimmed)
            if (lhBeats != null && currentSection != null && currentBar != null) {
                val bars = currentSection.bars
                val idx = bars.indexOfFirst { it.number == currentBar.number }
                if (idx >= 0) {
                    bars[idx] = bars[idx].copy(leftHand = lhBeats)
                    logger.debug { "Added ${lhBeats.size} beats to LH of bar ${currentBar.number}" }
                }
            }
        }
        
        return sheet
    }
    
    private fun parseSectionHeader(line: String): Section? {
        val sectionRegex = Regex(
            """BAGIAN\s+(\w+)\s*\(Time Signature:\s*(\d+)/(\d+)\)(?:\s*[-–]\s*(.+))?""",
            RegexOption.IGNORE_CASE
        )
        
        val match = sectionRegex.find(line) ?: return null
        
        return Section(
            id = match.groupValues[1],
            name = "BAGIAN ${match.groupValues[1]}",
            timeSigTop = match.groupValues[2].toInt(),
            timeSigBottom = match.groupValues[3].toInt(),
            style = match.groupValues[4].trim().takeIf { it.isNotBlank() }
        )
    }
    
    private fun parseBarHeader(line: String): Bar? {
        val barRegex = Regex(
            """BAR\s+(\d+)\s*\(Chord:\s*([^)]+)\)(?:\s*[-–>]+\s*(.+))?""",
            RegexOption.IGNORE_CASE
        )
        
        val match = barRegex.find(line) ?: return null
        
        return Bar(
            number = match.groupValues[1].toInt(),
            chord = match.groupValues[2].trim(),
            annotation = match.groupValues[3].trim().takeIf { it.isNotBlank() }
        )
    }
    
    private fun parseRightHand(line: String): List<Beat>? {
        val rhRegex = Regex("""Right Hand\s*\(RH\)\s*:\s*(.+)""", RegexOption.IGNORE_CASE)
        val match = rhRegex.find(line) ?: return null
        return parseHandLine(match.groupValues[1])
    }
    
    private fun parseLeftHand(line: String): List<Beat>? {
        val lhRegex = Regex("""Left Hand\s*\(LH\)\s*:\s*(.+)""", RegexOption.IGNORE_CASE)
        val match = lhRegex.find(line) ?: return null
        return parseHandLine(match.groupValues[1])
    }
    
    private fun parseHandLine(line: String): List<Beat> {
        // Remove annotations in parentheses
        val clean = line.replace(Regex("""\([^)]+\)"""), "").trim()
        
        // Split by pipe (|) for beat grouping
        val parts = clean.split("|")
        
        return parts.flatMap { part ->
            part.trim()
                .split(Regex("""\s+"""))
                .filter { it.isNotBlank() }
                .map { token -> parseBeatToken(token) }
        }
    }
    
    private fun parseBeatToken(token: String): Beat {
        return when {
            token == "." || token == "·" -> Beat(isRest = true)
            token == "-" || token == "—" -> Beat(isHeld = true)
            token.contains("-") -> Beat(notes = token.split("-").filter { it.isNotBlank() })
            else -> Beat(notes = listOf(token))
        }
    }
}

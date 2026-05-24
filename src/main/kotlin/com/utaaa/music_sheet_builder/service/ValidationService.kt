package com.utaaa.music_sheet_builder.service

import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.model.Section
import com.utaaa.music_sheet_builder.model.ValidationResult
import mu.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Service for validating MusicSheet objects
 */
@Service
class ValidationService {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Validate a music sheet
     * @param sheet The music sheet to validate
     * @return ValidationResult indicating success or errors
     */
    fun validate(sheet: MusicSheet): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Check if sheet has sections
        if (sheet.sections.isEmpty()) {
            errors.add("Sheet must have at least one section")
        }
        
        // Validate each section
        sheet.sections.forEach { section ->
            errors.addAll(validateSection(section))
        }
        
        return if (errors.isEmpty()) {
            logger.debug { "Validation successful for sheet: ${sheet.title}" }
            ValidationResult.Success
        } else {
            logger.warn { "Validation failed for sheet: ${sheet.title} with ${errors.size} errors" }
            ValidationResult.Error(errors)
        }
    }
    
    private fun validateSection(section: Section): List<String> {
        val errors = mutableListOf<String>()
        
        // Check if section has bars
        if (section.bars.isEmpty()) {
            errors.add("Section ${section.id} has no bars")
            return errors
        }
        
        // Check time signature validity
        if (section.timeSigTop <= 0 || section.timeSigBottom <= 0) {
            errors.add("Section ${section.id} has invalid time signature: ${section.timeSigTop}/${section.timeSigBottom}")
        }
        
        // Validate each bar
        section.bars.forEach { bar ->
            val expectedBeats = section.timeSigTop
            
            // Check right hand beat count
            if (bar.rightHand.size != expectedBeats) {
                errors.add(
                    "Section ${section.id}, Bar ${bar.number}: Right Hand has ${bar.rightHand.size} beats, expected $expectedBeats"
                )
            }
            
            // Check left hand beat count
            if (bar.leftHand.size != expectedBeats) {
                errors.add(
                    "Section ${section.id}, Bar ${bar.number}: Left Hand has ${bar.leftHand.size} beats, expected $expectedBeats"
                )
            }
            
            // Check if chord is not empty
            if (bar.chord.isBlank()) {
                errors.add("Section ${section.id}, Bar ${bar.number}: Chord is empty")
            }
        }
        
        return errors
    }
    
    /**
     * Validate with lenient mode (warnings instead of errors)
     */
    fun validateLenient(sheet: MusicSheet): ValidationResult {
        val warnings = mutableListOf<String>()
        
        if (sheet.sections.isEmpty()) {
            warnings.add("Sheet has no sections")
        }
        
        sheet.sections.forEach { section ->
            if (section.bars.isEmpty()) {
                warnings.add("Section ${section.id} has no bars")
            }
        }
        
        return if (warnings.isEmpty()) {
            ValidationResult.Success
        } else {
            logger.info { "Lenient validation found ${warnings.size} warnings" }
            ValidationResult.Error(warnings)
        }
    }
}

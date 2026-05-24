package com.utaaa.music_sheet_builder.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.utaaa.music_sheet_builder.entity.MusicSheetEntity
import com.utaaa.music_sheet_builder.exception.MusicSheetException
import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.repository.MusicSheetRepository
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service for storing and retrieving music sheets from database
 */
@Service
@Transactional
class MusicSheetStorageService(
    private val repository: MusicSheetRepository,
    private val objectMapper: ObjectMapper
) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Save a music sheet to database
     * @param sheet The music sheet to save
     * @param userId Optional user ID
     * @return The ID of the saved sheet
     */
    fun save(sheet: MusicSheet, userId: String? = null): Long {
        return try {
            logger.info { "Saving music sheet: ${sheet.title}" }
            
            val entity = MusicSheetEntity(
                title = sheet.title,
                composer = sheet.composer,
                arranger = sheet.arranger,
                content = objectMapper.writeValueAsString(sheet),
                userId = userId,
                sectionCount = sheet.getSectionCount(),
                barCount = sheet.getTotalBarCount(),
                createdAt = sheet.createdAt,
                updatedAt = LocalDateTime.now()
            )
            
            val saved = repository.save(entity)
            logger.info { "Saved music sheet with ID: ${saved.id}" }
            saved.id!!
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to save music sheet: ${sheet.title}" }
            throw MusicSheetException.StorageException("Failed to save music sheet: ${e.message}", e)
        }
    }
    
    /**
     * Update an existing music sheet
     * @param id The ID of the sheet to update
     * @param sheet The updated music sheet
     * @return The updated sheet ID
     */
    fun update(id: Long, sheet: MusicSheet): Long {
        return try {
            logger.info { "Updating music sheet ID: $id" }
            
            val existing = repository.findById(id)
                .orElseThrow { MusicSheetException.NotFoundException("Music sheet not found: $id") }
            
            val updated = existing.copy(
                title = sheet.title,
                composer = sheet.composer,
                arranger = sheet.arranger,
                content = objectMapper.writeValueAsString(sheet),
                sectionCount = sheet.getSectionCount(),
                barCount = sheet.getTotalBarCount(),
                updatedAt = LocalDateTime.now()
            )
            
            repository.save(updated)
            logger.info { "Updated music sheet ID: $id" }
            id
            
        } catch (e: MusicSheetException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Failed to update music sheet ID: $id" }
            throw MusicSheetException.StorageException("Failed to update music sheet: ${e.message}", e)
        }
    }
    
    /**
     * Find a music sheet by ID
     * @param id The sheet ID
     * @return The music sheet or null if not found
     */
    fun findById(id: Long): MusicSheet? {
        return try {
            repository.findById(id).map { entity ->
                objectMapper.readValue(entity.content, MusicSheet::class.java)
            }.orElse(null)
        } catch (e: Exception) {
            logger.error(e) { "Failed to load music sheet ID: $id" }
            null
        }
    }
    
    /**
     * Find all music sheets, optionally filtered by user
     * @param userId Optional user ID filter
     * @return List of (ID, MusicSheet) pairs
     */
    fun findAll(userId: String? = null): List<Pair<Long, MusicSheet>> {
        return try {
            val entities = userId?.let { repository.findByUserId(it) } 
                          ?: repository.findAll()
            
            entities.mapNotNull { entity ->
                try {
                    val sheet = objectMapper.readValue(entity.content, MusicSheet::class.java)
                    entity.id!! to sheet
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to deserialize sheet ID: ${entity.id}" }
                    null
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load music sheets" }
            emptyList()
        }
    }
    
    /**
     * Search music sheets by title
     * @param title Title search term
     * @return List of matching sheets
     */
    fun searchByTitle(title: String): List<Pair<Long, MusicSheet>> {
        return try {
            val entities = repository.findByTitleContainingIgnoreCase(title)
            
            entities.mapNotNull { entity ->
                try {
                    val sheet = objectMapper.readValue(entity.content, MusicSheet::class.java)
                    entity.id!! to sheet
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to deserialize sheet ID: ${entity.id}" }
                    null
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to search music sheets" }
            emptyList()
        }
    }
    
    /**
     * Delete a music sheet by ID
     * @param id The sheet ID to delete
     */
    fun delete(id: Long) {
        try {
            logger.info { "Deleting music sheet ID: $id" }
            repository.deleteById(id)
            logger.info { "Deleted music sheet ID: $id" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete music sheet ID: $id" }
            throw MusicSheetException.StorageException("Failed to delete music sheet: ${e.message}", e)
        }
    }
    
    /**
     * Get recent music sheets
     * @param limit Maximum number of sheets to return
     * @return List of recent sheets
     */
    fun findRecent(limit: Int = 10): List<Pair<Long, MusicSheet>> {
        return try {
            val entities = repository.findRecent().take(limit)
            
            entities.mapNotNull { entity ->
                try {
                    val sheet = objectMapper.readValue(entity.content, MusicSheet::class.java)
                    entity.id!! to sheet
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to deserialize sheet ID: ${entity.id}" }
                    null
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load recent music sheets" }
            emptyList()
        }
    }
}

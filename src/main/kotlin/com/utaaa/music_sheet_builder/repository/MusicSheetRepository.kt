package com.utaaa.music_sheet_builder.repository

import com.utaaa.music_sheet_builder.entity.MusicSheetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * Repository for MusicSheetEntity
 */
@Repository
interface MusicSheetRepository : JpaRepository<MusicSheetEntity, Long> {
    
    /**
     * Find all sheets by user ID
     */
    fun findByUserId(userId: String): List<MusicSheetEntity>
    
    /**
     * Find sheets by title containing (case-insensitive)
     */
    fun findByTitleContainingIgnoreCase(title: String): List<MusicSheetEntity>
    
    /**
     * Find sheets by composer
     */
    fun findByComposer(composer: String): List<MusicSheetEntity>
    
    /**
     * Find sheets created after a certain date
     */
    fun findByCreatedAtAfter(date: LocalDateTime): List<MusicSheetEntity>
    
    /**
     * Find recent sheets (ordered by creation date)
     */
    @Query("SELECT s FROM MusicSheetEntity s ORDER BY s.createdAt DESC")
    fun findRecent(): List<MusicSheetEntity>
    
    /**
     * Count sheets by user
     */
    fun countByUserId(userId: String): Long
}

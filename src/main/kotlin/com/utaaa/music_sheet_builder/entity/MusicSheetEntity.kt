package com.utaaa.music_sheet_builder.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * JPA Entity for storing music sheets in database
 */
@Entity
@Table(name = "music_sheets")
data class MusicSheetEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 255)
    val title: String,
    
    @Column(length = 255)
    val composer: String? = null,
    
    @Column(length = 255)
    val arranger: String? = null,
    
    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String, // JSON serialized MusicSheet
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "user_id", length = 100)
    val userId: String? = null,
    
    @Column(name = "section_count")
    val sectionCount: Int = 0,
    
    @Column(name = "bar_count")
    val barCount: Int = 0
)

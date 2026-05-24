package com.utaaa.music_sheet_builder.model

import java.time.LocalDateTime

/**
 * Represents a complete music sheet document
 * @property title Title of the music sheet
 * @property composer Optional composer name
 * @property arranger Optional arranger name
 * @property sections List of sections in this sheet
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
 */
data class MusicSheet(
    val title: String = "Untitled",
    val composer: String? = null,
    val arranger: String? = null,
    val sections: MutableList<Section> = mutableListOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Get total number of sections
     */
    fun getSectionCount(): Int = sections.size
    
    /**
     * Get total number of bars across all sections
     */
    fun getTotalBarCount(): Int = sections.sumOf { it.getBarCount() }
    
    /**
     * Check if this sheet has any content
     */
    fun hasContent(): Boolean = sections.isNotEmpty() && sections.any { it.hasBars() }
    
    /**
     * Get a section by its ID
     */
    fun getSectionById(id: String): Section? = sections.find { it.id == id }
}

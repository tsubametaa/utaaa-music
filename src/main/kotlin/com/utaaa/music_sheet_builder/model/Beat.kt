package com.utaaa.music_sheet_builder.model

/**
 * Represents a single beat in a bar
 * @property notes List of note names (e.g., ["F", "Ab", "C"])
 * @property isRest True if this beat is a rest (no sound)
 * @property isHeld True if this beat holds the previous note
 */
data class Beat(
    val notes: List<String> = emptyList(),
    val isRest: Boolean = false,
    val isHeld: Boolean = false
) {
    /**
     * Returns a display string for this beat
     */
    fun toDisplayString(): String = when {
        isRest -> "·"
        isHeld -> "—"
        notes.isEmpty() -> "—"
        else -> notes.joinToString("-")
    }
    
    /**
     * Check if this beat has any notes
     */
    fun hasNotes(): Boolean = notes.isNotEmpty() && !isRest && !isHeld
}

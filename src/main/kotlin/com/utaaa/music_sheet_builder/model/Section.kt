package com.utaaa.music_sheet_builder.model

/**
 * Represents a section of a music sheet (e.g., Verse, Chorus)
 * @property id Section identifier (e.g., "A", "B", "INTRO")
 * @property name Display name (e.g., "BAGIAN A", "INTRO")
 * @property timeSigTop Time signature numerator (beats per bar)
 * @property timeSigBottom Time signature denominator (note value)
 * @property style Optional style instruction (e.g., "Staccato", "Groove")
 * @property grouping Optional beat grouping (e.g., [2, 2, 3] for 7/8)
 * @property bars List of bars in this section
 */
data class Section(
    val id: String,
    val name: String,
    val timeSigTop: Int,
    val timeSigBottom: Int,
    val style: String? = null,
    val grouping: List<Int> = emptyList(),
    val bars: MutableList<Bar> = mutableListOf()
) {
    /**
     * Get time signature as a display string
     */
    fun getTimeSignature(): String = "$timeSigTop/$timeSigBottom"
    
    /**
     * Get total number of bars in this section
     */
    fun getBarCount(): Int = bars.size
    
    /**
     * Check if this section has any bars
     */
    fun hasBars(): Boolean = bars.isNotEmpty()
}

package com.utaaa.music_sheet_builder.model

/**
 * Represents a single bar (measure) in a music sheet
 * @property number Bar number within the section
 * @property chord Chord symbol (e.g., "Cmaj7", "Dm7")
 * @property annotation Optional annotation or instruction
 * @property rightHand List of beats for right hand
 * @property leftHand List of beats for left hand
 */
data class Bar(
    val number: Int,
    val chord: String,
    val annotation: String? = null,
    val rightHand: List<Beat> = emptyList(),
    val leftHand: List<Beat> = emptyList()
) {
    /**
     * Get the maximum number of beats between both hands
     */
    fun getBeatCount(): Int = maxOf(rightHand.size, leftHand.size)
    
    /**
     * Check if this bar is complete (has beats in both hands)
     */
    fun isComplete(): Boolean = rightHand.isNotEmpty() && leftHand.isNotEmpty()
}

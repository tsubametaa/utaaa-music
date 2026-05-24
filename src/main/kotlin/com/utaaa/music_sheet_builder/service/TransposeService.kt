package com.utaaa.music_sheet_builder.service

import com.utaaa.music_sheet_builder.model.Bar
import com.utaaa.music_sheet_builder.model.Beat
import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.model.Section
import mu.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Service for transposing music sheets to different keys
 */
@Service
class TransposeService {
    private val logger = KotlinLogging.logger {}
    
    // Note to semitone mapping
    private val noteMap = mapOf(
        "C" to 0, "C#" to 1, "Db" to 1,
        "D" to 2, "D#" to 3, "Eb" to 3,
        "E" to 4, "F" to 5, "F#" to 6,
        "Gb" to 6, "G" to 7, "G#" to 8,
        "Ab" to 8, "A" to 9, "A#" to 10,
        "Bb" to 10, "B" to 11
    )
    
    // Semitone to note mapping (prefer sharps)
    private val sharpNotes = listOf(
        "C", "C#", "D", "D#", "E", "F",
        "F#", "G", "G#", "A", "A#", "B"
    )
    
    // Semitone to note mapping (prefer flats)
    private val flatNotes = listOf(
        "C", "Db", "D", "Eb", "E", "F",
        "Gb", "G", "Ab", "A", "Bb", "B"
    )
    
    /**
     * Transpose a music sheet by a number of semitones
     * @param sheet The music sheet to transpose
     * @param semitones Number of semitones to transpose (positive = up, negative = down)
     * @param useFlats If true, use flat notation; if false, use sharp notation
     * @return Transposed music sheet
     */
    fun transpose(sheet: MusicSheet, semitones: Int, useFlats: Boolean = false): MusicSheet {
        logger.info { "Transposing sheet '${sheet.title}' by $semitones semitones" }
        
        return sheet.copy(
            sections = sheet.sections.map { section ->
                transposeSection(section, semitones, useFlats)
            }.toMutableList()
        )
    }
    
    private fun transposeSection(section: Section, semitones: Int, useFlats: Boolean): Section {
        return section.copy(
            bars = section.bars.map { bar ->
                transposeBar(bar, semitones, useFlats)
            }.toMutableList()
        )
    }
    
    private fun transposeBar(bar: Bar, semitones: Int, useFlats: Boolean): Bar {
        return bar.copy(
            chord = transposeChord(bar.chord, semitones, useFlats),
            rightHand = bar.rightHand.map { transposeBeat(it, semitones, useFlats) },
            leftHand = bar.leftHand.map { transposeBeat(it, semitones, useFlats) }
        )
    }
    
    private fun transposeChord(chord: String, semitones: Int, useFlats: Boolean): String {
        // Match chord root note (e.g., "C", "C#", "Db")
        val noteRegex = Regex("^([A-G][#b]?)")
        val match = noteRegex.find(chord) ?: return chord
        
        val rootNote = match.groupValues[1]
        val suffix = chord.substring(rootNote.length)
        
        val transposedRoot = transposeNote(rootNote, semitones, useFlats)
        return transposedRoot + suffix
    }
    
    private fun transposeBeat(beat: Beat, semitones: Int, useFlats: Boolean): Beat {
        if (beat.isRest || beat.isHeld || beat.notes.isEmpty()) {
            return beat
        }
        
        return beat.copy(
            notes = beat.notes.map { transposeNote(it, semitones, useFlats) }
        )
    }
    
    private fun transposeNote(note: String, semitones: Int, useFlats: Boolean): String {
        // Extract the note name without octave number
        val noteRegex = Regex("^([A-G][#b]?)(\\d*)$")
        val match = noteRegex.find(note) ?: return note
        
        val noteName = match.groupValues[1]
        val octave = match.groupValues[2]
        
        // Get current semitone value
        val currentSemitone = noteMap[noteName] ?: return note
        
        // Calculate new semitone value
        val newSemitone = (currentSemitone + semitones + 12) % 12
        
        // Get new note name
        val noteList = if (useFlats) flatNotes else sharpNotes
        val newNoteName = noteList[newSemitone]
        
        return newNoteName + octave
    }
    
    /**
     * Get the key name after transposition
     * @param originalKey Original key (e.g., "C", "Am")
     * @param semitones Number of semitones to transpose
     * @param useFlats Use flat notation
     * @return Transposed key name
     */
    fun getTransposedKey(originalKey: String, semitones: Int, useFlats: Boolean = false): String {
        return transposeChord(originalKey, semitones, useFlats)
    }
}

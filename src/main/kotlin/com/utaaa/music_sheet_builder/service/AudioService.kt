package com.utaaa.music_sheet_builder.service

import com.utaaa.music_sheet_builder.model.Beat
import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.model.Section
import org.springframework.stereotype.Service

/**
 * Service for converting MusicSheet to audio data
 */
@Service
class AudioService {
    
    /**
     * Convert MusicSheet to audio events for playback
     */
    fun convertToAudioEvents(sheet: MusicSheet, tempo: Int = 120): List<AudioEvent> {
        val events = mutableListOf<AudioEvent>()
        var currentTime = 0.0
        
        sheet.sections.forEach { section ->
            val beatDuration = calculateBeatDuration(tempo, section.timeSigBottom)
            
            section.bars.forEach { bar ->
                // Process right hand and left hand simultaneously
                val maxBeats = maxOf(bar.rightHand.size, bar.leftHand.size)
                
                for (i in 0 until maxBeats) {
                    val rhBeat = bar.rightHand.getOrNull(i)
                    val lhBeat = bar.leftHand.getOrNull(i)
                    
                    // Add right hand notes
                    rhBeat?.let { beat ->
                        if (!beat.isRest && !beat.isHeld && beat.notes.isNotEmpty()) {
                            beat.notes.forEach { note ->
                                events.add(
                                    AudioEvent(
                                        time = currentTime,
                                        note = note,
                                        duration = beatDuration,
                                        velocity = 0.7,
                                        hand = "right"
                                    )
                                )
                            }
                        }
                    }
                    
                    // Add left hand notes
                    lhBeat?.let { beat ->
                        if (!beat.isRest && !beat.isHeld && beat.notes.isNotEmpty()) {
                            beat.notes.forEach { note ->
                                events.add(
                                    AudioEvent(
                                        time = currentTime,
                                        note = note,
                                        duration = beatDuration,
                                        velocity = 0.6,
                                        hand = "left"
                                    )
                                )
                            }
                        }
                    }
                    
                    currentTime += beatDuration
                }
            }
        }
        
        return events
    }
    
    /**
     * Calculate beat duration in seconds based on tempo and time signature
     */
    private fun calculateBeatDuration(tempo: Int, timeSigBottom: Int): Double {
        // tempo is in BPM (beats per minute) for quarter notes
        val quarterNoteDuration = 60.0 / tempo
        
        return when (timeSigBottom) {
            4 -> quarterNoteDuration // quarter note
            8 -> quarterNoteDuration / 2 // eighth note
            2 -> quarterNoteDuration * 2 // half note
            16 -> quarterNoteDuration / 4 // sixteenth note
            else -> quarterNoteDuration
        }
    }
    
    /**
     * Convert note name to MIDI note number
     */
    fun noteToMidi(note: String): Int {
        val noteMap = mapOf(
            "C" to 0, "C#" to 1, "Db" to 1,
            "D" to 2, "D#" to 3, "Eb" to 3,
            "E" to 4,
            "F" to 5, "F#" to 6, "Gb" to 6,
            "G" to 7, "G#" to 8, "Ab" to 8,
            "A" to 9, "A#" to 10, "Bb" to 10,
            "B" to 11
        )
        
        // Parse note (e.g., "C4", "F#5", "Bb3")
        val regex = Regex("([A-G][#b]?)(\\d?)")
        val match = regex.find(note) ?: return 60 // Default to C4
        
        val noteName = match.groupValues[1]
        val octave = match.groupValues[2].toIntOrNull() ?: 4 // Default octave 4
        
        val noteValue = noteMap[noteName] ?: 0
        return (octave + 1) * 12 + noteValue
    }
    
    /**
     * Get total duration of the sheet in seconds
     */
    fun getTotalDuration(sheet: MusicSheet, tempo: Int = 120): Double {
        var totalBeats = 0
        var lastTimeSigBottom = 4
        
        sheet.sections.forEach { section ->
            lastTimeSigBottom = section.timeSigBottom
            section.bars.forEach { bar ->
                val maxBeats = maxOf(bar.rightHand.size, bar.leftHand.size)
                totalBeats += maxBeats
            }
        }
        
        return totalBeats * calculateBeatDuration(tempo, lastTimeSigBottom)
    }
}

/**
 * Represents a single audio event (note on/off)
 */
data class AudioEvent(
    val time: Double,        // Time in seconds
    val note: String,        // Note name (e.g., "C4", "F#5")
    val duration: Double,    // Duration in seconds
    val velocity: Double,    // Volume (0.0 - 1.0)
    val hand: String         // "right" or "left"
)

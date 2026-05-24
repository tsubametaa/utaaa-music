package com.utaaa.music_sheet_builder.service

import com.utaaa.music_sheet_builder.model.Bar
import com.utaaa.music_sheet_builder.model.Beat
import com.utaaa.music_sheet_builder.model.MusicSheet
import com.utaaa.music_sheet_builder.model.Section
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TransposeServiceTest {
    
    private val transposeService = TransposeService()
    
    @Test
    fun `should transpose notes up by 2 semitones`() {
        val sheet = MusicSheet(
            title = "Test",
            sections = mutableListOf(
                Section(
                    id = "A",
                    name = "BAGIAN A",
                    timeSigTop = 4,
                    timeSigBottom = 4,
                    bars = mutableListOf(
                        Bar(
                            number = 1,
                            chord = "C",
                            rightHand = listOf(Beat(notes = listOf("C", "E", "G"))),
                            leftHand = listOf(Beat(notes = listOf("C")))
                        )
                    )
                )
            )
        )
        
        val transposed = transposeService.transpose(sheet, 2)
        
        val bar = transposed.sections[0].bars[0]
        assertEquals("D", bar.chord)
        assertEquals(listOf("D", "F#", "A"), bar.rightHand[0].notes)
        assertEquals(listOf("D"), bar.leftHand[0].notes)
    }
    
    @Test
    fun `should transpose notes down by 3 semitones`() {
        val sheet = MusicSheet(
            title = "Test",
            sections = mutableListOf(
                Section(
                    id = "A",
                    name = "BAGIAN A",
                    timeSigTop = 4,
                    timeSigBottom = 4,
                    bars = mutableListOf(
                        Bar(
                            number = 1,
                            chord = "E",
                            rightHand = listOf(Beat(notes = listOf("E", "G#", "B"))),
                            leftHand = listOf(Beat(notes = listOf("E")))
                        )
                    )
                )
            )
        )
        
        val transposed = transposeService.transpose(sheet, -3)
        
        val bar = transposed.sections[0].bars[0]
        assertEquals("C#", bar.chord)
        assertEquals(listOf("C#", "F", "G#"), bar.rightHand[0].notes)
    }
    
    @Test
    fun `should handle complex chords`() {
        val sheet = MusicSheet(
            title = "Test",
            sections = mutableListOf(
                Section(
                    id = "A",
                    name = "BAGIAN A",
                    timeSigTop = 4,
                    timeSigBottom = 4,
                    bars = mutableListOf(
                        Bar(
                            number = 1,
                            chord = "Cmaj7",
                            rightHand = listOf(Beat(notes = listOf("C"))),
                            leftHand = listOf(Beat(notes = listOf("C")))
                        )
                    )
                )
            )
        )
        
        val transposed = transposeService.transpose(sheet, 5)
        
        val bar = transposed.sections[0].bars[0]
        assertEquals("Fmaj7", bar.chord)
    }
    
    @Test
    fun `should not transpose rest beats`() {
        val sheet = MusicSheet(
            title = "Test",
            sections = mutableListOf(
                Section(
                    id = "A",
                    name = "BAGIAN A",
                    timeSigTop = 4,
                    timeSigBottom = 4,
                    bars = mutableListOf(
                        Bar(
                            number = 1,
                            chord = "C",
                            rightHand = listOf(Beat(isRest = true)),
                            leftHand = listOf(Beat(isHeld = true))
                        )
                    )
                )
            )
        )
        
        val transposed = transposeService.transpose(sheet, 5)
        
        val bar = transposed.sections[0].bars[0]
        assertTrue(bar.rightHand[0].isRest)
        assertTrue(bar.leftHand[0].isHeld)
    }
}

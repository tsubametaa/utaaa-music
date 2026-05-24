package com.utaaa.music_sheet_builder.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ParserServiceTest {
    
    @Autowired
    private lateinit var parserService: ParserService
    
    @Test
    fun `should parse simple music sheet successfully`() {
        val notation = """
            BAGIAN A (Time Signature: 4/4)
            
            BAR 1 (Chord: Cmaj7)
            Right Hand (RH): C E G B
            Left Hand (LH): C . C .
        """.trimIndent()
        
        val result = parserService.parse(notation, "Test Sheet")
        
        assertTrue(result.isSuccess)
        result.onSuccess { sheet ->
            assertEquals("Test Sheet", sheet.title)
            assertEquals(1, sheet.sections.size)
            
            val section = sheet.sections[0]
            assertEquals("A", section.id)
            assertEquals(4, section.timeSigTop)
            assertEquals(4, section.timeSigBottom)
            assertEquals(1, section.bars.size)
            
            val bar = section.bars[0]
            assertEquals(1, bar.number)
            assertEquals("Cmaj7", bar.chord)
            assertEquals(4, bar.rightHand.size)
            assertEquals(4, bar.leftHand.size)
        }
    }
    
    @Test
    fun `should parse multiple sections`() {
        val notation = """
            BAGIAN A (Time Signature: 4/4)
            BAR 1 (Chord: C)
            Right Hand (RH): C E G B
            Left Hand (LH): C . C .
            
            BAGIAN B (Time Signature: 3/4)
            BAR 1 (Chord: Am)
            Right Hand (RH): A C E
            Left Hand (LH): A . .
        """.trimIndent()
        
        val result = parserService.parse(notation, "Multi Section")
        
        assertTrue(result.isSuccess)
        result.onSuccess { sheet ->
            assertEquals(2, sheet.sections.size)
            assertEquals("A", sheet.sections[0].id)
            assertEquals("B", sheet.sections[1].id)
        }
    }
    
    @Test
    fun `should handle rest beats correctly`() {
        val notation = """
            BAGIAN A (Time Signature: 4/4)
            BAR 1 (Chord: C)
            Right Hand (RH): C . E .
            Left Hand (LH): . C . E
        """.trimIndent()
        
        val result = parserService.parse(notation, "Rest Test")
        
        assertTrue(result.isSuccess)
        result.onSuccess { sheet ->
            val bar = sheet.sections[0].bars[0]
            assertTrue(bar.rightHand[1].isRest)
            assertTrue(bar.leftHand[0].isRest)
        }
    }
    
    @Test
    fun `should parse chord notes correctly`() {
        val notation = """
            BAGIAN A (Time Signature: 4/4)
            BAR 1 (Chord: C)
            Right Hand (RH): C-E-G B D-F A
            Left Hand (LH): C . C .
        """.trimIndent()
        
        val result = parserService.parse(notation, "Chord Test")
        
        assertTrue(result.isSuccess)
        result.onSuccess { sheet ->
            val bar = sheet.sections[0].bars[0]
            assertEquals(3, bar.rightHand[0].notes.size) // C-E-G
            assertEquals(listOf("C", "E", "G"), bar.rightHand[0].notes)
        }
    }
}

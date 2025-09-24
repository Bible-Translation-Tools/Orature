package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.junit.Assert.assertEquals
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.audio.metadata.BiblicalReferencesParser.parseBcv

class BcvParserTest {

    @Test
    fun `test valid U23003 references`() {
        // Test with translation, word, and char refs
        assertEquals(BCV("MAT", 2, 1), parseBcv("en-t-wsg.MAT 2:1!3+5"))
        // Test with verse range
        assertEquals(BCV("JHN", 3, 16, 17), parseBcv("JHN3:16-17"))
        // Test with word range
        assertEquals(BCV("MAT", 2, 1), parseBcv("MAT 2:1!3-4"))
        // Test with just book and chapter
        assertEquals(BCV("MAT", 2), parseBcv("MAT 2"))
    }

    @Test
    fun `test various valid formats`() {
        assertEquals(BCV("GEN"), parseBcv("GEN"))
        assertEquals(BCV("EXO", 14), parseBcv("EXO14"))
        assertEquals(BCV("JHN", 3, 16), parseBcv("JHN 3.16"))
        assertEquals(BCV("JHN", 3), parseBcv("JHN3"))
        assertEquals(BCV("JUD", 1), parseBcv("JUD 1-4"))
    }

    @Test
    fun `test invalid references`() {
        // Invalid book code
        assertEquals(null, parseBcv("JOH3:16"))
        // Missing chapter
        assertEquals(null, parseBcv("JHN:16"))
        // Missing verse
        assertEquals(null, parseBcv("JHN3-17"))
        // Invalid format
        assertEquals(null, parseBcv("invalid reference"))
    }
}
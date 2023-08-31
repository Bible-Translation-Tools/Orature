package org.wycliffeassociates.otter.common.domain.narration

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class VerseNodeTest {

    @Test
    fun `length of single sector`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(IntRange(0,1000))

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(1000, verseNode.length)
    }

    @Test
    fun `length of no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(0, verseNode.length)
    }

    @Test
    fun `length of multiple sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(IntRange(0,999))
        sectors.add(IntRange(1000,1999))
        sectors.add(IntRange(2000,2999))

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(3000, verseNode.length)
    }

    @Test
    fun `first frame no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(0, verseNode.firstFrame())
    }

    @Test
    fun `first frame one sector`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(IntRange(1000,1999))

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(1000, verseNode.firstFrame())
    }

    @Test
    fun `first frame two sectors out of order`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(IntRange(3000,3999))
        sectors.add(IntRange(1000,1999))

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(3000, verseNode.firstFrame())
    }

    // TODO: add test lastFrame

    @Test
    fun `add start no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()


        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(0, verseNode.sectors.size)

        verseNode.addStart(1000)

        Assert.assertEquals(1, verseNode.sectors.size)
        Assert.assertEquals(1000..1000, verseNode.sectors.first())
    }


    @Test
    fun `add start one sector`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000..1999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(1, verseNode.sectors.size)

        verseNode.addStart(500)

        Assert.assertEquals(2, verseNode.sectors.size)
        Assert.assertEquals(1000..1999, verseNode.sectors.first())
        Assert.assertEquals(500..500, verseNode.sectors.last())

    }


    @Test
    fun `finalize with no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        try {
            verseNode.finalize(500)
            Assert.fail("Expected illegal state exception")
        } catch (ise: IllegalStateException) {
            // Success: exception was expected
        }
    }

    @Test
    fun `finalize already finalized`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        try {
            verseNode.finalize(500)
            Assert.fail("Expected illegal state exception")
        } catch (ise: IllegalStateException) {
            // Success: exception was expected
        }
    }
}













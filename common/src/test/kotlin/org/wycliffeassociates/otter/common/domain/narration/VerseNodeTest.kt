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

        // QUESTION: is the intended implementation to use exclusive or inclusive vale for end?
        Assert.assertEquals(2997, verseNode.length)
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

    @Test
    fun `last frame no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(0, verseNode.lastFrame())
    }

    @Test
    fun `last frame one sector`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(IntRange(2000,2999))

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(2999, verseNode.lastFrame())
    }

    @Test
    fun `last frame two sectors out of order`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(IntRange(3000,3999))
        sectors.add(IntRange(1000,1999))

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(1999, verseNode.lastFrame())
    }


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


    @Test
    fun `finalize last equals UNPLACED_END`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(0.. -1)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        try {
            verseNode.finalize(500)
            Assert.assertEquals(0..500, sectors.last())
        } catch (ise: IllegalStateException) {
            Assert.fail("Not expecting illegal state exception")
        }
    }

    @Test
    fun `finalize last equals first`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1000)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        try {
            verseNode.finalize(1999)
            Assert.assertEquals(1000..1999, sectors.last())
        } catch (ise: IllegalStateException) {
            Assert.fail("Not expecting illegal state exception")
        }
    }


    @Test fun `take frames from start with no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromStart(1000)

        Assert.assertEquals(0, sectorsTaken.size)
    }


    @Test fun `take frames from start with less frames than needed`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)

        val sectorsCopy = sectors.map { it }
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromStart(2000)

        Assert.assertTrue(sectorsTaken.equals(sectorsCopy))
    }


    @Test fun `take frames from start with same number of frames needed`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(2000 .. 2999)
        sectors.add(3000 .. 3999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromStart(3000)

        val expectedSectorsTaken = mutableListOf<IntRange>()
        expectedSectorsTaken.add(1000.. 1999)
        expectedSectorsTaken.add(2000 .. 2999)
        expectedSectorsTaken.add(3000 .. 3999)

        Assert.assertTrue(sectorsTaken.equals(expectedSectorsTaken))

        Assert.assertEquals(0, verseNode.sectors.size)

    }



    @Test fun `take frames from start with more frames than needed and without splitting a node`() {

        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(2000 .. 2999)
        sectors.add(3000 .. 3999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromStart(1998)

        val expectedSectorsTaken = mutableListOf<IntRange>()
        expectedSectorsTaken.add(1000.. 1999)
        expectedSectorsTaken.add(2000 .. 2999)

        Assert.assertTrue(sectorsTaken.equals(expectedSectorsTaken))

        val expectedVerseNodeSectors = mutableListOf<IntRange>()
        expectedVerseNodeSectors.add(3000 .. 3999)

        Assert.assertTrue(verseNode.sectors.equals(expectedVerseNodeSectors))
    }

    @Test fun `take frames from start with more frames than needed and with splitting a node`() {

        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(2000 .. 2999)
        sectors.add(3000 .. 3999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromStart(2498)

        val expectedSectorsTaken = mutableListOf<IntRange>()
        expectedSectorsTaken.add(1000.. 1999)
        expectedSectorsTaken.add(2000 .. 2999)
        expectedSectorsTaken.add(3000 .. 3500)
        println(sectorsTaken)
        Assert.assertTrue(sectorsTaken.equals(expectedSectorsTaken))

        val expectedVerseNodeSectors = mutableListOf<IntRange>()
        expectedVerseNodeSectors.add(3500 .. 3999)

        Assert.assertTrue(verseNode.sectors.equals(expectedVerseNodeSectors))
    }


    @Test fun `take frames from end with no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromEnd(1000)

        Assert.assertEquals(0, sectorsTaken.size)
    }

    @Test fun `take frames from end with less frames than needed`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        val sectorsCopy = sectors.map { it }
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromEnd(2000)

        Assert.assertTrue(sectorsTaken.equals(sectorsCopy))
    }

    @Test fun `take frames from end with same number of frames needed`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(2000 .. 2999)
        sectors.add(3000 .. 3999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromEnd(3000)

        val expectedSectorsTaken = mutableListOf<IntRange>()
        expectedSectorsTaken.add(1000.. 1999)
        expectedSectorsTaken.add(2000 .. 2999)
        expectedSectorsTaken.add(3000 .. 3999)

        Assert.assertTrue(sectorsTaken.equals(expectedSectorsTaken))

        Assert.assertEquals(0, verseNode.sectors.size)

    }


    @Test fun `take frames from end with more frames than needed and without splitting a node`() {

        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(2000 .. 2999)
        sectors.add(3000 .. 3999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsTaken = verseNode.takeFramesFromEnd(2000)

        // need to test that sectorstaken is as expected
        val expectedSectorsTaken = mutableListOf<IntRange>()
        expectedSectorsTaken.add(2000 .. 2999)
        expectedSectorsTaken.add(3000 .. 3999)


        // QUESTION: Is remaining be decremented each iteration of the loop?
        // It seems to get stuck on 1, and stays stuck until it iterates through the
        // sectors list.
        // NOTE: seems to be splitting node unnecessarily
        Assert.assertTrue(sectorsTaken.equals(expectedSectorsTaken))

        val expectedVerseNodeSectors = mutableListOf<IntRange>()
        expectedVerseNodeSectors.add(1000.. 1999)

        Assert.assertTrue(verseNode.sectors.equals(expectedVerseNodeSectors))

    }

    // TODO: add test for takeFramesFromEnd with node split


    @Test
    fun `add range with empty ranges list`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val emptyList:  List<IntRange> = emptyList()

        verseNode.addRange(emptyList)

        Assert.assertEquals(0, verseNode.sectors.size)
    }

    @Test
    fun `add range with one item in ranges list`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val oneItemList = List(1) {1000..1999}

        verseNode.addRange(oneItemList)

        // QUESTION: is loop in flattenSectors supposed to have a range of [0, size-1)
        // NOTE: should it be [0, size - 1]?
        Assert.assertEquals(1, verseNode.sectors.size)

        Assert.assertEquals(1000, verseNode.sectors.first().first)
    }

    @Test
    fun `add range with multiple items in ordered ranges list that do not have overlapping ranges`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val ranges = mutableListOf<IntRange>()
        ranges.add(1000 .. 1999)
        ranges.add(2000 .. 2999)
        ranges.add(3000 .. 3999)

        verseNode.addRange(ranges)

        // QUESTION: same as "add range with one item in ranges list"
        Assert.assertTrue(verseNode.sectors.equals(ranges))
    }

    // NOTE: test is potentially no longer relevant since we are not sorting the list of ranges anymore
    @Test
    fun `add range with multiple items in unordered ranges list that do not have overlapping ranges`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val ranges = mutableListOf<IntRange>()
        ranges.add(1000 .. 1999)
        ranges.add(3000 .. 3999)
        ranges.add(2000 .. 2999)

        verseNode.addRange(ranges)
        // QUESTION: same as "add range with one item in ranges list"
        Assert.assertTrue(verseNode.sectors.equals(ranges.sortedBy { it.first }))
    }

    // NOTE: test is potentially no longer relevant since we are not flattening the list of ranges anymore
    @Test
    fun `add range with multiple items in ordered ranges list that have overlapping ranges`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val ranges = mutableListOf<IntRange>()
        ranges.add(1000 .. 1999)
        ranges.add(1999 .. 2999)
        ranges.add(3000 .. 3999)

        verseNode.addRange(ranges)
        // QUESTION: same as "add range with one item in ranges list"
        Assert.assertTrue(verseNode.sectors.equals(ranges))
    }

    // NOTE: test is potentially no longer relevant since we are not sorting the list of ranges anymore
    @Test
    fun `add range with multiple items in unordered ranges list that have overlapping ranges`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val ranges = mutableListOf<IntRange>()
        ranges.add(1999 .. 2999)
        ranges.add(1000 .. 1999)
        ranges.add(3000 .. 3999)

        verseNode.addRange(ranges)
        // QUESTION: same as "add range with one item in ranges list"
        Assert.assertTrue(verseNode.sectors.equals(ranges))
    }

    @Test
    fun `clear with no sectors and placed equal to false`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, false, verseMarker, sectors)

        Assert.assertEquals(0, verseNode.sectors.size)
        Assert.assertFalse(verseNode.placed)

        verseNode.clear()

        Assert.assertEquals(0, verseNode.sectors.size)
        Assert.assertFalse(verseNode.placed)
    }

    @Test
    fun `clear with multiple sectors and placed equal to true`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(6000.. 6999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertEquals(2, verseNode.sectors.size)
        Assert.assertTrue(verseNode.placed)

        verseNode.clear()

        Assert.assertEquals(0, verseNode.sectors.size)
        Assert.assertFalse(verseNode.placed)
    }


    @Test
    fun `contains with no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertFalse(verseNode.contains(500))
    }

    @Test
    fun `contains with one sector without frame`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(6000.. 6999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertFalse(verseNode.contains(500))
    }

    @Test
    fun `contains with one sector with frame`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(6000.. 6999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertTrue(verseNode.contains(6250))
    }

    @Test
    fun `contains with multiple sectors without frame`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(3000.. 3999)
        sectors.add(2000.. 2999)
        sectors.add(6000.. 6999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertFalse(verseNode.contains(500))
    }

    @Test
    fun `contains with multiple sectors with frame`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(3000.. 3999)
        sectors.add(2000.. 2999)
        sectors.add(6000.. 6999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        Assert.assertTrue(verseNode.contains(6250))
    }


    @Test
    fun `frames to position with absolute frame out of bounds`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(3000.. 3999)
        sectors.add(2000.. 2999)
        sectors.add(6000.. 6999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)


        try {
            verseNode.framesToPosition(10000)
            Assert.fail("expecting IndexOutOfBoundsException")
        } catch (ise: IndexOutOfBoundsException) {
            // Success: expecting IndexOutOfBoundsException
        }
    }

    @Test
    fun `frames to position with absolute frame in bounds`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(2000.. 2999)
        sectors.add(3000.. 3999)
        sectors.add(6000.. 6999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)


        try {
            Assert.assertEquals(500, verseNode.framesToPosition(2500))
        } catch (ise: IndexOutOfBoundsException) {
            Assert.fail("Not expecting IndexOutOfBoundsException")
        }
    }



    @Test
    fun `absolute frame from offset with one sector and out of bounds frames from start`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)


        try {
            verseNode.absoluteFrameFromOffset(2000)
            Assert.fail("Expecting IndexOutOfBoundsException")
        } catch (ise: IndexOutOfBoundsException) {
            // Success: expecting IndexOutOfBoundsException
        }
    }


    @Test
    fun `absolute frame from offset with one sector and in bounds frames from start`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        val verseNode = VerseNode(0,0, true, verseMarker, sectors)


        try {
            val absoluteFrame = verseNode.absoluteFrameFromOffset(500)
            Assert.assertEquals(1500, absoluteFrame)
        } catch (ise: IndexOutOfBoundsException) {
            Assert.fail("Not expecting IndexOutOfBoundsException")
        }
    }


    @Test
    fun `absolute frame from offset with multiple in order sectors and in bounds frames from start`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(5000.. 5999)
        sectors.add(8000.. 8999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)


        try {
            val absoluteFrame = verseNode.absoluteFrameFromOffset(1500)
            Assert.assertEquals(5501, absoluteFrame)
        } catch (ise: IndexOutOfBoundsException) {
            Assert.fail("Not expecting IndexOutOfBoundsException")
        }
    }

    @Test
    fun `absolute frame from offset with multiple out of order sectors and in bounds frames from start`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(8000.. 8999)
        sectors.add(5000.. 5999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)


        try {
            val absoluteFrame = verseNode.absoluteFrameFromOffset(1500)
            Assert.assertEquals(8501, absoluteFrame)
        } catch (ise: IndexOutOfBoundsException) {
            Assert.fail("Not expecting IndexOutOfBoundsException")
        }
    }

    // TODO: figure out how to hit the second IndexOutOfBoundsException for absoluteFrameFromOffset


    @Test
    fun `get sectors from offset with no sectors`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsFromOffset = verseNode.getSectorsFromOffset(500, 1000)
        Assert.assertEquals(0, sectorsFromOffset.size)
    }

    @Test
    fun `get sectors from offset with negative ftr and does not contains frame position`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(5000.. 5999)
        sectors.add(8000.. 8999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsFromOffset = verseNode.getSectorsFromOffset(500, -1)
        Assert.assertEquals(0, sectorsFromOffset.size)
    }

    @Test
    fun `get sectors from offset positive ftr and does not contain frame position`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(5000.. 5999)
        sectors.add(8000.. 8999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsFromOffset = verseNode.getSectorsFromOffset(500, 1000)
        Assert.assertEquals(0, sectorsFromOffset.size)
    }

    // Starts at a particular offset, then gets the number of sectors up to the framesToRead
    // Starting from some offset, how many sectors do I encounter while trying to read "framesToRead" number of frames
    @Test
    fun `get sectors from offset with framePosition equal to first sector's start and ftr less than first sector's end`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(5000.. 5999)
        sectors.add(8000.. 8999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsFromOffset = verseNode.getSectorsFromOffset(1000, 300)
        Assert.assertEquals(1, sectorsFromOffset.size)
        Assert.assertEquals(1000 .. 1299, sectorsFromOffset.first())

    }

    @Test
    fun `get sectors from offset with framePosition equal to first sector's start and ftr greater than first sector's end`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(5000.. 5999)
        sectors.add(8000.. 8999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsFromOffset = verseNode.getSectorsFromOffset(1000, 1300)
        Assert.assertEquals(2, sectorsFromOffset.size)
        // NOTE: making the returned list exclusive could possibly result in some unexpected return values
        Assert.assertEquals(1000 .. 1998, sectorsFromOffset[0])
        Assert.assertEquals(5000 .. 5301, sectorsFromOffset[1])
    }

    @Test
    fun `get sectors from offset with framePosition equal to first sector's start and ftr greater than sector's length`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(5000.. 5999)
        sectors.add(8000.. 8999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsFromOffset = verseNode.getSectorsFromOffset(1000, 4000)
        Assert.assertEquals(3, sectorsFromOffset.size)
        // NOTE: making the returned list exclusive could possibly result in some unexpected return values
        Assert.assertEquals(1000 .. 1998, sectorsFromOffset[0])
        Assert.assertEquals(5000 .. 5998, sectorsFromOffset[1])
        Assert.assertEquals(8000 .. 8998, sectorsFromOffset[2])
    }


    @Test
    fun `get sectors from offset with framePosition not equal to first sector's start and ftr less than sector's length`() {
        val verseMarker = VerseMarker(1, 1, 0)
        val sectors = mutableListOf<IntRange>()
        sectors.add(1000.. 1999)
        sectors.add(5000.. 5999)
        sectors.add(8000.. 8999)

        val verseNode = VerseNode(0,0, true, verseMarker, sectors)

        val sectorsFromOffset = verseNode.getSectorsFromOffset(1500, 1000)
        Assert.assertEquals(2, sectorsFromOffset.size)
        // NOTE: making the returned list exclusive could possibly result in some unexpected return values
        Assert.assertEquals(1500 .. 1998, sectorsFromOffset[0])
        Assert.assertEquals(5000 .. 5501, sectorsFromOffset[1])
    }
}













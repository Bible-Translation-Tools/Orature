package org.bibletranslationtools.kotlinscripturealignment

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsList
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsMap
import org.bibletranslationtools.kotlinscripturealignment.model.FormatType

class BurritoAudioAlignmentTest {

    @Test
    fun testLoadAudioExample1() {
        val resource = javaClass.classLoader.getResource("audio-example1.json")
        val file = File(resource!!.file)
        val alignment = BurritoAudioAlignment.load(file)

        assertEquals(FormatType.ALIGNMENT, alignment.format)
        assertEquals("0.3", alignment.version)
        assertEquals("audio-reference", alignment.type)
        assertEquals(2, (alignment.documents as? DocumentsList)?.list?.size)
        assertEquals("vtt-timecode", (alignment.documents as? DocumentsList)?.list?.get(0)?.scheme)
        assertEquals("ephesians_example_with_footnotes.mp3", (alignment.documents as? DocumentsList)?.list?.get(0)?.docid)
        assertEquals("u23003", (alignment.documents as? DocumentsList)?.list?.get(1)?.scheme)
        assertEquals(null, (alignment.documents as? DocumentsList)?.list?.get(1)?.docid)
        assertEquals(52, alignment.records.size)
    }

    @Test
    fun testLoadAudioExample2() {
        val resource = javaClass.classLoader.getResource("audio-example2.json")
        val file = File(resource!!.file)
        val alignment = BurritoAudioAlignment.load(file)

        assertEquals(FormatType.ALIGNMENT, alignment.format)
        assertEquals("0.3", alignment.version)
        assertEquals("audio-reference", alignment.type)
        // Documents should be a Map for audio-example2.json
        val documentsMap = alignment.documents as? DocumentsMap
        assertNotNull(documentsMap)
        assertEquals(2, documentsMap?.map?.size)
        assertEquals("vtt-timecode", documentsMap?.map?.get("timecode")?.scheme)
        assertEquals("ephesians_example_with_footnotes.mp3", documentsMap?.map?.get("timecode")?.docid)
        assertEquals("u23003", documentsMap?.map?.get("text-reference")?.scheme)
        assertEquals(null, documentsMap?.map?.get("text-reference")?.docid)
        assertNull(alignment.roles)
        assertEquals(52, alignment.records.size)
        assertEquals(listOf("00:00:00.000 --> 00:00:01.927"), alignment.records[0].timecode)
        assertEquals(listOf("en+ulb.EPH:0"), alignment.records[0].textReference)
    }

    @Test
    fun testLoadApmExample() {
        val resource = javaClass.classLoader.getResource("apm_example.json")
        val file = File(resource!!.file)
        val alignment = BurritoAudioAlignment.load(file)

        assertEquals(FormatType.ALIGNMENT, alignment.format)
        assertEquals("0.4", alignment.version)
        assertEquals("audio-reference", alignment.type)
        assertEquals(listOf("timecode", "text-reference"), alignment.roles)

        assertNotNull(alignment.groups)
        assertEquals(15, alignment.groups?.size)

        val firstGroup = alignment.groups?.get(0)
        assertNotNull(firstGroup)
        assertEquals(2, (firstGroup?.documents as? DocumentsList)?.list?.size)
        assertEquals("vtt-timecode", (firstGroup?.documents as? DocumentsList)?.list?.get(0)?.scheme)
        assertEquals("42LUK/001/SEHSAM-LUK-1_1-4v1.mp3", (firstGroup?.documents as? DocumentsList)?.list?.get(0)?.docid)
        assertEquals("u23003", (firstGroup?.documents as? DocumentsList)?.list?.get(1)?.scheme)
        assertNull((firstGroup?.documents as? DocumentsList)?.list?.get(1)?.docid)

        assertEquals(4, firstGroup?.records?.size)
        assertEquals(listOf(listOf("000:00:00.000 --> 000:00:05.860"), listOf("LUK 1:1")), firstGroup?.records?.get(0)?.references)
        assertEquals(listOf(listOf("000:00:05.860 --> 000:00:12.268"), listOf("LUK 1:2")), firstGroup?.records?.get(1)?.references)
        assertEquals(listOf(listOf("000:00:12.268 --> 000:00:24.129"), listOf("LUK 1:3")), firstGroup?.records?.get(2)?.references)
        assertEquals(listOf(listOf("000:00:24.129 --> 000:00:28.328"), listOf("LUK 1:4")), firstGroup?.records?.get(3)?.references)

        val seventhGroup = alignment.groups?.get(6)
        assertNotNull(seventhGroup)
        assertEquals(0, seventhGroup?.records?.size)
    }
}

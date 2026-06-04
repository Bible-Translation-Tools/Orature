package org.bibletranslationtools.kotlinscripturealignment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.Documents
import org.bibletranslationtools.kotlinscripturealignment.model.FormatType
import org.bibletranslationtools.kotlinscripturealignment.serializers.BurritoAudioAlignmentSerializer
import org.bibletranslationtools.kotlinscripturealignment.serializers.DocumentsSerializer
import org.bibletranslationtools.kotlinscripturealignment.serializers.GroupSerializer
import org.bibletranslationtools.kotlinscripturealignment.serializers.RecordSerializer
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsMap
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentReference
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsList
import org.bibletranslationtools.vtt.Cue
import org.bibletranslationtools.vtt.WebVttCue
import org.bibletranslationtools.vtt.WebVttDocument
import org.bibletranslationtools.vtt.WebvttCueInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File

class BurritoAudioAlignmentVttApiTest {

    // data class TestWebVttCueInfo(val cue: Cue, val startTimeUs: Long, val endTimeUs: Long)

    private val mapper = ObjectMapper().registerKotlinModule().apply {
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
        setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
        configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
        val module = com.fasterxml.jackson.databind.module.SimpleModule()
        module.addSerializer(Documents::class.java, DocumentsSerializer())
        module.addSerializer(BurritoAudioAlignment::class.java, BurritoAudioAlignmentSerializer())
        module.addSerializer(org.bibletranslationtools.kotlinscripturealignment.model.Group::class.java, GroupSerializer())
        module.addSerializer(org.bibletranslationtools.kotlinscripturealignment.model.Record::class.java, RecordSerializer())
        registerModule(module)
    }

    @Test
    fun testGetVttCuesFromAudioExample1() {
        val timingFile = File("src/test/resources/audio-example1.json")
        val alignment = BurritoAudioAlignment.Companion.load(timingFile)
        val docid = "ephesians_example_with_footnotes.mp3"
        val vttCues = alignment.getVttCues(docid)

        assertNotNull(vttCues)
        assertTrue(vttCues.isNotEmpty())
        assertEquals(52, vttCues.size) // As per previous tests

        // Assert specific cue content for the first few cues
        val firstCue = vttCues[0]
        assertEquals(0L, firstCue.cue.startTimeUs)
        assertEquals(1927000L, firstCue.cue.endTimeUs)
        assertEquals("en+ulb.EPH:0", firstCue.tag)
        assertEquals("en+ulb.EPH:0", firstCue.content)

        val secondCue = vttCues[1]
        assertEquals(1927000L, secondCue.cue.startTimeUs)
        assertEquals(3756000L, secondCue.cue.endTimeUs)
        assertEquals("en+ulb.EPH 1:0",secondCue.tag)
        assertEquals("en+ulb.EPH 1:0",secondCue.content)
    }

    @Test
    fun testGetVttCuesFromApmExample() {
        val timingFile = File("src/test/resources/apm_example.json")
        val alignment = BurritoAudioAlignment.Companion.load(timingFile)
        val docid = "42LUK/001/SEHSAM-LUK-1_1-4v1.mp3"
        val vttCues = alignment.getVttCues(docid)

        assertNotNull(vttCues)
        assertTrue(vttCues.isNotEmpty())
        assertEquals(4, vttCues.size) // Number of records in the first group of apm_example.json

        val firstCue = vttCues[0]
        assertEquals(0L, firstCue.cue.startTimeUs)
        assertEquals(5860000L, firstCue.cue.endTimeUs)
        assertEquals("LUK 1:1", firstCue.tag)
        assertEquals("LUK 1:1", firstCue.content)
    }

    @Test
    fun testGetVttCuesFromNonExistentDocidThrowsException() {
        val timingFile = File("src/test/resources/audio-example1.json")
        val alignment = BurritoAudioAlignment.Companion.load(timingFile)
        val nonExistentDocid = "non_existent.mp3"

        val exception = assertThrows(IllegalArgumentException::class.java) {
            alignment.getVttCues(nonExistentDocid)
        }
        assertEquals("No top-level vtt-timecode document found for docid: $nonExistentDocid", exception.message)
    }

    @Test
    fun testSetRecordsFromVttCueContent() {
        // Create some dummy VTT cues
        val vttCues = listOf(
            WebVttDocument.WebVttCueContent(
                tag = "v1",
                content = "v1",
                cue = WebVttCue(WebvttCueInfo(Cue.Builder().build(), 0L, 1000000L))
            ),
            WebVttDocument.WebVttCueContent(
                tag = "v2",
                content = "v2",
                cue = WebVttCue(WebvttCueInfo(Cue.Builder().build(), 1000000L, 2000000L))
            )
        )

        val alignment = BurritoAudioAlignment(FormatType.ALIGNMENT, "0.3", "audio-reference", null, listOf(), listOf(), null)
        val docid = "ephesians_example_with_footnotes.mp3"
        alignment.documents = DocumentsMap(mapOf("timecode" to DocumentReference("vtt-timecode", docid)))

        alignment.setRecordsFromVttCueContent(docid, vttCues)

        assertNotNull(alignment.records)
        assertEquals(2, alignment.records.size)

        val firstRecord = alignment.records[0]
        assertEquals(listOf("00:00:00.000 --> 00:00:01.000"), firstRecord.timecode)
        assertEquals(listOf("v1"), firstRecord.textReference)

        val secondRecord = alignment.records[1]
        assertEquals(listOf("00:00:01.000 --> 00:00:02.000"), secondRecord.timecode)
        assertEquals(listOf("v2"), secondRecord.textReference)
    }

    @Test
    fun testSetRecordsFromVttCueContentWithGroups() {
        val timingFile = File("src/test/resources/apm_example.json")
        val originalAlignment = BurritoAudioAlignment.Companion.load(timingFile)
        val docid = "42LUK/001/SEHSAM-LUK-1_1-4v1.mp3"

        // Create some dummy VTT cues
        val vttCues = listOf(
            WebVttDocument.WebVttCueContent(
                tag = "new_v1",
                content = "new_v1",
                cue = WebVttCue(WebvttCueInfo(Cue.Builder().build(), 0L, 500000L))
            )
        )

        originalAlignment.setRecordsFromVttCueContent(docid, vttCues)

        // Verify that the target group's records are updated, and other groups are untouched
        assertNotNull(originalAlignment.groups)
        assertEquals(15, originalAlignment.groups!!.size) // Total number of groups remains the same

        val targetGroup = originalAlignment.groups!!.first { group ->
            val groupDocs = group.documents
            when (groupDocs) {
                is DocumentsList -> groupDocs.list.any { it.scheme == "vtt-timecode" && it.docid == docid }
                is DocumentsMap -> groupDocs.map.any { (key, docRef) -> docRef.scheme == "vtt-timecode" && docRef.docid == docid }
                else -> false
            }
        }

        assertEquals(1, targetGroup.records.size)
        val updatedRecord = targetGroup.records.first()
        assertEquals(listOf("00:00:00.000 --> 00:00:00.500"), updatedRecord.timecode)
        assertEquals(listOf("new_v1"), updatedRecord.textReference)

        // Verify a different group remains unchanged
        val otherGroupDocid = "42LUK/001/SEHSAM-LUK-1_5-7v1.mp3"
        val otherGroup = originalAlignment.groups!!.first { group ->
            val groupDocs = group.documents
            when (groupDocs) {
                is DocumentsList -> groupDocs.list.any { it.scheme == "vtt-timecode" && it.docid == otherGroupDocid }
                is DocumentsMap -> groupDocs.map.any { (key, docRef) -> docRef.scheme == "vtt-timecode" && docRef.docid == otherGroupDocid }
                else -> false
            }
        }
        assertEquals(3, otherGroup.records.size) // Records in this group should be original size
    }

    @Test
    fun testSetRecordsFromNonExistentDocidThrowsException() {
        val timingFile = File("src/test/resources/audio-example1.json")
        val alignment = BurritoAudioAlignment.Companion.load(timingFile)
        val nonExistentDocid = "non_existent.mp3"

        val vttCues = listOf(
            WebVttDocument.WebVttCueContent(
                tag = "v1",
                content = "v1",
                cue = WebVttCue(WebvttCueInfo(Cue.Builder().build(), 0L, 1000000L))
            )
        )

        val exception = assertThrows(IllegalArgumentException::class.java) {
            alignment.setRecordsFromVttCueContent(nonExistentDocid, vttCues)
        }
        assertEquals("No top-level vtt-timecode document found for docid: $nonExistentDocid for setting records.", exception.message)
    }

    @Test
    fun testVttRoundTripForAudioExample1() {
        val timingFile = File("src/test/resources/audio-example1.json")
        val originalAlignment = BurritoAudioAlignment.Companion.load(timingFile)
        val docid = "ephesians_example_with_footnotes.mp3"

        // Normalize the original alignment to the cue/text-reference format
        val normalizedOriginalAlignment = BurritoAudioAlignment(
            originalAlignment.format,
            originalAlignment.version,
            originalAlignment.type,
            originalAlignment.documents,
            originalAlignment.roles,
            listOf(), // Start with empty records
            originalAlignment.groups
        )
        normalizedOriginalAlignment.setRecordsFromVttCueContent(docid, originalAlignment.getVttCues(docid))

        // 1. Get VTT cues from original alignment
        val vttCues = originalAlignment.getVttCues(docid)

        // 2. Create a new alignment and set records from VTT cues
        val newAlignment = BurritoAudioAlignment(
            originalAlignment.format,
            originalAlignment.version,
            originalAlignment.type,
            originalAlignment.documents,
            originalAlignment.roles,
            listOf(), // Start with empty records
            originalAlignment.groups
        )
        newAlignment.setRecordsFromVttCueContent(docid, vttCues)

        // 3. Serialize both to JSON and compare
        val originalJsonNode = mapper.readTree(mapper.writeValueAsString(normalizedOriginalAlignment))
        val newJsonNode = mapper.readTree(mapper.writeValueAsString(newAlignment))

        assertEquals(originalJsonNode, newJsonNode)
    }

    @Test
    fun testVttRoundTripForAudioExample3() {
        val timingFile = File("src/test/resources/audio-example3.json")
        val originalAlignment = BurritoAudioAlignment.Companion.load(timingFile)
        val docid = "en_ulb_psa_c117.mp3"

        // Normalize the original alignment to the cue/text-reference format
        val normalizedOriginalAlignment = BurritoAudioAlignment(
            originalAlignment.format,
            originalAlignment.version,
            originalAlignment.type,
            originalAlignment.documents,
            originalAlignment.roles,
            listOf(), // Start with empty records
            originalAlignment.groups
        )
        normalizedOriginalAlignment.setRecordsFromVttCueContent(docid, originalAlignment.getVttCues(docid))

        // 1. Get VTT cues from original alignment
        val vttCues = originalAlignment.getVttCues(docid)

        // 2. Create a new alignment and set records from VTT cues
        val newAlignment = BurritoAudioAlignment(
            originalAlignment.format,
            originalAlignment.version,
            originalAlignment.type,
            originalAlignment.documents,
            originalAlignment.roles,
            listOf(), // Start with empty records
            originalAlignment.groups
        )
        newAlignment.setRecordsFromVttCueContent(docid, vttCues)

        // 3. Serialize both to JSON and compare
        val originalJsonNode = mapper.readTree(mapper.writeValueAsString(normalizedOriginalAlignment))
        val newJsonNode = mapper.readTree(mapper.writeValueAsString(newAlignment))

        assertEquals(originalJsonNode, newJsonNode)
    }

    @Test
    fun testVttRoundTripForApmExample() {
        val timingFile = File("src/test/resources/apm_example.json")
        val originalAlignment = BurritoAudioAlignment.Companion.load(timingFile)
        val docid = "42LUK/001/SEHSAM-LUK-1_1-4v1.mp3"

        // Normalize the original alignment to the cue/text-reference format
        val normalizedOriginalAlignment = BurritoAudioAlignment(
            originalAlignment.format,
            originalAlignment.version,
            originalAlignment.type,
            originalAlignment.documents,
            originalAlignment.roles,
            listOf(), // Start with empty records
            originalAlignment.groups
        )
        normalizedOriginalAlignment.setRecordsFromVttCueContent(docid, originalAlignment.getVttCues(docid))

        // 1. Get VTT cues from original alignment
        val vttCues = originalAlignment.getVttCues(docid)

        // 2. Create a new alignment and set records from VTT cues
        val newAlignment = BurritoAudioAlignment(
            originalAlignment.format,
            originalAlignment.version,
            originalAlignment.type,
            originalAlignment.documents,
            originalAlignment.roles,
            listOf(), // Start with empty records
            originalAlignment.groups
        )
        newAlignment.setRecordsFromVttCueContent(docid, vttCues)

        // 3. Serialize both to JSON and compare
        val originalJsonNode = mapper.readTree(mapper.writeValueAsString(normalizedOriginalAlignment))
        val newJsonNode = mapper.readTree(mapper.writeValueAsString(newAlignment))

        assertEquals(originalJsonNode, newJsonNode)
    }

    @Test
    fun testGetAllDocids() {
        val alignment1 = BurritoAudioAlignment.Companion.load(File("src/test/resources/audio-example1.json"))
        val docids1 = alignment1.getAllDocids()
        assertEquals(1, docids1.size)
        assertTrue(docids1.contains("ephesians_example_with_footnotes.mp3"))

        val alignment3 = BurritoAudioAlignment.Companion.load(File("src/test/resources/audio-example3.json"))
        val docids3 = alignment3.getAllDocids()
        assertEquals(1, docids3.size)
        assertTrue(docids3.contains("en_ulb_psa_c117.mp3"))

        val apmAlignment = BurritoAudioAlignment.Companion.load(File("src/test/resources/apm_example.json"))
        val apmDocids = apmAlignment.getAllDocids()
        assertEquals(15, apmDocids.size)
        assertTrue(apmDocids.contains("42LUK/001/SEHSAM-LUK-1_1-4v1.mp3"))
        assertTrue(apmDocids.contains("42LUK/001/SEHSAM-LUK-1_67-80v1.mp3"))
    }
}

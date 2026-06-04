package org.bibletranslationtools.kotlinscripturealignment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment

class BurritoAudioAlignmentRecordsFieldTest {

    @Test
    fun testEmptyRecordsList() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": [],
                "records": []
            }
        """
        val tempFile = File.createTempFile("empty_records_list", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertNotNull(alignment.records)
        assertEquals(0, alignment.records.size)

        tempFile.delete()
    }

    @Test
    fun testRecordsWithCueAndTextReference() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": [],
                "records": [
                    {
                        "timecode": ["00:00:00.000 --> 00:00:01.000"],
                        "text-reference": ["text-ref-1"]
                    }
                ]
            }
        """
        val tempFile = File.createTempFile("records_cue_text_ref", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertEquals(1, alignment.records.size)
        val record = alignment.records.first()
        assertEquals(listOf("00:00:00.000 --> 00:00:01.000"), record.timecode)
        assertEquals(listOf("text-ref-1"), record.textReference)
        assertTrue(record.references.isEmpty())

        tempFile.delete()
    }

    @Test
    fun testRecordsWithReferences() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": [],
                "records": [
                    {
                        "references": [["00:00:00.000 --> 00:00:01.000"], ["text-ref-1"]]
                    }
                ]
            }
        """
        val tempFile = File.createTempFile("records_references", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertEquals(1, alignment.records.size)
        val record = alignment.records.first()
        assertEquals(listOf(listOf("00:00:00.000 --> 00:00:01.000"), listOf("text-ref-1")), record.references)
        assertEquals(null, record.timecode)
        assertEquals(null, record.textReference)

        tempFile.delete()
    }
}

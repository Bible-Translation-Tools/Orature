package org.bibletranslationtools.kotlinscripturealignment

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsList
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsMap
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentReference

class BurritoAudioAlignmentDocumentsFieldTest {

    @Test
    fun testDocumentsAsEmptyList() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": [],
                "records": []
            }
        """
        val tempFile = File.createTempFile("empty_documents_list", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertNotNull(alignment.documents)
        assertTrue(alignment.documents is DocumentsList)
        assertEquals(0, (alignment.documents as DocumentsList).list.size)

        tempFile.delete()
    }

    @Test
    fun testDocumentsAsEmptyMap() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": {},
                "records": []
            }
        """
        val tempFile = File.createTempFile("empty_documents_map", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertNotNull(alignment.documents)
        assertTrue(alignment.documents is DocumentsMap)
        assertEquals(0, (alignment.documents as DocumentsMap).map.size)

        tempFile.delete()
    }

    @Test
    fun testDocumentReferenceWithMissingScheme() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": [
                    {
                        "docid": "audio.mp3"
                    }
                ],
                "records": []
            }
        """
        val tempFile = File.createTempFile("doc_ref_missing_scheme", ".json")
        tempFile.writeText(json)

        // Expect MismatchedInputException because 'scheme' is a non-nullable property
        assertThrows(MismatchedInputException::class.java) { BurritoAudioAlignment.load(tempFile) }

        tempFile.delete()
    }

    @Test
    fun testDocumentReferenceWithMissingDocid() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": [
                    {
                        "scheme": "vtt-timecode"
                    }
                ],
                "records": []
            }
        """
        val tempFile = File.createTempFile("doc_ref_missing_docid", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertNotNull(alignment.documents)
        assertTrue(alignment.documents is DocumentsList)
        val docRef = (alignment.documents as DocumentsList).list.first()
        assertEquals("vtt-timecode", docRef.scheme)
        assertNull(docRef.docid)

        tempFile.delete()
    }
}

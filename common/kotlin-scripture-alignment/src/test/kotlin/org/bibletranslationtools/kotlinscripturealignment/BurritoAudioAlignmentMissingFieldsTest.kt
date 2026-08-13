package org.bibletranslationtools.kotlinscripturealignment

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.FormatType
import org.bibletranslationtools.kotlinscripturealignment.model.Record

class BurritoAudioAlignmentMissingFieldsTest {

    @Test
    fun testMissingTypeFieldThrowsException() {
        val jsonWithoutType = """
            {
                "format": "alignment",
                "version": "0.3",
                "documents": [
                    {
                        "scheme": "vtt-timecode",
                        "docid": "audio.mp3"
                    }
                ],
                "records": []
            }
        """
        val tempFile = File.createTempFile("missing_type_audio", ".json")
        tempFile.writeText(jsonWithoutType)

        // Expect MismatchedInputException because 'type' is a non-nullable property without a default value
        assertThrows(MismatchedInputException::class.java) { BurritoAudioAlignment.load(tempFile) }

        tempFile.delete()
    }

    @Test
    fun testMissingOptionalFieldsUsesDefaults() {
        val jsonMinimal = """
            {
                "type": "audio-reference"
            }
        """
        val tempFile = File.createTempFile("minimal_audio", ".json")
        tempFile.writeText(jsonMinimal)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertEquals(FormatType.ALIGNMENT, alignment.format)
        assertEquals("0.3", alignment.version)
        assertEquals("audio-reference", alignment.type)
        // Documents, roles, records, groups should be null or empty list by default
        assertEquals(null, alignment.documents)
        assertEquals(null, alignment.roles)
        assertEquals(listOf<Record>(), alignment.records)
        assertEquals(null, alignment.groups)

        tempFile.delete()
    }
}

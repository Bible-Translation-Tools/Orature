package org.bibletranslationtools.kotlinscripturealignment

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment

class BurritoAudioAlignmentMalformedJsonTest {

    @Test
    fun testMalformedJsonThrowsException() {
        val malformedJson = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "documents": [
                    {
                        "scheme": "vtt-timecode",
                        "docid": "audio.mp3"
                    }
                ],
                "records": [
                    {
                        "timecode": ["00:00:00.000 --> 00:00:01.000"],
                        "text-reference": ["text-ref-1"]
                    },
                    // Missing comma here
                    {
                        "timecode": ["00:00:01.000 --> 00:00:02.000"],
                        "text-reference": ["text-ref-2"]
                    }
                ]
            }
        """

        val tempFile = File.createTempFile("malformed_audio", ".json")
        tempFile.writeText(malformedJson)

        assertThrows(JsonParseException::class.java) { BurritoAudioAlignment.load(tempFile) }

        tempFile.delete()
    }
}

package org.bibletranslationtools.kotlinscripturealignment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsList
import org.bibletranslationtools.kotlinscripturealignment.model.FormatType

class BurritoAudioAlignmentCreateFunctionTest {

    private val mapper = ObjectMapper().registerKotlinModule()

     @Test
     fun testCreateFunctionSerialization() {
         val dummyAudioFile = File.createTempFile("dummy_audio", ".mp3")
         val dummyTimingFile = File.createTempFile("dummy_timing", ".json")

         val alignment = BurritoAudioAlignment.create(dummyAudioFile, dummyTimingFile)

         val jsonOutput = dummyTimingFile.readText()

         // Verify basic structure and expected values
         assertTrue(jsonOutput.contains("\"format\":\"alignment\""))
         assertTrue(jsonOutput.contains("\"version\":\"0.3\""))
         assertTrue(jsonOutput.contains("\"type\":\"audio-reference\""))
         assertTrue(jsonOutput.contains("\"groups\":["))

         val deserializedAlignment = BurritoAudioAlignment.load(dummyTimingFile)

         assertNotNull(deserializedAlignment.groups)
         assertEquals(1, deserializedAlignment.groups?.size)
         val group = deserializedAlignment.groups?.first()
         assertNotNull(group)

         assertNotNull(group?.documents)
         assertTrue(group?.documents is DocumentsList)
         val documentsList = group?.documents as DocumentsList
         assertEquals(2, documentsList.list.size)
         assertEquals("vtt-timecode", documentsList.list[0].scheme)
         assertEquals(dummyAudioFile.name, documentsList.list[0].docid)
         assertEquals("u23003", documentsList.list[1].scheme)
         assertEquals(null, documentsList.list[1].docid)

         assertNotNull(group?.records)
         assertEquals(0, group?.records?.size)

         dummyAudioFile.delete()
         dummyTimingFile.delete()
     }
}

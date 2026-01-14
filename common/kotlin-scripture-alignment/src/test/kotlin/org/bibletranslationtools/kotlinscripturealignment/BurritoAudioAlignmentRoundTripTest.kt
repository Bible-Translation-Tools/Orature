package org.bibletranslationtools.kotlinscripturealignment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment

class BurritoAudioAlignmentRoundTripTest {

    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testAudioExample1RoundTrip() {
        val resource = javaClass.classLoader.getResource("audio-example1.json")
        val originalFile = File(resource!!.file)
        val originalAlignment = BurritoAudioAlignment.load(originalFile)

        val tempOutputFile = File.createTempFile("audio_example1_output", ".json")
        originalAlignment.write(tempOutputFile)

        val deserializedAlignment = BurritoAudioAlignment.load(tempOutputFile)
        
        val originalJson = mapper.writeValueAsString(originalAlignment)
        val deserializedJson = mapper.writeValueAsString(deserializedAlignment)

        val originalJsonNode = mapper.readTree(originalJson)
        val deserializedJsonNode = mapper.readTree(deserializedJson)

        assertEquals(originalJsonNode, deserializedJsonNode)

        tempOutputFile.delete()
    }

    @Test
    fun testAudioExample2RoundTrip() {
        val resource = javaClass.classLoader.getResource("audio-example2.json")
        val originalFile = File(resource!!.file)
        val originalAlignment = BurritoAudioAlignment.load(originalFile)

        val tempOutputFile = File.createTempFile("audio_example2_output", ".json")
        originalAlignment.write(tempOutputFile)

        val deserializedAlignment = BurritoAudioAlignment.load(tempOutputFile)
        
        val originalJson = mapper.writeValueAsString(originalAlignment)
        val deserializedJson = mapper.writeValueAsString(deserializedAlignment)

        val originalJsonNode = mapper.readTree(originalJson)
        val deserializedJsonNode = mapper.readTree(deserializedJson)

        assertEquals(originalJsonNode, deserializedJsonNode)

        tempOutputFile.delete()
    }

    @Test
    fun testApmExampleRoundTrip() {
        val resource = javaClass.classLoader.getResource("apm_example.json")
        val originalFile = File(resource!!.file)
        val originalAlignment = BurritoAudioAlignment.load(originalFile)

        val tempOutputFile = File.createTempFile("apm_example_output", ".json")
        originalAlignment.write(tempOutputFile)

        val deserializedAlignment = BurritoAudioAlignment.load(tempOutputFile)
        
        val originalJson = mapper.writeValueAsString(originalAlignment)
        val deserializedJson = mapper.writeValueAsString(deserializedAlignment)

        val originalJsonNode = mapper.readTree(originalJson)
        val deserializedJsonNode = mapper.readTree(deserializedJson)

        assertEquals(originalJsonNode, deserializedJsonNode)

        tempOutputFile.delete()
    }
}

package org.bibletranslationtools.kotlinscripturealignment

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.FormatType
import org.bibletranslationtools.kotlinscripturealignment.model.Group

class BurritoAudioAlignmentSerializationTest {

    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun testSerializationOfNullableFieldsAndEmptyCollections() {
        val alignment = BurritoAudioAlignment(
            format = FormatType.ALIGNMENT,
            version = "0.3",
            type = "audio-reference",
            documents = null,
            roles = null,
            records = listOf(),
            groups = null
        )

        val tempOutputFile = File.createTempFile("nullable_fields_output", ".json")
        alignment.write(tempOutputFile)

        val jsonOutput = tempOutputFile.readText()

        // Expected JSON structure for nulls and empty lists
        val expectedJson = """{"format":"alignment","version":"0.3","type":"audio-reference","records":[]}"""
        // Jackson might reorder properties, so we check for presence/absence rather than exact string match
        assertTrue(jsonOutput.contains("\"format\":\"alignment\""))
        assertTrue(jsonOutput.contains("\"version\":\"0.3\""))
        assertTrue(jsonOutput.contains("\"type\":\"audio-reference\""))
        assertTrue(!jsonOutput.contains("\"records\""))
        assertTrue(!jsonOutput.contains("\"documents\""))
        assertTrue(!jsonOutput.contains("\"roles\""))
        assertTrue(!jsonOutput.contains("\"groups\""))

        tempOutputFile.delete()
    }

    @Test
    fun testSerializationOfGroupWithMissingDocumentsAndRecords() {
        val group = Group(documents = null, records = listOf())
        val alignment = BurritoAudioAlignment(
            type = "audio-reference",
            groups = listOf(group)
        )

        val tempOutputFile = File.createTempFile("group_nullable_fields_output", ".json")
        alignment.write(tempOutputFile)

        val jsonOutput = tempOutputFile.readText()

        // Check for expected structure within groups
        assertTrue(jsonOutput.contains("\"groups\":[{\"records\":[]}]"))
        assertTrue(!jsonOutput.contains("\"documents\" in jsonOutput.substringAfter(\"groups\":[{\").substringBefore(\"}]\")"))

        tempOutputFile.delete()
    }
}

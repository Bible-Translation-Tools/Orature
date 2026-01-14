package org.bibletranslationtools.kotlinscripturealignment

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import org.bibletranslationtools.kotlinscripturealignment.model.BurritoAudioAlignment
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsList
import org.bibletranslationtools.kotlinscripturealignment.model.DocumentsMap
import org.bibletranslationtools.kotlinscripturealignment.model.Group

class BurritoAudioAlignmentGroupsFieldTest {

    @Test
    fun testEmptyGroupsList() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "groups": []
            }
        """
        val tempFile = File.createTempFile("empty_groups_list", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertNotNull(alignment.groups)
        assertEquals(0, alignment.groups?.size)

        tempFile.delete()
    }

    @Test
    fun testGroupWithMissingDocuments() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "groups": [
                    {
                        "records": []
                    }
                ]
            }
        """
        val tempFile = File.createTempFile("group_missing_documents", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertEquals(1, alignment.groups?.size)
        val group = alignment.groups?.first()
        assertNotNull(group)
        assertNull(group?.documents)
        assertNotNull(group?.records)
        assertEquals(0, group?.records?.size)

        tempFile.delete()
    }

    @Test
    fun testGroupWithMissingRecords() {
        val json = """
            {
                "format": "alignment",
                "version": "0.3",
                "type": "audio-reference",
                "groups": [
                    {
                        "documents": []
                    }
                ]
            }
        """
        val tempFile = File.createTempFile("group_missing_records", ".json")
        tempFile.writeText(json)

        val alignment = BurritoAudioAlignment.load(tempFile)
        assertEquals(1, alignment.groups?.size)
        val group = alignment.groups?.first()
        assertNotNull(group)
        assertNotNull(group?.documents)
        assertTrue(group?.documents is DocumentsList)
        assertEquals(0, (group?.documents as DocumentsList).list.size)
        assertNotNull(group?.records)
        assertEquals(0, group?.records?.size)

        tempFile.delete()
    }
}

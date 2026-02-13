package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import org.wycliffeassociates.otter.common.domain.resourcecontainer.OtterResourceContainerConfig
import java.io.PrintWriter
import java.io.StringWriter

class BurritoWrapperTest {
    private fun assertSuccessfulConversion(
        converter: BurritoToResourceContainerConverter,
        result: Boolean
    ) {
        if (result) return
        val detail = converter.lastConversionError?.let { err ->
            val sw = StringWriter()
            err.printStackTrace(PrintWriter(sw))
            sw.toString()
        } ?: "No error captured"
        throw AssertionError("Conversion should succeed\n$detail")
    }

    @get:Rule
    val tempDir = TemporaryFolder()

    @Test
    fun `convert wrapper merges audio and text content`() {
        // Setup
        val root = tempDir.newFolder("wrapper_project")
        val audioDir = File(root, "audio").apply { mkdir() }
        val textDir = File(root, "text").apply { mkdir() }

        // Wrapper JSON
        val wrapperFile = File(root, "wrapper.json")
        wrapperFile.writeText("""
            {
              "meta": {
                "name": { "en": "Wrapper Project" },
                "version": "1.0.0",
                "generator": { "softwareName": "Test", "softwareVersion": "1.0" },
                "dateCreated": "2025-01-01",
                "description": { "en": "Test wrapper" },
                "abbreviation": { "en": "WRAP" }
              },
              "format": "scripture burrito wrapper",
              "contents": {
                "burritos": [
                  { "id": "audio", "path": "audio", "role": "source" },
                  { "id": "text", "path": "text", "role": "derived" }
                ]
              }
            }
        """.trimIndent())

        // Audio Burrito Metadata
        File(audioDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
              "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "audio", "softwareVersion": "1.0" },
                "category": "source"
              },
              "idAuthorities": {},
              "identification": {
                "name": { "en": "Audio Bible" },
                "abbreviation": { "en": "AB" },
                "primary": { "ag": { "revision": "1" } },
                "description": { "en": "Audio Desc" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" }, "scriptDirection": "ltr" }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "audioTranslation" },
                  "currentScope": { "GEN": [] }
                }
              },
              "ingredients": {
                "audio.wav": {
                  "mimeType": "audio/wav",
                  "checksum": { "md5": "abc" },
                  "size": 100,
                  "scope": { "GEN": ["1"] }
                }
              },
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())
        
        // Dummy Audio File
        File(audioDir, "audio.wav").createNewFile()

        // Text Burrito Metadata
        File(textDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
               "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "text", "softwareVersion": "1.0" },
                "category": "source"
              },
              "idAuthorities": {},
              "identification": {
                "name": { "en": "Text Bible" },
                "abbreviation": { "en": "TB" },
                "primary": { "ulb": { "revision": "1" } },
                "description": { "en": "Text Desc" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" }, "scriptDirection": "ltr" }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "textTranslation" },
                  "currentScope": { "GEN": [] }
                }
              },
              "ingredients": {
                "book.usfm": {
                  "mimeType": "text/usfm",
                  "checksum": { "md5": "xyz" },
                  "size": 200,
                  "scope": { "GEN": [] }
                }
              },
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())

        // Dummy USFM File
        File(textDir, "book.usfm").writeText("\\id GEN\n\\c 1\n\\p\n\\v 1 In the beginning...")

        // Mock DirectoryProvider
        val mockDirectoryProvider = mockk<IDirectoryProvider>(relaxed = true) {
            every { tempDirectory } returns tempDir.newFolder("temp_conversion")
        }

        val converter = BurritoToResourceContainerConverter(mockDirectoryProvider, mockk(relaxed=true))
        
        val outputZip = tempDir.newFile("output.zip")
        if (outputZip.exists()) outputZip.delete() // Ensure it's created by converter
        
        // Execute
        val result = converter.convert(wrapperFile, outputZip)

        // Assert
        assertSuccessfulConversion(converter, result)
        assertTrue("Output zip exists", outputZip.exists())
        
        ResourceContainer.load(outputZip, OtterResourceContainerConfig()).use { rc ->
             // Check for merged content
             val project = rc.manifest.projects.find { p -> p.identifier == "gen" }
             assertTrue("Should contain genesis project", project != null)
             assertEquals("Should use Text Bible title if available (or Audio if check failed)", "Text Bible", project?.title)
             
             // Check for media
             // The converter code for media manifest creation relies on specific flavors and formats. 
             // Without full library support in this test environment for parsing flavors, this might be fragile.
             // But let's check if basic manifest is written.
             assertTrue(rc.media?.projects?.isNotEmpty() == true)
        }
    }
}

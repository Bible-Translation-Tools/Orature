package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.audio.metadata.BurritoAlignmentMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.wycliffeassociates.otter.common.domain.resourcecontainer.OtterResourceContainerConfig
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Tests for burrito import issues:
 * 1. Files extracted to correct locations (media/ directory)
 * 2. No duplication of files
 * 3. Temp directory cleanup
 * 4. Cue files created for MP3 files with markers
 * 5. USFM files overwrite existing files (no duplicates)
 */
class BurritoImportIssuesTest {

    @get:Rule
    val tempDir = TemporaryFolder()

    private fun createMockDirectoryProvider(): IDirectoryProvider {
        return mockk<IDirectoryProvider>(relaxed = true) {
            every { tempDirectory } returns tempDir.newFolder("temp_conversion")
        }
    }

    private fun createConverter(): BurritoToResourceContainerConverter {
        return BurritoToResourceContainerConverter(
            createMockDirectoryProvider(),
            mockk<IVersificationRepository>(relaxed = true)
        )
    }

    @Test
    fun `test temp directory is cleaned up after conversion`() {
        val directoryProvider = createMockDirectoryProvider()
        val converter = BurritoToResourceContainerConverter(
            directoryProvider,
            mockk<IVersificationRepository>(relaxed = true)
        )
        val burritoFile = createTestBurritoZip()
        val outputFile = tempDir.newFile("output.zip")

        val tempDirBefore = File(directoryProvider.tempDirectory, burritoFile.nameWithoutExtension)
        converter.convert(burritoFile, outputFile)

        // Temp directory should be cleaned up
        assertFalse("Temp directory should be deleted", tempDirBefore.exists())
    }

    @Test
    fun `test MP3 files have cue files created`() {
        val converter = createConverter()
        val burritoFile = createTestBurritoWithMP3AndTiming()
        val outputFile = tempDir.newFile("output.zip")

        val result = converter.convert(burritoFile, outputFile)
        assertSuccessfulConversion(converter, result, "Conversion should succeed")

        ResourceContainer.load(outputFile, OtterResourceContainerConfig()).use { rc ->
            // Check that cue file exists for MP3
            val cueFiles = rc.accessor.list("media")
                .filter { it.endsWith(".cue") }
            
            assertTrue("Should have cue files for MP3", cueFiles.isNotEmpty())
            
            // Verify cue file content is valid
            cueFiles.forEach { cuePath ->
                val cueContent = rc.accessor.getReader(cuePath).readText()
                assertTrue("Cue file should contain TRACK", cueContent.contains("TRACK"))
                assertTrue("Cue file should contain FILE", cueContent.contains("FILE"))
            }
        }
    }

    @Test
    fun `test audio files are in media directory`() {
        val converter = createConverter()
        val burritoFile = createTestBurritoWithAudio()
        val outputFile = tempDir.newFile("output.zip")

        val result = converter.convert(burritoFile, outputFile)
        assertSuccessfulConversion(converter, result, "Conversion should succeed")

        ResourceContainer.load(outputFile, OtterResourceContainerConfig()).use { rc ->
            val mediaFiles = rc.accessor.list("media")
            val audioFiles = mediaFiles.filter { 
                it.endsWith(".mp3") || it.endsWith(".wav") 
            }
            
            assertTrue("Should have audio files in media/", audioFiles.isNotEmpty())
            assertTrue("All audio files should be in media/", 
                audioFiles.all { it.startsWith("media/") })
        }
    }

    @Test
    fun `test USFM files overwrite existing files without duplication`() {
        val converter = createConverter()
        val burritoFile = createTestBurritoWithUSFM()
        val outputFile = tempDir.newFile("output.zip")

        // First import
        var result = converter.convert(burritoFile, outputFile)
        assertSuccessfulConversion(converter, result, "First conversion should succeed")

        // Second import (simulating re-import of existing source)
        val burritoFile2 = createTestBurritoWithUSFM(bookName = "GEN", content = "\\id GEN\n\\c 1\n\\p\n\\v 1 Updated content")
        result = converter.convert(burritoFile2, outputFile)
        assertSuccessfulConversion(converter, result, "Second conversion should succeed")

        ResourceContainer.load(outputFile, OtterResourceContainerConfig()).use { rc ->
            val usfmFiles = rc.accessor.list(".")
                .filter { it.endsWith(".usfm") }
            
            // Should have only one USFM file per book
            val genFiles = usfmFiles.filter { it.contains("GEN") }
            assertEquals("Should have only one GEN.usfm file", 1, genFiles.size)
            
            // Verify content was updated
            val genFile = genFiles.first()
            val content = rc.accessor.getReader(genFile).readText()
            assertTrue("Content should be updated", content.contains("Updated content"))
        }
    }

    @Test
    fun `test no duplicate files after import`() {
        val converter = createConverter()
        val burritoFile = createTestBurritoWrapper()
        val outputFile = tempDir.newFile("output.zip")

        val result = converter.convert(burritoFile, outputFile)
        assertSuccessfulConversion(converter, result, "Conversion should succeed")

        ResourceContainer.load(outputFile, OtterResourceContainerConfig()).use { rc ->
            val allFiles = rc.accessor.list(".")
            
            // Check for duplicates by counting occurrences
            val fileNames = allFiles.map { File(it).name }
            val duplicates = fileNames.groupingBy { it }.eachCount()
                .filter { it.value > 1 }
            
            assertTrue("Should not have duplicate files: $duplicates", duplicates.isEmpty())
        }
    }

    @Test
    fun `test wrapper import extracts files to correct locations`() {
        val converter = createConverter()
        val wrapperFile = createTestBurritoWrapper()
        val outputFile = tempDir.newFile("output.zip")

        val result = converter.convert(wrapperFile, outputFile)
        assertSuccessfulConversion(converter, result, "Conversion should succeed")

        ResourceContainer.load(outputFile, OtterResourceContainerConfig()).use { rc ->
            // Check USFM files are at root
            val usfmFiles = rc.accessor.list(".")
                .filter { it.endsWith(".usfm") }
            assertTrue("Should have USFM files", usfmFiles.isNotEmpty())
            assertTrue("USFM files should be at root", 
                usfmFiles.all { !it.contains("/") || it.count { c -> c == '/' } == 0 })
            
            // Check audio files are in media/
            val audioFiles = rc.accessor.list("media")
                .filter { it.endsWith(".mp3") || it.endsWith(".wav") }
            assertTrue("Should have audio files in media/", audioFiles.isNotEmpty())
        }
    }

    // Helper methods to create test burritos

    private fun createTestBurritoZip(): File {
        val burritoDir = tempDir.newFolder("burrito")
        File(burritoDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
              "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "test", "softwareVersion": "1.0" },
                "category": "source"
              },
              "identification": {
                "name": { "en": "Test" },
                "abbreviation": { "en": "TEST" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" } }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "textTranslation" }
                }
              },
              "ingredients": {},
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())
        
        val zipFile = tempDir.newFile("test.burrito")
        ZipOutputStream(zipFile.outputStream()).use { zos ->
            burritoDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(burritoDir).invariantSeparatorsPath
                    val entry = ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                } else if (file.isDirectory && file != burritoDir) {
                    val relativePath = "${file.relativeTo(burritoDir).invariantSeparatorsPath}/"
                    val entry = ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    zos.closeEntry()
                }
            }
        }
        return zipFile
    }

    private fun createTestBurritoWithMP3AndTiming(): File {
        val burritoDir = tempDir.newFolder("burrito_mp3")
        val mediaDir = File(burritoDir, "media").apply { mkdirs() }
        
        // Create a minimal MP3 file (just a placeholder)
        val mp3File = File(mediaDir, "en_ulb_gen_c1.mp3")
        mp3File.writeBytes(ByteArray(1000)) // Dummy MP3 data
        
        // Create timing JSON file
        val timingFile = File(mediaDir, "en_ulb_gen_c1.json")
        timingFile.writeText("""
            {
              "format": "alignment",
              "version": "0.3",
              "type": "audio-reference",
              "groups": [{
                "documents": [{
                  "id": "gen",
                  "records": [
                    { "type": "verse", "start": 0, "end": 1000, "content": "1" }
                  ]
                }]
              }]
            }
        """.trimIndent())
        
        File(burritoDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
              "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "test", "softwareVersion": "1.0" },
                "category": "source"
              },
              "identification": {
                "name": { "en": "Test" },
                "abbreviation": { "en": "TEST" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" } }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "audioTranslation" },
                  "currentScope": { "GEN": ["1"] }
                }
              },
              "ingredients": {
                "media/en_ulb_gen_c1.mp3": {
                  "mimeType": "audio/mpeg",
                  "scope": { "GEN": ["1"] }
                },
                "media/en_ulb_gen_c1.json": {
                  "mimeType": "application/json",
                  "scope": { "GEN": ["1"] }
                }
              },
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())
        
        val zipFile = tempDir.newFile("test_mp3.burrito")
        ZipOutputStream(zipFile.outputStream()).use { zos ->
            burritoDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(burritoDir).invariantSeparatorsPath
                    val entry = ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                } else if (file.isDirectory && file != burritoDir) {
                    val relativePath = "${file.relativeTo(burritoDir).invariantSeparatorsPath}/"
                    val entry = ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    zos.closeEntry()
                }
            }
        }
        return zipFile
    }

    private fun createTestBurritoWithAudio(): File {
        val burritoDir = tempDir.newFolder("burrito_audio")
        val mediaDir = File(burritoDir, "media").apply { mkdirs() }
        
        File(mediaDir, "en_ulb_gen_c1.wav").writeBytes(ByteArray(1000))
        
        File(burritoDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
              "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "test", "softwareVersion": "1.0" },
                "category": "source"
              },
              "identification": {
                "name": { "en": "Test" },
                "abbreviation": { "en": "TEST" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" } }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "audioTranslation" },
                  "currentScope": { "GEN": ["1"] }
                }
              },
              "ingredients": {
                "media/en_ulb_gen_c1.wav": {
                  "mimeType": "audio/wav",
                  "scope": { "GEN": ["1"] }
                }
              },
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())
        
        return createZipFromDir(burritoDir, "test_audio.burrito")
    }

    private fun createTestBurritoWithUSFM(bookName: String = "GEN", content: String = "\\id GEN\n\\c 1\n\\p\n\\v 1 Test"): File {
        val burritoDir = tempDir.newFolder("burrito_usfm_${System.nanoTime()}")
        
        File(burritoDir, "01-GEN.usfm").writeText(content)
        
        File(burritoDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
              "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "test", "softwareVersion": "1.0" },
                "category": "source"
              },
              "identification": {
                "name": { "en": "Test" },
                "abbreviation": { "en": "TEST" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" } }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "textTranslation" },
                  "currentScope": { "$bookName": [] }
                }
              },
              "ingredients": {
                "01-GEN.usfm": {
                  "mimeType": "text/usfm",
                  "scope": { "$bookName": [] }
                }
              },
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())
        
        return createZipFromDir(burritoDir, "test_usfm.burrito")
    }

    private fun createTestBurritoWrapper(): File {
        val wrapperDir = tempDir.newFolder("wrapper")
        val audioDir = File(wrapperDir, "audio").apply { mkdirs() }
        val textDir = File(wrapperDir, "text").apply { mkdirs() }
        
        // Wrapper metadata
        File(wrapperDir, "metadata.json").writeText("""
            {
              "meta": {
                "name": { "en": "Wrapper" },
                "version": "1.0.0",
                "generator": { "softwareName": "test", "softwareVersion": "1.0" },
                "dateCreated": "2025-01-01"
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
        
        // Audio burrito
        val audioMediaDir = File(audioDir, "media").apply { mkdirs() }
        File(audioMediaDir, "en_ulb_gen_c1.wav").writeBytes(ByteArray(1000))
        File(audioDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
              "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "test", "softwareVersion": "1.0" },
                "category": "source"
              },
              "identification": {
                "name": { "en": "Audio" },
                "abbreviation": { "en": "AUD" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" } }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "audioTranslation" },
                  "currentScope": { "GEN": ["1"] }
                }
              },
              "ingredients": {
                "media/en_ulb_gen_c1.wav": {
                  "mimeType": "audio/wav",
                  "scope": { "GEN": ["1"] }
                }
              },
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())
        
        // Text burrito
        File(textDir, "01-GEN.usfm").writeText("\\id GEN\n\\c 1\n\\p\n\\v 1 Test")
        File(textDir, "metadata.json").writeText("""
            {
              "format": "scripture burrito",
              "meta": {
                "version": "1.0.0",
                "defaultLocale": "en",
                "dateCreated": "2025-01-01",
                "generator": { "softwareName": "test", "softwareVersion": "1.0" },
                "category": "source"
              },
              "identification": {
                "name": { "en": "Text" },
                "abbreviation": { "en": "TXT" }
              },
              "languages": [{ "tag": "en", "name": { "en": "English" } }],
              "type": {
                "flavorType": {
                  "name": "scripture",
                  "flavor": { "name": "textTranslation" },
                  "currentScope": { "GEN": [] }
                }
              },
              "ingredients": {
                "01-GEN.usfm": {
                  "mimeType": "text/usfm",
                  "scope": { "GEN": [] }
                }
              },
              "copyright": { "shortStatements": [] },
              "agencies": []
            }
        """.trimIndent())
        
        return createZipFromDir(wrapperDir, "test_wrapper.burrito")
    }

    private fun createZipFromDir(dir: File, zipName: String): File {
        val zipFile = File(tempDir.root, "${System.nanoTime()}_$zipName")
        ZipOutputStream(zipFile.outputStream()).use { zos ->
            dir.walkTopDown().forEach { file ->
                val relativePath = file.relativeTo(dir).invariantSeparatorsPath
                if (file.isFile) {
                    val entry = ZipEntry(relativePath)
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                } else if (file.isDirectory && file != dir) {
                    val entry = ZipEntry("$relativePath/")
                    zos.putNextEntry(entry)
                    zos.closeEntry()
                }
            }
        }
        return zipFile
    }
}
    private fun assertSuccessfulConversion(
        converter: BurritoToResourceContainerConverter,
        result: Boolean,
        message: String
    ) {
        if (result) return
        val detail = converter.lastConversionError?.let { err ->
            val sw = StringWriter()
            err.printStackTrace(PrintWriter(sw))
            sw.toString()
        } ?: "No error captured"
        fail("$message\n$detail")
    }

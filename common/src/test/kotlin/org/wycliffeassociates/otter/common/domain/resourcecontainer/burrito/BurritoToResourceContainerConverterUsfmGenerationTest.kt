package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bibletranslationtools.scriptureburrito.IngredientSchema
import org.bibletranslationtools.scriptureburrito.MetadataSchema
import org.bibletranslationtools.scriptureburrito.container.accessors.IContainerAccessor
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.resourcecontainer.IResourceContainerAccessor
import java.io.File

class BurritoToResourceContainerConverterUsfmGenerationTest {

    private val directoryProvider = mockk<IDirectoryProvider> {
        every { tempDirectory } returns File(System.getProperty("java.io.tmpdir"))
    }
    private val versificationRepository = mockk<IVersificationRepository>()
    private val converter = BurritoToResourceContainerConverter(directoryProvider, versificationRepository)

    @Test
    fun `generateUsfmContent generates correct content`() {
        val bookSlug = "gen"
        val versification = mockk<Versification> {
            every { getChaptersInBook(bookSlug) } returns 2
            every { getVersesInChapter(bookSlug, 1) } returns 3
            every { getVersesInChapter(bookSlug, 2) } returns 2
        }

        val expectedContent = """
            \id GEN
            \c 1
            \p
            \v 1 
            \v 2 
            \v 3 
            \c 2
            \p
            \v 1 
            \v 2 
            
        """.trimIndent()

        val content = converter.generateUsfmContent(bookSlug, versification)
        Assert.assertEquals(expectedContent, content)
    }

    @Test
    fun `generateMissingUsfmFiles generates files for missing books`() {
        val bookSlug = "gen"
        val booksInScope = setOf(bookSlug)
        val usfmFilesByBook = emptyMap<String, List<Pair<String, IngredientSchema>>>()
        val versification = mockk<Versification> {
            every { getChaptersInBook(bookSlug) } returns 1
            every { getVersesInChapter(bookSlug, 1) } returns 1
        }

        val result = converter.generateMissingUsfmFiles(usfmFilesByBook, booksInScope, versification)

        Assert.assertTrue(result.containsKey(bookSlug))
        Assert.assertEquals(1, result[bookSlug]?.size)
        val generatedFile = File(result[bookSlug]!!.first().first)
        Assert.assertTrue(generatedFile.exists())
        Assert.assertTrue(generatedFile.readText().contains("\\id GEN"))
    }
    
    @Test
    fun `generateMissingUsfmFiles does not overwrite existing files`() {
        val bookSlug = "gen"
        val booksInScope = setOf(bookSlug)
        val existingFile = File.createTempFile("existing", ".usfm")
        val usfmFilesByBook = mapOf(bookSlug to listOf(Pair(existingFile.absolutePath, IngredientSchema())))
        val versification = mockk<Versification>()

        val result = converter.generateMissingUsfmFiles(usfmFilesByBook, booksInScope, versification)

        Assert.assertEquals(existingFile.absolutePath, result[bookSlug]!!.first().first)
        verify(exactly = 0) { versification.getChaptersInBook(any<String>()) }
    }
    @Test
    fun testMoveUSFMFilesWithGeneratedFile() {
        val bookSlug = "gen"
        val tempFile = File.createTempFile("gen", ".usfm")
        tempFile.writeText("USFM Content")
        val usfmFilesByBook = mapOf(bookSlug to listOf(Pair(tempFile.absolutePath, IngredientSchema())))
        
        val inputAccessor = mockk<IContainerAccessor>()
        val outputAccessor = mockk<IResourceContainerAccessor>(relaxed = true)
        
        every { inputAccessor.fileExists(any()) } returns false

        val result = converter.moveUSFMFiles(usfmFilesByBook, inputAccessor, outputAccessor)
        
        Assert.assertTrue("Should contain book if file was moved", result.containsKey(bookSlug))
        verify { outputAccessor.write(any(), any()) }
    }
}

package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.createTempDirectory
import kotlin.jvm.Throws

class TestBibleImagesDataSource {

    private val testRCName = """bible_images_rc"""
    private lateinit var tempDir: File
    private val directoryProviderMock = mock(IDirectoryProvider::class.java)
    private val languageMock = mock(Language::class.java)
    private val metadataMock = mock(ResourceMetadata::class.java)

    // this name must be valid according to BibleImagesDataSource container name
    private val imagesContainerName = "bible_artwork"
    private val language = "en"
    private val resourceId = "ulb"
    private val project = "jas"

    @Before
    fun setUp() {
        val sourceRC = getResource(testRCName)
        tempDir = createTempDirectory().toFile()
        val tempContainer = tempDir.resolve(imagesContainerName)
        sourceRC.copyRecursively(tempContainer)


        `when`(directoryProviderMock.resourceContainerDirectory)
            .thenReturn(tempDir)
        `when`(directoryProviderMock.cacheDirectory)
            .thenReturn(
                tempDir.resolve("cache").apply { mkdirs() }
            )
        `when`(languageMock.slug).thenReturn(language)
    }

    @Test
    fun getBibleImage() {

        val dataSource = BibleImagesDataSource(directoryProviderMock)
        val image = dataSource.getImage(metadataMock, project)

        assertNotNull(
            "Could not get image for [$language-$resourceId-$project]",
            image
        )

        tempDir.deleteRecursively()
    }

    @Test
    fun testNotFoundImage() {
        val dataSource = BibleImagesDataSource(directoryProviderMock)
        val notFoundImage = dataSource.getImage(metadataMock, "gen")
        val nonBibleNotFoundImage =  dataSource.getImage(metadataMock, "unknown")

        assertNull(notFoundImage)
        assertNull(nonBibleNotFoundImage)
    }

    @Throws(FileNotFoundException::class)
    private fun getResource(name: String): File {
        val path = javaClass.classLoader.getResource(testRCName)?.file
        if (path == null) {
            throw FileNotFoundException("Could not find resource: $name")
        }
        return File(path)
    }
}
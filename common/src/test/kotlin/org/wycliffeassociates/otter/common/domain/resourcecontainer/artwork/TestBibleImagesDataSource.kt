package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate
import kotlin.io.path.createTempDirectory
import kotlin.jvm.Throws

class TestBibleImagesDataSource {

    private val rcName = """bible_images_rc"""

    // this name must be valid according to BibleImagesDataSource container name
    private val imagesContainerName = "en_ulb_bible_artwork"
    private val language = "en"
    private val resourceId = "ulb"
    private val project = "jas"

    @Test
    fun getBibleImage() {
        val sourceRC = getResource(rcName)
        val tempDir = createTempDirectory().toFile()
        val tempContainer = tempDir.resolve(imagesContainerName)

        sourceRC.copyRecursively(tempContainer)

        val directoryProviderMock = mock(IDirectoryProvider::class.java)
        val languageMock = mock(Language::class.java)

        `when`(directoryProviderMock.resourceContainerDirectory)
            .thenReturn(tempDir)
        `when`(directoryProviderMock.cacheDirectory)
            .thenReturn(
                tempDir.resolve("cache").apply { mkdirs() }
            )
        `when`(languageMock.slug).thenReturn(language)

        val metadata = ResourceMetadata(
            conformsTo = "unused",
            creator = "unused",
            description = "unused",
            format = "unused",
            identifier = resourceId,
            issued = mock(LocalDate::class.java),
            language = languageMock,
            modified = mock(LocalDate::class.java),
            publisher = "unused",
            subject = "unused",
            type = mock(ContainerType::class.java),
            title = "unused",
            version = "unused",
            path = mock(File::class.java)
        )

        val dataSource = BibleImagesDataSource(directoryProviderMock)
        val image = dataSource.getImage(metadata, project)
        val notFoundImage = dataSource.getImage(metadata, "gen")

        assertNotNull(
            "Could not get image for [$language-$resourceId-$project]",
            image
        )
        assertNull(notFoundImage)

        tempDir.deleteRecursively()
    }

    @Throws(FileNotFoundException::class)
    private fun getResource(name: String): File {
        val path = javaClass.classLoader.getResource(rcName)?.file
        if (path == null) {
            throw FileNotFoundException("Could not find resource: $name")
        }
        return File(path)
    }
}
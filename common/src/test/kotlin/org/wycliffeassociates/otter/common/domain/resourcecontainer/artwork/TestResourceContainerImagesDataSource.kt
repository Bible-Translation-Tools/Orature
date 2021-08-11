package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate
import kotlin.io.path.createTempDirectory
import kotlin.jvm.Throws

class TestResourceContainerImagesDataSource {
    private val rcName = """images_rc"""
    private val language = "en"
    private val resourceId = "ulb"
    private val project = "jas"

    @Test
    fun getImage() {
        val rcFile = getResource(rcName)
        val tempDir = createTempDirectory().toFile()

        val directoryProviderMock = Mockito.mock(IDirectoryProvider::class.java)
        val languageMock = Mockito.mock(Language::class.java)

        Mockito.`when`(directoryProviderMock.cacheDirectory)
            .thenReturn(
                tempDir.resolve("cache").apply { mkdirs() }
            )
        Mockito.`when`(languageMock.slug).thenReturn(language)

        val metadata = ResourceMetadata(
            conformsTo = "unused",
            creator = "unused",
            description = "unused",
            format = "unused",
            identifier = resourceId,
            issued = Mockito.mock(LocalDate::class.java),
            language = languageMock,
            modified = Mockito.mock(LocalDate::class.java),
            publisher = "unused",
            subject = "unused",
            type = Mockito.mock(ContainerType::class.java),
            title = "unused",
            version = "unused",
            path = rcFile
        )

        val dataSource = ResourceContainerImagesDataSource(directoryProviderMock)
        val image = dataSource.getImage(metadata, project)
        val notFoundImage = dataSource.getImage(metadata, "gen")

        Assert.assertNotNull(
            "Could not get image for [$language-$resourceId-$project]",
            image
        )
        Assert.assertNull(notFoundImage)

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
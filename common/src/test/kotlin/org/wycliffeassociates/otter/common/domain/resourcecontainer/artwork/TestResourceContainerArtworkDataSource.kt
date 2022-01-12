/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.createTempDirectory
import kotlin.jvm.Throws

class TestResourceContainerArtworkDataSource {
    private val rcName = """images_rc"""
    private val language = "en"
    private val resourceId = "ulb"
    private val project = "jas"
    private lateinit var rcFile: File
    private lateinit var tempDir: File
    private val directoryProviderMock = mock(IDirectoryProvider::class.java)
    private val metadataMock = mock(ResourceMetadata::class.java)

    @Before
    fun setUp() {
        rcFile = getResource(rcName)
        tempDir = createTempDirectory().toFile()
        `when`(directoryProviderMock.cacheDirectory)
            .thenReturn(
                tempDir.resolve("cache").apply { mkdirs() }
            )

        val languageMock = mock(Language::class.java)
        `when`(languageMock.slug).thenReturn(language)
        `when`(metadataMock.identifier).thenReturn(resourceId)
        `when`(metadataMock.language).thenReturn(languageMock)
        `when`(metadataMock.path).thenReturn(rcFile)
    }

    @After
    fun cleanUp() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testGetImage() {
        val dataSource = ResourceContainerArtworkDataSource(directoryProviderMock)
        val image = dataSource.getArtwork(metadataMock, project)

        assertNotNull(
            "Could not get image for $project",
            image
        )
    }

    @Test
    fun testGetImageWithRatio() {
        val ratio4x3 = ImageRatio.FOUR_BY_THREE
        val ratioString = ratio4x3.toString()
        val dataSource = ResourceContainerArtworkDataSource(directoryProviderMock)
        val image = dataSource.getArtwork(metadataMock, project, ratio4x3)

        assertNotNull(
            "Could not get image for $project",
            image
        )
        assertTrue(
            "Could not get image with ratio $ratioString for $project",
            image!!.file.nameWithoutExtension.endsWith(ratioString)
        )
    }

    @Test
    fun testNotFoundImage() {
        val genSlug = "gen"
        val nonBibleProject = "unknown"
        val remoteUrlProject = "tit"

        val dataSource = ResourceContainerArtworkDataSource(directoryProviderMock)
        val notFoundImage = dataSource.getArtwork(metadataMock, genSlug)
        val nonBibleNotFoundImage = dataSource.getArtwork(metadataMock, nonBibleProject)
        val remoteImageNotFound = dataSource.getArtwork(metadataMock, remoteUrlProject)

        assertNull(
            "Project $genSlug should not have image in data source",
            notFoundImage
        )
        assertNull(
            "Project $nonBibleProject should not have image in data source",
            nonBibleNotFoundImage
        )
        assertNull(
            "Project $remoteUrlProject should not have image in data source",
            remoteImageNotFound
        )
    }

    @Test
    fun `test fallback to default when aspect ratio not found`() {
        val ratio16x9 = ImageRatio.SIXTEEN_BY_NINE
        val ratioString = ratio16x9.toString()
        val dataSource = ResourceContainerArtworkDataSource(directoryProviderMock)
        val image = dataSource.getArtwork(metadataMock, project, ratio16x9)

        assertNotNull(
            "Could not get default image ($ratioString) for $project",
            image
        )
        assertFalse(
            "Project $project should not have image with ratio $ratioString in data source",
            image!!.file.nameWithoutExtension.endsWith(ratioString)
        )
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

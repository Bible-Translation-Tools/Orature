/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
    private val metadataMock = mock(ResourceMetadata::class.java)

    // this name must be valid according to BibleImagesDataSource container name
    private val imagesContainerName = "bible_artwork"
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
    }

    @After
    fun cleanUp() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testGetBibleImage() {
        val dataSource = BibleImagesDataSource(directoryProviderMock)
        val image = dataSource.getImage(metadataMock, project)

        assertNotNull(
            "Could not get artwork image for $project",
            image
        )
    }

    @Test
    fun testNotFoundImage() {
        val genSlug = "gen"
        val nonBibleProject = "unknown"
        val remoteContentProject = "tit"

        val dataSource = BibleImagesDataSource(directoryProviderMock)
        val notFoundImage = dataSource.getImage(metadataMock, genSlug)
        val nonBibleNotFoundImage =  dataSource.getImage(metadataMock, nonBibleProject)
        val remoteImageNotFound =  dataSource.getImage(metadataMock, remoteContentProject)

        assertNull(
            "Project '$genSlug' should not have image in data source",
            notFoundImage
        )
        assertNull(
            "Project '$nonBibleProject' should not have image in data source",
            nonBibleNotFoundImage
        )
        assertNull(
            "Project '$remoteContentProject' should not have image in data source",
            remoteImageNotFound
        )
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
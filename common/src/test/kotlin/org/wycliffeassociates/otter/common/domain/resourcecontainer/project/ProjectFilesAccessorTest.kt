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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import com.nhaarman.mockitokotlin2.any
import net.lingala.zip4j.ZipFile
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.RcConstants
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.io.FileNotFoundException
import java.time.LocalDate
import kotlin.io.path.createTempDirectory
import kotlin.jvm.Throws

class ProjectFilesAccessorTest {
    private lateinit var projectSourceDir: File
    private lateinit var sourceRC: File
    private val directoryProviderMock = mock(IDirectoryProvider::class.java)
    private val mediaProjectSize = 2

    private fun setUp_copySourceFiles(
        project: Collection,
        tempDir: File
    ): ProjectFilesAccessor {
        sourceRC = getResource("source_with_imported_media_jas_jud.zip")
        projectSourceDir = tempDir.resolve("source").apply { mkdir() }

        val sourceMetadata = ResourceMetadata(
            "_",
            "_",
            "_",
            "_",
            "ulb",
            LocalDate.now(),
            mock(Language::class.java),
            LocalDate.now(),
            "_",
            "_",
            mock(ContainerType::class.java),
            "_",
            "_",
            "_",
            sourceRC
        )
        val targetMetadata = ResourceMetadata(
            "_",
            "_",
            "_",
            "_",
            "ulb",
            LocalDate.now(),
            mock(Language::class.java),
            LocalDate.now(),
            "_",
            "_",
            mock(ContainerType::class.java),
            "_",
            "_",
            "_",
            File("")
        )

        `when`(
            directoryProviderMock.getProjectDirectory(
                sourceMetadata, targetMetadata, project
            )
        ).thenReturn(mock(File::class.java))

        `when`(
            directoryProviderMock.getProjectSourceDirectory(
                sourceMetadata, targetMetadata, project
            )
        ).thenReturn(projectSourceDir)

        `when`(
            directoryProviderMock.getProjectAudioDirectory(
                sourceMetadata, targetMetadata, project
            )
        ).thenReturn(mock(File::class.java))

        return ProjectFilesAccessor(
            directoryProviderMock, sourceMetadata, targetMetadata, project
        )
    }

    @Test
    fun copySourceFiles_excludeMedia() {
        val tempDir = createTempDirectory("otter-test").toFile()
        val project = mock(Collection::class.java)
        `when`(project.slug).thenReturn("jas")
        val projectFilesAccessor = setUp_copySourceFiles(project, tempDir)

        projectFilesAccessor.copySourceFiles(excludeMedia = true)

        assertEquals(
            "Target location must have exactly ONE item",
            1, projectSourceDir.listFiles().size
        )
        val target = projectSourceDir.listFiles().first()
        ResourceContainer.load(target).use {
            assertEquals(
                "Target media manifest should not have resources for other project's source.",
                1,
                it.media!!.projects.size
            )
        }
        ZipFile(target).extractAll(projectSourceDir.path)

        val mediaProjectDirCount = projectSourceDir.walk().firstOrNull {
            it.isDirectory && it.name == RcConstants.SOURCE_MEDIA_DIR
        }?.let {
            it.list().size
        } ?: 0

        assertEquals(
            "Copied files from source should not have media.",
            1,
            mediaProjectDirCount
        )
        verify(directoryProviderMock).getProjectSourceDirectory(
            any(), any(), any<Collection>()
        )

        tempDir.deleteRecursively()
    }

    @Test
    fun copySourceFiles_includeMedia() {
        val tempDir = createTempDirectory("otter-test").toFile()
        val project = mock(Collection::class.java)
        `when`(project.slug).thenReturn("jas")
        val projectFilesAccessor = setUp_copySourceFiles(project, tempDir)

        projectFilesAccessor.copySourceFiles(excludeMedia = false)

        assertEquals(
            "Target location must have exactly ONE item",
            1, projectSourceDir.listFiles().size
        )

        val target = projectSourceDir.listFiles().first()
        ResourceContainer.load(target).use {
            assertNotNull(
                it.media != null
            )
            assertEquals(
                mediaProjectSize,
                it.media!!.projects.size
            )
        }
        ZipFile(target).extractAll(projectSourceDir.path)

        val mediaProjectDirCount = projectSourceDir.walk().firstOrNull {
            it.isDirectory && it.name == RcConstants.SOURCE_MEDIA_DIR
        }?.let {
            it.list().size
        } ?: 0

        assertEquals(
            "Copied files from source should have media.",
            mediaProjectSize,
            mediaProjectDirCount
        )
        verify(directoryProviderMock).getProjectSourceDirectory(
            any(), any(), any<Collection>()
        )

        tempDir.deleteRecursively()
    }

    @Throws(FileNotFoundException::class)
    private fun getResource(name: String): File {
        val path = javaClass.classLoader.getResource(name)?.file
        if (path == null) {
            throw FileNotFoundException("Could not find resource: $name")
        }
        return File(path)
    }
}
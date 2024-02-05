/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package integrationtest.projects.importer

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import io.reactivex.Single
import junit.framework.TestCase.*
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.importer.ExistingSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.NewSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.project.ZipEntryTreeBuilder
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Project
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject
import javax.inject.Provider

class TestExistingSourceImporter {
    @Inject
    lateinit var newSourceImporterProvider: Provider<NewSourceImporter>

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var resourceMetadataRepository: IResourceMetadataRepository

    @Inject
    lateinit var resourceContainerRepository: IResourceContainerRepository

    @Inject
    lateinit var zipEntryTreeBuilder: IZipEntryTreeBuilder

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    @Inject
    lateinit var importUseCaseProvider: Provider<ImportProjectUseCase>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get()

    private val spyImportUseCase = spy(importUseCaseProvider.get())
    private val spyDeleteUseCase = DeleteResourceContainer(
        directoryProvider,
        resourceContainerRepository
    ).let { spy(it) }

    private val callbackMock = mock<ProjectImporterCallback> {
        on { onRequestUserInput() } doReturn (Single.just(ImportOptions(confirmed = true)))
    }

    private val importer: ExistingSourceImporter by lazy {
        val imp = ExistingSourceImporter(
            directoryProvider,
            resourceMetadataRepository,
            resourceContainerRepository,
            zipEntryTreeBuilder,
            spyDeleteUseCase,
            spyImportUseCase
        )
        // there will be a source file in the project file and we need to import it
        imp.setNext(newSourceImporterProvider.get())
        imp
    }

    @Test
    fun mergeExistingSourceWhenVersionMatching() {
        val spyImporter = spy(importer)
        spyImporter.import(getSourceFile("resource-containers/en_ulb.zip"))
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        verify(spyImporter, never()).mergeMedia(any(), any())

        spyImporter.import(getSourceFile("resource-containers/en_ulb_media_merge_test.zip"))
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        verify(spyImporter).mergeMedia(any(), any())
        verify(spyDeleteUseCase, never()).deleteSync(any())
    }

    @Test
    fun updateExistingSourceWhenVersionDifferent() {
        importer.import(getSourceFile("resource-containers/en_ulb.zip"))
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        db.assertRowCounts(
            RowCount(
                collections = 1256,
                contents = mapOf(
                    ContentType.META to 1189,
                    ContentType.TEXT to 31104,
                    ContentType.TITLE to 1255
                )
            )
        )

        val existingSource = resourceMetadataRepository.getAllSources().blockingGet().single()

        Assert.assertEquals("12", existingSource.version)
        Assert.assertTrue(existingSource.path.exists())
        verify(spyDeleteUseCase, never()).deleteSync(any())
        verify(spyImportUseCase, never()).import(any())

        // Import new source with a different version
        val file = getSourceFile("resource-containers/en_ulb_newer_ver.zip")
        importer
            .import(
                file,
                callback = callbackMock
            )
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        db.assertRowCounts(
            RowCount(collections = 1, contents = mapOf(), links = 0)
        )

        val newSource = resourceMetadataRepository.getAllSources().blockingGet().single()

        Assert.assertEquals("999", newSource.version)
        Assert.assertTrue(newSource.path.exists())
        Assert.assertFalse(
            "Old source file should be deleted after replacing with different source version.",
            existingSource.path.exists()
        )
        verify(callbackMock).onRequestUserInput()
        verify(spyDeleteUseCase).deleteSync(any())
        verify(spyImportUseCase).import(file) // re-import after deleting source
    }

    private fun getSourceFile(name: String): File {
        val path = javaClass.classLoader.getResource(name)
        if (path == null) {
            throw FileNotFoundException("Test resource not found")
        }
        return File(path.file)
    }

    @Test
    fun `test text is updated in database when different version or versification`() {
        val base = ResourceContainerBuilder()
            .setVersion(1)
            .setTargetLanguage(Language("aa", "Afar", "aa", "ltr", false, "Africa"))
            .setProjectManifest(
                listOf(
                    Project("Genesis", "ulb", "gen", 1, "./gen.usfm")
                )
            )

        val start = base.buildFile()
            .apply { deleteOnExit() }
        val startRc = ResourceContainer.load(start)
        startRc.accessor.write("gen.usfm") {
            it.write(
                """
                \c 1
                \p
                \v 1 In the beginning, God created the heavens and the earth.
            """.trimIndent().toByteArray()
            )
        }

        importer.import(start)
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        val collectionDao = db.getCollectionDao()
        val contentDao = db.getContentDao()

        var startingBook = collectionDao
            .fetchAll()
            .firstOrNull { it.slug == "gen_1" }
        var startingContent = contentDao
            .fetchAll()
            .firstOrNull { it.collectionFk == startingBook?.id && it.type_fk == 1 }
        var secondContent = contentDao
            .fetchAll()
            .firstOrNull { it.collectionFk == startingBook?.id && it.type_fk == 1 && it.sort == 2 }

        // First verse should have text, second verse should be allocated from versification but should not have text
        assertEquals("In the beginning, God created the heavens and the earth.", startingContent?.text)
        assertNotNull(secondContent)
        assertNull(secondContent?.text)

        val end = ResourceContainerBuilder()
            .setVersion(2)
            .setTargetLanguage(Language("aa", "Afar", "aa", "ltr", false, "Africa"))
            .setProjectManifest(
                listOf(
                    Project("Genesis", "ulb", "gen", 1, "./gen.usfm")
                )
            )
            .buildFile()
            .apply { deleteOnExit() }
        val endRc = ResourceContainer.load(end)
        endRc.accessor.write("gen.usfm") {
            it.write(
                """
                \c 1
                \p
                \v 1 Overwritten.
                \v 2 Text added.
            """.trimIndent().toByteArray()
            )
        }

        importer.import(end)
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        startingContent = contentDao
            .fetchAll()
            .firstOrNull { it.collectionFk == startingBook?.id && it.type_fk == 1 }
        secondContent = contentDao
            .fetchAll()
            .firstOrNull { it.collectionFk == startingBook?.id && it.type_fk == 1 && it.sort == 2 }

        // Both verses should be updated now
        assertEquals("Overwritten.", startingContent?.text)
        assertNotNull(secondContent)
        assertEquals("Text added.", secondContent?.text)


        // Verify that the RC version was updated
        val startingRcMetadata = resourceMetadataRepository
            .getAllSources()
            .blockingGet()
            .firstOrNull { it.language.slug == "aa" }
        assertEquals("2", startingRcMetadata!!.version)

        val differentVersification = ResourceContainerBuilder()
            .setVersion(2)
            .setTargetLanguage(Language("aa", "Afar", "aa", "ltr", false, "Africa"))
            .setProjectManifest(
                listOf(
                    Project("Genesis", "rsc", "gen", 1, "./gen.usfm")
                )
            )
            .buildFile()
            .apply { deleteOnExit() }
        val differentVersificationRc = ResourceContainer.load(differentVersification)
        differentVersificationRc.accessor.write("gen.usfm") {
            it.write(
                """
                \c 1
                \p
                \v 1 Different Versification
                \v 2 Different Versification
            """.trimIndent().toByteArray()
            )
        }

        importer.import(differentVersification)
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        startingContent = contentDao
            .fetchAll()
            .firstOrNull { it.collectionFk == startingBook?.id && it.type_fk == 1 }
        secondContent = contentDao
            .fetchAll()
            .firstOrNull { it.collectionFk == startingBook?.id && it.type_fk == 1 && it.sort == 2 }

        assertEquals("Different Versification", startingContent?.text)
        assertNotNull(secondContent)
        assertEquals("Different Versification", secondContent?.text)
    }
}
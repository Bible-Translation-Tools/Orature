/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.NewSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.OngoingProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class TestOngoingProjectImporter {
    @Inject
    lateinit var importerProvider: Provider<OngoingProjectImporter>

    @Inject
    lateinit var sourceImporterProvider: Provider<NewSourceImporter>

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    @Inject
    lateinit var workbookRepository: IWorkbookRepository

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get()
    private val importer: RCImporter by lazy {
        val imp = importerProvider.get()
        imp.setNext(sourceImporterProvider.get())
        imp
    }
    private val projectFile: File by lazy { setupRC() }
    private val narrationBackupFile: File by lazy { setupNarrationBackup() }
    private val takesInProject = 6
    private val takesPerChapter = 2
    private val chaptersSelected = listOf(1, 2)
    private val takesAdded = takesPerChapter * chaptersSelected.size

    private val callback = mock<ProjectImporterCallback> {
        on { onRequestUserInput(any()) } doReturn (Single.just(ImportOptions(chaptersSelected)))
    }

    @Test
    fun testImportDuplicatedProject() {
        importOngoingProject(callback = null)
        val originalTakes = db.db.takeDao.fetchAll()

        Assert.assertEquals(
            "There should be $takesInProject takes after the initial import.",
            takesInProject,
            originalTakes.size
        )

        /* Import the same project with chapter selection provided from callback */
        importOngoingProject(callback = this.callback)

        val currentTakes = db.db.takeDao.fetchAll()
        val addedTakes = currentTakes.filter { it !in originalTakes }
        val contents = addedTakes.map { db.db.contentDao.fetchById(it.contentFk) }
        val chapterCollections = contents.map { db.db.collectionDao.fetchById(it.collectionFk) }
        val chaptersImported = chapterCollections
            .map { it.sort }
            .sorted()
            .distinct()

        Assert.assertEquals(
            "There should be $takesInProject + $takesAdded takes " +
                    "after importing chapter: $chaptersSelected",
            takesInProject + takesAdded,
            currentTakes.size
        )
        Assert.assertEquals(takesAdded, addedTakes.size)
        Assert.assertEquals(chaptersSelected, chaptersImported)
    }

    @Test
    fun testPopulateProjectsWhenImport() {
        val bookCountBefore = db.db.workbookDescriptorDao.fetchAll().size
        Assert.assertEquals(0, bookCountBefore)
        Assert.assertEquals(0, workbookRepository.getProjects().blockingGet().size)

        importOngoingProject(callback = null)

        val bookCountAfter = db.db.workbookDescriptorDao.fetchAll().size
        Assert.assertEquals(66, bookCountAfter)
        Assert.assertEquals(66, workbookRepository.getProjects().blockingGet().size)
    }

    @Test
    fun testImportNarrationBackupPopulatesProjects() {
        val bookCountBefore = db.db.workbookDescriptorDao.fetchAll().size
        Assert.assertEquals(0, bookCountBefore)
        Assert.assertEquals(0, workbookRepository.getProjects().blockingGet().size)

        importOngoingProject(narrationBackupFile, null)

        val bookCountAfter = db.db.workbookDescriptorDao.fetchAll().size
        Assert.assertEquals(66, bookCountAfter)
        Assert.assertEquals(66, workbookRepository.getProjects().blockingGet().size)
    }

    @Test
    fun testImportNarrationBackupNoCreatedTakes() {
        val beforeTakes = db.db.takeDao.fetchAll()

        importOngoingProject(narrationBackupFile, null)

        val afterTakes = db.db.takeDao.fetchAll()

        Assert.assertEquals(beforeTakes.size, afterTakes.size)
    }

    private fun importOngoingProject(file: File = projectFile, callback: ProjectImporterCallback?) {
        importer
            .import(file, callback)
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }
    }

    private fun setupRC(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .setOngoingProject(true)
            .addTake(1, ContentType.META, 1, true)
            .addTake(2, ContentType.META, 1, true)
            .addTake(3, ContentType.META, 1, true)
            .addTake(1, ContentType.TEXT, 1, true, chapter = 1, start = 1, end = 1)
            .addTake(2, ContentType.TEXT, 1, true, chapter = 2, start = 1, end = 1)
            .addTake(3, ContentType.TEXT, 1, true, chapter = 3, start = 1, end = 1)
            .buildFile()
    }

    private fun setupNarrationBackup(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .setOngoingProject(true)
            .addInProgressChapter(1)
            .buildFile()
    }
}
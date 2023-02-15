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
package integrationtest.initialization

import com.nhaarman.mockitokotlin2.any
import integrationtest.di.DaggerTestPersistenceComponent
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.wycliffeassociates.otter.assets.initialization.InitializeUlb
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.project.importer.ExistingSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.IProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporter
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import javax.inject.Inject
import javax.inject.Provider

class TestInitializeUlb {

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var initUlbProvider: Provider<InitializeUlb>

    @Inject
    lateinit var importLanguages: Provider<ImportLanguages>

    @Inject
    lateinit var installedEntityRepo: IInstalledEntityRepository

    @Inject
    lateinit var rcImporterFactory: RCImporterFactory

    private val importer: IProjectImporter
        get() = rcImporterFactory.makeImporter()

    @Before
    fun setup() {
        DaggerTestPersistenceComponent.create().inject(this)
        val langNames = ClassLoader.getSystemResourceAsStream("content/langnames.json")!!
        importLanguages.get().import(langNames).blockingGet()
    }

    @Test
    fun testImportEnUlb() {
        val testSub = TestObserver<Completable>()

        val init = initUlbProvider.get()
        init.exec()
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        Assert.assertEquals(init.version, database.installedEntityDao.fetchVersion(init))
    }

    @Test
    fun `test en_ulb import skipped when already imported`() {
        val importer = Mockito.mock(ExistingSourceImporter::class.java)
        val importerSpy = Mockito.spy(importer)
        val factory = Mockito.mock(RCImporterFactory::class.java)

        doReturn(true).`when`(importerSpy).isRCAlreadyImported(any())
        doReturn(importerSpy).`when`(factory).makeImporter()

        val init = InitializeUlb(
            directoryProvider,
            installedEntityRepo,
            factory,
            importerSpy
        )
        val testSub = TestObserver<Completable>()

        init.exec()
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        verify(importerSpy).isRCAlreadyImported(any())
        verify(importerSpy, never()).import(any())
    }
}

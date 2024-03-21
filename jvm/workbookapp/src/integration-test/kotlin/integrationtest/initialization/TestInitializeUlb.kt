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
package integrationtest.initialization

import integrationtest.di.DaggerTestPersistenceComponent
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.ObservableEmitter
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeUlb
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.data.ProgressStatus
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

    @Before
    fun setup() {
        DaggerTestPersistenceComponent.create().inject(this)
        val langNames = ClassLoader.getSystemResourceAsStream("content/langnames.json")!!
        importLanguages.get().import(langNames).blockingGet()
    }

    @Test
    fun testImportEnUlb() {
        val testSub = TestObserver<Completable>()
        val mockProgressEmitter = mockk<ObservableEmitter<ProgressStatus>>{
            every { onNext(any()) } answers { }
        }

        val init = spyk(initUlbProvider.get())
        every { init.installGLSources(any()) } returns Unit // skip the GLs installations

        init.exec(mockProgressEmitter)
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        Assert.assertEquals(init.version, database.installedEntityDao.fetchVersion(init))
    }

    @Test
    fun `test en_ulb import skipped when already imported`() {
        val testSub = TestObserver<Completable>()
        val mockProgressEmitter = mockk<ObservableEmitter<ProgressStatus>>{
            every { onNext(any()) } answers { }
        }
        val importer = mockk<ImportProjectUseCase> {
            every { isAlreadyImported(any()) } returns true
        }

        val init = spyk(
            InitializeUlb(
                directoryProvider,
                installedEntityRepo,
                importer
            )
        )
        every { init.installGLSources(any()) } returns Unit // skip the GLs installations

        init.exec(mockProgressEmitter)
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        verify { importer.isAlreadyImported(any()) }
        verify(exactly = 0) { importer.import(any(), any(), any()) }
        verify(exactly = 0) { importer.import(any()) }
    }
}

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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import integrationtest.di.DaggerTestPersistenceComponent
import io.reactivex.Completable
import io.reactivex.ObservableEmitter
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeSources
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class TestInitializeSources {
    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var initSourcesProvider: Provider<InitializeSources>

    @Inject
    lateinit var importLanguages: Provider<ImportLanguages>

    @Inject
    lateinit var resourceMetadataRepository: IResourceMetadataRepository

    init {
        DaggerTestPersistenceComponent.create().inject(this)
        val langNames = ClassLoader.getSystemResourceAsStream("content/langnames.json")!!
        importLanguages.get().import(langNames).blockingGet()
    }

    @Test
    fun testInitializeSources() {
        prepareSource()

        Assert.assertEquals(
            0, resourceMetadataRepository.getAllSources().blockingGet().size
        )

        val testSub = TestObserver<Completable>()
        val init = initSourcesProvider.get()
        val mockProgressEmitter = mock<ObservableEmitter<ProgressStatus>>{
            on { onNext(any()) } doAnswer { }
        }

        init
            .exec(mockProgressEmitter)
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        Assert.assertEquals(init.version, database.installedEntityDao.fetchVersion(init))
        Assert.assertEquals(
            1, resourceMetadataRepository.getAllSources().blockingGet().size
        )
    }

    private fun prepareSource() {
        val sourceToCopy = File(javaClass.classLoader.getResource("resource-containers/hi_ulb.zip").file)
        val targetSource = directoryProvider.internalSourceRCDirectory.resolve(sourceToCopy.name)

        sourceToCopy.copyTo(targetSource, overwrite = true)
    }
}
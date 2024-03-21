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

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import integrationtest.di.DaggerTestPersistenceComponent
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeUlb
import org.wycliffeassociates.otter.assets.initialization.ResourceInfoSerializable
import org.wycliffeassociates.otter.assets.initialization.SOURCES_JSON_FILE
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.net.HttpURLConnection
import java.net.URL
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
    fun testInitializeGLSources() {
        val testSub = TestObserver<Completable>()
        val mockProgressEmitter = mockk<ObservableEmitter<ProgressStatus>>{
            every { onNext(any()) } answers { }
        }
        val sourceCount = countAvailableSources()

        val importSpy = mockk<ImportProjectUseCase> {
            every { isAlreadyImported(any()) } returns false
            every { import(any(), any(), any()) } returns Single.just(ImportResult.SUCCESS)
        }
        val init = InitializeUlb(
            directoryProvider,
            installedEntityRepo,
            importSpy
        )
        init.exec(mockProgressEmitter)
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()
        verify(exactly = sourceCount) { importSpy.import(any(), any(), any()) }
    }

    private fun countAvailableSources(): Int {
        val sourceJson = javaClass.classLoader.getResource(SOURCES_JSON_FILE)
        val tempFile = directoryProvider.tempDirectory.resolve(SOURCES_JSON_FILE)
            .apply {
                createNewFile()
                deleteOnExit()
            }
        sourceJson.openStream().use { input ->
            tempFile.outputStream().use { output ->
                input.transferTo(output)
            }
        }

        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
        val resources: List<ResourceInfoSerializable> = mapper.readValue(tempFile)
        val availableSourceCount = resources.filter { res -> urlExists(res.url) }.size
        return availableSourceCount
    }

    /**
     * "Pings" the endpoint URL to see if they are available.
     */
    private fun urlExists(url: String): Boolean {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "HEAD"
        val responseCode = connection.responseCode
        return responseCode == HttpURLConnection.HTTP_OK
    }

}

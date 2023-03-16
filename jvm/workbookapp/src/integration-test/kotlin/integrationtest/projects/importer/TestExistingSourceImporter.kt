package integrationtest.projects.importer

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.importer.ExistingSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.NewSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.resourcecontainer.DeleteResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
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
    fun replaceExistingSourceWhenVersionDifferent() {
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
                    ContentType.TEXT to 31102
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
            RowCount(collections = 1,contents = mapOf(),links = 0)
        )

        val newSource = resourceMetadataRepository.getAllSources().blockingGet().single()

        Assert.assertEquals("999",newSource.version)
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
}
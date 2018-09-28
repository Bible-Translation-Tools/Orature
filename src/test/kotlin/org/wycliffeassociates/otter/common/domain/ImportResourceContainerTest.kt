package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.repositories.*
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ImportResourceContainerTest {

    val mockLanguageRepo = MockLanguageRepository()
    val mockMetadataRepo = MockResourceMetadataRepository()
    val mockCollectionRepo = MockCollectionRepository()
    val mockDirectoryProvider = Mockito.mock(IDirectoryProvider::class.java)

    // Required in Kotlin to use Mockito any() argument matcher
    fun <T> helperAny(): T {
        return ArgumentMatchers.any()
    }

    @Before
    fun setup() {

        // Configure the mock directory provider
        Mockito.`when`(mockDirectoryProvider.resourceContainerDirectory)
                .thenReturn(
                        createTempDir()
                )
    }

    @Test
    fun testImport() {
        val classLoader = this.javaClass.classLoader
        val resource = File(classLoader.getResource("valid_single_book_rc").toURI().path)
        assertTrue(resource.exists())
        val rcImporter = ImportResourceContainer(
                mockLanguageRepo,
                mockMetadataRepo,
                mockCollectionRepo,
                mockDirectoryProvider
        )

        rcImporter.import(resource)
    }


}

class MockCollectionRepository: ICollectionRepository {
    override fun getChildren(collection: Collection): Single<List<Collection>> {
        return Single.just(listOf(Mockito.mock(Collection::class.java)))
    }

    override fun updateSource(collection: Collection, newSource: Collection): Completable {
       return Completable.complete()
    }

    override fun updateParent(collection: Collection, newParent: Collection): Completable {
        return Completable.complete()
    }

    override fun getAll(): Single<List<Collection>> {
        return Single.just(listOf(Mockito.mock(Collection::class.java)))
    }

    override fun update(obj: Collection): Completable {
        return Completable.complete()
    }

    override fun delete(obj: Collection): Completable {
        return Completable.complete()
    }

    override fun insert(obj: Collection): Single<Int> {
        return Single.just(1)
    }
}

class MockLanguageRepository: ILanguageRepository {
    override fun getGateway(): Single<List<Language>> {
        return Single.just(listOf(Mockito.mock(Language::class.java)))
    }

    override fun getTargets(): Single<List<Language>> {
        return Single.just(listOf(Mockito.mock(Language::class.java)))
    }

    override fun getAll(): Single<List<Language>> {
        return Single.just(listOf(Mockito.mock(Language::class.java)))
    }

    override fun getBySlug(slug: String): Single<Language> {
        return Single.just(Mockito.mock(Language::class.java))
    }

    override fun update(obj: Language): Completable {
        return Completable.complete()
    }

    override fun delete(obj: Language): Completable {
        return Completable.complete()
    }

    override fun insert(obj: Language): Single<Int> {
        return Single.just(1)
    }
}

class MockResourceMetadataRepository: IResourceMetadataRepository {

    override fun getAll(): Single<List<ResourceMetadata>> {
        return Single.just(listOf(Mockito.mock(ResourceMetadata::class.java)))
    }

    override fun update(obj: ResourceMetadata): Completable {
        return Completable.complete()
    }

    override fun delete(obj: ResourceMetadata): Completable {
        return Completable.complete()
    }

    override fun insert(obj: ResourceMetadata): Single<Int> {
        return Single.just(1)
    }
}
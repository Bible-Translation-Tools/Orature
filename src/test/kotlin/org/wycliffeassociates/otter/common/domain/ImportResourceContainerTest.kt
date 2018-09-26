package org.wycliffeassociates.otter.common.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.dao.LanguageDao
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ImportResourceContainerTest {

    val mockLanguageDao = MockLanguageDao()
    val mockMetadataDao = MockResourceMetadataDao()
    val mockCollectionDao = MockCollectionDao()
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
                mockLanguageDao,
                mockMetadataDao,
                mockCollectionDao,
                mockDirectoryProvider
        )

        rcImporter.import(resource)
    }


}

class MockCollectionDao: Dao<Collection> {
    override fun getById(id: Int): Observable<Collection> {
        return Observable.just(Mockito.mock(Collection::class.java))
    }

    override fun getAll(): Observable<List<Collection>> {
        return Observable.just(listOf(Mockito.mock(Collection::class.java)))
    }

    override fun update(obj: Collection): Completable {
        return Completable.complete()
    }

    override fun delete(obj: Collection): Completable {
        return Completable.complete()
    }

    override fun insert(obj: Collection): Observable<Int> {
        return Observable.just(1)
    }
}

class MockLanguageDao: LanguageDao {
    override fun getBySlug(slug: String): Observable<Language> {
        return Observable.just(Mockito.mock(Language::class.java))
    }

    override fun getById(id: Int): Observable<Language> {
        return Observable.just(Mockito.mock(Language::class.java))
    }

    override fun getAll(): Observable<List<Language>> {
        return Observable.just(listOf(Mockito.mock(Language::class.java)))
    }

    override fun update(obj: Language): Completable {
        return Completable.complete()
    }

    override fun delete(obj: Language): Completable {
        return Completable.complete()
    }

    override fun insert(obj: Language): Observable<Int> {
        return Observable.just(1)
    }
}

class MockResourceMetadataDao: Dao<ResourceMetadata> {
    override fun getById(id: Int): Observable<ResourceMetadata> {
        return Observable.just(Mockito.mock(ResourceMetadata::class.java))
    }

    override fun getAll(): Observable<List<ResourceMetadata>> {
        return Observable.just(listOf(Mockito.mock(ResourceMetadata::class.java)))
    }

    override fun update(obj: ResourceMetadata): Completable {
        return Completable.complete()
    }

    override fun delete(obj: ResourceMetadata): Completable {
        return Completable.complete()
    }

    override fun insert(obj: ResourceMetadata): Observable<Int> {
        return Observable.just(1)
    }
}
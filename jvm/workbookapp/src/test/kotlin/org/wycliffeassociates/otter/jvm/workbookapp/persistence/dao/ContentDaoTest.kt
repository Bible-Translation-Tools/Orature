package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.TestDataStore
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.ContentTypeDao
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.RecordMappers
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ContentEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.ContentMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import java.io.File

class ContentDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-content-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.contentDao }

    private lateinit var defaultEntity: ContentEntity
    private var defaultProjectId = 0 // updated after insertion

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile)
        seedLanguages()
        seedResourceMetadata()
        seedCollections()
        defaultEntity = ContentMapper(database.contentTypeDao)
            .mapToEntity(TestDataStore.content.first())
            .copy(
                id = 0,
                collectionFk = defaultProjectId,
                selectedTakeFk = null
            )
    }

    @Test
    fun testInsert() {
        Assert.assertEquals(0, dao.fetchAll().size)
        insertDefault()
        Assert.assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testNoReturn() {
        Assert.assertEquals(0, dao.fetchAll().size)
        dao.insertNoReturn(defaultEntity)
        Assert.assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testFetchByCollectionId() {
        insertDefault()
        val collectionId = database.collectionDao.fetchAll().first().id
        val result = dao.fetchByCollectionId(collectionId)

        Assert.assertEquals(1, result.size)
    }

    @Test
    fun testFetchByCollectionIdAndType() {
        insertDefault()
        val collectionId = database.collectionDao.fetchAll().first().id
        val result = dao.fetchByCollectionIdAndType(collectionId, ContentType.TEXT)
        val emptyResult = dao.fetchByCollectionIdAndType(collectionId, ContentType.BODY)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(0, emptyResult.size)
    }

    @Test
    fun testFetchAndUpdateSources() {
        insertAllSamples()
        Assert.assertEquals(3, dao.fetchAll().size)

        val chapterContentTypeId = ContentTypeDao(database.dsl).fetchId(ContentType.META)
        val verseContentTypeId = ContentTypeDao(database.dsl).fetchId(ContentType.TEXT)
        val chapter = dao.fetchAll().first { it.type_fk == chapterContentTypeId }
        val content = dao.fetchAll().first { it.type_fk == verseContentTypeId }

        Assert.assertEquals(0, dao.fetchSources(content).size)

        dao.updateSources(content, listOf(chapter))
        val result = dao.fetchSources(content)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(chapter, result.first())
    }

    @Test
    fun testFetchContentByProjectSlug() {
        val chapterCollectionId = database.collectionDao
            .insert(
                CollectionDaoTest.defaultCollection
                    .copy(
                        slug = "chapter-test",
                        title = "chapter-test",
                        label = "chapter-test",
                        sort = 2,
                        parentFk = defaultProjectId
                    )
            )
        dao.insert(
            defaultEntity.copy(
                collectionFk = chapterCollectionId
            )
        )

        val contents = dao.fetchContentByProjectSlug(
            CollectionDaoTest.defaultCollection.slug
        ).fetch {
            RecordMappers.mapToContentEntity(it)
        }

        Assert.assertEquals(1, contents.size)
        Assert.assertEquals(chapterCollectionId, contents.first().collectionFk)
    }

    @Test
    fun testDelete() {
        insertDefault()
        Assert.assertEquals(1, dao.fetchAll().size)
        dao.delete(defaultEntity.copy(id = 1))
        Assert.assertEquals(0, dao.fetchAll().size)
    }

    @Test
    fun testDeleteForCollection() {
        insertAllSamples()

        val newCollectionId = database.collectionDao
            .insert(
                CollectionDaoTest.defaultCollection
                    .copy(
                        slug = "second-collection"
                    )
            )
        dao.insert(
            defaultEntity.copy(
                id = 0,
                collectionFk = newCollectionId
            )
        )

        Assert.assertEquals(4, dao.fetchAll().size)

        val collection = database.collectionDao.fetchById(newCollectionId)
        dao.deleteForCollection(collection)

        Assert.assertEquals(3, dao.fetchAll().size)
    }

    private fun insertDefault(): Int {
        return dao.insert(defaultEntity)
    }

    private fun insertAllSamples() {
        val collectionId = database.collectionDao.fetchAll().first().id
        val mapper = ContentMapper(database.contentTypeDao)

        TestDataStore.content.forEach {
            val contentEntity = mapper.mapToEntity(it)
                .copy(
                    id = 0,
                    collectionFk = collectionId,
                    selectedTakeFk = null
                )
            dao.insert(contentEntity)
        }
    }

    private fun seedLanguages() {
        database.languageDao
            .insertAll(
                TestDataStore.languages.map {
                    LanguageMapper().mapToEntity(it)
                }
            )
    }

    private fun seedResourceMetadata() {
        ResourceMetadataDaoTest.sampleEntities.forEach {
            database.resourceMetadataDao.insert(it)
        }
    }

    private fun seedCollections() {
        defaultProjectId = database.collectionDao
            .insert(CollectionDaoTest.defaultCollection)
    }
}
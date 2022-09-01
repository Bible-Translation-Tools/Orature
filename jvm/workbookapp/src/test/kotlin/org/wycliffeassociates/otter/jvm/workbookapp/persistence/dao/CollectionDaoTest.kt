package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.TestDataStore
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.LanguageMapper
import java.io.File

class CollectionDaoTest {
    private val testDatabaseFile = File.createTempFile(
        "test-collection-dao", ".sqlite"
    ).also(File::deleteOnExit)
    private lateinit var database: AppDatabase
    private val dao by lazy { database.collectionDao }

    private val defaultCollection = CollectionEntity(
        id = 0,
        parentFk = null,
        sourceFk = null,
        label = "test",
        title = "test",
        slug = "test",
        sort = 1,
        dublinCoreFk = 1,
        modifiedTs = null
    )

    @Before
    fun setup() {
        database = AppDatabase(testDatabaseFile)
        seedLanguages()
        ResourceMetadataDaoTest.sampleEntities.forEach {
            database.resourceMetadataDao.insert(it)
        }
    }

    @Test
    fun testInsertCollection() {
        Assert.assertEquals(0, dao.fetchAll().size)

        insertDefaultCollection()

        Assert.assertEquals(1, dao.fetchAll().size)
    }

    @Test
    fun testFetch() {
        insertDefaultCollection()
        val result = dao.fetch(
            defaultCollection.slug,
            defaultCollection.dublinCoreFk!!,
            defaultCollection.label
        )

        Assert.assertNotNull(result)
        Assert.assertEquals(
            defaultCollection.copy(id = result!!.id),
            result
        )
    }

    @Test
    fun testFetchByLabel() {
        insertDefaultCollection()
        val result = dao.fetchByLabel(defaultCollection.label)

        Assert.assertEquals(1, result.size)
        Assert.assertEquals(defaultCollection.label, result.first().label)
    }

    @Test
    fun testFetchChildren() {
        insertDefaultCollection()
        val entity = dao.fetchAll().single()
        val child1 = entity.copy(id = 0, parentFk = entity.id, slug = "child-1")
        val child2 = entity.copy(id = 0, parentFk = entity.id, slug = "child-2")

        dao.insert(child1)
        dao.insert(child2)
        val children = dao.fetchChildren(entity)

        Assert.assertEquals(2, children.size)

        val firstChild = children.find { it.slug == child1.slug }
        val secondChild = children.find { it.slug == child2.slug }
        Assert.assertNotNull(firstChild)
        Assert.assertNotNull(secondChild)
    }

    @Test
    fun testFetchSource() {
        insertDefaultCollection()
        val source = dao.fetchAll().single()
        val target = source.copy(id = 0, sourceFk = source.id, slug = "source-collection")

        dao.insert(target)
        val result = dao.fetchSource(target)

        Assert.assertNotNull(result)
        Assert.assertEquals(source, result)
    }

    @Test
    fun testUpdate() {
        insertDefaultCollection()
        val entity = dao.fetchAll().single()
        val updated = CollectionEntity(
            parentFk = null,
            sourceFk = null,
            title = "updated",
            label = "updated",
            slug = "updated",
            sort = 1,
            modifiedTs = "updated",
            dublinCoreFk = 1,
            id = entity.id
        )

        dao.update(updated)

        Assert.assertEquals(updated, dao.fetchAll().single())
    }

    @Test
    fun testDelete() {
        insertDefaultCollection()

        fun fetch() = dao.fetch(
            defaultCollection.slug, defaultCollection.dublinCoreFk!!, defaultCollection.label
        )

        var entity = fetch()
        Assert.assertNotNull(entity)

        dao.delete(entity!!)
        entity = fetch()

        Assert.assertNull(entity)
    }

    private fun insertDefaultCollection() {
        dao.insert(defaultCollection)
    }

    private fun seedLanguages() {
        database.languageDao
            .insertAll(
                TestDataStore.languages.map {
                    LanguageMapper().mapToEntity(it)
                }
            )
    }
}
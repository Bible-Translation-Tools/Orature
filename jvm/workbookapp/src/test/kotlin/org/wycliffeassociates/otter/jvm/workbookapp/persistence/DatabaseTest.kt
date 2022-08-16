package org.wycliffeassociates.otter.jvm.workbookapp.persistence

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import jooq.tables.InstalledEntity
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DATABASE_INSTALLABLE_NAME
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.SCHEMA_VERSION
import java.io.File
import kotlin.io.path.createTempDirectory

class DatabaseTest {
    private val CURRENT_LATEST_VER = 8

    private val databaseDir = createTempDirectory("otter-database").toFile()
    private val databaseFile = databaseDir.resolve("test-db-v8.sqlite")
    private val databaseArchiveDir = databaseDir.resolve("archive")

    private val oldDatabaseDir = createTempDirectory("otter-old-database").toFile()
    private val oldDbFile = oldDatabaseDir.resolve("content.sqlite")

    private val schemaFile = File(javaClass.classLoader.getResource("sql/AppDatabaseSchema_v8.sql").file)
    private val oldSchemaFile = File(javaClass.classLoader.getResource("sql/AppDatabaseSchema0.sql").file)

    private val directoryProviderMock = mock<DirectoryProvider> {
        on { databaseDirectory } doReturn databaseDir
        on { getUserDataDirectory(any()) } doReturn oldDatabaseDir
    }

    @After
    fun cleanUp() {
        databaseDir.deleteRecursively()
        databaseArchiveDir.deleteRecursively()
        oldDatabaseDir.deleteRecursively()
    }

    @Before
    fun setup() {
        databaseDir.mkdirs()
        databaseArchiveDir.mkdirs()
        oldDatabaseDir.mkdirs()

        JooqTestConfiguration.createDatabase(databaseFile.absolutePath, schemaFile)
        JooqTestConfiguration.createDatabase(oldDbFile.absolutePath, oldSchemaFile)
            .dsl()
            .use {
                it.fetch("""INSERT INTO "installed_entity" VALUES ('DATABASE',1);""")
            }

        Assert.assertTrue(databaseFile.exists() && databaseFile.length() > 0)
        Assert.assertEquals(CURRENT_LATEST_VER, getDatabaseVersion(databaseFile))
        Assert.assertTrue(oldDbFile.exists() && oldDbFile.length() > 0)
        Assert.assertEquals(1, getDatabaseVersion(oldDbFile))
    }

    @Test
    fun `test handle multiple databases exist`() {
        Assert.assertTrue(oldDbFile.exists())
        Assert.assertTrue(databaseArchiveDir.list().isEmpty())

        AppDatabase(databaseFile, directoryProviderMock)

        Assert.assertFalse(oldDbFile.exists())
        Assert.assertTrue(databaseArchiveDir.list().isNotEmpty())
    }

    @Test
    fun `test when only old database exists`() {
        databaseFile.delete()
        Assert.assertFalse(databaseFile.exists())
        Assert.assertTrue(oldDbFile.exists())

        AppDatabase(databaseFile, directoryProviderMock)

        Assert.assertTrue(databaseFile.exists())
        Assert.assertFalse(oldDbFile.exists())

        val version = getDatabaseVersion(databaseFile)
        Assert.assertEquals(CURRENT_LATEST_VER, version)
    }

    private fun getDatabaseVersion(dbFile: File): Int {
        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${dbFile.path}"
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")

        DSL.using(sqLiteDataSource, SQLDialect.SQLITE).use { _dsl ->
            return _dsl
                .select()
                .from(InstalledEntity.INSTALLED_ENTITY)
                .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq(DATABASE_INSTALLABLE_NAME))
                .fetchSingle()
                .getValue(InstalledEntity.INSTALLED_ENTITY.VERSION)
        }
    }
}
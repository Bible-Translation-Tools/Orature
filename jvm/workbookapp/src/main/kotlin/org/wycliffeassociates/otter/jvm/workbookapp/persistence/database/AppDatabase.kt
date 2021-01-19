package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database

import jooq.tables.AudioPluginEntity
import jooq.tables.InstalledEntity
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.exists
import org.slf4j.LoggerFactory
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.*
import java.io.File
import java.io.IOException

const val CREATION_SCRIPT = "sql/CreateAppDb.sql"

class AppDatabase(
    databaseFile: File
) {
    val logger = LoggerFactory.getLogger(AppDatabase::class.java)

    val dsl: DSLContext

    val version = 2

    init {
        System.setProperty("org.jooq.no-logo", "true")

        // Load the SQLite JDBC drivers
        Class
            .forName("org.sqlite.JDBC")
            .getDeclaredConstructor()
            .newInstance()

        // Create a new sqlite data source
        val dbDoesNotExist = !databaseFile.exists() || databaseFile.length() == 0L

        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${databaseFile.toURI().path}"

        // Enable foreign key constraints (disabled by default for backwards compatibility)
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")

        // Create the jooq dsl
        dsl = DSL.using(sqLiteDataSource.connection, SQLDialect.SQLITE)
        if (dbDoesNotExist) setup()
        migrate()
    }

    private fun setup() {
        // Setup the tables
        val schemaFileStream = ClassLoader.getSystemResourceAsStream(CREATION_SCRIPT)
            ?: throw IOException("Couldn't read database creation script $CREATION_SCRIPT")

        // Make sure the database file has the tables we need
        val sqlStatements = schemaFileStream
            .bufferedReader()
            .readText()
            .split(";")
            .filter { it.isNotEmpty() }
            .map { "$it;" }

        // Execute each SQL statement
        sqlStatements.forEach {
            dsl.fetch(it)
        }
    }

    // Create the DAOs
    val languageDao = LanguageDao(dsl)
    val resourceMetadataDao = ResourceMetadataDao(dsl)
    val collectionDao = CollectionDao(dsl)
    val contentTypeDao = ContentTypeDao(dsl)
    val contentDao = ContentDao(dsl, contentTypeDao)
    val resourceLinkDao = ResourceLinkDao(dsl)
    val subtreeHasResourceDao = SubtreeHasResourceDao(dsl)
    val takeDao = TakeDao(dsl)
    val markerDao = MarkerDao(dsl)
    val audioPluginDao = AudioPluginDao(dsl)
    val preferenceDao = PreferenceDao(dsl)
    val installedEntityDao = InstalledEntityDao(dsl)

    // Transaction support
    fun transaction(block: (DSLContext) -> Unit) {
        dsl.transaction { config ->
            // Create local transaction DSL and pass to block
            block(DSL.using(config))
        }
    }

    fun <T> transactionResult(block: (DSLContext) -> T): T {
        return dsl.transactionResult { config ->
            // Create local transaction DSL and pass to block
            block(DSL.using(config))
        }
    }

    fun migrate() {
        var currentVersion = getDatabaseVersion()
        if (currentVersion != version) {
            currentVersion = `migrate 0 to 1`(currentVersion)
            currentVersion = `migrate 1 to 2`(currentVersion)
            updateDatabaseVersion(currentVersion)
        }
    }

    private fun getDatabaseVersion(): Int {
        val databaseVersionExists =
            dsl
                .fetchExists(
                    dsl
                        .select()
                        .from(InstalledEntity.INSTALLED_ENTITY)
                        .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq("database"))
                )
        return if (databaseVersionExists) {
            dsl
                .selectOne()
                .from(InstalledEntity.INSTALLED_ENTITY)
                .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq("database")).execute()
        } else 0
    }

    private fun `migrate 0 to 1`(current: Int): Int {
        return if (current < 1) {
            dsl
                .insertInto(
                    InstalledEntity.INSTALLED_ENTITY,
                    InstalledEntity.INSTALLED_ENTITY.NAME,
                    InstalledEntity.INSTALLED_ENTITY.VERSION
                ).values("database", 1)
                .execute()
            logger.info("Updated database from version 0 to 1")
            return 1
        } else current
    }

    private fun `migrate 1 to 2`(current: Int): Int {
        return if (current < 2) {
            dsl
                .alterTable(AudioPluginEntity.AUDIO_PLUGIN_ENTITY)
                .addColumnIfNotExists(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK)
            logger.info("Updated database from version 1 to 2")
            return 2
        } else {
            current
        }
    }

    private fun updateDatabaseVersion(version: Int) {
        dsl
            .update(InstalledEntity.INSTALLED_ENTITY)
            .set(InstalledEntity.INSTALLED_ENTITY.VERSION, version)
            .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq("database"))
            .execute()
    }
}

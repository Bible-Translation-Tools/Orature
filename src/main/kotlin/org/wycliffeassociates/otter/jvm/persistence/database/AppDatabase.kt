package org.wycliffeassociates.otter.jvm.persistence.database

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.jvm.persistence.database.daos.*
import java.io.File

class AppDatabase(
    databaseFile: File
) {
    val dsl: DSLContext

    init {
        // Load the SQLite JDBC drivers
        Class
            .forName("org.sqlite.JDBC")
            .newInstance()

        // Create a new sqlite data source
        val dbDoesNotExist = !databaseFile.exists()

        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${databaseFile.toURI().path}"

        // Enable foreign key constraints (disabled by default for backwards compatibility)
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")

        // Create the jooq dsl
        dsl = DSL.using(sqLiteDataSource.connection, SQLDialect.SQLITE)
        if (dbDoesNotExist) setup()
    }

    private fun setup() {
        // Setup the tables
        val schemaFileStream = ClassLoader.getSystemResourceAsStream("sql/CreateAppDb.sql")

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

    // Transaction support
    fun transaction(block: (DSLContext) -> Unit) {
        dsl.transaction { config ->
            // Create local transaction DSL and pass to block
            block(DSL.using(config))
        }
    }
}
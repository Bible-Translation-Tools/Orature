package org.wycliffeassociates.otter.jvm.persistence.database

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.jvm.persistence.database.daos.*
import org.wycliffeassociates.otter.jvm.persistence.entities.AudioPluginEntity
import java.io.File

class AppDatabase(
        databaseFile: File
) : IAppDatabase {
    private val dsl: DSLContext

    init {
        // Load the SQLite JDBC drivers
        Class
                .forName("org.sqlite.JDBC")
                .newInstance()

        // Create a new sqlite data source
        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${databaseFile.toURI().path}"

        // Enable foreign key constraints (disabled by default for backwards compatibility)
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")

        // Create the jooq dsl
        dsl = DSL.using(sqLiteDataSource.connection, SQLDialect.SQLITE)

        // Check if the database file exists
        val schemaFile = File(listOf("src", "main", "Resources", "CreateAppDb.sql").joinToString(File.separator))

        // Make sure the database file has the tables we need
        val sqlStatements = schemaFile
                .readText()
                .split(";")
                .filter { it.isNotEmpty() }
                .map { "$it;" }

        // Execute each SQL statement
        sqlStatements.forEach {
            dsl.fetch(it)
        }
    }

    // Create the daos
    private var languageDao = LanguageDao(dsl)
    private val resourceMetadataDao = ResourceMetadataDao(dsl)
    private val collectionDao = CollectionDao(dsl)
    private val chunkDao = ChunkDao(dsl)
    private val resourceLinkDao = ResourceLinkDao(dsl)
    private val takeDao = TakeDao(dsl)
    private val markerDao = MarkerDao(dsl)
    private val audioPluginDao = AudioPluginDao(dsl)

    // Override the getters
    override fun getLanguageDao(): ILanguageDao = languageDao
    override fun getResourceMetadataDao(): IResourceMetadataDao = resourceMetadataDao
    override fun getCollectionDao(): ICollectionDao = collectionDao
    override fun getChunkDao(): IChunkDao = chunkDao
    override fun getResourceLinkDao(): IResourceLinkDao = resourceLinkDao
    override fun getTakeDao(): ITakeDao = takeDao
    override fun getMarkerDao(): IMarkerDao = markerDao
    override fun getAudioPluginDao(): IDao<AudioPluginEntity> = audioPluginDao
}
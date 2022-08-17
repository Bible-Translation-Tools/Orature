package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database

import jooq.tables.InstalledEntity
import org.jooq.SQLDialect
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.common.persistence.IDatabaseUtil
import java.io.File
import javax.inject.Inject

class DatabaseUtil @Inject constructor() : IDatabaseUtil {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun getDatabaseVersion(databaseFile: File): Int {
        if (!databaseFile.exists() || databaseFile.length() == 0L) {
            logger.error("Error getting database version - Database doesn't exist at: $databaseFile")
            return -1
        }

        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${databaseFile.toURI().path}"
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")

        DSL.using(sqLiteDataSource, SQLDialect.SQLITE).use { dsl ->
            return try {
                dsl
                    .select()
                    .from(InstalledEntity.INSTALLED_ENTITY)
                    .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq(DATABASE_INSTALLABLE_NAME))
                    .fetchAny {
                        it.get(InstalledEntity.INSTALLED_ENTITY.VERSION)
                    }
            } catch (e: DataAccessException) {
                logger.error("Error getting version of the database at: $databaseFile", e)
                -1
            }
        }
    }

    override fun getSchemaVersion() = SCHEMA_VERSION
}
package org.wycliffeassociates.otter.common.persistence

import java.io.File

interface IDatabaseUtil {
    fun getDatabaseVersion(databaseFile: File): Int
    fun getSchemaVersion(): Int
}
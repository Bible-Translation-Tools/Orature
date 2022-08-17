package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.wycliffeassociates.otter.common.persistence.IDatabaseUtil
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Initializable
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class InitializeDatabase @Inject constructor(
    val directoryProvider: IDirectoryProvider,
    val dbUtil: IDatabaseUtil
) : Initializable {

    override fun exec(): Completable {
        return Completable.fromAction(::initialize)
    }

    private fun initialize() {
        val databaseFile = directoryProvider.databaseDirectory.resolve("app_db.sqlite")
        val oldDbFile = directoryProvider.getUserDataDirectory().resolve("content.sqlite")
        val oldDbExist = oldDbFile.exists() && oldDbFile.length() > 0
        val currentDbExists = databaseFile.exists() && databaseFile.length() > 0

        when {
            oldDbExist && currentDbExists -> {
                archiveDb(databaseFile)
                Files.move(oldDbFile.toPath(), databaseFile.toPath())
            }
            oldDbExist -> {
                Files.move(oldDbFile.toPath(), databaseFile.toPath())
            }
            currentDbExists -> {
                val existingDbSchemaVersion = dbUtil.getDatabaseVersion(databaseFile)
                val installedSchemaVersion = dbUtil.getSchemaVersion()
                if (existingDbSchemaVersion > installedSchemaVersion) {
                    archiveDb(databaseFile)
                }
            }
        }
    }

    private fun archiveDb(dbFile: File) {
        val archive = directoryProvider.databaseDirectory
            .resolve("archive")
            .let {
                it.mkdirs()
                it.resolve(
                    "app_db-${System.currentTimeMillis()}.sqlite"
                )
            }

        Files.move(
            dbFile.toPath(),
            archive.toPath()
        )
    }
}
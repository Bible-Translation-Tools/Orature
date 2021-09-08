/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import java.io.File
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import javax.inject.Inject
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider

private const val ARTWORK_FILENAME = "bible_artwork.zip"
private const val ARTWORK_PATH = "content/$ARTWORK_FILENAME"

class InitializeArtwork @Inject constructor(
    private val installedEntityRepo: IInstalledEntityRepository,
    private val directoryProvider: IDirectoryProvider
) : Installable {

    override val name = "ARTWORK"
    override val version = 1

    private val log = LoggerFactory.getLogger(InitializeArtwork::class.java)

    override fun exec(): Completable {
        return Completable
            .fromCallable {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    log.info("Initializing $name version: $version...")
                    copyBibleArtworkContainer()
                } else {
                    log.info("$name up to date with version: $version")
                }
            }
            .doOnError { e ->
                log.error("Error in initializeArtwork", e)
            }
    }

    private fun copyBibleArtworkContainer() {
        if (!File(directoryProvider.resourceContainerDirectory, ARTWORK_FILENAME).exists()) {
            log.info("Copying bible artwork")
            ClassLoader.getSystemResourceAsStream(ARTWORK_PATH)
                .transferTo(
                    File(
                        directoryProvider.resourceContainerDirectory.absolutePath,
                        ARTWORK_FILENAME
                    ).outputStream()
                )
        } else {
            log.info("Artwork not initialized but ${ARTWORK_FILENAME} exists in rc directory")
        }
    }
}

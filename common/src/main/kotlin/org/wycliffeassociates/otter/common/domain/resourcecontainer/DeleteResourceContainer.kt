/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.resourcecontainer

import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import javax.inject.Inject

class DeleteResourceContainer @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceContainerRepository: IResourceContainerRepository
) {
    private val logger = LoggerFactory.getLogger(DeleteResourceContainer::class.java)

    fun delete(resourceContainer: ResourceContainer): Single<DeleteResult> {
        return resourceContainerRepository
            .removeResourceContainer(resourceContainer)
            .doOnError {
                logger.error("Error when trying to delete rc: ${resourceContainer.file}.")
            }
            .doOnSuccess {
                if (it == DeleteResult.SUCCESS) {
                    val file = directoryProvider.getSourceContainerDirectory(resourceContainer)
                    logger.info("Deleting RC: $file")
                    if (file.deleteRecursively()) {
                        logger.error("RC partially deleted: $file")
                    } else {
                        logger.info("RC deleted successfully!")
                    }
                }
            }
    }
}
/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Initializable
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import java.io.File
import javax.inject.Inject

private const val ULB_VERSIFICATION_FILE = "ulb.json"
private const val UFW_VERSIFICATION_FILE = "ufw.json"
private const val ULB_VERSIFICATION_RESOURCE_PATH = "versification/ulb_versification.json"

class InitializeVersification @Inject constructor(
    val directoryProvider: IDirectoryProvider,
    val versificationRepository: IVersificationRepository
) : Initializable {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun exec(progressEmitter: ObservableEmitter<ProgressStatus>): Completable {
        return Single.fromCallable {
            progressEmitter.onNext(ProgressStatus(titleKey = "initializingVersification"))
            copyUlbVersification()

            directoryProvider.versificationDirectory.listFiles()?.forEach { file ->
                if (file.extension == "json") {
                    logger.info("Inserting versification: ${file.name}")
                    versificationRepository.insertVersification(file.nameWithoutExtension, file).blockingAwait()
                }
            }
        }.subscribeOn(Schedulers.io())
            .ignoreElement()
    }

    private fun copyUlbVersification() {
        if (!File(directoryProvider.versificationDirectory, ULB_VERSIFICATION_FILE).exists()) {
            directoryProvider.versificationDirectory.mkdirs()
            logger.info("Copying ulb versification")
            ClassLoader.getSystemResourceAsStream(ULB_VERSIFICATION_RESOURCE_PATH)
                .transferTo(
                    File(
                        directoryProvider.versificationDirectory.absolutePath,
                        ULB_VERSIFICATION_FILE
                    ).outputStream()
                )
            ClassLoader.getSystemResourceAsStream(ULB_VERSIFICATION_RESOURCE_PATH)
                .transferTo(
                    File(
                        directoryProvider.versificationDirectory.absolutePath,
                        UFW_VERSIFICATION_FILE
                    ).outputStream()
                )
        }
    }
}

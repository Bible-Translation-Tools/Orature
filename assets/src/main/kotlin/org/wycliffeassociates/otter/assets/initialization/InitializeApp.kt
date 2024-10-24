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

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import javax.inject.Inject

class InitializeApp @Inject constructor(
    private val initializeVersification: InitializeVersification,
    private val initializeSources: InitializeSources,
    private val initializeLanguages: InitializeLanguages,
    private val initializeUlb: InitializeUlb,
    private val initializeRecorder: InitializeRecorder,
    private val initializeMarker: InitializeMarker,
    private val initializePlugins: InitializePlugins,
    private val initializeTakeRepository: InitializeTakeRepository,
    private val initializeProjects: InitializeProjects,
    private val initializeTranslations: InitializeTranslations,
    private val directoryProvider: IDirectoryProvider
) {

    private val logger = LoggerFactory.getLogger(InitializeApp::class.java)

    fun initApp(): Observable<ProgressStatus> {
        val progressObservable = Observable
            .create<ProgressStatus> { progressStatusEmitter ->
                val initializers = listOf(
                    initializeVersification,
                    initializeLanguages,
                    initializeSources,
                    initializeUlb,
                    initializeRecorder,
                    initializeMarker,
                    initializePlugins,
                    initializeTakeRepository,
                    initializeProjects,
                    initializeTranslations
                )

                var total = 0.0
                val increment = (1.0).div(initializers.size)
                initializers.forEach {
                    total += increment
                    progressStatusEmitter.onNext(
                        ProgressStatus(percent = total)
                    )
                    it.exec(progressStatusEmitter).blockingAwait()
                }
                progressStatusEmitter.onComplete()
            }
            .doOnError { e ->
                logger.error("Error in initApp", e)
            }
            .doFinally {
                directoryProvider.cleanTempDirectory() // clears out temp files after migration & init
            }
            .subscribeOn(Schedulers.io())

        return progressObservable
    }
}
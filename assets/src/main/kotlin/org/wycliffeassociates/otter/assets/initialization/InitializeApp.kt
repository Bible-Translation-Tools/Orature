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

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import javax.inject.Inject

class InitializeApp @Inject constructor(
    private val initializeLanguages: InitializeLanguages,
    private val initializeUlb: InitializeUlb,
    private val initializeArtwork: InitializeArtwork,
    private val initializeRecorder: InitializeRecorder,
    private val initializeMarker: InitializeMarker,
    private val initializePlugins: InitializePlugins,
    private val initializeTakeRepository: InitializeTakeRepository,
    private val initializeProjects: InitializeProjects,
    private val initializeTranslations: InitializeTranslations
) {

    private val logger = LoggerFactory.getLogger(InitializeApp::class.java)

    fun initApp(): Observable<Double> {
        return Observable
            .fromPublisher<Double> { progress ->
                val initializers = listOf(
                    initializeLanguages,
                    initializeUlb,
                    initializeArtwork,
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
                    progress.onNext(total)
                    it.exec().blockingAwait()
                }
                progress.onComplete()
            }
            .doOnError { e ->
                logger.error("Error in initApp", e)
            }
            .subscribeOn(Schedulers.io())
    }
}

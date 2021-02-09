package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import javax.inject.Inject

class InitializeApp @Inject constructor(
    private val initializeLanguages: InitializeLanguages,
    private val initializeUlb: InitializeUlb,
    private val initializeRecorder: InitializeRecorder,
    private val initializeMarker: InitializeMarker,
    private val initializePlugins: InitializePlugins,
    private val initializeTakeRepository: InitializeTakeRepository,
    private val initializeProjects: InitializeProjects
) {

    private val logger = LoggerFactory.getLogger(InitializeApp::class.java)

    fun initApp(): Observable<Double> {
        return Observable
            .fromPublisher<Double> { progress ->
                val initializers = listOf(
                    initializeLanguages,
                    initializeUlb,
                    initializeRecorder,
                    initializeMarker,
                    initializePlugins,
                    initializeTakeRepository,
                    initializeProjects
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

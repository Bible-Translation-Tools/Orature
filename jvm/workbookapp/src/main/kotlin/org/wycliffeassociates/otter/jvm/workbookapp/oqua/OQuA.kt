package org.wycliffeassociates.otter.jvm.workbookapp.oqua

import javafx.stage.Stage
import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*

fun main() {
    launch<OQuAApp>()
}

class OQuAApp : App(OQuAWorkspace::class), IDependencyGraphProvider {
    override val dependencyGraph = DaggerAppDependencyGraph.builder().build()

    init {
        dependencyGraph.injectConfigureAudioSystem().configure()
    }

    override fun start(stage: Stage) {
        super.start(stage)

        stage.isMaximized = true
        stage.minWidth = 300.0
        stage.minHeight = 350.0
    }
}
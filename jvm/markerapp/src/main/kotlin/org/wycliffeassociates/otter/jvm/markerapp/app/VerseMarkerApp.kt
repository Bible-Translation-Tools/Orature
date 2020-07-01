package org.wycliffeassociates.otter.jvm.markerapp.app

import com.sun.javafx.application.ParametersImpl
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*

class VerseMarkerApp: App() {
    override val primaryView = MarkerView::class

    init {
        val args =
            if (!parameters?.raw.isNullOrEmpty()) parameters.raw.toTypedArray() else arrayOf("--wav=recording.wav")
        this.scope = ParameterizedScope(ParametersImpl(args)) {}
    }
}
package org.wycliffeassociates.otter.jvm.recorder.app

import com.sun.javafx.application.ParametersImpl
import org.wycliffeassociates.otter.jvm.recorder.app.view.RecorderView
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*

class RecordingApp : App() {

    override val primaryView = RecorderView::class

    init {
        val args =
            if (!parameters?.raw.isNullOrEmpty()) parameters.raw.toTypedArray() else arrayOf("--wav=recording.wav")
        this.scope = ParameterizedScope(ParametersImpl(args)) {}
    }
}

package org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.io.wav.WavFile
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File

class VerseMarkerViewModel : ViewModel() {

    val markerCountProperty = SimpleIntegerProperty()
    val markerRatioProperty = SimpleStringProperty()
    val headerTitle = SimpleStringProperty()
    val headerSubtitle = SimpleStringProperty()

    init {
        val scope = scope as ParameterizedScope
        val audioFile = File(scope.parameters.named["wav"])
        val wav = WavFile(audioFile)
        markerCountProperty.value = wav.metadata.getCues().size
        val totalMarkers: Int =
            scope.parameters.named["marker_total"]?.toInt() ?: markerCountProperty.value
        markerCountProperty.onChangeAndDoNow {
            markerRatioProperty.value = "$it/$totalMarkers"
        }
    }

    fun mediaToggle() {
    }

    fun seekNext() {
    }

    fun seekPrevious() {
    }
}

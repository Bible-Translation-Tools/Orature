package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.glass.ui.Screen
import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

interface ViewPortScrollable {
    fun scrollTo(x: Double)
}

class MainWaveform(
    verseMarkerViewModel: VerseMarkerViewModel,
    reader: AudioFileReader
) : ImageView(), ViewPortScrollable {

    val ht = Screen.getMainScreen().platformHeight
    val wd = Screen.getMainScreen().platformWidth

    init {
        styleClass.add("vm-waveform-holder")

        WaveformImageBuilder(
            wavColor = Color.web("#0A337390"),
            background = Color.web("#F7FAFF")
        ).build(
            reader,
            fitToAudioMax = false,
            width = wd,
            height = ht
        ).subscribe { image ->
            imageProperty().set(image)
        }

        verseMarkerViewModel.positionProperty.onChangeAndDoNow {
            val x = it?.toDouble() ?: 0.0
            scrollTo(x - verseMarkerViewModel.padding)
        }
    }

    override fun scrollTo(x: Double) {
        viewport = Rectangle2D(x, 0.0, wd.toDouble(), ht.toDouble())
    }
}

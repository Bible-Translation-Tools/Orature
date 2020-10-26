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
    val viewModel: VerseMarkerViewModel,
    val reader: AudioFileReader
) : ImageView(), ViewPortScrollable {

    init {
        styleClass.add("vm-waveform-holder")

        computeImage()

        viewModel.positionProperty.onChangeAndDoNow {
            val x = it?.toDouble() ?: 0.0
            scrollTo(x - viewModel.padding)
        }
    }

    private fun computeImage() {
        WaveformImageBuilder(
            wavColor = Color.web("#0A337390"),
            background = Color.web("#F7FAFF")
        ).build(
            reader,
            fitToAudioMax = false,
            width = viewModel.imageWidth.toInt(),
            height = viewModel.height
        ).subscribe { image ->
            imageProperty().set(image)
        }
    }

    override fun scrollTo(x: Double) {
        viewport = Rectangle2D(x, 0.0, viewModel.width.toDouble(), viewModel.height.toDouble())
    }
}

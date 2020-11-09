package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.geometry.Rectangle2D
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.jvm.controls.waveform.WaveformImageBuilder
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow

private const val WAV_COLOR = "#0A337390"
private const val BACKGROUND_COLOR = "#F7FAFF"

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
            wavColor = Color.web(WAV_COLOR),
            background = Color.web(BACKGROUND_COLOR)
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

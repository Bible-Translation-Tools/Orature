package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class PlaybackControlsFragment : Fragment() {

    val vm: VerseMarkerViewModel by inject()
    
    private val rootStyles = "vm-play-controls"
    private val playButtonStyle = "vm-play-controls__play-btn"
    private val roundedButtonStyle = "vm-play-controls__btn--rounded"
    private val seekButtonStyle = "vm-play-controls__seek-btn"

    private val playIcon = FontIcon("fa-play")
    private val pauseIcon = FontIcon("fa-pause")
    private val nextIcon = FontIcon("gmi-skip-next")
    private val previousIcon = FontIcon("gmi-skip-previous")

    private val playBtn = button {
        styleClass.addAll(
            playButtonStyle,
            roundedButtonStyle
        )
        graphic = playIcon
    }

    private val nextBtn = button {
        styleClass.addAll(
            seekButtonStyle,
            roundedButtonStyle
        )
        graphic = nextIcon
    }

    private val previousBtn = button {
        styleClass.addAll(
            seekButtonStyle,
            roundedButtonStyle
        )
        graphic = previousIcon
    }

    override val root = hbox {
        alignment = Pos.CENTER
        styleClass.add(rootStyles)
        add(previousBtn.apply { setOnAction { vm.seekPrevious() } })
        add(playBtn.apply { setOnAction { vm.mediaToggle() } })
        add(nextBtn.apply { setOnAction { vm.seekNext() } })
    }
}

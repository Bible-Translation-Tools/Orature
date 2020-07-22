package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.*

class PlaybackControlsFragment : Fragment() {

    val vm: VerseMarkerViewModel by inject()

    private val playIcon = FontIcon("fa-play")
    private val pauseIcon = FontIcon("fa-pause")
    private val nextIcon = FontIcon("gmi-skip-next")
    private val previousIcon = FontIcon("gmi-skip-previous")

    private val playBtn = button {
        styleClass.addAll(
            "vm-play-controls__play-btn",
            "vm-play-controls__btn--rounded"
        )
        graphic = playIcon
    }

    private val nextBtn = button {
        styleClass.addAll(
            "vm-play-controls__seek-btn",
            "vm-play-controls__btn--rounded"
        )
        graphic = nextIcon
    }

    private val previousBtn = button {
        styleClass.addAll(
            "vm-play-controls__seek-btn",
            "vm-play-controls__btn--rounded"
        )
        graphic = previousIcon
    }

    init {
        vm.isPlayingProperty.onChange { playing ->
            if (playing) {
                playBtn.graphicProperty().set(pauseIcon)
            } else {
                playBtn.graphicProperty().set(playIcon)
            }
        }
    }

    override val root = hbox {
        alignment = Pos.CENTER
        styleClass.add("vm-play-controls")
        add(previousBtn.apply { setOnAction { vm.seekPrevious() } })
        add(playBtn.apply { setOnAction { vm.mediaToggle() } })
        add(nextBtn.apply { setOnAction { vm.seekNext() } })
    }
}

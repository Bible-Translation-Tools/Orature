package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class PlaybackControlsFragment : Fragment() {

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

    override val root = hbox {
        alignment = Pos.CENTER
        styleClass.add("vm-play-controls")
        add(previousBtn)
        add(playBtn)
        add(nextBtn)
    }
}

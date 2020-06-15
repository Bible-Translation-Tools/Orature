package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import tornadofx.*

class AudioPlayerSkin(override val playerNode: AudioPlayerNode) : PlayerSkin(playerNode) {

    @FXML
    lateinit var sourceMissing: HBox
    @FXML
    lateinit var audioPlayer: HBox
    @FXML
    lateinit var sourceContainer: HBox
    @FXML
    lateinit var sourceTextContainer: HBox

    init {
        loadFXML()
        initializeControl()
    }

    override fun initializeControl() {
        super.initializeControl()

        sourceMissing.apply {
            hiddenWhen { playerNode.sourceAvailableProperty }
            managedWhen { visibleProperty() }
        }
        sourceContainer.apply {
            visibleWhen { playerNode.sourceAvailableProperty }
            managedWhen { visibleProperty() }
        }
        audioPlayer.apply {
            visibleWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceTextContainer.apply {
            hiddenWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
    }
}
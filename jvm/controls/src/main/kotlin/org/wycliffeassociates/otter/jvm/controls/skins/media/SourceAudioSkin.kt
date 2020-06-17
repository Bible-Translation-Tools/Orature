package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceAudioSkin(override val playerNode: AudioPlayerNode) : PlayerSkin(playerNode) {

    @FXML
    lateinit var root: VBox
    @FXML
    lateinit var sourceAudioLabel: Label

    init {
        loadFXML()
        initializeControl()
    }

    override fun initializeControl() {
        super.initializeControl()

        root.apply {
            playerNode.roundedStyleProperty.onChangeAndDoNow {
                if (it == true) {
                    addClass("scripture-takes-audioplayer--rounded")
                } else {
                    removeClass("scripture-takes-audioplayer--rounded")
                }
            }
        }
        playBtn.apply {
            visibleWhen { audioController.displayPlayerProperty }
        }
        sourceFormatToggle.apply {
            setOnMouseClicked {
                audioController.toggleSource()
                if (playerNode.refreshParentProperty.value) {
                    playerNode.scene.window.sizeToScene()
                }
            }
        }
        audioSlider.apply {
            visibleWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceTextScroll.apply {
            hiddenWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceAudioLabel.apply {
            textProperty().bind(playerNode.sourceAudioLabelProperty)
        }
    }

    override fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceAudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
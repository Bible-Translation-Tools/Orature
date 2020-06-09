package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.sourceaudiotoggle.SourceAudioToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceAudioSkin(private val playerNode: AudioPlayerNode) : SkinBase<AudioPlayerNode>(playerNode) {

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")

    @FXML
    lateinit var root: VBox
    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var audioSlider: Slider
    @FXML
    lateinit var sourceText: Text
    @FXML
    lateinit var sourceAudioLabel: Label
    @FXML
    lateinit var sourceToggle: SourceAudioToggle

    lateinit var audioController: AudioPlayerController

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        audioController = AudioPlayerController(playerNode.audioPlayerProperty.value, audioSlider)
        playBtn.apply {
            setOnMouseClicked {
                audioController.toggle()
            }
            visibleWhen { audioController.displayPlayerProperty }
        }
        sourceToggle.apply {
            setOnMouseClicked {
                audioController.sourceToggle()
                if (playerNode.refreshParentProperty.value) {
                    playerNode.scene.window.sizeToScene()
                }
            }
        }
        audioController.isPlayingProperty.onChangeAndDoNow {
            if (it == true) {
                playBtn.graphicProperty().set(PAUSE_ICON)
            } else {
                playBtn.graphicProperty().set(PLAY_ICON)
            }
        }
        playerNode.audioPlayerProperty.onChange { player ->
            player?.let {
                audioController.load(it)
            }
        }

        audioSlider.apply {
            visibleWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }

        sourceText.apply {
            hiddenWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }

            wrappingWidthProperty().bind(playerNode.sourceTextWidthProperty)
        }
        sourceAudioLabel.apply {
            textProperty().bind(playerNode.sourceAudioLabelProperty)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceAudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
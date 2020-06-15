package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
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
    private val MAX_HEIGHT = 200.0

    @FXML
    lateinit var root: VBox
    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var audioSlider: Slider
    @FXML
    lateinit var sourceTextScroll: ScrollPane
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

        root.apply {
            playerNode.roundedStyleProperty.onChangeAndDoNow {
                if (it == true) {
                    addClass("audioplayer--scripture-takes--rounded")
                } else {
                    removeClass("audioplayer--scripture-takes--rounded")
                }
            }
        }
        playBtn.apply {
            setOnMouseClicked {
                audioController.toggle()
            }
            visibleWhen { audioController.displayPlayerProperty }
        }
        sourceToggle.apply {
            setOnMouseClicked {
                audioController.toggleSource()
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

        sourceTextScroll.apply {
            hiddenWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
            whenVisible { vvalue = 0.0 }

            maxWidthProperty().bind(playerNode.sourceTextWidthProperty)
            maxHeightProperty().set(MAX_HEIGHT)

            sourceText.boundsInParentProperty().onChangeAndDoNow { bounds ->
                bounds?.let {
                    if (bounds.height < MAX_HEIGHT) {
                        sourceTextScroll.minHeightProperty().set(bounds.height)
                    } else {
                        sourceTextScroll.minHeightProperty().set(MAX_HEIGHT)
                    }
                }
            }
        }

        sourceText.apply {
            textProperty().bind(playerNode.sourceTextProperty)
            wrappingWidthProperty().bind(playerNode.sourceTextWidthProperty.minus(20.0))
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
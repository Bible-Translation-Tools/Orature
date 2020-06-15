package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.sourceaudiotoggle.SourceAudioToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class AudioPlayerSkin(private val playerNode: AudioPlayerNode) : SkinBase<AudioPlayerNode>(playerNode) {

    private val PLAY_ICON = FontIcon("fa-play:22")
    private val PAUSE_ICON = FontIcon("fa-pause:22")
    private val MAX_HEIGHT = 200.0

    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var sourceMissing: HBox
    @FXML
    lateinit var audioPlayer: HBox
    @FXML
    lateinit var audioSlider: Slider
    @FXML
    lateinit var sourceContainer: HBox
    @FXML
    lateinit var sourceTextContainer: HBox
    @FXML
    lateinit var sourceTextScroll: ScrollPane
    @FXML
    lateinit var sourceToggle: SourceAudioToggle
    @FXML
    lateinit var sourceText: Text

    lateinit var audioController: AudioPlayerController

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        audioController = AudioPlayerController(playerNode.audioPlayerProperty.value, audioSlider)

        playBtn.setOnMouseClicked {
            audioController.toggle()
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
        sourceMissing.apply {
            hiddenWhen { playerNode.sourceAvailableProperty }
            managedWhen { visibleProperty() }
        }
        sourceContainer.apply {
            visibleWhen { playerNode.sourceAvailableProperty }
            managedWhen { visibleProperty() }
        }
        sourceToggle.apply {
            setOnMouseClicked {
                audioController.toggleSource()
            }
        }
        audioPlayer.apply {
            visibleWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceTextContainer.apply {
            hiddenWhen { audioController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceTextScroll.apply {
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
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("AudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
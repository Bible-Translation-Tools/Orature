package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

abstract class PlayerSkin(protected open val playerNode: AudioPlayerNode) : SkinBase<AudioPlayerNode>(playerNode) {
    protected val PLAY_ICON = FontIcon("fa-play")
    protected val PAUSE_ICON = FontIcon("fa-pause")
    protected val MAX_HEIGHT = 200.0

    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var audioSlider: Slider
    @FXML
    lateinit var sourceTextScroll: ScrollPane
    @FXML
    lateinit var sourceText: Text
    @FXML
    lateinit var sourceFormatToggle: SourceFormatToggle

    lateinit var audioController: AudioPlayerController

    protected open fun initializeControl() {
        audioController = AudioPlayerController(playerNode.audioPlayerProperty.value, audioSlider)

        playBtn.apply {
            setOnMouseClicked {
                audioController.toggle()
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
        sourceFormatToggle.apply {
            setOnMouseClicked {
                audioController.toggleSource()
            }
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

    protected open fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("AudioPlayer.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
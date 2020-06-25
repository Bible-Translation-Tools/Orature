package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.SourceContentController
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

abstract class SourceContentBaseSkin(protected open val sourceContent: SourceContent) :
    SkinBase<SourceContent>(sourceContent) {

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
    lateinit var sourceContentController: SourceContentController

    protected open fun initializeControl() {
        audioController = AudioPlayerController(sourceContent.audioPlayerProperty.value, audioSlider)
        sourceContentController = SourceContentController()

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
        sourceContent.audioPlayerProperty.onChange { player ->
            player?.let {
                audioController.load(it)
            }
        }
        sourceFormatToggle.apply {
            setOnMouseClicked {
                sourceContentController.toggleSource()
            }
        }
        sourceTextScroll.apply {
            whenVisible { vvalue = 0.0 }

            maxWidthProperty().bind(sourceContent.sourceTextWidthProperty)
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
            textProperty().bind(sourceContent.sourceTextProperty)
            wrappingWidthProperty().bind(sourceContent.sourceTextWidthProperty.minus(20.0))
        }
    }

    abstract fun loadFXML()
}
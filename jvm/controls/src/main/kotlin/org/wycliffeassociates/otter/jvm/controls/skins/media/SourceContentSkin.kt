package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.SourceContent
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.SourceContentController
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceContentSkin(private val sourceContent: SourceContent) : SkinBase<SourceContent>(sourceContent) {

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")
    private val AUDIO_PLAYER_ICON = FontIcon("gmi-hearing")
    private val SOURCE_TEXT_ICON = FontIcon("gmi-sort-by-alpha")

    private val MAX_HEIGHT = 200.0

    @FXML
    lateinit var root: VBox

    @FXML
    lateinit var sourceContentLabel: Label

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

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        audioController = AudioPlayerController(sourceContent.audioPlayerProperty.value, audioSlider)
        sourceContentController = SourceContentController()

        if (sourceContent.applyRoundedStyleProperty.value) {
            root.addClass("source-content--rounded")
        }

        playBtn.apply {
            visibleWhen { sourceContentController.displayPlayerProperty }
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
        audioSlider.apply {
            visibleWhen { sourceContentController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceContent.audioPlayerProperty.onChange { player ->
            player?.let {
                audioController.load(it)
            }
        }
        sourceTextScroll.apply {
            hiddenWhen { sourceContentController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceFormatToggle.apply {
            setOnMouseClicked {
                sourceContentController.toggleSource()
                sourceContent.sourceFormatChangedProperty.set(
                    sourceContent.sourceFormatChangedProperty.value.not()
                )
            }
        }
        sourceContentLabel.apply {
            textProperty().bind(
                sourceContentController.sourceContentLabelBinding(
                    sourceContent.sourceAudioLabelProperty,
                    sourceContent.sourceTextLabelProperty
                )
            )
            graphicProperty().bind(
                sourceContentController.sourceContentIconBinding(
                    AUDIO_PLAYER_ICON,
                    SOURCE_TEXT_ICON
                )
            )
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

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
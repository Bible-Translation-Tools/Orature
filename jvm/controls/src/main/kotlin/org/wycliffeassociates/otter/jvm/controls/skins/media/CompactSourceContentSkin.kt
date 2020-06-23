package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.SourceContent
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.controllers.SourceContentController
import org.wycliffeassociates.otter.jvm.controls.sourceformattoggle.SourceFormatToggle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class CompactSourceContentSkin(private val sourceContent: SourceContent) : SkinBase<SourceContent>(sourceContent) {

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")

    private val MAX_HEIGHT = 200.0

    @FXML
    lateinit var sourceMissing: HBox

    @FXML
    lateinit var sourceContentContainer: HBox

    @FXML
    lateinit var playBtn: Button

    @FXML
    lateinit var audioPlayerContainer: HBox

    @FXML
    lateinit var audioSlider: Slider

    @FXML
    lateinit var sourceTextContainer: HBox

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

        sourceMissing.apply {
            hiddenWhen { sourceContent.sourceAudioAvailableProperty }
            managedWhen { visibleProperty() }
        }
        sourceContentContainer.apply {
            visibleWhen { sourceContent.sourceAudioAvailableProperty }
            managedWhen { visibleProperty() }
        }
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
        audioPlayerContainer.apply {
            visibleWhen { sourceContentController.displayPlayerProperty }
            managedWhen { visibleProperty() }
        }
        sourceTextContainer.apply {
            hiddenWhen { sourceContentController.displayPlayerProperty }
            managedWhen { visibleProperty() }
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

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("CompactSourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
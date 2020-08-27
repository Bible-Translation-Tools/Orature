package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.controls.waveform.AudioSlider
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class SourceContentSkin(private val sourceContent: SourceContent) : SkinBase<SourceContent>(sourceContent) {

    companion object {
        private const val SCROLL_TEXT_MAX_HEIGHT = 150.0
        private const val SCROLL_TEXT_MARGIN = 20.0
        private const val SCROLL_TEXT_RESIZE_RATIO = 1.05
    }

    private val playIcon = FontIcon("fa-play")
    private val pauseIcon = FontIcon("fa-pause")

    @FXML
    lateinit var sourceAudioContainer: HBox

    @FXML
    lateinit var playBtn: Button

    @FXML
    lateinit var audioSlider: AudioSlider

    @FXML
    lateinit var sourceAudioNotAvailable: HBox

    @FXML
    lateinit var audioNotAvailableText: Label

    @FXML
    lateinit var sourceTextContainer: VBox

    @FXML
    lateinit var sourceTextNotAvailable: HBox

    @FXML
    lateinit var textNotAvailableText: Label

    @FXML
    lateinit var sourceTextScroll: ScrollPane

    @FXML
    lateinit var sourceText: Text

    @FXML
    lateinit var title: Label

    lateinit var audioController: AudioPlayerController

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        initControllers()
        initAudioControls()
        initTextControls()
    }

    private fun initControllers() {
        audioController = AudioPlayerController(sourceContent.audioPlayerProperty.value, audioSlider)
    }

    private fun initAudioControls() {
        audioSlider.apply {
            player.bind(sourceContent.audioPlayerProperty)
        }

        sourceAudioContainer.apply {
            visibleWhen(sourceContent.sourceAudioAvailableProperty)
            managedWhen(visibleProperty())
        }

        sourceAudioNotAvailable.apply {
            hiddenWhen(sourceContent.sourceAudioAvailableProperty)
            managedWhen(visibleProperty())
        }

        audioNotAvailableText.apply {
            textProperty().bind(sourceContent.audioNotAvailableTextProperty)
        }

        playBtn.apply {
            setOnMouseClicked {
                audioController.toggle()
            }
        }

        audioController.isPlayingProperty.onChangeAndDoNow {
            togglePlayButtonIcon(it)
            togglePlayButtonStyle(it)
            togglePlayButtonText(it)
        }

        sourceContent.audioPlayerProperty.onChangeAndDoNow { player ->
            player?.let {
                audioController.load(it)
            }
        }
    }

    private fun initTextControls() {
        sourceTextNotAvailable.apply {
            hiddenWhen(sourceContent.sourceTextAvailableProperty)
            managedWhen(visibleProperty())
        }

        textNotAvailableText.apply {
            textProperty().bind(sourceContent.textNotAvailableTextProperty)
        }

        sourceTextContainer.apply {
            visibleWhen(sourceContent.sourceTextAvailableProperty)
            managedWhen(visibleProperty())
        }

        sourceTextScroll.apply {
            whenVisible { vvalue = 0.0 }

            maxWidthProperty().bind(
                sourceContent.widthProperty().divide(SCROLL_TEXT_RESIZE_RATIO)
            )
            maxHeightProperty().set(SCROLL_TEXT_MAX_HEIGHT)

            sourceText.boundsInParentProperty().onChangeAndDoNow { bounds ->
                bounds?.let {
                    if (bounds.height < SCROLL_TEXT_MAX_HEIGHT) {
                        sourceTextScroll.minHeightProperty().set(bounds.height)
                    } else {
                        sourceTextScroll.minHeightProperty().set(SCROLL_TEXT_MAX_HEIGHT)
                    }
                }
            }
        }

        sourceText.apply {
            textProperty().bind(sourceContent.sourceTextProperty)
            wrappingWidthProperty().bind(
                sourceTextScroll.maxWidthProperty().minus(SCROLL_TEXT_MARGIN)
            )
        }

        title.apply {
            textProperty().bind(sourceContent.contentTitleProperty)
        }
    }

    private fun togglePlayButtonIcon(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.graphicProperty().set(pauseIcon)
        } else {
            playBtn.graphicProperty().set(playIcon)
        }
    }

    private fun togglePlayButtonStyle(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.removeClass("btn--primary")
            playBtn.addClass("btn--secondary")
        } else {
            playBtn.removeClass("btn--secondary")
            playBtn.addClass("btn--primary")
        }
    }

    private fun togglePlayButtonText(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.text = sourceContent.pauseLabelProperty.value
        } else {
            playBtn.text = sourceContent.playLabelProperty.value
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SourceContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

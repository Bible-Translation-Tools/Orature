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
import org.wycliffeassociates.otter.jvm.controls.controllers.SourceContentController
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*


class SourceContentSkin(private val sourceContent: SourceContent) : SkinBase<SourceContent>(sourceContent) {

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")
    private val MAX_TEXT_HEIGHT = 150.0

    @FXML
    lateinit var sourceAudioContainer: HBox

    @FXML
    lateinit var playBtn: Button

    @FXML
    lateinit var audioSlider: Slider

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
    lateinit var sourceContentController: SourceContentController

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        audioController = AudioPlayerController(sourceContent.audioPlayerProperty.value, audioSlider)
        sourceContentController = SourceContentController()

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

            maxWidthProperty().bind(sourceContent.widthProperty().divide(1.05))
            maxHeightProperty().set(MAX_TEXT_HEIGHT)

            sourceText.boundsInParentProperty().onChangeAndDoNow { bounds ->
                bounds?.let {
                    if (bounds.height < MAX_TEXT_HEIGHT) {
                        sourceTextScroll.minHeightProperty().set(bounds.height)
                    } else {
                        sourceTextScroll.minHeightProperty().set(MAX_TEXT_HEIGHT)
                    }
                }
            }
        }

        sourceText.apply {
            textProperty().bind(sourceContent.sourceTextProperty)
            wrappingWidthProperty().bind(sourceTextScroll.maxWidthProperty().minus(20.0))
        }

        title.apply {
            textProperty().bind(
                sourceContentController.titleBinding(
                    sourceContent.bookTitleProperty,
                    sourceContent.chapterTitleProperty,
                    sourceContent.chunkTitleProperty
                )
            )
        }
    }

    private fun togglePlayButtonIcon(isPlaying: Boolean?) {
        if (isPlaying == true) {
            playBtn.graphicProperty().set(PAUSE_ICON)
        } else {
            playBtn.graphicProperty().set(PLAY_ICON)
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

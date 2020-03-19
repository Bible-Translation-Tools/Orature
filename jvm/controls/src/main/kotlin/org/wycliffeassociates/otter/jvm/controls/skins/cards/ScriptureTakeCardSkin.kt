package org.wycliffeassociates.otter.jvm.controls.skins.cards

import com.sun.javafx.event.EventUtil.fireEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.EditTakeEvent
import org.wycliffeassociates.otter.jvm.controls.controllers.AudioPlayerController
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.AnimateDragEvent
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.CompleteDragEvent
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.StartDragEvent
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ScriptureTakeCardSkin(val card: ScriptureTakeCard) : SkinBase<ScriptureTakeCard>(card) {

    @FXML
    lateinit var playBtn: Button
    @FXML
    lateinit var editBtn: Button
    @FXML
    lateinit var deleteBtn: Button
    @FXML
    lateinit var slider: Slider
    @FXML
    lateinit var takeLabel: Label
    @FXML
    lateinit var timestampLabel: Label

    lateinit var audioPlayerController: AudioPlayerController

    private val PLAY_ICON = FontIcon("fa-play")
    private val PAUSE_ICON = FontIcon("fa-pause")

    init {
        loadFXML()
        initializeControl()
    }

    fun initializeControl() {
        bindText()
        initController()
    }

    fun bindText() {
        deleteBtn.textProperty().bind(card.deleteTextProperty())
        editBtn.textProperty().bind(card.editTextProperty())
        playBtn.textProperty().set(card.playTextProperty().value)
        takeLabel.textProperty().bind(card.takeNumberProperty())
        timestampLabel.textProperty().bind(card.timestampProperty())
    }

    fun initController() {
        audioPlayerController = AudioPlayerController(card.audioPlayerProperty().value, slider)
        audioPlayerController.isPlayingProperty.onChangeAndDoNow { isPlaying ->
            if (isPlaying != null && isPlaying != true) {
                playBtn.textProperty().set(card.playTextProperty().value)
                playBtn.graphicProperty().set(PLAY_ICON)
            } else {
                playBtn.textProperty().set(card.pauseTextProperty().value)
                playBtn.graphicProperty().set(PAUSE_ICON)
            }
        }
        playBtn.setOnAction {
            audioPlayerController.toggle()
        }
        deleteBtn.setOnAction {
            error(
                FX.messages["deleteTakePrompt"],
                FX.messages["cannotBeUndone"],
                ButtonType.YES,
                ButtonType.NO,
                title = FX.messages["deleteTakePrompt"]
            ) { button: ButtonType ->
                if (button == ButtonType.YES) {
                    skinnable.fireEvent(
                        DeleteTakeEvent(card.takeProperty().value)
                    )
                }
            }
        }
        editBtn.setOnAction {
            skinnable.fireEvent(
                EditTakeEvent(card.takeProperty().value) {
                    card.audioPlayerProperty().value.load(card.takeProperty().value.file)
                }
            )
        }
        card.audioPlayerProperty().onChange { player ->
            player?.let {
                audioPlayerController.load(it)
            }
        }
    }

    private fun startDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            StartDragEvent(
                evt,
                card,
                skinnable.takeProperty().value
            )
        )
    }

    private fun animateDrag(evt: MouseEvent) {
        skinnable.fireEvent(AnimateDragEvent(evt))
    }

    @Suppress("UNUSED_PARAMETER")
    private fun completeDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            CompleteDragEvent(
                skinnable.takeProperty().value,
                ::onCancelDrag
            )
        )
    }

    private fun onCancelDrag() {}

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ScriptureTakeCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)

        importStylesheet(javaClass.getResource("/css/root.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/scripturetakecard.css").toExternalForm())
    }
}
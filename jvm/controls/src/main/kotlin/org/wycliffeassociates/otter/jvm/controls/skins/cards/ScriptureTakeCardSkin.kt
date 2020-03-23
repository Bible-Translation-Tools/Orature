package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.card.EmptyCardCell
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

    private val dragDropContainer = StackPane()
    private lateinit var cardNode: Node
    protected val back = EmptyCardCell()

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
        back.widthProperty().bind(skinnable.widthProperty())
        back.heightProperty().bind(skinnable.heightProperty())
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
        cardNode.apply {
            addEventHandler(MouseEvent.MOUSE_PRESSED, ::startDrag)
            addEventHandler(MouseEvent.MOUSE_DRAGGED, ::animateDrag)
            addEventHandler(MouseEvent.MOUSE_RELEASED, ::completeDrag)
        }
    }

    private fun startDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            StartDragEvent(
                evt,
                cardNode,
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

    private fun onCancelDrag() {
        dragDropContainer.add(cardNode)
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ScriptureTakeCard.fxml"))
        loader.setController(this)
        cardNode = loader.load()

        dragDropContainer.add(back)
        dragDropContainer.add(cardNode)
        children.addAll(dragDropContainer)

        importStylesheet(javaClass.getResource("/css/root.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/scripturetakecard.css").toExternalForm())
    }
}
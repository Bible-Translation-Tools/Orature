package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.control.ButtonType
import javafx.scene.control.SkinBase
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.AnimateDragEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.CompleteDragEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.PlayOrPauseEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.StartDragEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

abstract class TakeCardSkin(control: TakeCard) : SkinBase<TakeCard>(control) {

    private val defaultPlayPauseIconSize = 30
    // These can be overridden
    open val playIconView = MaterialIconView(MaterialIcon.PLAY_ARROW, "${defaultPlayPauseIconSize}px")
    open val pauseIconView = MaterialIconView(MaterialIcon.PAUSE, "${defaultPlayPauseIconSize}px")

    val playButton = createPlayButton()
    val editButton = createEditButton()
    val deleteButton = createDeleteButton()

    private val stackPane = StackPane()
    protected val back = VBox()
    protected val front = VBox()

    init {
        stackPane.add(back)
        stackPane.add(front)

        children.addAll(stackPane)

        skinnable.isAudioPlayingProperty.onChange {
            playButton.graphic = when (it) {
                true -> pauseIconView
                false -> playIconView
            }
        }

        front.apply {
            addEventHandler(MouseEvent.MOUSE_PRESSED, ::startDrag)
            addEventHandler(MouseEvent.MOUSE_DRAGGED, ::animateDrag)
            addEventHandler(MouseEvent.MOUSE_RELEASED, ::completeDrag)
        }

        consumeMouseEvents(false)
    }

    private fun startDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            StartDragEvent(
                StartDragEvent.START_DRAG,
                evt,
                front,
                skinnable.take
            )
        )
    }

    private fun animateDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            AnimateDragEvent(
                AnimateDragEvent.ANIMATE_DRAG,
                evt
            )
        )
    }

    private fun completeDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            CompleteDragEvent(
                CompleteDragEvent.COMPLETE_DRAG,
                evt,
                skinnable.take,
                ::onCancelDrag
            )
        )
    }

    private fun onCancelDrag() {
        stackPane.add(front)
    }

    private fun createDeleteButton() = JFXButton().apply {
        action {
            error(
                messages["deleteTakePrompt"],
                messages["cannotBeUndone"],
                ButtonType.YES,
                ButtonType.NO,
                title = messages["deleteTakePrompt"]
            ) { button: ButtonType ->
                if (button == ButtonType.YES) {
                    skinnable.fireDeleteTakeEvent()
                }
            }
        }
    }

    private fun createEditButton() = JFXButton().apply {
        action {
            skinnable.fireEditTakeEvent()
        }
    }

    private fun createPlayButton() = JFXButton().apply {
        // If playIconView is overridden in child class, the graphic needs to be set in the child class
        graphic = playIconView
        action {
            skinnable.fireEvent(
                PlayOrPauseEvent(
                    when (skinnable.isAudioPlayingProperty.get()) {
                        true -> PlayOrPauseEvent.PAUSE
                        false -> PlayOrPauseEvent.PLAY
                    }
                )
            )
        }
    }
}
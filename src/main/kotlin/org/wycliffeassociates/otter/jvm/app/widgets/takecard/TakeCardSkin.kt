package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.control.ButtonType
import javafx.scene.control.SkinBase
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

    private fun createDeleteButton() = JFXButton().apply {
        text = messages["delete"]
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
        text = messages["edit"]
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

    init {
        skinnable.isAudioPlayingProperty.onChange {
            playButton.graphic = when (it) {
                true -> pauseIconView
                false -> playIconView
            }
        }
    }
}
/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.controls.takecard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.control.ButtonType
import javafx.scene.control.SkinBase
import javafx.scene.input.MouseEvent
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.AnimateDragEvent
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.CompleteDragEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.PlayOrPauseEvent.PauseEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.PlayOrPauseEvent.PlayEvent
import org.wycliffeassociates.otter.jvm.controls.dragtarget.events.StartDragEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

abstract class TakeCardSkin(val control: TakeCard) : SkinBase<TakeCard>(control) {

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
    }

    private fun startDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            StartDragEvent(
                evt,
                front,
                skinnable.model.take
            )
        )
    }

    private fun animateDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            AnimateDragEvent(
                evt
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun completeDrag(evt: MouseEvent) {
        skinnable.fireEvent(
            CompleteDragEvent(
                skinnable.model.take,
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
            control.simpleAudioPlayer.close()
            skinnable.fireEditTakeEvent()
        }
    }

    private fun createPlayButton() = JFXButton().apply {
        // If playIconView is overridden in child class, the graphic needs to be set in the child class
        graphic = playIconView
        action {
            skinnable.fireEvent(
                when (skinnable.isAudioPlayingProperty.get()) {
                    true -> PauseEvent()
                    false -> PlayEvent()
                }
            )
        }
    }
}

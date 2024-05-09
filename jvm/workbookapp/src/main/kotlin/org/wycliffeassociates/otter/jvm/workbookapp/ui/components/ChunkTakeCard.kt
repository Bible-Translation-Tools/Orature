/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.animation.FadeTransition
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.TakeSelectionAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.controls.event.ChunkTakeEvent
import org.wycliffeassociates.otter.jvm.controls.event.TakeAction
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeCardModel
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class ChunkTakeCard(take: TakeCardModel) : HBox() {
    val animationMediatorProperty = SimpleObjectProperty<TakeSelectionAnimationMediator<ChunkTakeCard>>()

    init {
        addClass("take-card")
        simpleaudioplayer(take.audioPlayer) {
            hgrow = Priority.ALWAYS
            titleTextProperty.set(
                MessageFormat.format(
                    messages["takeTitle"],
                    messages["take"],
                    take.take.number
                )
            )
            enablePlaybackRateProperty.set(false)
            sideTextProperty.bind(remainingTimeProperty)
        }
        button {
            addClass("btn", "btn--icon--primary")
            tooltip(messages["delete"])
            graphic = FontIcon(MaterialDesign.MDI_DELETE)

            action {
                val fadeTransition = FadeTransition(Duration.millis(600.0), this@ChunkTakeCard).apply {
                    fromValue = 1.0
                    toValue = 0.0
                }
                fadeTransition.setOnFinished {
                    FX.eventbus.fire(ChunkTakeEvent(take.take, TakeAction.DELETE))
                }
                fadeTransition.play()
            }
        }
        button {
            addClass("btn", "btn--icon--primary")
            tooltip(messages["select"])
            togglePseudoClass("active", take.selected)

            graphic = if (take.selected) {
                FontIcon(MaterialDesign.MDI_STAR)
            } else {
                FontIcon(MaterialDesign.MDI_STAR_OUTLINE)
            }
            isMouseTransparent = take.selected
            isFocusTraversable = !take.selected

            action {
                animationMediatorProperty.value?.let { animator ->
                    if (animator.isAnimating || take.selected) {
                        return@action
                    }
                    animator.node = this@ChunkTakeCard
                    animator.animate {
                        FX.eventbus.fire(ChunkTakeEvent(take.take, TakeAction.SELECT))
                    }
                } ?: FX.eventbus.fire(ChunkTakeEvent(take.take, TakeAction.SELECT))
            }
        }
    }
}
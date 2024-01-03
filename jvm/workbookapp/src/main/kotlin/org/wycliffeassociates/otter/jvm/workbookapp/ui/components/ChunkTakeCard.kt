package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.animation.FadeTransition
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.TakeSelectionAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.event.ChunkTakeEvent
import org.wycliffeassociates.otter.jvm.controls.event.TakeAction
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
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
                    take.take.number,
                ),
            )
            enablePlaybackRateProperty.set(false)
            sideTextProperty.bind(remainingTimeProperty)
        }
        button {
            addClass("btn", "btn--icon")
            tooltip(messages["delete"])
            graphic = FontIcon(MaterialDesign.MDI_DELETE)

            action {
                val fadeTransition =
                    FadeTransition(Duration.millis(600.0), this@ChunkTakeCard).apply {
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
            addClass("btn", "btn--icon")
            tooltip(messages["select"])
            togglePseudoClass("active", take.selected)

            graphic =
                if (take.selected) {
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

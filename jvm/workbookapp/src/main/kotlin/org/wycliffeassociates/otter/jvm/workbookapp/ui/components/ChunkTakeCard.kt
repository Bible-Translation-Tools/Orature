package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.TakeSelectionAnimationMediator
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.TakeOptionMenu
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
            addClass("btn", "btn--icon", "btn--borderless")
            tooltip(messages["options"])
            graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)

            val menu = TakeOptionMenu(take.take).apply {
                setOnShowing {
                    this@button.addPseudoClass("active")
                }
                setOnHidden {
                    this@button.removePseudoClass("active")
                }
            }
            setOnAction {
                val bound = this.boundsInLocal
                val screenBound = this.localToScreen(bound)
                menu.show(
                    FX.primaryStage
                )
                menu.x = screenBound.minX - menu.width + this.width
                menu.y = screenBound.maxY
            }
        }
        button {
            addClass("btn", "btn--icon")
            tooltip(messages["select"])
            togglePseudoClass("active", take.selected)

            graphic = FontIcon(MaterialDesign.MDI_STAR_OUTLINE)
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
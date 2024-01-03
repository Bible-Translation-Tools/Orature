package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.event.EventTarget
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import tornadofx.*
import tornadofx.FX.Companion.messages

class NarrationMenu : ContextMenu() {
    val hasChapterTakeProperty = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()

    init {
        addClass("wa-context-menu")
        isAutoHide = true

        val openChapterOpt =
            MenuItem().apply {
                graphic =
                    label(messages["openChapterIn"]) {
                        graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
                        tooltip(text)
                    }
                action {
                    FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.EDITOR))
                }
                enableWhen(hasChapterTakeProperty)
            }
        val verseMarkerOpt =
            MenuItem().apply {
                graphic =
                    label(messages["editVerseMarkers"]) {
                        graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                        tooltip(text)
                    }
                action {
                    FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.MARKER))
                }
                enableWhen(hasChapterTakeProperty)
            }
        val restartChapterOpt =
            MenuItem().apply {
                graphic =
                    label(messages["restartChapter"]) {
                        graphic = FontIcon(MaterialDesign.MDI_DELETE)
                        tooltip(text)
                    }
                action {
                    FX.eventbus.fire(NarrationRestartChapterEvent())
                }
                enableWhen(hasVersesProperty)
            }

        items.setAll(openChapterOpt, verseMarkerOpt, restartChapterOpt)
    }
}

fun EventTarget.narrationMenuButton(
    hasChapterTakeBinding: ObservableBooleanValue,
    hasVersesBinding: ObservableBooleanValue,
    op: Button.() -> Unit = {},
): Button {
    return Button().attachTo(this).apply {
        addClass("btn", "btn--icon")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
        tooltip(messages["options"])

        val menu =
            NarrationMenu().apply {
                this.hasChapterTakeProperty.bind(hasChapterTakeBinding)
                this.hasVersesProperty.bind(hasVersesBinding)
            }

        menu.setOnShowing { addPseudoClass("active") }
        menu.setOnHidden { removePseudoClass("active") }

        action {
            val screenBound = localToScreen(boundsInLocal)
            menu.show(FX.primaryStage)
            menu.x = screenBound.centerX - menu.width + this.width
            menu.y = screenBound.minY + this.height - 5.0
        }
        op()
    }
}

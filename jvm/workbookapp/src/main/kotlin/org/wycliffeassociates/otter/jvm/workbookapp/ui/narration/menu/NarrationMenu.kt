package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.menu

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.control.MenuButton
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import tornadofx.*
import tornadofx.FX.Companion.messages

class NarrationMenu : MenuButton() {
    val undoActionTextProperty: ObservableValue<String> = SimpleStringProperty(messages["undoAction"])
    val redoActionTextProperty = SimpleStringProperty(messages["redoAction"])
    val openChapterInTextProperty = SimpleStringProperty(messages["openChapterIn"])
    val editVerseMarkersTextProperty = SimpleStringProperty(messages["editVerseMarkers"])
    val resetChapterTextProperty = SimpleStringProperty(messages["restartChapter"])

    val hasUndoProperty = SimpleBooleanProperty()
    val hasRedoProperty = SimpleBooleanProperty()
    val hasChapterTakeProperty = SimpleBooleanProperty()
    val hasVersesProperty = SimpleBooleanProperty()

    init {
        addClass("btn", "btn--secondary", "wa-menu-button")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)
        addClass("wa-context-menu")

        item(undoActionTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_UNDO)
            action {
                FX.eventbus.fire(NarrationUndoEvent())
            }
            enableWhen(hasUndoProperty)
        }
        item(redoActionTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_REDO)
            action {
                FX.eventbus.fire(NarrationRedoEvent())
            }
            enableWhen(hasRedoProperty)
        }
        item(openChapterInTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            action {
                FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.EDITOR))
            }
            enableWhen(hasChapterTakeProperty)
        }
        item(editVerseMarkersTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
            action {
                FX.eventbus.fire(NarrationOpenInPluginEvent(PluginType.MARKER))
            }
            enableWhen(hasChapterTakeProperty)
        }
        item(resetChapterTextProperty.value) {
            graphic = FontIcon(MaterialDesign.MDI_DELETE)
            action {
                FX.eventbus.fire(NarrationResetChapterEvent())
            }
            enableWhen(hasVersesProperty)
        }
    }
}

fun EventTarget.narrationMenu(op: NarrationMenu.() -> Unit = {}) = NarrationMenu().attachTo(this, op)
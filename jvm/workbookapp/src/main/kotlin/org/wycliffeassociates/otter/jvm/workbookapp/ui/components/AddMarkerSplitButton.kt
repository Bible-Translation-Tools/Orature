package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.event.EventTarget
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.button.debouncedButton
import org.wycliffeassociates.otter.jvm.controls.event.AddMarkerEvent
import org.wycliffeassociates.otter.jvm.controls.event.MarkerType
import tornadofx.*
import tornadofx.FX.Companion.messages

class AddMarkerSplitButton : HBox() {

    private val menu = AddMarkerMenu().also { menu ->
        menu.setOnShowing { addPseudoClass("active") }
        menu.setOnHidden { removePseudoClass("active") }
    }

    init {
        addClass("chapter-selector", "chapter-selector__controls")
        debouncedButton(messages["addVerseMarker"]) {
            addClass("btn", "btn--primary", "chapter-selector__btn-prev")
            graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_PLUS)
            fitToParentHeight()

            action {
                FX.eventbus.fire(AddMarkerEvent(MarkerType.VERSE))
            }
        }
        button {
            addClass("btn", "btn--primary", "chapter-selector__btn-next")
            graphic = FontIcon(MaterialDesign.MDI_CHEVRON_DOWN)

            action {
                val screenBound = localToScreen(boundsInLocal)
                menu.show(FX.primaryStage)
                menu.x = screenBound.centerX - menu.width + this.width
                menu.y = screenBound.centerY - menu.height
            }
        }
    }
}

class AddMarkerMenu : ContextMenu() {
    init {
        addClass("wa-context-menu")

        val addBookOption = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")

            graphic = label(messages["addBookMarker"]) {
                graphic = FontIcon(MaterialDesign.MDI_BOOK)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(AddMarkerEvent(MarkerType.BOOK))
            }
        }

        val addChapterOption = MenuItem().apply {
            addClass("btn", "btn--tertiary", "btn--borderless")

            graphic = label(messages["addChapterMarker"]) {
                graphic = FontIcon(MaterialDesign.MDI_FILE_DOCUMENT)
                tooltip(text)
            }
            action {
                FX.eventbus.fire(AddMarkerEvent(MarkerType.CHAPTER))
            }

        }

        items.setAll(addBookOption, addChapterOption)
    }

}

fun EventTarget.addMarkerSplitButton(op: AddMarkerSplitButton.() -> Unit = {}) = AddMarkerSplitButton().attachTo(this, op)
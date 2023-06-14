package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.controls.event.LanguageSelectedEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class LanguageTableView(
    languages: ObservableList<Language>
) : TableView<Language>(languages) {

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        column(messages["language"], String::class) {
            setCellValueFactory { it.value.name.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("h4", "h4--80")
                    tooltip(text)
                }
            }
            isReorderable = false
        }
        column(messages["anglicized"], String::class) {
            setCellValueFactory { it.value.anglicizedName.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("normal-text")
                    tooltip(text)
                }
            }
            isReorderable = false
        }
        column(messages["code"], String::class) {
            setCellValueFactory { it.value.slug.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("normal-text")
                }
            }
            isReorderable = false
        }
        column(messages["gateway"], Boolean::class) {
            setCellValueFactory { it.value.isGateway.toProperty() }
            cellFormat {
                graphic = label {
                    text = if (item) messages["yes"] else messages["no"]
                    addClass("normal-text")
                }
            }
            isReorderable = false
        }

        setRowFactory { LanguageTableRow() }

        focusedProperty().onChange {
            if (it && selectionModel.selectedIndex < 0) {
                selectionModel.select(0)
                focusModel.focus(0)
            }
        }

        addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
            if (keyEvent.code == KeyCode.SPACE || keyEvent.code == KeyCode.ENTER) {
                selectedItem?.let { language ->
                    FX.eventbus.fire(LanguageSelectedEvent(language))
                }
                keyEvent.consume()
            }
        }
    }
}

/**
 * Constructs a language table and attach it to the parent.
 */
fun EventTarget.languageTableView(
    values: ObservableList<Language>,
    op: LanguageTableView.() -> Unit = {}
) = LanguageTableView(values).attachTo(this, op)

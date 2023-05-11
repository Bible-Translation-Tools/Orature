package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.primitives.Language
import tornadofx.*
import tornadofx.FX.Companion.messages

class LanguageTableView(
    languages: ObservableList<Language>
) : TableView<Language>(languages) {

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        column(messages["languageName"], String::class) {
            setCellValueFactory { it.value.name.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("h4", "h4--80")
                    tooltip(text)
                }
            }
        }
        column(messages["anglicizedTitle"], String::class) {
            setCellValueFactory { it.value.anglicizedName.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("normal-text")
                    tooltip(text)
                }
            }
        }
        column(messages["code"], String::class) {
            setCellValueFactory { it.value.slug.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("normal-text")
                }
            }
        }
        column(messages["gateway"], Boolean::class) {
            setCellValueFactory { it.value.isGateway.toProperty() }
            cellFormat {
                graphic = label {
                    text = if (item) messages["Yes"] else messages["No"]
                    addClass("normal-text")
                }
            }
        }

        setRowFactory { LanguageTableRow() }
    }
}

/**
 * Constructs a language table and attach it to the parent.
 */
fun EventTarget.languageTableView(
    values: ObservableList<Language>,
    op: LanguageTableView.() -> Unit = {}
) = LanguageTableView(values).attachTo(this, op)

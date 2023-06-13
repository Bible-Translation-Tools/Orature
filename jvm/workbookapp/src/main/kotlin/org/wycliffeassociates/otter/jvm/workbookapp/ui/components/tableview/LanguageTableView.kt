package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.collections.ObservableList
import javafx.collections.transformation.SortedList
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.util.Callback
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
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
            isSortable = true

            bindColumnSortComparator()
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
            isSortable = true

            bindColumnSortComparator()
        }
        column(messages["code"], String::class) {
            setCellValueFactory { it.value.slug.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("normal-text")
                }
            }
            isReorderable = false
            isSortable = true

            bindColumnSortComparator()
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
            isSortable = true

            bindColumnSortComparator()
        }

        sortPolicy = CUSTOM_SORT_POLICY as (Callback<TableView<Language>, Boolean>)
        setRowFactory { LanguageTableRow() }

        bindTableSortComparator()
    }
}

/**
 * Constructs a language table and attach it to the parent.
 */
fun EventTarget.languageTableView(
    values: ObservableList<Language>,
    op: LanguageTableView.() -> Unit = {}
) = LanguageTableView(values).attachTo(this, op)

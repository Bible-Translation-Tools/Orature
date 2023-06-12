package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.utils.overrideDefaultKeyEventHandler
import org.wycliffeassociates.otter.jvm.utils.virtualFlow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.ProjectGroupOptionMenu
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.workbookTableView
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class BookSection(books: ObservableList<WorkbookDescriptor>) : StackPane() {
    private val projectsOptionMenu = ProjectGroupOptionMenu()
    private val titleProperty = SimpleStringProperty().apply {
        bind(books.stringBinding {
            if (it.isNotEmpty()) {
                val book = it.first()
                MessageFormat.format(
                    messages["projectGroupTitle"],
                    book.targetLanguage.name,
                    messages[book.mode.titleKey]
                )
            } else {
                ""
            }
        })
    }

    init {
        vbox {
            addClass("homepage__main-region")

            hbox {
                addClass("homepage__main-region__header-section")
                button {
                    addClass("btn", "btn--icon", "btn--borderless", "option-button")
                    graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL)

                    projectsOptionMenu.books.setAll(books)
                    setOnAction {
                        val bound = this.boundsInLocal
                        val screenBound = this.localToScreen(bound)
                        projectsOptionMenu.show(
                            FX.primaryStage
                        )
                        projectsOptionMenu.x = screenBound.minX
                        projectsOptionMenu.y = screenBound.maxY
                    }
                }
                label(titleProperty) { addClass("h4") }
            }

            workbookTableView(books) {
                hgrow = Priority.ALWAYS

                overrideDefaultKeyEventHandler {
                    val currentItem = selectionModel.selectedItem
                    var index = books.indexOf(currentItem)
                    when (it) {
                        KeyCode.UP -> index--
                        KeyCode.DOWN, KeyCode.TAB -> index++
                    }
                    val item = books.getOrElse(index) { currentItem }
                    selectionModel.select(item)
                    this.scrollTo(index)
                }

                focusedProperty().onChange { focused ->
                    if (focused) {
                        val item = books.firstOrNull()
                        val index = items.indexOf(item)
                        selectionModel.select(index)
                    }
                }
            }

            visibleWhen(books.booleanBinding { it.isNotEmpty() })
            managedWhen(visibleProperty())
        }

        emptyProjectSection {
            visibleWhen { books.booleanBinding { it.isEmpty() } }
            managedWhen(visibleProperty())
        }
    }
}
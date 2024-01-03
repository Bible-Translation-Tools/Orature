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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.animation.FadeTransition
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.util.Duration
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.jvm.controls.bar.searchBar
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.ProjectGroupOptionMenu
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.WorkBookTableView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview.workbookTableView
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class BookSection(
    books: ObservableList<WorkbookDescriptor>,
    filteredBooks: ObservableList<WorkbookDescriptor>
) : StackPane() {
    val bookSearchQueryProperty = SimpleStringProperty()
    private lateinit var bookTable: WorkBookTableView
    private val projectsOptionMenu = ProjectGroupOptionMenu()
    private val titleProperty = SimpleStringProperty().apply {
        bind(filteredBooks.stringBinding {
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
                    tooltip(messages["options"])
                    graphic = FontIcon(MaterialDesign.MDI_DOTS_VERTICAL)

                    projectsOptionMenu.books.setAll(filteredBooks)
                    projectsOptionMenu.setOnShowing {
                        addPseudoClass("active")
                    }
                    projectsOptionMenu.setOnHidden {
                        removePseudoClass("active")
                    }

                    setOnAction {
                        val bound = this.boundsInLocal
                        val screenBound = this.localToScreen(bound)
                        projectsOptionMenu.books.setAll(books)
                        projectsOptionMenu.show(
                            FX.primaryStage
                        )
                        projectsOptionMenu.x = screenBound.minX
                        projectsOptionMenu.y = screenBound.centerY
                    }
                }
                label(titleProperty) { addClass("h4") }
                region { hgrow = Priority.ALWAYS }
                searchBar {
                    textProperty().bindBidirectional(bookSearchQueryProperty)
                    promptText = messages["search"]
                }
            }

            workbookTableView(filteredBooks) {
                bookTable = this
                hgrow = Priority.ALWAYS
            }

            visibleWhen(books.booleanBinding { it.isNotEmpty() })
            managedWhen(visibleProperty())
        }

        emptyProjectSection {
            visibleWhen(books.booleanBinding { it.isEmpty() })
            managedWhen(visibleProperty())
        }

        books.onChange {
            if (it.list.size > 0) renderTransition()
        }
    }

    private fun renderTransition() {
        val fadeDuration = Duration.seconds(0.5)
        FadeTransition(fadeDuration, bookTable).apply {
            fromValue = 0.1
            toValue = 1.0
        }.play()
    }
}
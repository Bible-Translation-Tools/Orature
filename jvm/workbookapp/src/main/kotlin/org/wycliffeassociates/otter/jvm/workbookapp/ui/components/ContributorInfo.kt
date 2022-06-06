/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import tornadofx.*
import tornadofx.FX.Companion.messages

class ContributorInfo(
    private val contributors: ObservableList<Contributor>
) : VBox() {
    var contributorField: TextField by singleAssign()

    val lastModifiedIndex = SimpleIntegerProperty(-1)
    val addContributorCallbackProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val editContributorCallbackProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val removeContributorCallbackProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        addClass("contributor__container")

        vbox {
            label(messages["contributorHeading"]) {
                addClass("contributor__section-title")
            }
            label(messages["contributorDescription"]) {
                addClass("contributor__section-text")
                minHeight = Region.USE_PREF_SIZE // prevent overflow ellipsis
            }
        }
        hbox {
            addClass("contributor__add-section")

            textfield {
                contributorField = this
                hgrow = Priority.ALWAYS

                addClass("txt-input", "contributor__text-input")
                promptText = messages["contributorName"]

                setOnKeyReleased {
                    if (it.code == KeyCode.ENTER) {
                        addContributor()
                        contributorField.requestFocus()
                    }
                }
            }.hide()

            button(messages["addContributor"]) {
                addClass("btn", "btn--secondary", "contributor__add-btn")
                hgrow = Priority.SOMETIMES
                graphic = FontIcon(MaterialDesign.MDI_PLUS)
                tooltip(this.text)

                setOnAction {
                    if (!contributorField.isVisible) {
                        contributorField.show()
                    } else {
                        addContributor()
                    }
                    contributorField.requestFocus()
                }
            }
        }
        scrollpane {
            vgrow = Priority.ALWAYS
            isFitToWidth = true

            addClass("contributor__list")

            vbox {
                bindChildren(contributors) { contributor ->
                    ContributorCell().apply {
                        nameProperty.set(contributor.name)
                        indexProperty.bind(
                            Bindings.createIntegerBinding(
                                {
                                    contributors.indexOf(contributor)
                                },
                                contributors
                            )
                        )
                        lastModifiedIndexProperty.bind(lastModifiedIndex)
                        onRemoveContributorActionProperty.bind(removeContributorCallbackProperty)
                        onEditContributorActionProperty.bind(editContributorCallbackProperty)
                    }
                }
            }
        }
    }

    private fun addContributor() {
        if (contributorField.text.isBlank()) {
            return
        }

        addContributorCallbackProperty.value?.handle(
            ActionEvent(contributorField.text, null)
        )
        contributorField.clear()
    }
}

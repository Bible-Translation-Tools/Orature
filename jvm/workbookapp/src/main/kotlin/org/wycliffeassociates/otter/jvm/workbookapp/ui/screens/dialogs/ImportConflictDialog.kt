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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ConflictResolution
import org.wycliffeassociates.otter.jvm.controls.button.cardRadioButton
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.controls.toggleFontForText
import tornadofx.*
import java.text.MessageFormat

class ImportConflictDialog : OtterDialog() {

    val projectNameProperty = SimpleStringProperty()
    val chaptersProperty = SimpleIntegerProperty()
    private val onCloseActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onSubmitActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    private val conflictResolutionProperty = SimpleObjectProperty<ConflictResolution>(ConflictResolution.OVERRIDE)

    private val content = VBox().apply {
        addClass("confirm-dialog", "ethiopic-font") // include font for "am" language by default

        hbox {
            addClass("confirm-dialog__header")
            label {
                textProperty().bind(projectNameProperty.stringBinding {
                    MessageFormat.format(messages["bookNameImportTitle"], it)
                })
                addClass("h3")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                tooltip(messages["close"])
                onActionProperty().bind(onCloseActionProperty)
            }
        }

        vbox {
            addClass("confirm-dialog__body", "import-conflict-dialog__body")
            vgrow = Priority.ALWAYS

            label {
                addClass("normal-text")
                textProperty().bind(chaptersProperty.stringBinding {
                    MessageFormat.format(messages["importConflictDescription"], it, projectNameProperty.value)
                })
            }
            hbox {
                addClass("confirm-dialog__control-gap")
                vgrow = Priority.ALWAYS

                val tg = ToggleGroup()
                cardRadioButton(tg) {
                    hgrow = Priority.ALWAYS
                    titleProperty.set(messages["keepOriginal"])
                    subTitleProperty.set(messages["keepOriginalDescription"])

                    setOnAction {
                        conflictResolutionProperty.set(ConflictResolution.DISCARD)
                    }
                    prefWidthProperty().bind(this@hbox.widthProperty().divide(2))
                }

                cardRadioButton(tg) {
                    hgrow = Priority.ALWAYS
                    prefWidthProperty().bind(this@hbox.widthProperty().divide(2))
                    titleProperty.set(messages["overrideWithNew"])
                    subTitleProperty.set(messages["overrideWithNewDescription"])

                    setOnAction {
                        conflictResolutionProperty.set(ConflictResolution.OVERRIDE)
                    }
                    isSelected = true
                }

            }
        }

        hbox {
            addClass("confirm-dialog__footer")
            region { hgrow = Priority.ALWAYS }
            button(messages["cancelImport"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
                onActionProperty().bind(onCloseActionProperty)
            }
            button(messages["submit"]) {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_CHECK_CIRCLE)
                onActionProperty().bind(onSubmitActionProperty)
            }
        }
    }

    init {
        setContent(content)
    }

    override fun onDock() {
        super.onDock()
        content.toggleFontForText(projectNameProperty.value)
    }

    fun setOnCloseAction(op: () -> Unit) {
        onCloseActionProperty.set(EventHandler { op() })
    }

    fun setOnSubmitAction(op: (ConflictResolution) -> Unit) {
        onSubmitActionProperty.set(EventHandler { op(conflictResolutionProperty.value) })
    }
}
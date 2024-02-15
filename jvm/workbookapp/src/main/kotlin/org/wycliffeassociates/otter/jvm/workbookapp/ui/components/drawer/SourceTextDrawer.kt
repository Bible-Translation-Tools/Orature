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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.NodeOrientation
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.rollingtext.RollingSourceText
import org.wycliffeassociates.otter.jvm.controls.rollingtext.rollingSourceText
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class SourceTextDrawer : VBox() {
    val sourceInfoProperty = SimpleStringProperty()
    val textProperty = SimpleStringProperty()
    val licenseProperty = SimpleStringProperty()
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val highlightedChunk = SimpleIntegerProperty(-1)

    private val isCollapsedProperty = SimpleBooleanProperty(false)
    private lateinit var sourceTextContent: RollingSourceText

    init {
        addClass("source-text-drawer")
        hbox {
            addClass("source-text-drawer__header-section")
            label(messages["sourceText"]) {
                addClass("h3", "h3--80")
                visibleWhen { isCollapsedProperty.not() }
                managedWhen(visibleProperty())
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--secondary")
                graphicProperty().bind(isCollapsedProperty.objectBinding {
                    if (it == true) {
                        VBox(
                            FontIcon(MaterialDesign.MDI_CHEVRON_LEFT),
                            FontIcon(MaterialDesign.MDI_BOOK)
                        ).addClass("drawer-graphic-container")
                    } else {
                        HBox(
                            FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT),
                            FontIcon(MaterialDesign.MDI_BOOK)
                        ).addClass("drawer-graphic-container")
                    }
                })
                tooltip {
                    textProperty().bind(isCollapsedProperty.stringBinding {
                        if (it == true) messages["expand"] else messages["collapse"]
                    })
                }
                action {
                    this@SourceTextDrawer.togglePseudoClass("collapsed", !isCollapsedProperty.value)
                    isCollapsedProperty.set(!isCollapsedProperty.value)
                }
            }
        }
        rollingSourceText {
            sourceTextContent = this
            sourceTitleProperty.bind(sourceInfoProperty)
            sourceTextProperty.bind(textProperty)
            licenseTextProperty.bind(licenseProperty.stringBinding {
                it?.let {
                    MessageFormat.format(messages["licenseStatement"], it)
                } ?: ""
            })
            highlightedIndexProperty.bind(highlightedChunk)
            this.orientationProperty.bind(this@SourceTextDrawer.orientationProperty)

            visibleWhen { isCollapsedProperty.not() }
            managedWhen(visibleProperty())
        }
    }
}
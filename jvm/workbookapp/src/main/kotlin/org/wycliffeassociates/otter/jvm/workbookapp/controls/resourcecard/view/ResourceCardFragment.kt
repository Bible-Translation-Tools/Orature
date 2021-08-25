/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.button.highlightablebutton
import org.wycliffeassociates.otter.jvm.controls.statusindicator.StatusIndicator
import org.wycliffeassociates.otter.jvm.controls.statusindicator.statusindicator
import org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.model.ResourceCardItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.RecordResourcePage
import tornadofx.*

class ResourceCardFragment(
    private val item: ResourceCardItem,
    private val filterCompletedCardsProperty: BooleanProperty,
    private val navigator: NavigationMediator
) : Fragment() {
    override val root = HBox()
    val isCurrentResourceProperty = SimpleBooleanProperty(false)
    var primaryColorProperty = SimpleObjectProperty<Color>(Color.ORANGE)
    var primaryColor: Color by primaryColorProperty

    init {
        root.apply {
            alignment = Pos.CENTER_LEFT

            hiddenWhen { item.cardCompletedBinding().and(filterCompletedCardsProperty) }
            managedWhen { visibleProperty() }

            vbox {
                spacing = 3.0
                hbox {
                    spacing = 3.0
                    add(
                        statusindicator {
                            initForResourceCard()
                            progressProperty.bind(item.titleProgressProperty)
                        }
                    )
                    add(
                        statusindicator {
                            initForResourceCard()
                            item.bodyProgressProperty?.let { progressProperty.bind(it) }
                            isVisible = item.hasBodyAudio
                        }
                    )
                }
                text(item.title) {
                    wrappingWidthProperty().bind(root.widthProperty().divide(1.5))
                }
            }

            region {
                hgrow = Priority.ALWAYS
            }

            add(
                highlightablebutton {
                    highlightColorProperty.bind(primaryColorProperty)
                    secondaryColor = Color.WHITE
                    isHighlightedProperty.bind(isCurrentResourceProperty)
                    graphic = FontIcon("gmi-apps").apply { iconSize = 25 }
                    text = messages["open"]
                    action {
                        item.onSelect()
                        navigator.dock<RecordResourcePage>()
                    }
                }
            )
        }
    }

    private fun StatusIndicator.initForResourceCard() {
        prefWidth = 75.0
        primaryFillProperty.bind(primaryColorProperty)
        accentFill = Color.LIGHTGRAY
        trackFill = Color.LIGHTGRAY
        indicatorRadius = 3.0
    }
}

fun resourceCardFragment(
    resource: ResourceCardItem,
    filterCompletedCardsProperty: BooleanProperty,
    navigator: NavigationMediator,
    init: ResourceCardFragment.() -> Unit = {}
) = ResourceCardFragment(resource, filterCompletedCardsProperty, navigator).apply { init.invoke(this) }

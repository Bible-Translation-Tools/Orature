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
package org.wycliffeassociates.otter.jvm.controls.rollingtext

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.NodeOrientation
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.utils.enableScrollByKey
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import tornadofx.*

class RollingSourceText : VBox() {

    val sourceTitleProperty = SimpleStringProperty()
    val sourceTextProperty = SimpleStringProperty()
    val licenseTextProperty = SimpleStringProperty()
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val highlightedIndexProperty = SimpleIntegerProperty()
    val zoomRateProperty = SimpleIntegerProperty(100)

    private val sourceTextList = observableListOf<TextCellData>()
    private lateinit var sourceTextListView: ListView<TextCellData>

    init {
        addClass("source-content__top")
        vgrow = Priority.ALWAYS

        vbox {
            addClass("source-content__text-container")
            vgrow = Priority.ALWAYS

            listview(sourceTextList) {
                sourceTextListView = this
                addClass("wa-list-view", "source-content__chunk-list")
                vgrow = Priority.ALWAYS
                enableScrollByKey()

                setCellFactory {
                    RollingTextCell(
                        sourceTitleProperty,
                        licenseTextProperty,
                        orientationProperty,
                        highlightedIndexProperty
                    )
                }

                runLater { customizeScrollbarSkin() }
            }
        }

        setUpListeners()
    }

    private fun setUpListeners() {
        sourceTextProperty.onChangeAndDoNowWithDisposer { txt ->
            if (txt != null) {
                sourceTextList.setAll(convertTextData(txt))
            }
        }

        zoomRateProperty.onChangeAndDoNowWithDisposer { rate ->
            sourceTextListView.apply {
                styleClass.removeAll { it.startsWith("text-zoom") }
                addClass("text-zoom-$rate")
            }
        }
    }

    private fun convertTextData(txt: String): List<TextCellData> {
        val verses = txt.split("\n")
            .filter { it.isNotEmpty() }
            .map { TextCellData(it, TextCellType.TEXT) }

        return listOf(
            TextCellData(sourceTitleProperty.value, TextCellType.TITLE),
            *verses.toTypedArray(),
            TextCellData(licenseTextProperty.value, TextCellType.LICENSE)
        )
    }


}

fun EventTarget.rollingSourceText(op: RollingSourceText.() -> Unit = {}) = RollingSourceText().attachTo(this, op)
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
package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.EventTarget
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.utils.enableScrollByKey
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import tornadofx.*

class RollingSourceText : VBox() {

    val sourceTitleProperty = SimpleStringProperty()
    val sourceTextProperty = SimpleStringProperty()
    val licenseTextProperty = SimpleStringProperty()
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
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
                    SourceTextCell(sourceTitleProperty, licenseTextProperty, orientationProperty)
                }

                runLater { customizeScrollbarSkin() }
            }
        }

        setUpListeners()
    }

    private fun setUpListeners() {
        sourceTextProperty.onChangeAndDoNowWithDisposer { txt ->
            if (txt != null) {
                sourceTextList.setAll(convertText(txt))
            }
        }

        zoomRateProperty.onChangeAndDoNowWithDisposer { rate ->
            sourceTextListView.apply {
                styleClass.removeAll { it.startsWith("text-zoom") }
                addClass("text-zoom-$rate")
            }
        }
    }

    private fun convertText(txt: String): List<TextCellData> {
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

enum class TextCellType {
    TEXT,
    TITLE,
    LICENSE
}

data class TextCellData(val content: String, val type: TextCellType)

class SourceTextCell(
    private val sourceTitleProperty: StringProperty,
    private val licenseProperty: StringProperty,
    private val orientationProperty: ObjectProperty<NodeOrientation>
) : ListCell<TextCellData>() {
    override fun updateItem(item: TextCellData?, empty: Boolean) {
        super.updateItem(item, empty)

        /*
        allows the list cell width to be overridden to listview.width - insets,
        without this the cell width will extend beyond the listview boundary causing
        a horizontal scroll bar and no word wrapping on the label elements.
        */
        prefWidthProperty().set(0.0)

        if (item == null) {
            graphic = null
            text = null
        } else {
            graphic = renderTextNode(item)
        }
    }

    private fun renderTextNode(textItem: TextCellData): Node {
        return when (textItem.type) {
            TextCellType.TITLE -> buildSourceTitle()
            TextCellType.LICENSE -> buildLicenseText()
            TextCellType.TEXT -> buildChunkText(textItem.content)
        }
    }

    private fun buildSourceTitle(): Label {
        return Label().apply {
            addClass("h4", "h4--80", "source-content__info-text")
            textProperty().bind(sourceTitleProperty)
        }
    }

    private fun buildChunkText(text: String): HBox {
        val markerTitleRegex = Regex("""^\d{1,3}(-\d*)?\.""")
        val markerLabel = markerTitleRegex.find(text)?.value ?: ""
        val textContent = text.substringAfter(markerLabel).trim()

        return HBox().apply {
            addClass("source-content__chunk")
            label(markerLabel.removeSuffix(".")) {
                addClass("source-content__verse-number")
                minWidth = USE_PREF_SIZE
            }
            label(textContent) {
                addClass("source-content__text")
                minHeight = USE_PREF_SIZE // avoid ellipsis
            }
        }
    }

    private fun buildLicenseText(): Label {
        return Label().apply {
            addClass("source-content__license-text")

            textProperty().bind(licenseProperty)
            styleProperty().bind(orientationProperty.objectBinding {
                when (it) {
                    NodeOrientation.LEFT_TO_RIGHT -> "-fx-font-style: italic;"
                    else -> ""
                }
            })
        }
    }
}

fun EventTarget.rollingSourceText(op: RollingSourceText.() -> Unit = {}) = RollingSourceText().attachTo(this, op)
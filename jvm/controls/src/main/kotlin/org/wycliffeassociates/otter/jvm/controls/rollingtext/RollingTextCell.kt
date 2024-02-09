package org.wycliffeassociates.otter.jvm.controls.rollingtext

import javafx.beans.property.IntegerProperty
import javafx.beans.property.ObjectProperty
import javafx.beans.property.StringProperty
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import tornadofx.*

enum class TextCellType {
    TEXT,
    TITLE,
    LICENSE
}

data class TextCellData(val content: String, val type: TextCellType)

class RollingTextCell(
    private val sourceTitleProperty: StringProperty,
    private val licenseProperty: StringProperty,
    private val orientationProperty: ObjectProperty<NodeOrientation>,
    highlightedVerseProperty: IntegerProperty
) : ListCell<TextCellData>() {

    private val shouldHighlight = booleanBinding(highlightedVerseProperty, indexProperty()) {
        highlightedVerseProperty.value == index - 1 // offset the first item which is the source title
    }

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

    /**
     * Convert input text to appropriate data structure for rendering inside
     * the ListView. This always injects the source title and license footer
     * at the beginning and the end of the list, respectively.
     */
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

            shouldHighlight.onChangeAndDoNowWithDisposer {
                togglePseudoClass("highlighted", it == true)
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
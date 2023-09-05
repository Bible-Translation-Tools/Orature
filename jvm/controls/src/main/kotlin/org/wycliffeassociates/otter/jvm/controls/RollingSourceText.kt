package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.controlsfx.control.GridView
import org.wycliffeassociates.otter.jvm.utils.enableScrollByKey
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import tornadofx.*

class RollingSourceText : VBox() {

    val sourceTitleProperty = SimpleStringProperty()
    val licenseTextProperty = SimpleStringProperty()
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val highlightedChunk = SimpleIntegerProperty(-1)
    val sourceTextProperty = SimpleStringProperty()
    val zoomRateProperty = SimpleIntegerProperty(100)

    private lateinit var sourceTextChunksContainer: ListView<Node>

    init {
        addClass("source-content__top")
        vgrow = Priority.ALWAYS

        vbox {
            addClass("source-content__text-container")
            vgrow = Priority.ALWAYS

            listview<Node> {
                sourceTextChunksContainer = this
                addClass("wa-list-view", "source-content__chunk-list")
                vgrow = Priority.ALWAYS
                enableScrollByKey()

                setCellFactory {
                    object : ListCell<Node>() {
                        override fun updateItem(item: Node?, empty: Boolean) {
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
                                graphic = item
                            }
                        }
                    }
                }

                runLater { customizeScrollbarSkin() }
            }
        }

        setUpListeners()
    }

    private fun setUpListeners() {
        sourceTextProperty.onChangeAndDoNowWithDisposer { txt ->
            val chunks = txt?.trim()?.split(Regex("\\d{1,3}\\.")) ?: listOf()
            val nodes = mutableListOf<Node>()
            val sourceTitle = buildSourceTitle()
            val licenseText = buildLicenseText()
            val textNodes = chunks
                .filter { it.isNotBlank() }
                .mapIndexed { index, chunkText ->
                buildChunkText(chunkText, index)
            }.toMutableList()

            nodes.add(sourceTitle)
            nodes.addAll(textNodes)
            nodes.add(licenseText)

            sourceTextChunksContainer.items.setAll(nodes)
        }

        zoomRateProperty.onChangeAndDoNowWithDisposer { rate ->
            sourceTextChunksContainer.apply {
                styleClass.removeAll { it.startsWith("text-zoom") }
                addClass("text-zoom-$rate")
            }
        }
    }

    private fun buildSourceTitle(): HBox {
        return HBox().apply {
                label {
                    addClass("h4")
                    textProperty().bind(sourceTitleProperty)
            }
        }
    }

    private fun buildChunkText(textContent: String, index: Int): HBox {
        val isChunkHighlightedProperty = highlightedChunk.booleanBinding { highlightedIndex ->
            if (highlightedIndex == index) {
                sourceTextChunksContainer.scrollTo(index)
            }
            highlightedIndex == index
        }
        return HBox().apply {
            label((index + 1).toString()) {
                addClass("source-content__verse-number")

                isChunkHighlightedProperty.onChangeAndDoNow {
                    togglePseudoClass("highlighted", it == true)
                }
            }
            label(textContent) {
                addClass("source-content__text")
                minHeight = Region.USE_PREF_SIZE // avoid ellipsis

                isChunkHighlightedProperty.onChangeAndDoNow {
                    togglePseudoClass("highlighted", it == true)
                }
            }
        }
    }

    private fun buildLicenseText(): Label {
        return Label().apply {
            addClass("source-content__license-text")

            textProperty().bind(licenseTextProperty)
            styleProperty().bind(orientationProperty.objectBinding {
                when (it) {
                    NodeOrientation.LEFT_TO_RIGHT -> "-fx-font-style: italic;"
                    else -> ""
                }
            })
        }
    }
}
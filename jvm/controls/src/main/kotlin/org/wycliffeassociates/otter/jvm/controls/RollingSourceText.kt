package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.enableScrollByKey
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer
import tornadofx.*

class RollingSourceText : VBox() {

    val sourceTitleProperty = SimpleStringProperty()
    val sourceTextProperty = SimpleStringProperty()
    val licenseTextProperty = SimpleStringProperty()
    val orientationProperty = SimpleObjectProperty<NodeOrientation>()
    val highlightedChunk = SimpleIntegerProperty(-1)
    val zoomRateProperty = SimpleIntegerProperty(100)

    private lateinit var sourceTextChunksContainer: ListView<Node>
    private val listeners = mutableListOf<ListenerDisposer>()

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

    fun disposeOfListeners() {
        listeners.forEach { it.dispose() }
        listeners.clear()
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
        }.also { listeners.add(it) }

        zoomRateProperty.onChangeAndDoNowWithDisposer { rate ->
            sourceTextChunksContainer.apply {
                styleClass.removeAll { it.startsWith("text-zoom") }
                addClass("text-zoom-$rate")
            }
        }.also { listeners.add(it) }
    }

    private fun buildSourceTitle(): HBox {
        return HBox().apply {
            label {
                addClass("h4", "h4--80")
                textProperty().bind(sourceTitleProperty)
            }
        }
    }

    private fun buildChunkText(textContent: String, index: Int): HBox {
        return HBox().apply {
            addClass("source-content__chunk")
            highlightedChunk.onChangeAndDoNowWithDisposer { highlightedIndex ->
                if (highlightedIndex == index) {
                    sourceTextChunksContainer.scrollTo(index)
                }
                togglePseudoClass("highlighted", highlightedIndex == index)
            }.also { listeners.add(it) }

            label((index + 1).toString()) {
                addClass("source-content__verse-number")
            }
            label(textContent) {
                addClass("source-content__text")
                minHeight = Region.USE_PREF_SIZE // avoid ellipsis
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

fun EventTarget.rollingSourceText(op: RollingSourceText.() -> Unit = {}) = RollingSourceText().attachTo(this, op)
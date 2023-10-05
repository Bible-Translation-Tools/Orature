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
            if (txt == null) {
                return@onChangeAndDoNowWithDisposer
            }
            val nodes = buildTextNodes(txt)
            sourceTextChunksContainer.items.setAll(nodes)
        }

        zoomRateProperty.onChangeAndDoNowWithDisposer { rate ->
            sourceTextChunksContainer.apply {
                styleClass.removeAll { it.startsWith("text-zoom") }
                addClass("text-zoom-$rate")
            }
        }
    }

    private fun buildTextNodes(txt: String): List<Node> {
        val markerRegex = Regex("""\d{1,3}(-\d*)?\.""")
        val matches = markerRegex.findAll(txt)

        val markerLabels = matches.map { it.value.removeSuffix(".") }.toList()
        val chunks = markerRegex.split(txt).filter { it.isNotBlank() }.map { it.trim() }.toList()

        val nodes = mutableListOf<Node>()
        val sourceTitle = buildSourceTitle()
        val licenseText = buildLicenseText()

        nodes.add(sourceTitle)
        for (i in 0 until markerLabels.size) {
            nodes.add(
                buildChunkText(chunks[i], markerLabels[i])
            )
        }
        nodes.add(licenseText)
        return nodes
    }

    private fun buildSourceTitle(): HBox {
        return HBox().apply {
            label {
                addClass("h4", "h4--80", "source-content__info-text")
                textProperty().bind(sourceTitleProperty)
            }
        }
    }

    private fun buildChunkText(textContent: String, chunkLabel: String): HBox {
        return HBox().apply {
            addClass("source-content__chunk")
            label(chunkLabel) {
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
package org.wycliffeassociates.otter.jvm.controls.item

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.Take
import tornadofx.*

class ChunkItem : VBox() {

    val chunkTitleProperty = SimpleStringProperty()
    val showTakesProperty = SimpleBooleanProperty(false)
    val takes = observableListOf<Take>()

    private val downIcon = FontIcon(MaterialDesign.MDI_MENU_DOWN)
    private val upIcon = FontIcon(MaterialDesign.MDI_MENU_UP)

    private val onChunkOpenActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        importStylesheet(javaClass.getResource("/css/chunk-item.css").toExternalForm())
        importStylesheet(javaClass.getResource("/css/take-item.css").toExternalForm())
        styleClass.setAll("chunk-item")

        hbox {
            vbox {
                hgrow = Priority.ALWAYS
                label {
                    addClass("chunk-item__title")
                    textProperty().bind(chunkTitleProperty)
                }
                label {
                    addClass("chunk-item__take-counter")
                    graphic = FontIcon(MaterialDesign.MDI_LIBRARY_MUSIC)
                    text = "3"
                }
            }
            hbox {
                addClass("chunk-item__status")
                circle {
                    addClass("chunk-item__selected-status")
                    radius = 12.0
                }
                label {
                    addClass("chunk-item__show-takes")
                    graphicProperty().bind(showTakesProperty.objectBinding {
                        when (it) {
                            true -> upIcon
                            else ->downIcon
                        }
                    })
                }
            }

            setOnMouseClicked {
                showTakesProperty.set(showTakesProperty.value.not())
            }
        }
        vbox {
            addClass("chunk-item__takes")
            visibleProperty().bind(showTakesProperty)
            managedProperty().bind(visibleProperty())

            button {
                addClass("btn", "btn--secondary")
                text = "Open Verse"
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                onActionProperty().bind(onChunkOpenActionProperty)
            }

            vbox {
                addClass("chunk-item__take-items")
                val toggleGroup = ToggleGroup()
                bindChildren(takes) {
                    TakeItem(toggleGroup).apply {
                        audioProperty.set("Hello")
                    }
                }
            }
        }
    }

    fun setOnChunkOpen(op: () -> Unit) {
        onChunkOpenActionProperty.set(EventHandler { op.invoke() })
    }
}

package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.binding.Bindings
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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*

private const val TAKE_CELL_HEIGHT = 60.0

class ChunkItem : VBox() {
    val chunkTitleProperty = SimpleStringProperty()
    val showTakesProperty = SimpleBooleanProperty(false)
    val hasSelectedProperty = SimpleBooleanProperty(false)

    val takes = observableListOf<TakeModel>()

    private val downIcon = FontIcon(MaterialDesign.MDI_MENU_DOWN)
    private val upIcon = FontIcon(MaterialDesign.MDI_MENU_UP)

    private val onChunkOpenActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onTakeSelectedActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("chunk-item")

        takes.onChange {
            hasSelectedProperty.set(it.list?.any { it.selected } ?: false)
        }

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
                    textProperty().bind(takes.sizeProperty.asString())
                }
            }
            hbox {
                addClass("chunk-item__status")
                circle {
                    addClass("chunk-item__selected-status")
                    hasSelectedProperty.onChange {
                        toggleClass("chunk-item__selected-status--active", it)
                    }
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
            visibleWhen(showTakesProperty)
            managedProperty().bind(visibleProperty())

            button {
                addClass("btn", "btn--secondary")
                text = FX.messages["openVerse"]
                graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                onActionProperty().bind(onChunkOpenActionProperty)
            }

            vbox {
                addClass("chunk-item__take-items")

                val toggleGroup = ToggleGroup()
                listview(takes) {
                    setCellFactory {
                        TakeCell(toggleGroup) {
                            onTakeSelectedActionProperty.value?.handle(
                                ActionEvent(it, null)
                            )
                        }
                    }
                    prefHeightProperty().bind(Bindings.size(takes).multiply(TAKE_CELL_HEIGHT))
                }
            }
        }
    }

    fun setOnChunkOpen(op: () -> Unit) {
        onChunkOpenActionProperty.set(EventHandler { op.invoke() })
    }

    fun setOnTakeSelected(op: (take: TakeModel) -> Unit) {
        onTakeSelectedActionProperty.set(
            EventHandler { op.invoke(it.source as TakeModel) }
        )
    }
}

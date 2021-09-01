package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.controls.media.simpleaudioplayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*

class TakeItem : HBox() {
    val takeProperty = SimpleObjectProperty<TakeModel>()
    val selectedProperty = SimpleBooleanProperty(false)
    val radioGroupProperty = SimpleObjectProperty(ToggleGroup())

    private val onTakeSelectedActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("take-item")

        simpleaudioplayer {
            hgrow = Priority.ALWAYS

            takeProperty.onChange { take ->
                take?.let {
                    fileProperty.set(take.take.file)
                    playerProperty.set(take.audioPlayer)
                }
            }
        }

        radiobutton {
            addClass("wa-radio", "wa-radio--bordered")
            toggleGroupProperty().bind(radioGroupProperty)
            selectedProperty().bindBidirectional(selectedProperty)
            selectedProperty().onChange { selected ->
                onTakeSelectedActionProperty.value?.let {
                    if (selected) it.handle(ActionEvent())
                }
            }
        }
    }

    fun setOnTakeSelected(op: () -> Unit) {
        onTakeSelectedActionProperty.set(EventHandler { op.invoke() })
    }
}

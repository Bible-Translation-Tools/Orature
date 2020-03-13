package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ScriptureTakeCardSkin

class ScriptureTakeCard(
    val player: IAudioPlayer
) : Control() {

    private val onDeleteProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(EventHandler {})
    private val onEditProperty = SimpleObjectProperty<EventHandler<ActionEvent>>(EventHandler {})
    private val deleteTextProperty = SimpleStringProperty("delete")
    private val editTextProperty = SimpleStringProperty("edit")
    private val playTextProperty = SimpleStringProperty("play")
    private val pauseTextProperty = SimpleStringProperty("pause")
    private val takeNumberProperty = SimpleStringProperty("Take 01")
    private val timestampProperty = SimpleStringProperty("")

    fun deleteTextProperty(): StringProperty {
        return deleteTextProperty
    }

    fun editTextProperty(): StringProperty {
        return editTextProperty
    }

    fun playTextProperty(): StringProperty {
        return playTextProperty
    }

    fun pauseTextProperty(): StringProperty {
        return pauseTextProperty
    }

    fun takeNumberProperty(): StringProperty {
        return takeNumberProperty
    }

    fun timestampProperty(): StringProperty {
        return timestampProperty
    }

    fun onDeleteProperty() = onDeleteProperty
    fun onEditProperty() = onEditProperty

    fun setOnDelete(op: () -> Unit) {
        onDeleteProperty.set(EventHandler { op() })
    }

    fun setOnEdit(op: () -> Unit) {
        onEditProperty.set(EventHandler { op() })
    }

    override fun createDefaultSkin(): Skin<*> {
        return ScriptureTakeCardSkin(this)
    }
}
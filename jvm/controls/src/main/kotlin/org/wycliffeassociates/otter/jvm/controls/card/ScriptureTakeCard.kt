package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.*
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ScriptureTakeCardSkin

class ScriptureTakeCard : Control() {

    private val takeProperty = SimpleObjectProperty<Take>()
    private val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    private val deleteTextProperty = SimpleStringProperty("delete")
    private val editTextProperty = SimpleStringProperty("edit")
    private val playTextProperty = SimpleStringProperty("play")
    private val pauseTextProperty = SimpleStringProperty("pause")
    private val takeNumberProperty = SimpleStringProperty("Take 01")
    private val timestampProperty = SimpleStringProperty("")
    private val isDraggingProperty = SimpleBooleanProperty(false)

    fun takeProperty(): ObjectProperty<Take> {
        return takeProperty
    }

    fun audioPlayerProperty(): ObjectProperty<IAudioPlayer> {
        return audioPlayerProperty
    }

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

    fun isDraggingProperty(): BooleanProperty {
        return isDraggingProperty
    }

    override fun createDefaultSkin(): Skin<*> {
        return ScriptureTakeCardSkin(this)
    }
}
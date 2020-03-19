package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ButtonType
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.card.events.DeleteTakeEvent
import org.wycliffeassociates.otter.jvm.controls.card.events.EditTakeEvent
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ScriptureTakeCardSkin
import tornadofx.*
import kotlin.error

class ScriptureTakeCard : Control() {

    private val takeProperty = SimpleObjectProperty<Take>()
    private val audioPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    private val deleteTextProperty = SimpleStringProperty("delete")
    private val editTextProperty = SimpleStringProperty("edit")
    private val playTextProperty = SimpleStringProperty("play")
    private val pauseTextProperty = SimpleStringProperty("pause")
    private val takeNumberProperty = SimpleStringProperty("Take 01")
    private val timestampProperty = SimpleStringProperty("")

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

    override fun createDefaultSkin(): Skin<*> {
        return ScriptureTakeCardSkin(this)
    }
}
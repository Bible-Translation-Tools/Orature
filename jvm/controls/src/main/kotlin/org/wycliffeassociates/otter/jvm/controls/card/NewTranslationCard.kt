package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.NewTranslationCardSkin

class NewTranslationCard : Control() {

    val sourceLanguageProperty = SimpleStringProperty("???")
    val targetLanguageProperty = SimpleStringProperty("???")

    val newTranslationTextProperty = SimpleStringProperty()
    val onActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        styleClass.setAll("new-translation-card")
    }

    override fun createDefaultSkin(): Skin<*> {
        return NewTranslationCardSkin(this)
    }

    fun setOnAction(op: () -> Unit) {
        onActionProperty.set(EventHandler { op.invoke() })
    }
}

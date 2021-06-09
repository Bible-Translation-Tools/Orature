package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.TranslationCardSkin
import tornadofx.*

class TranslationCard<T>(
    sourceLanguage: String = "",
    targetLanguage: String = "",
    items: ObservableList<T> = observableListOf()
) : Control() {

    val sourceLanguageProperty = SimpleStringProperty(sourceLanguage)
    val targetLanguageProperty = SimpleStringProperty(targetLanguage)
    val itemsProperty = SimpleListProperty<T>(items)
    val seeMoreTextProperty = SimpleStringProperty()
    val seeLessTextProperty = SimpleStringProperty()

    val onNewBookActionProperty = SimpleObjectProperty<() -> Unit>()
    val shownItemsNumberProperty = SimpleIntegerProperty(3)

    internal val converterProperty = SimpleObjectProperty<(T) -> Node>()
    internal val seeAllProperty = SimpleBooleanProperty(false)

    init {
        styleClass.setAll("translation-card")
    }

    override fun createDefaultSkin(): Skin<*> {
        return TranslationCardSkin(this)
    }

    fun setOnNewBookAction(op: () -> Unit) {
        onNewBookActionProperty.set(op)
    }

    fun setConverter(op: (T) -> Node) {
        converterProperty.set(op)
    }
}

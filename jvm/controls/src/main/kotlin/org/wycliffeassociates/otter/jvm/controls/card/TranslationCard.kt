package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.Control
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.Skin
import javafx.util.Callback
import org.wycliffeassociates.otter.jvm.controls.skins.cards.TranslationCardSkin
import tornadofx.*

class TranslationCard<T>(
    sourceLanguage: String = "",
    targetLanguage: String = "",
    books: ObservableList<T> = observableListOf()
) : Control() {

    val sourceLanguageProperty = SimpleStringProperty(sourceLanguage)
    val targetLanguageProperty = SimpleStringProperty(targetLanguage)
    val booksProperty = SimpleObjectProperty<ObservableList<T>>(books)

    val cellFactoryProperty = SimpleObjectProperty<Callback<ListView<T>, ListCell<T>>>()
    val onNewBookActionProperty = SimpleObjectProperty<() -> Unit>()

    init {
        importStylesheet(javaClass.getResource("/css/translation-card.css").toExternalForm())
        styleClass.setAll("translation-card")
    }

    override fun createDefaultSkin(): Skin<*> {
        return TranslationCardSkin(this)
    }

    fun setCellFactory(value: (ListView<T>) -> ListCell<T>) {
        cellFactoryProperty.set(Callback(value))
    }

    fun setOnNewBookAction(op: () -> Unit) {
        onNewBookActionProperty.set(op)
    }
}

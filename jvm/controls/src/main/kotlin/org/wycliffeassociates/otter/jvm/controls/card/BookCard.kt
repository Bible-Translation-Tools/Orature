package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.cards.BookCardSkin
import tornadofx.*
import java.io.File

class BookCard(
    title: String = "",
    projectType: String = "",
    coverArt: File? = null,
    newBook: Boolean = false
) : Control() {

    val coverArtProperty = SimpleObjectProperty<File>(coverArt)
    val titleProperty = SimpleStringProperty(title)
    val projectTypeProperty = SimpleStringProperty(projectType)
    val newBookProperty = SimpleBooleanProperty(newBook)

    val addBookTextProperty = SimpleStringProperty("Add Book")
    val onAddBookActionProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()

    init {
        importStylesheet(javaClass.getResource("/css/book-card.css").toExternalForm())
        styleClass.setAll("book-card")
    }

    override fun createDefaultSkin(): Skin<*> {
        return BookCardSkin(this)
    }

    fun onAddBookAction(op: () -> Unit) {
        onAddBookActionProperty.set(EventHandler { op.invoke() })
    }
}

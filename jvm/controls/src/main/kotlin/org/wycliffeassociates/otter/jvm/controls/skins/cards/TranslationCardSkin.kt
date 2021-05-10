package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.SkinBase
import org.wycliffeassociates.otter.jvm.controls.card.BookCard
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard
import tornadofx.*

class TranslationCardSkin<T>(private val card: TranslationCard<T>) : SkinBase<TranslationCard<T>>(card) {

    @FXML
    lateinit var sourceLanguageText: Label

    @FXML
    lateinit var targetLanguageText: Label

    @FXML
    lateinit var bookCards: ListView<T>

    @FXML
    lateinit var newBookCard: BookCard

    init {
        loadFXML()
        initializeControl()

        importStylesheet(javaClass.getResource("/css/translation-card.css").toExternalForm())
    }

    private fun initializeControl() {
        sourceLanguageText.textProperty().bind(card.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(card.targetLanguageProperty)

        bookCards.apply {
            itemsProperty().bind(card.booksProperty)
            cellFactoryProperty().bind(card.cellFactoryProperty)
        }

        newBookCard.apply {
            newBookProperty.set(true)
            setOnAddBookAction {
                card.onNewBookActionProperty.value.invoke()
            }
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("TranslationCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

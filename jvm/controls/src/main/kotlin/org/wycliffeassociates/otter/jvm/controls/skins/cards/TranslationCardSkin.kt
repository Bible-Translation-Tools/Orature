package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.card.BookCard
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class TranslationCardSkin<T>(private val card: TranslationCard<T>) : SkinBase<TranslationCard<T>>(card) {

    @FXML
    lateinit var sourceLanguageText: Label

    @FXML
    lateinit var targetLanguageText: Label

    @FXML
    lateinit var bookCards: VBox

    @FXML
    lateinit var newBookCard: BookCard

    @FXML
    lateinit var showMoreBtn: Button

    init {
        loadFXML()
        initializeControl()

        importStylesheet(javaClass.getResource("/css/translation-card.css").toExternalForm())
    }

    private fun initializeControl() {
        sourceLanguageText.textProperty().bind(card.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(card.targetLanguageProperty)

        bookCards.apply {
            val toShow = card.shownItemsNumberProperty.value

            children.bind(card.itemsProperty, card.converterProperty.value)

            children.onChangeAndDoNow { list ->
                if (list.size > toShow) {
                    list.slice(toShow..list.lastIndex).forEach(::hideNode)
                }
            }

            card.showAllProperty.onChange {
                if (it) {
                    children.forEach(::showNode)
                } else {
                    if (children.size > toShow) {
                        children.slice(toShow..children.lastIndex).forEach(::hideNode)
                    }
                }
            }
        }

        newBookCard.apply {
            newBookProperty.set(true)
            setOnAddBookAction {
                card.onNewBookActionProperty.value?.invoke()
            }
        }

        showMoreBtn.apply {
            textProperty().bind(card.showMoreTextProperty)
            visibleProperty().bind(card.itemsProperty.booleanBinding {
                it?.let {
                    it.size > card.shownItemsNumberProperty.value
                } ?: false
            })
            managedProperty().bind(visibleProperty())
            setOnAction {
                card.showAllProperty.set(!card.showAllProperty.value)
            }
        }
    }

    private fun showNode(node: Node) {
        node.isVisible = true
        node.isManaged = true
    }

    private fun hideNode(node: Node) {
        node.isVisible = false
        node.isManaged = false
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("TranslationCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

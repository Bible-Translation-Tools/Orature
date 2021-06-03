package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.card.BookCard
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import java.text.MessageFormat
import java.util.concurrent.Callable

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
    lateinit var seeMoreBtn: Button

    private val downIcon = FontIcon(MaterialDesign.MDI_MENU_DOWN)
    private val upIcon = FontIcon(MaterialDesign.MDI_MENU_UP)

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        sourceLanguageText.textProperty().bind(card.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(card.targetLanguageProperty)

        bookCards.apply {
            val toShow = card.shownItemsNumberProperty.value

            children.bind(card.itemsProperty, card.converterProperty.value)

            children.onChangeAndDoNow { list ->
                card.seeAllProperty.set(false)
                if (list.size > toShow) {
                    list.slice(toShow..list.lastIndex).forEach(::hideNode)
                }
            }

            card.seeAllProperty.onChange {
                when (it) {
                    true -> children.forEach(::showNode)
                    false -> children.slice(toShow..children.lastIndex).forEach(::hideNode)
                }
            }
        }

        newBookCard.apply {
            newBookProperty.set(true)
            setOnAddBookAction {
                card.onNewBookActionProperty.value?.invoke()
            }
        }

        seeMoreBtn.apply {
            textProperty().bind(card.seeMoreTextProperty)
            visibleProperty().bind(card.itemsProperty.booleanBinding {
                it?.let {
                    it.size > card.shownItemsNumberProperty.value
                } ?: false
            })
            managedProperty().bind(visibleProperty())
            textProperty().bind(seeMoreLessTextBinding())
            graphicProperty().bind(
                Bindings.`when`(card.seeAllProperty)
                    .then(upIcon)
                    .otherwise(downIcon)
            )
            setOnAction {
                card.seeAllProperty.set(!card.seeAllProperty.value)
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

    private fun seeMoreLessTextBinding(): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                val hidden = card.itemsProperty.value.size - card.shownItemsNumberProperty.value
                when (card.seeAllProperty.value) {
                     true -> {
                        MessageFormat.format(
                            "{0} ({1})",
                            card.seeLessTextProperty.value,
                            hidden
                        )
                     }
                     false -> {
                         MessageFormat.format(
                             "{0} ({1})",
                             card.seeMoreTextProperty.value,
                             hidden
                         )
                     }
                 }
            },
            card.seeAllProperty,
            card.seeMoreTextProperty,
            card.seeLessTextProperty,
            card.itemsProperty
        )
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("TranslationCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

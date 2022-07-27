/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.ListProperty
import javafx.collections.ObservableList

class TranslationCardSkin<T>(private val card: TranslationCard<T>) : SkinBase<TranslationCard<T>>(card) {

    @FXML
    lateinit var sourceLanguageText: Label

    @FXML
    lateinit var targetLanguageText: Label

    @FXML
    lateinit var removeTranslationBtn: Button

    @FXML
    lateinit var bookCards: VBox

    @FXML
    lateinit var newBookCard: BookCard

    @FXML
    lateinit var seeMoreBtn: Button

    @FXML
    lateinit var divider: Label

    private val downIcon = FontIcon(MaterialDesign.MDI_MENU_DOWN)
    private val upIcon = FontIcon(MaterialDesign.MDI_MENU_UP)

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        sourceLanguageText.textProperty().bind(card.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(card.targetLanguageProperty)

        removeTranslationBtn.apply {
            textProperty().bind(card.removeTranslationTextProperty)
            disableWhen {
                !card.itemsProperty.emptyProperty()
            }
            setOnAction { card.onRemoveTranslationProperty.value?.invoke() }
        }

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

        divider.apply {
            graphic.scaleXProperty().bind(card.orientationScaleProperty)
        }

        seeMoreBtn.apply {
            textProperty().bind(card.showMoreTextProperty)
            visibleProperty().bind(
                booleanBinding(
                    card.itemsProperty,
                    op = {
                        card.itemsProperty.value?.let {
                            it.size > card.shownItemsNumberProperty.value
                        } ?: false
                    }
                )
            )
            managedProperty().bind(visibleProperty())
            textProperty().bind(seeMoreLessTextBinding())
            tooltip { textProperty().bind(seeMoreLessTextBinding()) }
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
                            card.showLessTextProperty.value,
                            hidden
                        )
                    }
                    false -> {
                        MessageFormat.format(
                            "{0} ({1})",
                            card.showMoreTextProperty.value,
                            hidden
                        )
                    }
                }
            },
            card.seeAllProperty,
            card.showMoreTextProperty,
            card.showLessTextProperty,
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

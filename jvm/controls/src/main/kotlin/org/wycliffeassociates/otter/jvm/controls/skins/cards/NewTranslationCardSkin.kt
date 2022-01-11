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

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import org.wycliffeassociates.otter.jvm.controls.card.NewTranslationCard
import tornadofx.*

class NewTranslationCardSkin(private val card: NewTranslationCard) : SkinBase<NewTranslationCard>(card) {

    @FXML
    lateinit var sourceLanguageText: Label

    @FXML
    lateinit var targetLanguageText: Label

    @FXML
    lateinit var newTranslationBtn: Button

    @FXML
    lateinit var divider: Label

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bindText()
        bindAction()
    }

    private fun bindText() {
        sourceLanguageText.textProperty().bind(card.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(card.targetLanguageProperty)
        newTranslationBtn.textProperty().bind(card.newTranslationTextProperty)
        newTranslationBtn.tooltip {
            textProperty().bind(newTranslationBtn.textProperty())
        }
        divider.apply {
            graphic.scaleXProperty().bind(card.orientationScaleProperty)
        }
    }

    private fun bindAction() {
        newTranslationBtn.onActionProperty().bind(card.onActionProperty)
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("NewTranslationCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

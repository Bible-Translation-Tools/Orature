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
package org.wycliffeassociates.otter.jvm.controls.skins.button

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.controls.button.SelectButton

class SelectButtonSkin(private val button: SelectButton) : SkinBase<SelectButton>(button) {
    @FXML
    lateinit var root: HBox

    @FXML
    lateinit var btnLabel: Label

    @FXML
    lateinit var btnIcon: Label

    @FXML
    lateinit var btnRadio: RadioButton

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        button.setOnMouseClicked {
            if (!button.isSelected) {
                button.fire()
            }
        }
        btnRadio.selectedProperty().bindBidirectional(button.selectedProperty())

        btnLabel.textProperty().bind(button.textProperty())
        btnIcon.graphicProperty().bind(button.graphicProperty())
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SelectButton.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

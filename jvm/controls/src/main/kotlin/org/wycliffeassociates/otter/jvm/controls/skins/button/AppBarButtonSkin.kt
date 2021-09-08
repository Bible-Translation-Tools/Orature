/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
import javafx.scene.control.SkinBase
import javafx.scene.control.ToggleButton
import javafx.scene.layout.VBox

class AppBarButtonSkin(private val button: ToggleButton) : SkinBase<ToggleButton>(button) {

    @FXML
    lateinit var root: VBox

    @FXML
    lateinit var btnLabel: Label

    @FXML
    lateinit var btnIcon: Label

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        button.setOnMouseClicked { button.fire() }

        btnLabel.apply {
            textProperty().bind(button.textProperty())
        }
        btnIcon.apply {
            graphicProperty().bind(button.graphicProperty())
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("AppBarButton.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

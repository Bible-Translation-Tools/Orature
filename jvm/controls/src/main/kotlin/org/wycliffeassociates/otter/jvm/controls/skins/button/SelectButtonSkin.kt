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

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.controls.button.SelectButton
import tornadofx.*

class SelectButtonSkin(private val button: SelectButton) : SkinBase<SelectButton>(button) {
    private val behavior = ButtonBehavior(button)

    @FXML
    lateinit var root: HBox

    @FXML
    lateinit var btnLabel: Label

    @FXML
    lateinit var btnIcon: Label

    @FXML
    lateinit var spacer: Region

    @FXML
    lateinit var btnRadio: RadioButton

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        btnRadio.isMouseTransparent = true
        btnRadio.selectedProperty().bind(button.selectedProperty())

        btnLabel.apply {
            textProperty().bind(button.textProperty())
            managedProperty().bind(textProperty().booleanBinding { it?.isNotEmpty() ?: false })
        }
        btnIcon.apply {
            graphicProperty().bind(button.graphicProperty())
            managedProperty().bind(graphicProperty().booleanBinding { it != null })
        }
        spacer.managedProperty().bind(
            btnLabel.textProperty().booleanBinding { it?.isNotEmpty() ?: false }
                .or(btnIcon.graphicProperty().booleanBinding { it != null })
        )
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SelectButton.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}

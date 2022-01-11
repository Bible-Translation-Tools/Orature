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
package org.wycliffeassociates.otter.jvm.controls.skins.media

import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.NodeOrientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.SkinBase
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.media.ExceptionContent
import tornadofx.*

class ExceptionContentSkin(private var content: ExceptionContent) : SkinBase<ExceptionContent>(content) {

    @FXML
    private lateinit var titleLabel: Label

    @FXML
    private lateinit var headerLabel: Label

    @FXML
    private lateinit var showMoreButton: Button

    @FXML
    private lateinit var stacktraceScrollPane: ScrollPane

    @FXML
    private lateinit var stacktraceText: Label

    @FXML
    private lateinit var sendReportCheckbox: CheckBox

    @FXML
    private lateinit var closeButton: Button

    private val showMoreIcon = FontIcon("gmi-expand-more").apply { styleClass.add("btn__icon") }
    private val showLessIcon = FontIcon("gmi-expand-less").apply { styleClass.add("btn__icon") }

    init {
        loadFXML()
        bindText()
        bindAction()

        stacktraceScrollPane.apply {
            visibleProperty().bind(this@ExceptionContentSkin.content.showMoreProperty())
            managedProperty().bind(this@ExceptionContentSkin.content.showMoreProperty())
            nodeOrientation = NodeOrientation.LEFT_TO_RIGHT
        }

        importStylesheet(javaClass.getResource("/css/exception-content.css").toExternalForm())
    }

    private fun bindText() {
        titleLabel.textProperty().bind(content.titleTextProperty())
        headerLabel.textProperty().bind(content.headerTextProperty())
        sendReportCheckbox.apply {
            textProperty().bind(content.sendReportTextProperty())
            tooltip { textProperty().bind(this@apply.textProperty()) }
            content.sendReportProperty().bind(selectedProperty())
        }
        stacktraceText.textProperty().bind(content.stackTraceProperty())
        showMoreButton.apply {
            textProperty().bind(
                Bindings.`when`(content.showMoreProperty())
                    .then(content.showLessTextProperty())
                    .otherwise(content.showMoreTextProperty())
            )
            tooltip { textProperty().bind(this@apply.textProperty()) }
            graphicProperty().bind(
                Bindings.`when`(content.showMoreProperty())
                    .then(showLessIcon)
                    .otherwise(showMoreIcon)
            )
        }
        closeButton.apply {
            tooltip { textProperty().bind(content.closeTextProperty()) }
        }
    }

    private fun bindAction() {
        showMoreButton.setOnAction {
            showMore()
        }
        closeButton.apply {
            onActionProperty().bind(content.onCloseActionProperty())
            textProperty().bind(content.closeTextProperty())
            disableProperty().bind(content.sendingReportProperty())
        }
    }

    private fun showMore() {
        content.showMoreProperty().set(!content.showMoreProperty().get())
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ExceptionContent.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

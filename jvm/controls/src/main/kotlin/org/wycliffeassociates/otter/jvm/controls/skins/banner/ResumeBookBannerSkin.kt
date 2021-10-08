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
package org.wycliffeassociates.otter.jvm.controls.skins.banner

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.banner.ResumeBookBanner
import tornadofx.tooltip

class ResumeBookBannerSkin(private val banner: ResumeBookBanner) : SkinBase<ResumeBookBanner>(banner) {

    @FXML
    lateinit var bgGraphic: HBox

    @FXML
    lateinit var bookTitle: Label

    @FXML
    lateinit var sourceLanguageText: Label

    @FXML
    lateinit var targetLanguageText: Label

    @FXML
    lateinit var resumeButton: Button

    private val cornerRadius = 20.0

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bgGraphic.apply {
            backgroundProperty().bind(banner.backgroundBinding())
            val rect = Rectangle().apply {
                widthProperty().bind(bgGraphic.widthProperty())
                heightProperty().bind(bgGraphic.heightProperty())

                arcWidth = cornerRadius
                arcHeight = cornerRadius
            }
            clip = rect
        }

        bindText()
        bindAction()
    }

    private fun bindText() {
        bookTitle.textProperty().bind(banner.bookTitleProperty)
        sourceLanguageText.textProperty().bind(banner.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(banner.targetLanguageProperty)
        resumeButton.textProperty().bind(banner.resumeTextProperty)
    }

    private fun bindAction() {
        resumeButton.onActionProperty().bind(banner.onResumeActionProperty)
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ResumeBookBanner.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        root.apply {
            tooltip {
                textProperty().bind(banner.attributionTextProperty)
            }
        }
        children.add(root)
    }
}

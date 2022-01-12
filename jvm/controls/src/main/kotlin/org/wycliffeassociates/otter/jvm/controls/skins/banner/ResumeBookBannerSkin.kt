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
package org.wycliffeassociates.otter.jvm.controls.skins.banner

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.banner.ResumeBookBanner
import tornadofx.*

class ResumeBookBannerSkin(private val banner: ResumeBookBanner) : SkinBase<ResumeBookBanner>(banner) {

    @FXML
    lateinit var bgGraphic: HBox

    @FXML
    lateinit var bookCoverImage: ImageView

    @FXML
    lateinit var bookTitle: Label

    @FXML
    lateinit var sourceLanguageText: Label

    @FXML
    lateinit var targetLanguageText: Label

    @FXML
    lateinit var resumeButton: Button

    @FXML
    lateinit var divider: Label

    private val cornerRadius = 20.0

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bgGraphic.apply {
            val rect = Rectangle().apply {
                widthProperty().bind(bgGraphic.widthProperty())
                heightProperty().bind(bgGraphic.heightProperty())

                arcWidth = cornerRadius
                arcHeight = cornerRadius
            }
            clip = rect
        }
        bookCoverImage.apply {
            imageProperty().bind(banner.coverImageBinding())
            fitHeightProperty().bind(banner.maxHeightProperty())
            // tooltip hover for underlay node is set in .fxml (pickOnBounds)
            tooltip {
                textProperty().bind(banner.attributionTextProperty)
            }
        }

        bindText()
        bindAction()
    }

    private fun bindText() {
        bookTitle.textProperty().bind(banner.bookTitleProperty)
        sourceLanguageText.textProperty().bind(banner.sourceLanguageProperty)
        targetLanguageText.textProperty().bind(banner.targetLanguageProperty)
        resumeButton.apply {
            textProperty().bind(banner.resumeTextProperty)
            tooltip {
                textProperty().bind(this@apply.textProperty())
            }
            graphic.scaleXProperty().bind(banner.orientationScaleProperty)
        }
        divider.apply {
            graphic.scaleXProperty().bind(banner.orientationScaleProperty)
        }
    }

    private fun bindAction() {
        resumeButton.onActionProperty().bind(banner.onResumeActionProperty)
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ResumeBookBanner.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

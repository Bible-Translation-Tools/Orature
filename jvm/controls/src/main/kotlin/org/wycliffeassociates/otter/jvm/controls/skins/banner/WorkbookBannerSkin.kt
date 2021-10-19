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
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import tornadofx.tooltip

class WorkbookBannerSkin(private val banner: WorkbookBanner) : SkinBase<WorkbookBanner>(banner) {

    @FXML
    lateinit var bgGraphic: HBox

    @FXML
    lateinit var bookCoverImage: ImageView

    @FXML
    lateinit var bookTitle: Label

    @FXML
    lateinit var resourceTitle: Label

    @FXML
    lateinit var deleteBtn: Button

    @FXML
    lateinit var exportBtn: Button

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bgGraphic.apply {
            val rect = Rectangle().apply {
                widthProperty().bind(bgGraphic.widthProperty())
                heightProperty().bind(bgGraphic.heightProperty())

                arcWidth = 20.0
                arcHeight = 20.0
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
        resourceTitle.textProperty().bind(banner.resourceTitleProperty)

        deleteBtn.textProperty().bind(banner.deleteTitleProperty)
        deleteBtn.tooltip {
            textProperty().bind(deleteBtn.textProperty())
        }
        exportBtn.textProperty().bind(banner.exportTitleProperty)
        exportBtn.tooltip {
            textProperty().bind(exportBtn.textProperty())
        }
    }

    private fun bindAction() {
        deleteBtn.apply {
            visibleProperty().bind(banner.hideDeleteButtonProperty.not())
            managedProperty().bind(visibleProperty())
            onActionProperty().bind(banner.onDeleteActionProperty)
        }
        exportBtn.apply {
            onActionProperty().bind(banner.onExportActionProperty)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("WorkbookBanner.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

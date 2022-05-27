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
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.SkinBase
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportOption
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import org.wycliffeassociates.otter.jvm.controls.listview.DummyExportComboBoxButton
import org.wycliffeassociates.otter.jvm.controls.listview.ExportOptionListCell
import tornadofx.*
import tornadofx.FX.Companion.messages

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

    @FXML
    lateinit var exportSelectMenu: ComboBox<ExportOption>

    @FXML
    lateinit var fakeExportMenu: ComboBox<String> // this menu displays on top of the actual menu

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
        exportSelectMenu.apply {
            items = banner.exportOptions
            setCellFactory {
                ExportOptionListCell()
            }
            tooltip {
                text = messages["exportOptions"]
            }

            selectionModel.selectedItemProperty().onChange {
                runLater { selectionModel.clearSelection() }
            }
        }

        fakeExportMenu.apply {
            prefWidthProperty().bind(exportSelectMenu.widthProperty())
            minHeightProperty().bind(exportSelectMenu.prefHeightProperty())

            items.setAll(messages["exportOptions"])
            buttonCell = DummyExportComboBoxButton()
            selectionModel.selectFirst()
        }
        bindText()
        bindAction()
    }

    private fun bindText() {
        bookTitle.textProperty().bind(banner.bookTitleProperty)
        resourceTitle.textProperty().bind(banner.resourceTitleProperty)
        deleteBtn.tooltip {
            textProperty().bind(banner.deleteTitleProperty)
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

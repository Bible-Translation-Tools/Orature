package org.wycliffeassociates.otter.jvm.controls.skins.banner

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import tornadofx.*

class WorkbookBannerSkin(private val banner: WorkbookBanner) : SkinBase<WorkbookBanner>(banner) {

    @FXML
    lateinit var bgGraphic: HBox

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

        importStylesheet(javaClass.getResource("/css/workbook-banner.css").toExternalForm())
    }

    private fun initializeControl() {
        bgGraphic.apply {
            backgroundProperty().bind(banner.backgroundBinding())
            val rect = Rectangle().apply {
                widthProperty().bind(bgGraphic.widthProperty())
                heightProperty().bind(bgGraphic.heightProperty())

                arcWidth = 20.0
                arcHeight = 20.0
            }
            clip = rect
        }

        bindText()
        bindAction()
    }

    private fun bindText() {
        bookTitle.textProperty().bind(banner.bookTitleProperty)
        resourceTitle.textProperty().bind(banner.resourceTitleProperty)

        deleteBtn.textProperty().bind(banner.deleteTitleProperty)
        exportBtn.textProperty().bind(banner.exportTitleProperty)
    }

    private fun bindAction() {
        deleteBtn.apply {
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

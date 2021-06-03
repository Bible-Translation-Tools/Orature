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
        children.add(root)
    }
}

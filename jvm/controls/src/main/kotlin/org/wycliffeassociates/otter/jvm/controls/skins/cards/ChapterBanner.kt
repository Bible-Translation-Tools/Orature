package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox

class ChapterBanner() : VBox() {

    @FXML
    lateinit var bookTitle: Label
    @FXML
    lateinit var chapterCount: Label
    @FXML
    lateinit var chunkCount: Label
    @FXML
    lateinit var openButton: Button

    init {
        val loader = FXMLLoader(javaClass.getResource("ChapterBanner.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
package org.wycliffeassociates.otter.jvm.controls.skins.button

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.control.ToggleButton
import javafx.scene.layout.VBox
import tornadofx.*

class AppBarButtonSkin(private val button: ToggleButton) : SkinBase<ToggleButton>(button) {

    @FXML
    lateinit var root: VBox

    @FXML
    lateinit var btnLabel: Label

    @FXML
    lateinit var btnIcon: Label

    init {
        importStylesheet(javaClass.getResource("/css/app-bar-button.css").toExternalForm())

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

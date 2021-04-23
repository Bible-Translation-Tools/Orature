package org.wycliffeassociates.otter.jvm.controls.skins.button

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.button.AppBarButton
import tornadofx.*

class AppBarButtonSkin(private val button: AppBarButton) : SkinBase<AppBarButton>(button) {

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
        root.apply {
            onMouseClickedProperty().bind(button.onActionProperty)
        }

        btnLabel.apply {
            textProperty().bind(button.btnTextProperty)
        }
        btnIcon.apply {
            graphicProperty().bind(button.btnIconProperty)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("AppBarButton.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

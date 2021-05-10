package org.wycliffeassociates.otter.jvm.controls.skins.button

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import tornadofx.*

class CheckboxButtonSkin(private val checkbox: CheckBox) : SkinBase<CheckBox>(checkbox) {

    @FXML
    lateinit var root: HBox

    @FXML
    lateinit var btnLabel: Label

    @FXML
    lateinit var btnIcon: Label

    @FXML
    lateinit var btnCheckbox: CheckBox

    init {
        importStylesheet(javaClass.getResource("/css/checkbox-button.css").toExternalForm())

        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        checkbox.setOnMouseClicked { checkbox.fire() }
        checkbox.selectedProperty().bindBidirectional(btnCheckbox.selectedProperty())

        btnLabel.apply {
            textProperty().bind(checkbox.textProperty())
        }
        btnIcon.apply {
            graphicProperty().bind(checkbox.graphicProperty())
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("CheckboxButton.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

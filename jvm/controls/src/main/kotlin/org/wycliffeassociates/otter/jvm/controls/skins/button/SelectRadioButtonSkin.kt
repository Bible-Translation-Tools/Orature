package org.wycliffeassociates.otter.jvm.controls.skins.button

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import org.wycliffeassociates.otter.jvm.controls.button.SelectRadioButton

class SelectRadioButtonSkin(private val button: SelectRadioButton) : SkinBase<SelectRadioButton>(button) {

    @FXML
    lateinit var root: HBox

    @FXML
    lateinit var btnLabel: Label

    @FXML
    lateinit var btnIcon: Label

    @FXML
    lateinit var btnRadio: RadioButton

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        button.setOnMouseClicked {
            if (!button.isSelected) button.fire()
        }
        btnLabel.textProperty().bind(button.textProperty())
        btnIcon.graphicProperty().bind(button.graphicProperty())
        btnRadio.selectedProperty().bindBidirectional(button.selectedProperty())
        btnRadio.setOnMouseClicked {
            if (!button.isSelected) button.fire()
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("SelectRadioButton.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}

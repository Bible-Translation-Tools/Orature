package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.layout.HBox
import tornadofx.*

class ComboboxButton : HBox() {
    val iconProperty = SimpleObjectProperty<Node>()
    val textProperty = SimpleStringProperty()

    init {
        addClass("wa-combobox-button")
        label {
            graphicProperty().bind(iconProperty)
        }
        label(textProperty)
    }
}

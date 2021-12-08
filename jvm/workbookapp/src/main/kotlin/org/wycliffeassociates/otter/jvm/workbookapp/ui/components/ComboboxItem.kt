package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.VBox
import tornadofx.*

class ComboboxItem : VBox() {
    val topTextProperty = SimpleStringProperty()
    val bottomTextProperty = SimpleStringProperty()
    init {
        addClass("wa-combobox-item")
        label(topTextProperty).apply {
            addClass("wa-combobox-item__top")
        }
        label(bottomTextProperty).apply {
            addClass("wa-combobox-item__bottom")
            managedProperty().bind(bottomTextProperty.isNotNull)
        }
    }
}

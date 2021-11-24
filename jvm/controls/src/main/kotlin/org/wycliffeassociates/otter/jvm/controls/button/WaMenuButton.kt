package org.wycliffeassociates.otter.jvm.controls.button

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.layout.HBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class WaMenuButton : Button() {
    val buttonTextProperty = SimpleStringProperty()
    val sideProperty = SimpleObjectProperty<Side>()
    val items: ObservableList<MenuItem> = FXCollections.observableArrayList()

    private var menu: ContextMenu? = null

    init {
        addClass("wa-menu-button")

        graphic = HBox().apply {
            addClass("wa-menu-button__content")

            label {
                graphic = FontIcon(MaterialDesign.MDI_SPEEDOMETER)
            }
            label {
                textProperty().bind(buttonTextProperty)
            }
            label {
                addClass("wa-menu-button__arrow")
                graphic = FontIcon(MaterialDesign.MDI_TRIANGLE)
            }
        }

        action {
            toggle()
        }

        items.onChange {
            hide()
            if (it.list.isNotEmpty()) {
                menu = ContextMenu()
                menu?.items?.setAll(it.list)
            }
        }
    }

    fun show() {
        menu?.show(this, sideProperty.value, 0.0, 0.0)
    }

    fun hide() {
        menu?.hide()
    }

    private fun toggle() {
        when (menu?.isShowing) {
            true -> hide()
            else -> show()
        }
    }
}

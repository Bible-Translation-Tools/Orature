package org.wycliffeassociates.otter.jvm.controls.bar

import javafx.beans.binding.Bindings
import javafx.beans.binding.ObjectBinding
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.Button
import org.controlsfx.control.textfield.CustomTextField
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*
import tornadofx.FX.Companion.messages

class SearchBar : CustomTextField() {

    private val searchIcon = FontIcon(MaterialDesign.MDI_MAGNIFY)
    private val clearBtn = Button().apply {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_CLOSE_CIRCLE)
    }

    init {
        addClass("txt-input", "filtered-search-bar__input")
        promptText = messages["searchPlaceholder"]

        clearBtn.setOnAction {
            text = ""
            this.requestFocus()
        }
        rightProperty().bind(createGraphicBinding())
    }

    private fun createGraphicBinding(): ObjectBinding<Node> {
        return Bindings.createObjectBinding(
            {
                if (textProperty().isEmpty.value) {
                    searchIcon
                } else {
                    clearBtn
                }
            },
            textProperty()
        )
    }
}

fun EventTarget.searchBar(op: SearchBar.() -> Unit = {}) = SearchBar().attachTo(this, op)

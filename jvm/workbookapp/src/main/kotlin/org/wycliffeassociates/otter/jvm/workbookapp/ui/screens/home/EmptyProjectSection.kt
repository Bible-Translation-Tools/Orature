package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.home

import javafx.event.EventTarget
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class EmptyProjectSection : VBox() {
    init {
        addClass("homepage__main-region")

        hbox {
            addClass("homepage__main-region__header-section")
            button {
                addClass("btn", "btn--icon", "btn--borderless", "option-button")
                graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL)
            }
            label(FX.messages["home"]) { addClass("h4") }
        }

        vbox {
            addClass("homepage__main-region__body", "homepage__main-region__empty-section")
            vgrow = Priority.ALWAYS

            label {
                graphic = FontIcon(MaterialDesign.MDI_LIBRARY_BOOKS).apply {
                    addClass("icon-xl")
                }
            }
            label(FX.messages["createProjectMessageTitle"]) {
                addClass("h4", "h4--80")
            }
            region { addClass("line-break") }
            label(FX.messages["createProjectMessageBody"]) {
                addClass("normal-text")
            }
        }
    }
}

fun EventTarget.emptyProjectSection(op: EmptyProjectSection.() -> Unit = {}) = EmptyProjectSection().attachTo(this, op)

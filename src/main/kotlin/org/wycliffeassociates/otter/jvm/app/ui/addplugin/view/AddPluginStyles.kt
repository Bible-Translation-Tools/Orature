package org.wycliffeassociates.otter.jvm.app.ui.addplugin.view

import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*

class AddPluginStyles : Stylesheet() {
    companion object {
        val saveButton by cssclass()
        val jfxCheckBox by cssclass("jfx-check-box")
        val jfxTextField by cssclass("jfx-text-field")
    }

    init {
        form {
            backgroundColor += c(Colors["base"])
        }
        button {
            unsafe("-jfx-button-type", raw("FLAT"))
            backgroundColor += c(Colors["baseMedium"])
            fontWeight = FontWeight.BOLD
            textFill = c(Colors["primary"])
            padding = box(5.px, 10.px)
            and(saveButton) {
                textFill = c(Colors["base"])
                backgroundColor += c(Colors["primary"])
                unsafe("-jfx-button-type", raw("RAISED"))
                and(disabled) {
                    unsafe("-jfx-button-type", raw("FLAT"))
                }
            }
        }
        jfxCheckBox {
            unsafe("-jfx-checked-color", raw(Colors["primary"]))
        }
        jfxTextField {
            unsafe("-jfx-focus-color", raw(Colors["primary"]))
            padding = box(15.px, 0.px, 0.px, 0.px)
        }
    }
}
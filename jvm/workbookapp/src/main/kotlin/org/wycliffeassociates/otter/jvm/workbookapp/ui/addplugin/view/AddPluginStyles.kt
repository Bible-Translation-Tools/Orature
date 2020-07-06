package org.wycliffeassociates.otter.jvm.workbookapp.ui.addplugin.view

import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import tornadofx.*

class AddPluginStyles : Stylesheet() {
    companion object {
        val saveButton by cssclass()
        val browseButton by cssclass()
        val jfxCheckBox by cssclass("jfx-check-box")
        val jfxTextField by cssclass("jfx-text-field")
    }

    init {
        form {
            backgroundColor += AppTheme.colors.defaultBackground
            prefWidth = 500.px
        }
        button {
            and(saveButton, browseButton) {
                unsafe("-jfx-button-type", raw("FLAT"))
                backgroundColor += AppTheme.colors.colorlessButton
                fontWeight = FontWeight.BOLD
                textFill = AppTheme.colors.appRed
                padding = box(5.px, 10.px)
                and(saveButton) {
                    textFill = AppTheme.colors.white
                    backgroundColor += AppTheme.colors.appRed
                    unsafe("-jfx-button-type", raw("RAISED"))
                    and(disabled) {
                        unsafe("-jfx-button-type", raw("FLAT"))
                    }
                }
            }
        }
        jfxCheckBox {
            unsafe("-jfx-checked-color", AppTheme.colors.appRed.css)
            textFill = AppTheme.colors.defaultText
        }
        jfxTextField {
            unsafe("-jfx-focus-color", AppTheme.colors.appRed.css)
            padding = box(15.px, 0.px, 0.px, 0.px)
            textFill = AppTheme.colors.defaultText
        }
    }
}

package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import tornadofx.*
import tornadofx.WizardStyles.Companion.wizard


class ProjectWizardStyles : Stylesheet() {

    companion object {
        val wizardCard by cssclass()
        val wizardCardGraphicsContainer by cssclass()
        val noResource by cssclass()
        val filterableComboBox by cssclass()
        val wizardButton by cssclass()
    }

    init {
        wizard {
            backgroundColor += c(UIColorsObject.Colors["base"])
        }

        wizardCard {
            prefWidth = 280.px
            prefHeight = 300.px
            backgroundColor += c(Colors["base"])
            padding = box(10.px)
            backgroundRadius += box(10.px)
            spacing = 10.px
            wizardCardGraphicsContainer {
                backgroundRadius += box(10.px)
                backgroundColor += c(Colors["base"])
            }
            label {
                textFill = Color.BLACK
                fontWeight = FontWeight.BOLD
                fontSize = 16.px
            }
            s(".jfx-button") {
                minHeight = 40.px
                maxWidth = Double.MAX_VALUE.px
                backgroundColor += c(Colors["primary"])
                textFill = c(Colors["base"])
                cursor = Cursor.HAND
                fontSize = 16.px
                fontWeight = FontWeight.BOLD
            }
        }

        noResource {
            padding = box(50.px)
            backgroundColor += c(Colors["base"])
            fontSize = 24.px
            fontWeight = FontWeight.BOLD
            textFill = c(Colors["primary"])
        }

        filterableComboBox {
            backgroundColor += Color.TRANSPARENT
            borderColor += box(null, null, c(UIColorsObject.Colors["primary"]), null)
            borderWidth += box(0.px, 0.px, 2.px, 0.px)
            minWidth = 350.px
            child(".text-input") {
                backgroundColor += Color.TRANSPARENT
            }
            child(".arrow-button") {
                backgroundColor += Color.TRANSPARENT
                child(".arrow") {
                    visibility = FXVisibility.HIDDEN
                }
            }
        }

        wizardButton {
            prefHeight = 40.0.px
            prefWidth = 120.0.px
            backgroundColor += c(Colors["primary"])
            textFill = c(Colors["base"])
            cursor = Cursor.HAND
        }
    }


}
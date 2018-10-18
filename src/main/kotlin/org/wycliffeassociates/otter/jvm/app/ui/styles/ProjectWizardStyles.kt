package org.wycliffeassociates.otter.jvm.app.ui.styles

import javafx.scene.Cursor
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.widgets.WidgetsStyles
import org.wycliffeassociates.otter.jvm.app.widgets.progressstepper.DefaultProgressStepperStylesheet.Companion.completed
import tornadofx.*
import tornadofx.WizardStyles.Companion.wizard


class ProjectWizardStyles : Stylesheet() {

    companion object {
        val selectedCard by cssclass()
        val unselectedCard by cssclass()
        val stepper by cssclass()
        val filterableComboBox by cssclass()
        val wizardButton by cssclass()
        val nextButton by cssclass()
    }

    init {
        wizard {
            backgroundColor += c(UIColorsObject.Colors["base"])
        }

        selectedCard {
            prefHeight = 364.0.px
            prefWidth = 364.0.px
            backgroundRadius += box(12.0.px)
            backgroundColor += c(UIColorsObject.Colors["primary"])
            textFill = c(UIColorsObject.Colors["base"])
            fontSize = 24.px
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND
        }

        unselectedCard {
            prefHeight = 364.0.px
            prefWidth = 364.0.px
            backgroundRadius += box(12.0.px)
            backgroundColor += c(UIColorsObject.Colors["base"])
            textFill = c(UIColorsObject.Colors["primary"])
            fontSize = 24.px
            effect = DropShadow(10.0, Color.GRAY)
            cursor = Cursor.HAND
        }

        stepper {
            line {
                and(completed) {
                    stroke = c(UIColorsObject.Colors["primary"])
                }
            }

            button {
                backgroundColor += c(UIColorsObject.Colors["base"])
                borderColor += box(c(UIColorsObject.Colors["primary"]))
                child("*") {
                    fill = c(UIColorsObject.Colors["primary"])
                }
                and(completed) {
                    borderColor += box(Color.TRANSPARENT)
                    backgroundColor += c(UIColorsObject.Colors["primary"])
                    and(hover) {
                        backgroundColor += c(UIColorsObject.Colors["primaryShade"])
                    }
                }
                and(hover) {
                    backgroundColor += c(UIColorsObject.Colors["primary"])
                }
            }
        }

        filterableComboBox {
            backgroundColor += Color.TRANSPARENT
            borderColor += box(null, null, c(UIColorsObject.Colors["primary"]), null)
            borderWidth += box(0.px, 0.px, 2.px, 0.px)
            prefWidth = 250.px
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

        nextButton {
            prefHeight = 40.0.px
            prefWidth = 100.0.px
            backgroundColor += c(Colors["primary"])
            textFill = c(Colors["base"])
            cursor = Cursor.HAND
        }
    }


}
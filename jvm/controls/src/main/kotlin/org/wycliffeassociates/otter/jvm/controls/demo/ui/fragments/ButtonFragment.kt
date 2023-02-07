package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class ButtonFragment : Fragment() {
    override val root = stackpane {
        vbox {
            spacing = 10.0
            alignment = Pos.CENTER

            button("Primary") {
                addClass("btn", "btn--primary")
            }
            button("Secondary") {
                addClass("btn", "btn--secondary")
            }
            button("Borderless") {
                addClass("btn", "btn--secondary", "btn--borderless")
            }
            button("Call to Action") {
                addClass("btn", "btn--cta")
            }
            button("With Icon") {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ACCOUNT)
            }
            button("Disabled") {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ACCOUNT)
                isDisable = true
            }
        }
    }
}
package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.geometry.Pos
import tornadofx.*
import tornadofx.FX.Companion.stylesheets

class TitleFragment : Fragment() {

    override val root = vbox {
        alignment = Pos.CENTER
        styleClass.add("vm-header")
        text {
            styleClass.add("vm-header__title")
        }
        text {
            styleClass.add("vm-header__subtitle")
        }
    }
}
